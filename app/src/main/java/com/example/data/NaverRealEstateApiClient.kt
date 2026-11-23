package com.example.data

import com.example.BuildConfig
import com.example.engine.NearbyMarketEngine
import com.example.model.AuctionCaseIntel
import com.example.model.LegalDoctrineReport
import com.example.model.MarketApiSourceStatus
import com.example.model.NearbyMarketAnalysisReport
import com.example.model.NearbyMarketComp
import com.example.model.PropertyType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Dual-source real HTTP integration for Naver free APIs:
 * 1) Naver Developer Search API (local.json + blog.json) with X-Naver-Client-Id/Secret
 * 2) Naver Real Estate mobile public listing endpoint (m.land.naver.com/cluster/ajax/articleList)
 *
 * Strictly does NOT invent fake listings when keys are absent or Naver rate-limits requests.
 */
class NaverRealEstateApiClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build(),
    private val naverSearchBaseUrl: String = "https://openapi.naver.com",
    private val naverLandBaseUrl: String = "https://m.land.naver.com",
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val defaultClientId: String = BuildConfig.NAVER_CLIENT_ID,
    private val defaultClientSecret: String = BuildConfig.NAVER_CLIENT_SECRET
) {

    data class GeoPoint(val lat: Double, val lon: Double)

    suspend fun fetchNearbyMarketAnalysis(
        auction: AuctionCaseIntel,
        legalReport: LegalDoctrineReport,
        clientId: String = defaultClientId,
        clientSecret: String = defaultClientSecret
    ): NearbyMarketAnalysisReport = withContext(ioDispatcher) {
        val regionTokens = auction.address.split(" ").filter { it.isNotBlank() }
        val shortRegion = regionTokens.take(3).joinToString(" ").ifBlank { auction.address }
        val validClientId = clientId.takeIf { it.isNotBlank() && it != "NAVER_CLIENT_ID_DEFAULT_VALUE" }
        val validClientSecret = clientSecret.takeIf { it.isNotBlank() && it != "NAVER_CLIENT_SECRET_DEFAULT_VALUE" }
        val hasSearchKeys = validClientId != null && validClientSecret != null

        var resolvedGeo: GeoPoint? = null
        var searchHitCount = 0
        val blogTrendTitles = mutableListOf<String>()
        var searchErrorNote: String? = null

        // 1. Naver Developer Free Search API (local.json + blog.json)
        if (validClientId != null && validClientSecret != null) {
            val localQuery = URLEncoder.encode(shortRegion, "UTF-8")
            val localReq = Request.Builder()
                .url("${naverSearchBaseUrl.trimEnd('/')}/v1/search/local.json?query=$localQuery&display=5&sort=random")
                .get()
                .addHeader("X-Naver-Client-Id", validClientId)
                .addHeader("X-Naver-Client-Secret", validClientSecret)
                .build()

            val localRes = executeRequest(localReq)
            if (localRes.isSuccess) {
                val parsedLocal = NearbyMarketEngine.parseNaverLocalSearchJson(localRes.body)
                searchHitCount += parsedLocal.items.size
                resolvedGeo = parsedLocal.firstGeo
            } else {
                searchErrorNote = "검색API(local) HTTP ${localRes.statusCode}"
            }

            val blogQueryText = "$shortRegion ${auction.propertyType.displayLabel.substringBefore("·")} 급매 시세 실거래"
            val encodedBlogQuery = URLEncoder.encode(blogQueryText, "UTF-8")
            val blogReq = Request.Builder()
                .url("${naverSearchBaseUrl.trimEnd('/')}/v1/search/blog.json?query=$encodedBlogQuery&display=5&sort=date")
                .get()
                .addHeader("X-Naver-Client-Id", validClientId)
                .addHeader("X-Naver-Client-Secret", validClientSecret)
                .build()

            val blogRes = executeRequest(blogReq)
            if (blogRes.isSuccess) {
                val parsedBlogs = NearbyMarketEngine.parseNaverBlogSearchJson(blogRes.body)
                searchHitCount += parsedBlogs.size
                blogTrendTitles += parsedBlogs
            } else if (searchErrorNote == null) {
                searchErrorNote = "검색API(blog) HTTP ${blogRes.statusCode}"
            }
        }

        // 2. Naver Real Estate mobile public endpoint (m.land.naver.com/cluster/ajax/articleList)
        val centerGeo = resolvedGeo ?: lookupDistrictSeedCoordinates(auction.address)
        val landComps = mutableListOf<NearbyMarketComp>()
        var landStatusNote: String? = null

        if (centerGeo != null) {
            val rletCode = mapPropertyTypeToNaverRletCode(auction.propertyType)
            val deltaLat = 0.008
            val deltaLon = 0.011
            val landUrl = buildString {
                append("${naverLandBaseUrl.trimEnd('/')}/cluster/ajax/articleList")
                append("?rletTpCd=").append(URLEncoder.encode(rletCode, "UTF-8"))
                append("&tradTpCd=").append(URLEncoder.encode("A1:B1", "UTF-8"))
                append("&z=15")
                append("&lat=").append(centerGeo.lat)
                append("&lon=").append(centerGeo.lon)
                append("&btm=").append(centerGeo.lat - deltaLat)
                append("&lft=").append(centerGeo.lon - deltaLon)
                append("&top=").append(centerGeo.lat + deltaLat)
                append("&rgt=").append(centerGeo.lon + deltaLon)
                append("&page=1")
            }

            val landReq = Request.Builder()
                .url(landUrl)
                .get()
                .addHeader(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
                )
                .addHeader("Referer", "https://m.land.naver.com/")
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .build()

            val landRes = executeRequest(landReq)
            if (landRes.isSuccess && landRes.body.trim().startsWith("{")) {
                landComps += NearbyMarketEngine.parseNaverLandArticleListJson(landRes.body)
                if (landComps.isEmpty()) {
                    landStatusNote = "반경 내 매물 0건 반환"
                }
            } else {
                landStatusNote = if (landRes.statusCode == 429 || landRes.statusCode == 403 || landRes.statusCode == 302) {
                    "네이버 부동산 요청 제한(HTTP ${landRes.statusCode})"
                } else {
                    landRes.errorMessage ?: "네이버 부동산 응답 코드 ${landRes.statusCode}"
                }
            }
        }

        val status = when {
            searchHitCount > 0 && landComps.isNotEmpty() -> MarketApiSourceStatus.LIVE_FULL
            landComps.isNotEmpty() -> MarketApiSourceStatus.LIVE_LAND_ONLY
            searchHitCount > 0 -> MarketApiSourceStatus.LIVE_SEARCH_ONLY
            !hasSearchKeys -> MarketApiSourceStatus.API_KEY_REQUIRED
            else -> MarketApiSourceStatus.RATE_LIMITED_OR_ERROR
        }

        val statusDetail = when (status) {
            MarketApiSourceStatus.LIVE_FULL ->
                "네이버 검색 ${searchHitCount}건 · 네이버 부동산 실매물 ${landComps.size}건 수신 완료"
            MarketApiSourceStatus.LIVE_LAND_ONLY ->
                "네이버 부동산 실매물 ${landComps.size}건 수신 · 검색API 키(Secrets: NAVER_CLIENT_ID/SECRET) 미설정"
            MarketApiSourceStatus.LIVE_SEARCH_ONLY ->
                "네이버 검색API ${searchHitCount}건 수신 (${landStatusNote ?: "부동산 매물 접근 제한"})"
            MarketApiSourceStatus.API_KEY_REQUIRED ->
                "AI Studio Secrets 패널에 NAVER_CLIENT_ID / NAVER_CLIENT_SECRET 설정 시 검색API 연동 활성화 (${landStatusNote ?: "실매물 0건"})"
            MarketApiSourceStatus.RATE_LIMITED_OR_ERROR ->
                listOfNotNull(searchErrorNote, landStatusNote).joinToString(" · ").ifBlank { "네이버 서버 응답 지연 또는 접근 제한" }
            MarketApiSourceStatus.IDLE -> "조회 대기"
        }

        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())
        NearbyMarketEngine.buildMarketAnalysisReport(
            auction = auction,
            legalReport = legalReport,
            regionQuery = shortRegion,
            status = status,
            statusDetail = statusDetail,
            naverSearchHitCount = searchHitCount,
            comps = landComps,
            blogTrendTitles = blogTrendTitles,
            fetchedAt = now
        )
    }

    private fun executeRequest(request: Request): HttpCallResult = try {
        client.newCall(request).execute().use { res ->
            HttpCallResult(
                isSuccess = res.isSuccessful,
                statusCode = res.code,
                body = res.body?.string().orEmpty(),
                errorMessage = if (res.isSuccessful) null else "HTTP ${res.code}"
            )
        }
    } catch (e: Exception) {
        HttpCallResult(
            isSuccess = false,
            statusCode = -1,
            body = "",
            errorMessage = e.localizedMessage ?: "네트워크 오류"
        )
    }

    companion object {
        fun mapPropertyTypeToNaverRletCode(type: PropertyType): String = when (type) {
            PropertyType.APT_OFFICETEL -> "APT:OPST"
            PropertyType.VILLA_MULTIFAMILY -> "VL:DDDGG"
            PropertyType.COMMERCIAL_RETAIL -> "SG:SMS"
            PropertyType.LAND_FARMLAND -> "TJ"
            PropertyType.FACTORY_SPECIAL -> "GJ:SG"
        }

        fun lookupDistrictSeedCoordinates(address: String): GeoPoint? = when {
            address.contains("정관") || address.contains("기장") -> GeoPoint(35.3247, 129.1795)
            address.contains("해운대") -> GeoPoint(35.1631, 129.1636)
            address.contains("강서") || address.contains("화곡") -> GeoPoint(37.5416, 126.8404)
            address.contains("분당") || address.contains("정자동") -> GeoPoint(37.3665, 127.1086)
            address.contains("포항") || address.contains("흥해") -> GeoPoint(36.1095, 129.3446)
            address.contains("인천") || address.contains("서구") || address.contains("오류") -> GeoPoint(37.5892, 126.6256)
            address.contains("용인") || address.contains("수지") -> GeoPoint(37.3168, 127.0789)
            address.contains("도봉") || address.contains("창동") -> GeoPoint(37.6531, 127.0477)
            address.contains("송파") || address.contains("문정") -> GeoPoint(37.4858, 127.1225)
            address.contains("원주") || address.contains("지정면") -> GeoPoint(37.3728, 127.8781)
            address.contains("시흥") || address.contains("정왕") -> GeoPoint(37.3422, 126.7327)
            address.contains("부산") -> GeoPoint(35.1796, 129.0756)
            address.contains("서울") -> GeoPoint(37.5665, 126.9780)
            address.contains("수원") -> GeoPoint(37.2636, 127.0286)
            else -> GeoPoint(37.5665, 126.9780)
        }
    }
}
