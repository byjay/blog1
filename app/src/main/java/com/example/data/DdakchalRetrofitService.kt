package com.example.data

import com.example.model.ChannelPlatform
import com.example.model.DdakchalQueueItemRecord
import com.example.model.PostStatus
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class DdakchalQueueEnvelopeDto(
    val ok: Boolean? = null,
    val data: DdakchalQueueDataDto? = null,
    val items: List<DdakchalQueueItemDto>? = null,
    val message: String? = null,
    val error: String? = null
)

data class DdakchalQueueDataDto(
    val items: List<DdakchalQueueItemDto>? = null,
    val total: Int? = null
)

data class DdakchalQueueItemDto(
    val id: String? = null,
    @Json(name = "post_id") val postId: String? = null,
    @Json(name = "case_no") val caseNoSnake: String? = null,
    val caseNo: String? = null,
    @Json(name = "case_id") val caseIdSnake: String? = null,
    @Json(name = "auction_dedup_key") val auctionDedupKey: String? = null,
    val dedupKey: String? = null,
    val title: String? = null,
    val platform: String? = null,
    val channel: String? = null,
    val status: String? = null,
    @Json(name = "publish_date") val publishDateSnake: String? = null,
    val publishDate: String? = null,
    @Json(name = "scheduled_for") val scheduledForSnake: String? = null,
    val scheduledFor: String? = null,
    @Json(name = "scheduled_at") val scheduledAtSnake: String? = null,
    val scheduledAt: String? = null,
    @Json(name = "published_at") val publishedAtSnake: String? = null,
    val publishedAt: String? = null,
    @Json(name = "created_at") val createdAtSnake: String? = null,
    @Json(name = "updated_at") val updatedAtSnake: String? = null,
    @Json(name = "blog_post_url") val blogPostUrlSnake: String? = null,
    val blogPostUrl: String? = null,
    @Json(name = "published_url") val publishedUrlSnake: String? = null,
    @Json(name = "last_error") val lastErrorSnake: String? = null,
    val lastError: String? = null
)

interface DdakchalBlogRetrofitApi {
    @GET("api/v1/blog/queue")
    suspend fun getBlogQueue(
        @Query("limit") limit: Int = 100
    ): Response<DdakchalQueueEnvelopeDto>

    @GET("api/v1/blog/queue")
    fun getBlogQueueCall(
        @Query("limit") limit: Int = 100
    ): Call<DdakchalQueueEnvelopeDto>
}

class DdakchalRetrofitClientFactory(
    private val baseOkHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .build()
) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun create(apiBase: String, syncToken: String): DdakchalBlogRetrofitApi {
        val cleanBase = apiBase.trim().removeSuffix("/") + "/"
        val cleanToken = syncToken.trim()

        val existingInterceptors = baseOkHttpClient.interceptors.toList()
        val authedClient = baseOkHttpClient.newBuilder().apply {
            interceptors().clear()
            addInterceptor { chain ->
                val original: Request = chain.request()
                val reqBuilder = original.newBuilder()
                    .header("Accept", "application/json")
                if (cleanToken.isNotBlank()) {
                    reqBuilder.header("Authorization", "Bearer $cleanToken")
                    reqBuilder.header("X-Crawler-Sync-Token", cleanToken)
                }
                chain.proceed(reqBuilder.build())
            }
            existingInterceptors.forEach { addInterceptor(it) }
        }.build()

        val retrofit = Retrofit.Builder()
            .baseUrl(cleanBase)
            .client(authedClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(DdakchalBlogRetrofitApi::class.java)
    }

    companion object {
        fun DdakchalQueueItemDto.toDomainRecord(index: Int): DdakchalQueueItemRecord {
            val resolvedId = (id ?: postId).orEmpty().ifBlank { "queue-item-$index" }
            val resolvedCaseNo = (caseNoSnake ?: caseNo ?: caseIdSnake).orEmpty().ifBlank { "미지정" }
            val resolvedDedup = (auctionDedupKey ?: dedupKey).orEmpty().ifBlank { resolvedId }
            val resolvedPlatform = ChannelPlatform.fromCode(
                (platform ?: channel).orEmpty().ifBlank { ChannelPlatform.CLOUDFLARE.code }
            )
            val rawStatus = status.orEmpty().trim().uppercase()
            val resolvedStatus = when (rawStatus) {
                "READY", "QUEUED", "PENDING" -> PostStatus.QUEUED
                "DRAFT_SAVED", "DRAFT" -> PostStatus.DRAFT
                "PUBLISHED", "DONE", "SUCCESS" -> PostStatus.PUBLISHED
                "HANDOFF", "CAPTCHA" -> PostStatus.HANDOFF
                "FAILED", "ERROR" -> PostStatus.FAILED
                else -> PostStatus.fromCode(rawStatus)
            }
            val rawScheduled = (scheduledForSnake ?: scheduledFor ?: scheduledAtSnake ?: scheduledAt).orEmpty()
            val rawPublished = (publishedAtSnake ?: publishedAt).orEmpty()
            val rawUpdated = (updatedAtSnake ?: createdAtSnake).orEmpty()
            val resolvedDate = (publishDateSnake ?: publishDate).orEmpty()
                .ifBlank { rawPublished.take(10) }
                .ifBlank { rawScheduled.take(10) }
                .ifBlank { rawUpdated.take(10) }
                .ifBlank { "미정" }

            return DdakchalQueueItemRecord(
                id = resolvedId,
                caseNo = resolvedCaseNo,
                dedupKey = resolvedDedup,
                title = title.orEmpty().ifBlank { "$resolvedCaseNo 블로그 발행 건" },
                platform = resolvedPlatform,
                status = resolvedStatus,
                publishDate = resolvedDate,
                scheduledFor = rawScheduled,
                publishedAt = rawPublished,
                blogPostUrl = (blogPostUrlSnake ?: blogPostUrl ?: publishedUrlSnake).orEmpty(),
                lastError = (lastErrorSnake ?: lastError).orEmpty()
            )
        }
    }
}
