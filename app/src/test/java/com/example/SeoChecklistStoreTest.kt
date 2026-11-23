package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.data.SeoChecklistStore
import com.example.engine.DefaultSpecDataFactory
import com.example.engine.SeoGuideEngine
import com.example.model.SeoRuleCategory
import com.example.model.SeoRulePriority
import com.example.model.ServerAutomationConfig
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
class SeoChecklistStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createTestStore(scope: TestScope): SeoChecklistStore {
        val file = tempFolder.newFile("test_seo_checklist_${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file }
        )
        return SeoChecklistStore(dataStore)
    }

    @Test
    fun dataStore_seeds12SeoChecklistItems_andSupportsToggleCreateEditDeleteReset() =
        runTest(UnconfinedTestDispatcher()) {
            val store = createTestStore(this)

            // 1. Verify initial seed has 12 items covering NAVER, TISTORY, GOOGLE_GEO, ANTI_BOT
            val initial = store.checklistFlow.first()
            assertEquals(12, initial.size)
            assertEquals(
                setOf(
                    SeoRuleCategory.NAVER,
                    SeoRuleCategory.TISTORY,
                    SeoRuleCategory.GOOGLE_GEO,
                    SeoRuleCategory.ANTI_BOT
                ),
                initial.map { it.category }.toSet()
            )

            // 2. Toggle an existing item and verify persistence
            val targetId = "chk-nv-no-ext-link"
            assertTrue(initial.first { it.id == targetId }.isChecked)
            store.toggleItem(targetId)
            assertFalse(store.getSnapshot().first { it.id == targetId }.isChecked)
            store.toggleItem(targetId)
            assertTrue(store.getSnapshot().first { it.id == targetId }.isChecked)

            // 3. Create a custom SEO rule
            val created = store.createItem(
                title = "구글 Core Web Vitals 이미지 WebP 압축",
                description = "자체웹 블로그 이미지 용량 150KB 이하 유지 및 alt 태그 필수 부여",
                category = SeoRuleCategory.GOOGLE_GEO,
                priority = SeoRulePriority.RECOMMENDED
            )
            val afterCreate = store.checklistFlow.first()
            assertEquals(13, afterCreate.size)
            val savedCustom = afterCreate.first { it.id == created.id }
            assertTrue(savedCustom.isCustom)
            assertEquals("CUSTOM", savedCustom.ruleCode)
            assertFalse(savedCustom.isChecked)

            // 4. Edit and save the custom rule
            store.saveItem(
                savedCustom.copy(
                    title = "구글 Core Web Vitals & 모바일 LCP 최적화",
                    priority = SeoRulePriority.CRITICAL,
                    isChecked = true
                )
            )
            val afterEdit = store.getSnapshot().first { it.id == created.id }
            assertEquals("구글 Core Web Vitals & 모바일 LCP 최적화", afterEdit.title)
            assertEquals(SeoRulePriority.CRITICAL, afterEdit.priority)
            assertTrue(afterEdit.isChecked)

            // 5. Delete custom item and reset to defaults
            store.deleteItem(created.id)
            assertEquals(12, store.getSnapshot().size)
            assertTrue(store.getSnapshot().none { it.id == created.id })

            store.deleteItem(targetId)
            assertEquals(11, store.getSnapshot().size)

            val restored = store.resetToDefaults()
            assertEquals(12, restored.size)
            assertEquals(12, store.getSnapshot().size)
        }

    @Test
    fun seoGuideEngine_provides4PolicyGuides_andEvaluatesLiveConfigAndQueueNotes() {
        val guides = SeoGuideEngine.policyGuides()
        assertEquals(4, guides.size)
        assertTrue(guides.all { it.keyPolicies.isNotEmpty() && it.penaltyTriggers.isNotEmpty() })

        val safeConfig = ServerAutomationConfig(
            enableExternalLinkNaver = false,
            cronExpression = "17 0 * * *",
            dailyPostLimit = 5
        )
        val riskyConfig = ServerAutomationConfig(
            enableExternalLinkNaver = true,
            cronExpression = "0 * * * *",
            dailyPostLimit = 5
        )
        val slots = DefaultSpecDataFactory.buildInitialDaily5Slots(safeConfig)
        val defaults = SeoGuideEngine.defaultChecklistItems()

        val naverLinkItem = defaults.first { it.ruleCode == "NAVER_NO_DIRECT_URL" }
        val safeNaverNote = SeoGuideEngine.evaluateLiveAutoNote(naverLinkItem, safeConfig, slots)
        val riskyNaverNote = SeoGuideEngine.evaluateLiveAutoNote(naverLinkItem, riskyConfig, slots)
        assertNotNull(safeNaverNote)
        assertTrue(safeNaverNote!!.contains("안전"))
        assertTrue(riskyNaverNote!!.contains("주의"))

        val cronItem = defaults.first { it.ruleCode == "NO_EXACT_HOUR_JITTER" }
        val safeCronNote = SeoGuideEngine.evaluateLiveAutoNote(cronItem, safeConfig, slots)
        val riskyCronNote = SeoGuideEngine.evaluateLiveAutoNote(cronItem, riskyConfig, slots)
        assertTrue(safeCronNote!!.contains("정각 회피"))
        assertTrue(riskyCronNote!!.contains("경고"))

        val simItem = defaults.first { it.ruleCode == "SIMILARITY_UNDER_30" }
        val simNote = SeoGuideEngine.evaluateLiveAutoNote(simItem, safeConfig, slots)
        assertNotNull(simNote)
        assertTrue(simNote!!.contains("통과"))
    }
}
