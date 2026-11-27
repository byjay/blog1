package com.example.engine

import com.example.model.BlogPostTemplate
import com.example.model.ChannelPlatform
import com.example.model.ExposurePotentialGrade
import com.example.model.GeneratedPostSlot
import com.example.model.KeywordBlogExposureReport
import com.example.model.LegalDoctrineReport
import com.example.model.ProFunnelConfig
import com.example.model.ServerAutomationConfig
import com.example.model.TopicCandidateRanking
import com.example.model.TopicSelectionBoardState
import com.example.model.TopicSortCriteria
import com.example.model.WarehouseArticle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Evaluates and ranks auction blog topic candidates across 4 pillars (25 pts each = 100 pts):
 * 1) 글의 품질 (Article Quality & Fact Completeness)
 * 2) SEO 적합성 (Cross-channel Uniqueness, Quality Defense, Link/Title/Template Compliance)
 * 3) AI 노출 적합성 (GEO/AEO Q&A Structure, FAQ JSON-LD, Quantitative Citation, Keyword Exposure)
 * 4) 인기 있는 매물 (Demand, Clicks, Discount Rate, Real-Trade Safety Margin, Legal Clarity)
 */
object TopicSelectionEngine {

    private val QA_BLOCK_REGEX = Regex("""(Q\d|질문|자주\s*묻는\s*질문|\?\s*\n)""")

    fun rankCandidates(
        slots: List<GeneratedPostSlot>,
        config: ServerAutomationConfig = ServerAutomationConfig(),
        proFunnel: ProFunnelConfig = ProFunnelConfig(),
        sortMode: TopicSortCriteria = TopicSortCriteria.BALANCED_4_PILLAR,
        naverKeywordDiagnosis: KeywordBlogExposureReport? = null,
        nowText: String = currentTimestamp(),
        warehouseArticles: List<WarehouseArticle> = emptyList(),
        postTemplates: List<BlogPostTemplate> = emptyList(),
        legalByCaseId: Map<String, LegalDoctrineReport> = emptyMap(),
        recent7DayCorpus: List<String> = emptyList()
    ): TopicSelectionBoardState = evaluateAndRankTopics(
        slots = slots,
        warehouseArticles = warehouseArticles,
        config = config,
        proFunnel = proFunnel,
        postTemplates = postTemplates,
        legalByCaseId = legalByCaseId,
        keywordReport = naverKeywordDiagnosis,
        recent7DayCorpus = recent7DayCorpus,
        sortMode = sortMode,
        nowText = nowText
    )

