package com.example.engine

import com.example.model.AuctionCaseIntel
import com.example.model.ChannelPlatform
import com.example.model.ChannelVariantContent
import com.example.model.QaAuditReport
import com.example.model.ServerAutomationConfig
import java.security.MessageDigest
import kotlin.math.roundToInt
import kotlin.math.sqrt

object QaVerificationEngine {

    private val FORBIDDEN_SPAM_TERMS = listOf(
        "100% 무조건 수익",
        "원금 보장",
        "무조건 낙찰",
        "확정 수익",
        "100% 수익 보장",
        "undefined",
        "NaN",
        "null"
    )

    private val URL_REGEX = Regex("""https?://[^\s"'<>]+""", RegexOption.IGNORE_CASE)

    fun auditSlot(
        auctionCase: AuctionCaseIntel,
        variants: Map<ChannelPlatform, ChannelVariantContent>,
        scheduledTimes: Map<ChannelPlatform, String>,
        config: ServerAutomationConfig,
        recent7DayCorpus: List<String> = emptyList()
    ): QaAuditReport {
        val cf = variants[ChannelPlatform.CLOUDFLARE]
        val ts = variants[ChannelPlatform.TISTORY]
        val nv = variants[ChannelPlatform.NAVER]

        val cfText = cf?.bodyText.orEmpty()
        val tsText = ts?.bodyText.orEmpty()
        val nvText = nv?.bodyText.orEmpty()

        val pairCosine = linkedMapOf<String, Double>()
        val pairJaccard = linkedMapOf<String, Double>()

        pairCosine["CF ↔ TS"] = cosineSimilarityPct(cfText, tsText)
        pairCosine["TS ↔ NV"] = cosineSimilarityPct(tsText, nvText)
        pairCosine["CF ↔ NV"] = cosineSimilarityPct(cfText, nvText)

        pairJaccard["CF ↔ TS"] = jaccard5GramPct(cfText, tsText)
        pairJaccard["TS ↔ NV"] = jaccard5GramPct(tsText, nvText)
        pairJaccard["CF ↔ NV"] = jaccard5GramPct(cfText, nvText)

        val maxCosine = pairCosine.values.maxOrNull() ?: 0.0
        val maxJaccard = pairJaccard.values.maxOrNull() ?: 0.0

        val recentMax = if (recent7DayCorpus.isEmpty()) {
            0.0
        } else {
            listOf(cfText, tsText, nvText)
                .filter { it.isNotBlank() }
                .maxOfOrNull { body ->
                    recent7DayCorpus.maxOfOrNull { old -> cosineSimilarityPct(body, old) } ?: 0.0
                } ?: 0.0
        }

        val similarityPassed = maxCosine < 30.0 && maxJaccard < 30.0 && recentMax < 30.0

        // 0% Fact-Check across all channels
        val mismatches = mutableListOf<String>()
        val formattedAppraisal = formatKrwShort(auctionCase.appraisalPriceKrw)
        val formattedMinBid = formatKrwShort(auctionCase.minBidPriceKrw)

        variants.forEach { (platform, variant) ->
            val combined = "${variant.title}\n${variant.bodyText}"
            if (!combined.contains(auctionCase.caseId)) {
                mismatches += "${platform.displayName}: 사건번호(${auctionCase.caseId}) 누락"
            }
            if (!containsPriceSignal(combined, auctionCase.appraisalPriceKrw, formattedAppraisal)) {
                mismatches += "${platform.displayName}: 감정가($formattedAppraisal) 불일치 또는 누락"
            }
            if (!containsPriceSignal(combined, auctionCase.minBidPriceKrw, formattedMinBid)) {
                mismatches += "${platform.displayName}: 최저매각가($formattedMinBid) 불일치 또는 누락"
            }
            if (!combined.contains(auctionCase.baseRightDate)) {
                mismatches += "${platform.displayName}: 말소기준권리(${auctionCase.baseRightDate}) 누락"
            }
        }
        val factCheckPassed = mismatches.isEmpty()

        // Forbidden terms, Personal Information (PII), and Naver Quality Defense Gate
        val forbiddenHits = mutableListOf<String>()
        var qualityDefenseTotalFlags = 0
        variants.forEach { (platform, variant) ->
            val combined = "${variant.title}\n${variant.bodyText}\n${variant.bodyHtml}"
            FORBIDDEN_SPAM_TERMS.forEach { term ->
                val regex = if (term in listOf("null", "NaN", "undefined")) {
                    Regex("""\b$term\b""")
                } else {
                    Regex(Regex.escape(term), RegexOption.IGNORE_CASE)
                }
                if (regex.containsMatchIn(combined)) {
                    forbiddenHits += "${platform.displayName}: 금칙어 '$term'"
                }
            }
            val piiScopeText = buildString {
                appendLine(variant.title)
                appendLine(variant.bodyText)
                appendLine(variant.pinnedCommentSnippet)
                variant.infographicCards.forEach { card ->
                    appendLine(card.headline)
                    appendLine(card.altText)
                }
            }
            PrivacySanitizerEngine.detectPersonalInfoLeaks(piiScopeText).forEach { leak ->
                forbiddenHits += "${platform.displayName}: [개인정보] $leak"
            }

            if (platform == ChannelPlatform.NAVER) {
                val defenseHits = QualityDefenseEngine.detectIssues(variant.bodyText, variant.title, platform)
                qualityDefenseTotalFlags += defenseHits.sumOf { it.count }
                defenseHits.forEach { hit ->
                    forbiddenHits += "${platform.displayName}: [품질방어] ${hit.category.label} (${hit.matchedSnippet})"
                }
            }
        }

        // Legal-Doctrine Verification (임차인 배당 · 관리비 2001다8677 · 유치권 · 법정지상권 · 공부 모순)
        val legalReport = LegalDoctrineVerifier.verify(auctionCase)

        // Character counts excluding whitespace: 1,200 ~ 2,500
        val charCounts = variants.mapValues { (_, v) -> v.bodyText.count { !it.isWhitespace() } }
        val charCountOk = charCounts.mapValues { (_, count) -> count in 1200..2500 }

        // External link penalty check
        val naverHasDirectUrl = if (nv != null) {
            URL_REGEX.containsMatchIn(nv.bodyText) || URL_REGEX.containsMatchIn(nv.bodyHtml)
        } else {
            false
        }
        val naverRuleOk = if (!config.enableExternalLinkNaver) !naverHasDirectUrl else {
            val matches = URL_REGEX.findAll(nv?.bodyHtml.orEmpty()).count()
            matches <= 1
        }
        val tistoryNofollowOk = ts?.bodyHtml?.contains("nofollow sponsored") == true
        val externalLinkRuleOk = naverRuleOk && tistoryNofollowOk
        val externalLinkDetail = buildString {
            append(
                if (naverRuleOk) "네이버 외부URL 차단(ENABLE_EXTERNAL_LINK_NAVER=${config.enableExternalLinkNaver}) 통과"
                else "네이버 본문 내 직접 외부 URL 감지됨(저품질 위험)"
            )
            append(" · ")
            append(
                if (tistoryNofollowOk) "티스토리 rel=\"nofollow sponsored\" 적용"
                else "티스토리 rel=\"nofollow sponsored\" 누락"
            )
        }

        // Image 4~7 cards + EXIF stripped + distinct hashes across channels
        val allCards = variants.values.flatMap { it.infographicCards }
        val countsOk = variants.values.all { it.infographicCards.size in 4..7 }
        val exifOk = allCards.all { it.exifStripped }
        val uniqueShaCount = allCards.map { it.sha256Hash }.distinct().size
        val uniqueMd5Count = allCards.map { it.md5Hash }.distinct().size
        val distinctHashOk = allCards.isNotEmpty() && uniqueShaCount == allCards.size && uniqueMd5Count == allCards.size
        val imageCountAndHashOk = countsOk && exifOk && distinctHashOk
        val imageHashDetail = if (imageCountAndHashOk) {
            "채널당 4~7장(${variants.values.firstOrNull()?.infographicCards?.size ?: 0}장) · EXIF 100% 삭제 · 해시 ${uniqueShaCount}종 전부 상이"
        } else {
            "이미지 수량(4~7장) 또는 채널별 해시 변형 미충족"
        }

        // JSON-LD check on Cloudflare
        val jsonLdValid = cf != null &&
            cf.jsonLdSchema.contains("\"@type\": \"Article\"") &&
            cf.jsonLdSchema.contains("\"@type\": \"FAQPage\"")

        // Exact :00:00 anti-bot check
        val scheduleJitterValid = scheduledTimes.values.all { timeStr ->
            !timeStr.endsWith(":00:00") && !timeStr.endsWith(":00")
        }

        val overallPassed = similarityPassed &&
            factCheckPassed &&
            forbiddenHits.isEmpty() &&
            charCountOk.values.all { it } &&
            externalLinkRuleOk &&
            imageCountAndHashOk &&
            jsonLdValid &&
            scheduleJitterValid &&
            legalReport.overallPassed

        return QaAuditReport(
            maxCrossChannelCosinePct = round1(maxCosine),
            maxCrossChannelJaccard5GramPct = round1(maxJaccard),
            recent7DayMaxSimilarityPct = round1(recentMax),
            pairSimilarities = pairCosine.mapValues { round1(it.value) },
            similarityPassed = similarityPassed,
            factCheckPassed = factCheckPassed,
            factCheckMismatches = mismatches,
            forbiddenHits = forbiddenHits,
            charCountByChannel = charCounts,
            charCountOkByChannel = charCountOk,
            externalLinkRuleOk = externalLinkRuleOk,
            externalLinkDetail = externalLinkDetail,
            imageCountAndHashOk = imageCountAndHashOk,
            imageHashDetail = imageHashDetail,
            jsonLdValid = jsonLdValid,
            scheduleJitterValid = scheduleJitterValid,
            overallPassed = overallPassed,
            legalDoctrinePassed = legalReport.overallPassed,
            legalDoctrineSummary = legalReport.auditSummary,
            qualityDefenseFlagsCount = qualityDefenseTotalFlags
        )
    }

