package com.example.model

enum class PropertyType(
    val code: String,
    val displayLabel: String,
    val blogCategory: String,
    val yamlFileName: String,
    val focusSummary: String,
    val seoKeywordPattern: String
) {
    APT_OFFICETEL(
        code = "APT_OFFICETEL",
        displayLabel = "아파트·오피스텔",
        blogCategory = "주거용 경매 분석",
        yamlFileName = "apt_officetel.yaml",
        focusSummary = "국토부 실거래가 대비 시세차익(안전마진), 학군·역세권, 대법원 2001다8677 공용관리비 체납(3년 원금), 임차인 대항력·배당요구",
        seoKeywordPattern = "{지역} 아파트 경매, {단지명} 낙찰가"
    ),
    VILLA_MULTIFAMILY(
        code = "VILLA_MULTIFAMILY",
        displayLabel = "빌라·다세대·다가구",
        blogCategory = "소액투자·빌라 경매",
        yamlFileName = "villa_multifamily.yaml",
        focusSummary = "전세사기·선순위 임차인 미배당 인수 리스크, 다가구 전입세대 호수 대조, 개별공시지가 비율, 모아타운·재개발 호재, 위반건축물",
        seoKeywordPattern = "{지역} 빌라 경매, 소액 경매 물건"
    ),
    COMMERCIAL_RETAIL(
        code = "COMMERCIAL_RETAIL",
        displayLabel = "상가·근린시설",
        blogCategory = "수익형·상가 경매",
        yamlFileName = "commercial_retail.yaml",
        focusSummary = "상임법 환산보증금·대항력, 예상 임대수익률(CapRate), 반경 500m 상권·공실 리스크, 장기공실 체납 공용관리비, 클릭/관심도 급증 지표, HBU 3안",
        seoKeywordPattern = "{지역} 상가 경매, 수익형 부동산"
    ),
    LAND_FARMLAND(
        code = "LAND_FARMLAND",
        displayLabel = "토지·임야·농지",
        blogCategory = "토지·지분 경매",
        yamlFileName = "land_farmland.yaml",
        focusSummary = "지목, 용도지역(건폐율/용적률), 맹지 여부(4m 도로 접면), 농지취득자격증명(미제출 시 보증금 몰수), 법정지상권·분묘기지권 체크",
        seoKeywordPattern = "{지역} 토지 경매, 농지취득자격증명"
    ),
    FACTORY_SPECIAL(
        code = "FACTORY_SPECIAL",
        displayLabel = "공장·창고·특수물건",
        blogCategory = "특수·지식산업센터",
        yamlFileName = "factory_special.yaml",
        focusSummary = "공장저당법 제6조 기계기구 목록 실물 대조, 유치권 신고 파훼, 산업단지 입주자격(업종코드), 층고(호이스트)·전력(kW)·대형차량(40ft) 진입 여부",
        seoKeywordPattern = "{지역} 공장 경매, 유치권 분석"
    );

    companion object {
        fun fromRaw(raw: String?): PropertyType {
            val normalized = raw?.trim().orEmpty()
            return when {
                normalized.equals("APT_OFFICETEL", true) || normalized.contains("아파트") || normalized.contains("오피스텔") -> APT_OFFICETEL
                normalized.equals("VILLA_MULTIFAMILY", true) || normalized.contains("빌라") || normalized.contains("다세대") || normalized.contains("다가구") -> VILLA_MULTIFAMILY
                normalized.equals("COMMERCIAL_RETAIL", true) || normalized.contains("상가") || normalized.contains("근린") -> COMMERCIAL_RETAIL
                normalized.equals("LAND_FARMLAND", true) || normalized.equals("LAND", true) || normalized.contains("토지") || normalized.contains("임야") || normalized.contains("농지") -> LAND_FARMLAND
                normalized.equals("FACTORY_SPECIAL", true) || normalized.contains("공장") || normalized.contains("창고") || normalized.contains("지식산업") -> FACTORY_SPECIAL
                else -> APT_OFFICETEL
            }
        }
    }
}

