package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.ai.AiCaseSearchAdvisor
import com.example.ai.AiCaseSearchOutcome
import com.example.ai.AiEvaluationOutcome
import com.example.ai.AiSeoGeoAdvisor
import com.example.ai.FirebaseAiCaseSearchService
import com.example.data.BlogAutomationRepository
import com.example.data.DirectChannelPublishClient
import com.example.data.NaverRealEstateApiClient
import com.example.data.PostWarehouseStore
import com.example.data.ServerConfigStore
import com.example.engine.Article30PointEvaluator
import com.example.engine.DefaultSpecDataFactory
import com.example.engine.HtmlExportEngine
import com.example.engine.SecurityAuditEngine
import com.example.model.AiCaseGeoOptimizationPack
import com.example.model.AiCaseSearchUiState
import com.example.model.AiEvaluationReport
import com.example.model.ChannelPlatform
import com.example.model.DdakchalQaPair
import com.example.model.GeneratedPostSlot
import com.example.model.PostStatus
import com.example.model.ProFunnelConfig
import com.example.model.PublishLogEntry
import com.example.model.SecurityGrade
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
class SecurityAiSearchHtmlTest {

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
        val file = tempFolder.newFile("sec_cfg_${System.nanoTime()}.preferences_pb")
        return ServerConfigStore(PreferenceDataStoreFactory.create(scope = scope) { file })
    }

    private fun createWarehouseStore(scope: TestScope): PostWarehouseStore {
        val file = tempFolder.newFile("sec_wh_${System.nanoTime()}.preferences_pb")
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
                    aiSummary = "테스트 평가",
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
        ): AiCaseSearchOutcome {
            val c = slot.auctionCase
            val faqs = listOf(
                DdakchalQaPair("${c.caseId} 최저가와 입찰보증금은 얼마인가요?", "최저가는 감정가 대비 ${c.discountRatePct}%이며 보증금은 10%입니다."),
                DdakchalQaPair("${c.caseId} 말소기준권리 이후 권리는 인수되나요?", "말소기준권리(${c.baseRightDate}) 이후 후순위 권리는 원칙적으로 말소 대상입니다."),
                DdakchalQaPair("${c.caseId} 현장 임장에서 확인할 포인트는?", "관리비 체납 여부와 임차인 점유 현황을 반드시 확인하세요.")
            )
            val jsonLd = FirebaseAiCaseSearchService.buildFaqAndListingJsonLd(
                slot = slot,
                directAnswer = "${c.courtName} ${c.caseId} 물건 핵심 분석입니다.",
                faqs = faqs
            )
            return AiCaseSearchOutcome.Success(
                AiCaseGeoOptimizationPack(
                    caseId = c.caseId,
                    searchQuery = searchQuery,
                    hookSnippet = "${c.caseId} ${c.address} 경매 핵심 브리핑",
                    directAnswerParagraph = "${c.courtName} ${c.caseId} 물건의 말소기준권리는 ${c.baseRightDate}입니다.",
                    aiKeywords = listOf("${c.caseId}경매", "권리분석핵심", "입찰보증금계산", "말소기준권리", "실거래가비교"),
                    faqPairs = faqs,
                    faqJsonLd = jsonLd,
                    estimatedGeoScore = 97,
                    modelUsed = "gemini-3.8-flash-test",
                    generatedAt = "2026-11-24 12:00:00"
                )
            )
        }
    }

    @Test
    fun `security audit detects token leak and legacy passwords while purge restores zero password compliance`() = runTest(testDispatcher) {
        val secretToken = "sync_secret_99887766"
        val cleanCfg = ServerAutomationConfig(
            ddakchalApiBase = "https://ddakchal.pages.dev",
            crawlerSyncToken = secretToken,
            cfAccountId = "cf-acc-01",
            cfApiToken = "cf_token_abc123456789",
            naverBlogId = "jay_auction_blog",
            naverWorkerRelayEndpoint = "https://ddakchal.pages.dev/api/v1/blog/relay",
            naverPw = "",
            tistoryBlogUrl = "https://jay-auction.tistory.com",
            tistoryId = "kakao_jay",
            tistoryPw = ""
        )
        val slots = DefaultSpecDataFactory.buildInitialDaily5Slots(cleanCfg)

        // 1. Clean zero-password config yields no leaks and high score (S grade)
        val cleanSummary = SecurityAuditEngine.auditSecurity(
            config = cleanCfg,
            slots = slots,
            publishLogs = emptyList(),
            naverReachable = true,
            tistoryReachable = true,
            nowText = "2026-11-24 12:00:00"
        )
        assertEquals(0, cleanSummary.externalLeakCount)
        assertFalse(cleanSummary.legacyPlaintextPwDetected)
        assertEquals(SecurityGrade.S, cleanSummary.overallGrade)
        assertTrue(cleanSummary.overallScore >= 90)

        // 2. Inject secret leak & legacy plaintext password -> audit flags both
        val dirtySlot = slots.first().let { s ->
            val nv = s.variants[ChannelPlatform.NAVER]!!
            s.copy(variants = s.variants + (ChannelPlatform.NAVER to nv.copy(bodyText = "${nv.bodyText}\nToken: $secretToken")))
        }
        val dirtyCfg = cleanCfg.copy(naverPw = "legacy-plain-pw!", tistoryPw = "kakao-plain!")
        val dirtyLogs = listOf(
            PublishLogEntry(
                logId = "log-leak",
                postId = dirtySlot.postId,
                caseId = dirtySlot.auctionCase.caseId,
                platform = ChannelPlatform.NAVER,
                status = PostStatus.FAILED,
                publishedUrl = null,
                errorMessage = "Header Bearer leakedToken1234567",
                handoffReason = null,
                retryCount = 0,
                publishedAt = "2026-11-24 12:01:00"
            )
        )
        val dirtySummary = SecurityAuditEngine.auditSecurity(
            config = dirtyCfg,
            slots = listOf(dirtySlot),
            publishLogs = dirtyLogs
        )
        assertTrue(dirtySummary.externalLeakCount >= 2)
        assertTrue(dirtySummary.legacyPlaintextPwDetected)
        assertTrue(dirtySummary.overallScore < cleanSummary.overallScore)

        // 3. ViewModel purge removes legacy plaintext passwords cleanly
        val cfgStore = createConfigStore(this)
        cfgStore.saveConfig(dirtyCfg)
        val repo = BlogAutomationRepository(
            configStore = cfgStore,
            postWarehouseStore = createWarehouseStore(this)
        )
        val vm = StudioViewModel(
            repository = repo,
            aiAdvisor = stubEvalAdvisor,
            aiCaseSearchAdvisor = stubSearchAdvisor
        )
        vm.updateConfig(dirtyCfg)
        assertTrue(vm.uiState.value.config.naverPw.isNotBlank())

        vm.purgeLegacyPlaintextPasswords()
        val postPurge = vm.uiState.value
        assertEquals("", postPurge.config.naverPw)
        assertEquals("", postPurge.config.tistoryPw)
        assertFalse(postPurge.securityDashboard.legacyPlaintextPwDetected)
    }

    @Test
    fun `probeNaverAndTistory verifies live urls and updates security account grades`() = runTest(testDispatcher) {
        val fakeOkClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("<html><body>OK</body></html>".toResponseBody("text/html".toMediaType()))
                    .build()
            })
            .build()

        val publishClient = DirectChannelPublishClient(
            httpClient = fakeOkClient,
            ioDispatcher = testDispatcher
        )
        val cfg = ServerAutomationConfig(
            ddakchalApiBase = "https://ddakchal.pages.dev",
            crawlerSyncToken = "sync-tok-12345",
            naverBlogId = "jay_auction",
            tistoryBlogUrl = "https://jay-auction.tistory.com",
            tistoryId = "kakao_id"
        )
        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            postWarehouseStore = createWarehouseStore(this)
        )
        val vm = StudioViewModel(
            repository = repo,
            aiAdvisor = stubEvalAdvisor,
            naverApiClient = NaverRealEstateApiClient(fakeOkClient),
            directPublishClient = publishClient,
            aiCaseSearchAdvisor = stubSearchAdvisor
        )
        vm.updateConfig(cfg)
        vm.probeNaverAndTistoryConnections()

        val state = vm.uiState.value
        val naverProbe = requireNotNull(state.naverProbeResult)
        val tistoryProbe = requireNotNull(state.tistoryProbeResult)
        assertTrue(naverProbe.reachableOk)
        assertTrue(tistoryProbe.reachableOk)
        assertEquals(200, tistoryProbe.httpStatusCode)
        assertEquals(SecurityGrade.S, state.securityDashboard.overallGrade)
    }

    @Test
    fun `ai case search injects FAQPage GEO pack without naver url leak and supports test registration`() = runTest(testDispatcher) {
        val fakeOkClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val url = chain.request().url.toString()
                val jsonBody = if (url.contains("/api/")) """{"ok":true,"url":"$url"}""" else "<html><body>OK</body></html>"
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(jsonBody.toResponseBody("application/json".toMediaType()))
                    .build()
            })
            .build()

        val cfg = ServerAutomationConfig(
            ddakchalApiBase = "https://ddakchal.pages.dev",
            crawlerSyncToken = "sync-tok-99999",
            naverBlogId = "jay_auction",
            tistoryBlogUrl = "https://jay-auction.tistory.com",
            enableExternalLinkNaver = false,
            publishTargetNaver = true,
            publishTargetTistory = true,
            publishTargetCloudflare = true
        )
        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            postWarehouseStore = createWarehouseStore(this)
        )
        val vm = StudioViewModel(
            repository = repo,
            aiAdvisor = stubEvalAdvisor,
            naverApiClient = NaverRealEstateApiClient(fakeOkClient),
            directPublishClient = DirectChannelPublishClient(fakeOkClient, ioDispatcher = testDispatcher),
            aiCaseSearchAdvisor = stubSearchAdvisor
        )
        vm.updateConfig(cfg)

        val targetCaseId = vm.uiState.value.slots.first().auctionCase.caseId
        vm.searchCaseWithAiAndOptimize(targetCaseId)

        val searchState = vm.uiState.value.aiCaseSearchState
        assertTrue(searchState is AiCaseSearchUiState.Success)
        val matched = (searchState as AiCaseSearchUiState.Success).matchedSlot
        val cfVariant = matched.variants[ChannelPlatform.CLOUDFLARE]!!
        val naverVariant = matched.variants[ChannelPlatform.NAVER]!!

        assertTrue(cfVariant.jsonLdSchema.contains("FAQPage"))
        assertTrue(cfVariant.bodyHtml.contains("faq-geo"))
        assertFalse(naverVariant.bodyText.contains("http://"))
        assertFalse(naverVariant.bodyText.contains("https://"))

        vm.testRegisterAiSearchedCase()
        val updatedSlot = vm.uiState.value.slots.first { it.postId == matched.postId }
        assertTrue(updatedSlot.status == PostStatus.PUBLISHED || updatedSlot.status == PostStatus.QUEUED)
    }

    @Test
    fun `generateThreePureHtmlPosts produces 3 standalone HTML documents in both 3-case and 3-channel modes`() = runTest(testDispatcher) {
        val cfg = ServerAutomationConfig(
            crawlerSyncToken = "secret_should_never_appear_in_html",
            enableExternalLinkNaver = false
        )
        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            postWarehouseStore = createWarehouseStore(this)
        )
        val vm = StudioViewModel(
            repository = repo,
            aiAdvisor = stubEvalAdvisor,
            aiCaseSearchAdvisor = stubSearchAdvisor
        )
        vm.updateConfig(cfg)

        // Mode 1: 3 distinct auction cases
        vm.generateThreePureHtmlPosts(threeCasesMode = true, preferredChannel = ChannelPlatform.NAVER)
        val bundle3Cases = vm.uiState.value.htmlBatchBundle
        assertNotNull(bundle3Cases)
        assertEquals(3, bundle3Cases!!.items.size)
        assertTrue(bundle3Cases.threeCasesMode)
        bundle3Cases.items.forEach { item ->
            assertTrue(item.pureHtml.startsWith("<!DOCTYPE html>"))
            assertTrue(item.pureHtml.contains("<html lang=\"ko\">"))
            assertTrue(item.pureHtml.contains("</html>"))
            assertFalse(item.pureHtml.contains("secret_should_never_appear_in_html"))
            assertFalse(item.pureHtml.contains("https://"))
        }

        // Mode 2: 1 auction case across 3 channels
        val directBatch = HtmlExportEngine.buildThreePureHtmlBatch(
            slots = vm.uiState.value.slots,
            threeCasesMode = false,
            preferredChannel = ChannelPlatform.CLOUDFLARE,
            config = cfg,
            nowText = "2026-11-24 12:15:00"
        )
        assertEquals(3, directBatch.items.size)
        assertFalse(directBatch.threeCasesMode)
        assertEquals(
            listOf(ChannelPlatform.CLOUDFLARE, ChannelPlatform.TISTORY, ChannelPlatform.NAVER),
            directBatch.items.map { it.platform }
        )
    }
}
