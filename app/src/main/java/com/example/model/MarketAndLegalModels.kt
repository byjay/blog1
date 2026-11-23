package com.example.model

enum class QualityDefenseCategory(val label: String) {
    AI_STRUCTURAL_PATTERN("AI 기계 구조(■·Q1~Q6)"),
    AI_CLICHE_PHRASE("AI 상용 어미·인공 서술"),
    EMOJI_DECORATION("불필요한 이모지·장식 기호"),
    SEO_SPAM_TRIGGER("저품질 과장·금칙 키워드"),
    TITLE_BRACKET_SPAM("제목 대괄호·어그로 태그"),
    NAVER_DIRECT_URL("네이버 본문 직접 외부 URL"),
    PII_EXPOSURE("채무자·임차인 개인정보 노출")
}

data class QualityDefenseHit(
    val category: QualityDefenseCategory,
    val matchedSnippet: String,
    val replacementSnippet: String,
    val count: Int
)

data class QualityDefenseResult(
    val originalTitle: String,
    val sanitizedTitle: String,
    val originalBody: String,
    val sanitizedBody: String,
    val hits: List<QualityDefenseHit>,
    val totalHitsCount: Int,
    val humanToneScorePct: Int,
    val summary: String
)

data class QualityDefenseModuleState(
    val autoDefenseEnabled: Boolean = true,
    val lastAuditedCaseId: String = "",
    val lastChannel: ChannelPlatform = ChannelPlatform.NAVER,
    val lastResult: QualityDefenseResult? = null,
    val totalAutoFixedCount: Int = 0,
    val lastUpdatedAt: String = "대기"
)

enum class MarketApiSourceStatus(val label: String) {
    IDLE("조회 대기"),
    LIVE_FULL("네이버 검색API + 부동산 호가 실연동"),
    LIVE_LAND_ONLY("네이버 부동산 공개 호가 연동 (검색API 키 미설정)"),
    LIVE_SEARCH_ONLY("네이버 검색API 동향 연동 (부동산 호가 접근제한)"),
    API_KEY_REQUIRED("네이버 개발자 검색API 키(Secrets) 필요"),
    RATE_LIMITED_OR_ERROR("네이버 요청 제한(429/403) 또는 네트워크 오류")
}

data class NearbyMarketComp(
    val sourceLabel: String,
    val title: String,
    val propertyCategory: String,
    val tradeType: String,
    val priceKrw: Long,
    val areaSqmLabel: String,
    val floorInfo: String,
    val listingDate: String,
    val isUrgentSale: Boolean
)

data class RecalculatedBidBand(
    val conservativeBidKrw: Long,
    val balancedBidKrw: Long,
    val aggressiveBidKrw: Long,
    val urgentAskMarginKrw: Long,
    val urgentAskMarginPct: Double,
    val realTradeMarginKrw: Long,
    val safetyRuleNote: String
)

data class NearbyMarketAnalysisReport(
    val caseId: String,
    val regionQuery: String,
    val status: MarketApiSourceStatus,
    val statusDetail: String,
    val naverSearchHitCount: Int = 0,
    val naverLandListingCount: Int = 0,
    val urgentAskMinKrw: Long? = null,
    val nearbyAskMedianKrw: Long? = null,
    val realTradeBenchmarkKrw: Long,
    val comps: List<NearbyMarketComp> = emptyList(),
    val blogTrendTitles: List<String> = emptyList(),
    val recalculatedBids: RecalculatedBidBand? = null,
    val fetchedAt: String = ""
)

enum class LegalVerificationSeverity(val label: String) {
    VERIFIED_PASS("법리 검증 통과"),
    CAUTION_NOTE("실무 유의 고지"),
    CRITICAL_BLOCK("QA 차단·모순 발견")
}

enum class LegalVerificationDomain(val label: String) {
    BASE_RIGHT_CANCELLATION("말소기준권리·소멸/인수"),
    TENANT_PRIORITY_DIVIDEND("임차인 대항력·소액최우선변제"),
    MANAGEMENT_FEE_2001DA8677("공용관리비 승계(대법원 2001다8677)"),
    LIEN_AFTER_SEIZURE("유치권·압류선후 대항력"),
    STATUTORY_SUPERFICIES("법정지상권(민법366·전합)"),
    SPECIAL_STATUTORY_AND_DOCS("특수법리·4대 공부 모순")
}

data class LegalDoctrineCheckItem(
    val id: String,
    val domain: LegalVerificationDomain,
    val title: String,
    val statuteOrPrecedent: String,
    val severity: LegalVerificationSeverity,
    val passed: Boolean,
    val estimatedBuyerBurdenKrw: Long,
    val findingDetail: String,
    val actionGuidance: String
)

data class LegalDoctrineReport(
    val caseId: String,
    val overallPassed: Boolean,
    val criticalBlockCount: Int,
    val cautionCount: Int,
    val totalEstimatedLegalBurdenKrw: Long,
    val checks: List<LegalDoctrineCheckItem>,
    val auditSummary: String,
    val verifiedAt: String
)
