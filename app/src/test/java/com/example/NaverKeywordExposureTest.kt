package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.ai.AiCaseSearchAdvisor
import com.example.ai.AiCaseSearchOutcome
import com.example.ai.AiEvaluationOutcome
import com.example.ai.AiSeoGeoAdvisor
import com.example.data.BlogAutomationRepository
import com.example.data.NaverRealEstateApiClient
import com.example.data.PostWarehouseStore
import com.example.data.ServerConfigStore
import com.example.engine.Article30PointEvaluator
import com.example.engine.NaverKeywordExposureEngine
import com.example.model.AiEvaluationReport
import com.example.model.ExposurePotentialGrade
import com.example.model.GeneratedPostSlot
import com.example.model.NaverKeywordDiagnosisUiState
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
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
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
class NaverKeywordExposureTest {

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
        val file = tempFolder.newFile("nv_diag_cfg_${System.nanoTime()}.preferences_pb")
        return ServerConfigStore(PreferenceDataStoreFactory.create(scope = scope) { file })
    }

    private fun createWarehouseStore(scope: TestScope): PostWarehouseStore {
        val file = tempFolder.newFile("nv_diag_wh_${System.nanoTime()}.preferences_pb")
        return PostWarehouseStore(PreferenceDataStoreFactory.create(scope = scope) { file })
    }

    private val stubEvalAdvisor = object : AiSeoGeoAdvisor {
        override suspend fun evaluateWith30Criteria(
            slot: GeneratedPostSlot,
            config: ServerAutomationConfig,
            proFunnel: ProFunnelConfig
        ): AiEvaluationOutcome {
            val eval = Article30PointEvaluator.evaluate(slot, config, proFunnel)
            return AiEvaluationOutcome.Success(
                AiEvaluationReport(
                    caseId = slot.auctionCase.caseId,
                    modelName = "test-eval",
                    totalScore = eval.totalScore,
                    passedCount = eval.passedCount,
                    aiSummary = "테스트",
                    overallVerdict = "등록 가능",
                    criteriaResults = eval.items,
                    autoFixSuggestions = emptyList(),
                    evaluatedAt = eval.evaluatedAt
                )
            )
        }
    }

    private val stubSearchAdvisor = object : AiCaseSearchAdvisor {
        override suspend fun optimizeCaseForAiSearch(
            slot: GeneratedPostSlot,
            searchQuery: String
        ): AiCaseSearchOutcome = AiCaseSearchOutcome.Error("unused")
    }

    @Test
    fun `diagnoseNaverKeywordExposure returns apiKeyMissing error when env placeholder keys are not configured`() = runTest(testDispatcher) {
        val client = NaverRealEstateApiClient(
            defaultClientId = "NAVER_CLIENT_ID_DEFAULT_VALUE",
            defaultClientSecret = "NAVER_CLIENT_SECRET_DEFAULT_VALUE",
            ioDispatcher = testDispatcher
        )
        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            postWarehouseStore = createWarehouseStore(this)
        )
        val vm = StudioViewModel(
            repository = repo,
            aiAdvisor = stubEvalAdvisor,
            naverApiClient = client,
            aiCaseSearchAdvisor = stubSearchAdvisor
        )

        vm.diagnoseNaverKeywordExposure("부산 정관 2024타경4501 경매")
        val state = vm.uiState.value.naverKeywordDiagnosis
        assertTrue(state is NaverKeywordDiagnosisUiState.Error)
        val err = state as NaverKeywordDiagnosisUiState.Error
        assertTrue(err.apiKeyMissing)
        assertTrue(err.message.contains("NAVER_CLIENT_ID"))
    }

    @Test
    fun `diagnoseNaverKeywordExposure calls Naver Search API with env headers and scores blue ocean keyword`() = runTest(testDispatcher) {
        var capturedId: String? = null
        var capturedSecret: String? = null
        var capturedUrl: String? = null

        val fakeJson = """
            {
              "lastBuildDate": "Tue, 24 Nov 2026 10:00:00 +0900",
              "total": 148,
              "start": 1,
              "display": 3,
              "items": [
                {
                  "title": "<b>2024타경4501</b> 기장 정관 아파트 임장 후기",
                  "link": "https://blog.naver.com/tester/1",
                  "description": "감정가 대비 최저가 64% 수준입니다.",
                  "bloggername": "경매노트",
                  "postdate": "20261010"
                },
                {
                  "title": "부산 기장 정관동 매매 전세 시세 정리",
                  "link": "https://blog.naver.com/tester/2",
                  "description": "정관신도시 학군과 교통 분석.",
                  "bloggername": "동부산부동산",
                  "postdate": "20260928"
                },
                {
                  "title": "정관 동일스위트 최근 실거래가 확인",
                  "link": "https://blog.naver.com/tester/3",
                  "description": "일반 매매 거래 추이입니다.",
                  "bloggername": "시세리포트",
                  "postdate": "20260915"
                }
              ]
            }
        """.trimIndent()

        val client = NaverRealEstateApiClient(
            client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
                val req = chain.request()
                capturedId = req.header("X-Naver-Client-Id")
                capturedSecret = req.header("X-Naver-Client-Secret")
                capturedUrl = req.url.toString()
                Response.Builder()
                    .request(req)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(fakeJson.toResponseBody("application/json".toMediaType()))
                    .build()
            }).build(),
            ioDispatcher = testDispatcher,
            defaultClientId = "env-naver-client-id-123",
            defaultClientSecret = "env-naver-client-secret-456"
        )
        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            postWarehouseStore = createWarehouseStore(this)
        )
        val vm = StudioViewModel(
            repository = repo,
            aiAdvisor = stubEvalAdvisor,
            naverApiClient = client,
            aiCaseSearchAdvisor = stubSearchAdvisor
        )

        vm.diagnoseNaverKeywordExposure("부산 정관 2024타경4501 경매")

        assertEquals("env-naver-client-id-123", capturedId)
        assertEquals("env-naver-client-secret-456", capturedSecret)
        assertTrue(capturedUrl!!.contains("/v1/search/blog.json"))

        val diagState = vm.uiState.value.naverKeywordDiagnosis
        assertTrue(diagState is NaverKeywordDiagnosisUiState.Success)
        val report = (diagState as NaverKeywordDiagnosisUiState.Success).report
        assertEquals(148, report.snapshot.totalSearchCount)
        assertEquals(ExposurePotentialGrade.BLUE_OCEAN, report.grade)
        assertTrue(report.exposureScore >= 78)
        assertEquals(1, report.snapshot.exactCaseMatchCount)
        assertFalse(report.recommendedTitlePattern.isBlank())
        assertTrue(report.recommendedSubKeywords.isNotEmpty())
    }

    @Test
    fun `NaverKeywordExposureEngine scores saturated broad keyword lower than specific case keyword`() {
        val saturatedJson = """
            {
              "total": 98400,
              "items": [
                {"title": "아파트 경매 방법 완벽 정리", "description": "경매 권리분석 낙찰 임차인", "bloggername": "A", "postdate": "20261124"},
                {"title": "서울 아파트 경매 추천 물건", "description": "감정가 최저가 입찰가", "bloggername": "B", "postdate": "20261123"},
                {"title": "아파트 경매 초보 가이드", "description": "말소기준권리 등기부 임장", "bloggername": "C", "postdate": "20261122"},
                {"title": "수도권 경매 매각기일 일정", "description": "유찰 최저가", "bloggername": "D", "postdate": "20261121"},
                {"title": "아파트 경매 대항력 체크", "description": "임차인 배당요구", "bloggername": "E", "postdate": "20261120"},
                {"title": "경매 낙찰 후 명도 절차", "description": "인도명령 강제집행", "bloggername": "F", "postdate": "20261119"},
                {"title": "아파트 경매 대출 한도", "description": "경매 잔금 대출", "bloggername": "G", "postdate": "20261118"}
              ]
            }
        """.trimIndent()

        val report = NaverKeywordExposureEngine.analyzeBlogSearchJson(
            keyword = "아파트 경매",
            rawJson = saturatedJson,
            referenceDateYmd = "20261124"
        )
        assertEquals(98400, report.snapshot.totalSearchCount)
        assertEquals(7, report.snapshot.recent7DaysCount)
        assertTrue(report.exposureScore < 38)
        assertEquals(ExposurePotentialGrade.SATURATED, report.grade)
    }
}
