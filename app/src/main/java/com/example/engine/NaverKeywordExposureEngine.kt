package com.example.engine

import com.example.model.ExposurePotentialGrade
import com.example.model.KeywordBlogExposureReport
import com.example.model.NaverBlogSearchSnapshot
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/**
 * Pure diagnostic engine evaluating Naver Blog top-exposure potential
 * for a specific auction keyword using real Naver Blog Search API results.
 */
object NaverKeywordExposureEngine {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val mapAdapter = moshi.adapter<Map<String, Any?>>(
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    )

    private val AUCTION_DOMAIN_TERMS = listOf(
        "경매", "타경", "감정가", "최저가", "말소기준", "낙찰", "입찰", "유찰",
        "임차인", "권리분석", "임장", "매각기일", "등기부"
    )

    private val CASE_ID_REGEX = Regex("""20\d{2}\s*타경\s*\d+""")
    private val HTML_TAG_REGEX = Regex("""<[^>]*>""")

    fun analyzeBlogSearchJson(
        keyword: String,
        rawJson: String,
        referenceDateYmd: String? = null,
        diagnosedAt: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())
    ): KeywordBlogExposureReport {
        val cleanKeyword = keyword.trim()
        val todayYmd = referenceDateYmd?.takeIf { it.length == 8 }
            ?: SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(Date())

        val root = runCatching { mapAdapter.fromJson(rawJson) }.getOrNull().orEmpty()
        val total = when (val rawTotal = root["total"]) {
            is Number -> rawTotal.toInt().coerceAtLeast(0)
            is String -> rawTotal.toIntOrNull()?.coerceAtLeast(0) ?: 0
            else -> 0
        }
        @Suppress("UNCHECKED_CAST")
        val items = (root["items"] as? List<*>)
            ?.mapNotNull { it as? Map<String, Any?> }
            .orEmpty()

        val titles = mutableListOf<String>()
        val bloggers = mutableListOf<String>()
        var recent7Count = 0
        var recent30Count = 0
        var auctionFocusedCount = 0
        var exactCaseMatches = 0
        var latestPostDate = "-"

        val caseTokens = CASE_ID_REGEX.findAll(cleanKeyword)
            .map { it.value.replace(" ", "") }
            .toList()

        items.forEach { item ->
            val rawTitle = item["title"]?.toString().orEmpty()
            val rawDesc = item["description"]?.toString().orEmpty()
            val cleanTitle = cleanHtmlText(rawTitle)
            val cleanDesc = cleanHtmlText(rawDesc)
            val blogger = cleanHtmlText(item["bloggername"]?.toString().orEmpty())
            val postDate = item["postdate"]?.toString().orEmpty().trim()

            if (cleanTitle.isNotBlank()) titles += cleanTitle
            if (blogger.isNotBlank()) bloggers += blogger

            if (postDate.length == 8) {
                if (latestPostDate == "-" || postDate > latestPostDate) {
                    latestPostDate = postDate
                }
                val ageDays = computeDayDiff(todayYmd, postDate)
                if (ageDays != null) {
                    if (ageDays <= 7) recent7Count++
                    if (ageDays <= 30) recent30Count++
                }
            }

            val combined = "$cleanTitle $cleanDesc"
            if (AUCTION_DOMAIN_TERMS.any { combined.contains(it) }) {
                auctionFocusedCount++
            }
            if (caseTokens.isNotEmpty() && caseTokens.any { combined.replace(" ", "").contains(it) }) {
                exactCaseMatches++
            }
        }

        val sampledCount = items.size
        val auctionRatioPct = if (sampledCount == 0) {
            0
        } else {
            ((auctionFocusedCount.toDouble() / sampledCount.toDouble()) * 100.0).roundToInt()
        }

        // 1. Supply saturation score (max 45 pts): fewer total posts = easier top exposure
        val supplyScore = when {
            total == 0 -> 45
            total <= 300 -> 42
            total <= 1_000 -> 36
            total <= 3_500 -> 28
            total <= 10_000 -> 20
            total <= 30_000 -> 13
            else -> 6
        }

        // 2. Freshness gap score (max 25 pts): fewer ultra-recent competitors in top SERP = high window
        val freshnessScore = when {
            sampledCount == 0 -> 25
            recent7Count == 0 -> 24
            recent7Count <= 2 -> 19
            recent7Count <= 5 -> 13
            recent7Count <= 10 -> 8
            else -> 4
        }

        // 3. Domain differentiation score (max 20 pts):
        // if top posts are weak on case numbers or auction depth, our 30-point legal/price analysis stands out
        val differentiationScore = when {
            caseTokens.isNotEmpty() && exactCaseMatches <= 2 -> 20
            auctionRatioPct < 35 -> 17
            auctionRatioPct < 65 -> 13
            else -> 8
        }

        // 4. Long-tail intent specificity bonus (max 10 pts)
        val wordCount = cleanKeyword.split(Regex("""\s+""")).count { it.isNotBlank() }
        val specificityBonus = when {
            caseTokens.isNotEmpty() && wordCount >= 2 -> 10
            wordCount >= 3 -> 9
            wordCount == 2 -> 6
            else -> 2
        }

        val exposureScore = (supplyScore + freshnessScore + differentiationScore + specificityBonus)
            .coerceIn(5, 99)

        val grade = when {
            exposureScore >= 78 -> ExposurePotentialGrade.BLUE_OCEAN
            exposureScore >= 58 -> ExposurePotentialGrade.COMPETITIVE_WINNABLE
            exposureScore >= 38 -> ExposurePotentialGrade.CROWDED_NICHE
            else -> ExposurePotentialGrade.SATURATED
        }

        val snapshot = NaverBlogSearchSnapshot(
            keyword = cleanKeyword,
            totalSearchCount = total,
            sampledCount = sampledCount,
            recent7DaysCount = recent7Count,
            recent30DaysCount = recent30Count,
            auctionFocusedRatioPct = auctionRatioPct,
            exactCaseMatchCount = exactCaseMatches,
            topPostTitles = titles.take(5),
            topBloggerNames = bloggers.distinct().take(5),
            latestPostDate = latestPostDate
        )

        val competitionSummary = buildString {
            append("총 블로그 문서 ${formatCount(total)}건")
            append(" · 상위 ${sampledCount}건 중 최근 7일 발행 ${recent7Count}건")
            append(" · 경매 전문글 비중 ${auctionRatioPct}%")
            if (caseTokens.isNotEmpty()) {
                append(" · 동일 사건번호 직접 언급 ${exactCaseMatches}건")
            }
        }

        val recommendedTitlePattern = buildRecommendedTitlePattern(cleanKeyword, grade, caseTokens.isNotEmpty())
        val recommendedSubKeywords = buildSubKeywords(cleanKeyword, caseTokens.firstOrNull())
        val actionChecklist = buildActionChecklist(total, recent7Count, auctionRatioPct, caseTokens.isNotEmpty(), grade)

        return KeywordBlogExposureReport(
            keyword = cleanKeyword,
            exposureScore = exposureScore,
            grade = grade,
            snapshot = snapshot,
            competitionSummary = competitionSummary,
            recommendedTitlePattern = recommendedTitlePattern,
            recommendedSubKeywords = recommendedSubKeywords,
            actionChecklist = actionChecklist,
            diagnosedAt = diagnosedAt
        )
    }

    private fun buildRecommendedTitlePattern(
        keyword: String,
        grade: ExposurePotentialGrade,
        hasCaseId: Boolean
    ): String = when {
        hasCaseId -> "$keyword 최저가·실거래·말소기준권리 핵심 분석 (임장 체크)"
        grade == ExposurePotentialGrade.BLUE_OCEAN || grade == ExposurePotentialGrade.COMPETITIVE_WINNABLE ->
            "$keyword 유찰 최저가 계산과 권리분석 주의사항 정리"
        else -> "$keyword 소액·특수물건 권리분석 및 실거래 대비 안전마진 비교"
    }

    private fun buildSubKeywords(keyword: String, caseId: String?): List<String> {
        val baseToken = keyword.split(Regex("""\s+""")).firstOrNull().orEmpty()
        return buildList {
            if (!caseId.isNullOrBlank()) {
                add("$caseId 권리분석")
                add("$caseId 입찰보증금")
            }
            if (baseToken.isNotBlank()) {
                add("$baseToken 경매 최저가")
                add("$baseToken 실거래가 비교")
                add("$baseToken 임차인 대항력")
            }
            add("$keyword 유찰 이유")
        }.distinct().take(5)
    }

    private fun buildActionChecklist(
        total: Int,
        recent7Count: Int,
        auctionRatioPct: Int,
        hasCaseId: Boolean,
        grade: ExposurePotentialGrade
    ): List<String> = buildList {
        if (grade == ExposurePotentialGrade.SATURATED || total > 15_000) {
            add("단일 대표 키워드 경쟁(${formatCount(total)}건)이 높으므로 동/역세권·사건번호(타경)를 제목 앞부분에 결합하세요.")
        } else {
            add("문서 공급량(${formatCount(total)}건)이 적정해 1페이지 상위 진입 여력이 충분합니다.")
        }
        if (recent7Count >= 6) {
            add("최근 7일 내 신규 글이 ${recent7Count}건으로 활발하므로 현장 R2 사진 + 실측 입찰표 차이점을 첫 화면에 배치하세요.")
        } else {
            add("최근 7일 경쟁 글이 ${recent7Count}건으로 적어 최신 매각기일 기준으로 발행 시 신선도 가산점을 얻기 유리합니다.")
        }
        if (!hasCaseId) {
            add("구체적 사건번호(예: 2024타경XXXX)와 말소기준권리일을 본문·소제목에 명시하면 롱테일 유입이 급증합니다.")
        }
        if (auctionRatioPct < 50) {
            add("상위 노출 글 중 경매 권리분석 비율(${auctionRatioPct}%)이 낮아 등기·보증금 표 삽입만으로도 체류시간 우위를 확보할 수 있습니다.")
        }
        add("네이버 저품질 예방을 위해 외부 URL은 제거하고 95점+ 통과 후 단독 업로드하세요.")
    }

    private fun cleanHtmlText(raw: String): String = raw
        .replace(HTML_TAG_REGEX, "")
        .replace("&quot;", "\"")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .trim()

    private fun computeDayDiff(todayYmd: String, postYmd: String): Int? = runCatching {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.KOREA).apply { isLenient = false }
        val t = sdf.parse(todayYmd) ?: return null
        val p = sdf.parse(postYmd) ?: return null
        val diffMs = (t.time - p.time).coerceAtLeast(0L)
        TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
    }.getOrNull()

    private fun formatCount(count: Int): String = String.format(Locale.KOREA, "%,d", count)
}
