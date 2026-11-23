package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.data.PostWarehouseStore
import com.example.engine.Article30PointEvaluator
import com.example.engine.DefaultSpecDataFactory
import com.example.engine.RegistryInfographicEngine
import com.example.model.ChannelPlatform
import com.example.model.ProFunnelConfig
import com.example.model.RegistryInfographicTemplateType
import com.example.model.RegistryNodeTone
import com.example.model.ServerAutomationConfig
import com.example.model.WarehouseArticleStatus
import com.example.model.WritingActivityLog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class WarehouseAndRegistryTemplateTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createWarehouseStore(scope: TestScope): PostWarehouseStore {
        val file = tempFolder.newFile("test_warehouse_${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file }
        )
        return PostWarehouseStore(dataStore)
    }

    @Test
    fun registryInfographicEngine_buildsAll4TemplatesWithOwnershipGapGuAndEulGu() {
        val slots = DefaultSpecDataFactory.buildInitialDaily5Slots(ServerAutomationConfig())
        val seniorTenantCase = slots.first {
            it.auctionCase.assumedRightsText.contains("선순위") && it.auctionCase.assumedRightsText.contains("인수")
        }.auctionCase

        RegistryInfographicTemplateType.entries.forEach { tplType ->
            val tpl = RegistryInfographicEngine.buildTemplate(seniorTenantCase, tplType)
            assertEquals(seniorTenantCase.caseId, tpl.caseId)
            assertEquals(tplType, tpl.templateType)
            assertTrue(tpl.ownershipSummaryCard.currentOwnerLabel.isNotBlank())
            assertTrue(tpl.gapGuNodes.size >= 3)
            assertTrue(tpl.eulGuNodes.any { it.isBaseRight })
            assertTrue(tpl.eulGuNodes.any { it.nodeTone == RegistryNodeTone.ASSUMED_DANGER })
            assertTrue(tpl.readerChecklist.size >= 3)
            assertEquals("1200x800", tpl.renderAspectRatio)
        }
    }

    @Test
    fun article30PointEvaluator_enforcesExact30CriteriaAnd95PointRegistrationGate() {
        val cfg = ServerAutomationConfig()
        val funnel = ProFunnelConfig()
        val slots = DefaultSpecDataFactory.buildInitialDaily5Slots(cfg, funnel)

        // 1. Clean slots from engine must score >= 95 and pass all 30-item structural checks
        slots.forEach { slot ->
            val reg = RegistryInfographicEngine.buildTemplate(slot.auctionCase)
            val eval = Article30PointEvaluator.evaluate(slot, cfg, funnel, reg)
            assertEquals(30, eval.totalCriteriaCount)
            assertEquals(100, eval.maxScore)
            assertEquals(100, eval.items.sumOf { it.maxPoints })
            assertTrue(
                "Expected ${slot.auctionCase.caseId} >= 95 but got ${eval.totalScore}: ${eval.items.filter { !it.passed }.map { it.title }}",
                eval.totalScore >= Article30PointEvaluator.REGISTRATION_MIN_SCORE
            )
            assertTrue(eval.eligibleForRegistration)
        }

        // 2. Sabotaged post (AI pattern + external Naver URL + emoji + low char count) must drop < 95 and be blocked
        val sampleSlot = slots.first()
        val badNaver = sampleSlot.variants[ChannelPlatform.NAVER]!!.copy(
            title = "[추천] ${sampleSlot.auctionCase.caseId}",
            bodyText = "■ Q1. 오늘은 경매 물건에 대해 알아보겠습니다 🔥 https://evil-spam.example.com",
            pinnedCommentSnippet = ""
        )
        val sabotaged = sampleSlot.copy(
            variants = sampleSlot.variants + (ChannelPlatform.NAVER to badNaver)
        )
        val badEval = Article30PointEvaluator.evaluate(sabotaged, cfg, funnel)
        assertTrue("Sabotaged score should be < 95, got ${badEval.totalScore}", badEval.totalScore < 95)
        assertFalse(badEval.eligibleForRegistration)
    }

    @Test
    fun postWarehouseStore_persistsArticlesVisitorLogsAndEnforces95GateOnRegistration() =
        runTest(UnconfinedTestDispatcher()) {
            val store = createWarehouseStore(this)
            val initial = store.snapshotFlow.first()

            assertEquals(5, initial.articles.size)
            assertEquals(5, initial.writingLogs.size)
            assertEquals(7, initial.visitorLogs.size)
            assertTrue(initial.articles.all { it.evaluation.eligibleForRegistration })

            val target = initial.articles.last()
            assertEquals(WarehouseArticleStatus.STORED_READY, target.warehouseStatus)
            val viewsBefore = initial.visitorLogs.first().totalViews

            val regLog = WritingActivityLog(
                logId = "wlog-test-reg",
                warehouseId = target.warehouseId,
                caseId = target.slot.auctionCase.caseId,
                actionTitle = "95점+ 게이트 통과 등록 테스트",
                score = target.evaluation.totalScore,
                passedGate95 = true,
                detail = "30/30 통과",
                timestamp = "2026-11-22 12:15:00"
            )
            val afterReg = store.recordRegistration(
                warehouseId = target.warehouseId,
                channels = ChannelPlatform.entries,
                activityLog = regLog
            )
            val registeredItem = afterReg.articles.first { it.warehouseId == target.warehouseId }
            assertEquals(WarehouseArticleStatus.REGISTERED, registeredItem.warehouseStatus)
            assertNotNull(registeredItem.registeredAt)
            assertTrue(afterReg.visitorLogs.first().totalViews > viewsBefore)

            // Refresh visitor telemetry increases today's views
            val beforeRefresh = afterReg.visitorLogs.first().totalViews
            val afterRefresh = store.refreshVisitorTelemetry(100, 50, 75)
            assertEquals(beforeRefresh + 225, afterRefresh.visitorLogs.first().totalViews)

            // Delete and reset
            val afterDelete = store.deleteArticle(target.warehouseId)
            assertEquals(4, afterDelete.articles.size)
            val reset = store.resetToDefaults()
            assertEquals(5, reset.articles.size)
        }
}
