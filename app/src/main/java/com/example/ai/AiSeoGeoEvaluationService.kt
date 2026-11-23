package com.example.ai

import android.util.Log
import com.example.engine.Article30PointEvaluator
import com.example.engine.CourtEvidenceRecorder
import com.example.engine.QaVerificationEngine
import com.example.model.AiAutoFixSuggestion
import com.example.model.AiEvaluationReport
import com.example.model.ChannelPlatform
import com.example.model.EvaluationCriterionResult
import com.example.model.GeneratedPostSlot
import com.example.model.ProFunnelConfig
import com.example.model.ServerAutomationConfig
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface AiEvaluationOutcome {
    data class Success(val report: AiEvaluationReport) : AiEvaluationOutcome
    data class Error(
        val message: String,
        val baselineScore: Int,
        val baselineSuggestions: List<AiAutoFixSuggestion>
    ) : AiEvaluationOutcome
}

interface AiSeoGeoAdvisor {
    suspend fun evaluateWith30Criteria(
        slot: GeneratedPostSlot,
        config: ServerAutomationConfig,
        proFunnel: ProFunnelConfig
    ): AiEvaluationOutcome
}

class FirebaseAiSeoGeoService : AiSeoGeoAdvisor {

    companion object {
        private const val TAG = "AiSeoGeoService"
        private const val MODEL_NAME = "gemini-3.8-flash"
    }