enum class ChannelPlatform(
    val code: String,
    val displayName: String,
    val staggerWindowLabel: String,
    val imageResolutionSpec: String
) {
    CLOUDFLARE("CLOUDFLARE", "Cloudflare 자체웹", "1순위 T+0분 (원본 색인)", "1200x630"),
    TISTORY("TISTORY", "티스토리", "2순위 T+35~60분", "1200x675"),
    NAVER("NAVER", "네이버 블로그", "3순위 T+100~160분", "800x800");

    companion object {
        fun fromCode(code: String): ChannelPlatform =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: CLOUDFLARE
    }
}

enum class PostStatus(val code: String, val label: String) {
    DRAFT("DRAFT", "초안검수"),
    QUEUED("QUEUED", "예약대기"),
    PUBLISHED("PUBLISHED", "발행완료"),
    FAILED("FAILED", "실패/차단"),
    HANDOFF("HANDOFF", "캡차중단");

    companion object {
        fun fromCode(code: String?): PostStatus =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DRAFT
    }
}

data class InfographicCardSpec(
    val cardIndex: Int,
    val cardType: String,
    val aspectRatio: String,
    val headline: String,
    val subline: String,
    val seoFilename: String,
    val altText: String,
    val md5Hash: String,
    val sha256Hash: String,
    val exifStripped: Boolean = true
)

data class ChannelVariantContent(
    val platform: ChannelPlatform,
    val categoryCode: String,
    val categoryLabel: String,
    val title: String,
    val bodyText: String,
    val bodyHtml: String,
    val metaDescription: String,
    val tags: List<String>,
    val ctaUrl: String,
    val relAttribute: String,
    val jsonLdSchema: String,
    val infographicCards: List<InfographicCardSpec>,
    val pinnedCommentSnippet: String = "",
    val proFunnelBadge: String = ""
)

data class AuctionCaseIntel(
    val caseId: String,
    val dedupKey: String,
    val courtName: String,
    val address: String,
    val propertyType: PropertyType,
    val appraisalPriceKrw: Long,
    val minBidPriceKrw: Long,
    val winningPriceKrw: Long? = null,
    val bidCount: Int,
    val discountRatePct: Int,
    val auctionDate: String,
    val baseRightDate: String,
    val dividendDeadline: String,
    val claimAmountKrw: Long,
    val depositRatePct: Int,
    val possessionSummary: String,
    val tenantSummary: String,
    val assumedRightsText: String,
    val specialCheckNotice: String,
    val recentRealTradeMedianKrw: Long,
    val stats3yRatePct: Double,
    val clicksLast7d: Int,
    val conservativeBidKrw: Long,
    val balancedBidKrw: Long,
    val aggressiveBidKrw: Long
)

data class QaAuditReport(
    val maxCrossChannelCosinePct: Double,
    val maxCrossChannelJaccard5GramPct: Double,
    val recent7DayMaxSimilarityPct: Double,
    val pairSimilarities: Map<String, Double>,
    val similarityPassed: Boolean,
    val factCheckPassed: Boolean,
    val factCheckMismatches: List<String>,
    val forbiddenHits: List<String>,
    val charCountByChannel: Map<ChannelPlatform, Int>,
    val charCountOkByChannel: Map<ChannelPlatform, Boolean>,
    val externalLinkRuleOk: Boolean,
    val externalLinkDetail: String,
    val imageCountAndHashOk: Boolean,
    val imageHashDetail: String,
    val jsonLdValid: Boolean,
    val scheduleJitterValid: Boolean,
    val overallPassed: Boolean,
    val legalDoctrinePassed: Boolean = true,
    val legalDoctrineSummary: String = "6대 법리·판례 모순 0건",
    val qualityDefenseFlagsCount: Int = 0
)

