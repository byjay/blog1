package com.example.engine

import com.example.model.AuctionCaseIntel

/**
 * Strips and detects debtor/owner/tenant personal names, masked surnames (e.g. 박OO),
 * exact personal move-in dates, phone numbers, RRNs, and emails so NO personal data
 * is ever published to blogs or infographics.
 */
object PrivacySanitizerEngine {

    private val NON_NAME_LEGAL_WORDS = setOf(
        "겸", "법인", "본인", "없음", "유무", "대항력", "보증금", "현황", "권리",
        "명세", "점유", "미확인", "퇴거", "배당", "진술", "부담", "존재", "세대",
        "보호", "관계", "여부", "내역", "조사", "기준", "확인", "승계", "차감",
        "인수", "전원", "단독", "공동", "신고", "미신", "유지", "자격", "세대주",
        "임차", "소유", "채무", "명의", "대상", "확인서", "전입", "확정", "전체"
    )

    private val MASKED_SURNAME_REGEX = Regex("""[가-힣]{1}(?:OO|XX|○○|\*\*|O\*)""")
    private val MASKED_PREFIX_COMPANY_REGEX = Regex("""(?<![A-Za-z0-9])(?:OO|XX|○○)[가-힣]{1,6}""")
    private val ROLE_WITH_NAME_REGEX = Regex(
        """(채무자|소유자|임차인|세입자|점유자|신고인|대표자|보증인|신청인)\s+([가-힣]{2,4})(?=\s*(?:점유|소유|명의|거주|실거주|씨|님|외|세대|보증|전입|확정|미배당|대항|부동산|아파트|주택|빌라|다세대|상가|토지|공장|의|,|\.|·|\)|$))"""
    )
    private val PHONE_REGEX = Regex("""(?<!\d)01[016789][-\s]?\d{3,4}[-\s]?\d{4}(?!\d)""")
    private val RRN_REGEX = Regex("""(?<!\d)\d{6}[-\s]?[1-4]\d{6}(?!\d)""")
    private val EMAIL_REGEX = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
    private val EXACT_MOVEIN_DATE_REGEX = Regex("""(전입(?:일자?)?|확정(?:일자?)?)\s*[:：]?\s*\d{4}[-./]\d{2}[-./]\d{2}""")

    fun sanitizeForPublicBlog(raw: String): String {
        if (raw.isBlank()) return raw
        var cleaned = raw

        // 1. Replace explicit role + Korean personal name unless it's a known legal noun
        cleaned = ROLE_WITH_NAME_REGEX.replace(cleaned) { match ->
            val role = match.groupValues[1]
            val candidate = match.groupValues[2]
            if (candidate in NON_NAME_LEGAL_WORDS || NON_NAME_LEGAL_WORDS.any { candidate.startsWith(it) }) {
                match.value
            } else {
                "$role(비식별)"
            }
        }

        // 2. Replace partially masked names (e.g. 박OO, 김XX) & masked entity names (e.g. OO기계설비)
        cleaned = MASKED_SURNAME_REGEX.replace(cleaned, "[당사자 비식별]")
        cleaned = MASKED_PREFIX_COMPANY_REGEX.replace(cleaned, "신고업체(비식별)")

        // 3. Mask exact household move-in / fixed confirmation dates
        cleaned = EXACT_MOVEIN_DATE_REGEX.replace(cleaned) { m ->
            "${m.groupValues[1]}(상세일자 비공개)"
        }

        // 4. Mask phone numbers, RRNs, personal emails
        cleaned = PHONE_REGEX.replace(cleaned, "[연락처 비공개]")
        cleaned = RRN_REGEX.replace(cleaned, "[식별번호 비공개]")
        cleaned = EMAIL_REGEX.replace(cleaned, "[이메일 비공개]")

        return cleaned
    }

    fun sanitizeAuctionForBlog(auction: AuctionCaseIntel): AuctionCaseIntel = auction.copy(
        possessionSummary = sanitizeForPublicBlog(auction.possessionSummary),
        tenantSummary = sanitizeForPublicBlog(auction.tenantSummary),
        assumedRightsText = sanitizeForPublicBlog(auction.assumedRightsText),
        specialCheckNotice = sanitizeForPublicBlog(auction.specialCheckNotice)
    )

    fun detectPersonalInfoLeaks(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        val leaks = mutableListOf<String>()

        ROLE_WITH_NAME_REGEX.findAll(text).forEach { match ->
            val candidate = match.groupValues[2]
            if (candidate !in NON_NAME_LEGAL_WORDS && NON_NAME_LEGAL_WORDS.none { candidate.startsWith(it) }) {
                leaks += "실명 노출 감지(${match.groupValues[1]} 성명)"
            }
        }
        if (MASKED_SURNAME_REGEX.containsMatchIn(text) || MASKED_PREFIX_COMPANY_REGEX.containsMatchIn(text)) {
            leaks += "성씨·상호 일부 마스킹 표기(OO/XX) 감지 — 완전 비식별 권장"
        }
        if (EXACT_MOVEIN_DATE_REGEX.containsMatchIn(text)) {
            leaks += "개별 임차인 상세 전입·확정일자 노출 감지"
        }
        if (PHONE_REGEX.containsMatchIn(text)) {
            leaks += "개인 전화번호 노출 감지"
        }
        if (RRN_REGEX.containsMatchIn(text)) {
            leaks += "주민등록번호 패턴 노출 감지"
        }
        if (EMAIL_REGEX.containsMatchIn(text)) {
            leaks += "개인 이메일 노출 감지"
        }
        return leaks.distinct()
    }
}
