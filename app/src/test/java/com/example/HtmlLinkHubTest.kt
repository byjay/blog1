package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.ai.AiEvaluationOutcome
import com.example.ai.AiSeoGeoAdvisor
import com.example.data.BlogAutomationRepository
import com.example.data.CloudflareAndDdakchalApi
import com.example.data.NaverRealEstateApiClient
import com.example.data.PostWarehouseStore
import com.example.data.ServerConfigStore
import com.example.engine.Article30PointEvaluator
import com.example.engine.HtmlExportEngine
import com.example.model.AiEvaluationReport
import com.example.model.ChannelPlatform
import com.example.model.GeneratedPostSlot
import com.example.model.HtmlLinkFilter
import com.example.model.HtmlViewerTarget
import com.example.model.PostStatus
import com.example.model.ProFunnelConfig
import com.example.model.PublishLogEntry
import com.example.model.ServerAutomationConfig
import com.example.ui.StudioViewModel
import com.example.ui.screens.parseDdakchalInternalLink
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
import org.junit.Assert.assertNull
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
class HtmlLinkHubTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()

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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createConfigStore(scope: TestScope): ServerConfigStore {
        val file = tempFolder.newFile("hub_cfg_${System.nanoTime()}.preferences_pb")
        return ServerConfigStore(PreferenceDataStoreFactory.create(scope = scope) { file })
    }

    private fun createWarehouseStore(scope: TestScope): PostWarehouseStore {
        val file = tempFolder.newFile("hub_wh_${System.nanoTime()}.preferences_pb")
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

    private val sampleAssetJson = """
        {
          "ok": true,
          "data": {
            "id": "asset-2025-6297",
            "case_no": "2025타경6297",
            "auction_dedup_key": "busan-2025-6297",
            "court_name": "부산지방법원 동부지원",
            "address": "부산광역시 기장군 정관읍 모전리 롯데캐슬2차 105동 1201호",
            "property_type": "APT_OFFICETEL",
            "appraisal_krw": 420000000,
            "min_bid_krw": 268800000,
            "discount_rate_pct": 64,
            "base_right_date": "2020-05-14",
            "dividend_deadline": "2026-03-10",
            "claim_amount_krw": 240000000,
            "possession_summary": "채무자 겸 소유자 점유",
            "tenant_summary": "전입세대열람 소유자 세대만 등재",
            "assumed_rights_text": "말소기준권리 이후 후순위 소멸",
            "real_trade_median_krw": 395000000,
            "photo_urls": [
              "https://r2.ddakchal.pages.dev/photos/2025-6297-1.webp"
            ],
            "registry_eul_gu": [
              {"rankOrder": "을구 1번", "registeredDate": "2020-05-14", "title": "근저당권설정", "holderOrCreditor": "시중은행", "amountOrShare": "2억 8,800만원", "statusBadge": "말소기준권리", "plainExplanation": "기준 권리", "isBaseRight": true}
            ],
            "visit_records": [
              {"visitRound": "집행관 1차 방문", "visitedAt": "2026-11-09", "source": "COURT_BAILIFF", "visitorLabel": "집행관", "occupancyStatus": "소유자 점유", "findingsNote": "임대차 없음 확인"}
            ]
          }
        }
    """.trimIndent()

    @Test
    fun `empty catalog renders honest empty state hub html with zero fake entries or urls`() {
        val cfg = ServerAutomationConfig()
        val catalog = HtmlExportEngine.buildLinkedHtmlCatalog(
            slots = emptyList(),
            warehouseArticles = emptyList(),
            publishLogs = emptyList(),
            config = cfg
        )
        assertTrue(catalog.isEmpty())

        val hubHtml = HtmlExportEngine.renderHubIndexHtml(
            entries = catalog,
            config = cfg,
            generatedAt = "2026-11-28 09:00",
            forExternalFileExport = false
        )
        assertTrue(hubHtml.contains("아직 생성된 글이 없습니다"))
        assertFalse(hubHtml.contains("ddakchal://post/"))
    }

    @Test
    fun `hub index and article detail html contain bidirectional links and only real published urls`() = runTest(testDispatcher) {
        val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            val req = chain.request()
            if (req.url.encodedPath.contains("/api/v1/blog/assets/")) {
                jsonResponse(req, 200, sampleAssetJson)
            } else {
                jsonResponse(req, 404, """{"error":"not found"}""")
            }
        }).build()

        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            api = CloudflareAndDdakchalApi(client = client, ioDispatcher = testDispatcher),
            postWarehouseStore = createWarehouseStore(this)
        )
        val vm = StudioViewModel(
            repository = repo,
            aiAdvisor = offlineAdvisor,
            naverApiClient = NaverRealEstateApiClient(ioDispatcher = testDispatcher)
        )
        vm.applyInlineServerAuthAndSave("https://ddakchal.pages.dev", "sync-token", "", "", "")
        vm.lookupCaseNumberFromTodakAndAutoPreview("2025타경6297", preferredTabIndex = 0)

        val slot = vm.uiState.value.slots.first()
        val cfg = vm.uiState.value.config

        // Without publishLogs/publishedUrls -> publishedUrls stays empty (no fake live links)
        val unpubCatalog = HtmlExportEngine.buildLinkedHtmlCatalog(
            slots = listOf(slot),
            warehouseArticles = vm.uiState.value.warehouseArticles,
            publishLogs = emptyList(),
            config = cfg
        )
        assertEquals(1, unpubCatalog.size)
        assertTrue("Must not invent fake published web URLs", unpubCatalog.first().publishedUrls.isEmpty())

        // With verified PublishLogEntry -> catalog exposes exact verified URL
        val realUrl = "https://blog.naver.com/cstarfirst/22334455"
        val verifiedLog = PublishLogEntry(
            logId = "log-1",
            postId = slot.postId,
            caseId = slot.auctionCase.caseId,
            platform = ChannelPlatform.NAVER,
            status = PostStatus.PUBLISHED,
            publishedUrl = realUrl,
            errorMessage = null,
            handoffReason = null,
            retryCount = 0,
            publishedAt = "2026-11-28 09:05"
        )
        val pubCatalog = HtmlExportEngine.buildLinkedHtmlCatalog(
            slots = listOf(slot),
            warehouseArticles = vm.uiState.value.warehouseArticles,
            publishLogs = listOf(verifiedLog),
            config = cfg
        )
        assertEquals(realUrl, pubCatalog.first().publishedUrls[ChannelPlatform.NAVER])

        val inAppHubHtml = HtmlExportEngine.renderHubIndexHtml(pubCatalog, cfg, "2026-11-28 09:10", forExternalFileExport = false)
        assertTrue(inAppHubHtml.contains("ddakchal://post/${slot.postId}/"))
        assertTrue(inAppHubHtml.contains(realUrl))

        val inAppDetailHtml = HtmlExportEngine.renderLinkedArticleDetailHtml(
            slot = slot,
            channel = ChannelPlatform.CLOUDFLARE,
            config = cfg,
            forExternalFileExport = false
        )
        assertTrue(inAppDetailHtml.contains("ddakchal://hub"))
        assertTrue(inAppDetailHtml.contains("ddakchal://post/${slot.postId}/naver"))

        val fileHubHtml = HtmlExportEngine.renderHubIndexHtml(pubCatalog, cfg, "2026-11-28 09:10", forExternalFileExport = true)
        val slug = HtmlExportEngine.buildLinkedHtmlSlug(slot.auctionCase.caseId, slot.auctionCase.dedupKey, ChannelPlatform.CLOUDFLARE)
        assertTrue(fileHubHtml.contains("./$slug"))

        val fileDetailHtml = HtmlExportEngine.renderLinkedArticleDetailHtml(
            slot = slot,
            channel = ChannelPlatform.CLOUDFLARE,
            config = cfg,
            forExternalFileExport = true
        )
        assertTrue(fileDetailHtml.contains("./ddakchal_hub.html"))

        // Internal link parser check
        assertEquals(HtmlViewerTarget.HubIndex, parseDdakchalInternalLink("ddakchal://hub"))
        assertEquals(
            HtmlViewerTarget.ArticleDetail(slot.postId, ChannelPlatform.NAVER),
            parseDdakchalInternalLink("ddakchal://post/${slot.postId}/naver")
        )
        assertNull(parseDdakchalInternalLink("https://aution.codeb.app/blog/abc"))
    }

    @Test
    fun `viewmodel html hub and verify-edit-preview workflow updates state cleanly without premature publish`() = runTest(testDispatcher) {
        val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            val req = chain.request()
            if (req.url.encodedPath.contains("/api/v1/blog/assets/")) {
                jsonResponse(req, 200, sampleAssetJson)
            } else {
                jsonResponse(req, 404, """{"error":"not found"}""")
            }
        }).build()

        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            api = CloudflareAndDdakchalApi(client = client, ioDispatcher = testDispatcher),
            postWarehouseStore = createWarehouseStore(this)
        )
        val vm = StudioViewModel(
            repository = repo,
            aiAdvisor = offlineAdvisor,
            naverApiClient = NaverRealEstateApiClient(ioDispatcher = testDispatcher)
        )
        vm.applyInlineServerAuthAndSave("https://ddakchal.pages.dev", "sync-token", "", "", "")
        vm.lookupCaseNumberFromTodakAndAutoPreview("2025타경6297", preferredTabIndex = 0)
        val slot = vm.uiState.value.slots.first()

        // 1. HTML Hub state transitions
        vm.openHtmlLinkHub()
        assertTrue(vm.uiState.value.showHtmlLinkHub)
        vm.setHtmlLinkFilter(HtmlLinkFilter.LOCAL_ONLY)
        assertEquals(HtmlLinkFilter.LOCAL_ONLY, vm.uiState.value.htmlLinkFilter)
        vm.openHtmlHubViewer()
        assertEquals(HtmlViewerTarget.HubIndex, vm.uiState.value.activeHtmlViewerTarget)
        vm.openHtmlArticleViewer(slot.postId, ChannelPlatform.TISTORY)
        assertEquals(HtmlViewerTarget.ArticleDetail(slot.postId, ChannelPlatform.TISTORY), vm.uiState.value.activeHtmlViewerTarget)
        vm.closeHtmlViewer()
        assertNull(vm.uiState.value.activeHtmlViewerTarget)
        vm.closeHtmlLinkHub()
        assertFalse(vm.uiState.value.showHtmlLinkHub)

        // 2. Verify -> AutoFix without publishing -> Manual Edit & Reverify
        vm.verifySlotBeforeRegistration(slot.postId)
        assertTrue(vm.uiState.value.statusMessage.orEmpty().contains("검증 완료"))

        vm.autoFixSlotForPreview(slot.postId)
        val fixedSlot = vm.uiState.value.slots.first { it.postId == slot.postId }
        assertFalse("autoFixSlotForPreview must keep slot unpublished until user clicks Register", fixedSlot.status == PostStatus.PUBLISHED)

        val editedTitle = "[수정완료] 기장 롯데캐슬2차 2025타경6297 권리·수익성 분석"
        val editedBody = fixedSlot.variants[ChannelPlatform.NAVER]?.bodyText.orEmpty() +
            "\n\n[직접 수정 검증 메모] 010-1234-5678 연락처 자동 마스킹 확인 및 입찰 체크리스트 보완 완료."
        val editedPinned = "딱찰 무료 권리분석 리포트 신청 링크"

        vm.updateSlotVariantContent(
            postId = slot.postId,
            platform = ChannelPlatform.NAVER,
            editedTitle = editedTitle,
            editedBodyText = editedBody,
            editedPinnedComment = editedPinned
        )

        val editedSlot = vm.uiState.value.slots.first { it.postId == slot.postId }
        val naverVariant = editedSlot.variants[ChannelPlatform.NAVER]
        assertNotNull(naverVariant)
        assertEquals(editedTitle, naverVariant!!.title)
        assertTrue(naverVariant.bodyText.contains("[직접 수정 검증 메모]"))
        assertFalse("Phone PII must be sanitized on manual edit", naverVariant.bodyText.contains("010-1234-5678"))
        assertEquals(editedPinned, naverVariant.pinnedCommentSnippet)
        assertTrue(vm.uiState.value.statusMessage.orEmpty().contains("수정 저장 및 재검증 완료"))
    }
}