    fun evaluateAndRankTopics(
        slots: List<GeneratedPostSlot>,
        warehouseArticles: List<WarehouseArticle> = emptyList(),
        config: ServerAutomationConfig = ServerAutomationConfig(),
        proFunnel: ProFunnelConfig = ProFunnelConfig(),
        postTemplates: List<BlogPostTemplate> = emptyList(),
        legalByCaseId: Map<String, LegalDoctrineReport> = emptyMap(),
        keywordReport: KeywordBlogExposureReport? = null,
        recent7DayCorpus: List<String> = emptyList(),
        sortMode: TopicSortCriteria = TopicSortCriteria.BALANCED_4_PILLAR,
        nowText: String = currentTimestamp()
    ): TopicSelectionBoardState {
        // Deduplicate by caseId, prioritizing active queue slots first, then warehouse slots
        val uniqueByCase = linkedMapOf<String, GeneratedPostSlot>()
        slots.forEach { slot ->
            uniqueByCase[slot.auctionCase.caseId] = slot
        }
        warehouseArticles.forEach { wh ->
            if (!uniqueByCase.containsKey(wh.slot.auctionCase.caseId)) {
                uniqueByCase[wh.slot.auctionCase.caseId] = wh.slot
            }
        }

        if (uniqueByCase.isEmpty()) {
            return TopicSelectionBoardState(
                sortMode = sortMode,
                candidates = emptyList(),
                topPickedPostIds = emptySet(),
                lastEvaluatedAt = nowText,
                summaryBanner = "선정 가능한 경매 물건 풀이 비어 있습니다. 딱찰 피드 동기화 또는 물건 생성을 실행해 주세요."
            )
        }

        val rawScored = uniqueByCase.values.map { slot ->
            scoreSingleCandidate(
                slot = slot,
                config = config,
                proFunnel = proFunnel,
                postTemplates = postTemplates,
                legalReport = legalByCaseId[slot.auctionCase.caseId],
                keywordReport = keywordReport,
                recent7DayCorpus = recent7DayCorpus
            )
        }

        val sorted = when (sortMode) {
            TopicSortCriteria.BALANCED_4_PILLAR -> rawScored.sortedWith(
                compareByDescending<TopicCandidateRanking> { it.totalTopicScore }
                    .thenByDescending { it.popularityScore }
                    .thenByDescending { it.seoFitScore }
            )
            TopicSortCriteria.QUALITY_FIRST -> rawScored.sortedWith(
                compareByDescending<TopicCandidateRanking> { it.qualityScore * 3 + it.totalTopicScore }
            )
            TopicSortCriteria.SEO_FIRST -> rawScored.sortedWith(
                compareByDescending<TopicCandidateRanking> { it.seoFitScore * 3 + it.totalTopicScore }
            )
            TopicSortCriteria.AI_GEO_FIRST -> rawScored.sortedWith(
                compareByDescending<TopicCandidateRanking> { it.aiExposureScore * 3 + it.totalTopicScore }
            )
            TopicSortCriteria.POPULARITY_FIRST -> rawScored.sortedWith(
                compareByDescending<TopicCandidateRanking> { it.popularityScore * 3 + it.totalTopicScore }
            )
        }.mapIndexed { idx, candidate ->
            candidate.copy(rank = idx + 1)
        }

        val topPickedIds = sorted.take(3).map { it.postId }.toSet()
        val top1 = sorted.firstOrNull()
        val banner = if (top1 != null) {
            "최우선 추천 #1: ${top1.caseId} (${top1.totalTopicScore}점 · ${top1.tierLabel}) — ${top1.recommendedAngle}"
        } else {
            "후보 분석 완료"
        }

        return TopicSelectionBoardState(
            sortMode = sortMode,
            candidates = sorted,
            topPickedPostIds = topPickedIds,
            lastEvaluatedAt = nowText,
            summaryBanner = banner
        )
    }

