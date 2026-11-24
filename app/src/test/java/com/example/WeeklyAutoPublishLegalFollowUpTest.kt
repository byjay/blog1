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
import com.example.engine.InvestmentAttractivenessEngine
import com.example.engine.LegalTermStudyEngine
import com.example.engine.PostAuctionFollowUpEngine
import com.example.engine.QaVerificationEngine
import com.example.engine.WeeklyBatchScheduleEngine
import com.example.model.AiEvaluationReport
import com.example.model.ChannelPlatform
import com.example.model.GeneratedPostSlot
import com.example.model.LegalStudyCategory
import com.example.model.PostAuctionOutcomeType
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

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyAutoPublishLegalFollowUpTest {

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
        val file = tempFolder.newFile("wk_cfg_${System.nanoTime()}.preferences_pb")
        return ServerConfigStore(PreferenceDataStoreFactory.create(scope = scope) { file })
    }

    private fun createWarehouseStore(scope: TestScope): PostWarehouseStore {
        val file = tempFolder.newFile("wk_wh_${System.nanoTime()}.preferences_pb")
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
                    aiSummary = "테스트 30항목 평가",
                    overallVerdict = if (eval.eligibleForRegistration) "등록 가능" else "개선 필요",
                    criteriaResults = eval.items,
                    autoFixSuggestions = emptyList(),
                    evaluatedAt = eval.evaluatedAt
                )
            )
        }
    }

    @Test
    fun defaultSpecDataFactory_preserves5SlotsWithContextualLegalNoteAnd95Score() {
        val cfg = ServerAutomationConfig()
        val funnel = ProFunnelConfig()
        val slots = DefaultSpecDataFactory.buildInitialDaily5Slots(cfg, funnel)
        assertEquals(5, slots.size)

        slots.forEach { slot ->
            val naverBody = slot.variants[ChannelPlatform.NAVER]?.bodyText.orEmpty()
            assertTrue("Naver body should embed [법리 용어 공부]", naverBody.contains("[법리 용어 공부]"))
            assertTrue(slot.qaReport.overallPassed)
            val eval = Article30PointEvaluator.evaluate(slot, cfg, funnel)
            assertTrue(
                "Slot ${slot.auctionCase.caseId} scored ${eval.totalScore}",
                eval.totalScore >= Article30PointEvaluator.REGISTRATION_MIN_SCORE
            )
            slot.variants.values.forEach { variant ->
                val noSpace = variant.bodyText.count { !it.isWhitespace() }
                assertTrue(
                    "Char count $noSpace out of range 1200..2500 for ${variant.platform}",
                    noSpace in 1200..2500
                )
            }
        }
    }

    @Test
    fun legalTermStudyEngine_coversAll6CategoriesAndContextualSelection() {
        val categoriesPresent = LegalTermStudyEngine.masterTerms.map { it.category }.toSet()
        assertEquals(LegalStudyCategory.entries.toSet(), categoriesPresent)

        val redevelopmentFilter = LegalTermStudyEngine.filterTerms(
            category = LegalStudyCategory.URBAN_REDEVELOPMENT,
            query = "토지만의 경매"
        )
        assertEquals(1, redevelopmentFilter.size)
        assertEquals("DB-URB-001", redevelopmentFilter.first().codeId)

        val baseAuction = DefaultSpecDataFactory.buildInitialDaily5Slots(ServerAutomationConfig()).first().auctionCase
        val landTerm = LegalTermStudyEngine.selectContextualTerm(
            baseAuction.copy(propertyType = PropertyType.LAND_FARMLAND, assumedRightsText = "지목 전 맹지 확인")
        )
        assertEquals("DB-REG-001", landTerm.codeId)

        val seniorTenantTerm = LegalTermStudyEngine.selectContextualTerm(
            baseAuction.copy(assumedRightsText = "대위변제 승격 및 선순위 확인")
        )
        assertEquals("DB-SPC-001", seniorTenantTerm.codeId)
    }

    @Test
    fun investmentAttractivenessEngine_scoresWithinBoundsAndIncludesDisclaimerAnd5Competitors() {
        val slots = DefaultSpecDataFactory.buildInitialDaily5Slots(ServerAutomationConfig())
        slots.forEach { slot ->
            val score = InvestmentAttractivenessEngine.score(slot.auctionCase)
            assertTrue(score.totalScore in 0..100)
            assertTrue(score.disclaimer.contains("보장"))
            assertTrue(score.disclaimer.contains("아닌"))
            assertTrue(score.selectionVerdict.isNotBlank())
        }

        val competitors = InvestmentAttractivenessEngine.buildCompetitorComparisonRows()
        assertEquals(5, competitors.size)
        assertTrue(competitors.any { it.blogName.contains("태인경매") })
        assertTrue(competitors.any { it.blogName.contains("조은인연") })
    }

    @Test
    fun postAuctionFollowUpEngine_handlesWinningAndFailedDropAndPassesQa() {
        val cfg = ServerAutomationConfig()
        val funnel = ProFunnelConfig()
        val baseSlot = DefaultSpecDataFactory.buildInitialDaily5Slots(cfg, funnel).first()
        val pastAuction = baseSlot.auctionCase.copy(auctionDate = "2026-10-15")
        val pastSlot = baseSlot.copy(auctionCase = pastAuction)

        assertTrue(PostAuctionFollowUpEngine.isAuctionDatePassed("2026-10-15", "2026-11-23"))
        assertFalse(PostAuctionFollowUpEngine.isAuctionDatePassed("2026-12-20", "2026-11-23"))

        // 1. WINNING_BID follow-up
        val (winRecord, winSlot) = PostAuctionFollowUpEngine.reinvestigateAndBuildFollowUp(
            originalSlot = pastSlot,
            outcomeType = PostAuctionOutcomeType.WINNING_BID,
            actualWinningManWon = 31_200L,
            courtInvestigationNote = "잔금납부기한 12월 예정",
            config = cfg,
            proFunnel = funnel,
            referenceDate = "2026-11-23"
        )
        assertTrue(winRecord.isPastDue)
        assertEquals(312_000_000L, winRecord.winningPriceKrw)
        val naverWinTitle = winSlot.variants[ChannelPlatform.NAVER]?.title.orEmpty()
        assertFalse("Naver title should not start with bracket", naverWinTitle.trim().startsWith("["))
        assertTrue(winSlot.variants[ChannelPlatform.NAVER]?.bodyText?.contains("사후 재조사") == true)
        assertTrue(
            "Winning follow-up QA failed: ${winSlot.qaReport.factCheckMismatches} / ${winSlot.qaReport.forbiddenHits} / ${winSlot.qaReport.legalDoctrineSummary}",
            winSlot.qaReport.overallPassed
        )

        // 2. FAILED_DROP (-20% minimum bid) follow-up
        val (dropRecord, dropSlot) = PostAuctionFollowUpEngine.reinvestigateAndBuildFollowUp(
            originalSlot = pastSlot,
            outcomeType = PostAuctionOutcomeType.FAILED_DROP,
            nextAuctionDateInput = "2026-12-28",
            config = cfg,
            proFunnel = funnel,
            referenceDate = "2026-11-23"
        )
        val expectedNextMin = (pastAuction.minBidPriceKrw * 80L) / 100L
        assertEquals(expectedNextMin, dropRecord.nextMinBidKrw)
        assertEquals("2026-12-28", dropRecord.nextAuctionDate)
        assertEquals(pastAuction.bidCount + 1, dropSlot.auctionCase.bidCount)
        assertTrue(
            "Drop follow-up QA failed: ${dropSlot.qaReport.factCheckMismatches} / ${dropSlot.qaReport.forbiddenHits} / ${dropSlot.qaReport.legalDoctrineSummary}",
            dropSlot.qaReport.overallPassed
        )
    }

    @Test
    fun weeklyBatchScheduleEngine_generatesDeduplicated21And35SlotsWith95PlusAndStaggeredTimes() {
        val cfg = ServerAutomationConfig()
        val funnel = ProFunnelConfig()
        val initialSlots = DefaultSpecDataFactory.buildInitialDaily5Slots(cfg, funnel)
        val existingServerSlot = initialSlots.first()

        // 3/day x 7 days = 21 deduplicated slots, prioritizing server slot without duplicating caseId
        val (plan21, _) = WeeklyBatchScheduleEngine.buildWeekly7DayPlan(
            dailyCount = 3,
            days = 7,
            serverSyncedSlots = listOf(existingServerSlot),
            existingCaseIds = emptySet(),
            config = cfg,
            proFunnel = funnel,
            startDateIso = "2026-11-23"
        )
        assertEquals(21, plan21.size)
        assertEquals(21, plan21.map { it.auctionCase.caseId }.toSet().size)
        assertEquals(existingServerSlot.auctionCase.caseId, plan21.first().auctionCase.caseId)
        assertEquals(7, plan21.map { it.publishDate }.toSet().size)

        plan21.forEach { slot ->
            val eval = Article30PointEvaluator.evaluate(slot, cfg, funnel)
            assertTrue("Expected >= 95 for ${slot.auctionCase.caseId}, got ${eval.totalScore}", eval.totalScore >= 95)
            assertTrue(
                "Slot ${slot.auctionCase.caseId} QA failed: cos=${slot.qaReport.maxCrossChannelCosinePct}, jac=${slot.qaReport.maxCrossChannelJaccard5GramPct}, rec=${slot.qaReport.recent7DayMaxSimilarityPct}, chars=${slot.qaReport.charCountByChannel}, link=${slot.qaReport.externalLinkRuleOk}, hits=${slot.qaReport.forbiddenHits}",
                slot.qaReport.overallPassed
            )
            slot.scheduledTimes.values.forEach { ts ->
                assertFalse("Anti-bot rule prohibits :00: minute in $ts", ts.contains(":00:"))
            }
        }

        // 5/day x 7 days = 35 deduplicated slots
        val (plan35, _) = WeeklyBatchScheduleEngine.buildWeekly7DayPlan(
            dailyCount = 5,
            days = 7,
            serverSyncedSlots = emptyList(),
            existingCaseIds = setOf(existingServerSlot.auctionCase.caseId),
            config = cfg,
            proFunnel = funnel,
            startDateIso = "2026-11-23"
        )
        assertEquals(35, plan35.size)
        assertEquals(35, plan35.map { it.auctionCase.caseId }.toSet().size)
        assertFalse(plan35.any { it.auctionCase.caseId == existingServerSlot.auctionCase.caseId })
    }

    @Test
    fun directChannelPublishClient_verifiesLiveReachabilityAndRejectsBlankOr404Honestly() = runTest(testDispatcher) {
        val cfgBase = ServerAutomationConfig(
            naverBlogId = "ddakchal_official",
            naverCategoryName = "부동산 경매 분석",
            tistoryBlogUrl = "https://ddakchal-report.tistory.com",
            ddakchalApiBase = "https://ddakchal.com",
            publishTargetNaver = true,
            publishTargetTistory = true,
            publishTargetCloudflare = true
        )
        val slot = DefaultSpecDataFactory.buildInitialDaily5Slots(cfgBase).first()

        // 1. Live 200 OK interceptor (relay POST + HTTP GET)
        val liveInterceptor = Interceptor { chain ->
            val req = chain.request()
            val url = req.url.toString()
            val body = if (req.method == "POST") {
                """{"publishedUrl":"https://blog.naver.com/ddakchal_official/223700001"}"""
            } else {
                "<html><body>정상 블로그 게시물 확인 ($url)</body></html>"
            }
            Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body.toResponseBody("text/html".toMediaType()))
                .build()
        }
        val liveClient = DirectChannelPublishClient(
            httpClient = OkHttpClient.Builder().addInterceptor(liveInterceptor).build(),
            ioDispatcher = testDispatcher
        )
        val liveReport = liveClient.publishSelectedChannelsAndVerify(
            slot = slot,
            config = cfgBase.copy(naverWorkerRelayEndpoint = "https://relay.ddakchal.com/naver")
        )
        assertEquals(3, liveReport.selectedChannelCount)
        assertTrue(liveReport.naverDirectPassed)
        assertTrue(liveReport.outcomes.all { it.reachableOk })
        assertTrue(liveReport.verificationChecks.all { it.passed })

        // 2. Blank Naver blog ID + HTTP 404 on Tistory must report honest failures
        val fail404Client = DirectChannelPublishClient(
            httpClient = OkHttpClient.Builder().addInterceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(404)
                    .message("Not Found")
                    .body("존재하지 않는 게시물".toResponseBody("text/html".toMediaType()))
                    .build()
            }.build(),
            ioDispatcher = testDispatcher
        )
        val failReport = fail404Client.publishSelectedChannelsAndVerify(
            slot = slot,
            config = cfgBase.copy(naverBlogId = "", publishTargetCloudflare = false)
        )
        assertFalse(failReport.naverDirectPassed)
        assertEquals(2, failReport.outcomes.size)
        assertTrue(failReport.outcomes.none { it.reachableOk })
    }

    @Test
    fun studioViewModel_endToEndWeeklyScheduleAutoRegisterDestinationsAndFollowUp() = runTest(testDispatcher) {
        val okClient = DirectChannelPublishClient(
            httpClient = OkHttpClient.Builder().addInterceptor { chain ->
                val req = chain.request()
                val body = if (req.method == "POST") {
                    """{"publishedUrl":"https://blog.naver.com/my_auction_blog/991122"}"""
                } else {
                    "<html><body>OK</body></html>"
                }
                Response.Builder()
                    .request(req)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(body.toResponseBody("application/json".toMediaType()))
                    .build()
            }.build(),
            ioDispatcher = testDispatcher
        )
        val fastMockApi = CloudflareAndDdakchalApi(
            client = OkHttpClient.Builder().addInterceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("""{"items":[]}""".toResponseBody("application/json".toMediaType()))
                    .build()
            }.build(),
            ioDispatcher = testDispatcher
        )
        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            api = fastMockApi,
            postWarehouseStore = createWarehouseStore(this)
        )
        val vm = StudioViewModel(
            repository = repo,
            aiAdvisor = offlineAdvisor,
            naverApiClient = NaverRealEstateApiClient(ioDispatcher = testDispatcher),
            directPublishClient = okClient
        )

        // 1. Save 3-channel destinations
        vm.saveThreeChannelDestinations(
            naverBlogId = "my_auction_blog",
            naverCategoryName = "실전 경매 분석",
            naverWorkerRelayEndpoint = "https://relay.example.com/publish",
            tistoryBlogUrl = "myauction.tistory.com",
            tistoryCategoryName = "배당·세무",
            ddakchalApiBase = "https://ddakchal.com",
            cloudflareBlogPath = "/reports",
            publishNaver = true,
            publishTistory = true,
            publishCloudflare = true
        )
        assertEquals("my_auction_blog", vm.uiState.value.config.naverBlogId)
        assertEquals("https://ddakchal.com", vm.uiState.value.config.ddakchalApiBase)

        // 2. Generate 7-day weekly batch (3/day = 21 deduplicated posts)
        vm.setWeeklyDailyCount(3)
        vm.generateWeeklyBatchAndSchedule(perDay = 3)
        val weeklyState = vm.uiState.value
        assertEquals("Unexpected error: ${weeklyState.errorMessage}", null, weeklyState.errorMessage)
        assertEquals(21, weeklyState.slots.size)
        val summary = weeklyState.weeklyBatchSummary
        assertNotNull(summary)
        assertEquals(21, summary!!.totalGeneratedCount)
        assertEquals(21, summary.score95PlusCount)

        // 3. One-tap high-score create & auto-register to selected channels
        vm.createHighScoreAndAutoRegister(PropertyType.APT_OFFICETEL)
        val afterAutoReg = vm.uiState.value
        assertNotNull(afterAutoReg.publishVerificationReport)
        assertTrue(afterAutoReg.publishVerificationReport!!.naverDirectPassed)
        assertEquals(PostStatus.PUBLISHED, afterAutoReg.slots.first().status)

        // 4. Direct Naver verify & Post-Auction Re-Investigation Follow-Up
        vm.publishNaverDirectAndVerify(afterAutoReg.slots.first().postId)
        assertTrue(vm.uiState.value.publishVerificationReport!!.naverDirectPassed)

        val firstCaseId = afterAutoReg.slots.first().auctionCase.caseId
        vm.reinvestigatePostAuctionAndCreateFollowUp(
            postId = afterAutoReg.slots.first().postId,
            outcomeType = PostAuctionOutcomeType.FAILED_DROP,
            nextAuctionDate = "2026-12-30",
            courtNote = "응찰자 없음 유찰"
        )
        val afterFollowUp = vm.uiState.value
        assertNotNull(afterFollowUp.followUpRecordsByCaseId[firstCaseId])
        assertEquals(PostAuctionOutcomeType.FAILED_DROP, afterFollowUp.followUpRecordsByCaseId[firstCaseId]!!.outcomeType)
        assertTrue(afterFollowUp.slots.first().sourceLabel.contains("사후재조사"))
    }
}
