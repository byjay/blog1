package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class HttpCallResult(
    val isSuccess: Boolean,
    val statusCode: Int,
    val body: String,
    val errorMessage: String? = null
)

class CloudflareAndDdakchalApi {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun triggerGenerateDaily5(
        apiBase: String,
        syncToken: String,
        limit: Int,
        minDelayMinutes: Int,
        maxDelayMinutes: Int,
        enableExternalLinkNaver: Boolean
    ): HttpCallResult = withContext(Dispatchers.IO) {
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
        if (syncToken.isNotBlank()) {
            reqBuilder.addHeader("Authorization", "Bearer $syncToken")
        }
        execute(reqBuilder.build())
    }

    suspend fun fetchSyndicationPack(
        apiBase: String,
        syncToken: String
    ): HttpCallResult = withContext(Dispatchers.IO) {
        val url = "${apiBase.trimEnd('/')}/api/v1/blog/syndication-pack"
        val reqBuilder = Request.Builder()
            .url(url)
            .get()
            .addHeader("Accept", "application/json")
        if (syncToken.isNotBlank()) {
            reqBuilder.addHeader("Authorization", "Bearer $syncToken")
        }
        execute(reqBuilder.build())
    }

    suspend fun listCloudflareD1Databases(
        accountId: String,
        apiToken: String
    ): HttpCallResult = withContext(Dispatchers.IO) {
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
    ): HttpCallResult = withContext(Dispatchers.IO) {
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
    ): HttpCallResult = withContext(Dispatchers.IO) {
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
    ): HttpCallResult = withContext(Dispatchers.IO) {
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
    ): HttpCallResult = withContext(Dispatchers.IO) {
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
        return try {
            client.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                HttpCallResult(
                    isSuccess = response.isSuccessful,
                    statusCode = response.code,
                    body = rawBody,
                    errorMessage = if (response.isSuccessful) null else "HTTP ${response.code}"
                )
            }
        } catch (e: Exception) {
            HttpCallResult(
                isSuccess = false,
                statusCode = -1,
                body = "",
                errorMessage = e.localizedMessage ?: "네트워크 연결 오류"
            )
        }
    }
}
