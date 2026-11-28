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
import com.example.engine.CourtEvidenceRecorder
import com.example.engine.CourtSharpAnalyzer
import com.example.engine.DefaultSpecDataFactory
import com.example.engine.HtmlExportEngine
import com.example.engine.LegalDoctrineVerifier
import com.example.engine.ProAnalysisAndPhotoGuideEngine
import com.example.engine.QaVerificationEngine
import com.example.engine.QualityDefenseEngine
import com.example.model.AiEvaluationReport
import com.example.model.ChannelPlatform
import com.example.model.ChannelPublishStat
import com.example.model.CourtDocKind
import com.example.model.DdakchalTriggerStatusSnapshot
import com.example.model.GeneratedPostSlot
import com.example.model.PostStatus
import com.example.model.ProFunnelConfig
import com.example.model.PropertyType
import com.example.model.QueueAnalyticsSummary
import com.example.model.ServerAutomationConfig
import com.example.model.ServerDbDiscoveredItem
import com.example.model.ServerTelemetryState
import com.example.model.TodakAdminConsoleState
import com.example.model.VisitorTrafficLog
import com.example.ui.StudioUiState
import com.example.ui.StudioViewModel
import com.example.ui.screens.computeDdakchalTopGraphMetrics
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
class CourtCaseAutoPreviewAndSharpAnalysisTest {

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
        val file = tempFolder.newFile("sharp_cfg_${System.nanoTime()}.preferences_pb")
        return ServerConfigStore(PreferenceDataStoreFactory.create(scope = scope) { file })
    }

    private fun createWarehouseStore(scope: TestScope): PostWarehouseStore {
        val file = tempFolder.newFile("sharp_wh_${System.nanoTime()}.preferences_pb")
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
    fun `StudioViewModel launches strictly with zero fake slots warehouse articles or visitor logs`() = runTest(testDispatcher) {
        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            postWarehouseStore = createWarehouseStore(this)
        )
        val vm = StudioViewModel(
            repository = repo,
            aiAdvisor = offlineAdvisor,
            naverApiClient = NaverRealEstateApiClient(ioDispatcher = testDispatcher)
        )
        val state = vm.uiState.value
        assertTrue("Initial slots must be empty (no dummy seed)", state.slots.isEmpty())
        assertNull("Initial selectedSlotId must be null", state.selectedSlotId)
        assertTrue("Initial warehouseArticles must be empty", state.warehouseArticles.isEmpty())
        assertTrue("Initial writingLogs must be empty", state.writingLogs.isEmpty())
        assertTrue("Initial visitorLogs must be empty", state.visitorLogs.isEmpty())
    }

    @Test
    fun `lookupCaseNumberFromTodakAndAutoPreview fetches live court photos registry visits and sets pending auto preview`() = runTest(testDispatcher) {
        val liveAssetJson = """
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
                  "https://r2.ddakchal.pages.dev/photos/2025-6297-1.webp",
                  "https://r2.ddakchal.pages.dev/photos/2025-6297-2.webp"
                ],
                "registry_gap_gu": [
                  {"rankOrder": "갑구 1번", "registeredDate": "2018-04-02", "title": "소유권이전", "holderOrCreditor": "현 소유자", "amountOrShare": "지분 1/1", "statusBadge": "매각이전", "plainExplanation": "잔금 완납 시 이전"},
                  {"rankOrder": "갑구 2번", "registeredDate": "2025-01-12", "title": "임의경매개시결정", "holderOrCreditor": "부산지방법원", "amountOrShare": "2억 4,000만원", "statusBadge": "매각소멸", "plainExplanation": "낙찰 후 말소"}
                ],
                "registry_eul_gu": [
                  {"rankOrder": "을구 1번", "registeredDate": "2020-05-14", "title": "근저당권설정", "holderOrCreditor": "시중은행", "amountOrShare": "2억 8,800만원", "statusBadge": "말소기준권리", "plainExplanation": "기준 권리", "isBaseRight": true}
                ],
                "court_documents": [
                  {"docKind": "SALE_SPECIFICATION", "referenceNo": "2025타경6297-명세", "issuedDate": "2026-11-15", "issuer": "부산지방법원 동부지원", "summaryHighlight": "인수되는 권리 없음", "fullRecordLines": ["최선순위 설정일 2020-05-14"], "riskFlagNote": "관리비 체납 확인"}
                ],
                "visit_records": [
                  {"visitRound": "집행관 1차 방문", "visitedAt": "2026-11-09", "source": "COURT_BAILIFF", "visitorLabel": "집행관", "occupancyStatus": "소유자 점유", "findingsNote": "임대차 없음 확인"},
                  {"visitRound": "현장 임장", "visitedAt": "2026-11-20", "source": "FIELD_IMJANG", "visitorLabel": "현장조사", "occupancyStatus": "외관 양호", "findingsNote": "관리사무소 체납 없음 확인"}
                ]
              }
            }
        """.trimIndent()

        val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            val req = chain.request()
            if (req.url.encodedPath.contains("/api/v1/blog/assets/")) {
                jsonResponse(req, 200, liveAssetJson)
            } else {
                jsonResponse(req, 404, """{"error":"not found"}""")
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
        vm.applyInlineServerAuthAndSave(
            apiBase = "https://ddakchal.pages.dev",
            syncToken = "live-secret-token",
            cfAccountId = "",
            cfD1DatabaseId = "",
            cfApiToken = ""
        )

        // With spaces in input -> normalized cleanly
        vm.lookupCaseNumberFromTodakAndAutoPreview(" 2025 타경 6297 ", preferredTabIndex = 2)

        val state = vm.uiState.value
        assertEquals("2025타경6297", state.slots.first().auctionCase.caseId)
        assertNotNull("pendingAutoPreviewSlotId must be set", state.pendingAutoPreviewSlotId)
        assertEquals(2, state.pendingAutoPreviewTab)
        assertTrue(state.pendingAutoPreviewNonce > 0L)

        val archive = state.activeCourtArchive
        assertNotNull(archive)
        assertEquals(2, archive!!.photos.size)
        assertEquals(2, archive.registryGapGu.size)
        assertEquals(1, archive.registryEulGu.size)
        assertEquals(1, archive.documents.size)
        assertEquals(2, archive.visitRecords.size)
        assertNotNull(archive.sharpAnalysis)
        assertEquals(4, archive.sharpAnalysis!!.checks.size)
        assertEquals(3, archive.sharpAnalysis!!.killerTrapPoints.size)
        assertTrue(archive.sharpAnalysis!!.sharpGradeBadge.contains("칼분석"))

        // Consuming preview clears pendingAutoPreviewSlotId
        vm.consumePendingAutoPreview()
        assertNull(vm.uiState.value.pendingAutoPreviewSlotId)
    }

    @Test
    fun `lookupCaseNumberFromTodakAndAutoPreview fails honestly on 404 without creating dummy slot`() = runTest(testDispatcher) {
        val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            jsonResponse(chain.request(), 404, """{"error":"case not found"}""")
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
        vm.applyInlineServerAuthAndSave("https://ddakchal.pages.dev", "tok-123", "", "", "")

        vm.lookupCaseNumberFromTodakAndAutoPreview("2025타경999999", preferredTabIndex = 0)

        val state = vm.uiState.value
        assertTrue("No fake slot may be created on 404", state.slots.isEmpty())
        assertNull(state.pendingAutoPreviewSlotId)
        assertEquals(0L, state.pendingAutoPreviewNonce)
        assertTrue(state.errorMessage?.contains("404") == true || state.todakAdmin.caseLookupStatus.contains("404"))
    }

    @Test
    fun `all 5 property types across 12 style rotations embed sharp analysis pass 95plus score and QA gates`() {
        val cfg = ServerAutomationConfig(enableExternalLinkNaver = false)
        val funnel = ProFunnelConfig()
        val auctions = TestFixtures.sampleAuctions()

        for (styleIdx in 0 until 12) {
            auctions.forEachIndexed { propIdx, auction ->
                val gap = TestFixtures.sampleGapGu(auction.caseId, auction.courtName)
                val eul = TestFixtures.sampleEulGu(auction.caseId, auction.baseRightDate)
                val docs = TestFixtures.sampleDocuments(auction.caseId, auction.courtName)
                val visits = TestFixtures.sampleVisits(auction.caseId)
                val variants = DefaultSpecDataFactory.buildDistinct3ChannelVariants(
                    auction = auction,
                    seedIndex = styleIdx + propIdx,
                    config = cfg,
                    proFunnel = funnel,
                    serverGapGu = gap,
                    serverEulGu = eul,
                    serverDocuments = docs,
                    visitRecords = visits
                )
                val scheduled = mapOf(
                    ChannelPlatform.CLOUDFLARE to "2026-11-23 09:13:17",
                    ChannelPlatform.TISTORY to "2026-11-23 10:21:29",
                    ChannelPlatform.NAVER to "2026-11-23 11:37:43"
                )
                val qa = QaVerificationEngine.auditSlot(auction, variants, scheduled, cfg)
                val slot = GeneratedPostSlot(
                    postId = "post-rot-$styleIdx-$propIdx",
                    queueId = "q-$styleIdx-$propIdx",
                    slotIndex = styleIdx + 1,
                    publishDate = "2026-11-23",
                    auctionCase = auction,
                    variants = variants,
                    scheduledTimes = scheduled,
                    status = PostStatus.QUEUED,
                    qaReport = qa,
                    serverRegistryGapGu = gap,
                    serverRegistryEulGu = eul,
                    serverCourtDocuments = docs,
                    visitRecords = visits
                )
                val eval = Article30PointEvaluator.evaluate(slot, cfg, funnel)

                assertTrue(
                    "QA failed for ${auction.propertyType} style=$styleIdx: chars=${qa.charCountByChannel}, cos=${qa.maxCrossChannelCosinePct}, jac=${qa.maxCrossChannelJaccard5GramPct}, hits=${qa.forbiddenHits}, mismatches=${qa.factCheckMismatches}",
                    qa.overallPassed
                )
                assertTrue(
                    "Score < 95 for ${auction.propertyType} style=$styleIdx: score=${eval.totalScore}, failed=${eval.items.filter { !it.passed }.map { it.title }}",
                    eval.totalScore >= 95
                )
                val naver = variants[ChannelPlatform.NAVER]!!
                assertFalse(naver.bodyText.contains("http://"))
                assertFalse(naver.bodyText.contains("https://"))
                assertTrue(QualityDefenseEngine.detectIssues(naver.bodyText, naver.title, ChannelPlatform.NAVER).isEmpty())
            }
        }
    }

    @Test
    fun `CourtSharpAnalyzer and HtmlExportEngine reflect real data and honest missing states`() {
        val cfg = ServerAutomationConfig(enableExternalLinkNaver = false)
        val fullSlot = TestFixtures.sampleSlots(cfg, withCourtEvidence = true).first()
        val cfVariant = fullSlot.variants[ChannelPlatform.CLOUDFLARE]!!
        val fullHtml = HtmlExportEngine.renderStandaloneHtmlDocument(fullSlot, cfVariant, cfg)

        assertTrue(fullHtml.contains("class=\"sharp-court-analysis\""))
        assertTrue(fullHtml.contains("class=\"court-photos\""))
        assertTrue(fullHtml.contains("class=\"registry-table\""))
        assertTrue(fullHtml.contains("class=\"visit-logs\""))

        // When photos, registry, docs, visits are missing from server -> analyzer flags them honestly without faking
        val sparseAuction = fullSlot.auctionCase
        val sparseArchive = CourtEvidenceRecorder.recordFullArchive(auction = sparseAuction)
        assertTrue(sparseArchive.photos.isEmpty())
        assertTrue(sparseArchive.documents.isEmpty())
        assertTrue(sparseArchive.registryGapGu.isEmpty())
        assertTrue(sparseArchive.registryEulGu.isEmpty())
        assertTrue(sparseArchive.visitRecords.isEmpty())
        assertNotNull(sparseArchive.sharpAnalysis)
        assertTrue(sparseArchive.sharpAnalysis!!.checks.any { it.isAlert && it.verdictBadge.contains("미수신") })

        val sparseSlot = fullSlot.copy(
            courtPhotoUrls = emptyList(),
            visitRecords = emptyList(),
            serverRegistryGapGu = emptyList(),
            serverRegistryEulGu = emptyList(),
            serverCourtDocuments = emptyList()
        )
        val sparseHtml = HtmlExportEngine.renderStandaloneHtmlDocument(sparseSlot, cfVariant, cfg)
        assertTrue(sparseHtml.contains("수신된 법원 실사 사진이 아직 없습니다"))
        assertTrue(sparseHtml.contains("수신된 등기부등본 갑구·을구 상세 항목이 없습니다"))
        assertTrue(sparseHtml.contains("수신된 법원 현황조사 방문기록 및 현장 임장 이력이 없습니다"))
    }

    @Test
    fun `ddakchal branding contains zero todak typos across state recorder analyzer and html`() {
        val cfg = ServerAutomationConfig(enableExternalLinkNaver = false)
        val slot = TestFixtures.sampleSlots(cfg, withCourtEvidence = false).first()
        val adminDefault = TodakAdminConsoleState()
        val archive = CourtEvidenceRecorder.recordFullArchive(auction = slot.auctionCase)
        val sharp = CourtSharpAnalyzer.analyze(
            auction = slot.auctionCase,
            photos = archive.photos,
            documents = archive.documents,
            registryGapGu = archive.registryGapGu,
            registryEulGu = archive.registryEulGu,
            visitRecords = archive.visitRecords
        )
        val html = HtmlExportEngine.renderStandaloneHtmlDocument(slot, slot.variants[ChannelPlatform.CLOUDFLARE]!!, cfg)

        val combined = buildString {
            appendLine(adminDefault.summaryMessage)
            appendLine(archive.rightsVerdictSummary)
            appendLine(sharp.executiveVerdict)
            sharp.checks.forEach { appendLine("${it.findingDetail} ${it.actionDirective}") }
            appendLine(html)
        }
        val forbiddenTypos = listOf("토닥", "토탁", "딸찰")
        forbiddenTypos.forEach { typo ->
            assertFalse("Found forbidden typo '$typo' in output", combined.contains(typo))
        }
        assertTrue(adminDefault.summaryMessage.contains("딱찰"))
        assertTrue(html.contains("딱찰"))
    }

    @Test
    fun `lookup parses court images alias and flat status survey summary into official docs and visit record`() = runTest(testDispatcher) {
        val flatCourtAssetJson = """
            {
              "ok": true,
              "data": {
                "id": "asset-2025-7777",
                "case_no": "2025타경7777",
                "court_name": "서울동부지방법원",
                "address": "서울특별시 송파구 잠실동 101-2",
                "property_type": "APT_OFFICETEL",
                "appraisal_krw": 850000000,
                "min_bid_krw": 680000000,
                "discount_rate_pct": 80,
                "base_right_date": "2021-03-01",
                "images": [
                  "https://r2.ddakchal.pages.dev/photos/2025-7777-a.webp"
                ],
                "status_survey_summary": "집행관 2차 방문 결과 임차인 1세대 전입 확인, 관리비 양호",
                "sale_spec_summary": "대항력 있는 선순위 임차인 없음, 매각으로 말소",
                "appraisal_summary": "인근 실거래 평균 8.6억원 반영 적정 평가"
              }
            }
        """.trimIndent()

        val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            val req = chain.request()
            if (req.url.encodedPath.contains("/api/v1/blog/assets/")) {
                jsonResponse(req, 200, flatCourtAssetJson)
            } else {
                jsonResponse(req, 404, """{"error":"not found"}""")
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
        vm.applyInlineServerAuthAndSave("https://ddakchal.pages.dev", "tok-flat", "", "", "")
        vm.lookupCaseNumberFromTodakAndAutoPreview("2025타경7777", preferredTabIndex = 2)

        val archive = vm.uiState.value.activeCourtArchive
        assertNotNull(archive)
        assertEquals(1, archive!!.photos.size)
        assertEquals("https://r2.ddakchal.pages.dev/photos/2025-7777-a.webp", archive.photos.first().imageUrl)
        assertTrue(archive.documents.any { it.docKind == CourtDocKind.STATUS_SURVEY })
        assertTrue(archive.documents.any { it.docKind == CourtDocKind.SALE_SPECIFICATION })
        assertTrue(archive.documents.any { it.docKind == CourtDocKind.APPRAISAL_REPORT })
        assertTrue(archive.visitRecords.isNotEmpty())
        assertTrue(archive.visitRecords.first().findingsNote.contains("임차인 1세대"))
    }

    @Test
    fun `computeDdakchalTopGraphMetrics returns honest zeros on empty state and exact metrics when populated`() {
        val emptyMetrics = computeDdakchalTopGraphMetrics(StudioUiState())
        assertFalse(emptyMetrics.serverConnected)
        assertEquals(0, emptyMetrics.dbItemCount)
        assertEquals(0, emptyMetrics.totalArticlesCount)
        assertEquals(0, emptyMetrics.publishedCount)
        assertEquals(0, emptyMetrics.successRatePct)
        assertEquals(0, emptyMetrics.totalVisitors)
        assertEquals(0, emptyMetrics.totalDbInquiries)
        assertTrue(emptyMetrics.visitorSeries.isEmpty())

        val sampleSlot = TestFixtures.sampleSlots(ServerAutomationConfig(), withCourtEvidence = true).first()
        val populatedState = StudioUiState(
            telemetry = ServerTelemetryState(
                ddakchalConnected = true,
                apiLatencyMs = 42L,
                triggerStatus = DdakchalTriggerStatusSnapshot(
                    pendingCount = 2,
                    processedCount = 11,
                    failedCount = 1,
                    snapshotsCount = 4
                )
            ),
            todakAdmin = TodakAdminConsoleState(
                d1TablesRaw = listOf("blog_assets", "blog_queue"),
                discoveredItems = listOf(
                    ServerDbDiscoveredItem(
                        recordId = "rec-1",
                        dedupKey = sampleSlot.auctionCase.dedupKey,
                        caseId = sampleSlot.auctionCase.caseId,
                        courtName = sampleSlot.auctionCase.courtName,
                        address = sampleSlot.auctionCase.address,
                        propertyType = sampleSlot.auctionCase.propertyType,
                        appraisalKrw = sampleSlot.auctionCase.appraisalPriceKrw,
                        minBidKrw = sampleSlot.auctionCase.minBidPriceKrw,
                        photoCount = 2,
                        visitCount = 1,
                        hasAnalysisData = true,
                        sourceOrigin = "D1",
                        updatedAt = "2026-11-27 12:30:00",
                        slot = sampleSlot
                    )
                ),
                lastUpdatedAt = "2026-11-27 12:30:00"
            ),
            slots = listOf(sampleSlot),
            queueAnalytics = QueueAnalyticsSummary(
                totalSlots = 10,
                publishedCount = 7,
                queuedCount = 2,
                failedOrHandoffCount = 1,
                draftCount = 0,
                successRatePct = 70,
                failureRatePct = 10,
                dailySeries = emptyList(),
                channelStats = listOf(
                    ChannelPublishStat(ChannelPlatform.NAVER, publishedCount = 3, queuedCount = 1, failedCount = 0),
                    ChannelPublishStat(ChannelPlatform.TISTORY, publishedCount = 2, queuedCount = 1, failedCount = 0),
                    ChannelPublishStat(ChannelPlatform.CLOUDFLARE, publishedCount = 2, queuedCount = 0, failedCount = 1)
                )
            ),
            visitorLogs = listOf(
                VisitorTrafficLog("11-27", 60, 30, 30, 120, 80, 4, 2, 1200L),
                VisitorTrafficLog("11-26", 40, 20, 20, 80, 70, 2, 1, 800L)
            )
        )

        val metrics = computeDdakchalTopGraphMetrics(populatedState)
        assertTrue(metrics.serverConnected)
        assertEquals(42L, metrics.latencyMs)
        assertEquals(1, metrics.dbItemCount)
        assertEquals(2, metrics.d1TableCount)
        assertEquals(11, metrics.triggerProcessedCount)
        assertEquals(10, metrics.totalArticlesCount)
        assertEquals(7, metrics.publishedCount)
        assertEquals(70, metrics.successRatePct)
        assertEquals(200, metrics.totalVisitors)
        assertEquals(6, metrics.totalDbInquiries)
        assertEquals(2, metrics.visitorSeries.size)
    }
}
