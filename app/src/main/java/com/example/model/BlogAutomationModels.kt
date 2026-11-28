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
    val sourceLabel: String = "스펙 5슬롯 프리셋",
    val ddakchalAnalysisData: DdakchalBlogAnalysisData? = null,
    val courtPhotoUrls: List<String> = emptyList(),
    val visitRecords: List<FieldVisitRecord> = emptyList(),
    val serverUpdatedAt: String = "",
    val serverRegistryGapGu: List<RegistryVisualNode> = emptyList(),
    val serverRegistryEulGu: List<RegistryVisualNode> = emptyList(),
    val serverCourtDocuments: List<CourtDocumentRecord> = emptyList()
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
    val cloudflareSiteBase: String = "https://aution.codeb.app",
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
    val naverSessionStatus: String = "세션 미확인 (연결 점검 또는 워커 릴레이 필요)",
    val tistorySessionStatus: String = "미연동 (릴레이·URL 연결 점검 대기)",
    val naverKeepLoggedIn: Boolean = true,
    val tistoryKeepLoggedIn: Boolean = true,
    val tistoryAutoDirectPublish: Boolean = true,
    val naverWriteMode: NaverAutoWriteMode = NaverAutoWriteMode.WORKER_RELAY,
    val aiPrimaryGeminiFirst: Boolean = true,
    val grokFallbackEnabled: Boolean = true,
    val grokModelName: String = "grok-3-mini",
    val autoPauseOnHighRisk: Boolean = true,
    val telegramWebhookConfigured: Boolean = true
)

