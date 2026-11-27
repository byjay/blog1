package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.NaverAutoWriteMode
import com.example.model.PropertyType
import com.example.model.ServerAutomationConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.studioDataStore: DataStore<Preferences> by preferencesDataStore(name = "ddakchal_studio_prefs")

class ServerConfigStore(private val dataStore: DataStore<Preferences>) {
    constructor(context: Context) : this(context.studioDataStore)

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val stringListAdapter = moshi.adapter<List<String>>(
        Types.newParameterizedType(List::class.java, String::class.java)
    )

    private object Keys {
        val DDAKCHAL_API_BASE = stringPreferencesKey("ddakchal_api_base")
        val CLOUDFLARE_SITE_BASE = stringPreferencesKey("cloudflare_site_base")
        val CRAWLER_SYNC_TOKEN = stringPreferencesKey("crawler_sync_token")
        val CF_ACCOUNT_ID = stringPreferencesKey("cf_account_id")
        val CF_API_TOKEN = stringPreferencesKey("cf_api_token")
        val CF_D1_DATABASE_ID = stringPreferencesKey("cf_d1_database_id")
        val CF_WORKER_SCRIPT_NAME = stringPreferencesKey("cf_worker_script_name")
        val CRON_EXPRESSION = stringPreferencesKey("cron_expression")
        val DAILY_POST_LIMIT = intPreferencesKey("daily_post_limit")
        val WEEKLY_TARGET_POSTS = intPreferencesKey("weekly_target_posts")
        val MIN_DELAY_MINUTES = intPreferencesKey("min_delay_minutes")
        val MAX_DELAY_MINUTES = intPreferencesKey("max_delay_minutes")
        val ENABLE_EXTERNAL_LINK_NAVER = booleanPreferencesKey("enable_external_link_naver")
        val PUBLISH_TARGET_NAVER = booleanPreferencesKey("publish_target_naver")
        val PUBLISH_TARGET_TISTORY = booleanPreferencesKey("publish_target_tistory")
        val PUBLISH_TARGET_CLOUDFLARE = booleanPreferencesKey("publish_target_cloudflare")
        val NAVER_BLOG_ID = stringPreferencesKey("naver_blog_id")
        val NAVER_CATEGORY_NAME = stringPreferencesKey("naver_category_name")
        val NAVER_WORKER_RELAY_ENDPOINT = stringPreferencesKey("naver_worker_relay_endpoint")
        val NAVER_ID = stringPreferencesKey("naver_id")
        val NAVER_PW = stringPreferencesKey("naver_pw")
        val TISTORY_BLOG_URL = stringPreferencesKey("tistory_blog_url")
        val TISTORY_CATEGORY_NAME = stringPreferencesKey("tistory_category_name")
        val TISTORY_ID = stringPreferencesKey("tistory_id")
        val TISTORY_PW = stringPreferencesKey("tistory_pw")
        val CLOUDFLARE_BLOG_PATH = stringPreferencesKey("cloudflare_blog_path")
        val LLM_API_ENDPOINT = stringPreferencesKey("llm_api_endpoint")
        val LLM_API_KEY = stringPreferencesKey("llm_api_key")
        val NAVER_SESSION_STATUS = stringPreferencesKey("naver_session_status")
        val TISTORY_SESSION_STATUS = stringPreferencesKey("tistory_session_status")
        val NAVER_KEEP_LOGGED_IN = booleanPreferencesKey("naver_keep_logged_in")
        val TISTORY_KEEP_LOGGED_IN = booleanPreferencesKey("tistory_keep_logged_in")
        val TISTORY_AUTO_DIRECT_PUBLISH = booleanPreferencesKey("tistory_auto_direct_publish")
        val NAVER_WRITE_MODE = stringPreferencesKey("naver_write_mode")
        val AI_PRIMARY_GEMINI_FIRST = booleanPreferencesKey("ai_primary_gemini_first")
        val GROK_FALLBACK_ENABLED = booleanPreferencesKey("grok_fallback_enabled")
        val GROK_MODEL_NAME = stringPreferencesKey("grok_model_name")
        val AUTO_PAUSE_ON_HIGH_RISK = booleanPreferencesKey("auto_pause_on_high_risk")
        val RECENT_7D_CORPUS_JSON = stringPreferencesKey("recent_7d_corpus_json")
        fun yamlKey(type: PropertyType) = stringPreferencesKey("yaml_${type.code}")
    }

