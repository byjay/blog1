package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.ai.AiEvaluationOutcome
import com.example.ai.AiSeoGeoAdvisor
import com.example.ai.FirebaseAiSeoGeoService
import com.example.data.BlogAutomationRepository
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
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
        val candidates = CourtEvidenceRecorder.findProfitableBusanLandUnder50M()
        assertTrue(candidates.size >= 2)

        candidates.forEach { auction ->
            assertTrue("Must be Busan property: ${auction.address}", auction.address.contains("부산"))
            assertEquals(PropertyType.LAND_FARMLAND, auction.propertyType)
            assertTrue("Min bid must be under 50M KRW: ${auction.minBidPriceKrw}", auction.minBidPriceKrw < 50_000_000L)

            val proReport = ProAnalysisAndPhotoGuideEngine.buildProReport(auction)
            assertTrue("Profitable margin expected for ${auction.caseId}", proReport.safetyMarginKrw > 0L)

            val archive = CourtEvidenceRecorder.recordFullArchive(auction)
            assertEquals(5, archive.photos.size)
            assertEquals(3, archive.documents.size)
            assertEquals(
                listOf(CourtDocKind.SALE_SPECIFICATION, CourtDocKind.STATUS_SURVEY, CourtDocKind.APPRAISAL_REPORT),
                archive.documents.map { it.docKind }
            )
            assertTrue(archive.registryGapGu.size >= 3)
            assertTrue(archive.registryEulGu.isNotEmpty())
            assertTrue(archive.rightsVerdictSummary.isNotBlank())
            assertTrue(archive.assumedCostSummary.isNotBlank())
            assertTrue(archive.profitabilityBadge.contains("부산 5천만원 미만 수익성 토지"))
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
        val candidates = CourtEvidenceRecorder.findProfitableBusanLandUnder50M()

        candidates.forEachIndexed { idx, busanCase ->
            val variants = DefaultSpecDataFactory.buildDistinct3ChannelVariants(
                auction = busanCase,
                seedIndex = idx + 4,
                config = cfg,
                proFunnel = funnel
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
                sourceLabel = "부산 5천만원 미만 수익성 토지 테스트"
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
        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            postWarehouseStore = createWarehouseStore(this)
        )
        val vm = StudioViewModel(repository = repo, aiAdvisor = createTestAdvisor())

        // 1. Run Busan <50M profitable land test via ViewModel
        vm.createAndEvaluateBusanProfitableLandTest()

        val stateAfterBusan = vm.uiState.value
        val busanSlot = stateAfterBusan.slots.firstOrNull { it.auctionCase.caseId == "2025타경101915" }
        assertNotNull("Busan 2025타경101915 slot should be created", busanSlot)
        assertNotNull("Full court archive should be recorded", stateAfterBusan.activeCourtArchive)
        assertEquals("2025타경101915", stateAfterBusan.activeCourtArchive?.caseId)
        assertEquals(5, stateAfterBusan.activeCourtArchive?.photos?.size)
        assertEquals(3, stateAfterBusan.activeCourtArchive?.documents?.size)

        val busanEval = stateAfterBusan.aiEvalState as? AiEvalUiState.Success
        assertNotNull("AI eval state should be Success, was ${stateAfterBusan.aiEvalState}", busanEval)
        assertEquals("2025타경101915", busanEval!!.report.caseId)
        assertTrue("Busan score must be >= 95, got ${busanEval.report.totalScore}", busanEval.report.totalScore >= 95)

        // 2. Create a <95 draft -> verify auto-fix suggestions -> apply auto-fix -> recover to >=95
        vm.createLowScoreDraftForAutoFixTest()
        val lowState = vm.uiState.value.aiEvalState as? AiEvalUiState.Success
        assertNotNull("Low-score AI evaluation should succeed", lowState)
        assertTrue("Sabotaged draft should score < 95, got ${lowState!!.report.totalScore}", lowState.report.totalScore < 95)
        assertFalse(lowState.report.autoFixSuggestions.isEmpty())
        println("LOW_SCORE_DRAFT|case=${lowState.report.caseId}|score=${lowState.report.totalScore}|passed=${lowState.report.passedCount}/30|suggestions=${lowState.report.autoFixSuggestions.size}")

        vm.applyAiAutoFixesToArticle(lowState.report.caseId)
        val fixedState = vm.uiState.value.aiEvalState as? AiEvalUiState.Success
        assertNotNull(fixedState)
        assertTrue("After applying auto-fix, score must be >= 95, got ${fixedState!!.report.totalScore}", fixedState.report.totalScore >= 95)
        println("AUTO_FIXED_DRAFT|case=${fixedState.report.caseId}|score=${fixedState.report.totalScore}|passed=${fixedState.report.passedCount}/30")
    }

    @Test
    fun autoFixSuggestionBuilder_targetsFailedCriteriaWithConcreteFixes() {
        val busanCase = CourtEvidenceRecorder.findProfitableBusanLandUnder50M().first()
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