data class ServerTelemetryState(
    val ddakchalConnected: Boolean = false,
    val cloudflareD1Connected: Boolean = false,
    val activeCronSchedules: List<String> = emptyList(),
    val d1DatabaseName: String = "ddakchal-db",
    val lastSyncTime: String = "동기화 대기",
    val lastD1ExecutionSummary: String = "미실행",
    val dailyQuotaUsed: Int = 0,
    val apiLatencyMs: Long? = null,
    val lastSyncEpochMs: Long = 0L,
    val lastHttpStatusCode: Int? = null,
    val lastErrorDetail: String? = null,
    val lastFeedCursorSince: String? = null,
    val triggerStatus: DdakchalTriggerStatusSnapshot? = null,
    val autoMonitorEnabled: Boolean = true,
    val isPinging: Boolean = false
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

enum class VisitRecordSource(val label: String, val badgeText: String) {
    BAILIFF_SURVEY("집행관 현황조사 방문", "집행관방문"),
    RESIDENT_CHECK("전입세대·임대차 열람", "전입·임차"),
    MANAGEMENT_OFFICE("관리사무소·공과금 탐문", "관리실탐문"),
    FIELD_IMJANG("현장 임장 직접 방문", "직접임장")
}

data class FieldVisitRecord(
    val visitId: String,
    val visitRound: String,
    val visitedAt: String,
    val source: VisitRecordSource,
    val visitorLabel: String,
    val occupancyStatus: String,
    val findingsNote: String,
    val attachedPhotoUri: String = ""
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
    val blogCaption: String,
    val imageUrl: String = ""
)

data class CourtSharpDiscrepancyCheck(
    val id: String,
    val axisTitle: String,
    val documentsCompared: String,
    val verdictBadge: String,
    val isAlert: Boolean,
    val findingDetail: String,
    val actionDirective: String
)

data class CourtSharpAnalysisReport(
    val caseId: String,
    val sharpGradeBadge: String,
    val executiveVerdict: String,
    val absorbedCostKrw: Long,
    val totalAcquisitionCostKrw: Long,
    val checks: List<CourtSharpDiscrepancyCheck>,
    val killerTrapPoints: List<String>,
    val authorityPromoHeadline: String,
    val analyzedAt: String
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
    val recordedAt: String,
    val visitRecords: List<FieldVisitRecord> = emptyList(),
    val sharpAnalysis: CourtSharpAnalysisReport? = null
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

// ==================== DDAKCHAL Live Feed & Queue Analytics Models ====================

data class DdakchalKeyMetricsChartData(
    val appraisalKrw: Long = 0L,
    val minBidKrw: Long = 0L,
    val realTradeMedianKrw: Long = 0L,
    val conservativeBidKrw: Long = 0L,
    val balancedBidKrw: Long = 0L,
    val aggressiveBidKrw: Long = 0L,
    val discountRatePct: Int = 0,
    val stats3yRatePct: Double = 0.0
)

data class DdakchalRiskOpportunity(
    val riskSummary: String = "",
    val opportunitySummary: String = "",
    val evictionDifficulty: String = "",
    val tenantRiskNote: String = ""
)

data class DdakchalQaPair(
    val question: String,
    val answer: String
)

data class DdakchalFreshness(
    val updatedAt: String = "",
    val source: String = "ddakchal-crawler-d1",
    val courtPhotoCount: Int = 0
)

data class DdakchalBlogAnalysisData(
    val hookHeadline: String = "",
    val summary3Lines: List<String> = emptyList(),
    val keyMetricsChartData: DdakchalKeyMetricsChartData? = null,
    val riskAndOpportunity: DdakchalRiskOpportunity? = null,
    val qaBlocks: List<DdakchalQaPair> = emptyList(),
    val freshness: DdakchalFreshness? = null
)

data class DdakchalTriggerEventSummary(
    val eventId: String,
    val caseNo: String,
    val eventType: String,
    val status: String,
    val createdAt: String
)

data class DdakchalTriggerStatusSnapshot(
    val pendingCount: Int = 0,
    val processedCount: Int = 0,
    val failedCount: Int = 0,
    val snapshotsCount: Int = 0,
    val latestSnapshotAt: String = "",
    val todayQueueCount: Int = 0,
    val generatedPostsCount: Int = 0,
    val checkedAt: String = "",
    val recentEvents: List<DdakchalTriggerEventSummary> = emptyList()
)

data class DailyPublishVolumePoint(
    val dateLabel: String,
    val publishedCount: Int,
    val queuedCount: Int,
    val failedCount: Int
) {
    val totalCount: Int get() = publishedCount + queuedCount + failedCount
}

data class ChannelPublishStat(
    val platform: ChannelPlatform,
    val publishedCount: Int,
    val queuedCount: Int,
    val failedCount: Int
) {
    val totalCount: Int get() = publishedCount + queuedCount + failedCount
}

data class QueueAnalyticsSummary(
    val totalSlots: Int,
    val publishedCount: Int,
    val queuedCount: Int,
    val failedOrHandoffCount: Int,
    val draftCount: Int,
    val successRatePct: Int,
    val failureRatePct: Int,
    val dailySeries: List<DailyPublishVolumePoint>,
    val channelStats: List<ChannelPublishStat>
)

// ==================== Security Dashboard & Zero-Password Auth Models ====================

enum class SecurityGrade(val label: String, val scoreRangeLabel: String) {
    S("S등급 · 최고안전", "90~100점"),
    A("A등급 · 양호", "75~89점"),
    B("B등급 · 주의", "55~74점"),
    C("C등급 · 조치필요", "0~54점");

    companion object {
        fun fromScore(score: Int): SecurityGrade = when {
            score >= 90 -> S
            score >= 75 -> A
            score >= 55 -> B
            else -> C
        }
    }
}

data class TokenSecurityItem(
    val keyName: String,
    val label: String,
    val isConfigured: Boolean,
    val maskedPreview: String,
    val leakCount: Int,
    val statusNote: String
)

data class AccountSecurityGrade(
    val channelName: String,
    val platform: ChannelPlatform?,
    val targetAddress: String,
    val score: Int,
    val grade: SecurityGrade,
    val zeroPasswordCompliant: Boolean,
    val sessionStatus: String,
    val endpointReachable: Boolean? = null,
    val findings: List<String>
)

data class SecurityDashboardSummary(
    val overallScore: Int,
    val overallGrade: SecurityGrade,
    val legacyPlaintextPwDetected: Boolean,
    val externalLeakCount: Int,
    val leakedTokensDetail: List<String>,
    val tokenItems: List<TokenSecurityItem>,
    val accountGrades: List<AccountSecurityGrade>,
    val lastAuditedAt: String,
    val recommendationText: String
)

data class ChannelProbeResult(
    val platform: ChannelPlatform,
    val targetUrl: String,
    val reachableOk: Boolean,
    val httpsCompliant: Boolean,
    val httpStatusCode: Int,
    val detailMessage: String,
    val checkedAt: String
)

// ==================== AI Case Search (GEO/AEO) & Pure HTML Export Models ====================

data class AiCaseGeoOptimizationPack(
    val caseId: String,
    val searchQuery: String,
    val hookSnippet: String,
    val directAnswerParagraph: String,
    val aiKeywords: List<String>,
    val faqPairs: List<DdakchalQaPair>,
    val faqJsonLd: String,
    val estimatedGeoScore: Int,
    val modelUsed: String,
    val generatedAt: String
)

sealed interface AiCaseSearchUiState {
    data object Idle : AiCaseSearchUiState
    data class Searching(val query: String) : AiCaseSearchUiState
    data class Success(
        val matchedSlot: GeneratedPostSlot,
        val geoPack: AiCaseGeoOptimizationPack
    ) : AiCaseSearchUiState
    data class Error(
        val query: String,
        val message: String
    ) : AiCaseSearchUiState
}

data class HtmlGeneratedPostItem(
    val index: Int,
    val caseId: String,
    val title: String,
    val platform: ChannelPlatform,
    val propertyTypeLabel: String,
    val pureHtml: String,
    val score: Int,
    val bodyOnlyHtml: String = ""
)

data class HtmlBatchBundleState(
    val modeLabel: String,
    val threeCasesMode: Boolean,
    val items: List<HtmlGeneratedPostItem>,
    val generatedAt: String
)

// ==================== Naver Search API Keyword Blog Exposure Diagnostic Models ====================

enum class ExposurePotentialGrade(val label: String) {
    BLUE_OCEAN("최상 · 블루오션 공략"),
    COMPETITIVE_WINNABLE("양호 · 롱테일 진입 추천"),
    CROWDED_NICHE("경합 · 권리·실측 차별화 필수"),
    SATURATED("포화 · 세부 사건·입지 결합 필요")
}

data class NaverBlogSearchSnapshot(
    val keyword: String,
    val totalSearchCount: Int,
    val sampledCount: Int,
    val recent7DaysCount: Int,
    val recent30DaysCount: Int,
    val auctionFocusedRatioPct: Int,
    val exactCaseMatchCount: Int,
    val topPostTitles: List<String>,
    val topBloggerNames: List<String>,
    val latestPostDate: String
)

data class KeywordBlogExposureReport(
    val keyword: String,
    val exposureScore: Int,
    val grade: ExposurePotentialGrade,
    val snapshot: NaverBlogSearchSnapshot,
    val competitionSummary: String,
    val recommendedTitlePattern: String,
    val recommendedSubKeywords: List<String>,
    val actionChecklist: List<String>,
    val diagnosedAt: String
)

sealed interface NaverKeywordDiagnosisUiState {
    data object Idle : NaverKeywordDiagnosisUiState
    data class Loading(val keyword: String) : NaverKeywordDiagnosisUiState
    data class Success(val report: KeywordBlogExposureReport) : NaverKeywordDiagnosisUiState
    data class Error(
        val keyword: String,
        val message: String,
        val apiKeyMissing: Boolean = false
    ) : NaverKeywordDiagnosisUiState
}

// ==================== 4-Pillar Topic Selection Engine Models ====================

enum class TopicSortCriteria(val label: String) {
    BALANCED_4_PILLAR("4대 기준 종합"),
    QUALITY_FIRST("글 품질 우선"),
    SEO_FIRST("SEO 적합성 우선"),
    AI_GEO_FIRST("AI 노출(GEO) 우선"),
    POPULARITY_FIRST("인기 매물 우선")
}

data class TopicCandidateRanking(
    val postId: String,
    val caseId: String,
    val address: String,
    val propertyTypeLabel: String,
    val rank: Int,
    val totalTopicScore: Int,
    val qualityScore: Int,
    val seoFitScore: Int,
    val aiExposureScore: Int,
    val popularityScore: Int,
    val tierLabel: String,
    val selectionReasonSummary: String,
    val recommendedAngle: String,
    val tags: List<String>,
    val isFromLiveServer: Boolean
)

data class TopicSelectionBoardState(
    val sortMode: TopicSortCriteria = TopicSortCriteria.BALANCED_4_PILLAR,
    val candidates: List<TopicCandidateRanking> = emptyList(),
    val topPickedPostIds: Set<String> = emptySet(),
    val lastEvaluatedAt: String = "",
    val summaryBanner: String = ""
)

// ==================== Full DDAKCHAL Server Integration Diagnostic Models ====================

data class DdakchalEndpointCheckItem(
    val id: String,
    val name: String,
    val endpointPath: String,
    val method: String,
    val passed: Boolean,
    val optional: Boolean = false,
    val httpStatusCode: Int = 0,
    val latencyMs: Long = 0L,
    val detailMessage: String,
    val checkedSpec: String
)

data class DdakchalFullDiagnosticReport(
    val overallConnected: Boolean,
    val overallScorePct: Int,
    val healthGradeLabel: String,
    val apiBase: String,
    val authHeaderReady: Boolean,
    val checks: List<DdakchalEndpointCheckItem>,
    val recommendation: String,
    val checkedAt: String
)

// ==================== Naver Auto-Write Alternatives, Publish Status & Google Gemini Account Models ====================

enum class NaverAutoWriteMode(
    val title: String,
    val shortBadge: String,
    val description: String,
    val prosCons: String
) {
    WORKER_RELAY(
        title = "1) Playwright 워커 릴레이 (가장 완전한 무인 자동화)",
        shortBadge = "무인 자동발행",
        description = "Cloudflare Worker 또는 PC/서버 Playwright가 네이버 쿠키(storage_state)를 유지하며 스마트에디터 ONE에 제목·본문·사진·태그를 자동 입력·발행합니다.",
        prosCons = "장점: 예약·카테고리·사진 자동등록 / 단점: 워커·세션 1회 유지 필요"
    ),
    MOBILE_WRITE_URL(
        title = "2) 모바일 글쓰기 URL + 원고 자동 복사 브릿지",
        shortBadge = "즉시 반자동",
        description = "안드로이드에서 네이버 모바일 글쓰기 창을 즉시 열고 제목·본문·고정댓글을 클립보드에 자동 탑재해 붙여넣기 한 번으로 등록합니다.",
        prosCons = "장점: 캡차·저품질 위험 최소 / 단점: 최종 발행 버튼 1회 터치 필요"
    ),
    POST_MAIL_BRIDGE(
        title = "3) 네이버 블로그 '글쓰기 전용 메일(포스트메일)' 전송",
        shortBadge = "공식 이메일발행",
        description = "네이버 블로그 관리 > 글쓰기 설정의 전용 메일 주소로 제목·본문·사진을 SMTP/Worker로 발송해 네이버가 직접 포스팅하도록 합니다.",
        prosCons = "장점: 공식 기능·캡차 우회 / 단점: 서식·고정댓글은 후속 보완 필요"
    ),
    SESSION_HANDOFF(
        title = "4) 영구 브라우저 세션 핸드오프 (비번 미저장)",
        shortBadge = "캡차 안전유지",
        description = "기기 브라우저에 로그인 상태를 계속 유지(네이버·티스토리)한 채 에디터 연동 또는 HTML 에디터 붙여넣기로 즉시 발행합니다.",
        prosCons = "장점: 비밀번호 미저장 보안 / 단점: 브라우저 로그인 세션 만료 시 1회 재인증"
    )
}

data class NaverWriteMethodGuide(
    val mode: NaverAutoWriteMode,
    val isActive: Boolean,
    val readinessLabel: String,
    val setupSteps: List<String>,
    val quickActionUrl: String = ""
)

data class NaverPublishStatusRecord(
    val postId: String,
    val caseId: String,
    val title: String,
    val statusLabel: String,
    val isRegisteredOk: Boolean,
    val methodUsed: String,
    val httpStatusCode: Int,
    val publishedUrl: String,
    val detailNote: String,
    val updatedAt: String
)

data class GoogleGeminiAccountState(
    val isSignedIn: Boolean = false,
    val email: String = "",
    val displayName: String = "",
    val uid: String = "",
    val activePrimaryEngineLabel: String = "1순위 Google Gemini (Firebase AI · 계정 연동 대기)",
    val activeBackupEngineLabel: String = "2순위 Grok API (보조 대기)",
    val lastSignInStatus: String = "미로그인 (Google 계정 로그인 시 본인 계정 기반 할당·추적 우선 사용)",
    val isSigningIn: Boolean = false
)

// ==================== Retrofit /api/v1/blog/queue Live Sync Models ====================

data class DdakchalQueueItemRecord(
    val id: String,
    val caseNo: String,
    val dedupKey: String,
    val title: String,
    val platform: ChannelPlatform,
    val status: PostStatus,
    val publishDate: String,
    val scheduledFor: String,
    val publishedAt: String,
    val blogPostUrl: String,
    val lastError: String
)

data class QueueSyncResult(
    val items: List<DdakchalQueueItemRecord>,
    val analytics: QueueAnalyticsSummary,
    val syncedAt: String,
    val sourceEndpoint: String
)

sealed interface QueueSyncState {
    data object Idle : QueueSyncState
    data object Loading : QueueSyncState
    data class Loaded(
        val items: List<DdakchalQueueItemRecord>,
        val analytics: QueueAnalyticsSummary,
        val syncedAt: String,
        val latencyMs: Long,
        val sourceEndpoint: String
    ) : QueueSyncState
    data class Empty(
        val message: String,
        val syncedAt: String,
        val latencyMs: Long,
        val sourceEndpoint: String
    ) : QueueSyncState
    data class Error(
        val httpStatusCode: Int,
        val message: String,
        val tokenMissing: Boolean = false
    ) : QueueSyncState
}

// ==================== Post-Publish Channel Index & Low-Quality Risk Models ====================

enum class LowQualityRiskLevel(val label: String, val badgeText: String) {
    SAFE("안전 · 지수 양호", "안전"),
    CAUTION("주의 · 지수 둔화 징후", "주의"),
    HIGH_RISK("위험 높음 · 자동발행 일시 중지", "위험 높음")
}

data class ChannelIndexHealth(
    val platform: ChannelPlatform,
    val publishedCount: Int,
    val failedCount: Int,
    val errorRatePct: Int,
    val baselineIndexScore: Int,
    val currentIndexScore: Int,
    val indexDelta: Int,
    val avgDwellSec: Int,
    val exposureScore: Int,
    val qualityFlagCount: Int,
    val externalLinkViolationCount: Int,
    val riskLevel: LowQualityRiskLevel,
    val riskFactors: List<String>,
    val lastEvaluatedAt: String
)

data class LowQualityRiskReport(
    val overallRisk: LowQualityRiskLevel,
    val overallRiskScore: Int,
    val autoPublishPaused: Boolean,
    val pauseReason: String?,
    val channelHealthList: List<ChannelIndexHealth>,
    val triggeredAlerts: List<String>,
    val recoveryActions: List<String>,
    val evaluatedAt: String
)

// ==================== Todak/Ddakchal Top Admin & Server DB Crawl Engine Models ====================

enum class AdminPipelineStageStatus(val label: String) {
    IDLE("대기"),
    RUNNING("실행중"),
    SUCCESS("완료"),
    WARN("부분완료"),
    ERROR("오류")
}

data class AdminPipelineStepLog(
    val stepIndex: Int,
    val title: String,
    val endpoint: String,
    val status: AdminPipelineStageStatus,
    val httpCode: Int = 0,
    val latencyMs: Long = 0L,
    val detail: String
)

data class ServerDbTableInfo(
    val sourceName: String,
    val tableOrEndpoint: String,
    val rowCount: Int,
    val httpStatusCode: Int,
    val sampleColumns: List<String>,
    val statusDetail: String
)

data class ServerDbDiscoveredItem(
    val recordId: String,
    val dedupKey: String,
    val caseId: String,
    val courtName: String,
    val address: String,
    val propertyType: PropertyType,
    val appraisalKrw: Long,
    val minBidKrw: Long,
    val photoCount: Int,
    val visitCount: Int,
    val hasAnalysisData: Boolean,
    val sourceOrigin: String,
    val updatedAt: String,
    val slot: GeneratedPostSlot
)

data class TodakAdminConsoleState(
    val selectedRegions: List<String> = listOf("서울", "경기"),
    val selectedCategoryFilter: String = "",
    val searchQuery: String = "",
    val crawlLimit: Int = 5,
    val isRunning: Boolean = false,
    val currentActionLabel: String = "",
    val discoveredTables: List<ServerDbTableInfo> = emptyList(),
    val discoveredItems: List<ServerDbDiscoveredItem> = emptyList(),
    val generatedPostIds: List<String> = emptyList(),
    val stepLogs: List<AdminPipelineStepLog> = emptyList(),
    val d1TablesRaw: List<String> = emptyList(),
    val lastUpdatedAt: String = "",
    val summaryMessage: String = "최상단 관리자 콘솔 준비됨 · 서버 DB 조회 및 딱찰 크롤링 엔진 실행 가능",
    val caseLookupInput: String = "",
    val caseLookupStatus: String = ""
)
