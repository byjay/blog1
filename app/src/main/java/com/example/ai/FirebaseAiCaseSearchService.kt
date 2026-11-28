package com.example.ai

import android.util.Log
import com.example.engine.QaVerificationEngine
import com.example.model.AiCaseGeoOptimizationPack
import com.example.model.ChannelPlatform
import com.example.model.DdakchalBlogAnalysisData
import com.example.model.DdakchalQaPair
import com.example.model.GeneratedPostSlot
import com.example.model.ServerAutomationConfig
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface AiCaseSearchOutcome {
    data class Success(val pack: AiCaseGeoOptimizationPack) : AiCaseSearchOutcome
    data class Error(val message: String) : AiCaseSearchOutcome
}

interface AiCaseSearchAdvisor {
    suspend fun optimizeCaseForAiSearch(
        slot: GeneratedPostSlot,
        searchQuery: String
    ): AiCaseSearchOutcome

    suspend fun optimizeCaseForAiSearch(
        slot: GeneratedPostSlot,
        searchQuery: String,
        config: ServerAutomationConfig
    ): AiCaseSearchOutcome = optimizeCaseForAiSearch(slot, searchQuery)
}

class FirebaseAiCaseSearchService(
    private val grokFallbackClient: GrokFallbackClient = GrokFallbackClient()
) : AiCaseSearchAdvisor {

    private val geoSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "hookSnippet" to Schema.string("AI 검색(네이버 Cue·Google AI Overview·ChatGPT) 상단 인용용 1문장 핵심 요약"),
                "directAnswerParagraph" to Schema.string("사건번호·감정가·최저가·말소기준권리 실측치를 포함한 2~3문장 직접 답변 문단 (외부 URL 금지)"),
                "aiKeywords" to Schema.array(
                    items = Schema.string(),
                    description = "사용자가 AI 검색창에 입력하는 대화형 롱테일 검색어 5개"
                ),
                "faqPairs" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "question" to Schema.string("실제 입찰자가 AI에게 물어보는 핵심 질문"),
                            "answer" to Schema.string("제공된 실측 수치 기반의 명확한 답변 (외부 URL 금지)")
                        )
                    ),
                    description = "FAQPage 구조화 스키마용 Q&A 3쌍"
                ),
                "estimatedGeoScore" to Schema.integer("AI 검색 인용 및 GEO 노출 예상 점수 (70~100)")
            )
        )
    }

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = geoSchema
            }
        )
    }

    override suspend fun optimizeCaseForAiSearch(
        slot: GeneratedPostSlot,
        searchQuery: String
    ): AiCaseSearchOutcome = optimizeCaseForAiSearch(slot, searchQuery, ServerAutomationConfig())

    override suspend fun optimizeCaseForAiSearch(
        slot: GeneratedPostSlot,
        searchQuery: String,
        config: ServerAutomationConfig
    ): AiCaseSearchOutcome {
        val c = slot.auctionCase
        val appraisal = QaVerificationEngine.formatKrwShort(c.appraisalPriceKrw)
        val minBid = QaVerificationEngine.formatKrwShort(c.minBidPriceKrw)
        val realTrade = QaVerificationEngine.formatKrwShort(c.recentRealTradeMedianKrw)

        val prompt = """
            당신은 대한민국 부동산 경매 AI 검색엔진 최적화(GEO/AEO) 수석 에디터입니다.
            사용자가 네이버 Cue, Google AI Overview, Gemini, ChatGPT, Perplexity에서 아래 경매 사건을 검색할 때 우리 블로그 글이 핵심 근거 출처로 인용되도록 구조화 콘텐츠를 생성하세요.

            [엄격 준수 규칙]
            1. 아래 제공된 실측 수치(사건번호·감정가·최저가·유찰회수·말소기준일)를 1원/1일 오차 없이 유지하세요.
            2. http:// 또는 https:// 로 시작하는 외부 URL은 절대 포함하지 마세요.
            3. 과장/확정 수익 표현("무조건 낙찰", "100% 보장")을 쓰지 마세요.

            [사용자 검색어] $searchQuery
            [경매 실측 데이터]
            - 사건번호: ${c.caseId} (${c.courtName})
            - 소재지: ${c.address} (${c.propertyType.displayLabel})
            - 감정가: $appraisal / 최저가: $minBid (${c.discountRatePct}%, 유찰 ${c.bidCount}회)
            - 실거래 중위가: $realTrade / 권역 3개년 낙찰률: ${c.stats3yRatePct}%
            - 말소기준권리: ${c.baseRightDate} / 매각기일: ${c.auctionDate}
            - 점유·임차: ${c.possessionSummary} / ${c.tenantSummary}
            - 인수권리·특수체크: ${c.assumedRightsText} (${c.specialCheckNotice})
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val rawJson = response.text
                ?: return AiCaseSearchOutcome.Error("AI 사건검색 최적화 응답이 비어 있습니다.")

            val obj = JSONObject(rawJson)
            val hook = obj.optString("hookSnippet").ifBlank {
                "${c.address} ${c.caseId} 감정가 $appraisal 대비 최저가 $minBid(${c.discountRatePct}%) 권리분석 핵심 브리핑"
            }
            val directAnswer = obj.optString("directAnswerParagraph").ifBlank {
                "${c.courtName} ${c.caseId} 물건은 감정가 $appraisal, 최저매각가 $minBid(유찰 ${c.bidCount}회)이며 말소기준권리는 ${c.baseRightDate}입니다."
            }
            val kwArr = obj.optJSONArray("aiKeywords") ?: JSONArray()
            val keywords = (0 until kwArr.length())
                .mapNotNull { idx -> kwArr.optString(idx).trim().takeIf { it.isNotBlank() } }
                .ifEmpty {
                    listOf(
                        "${c.caseId} 경매 권리분석",
                        "${c.address.split(" ").take(2).joinToString(" ")} ${c.propertyType.displayLabel} 경매",
                        "${c.caseId} 말소기준권리 인수 여부",
                        "${c.caseId} 적정 입찰가 계산",
                        "${c.propertyType.displayLabel} 경매 주의사항"
                    )
                }

            val faqArr = obj.optJSONArray("faqPairs") ?: JSONArray()
            val faqs = (0 until faqArr.length()).mapNotNull { idx ->
                val f = faqArr.optJSONObject(idx) ?: return@mapNotNull null
                val q = f.optString("question").trim()
                val a = f.optString("answer").trim()
                if (q.isNotBlank() && a.isNotBlank()) DdakchalQaPair(q, a) else null
            }.ifEmpty {
                listOf(
                    DdakchalQaPair(
                        "${c.caseId} 물건의 최저매각가와 보증금은 얼마인가요?",
                        "최저매각가는 $minBid(감정가 대비 ${c.discountRatePct}%)이며 입찰보증금은 ${c.depositRatePct}% 기준입니다."
                    ),
                    DdakchalQaPair(
                        "${c.caseId} 매각에서 인수해야 할 권리가 있나요?",
                        "말소기준권리(${c.baseRightDate}) 기준으로 ${c.assumedRightsText} 여부를 매각물건명세서와 등기부로 최종 대조해야 합니다."
                    )
                )
            }

            val faqJsonLd = buildFaqAndListingJsonLd(slot, directAnswer, faqs)
            val geoScore = obj.optInt("estimatedGeoScore", 94).coerceIn(70, 100)
            val nowTs = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())

            val googleEmail = runCatching { Firebase.auth.currentUser?.email }.getOrNull()
            val primaryTag = if (!googleEmail.isNullOrBlank()) {
                "$MODEL_NAME (1순위 Google Gemini · $googleEmail)"
            } else {
                "$MODEL_NAME (1순위 Google Gemini)"
            }

            AiCaseSearchOutcome.Success(
                AiCaseGeoOptimizationPack(
                    caseId = c.caseId,
                    searchQuery = searchQuery,
                    hookSnippet = hook,
                    directAnswerParagraph = directAnswer,
                    aiKeywords = keywords,
                    faqPairs = faqs,
                    faqJsonLd = faqJsonLd,
                    estimatedGeoScore = geoScore,
                    modelUsed = primaryTag,
                    generatedAt = nowTs
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI case search GEO timed out", e)
            if (config.grokFallbackEnabled && grokFallbackClient.isConfigured()) {
                val backup = grokFallbackClient.optimizeCaseForAiSearchFallback(slot, searchQuery, config.grokModelName)
                if (backup is AiCaseSearchOutcome.Success) return backup
            }
            AiCaseSearchOutcome.Error("1순위 Google Gemini 시간 초과(60초). 다시 시도해 주세요.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI case search failed", e)
            if (config.grokFallbackEnabled && grokFallbackClient.isConfigured()) {
                val backup = grokFallbackClient.optimizeCaseForAiSearchFallback(slot, searchQuery, config.grokModelName)
                if (backup is AiCaseSearchOutcome.Success) return backup
            }
            AiCaseSearchOutcome.Error("AI 사건검색 최적화 오류: ${e.localizedMessage ?: "1순위 Google Gemini 호출 실패"}")
        }
    }

    companion object {
        private const val TAG = "AiCaseSearchService"
        private const val MODEL_NAME = "gemini-3.8-flash"
        private val urlStripRegex = Regex("""https?://[^\s"'<>]+""", RegexOption.IGNORE_CASE)

        fun buildFaqAndListingJsonLd(
            slot: GeneratedPostSlot,
            directAnswer: String,
            faqs: List<DdakchalQaPair>
        ): String {
            val c = slot.auctionCase
            val mainEntity = JSONArray().apply {
                faqs.forEach { qa ->
                    put(
                        JSONObject().apply {
                            put("@type", "Question")
                            put("name", qa.question.replace(urlStripRegex, ""))
                            put(
                                "acceptedAnswer",
                                JSONObject().apply {
                                    put("@type", "Answer")
                                    put("text", qa.answer.replace(urlStripRegex, ""))
                                }
                            )
                        }
                    )
                }
            }
            val articleObj = JSONObject().apply {
                put("@type", "Article")
                put("headline", "${c.caseId} ${c.address} ${c.propertyType.displayLabel} 경매 AI 권리분석 리포트")
                put("datePublished", "2026-11-22T08:17:00+09:00")
                put("author", JSONObject().apply {
                    put("@type", "Organization")
                    put("name", "딱찰(DDAKCHAL)")
                })
                put("about", c.address)
            }
            val faqObj = JSONObject().apply {
                put("@type", "FAQPage")
                put("name", "${c.caseId} ${c.address} 경매 AI 권리분석 Q&A")
                put("description", directAnswer.replace(urlStripRegex, ""))
                put("mainEntity", mainEntity)
            }
            return JSONObject().apply {
                put("@context", "https://schema.org")
                put("@graph", JSONArray().apply {
                    put(articleObj)
                    put(faqObj)
                })
            }.toString(2)
        }

        fun applyGeoPackToSlot(
            slot: GeneratedPostSlot,
            pack: AiCaseGeoOptimizationPack,
            config: ServerAutomationConfig
        ): GeneratedPostSlot {
            val cleanHook = pack.hookSnippet.replace(urlStripRegex, "").trim()
            val cleanDirect = pack.directAnswerParagraph.replace(urlStripRegex, "").trim()
            val faqSectionText = buildString {
                appendLine()
                appendLine("[AI 검색 답변 요약 · 핵심 Q&A]")
                appendLine(cleanHook)
                appendLine(cleanDirect)
                pack.faqPairs.forEach { qa ->
                    appendLine("Q. ${qa.question.replace(urlStripRegex, "")}")
                    appendLine("A. ${qa.answer.replace(urlStripRegex, "")}")
                }
            }.trim()

            val updatedVariants = slot.variants.mapValues { (platform, variant) ->
                val headerLabel = when (platform) {
                    ChannelPlatform.NAVER -> "[핵심 질의응답 브리핑]"
                    ChannelPlatform.TISTORY -> "[실전 권리·입찰 문답 정리]"
                    ChannelPlatform.CLOUDFLARE -> "[AI 검색 요약 · 핵심 Q&A]"
                }
                val topFaqs = pack.faqPairs.take(2)
                val channelFaqSnippet = buildString {
                    appendLine(headerLabel)
                    if (platform == ChannelPlatform.CLOUDFLARE) {
                        appendLine(cleanHook)
                    } else if (platform == ChannelPlatform.TISTORY) {
                        appendLine(cleanDirect)
                    }
                    topFaqs.forEach { qa ->
                        val qClean = qa.question.replace(urlStripRegex, "").trim()
                        val aClean = qa.answer.replace(urlStripRegex, "").trim()
                        appendLine("질문: $qClean")
                        appendLine("답변: $aClean")
                    }
                }.trim()

                val alreadyHasFaq = variant.bodyText.contains("[핵심 질의응답") ||
                    variant.bodyText.contains("[실전 권리·입찰") ||
                    variant.bodyText.contains("[AI 검색 요약")
                val mergedBodyText = if (alreadyHasFaq) {
                    variant.bodyText
                } else {
                    val candidate = "${variant.bodyText}\n\n$channelFaqSnippet"
                    if (candidate.count { !it.isWhitespace() } <= 2420) {
                        candidate
                    } else {
                        val compactSnippet = buildString {
                            appendLine(headerLabel)
                            topFaqs.take(1).forEach { qa ->
                                appendLine("질문: ${qa.question.replace(urlStripRegex, "").trim()}")
                                append("답변: ${qa.answer.replace(urlStripRegex, "").trim()}")
                            }
                        }.trim()
                        if ("${variant.bodyText}\n\n$compactSnippet".count { !it.isWhitespace() } <= 2480) {
                            "${variant.bodyText}\n\n$compactSnippet"
                        } else {
                            variant.bodyText
                        }
                    }
                }
                val mergedBodyHtml = if (variant.bodyHtml.contains("faq-geo")) {
                    variant.bodyHtml
                } else {
                    val faqHtml = pack.faqPairs.joinToString("") { qa ->
                        "<p><strong>Q. ${qa.question}</strong><br/>A. ${qa.answer}</p>"
                    }
                    "${variant.bodyHtml}<section class='faq-geo'><h2>AI 검색 핵심 답변</h2><p>$cleanDirect</p>$faqHtml</section>"
                }
                val safeHtml = if (platform == ChannelPlatform.NAVER && !config.enableExternalLinkNaver) {
                    mergedBodyHtml.replace(urlStripRegex, "#")
                } else {
                    mergedBodyHtml
                }
                val safeText = if (platform == ChannelPlatform.NAVER && !config.enableExternalLinkNaver) {
                    mergedBodyText.replace(urlStripRegex, "[프로필 링크 참조]")
                } else {
                    mergedBodyText
                }
                val mergedTags = (variant.tags + pack.aiKeywords.map { it.replace(" ", "") })
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .take(8)
                    .let { list -> if (list.size >= 5) list else (list + variant.tags).distinct().take(8) }
                val safeJsonLd = if (platform == ChannelPlatform.NAVER && !config.enableExternalLinkNaver) {
                    variant.jsonLdSchema
                } else if (pack.faqJsonLd.contains("\"@type\": \"Article\"") && pack.faqJsonLd.contains("\"@type\": \"FAQPage\"")) {
                    pack.faqJsonLd
                } else {
                    buildFaqAndListingJsonLd(slot, cleanDirect, pack.faqPairs)
                }
                variant.copy(
                    bodyText = safeText,
                    bodyHtml = safeHtml,
                    metaDescription = cleanDirect.take(150).ifBlank { variant.metaDescription },
                    tags = mergedTags,
                    jsonLdSchema = safeJsonLd
                )
            }

            val updatedAnalysis = (slot.ddakchalAnalysisData ?: DdakchalBlogAnalysisData()).copy(
                hookHeadline = cleanHook,
                summary3Lines = listOf(cleanHook, cleanDirect) + slot.ddakchalAnalysisData?.summary3Lines.orEmpty().take(1),
                qaBlocks = pack.faqPairs
            )

            return slot.copy(
                variants = updatedVariants,
                ddakchalAnalysisData = updatedAnalysis
            )
        }
    }
}