    fun cosineSimilarityPct(a: String, b: String): Double {
        if (a.isBlank() || b.isBlank()) return 0.0
        val vecA = buildWordTokenFrequency(a)
        val vecB = buildWordTokenFrequency(b)
        if (vecA.isEmpty() || vecB.isEmpty()) return 0.0

        var dot = 0.0
        var normA = 0.0
        var normB = 0.0
        for ((k, v) in vecA) {
            normA += v * v
            val bv = vecB[k] ?: 0
            if (bv > 0) dot += v * bv
        }
        for (v in vecB.values) {
            normB += v * v
        }
        val denom = sqrt(normA) * sqrt(normB)
        return if (denom == 0.0) 0.0 else (dot / denom) * 100.0
    }

    fun jaccard5GramPct(a: String, b: String): Double {
        val setA = buildNGramSet(normalizeForSimilarity(a), 5)
        val setB = buildNGramSet(normalizeForSimilarity(b), 5)
        if (setA.isEmpty() || setB.isEmpty()) return 0.0
        val intersection = setA.count { it in setB }
        val union = setA.size + setB.size - intersection
        return if (union == 0) 0.0 else (intersection.toDouble() / union.toDouble()) * 100.0
    }

    private fun normalizeForSimilarity(text: String): String =
        text.lowercase()
            .replace(Regex("""[^\p{L}\p{N}]"""), "")

