package com.example.engine

import com.example.data.NaverRealEstateApiClient
import com.example.model.AuctionCaseIntel
import com.example.model.LegalDoctrineReport
import com.example.model.MarketApiSourceStatus
import com.example.model.NearbyMarketAnalysisReport
import com.example.model.NearbyMarketComp
import com.example.model.RecalculatedBidBand
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlin.math.roundToInt

/**
 * Pure parser and valuation recomputation engine for Naver Search API +
 * Naver Real Estate nearby listings. Computes urgent asking price margins
 * and dynamically recalculates 3-tier bid bands subtracting legal burdens.
 */
object NearbyMarketEngine {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val mapAdapter = moshi.adapter<Map<String, Any?>>(
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    )

    data class ParsedLocalSearch(
        val items: List<String>,
        val firstGeo: NaverRealEstateApiClient.GeoPoint?
    )

    @Suppress("UNCHECKED_CAST")
    private fun extractMapList(rawJson: String, key: String): List<Map<String, Any?>> {
        val root = mapAdapter.fromJson(rawJson) ?: return emptyList()
        val rawList = root[key] as? List<*> ?: return emptyList()
        return rawList.mapNotNull { it as? Map<String, Any?> }
    }

    private fun Any?.asCleanString(): String = when (this) {
        null -> ""
        is String -> this
        is Number -> this.toString()
        else -> this.toString()
    }

    private fun Any?.asLongValue(): Long = when (this) {
        is Number -> this.toLong()
        is String -> this.replace(",", "").toLongOrNull() ?: 0L
        else -> 0L
    }

    fun parseNaverLocalSearchJson(rawJson: String): ParsedLocalSearch {
        if (rawJson.isBlank()) return ParsedLocalSearch(emptyList(), null)
        return runCatching {
            val arr = extractMapList(rawJson, "items")
            val titles = mutableListOf<String>()
            var firstGeo: NaverRealEstateApiClient.GeoPoint? = null
            for (obj in arr) {
                val title = stripHtmlTags(obj["title"].asCleanString())
                if (title.isNotBlank()) titles += title
                if (firstGeo == null) {
                    val rawX = obj["mapx"].asCleanString().toDoubleOrNull()
                    val rawY = obj["mapy"].asCleanString().toDoubleOrNull()
                    if (rawX != null && rawY != null && rawX > 1_000_000.0 && rawY > 1_000_000.0) {
                        val lon = rawX / 10_000_000.0
                        val lat = rawY / 10_000_000.0
                        if (lat in 32.0..39.5 && lon in 124.0..132.0) {
                            firstGeo = NaverRealEstateApiClient.GeoPoint(lat = lat, lon = lon)
                        }
                    }
                }
            }
            ParsedLocalSearch(titles, firstGeo)
        }.getOrDefault(ParsedLocalSearch(emptyList(), null))
    }

    fun parseNaverBlogSearchJson(rawJson: String): List<String> {
        if (rawJson.isBlank()) return emptyList()
        return runCatching {
            extractMapList(rawJson, "items").mapNotNull { obj ->
                stripHtmlTags(obj["title"].asCleanString()).takeIf { it.isNotBlank() }
            }
        }.getOrDefault(emptyList())
    }

    fun parseNaverLandArticleListJson(rawJson: String): List<NearbyMarketComp> {
        if (rawJson.isBlank()) return emptyList()
        return runCatching {
            val bodyArray = extractMapList(rawJson, "body")
            val comps = mutableListOf<NearbyMarketComp>()
            bodyArray.forEachIndexed { idx, item ->
                val tradTpNm = item["tradTpNm"].asCleanString().ifBlank { "매매" }
                if (tradTpNm == "월세") return@forEachIndexed

                val hanPrc = item["hanPrc"].asCleanString()
                val prcMan = item["prc"].asLongValue()
                val priceKrw = parseNaverKoreanPriceToKrw(hanPrc, prcMan)
                if (priceKrw <= 0L) return@forEachIndexed

                val title = item["atclNm"].asCleanString().ifBlank { "인근 매물 #${idx + 1}" }
                val featureDesc = item["atclFetrDesc"].asCleanString()
                val isUrgent = featureDesc.contains("급매") || featureDesc.contains("급처") ||
                    title.contains("급매") || featureDesc.contains("최저")
                val spc1 = item["spc1"].asCleanString()
                val spc2 = item["spc2"].asCleanString()
                val areaLabel = if (spc2.isNotBlank()) "전용 ${spc2}㎡" else if (spc1.isNotBlank()) "공급 ${spc1}㎡" else "면적미상"

                comps += NearbyMarketComp(
                    sourceLabel = "네이버부동산",
                    title = title,
                    propertyCategory = item["rletTpNm"].asCleanString().ifBlank { "매물" },
                    tradeType = tradTpNm,
                    priceKrw = priceKrw,
                    areaSqmLabel = areaLabel,
                    floorInfo = item["flrInfo"].asCleanString().ifBlank { "-" },
                    listingDate = item["atclCfmYmd"].asCleanString().ifBlank { "-" },
                    isUrgentSale = isUrgent
                )
            }
            comps.sortedBy { it.priceKrw }.take(12)
        }.getOrDefault(emptyList())
    }