data class GeneratedPostSlot(
    val postId: String,
    val queueId: String,
    val slotIndex: Int,
    val publishDate: String,
    val auctionCase: AuctionCaseIntel,
    val variants: Map<ChannelPlatform, ChannelVariantContent>,
    val scheduledTimes: Map<ChannelPlatform, String>,
    val status: PostStatus,
    val qaReport: QaAuditReport,
    val publishedUrls: Map<ChannelPlatform, String> = emptyMap(),
    val sourceLabel: String = "스펙 5슬롯 프리셋"
)

data class PublishLogEntry(
    val logId: String,
    val postId: String,
    val caseId: String,
    val platform: ChannelPlatform,
    val status: PostStatus,
    val publishedUrl: String?,
    val errorMessage: String?,
    val handoffReason: String?,
    val retryCount: Int,
    val publishedAt: String
)

data class PropertyTemplateYaml(
    val propertyType: PropertyType,
    val yamlContent: String,
    val lastEditedAt: String
)

data class BlogPostTemplate(
    val id: String,
    val name: String,
    val propertyType: PropertyType,
    val targetChannel: ChannelPlatform,
    val titleTemplate: String,
    val bodyTemplate: String,
    val tagsTemplate: String,
    val isPreset: Boolean = false,
    val isActiveForGeneration: Boolean = true,
    val updatedAt: String = ""
)

data class ServerAutomationConfig(
    val ddakchalApiBase: String = "https://ddakchal.pages.dev",
    val crawlerSyncToken: String = "",
    val cfAccountId: String = "",
    val cfApiToken: String = "",
    val cfD1DatabaseId: String = "",
    val cfWorkerScriptName: String = "ddakchal-blog-worker",
    val cronExpression: String = "17 8 * * *",
    val dailyPostLimit: Int = 2,
    val weeklyTargetPosts: Int = 25,
    val minDelayMinutes: Int = 45,
    val maxDelayMinutes: Int = 120,
    val enableExternalLinkNaver: Boolean = false,
    val publishTargetNaver: Boolean = true,
    val publishTargetTistory: Boolean = true,
    val publishTargetCloudflare: Boolean = true,
    val naverBlogId: String = "",
    val naverCategoryName: String = "부동산 경매 분석",
    val naverWorkerRelayEndpoint: String = "",
    val naverId: String = "",
    val naverPw: String = "",
    val tistoryBlogUrl: String = "",
    val tistoryCategoryName: String = "경매·권리·세무 리포트",
    val tistoryId: String = "",
    val tistoryPw: String = "",
    val cloudflareBlogPath: String = "/blog",
    val llmApiEndpoint: String = "",
    val llmApiKey: String = "",
    val naverSessionStatus: String = "유효 (storage_state 로드 대기)",
    val tistorySessionStatus: String = "유효 (storage_state 로드 대기)",
    val telegramWebhookConfigured: Boolean = true
)

data class ServerTelemetryState(
    val ddakchalConnected: Boolean = false,
    val cloudflareD1Connected: Boolean = false,
    val activeCronSchedules: List<String> = emptyList(),
    val d1DatabaseName: String = "ddakchal-db",
    val lastSyncTime: String = "동기화 대기",
    val lastD1ExecutionSummary: String = "미실행",
    val dailyQuotaUsed: Int = 0
)

enum class SeoRuleCategory(val label: String) {
    NAVER("네이버 C-Rank·D.I.A.+"),
    TISTORY("티스토리·다음 SEO"),
    GOOGLE_GEO("구글·GEO/AIO"),
    ANTI_BOT("중복·이미지·봇 방어"),
    CUSTOM("사용자 규칙")
}

enum class SeoRulePriority(val label: String) {
    CRITICAL("필수 차단"),
    RECOMMENDED("지수 권장")
}

data class SeoChecklistItem(
    val id: String,
    val title: String,
    val description: String,
    val category: SeoRuleCategory,
    val priority: SeoRulePriority,
    val isChecked: Boolean = false,
    val isCustom: Boolean = false,
    val ruleCode: String = "",
    val updatedAt: String = ""
)

