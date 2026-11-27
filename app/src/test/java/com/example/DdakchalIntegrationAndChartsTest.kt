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
import com.example.engine.Article30PointEvaluator
import com.example.engine.DefaultSpecDataFactory
import com.example.model.AiEvaluationReport
import com.example.model.ChannelPlatform
import com.example.model.GeneratedPostSlot
import com.example.model.PostStatus
import com.example.model.ProFunnelConfig
import com.example.model.PropertyType
import com.example.model.PublishLogEntry
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
class DdakchalIntegrationAndChartsTest {

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
        val file = tempFolder.newFile("ddak_cfg_${System.nanoTime()}.preferences_pb")
        return ServerConfigStore(PreferenceDataStoreFactory.create(scope = scope) { file })
    }

    private fun createWarehouseStore(scope: TestScope): PostWarehouseStore {
        val file = tempFolder.newFile("ddak_wh_${System.nanoTime()}.preferences_pb")
        return PostWarehouseStore(PreferenceDataStoreFactory.create(scope = scope) { file })
    }

    private val offlineAdvisor = object : AiSeoGeoAdvisor {
        override suspend fun evaluateWith30Criteria(
            slot: GeneratedPostSlot,
            config: ServerAutomationConfig,
            proFunnel: ProFunnelConfig
        ): AiEvaluationOutcome {
            val eval = Article30PointEvaluator.evaluate(slot, config, proFunnel)
            return AiEvaluationOutcome.Success(
                AiEvaluationReport(
                    caseId = slot.auctionCase.caseId,
                    modelName = "test-local-eval",
                    totalScore = eval.totalScore,
                    passedCount = eval.passedCount,
                    aiSummary = "테스트 평가",
                    overallVerdict = "등록 가능",
                    criteriaResults = eval.items,
                    autoFixSuggestions = emptyList(),
                    evaluatedAt = eval.evaluatedAt
                )
            )
        }
    }

    private data class RecordedHttpCall(
        val method: String,
        val url: String,
        val authHeader: String?,
        val crawlerHeader: String?,
        val bodyText: String
    )

    private fun sampleAnalysisFeedJson(): String = """
        {
          "ok": true,
          "data": {
            "items": [
              {
                "id": "queue-8841",
                "case_no": "2025타경8841",
                "auction_dedup_key": "seoul-2025-8841",
                "publish_date": "2026-11-24",
                "updated_at": "2026-11-24T09:15:30Z",
                "slot_index": 1,
                "status": "READY",
                "property_type": "APT_OFFICETEL",
                "category": "LAND",
                "court_name": "서울중앙지방법원 경매5계",
                "address": "서울특별시 서초구 반포동 18-1 래미안",
                "title": "서초 반포동 토지 급매 참고용",
                "appraisal_krw": 1850000000,
                "min_bid_krw": 1184000000,
                "discount_rate_pct": 64,
                "bid_count": 2,
                "deposit_rate_pct": 10,
                "auction_date": "2026-12-18",
                "base_right_date": "2020-03-10",
                "dividend_deadline": "2026-02-20",
                "claim_amount_krw": 620000000,
                "possession_summary": "채무자 겸 소유자 점유",
                "tenant_summary": "전입 세대열람 내역 없음",
                "assumed_rights_text": "말소기준권리 이후 후순위 소멸",
                "real_trade_median_krw": 1790000000,
                "stats_3y_rate_pct": 88.4,
                "conservative_bid_krw": 1240000000,
                "balanced_bid_krw": 1310000000,
                "aggressive_bid_krw": 1390000000,
                "photo_urls": [
                  "https://r2.ddakchal.pages.dev/photos/2025-8841-1.webp",
                  "https://r2.ddakchal.pages.dev/photos/2025-8841-2.webp"
                ],
                "blog_analysis_data": {
                  "hookHeadline": "서초 반포 대단지 2회 유찰 실측 마진 분석",
                  "summary3Lines": [
                    "감정가 18억 5천만 원 대비 최저가 11억 8,400만 원(64%)",
                    "후순위 권리 전부 소멸 구조 · 명도 리스크 낮음",
                    "최근 실거래 중위가 17.9억 기준 약 4.8억 안전마진"
                  ],
                  "keyMetricsChartData": {
                    "appraisalKrw": 1850000000,
                    "minBidKrw": 1184000000,
                    "realTradeMedianKrw": 1790000000,
                    "conservativeBidKrw": 1240000000,
                    "balancedBidKrw": 1310000000,
                    "aggressiveBidKrw": 1390000000,
                    "discountRatePct": 64,
                    "stats3yRatePct": 88.4
                  },
                  "riskAndOpportunity": {
                    "riskSummary": "관리비 체납 여부 현장 확인 필요",
                    "opportunitySummary": "인근 실거래 대비 6억 이상 가격 메리트",
                    "evictionDifficulty": "낮음",
                    "tenantRiskNote": "대항력 임차인 없음"
                  },
                  "qaBlocks": [
                    {
                      "question": "입찰보증금은 얼마인가요?",
                      "answer": "최저가의 10%인 1억 1,840만 원입니다."
                    }
                  ],
                  "freshness": {
                    "updatedAt": "2026-11-24T09:15:30Z",
                    "source": "ddakchal-d1-crawler",
                    "courtPhotoCount": 2
                  }
                },
                "channelVariants": {
                  "naver": {
                    "categoryCode": "COMMERCIAL_RETAIL",
                    "categoryLabel": "경매분석",
                    "title": "서초구 반포동 2025타경8841 아파트 경매 권리분석",
                    "bodyText": "감정가 18억5천, 최저가 11억8400만 원입니다. 자세히 보기는 https://spam.example.com/out 를 누르지 마세요.",
                    "tags": ["딱찰", "반포경매", "2025타경8841"]
                  }
                }
              }
            ]
          }
        }
    """.trimIndent()

    private fun sampleTriggerStatusJson(): String = """
        {
          "ok": true,
          "data": {
            "triggers": { "pending": 3, "processed": 42, "failed": 1 },
            "snapshots": { "count": 18, "latestAt": "2026-11-24T09:15:00Z" },
            "queue": { "todayCount": 5, "generatedPostsCount": 126 },
            "recentEvents": [
              { "id": "ev-1", "case_no": "2025타경8841", "event_type": "CRAWL_SYNC", "status": "DONE", "created_at": "09:15:00" }
            ]
          }
        }
    """.trimIndent()

    private fun readRequestBody(req: Request): String {
        val body = req.body ?: return ""
        val buf = Buffer()
        body.writeTo(buf)
        return buf.readUtf8()
    }

    @Test
    fun ddakchalApi_sendsDualAuthHeadersAndCallsAllEndpointsWithLatency() = runTest(testDispatcher) {
        val calls = mutableListOf<RecordedHttpCall>()
        val interceptor = Interceptor { chain ->
            val req = chain.request()
            val url = req.url.toString()
            val reqBody = readRequestBody(req)
            calls += RecordedHttpCall(
                method = req.method,
                url = url,
                authHeader = req.header("Authorization"),
                crawlerHeader = req.header("X-Crawler-Sync-Token"),
                bodyText = reqBody
            )
            val responseJson = when {
                url.contains("/triggers/status") -> sampleTriggerStatusJson()
                else -> sampleAnalysisFeedJson()
            }
            Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(responseJson.toResponseBody("application/json".toMediaType()))
                .build()
        }

        val api = CloudflareAndDdakchalApi(
            client = OkHttpClient.Builder().addInterceptor(interceptor).build(),
            ioDispatcher = testDispatcher
        )

        val token = "test-crawler-sync-secret"
        val base = "https://ddakchal.pages.dev"

        val feedRes = api.fetchAnalysisFeed(
            apiBase = base,
            syncToken = token,
            limit = 10,
            since = "2026-11-23T00:00:00Z",
            category = "HOUSING",
            enableExternalLinkNaver = false
        )
        assertTrue(feedRes.isSuccess)
        assertTrue(feedRes.latencyMs > 0L)

        api.fetchSyndicationPack(base, token, limit = 5, date = "2026-11-24", enableExternalLinkNaver = false)
        api.fetchAssetByKey(base, token, keyOrCaseNo = "2025타경8841", refresh = true, enableExternalLinkNaver = false)
        api.fetchTriggerStatus(base, token)
        api.runTriggers(base, token, maxBatch = 15, autoFillDailyQueue = true, enableExternalLinkNaver = false)
        api.triggerCrawlAndSync(base, token, regions = listOf("서울"), maxBatch = 10, limit = 5, enableExternalLinkNaver = false)
        api.updateQueueItemStatus(
            apiBase = base,
            syncToken = token,
            itemId = "queue-8841",
            status = "PUBLISHED",
            blogId = "ddakchal_official",
            blogPostUrl = "https://blog.naver.com/ddakchal_official/12345"
        )

        assertEquals(7, calls.size)
        calls.forEach { call ->
            assertEquals("Bearer $token", call.authHeader)
            assertEquals(token, call.crawlerHeader)
        }

        val feedCall = calls[0]
        assertTrue(feedCall.url.contains("/api/v1/blog/analysis-feed"))
        assertTrue(feedCall.url.contains("since=2026-11-23T00%3A00%3A00Z") || feedCall.url.contains("since=2026-11-23T00:00:00Z"))
        assertTrue(feedCall.url.contains("category=HOUSING"))
        assertTrue(feedCall.url.contains("enableExternalLinkNaver=false"))

        assertTrue(calls[1].url.contains("/api/v1/blog/syndication-pack"))
        assertTrue(calls[2].url.contains("/api/v1/blog/assets/"))
        assertTrue(calls[3].url.contains("/api/v1/blog/triggers/status"))
        assertTrue(calls[4].url.contains("/api/v1/blog/triggers/run"))
        assertTrue(calls[5].url.contains("/api/v1/blog/crawl-and-sync"))

        val putCall = calls[6]
        assertEquals("PUT", putCall.method)
        assertTrue(putCall.url.contains("/api/v1/blog/queue/queue-8841"))
        assertTrue(putCall.bodyText.contains("PUBLISHED"))
        assertTrue(putCall.bodyText.contains("ddakchal_official"))
    }

    @Test
    fun repository_parsesPropertyTypePrecedenceAnalysisDataPhotosAndStripsNaverUrls() = runTest(testDispatcher) {
        val api = CloudflareAndDdakchalApi(
            client = OkHttpClient.Builder().addInterceptor { chain ->
                val url = chain.request().url.toString()
                val json = if (url.contains("/triggers/status")) sampleTriggerStatusJson() else sampleAnalysisFeedJson()
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(json.toResponseBody("application/json".toMediaType()))
                    .build()
            }.build(),
            ioDispatcher = testDispatcher
        )
        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            api = api,
            postWarehouseStore = createWarehouseStore(this)
        )
        val cfg = ServerAutomationConfig(
            ddakchalApiBase = "https://ddakchal.pages.dev",
            crawlerSyncToken = "tok-123",
            enableExternalLinkNaver = false
        )

        val feedOutcome = repo.syncAnalysisFeedWithTelemetry(cfg, limit = 10)
        assertTrue(feedOutcome.isSuccess)
        val slots = feedOutcome.data.orEmpty()
        assertEquals(1, slots.size)

        val slot = slots.first()
        // Explicit property_type (APT_OFFICETEL) must beat categoryCode (COMMERCIAL_RETAIL) and category (LAND)
        assertEquals(PropertyType.APT_OFFICETEL, slot.auctionCase.propertyType)
        assertEquals(1_850_000_000L, slot.auctionCase.appraisalPriceKrw)
        assertEquals(1_184_000_000L, slot.auctionCase.minBidPriceKrw)
        assertEquals(64, slot.auctionCase.discountRatePct)
        assertEquals(2, slot.courtPhotoUrls.size)
        assertTrue(slot.courtPhotoUrls.first().endsWith(".webp"))

        val analysis = slot.ddakchalAnalysisData
        assertNotNull(analysis)
        assertEquals("서초 반포 대단지 2회 유찰 실측 마진 분석", analysis!!.hookHeadline)
        assertEquals(3, analysis.summary3Lines.size)
        assertEquals(1_850_000_000L, analysis.keyMetricsChartData?.appraisalKrw)
        assertEquals(1, analysis.qaBlocks.size)
        assertEquals(2, analysis.freshness?.courtPhotoCount)

        // Naver variant bodyText must have URLs stripped when enableExternalLinkNaver=false
        val naverBody = slot.variants[ChannelPlatform.NAVER]?.bodyText.orEmpty()
        assertFalse(naverBody.contains("https://spam.example.com"))
        assertTrue(naverBody.contains("[프로필 링크 참조]"))

        // Trigger status parsing
        val triggerOutcome = repo.fetchTriggerStatusWithTelemetry(cfg)
        assertTrue(triggerOutcome.isSuccess)
        assertEquals(3, triggerOutcome.data?.pendingCount)
        assertEquals(42, triggerOutcome.data?.processedCount)
        assertEquals(5, triggerOutcome.data?.todayQueueCount)
    }

    @Test
    fun repository_computeQueueAnalyticsCalculatesDailyVolumeRatesAndChannels() = runTest(testDispatcher) {
        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            api = CloudflareAndDdakchalApi(ioDispatcher = testDispatcher),
            postWarehouseStore = createWarehouseStore(this)
        )
        val baseSlots = DefaultSpecDataFactory.buildInitialDaily5Slots(ServerAutomationConfig())
        val customSlots = listOf(
            baseSlots[0].copy(
                postId = "p-1",
                publishDate = "2026-11-23",
                status = PostStatus.PUBLISHED,
                publishedUrls = mapOf(
                    ChannelPlatform.NAVER to "https://blog.naver.com/a/1",
                    ChannelPlatform.TISTORY to "https://a.tistory.com/1",
                    ChannelPlatform.CLOUDFLARE to "https://ddakchal.pages.dev/blog/1"
                )
            ),
            baseSlots[1].copy(
                postId = "p-2",
                publishDate = "2026-11-23",
                status = PostStatus.FAILED
            ),
            baseSlots[2].copy(
                postId = "p-3",
                publishDate = "2026-11-24",
                status = PostStatus.QUEUED
            ),
            baseSlots[3].copy(
                postId = "p-4",
                publishDate = "2026-11-24",
                status = PostStatus.PUBLISHED,
                publishedUrls = mapOf(ChannelPlatform.NAVER to "https://blog.naver.com/a/4")
            )
        )
        val logs = listOf(
            PublishLogEntry(
                logId = "l-1",
                postId = "p-2",
                caseId = customSlots[1].auctionCase.caseId,
                platform = ChannelPlatform.TISTORY,
                status = PostStatus.FAILED,
                publishedUrl = null,
                errorMessage = "403",
                handoffReason = null,
                retryCount = 1,
                publishedAt = "2026-11-23 10:00:00"
            )
        )

        val summary = repo.computeQueueAnalytics(customSlots, logs)
        assertEquals(4, summary.totalSlots)
        assertEquals(2, summary.publishedCount)
        assertEquals(1, summary.queuedCount)
        assertEquals(1, summary.failedOrHandoffCount)
        // 2 published vs 1 failed -> decided=3 -> 67% success / 33% fail
        assertEquals(67, summary.successRatePct)
        assertEquals(33, summary.failureRatePct)
        assertEquals(2, summary.dailySeries.size)
        assertEquals("11-23", summary.dailySeries[0].dateLabel)
        assertEquals(1, summary.dailySeries[0].publishedCount)
        assertEquals(1, summary.dailySeries[0].failedCount)
        assertEquals("11-24", summary.dailySeries[1].dateLabel)
        assertEquals(1, summary.dailySeries[1].publishedCount)
        assertEquals(1, summary.dailySeries[1].queuedCount)
        assertEquals(3, summary.channelStats.size)
        assertEquals(2, summary.channelStats.first { it.platform == ChannelPlatform.NAVER }.publishedCount)
    }

    @Test
    fun studioViewModel_telemetryRefreshCrawlSyncAndReverseQueueSyncWorkEndToEnd() = runTest(testDispatcher) {
        val recordedUrls = mutableListOf<String>()
        val apiClient = OkHttpClient.Builder().addInterceptor { chain ->
            val req = chain.request()
            val url = req.url.toString()
            recordedUrls += "${req.method} $url"
            val body = when {
                url.contains("/triggers/status") -> sampleTriggerStatusJson()
                url.contains("/crawl-and-sync") || url.contains("/analysis-feed") || url.contains("/syndication-pack") ->
                    sampleAnalysisFeedJson()
                url.contains("/api/v1/blog/queue/") -> """{"ok":true}"""
                else -> """{"ok":true}"""
            }
            Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body.toResponseBody("application/json".toMediaType()))
                .build()
        }.build()

        val publishClient = DirectChannelPublishClient(
            httpClient = OkHttpClient.Builder().addInterceptor { chain ->
                val req = chain.request()
                val body = if (req.method == "POST") {
                    """{"publishedUrl":"https://blog.naver.com/ddakchal_official/55511"}"""
                } else {
                    "<html><body>OK</body></html>"
                }
                Response.Builder()
                    .request(req)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(body.toResponseBody("text/html".toMediaType()))
                    .build()
            }.build(),
            ioDispatcher = testDispatcher
        )

        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            api = CloudflareAndDdakchalApi(client = apiClient, ioDispatcher = testDispatcher),
            postWarehouseStore = createWarehouseStore(this)
        )
        val vm = StudioViewModel(
            repository = repo,
            aiAdvisor = offlineAdvisor,
            naverApiClient = NaverRealEstateApiClient(ioDispatcher = testDispatcher),
            directPublishClient = publishClient
        )

        vm.updateConfig(
            vm.uiState.value.config.copy(
                ddakchalApiBase = "https://ddakchal.pages.dev",
                crawlerSyncToken = "sync-tok",
                naverBlogId = "ddakchal_official",
                naverWorkerRelayEndpoint = "https://relay.ddakchal.pages.dev/naver"
            )
        )

        // Toggle mobile/precision view mode & auto-polling
        vm.setMobileSimpleMode(false)
        assertFalse(vm.uiState.value.isMobileSimpleMode)
        vm.setMobileSimpleMode(true)
        assertTrue(vm.uiState.value.isMobileSimpleMode)

        // Live server telemetry refresh
        vm.refreshDdakchalServerTelemetry(alsoSyncFeed = true)
        val afterRefresh = vm.uiState.value
        assertTrue(afterRefresh.telemetry.ddakchalConnected)
        assertNotNull(afterRefresh.telemetry.apiLatencyMs)
        assertEquals(200, afterRefresh.telemetry.lastHttpStatusCode)
        assertEquals(42, afterRefresh.telemetry.triggerStatus?.processedCount)
        assertTrue(afterRefresh.slots.any { it.auctionCase.caseId == "2025타경8841" })

        // Immediate crawl & sync trigger
        vm.triggerCrawlAndSyncPipeline()
        assertTrue(recordedUrls.any { it.contains("POST https://ddakchal.pages.dev/api/v1/blog/crawl-and-sync") })

        // Publish and verify reverse PUT /api/v1/blog/queue/{item.id}
        val syncedSlot = vm.uiState.value.slots.first { it.auctionCase.caseId == "2025타경8841" }
        vm.publishSelectedChannelsAndVerify(syncedSlot.postId)
        assertTrue(
            "Expected PUT reverse-sync call to queue item, recorded=$recordedUrls",
            recordedUrls.any { it.startsWith("PUT https://ddakchal.pages.dev/api/v1/blog/queue/queue-8841") }
        )
    }
}
