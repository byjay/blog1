package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.data.BlogPostTemplateStore
import com.example.engine.BlogPostTemplateEngine
import com.example.model.ChannelPlatform
import com.example.model.PropertyType
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
class BlogPostTemplateStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createTestStore(scope: TestScope): BlogPostTemplateStore {
        val file = tempFolder.newFile("test_templates_${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file }
        )
        return BlogPostTemplateStore(dataStore)
    }

    @Test
    fun dataStore_seeds5Presets_andSupportsCreateEditActivateDeleteReset() = runTest(UnconfinedTestDispatcher()) {
        val store = createTestStore(this)

        // 1. Initial seed contains 5 property-type starter templates
        val initial = store.templatesFlow.first()
        assertEquals(5, initial.size)
        assertEquals(PropertyType.entries.toSet(), initial.map { it.propertyType }.toSet())

        // 2. Create a new custom template for APT_OFFICETEL + NAVER and verify active switching
        val created = store.createTemplate(
            name = "강남권 아파트 실거주 커스텀",
            propertyType = PropertyType.APT_OFFICETEL,
            targetChannel = ChannelPlatform.NAVER,
            titleTemplate = "{지역} 아파트 {사건번호} 최저가 {최저가}",
            bodyTemplate = "{법원} {사건번호} 감정가 {감정가} 최저가 {최저가} 말소기준권리 {말소기준권리} 본문 테스트",
            tagsTemplate = "딱찰,{지역},아파트경매",
            activateForGeneration = true
        )

        val afterCreate = store.templatesFlow.first()
        assertEquals(6, afterCreate.size)
        val savedCustom = afterCreate.first { it.id == created.id }
        assertTrue(savedCustom.isActiveForGeneration)
        // Previous APT + NAVER preset should be deactivated automatically
        val oldAptPreset = afterCreate.first { it.id == "preset-apt-naver" }
        assertFalse(oldAptPreset.isActiveForGeneration)

        // 3. Edit and save the custom template
        val edited = savedCustom.copy(
            name = "강남권 아파트 수정본",
            titleTemplate = "[수정] {지역} {사건번호} {최저가}"
        )
        store.saveTemplate(edited)
        val afterEdit = store.getTemplatesSnapshot().first { it.id == created.id }
        assertEquals("강남권 아파트 수정본", afterEdit.name)
        assertEquals("[수정] {지역} {사건번호} {최저가}", afterEdit.titleTemplate)

        // 4. Reactivate preset & delete custom template
        store.setActiveTemplate("preset-apt-naver")
        assertTrue(store.getTemplatesSnapshot().first { it.id == "preset-apt-naver" }.isActiveForGeneration)
        assertFalse(store.getTemplatesSnapshot().first { it.id == created.id }.isActiveForGeneration)

        store.deleteTemplate(created.id)
        val afterDelete = store.getTemplatesSnapshot()
        assertEquals(5, afterDelete.size)
        assertTrue(afterDelete.none { it.id == created.id })

        // 5. Reset restores defaults cleanly
        store.deleteTemplate("preset-apt-naver")
        assertEquals(4, store.getTemplatesSnapshot().size)
        store.resetToDefaults()
        assertEquals(5, store.getTemplatesSnapshot().size)
    }

    @Test
    fun templateEngine_rendersPlaceholders_andEnforcesNaverExternalUrlBlock() {
        val config = ServerAutomationConfig(enableExternalLinkNaver = false)
        val slot = TestFixtures.sampleSlots(config).first()
        val auction = slot.auctionCase
        val baseNaver = slot.variants[ChannelPlatform.NAVER]!!

        val customTpl = BlogPostTemplateEngine.defaultBlogPostTemplates()
            .first { it.propertyType == PropertyType.APT_OFFICETEL }
            .copy(
                bodyTemplate = """
                    사건 {사건번호} ({주소}) 감정가 {감정가}, 최저가 {최저가}, 말소기준권리 {말소기준권리}.
                    자세한 원문은 https://evil-external-site.example.com 참고하세요.
                """.trimIndent()
            )

        val applied = BlogPostTemplateEngine.applyTemplateToVariant(baseNaver, customTpl, auction, config)
        assertTrue(applied.title.contains(auction.caseId))
        assertTrue(applied.bodyText.contains(auction.caseId))
        assertTrue(applied.bodyText.contains(auction.baseRightDate))
        assertFalse("External URL must be stripped for Naver", applied.bodyText.contains("https://"))
    }
}
