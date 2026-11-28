package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.ai.AiEvaluationOutcome
import com.example.ai.AiSeoGeoAdvisor
import com.example.ai.FirebaseAiSeoGeoService
import com.example.data.BlogAutomationRepository
import com.example.data.CloudflareAndDdakchalApi
import com.example.data.PostWarehouseStore
import com.example.data.ServerConfigStore
import com.example.engine.Article30PointEvaluator
import com.example.engine.CourtEvidenceRecorder
import com.example.engine.DefaultSpecDataFactory
import com.example.engine.InfographicBitmapRenderer
import com.example.engine.ProAnalysisAndPhotoGuideEngine
import com.example.engine.QaVerificationEngine
import com.example.engine.RegistryInfographicEngine
import com.example.model.AiEvalUiState
import com.example.model.AiEvaluationReport
import com.example.model.ChannelPlatform
import com.example.model.CourtDocKind
import com.example.model.GeneratedPostSlot
import com.example.model.PostStatus
import com.example.model.ProFunnelConfig
import com.example.model.PropertyType
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
class BusanLandAndAiEvaluationTest {

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

    private fun createTestAdvisor(): AiSeoGeoAdvisor = object : AiSeoGeoAdvisor {
        private val suggestionHelper = FirebaseAiSeoGeoService()
        override suspend fun evaluateWith30Criteria(
            slot: GeneratedPostSlot,
            config: ServerAutomationConfig,
            proFunnel: ProFunnelConfig
        ): AiEvaluationOutcome {
            val reg = RegistryInfographicEngine.buildTemplate(slot.auctionCase)
            val eval = Article30PointEvaluator.evaluate(slot, config, proFunnel, reg)
            val suggestions = suggestionHelper.buildAutoFixSuggestionsFromFailures(slot, eval.items)
            return AiEvaluationOutcome.Success(
                AiEvaluationReport(
                    caseId = slot.auctionCase.caseId,
                    modelName = "gemini-3.8-flash",
                    totalScore = eval.totalScore,
                    passedCount = eval.passedCount,
                    aiSummary = if (eval.totalScore >= 95) {
                        "30개 SEO/GEO 기준 중 ${eval.passedCount}개 항목을 통과해 즉시 등록 가능합니다."
                    } else {
                        "30개 항목 중 ${30 - eval.passedCount}개 감점 요인이 발견되어 자동 수정 제안 ${suggestions.size}건을 생성했습니다."
                    },
                    overallVerdict = if (eval.totalScore >= 95) "95점 이상 등록 승인" else "95점 미만 자동 수정 필요",
                    criteriaResults = eval.items,
                    autoFixSuggestions = suggestions,
                    evaluatedAt = eval.evaluatedAt
                )
            )
        }
    }

    private fun createConfigStore(scope: TestScope): ServerConfigStore {
        val file = tempFolder.newFile("busan_cfg_${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file }
        )
        return ServerConfigStore(dataStore)
    }

    private fun createWarehouseStore(scope: TestScope): PostWarehouseStore {
        val file = tempFolder.newFile("busan_wh_${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file }
        )
        return PostWarehouseStore(dataStore)
    }

    @Test
    fun busanLandUnder50M_isProfitableAndRecordsAllCourtPhotosDocumentsAndRegistry() {
        val candidates = TestFixtures.sampleBusanLands()
        assertTrue(candidates.size >= 2)

        candidates.forEach { auction ->
            assertTrue("Must be Busan property: ${auction.address}", auction.address.contains("부산"))
            assertEquals(PropertyType.LAND_FARMLAND, auction.propertyType)
            assertTrue("Min bid must be under 50M KRW: ${auction.minBidPriceKrw}", auction.minBidPriceKrw < 50_000_000L)

            val proReport = ProAnalysisAndPhotoGuideEngine.buildProReport(auction)
            assertTrue("Profitable margin expected for ${auction.caseId}", proReport.safetyMarginKrw > 0L)

            val archive = CourtEvidenceRecorder.recordFullArchive(
                auction = auction,
                courtPhotoUrls = listOf(
                    "https://r2.ddakchal.pages.dev/photos/${auction.dedupKey}-1.webp",
                    "https://r2.ddakchal.pages.dev/photos/${auction.dedupKey}-2.webp"
                ),
                customVisitRecords = TestFixtures.sampleVisits(auction.caseId),
                serverGapGu = TestFixtures.sampleGapGu(auction.caseId, auction.courtName),
                serverEulGu = TestFixtures.sampleEulGu(auction.caseId, auction.baseRightDate),
                serverDocuments = TestFixtures.sampleDocuments(auction.caseId, auction.courtName)
            )
            assertEquals(2, archive.photos.size)
            assertEquals(3, archive.documents.size)
            assertEquals(
                listOf(CourtDocKind.SALE_SPECIFICATION, CourtDocKind.STATUS_SURVEY, CourtDocKind.APPRAISAL_REPORT),
                archive.documents.map { it.docKind }
            )
            assertTrue(archive.registryGapGu.size >= 3)
            assertTrue(archive.registryEulGu.isNotEmpty())
            assertTrue(archive.rightsVerdictSummary.isNotBlank())
            assertTrue(archive.assumedCostSummary.isNotBlank())
            assertTrue(archive.profitabilityBadge.contains("안전마진"))
            assertEquals(
                R.drawable.img_court_land_site,
                InfographicBitmapRenderer.drawableResForPropertyType(auction.propertyType)
            )
        }
    }

