package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.ai.AiEvaluationOutcome
import com.example.ai.AiSeoGeoAdvisor
import com.example.data.BlogAutomationRepository
import com.example.data.CloudflareAndDdakchalApi
import com.example.data.DirectChannelPublishClient
import com.example.data.NaverRealEstateApiClient
import com.example.data.PostWarehouseStore
import com.example.data.ServerConfigStore
import com.example.model.AdminPipelineStageStatus
import com.example.model.ChannelPlatform
import com.example.model.GeneratedPostSlot
import com.example.model.ProFunnelConfig
import com.example.model.ServerAutomationConfig
import com.example.ui.StudioViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TodakAdminDbCrawlEngineTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val offlineAdvisor = object : AiSeoGeoAdvisor {
        override suspend fun evaluateWith30Criteria(
            slot: GeneratedPostSlot,
            config: ServerAutomationConfig,
            proFunnel: ProFunnelConfig
        ): AiEvaluationOutcome = AiEvaluationOutcome.Error(
            message = "offline",
            baselineScore = 0,
            baselineSuggestions = emptyList()
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createConfigStore(scope: TestScope): ServerConfigStore {
        val file = tempFolder.newFile("todak_cfg_${System.nanoTime()}.preferences_pb")
        return ServerConfigStore(PreferenceDataStoreFactory.create(scope = scope) { file })
    }

    private fun createWarehouseStore(scope: TestScope): PostWarehouseStore {
        val file = tempFolder.newFile("todak_wh_${System.nanoTime()}.preferences_pb")
        return PostWarehouseStore(PreferenceDataStoreFactory.create(scope = scope) { file })
    }

    private fun jsonResponse(request: Request, code: Int, bodyJson: String): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code in 200..299) "OK" else "ERR")
            .body(bodyJson.toResponseBody("application/json; charset=utf-8".toMediaType()))
            .build()
    }

    private fun readRequestBody(req: Request): String {
        val body = req.body ?: return ""
        val buffer = Buffer()
        body.writeTo(buffer)
        return buffer.readUtf8()
    }

    @Test
    fun `discoverTodakServerDb inspects server endpoints and CF D1 tables without dummy fallback`() = runTest(testDispatcher) {
        val seenPaths = mutableListOf<String>()
        val seenD1Sql = mutableListOf<String>()

        val assetsFeedJson = """
            {
              "ok": true,
              "data": {
                "items": [
                  {
                    "id": "asset-2025-8801",
                    "case_no": "2025타경8801",
                    "auction_dedup_key": "ca-2025-8801",
                    "court_name": "서울중앙지방법원",
                    "address": "서울특별시 서초구 서초동 래미안리더스원 104동 1202호",
                    "property_type": "APT_OFFICETEL",
                    "appraisal_krw": 2450000000,
                    "min_bid_krw": 1960000000,
                    "discount_rate_pct": 80,
                    "base_right_date": "2020-03-10",
                    "photo_urls": ["https://court.example.com/p1.jpg", "https://court.example.com/p2.jpg"],
                    "visit_records": [
                      {"visitRound": "1차 현황조사", "visitedAt": "2026-11-10", "occupancyStatus": "소유자 점유", "findingsNote": "관리비 정상 납부 확인"}
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

        val analysisFeedJson = """
            {
              "ok": true,
              "data": {
                "items": [
                  {
                    "id": "an-2025-8802",
                    "case_no": "2025타경8802",
                    "auction_dedup_key": "ca-2025-8802",
                    "court_name": "수원지방법원",
                    "address": "경기도 성남시 분당구 정자동 파크뷰 602동 901호",
                    "property_type": "APT_OFFICETEL",
                    "appraisal_krw": 1820000000,
                    "min_bid_krw": 1274000000,
                    "discount_rate_pct": 70,
                    "base_right_date": "2021-05-04",
                    "blog_analysis_data": {
                      "hookHeadline": "분당 정자동 70% 최저가 분석",
                      "summary3Lines": ["말소기준권리 이후 권리 소멸", "전입세대 확인 완료", "보수 입찰가 13.1억"]
                    }
                  }
                ]
              }
            }
        """.trimIndent()

        val triggerStatusJson = """
            {
              "ok": true,
              "data": {
                "triggers": { "pending": 2, "processed": 14, "failed": 0 },
                "snapshots": { "count": 9, "latestAt": "2026-11-26T09:00:00Z" },
                "queue": { "todayCount": 5, "generatedPostsCount": 21 }
              }
            }
        """.trimIndent()

        val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            val req = chain.request()
            val path = req.url.encodedPath
            seenPaths += path
            when {
                path.endsWith("/api/v1/blog/assets/feed") -> jsonResponse(req, 200, assetsFeedJson)
                path.endsWith("/api/v1/blog/analysis-feed") -> jsonResponse(req, 200, analysisFeedJson)
                path.endsWith("/api/v1/blog/triggers/status") -> jsonResponse(req, 200, triggerStatusJson)
                path.contains("/d1/database/") && path.endsWith("/query") -> {
                    val sql = runCatching { JSONObject(readRequestBody(req)).optString("sql") }.getOrDefault("")
                    seenD1Sql += sql
                    if (sql.contains("sqlite_master")) {
                        jsonResponse(
                            req,
                            200,
                            """{"success":true,"result":[{"results":[{"name":"generated_posts"},{"name":"auction_snapshots"}]}]}"""
                        )
                    } else {
                        jsonResponse(
                            req,
                            200,
                            """{"success":true,"result":[{"results":[{"cnt":17}]}]}"""
                        )
                    }
                }
                else -> jsonResponse(req, 200, """{"ok":true}""")
            }
        }).build()

        val api = CloudflareAndDdakchalApi(client = client, ioDispatcher = testDispatcher)
        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            api = api,
            postWarehouseStore = createWarehouseStore(this)
        )
        val vm = StudioViewModel(
            repository = repo,
            aiAdvisor = offlineAdvisor,
            naverApiClient = NaverRealEstateApiClient(ioDispatcher = testDispatcher)
        )

        vm.applyInlineServerAuthAndSave(
            apiBase = "https://ddakchal.pages.dev",
            syncToken = "todak-admin-secret",
            cfAccountId = "acc-12345678",
            cfD1DatabaseId = "db-99887766",
            cfApiToken = "cf-tok-999"
        )
        vm.discoverTodakServerDb()

        assertTrue(seenPaths.any { it.endsWith("/api/v1/blog/assets/feed") })
        assertTrue(seenPaths.any { it.endsWith("/api/v1/blog/analysis-feed") })
        assertTrue(seenPaths.any { it.endsWith("/api/v1/blog/triggers/status") })
        assertTrue(seenD1Sql.any { it.contains("sqlite_master") })
        assertTrue(seenD1Sql.any { it.contains("SELECT COUNT(*) AS cnt FROM generated_posts") })

        val admin = vm.uiState.value.todakAdmin
        assertEquals(listOf("generated_posts", "auction_snapshots"), admin.d1TablesRaw)
        assertEquals(2, admin.discoveredItems.size)
        assertTrue(admin.discoveredItems.any { it.caseId == "2025타경8801" && it.photoCount == 2 && it.visitCount == 1 })
        assertTrue(admin.discoveredItems.any { it.caseId == "2025타경8802" && it.hasAnalysisData })
        assertTrue(admin.discoveredTables.any { it.tableOrEndpoint == "d1://generated_posts" && it.rowCount == 17 })
    }

    @Test
    fun `runTodakFullAdminPipeline crawls with selected regions triggers run and generates 95plus posts`() = runTest(testDispatcher) {
        val postedCrawlBodies = mutableListOf<String>()
        var triggerRunCalled = false

        val crawledJson = """
            {
              "ok": true,
              "data": {
                "items": [
                  {
                    "id": "crawl-2026-9901",
                    "case_no": "2026타경9901",
                    "auction_dedup_key": "ca-crawl-9901",
                    "court_name": "서울동부지방법원",
                    "address": "서울특별시 송파구 잠실동 잠실엘스 118동 502호",
                    "property_type": "APT_OFFICETEL",
                    "appraisal_krw": 2200000000,
                    "min_bid_krw": 1760000000,
                    "discount_rate_pct": 80,
                    "base_right_date": "2021-01-12"
                  }
                ]
              }
            }
        """.trimIndent()

        val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            val req = chain.request()
            val path = req.url.encodedPath
            when {
                path.endsWith("/api/v1/blog/assets/feed") ->
                    jsonResponse(req, 200, """{"ok":true,"data":{"items":[]}}""")
                path.endsWith("/api/v1/blog/crawl-and-sync") -> {
                    postedCrawlBodies += readRequestBody(req)
                    jsonResponse(req, 200, crawledJson)
                }
                path.endsWith("/api/v1/blog/triggers/run") -> {
                    triggerRunCalled = true
                    jsonResponse(req, 200, """{"ok":true,"processed":1}""")
                }
                path.endsWith("/api/v1/blog/analysis-feed") ->
                    jsonResponse(req, 200, crawledJson)
                path.endsWith("/api/v1/blog/triggers/status") ->
                    jsonResponse(req, 200, """{"ok":true,"data":{"triggers":{"pending":0,"processed":15,"failed":0},"snapshots":{"count":10}}}""")
                path.endsWith("/api/v1/blog/queue") ->
                    jsonResponse(req, 200, """{"ok":true,"data":{"items":[]}}""")
                else -> jsonResponse(req, 200, """{"ok":true}""")
            }
        }).build()

        val api = CloudflareAndDdakchalApi(client = client, ioDispatcher = testDispatcher)
        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            api = api,
            postWarehouseStore = createWarehouseStore(this)
        )
        val vm = StudioViewModel(
            repository = repo,
            aiAdvisor = offlineAdvisor,
            naverApiClient = NaverRealEstateApiClient(ioDispatcher = testDispatcher)
        )

        vm.applyInlineServerAuthAndSave(
            apiBase = "https://ddakchal.pages.dev",
            syncToken = "todak-admin-secret",
            cfAccountId = "",
            cfD1DatabaseId = "",
            cfApiToken = ""
        )
        vm.toggleTodakAdminRegion("부산")
        vm.setTodakAdminCrawlLimit(3)
        vm.runTodakFullAdminPipeline()

        assertTrue(triggerRunCalled)
        assertTrue(postedCrawlBodies.isNotEmpty())
        val crawlBodyJson = JSONObject(postedCrawlBodies.first())
        val regionsArr = crawlBodyJson.getJSONArray("regions")
        val regionsList = (0 until regionsArr.length()).map { regionsArr.getString(it) }
        assertTrue(regionsList.containsAll(listOf("서울", "경기", "부산")))

        val state = vm.uiState.value
        assertTrue(state.todakAdmin.generatedPostIds.isNotEmpty())
        val generatedSlot = state.slots.firstOrNull { it.auctionCase.caseId == "2026타경9901" }
        assertTrue(generatedSlot != null)
        assertTrue(state.warehouseArticles.any { it.slot.auctionCase.caseId == "2026타경9901" && it.evaluation.totalScore >= 95 })
        assertTrue(state.topicSelectionBoard.candidates.any { it.caseId == "2026타경9901" })
        assertTrue(state.legalVerificationByCaseId.containsKey("2026타경9901"))
    }

    @Test
    fun `discoverTodakServerDb reports honest error status on 401 with zero fake items`() = runTest(testDispatcher) {
        val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            jsonResponse(chain.request(), 401, """{"error":"unauthorized"}""")
        }).build()

        val api = CloudflareAndDdakchalApi(client = client, ioDispatcher = testDispatcher)
        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            api = api,
            postWarehouseStore = createWarehouseStore(this)
        )
        val vm = StudioViewModel(
            repository = repo,
            aiAdvisor = offlineAdvisor,
            naverApiClient = NaverRealEstateApiClient(ioDispatcher = testDispatcher)
        )

        vm.applyInlineServerAuthAndSave(
            apiBase = "https://ddakchal.pages.dev",
            syncToken = "bad-token",
            cfAccountId = "",
            cfD1DatabaseId = "",
            cfApiToken = ""
        )
        vm.discoverTodakServerDb()

        val admin = vm.uiState.value.todakAdmin
        assertTrue(admin.discoveredItems.isEmpty())
        assertTrue(admin.stepLogs.any { it.httpCode == 401 && it.status == AdminPipelineStageStatus.ERROR })
        assertFalse(vm.uiState.value.telemetry.ddakchalConnected)
    }

    @Test
    fun `DirectChannelPublishClient embeds evidence html and reports CF_D1_SAVED_WORKER_ROUTE_NEEDED when blog route 404`() = runTest(testDispatcher) {
        val d1Bodies = mutableListOf<String>()
        val requestedUrls = mutableListOf<String>()

        val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            val req = chain.request()
            val urlStr = req.url.toString()
            requestedUrls += urlStr
            when {
                urlStr.contains("/d1/database/") -> {
                    d1Bodies += readRequestBody(req)
                    jsonResponse(req, 200, """{"success":true,"result":[{"results":[]}]}""")
                }
                urlStr.contains("/blog/") -> {
                    jsonResponse(req, 404, "Not Found")
                }
                else -> jsonResponse(req, 200, "OK")
            }
        }).build()

        val d1Api = CloudflareAndDdakchalApi(client = client, ioDispatcher = testDispatcher)
        val publishClient = DirectChannelPublishClient(
            httpClient = client,
            ioDispatcher = testDispatcher,
            d1Api = d1Api
        )
        val cfg = ServerAutomationConfig(
            publishTargetNaver = false,
            publishTargetTistory = false,
            publishTargetCloudflare = true,
            cloudflareSiteBase = "https://aution.codeb.app",
            cloudflareBlogPath = "/blog",
            cfAccountId = "acc-test",
            cfD1DatabaseId = "d1-test",
            cfApiToken = "cf-tok-test"
        )
        val slot = TestFixtures.sampleSlots(cfg).first()

        val report = publishClient.publishSelectedChannelsAndVerify(slot, cfg)
        val cfOutcome = report.outcomes.first { it.platform == ChannelPlatform.CLOUDFLARE }

        assertEquals("CF_D1_SAVED_WORKER_ROUTE_NEEDED", cfOutcome.executedMethod)
        assertFalse(cfOutcome.reachableOk)
        assertEquals(404, cfOutcome.httpStatusCode)
        assertTrue(requestedUrls.any { it == "https://aution.codeb.app/blog/${slot.auctionCase.dedupKey}" })

        val payloadJson = d1Bodies.joinToString("\n")
        assertTrue(payloadJson.contains("court-photos"))
        assertTrue(payloadJson.contains("registry-table"))
        assertTrue(payloadJson.contains("visit-logs"))
    }
}
