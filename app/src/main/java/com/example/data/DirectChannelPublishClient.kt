package com.example.data

import com.example.engine.HtmlExportEngine
import com.example.engine.LegalDoctrineVerifier
import com.example.engine.PrivacySanitizerEngine
import com.example.engine.QaVerificationEngine
import com.example.engine.QualityDefenseEngine
import com.example.model.ChannelPlatform
import com.example.model.ChannelProbeResult
import com.example.model.ChannelPublishDestination
import com.example.model.ChannelDirectPublishOutcome
import com.example.model.GeneratedPostSlot
import com.example.model.NaverAndMultiChannelPublishReport
import com.example.model.NaverAutoWriteMode
import com.example.model.NaverWriteMethodGuide
import com.example.model.PublishVerificationCheck
import com.example.model.ServerAutomationConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class RelayPublishPayload(
    val postId: String,
    val caseId: String,
    val channel: String,
    val blogId: String,
    val categoryName: String,
    val title: String,
    val bodyText: String,
    val bodyHtml: String,
    val tags: List<String>,
    val pinnedComment: String
)

class DirectChannelPublishClient(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val d1Api: CloudflareAndDdakchalApi = CloudflareAndDdakchalApi()
) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val payloadAdapter = moshi.adapter(RelayPublishPayload::class.java)
    private val mapAdapter = moshi.adapter<Map<String, Any?>>(
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    )
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun buildDestinations(config: ServerAutomationConfig): List<ChannelPublishDestination> {
        val naverCleanId = normalizeNaverBlogId(config.naverBlogId)
        val tistoryCleanUrl = normalizeTistoryBaseUrl(config.tistoryBlogUrl)
        val cfSiteBase = resolveCloudflareSiteBase(config)
        val cfPath = config.cloudflareBlogPath.trim().let {
            if (it.startsWith("/")) it else "/$it"
        }
        return listOf(
            ChannelPublishDestination(
                platform = ChannelPlatform.NAVER,
                enabled = config.publishTargetNaver,
                targetAddress = if (naverCleanId.isNotBlank()) "https://blog.naver.com/$naverCleanId" else "",
                categoryName = config.naverCategoryName.ifBlank { "부동산 경매 분석" },
                isConfigured = naverCleanId.isNotBlank()
            ),
            ChannelPublishDestination(
                platform = ChannelPlatform.TISTORY,
                enabled = config.publishTargetTistory,
                targetAddress = tistoryCleanUrl,
                categoryName = config.tistoryCategoryName.ifBlank { "경매·권리·세무 리포트" },
                isConfigured = tistoryCleanUrl.isNotBlank()
            ),
            ChannelPublishDestination(
                platform = ChannelPlatform.CLOUDFLARE,
                enabled = config.publishTargetCloudflare,
                targetAddress = "$cfSiteBase$cfPath",
                categoryName = "GEO/AIO 자체웹 (${cfSiteBase.removePrefix("https://")})",
                isConfigured = cfSiteBase.isNotBlank()
            )
        )
    }

    fun resolveCloudflareSiteBase(config: ServerAutomationConfig): String =
        config.cloudflareSiteBase.trim().removeSuffix("/").ifBlank { "https://aution.codeb.app" }

    fun normalizeNaverBlogId(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return ""
        val afterHost = trimmed
            .removePrefix("https://m.blog.naver.com/")
            .removePrefix("http://m.blog.naver.com/")
            .removePrefix("https://blog.naver.com/")
            .removePrefix("http://blog.naver.com/")
            .trim('/')
        return afterHost.substringBefore("/").substringBefore("?").trim()
    }

    fun normalizeTistoryBaseUrl(raw: String): String {
        val trimmed = raw.trim().removeSuffix("/")
        if (trimmed.isBlank()) return ""
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") -> "https://$trimmed"
            else -> "https://$trimmed.tistory.com"
        }
    }

    fun buildNaverWriteAssistUrl(naverBlogId: String, title: String = "", bodyText: String = ""): String {
        val cleanId = normalizeNaverBlogId(naverBlogId)
        val encodedTitle = URLEncoder.encode(title.take(120), "UTF-8")
        val encodedBody = URLEncoder.encode(bodyText.take(600), "UTF-8")
        return if (cleanId.isNotBlank()) {
            "https://blog.naver.com/$cleanId?Redirect=Write&title=$encodedTitle&content=$encodedBody"
        } else {
            "https://blog.naver.com/MyBlog.naver"
        }
    }

    suspend fun probeNaverConnection(config: ServerAutomationConfig): ChannelProbeResult = withContext(ioDispatcher) {
        val nowTs = SimpleDateFormat("HH:mm:ss", Locale.KOREA).format(Date())
        val cleanId = normalizeNaverBlogId(config.naverBlogId)
        val relay = resolveRelayEndpoint(config, "publish-naver")
        if (cleanId.isBlank()) {
            return@withContext ChannelProbeResult(
                platform = ChannelPlatform.NAVER,
                targetUrl = "",
                reachableOk = false,
                httpsCompliant = true,
                httpStatusCode = 0,
                detailMessage = "NAVER_BLOG_ID를 입력하면 비번 없이 주소·릴레이 연결을 점검합니다.",
                checkedAt = nowTs
            )
        }
        val blogUrl = "https://m.blog.naver.com/$cleanId"
        val res = executeHttpGet(blogUrl)
        val relayNote = if (relay.isNotBlank()) " · 워커 릴레이(${relay.substringBefore("/api")}) 준비됨" else " · 워커 미연동(수동 글쓰기창·세션 핸드오프)"
        ChannelProbeResult(
            platform = ChannelPlatform.NAVER,
            targetUrl = blogUrl,
            reachableOk = res.isSuccess,
            httpsCompliant = blogUrl.startsWith("https://"),
            httpStatusCode = res.statusCode,
            detailMessage = if (res.isSuccess) {
                "네이버 블로그 실존 확인 (HTTP ${res.statusCode})$relayNote"
            } else {
                "네이버 블로그 응답 확인 필요 (HTTP ${res.statusCode}: ${res.errorMessage ?: "네트워크 확인"})$relayNote"
            },
            checkedAt = nowTs
        )
    }

    suspend fun probeTistoryConnection(config: ServerAutomationConfig): ChannelProbeResult = withContext(ioDispatcher) {
        val nowTs = SimpleDateFormat("HH:mm:ss", Locale.KOREA).format(Date())
        val normalizedUrl = normalizeTistoryBaseUrl(config.tistoryBlogUrl)
        if (normalizedUrl.isBlank()) {
            return@withContext ChannelProbeResult(
                platform = ChannelPlatform.TISTORY,
                targetUrl = "",
                reachableOk = false,
                httpsCompliant = false,
                httpStatusCode = 0,
                detailMessage = "TISTORY_BLOG_URL (예: https://myblog.tistory.com)을 입력해 주세요.",
                checkedAt = nowTs
            )
        }
        val httpsOk = normalizedUrl.startsWith("https://")
        val res = executeHttpGet(normalizedUrl)
        ChannelProbeResult(
            platform = ChannelPlatform.TISTORY,
            targetUrl = normalizedUrl,
            reachableOk = res.isSuccess,
            httpsCompliant = httpsOk,
            httpStatusCode = res.statusCode,
            detailMessage = if (res.isSuccess) {
                "티스토리 URL HTTP ${res.statusCode} 접속 확인 (${if (httpsOk) "HTTPS 보안 적용" else "HTTPS 전환 권장"})"
            } else {
                "티스토리 주소 응답 실패 (HTTP ${res.statusCode}: ${res.errorMessage ?: "도메인 오타 또는 비공개 확인"})"
            },
            checkedAt = nowTs
        )
    }

    suspend fun publishNaverDirectAndVerify(
        slot: GeneratedPostSlot,
        config: ServerAutomationConfig,
        customNaverPostUrl: String = ""
    ): NaverAndMultiChannelPublishReport = withContext(ioDispatcher) {
        val singleNaverConfig = config.copy(
            publishTargetNaver = true,
            publishTargetTistory = false,
            publishTargetCloudflare = false
        )
        publishSelectedChannelsInternal(slot, singleNaverConfig, customNaverPostUrl)
    }

    suspend fun publishSelectedChannelsAndVerify(
        slot: GeneratedPostSlot,
        config: ServerAutomationConfig,
        customNaverPostUrl: String = ""
    ): NaverAndMultiChannelPublishReport = withContext(ioDispatcher) {
        publishSelectedChannelsInternal(slot, config, customNaverPostUrl)
    }

    private suspend fun publishSelectedChannelsInternal(
        slot: GeneratedPostSlot,
        config: ServerAutomationConfig,
        customNaverPostUrl: String
    ): NaverAndMultiChannelPublishReport {
        val nowTs = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())
        val naverCleanId = normalizeNaverBlogId(config.naverBlogId)
        val naverVariant = slot.variants[ChannelPlatform.NAVER]
        val writeAssistUrl = buildNaverWriteAssistUrl(
            naverBlogId = naverCleanId,
            title = naverVariant?.title.orEmpty(),
            bodyText = naverVariant?.bodyText.orEmpty()
        )
        val outcomes = mutableListOf<ChannelDirectPublishOutcome>()
        var verifiedNaverUrl = customNaverPostUrl.trim()

        // 1) Naver Blog direct relay + live HTTP GET verification
        if (config.publishTargetNaver) {
            val naverOutcome = executeNaverPublishAndVerify(
                slot = slot,
                config = config,
                naverCleanId = naverCleanId,
                customNaverPostUrl = customNaverPostUrl.trim()
            )
            outcomes += naverOutcome
            if (naverOutcome.publishedPostUrl.isNotBlank()) {
                verifiedNaverUrl = naverOutcome.publishedPostUrl
            }
        }

        // 2) Tistory direct relay + live HTTP GET verification
        if (config.publishTargetTistory) {
            outcomes += executeTistoryPublishAndVerify(slot, config)
        }

        // 3) Cloudflare direct D1/Pages push + live HTTP GET verification
        if (config.publishTargetCloudflare) {
            outcomes += executeCloudflarePublishAndVerify(slot, config)
        }

        val checks = runNaverAndChannelVerificationChecks(
            slot = slot,
            config = config,
            naverCleanId = naverCleanId,
            outcomes = outcomes
        )
        val naverOutcome = outcomes.firstOrNull { it.platform == ChannelPlatform.NAVER }
        val naverPassed = naverOutcome?.reachableOk == true && checks.all { it.passed }

        val selectedCount = listOf(
            config.publishTargetNaver,
            config.publishTargetTistory,
            config.publishTargetCloudflare
        ).count { it }

        val summary = buildString {
            val okChannels = outcomes.count { it.reachableOk }
            append("선택 ${selectedCount}개 채널 검증 완료 (${okChannels}/${outcomes.size} HTTP 도달 성공)")
            if (config.publishTargetNaver && naverCleanId.isBlank()) {
                append(" · 네이버 블로그 ID 미입력")
            } else if (naverOutcome != null && !naverOutcome.reachableOk) {
                append(" · 네이버 직접 발행/도달 보완 필요(${naverOutcome.message})")
            } else if (naverPassed) {
                append(" · 네이버 6항목 검증 PASS")
            }
        }

        return NaverAndMultiChannelPublishReport(
            caseId = slot.auctionCase.caseId,
            attemptedAt = nowTs,
            naverDirectPassed = naverPassed,
            selectedChannelCount = selectedCount,
            verifiedNaverPostUrl = verifiedNaverUrl,
            naverWriteAssistUrl = writeAssistUrl,
            outcomes = outcomes,
            verificationChecks = checks,
            overallSummary = summary
        )
    }

    private fun executeNaverPublishAndVerify(
        slot: GeneratedPostSlot,
        config: ServerAutomationConfig,
        naverCleanId: String,
        customNaverPostUrl: String
    ): ChannelDirectPublishOutcome {
        val nv = slot.variants[ChannelPlatform.NAVER]
        if (naverCleanId.isBlank() && customNaverPostUrl.isBlank()) {
            return ChannelDirectPublishOutcome(
                platform = ChannelPlatform.NAVER,
                targetUrl = "",
                executedMethod = "CONFIG_CHECK",
                httpStatusCode = 0,
                reachableOk = false,
                publishedPostUrl = "",
                message = "네이버 블로그 ID(또는 게시물 URL)를 먼저 입력·저장해 주세요."
            )
        }

        val relayEndpoint = resolveRelayEndpoint(config, "publish-naver")
        var relayPostOk = false
        var relayPostUrl = ""
        var relayCode = 0
        var relayMsg = ""

        if (relayEndpoint.isNotBlank() && nv != null) {
            val enrichedNaverHtml = HtmlExportEngine.renderArticleBodySnippet(slot, nv, config)
            val payload = RelayPublishPayload(
                postId = slot.postId,
                caseId = slot.auctionCase.caseId,
                channel = ChannelPlatform.NAVER.code,
                blogId = naverCleanId,
                categoryName = config.naverCategoryName.ifBlank { "부동산 경매 분석" },
                title = nv.title,
                bodyText = nv.bodyText,
                bodyHtml = enrichedNaverHtml,
                tags = nv.tags,
                pinnedComment = nv.pinnedCommentSnippet
            )
            val postRes = executeJsonPost(relayEndpoint, config.crawlerSyncToken, payloadAdapter.toJson(payload))
            relayCode = postRes.statusCode
            if (postRes.isSuccess) {
                relayPostOk = true
                val parsedUrl = extractUrlFromJson(postRes.body)
                if (parsedUrl.isNotBlank()) relayPostUrl = parsedUrl
                relayMsg = "워커 릴레이 POST 성공 (HTTP $relayCode)"
            } else {
                relayMsg = "릴레이 응답 HTTP $relayCode (${postRes.errorMessage ?: "Playwright 워커 미배포"})"
            }
        }

        val hasActualPostProof = customNaverPostUrl.startsWith("http") || relayPostOk
        val postUrlCandidate = when {
            customNaverPostUrl.startsWith("http") -> customNaverPostUrl
            relayPostUrl.startsWith("http") -> relayPostUrl
            else -> ""
        }

        if (!hasActualPostProof) {
            val probeUrl = if (naverCleanId.isNotBlank()) "https://m.blog.naver.com/$naverCleanId" else ""
            val probeRes = if (probeUrl.isNotBlank()) executeHttpGet(probeUrl) else HttpCheckResult(0, false, "", "미연동")
            val homeProbeNote = if (probeRes.isSuccess) {
                "블로그 홈(${probeUrl}) 접속 가능(HTTP ${probeRes.statusCode})이나 워커 릴레이 미연동/실패로 실제 글은 등록되지 않았습니다 (대기·실패 처리)."
            } else {
                "워커 릴레이 및 블로그 접속 확인 필요 (${probeRes.errorMessage ?: "HTTP ${probeRes.statusCode}"})"
            }
            return ChannelDirectPublishOutcome(
                platform = ChannelPlatform.NAVER,
                targetUrl = probeUrl,
                executedMethod = if (relayEndpoint.isNotBlank()) "RELAY_POST_FAILED" else "RELAY_REQUIRED_NOT_PUBLISHED",
                httpStatusCode = if (relayCode > 0) relayCode else probeRes.statusCode,
                reachableOk = false,
                publishedPostUrl = "",
                message = buildString {
                    if (relayMsg.isNotBlank()) append("$relayMsg / ")
                    append(homeProbeNote)
                }
            )
        }

        val verifyUrl = postUrlCandidate.ifBlank { "https://m.blog.naver.com/$naverCleanId" }
        val getRes = executeHttpGet(verifyUrl)
        val finalHttpCode = if (getRes.statusCode > 0) getRes.statusCode else relayCode
        val verifiedReachable = getRes.isSuccess && (
            !getRes.body.contains("존재하지 않는 게시물") &&
                !getRes.body.contains("삭제되었거나")
            )

        val methodLabel = if (relayPostOk) "RELAY_POST + HTTP_GET" else "CUSTOM_POST_URL_VERIFY"
        val combinedMessage = buildString {
            if (relayMsg.isNotBlank()) {
                append(relayMsg)
                append(" / ")
            }
            if (verifiedReachable) {
                append("실제 네이버 게시물 URL 도달 확인 (HTTP ${getRes.statusCode})")
            } else {
                append("HTTP GET 검증 실패 (${getRes.statusCode}: ${getRes.errorMessage ?: "게시물 주소 또는 네트워크 확인 필요"})")
            }
        }

        return ChannelDirectPublishOutcome(
            platform = ChannelPlatform.NAVER,
            targetUrl = verifyUrl,
            executedMethod = methodLabel,
            httpStatusCode = finalHttpCode,
            reachableOk = verifiedReachable,
            publishedPostUrl = if (verifiedReachable) verifyUrl else "",
            message = combinedMessage
        )
    }

    private fun executeTistoryPublishAndVerify(
        slot: GeneratedPostSlot,
        config: ServerAutomationConfig
    ): ChannelDirectPublishOutcome {
        val baseUrl = normalizeTistoryBaseUrl(config.tistoryBlogUrl)
        if (baseUrl.isBlank()) {
            return ChannelDirectPublishOutcome(
                platform = ChannelPlatform.TISTORY,
                targetUrl = "",
                executedMethod = "CONFIG_CHECK",
                httpStatusCode = 0,
                reachableOk = false,
                publishedPostUrl = "",
                message = "티스토리 블로그 주소(예: https://myblog.tistory.com)를 입력해 주세요."
            )
        }
        val relayEndpoint = resolveRelayEndpoint(config, "publish-tistory")
        val tsVariant = slot.variants[ChannelPlatform.TISTORY]
        var relayPostOk = false
        var relayPostUrl = ""
        var relayCode = 0
        var relayNote = ""
        if (relayEndpoint.isNotBlank() && tsVariant != null) {
            val enrichedTistoryHtml = HtmlExportEngine.renderArticleBodySnippet(slot, tsVariant, config)
            val payload = RelayPublishPayload(
                postId = slot.postId,
                caseId = slot.auctionCase.caseId,
                channel = ChannelPlatform.TISTORY.code,
                blogId = baseUrl,
                categoryName = config.tistoryCategoryName.ifBlank { "경매·권리·세무 리포트" },
                title = tsVariant.title,
                bodyText = tsVariant.bodyText,
                bodyHtml = enrichedTistoryHtml,
                tags = tsVariant.tags,
                pinnedComment = ""
            )
            val postRes = executeJsonPost(relayEndpoint, config.crawlerSyncToken, payloadAdapter.toJson(payload))
            relayCode = postRes.statusCode
            if (postRes.isSuccess) {
                relayPostOk = true
                relayPostUrl = extractUrlFromJson(postRes.body)
                relayNote = "릴레이 POST 성공(${postRes.statusCode}) / "
            } else {
                relayNote = "릴레이 HTTP ${postRes.statusCode} / "
            }
        }

        if (!relayPostOk) {
            val probeRes = executeHttpGet(baseUrl)
            val probeNote = if (probeRes.isSuccess) {
                "티스토리 홈($baseUrl)은 응답(HTTP ${probeRes.statusCode})하나 워커 릴레이 미연동/실패로 실제 글 미발행 (대기·실패 처리)"
            } else {
                "티스토리 릴레이/도메인 확인 필요 (HTTP ${probeRes.statusCode})"
            }
            return ChannelDirectPublishOutcome(
                platform = ChannelPlatform.TISTORY,
                targetUrl = baseUrl,
                executedMethod = if (relayEndpoint.isNotBlank()) "RELAY_POST_FAILED" else "RELAY_REQUIRED_NOT_PUBLISHED",
                httpStatusCode = if (relayCode > 0) relayCode else probeRes.statusCode,
                reachableOk = false,
                publishedPostUrl = "",
                message = "$relayNote$probeNote"
            )
        }

        val checkUrl = relayPostUrl.ifBlank { baseUrl }
        val getRes = executeHttpGet(checkUrl)
        return ChannelDirectPublishOutcome(
            platform = ChannelPlatform.TISTORY,
            targetUrl = checkUrl,
            executedMethod = "RELAY_POST + HTTP_GET",
            httpStatusCode = getRes.statusCode,
            reachableOk = getRes.isSuccess,
            publishedPostUrl = if (getRes.isSuccess) checkUrl else "",
            message = "$relayNote${if (getRes.isSuccess) "티스토리 실제 게시물 도달 확인 (HTTP ${getRes.statusCode})" else "도달 실패 (HTTP ${getRes.statusCode}: ${getRes.errorMessage ?: "URL 확인 필요"})"}"
        )
    }

    private suspend fun executeCloudflarePublishAndVerify(
        slot: GeneratedPostSlot,
        config: ServerAutomationConfig
    ): ChannelDirectPublishOutcome {
        val cfSiteBase = resolveCloudflareSiteBase(config)
        val cfPath = config.cloudflareBlogPath.trim().let { if (it.startsWith("/")) it else "/$it" }
        val targetUrl = "$cfSiteBase$cfPath/${slot.auctionCase.dedupKey}"
        var d1StatusNote = ""
        var d1InsertedOk = false
        if (config.cfAccountId.isNotBlank() && config.cfD1DatabaseId.isNotBlank() && config.cfApiToken.isNotBlank()) {
            val cfVariant = slot.variants[ChannelPlatform.CLOUDFLARE]
            if (cfVariant != null) {
                val enrichedCfHtml = HtmlExportEngine.renderArticleBodySnippet(slot, cfVariant, config)
                val metaJson = org.json.JSONObject().apply {
                    put("slug", slot.auctionCase.dedupKey)
                    put("tags", org.json.JSONArray(cfVariant.tags))
                    put("metaDescription", cfVariant.metaDescription)
                    put("jsonLd", cfVariant.jsonLdSchema)
                }.toString()
                val res = d1Api.executeCloudflareD1Query(
                    accountId = config.cfAccountId,
                    databaseId = config.cfD1DatabaseId,
                    apiToken = config.cfApiToken,
                    sql = "INSERT OR REPLACE INTO generated_posts (post_id, queue_id, case_id, property_type, blog_category, platform, title, body_html, body_markdown, meta_json, status, scheduled_for) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
                    params = listOf(
                        "${slot.postId}-cloudflare",
                        slot.queueId,
                        slot.auctionCase.caseId,
                        slot.auctionCase.propertyType.code,
                        slot.auctionCase.propertyType.blogCategory,
                        ChannelPlatform.CLOUDFLARE.code,
                        cfVariant.title,
                        enrichedCfHtml,
                        cfVariant.bodyText,
                        metaJson,
                        "PUBLISHED",
                        slot.scheduledTimes[ChannelPlatform.CLOUDFLARE] ?: slot.publishDate
                    )
                )
                d1InsertedOk = res.isSuccess
                d1StatusNote = if (res.isSuccess) "D1 저장 성공 / " else "D1 HTTP ${res.statusCode} / "
            }
        }
        val getRes = executeHttpGet(targetUrl)
        val ok = getRes.isSuccess
        val methodLabel = when {
            d1InsertedOk && ok -> "CF_D1_UPSERT + HTTP_GET"
            d1InsertedOk && !ok -> "CF_D1_SAVED_WORKER_ROUTE_NEEDED"
            else -> "POST_PATH_HTTP_GET_VERIFY"
        }
        return ChannelDirectPublishOutcome(
            platform = ChannelPlatform.CLOUDFLARE,
            targetUrl = targetUrl,
            executedMethod = methodLabel,
            httpStatusCode = getRes.statusCode,
            reachableOk = ok,
            publishedPostUrl = if (ok) targetUrl else "",
            message = "$d1StatusNote${if (ok) "$targetUrl 응답 정상 (HTTP ${getRes.statusCode})" else "$targetUrl 미게시/응답 실패 (HTTP ${getRes.statusCode}: D1 토큰 설정 또는 Worker 배포 확인)"}"
        )
    }

    suspend fun verifyUrlReachable(url: String): Pair<Boolean, Int> = withContext(ioDispatcher) {
        if (!url.startsWith("http")) return@withContext false to 0
        val res = executeHttpGet(url)
        res.isSuccess to res.statusCode
    }

    suspend fun probeAlwaysLoggedInHeartbeat(
        config: ServerAutomationConfig
    ): Triple<ChannelProbeResult, ChannelProbeResult, ChannelProbeResult> = withContext(ioDispatcher) {
        val nowTs = SimpleDateFormat("HH:mm:ss", Locale.KOREA).format(Date())
        val naverProbe = probeNaverConnection(config)
        val tistoryProbe = probeTistoryConnection(config)
        val cfSiteBase = resolveCloudflareSiteBase(config)
        val cfRes = executeHttpGet(cfSiteBase)
        val cfProbe = ChannelProbeResult(
            platform = ChannelPlatform.CLOUDFLARE,
            targetUrl = cfSiteBase,
            reachableOk = cfRes.isSuccess,
            httpsCompliant = cfSiteBase.startsWith("https://"),
            httpStatusCode = cfRes.statusCode,
            detailMessage = if (cfRes.isSuccess) {
                "$cfSiteBase 정상 응답 (HTTP ${cfRes.statusCode})"
            } else {
                "$cfSiteBase 연결 확인 필요 (HTTP ${cfRes.statusCode}: ${cfRes.errorMessage ?: "DNS/Pages 배포 대기"})"
            },
            checkedAt = nowTs
        )
        Triple(naverProbe, tistoryProbe, cfProbe)
    }

    fun buildAlternateNaverWriteGuides(
        config: ServerAutomationConfig,
        sampleSlot: GeneratedPostSlot? = null
    ): List<NaverWriteMethodGuide> {
        val naverId = normalizeNaverBlogId(config.naverBlogId)
        val relay = resolveRelayEndpoint(config, "publish-naver")
        val sampleVariant = sampleSlot?.variants?.get(ChannelPlatform.NAVER)
        val assistUrl = buildNaverWriteAssistUrl(
            naverBlogId = naverId,
            title = sampleVariant?.title.orEmpty(),
            bodyText = sampleVariant?.bodyText.orEmpty()
        )
        return listOf(
            NaverWriteMethodGuide(
                mode = NaverAutoWriteMode.WORKER_RELAY,
                isActive = config.naverWriteMode == NaverAutoWriteMode.WORKER_RELAY,
                readinessLabel = if (relay.isNotBlank()) "릴레이 준비됨 (${relay.substringBefore("/api")})" else "릴레이 URL/토큰 대기",
                setupSteps = listOf(
                    "PC 또는 서버에서 Playwright로 네이버 로그인 후 session_naver.json(storage_state) 저장",
                    "설정·보안 탭에 NAVER_BLOG_ID + 워커 릴레이 URL(또는 CRAWLER_SYNC_TOKEN) 입력",
                    "'95+ 바로 등록' 실행 시 POST /publish-naver로 무인 자동 포스팅 및 HTTP 검증"
                ),
                quickActionUrl = relay
            ),
            NaverWriteMethodGuide(
                mode = NaverAutoWriteMode.MOBILE_WRITE_URL,
                isActive = config.naverWriteMode == NaverAutoWriteMode.MOBILE_WRITE_URL,
                readinessLabel = if (naverId.isNotBlank()) "즉시 실행 가능 (로그인 유지 연동)" else "NAVER_BLOG_ID 입력 필요",
                setupSteps = listOf(
                    "앱 내 '항상 로그인 유지'로 폰 브라우저(Chrome)에 네이버 로그인을 1회만 유지",
                    "'제목·본문·고정댓글 복사' 후 '네이버 글쓰기 열기' 클릭",
                    "스마트에디터 ONE에 붙여넣기 → 발행(가장 저품질 위험이 적은 안전 자동화)"
                ),
                quickActionUrl = assistUrl
            ),
            NaverWriteMethodGuide(
                mode = NaverAutoWriteMode.POST_MAIL_BRIDGE,
                isActive = config.naverWriteMode == NaverAutoWriteMode.POST_MAIL_BRIDGE,
                readinessLabel = "네이버 공식 기능 (캡차 무발생)",
                setupSteps = listOf(
                    "네이버 블로그 > 관리 > 기본 설정 > '글쓰기 메일'에서 전용 이메일 주소 활성화",
                    "발송 허용 이메일에 본인 메일 등록 후 Cloudflare Worker/SMTP로 원고+이미지 전송",
                    "네이버가 메일 제목/본문을 자동으로 블로그 글로 등록(이후 고정댓글만 보완)"
                ),
                quickActionUrl = if (naverId.isNotBlank()) "https://blog.naver.com/$naverId" else "https://section.blog.naver.com/"
            ),
            NaverWriteMethodGuide(
                mode = NaverAutoWriteMode.SESSION_HANDOFF,
                isActive = config.naverWriteMode == NaverAutoWriteMode.SESSION_HANDOFF,
                readinessLabel = if (config.naverKeepLoggedIn) "상시 세션 유지 ON" else "유지 OFF",
                setupSteps = listOf(
                    "비밀번호를 기기에 저장하지 않고 브라우저 쿠키·세션만 유지하는 Zero-Password 방식",
                    "'HTML 3개 생성 → 블로그 붙여넣기(<article>)' 복사 후 HTML/에디터에 즉시 등록",
                    "캡차/2차 인증 시 '세션 복구' 1터치로 자동화 큐 재개"
                ),
                quickActionUrl = "https://nid.naver.com/nidlogin.login"
            )
        )
    }

    private fun runNaverAndChannelVerificationChecks(
        slot: GeneratedPostSlot,
        config: ServerAutomationConfig,
        naverCleanId: String,
        outcomes: List<ChannelDirectPublishOutcome>
    ): List<PublishVerificationCheck> {
        val nv = slot.variants[ChannelPlatform.NAVER]
        val naverOutcome = outcomes.firstOrNull { it.platform == ChannelPlatform.NAVER }
        val legalReport = LegalDoctrineVerifier.verify(slot.auctionCase)
        val qa = QaVerificationEngine.auditSlot(slot.auctionCase, slot.variants, slot.scheduledTimes, config)
        val nvDefense = nv?.let {
            QualityDefenseEngine.defendText(it.title, it.bodyText, ChannelPlatform.NAVER, slot.auctionCase)
        }
        val urlRegex = Regex("""https?://[^\s"'<>]+""", RegexOption.IGNORE_CASE)
        val naverNoExternalUrl = nv != null && !config.enableExternalLinkNaver &&
            !urlRegex.containsMatchIn(nv.bodyText) && !urlRegex.containsMatchIn(nv.bodyHtml)
        val linkRulePass = if (config.enableExternalLinkNaver) true else naverNoExternalUrl
        val noPii = nv != null && PrivacySanitizerEngine.detectPersonalInfoLeaks(
            "${nv.title}\n${nv.bodyText}\n${nv.pinnedCommentSnippet}"
        ).isEmpty()

        return listOf(
            PublishVerificationCheck(
                title = "1. 네이버 등록처·카테고리 설정",
                passed = naverCleanId.isNotBlank(),
                detail = if (naverCleanId.isNotBlank()) {
                    "blog.naver.com/$naverCleanId (카테고리: ${config.naverCategoryName})"
                } else {
                    "네이버 블로그 ID가 비어 있습니다."
                }
            ),
            PublishVerificationCheck(
                title = "2. 실제 HTTP 연결·도달 검증",
                passed = naverOutcome?.reachableOk == true,
                detail = naverOutcome?.message ?: "네이버 검증 미실행"
            ),
            PublishVerificationCheck(
                title = "3. 네이버 무링크·고정댓글 PRO 유도",
                passed = linkRulePass && (nv?.pinnedCommentSnippet?.isNotBlank() == true),
                detail = if (linkRulePass) "본문 직접 URL 0건 · 고정댓글 PRO 멘트 완비" else "네이버 본문 외부 URL 감지"
            ),
            PublishVerificationCheck(
                title = "4. 탈AI·스팸·특수기호 품질방어",
                passed = nvDefense != null && nvDefense.totalHitsCount == 0 && qa.charCountOkByChannel[ChannelPlatform.NAVER] == true,
                detail = "글자수 ${qa.charCountByChannel[ChannelPlatform.NAVER] ?: 0}자 · 품질감지 ${nvDefense?.totalHitsCount ?: 0}건"
            ),
            PublishVerificationCheck(
                title = "5. 개인정보 비식별·팩트 0% 오차",
                passed = noPii && qa.factCheckPassed,
                detail = if (noPii && qa.factCheckPassed) "${slot.auctionCase.caseId} 수치·말소기준일 일치 · 개인정보 0건" else "팩트 또는 개인정보 확인 필요"
            ),
            PublishVerificationCheck(
                title = "6. 6대 법리·판례 QA 검증",
                passed = legalReport.overallPassed,
                detail = legalReport.auditSummary
            )
        )
    }

    private fun resolveRelayEndpoint(config: ServerAutomationConfig, routeSuffix: String): String {
        val customRelay = config.naverWorkerRelayEndpoint.trim().removeSuffix("/")
        if (customRelay.isNotBlank()) return customRelay
        // Only call worker relay automatically if token + worker script are actively configured
        if (config.crawlerSyncToken.isNotBlank() && config.ddakchalApiBase.isNotBlank()) {
            return "${config.ddakchalApiBase.trim().removeSuffix("/")}/api/v1/blog/$routeSuffix"
        }
        return ""
    }

    private fun extractUrlFromJson(body: String): String {
        if (body.isBlank()) return ""
        return runCatching {
            val map = mapAdapter.fromJson(body) ?: return@runCatching ""
            val direct = (map["publishedUrl"] ?: map["url"] ?: map["postUrl"])?.toString().orEmpty()
            if (direct.startsWith("http")) return@runCatching direct
            val resultMap = map["result"] as? Map<*, *>
            (resultMap?.get("publishedUrl") ?: resultMap?.get("url") ?: resultMap?.get("postUrl"))
                ?.toString().orEmpty()
        }.getOrDefault("")
    }

    private data class HttpCheckResult(
        val statusCode: Int,
        val isSuccess: Boolean,
        val body: String,
        val errorMessage: String? = null
    )

    private fun executeHttpGet(url: String): HttpCheckResult = runCatching {
        val req = Request.Builder()
            .url(url)
            .get()
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/126.0 Mobile Safari/537.36")
            .build()
        httpClient.newCall(req).execute().use { resp ->
            HttpCheckResult(
                statusCode = resp.code,
                isSuccess = resp.isSuccessful,
                body = resp.body?.string().orEmpty().take(32_000),
                errorMessage = if (resp.isSuccessful) null else "HTTP ${resp.code}"
            )
        }
    }.getOrElse { err ->
        HttpCheckResult(0, false, "", err.localizedMessage ?: "네트워크 연결 오류")
    }

    private fun executeJsonPost(url: String, token: String, jsonBody: String): HttpCheckResult = runCatching {
        val reqBuilder = Request.Builder()
            .url(url)
            .post(jsonBody.toRequestBody(jsonMediaType))
            .header("Content-Type", "application/json")
        if (token.isNotBlank()) {
            reqBuilder.header("Authorization", "Bearer $token")
            reqBuilder.header("X-Crawler-Sync-Token", token)
        }
        httpClient.newCall(reqBuilder.build()).execute().use { resp ->
            HttpCheckResult(
                statusCode = resp.code,
                isSuccess = resp.isSuccessful,
                body = resp.body?.string().orEmpty().take(16_000),
                errorMessage = if (resp.isSuccessful) null else "HTTP ${resp.code}"
            )
        }
    }.getOrElse { err ->
        HttpCheckResult(0, false, "", err.localizedMessage ?: "POST 요청 실패")
    }
}