data class LowQualityPolicyGuide(
    val id: String,
    val title: String,
    val engineFocus: String,
    val summary: String,
    val keyPolicies: List<String>,
    val penaltyTriggers: List<String>,
    val category: SeoRuleCategory
)

data class DividendWaterfallStep(
    val priorityRank: Int,
    val creditorLabel: String,
    val claimAmountKrw: Long,
    val distributedKrw: Long,
    val absorbedByBuyerKrw: Long,
    val statusTag: String
)

data class TaxScenarioBreakdown(
    val entityType: String,
    val acquisitionTaxPct: String,
    val holdingTaxNote: String,
    val capitalGainsTaxNote: String,
    val recommendedWhen: String
)

data class FieldPhotoGuideShot(
    val shotNumber: Int,
    val targetTitle: String,
    val checkPoint: String,
    val redFlagSignal: String,
    val blogCaptionTemplate: String
)

data class BenchmarkTierRow(
    val dimension: String,
    val generalBlogLevel: String,
    val paidAppLevel: String,
    val ddakchalFreeBlogLevel: String,
    val ddakchalSignupProLevel: String
)

data class ProAuctionAnalysisReport(
    val caseId: String,
    val safetyMarginKrw: Long,
    val safetyMarginPct: Double,
    val assumedExtraCostKrw: Long,
    val effectiveCostKrw: Long,
    val ltv70LoanKrw: Long,
    val netEquityRequiredKrw: Long,
    val estimatedMonthlyRentKrw: Long,
    val estimatedCapRatePct: Double,
    val estimated12mRoiPct: Double,
    val evictionRiskLevel: String,
    val estimatedEvictionDays: Int,
    val estimatedEvictionReserveKrw: Long,
    val dividendWaterfall: List<DividendWaterfallStep>,
    val taxScenarios: List<TaxScenarioBreakdown>,
    val photoGuideShots: List<FieldPhotoGuideShot>,
    val nextDropMinBidKrw: Long = 0L,
    val breakevenResalePriceKrw: Long = 0L,
    val rateStressMonthlyDeltaKrw: Long = 0L,
    val privacyMaskedConfirmed: Boolean = true
)

data class ProFunnelConfig(
    val funnelEnabled: Boolean = true,
    val proLandingUrl: String = "https://ddakchal.pages.dev/pro",
    val naverSearchKeyword: String = "딱찰 경매",
    val teaserHeadline: String = "30년 업력·100인+ 전담팀이 유료 분석 무료 공개 · 실시간 AI 경쟁률은 딱찰 회원가입(PRO) 전용",
    val freeUnlockedItems: List<String> = listOf(
        "4대 공부 배당순위 워터폴 · 선순위 인수액·손익분기(BEP) 자동 산출",
        "LTV 70% 실투자금 · CapRate · 12개월 ROE · 금리 스트레스 테스트",
        "개인·부부공동·법인 세무 3안 손익 비교표 (개인정보 100% 비식별)",
        "물건 유형별 현장 임장 사진 5컷 촬영·함정 체크 가이드"
    ),
    val proLockedItems: List<String> = listOf(
        "딱찰 회원가입 전용: 실시간 AI 예상 경쟁률 · 입찰가 분포 히트맵",
        "등기부 초단위 변동 · 매각기일 변경 즉시 푸시 알림",
        "대출 한도·DSR 1분 정밀 심사 및 매수대리 위임 리포트",
        "입찰 당일 직전 1시간 경쟁자 유입량 및 낙찰 확률 분포표"
    ),
    val naverPinnedCommentTemplate: String = "[30년·100인 딱찰 경매법인 안내] 채무자·임차인 개인정보는 100% 비식별 처리했으며 권리·세무3안·손익분기(BEP)·사진 5컷은 본문에 무료 공개했습니다. {사건번호} 실시간 입찰 경쟁률 히트맵·등기 변동 알림은 네이버에서 '{검색어}' 검색 후 딱찰 무료 회원가입으로 바로 확인하세요.",
    val updatedAt: String = "2026-11-22 08:17"
)

