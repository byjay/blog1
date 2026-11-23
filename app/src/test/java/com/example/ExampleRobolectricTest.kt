package com.example

import com.example.engine.BoilerplateAndTemplateEngine
import com.example.model.ChannelPlatform
import com.example.model.PropertyType
import com.example.model.ServerAutomationConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun yamlTemplates_coverAll5PropertyTypes() {
        val templates = BoilerplateAndTemplateEngine.defaultYamlTemplates()
        assertEquals(5, templates.size)
        val types = templates.map { it.propertyType }.toSet()
        assertEquals(PropertyType.entries.toSet(), types)
        templates.forEach { tpl ->
            assertTrue("Missing case number token in ${tpl.propertyType}", tpl.yamlContent.contains("사건번호"))
            assertTrue("Missing anti_low_quality in ${tpl.propertyType}", tpl.yamlContent.contains("anti_low_quality"))
            assertTrue("Missing 3-channel rules in ${tpl.propertyType}", tpl.yamlContent.contains("naver") && tpl.yamlContent.contains("tistory") && tpl.yamlContent.contains("cloudflare"))
        }
    }

    @Test
    fun cloudflareBundleGenerators_includeD1TablesCronAndMasking() {
        val config = ServerAutomationConfig(
            cfAccountId = "acc-12345678",
            cfD1DatabaseId = "db-87654321",
            cfApiToken = "cf_secret_token_9999",
            crawlerSyncToken = "sync_secret_abcd",
            cronExpression = "17 8 * * *"
        )
        val sql = BoilerplateAndTemplateEngine.generateD1MigrationSql()
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS generated_posts"))
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS publish_logs"))

        val wrangler = BoilerplateAndTemplateEngine.generateWranglerToml(config)
        assertTrue(wrangler.contains("17 8 * * *"))
        assertTrue(wrangler.contains("db-87654321"))

        val maskedEnv = BoilerplateAndTemplateEngine.generateEnvBlogMasked(config, masked = true)
        assertFalse("Secret token must be masked in default .env view", maskedEnv.contains("cf_secret_token_9999"))
        assertFalse("Sync token must be masked in default .env view", maskedEnv.contains("sync_secret_abcd"))

        val workerTs = BoilerplateAndTemplateEngine.generateWorkerScheduledTs(config)
        assertTrue(workerTs.contains("scheduled("))
        assertTrue(workerTs.contains("/api/v1/blog/generate-daily-5"))
    }

    @Test
    fun channelPlatforms_followStaggeredOrder() {
        assertEquals(
            listOf(ChannelPlatform.CLOUDFLARE, ChannelPlatform.TISTORY, ChannelPlatform.NAVER),
            ChannelPlatform.entries
        )
        assertTrue(ChannelPlatform.CLOUDFLARE.staggerWindowLabel.contains("1순위"))
        assertTrue(ChannelPlatform.TISTORY.staggerWindowLabel.contains("2순위"))
        assertTrue(ChannelPlatform.NAVER.staggerWindowLabel.contains("3순위"))
    }
}