    val configFlow: Flow<ServerAutomationConfig> = dataStore.data.map { prefs ->
        val rawWriteMode = prefs[Keys.NAVER_WRITE_MODE].orEmpty()
        val parsedWriteMode = NaverAutoWriteMode.entries.firstOrNull { it.name == rawWriteMode }
            ?: NaverAutoWriteMode.WORKER_RELAY
        ServerAutomationConfig(
            ddakchalApiBase = prefs[Keys.DDAKCHAL_API_BASE] ?: "https://ddakchal.pages.dev",
            cloudflareSiteBase = prefs[Keys.CLOUDFLARE_SITE_BASE]?.ifBlank { "https://aution.codeb.app" }
                ?: "https://aution.codeb.app",
            crawlerSyncToken = prefs[Keys.CRAWLER_SYNC_TOKEN] ?: "",
            cfAccountId = prefs[Keys.CF_ACCOUNT_ID] ?: "",
            cfApiToken = prefs[Keys.CF_API_TOKEN] ?: "",
            cfD1DatabaseId = prefs[Keys.CF_D1_DATABASE_ID] ?: "",
            cfWorkerScriptName = prefs[Keys.CF_WORKER_SCRIPT_NAME] ?: "ddakchal-blog-worker",
            cronExpression = prefs[Keys.CRON_EXPRESSION] ?: "17 8 * * *",
            dailyPostLimit = (prefs[Keys.DAILY_POST_LIMIT] ?: 2).coerceIn(1, 10),
            weeklyTargetPosts = (prefs[Keys.WEEKLY_TARGET_POSTS] ?: 25).coerceIn(10, 60),
            minDelayMinutes = prefs[Keys.MIN_DELAY_MINUTES] ?: 45,
            maxDelayMinutes = prefs[Keys.MAX_DELAY_MINUTES] ?: 120,
            enableExternalLinkNaver = prefs[Keys.ENABLE_EXTERNAL_LINK_NAVER] ?: false,
            publishTargetNaver = prefs[Keys.PUBLISH_TARGET_NAVER] ?: true,
            publishTargetTistory = prefs[Keys.PUBLISH_TARGET_TISTORY] ?: true,
            publishTargetCloudflare = prefs[Keys.PUBLISH_TARGET_CLOUDFLARE] ?: true,
            naverBlogId = prefs[Keys.NAVER_BLOG_ID] ?: "",
            naverCategoryName = prefs[Keys.NAVER_CATEGORY_NAME] ?: "부동산 경매 분석",
            naverWorkerRelayEndpoint = prefs[Keys.NAVER_WORKER_RELAY_ENDPOINT] ?: "",
            naverId = prefs[Keys.NAVER_ID] ?: "",
            naverPw = prefs[Keys.NAVER_PW] ?: "",
            tistoryBlogUrl = prefs[Keys.TISTORY_BLOG_URL] ?: "",
            tistoryCategoryName = prefs[Keys.TISTORY_CATEGORY_NAME] ?: "경매·권리·세무 리포트",
            tistoryId = prefs[Keys.TISTORY_ID] ?: "",
            tistoryPw = prefs[Keys.TISTORY_PW] ?: "",
            cloudflareBlogPath = prefs[Keys.CLOUDFLARE_BLOG_PATH] ?: "/blog",
            llmApiEndpoint = prefs[Keys.LLM_API_ENDPOINT] ?: "",
            llmApiKey = prefs[Keys.LLM_API_KEY] ?: "",
            naverSessionStatus = prefs[Keys.NAVER_SESSION_STATUS]?.ifBlank {
                "세션 미확인 (연결 점검 또는 워커 릴레이 필요)"
            } ?: "세션 미확인 (연결 점검 또는 워커 릴레이 필요)",
            tistorySessionStatus = prefs[Keys.TISTORY_SESSION_STATUS]?.ifBlank {
                "미연동 (릴레이·URL 연결 점검 대기)"
            } ?: "미연동 (릴레이·URL 연결 점검 대기)",
            naverKeepLoggedIn = prefs[Keys.NAVER_KEEP_LOGGED_IN] ?: true,
            tistoryKeepLoggedIn = prefs[Keys.TISTORY_KEEP_LOGGED_IN] ?: true,
            tistoryAutoDirectPublish = prefs[Keys.TISTORY_AUTO_DIRECT_PUBLISH] ?: true,
            naverWriteMode = parsedWriteMode,
            aiPrimaryGeminiFirst = prefs[Keys.AI_PRIMARY_GEMINI_FIRST] ?: true,
            grokFallbackEnabled = prefs[Keys.GROK_FALLBACK_ENABLED] ?: true,
            grokModelName = prefs[Keys.GROK_MODEL_NAME]?.ifBlank { "grok-3-mini" } ?: "grok-3-mini",
            autoPauseOnHighRisk = prefs[Keys.AUTO_PAUSE_ON_HIGH_RISK] ?: true
        )
    }