    fun scoreSingleCandidate(
        slot: GeneratedPostSlot,
        config: ServerAutomationConfig = ServerAutomationConfig(),
        proFunnel: ProFunnelConfig = ProFunnelConfig(),
        postTemplates: List<BlogPostTemplate> = emptyList(),
        legalReport: LegalDoctrineReport? = null,
        keywordReport: KeywordBlogExposureReport? = null,
        recent7DayCorpus: List<String> = emptyList()
    ): TopicCandidateRanking {
        val auction = slot.auctionCase
        val resolvedLegal = legalReport ?: LegalDoctrineVerifier.verify(auction)
        val regTemplate = RegistryInfographicEngine.buildTemplate(auction)
        val eval30 = Article30PointEvaluator.evaluate(
            slot = slot,
            config = config,
            proFunnel = proFunnel,
            registryTemplate = regTemplate,
            recent7DayCorpus = recent7DayCorpus
        )
        val attractiveness = InvestmentAttractivenessEngine.score(auction, resolvedLegal)
        val proReport = ProAnalysisAndPhotoGuideEngine.buildProReport(auction)

        val nv = slot.variants[ChannelPlatform.NAVER]
        val ts = slot.variants[ChannelPlatform.TISTORY]
        val cf = slot.variants[ChannelPlatform.CLOUDFLARE]
        val analysis = slot.ddakchalAnalysisData

        // 1) 글의 품질 (0..25)
        val articleEvalPts = ((eval30.totalScore / 100.0) * 18.0).roundToInt().coerceIn(0, 18)
        val legalPurityPts = when {
            resolvedLegal.overallPassed && resolvedLegal.criticalBlockCount == 0 -> 4
            resolvedLegal.criticalBlockCount == 0 -> 2
            else -> 0
        }
        val courtPhotoCount = slot.courtPhotoUrls.size.coerceAtLeast(analysis?.freshness?.courtPhotoCount ?: 0)
        val visualEvidencePts = when {
            courtPhotoCount >= 3 -> 3
            courtPhotoCount >= 1 -> 2
            (nv?.infographicCards?.size ?: 0) >= 4 -> 1
            else -> 0
        }
        val qualityScore = (articleEvalPts + legalPurityPts + visualEvidencePts).coerceIn(0, 25)

        // 2) SEO 적합성 (0..25)
        val uniquenessPts = when {
            slot.qaReport.similarityPassed -> 8
            slot.qaReport.maxCrossChannelCosinePct < 40.0 -> 4
            else -> 1
        }
        val defenseCleanPts = when (slot.qaReport.forbiddenHits.size) {
            0 -> 6
            1 -> 3
            else -> 0
        }
        val linkHygienePts = if (slot.qaReport.externalLinkRuleOk) 5 else 1
        val nvTitle = nv?.title.orEmpty()
        val hasTitleCaseAnchor = nvTitle.contains(auction.caseId)
        val titleLenOk = nvTitle.length in 16..58
        val templateReady = postTemplates.any {
            it.propertyType == auction.propertyType && it.isActiveForGeneration
        }
        val titleTemplatePts = ((if (hasTitleCaseAnchor) 2 else 0) +
            (if (titleLenOk) 2 else 1) +
            (if (templateReady || postTemplates.isEmpty()) 2 else 1)).coerceIn(0, 6)
        val seoFitScore = (uniquenessPts + defenseCleanPts + linkHygienePts + titleTemplatePts).coerceIn(0, 25)

        // 3) AI 노출 적합성 GEO/AEO (0..25)
        val qaBlockCount = analysis?.qaBlocks?.size ?: 0
        val bodyQaHits = QA_BLOCK_REGEX.findAll(nv?.bodyText.orEmpty() + "\n" + cf?.bodyText.orEmpty()).count()
        val qaPts = when {
            qaBlockCount >= 3 || bodyQaHits >= 4 -> 7
            qaBlockCount >= 1 || bodyQaHits >= 2 -> 4
            else -> 1
        }
        val hasDirectSummary = (analysis?.summary3Lines?.size ?: 0) >= 2 ||
            analysis?.hookHeadline?.isNotBlank() == true ||
            (cf?.metaDescription?.length ?: 0) >= 40
        val summaryPts = if (hasDirectSummary) 6 else 3

        val cfJsonLd = cf?.jsonLdSchema.orEmpty()
        val hasFaqSchema = cfJsonLd.contains("FAQPage", ignoreCase = true) || cfJsonLd.contains("schema.org", ignoreCase = true)
        val hasExactMetrics = auction.appraisalPriceKrw > 0L && auction.minBidPriceKrw > 0L && auction.recentRealTradeMedianKrw > 0L
        val schemaAndDataPts = ((if (hasFaqSchema) 4 else 1) + (if (hasExactMetrics) 3 else 1)).coerceIn(0, 7)

        val keywordBonusPts = if (keywordReport != null &&
            (keywordReport.keyword.contains(auction.caseId) ||
                auction.address.split(" ").any { token -> token.length >= 2 && keywordReport.keyword.contains(token) })
        ) {
            when (keywordReport.grade) {
                ExposurePotentialGrade.BLUE_OCEAN -> 5
                ExposurePotentialGrade.COMPETITIVE_WINNABLE -> 4
                ExposurePotentialGrade.CROWDED_NICHE -> 3
                ExposurePotentialGrade.SATURATED -> 1
            }
        } else {
            val regionTokens = auction.address.split(" ").count { it.length >= 2 }
            if (regionTokens >= 2 && auction.caseId.isNotBlank()) 4 else 2
        }
        val aiExposureScore = (qaPts + summaryPts + schemaAndDataPts + keywordBonusPts).coerceIn(0, 25)

        // 4) 인기 있는 매물 (0..25)
        val investPts = ((attractiveness.totalScore / 100.0) * 11.0).roundToInt().coerceIn(0, 11)
        val clickPts = when {
            auction.clicksLast7d >= 700 -> 6
            auction.clicksLast7d >= 500 -> 4
            auction.clicksLast7d >= 300 -> 3
            else -> 1
        }
        val dropPts = when {
            auction.bidCount >= 2 || auction.discountRatePct <= 64 -> 4
            auction.bidCount >= 1 || auction.discountRatePct <= 80 -> 3
            else -> 1
        }
        val marginPts = when {
            proReport.safetyMarginPct >= 20.0 -> 4
            proReport.safetyMarginPct >= 8.0 -> 3
            proReport.safetyMarginKrw > 0L -> 2
            else -> 0
        }
        val popularityScore = (investPts + clickPts + dropPts + marginPts).coerceIn(0, 25)

        val totalScore = (qualityScore + seoFitScore + aiExposureScore + popularityScore).coerceIn(0, 100)
        val tierLabel = when {
            totalScore >= 88 -> "S급 최우선 글감"
            totalScore >= 76 -> "A급 추천 글감"
            totalScore >= 64 -> "B급 보완 후 발행"
            else -> "C급 데이터 보강 필요"
        }

        val isFromLiveServer = slot.sourceLabel.contains("실서버") || slot.ddakchalAnalysisData != null
        val tags = buildList {
            if (qualityScore >= 21) add("품질95+")
            if (seoFitScore >= 21) add("SEO무결")
            if (aiExposureScore >= 20) add("AI인용최적")
            if (popularityScore >= 20) add("인기급상승")
            if (isFromLiveServer) add("딱찰실서버")
            if (courtPhotoCount > 0) add("법원사진${courtPhotoCount}컷")
        }

        val angle = buildRecommendedAngle(
            auction = auction,
            safetyMarginPct = proReport.safetyMarginPct,
            legalClear = resolvedLegal.overallPassed && resolvedLegal.criticalBlockCount == 0,
            aiExposureScore = aiExposureScore,
            popularityScore = popularityScore
        )

        val reasonSummary = "품질 $qualityScore/25 · SEO $seoFitScore/25 · AI노출 $aiExposureScore/25 · 인기 $popularityScore/25 " +
            "(조회 ${auction.clicksLast7d} · 마진 ${proReport.safetyMarginPct}% · 30항목 ${eval30.totalScore}점)"

        return TopicCandidateRanking(
            postId = slot.postId,
            caseId = auction.caseId,
            address = auction.address,
            propertyTypeLabel = auction.propertyType.displayLabel,
            rank = 0,
            totalTopicScore = totalScore,
            qualityScore = qualityScore,
            seoFitScore = seoFitScore,
            aiExposureScore = aiExposureScore,
            popularityScore = popularityScore,
            tierLabel = tierLabel,
            selectionReasonSummary = reasonSummary,
            recommendedAngle = angle,
            tags = tags,
            isFromLiveServer = isFromLiveServer
        )
    }

    private fun buildRecommendedAngle(
        auction: com.example.model.AuctionCaseIntel,
        safetyMarginPct: Double,
        legalClear: Boolean,
        aiExposureScore: Int,
        popularityScore: Int
    ): String {
        val marginStr = String.format(Locale.KOREA, "%.1f%%", safetyMarginPct)
        return when {
            popularityScore >= 20 && legalClear ->
                "안전마진 $marginStr · 인수위험 없음 강조 (실수요·투자자 클릭 유도형)"
            aiExposureScore >= 21 ->
                "사건번호 FAQ·JSON-LD 스니펫 전면 배치 (AI 브리핑 인용 공략형)"
            auction.bidCount >= 2 ->
                "${auction.bidCount}회 유찰 최저가율 ${auction.discountRatePct}% 반값 메리트+명도 체크형"
            else ->
                "${auction.propertyType.displayLabel} 권리분석·실거래 3전략 입찰가 비교형"
        }
    }

    private fun currentTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())
}
