package com.example.ai

import com.example.BuildConfig
import com.example.engine.Article30PointEvaluator
import com.example.engine.CourtEvidenceRecorder
import com.example.engine.QaVerificationEngine
import com.example.model.AiAutoFixSuggestion
import com.example.model.AiCaseGeoOptimizationPack
import com.example.model.AiEvaluationReport
import com.example.model.ChannelPlatform
import com.example.model.DdakchalQaPair
import com.example.model.GeneratedPostSlot
import com.example.model.ProFunnelConfig
import com.example.model.ServerAutomationConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Priority #2 Secondary AI Backup Engine (xAI Grok API).
 * Priority #1 is always the user's Google Account + Firebase AI Logic (Google Gemini).
 * Uses BuildConfig.GROK_API_KEY configured via .env / AI Studio Secrets panel.
 */
class GrokFallbackClient(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val apiKeyProvider: () -> String = { BuildConfig.GROK_API_KEY },
    private val endpointUrl: String = "https://api.x.ai/v1/chat/completions"
) {
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    fun isConfigured(): Boolean {
        val key = apiKeyProvider().trim()
        return key.isNotBlank() &&
            !key.contains("DEFAULT_VALUE", ignoreCase = true) &&
            !key.startsWith("REPLACE_", ignoreCase = true)
    }

    suspend fun evaluateWith30CriteriaFallback(
        slot: GeneratedPostSlot,
        config: ServerAutomationConfig,
        proFunnel: ProFunnelConfig
    ): AiEvaluationOutcome = withContext(ioDispatcher) {
        val baseline = Article30PointEvaluator.evaluate(slot, config, proFunnel)
        val fallbackSuggestions = FirebaseAiSeoGeoService().buildAutoFixSuggestionsFromFailures(slot, baseline.items)
        val apiKey = apiKeyProvider().trim()
        if (!isConfigured()) {
            return@withContext AiEvaluationOutcome.Error(
                message = "GROK_API_KEY가 AI Studio Secrets(.env)에 미설정 상태입니다.",
                baselineScore = baseline.totalScore,
                baselineSuggestions = fallbackSuggestions
            )
        }

        val archive = CourtEvidenceRecorder.recordFullArchive(slot.auctionCase)
        val nv = slot.variants[ChannelPlatform.NAVER]
        val modelName = config.grokModelName.ifBlank { "grok-3-mini" }

        val systemPrompt = """
            당신은 대한민국 부동산 경매 블로그 SEO/GEO 수석 검증관입니다.
            반드시 JSON 객체 하나만 반환하세요. 키는 aiSummary(string), overallVerdict(string), criterionScores(array of {orderIndex, passed, earnedPoints, reason}), autoFixSuggestions(array of {targetChannel, criterionOrder, issueTitle, suggestedFix, improvedSnippet}) 입니다.
        """.trimIndent()

        val userPrompt = """
            사건번호: ${slot.auctionCase.caseId} (${slot.auctionCase.courtName})
            소재지: ${slot.auctionCase.address}
            사전 규칙 점수: ${baseline.totalScore}/100 (${baseline.passedCount}/30 통과)
            감정가/최저가: ${QaVerificationEngine.formatKrwShort(slot.auctionCase.appraisalPriceKrw)} / ${QaVerificationEngine.formatKrwShort(slot.auctionCase.minBidPriceKrw)}
            권리요약: ${archive.rightsVerdictSummary}
            네이버 제목: ${nv?.title.orEmpty()}
            네이버 본문 앞 600자: ${nv?.bodyText?.take(600).orEmpty()}
            감점 보완 포함 JSON으로 응답하세요.
        """.trimIndent()

        return@withContext try {
            val contentJson = callChatCompletion(
                apiKey = apiKey,
                modelName = modelName,
                systemPrompt = systemPrompt,
                userPrompt = userPrompt
            )
            val root = JSONObject(extractJsonBlock(contentJson))
            val aiScoreArr = root.optJSONArray("criterionScores")
            val aiByOrder = mutableMapOf<Int, Triple<Boolean, Int, String>>()
            if (aiScoreArr != null) {
                for (i in 0 until aiScoreArr.length()) {
                    val obj = aiScoreArr.optJSONObject(i) ?: continue
                    val order = obj.optInt("orderIndex", -1)
                    if (order in 1..30) {
                        aiByOrder[order] = Triple(
                            obj.optBoolean("passed", true),
                            obj.optInt("earnedPoints", 0),
                            obj.optString("reason", "")
                        )
                    }
                }
            }
            val mergedItems = baseline.items.map { base ->
                val aiEntry = aiByOrder[base.orderIndex] ?: return@map base
                val effectivePassed = base.passed && aiEntry.first
                val boundedPts = if (effectivePassed) {
                    aiEntry.second.coerceIn(1, base.maxPoints)
                } else {
                    aiEntry.second.coerceIn(0, (base.maxPoints - 1).coerceAtLeast(0))
                }
                base.copy(
                    passed = effectivePassed,
                    earnedPoints = if (base.passed && !effectivePassed) boundedPts else base.earnedPoints,
                    detail = if (aiEntry.third.isNotBlank()) "${base.detail} | Grok: ${aiEntry.third}" else base.detail
                )
            }
            val totalScore = mergedItems.sumOf { it.earnedPoints }.coerceIn(0, 100)
            val parsedFixes = mutableListOf<AiAutoFixSuggestion>()
            val fixArr = root.optJSONArray("autoFixSuggestions")
            if (fixArr != null) {
                for (i in 0 until fixArr.length()) {
                    val obj = fixArr.optJSONObject(i) ?: continue
                    val ch = when (obj.optString("targetChannel", "NAVER").uppercase()) {
                        "TISTORY" -> ChannelPlatform.TISTORY
                        "CLOUDFLARE" -> ChannelPlatform.CLOUDFLARE
                        else -> ChannelPlatform.NAVER
                    }
                    parsedFixes += AiAutoFixSuggestion(
                        targetChannel = ch,
                        criterionOrder = obj.optInt("criterionOrder", 1),
                        issueTitle = obj.optString("issueTitle", "SEO/GEO 보완"),
                        suggestedFix = obj.optString("suggestedFix", ""),
                        improvedSnippet = obj.optString("improvedSnippet", "")
                    )
                }
            }
            val finalFixes = if (totalScore < Article30PointEvaluator.REGISTRATION_MIN_SCORE && parsedFixes.isEmpty()) {
                fallbackSuggestions
            } else parsedFixes

            AiEvaluationOutcome.Success(
                AiEvaluationReport(
                    caseId = slot.auctionCase.caseId,
                    modelName = "$modelName (2순위 Grok API 보조)",
                    totalScore = totalScore,
                    passedCount = mergedItems.count { it.passed },
                    aiSummary = root.optString("aiSummary", "Google Gemini 백업으로 Grok API가 30개 항목 검증을 완료했습니다."),
                    overallVerdict = root.optString(
                        "overallVerdict",
                        if (totalScore >= 95) "95점 이상 등록 승인" else "95점 미만 자동 수정 필요"
                    ),
                    criteriaResults = mergedItems,
                    autoFixSuggestions = finalFixes,
                    evaluatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())
                )
            )
        } catch (e: Exception) {
            AiEvaluationOutcome.Error(
                message = "2순위 Grok API 보조 검증 실패: ${e.localizedMessage ?: "응답 오류"}",
                baselineScore = baseline.totalScore,
                baselineSuggestions = fallbackSuggestions
            )
        }
    }

    suspend fun optimizeCaseForAiSearchFallback(
        slot: GeneratedPostSlot,
        searchQuery: String,
        modelName: String = "grok-3-mini"
    ): AiCaseSearchOutcome = withContext(ioDispatcher) {
        val apiKey = apiKeyProvider().trim()
        if (!isConfigured()) {
            return@withContext AiCaseSearchOutcome.Error("GROK_API_KEY 미설정")
        }
        val c = slot.auctionCase
        val appraisal = QaVerificationEngine.formatKrwShort(c.appraisalPriceKrw)
        val minBid = QaVerificationEngine.formatKrwShort(c.minBidPriceKrw)
        val resolvedModel = modelName.ifBlank { "grok-3-mini" }

        val systemPrompt = """
            당신은 부동산 경매 AI 검색(GEO/AEO) 최적화 전문가입니다.
            반드시 순수 JSON 객체 하나만 반환하세요. 키: hookSnippet(string), directAnswerParagraph(string), aiKeywords(string array 5개), faqPairs(array of {question, answer}), estimatedGeoScore(int 70..100).
            http:// 또는 https:// 로 시작하는 외부 URL을 포함하지 마세요.
        """.trimIndent()
        val userPrompt = """
            검색어: $searchQuery
            사건번호: ${c.caseId} (${c.courtName})
            소재지: ${c.address} (${c.propertyType.displayLabel})
            감정가/최저가: $appraisal / $minBid (${c.discountRatePct}%, 유찰 ${c.bidCount}회)
            말소기준권리: ${c.baseRightDate}, 매각기일: ${c.auctionDate}
            점유·임차: ${c.possessionSummary} / ${c.tenantSummary}
        """.trimIndent()

        return@withContext try {
            val contentJson = callChatCompletion(
                apiKey = apiKey,
                modelName = resolvedModel,
                systemPrompt = systemPrompt,
                userPrompt = userPrompt
            )
            val obj = JSONObject(extractJsonBlock(contentJson))
            val hook = obj.optString("hookSnippet").ifBlank {
                "${c.address} ${c.caseId} 감정가 $appraisal 대비 최저가 $minBid(${c.discountRatePct}%) 핵심 분석"
            }
            val direct = obj.optString("directAnswerParagraph").ifBlank {
                "${c.courtName} ${c.caseId} 물건은 최저가 $minBid(유찰 ${c.bidCount}회)이며 말소기준권리는 ${c.baseRightDate}입니다."
            }
            val kwArr = obj.optJSONArray("aiKeywords") ?: JSONArray()
            val keywords = (0 until kwArr.length())
                .mapNotNull { idx -> kwArr.optString(idx).trim().takeIf { it.isNotBlank() } }
                .ifEmpty { listOf("${c.caseId} 경매 권리분석", "${c.propertyType.displayLabel} 경매 입찰가") }
            val faqArr = obj.optJSONArray("faqPairs") ?: JSONArray()
            val faqs = (0 until faqArr.length()).mapNotNull { idx ->
                val f = faqArr.optJSONObject(idx) ?: return@mapNotNull null
                val q = f.optString("question").trim()
                val a = f.optString("answer").trim()
                if (q.isNotBlank() && a.isNotBlank()) DdakchalQaPair(q, a) else null
            }.ifEmpty {
                listOf(
                    DdakchalQaPair(
                        "${c.caseId} 최저매각가는 얼마인가요?",
                        "감정가 $appraisal 대비 최저매각가는 $minBid(${c.discountRatePct}%)입니다."
                    )
                )
            }
            val faqJsonLd = FirebaseAiCaseSearchService.buildFaqAndListingJsonLd(slot, direct, faqs)
            val nowTs = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())

            AiCaseSearchOutcome.Success(
                AiCaseGeoOptimizationPack(
                    caseId = c.caseId,
                    searchQuery = searchQuery,
                    hookSnippet = hook,
                    directAnswerParagraph = direct,
                    aiKeywords = keywords,
                    faqPairs = faqs,
                    faqJsonLd = faqJsonLd,
                    estimatedGeoScore = obj.optInt("estimatedGeoScore", 92).coerceIn(70, 100),
                    modelUsed = "$resolvedModel (2순위 Grok API 보조)",
                    generatedAt = nowTs
                )
            )
        } catch (e: Exception) {
            AiCaseSearchOutcome.Error("2순위 Grok API 보조 호출 오류: ${e.localizedMessage ?: "네트워크 오류"}")
        }
    }

    private fun callChatCompletion(
        apiKey: String,
        modelName: String,
        systemPrompt: String,
        userPrompt: String
    ): String {
        val bodyObj = JSONObject().apply {
            put("model", modelName)
            put("temperature", 0.2)
            put("response_format", JSONObject().apply { put("type", "json_object") })
            put(
                "messages",
                JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userPrompt)
                    })
                }
            )
        }
        val request = Request.Builder()
            .url(endpointUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(bodyObj.toString().toRequestBody(jsonMedia))
            .build()

        httpClient.newCall(request).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw IllegalStateException("Grok HTTP ${resp.code}: ${raw.take(180)}")
            }
            val root = JSONObject(raw)
            val choices = root.optJSONArray("choices")
            val msgContent = choices?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
                .orEmpty()
            if (msgContent.isBlank()) {
                throw IllegalStateException("Grok API 응답 content가 비어 있습니다.")
            }
            return msgContent
        }
    }

    private fun extractJsonBlock(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) return trimmed
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        return if (start >= 0 && end > start) trimmed.substring(start, end + 1) else trimmed
    }
}