    private fun buildWordTokenFrequency(text: String): Map<String, Int> {
        val words = text.lowercase()
            .replace(Regex("""[^\p{L}\p{N}\s]"""), " ")
            .trim()
            .split(Regex("""\s+"""))
            .filter { it.length >= 2 }
        if (words.isEmpty()) return emptyMap()
        val map = HashMap<String, Int>(words.size * 2)
        for (i in words.indices) {
            val w = words[i]
            map[w] = (map[w] ?: 0) + 1
            if (i + 1 < words.size) {
                val bigram = "$w ${words[i + 1]}"
                map[bigram] = (map[bigram] ?: 0) + 1
            }
        }
        return map
    }

    private fun buildNGramSet(text: String, n: Int): Set<String> {
        if (text.length < n) return emptySet()
        val set = HashSet<String>(text.length)
        for (i in 0..(text.length - n)) {
            set.add(text.substring(i, i + n))
        }
        return set
    }

    private fun containsPriceSignal(text: String, priceKrw: Long, formattedShort: String): Boolean {
        val compactText = text.replace(",", "").replace(" ", "")
        val compactShort = formattedShort.replace(",", "").replace(" ", "")
        val manWon = (priceKrw / 10_000L).toString()
        return compactText.contains(compactShort) || compactText.contains(manWon)
    }

    fun formatKrwShort(krw: Long): String {
        val eok = krw / 100_000_000L
        val man = (krw % 100_000_000L) / 10_000L
        return when {
            eok > 0 && man > 0 -> "${eok}억 ${"%,d".format(man)}만원"
            eok > 0 -> "${eok}억원"
            man > 0 -> "${"%,d".format(man)}만원"
            else -> "${"%,d".format(krw)}원"
        }
    }

    fun sha256Short(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(16)
    }

    fun md5Short(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(16)
    }

    private fun round1(value: Double): Double = (value * 10.0).roundToInt() / 10.0
}
