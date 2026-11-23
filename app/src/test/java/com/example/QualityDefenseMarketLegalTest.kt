package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.data.BlogAutomationRepository
import com.example.data.NaverRealEstateApiClient
import com.example.data.PostWarehouseStore
import com.example.data.ServerConfigStore
import com.example.engine.Article30PointEvaluator
import com.example.engine.DefaultSpecDataFactory
import com.example.engine.LegalDoctrineVerifier
import com.example.engine.NearbyMarketEngine
import com.example.engine.QaVerificationEngine
import com.example.engine.QualityDefenseEngine
import com.example.model.ChannelPlatform
import com.example.model.LegalVerificationDomain
import com.example.model.MarketApiSourceStatus
import com.example.model.PropertyType
import com.example.model.QualityDefenseCategory
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class QualityDefenseMarketLegalTest {

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
        val file = tempFolder.newFile("qd_cfg_${System.nanoTime()}.preferences_pb")
        return ServerConfigStore(PreferenceDataStoreFactory.create(scope = scope) { file })
    }

    private fun createWarehouseStore(scope: TestScope): PostWarehouseStore {
        val file = tempFolder.newFile("qd_wh_${System.nanoTime()}.preferences_pb")
        return PostWarehouseStore(PreferenceDataStoreFactory.create(scope = scope) { file })
    }

    @Test
    fun qualityDefenseEngine_detectsAndReplacesAllAiPatternsEmojisSpamUrlsAndPii() {
        val baseAuction = DefaultSpecDataFactory.buildInitialDaily5Slots(ServerAutomationConfig()).first().auctionCase
        val rawTitle = "[급매추천] ${baseAuction.caseId} 오늘의 경매 🔥"
        val rawBody = """
            오늘 살펴볼 물건은 채무자 김철수 소유 아파트입니다.
            ■ 핵심 체크
            Q1. 지금 사도 되나요?
            A1. 100% 무조건 수익 구조로 매우 합리적입니다 알아보겠습니다 ✅
            자세한 내용은 https://spam-test.example.com 확인하세요.
        """.trimIndent()

        val result = QualityDefenseEngine.defendText(
            rawTitle = rawTitle,
            rawBody = rawBody,
            channel = ChannelPlatform.NAVER,
            auction = baseAuction
        )

        val categories = result.hits.map { it.category }.toSet()
        assertTrue(QualityDefenseCategory.AI_STRUCTURAL_PATTERN in categories)
        assertTrue(QualityDefenseCategory.AI_CLICHE_PHRASE in categories)
        assertTrue(QualityDefenseCategory.EMOJI_DECORATION in categories)
        assertTrue(QualityDefenseCategory.SEO_SPAM_TRIGGER in categories)
        assertTrue(QualityDefenseCategory.TITLE_BRACKET_SPAM in categories)
        assertTrue(QualityDefenseCategory.NAVER_DIRECT_URL in categories)
        assertTrue(QualityDefenseCategory.PII_EXPOSURE in categories)

        // After defense, zero AI/spam/URL/PII patterns remain
        assertFalse(result.sanitizedTitle.startsWith("["))
        assertFalse(result.sanitizedBody.contains("■"))
        assertFalse(result.sanitizedBody.contains("Q1."))
        assertFalse(result.sanitizedBody.contains("알아보겠습니다"))
        assertFalse(result.sanitizedBody.contains("합리적입니다"))
        assertFalse(result.sanitizedBody.contains("🔥"))
        assertFalse(result.sanitizedBody.contains("✅"))
        assertFalse(result.sanitizedBody.contains("https://"))
        assertFalse(result.sanitizedBody.contains("김철수"))
        assertFalse(result.sanitizedBody.contains("100% 무조건 수익"))
        assertTrue(QualityDefenseEngine.detectIssues(result.sanitizedBody, result.sanitizedTitle, ChannelPlatform.NAVER).isEmpty())
    }

    @Test
    fun legalDoctrineVerifier_catchesContradictionsAndBlocksQaPass() {
        val cfg = ServerAutomationConfig()
        val baseSlot = DefaultSpecDataFactory.buildInitialDaily5Slots(cfg).first()

        // 1. Clean default cases must pass 6-point legal doctrine check
        val cleanLegal = LegalDoctrineVerifier.verify(baseSlot.auctionCase)
        assertTrue(cleanLegal.overallPassed)
        assertEquals(0, cleanLegal.criticalBlockCount)
        assertEquals(6, cleanLegal.checks.size)

        // 2. Contradictory senior tenant + 2001다8677 late-fee succession must trigger CRITICAL_BLOCK and block QA
        val contradictoryAuction = baseSlot.auctionCase.copy(
            tenantSummary = "말소기준권리 이전 선순위 임차인 보증금 1억 5,000만원 배당요구 미신청",
            assumedRightsText = "후순위 권리 전부 소멸, 인수 없음, 공용관리비 연체료 포함 승계"
        )
        val badLegal = LegalDoctrineVerifier.verify(contradictoryAuction)
        assertFalse("Legal doctrine contradictions must fail overallPassed", badLegal.overallPassed)
        assertTrue(badLegal.criticalBlockCount >= 2)
        assertTrue(badLegal.checks.any { it.domain == LegalVerificationDomain.TENANT_PRIORITY_DIVIDEND && !it.passed })
        assertTrue(badLegal.checks.any { it.domain == LegalVerificationDomain.MANAGEMENT_FEE_2001DA8677 && !it.passed })

        val blockedQa = QaVerificationEngine.auditSlot(
            auctionCase = contradictoryAuction,
            variants = baseSlot.variants,
            scheduledTimes = baseSlot.scheduledTimes,
            config = cfg
        )
        assertFalse(blockedQa.legalDoctrinePassed)
        assertFalse("QA gate must block when legalDoctrinePassed is false", blockedQa.overallPassed)

        // 3. Post-seizure lien on factory case must recognize buyer immunity (pass, 0 burden)
        val factoryCase = DefaultSpecDataFactory.buildInitialDaily5Slots(cfg)
            .first { it.auctionCase.propertyType == PropertyType.FACTORY_SPECIAL }.auctionCase
        val factoryLegal = LegalDoctrineVerifier.verify(factoryCase)
        assertTrue(factoryLegal.overallPassed)
        val lienCheck = factoryLegal.checks.first { it.domain == LegalVerificationDomain.LIEN_AFTER_SEIZURE }
        assertTrue(lienCheck.findingDetail.contains("대항 불가"))
    }

    @Test
    fun naverMarketEngine_andApiClient_recalculateBidsWithLiveResponsesAndHandleErrorsHonestly() = runTest(testDispatcher) {
        val cfg = ServerAutomationConfig()
        val slot = DefaultSpecDataFactory.buildInitialDaily5Slots(cfg).first()
        val auction = slot.auctionCase

        // 1. Live dual-source interceptor (Naver Local + Blog + m.land.naver.com)
        val liveInterceptor = Interceptor { chain ->
            val url = chain.request().url.toString()
            val json = when {
                url.contains("/v1/search/local.json") -> """
                    {"items":[{"title":"<b>정관롯데캐슬2차</b>","mapx":"1291795123","mapy":"353247123"}]}
                """.trimIndent()
                url.contains("/v1/search/blog.json") -> """
                    {"items":[{"title":"정관 아파트 급매 실거래 흐름 분석"}]}
                """.trimIndent()
                url.contains("/cluster/ajax/articleList") -> """
                    {"body":[
                      {"atclNm":"정관롯데캐슬2차 105동","rletTpNm":"아파트","tradTpNm":"매매","hanPrc":"3억 4,000","prc":34000,"spc2":"84.9","flrInfo":"12/20","atclCfmYmd":"26.11.20","atclFetrDesc":"급매 로열동"},
                      {"atclNm":"정관동일스위트 201동","rletTpNm":"아파트","tradTpNm":"매매","hanPrc":"3억 6,000","prc":36000,"spc2":"84.5","flrInfo":"7/25","atclCfmYmd":"26.11.19","atclFetrDesc":"올수리"}
                    ]}
                """.trimIndent()
                else -> "{}"
            }
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(json.toResponseBody("application/json".toMediaType()))
                .build()
        }

        val liveClient = NaverRealEstateApiClient(
            client = OkHttpClient.Builder().addInterceptor(liveInterceptor).build(),
            ioDispatcher = testDispatcher,
            defaultClientId = "test-naver-id",
            defaultClientSecret = "test-naver-secret"
        )
        val legal = LegalDoctrineVerifier.verify(auction)
        val liveReport = liveClient.fetchNearbyMarketAnalysis(
            auction = auction,
            legalReport = legal
        )

        assertEquals(MarketApiSourceStatus.LIVE_FULL, liveReport.status)
        assertEquals(2, liveReport.naverSearchHitCount)
        assertEquals(2, liveReport.naverLandListingCount)
        assertEquals(340_000_000L, liveReport.urgentAskMinKrw)
        val bids = liveReport.recalculatedBids
        assertNotNull(bids)
        assertTrue(bids!!.conservativeBidKrw >= auction.minBidPriceKrw)
        assertTrue(bids.conservativeBidKrw <= bids.balancedBidKrw)

        // Verify ViewModel market + Quality Defense end-to-end
        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            postWarehouseStore = createWarehouseStore(this)
        )
        val offlineAdvisor = object : com.example.ai.AiSeoGeoAdvisor {
            override suspend fun evaluateWith30Criteria(
                slot: com.example.model.GeneratedPostSlot,
                config: ServerAutomationConfig,
                proFunnel: com.example.model.ProFunnelConfig
            ): com.example.ai.AiEvaluationOutcome {
                val eval = Article30PointEvaluator.evaluate(slot, config, proFunnel)
                return com.example.ai.AiEvaluationOutcome.Success(
                    com.example.model.AiEvaluationReport(
                        caseId = slot.auctionCase.caseId,
                        modelName = "local-eval",
                        totalScore = eval.totalScore,
                        passedCount = eval.passedCount,
                        aiSummary = "테스트 채점",
                        overallVerdict = if (eval.eligibleForRegistration) "등록 가능" else "개선 필요",
                        criteriaResults = eval.items,
                        autoFixSuggestions = emptyList(),
                        evaluatedAt = eval.evaluatedAt
                    )
                )
            }
        }
        val vm = StudioViewModel(
            repository = repo,
            aiAdvisor = offlineAdvisor,
            naverApiClient = liveClient
        )
        vm.fetchNearbyMarketAndRebid(slot.postId)
        val updatedMarket = vm.uiState.value.marketAnalysisByCaseId[auction.caseId]
        assertNotNull(updatedMarket)
        assertEquals(MarketApiSourceStatus.LIVE_FULL, updatedMarket!!.status)
        assertEquals(340_000_000L, updatedMarket.urgentAskMinKrw)

        // Sabotage a draft and confirm Quality Defense ViewModel method cleans it back to >= 95
        vm.createLowScoreDraftForAutoFixTest()
        val draftSlot = vm.uiState.value.slots.first()
        assertTrue(draftSlot.qaReport.qualityDefenseFlagsCount > 0)
        vm.applyQualityDefenseToSlot(draftSlot.postId)
        val cleanedSlot = vm.uiState.value.slots.first { it.postId == draftSlot.postId }
        assertEquals(0, cleanedSlot.qaReport.qualityDefenseFlagsCount)
        val evalAfterDefense = Article30PointEvaluator.evaluate(cleanedSlot, cfg)
        assertTrue("Expected >= 95 after Quality Defense, got ${evalAfterDefense.totalScore}", evalAfterDefense.totalScore >= 95)

        // 2. Missing keys + HTTP 429 from m.land must NOT invent fake comps
        val blockedClient = NaverRealEstateApiClient(
            client = OkHttpClient.Builder().addInterceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(429)
                    .message("Too Many Requests")
                    .body("{}".toResponseBody("application/json".toMediaType()))
                    .build()
            }.build(),
            ioDispatcher = testDispatcher
        )
        val missingKeyReport = blockedClient.fetchNearbyMarketAnalysis(
            auction = auction,
            legalReport = legal,
            clientId = "NAVER_CLIENT_ID_DEFAULT_VALUE",
            clientSecret = "NAVER_CLIENT_SECRET_DEFAULT_VALUE"
        )
        assertEquals(MarketApiSourceStatus.API_KEY_REQUIRED, missingKeyReport.status)
        assertTrue(missingKeyReport.comps.isEmpty())
        assertNull(missingKeyReport.recalculatedBids)
        assertEquals(385_000_000L, NearbyMarketEngine.parseNaverKoreanPriceToKrw("3억 8,500", 0L))
    }
}