enum class RegistrySection(val label: String) {
    GAP_GU("갑구 (소유권·처분)"),
    EUL_GU("을구 (담보·용익)"),
    TENANT_COURT("임차·관리비·현황")
}

data class RegistryRightEntry(
    val orderNo: String,
    val section: RegistrySection,
    val registeredDate: String,
    val rightTitle: String,
    val amountText: String,
    val isBaseRight: Boolean,
    val outcomeLabel: String,
    val isAssumedRisk: Boolean
)

data class MotionGraphicScene(
    val sceneNumber: Int,
    val timeRange: String,
    val durationSec: Int,
    val sceneTitle: String,
    val motionEffect: String,
    val onScreenCaption: String,
    val narrationScript: String,
    val visualAssetHint: String
)

data class NaverBlogResearchInsight(
    val id: String,
    val pillar: String,
    val title: String,
    val metricHighlight: String,
    val actionRules: List<String>
)

data class MarketingRevenueForecast(
    val dailyVisitors: Int,
    val cpmKrw: Int,
    val monthlyAdpostKrw: Long,
    val monthlyDbLeads: Int,
    val monthlyDbRevenueKrw: Long,
    val monthlyProConversions: Int,
    val monthlyProRevenueKrw: Long,
    val totalMonthlyRevenueKrw: Long
)

enum class RegistryInfographicTemplateType(val label: String, val summary: String) {
    COMBINED_EXPERT_BOARD("종합 해부형", "소유권·갑구·을구·인수리스크 한눈에"),
    TIMELINE_CANCELLATION("말소기준 도해형", "시간순 소멸·생존 권리선 직관 비교"),
    OWNERSHIP_CHAIN_AND_GAPGU("소유권·갑구형", "소유자 변동 및 압류·경매개시 추적"),
    EULGU_MORTGAGE_WATERFALL("을구·배당여력형", "근저당 채권최고액·배당 잔액 검증")
}

enum class RegistryNodeTone {
    NEUTRAL_OWNERSHIP,
    BASE_RIGHT_ANCHOR,
    EXTINGUISHED_SAFE,
    ASSUMED_DANGER
}

data class OwnershipStatusCard(
    val currentOwnerLabel: String,
    val acquisitionCause: String,
    val ownershipRiskNote: String,
    val coOwnerOrShareNote: String
)

data class RegistryVisualNode(
    val nodeId: String,
    val section: RegistrySection,
    val rankOrder: String,
    val registeredDate: String,
    val title: String,
    val holderOrCreditor: String,
    val amountOrShare: String,
    val statusBadge: String,
    val plainExplanation: String,
    val nodeTone: RegistryNodeTone,
    val isBaseRight: Boolean,
    val isAssumedRisk: Boolean
)

data class RegistryInfographicTemplate(
    val caseId: String,
    val templateType: RegistryInfographicTemplateType,
    val headline: String,
    val subHeadline: String,
    val ownershipSummaryCard: OwnershipStatusCard,
    val gapGuNodes: List<RegistryVisualNode>,
    val eulGuNodes: List<RegistryVisualNode>,
    val cancellationBaselineSummary: String,
    val buyerTakeoverSummary: String,
    val readerChecklist: List<String>,
    val renderAspectRatio: String = "1200x800"
)

enum class EvaluationPillar(val label: String) {
    FACT_REGISTRY("팩트·등기정확성"),
    HUMAN_TONE("탈AI 사람문체"),
    SEO_LOW_QUALITY("저품질·SEO규격"),
    VISUAL_MOTION("인포·모션가이드"),
    FUNNEL_SAFETY("수익·퍼널·안전")
}

data class EvaluationCriterionResult(
    val id: String,
    val orderIndex: Int,
    val pillar: EvaluationPillar,
    val title: String,
    val maxPoints: Int,
    val earnedPoints: Int,
    val passed: Boolean,
    val detail: String
)

data class ArticleEvaluationResult(
    val totalScore: Int,
    val maxScore: Int = 100,
    val passedCount: Int,
    val totalCriteriaCount: Int = 30,
    val eligibleForRegistration: Boolean,
    val items: List<EvaluationCriterionResult>,
    val evaluatedAt: String
)