    suspend fun saveConfig(config: ServerAutomationConfig) {
        dataStore.edit { prefs ->
            prefs[Keys.DDAKCHAL_API_BASE] = config.ddakchalApiBase.trim().removeSuffix("/").ifBlank { "https://ddakchal.pages.dev" }
            prefs[Keys.CLOUDFLARE_SITE_BASE] = config.cloudflareSiteBase.trim().removeSuffix("/").ifBlank { "https://aution.codeb.app" }
            prefs[Keys.CRAWLER_SYNC_TOKEN] = config.crawlerSyncToken.trim()
            prefs[Keys.CF_ACCOUNT_ID] = config.cfAccountId.trim()
            prefs[Keys.CF_API_TOKEN] = config.cfApiToken.trim()
            prefs[Keys.CF_D1_DATABASE_ID] = config.cfD1DatabaseId.trim()
            prefs[Keys.CF_WORKER_SCRIPT_NAME] = config.cfWorkerScriptName.trim()
            prefs[Keys.CRON_EXPRESSION] = config.cronExpression.trim()
            prefs[Keys.DAILY_POST_LIMIT] = config.dailyPostLimit.coerceIn(1, 10)
            prefs[Keys.WEEKLY_TARGET_POSTS] = config.weeklyTargetPosts.coerceIn(10, 60)
            prefs[Keys.MIN_DELAY_MINUTES] = config.minDelayMinutes.coerceIn(15, 180)
            prefs[Keys.MAX_DELAY_MINUTES] = config.maxDelayMinutes.coerceAtLeast(config.minDelayMinutes + 15)
            prefs[Keys.ENABLE_EXTERNAL_LINK_NAVER] = config.enableExternalLinkNaver
            prefs[Keys.PUBLISH_TARGET_NAVER] = config.publishTargetNaver
            prefs[Keys.PUBLISH_TARGET_TISTORY] = config.publishTargetTistory
            prefs[Keys.PUBLISH_TARGET_CLOUDFLARE] = config.publishTargetCloudflare
            prefs[Keys.NAVER_BLOG_ID] = config.naverBlogId.trim()
            prefs[Keys.NAVER_CATEGORY_NAME] = config.naverCategoryName.trim().ifBlank { "부동산 경매 분석" }
            prefs[Keys.NAVER_WORKER_RELAY_ENDPOINT] = config.naverWorkerRelayEndpoint.trim()
            prefs[Keys.NAVER_ID] = config.naverId.trim()
            prefs[Keys.NAVER_PW] = config.naverPw
            prefs[Keys.TISTORY_BLOG_URL] = config.tistoryBlogUrl.trim().removeSuffix("/")
            prefs[Keys.TISTORY_CATEGORY_NAME] = config.tistoryCategoryName.trim().ifBlank { "경매·권리·세무 리포트" }
            prefs[Keys.TISTORY_ID] = config.tistoryId.trim()
            prefs[Keys.TISTORY_PW] = config.tistoryPw
            prefs[Keys.CLOUDFLARE_BLOG_PATH] = config.cloudflareBlogPath.trim().ifBlank { "/blog" }
            prefs[Keys.LLM_API_ENDPOINT] = config.llmApiEndpoint.trim()
            prefs[Keys.LLM_API_KEY] = config.llmApiKey.trim()
            prefs[Keys.NAVER_SESSION_STATUS] = config.naverSessionStatus
            prefs[Keys.TISTORY_SESSION_STATUS] = config.tistorySessionStatus
            prefs[Keys.NAVER_KEEP_LOGGED_IN] = config.naverKeepLoggedIn
            prefs[Keys.TISTORY_KEEP_LOGGED_IN] = config.tistoryKeepLoggedIn
            prefs[Keys.TISTORY_AUTO_DIRECT_PUBLISH] = config.tistoryAutoDirectPublish
            prefs[Keys.NAVER_WRITE_MODE] = config.naverWriteMode.name
            prefs[Keys.AI_PRIMARY_GEMINI_FIRST] = config.aiPrimaryGeminiFirst
            prefs[Keys.GROK_FALLBACK_ENABLED] = config.grokFallbackEnabled
            prefs[Keys.GROK_MODEL_NAME] = config.grokModelName.trim().ifBlank { "grok-3-mini" }
            prefs[Keys.AUTO_PAUSE_ON_HIGH_RISK] = config.autoPauseOnHighRisk
        }
    }

    suspend fun saveCustomYaml(type: PropertyType, yamlContent: String) {
        dataStore.edit { prefs ->
            prefs[Keys.yamlKey(type)] = yamlContent
        }
    }

    suspend fun getCustomYamlOverrides(): Map<PropertyType, String> {
        val prefs = dataStore.data.first()
        return PropertyType.entries.mapNotNull { type ->
            prefs[Keys.yamlKey(type)]?.let { type to it }
        }.toMap()
    }

    suspend fun appendRecent7DayCorpus(bodies: List<String>) {
        if (bodies.isEmpty()) return
        dataStore.edit { prefs ->
            val existing = runCatching {
                prefs[Keys.RECENT_7D_CORPUS_JSON]?.let { stringListAdapter.fromJson(it) } ?: emptyList()
            }.getOrDefault(emptyList())
            val merged = (bodies.map { it.take(2600) } + existing).distinct().take(21)
            prefs[Keys.RECENT_7D_CORPUS_JSON] = stringListAdapter.toJson(merged)
        }
    }

    suspend fun getRecent7DayCorpus(): List<String> {
        val prefs = dataStore.data.first()
        return runCatching {
            prefs[Keys.RECENT_7D_CORPUS_JSON]?.let { stringListAdapter.fromJson(it) } ?: emptyList()
        }.getOrDefault(emptyList())
    }

    companion object {
        fun maskSecret(raw: String): String {
            if (raw.isBlank()) return "미설정"
            return if (raw.length <= 4) "****" else "****${raw.takeLast(4)}"
        }
    }
}
