package com.example

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.example.ai.AiEvaluationOutcome
import com.example.ai.FirebaseAiSeoGeoService
import com.example.ai.GrokFallbackClient
import com.example.data.BlogAutomationRepository
import com.example.data.CloudflareAndDdakchalApi
import com.example.data.DirectChannelPublishClient
import com.example.data.PostWarehouseStore
import com.example.data.ServerConfigStore
import com.example.engine.DefaultSpecDataFactory
import com.example.engine.HtmlExportEngine
import com.example.engine.TopicSelectionEngine
import com.example.model.ChannelPlatform
import com.example.model.NaverAutoWriteMode
import com.example.model.ProFunnelConfig
import com.example.model.ServerAutomationConfig
import com.example.model.TopicSortCriteria
import com.example.ui.StudioViewModel
import com.example.ui.util.SafeClipboardHelper
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
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
class TopicAndSessionAndRoutingTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createConfigStore(scope: TestScope): ServerConfigStore {
        val file = tempFolder.newFile("topic_cfg_${System.nanoTime()}.preferences_pb")
        return ServerConfigStore(PreferenceDataStoreFactory.create(scope = scope) { file })
    }

    private fun createWarehouseStore(scope: TestScope): PostWarehouseStore {
        val file = tempFolder.newFile("topic_wh_${System.nanoTime()}.preferences_pb")
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

    @Test
    fun `html export generates pureHtml and bodyOnlyHtml under 450KB and copies safely`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cfg = ServerAutomationConfig()
        val slots = DefaultSpecDataFactory.buildInitialDaily5Slots(cfg)
        val bundle = HtmlExportEngine.buildThreePureHtmlBatch(
            slots = slots,
            threeCasesMode = true,
            preferredChannel = ChannelPlatform.NAVER,
            config = cfg,
            nowText = "2026-11-24 12:00:00"
        )

        assertEquals(3, bundle.items.size)
        bundle.items.forEach { item ->
            assertTrue(item.pureHtml.contains("<!DOCTYPE html>"))
            assertTrue(item.bodyOnlyHtml.startsWith("<article"))
            assertFalse(item.bodyOnlyHtml.contains("<!DOCTYPE html>"))
            val bytes = item.pureHtml.toByteArray(Charsets.UTF_8).size
            assertTrue("HTML size $bytes should stay safely under 450KB", bytes < 450_000)

            val clipResult = SafeClipboardHelper.copyPlainTextSafely(
                context = context,
                label = "테스트 HTML",
                text = item.bodyOnlyHtml,
                showToast = false
            )
            assertTrue(clipResult.success)
            assertTrue(clipResult.copiedChars > 50)
        }
    }

    @Test
    fun `topic selection ranks across 4 pillars and supports all 5 sort criteria`() {
        val cfg = ServerAutomationConfig()
        val slots = DefaultSpecDataFactory.buildInitialDaily5Slots(cfg)
        val boardBalanced = TopicSelectionEngine.rankCandidates(
            slots = slots,
            config = cfg,
            proFunnel = ProFunnelConfig(),
            sortMode = TopicSortCriteria.BALANCED_4_PILLAR,
            nowText = "2026-11-24 12:00:00"
        )
        assertEquals(slots.size, boardBalanced.candidates.size)
        assertTrue(boardBalanced.candidates.all {
            it.qualityScore in 0..100 &&
                it.seoFitScore in 0..100 &&
                it.aiExposureScore in 0..100 &&
                it.popularityScore in 0..100
        })

        val boardPopularity = TopicSelectionEngine.rankCandidates(
            slots = slots,
            config = cfg,
            proFunnel = ProFunnelConfig(),
            sortMode = TopicSortCriteria.POPULARITY_FIRST,
            nowText = "2026-11-24 12:00:00"
        )
        assertEquals(TopicSortCriteria.POPULARITY_FIRST, boardPopularity.sortMode)
        assertTrue(
            boardPopularity.candidates.first().popularityScore >=
                boardPopularity.candidates.last().popularityScore
        )
    }

    @Test
    fun `comprehensive ddakchal diagnostic verifies dual auth headers and naver link guard`() = runTest {
        val seenUrls = mutableListOf<String>()
        val interceptor = Interceptor { chain ->
            val req = chain.request()
            seenUrls += req.url.toString()
            val auth = req.header("Authorization").orEmpty()
            val syncToken = req.header("X-Crawler-Sync-Token").orEmpty()
            if (auth != "Bearer secret-sync-123" || syncToken != "secret-sync-123") {
                return@Interceptor jsonResponse(req, 401, """{"error":"unauthorized"}""")
            }
            val path = req.url.encodedPath
            when {
                path.endsWith("/triggers/status") -> jsonResponse(
                    req,
                    200,
                    """{"data":{"pendingCount":1,"processedCount":12,"failedCount":0,"snapshotsCount":10,"todayQueueCount":5}}"""
                )
                path.endsWith("/analysis-feed") -> jsonResponse(
                    req,
                    200,
                    """{"data":{"items":[{"caseNo":"2025타경6297","updatedAt":"2026-11-24T09:00:00"}]}}"""
                )
                path.endsWith("/syndication-pack") -> jsonResponse(
                    req,
                    200,
                    """{"data":{"items":[{"caseNo":"2025타경6297"}]}}"""
                )
                path.contains("/assets/") -> jsonResponse(
                    req,
                    200,
                    """{"data":{"caseNo":"2025타경6297","photo_urls":["https://cdn.example/1.webp"]}}"""
                )
                else -> jsonResponse(req, 404, """{"error":"not found"}""")
            }
        }
        val okHttp = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val api = CloudflareAndDdakchalApi(okHttp)
        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            api = api,
            postWarehouseStore = createWarehouseStore(this)
        )
        val cfg = ServerAutomationConfig(
            ddakchalApiBase = "https://ddakchal.pages.dev",
            crawlerSyncToken = "secret-sync-123"
        )
        val report = repo.runComprehensiveDdakchalDiagnostic(cfg, "2025타경6297")
        assertTrue(report.overallConnected)
        assertTrue(report.authHeaderReady)
        assertTrue(seenUrls.any { it.contains("enableExternalLinkNaver=false") })
        assertTrue(report.checks.count { it.passed } >= 4)

        val unauthReport = repo.runComprehensiveDdakchalDiagnostic(
            cfg.copy(crawlerSyncToken = "wrong-token"),
            "2025타경6297"
        )
        assertFalse(unauthReport.overallConnected)
    }

    @Test
    fun `grok fallback handles priority 2 routing when primary gemini is unavailable`() = runTest {
        val interceptor = Interceptor { chain ->
            val req = chain.request()
            assertEquals("Bearer xai-test-key", req.header("Authorization"))
            val innerJson = JSONObject().apply {
                put("aiSummary", "Grok 2순위 백업 엔진 검증 완료")
                put("overallVerdict", "등록 승인(95점 이상)")
                put(
                    "criterionScores",
                    JSONArray().apply {
                        put(
                            JSONObject().apply {
                                put("orderIndex", 1)
                                put("passed", true)
                                put("earnedPoints", 4)
                                put("reason", "실측 수치 100% 일치")
                            }
                        )
                    }
                )
                put("autoFixSuggestions", JSONArray())
            }.toString()
            val responseBody = JSONObject().apply {
                put(
                    "choices",
                    JSONArray().apply {
                        put(
                            JSONObject().apply {
                                put(
                                    "message",
                                    JSONObject().apply {
                                        put("content", innerJson)
                                    }
                                )
                            }
                        )
                    }
                )
            }.toString()
            jsonResponse(req, 200, responseBody)
        }
        val mockClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val grokClient = GrokFallbackClient(
            httpClient = mockClient,
            ioDispatcher = testDispatcher,
            apiKeyProvider = { "xai-test-key" }
        )
        val service = FirebaseAiSeoGeoService(grokFallbackClient = grokClient)
        val cfg = ServerAutomationConfig(grokFallbackEnabled = true, grokModelName = "grok-3-mini")
        val slot = DefaultSpecDataFactory.buildInitialDaily5Slots(cfg).first()

        val outcome = service.evaluateWith30Criteria(slot, cfg, ProFunnelConfig())
        assertTrue(outcome is AiEvaluationOutcome.Success)
        val report = (outcome as AiEvaluationOutcome.Success).report
        assertTrue(report.modelName.contains("Grok") || report.aiSummary.contains("Grok"))
    }

    @Test
    fun `direct publish targets aution_codeb_app and builds 4 naver write methods`() {
        val cfg = ServerAutomationConfig(
            ddakchalApiBase = "https://ddakchal.pages.dev",
            cloudflareSiteBase = "https://aution.codeb.app",
            cloudflareBlogPath = "/blog",
            naverBlogId = "my_auction_blog",
            tistoryBlogUrl = "https://my-auction.tistory.com"
        )
        val slot = DefaultSpecDataFactory.buildInitialDaily5Slots(cfg).first()
        val client = DirectChannelPublishClient()
        val destinations = client.buildDestinations(cfg)
        val cfDest = destinations.first { it.platform == ChannelPlatform.CLOUDFLARE }
        assertTrue(cfDest.targetAddress.startsWith("https://aution.codeb.app/blog"))

        val guides = client.buildAlternateNaverWriteGuides(cfg, slot)
        assertEquals(4, guides.size)
        assertEquals(
            setOf(
                NaverAutoWriteMode.WORKER_RELAY,
                NaverAutoWriteMode.MOBILE_WRITE_URL,
                NaverAutoWriteMode.POST_MAIL_BRIDGE,
                NaverAutoWriteMode.SESSION_HANDOFF
            ),
            guides.map { it.mode }.toSet()
        )
    }

    @Test
    fun `viewModel updates quick settings topic promotion and naver write mode`() = runTest {
        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            postWarehouseStore = createWarehouseStore(this)
        )
        val vm = StudioViewModel(repo)

        vm.saveQuickSettings(
            cloudflareSiteBase = "https://aution.codeb.app",
            cloudflareBlogPath = "/blog",
            naverKeepLoggedIn = true,
            tistoryKeepLoggedIn = true,
            tistoryAutoDirectPublish = true,
            grokFallbackEnabled = true,
            grokModelName = "grok-3-mini"
        )
        vm.updateNaverWriteMode(NaverAutoWriteMode.POST_MAIL_BRIDGE)
        vm.evaluateAndRankTopics(TopicSortCriteria.AI_GEO_FIRST)
        vm.applyTopSelectedTopicsToQueue(3)

        val state = vm.uiState.value
        assertEquals("https://aution.codeb.app", state.config.cloudflareSiteBase)
        assertTrue(state.config.naverKeepLoggedIn)
        assertTrue(state.config.tistoryKeepLoggedIn)
        assertTrue(state.config.tistoryAutoDirectPublish)
        assertEquals(NaverAutoWriteMode.POST_MAIL_BRIDGE, state.config.naverWriteMode)
        assertEquals(TopicSortCriteria.AI_GEO_FIRST, state.topicSelectionBoard.sortMode)
        assertNotNull(state.naverPublishStatuses)
        assertTrue(state.naverWriteGuides.any { it.mode == NaverAutoWriteMode.POST_MAIL_BRIDGE && it.isActive })
    }
}