    fun parseNaverKoreanPriceToKrw(rawHanPrc: String, fallbackManWon: Long = 0L): Long {
        val cleaned = rawHanPrc.trim()
        if (cleaned.isNotBlank()) {
            val eokRegex = Regex("""(\d[\d,]*)\s*억(?:\s*(\d[\d,]*))?""")
            val eokMatch = eokRegex.find(cleaned)
            if (eokMatch != null) {
                val eok = eokMatch.groupValues[1].replace(",", "").toLongOrNull() ?: 0L
                val man = eokMatch.groupValues[2].replace(",", "").toLongOrNull() ?: 0L
                return eok * 100_000_000L + man * 10_000L
            }
            val pureMan = cleaned.replace(",", "").replace("만", "").toLongOrNull()
            if (pureMan != null && pureMan > 0L) {
                return pureMan * 10_000L
            }
        }
        return if (fallbackManWon > 0L) fallbackManWon * 10_000L else 0L
    }

    fun buildMarketAnalysisReport(
        auction: AuctionCaseIntel,
        legalReport: LegalDoctrineReport,
        regionQuery: String,
        status: MarketApiSourceStatus,
        statusDetail: String,
        naverSearchHitCount: Int,
        comps: List<NearbyMarketComp>,
        blogTrendTitles: List<String>,
        fetchedAt: String
    ): NearbyMarketAnalysisReport {
        val saleComps = comps.filter { it.tradeType == "매매" }.ifEmpty { comps }
        val urgentAskMinKrw = saleComps.filter { it.isUrgentSale }.minOfOrNull { it.priceKrw }
            ?: saleComps.minOfOrNull { it.priceKrw }

        val sortedPrices = saleComps.map { it.priceKrw }.sorted()
        val askMedianKrw = if (sortedPrices.isNotEmpty()) {
            sortedPrices[sortedPrices.size / 2]
        } else null

        val realTradeBenchmarkKrw = when {
            askMedianKrw != null -> ((askMedianKrw * 0.55) + (auction.recentRealTradeMedianKrw * 0.45)).toLong()
            else -> auction.recentRealTradeMedianKrw
        }

        val recalculatedBids = if (urgentAskMinKrw != null || askMedianKrw != null) {
            recalculateBidBand(
                auction = auction,
                urgentAskMinKrw = urgentAskMinKrw ?: askMedianKrw!!,
                realTradeBenchmarkKrw = realTradeBenchmarkKrw,
                legalBurdenKrw = legalReport.totalEstimatedLegalBurdenKrw
            )
        } else null

        return NearbyMarketAnalysisReport(
            caseId = auction.caseId,
            regionQuery = regionQuery,
            status = status,
            statusDetail = statusDetail,
            naverSearchHitCount = naverSearchHitCount,
            naverLandListingCount = comps.size,
            urgentAskMinKrw = urgentAskMinKrw,
            nearbyAskMedianKrw = askMedianKrw,
            realTradeBenchmarkKrw = realTradeBenchmarkKrw,
            comps = comps,
            blogTrendTitles = blogTrendTitles,
            recalculatedBids = recalculatedBids,
            fetchedAt = fetchedAt
        )
    }

    fun recalculateBidBand(
        auction: AuctionCaseIntel,
        urgentAskMinKrw: Long,
        realTradeBenchmarkKrw: Long,
        legalBurdenKrw: Long
    ): RecalculatedBidBand {
        val marketCeiling = minOf(urgentAskMinKrw, realTradeBenchmarkKrw).coerceAtLeast(auction.minBidPriceKrw)
        val transactionFriction = (marketCeiling * 0.045).toLong() + legalBurdenKrw

        val rawConservative = (marketCeiling * 0.84).toLong() - legalBurdenKrw
        val rawBalanced = (marketCeiling * 0.89).toLong() - (legalBurdenKrw * 0.9).toLong()
        val rawAggressive = (marketCeiling * 0.93).toLong() - (legalBurdenKrw * 0.8).toLong()

        val floorMin = auction.minBidPriceKrw
        val conservative = rawConservative.coerceAtLeast(floorMin)
        val balanced = rawBalanced.coerceAtLeast((conservative * 1.03).toLong())
        val aggressive = rawAggressive.coerceAtLeast((balanced * 1.03).toLong())

        val urgentMarginKrw = (urgentAskMinKrw - balanced - transactionFriction)
        val urgentMarginPct = if (urgentAskMinKrw > 0L) {
            ((urgentMarginKrw.toDouble() / urgentAskMinKrw.toDouble()) * 1000.0).roundToInt() / 10.0
        } else 0.0
        val realTradeMargin = realTradeBenchmarkKrw - balanced - legalBurdenKrw

        val ruleNote = buildString {
            append("인근 급매 호가(${QaVerificationEngine.formatKrwShort(urgentAskMinKrw)})·실거래(${QaVerificationEngine.formatKrwShort(realTradeBenchmarkKrw)}) ")
            append("및 법리 예상부담(${QaVerificationEngine.formatKrwShort(legalBurdenKrw)}) 차감 재산정 완료")
        }

        return RecalculatedBidBand(
            conservativeBidKrw = conservative,
            balancedBidKrw = balanced,
            aggressiveBidKrw = aggressive,
            urgentAskMarginKrw = urgentMarginKrw,
            urgentAskMarginPct = urgentMarginPct,
            realTradeMarginKrw = realTradeMargin,
            safetyRuleNote = ruleNote
        )
    }

    fun applyMarketReportToAuction(
        auction: AuctionCaseIntel,
        report: NearbyMarketAnalysisReport
    ): AuctionCaseIntel {
        val bids = report.recalculatedBids ?: return auction
        return auction.copy(
            recentRealTradeMedianKrw = report.realTradeBenchmarkKrw,
            conservativeBidKrw = bids.conservativeBidKrw,
            balancedBidKrw = bids.balancedBidKrw,
            aggressiveBidKrw = bids.aggressiveBidKrw
        )
    }

    private fun stripHtmlTags(raw: String): String =
        raw.replace(Regex("""<[^>]*>"""), "")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .trim()
}
