package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.ai.AiEvaluationOutcome
import com.example.ai.AiSeoGeoAdvisor
import com.example.data.BlogAutomationRepository
import com.example.data.PostWarehouseStore
import com.example.data.ServerConfigStore
import com.example.engine.CourtEvidenceRecorder
import com.example.engine.DefaultSpecDataFactory
import com.example.engine.HtmlExportEngine
import com.example.model.ChannelPlatform
import com.example.model.FieldVisitRecord
import com.example.model.GeneratedPostSlot
import com.example.model.ProFunnelConfig
import com.example.model.ServerAutomationConfig
import com.example.model.VisitRecordSource
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CourtEvidencePreviewAndPublishTest {

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
        val file = tempFolder.newFile("ev_cfg_${System.nanoTime()}.preferences_pb")
        return ServerConfigStore(PreferenceDataStoreFactory.create(scope = scope) { file })
    }

    private fun createWarehouseStore(scope: TestScope): PostWarehouseStore {
        val file = tempFolder.newFile("ev_wh_${System.nanoTime()}.preferences_pb")
        return PostWarehouseStore(PreferenceDataStoreFactory.create(scope = scope) { file })
    }

    @Test
    fun `CourtEvidenceRecorder binds real photo URLs and generates sanitized court and field visit records`() {
        val auction = CourtEvidenceRecorder.findProfitableBusanLandUnder50M().first()
        val photoUrls = listOf(
            "https://cdn.ddakchal.pages.dev/photos/cut1.jpg",
            "https://cdn.ddakchal.pages.dev/photos/cut2.jpg"
        )
        val customVisit = FieldVisitRecord(
            visitId = "custom-visit-1",
            visitRound = "직접 임장 1차",
            visitedAt = "2026-11-24",
            source = VisitRecordSource.FIELD_IMJANG,
            visitorLabel = "직접 현장 방문",
            occupancyStatus = "공실 확인",
            findingsNote = "임차인 홍길동 010-1234-5678 확인 및 도로 접면 양호"
        )

        val archive = CourtEvidenceRecorder.recordFullArchive(
            auction = auction,
            courtPhotoUrls = photoUrls,
            customVisitRecords = listOf(customVisit)
        )

        assertEquals(5, archive.photos.size)
        assertEquals("https://cdn.ddakchal.pages.dev/photos/cut1.jpg", archive.photos[0].imageUrl)
        assertEquals("https://cdn.ddakchal.pages.dev/photos/cut2.jpg", archive.photos[1].imageUrl)
        assertEquals("", archive.photos[2].imageUrl)

        // Default 4 court/survey/mgmt/imjang visits + 1 custom visit = 5 total
        assertEquals(5, archive.visitRecords.size)
        assertEquals("custom-visit-1", archive.visitRecords.first().visitId)
        assertFalse(archive.visitRecords.first().findingsNote.contains("010-1234-5678"))
        assertTrue(archive.registryGapGu.isNotEmpty())
        assertTrue(archive.registryEulGu.isNotEmpty())
    }

    @Test
    fun `HtmlExportEngine embeds court photos, registry tables, and visit logs while respecting Naver link policy`() {
        val config = ServerAutomationConfig(enableExternalLinkNaver = false)
        val slot = DefaultSpecDataFactory.buildInitialDaily5Slots(config).first().copy(
            courtPhotoUrls = listOf("https://r2.aution.codeb.app/case/photo1.webp"),
            visitRecords = listOf(
                FieldVisitRecord(
                    visitId = "v-html-1",
                    visitRound = "임장 실사",
                    visitedAt = "2026-11-25",
                    source = VisitRecordSource.FIELD_IMJANG,
                    visitorLabel = "현장팀",
                    occupancyStatus = "현장 진입 양호",
                    findingsNote = "관리비 체납 내역 없음 확인 완료"
                )
            )
        )

        val cfVariant = slot.variants[ChannelPlatform.CLOUDFLARE]!!
        val cfHtml = HtmlExportEngine.renderStandaloneHtmlDocument(slot, cfVariant, config)
        assertTrue(cfHtml.contains("class=\"court-photos\""))
        assertTrue(cfHtml.contains("class=\"registry-table\""))
        assertTrue(cfHtml.contains("class=\"visit-logs\""))
        assertTrue(cfHtml.contains("https://r2.aution.codeb.app/case/photo1.webp"))
        assertTrue(cfHtml.contains("관리비 체납 내역 없음 확인 완료"))
        assertTrue(cfHtml.contains("갑구"))
        assertTrue(cfHtml.contains("을구"))

        val nvVariant = slot.variants[ChannelPlatform.NAVER]!!
        val nvHtml = HtmlExportEngine.renderStandaloneHtmlDocument(slot, nvVariant, config)
        assertTrue(nvHtml.contains("class=\"court-photos\""))
        assertTrue(nvHtml.contains("class=\"registry-table\""))
        assertTrue(nvHtml.contains("class=\"visit-logs\""))
        // Naver external URL block enforced when enableExternalLinkNaver = false
        assertFalse(nvHtml.contains("https://r2.aution.codeb.app"))

        val previewHtml = HtmlExportEngine.renderWebViewPreviewHtml(
            slot = slot,
            variant = nvVariant,
            config = config,
            localPhotoDataUris = mapOf(2 to "data:image/jpeg;base64,QUJD")
        )
        assertTrue(previewHtml.contains("품질"))
        assertTrue(previewHtml.contains("data:image/jpeg;base64,QUJD"))
        assertTrue(previewHtml.contains("등기부등본 갑구 · 을구"))
        assertTrue(previewHtml.contains("법원 현황조사 방문기록"))
    }

    @Test
    fun `StudioViewModel attachCourtPhotoToSlot and addFieldVisitRecord update slot and activeCourtArchive cleanly`() = runTest {
        val repo = BlogAutomationRepository(
            configStore = createConfigStore(this),
            postWarehouseStore = createWarehouseStore(this)
        )
        val viewModel = StudioViewModel(
            repository = repo,
            aiAdvisor = offlineAdvisor
        )

        val firstSlot = viewModel.uiState.value.slots.first()
        viewModel.selectSlot(firstSlot.postId)

        viewModel.attachCourtPhotoToSlot(
            postId = firstSlot.postId,
            shotOrder = 2,
            photoUriString = "content://media/picker/0/com.android.providers.media.photopicker/media/1001"
        )
        viewModel.addFieldVisitRecord(
            postId = firstSlot.postId,
            source = VisitRecordSource.MANAGEMENT_OFFICE,
            visitedAt = "2026-11-25",
            occupancyStatus = "공용관리비 체납 없음",
            findingsNote = "관리소장 010-9999-8888 문의 결과 장기수선충당금 정상"
        )

        val updatedSlot = viewModel.uiState.value.slots.first { it.postId == firstSlot.postId }
        val archive = viewModel.uiState.value.activeCourtArchive

        assertNotNull(archive)
        assertEquals(
            "content://media/picker/0/com.android.providers.media.photopicker/media/1001",
            updatedSlot.courtPhotoUrls[1]
        )
        assertEquals(
            "content://media/picker/0/com.android.providers.media.photopicker/media/1001",
            archive!!.photos[1].imageUrl
        )
        assertEquals(1, updatedSlot.visitRecords.size)
        assertTrue(archive.visitRecords.any { it.findingsNote.contains("장기수선충당금 정상") })
        // Verify PII sanitization
        assertFalse(archive.visitRecords.first().findingsNote.contains("010-9999-8888"))
    }
}