    @Test
    fun busanLand2025Tagyeong101915_and_20368_generateArticlesAndPass30CriteriaOver95() {
        val cfg = ServerAutomationConfig()
        val funnel = ProFunnelConfig()
        val candidates = TestFixtures.sampleBusanLands()

        candidates.forEachIndexed { idx, busanCase ->
            val gap = TestFixtures.sampleGapGu(busanCase.caseId, busanCase.courtName)
            val eul = TestFixtures.sampleEulGu(busanCase.caseId, busanCase.baseRightDate)
            val docs = TestFixtures.sampleDocuments(busanCase.caseId, busanCase.courtName)
            val visits = TestFixtures.sampleVisits(busanCase.caseId)
            val variants = DefaultSpecDataFactory.buildDistinct3ChannelVariants(
                auction = busanCase,
                seedIndex = idx + 4,
                config = cfg,
                proFunnel = funnel,
                serverGapGu = gap,
                serverEulGu = eul,
                serverDocuments = docs,
                visitRecords = visits
            )
            val scheduled = mapOf(
                ChannelPlatform.CLOUDFLARE to "2026-11-22 08:17",
                ChannelPlatform.TISTORY to "2026-11-22 09:04",
                ChannelPlatform.NAVER to "2026-11-22 10:19"
            )
            val qa = QaVerificationEngine.auditSlot(
                auctionCase = busanCase,
                variants = variants,
                scheduledTimes = scheduled,
                config = cfg
            )
            val slot = GeneratedPostSlot(
                postId = "post-${busanCase.dedupKey}",
                queueId = "q-busan-$idx",
                slotIndex = idx + 1,
                publishDate = "2026-11-22",
                auctionCase = busanCase,
                variants = variants,
                scheduledTimes = scheduled,
                status = PostStatus.QUEUED,
                qaReport = qa,
                sourceLabel = "부산 5천만원 미만 수익성 토지 테스트",
                courtPhotoUrls = listOf("https://r2.ddakchal.pages.dev/photos/${busanCase.dedupKey}-1.webp"),
                visitRecords = visits,
                serverRegistryGapGu = gap,
                serverRegistryEulGu = eul,
                serverCourtDocuments = docs
            )
            val regTemplate = RegistryInfographicEngine.buildTemplate(busanCase)
            val eval = Article30PointEvaluator.evaluate(slot, cfg, funnel, regTemplate)

            assertEquals(30, eval.totalCriteriaCount)
            assertTrue(
                "Expected >= 95 for ${busanCase.caseId}, got ${eval.totalScore}. Failed: ${eval.items.filter { !it.passed }.map { "${it.orderIndex}:${it.title}" }}",
                eval.totalScore >= 95
            )
            assertTrue(eval.eligibleForRegistration)
            assertEquals(30, eval.passedCount)

            println("BUSAN_EVAL|case=${busanCase.caseId}|minBid=${busanCase.minBidPriceKrw}|score=${eval.totalScore}|passed=${eval.passedCount}/30|cosine=${qa.maxCrossChannelCosinePct}%|naverTitle=${variants[ChannelPlatform.NAVER]?.title}")
        }
    }

