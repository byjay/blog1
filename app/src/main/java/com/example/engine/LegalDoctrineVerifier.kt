package com.example.engine

import com.example.model.AuctionCaseIntel
import com.example.model.LegalDoctrineCheckItem
import com.example.model.LegalDoctrineReport
import com.example.model.LegalVerificationDomain
import com.example.model.LegalVerificationSeverity
import com.example.model.PropertyType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Precedent- and statute-grounded Korean auction legal-doctrine verifier.
 * Checks tenant priority dividends, 2001다8677 management fee succession,
 * post-seizure lien invalidity, statutory superficies, special property statutes,
 * and 4-court-document contradictions.
 */
object LegalDoctrineVerifier {

    private val DATE_REGEX = Regex("""^\d{4}-\d{2}-\d{2}$""")
    private val KRW_EOK_MAN_REGEX = Regex("""(\d[\d,]*)\s*억(?:\s*(\d[\d,]*)\s*만)?\s*원?""")
    private val KRW_MAN_ONLY_REGEX = Regex("""(\d[\d,]*)\s*만\s*원?""")

    private data class SmallTenantBracket(
        val zoneLabel: String,
        val depositCapKrw: Long,
        val priorityRefundCapKrw: Long
    )

    fun verify(auction: AuctionCaseIntel): LegalDoctrineReport {
        val checks = mutableListOf<LegalDoctrineCheckItem>()
        var totalLegalBurden = 0L

        // 1. 말소기준권리 & 소멸/인수 판정 검증
        val baseDateValid = DATE_REGEX.matches(auction.baseRightDate.trim())
        val trimmedDiv = auction.dividendDeadline.trim()
        val divDateValid = trimmedDiv.isEmpty() || DATE_REGEX.matches(trimmedDiv)
        val baseCheckPassed = baseDateValid && divDateValid
        val divMissingOnly = baseDateValid && trimmedDiv.isEmpty()
        checks += LegalDoctrineCheckItem(
            id = "legal-01",
            domain = LegalVerificationDomain.BASE_RIGHT_CANCELLATION,
            title = "말소기준권리일·배당요구종기 기준선 검증",
            statuteOrPrecedent = "민사집행법 제91조(인수주의·소멸주의)",
            severity = when {
                !baseCheckPassed -> LegalVerificationSeverity.CRITICAL_BLOCK
                divMissingOnly -> LegalVerificationSeverity.CAUTION_NOTE
                else -> LegalVerificationSeverity.VERIFIED_PASS
            },
            passed = baseCheckPassed,
            estimatedBuyerBurdenKrw = 0L,
            findingDetail = when {
                !baseCheckPassed ->
                    "말소기준권리('${auction.baseRightDate}') 또는 배당종기('${auction.dividendDeadline}') 형식 누락 — 권리 소멸 판단 불가"
                divMissingOnly ->
                    "말소기준권리(${auction.baseRightDate}) 일자 유효 · 배당요구종기 서버 미기재(매각물건명세서 원문 확인 권장)"
                else ->
                    "말소기준권리(${auction.baseRightDate}) 및 배당요구종기(${auction.dividendDeadline}) 일자 유효 — 기준일 이후 후순위 담보·가압류 소멸 판정 일관됨"
            },
            actionGuidance = if (baseCheckPassed) {
                "최선순위 근저당·가압류 설정일보다 앞선 등기·전입 존재 여부 및 배당요구종기 도과 여부 최종 확인"
            } else {
                "등기부등본 최선순위 담보·가압류 일자(YYYY-MM-DD)를 정확히 입력하세요."
            }
        )

        // 2. 임차인 대항력·확정일자·소액임차인 최우선변제 검증
        val bracket = resolveSmallTenantBracket2023(auction.address)
        val combinedTenantAndRights = "${auction.possessionSummary}\n${auction.tenantSummary}\n${auction.assumedRightsText}"
        val statedTenantDeposit = extractFirstKrwAmount(auction.tenantSummary)
            ?: extractFirstKrwAmount(auction.assumedRightsText)
            ?: 0L

        val hasSeniorTenantSignal = combinedTenantAndRights.contains("선순위") &&
            (combinedTenantAndRights.contains("임차") || combinedTenantAndRights.contains("대항력"))
        val tenantNoDividendDemand = combinedTenantAndRights.contains("배당요구 미신청") ||
            combinedTenantAndRights.contains("배당요구 없음")
        val assumedClaimsZeroRisk = auction.assumedRightsText.contains("인수 없음") ||
            auction.assumedRightsText.contains("인수분 없음") ||
            auction.assumedRightsText.contains("전부 소멸")

        val tenantContradiction = hasSeniorTenantSignal && tenantNoDividendDemand && assumedClaimsZeroRisk
        val tenantAssumedBurden = when {
            hasSeniorTenantSignal && tenantNoDividendDemand -> statedTenantDeposit.coerceAtLeast(50_000_000L)
            hasSeniorTenantSignal && !combinedTenantAndRights.contains("전액 배당") -> (statedTenantDeposit / 2L).coerceAtLeast(0L)
            else -> 0L
        }
        totalLegalBurden += tenantAssumedBurden

        val halfBidCap = auction.minBidPriceKrw / 2L
        val smallTenantCapApplied = minOf(bracket.priorityRefundCapKrw, halfBidCap)
        val tenantCheckTitle = if (auction.propertyType == PropertyType.COMMERCIAL_RETAIL) {
            "상가임대차보호법 환산보증금·대항력·배당순위 검증"
        } else {
            "주택임대차보호법 대항력·소액임차인 최우선변제 검증"
        }
        val tenantPrecedent = if (auction.propertyType == PropertyType.COMMERCIAL_RETAIL) {
            "상임법 제2조 환산보증금(보증금+월세×100) & 제3조 대항력"
        } else {
            "주임법 시행령(2023.2.21 개정) ${bracket.zoneLabel} 기준"
        }

        checks += LegalDoctrineCheckItem(
            id = "legal-02",
            domain = LegalVerificationDomain.TENANT_PRIORITY_DIVIDEND,
            title = tenantCheckTitle,
            statuteOrPrecedent = tenantPrecedent,
            severity = when {
                tenantContradiction -> LegalVerificationSeverity.CRITICAL_BLOCK
                tenantAssumedBurden > 0L -> LegalVerificationSeverity.CAUTION_NOTE
                else -> LegalVerificationSeverity.VERIFIED_PASS
            },
            passed = !tenantContradiction,
            estimatedBuyerBurdenKrw = tenantAssumedBurden,
            findingDetail = when {
                tenantContradiction -> "법리 모순 차단: 임차현황에 선순위 임차인 배당요구 미신청이 기재되었으나 인수 요약이 '소멸/인수 없음'으로 상충됨"
                tenantAssumedBurden > 0L -> "선순위 임차인 미배당 예상 인수액 ${QaVerificationEngine.formatKrwShort(tenantAssumedBurden)} 반영 완료 · ${bracket.zoneLabel} 소액보증금 ${QaVerificationEngine.formatKrwShort(bracket.depositCapKrw)} 이하 시 최우선 ${QaVerificationEngine.formatKrwShort(smallTenantCapApplied)}(매각대금 1/2 한도)"
                else -> "${bracket.zoneLabel} 소액최우선 기준(보증금 ${QaVerificationEngine.formatKrwShort(bracket.depositCapKrw)} 이하 / 최우선 ${QaVerificationEngine.formatKrwShort(smallTenantCapApplied)}, 최초 근저당 설정일 기준) 적용 · 선순위 미배당 인수 위험 없음"
            },
            actionGuidance = if (tenantContradiction) {
                "인수권리 요약에 선순위 미배당 보증금 인수액을 명시하고 입찰가에서 차감하세요."
            } else {
                "최우선변제 기준일은 임대차계약일이 아니라 최선순위 근저당(${auction.baseRightDate}) 설정일 기준으로 판정"
            }
        )

        // 3. 공용관리비 승계 판례 검증 (대법원 2001다8677 전원합의체)
        val hasCollectiveBuilding = auction.propertyType in setOf(
            PropertyType.APT_OFFICETEL,
            PropertyType.VILLA_MULTIFAMILY,
            PropertyType.COMMERCIAL_RETAIL
        )
        val extractedMgmtFee = if (combinedTenantAndRights.contains("관리비")) {
            extractKrwNearKeyword(combinedTenantAndRights, "관리비") ?: 0L
        } else 0L
        val feeContradiction = combinedTenantAndRights.contains("연체료 포함") &&
            combinedTenantAndRights.contains("승계") &&
            !combinedTenantAndRights.contains("제외")
        val legalMgmtBurden = if (hasCollectiveBuilding) {
            if (extractedMgmtFee > 0L) extractedMgmtFee else when (auction.propertyType) {
                PropertyType.COMMERCIAL_RETAIL -> 4_500_000L
                PropertyType.APT_OFFICETEL -> 1_800_000L
                else -> 1_200_000L
            }
        } else 0L
        totalLegalBurden += legalMgmtBurden

        checks += LegalDoctrineCheckItem(
            id = "legal-03",
            domain = LegalVerificationDomain.MANAGEMENT_FEE_2001DA8677,
            title = "체납 관리비 공용부분 원금 승계 범위 검증",
            statuteOrPrecedent = "대법원 2001.9.20. 선고 2001다8677 전원합의체",
            severity = when {
                feeContradiction -> LegalVerificationSeverity.CRITICAL_BLOCK
                hasCollectiveBuilding && legalMgmtBurden > 0L -> LegalVerificationSeverity.CAUTION_NOTE
                else -> LegalVerificationSeverity.VERIFIED_PASS
            },
            passed = !feeContradiction,
            estimatedBuyerBurdenKrw = legalMgmtBurden,
            findingDetail = when {
                feeContradiction -> "판례 위배 모순: 전유부분 사용료 또는 연체료까지 승계 대상으로 잘못 표기됨(2001다8677 판례상 연체료·전유부분은 승계 제외)"
                hasCollectiveBuilding -> "집합건물 공용부분 체납 원금(3년 소멸시효 내)만 특별승계 추정 ${QaVerificationEngine.formatKrwShort(legalMgmtBurden)} 반영 · 전유(전기·수도)·연체료 제외 원칙 적용"
                else -> "비집합건물(토지·단독공장)로 공동주택 체납관리비 승계 리스크 해당 없음"
            },
            actionGuidance = "잔금 전 관리사무소에 공용부분 원금과 전유부분·연체료 분리 명세서 서면 요청 필수"
        )

        // 4. 유치권 성립요건·압류 후 취득 대항 불가 검증
        val mentionsLien = combinedTenantAndRights.contains("유치권")
        val lienPostSeizure = mentionsLien && (
            combinedTenantAndRights.contains("말소기준권리 이후") ||
                combinedTenantAndRights.contains("압류 이후") ||
                combinedTenantAndRights.contains("경매개시결정 이후")
            )
        val lienContradiction = mentionsLien && !lienPostSeizure && assumedClaimsZeroRisk
        val lienBurden = when {
            mentionsLien && !lienPostSeizure -> extractKrwNearKeyword(combinedTenantAndRights, "유치권") ?: 50_000_000L
            else -> 0L
        }
        totalLegalBurden += lienBurden

        checks += LegalDoctrineCheckItem(
            id = "legal-04",
            domain = LegalVerificationDomain.LIEN_AFTER_SEIZURE,
            title = "유치권 견련관계 · 압류(기입등기) 선후 대항력 검증",
            statuteOrPrecedent = "민법 제320조 · 대법원 압류 효력 후 유치권 대항 불가 판례",
            severity = when {
                lienContradiction -> LegalVerificationSeverity.CRITICAL_BLOCK
                mentionsLien && !lienPostSeizure -> LegalVerificationSeverity.CAUTION_NOTE
                else -> LegalVerificationSeverity.VERIFIED_PASS
            },
            passed = !lienContradiction,
            estimatedBuyerBurdenKrw = lienBurden,
            findingDetail = when {
                lienContradiction -> "법리 모순 차단: 압류 전 성립 가능성이 있는 유치권 신고가 존재하나 인수 요약에 '인수 없음'으로 단정됨"
                lienPostSeizure -> "유치권 신고가 존재하나 점유·채권 취득이 압류(말소기준권리) 이후로 기재되어 매수인 대항 불가(명도·배제 소송 우위) 판정"
                mentionsLien -> "유치권 신고액 ${QaVerificationEngine.formatKrwShort(lienBurden)} 성립요건(압류 전 적법 점유·견련관계·변제기) 예비 충당 반영"
                else -> "유치권 신고 이력 없음 (현장 임장 시 점유 공사표지판 여부만 보조 확인)"
            },
            actionGuidance = "임장 시 실제 점유 주체·공사계약서·세금계산서·점유 개시 입증자료가 압류등기 전인지 확인"
        )

        // 5. 법정지상권·관습법상 법정지상권 성립 검증
        val mentionsSuperficies = combinedTenantAndRights.contains("법정지상권") ||
            combinedTenantAndRights.contains("제시외") ||
            combinedTenantAndRights.contains("토지 별도") ||
            combinedTenantAndRights.contains("지분")
        val superficiesRiskPresent = combinedTenantAndRights.contains("성립 여지") ||
            combinedTenantAndRights.contains("토지만 매각") ||
            combinedTenantAndRights.contains("건물만 매각")
        val superficiesContradiction = superficiesRiskPresent && assumedClaimsZeroRisk

        checks += LegalDoctrineCheckItem(
            id = "legal-05",
            domain = LegalVerificationDomain.STATUTORY_SUPERFICIES,
            title = "법정지상권(민법 제366조) · 관습법상 기준시점 검증",
            statuteOrPrecedent = "민법 제366조 · 대법원 2012.6.18. 전원합의체(강제경매 압류시점 기준)",
            severity = when {
                superficiesContradiction -> LegalVerificationSeverity.CRITICAL_BLOCK
                mentionsSuperficies -> LegalVerificationSeverity.CAUTION_NOTE
                else -> LegalVerificationSeverity.VERIFIED_PASS
            },
            passed = !superficiesContradiction,
            estimatedBuyerBurdenKrw = 0L,
            findingDetail = when {
                superficiesContradiction -> "법리 모순 차단: 토지·건물 분리 매각 또는 법정지상권 성립 여지가 있으나 인수 위험이 누락됨"
                mentionsSuperficies -> "제시외·지상권 체크 항목 감지 — 임의경매는 저당권 설정 시, 강제경매는 압류(선행가압류) 시 동일인 소유 여부 확인 필요"
                else -> "토지·건물 일괄매각 또는 집합건물 대지권 포함 물건으로 법정지상권 성립 모순 없음"
            },
            actionGuidance = "건축물대장과 토지등기부의 소유자 변동 시점을 저당권·압류 설정일과 1:1 대조"
        )

        // 6. 물건유형별 필수 법리 요건 & 4대 공부 수치 모순 탐지
        val numericContradiction = auction.appraisalPriceKrw <= 0L ||
            auction.minBidPriceKrw <= 0L ||
            auction.minBidPriceKrw > auction.appraisalPriceKrw ||
            auction.conservativeBidKrw > auction.balancedBidKrw ||
            auction.balancedBidKrw > auction.aggressiveBidKrw

        val hasFarmlandParcelSignal = auction.propertyType == PropertyType.LAND_FARMLAND &&
            Regex("""(?:지목\s*(?:전|답|과수원)|\((?:전|답|과수원)|농지|휴경지)""")
                .containsMatchIn("${auction.address}\n${auction.possessionSummary}") &&
            !auction.possessionSummary.contains("임야화")

        val missingFarmlandRule = hasFarmlandParcelSignal &&
            !auction.specialCheckNotice.contains("농지취득자격증명") &&
            !auction.specialCheckNotice.contains("농취증") &&
            !auction.assumedRightsText.contains("농취증") &&
            !auction.assumedRightsText.contains("농지취득자격증명")

        val specialDocFailed = numericContradiction || missingFarmlandRule
        checks += LegalDoctrineCheckItem(
            id = "legal-06",
            domain = LegalVerificationDomain.SPECIAL_STATUTORY_AND_DOCS,
            title = "4대 공부 수치 정합성 & 물건유형별 강행규정 검증",
            statuteOrPrecedent = when (auction.propertyType) {
                PropertyType.LAND_FARMLAND -> "농지법 제8조(농취증 미제출 보증금 몰수) · 건축법 접도요건"
                PropertyType.FACTORY_SPECIAL -> "공장 및 광업재단 저당법 제6조(기계기구 일괄경매)"
                else -> "민사집행법 매각물건명세서·감정평가서 정합성 원칙"
            },
            severity = if (specialDocFailed) LegalVerificationSeverity.CRITICAL_BLOCK else LegalVerificationSeverity.VERIFIED_PASS,
            passed = !specialDocFailed,
            estimatedBuyerBurdenKrw = 0L,
            findingDetail = when {
                numericContradiction -> "수치 모순 차단: 감정가(${auction.appraisalPriceKrw})·최저가(${auction.minBidPriceKrw}) 또는 3전략 입찰가 서열이 역전됨"
                missingFarmlandRule -> "농지 물건 필수 요건 누락: 농지취득자격증명원 미제출 시 보증금 몰수 경고가 특이체크에서 누락됨"
                else -> "감정가 > 최저가, 보수<=균형<=공격 입찰가 서열 및 ${auction.propertyType.displayLabel} 특이 법리 요건 통과"
            },
            actionGuidance = "매각결정기일 전 관할법원 매각물건명세서 최종 수정 여부 및 강행 서류 제출기한 준수"
        )

        val criticalCount = checks.count { it.severity == LegalVerificationSeverity.CRITICAL_BLOCK }
        val cautionCount = checks.count { it.severity == LegalVerificationSeverity.CAUTION_NOTE }
        val overallPassed = criticalCount == 0
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())
        val summary = if (overallPassed) {
            "6대 법리·판례 검증 PASS (모순 0건 · 유의 ${cautionCount}건 · 법리 예상부담 ${QaVerificationEngine.formatKrwShort(totalLegalBurden)})"
        } else {
            "법리 모순 ${criticalCount}건 적발 → QA 발행 차단 (총 예상부담 ${QaVerificationEngine.formatKrwShort(totalLegalBurden)})"
        }