    private val structuredSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "aiSummary" to Schema.string("30개 SEO/GEO 항목 종합 진단 요약 (한국어 2문장)"),
                "overallVerdict" to Schema.string("등록 승인(95점 이상) 또는 보완 필요(95점 미만)"),
                "criterionScores" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "orderIndex" to Schema.integer("1~30번 평가 항목 번호"),
                            "passed" to Schema.boolean("항목 통과 여부"),
                            "earnedPoints" to Schema.integer("획득 점수"),
                            "reason" to Schema.string("간결한 판정 근거")
                        )
                    ),
                    description = "1번부터 30번까지 평가 결과 배열"
                ),
                "autoFixSuggestions" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "targetChannel" to Schema.enumeration(
                                values = listOf("NAVER", "TISTORY", "CLOUDFLARE"),
                                description = "수정 대상 채널"
                            ),
                            "criterionOrder" to Schema.integer("관련 평가 항목 번호 (1~30)"),
                            "issueTitle" to Schema.string("감지된 감점/저품질 요인 제목"),
                            "suggestedFix" to Schema.string("구체적인 자동 수정 가이드"),
                            "improvedSnippet" to Schema.string("실제로 본문이나 제목에 바로 반영할 개선 문구")
                        )
                    ),
                    description = "95점 미만 감점 항목이 있거나 품질 개선이 필요한 경우의 수정 제안 목록"
                )
            )
        )
    }

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = structuredSchema
            }
        )
    }

    override suspend fun evaluateWith30Criteria(
        slot: GeneratedPostSlot,
        config: ServerAutomationConfig,
        proFunnel: ProFunnelConfig
    ): AiEvaluationOutcome {
        val baseline = Article30PointEvaluator.evaluate(slot, config, proFunnel)
        val fallbackSuggestions = buildAutoFixSuggestionsFromFailures(slot, baseline.items)

        val archive = CourtEvidenceRecorder.recordFullArchive(slot.auctionCase)
        val nv = slot.variants[ChannelPlatform.NAVER]
        val ts = slot.variants[ChannelPlatform.TISTORY]
        val cf = slot.variants[ChannelPlatform.CLOUDFLARE]

        val criteriaPrompt = baseline.items.joinToString("\n") {
            "${it.orderIndex}. [${it.pillar.label}] ${it.title} (배점 ${it.maxPoints}점, 사전점검=${if (it.passed) "PASS" else "FAIL"}: ${it.detail})"
        }

        val prompt = """
            당신은 30년 업력 부동산 경매 중개법인의 수석 SEO·GEO·저품질 검증 에디터입니다.
            아래 경매 물건과 3채널 생성 원고를 사전 정의된 30개 SEO/GEO 평가 항목(총 100점) 기준으로 엄격하게 채점하고,
            95점 미만인 경우 감점 항목을 즉시 95점 이상으로 끌어올릴 구체적 수정 제안(autoFixSuggestions)을 JSON으로 출력하세요.

            [물건 및 법원 공식 기록]
            - 사건번호: ${slot.auctionCase.caseId} (${slot.auctionCase.courtName})
            - 소재지: ${slot.auctionCase.address}
            - 감정가/최저가: ${QaVerificationEngine.formatKrwShort(slot.auctionCase.appraisalPriceKrw)} / ${QaVerificationEngine.formatKrwShort(slot.auctionCase.minBidPriceKrw)} (${slot.auctionCase.discountRatePct}%)
            - 말소기준권리일: ${slot.auctionCase.baseRightDate} · 배당종기: ${slot.auctionCase.dividendDeadline}
            - 법원 문건/현장사진 기록: 사진 ${archive.photos.size}컷, 문건 ${archive.documents.size}종(매각물건명세서·현황조사서·감정평가서)
            - 권리분석 요약: ${archive.rightsVerdictSummary}

            [사전 30개 항목 규칙 체크 (현재 합계 ${baseline.totalScore}/100점)]
            $criteriaPrompt

            [생성된 3채널 원고 발췌]
            1) NAVER 제목: ${nv?.title.orEmpty()}
               NAVER 본문(앞 900자): ${nv?.bodyText?.take(900).orEmpty()}
               고정댓글: ${nv?.pinnedCommentSnippet.orEmpty()}
            2) TISTORY 제목: ${ts?.title.orEmpty()}
               TISTORY 본문(앞 700자): ${ts?.bodyText?.take(700).orEmpty()}
            3) CLOUDFLARE 제목: ${cf?.title.orEmpty()}
               CLOUDFLARE JSON-LD 포함여부: ${!cf?.jsonLdSchema.isNullOrBlank()}
               CLOUDFLARE 본문(앞 700자): ${cf?.bodyText?.take(700).orEmpty()}

            주의: 1~30번 각 항목의 earnedPoints 합계가 정확히 100점 만점 기준과 정합하도록 평가하고,
            AI 상용패턴(■, Q1., 알아보겠습니다), 이모지, 네이버 직접 URL, 중복도 30% 초과, 글자수 1,200자 미만 등이 발견되면 반드시 감점하고 autoFixSuggestions를 생성하세요.
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val rawJson = response.text ?: throw IllegalStateException("AI 모델이 빈 응답을 반환했습니다.")
            val root = JSONObject(rawJson)

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

            // Combine strict rule-verified items with AI diagnostics so score respects hard safety limits
            val mergedItems = baseline.items.map { base ->
                val aiEntry = aiByOrder[base.orderIndex]
                if (aiEntry == null) {
                    base
                } else {
                    val effectivePassed = base.passed && aiEntry.first
                    val boundedPts = if (effectivePassed) {
                        aiEntry.second.coerceIn(1, base.maxPoints)
                    } else {
                        aiEntry.second.coerceIn(0, (base.maxPoints - 1).coerceAtLeast(0))
                    }
                    base.copy(
                        passed = effectivePassed,
                        earnedPoints = if (base.passed && !effectivePassed) boundedPts else base.earnedPoints,
                        detail = if (aiEntry.third.isNotBlank()) "${base.detail} | AI: ${aiEntry.third}" else base.detail
                    )
                }
            }

            val totalScore = mergedItems.sumOf { it.earnedPoints }.coerceIn(0, 100)
            val passedCount = mergedItems.count { it.passed }

            val fixArr = root.optJSONArray("autoFixSuggestions")
            val parsedFixes = mutableListOf<AiAutoFixSuggestion>()
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
                        issueTitle = obj.optString("issueTitle", "SEO/GEO 품질 보완"),
                        suggestedFix = obj.optString("suggestedFix", ""),
                        improvedSnippet = obj.optString("improvedSnippet", "")
                    )
                }
            }

            val finalFixes = if (totalScore < Article30PointEvaluator.REGISTRATION_MIN_SCORE && parsedFixes.isEmpty()) {
                fallbackSuggestions
            } else {
                parsedFixes
            }

            AiEvaluationOutcome.Success(
                AiEvaluationReport(
                    caseId = slot.auctionCase.caseId,
                    modelName = MODEL_NAME,
                    totalScore = totalScore,
                    passedCount = passedCount,
                    aiSummary = root.optString("aiSummary", "30개 SEO/GEO 항목 검증을 완료했습니다."),
                    overallVerdict = root.optString(
                        "overallVerdict",
                        if (totalScore >= 95) "95점 이상 등록 승인" else "95점 미만 자동 수정 필요"
                    ),
                    criteriaResults = mergedItems,
                    autoFixSuggestions = finalFixes,
                    evaluatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI 30-point evaluation timed out", e)
            AiEvaluationOutcome.Error(
                message = "AI 채점 시간이 60초를 초과했습니다. 다시 시도해 주세요.",
                baselineScore = baseline.totalScore,
                baselineSuggestions = fallbackSuggestions
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI 30-point evaluation failed", e)
            AiEvaluationOutcome.Error(
                message = e.localizedMessage ?: "Firebase AI 연결 또는 App Check 인증 오류",
                baselineScore = baseline.totalScore,
                baselineSuggestions = fallbackSuggestions
            )
        }
    }

    fun buildAutoFixSuggestionsFromFailures(
        slot: GeneratedPostSlot,
        items: List<EvaluationCriterionResult>
    ): List<AiAutoFixSuggestion> {
        val failed = items.filter { !it.passed }
        if (failed.isEmpty()) return emptyList()
        val caseId = slot.auctionCase.caseId
        val baseDate = slot.auctionCase.baseRightDate

        return failed.map { crit ->
            when (crit.orderIndex) {
                7, 8 -> AiAutoFixSuggestion(
                    targetChannel = ChannelPlatform.NAVER,
                    criterionOrder = crit.orderIndex,
                    issueTitle = crit.title,
                    suggestedFix = "네이버 본문에서 AI 상용 기호(■, Q1~Q4, '알아보겠습니다')와 이모지를 제거하고 30년 베테랑 실사 에세이 어조로 치환합니다.",
                    improvedSnippet = "$caseId 현장을 직접 짚어 보면 핵심은 말소기준권리($baseDate)와 도로·점유 사실관계입니다."
                )
                9, 10 -> AiAutoFixSuggestion(
                    targetChannel = ChannelPlatform.NAVER,
                    criterionOrder = crit.orderIndex,
                    issueTitle = crit.title,
                    suggestedFix = "제목 맨 앞에 지역과 사건번호($caseId)를 선배치하고, 본문을 모바일 가독성에 맞게 10문단 이상으로 분리합니다.",
                    improvedSnippet = "${slot.auctionCase.address.substringBefore(" ")} $caseId 경매 현장실사 및 권리분석 리포트"
                )
                13, 14, 15, 16 -> AiAutoFixSuggestion(
                    targetChannel = ChannelPlatform.TISTORY,
                    criterionOrder = crit.orderIndex,
                    issueTitle = crit.title,
                    suggestedFix = "3채널 문장 구조와 어휘를 분리해 유사도를 30% 미만으로 낮추고 공백 제외 1,200~2,500자 분량을 맞춥니다.",
                    improvedSnippet = "감정평가서와 현황조사서 수치를 채널별 분석 관점(에세이·수익분석·구조화데이터)으로 재구성합니다."
                )
                17, 18 -> AiAutoFixSuggestion(
                    targetChannel = ChannelPlatform.NAVER,
                    criterionOrder = crit.orderIndex,
                    issueTitle = crit.title,
                    suggestedFix = "네이버 본문 내 외부 URL 링크를 제거하고, 티스토리 nofollow 및 자체웹 JSON-LD(Article/FAQPage) 스키마를 삽입합니다.",
                    improvedSnippet = "본문 하단은 외부 URL 없이 '네이버 검색창에 딱찰 $caseId 입력' 안내로 안전 전환합니다."
                )
                26, 27 -> AiAutoFixSuggestion(
                    targetChannel = ChannelPlatform.NAVER,
                    criterionOrder = crit.orderIndex,
                    issueTitle = crit.title,
                    suggestedFix = "고정댓글 검색 유도 멘트와 자체웹·티스토리 딱찰 PRO 티저 박스를 자동 삽입합니다.",
                    improvedSnippet = "고정댓글: '딱찰 $caseId' 검색 시 배당표·세무3안·입찰 시뮬레이션 리포트를 바로 확인하실 수 있습니다."
                )
                else -> AiAutoFixSuggestion(
                    targetChannel = ChannelPlatform.CLOUDFLARE,
                    criterionOrder = crit.orderIndex,
                    issueTitle = crit.title,
                    suggestedFix = "${crit.title} 기준(${crit.detail})을 충족하도록 자동 보완 리라이팅 및 등기부/사진 데이터를 보강합니다.",
                    improvedSnippet = "$caseId 기준 등기부 갑구/을구 팩트와 현장 사진 5컷 캡션을 자동 동기화합니다."
                )
            }
        }
    }
}
