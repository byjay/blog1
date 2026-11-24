package com.example.model

enum class LegalStudyCategory(val label: String) {
    CIVIL_GENERAL("민법총칙"),
    CIVIL_SPECIAL("민사특별법"),
    AUCTION_DIVIDEND("경매권리·배당"),
    PUBLIC_REGISTRY("부동산공시·등기"),
    URBAN_REDEVELOPMENT("도시정비·환권"),
    REAL_ESTATE_TAX("부동산세법")
}

data class LegalStudyTerm(
    val codeId: String,
    val category: LegalStudyCategory,
    val termTitle: String,
    val keywords: List<String>,
    val statuteRef: String,
    val coreDoctrine: String,
    val practicalTrap: String,
    val userQuestionExample: String,
    val blogStudyOneLiner: String,
    val searchTags: List<String>,
    val apiJsonSnippet: String
)

data class InvestmentAttractivenessScore(
    val caseId: String,
    val totalScore: Int,
    val gradeLabel: String,
    val priceCompetitivenessPts: Int,
    val dropDiscountPts: Int,
    val rightsClarityPts: Int,
    val stats3yPts: Int,
    val yieldCostPts: Int,
    val demandSearchPts: Int,
    val selectionVerdict: String,
    val disclaimer: String = "본 점수는 수익을 보장하는 지표가 아닌 콘텐츠 선정 및 투자 검토 보조 지표입니다."
)

data class CompetitorBlogBenchmarkRow(
    val blogName: String,
    val weeklyPostsStat: String,
    val primaryFocus: String,
    val ddakchalSuperiority: String
)

enum class PostAuctionOutcomeType(val label: String, val badge: String) {
    WINNING_BID("낙찰(매각 확정)", "낙찰 확정"),
    FAILED_DROP("유찰(차회 최저가 하락)", "유찰 재입찰"),
    RESCHEDULED("기일 변경·연기", "기일 변경"),
    WITHDRAWN("취하·기각 종결", "사건 취하")
}

data class PostAuctionFollowUpRecord(
    val originalCaseId: String,
    val originalAuctionDate: String,
    val isPastDue: Boolean,
    val outcomeType: PostAuctionOutcomeType,
    val outcomeDate: String,
    val winningPriceKrw: Long? = null,
    val winningRatePct: Double? = null,
    val bidderCount: Int = 0,
    val nextAuctionDate: String? = null,
    val nextMinBidKrw: Long? = null,
    val predictionDiffNote: String,
    val followUpSummary: String,
    val followUpPostId: String? = null,
    val recordedAt: String
)

data class ChannelPublishDestination(
    val platform: ChannelPlatform,
    val enabled: Boolean,
    val targetAddress: String,
    val categoryName: String,
    val isConfigured: Boolean
)

data class ChannelDirectPublishOutcome(
    val platform: ChannelPlatform,
    val targetUrl: String,
    val executedMethod: String,
    val httpStatusCode: Int,
    val reachableOk: Boolean,
    val publishedPostUrl: String,
    val message: String
)

data class PublishVerificationCheck(
    val title: String,
    val passed: Boolean,
    val detail: String
)

data class NaverAndMultiChannelPublishReport(
    val caseId: String,
    val attemptedAt: String,
    val naverDirectPassed: Boolean,
    val selectedChannelCount: Int,
    val verifiedNaverPostUrl: String,
    val naverWriteAssistUrl: String,
    val outcomes: List<ChannelDirectPublishOutcome>,
    val verificationChecks: List<PublishVerificationCheck>,
    val overallSummary: String
)

data class WeeklyBatchRunSummary(
    val generatedAt: String,
    val dailyCount: Int,
    val totalDays: Int,
    val totalGeneratedCount: Int,
    val score95PlusCount: Int,
    val autoFixedCount: Int,
    val serverSyncedCount: Int,
    val ddakchalLinked: Boolean,
    val summaryText: String
)