enum class WarehouseArticleStatus(val label: String) {
    STORED_READY("창고보관(95+ 등록가능)"),
    NEEDS_IMPROVEMENT("보완필요(95점 미만)"),
    REGISTERED("발행·등록완료")
}

data class WarehouseArticle(
    val warehouseId: String,
    val slot: GeneratedPostSlot,
    val evaluation: ArticleEvaluationResult,
    val registryInfographic: RegistryInfographicTemplate,
    val warehouseStatus: WarehouseArticleStatus,
    val createdAt: String,
    val registeredAt: String? = null,
    val registeredChannels: List<ChannelPlatform> = emptyList(),
    val accumulatedViews: Int = 0,
    val dbLeadCount: Int = 0,
    val proClickCount: Int = 0
)

data class WritingActivityLog(
    val logId: String,
    val warehouseId: String,
    val caseId: String,
    val actionTitle: String,
    val score: Int,
    val passedGate95: Boolean,
    val detail: String,
    val timestamp: String
)

data class VisitorTrafficLog(
    val dateLabel: String,
    val naverViews: Int,
    val tistoryViews: Int,
    val cloudflareViews: Int,
    val totalViews: Int,
    val avgDwellSec: Int,
    val dbInquiries: Int,
    val proConversions: Int,
    val estimatedAdpostKrw: Long
)

enum class CourtDocKind(val title: String, val badge: String) {
    SALE_SPECIFICATION("매각물건명세서", "최선순위·배당종기·인수권리"),
    STATUS_SURVEY("현황조사서", "집행관 점유·임차·제시외 조사"),
    APPRAISAL_REPORT("감정평가서", "토지/건물 단가·용도·지목 산출")
}

data class CourtDocumentRecord(
    val docKind: CourtDocKind,
    val referenceNo: String,
    val issuedDate: String,
    val issuer: String,
    val summaryHighlight: String,
    val fullRecordLines: List<String>,
    val riskFlagNote: String
)

data class CourtPhotoEvidence(
    val shotOrder: Int,
    val title: String,
    val courtSourceLabel: String,
    val captureAngle: String,
    val parcelOrFloorTag: String,
    val observationNote: String,
    val redFlagCheck: String,
    val exifStatusLabel: String,
    val photoKey: String,
    val blogCaption: String
)

data class CourtFullArchiveRecord(
    val caseId: String,
    val courtName: String,
    val regionTag: String,
    val areaSqmLabel: String,
    val landCategoryLabel: String,
    val zoningLabel: String,
    val profitabilityBadge: String,
    val photos: List<CourtPhotoEvidence>,
    val documents: List<CourtDocumentRecord>,
    val registryGapGu: List<RegistryVisualNode>,
    val registryEulGu: List<RegistryVisualNode>,
    val rightsVerdictSummary: String,
    val assumedCostSummary: String,
    val recordedAt: String
)

data class AiAutoFixSuggestion(
    val targetChannel: ChannelPlatform,
    val criterionOrder: Int,
    val issueTitle: String,
    val suggestedFix: String,
    val improvedSnippet: String
)

data class AiEvaluationReport(
    val caseId: String,
    val modelName: String,
    val totalScore: Int,
    val passedCount: Int,
    val aiSummary: String,
    val overallVerdict: String,
    val criteriaResults: List<EvaluationCriterionResult>,
    val autoFixSuggestions: List<AiAutoFixSuggestion>,
    val evaluatedAt: String
)

sealed interface AiEvalUiState {
    data object Idle : AiEvalUiState
    data class Evaluating(val caseId: String) : AiEvalUiState
    data class Success(val report: AiEvaluationReport) : AiEvalUiState
    data class Error(
        val caseId: String,
        val message: String,
        val baselineScore: Int,
        val baselineSuggestions: List<AiAutoFixSuggestion> = emptyList()
    ) : AiEvalUiState
}




