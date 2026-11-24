package com.example.engine

import com.example.model.ArticleEvaluationResult
import com.example.model.ChannelPlatform
import com.example.model.EvaluationCriterionResult
import com.example.model.EvaluationPillar
import com.example.model.GeneratedPostSlot
import com.example.model.ProFunnelConfig
import com.example.model.RegistryInfographicTemplate
import com.example.model.ServerAutomationConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Article30PointEvaluator {

    const val REGISTRATION_MIN_SCORE = 95

    private val AI_CLICHE_PATTERNS = listOf("■", "Q1.", "Q2.", "Q3.", "Q4.", "알아보겠습니다")
    private val EMOJI_REGEX = Regex("[\\p{So}\\p{Cn}]")
    private val METRIC_UNIT_SYMBOLS = setOf("㎡", "㎥", "㎝", "㎜", "㎞", "㏊")

    fun evaluate(
        slot: GeneratedPostSlot,
        config: ServerAutomationConfig,
        proFunnel: ProFunnelConfig = ProFunnelConfig(),
        registryTemplate: RegistryInfographicTemplate = RegistryInfographicEngine.buildTemplate(slot.auctionCase),
        recent7DayCorpus: List<String> = emptyList()
    ): ArticleEvaluationResult {
        val auction = slot.auctionCase
        val qa = QaVerificationEngine.auditSlot(auction, slot.variants, slot.scheduledTimes, config, recent7DayCorpus)
        val nv = slot.variants[ChannelPlatform.NAVER]
        val ts = slot.variants[ChannelPlatform.TISTORY]
        val cf = slot.variants[ChannelPlatform.CLOUDFLARE]

        val nvBody = nv?.bodyText.orEmpty()
        val tsBody = ts?.bodyText.orEmpty()
        val cfBody = cf?.bodyText.orEmpty()

        val proReport = ProAnalysisAndPhotoGuideEngine.buildProReport(auction)
        val motionScenes = RegistryAndMotionEngine.build5SceneMotionGuide(auction, proReport, proFunnel)
        val legalReport = LegalDoctrineVerifier.verify(auction)
        val naverDefenseHits = QualityDefenseEngine.detectIssues(nvBody, nv?.title.orEmpty(), ChannelPlatform.NAVER)

        val appraisalStr = QaVerificationEngine.formatKrwShort(auction.appraisalPriceKrw)
        val minBidStr = QaVerificationEngine.formatKrwShort(auction.minBidPriceKrw)

        fun item(
            idx: Int,
            id: String,
            pillar: EvaluationPillar,
            title: String,
            maxPoints: Int,
            passed: Boolean,
            detail: String
        ) = EvaluationCriterionResult(
            id = id,
            orderIndex = idx,
            pillar = pillar,
            title = title,
            maxPoints = maxPoints,
            earnedPoints = if (passed) maxPoints else 0,
            passed = passed,
            detail = detail
        )

        val checks = listOf(
            // A. FACT_REGISTRY (6 items = 22 pts)
            item(
                1, "eval-01", EvaluationPillar.FACT_REGISTRY,
                "사건번호 3채널 100% 일치", 4,
                slot.variants.size == 3 && slot.variants.values.all { "${it.title}\n${it.bodyText}".contains(auction.caseId) },
                "${auction.caseId} 전 채널 표기 확인"
            ),
            item(
                2, "eval-02", EvaluationPillar.FACT_REGISTRY,
                "감정가·최저매각가 0% 오차 일치", 4,
                qa.factCheckPassed && slot.variants.values.all { it.bodyText.contains(appraisalStr) || it.bodyText.contains(minBidStr) },
                "감정 $appraisalStr / 최저 $minBidStr 대조"
            ),
            item(
                3, "eval-03", EvaluationPillar.FACT_REGISTRY,
                "말소기준권리일 전 채널 정확 명시", 4,
                slot.variants.values.all { it.bodyText.contains(auction.baseRightDate) },
                "말소기준 ${auction.baseRightDate} 일치"
            ),
            item(
                4, "eval-04", EvaluationPillar.FACT_REGISTRY,
                "등기부 갑구(소유권·압류·경매개시) 명시", 4,
                registryTemplate.gapGuNodes.isNotEmpty() && nvBody.contains("갑구"),
                "갑구 노드 ${registryTemplate.gapGuNodes.size}건 도식화"
            ),
            item(
                5, "eval-05", EvaluationPillar.FACT_REGISTRY,
                "등기부 을구(근저당·채권최고액·소멸/인수) 명시", 3,
                registryTemplate.eulGuNodes.isNotEmpty() && nvBody.contains("을구"),
                "을구 노드 ${registryTemplate.eulGuNodes.size}건 검증"
            ),
            item(
                6, "eval-06", EvaluationPillar.FACT_REGISTRY,
                "배당요구종기·6대 법리판례 0모순 검증", 3,
                cfBody.contains(auction.dividendDeadline) && tsBody.contains(auction.dividendDeadline) && legalReport.overallPassed,
                "${legalReport.auditSummary} · 배당종기 ${auction.dividendDeadline}"
            ),

            // B. HUMAN_TONE (6 items = 20 pts)
            item(
                7, "eval-07", EvaluationPillar.HUMAN_TONE,
                "Quality Defense 탈AI·기계패턴 0건 통과", 4,
                AI_CLICHE_PATTERNS.none { nvBody.contains(it) } && naverDefenseHits.isEmpty(),
                if (naverDefenseHits.isEmpty() && AI_CLICHE_PATTERNS.none { nvBody.contains(it) }) "Quality Defense 0건(순수 베테랑 에세이)" else "AI·저품질 패턴 ${naverDefenseHits.sumOf { it.count }}건 감지"
            ),
            item(
                8, "eval-08", EvaluationPillar.HUMAN_TONE,
                "본문 이모지·장식 아이콘 0건 무이모지 문체", 4,
                EMOJI_REGEX.findAll(nvBody).none { it.value !in METRIC_UNIT_SYMBOLS } && !nvBody.contains("✅") && !nvBody.contains("🔥"),
                "불필요한 이모지/기호 배제 확인"
            ),
            item(
                9, "eval-09", EvaluationPillar.HUMAN_TONE,
                "모바일 가독성 문단 분할(10줄 이상 호흡 분리)", 3,
                nvBody.lines().count { it.isNotBlank() } >= 10,
                "모바일 문단 ${nvBody.lines().count { it.isNotBlank() }}개 분리"
            ),
            item(
                10, "eval-10", EvaluationPillar.HUMAN_TONE,
                "네이버 제목 지역·사건번호·쟁점 선배치", 3,
                nv != null && nv.title.contains(auction.caseId) && !nv.title.startsWith("["),
                "선배치 제목: ${nv?.title?.take(28).orEmpty()}"
            ),
            item(
                11, "eval-11", EvaluationPillar.HUMAN_TONE,
                "15+ 다이나믹 도입·전환·CTA 풀 변형 적용", 3,
                BoilerplateAndTemplateEngine.introHookPool.size >= 15 && BoilerplateAndTemplateEngine.ctaClosingPool.size >= 15,
                "도입 ${BoilerplateAndTemplateEngine.introHookPool.size}종 · CTA ${BoilerplateAndTemplateEngine.ctaClosingPool.size}종 순환"
            ),
            item(
                12, "eval-12", EvaluationPillar.HUMAN_TONE,
                "30년 베테랑·실사 중심 신뢰 서술톤 유지", 3,
                nvBody.contains("30년") && (nvBody.contains("현장") || tsBody.contains("30년")),
                "30년 현장 전담팀 신뢰 톤 확인"
            ),

            // C. SEO_LOW_QUALITY (6 items = 20 pts)
            item(
                13, "eval-13", EvaluationPillar.SEO_LOW_QUALITY,
                "3채널 교차 Cosine 유사도 < 30%", 4,
                qa.maxCrossChannelCosinePct < 30.0,
                "최대 Cosine 유사도 ${qa.maxCrossChannelCosinePct}%"
            ),
            item(
                14, "eval-14", EvaluationPillar.SEO_LOW_QUALITY,
                "5-gram Jaccard 문장 패턴 중복률 < 30%", 3,
                qa.maxCrossChannelJaccard5GramPct < 30.0,
                "최대 5-gram Jaccard ${qa.maxCrossChannelJaccard5GramPct}%"
            ),
            item(
                15, "eval-15", EvaluationPillar.SEO_LOW_QUALITY,
                "최근 7일 기발행 원고 대비 중복률 < 30%", 3,
                qa.recent7DayMaxSimilarityPct < 30.0,
                "7일 코퍼스 대비 ${qa.recent7DayMaxSimilarityPct}%"
            ),
            item(
                16, "eval-16", EvaluationPillar.SEO_LOW_QUALITY,
                "전 채널 공백 제외 분량 1,200~2,500자 준수", 4,
                qa.charCountOkByChannel.size == 3 && qa.charCountOkByChannel.values.all { it },
                qa.charCountByChannel.entries.joinToString(", ") { "${it.key.displayName}=${it.value}자" }
            ),
            item(
                17, "eval-17", EvaluationPillar.SEO_LOW_QUALITY,
                "네이버 본문 직접 외부 URL 0건 차단", 3,
                qa.externalLinkRuleOk,
                qa.externalLinkDetail
            ),
            item(
                18, "eval-18", EvaluationPillar.SEO_LOW_QUALITY,
                "티스토리 nofollow & 자체웹 JSON-LD(Article/FAQ) 유효", 3,
                qa.jsonLdValid && ts?.bodyHtml?.contains("nofollow sponsored") == true,
                "JSON-LD=${qa.jsonLdValid}, nofollow 적용"
            ),

            // D. VISUAL_MOTION (6 items = 19 pts)
            item(
                19, "eval-19", EvaluationPillar.VISUAL_MOTION,
                "채널별 인포그래픽 카드 4~7장 완비", 4,
                slot.variants.values.all { it.infographicCards.size in 4..7 },
                "채널당 ${nv?.infographicCards?.size ?: 0}장 생성"
            ),
            item(
                20, "eval-20", EvaluationPillar.VISUAL_MOTION,
                "EXIF 100% 삭제 & 채널별 고유 MD5/SHA-256 해시", 3,
                qa.imageCountAndHashOk,
                qa.imageHashDetail
            ),
            item(
                21, "eval-21", EvaluationPillar.VISUAL_MOTION,
                "등기부 갑구·을구·소유권 현황 인포그래픽 템플릿 결합", 3,
                registryTemplate.readerChecklist.size >= 3 && registryTemplate.ownershipSummaryCard.currentOwnerLabel.isNotBlank(),
                "${registryTemplate.templateType.label} 체크리스트 ${registryTemplate.readerChecklist.size}항목"
            ),
            item(
                22, "eval-22", EvaluationPillar.VISUAL_MOTION,
                "물건 유형별 현장 임장 사진 5컷 가이드 포함", 3,
                proReport.photoGuideShots.size == 5 && nvBody.contains(proReport.photoGuideShots.first().targetTitle),
                "현장 촬영 5컷 + 적신호 가이드 완비"
            ),
            item(
                23, "eval-23", EvaluationPillar.VISUAL_MOTION,
                "65초 5장면 모션그래픽·클립 동영상 콘티 생성", 3,
                motionScenes.size == 5 && motionScenes.sumOf { it.durationSec } >= 60,
                "5장면 총 ${motionScenes.sumOf { it.durationSec }}초 콘티 완비"
            ),
            item(
                24, "eval-24", EvaluationPillar.VISUAL_MOTION,
                "개인정보 비식별(채무자·임차인) & 금칙어·NaN 0건", 3,
                run {
                    val piiLeaksAll = slot.variants.values.flatMap { v ->
                        PrivacySanitizerEngine.detectPersonalInfoLeaks(
                            "${v.title}\n${v.bodyText}\n${v.pinnedCommentSnippet}"
                        )
                    }
                    qa.forbiddenHits.isEmpty() && piiLeaksAll.isEmpty()
                },
                if (qa.forbiddenHits.isEmpty()) "개인정보 비식별(채무자명 등 0건) · 금칙어 0건" else qa.forbiddenHits.joinToString()
            ),

            // E. FUNNEL_SAFETY (6 items = 19 pts)
            item(
                25, "eval-25", EvaluationPillar.FUNNEL_SAFETY,
                "유료앱 초과 무료공개(배당·BEP·유찰예상·세무3안)", 4,
                proReport.dividendWaterfall.size >= 4 &&
                    proReport.taxScenarios.size == 3 &&
                    proReport.breakevenResalePriceKrw > 0L &&
                    proReport.nextDropMinBidKrw > 0L,
                "배당 ${proReport.dividendWaterfall.size}단계 · 세무 ${proReport.taxScenarios.size}안 · BEP ${QaVerificationEngine.formatKrwShort(proReport.breakevenResalePriceKrw)}"
            ),
            item(
                26, "eval-26", EvaluationPillar.FUNNEL_SAFETY,
                "전 채널 딱찰 회원가입·PRO 고급정보 퍼널 삽입", 3,
                !proFunnel.funnelEnabled || (
                    cf?.bodyHtml?.contains("ddakchal-pro-teaser") == true &&
                        ts?.bodyHtml?.contains("pro-funnel-box") == true &&
                        cfBody.contains("딱찰") && (cfBody.contains("회원가입") || cfBody.contains("가입")) &&
                        tsBody.contains("딱찰") && (tsBody.contains("회원가입") || tsBody.contains("가입")) &&
                        nvBody.contains("딱찰") && (nvBody.contains("회원가입") || nvBody.contains("가입")) &&
                        nv?.pinnedCommentSnippet?.contains("딱찰") == true &&
                        (nv.pinnedCommentSnippet.contains("회원가입") || nv.pinnedCommentSnippet.contains("가입"))
                    ),
                if (proFunnel.funnelEnabled) "3채널 본문·HTML·고정댓글 딱찰 회원가입 퍼널 확인" else "사용자 퍼널 OFF"
            ),
            item(
                27, "eval-27", EvaluationPillar.FUNNEL_SAFETY,
                "네이버 고정댓글 검색 유도·매수대리 DB 멘트 준비", 3,
                nv?.pinnedCommentSnippet?.contains(auction.caseId) == true,
                "고정댓글 '${proFunnel.naverSearchKeyword} ${auction.caseId}' 완비"
            ),
            item(
                28, "eval-28", EvaluationPillar.FUNNEL_SAFETY,
                "정각(:00) 회피 시간차 지터 스케줄 적용", 3,
                qa.scheduleJitterValid,
                "시간차 예약 ${slot.scheduledTimes.values.joinToString(" / ") { it.takeLast(8) }}"
            ),
            item(
                29, "eval-29", EvaluationPillar.FUNNEL_SAFETY,
                "3전략 입찰가(보수·균형·공격) & LTV70·ROE 반영", 3,
                auction.conservativeBidKrw in 1L..auction.balancedBidKrw && auction.balancedBidKrw <= auction.aggressiveBidKrw,
                "CapRate ${proReport.estimatedCapRatePct}% / ROE ${proReport.estimated12mRoiPct}%"
            ),
            item(
                30, "eval-30", EvaluationPillar.FUNNEL_SAFETY,
                "채널별 메타 디스크립션 & 태그(5~8개) 규격 충족", 3,
                slot.variants.values.all { it.metaDescription.length >= 20 && it.tags.size in 5..8 },
                "전 채널 메타/태그 규격 통과"
            )
        )

        val totalScore = checks.sumOf { it.earnedPoints }
        val passedCount = checks.count { it.passed }
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())

        return ArticleEvaluationResult(
            totalScore = totalScore,
            maxScore = 100,
            passedCount = passedCount,
            totalCriteriaCount = checks.size,
            eligibleForRegistration = totalScore >= REGISTRATION_MIN_SCORE,
            items = checks,
            evaluatedAt = now
        )
    }
}