    @Test
    fun viewModel_evaluatesBusanLandAndFixesLowScoreDraftBackOver95() = runTest(testDispatcher) {
        val busanServerJson = """
            {
              "ok": true,
              "data": {
                "items": [
                  {
                    "id": "busan-101915",
                    "case_no": "2025타경101915",
                    "auction_dedup_key": "busan-land-2025-101915",
                    "court_name": "부산지방법원 동부지원",
                    "address": "부산광역시 기장군 일광읍 화전리 214-7 전",
                    "property_type": "LAND_FARMLAND",
                    "appraisal_krw": 64000000,
                    "min_bid_krw": 32768000,
                    "discount_rate_pct": 51,
                    "base_right_date": "2021-06-15",
                    "real_trade_median_krw": 58000000,
                    "photo_urls": [
                      "https://r2.ddakchal.pages.dev/photos/busan-1.webp",
                      "https://r2.ddakchal.pages.dev/photos/busan-2.webp"
                    ],
                    "court_documents": [
                      {"docKind": "SALE_SPECIFICATION", "referenceNo": "명세", "issuedDate": "2026-11-18", "issuer": "부산지방법원 동부지원", "summaryHighlight": "인수권리 없음", "fullRecordLines": ["기준일 2021-06-15"], "riskFlagNote": "도로 확인"},
                      {"docKind": "STATUS_SURVEY", "referenceNo": "현황", "issuedDate": "2026-11-12", "issuer": "집행관실", "summaryHighlight": "공실 나대지", "fullRecordLines": ["임차인 없음"], "riskFlagNote": "경계 확인"},
                      {"docKind": "APPRAISAL_REPORT", "referenceNo": "감정", "issuedDate": "2026-10-25", "issuer": "감정법인", "summaryHighlight": "거래사례 평가", "fullRecordLines": ["감정 6400만"], "riskFlagNote": "농취증 확인"}
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

        val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            val req = chain.request()
            val path = req.url.encodedPath
            val body = when {
                path.endsWith("/api/v1/blog/crawl-and-sync") ||
                    path.endsWith("/api/v1/blog/assets/feed") ||
                    path.endsWith("/api/v1/blog/analysis-feed") -> busanServerJson
                else -> """{"ok":true,"data":{"triggers":{"pending":0,"processed":1,"failed":0},"snapshots":{"count":1}}}"""
            }
            Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body.toResponseBody("application/json; charset=utf-8".toMediaType()))
                .build()
        }).build()

        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            api = CloudflareAndDdakchalApi(client = client, ioDispatcher = testDispatcher),
            postWarehouseStore = createWarehouseStore(this)
        )
        val vm = StudioViewModel(repository = repo, aiAdvisor = createTestAdvisor())
        vm.applyInlineServerAuthAndSave("https://ddakchal.pages.dev", "test-token", "", "", "")

        // 1. Fetch & evaluate real Busan property from mocked Ddakchal server
        vm.createAndEvaluateBusanProfitableLandTest()

        val stateAfterBusan = vm.uiState.value
        val busanSlot = stateAfterBusan.slots.firstOrNull { it.auctionCase.caseId == "2025타경101915" }
        assertNotNull("Busan 2025타경101915 slot should be loaded from server", busanSlot)
        assertNotNull("Full court archive should be recorded", stateAfterBusan.activeCourtArchive)
        assertEquals("2025타경101915", stateAfterBusan.activeCourtArchive?.caseId)
        assertEquals(2, stateAfterBusan.activeCourtArchive?.photos?.size)
        assertEquals(3, stateAfterBusan.activeCourtArchive?.documents?.size)

        val busanEval = stateAfterBusan.aiEvalState as? AiEvalUiState.Success
        assertNotNull("AI eval state should be Success, was ${stateAfterBusan.aiEvalState}", busanEval)
        assertEquals("2025타경101915", busanEval!!.report.caseId)
        assertTrue("Busan score must be >= 95, got ${busanEval.report.totalScore}", busanEval.report.totalScore >= 95)

        // 2. Apply auto-fix rewrite preserves >= 95 and active archive cleanly
        vm.applyAiAutoFixesToArticle("2025타경101915")
        val fixedState = vm.uiState.value.aiEvalState as? AiEvalUiState.Success
        assertNotNull(fixedState)
        assertTrue("After applying auto-fix, score must be >= 95, got ${fixedState!!.report.totalScore}", fixedState.report.totalScore >= 95)
    }

    @Test
    fun autoFixSuggestionBuilder_targetsFailedCriteriaWithConcreteFixes() {
        val busanCase = TestFixtures.sampleBusanLands().first()
        val cfg = ServerAutomationConfig()
        val funnel = ProFunnelConfig()
        val baseVariants = DefaultSpecDataFactory.buildDistinct3ChannelVariants(busanCase, 2, cfg, proFunnel = funnel)
        val sabotaged = baseVariants + (
            ChannelPlatform.NAVER to baseVariants[ChannelPlatform.NAVER]!!.copy(
                title = "[급매] ${busanCase.caseId}",
                bodyText = "■ Q1. 부산 땅 알아보겠습니다 🔥 https://spam.example.com",
                pinnedCommentSnippet = ""
            )
            )
        val slot = GeneratedPostSlot(
            postId = "post-fix-check",
            queueId = "q-fix",
            slotIndex = 1,
            publishDate = "2026-11-22",
            auctionCase = busanCase,
            variants = sabotaged,
            scheduledTimes = mapOf(
                ChannelPlatform.CLOUDFLARE to "2026-11-22 08:17",
                ChannelPlatform.TISTORY to "2026-11-22 09:04",
                ChannelPlatform.NAVER to "2026-11-22 10:19"
            ),
            status = PostStatus.DRAFT,
            qaReport = QaVerificationEngine.auditSlot(busanCase, sabotaged, emptyMap(), cfg),
            sourceLabel = "수정 제안 검증"
        )
        val eval = Article30PointEvaluator.evaluate(slot, cfg, funnel)
        assertTrue(eval.totalScore < 95)
        val suggestions = FirebaseAiSeoGeoService().buildAutoFixSuggestionsFromFailures(slot, eval.items)
        assertTrue(suggestions.size >= 8)
        assertTrue(suggestions.any { it.criterionOrder == 7 && it.targetChannel == ChannelPlatform.NAVER })
        assertTrue(suggestions.any { it.criterionOrder == 17 && it.suggestedFix.contains("외부 URL") })
        assertTrue(suggestions.any { it.criterionOrder == 27 && it.improvedSnippet.contains(busanCase.caseId) })
    }
}