        return LegalDoctrineReport(
            caseId = auction.caseId,
            overallPassed = overallPassed,
            criticalBlockCount = criticalCount,
            cautionCount = cautionCount,
            totalEstimatedLegalBurdenKrw = totalLegalBurden,
            checks = checks,
            auditSummary = summary,
            verifiedAt = now
        )
    }

    private fun resolveSmallTenantBracket2023(address: String): SmallTenantBracket = when {
        address.contains("서울") -> SmallTenantBracket("서울특별시", 165_000_000L, 55_000_000L)
        listOf("수원", "성남", "용인", "화성", "김포", "세종", "고양", "과천", "광명", "구리", "군포", "부천", "안양", "의왕", "의정부", "하남", "인천")
            .any { address.contains(it) } -> SmallTenantBracket("과밀억제·세종·용인·화성·김포", 145_000_000L, 48_000_000L)
        listOf("부산", "대구", "대전", "광주", "울산", "안산", "파주", "이천", "평택")
            .any { address.contains(it) } -> SmallTenantBracket("광역시·안산·파주·이천·평택", 85_000_000L, 28_000_000L)
        else -> SmallTenantBracket("그 밖의 지역", 75_000_000L, 25_000_000L)
    }

    private fun extractKrwNearKeyword(text: String, keyword: String): Long? {
        val idx = text.indexOf(keyword)
        if (idx < 0) return null
        val slice = text.substring(idx, minOf(text.length, idx + 48))
        return extractFirstKrwAmount(slice)
    }

    fun extractFirstKrwAmount(text: String): Long? {
        val eokMatch = KRW_EOK_MAN_REGEX.find(text)
        if (eokMatch != null) {
            val eok = eokMatch.groupValues[1].replace(",", "").toLongOrNull() ?: 0L
            val man = eokMatch.groupValues[2].replace(",", "").toLongOrNull() ?: 0L
            return eok * 100_000_000L + man * 10_000L
        }
        val manMatch = KRW_MAN_ONLY_REGEX.find(text)
        if (manMatch != null) {
            val man = manMatch.groupValues[1].replace(",", "").toLongOrNull() ?: return null
            return man * 10_000L
        }
        return null
    }
}
