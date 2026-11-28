package com.example.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class HttpCallResult(
    val isSuccess: Boolean,
    val statusCode: Int,
    val body: String,
    val errorMessage: String? = null,
    val latencyMs: Long = 0L
)

class CloudflareAndDdakchalApi(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    internal val okHttpClient: OkHttpClient get() = client
    internal val coroutineDispatcher: CoroutineDispatcher get() = ioDispatcher

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun Request.Builder.applyDdakchalAuth(syncToken: String): Request.Builder {
        val trimmed = syncToken.trim()
        if (trimmed.isNotBlank()) {
            addHeader("Authorization", "Bearer $trimmed")
            addHeader("X-Crawler-Sync-Token", trimmed)
        }
        return this
    }

    suspend fun triggerGenerateDaily5(
        apiBase: String,
        syncToken: String,
        limit: Int,
        minDelayMinutes: Int,
        maxDelayMinutes: Int,
        enableExternalLinkNaver: Boolean
    ): HttpCallResult = withContext(ioDispatcher) {
        val url = "${apiBase.trimEnd('/')}/api/v1/blog/generate-daily-5"
        val payload = JSONObject().apply {
            put("limit", limit)
            put("minDelayMinutes", minDelayMinutes)
            put("maxDelayMinutes", maxDelayMinutes)
            put("enableExternalLinkNaver", enableExternalLinkNaver)
        }.toString()

        val reqBuilder = Request.Builder()
            .url(url)
            .post(payload.toRequestBody(jsonMediaType))
            .addHeader("Accept", "application/json")
            .applyDdakchalAuth(syncToken)
        execute(reqBuilder.build())
    }

    suspend fun fetchSyndicationPack(
        apiBase: String,
        syncToken: String,
        limit: Int = 5,
        date: String? = null,
        enableExternalLinkNaver: Boolean = false
    ): HttpCallResult = withContext(ioDispatcher) {
        val baseUrl = "${apiBase.trimEnd('/')}/api/v1/blog/syndication-pack"
        val httpUrl = baseUrl.toHttpUrlOrNull()?.newBuilder()?.apply {
            addQueryParameter("limit", limit.coerceIn(1, 20).toString())
            addQueryParameter("enableExternalLinkNaver", enableExternalLinkNaver.toString())
            if (!date.isNullOrBlank()) {
                addQueryParameter("date", date.trim())
            }
        }?.build()?.toString() ?: "$baseUrl?limit=$limit&enableExternalLinkNaver=$enableExternalLinkNaver"

        val reqBuilder = Request.Builder()
            .url(httpUrl)
            .get()
            .addHeader("Accept", "application/json")
            .applyDdakchalAuth(syncToken)
        execute(reqBuilder.build())
    }

    suspend fun fetchAnalysisFeed(
        apiBase: String,
        syncToken: String,
        limit: Int = 10,
        since: String? = null,
        category: String? = null,
        enableExternalLinkNaver: Boolean = false
    ): HttpCallResult = withContext(ioDispatcher) {
        val baseUrl = "${apiBase.trimEnd('/')}/api/v1/blog/analysis-feed"
        val httpUrl = baseUrl.toHttpUrlOrNull()?.newBuilder()?.apply {
            addQueryParameter("limit", limit.coerceIn(1, 30).toString())
            addQueryParameter("enableExternalLinkNaver", enableExternalLinkNaver.toString())
            if (!since.isNullOrBlank()) {
                addQueryParameter("since", since.trim())
            }
            if (!category.isNullOrBlank()) {
                addQueryParameter("category", category.trim())
            }
        }?.build()?.toString() ?: "$baseUrl?limit=$limit&enableExternalLinkNaver=$enableExternalLinkNaver"

        val reqBuilder = Request.Builder()
            .url(httpUrl)
            .get()
            .addHeader("Accept", "application/json")
            .applyDdakchalAuth(syncToken)
        execute(reqBuilder.build())
    }

    suspend fun fetchAssetByKey(
        apiBase: String,
        syncToken: String,
        keyOrCaseNo: String,
        refresh: Boolean = false,
        enableExternalLinkNaver: Boolean = false
    ): HttpCallResult = withContext(ioDispatcher) {
        val encodedKey = URLEncoder.encode(keyOrCaseNo.trim(), "UTF-8").replace("+", "%20")
        val baseUrl = "${apiBase.trimEnd('/')}/api/v1/blog/assets/$encodedKey"
        val httpUrl = baseUrl.toHttpUrlOrNull()?.newBuilder()?.apply {
            addQueryParameter("enableExternalLinkNaver", enableExternalLinkNaver.toString())
            if (refresh) {
                addQueryParameter("refresh", "true")
            }
        }?.build()?.toString() ?: "$baseUrl?enableExternalLinkNaver=$enableExternalLinkNaver"

        val reqBuilder = Request.Builder()
            .url(httpUrl)
            .get()
            .addHeader("Accept", "application/json")
            .applyDdakchalAuth(syncToken)
        execute(reqBuilder.build())
    }

    suspend fun fetchAssetsFeed(
        apiBase: String,
        syncToken: String,
        since: String? = null,
        category: String? = null,
        limit: Int = 20,
        flush: Boolean = true,
        query: String? = null
    ): HttpCallResult = withContext(ioDispatcher) {
        val baseUrl = "${apiBase.trimEnd('/')}/api/v1/blog/assets/feed"
        val httpUrl = baseUrl.toHttpUrlOrNull()?.newBuilder()?.apply {
            addQueryParameter("limit", limit.coerceIn(1, 50).toString())
            addQueryParameter("flush", flush.toString())
            if (!since.isNullOrBlank()) addQueryParameter("since", since.trim())
            if (!category.isNullOrBlank()) addQueryParameter("category", category.trim())
            if (!query.isNullOrBlank()) addQueryParameter("q", query.trim())
        }?.build()?.toString() ?: "$baseUrl?limit=$limit&flush=$flush"

        val reqBuilder = Request.Builder()
            .url(httpUrl)
            .get()
            .addHeader("Accept", "application/json")
            .applyDdakchalAuth(syncToken)
        execute(reqBuilder.build())
    }

    suspend fun fetchTriggerStatus(
        apiBase: String,
        syncToken: String
    ): HttpCallResult = withContext(ioDispatcher) {
        val url = "${apiBase.trimEnd('/')}/api/v1/blog/triggers/status"
        val reqBuilder = Request.Builder()
            .url(url)
            .get()
            .addHeader("Accept", "application/json")
            .applyDdakchalAuth(syncToken)
        execute(reqBuilder.build())
    }

    suspend fun runTriggers(
        apiBase: String,
        syncToken: String,
        maxBatch: Int = 15,
        autoFillDailyQueue: Boolean = true,
        enableExternalLinkNaver: Boolean = false
    ): HttpCallResult = withContext(ioDispatcher) {
        val url = "${apiBase.trimEnd('/')}/api/v1/blog/triggers/run"
        val payload = JSONObject().apply {
            put("maxBatch", maxBatch.coerceIn(1, 50))
            put("autoFillDailyQueue", autoFillDailyQueue)
            put("enableExternalLinkNaver", enableExternalLinkNaver)
        }.toString()

        val reqBuilder = Request.Builder()
            .url(url)
            .post(payload.toRequestBody(jsonMediaType))
            .addHeader("Accept", "application/json")
            .applyDdakchalAuth(syncToken)
        execute(reqBuilder.build())
    }

    suspend fun triggerCrawlAndSync(
        apiBase: String,
        syncToken: String,
        regions: List<String> = emptyList(),
        runCrawl: Boolean = true,
        maxBatch: Int = 15,
        limit: Int = 5,
        enableExternalLinkNaver: Boolean = false
    ): HttpCallResult = withContext(ioDispatcher) {
        val url = "${apiBase.trimEnd('/')}/api/v1/blog/crawl-and-sync"
        val payload = JSONObject().apply {
            if (regions.isNotEmpty()) {
                put("regions", JSONArray(regions))
            }
            put("runCrawl", runCrawl)
            put("maxBatch", maxBatch.coerceIn(1, 50))
            put("limit", limit.coerceIn(1, 20))
            put("enableExternalLinkNaver", enableExternalLinkNaver)
        }.toString()

        val reqBuilder = Request.Builder()
            .url(url)
            .post(payload.toRequestBody(jsonMediaType))
            .addHeader("Accept", "application/json")
            .applyDdakchalAuth(syncToken)
        execute(reqBuilder.build())
    }

    suspend fun updateQueueItemStatus(
        apiBase: String,
        syncToken: String,
        itemId: String,
        status: String,
        blogId: String? = null,
        blogPostUrl: String? = null,
        lastError: String? = null
    ): HttpCallResult = withContext(ioDispatcher) {
        val encodedId = URLEncoder.encode(itemId.trim(), "UTF-8").replace("+", "%20")
        val url = "${apiBase.trimEnd('/')}/api/v1/blog/queue/$encodedId"
        val payload = JSONObject().apply {
            put("status", status)
            if (!blogId.isNullOrBlank()) put("blogId", blogId)
            if (!blogPostUrl.isNullOrBlank()) put("blogPostUrl", blogPostUrl)
            if (!lastError.isNullOrBlank()) put("lastError", lastError)
        }.toString()

        val reqBuilder = Request.Builder()
            .url(url)
            .put(payload.toRequestBody(jsonMediaType))
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .applyDdakchalAuth(syncToken)
        execute(reqBuilder.build())
    }

    suspend fun listCloudflareD1Databases(
        accountId: String,
        apiToken: String
    ): HttpCallResult = withContext(ioDispatcher) {
        val url = "https://api.cloudflare.com/client/v4/accounts/$accountId/d1/database"
        val req = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $apiToken")
            .addHeader("Accept", "application/json")
            .build()
        execute(req)
    }

    suspend fun executeCloudflareD1Query(
        accountId: String,
        databaseId: String,
        apiToken: String,
        sql: String,
        params: List<Any> = emptyList()
    ): HttpCallResult = withContext(ioDispatcher) {
        val url = "https://api.cloudflare.com/client/v4/accounts/$accountId/d1/database/$databaseId/query"
        val jsonBody = JSONObject().apply {
            put("sql", sql)
            if (params.isNotEmpty()) {
                put("params", JSONArray(params))
            }
        }.toString()

        val req = Request.Builder()
            .url(url)
            .post(jsonBody.toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Bearer $apiToken")
            .addHeader("Content-Type", "application/json")
            .build()
        execute(req)
    }

    suspend fun getWorkerSchedules(
        accountId: String,
        scriptName: String,
        apiToken: String
    ): HttpCallResult = withContext(ioDispatcher) {
        val url = "https://api.cloudflare.com/client/v4/accounts/$accountId/workers/scripts/$scriptName/schedules"
        val req = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $apiToken")
            .addHeader("Accept", "application/json")
            .build()
        execute(req)
    }

    suspend fun putWorkerSchedules(
        accountId: String,
        scriptName: String,
        apiToken: String,
        crons: List<String>
    ): HttpCallResult = withContext(ioDispatcher) {
        val url = "https://api.cloudflare.com/client/v4/accounts/$accountId/workers/scripts/$scriptName/schedules"
        val schedulesArray = JSONArray().apply {
            crons.forEach { cron ->
                put(JSONObject().put("cron", cron))
            }
        }
        val jsonBody = JSONObject().put("schedules", schedulesArray).toString()
        val req = Request.Builder()
            .url(url)
            .put(jsonBody.toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Bearer $apiToken")
            .addHeader("Content-Type", "application/json")
            .build()
        execute(req)
    }

    suspend fun callCustomLlmRewrite(
        endpoint: String,
        apiKey: String,
        prompt: String
    ): HttpCallResult = withContext(ioDispatcher) {
        val jsonBody = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put(
                "messages",
                JSONArray().put(
                    JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    }
                )
            )
            put("temperature", 0.7)
        }.toString()

        val req = Request.Builder()
            .url(endpoint)
            .post(jsonBody.toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()
        execute(req)
    }

    private fun execute(request: Request): HttpCallResult {
        val startNs = System.nanoTime()
        return try {
            client.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)
                HttpCallResult(
                    isSuccess = response.isSuccessful,
                    statusCode = response.code,
                    body = rawBody,
                    errorMessage = if (response.isSuccessful) null else "HTTP ${response.code}",
                    latencyMs = elapsedMs
                )
            }
        } catch (e: Exception) {
            val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)
            HttpCallResult(
                isSuccess = false,
                statusCode = -1,
                body = "",
                errorMessage = e.localizedMessage ?: "네트워크 연결 오류",
                latencyMs = elapsedMs
            )
        }
    }
}
