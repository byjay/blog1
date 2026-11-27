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
import com.example.engine.ChannelIndexRiskEngine
import com.example.engine.DefaultSpecDataFactory
import com.example.model.ChannelPlatform
import com.example.model.ChannelPublishStat
import com.example.model.DailyPublishVolumePoint
import com.example.model.GeneratedPostSlot
import com.example.model.LowQualityRiskLevel
import com.example.model.PostStatus
import com.example.model.ProFunnelConfig
import com.example.model.PublishLogEntry
import com.example.model.QueueAnalyticsSummary
import com.example.model.QueueSyncState
import com.example.model.ServerAutomationConfig
import com.example.model.VisitorTrafficLog
import com.example.ui.StudioViewModel
import com.example.ui.screens.RechartsDashboardHtmlBuilder
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
class QueueRechartsAndRiskAutoPauseTest {

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
        val file = tempFolder.newFile("qr_cfg_${System.nanoTime()}.preferences_pb")
        return ServerConfigStore(PreferenceDataStoreFactory.create(scope = scope) { file })
    }

    private fun createWarehouseStore(scope: TestScope): PostWarehouseStore {
        val file = tempFolder.newFile("qr_wh_${System.nanoTime()}.preferences_pb")
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
    fun `retrofit blog queue sync parses real data with dual auth headers and drives QueueSyncState Loaded`() = runTest(testDispatcher) {
        val seenAuth = mutableListOf<String>()
        val seenSyncToken = mutableListOf<String>()
        val seenPaths = mutableListOf<String>()

        val queuePayload = """
            {
              "ok": true,
              "data": {
                "items": [
                  {
                    "id": "q-101",
                    "caseNo": "2025타경1101",
                    "channel": "naver",
                    "status": "PUBLISHED",
                    "scheduledAt": "2026-11-23T09:10:00Z",
                    "blogPostUrl": "https://blog.naver.com/my_blog/101"
                  },
                  {
                    "id": "q-102",
                    "caseNo": "2025타경1102",
                    "channel": "tistory",
                    "status": "FAILED",
                    "scheduledAt": "2026-11-23T11:20:00Z",
                    "lastError": "HTTP 403"
                  },
                  {
                    "id": "q-103",
                    "caseNo": "2025타경1103",
                    "channel": "cloudflare",
                    "status": "PUBLISHED",
                    "scheduledAt": "2026-11-24T08:00:00Z",
                    "blogPostUrl": "https://aution.codeb.app/blog/2025-1103"
                  },
                  {
                    "id": "q-104",
                    "caseNo": "2025타경1104",
                    "channel": "naver",
                    "status": "QUEUED",
                    "scheduledAt": "2026-11-24T13:00:00Z"
                  }
                ]
              }
            }
        """.trimIndent()

        val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            val req = chain.request()
            seenPaths += req.url.encodedPath
            seenAuth += req.header("Authorization").orEmpty()
            seenSyncToken += req.header("X-Crawler-Sync-Token").orEmpty()
            if (req.url.encodedPath.endsWith("/api/v1/blog/queue")) {
                jsonResponse(req, 200, queuePayload)
            } else {
                jsonResponse(req, 200, """{"ok":true}""")
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

        // Initial state must have zero fake logs and QueueSyncState.Idle
        assertTrue(vm.uiState.value.publishLogs.isEmpty())
        assertTrue(vm.uiState.value.queueSyncState is QueueSyncState.Idle)
        assertEquals(0, vm.uiState.value.queueAnalytics.totalSlots)

        vm.updateConfig(
            vm.uiState.value.config.copy(
                ddakchalApiBase = "https://ddakchal.pages.dev",
                crawlerSyncToken = "tok-real-777"
            )
        )
        vm.syncRealBlogQueueWithRetrofit(limit = 50, announceBanner = true)

        assertTrue(seenPaths.any { it.endsWith("/api/v1/blog/queue") })
        assertTrue(seenAuth.any { it == "Bearer tok-real-777" })
        assertTrue(seenSyncToken.any { it == "tok-real-777" })

        val state = vm.uiState.value
        assertTrue("Expected Loaded state, got ${state.queueSyncState}", state.queueSyncState is QueueSyncState.Loaded)
        val loaded = state.queueSyncState as QueueSyncState.Loaded
        assertEquals(4, loaded.items.size)
        assertEquals(4, state.queueAnalytics.totalSlots)
        assertEquals(2, state.queueAnalytics.publishedCount)
        assertEquals(1, state.queueAnalytics.failedOrHandoffCount)
        assertEquals(1, state.queueAnalytics.queuedCount)
        assertEquals(67, state.queueAnalytics.successRatePct)
        assertEquals(33, state.queueAnalytics.failureRatePct)
        assertEquals(2, state.queueAnalytics.dailySeries.size)
        assertEquals("11-23", state.queueAnalytics.dailySeries[0].dateLabel)
        assertEquals("11-24", state.queueAnalytics.dailySeries[1].dateLabel)
        assertEquals(1, state.queueAnalytics.channelStats.first { it.platform == ChannelPlatform.CLOUDFLARE }.publishedCount)
    }

    @Test
    fun `retrofit queue returns honest Error on 401 and Empty on zero items with no dummy fallback`() = runTest(testDispatcher) {
        var nextCode = 401
        var nextBody = """{"error":"unauthorized"}"""

        val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            jsonResponse(chain.request(), nextCode, nextBody)
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
        vm.updateConfig(
            vm.uiState.value.config.copy(
                ddakchalApiBase = "https://ddakchal.pages.dev",
                crawlerSyncToken = ""
            )
        )

        vm.syncRealBlogQueueWithRetrofit()
        val errState = vm.uiState.value.queueSyncState
        assertTrue(errState is QueueSyncState.Error)
        errState as QueueSyncState.Error
        assertEquals(401, errState.httpStatusCode)
        assertTrue(errState.tokenMissing)
        assertEquals(0, vm.uiState.value.queueAnalytics.totalSlots)

        // Now server returns 200 with empty items -> QueueSyncState.Empty
        nextCode = 200
        nextBody = """{"ok":true,"data":{"items":[]}}"""
        vm.syncRealBlogQueueWithRetrofit()
        assertTrue(vm.uiState.value.queueSyncState is QueueSyncState.Empty)
        assertEquals(0, vm.uiState.value.queueAnalytics.totalSlots)
    }

    @Test
    fun `RechartsDashboardHtmlBuilder outputs valid Recharts markup and prevents script or secret leaks`() {
        val summary = QueueAnalyticsSummary(
            totalSlots = 5,
            publishedCount = 3,
            queuedCount = 1,
            failedOrHandoffCount = 1,
            draftCount = 0,
            successRatePct = 75,
            failureRatePct = 25,
            dailySeries = listOf(DailyPublishVolumePoint("11-24", publishedCount = 3, queuedCount = 1, failedCount = 1)),
            channelStats = listOf(
                ChannelPublishStat(ChannelPlatform.NAVER, publishedCount = 2, queuedCount = 0, failedCount = 1),
                ChannelPublishStat(ChannelPlatform.TISTORY, publishedCount = 0, queuedCount = 1, failedCount = 0),
                ChannelPublishStat(ChannelPlatform.CLOUDFLARE, publishedCount = 1, queuedCount = 0, failedCount = 0)
            )
        )

        val html = RechartsDashboardHtmlBuilder.buildHtml(
            analytics = summary,
            syncedAt = "</script><script>alert(1)</script>2026-11-24",
            sourceEndpoint = "https://ddakchal.pages.dev/api/v1/blog/queue?token=super-secret-xyz"
        )

        assertTrue(html.contains("recharts@2.12.7/umd/Recharts.js"))
        assertTrue(html.contains("R.BarChart"))
        assertTrue(html.contains("R.PieChart"))
        assertFalse("Query params / tokens must be stripped", html.contains("super-secret-xyz"))
        assertFalse("Raw closing script tag must be escaped in JSON payload", html.contains("</script><script>alert(1)</script>"))
    }

    @Test
    fun `DirectChannelPublishClient does not fake PUBLISHED when blog homepage returns HTTP 200`() = runTest(testDispatcher) {
        val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            val req = chain.request()
            // Simulate blog homepage returning 200 HTML for GET, with no worker relay configured
            Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("<html><body>Naver Blog Home</body></html>".toResponseBody("text/html".toMediaType()))
                .build()
        }).build()

        val directClient = DirectChannelPublishClient(httpClient = client, ioDispatcher = testDispatcher)
        val cfg = ServerAutomationConfig(
            naverBlogId = "my_auction_blog",
            naverWorkerRelayEndpoint = "",
            tistoryBlogUrl = "https://my-auction.tistory.com",
            cloudflareSiteBase = "https://aution.codeb.app"
        )
        val slot = DefaultSpecDataFactory.buildInitialDaily5Slots(cfg).first()

        val naverReport = directClient.publishNaverDirectAndVerify(slot, cfg, customNaverPostUrl = "")
        assertFalse("Homepage GET 200 without relay/postUrl must NOT pass", naverReport.naverDirectPassed)
        val naverOutcome = naverReport.outcomes.first { it.platform == ChannelPlatform.NAVER }
        assertFalse(naverOutcome.reachableOk)
        assertEquals("", naverOutcome.publishedPostUrl)

        val multiReport = directClient.publishSelectedChannelsAndVerify(slot, cfg, customNaverPostUrl = "")
        val tistoryOutcome = multiReport.outcomes.first { it.platform == ChannelPlatform.TISTORY }
        assertFalse("Tistory homepage GET 200 without relay must NOT pass", tistoryOutcome.reachableOk)
        assertEquals("", tistoryOutcome.publishedPostUrl)
    }

    @Test
    fun `ChannelIndexRiskEngine detects HIGH_RISK and StudioViewModel pauses and blocks auto-publish until resumed`() = runTest(testDispatcher) {
        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            postWarehouseStore = createWarehouseStore(this)
        )
        val vm = StudioViewModel(
            repository = repo,
            aiAdvisor = offlineAdvisor,
            naverApiClient = NaverRealEstateApiClient(ioDispatcher = testDispatcher)
        )

        val baseConfig = ServerAutomationConfig(
            enableExternalLinkNaver = false,
            autoPauseOnHighRisk = true,
            dailyPostLimit = 8
        )
        val baseSlot = DefaultSpecDataFactory.buildInitialDaily5Slots(baseConfig).first()
        val riskyNaverVariant = baseSlot.variants[ChannelPlatform.NAVER]!!.copy(
            bodyText = "반드시 클릭 https://spam-external.example.com 지금 확인하세요!",
            bodyHtml = "<p>https://spam-external.example.com</p>"
        )
        val riskySlot = baseSlot.copy(
            variants = baseSlot.variants + (ChannelPlatform.NAVER to riskyNaverVariant)
        )
        val failingLogs = listOf(
            PublishLogEntry("f1", riskySlot.postId, riskySlot.auctionCase.caseId, ChannelPlatform.NAVER, PostStatus.HANDOFF, null, "CAPTCHA", "CAPTCHA", 1, "2026-11-24 10:00:00"),
            PublishLogEntry("f2", riskySlot.postId, riskySlot.auctionCase.caseId, ChannelPlatform.NAVER, PostStatus.FAILED, null, "403", null, 2, "2026-11-24 09:00:00"),
            PublishLogEntry("f3", riskySlot.postId, riskySlot.auctionCase.caseId, ChannelPlatform.NAVER, PostStatus.FAILED, null, "500", null, 3, "2026-11-24 08:00:00")
        )
        val droppingVisitorLogs = listOf(
            VisitorTrafficLog("2026-11-24", 40, 10, 15, 65, 50, 0, 0, 0L),
            VisitorTrafficLog("2026-11-23", 400, 220, 90, 710, 180, 5, 2, 50_000L)
        )

        val riskReport = ChannelIndexRiskEngine.evaluate(
            config = baseConfig,
            slots = listOf(riskySlot),
            publishLogs = failingLogs,
            visitorLogs = droppingVisitorLogs
        )
        assertEquals(LowQualityRiskLevel.HIGH_RISK, riskReport.overallRisk)
        assertTrue(riskReport.autoPublishPaused)
        assertNotNull(riskReport.pauseReason)
        assertTrue(riskReport.triggeredAlerts.isNotEmpty())

        // Now trigger HIGH_RISK inside StudioViewModel via repeated HANDOFF/CAPTCHA alerts + unsafe settings
        vm.updateConfig(
            vm.uiState.value.config.copy(
                autoPauseOnHighRisk = true,
                enableExternalLinkNaver = true,
                dailyPostLimit = 9
            )
        )
        vm.triggerCaptchaHandoffSimulation(ChannelPlatform.NAVER)
        vm.triggerCaptchaHandoffSimulation(ChannelPlatform.NAVER)
        vm.evaluateLowQualityRiskAndPauseIfNeeded()

        val pausedState = vm.uiState.value
        assertTrue("Expected autoPublishPausedByRisk = true", pausedState.autoPublishPausedByRisk)
        assertEquals(LowQualityRiskLevel.HIGH_RISK, pausedState.lowQualityRiskReport?.overallRisk)

        // Attempt to generate weekly batch while paused -> must block!
        vm.generateWeeklyBatchAndSchedule(perDay = 4)
        assertTrue(vm.uiState.value.errorMessage?.contains("중지") == true)
        assertEquals(null, vm.uiState.value.weeklyBatchSummary)

        // Attempt to instant create & auto-register while paused -> must block!
        val beforeCount = vm.uiState.value.slots.size
        vm.createHighScoreAndAutoRegister()
        assertEquals(beforeCount, vm.uiState.value.slots.size)

        // Resume after mitigation
        vm.resumeAutoPublishAfterRiskMitigation()
        assertFalse(vm.uiState.value.autoPublishPausedByRisk)
    }
}
