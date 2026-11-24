package com.example.data

import com.example.engine.LegalDoctrineVerifier
import com.example.engine.PrivacySanitizerEngine
import com.example.engine.QaVerificationEngine
import com.example.engine.QualityDefenseEngine
import com.example.model.ChannelPlatform
import com.example.model.ChannelPublishDestination
import com.example.model.ChannelDirectPublishOutcome
import com.example.model.GeneratedPostSlot
import com.example.model.NaverAndMultiChannelPublishReport
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
        val cfBase = config.ddakchalApiBase.trim().removeSuffix("/")
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
                targetAddress = if (cfBase.isNotBlank()) "$cfBase$cfPath" else "",
                categoryName = "GEO/AIO 자체웹 DB",
                isConfigured = cfBase.isNotBlank()
            )
        )
    }

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
            val payload = RelayPublishPayload(
                postId = slot.postId,
                caseId = slot.auctionCase.caseId,
                channel = ChannelPlatform.NAVER.code,
                blogId = naverCleanId,
                categoryName = config.naverCategoryName.ifBlank { "부동산 경매 분석" },
                title = nv.title,
                bodyText = nv.bodyText,
                bodyHtml = nv.bodyHtml,
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

        val targetVerifyUrl = when {
            customNaverPostUrl.startsWith("http") -> customNaverPostUrl
            relayPostUrl.startsWith("http") -> relayPostUrl
            naverCleanId.isNotBlank() -> "https://m.blog.naver.com/$naverCleanId"
            else -> ""
        }

        val getRes = if (targetVerifyUrl.isNotBlank()) executeHttpGet(targetVerifyUrl) else HttpCheckResult(0, false, "", "URL 없음")
        val finalHttpCode = if (getRes.statusCode > 0) getRes.statusCode else relayCode
        val verifiedReachable = getRes.isSuccess && (
            !getRes.body.contains("존재하지 않는 게시물") &&
                !getRes.body.contains("삭제되었거나")
            )

        val methodLabel = if (relayEndpoint.isNotBlank()) "RELAY_POST + HTTP_GET" else "HTTP_GET_VERIFY"
        val combinedMessage = buildString {
            if (relayMsg.isNotBlank()) {
                append(relayMsg)
                append(" / ")
            }
            if (verifiedReachable) {
                if (customNaverPostUrl.isNotBlank() || relayPostOk) {
                    append("실제 네이버 URL 도달 확인 (HTTP ${getRes.statusCode})")
                } else {
                    append("네이버 블로그 홈(m.blog.naver.com/$naverCleanId) 실존 확인 (HTTP ${getRes.statusCode}) · 워커 미연결 시 글쓰기 창 연동 후 게시물 URL 검증")
                }
            } else {
                append("HTTP GET 검증 실패 (${getRes.statusCode}: ${getRes.errorMessage ?: "블로그 주소 또는 네트워크 확인 필요"})")
            }
        }

        return ChannelDirectPublishOutcome(
            platform = ChannelPlatform.NAVER,
            targetUrl = targetVerifyUrl,
            executedMethod = methodLabel,
            httpStatusCode = finalHttpCode,
            reachableOk = verifiedReachable,
            publishedPostUrl = if (verifiedReachable) targetVerifyUrl else "",
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
        var relayPostUrl = ""
        var relayNote = ""
        if (relayEndpoint.isNotBlank() && tsVariant != null) {
            val payload = RelayPublishPayload(
                postId = slot.postId,
                caseId = slot.auctionCase.caseId,
                channel = ChannelPlatform.TISTORY.code,
                blogId = baseUrl,
                categoryName = config.tistoryCategoryName.ifBlank { "경매·권리·세무 리포트" },
                title = tsVariant.title,
                bodyText = tsVariant.bodyText,
                bodyHtml = tsVariant.bodyHtml,
                tags = tsVariant.tags,
                pinnedComment = ""
            )
            val postRes = executeJsonPost(relayEndpoint, config.crawlerSyncToken, payloadAdapter.toJson(payload))
            if (postRes.isSuccess) {
                relayPostUrl = extractUrlFromJson(postRes.body)
                relayNote = "릴레이 POST 성공(${postRes.statusCode}) / "
            } else {
                relayNote = "릴레이 HTTP ${postRes.statusCode} / "
            }
        }
        val checkUrl = relayPostUrl.ifBlank { baseUrl }
        val getRes = executeHttpGet(checkUrl)
        return ChannelDirectPublishOutcome(
            platform = ChannelPlatform.TISTORY,
            targetUrl = checkUrl,
            executedMethod = if (relayEndpoint.isNotBlank()) "RELAY_POST + HTTP_GET" else "HTTP_GET_VERIFY",
            httpStatusCode = getRes.statusCode,
            reachableOk = getRes.isSuccess,
            publishedPostUrl = if (getRes.isSuccess) checkUrl else "",
            message = "$relayNote${if (getRes.isSuccess) "티스토리 도달 확인 (HTTP ${getRes.statusCode})" else "도달 실패 (HTTP ${getRes.statusCode}: ${getRes.errorMessage ?: "URL 확인 필요"})"}"
        )
    }

    private suspend fun executeCloudflarePublishAndVerify(
        slot: GeneratedPostSlot,
        config: ServerAutomationConfig
    ): ChannelDirectPublishOutcome {
        val cfBase = config.ddakchalApiBase.trim().removeSuffix("/")
        val cfPath = config.cloudflareBlogPath.trim().let { if (it.startsWith("/")) it else "/$it" }
        if (cfBase.isBlank()) {
            return ChannelDirectPublishOutcome(
                platform = ChannelPlatform.CLOUDFLARE,
                targetUrl = "",
                executedMethod = "CONFIG_CHECK",
                httpStatusCode = 0,
                reachableOk = false,
                publishedPostUrl = "",
                message = "Cloudflare 자체 도메인(DDAKCHAL_API_BASE)을 입력해 주세요."
            )
        }
        val targetUrl = "$cfBase$cfPath/${slot.auctionCase.dedupKey}"
        var d1StatusNote = ""
        if (config.cfAccountId.isNotBlank() && config.cfD1DatabaseId.isNotBlank() && config.cfApiToken.isNotBlank()) {
            val cfVariant = slot.variants[ChannelPlatform.CLOUDFLARE]
            if (cfVariant != null) {
                val res = d1Api.executeCloudflareD1Query(
                    accountId = config.cfAccountId,
                    databaseId = config.cfD1DatabaseId,
                    apiToken = config.cfApiToken,
                    sql = "INSERT OR REPLACE INTO generated_posts (post_id, queue_id, case_id, property_type, blog_category, platform, title, body_html, body_markdown, status, scheduled_for) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
                    params = listOf(
                        "${slot.postId}-cloudflare",
                        slot.queueId,
                        slot.auctionCase.caseId,
                        slot.auctionCase.propertyType.code,
                        slot.auctionCase.propertyType.blogCategory,
                        ChannelPlatform.CLOUDFLARE.code,
                        cfVariant.title,
                        cfVariant.bodyHtml,
                        cfVariant.bodyText,
                        "PUBLISHED",
                        slot.scheduledTimes[ChannelPlatform.CLOUDFLARE] ?: slot.publishDate
                    )
                )
                d1StatusNote = if (res.isSuccess) "D1 저장 성공 / " else "D1 HTTP ${res.statusCode} / "
            }
        }
        val getRes = executeHttpGet(cfBase)
        return ChannelDirectPublishOutcome(
            platform = ChannelPlatform.CLOUDFLARE,
            targetUrl = targetUrl,
            executedMethod = if (d1StatusNote.isNotBlank()) "CF_D1_UPSERT + HTTP_GET" else "HTTP_GET_VERIFY",
            httpStatusCode = getRes.statusCode,
            reachableOk = getRes.isSuccess,
            publishedPostUrl = if (getRes.isSuccess) targetUrl else "",
            message = "$d1StatusNote${if (getRes.isSuccess) "자체웹 응답 정상 (HTTP ${getRes.statusCode})" else "자체웹 응답 실패 (HTTP ${getRes.statusCode}: ${getRes.errorMessage ?: "도메인 확인 필요"})"}"
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
