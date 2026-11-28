package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.data.ProFunnelStore
import com.example.engine.ProAnalysisAndPhotoGuideEngine
import com.example.engine.RegistryAndMotionEngine
import com.example.model.ChannelPlatform
import com.example.model.ProFunnelConfig
import com.example.model.RegistrySection
import com.example.model.ServerAutomationConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class RegistryMotionAndMarketingTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createFunnelStore(scope: TestScope): ProFunnelStore {
        val file = tempFolder.newFile("test_pro_funnel_${System.nanoTime()}.preferences_pb")
        val ds = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file }
        )
        return ProFunnelStore(ds)
    }

    @Test
    fun naverGeneratedProse_usesHumanVeteranStyleWithoutAiCliches_andPassesAllQa() {
        val config = ServerAutomationConfig(enableExternalLinkNaver = false)
        val slots = TestFixtures.sampleSlots(config)
        assertEquals(5, slots.size)

        slots.forEach { slot ->
            val naver = slot.variants[ChannelPlatform.NAVER]!!
            assertFalse("Naver body must not contain ■", naver.bodyText.contains("■"))
            assertFalse("Naver body must not contain Q1. pattern", naver.bodyText.contains("Q1."))
            assertFalse("Naver body must not contain Q2. pattern", naver.bodyText.contains("Q2."))
            assertFalse("Naver body must not contain Q3. pattern", naver.bodyText.contains("Q3."))
            assertFalse("Naver body must not contain Q4. pattern", naver.bodyText.contains("Q4."))
            assertTrue("Naver body must contain registry anatomy section", naver.bodyText.contains("[등기부 갑구·을구]"))
            assertTrue("Naver body must contain 4-shot field photo section", naver.bodyText.contains("[현장 4컷 실사]"))
            assertTrue("Naver title must lead with caseId", naver.title.contains(slot.auctionCase.caseId))
            assertTrue("Naver pinned comment must be generated", naver.pinnedCommentSnippet.contains(slot.auctionCase.caseId))

            val qa = slot.qaReport
            assertTrue("QA overall must pass for ${slot.auctionCase.caseId}", qa.overallPassed)
            assertTrue("Char counts must be 1200..2500 (was ${qa.charCountByChannel})", qa.charCountOkByChannel.values.all { it })
            assertTrue("Similarity must be <30% (was ${qa.maxCrossChannelCosinePct}%)", qa.similarityPassed)
        }
    }

    @Test
    fun registryAndMotionEngine_buildsRegistryTimeline5SceneMotionAndRevenueForecast() {
        val slots = TestFixtures.sampleSlots(ServerAutomationConfig())
        val villaSlot = slots[1] // 2025타경11842 with senior tenant risk
        val villaFacts = RegistryAndMotionEngine.buildRegistryFacts(villaSlot.auctionCase)

        assertTrue(villaFacts.any { it.section == RegistrySection.GAP_GU })
        assertTrue(villaFacts.any { it.section == RegistrySection.EUL_GU && it.isBaseRight })
        assertTrue(villaFacts.any { it.section == RegistrySection.TENANT_COURT && it.isAssumedRisk })

        val proReport = ProAnalysisAndPhotoGuideEngine.buildProReport(villaSlot.auctionCase)
        val scenes = RegistryAndMotionEngine.build5SceneMotionGuide(
            auction = villaSlot.auctionCase,
            report = proReport,
            funnel = ProFunnelConfig()
        )
        assertEquals(5, scenes.size)
        assertEquals(65, scenes.sumOf { it.durationSec })
        assertTrue(scenes.first().onScreenCaption.contains(villaSlot.auctionCase.caseId))
        assertTrue(scenes.last().narrationScript.contains("딱찰 경매"))

        val insights = RegistryAndMotionEngine.naverTopBlogResearchInsights()
        assertEquals(4, insights.size)

        val forecast = RegistryAndMotionEngine.simulateMarketingRevenue(
            dailyVisitors = 2000,
            cpmKrw = 2500,
            dbConversionPct = 0.5,
            proConversionPct = 1.0
        )
        // monthlyVisitors = 60,000, impressions = 96,000 -> AdPost = 240,000 KRW
        assertEquals(240_000L, forecast.monthlyAdpostKrw)
        // 60,000 * 0.5% = 300 DB leads -> 54,000,000 KRW
        assertEquals(300, forecast.monthlyDbLeads)
        assertEquals(54_000_000L, forecast.monthlyDbRevenueKrw)
        // 60,000 * 1.0% = 600 PRO conversions -> 17,400,000 KRW
        assertEquals(600, forecast.monthlyProConversions)
        assertEquals(17_400_000L, forecast.monthlyProRevenueKrw)
        assertEquals(71_640_000L, forecast.totalMonthlyRevenueKrw)
    }

    @Test
    fun proFunnelStore_savesAndResetsConfigInDataStore() = runTest(UnconfinedTestDispatcher()) {
        val store = createFunnelStore(this)
        val initial = store.funnelFlow.first()
        assertTrue(initial.funnelEnabled)
        assertEquals("https://ddakchal.pages.dev/pro", initial.proLandingUrl)

        val updated = ProFunnelConfig(
            funnelEnabled = false,
            proLandingUrl = "https://ddakchal.pages.dev/vip",
            naverSearchKeyword = "딱찰 등기분석",
            teaserHeadline = "30년 전담팀 테스트 헤드라인"
        )
        store.saveConfig(updated)
        val snapshot = store.getSnapshot()
        assertFalse(snapshot.funnelEnabled)
        assertEquals("https://ddakchal.pages.dev/vip", snapshot.proLandingUrl)
        assertEquals("딱찰 등기분석", snapshot.naverSearchKeyword)

        val reset = store.resetToDefaults()
        assertTrue(reset.funnelEnabled)
        assertEquals("https://ddakchal.pages.dev/pro", store.getSnapshot().proLandingUrl)
    }
}
