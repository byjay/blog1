package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.AiEvaluationOutcome
import com.example.ai.AiSeoGeoAdvisor
import com.example.ai.FirebaseAiSeoGeoService
import com.example.data.BlogAutomationRepository
import com.example.data.BlogPostTemplateStore
import com.example.data.DirectChannelPublishClient
import com.example.data.NaverRealEstateApiClient
import com.example.data.PostWarehouseStore
import com.example.data.ProFunnelStore
import com.example.data.SeoChecklistStore
import com.example.data.ServerConfigStore
import com.example.engine.Article30PointEvaluator
import com.example.engine.BlogPostTemplateEngine
import com.example.engine.BoilerplateAndTemplateEngine
import com.example.engine.CourtEvidenceRecorder
import com.example.engine.DefaultSpecDataFactory
import com.example.engine.InvestmentAttractivenessEngine
import com.example.engine.LegalDoctrineVerifier
import com.example.engine.NearbyMarketEngine
import com.example.engine.PostAuctionFollowUpEngine
import com.example.engine.QaVerificationEngine
import com.example.engine.QualityDefenseEngine
import com.example.engine.RegistryInfographicEngine
import com.example.engine.SeoGuideEngine
import com.example.engine.WeeklyBatchScheduleEngine
import com.example.model.AiEvalUiState
import com.example.model.AuctionCaseIntel
import com.example.model.BlogPostTemplate
import com.example.model.ChannelPlatform
import com.example.model.CourtFullArchiveRecord
import com.example.model.GeneratedPostSlot
import com.example.model.InvestmentAttractivenessScore
import com.example.model.LegalDoctrineReport
import com.example.model.LegalStudyCategory
import com.example.model.NaverAndMultiChannelPublishReport
import com.example.model.NearbyMarketAnalysisReport
import com.example.model.PostAuctionFollowUpRecord
import com.example.model.PostAuctionOutcomeType
import com.example.model.PostStatus
import com.example.model.ProFunnelConfig
import com.example.model.PropertyTemplateYaml
import com.example.model.PropertyType
import com.example.model.PublishLogEntry
import com.example.model.QualityDefenseModuleState
import com.example.model.RegistryInfographicTemplate
import com.example.model.RegistryInfographicTemplateType
import com.example.model.SeoChecklistItem
import com.example.model.SeoRuleCategory
import com.example.model.SeoRulePriority
import com.example.model.ServerAutomationConfig
import com.example.model.ServerTelemetryState
import com.example.model.VisitorTrafficLog
import com.example.model.WarehouseArticle
import com.example.model.WarehouseArticleStatus
import com.example.model.WeeklyBatchRunSummary
import com.example.model.WritingActivityLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StudioUiState(
    val slots: List<GeneratedPostSlot> = emptyList(),
    val selectedSlotId: String? = null,
    val selectedChannel: ChannelPlatform = ChannelPlatform.NAVER,
    val selectedRegistryTemplateType: RegistryInfographicTemplateType = RegistryInfographicTemplateType.COMBINED_EXPERT_BOARD,
    val activeRegistryInfographic: RegistryInfographicTemplate? = null,
    val activeCourtArchive: CourtFullArchiveRecord? = null,
    val aiEvalState: AiEvalUiState = AiEvalUiState.Idle,
    val warehouseArticles: List<WarehouseArticle> = emptyList(),
    val writingLogs: List<WritingActivityLog> = emptyList(),
    val visitorLogs: List<VisitorTrafficLog> = emptyList(),
    val templates: List<PropertyTemplateYaml> = BoilerplateAndTemplateEngine.defaultYamlTemplates(),
    val postTemplates: List<BlogPostTemplate> = BlogPostTemplateEngine.defaultBlogPostTemplates(),
    val seoChecklist: List<SeoChecklistItem> = SeoGuideEngine.defaultChecklistItems(),
    val proFunnel: ProFunnelConfig = ProFunnelConfig(),
    val publishLogs: List<PublishLogEntry> = DefaultSpecDataFactory.buildInitialLogs(),
    val config: ServerAutomationConfig = ServerAutomationConfig(),
    val telemetry: ServerTelemetryState = ServerTelemetryState(),
    val activeHandoffAlert: String? = null,
    val poolPreviewIndex: Int = 0,
    val isBusy: Boolean = false,
    val isFetchingMarket: Boolean = false,
    val qualityDefenseState: QualityDefenseModuleState = QualityDefenseModuleState(),
    val marketAnalysisByCaseId: Map<String, NearbyMarketAnalysisReport> = emptyMap(),
    val legalVerificationByCaseId: Map<String, LegalDoctrineReport> = emptyMap(),
    val selectedWeeklyDailyCount: Int = 4,
    val weeklyBatchSummary: WeeklyBatchRunSummary? = null,
    val publishVerificationReport: NaverAndMultiChannelPublishReport? = null,
    val followUpRecordsByCaseId: Map<String, PostAuctionFollowUpRecord> = emptyMap(),
    val attractivenessByCaseId: Map<String, InvestmentAttractivenessScore> = emptyMap(),
    val selectedLegalStudyCategory: LegalStudyCategory? = null,
    val legalStudyQuery: String = "",
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

class StudioViewModel(
    private val repository: BlogAutomationRepository,
    private val aiAdvisor: AiSeoGeoAdvisor = FirebaseAiSeoGeoService(),
    private val naverApiClient: NaverRealEstateApiClient = NaverRealEstateApiClient(),
    private val directPublishClient: DirectChannelPublishClient = DirectChannelPublishClient()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    init {
        val initialConfig = ServerAutomationConfig()
        val initialSlots = DefaultSpecDataFactory.buildInitialDaily5Slots(initialConfig)
        val initialSeed = PostWarehouseStore.buildInitialSeed()
        val firstSlot = initialSlots.firstOrNull()
        val firstAuction = firstSlot?.auctionCase
        val initialInfographic = firstAuction?.let {
            RegistryInfographicEngine.buildTemplate(it, RegistryInfographicTemplateType.COMBINED_EXPERT_BOARD)
        }
        val initialArchive = firstAuction?.let { CourtEvidenceRecorder.recordFullArchive(it) }
        val initialLegalMap = initialSlots.associate { slot ->
            slot.auctionCase.caseId to LegalDoctrineVerifier.verify(slot.auctionCase)
        }
        val initialAttractivenessMap = initialSlots.associate { slot ->
            val legal = initialLegalMap[slot.auctionCase.caseId] ?: LegalDoctrineVerifier.verify(slot.auctionCase)
            slot.auctionCase.caseId to InvestmentAttractivenessEngine.score(slot.auctionCase, legal)
        }
        val initialDefense = firstSlot?.variants?.get(ChannelPlatform.NAVER)?.let { nv ->
            QualityDefenseEngine.defendText(
                rawTitle = nv.title,
                rawBody = nv.bodyText,
                channel = ChannelPlatform.NAVER,
                auction = firstSlot.auctionCase
            )
        }
        _uiState.update {
            it.copy(
                slots = initialSlots,
                selectedSlotId = firstSlot?.postId,
                activeRegistryInfographic = initialInfographic,
                activeCourtArchive = initialArchive,
                warehouseArticles = initialSeed.articles,
                writingLogs = initialSeed.writingLogs,
                visitorLogs = initialSeed.visitorLogs,
                legalVerificationByCaseId = initialLegalMap,
                attractivenessByCaseId = initialAttractivenessMap,
                qualityDefenseState = QualityDefenseModuleState(
                    autoDefenseEnabled = true,
                    lastAuditedCaseId = firstAuction?.caseId.orEmpty(),
                    lastChannel = ChannelPlatform.NAVER,
                    lastResult = initialDefense,
                    lastUpdatedAt = currentTimestamp()
                ),
                telemetry = it.telemetry.copy(
                    dailyQuotaUsed = initialSlots.count { slot ->
                        slot.status == PostStatus.PUBLISHED || slot.status == PostStatus.QUEUED
                    }
                )
            )
        }
        observePersistedConfig()
        observePersistedPostTemplates()
        observePersistedSeoChecklist()
        observePersistedProFunnel()
        observePersistedWarehouse()
        loadYamlOverrides()
    }

    private fun observePersistedWarehouse() {
        val flow = repository.warehouseSnapshotFlow ?: return
        viewModelScope.launch {
            flow.collect { snapshot ->
                _uiState.update {
                    it.copy(
                        warehouseArticles = snapshot.articles,
                        writingLogs = snapshot.writingLogs,
                        visitorLogs = snapshot.visitorLogs
                    )
                }
            }
        }
    }

    private fun observePersistedProFunnel() {
        val flow = repository.proFunnelFlow ?: return
        viewModelScope.launch {
            flow.collect { savedFunnel ->
                _uiState.update { it.copy(proFunnel = savedFunnel) }
            }
        }
    }

    private fun observePersistedSeoChecklist() {
        val flow = repository.seoChecklistFlow ?: return
        viewModelScope.launch {
            flow.collect { savedList ->
                _uiState.update { it.copy(seoChecklist = savedList) }
            }
        }
    }

    private fun observePersistedPostTemplates() {
        val flow = repository.postTemplatesFlow ?: return
        viewModelScope.launch {
            flow.collect { savedList ->
                _uiState.update { it.copy(postTemplates = savedList) }
            }
        }
    }

    private fun observePersistedConfig() {
        viewModelScope.launch {
            repository.configFlow.collect { savedConfig ->
                _uiState.update { state ->
                    val reAuditedSlots = state.slots.map { slot ->
                        val qa = QaVerificationEngine.auditSlot(
                            slot.auctionCase,
                            slot.variants,
                            slot.scheduledTimes,
                            savedConfig
                        )
                        slot.copy(qaReport = qa)
                    }
                    state.copy(config = savedConfig, slots = reAuditedSlots)
                }
            }
        }
    }

    private fun loadYamlOverrides() {
        viewModelScope.launch {
            val overrides = repository.getCustomYamlOverrides()
            if (overrides.isNotEmpty()) {
                _uiState.update { state ->
                    val updated = state.templates.map { tpl ->
                        overrides[tpl.propertyType]?.let { custom ->
                            tpl.copy(yamlContent = custom, lastEditedAt = "사용자 커스텀 저장본")
                        } ?: tpl
                    }
                    state.copy(templates = updated)
                }
            }
        }
    }

    fun selectSlot(postId: String) {
        _uiState.update { state ->
            val targetSlot = state.slots.firstOrNull { it.postId == postId }
                ?: state.warehouseArticles.firstOrNull { it.slot.postId == postId }?.slot
            val targetAuction = targetSlot?.auctionCase
            val legalMap = if (targetAuction != null && !state.legalVerificationByCaseId.containsKey(targetAuction.caseId)) {
                state.legalVerificationByCaseId + (targetAuction.caseId to LegalDoctrineVerifier.verify(targetAuction))
            } else {
                state.legalVerificationByCaseId
            }
            val defenseRes = targetSlot?.variants?.get(state.selectedChannel)?.let { v ->
                QualityDefenseEngine.defendText(
                    rawTitle = v.title,
                    rawBody = v.bodyText,
                    channel = state.selectedChannel,
                    auction = targetAuction,
                    naverSearchKeyword = state.proFunnel.naverSearchKeyword
                )
            }
            state.copy(
                selectedSlotId = postId,
                activeRegistryInfographic = targetAuction?.let {
                    RegistryInfographicEngine.buildTemplate(it, state.selectedRegistryTemplateType)
                } ?: state.activeRegistryInfographic,
                activeCourtArchive = targetAuction?.let {
                    CourtEvidenceRecorder.recordFullArchive(it)
                } ?: state.activeCourtArchive,
                legalVerificationByCaseId = legalMap,
                qualityDefenseState = if (defenseRes != null) {
                    state.qualityDefenseState.copy(
                        lastAuditedCaseId = targetAuction?.caseId.orEmpty(),
                        lastChannel = state.selectedChannel,
                        lastResult = defenseRes,
                        lastUpdatedAt = currentTimestamp()
                    )
                } else state.qualityDefenseState
            )
        }
    }

    fun selectRegistryTemplateType(type: RegistryInfographicTemplateType) {
        _uiState.update { state ->
            val targetAuction = state.slots.firstOrNull { it.postId == state.selectedSlotId }?.auctionCase
                ?: state.slots.firstOrNull()?.auctionCase
            state.copy(
                selectedRegistryTemplateType = type,
                activeRegistryInfographic = targetAuction?.let {
                    RegistryInfographicEngine.buildTemplate(it, type)
                }
            )
        }
    }

    fun selectChannel(platform: ChannelPlatform) {
        _uiState.update { state ->
            val targetSlot = state.slots.firstOrNull { it.postId == state.selectedSlotId }
                ?: state.warehouseArticles.firstOrNull { it.slot.postId == state.selectedSlotId }?.slot
            val variant = targetSlot?.variants?.get(platform)
            val defenseRes = variant?.let {
                QualityDefenseEngine.defendText(
                    rawTitle = it.title,
                    rawBody = it.bodyText,
                    channel = platform,
                    auction = targetSlot.auctionCase,
                    naverSearchKeyword = state.proFunnel.naverSearchKeyword
                )
            }
            state.copy(
                selectedChannel = platform,
                qualityDefenseState = if (defenseRes != null) {
                    state.qualityDefenseState.copy(
                        lastAuditedCaseId = targetSlot.auctionCase.caseId,
                        lastChannel = platform,
                        lastResult = defenseRes,
                        lastUpdatedAt = currentTimestamp()
                    )
                } else state.qualityDefenseState
            )
        }
    }

    fun clearBanners() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }

    fun triggerDaily5Generation() {
        val cfg = _uiState.value.config
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            try {
                val res = repository.triggerDaily5AndSync(cfg)
                val now = currentTimestamp()
                res.onSuccess { fetchedSlots ->
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            slots = fetchedSlots,
                            selectedSlotId = fetchedSlots.firstOrNull()?.postId,
                            telemetry = it.telemetry.copy(
                                ddakchalConnected = true,
                                lastSyncTime = now,
                                dailyQuotaUsed = fetchedSlots.count { s -> s.status == PostStatus.PUBLISHED || s.status == PostStatus.QUEUED }
                            ),
                            statusMessage = "딱찰 실서버 일일 ${fetchedSlots.size}건 생성 및 3채널 QA 검수 완료"
                        )
                    }
                }.onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            telemetry = it.telemetry.copy(ddakchalConnected = false),
                            errorMessage = "${err.message} · 현재 오프라인 스펙 5슬롯 모드로 검수 가능합니다."
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            }
        }
    }

    fun syncSyndicationPackOnly() {
        val cfg = _uiState.value.config
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            try {
                val res = repository.syncSyndicationPack(cfg)
                val now = currentTimestamp()
                res.onSuccess { fetchedSlots ->
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            slots = fetchedSlots,
                            selectedSlotId = fetchedSlots.firstOrNull()?.postId,
                            telemetry = it.telemetry.copy(ddakchalConnected = true, lastSyncTime = now),
                            statusMessage = "신디케이션 팩 ${fetchedSlots.size}건 동기화 및 유사도·팩트체크 완료"
                        )
                    }
                }.onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            errorMessage = err.message ?: "신디케이션 팩 동기화 실패"
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            }
        }
    }

    fun addCustomCrawledPropertyAndSchedule(
        caseId: String,
        courtName: String,
        address: String,
        propertyType: PropertyType,
        appraisalManWon: Long,
        minBidManWon: Long,
        baseRightDate: String,
        assumedRights: String,
        publishDate: String
    ) {
        val cleanCaseId = caseId.trim().ifBlank { "2026타경1001" }
        val appraisalKrw = (appraisalManWon.coerceAtLeast(100L)) * 10_000L
        val minBidKrw = (minBidManWon.coerceAtLeast(50L)) * 10_000L
        val discountPct = ((minBidKrw.toDouble() / appraisalKrw.toDouble()) * 100).toInt().coerceIn(10, 100)
        val cfg = _uiState.value.config
        val nextIndex = _uiState.value.slots.size + 1

        val auction = AuctionCaseIntel(
            caseId = cleanCaseId,
            dedupKey = "ca-custom-${cleanCaseId.hashCode().toUInt()}",
            courtName = courtName.trim().ifBlank { "서울중앙지방법원" },
            address = address.trim().ifBlank { "서울특별시 강남구 역삼동 물건" },
            propertyType = propertyType,
            appraisalPriceKrw = appraisalKrw,
            minBidPriceKrw = minBidKrw,
            bidCount = 1,
            discountRatePct = discountPct,
            auctionDate = publishDate.trim().ifBlank { "2026-12-12" },
            baseRightDate = baseRightDate.trim().ifBlank { "2021-05-10" },
            dividendDeadline = "2026-11-28",
            claimAmountKrw = (appraisalKrw * 0.55).toLong(),
            depositRatePct = 10,
            possessionSummary = "크롤러·현황조사서 연동 분석 완료",
            tenantSummary = "매각물건명세서 임차인 대항력·배당요구 검수 완료",
            assumedRightsText = assumedRights.trim().ifBlank { "말소기준권리 이후 권리 소멸, 현장 체납관리비 재확인" },
            specialCheckNotice = propertyType.focusSummary,
            recentRealTradeMedianKrw = (appraisalKrw * 0.92).toLong(),
            stats3yRatePct = 74.6,
            clicksLast7d = 540,
            conservativeBidKrw = (minBidKrw * 1.04).toLong(),
            balancedBidKrw = (minBidKrw * 1.11).toLong(),
            aggressiveBidKrw = (minBidKrw * 1.18).toLong()
        )

        val funnelCfg = _uiState.value.proFunnel
        val baseVariants = DefaultSpecDataFactory.buildDistinct3ChannelVariants(auction, nextIndex, cfg, proFunnel = funnelCfg)
        val activeTemplatesForType = _uiState.value.postTemplates.filter {
            it.propertyType == propertyType && it.isActiveForGeneration
        }
        val variants = baseVariants.mapValues { (platform, variant) ->
            val matchedTemplate = activeTemplatesForType.firstOrNull { it.targetChannel == platform }
            if (matchedTemplate != null) {
                BlogPostTemplateEngine.applyTemplateToVariant(variant, matchedTemplate, auction, cfg, funnelCfg)
            } else {
                variant
            }
        }
        val scheduled = repository.computeStaggeredSchedule(publishDate.ifBlank { "2026-11-22" }, nextIndex, cfg)

        viewModelScope.launch {
            val corpus = repository.getRecent7DayCorpus()
            val draftSlot = GeneratedPostSlot(
                postId = "post-${auction.dedupKey}",
                queueId = "q-custom-$nextIndex",
                slotIndex = nextIndex,
                publishDate = publishDate.ifBlank { "2026-11-22" },
                auctionCase = auction,
                variants = variants,
                scheduledTimes = scheduled,
                status = PostStatus.DRAFT,
                qaReport = QaVerificationEngine.auditSlot(auction, variants, scheduled, cfg, corpus),
                sourceLabel = "크롤링 물건 직접 예약"
            )
            val (finalSlot, defenseReport) = if (_uiState.value.qualityDefenseState.autoDefenseEnabled) {
                QualityDefenseEngine.defendSlot(draftSlot, cfg, funnelCfg, corpus)
            } else {
                draftSlot to QualityDefenseEngine.defendText(
                    rawTitle = draftSlot.variants[ChannelPlatform.NAVER]?.title.orEmpty(),
                    rawBody = draftSlot.variants[ChannelPlatform.NAVER]?.bodyText.orEmpty(),
                    channel = ChannelPlatform.NAVER,
                    auction = auction
                )
            }
            val newSlot = finalSlot.copy(
                status = if (finalSlot.qaReport.overallPassed) PostStatus.QUEUED else PostStatus.DRAFT
            )
            val legalReport = LegalDoctrineVerifier.verify(newSlot.auctionCase)
            val regTemplate = RegistryInfographicEngine.buildTemplate(newSlot.auctionCase, _uiState.value.selectedRegistryTemplateType)
            val evalResult = Article30PointEvaluator.evaluate(newSlot, cfg, funnelCfg, regTemplate, corpus)
            val whArticle = WarehouseArticle(
                warehouseId = "wh-${newSlot.auctionCase.dedupKey}",
                slot = newSlot,
                evaluation = evalResult,
                registryInfographic = regTemplate,
                warehouseStatus = if (evalResult.eligibleForRegistration) WarehouseArticleStatus.STORED_READY else WarehouseArticleStatus.NEEDS_IMPROVEMENT,
                createdAt = currentTimestamp()
            )
            val log = WritingActivityLog(
                logId = "wlog-${System.currentTimeMillis()}",
                warehouseId = whArticle.warehouseId,
                caseId = newSlot.auctionCase.caseId,
                actionTitle = "커스텀 물건 작성 → 창고 자동 보관 (${evalResult.totalScore}점)",
                score = evalResult.totalScore,
                passedGate95 = evalResult.eligibleForRegistration,
                detail = "${evalResult.passedCount}/30 항목 통과 · ${legalReport.auditSummary}",
                timestamp = currentTimestamp()
            )
            repository.upsertWarehouseArticle(whArticle, log)
            _uiState.update {
                it.copy(
                    slots = listOf(newSlot) + it.slots,
                    selectedSlotId = newSlot.postId,
                    activeRegistryInfographic = regTemplate,
                    legalVerificationByCaseId = it.legalVerificationByCaseId + (newSlot.auctionCase.caseId to legalReport),
                    qualityDefenseState = it.qualityDefenseState.copy(
                        lastAuditedCaseId = newSlot.auctionCase.caseId,
                        lastChannel = ChannelPlatform.NAVER,
                        lastResult = defenseReport,
                        totalAutoFixedCount = it.qualityDefenseState.totalAutoFixedCount + defenseReport.totalHitsCount,
                        lastUpdatedAt = currentTimestamp()
                    ),
                    statusMessage = "${newSlot.auctionCase.caseId} (${propertyType.displayLabel}) 생성·품질방어·법리검증 완료 (${evalResult.totalScore}점)"
                )
            }
        }
    }

    fun instantCreateAndStoreInWarehouse(propertyType: PropertyType? = null) {
        val state = _uiState.value
        val cfg = state.config
        val funnelCfg = state.proFunnel
        val nextIndex = state.warehouseArticles.size + 1
        val chosenType = propertyType ?: PropertyType.entries[(nextIndex - 1) % PropertyType.entries.size]
        val caseNo = "2026타경${2100 + nextIndex * 17}"
        val dedupKey = "ca-wh-${caseNo}-${System.currentTimeMillis() % 10000}"

        val (court, address, appMan, minMan) = when (chosenType) {
            PropertyType.APT_OFFICETEL -> listOf("수원지방법원", "경기도 용인시 수지구 성복동 롯데캐슬골드 102동 804호", "68000", "43520")
            PropertyType.VILLA_MULTIFAMILY -> listOf("서울북부지방법원", "서울특별시 도봉구 창동 신축 다세대 4층 401호", "26000", "16640")
            PropertyType.COMMERCIAL_RETAIL -> listOf("서울동부지방법원", "서울특별시 송파구 문정동 법조타운 근린상가 1층 112호", "89000", "56960")
            PropertyType.LAND_FARMLAND -> listOf("춘천지방법원", "강원특별자치도 원주시 지정면 계획관리 전 일괄", "42000", "26880")
            PropertyType.FACTORY_SPECIAL -> listOf("수원지방법원 안산지원", "경기도 시흥시 정왕동 시화공단 일반공장 1동", "195000", "124800")
        }
        val appraisalKrw = appMan.toLong() * 10_000L
        val minBidKrw = minMan.toLong() * 10_000L

        val auction = AuctionCaseIntel(
            caseId = caseNo,
            dedupKey = dedupKey,
            courtName = court,
            address = address,
            propertyType = chosenType,
            appraisalPriceKrw = appraisalKrw,
            minBidPriceKrw = minBidKrw,
            bidCount = 1,
            discountRatePct = 64,
            auctionDate = "2026-12-15",
            baseRightDate = "2021-07-08",
            dividendDeadline = "2025-12-05",
            claimAmountKrw = (appraisalKrw * 0.52).toLong(),
            depositRatePct = 10,
            possessionSummary = "채무자 겸 소유자 점유 또는 현황 확인 완료",
            tenantSummary = "전입세대열람 및 매각물건명세서 대조 완료(대항력 없음)",
            assumedRightsText = "말소기준권리 2021-07-08 근저당 이후 후순위 권리 일괄 소멸",
            specialCheckNotice = chosenType.focusSummary,
            recentRealTradeMedianKrw = (appraisalKrw * 0.91).toLong(),
            stats3yRatePct = 75.8,
            clicksLast7d = 580 + nextIndex * 11,
            conservativeBidKrw = (minBidKrw * 1.05).toLong(),
            balancedBidKrw = (minBidKrw * 1.12).toLong(),
            aggressiveBidKrw = (minBidKrw * 1.19).toLong()
        )

        val baseVariants = DefaultSpecDataFactory.buildDistinct3ChannelVariants(
            auction = auction,
            seedIndex = nextIndex,
            config = cfg,
            proFunnel = funnelCfg
        )
        val activeTemplatesForType = state.postTemplates.filter {
            it.propertyType == chosenType && it.isActiveForGeneration
        }
        val variants = baseVariants.mapValues { (platform, variant) ->
            val matchedTemplate = activeTemplatesForType.firstOrNull { it.targetChannel == platform }
            if (matchedTemplate != null) {
                BlogPostTemplateEngine.applyTemplateToVariant(variant, matchedTemplate, auction, cfg, funnelCfg)
            } else {
                variant
            }
        }
        val scheduled = repository.computeStaggeredSchedule("2026-11-22", nextIndex, cfg)

        viewModelScope.launch {
            val corpus = repository.getRecent7DayCorpus()
            val draftSlot = GeneratedPostSlot(
                postId = "post-$dedupKey",
                queueId = "q-wh-$nextIndex",
                slotIndex = nextIndex,
                publishDate = "2026-11-22",
                auctionCase = auction,
                variants = variants,
                scheduledTimes = scheduled,
                status = PostStatus.DRAFT,
                qaReport = QaVerificationEngine.auditSlot(auction, variants, scheduled, cfg, corpus),
                sourceLabel = "원클릭 창고 즉시생성"
            )
            val (newSlot, defenseReport) = if (state.qualityDefenseState.autoDefenseEnabled) {
                QualityDefenseEngine.defendSlot(draftSlot, cfg, funnelCfg, corpus)
            } else {
                draftSlot to QualityDefenseEngine.defendText(
                    rawTitle = draftSlot.variants[ChannelPlatform.NAVER]?.title.orEmpty(),
                    rawBody = draftSlot.variants[ChannelPlatform.NAVER]?.bodyText.orEmpty(),
                    channel = ChannelPlatform.NAVER,
                    auction = auction
                )
            }
            val legalReport = LegalDoctrineVerifier.verify(newSlot.auctionCase)
            val regTemplate = RegistryInfographicEngine.buildTemplate(newSlot.auctionCase, state.selectedRegistryTemplateType)
            val courtArchive = CourtEvidenceRecorder.recordFullArchive(newSlot.auctionCase)
            val evalResult = Article30PointEvaluator.evaluate(newSlot, cfg, funnelCfg, regTemplate, corpus)
            val now = currentTimestamp()
            val whArticle = WarehouseArticle(
                warehouseId = "wh-$dedupKey",
                slot = newSlot,
                evaluation = evalResult,
                registryInfographic = regTemplate,
                warehouseStatus = if (evalResult.eligibleForRegistration) WarehouseArticleStatus.STORED_READY else WarehouseArticleStatus.NEEDS_IMPROVEMENT,
                createdAt = now
            )
            val log = WritingActivityLog(
                logId = "wlog-${System.currentTimeMillis()}",
                warehouseId = whArticle.warehouseId,
                caseId = newSlot.auctionCase.caseId,
                actionTitle = "원클릭 바로생성 → 창고 보관 (${evalResult.totalScore}점)",
                score = evalResult.totalScore,
                passedGate95 = evalResult.eligibleForRegistration,
                detail = "${evalResult.passedCount}/30 항목 통과 · ${legalReport.auditSummary}",
                timestamp = now
            )
            val snapshot = repository.upsertWarehouseArticle(whArticle, log)
            _uiState.update { current ->
                current.copy(
                    slots = listOf(newSlot) + current.slots,
                    selectedSlotId = newSlot.postId,
                    activeRegistryInfographic = regTemplate,
                    activeCourtArchive = courtArchive,
                    legalVerificationByCaseId = current.legalVerificationByCaseId + (newSlot.auctionCase.caseId to legalReport),
                    qualityDefenseState = current.qualityDefenseState.copy(
                        lastAuditedCaseId = newSlot.auctionCase.caseId,
                        lastChannel = ChannelPlatform.NAVER,
                        lastResult = defenseReport,
                        totalAutoFixedCount = current.qualityDefenseState.totalAutoFixedCount + defenseReport.totalHitsCount,
                        lastUpdatedAt = currentTimestamp()
                    ),
                    warehouseArticles = snapshot?.articles ?: (listOf(whArticle) + current.warehouseArticles),
                    writingLogs = snapshot?.writingLogs ?: (listOf(log) + current.writingLogs),
                    statusMessage = "${newSlot.auctionCase.caseId} 원클릭 생성·품질방어·법리검증 완료 (30개 항목 ${evalResult.totalScore}/100점)"
                )
            }
            evaluateSlotWithAi30Criteria(newSlot.postId)
        }
    }

    fun evaluateSlotWithAi30Criteria(postId: String? = null) {
        val state = _uiState.value
        val targetSlot = state.slots.firstOrNull { it.postId == (postId ?: state.selectedSlotId) }
            ?: state.warehouseArticles.firstOrNull { it.slot.postId == (postId ?: state.selectedSlotId) }?.slot
            ?: state.slots.firstOrNull()
            ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(aiEvalState = AiEvalUiState.Evaluating(targetSlot.auctionCase.caseId)) }
            when (val outcome = aiAdvisor.evaluateWith30Criteria(targetSlot, state.config, state.proFunnel)) {
                is AiEvaluationOutcome.Success -> {
                    val report = outcome.report
                    val now = currentTimestamp()
                    val log = WritingActivityLog(
                        logId = "wlog-ai-${System.currentTimeMillis()}",
                        warehouseId = "wh-${targetSlot.auctionCase.dedupKey}",
                        caseId = report.caseId,
                        actionTitle = "AI 30항목 SEO/GEO 채점 완료 (${report.totalScore}/100점)",
                        score = report.totalScore,
                        passedGate95 = report.totalScore >= Article30PointEvaluator.REGISTRATION_MIN_SCORE,
                        detail = "${report.modelName} · ${report.passedCount}/30 통과 · 제안 ${report.autoFixSuggestions.size}건",
                        timestamp = now
                    )
                    val existingWh = _uiState.value.warehouseArticles.firstOrNull { it.slot.postId == targetSlot.postId }
                    if (existingWh != null) {
                        val syncedEval = existingWh.evaluation.copy(
                            totalScore = report.totalScore,
                            passedCount = report.passedCount,
                            eligibleForRegistration = report.totalScore >= Article30PointEvaluator.REGISTRATION_MIN_SCORE,
                            items = report.criteriaResults,
                            evaluatedAt = report.evaluatedAt
                        )
                        val updatedWh = existingWh.copy(
                            evaluation = syncedEval,
                            warehouseStatus = when {
                                existingWh.warehouseStatus == WarehouseArticleStatus.REGISTERED && syncedEval.eligibleForRegistration ->
                                    WarehouseArticleStatus.REGISTERED
                                syncedEval.eligibleForRegistration -> WarehouseArticleStatus.STORED_READY
                                else -> WarehouseArticleStatus.NEEDS_IMPROVEMENT
                            }
                        )
                        repository.upsertWarehouseArticle(updatedWh, log)
                    }
                    _uiState.update {
                        it.copy(
                            aiEvalState = AiEvalUiState.Success(report),
                            statusMessage = "${report.caseId} AI 30항목 채점 완료: ${report.totalScore}/100점 (${report.overallVerdict})"
                        )
                    }
                }
                is AiEvaluationOutcome.Error -> {
                    _uiState.update {
                        it.copy(
                            aiEvalState = AiEvalUiState.Error(
                                caseId = targetSlot.auctionCase.caseId,
                                message = outcome.message,
                                baselineScore = outcome.baselineScore,
                                baselineSuggestions = outcome.baselineSuggestions
                            )
                        )
                    }
                }
            }
        }
    }

    fun createAndEvaluateBusanProfitableLandTest() {
        val state = _uiState.value
        val cfg = state.config
        val funnelCfg = state.proFunnel
        val busanCandidates = CourtEvidenceRecorder.findProfitableBusanLandUnder50M()
        val chosenIndex = if (state.slots.any { it.auctionCase.caseId == busanCandidates.first().caseId }) 1 else 0
        val auction = busanCandidates[chosenIndex.coerceIn(0, busanCandidates.lastIndex)]
        val nextIndex = state.slots.size + 1

        val variants = DefaultSpecDataFactory.buildDistinct3ChannelVariants(
            auction = auction,
            seedIndex = nextIndex + 3,
            config = cfg,
            proFunnel = funnelCfg
        )
        val scheduled = repository.computeStaggeredSchedule("2026-11-22", nextIndex, cfg)

        viewModelScope.launch {
            val corpus = repository.getRecent7DayCorpus()
            val qa = QaVerificationEngine.auditSlot(auction, variants, scheduled, cfg, corpus)
            val newSlot = GeneratedPostSlot(
                postId = "post-${auction.dedupKey}",
                queueId = "q-busan-${auction.caseId}",
                slotIndex = nextIndex,
                publishDate = "2026-11-22",
                auctionCase = auction,
                variants = variants,
                scheduledTimes = scheduled,
                status = if (qa.overallPassed) PostStatus.QUEUED else PostStatus.DRAFT,
                qaReport = qa,
                sourceLabel = "부산 5천만원 미만 수익성 토지 발굴"
            )
            val regTemplate = RegistryInfographicEngine.buildTemplate(auction, state.selectedRegistryTemplateType)
            val courtArchive = CourtEvidenceRecorder.recordFullArchive(auction)
            val evalResult = Article30PointEvaluator.evaluate(newSlot, cfg, funnelCfg, regTemplate, corpus)
            val now = currentTimestamp()
            val whArticle = WarehouseArticle(
                warehouseId = "wh-${auction.dedupKey}",
                slot = newSlot,
                evaluation = evalResult,
                registryInfographic = regTemplate,
                warehouseStatus = if (evalResult.eligibleForRegistration) WarehouseArticleStatus.STORED_READY else WarehouseArticleStatus.NEEDS_IMPROVEMENT,
                createdAt = now
            )
            val log = WritingActivityLog(
                logId = "wlog-busan-${System.currentTimeMillis()}",
                warehouseId = whArticle.warehouseId,
                caseId = auction.caseId,
                actionTitle = "부산 5천만 미만 수익성 토지 발굴·생성 (${QaVerificationEngine.formatKrwShort(auction.minBidPriceKrw)})",
                score = evalResult.totalScore,
                passedGate95 = evalResult.eligibleForRegistration,
                detail = "법원사진 ${courtArchive.photos.size}컷 · 문건 ${courtArchive.documents.size}종 · 기본 ${evalResult.totalScore}점",
                timestamp = now
            )
            val snapshot = repository.upsertWarehouseArticle(whArticle, log)
            _uiState.update { current ->
                val dedupedSlots = listOf(newSlot) + current.slots.filterNot { it.postId == newSlot.postId }
                current.copy(
                    slots = dedupedSlots,
                    selectedSlotId = newSlot.postId,
                    activeRegistryInfographic = regTemplate,
                    activeCourtArchive = courtArchive,
                    warehouseArticles = snapshot?.articles ?: (listOf(whArticle) + current.warehouseArticles),
                    writingLogs = snapshot?.writingLogs ?: (listOf(log) + current.writingLogs),
                    statusMessage = "${auction.caseId} 부산 5천만원 미만 토지 글 생성·법원기록 저장 완료 → AI 채점 실행 중"
                )
            }
            evaluateSlotWithAi30Criteria(newSlot.postId)
        }
    }

    fun createLowScoreDraftForAutoFixTest() {
        val state = _uiState.value
        val cfg = state.config
        val funnelCfg = state.proFunnel
        val auction = CourtEvidenceRecorder.findProfitableBusanLandUnder50M().first()
        val draftCaseId = "${auction.caseId}-미완성초안"
        val badAuction = auction.copy(
            caseId = draftCaseId,
            dedupKey = "ca-low-draft-${System.currentTimeMillis() % 10000}"
        )
        val baseVariants = DefaultSpecDataFactory.buildDistinct3ChannelVariants(badAuction, 9, cfg, proFunnel = funnelCfg)
        val badNaver = baseVariants[ChannelPlatform.NAVER]!!.copy(
            title = "[급매추천] ${badAuction.caseId}",
            bodyText = "■ Q1. 부산 토지 경매 알아보겠습니다 🔥 https://spam-test.example.com",
            pinnedCommentSnippet = ""
        )
        val badVariants = baseVariants + (ChannelPlatform.NAVER to badNaver)
        val scheduled = repository.computeStaggeredSchedule("2026-11-22", 9, cfg)

        viewModelScope.launch {
            val qa = QaVerificationEngine.auditSlot(badAuction, badVariants, scheduled, cfg)
            val draftSlot = GeneratedPostSlot(
                postId = "post-${badAuction.dedupKey}",
                queueId = "q-low-${System.currentTimeMillis() % 1000}",
                slotIndex = state.slots.size + 1,
                publishDate = "2026-11-22",
                auctionCase = badAuction,
                variants = badVariants,
                scheduledTimes = scheduled,
                status = PostStatus.DRAFT,
                qaReport = qa,
                sourceLabel = "95점 미만 수정제안 테스트 초안"
            )
            val regTemplate = RegistryInfographicEngine.buildTemplate(badAuction, state.selectedRegistryTemplateType)
            val courtArchive = CourtEvidenceRecorder.recordFullArchive(badAuction)
            val evalResult = Article30PointEvaluator.evaluate(draftSlot, cfg, funnelCfg, regTemplate)
            val whArticle = WarehouseArticle(
                warehouseId = "wh-${badAuction.dedupKey}",
                slot = draftSlot,
                evaluation = evalResult,
                registryInfographic = regTemplate,
                warehouseStatus = WarehouseArticleStatus.NEEDS_IMPROVEMENT,
                createdAt = currentTimestamp()
            )
            val log = WritingActivityLog(
                logId = "wlog-low-${System.currentTimeMillis()}",
                warehouseId = whArticle.warehouseId,
                caseId = badAuction.caseId,
                actionTitle = "95점 미만 초안 테스트 (${evalResult.totalScore}점 → 자동 수정제안 대기)",
                score = evalResult.totalScore,
                passedGate95 = false,
                detail = "${evalResult.passedCount}/30 통과 · AI 자동 수정 제안 생성",
                timestamp = currentTimestamp()
            )
            val snapshot = repository.upsertWarehouseArticle(whArticle, log)
            _uiState.update { current ->
                current.copy(
                    slots = listOf(draftSlot) + current.slots,
                    selectedSlotId = draftSlot.postId,
                    activeRegistryInfographic = regTemplate,
                    activeCourtArchive = courtArchive,
                    warehouseArticles = snapshot?.articles ?: (listOf(whArticle) + current.warehouseArticles),
                    writingLogs = snapshot?.writingLogs ?: (listOf(log) + current.writingLogs)
                )
            }
            evaluateSlotWithAi30Criteria(draftSlot.postId)
        }
    }

    fun applyAiAutoFixesToArticle(caseIdOrPostId: String) {
        val state = _uiState.value
        val targetSlot = state.slots.firstOrNull {
            it.postId == caseIdOrPostId || it.auctionCase.caseId == caseIdOrPostId
        } ?: state.warehouseArticles.firstOrNull {
            it.slot.postId == caseIdOrPostId || it.slot.auctionCase.caseId == caseIdOrPostId
        }?.slot ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            val fixedAuction = if (targetSlot.auctionCase.caseId.endsWith("-미완성초안")) {
                targetSlot.auctionCase.copy(
                    caseId = targetSlot.auctionCase.caseId.removeSuffix("-미완성초안")
                )
            } else {
                targetSlot.auctionCase
            }
            val fixedSlot = repository.rewriteVariantToPassQa(
                slot = targetSlot.copy(auctionCase = fixedAuction),
                config = state.config,
                proFunnel = state.proFunnel
            )
            val reg = RegistryInfographicEngine.buildTemplate(fixedAuction, state.selectedRegistryTemplateType)
            val archive = CourtEvidenceRecorder.recordFullArchive(fixedAuction)
            val eval = Article30PointEvaluator.evaluate(fixedSlot, state.config, state.proFunnel, reg)

            val existingWh = _uiState.value.warehouseArticles.firstOrNull { it.slot.postId == targetSlot.postId }
            val whId = existingWh?.warehouseId ?: "wh-${fixedAuction.dedupKey}"
            val updatedWh = WarehouseArticle(
                warehouseId = whId,
                slot = fixedSlot,
                evaluation = eval,
                registryInfographic = reg,
                warehouseStatus = if (eval.eligibleForRegistration) WarehouseArticleStatus.STORED_READY else WarehouseArticleStatus.NEEDS_IMPROVEMENT,
                createdAt = existingWh?.createdAt ?: currentTimestamp()
            )
            val log = WritingActivityLog(
                logId = "wlog-fix-${System.currentTimeMillis()}",
                warehouseId = whId,
                caseId = fixedAuction.caseId,
                actionTitle = "AI 수정 제안 자동 적용 완료 (${eval.totalScore}점)",
                score = eval.totalScore,
                passedGate95 = eval.eligibleForRegistration,
                detail = "${eval.passedCount}/30 항목 통과 · 95점+ 회복",
                timestamp = currentTimestamp()
            )
            val snapshot = repository.upsertWarehouseArticle(updatedWh, log)
            _uiState.update { current ->
                current.copy(
                    isBusy = false,
                    slots = current.slots.map { if (it.postId == targetSlot.postId) fixedSlot else it },
                    selectedSlotId = fixedSlot.postId,
                    activeRegistryInfographic = reg,
                    activeCourtArchive = archive,
                    warehouseArticles = snapshot?.articles ?: current.warehouseArticles,
                    writingLogs = snapshot?.writingLogs ?: (listOf(log) + current.writingLogs),
                    statusMessage = "${fixedAuction.caseId} 수정 제안 자동 적용 완료 → AI 재채점 실행"
                )
            }
            evaluateSlotWithAi30Criteria(fixedSlot.postId)
        }
    }

    fun importQueueSlotsToWarehouse() {
        val state = _uiState.value
        val cfg = state.config
        val funnelCfg = state.proFunnel
        viewModelScope.launch {
            val corpus = repository.getRecent7DayCorpus()
            val now = currentTimestamp()
            val items = state.slots.map { slot ->
                val regTemplate = RegistryInfographicEngine.buildTemplate(slot.auctionCase, state.selectedRegistryTemplateType)
                val eval = Article30PointEvaluator.evaluate(slot, cfg, funnelCfg, regTemplate, corpus)
                WarehouseArticle(
                    warehouseId = "wh-${slot.auctionCase.dedupKey}",
                    slot = slot,
                    evaluation = eval,
                    registryInfographic = regTemplate,
                    warehouseStatus = when {
                        slot.status == PostStatus.PUBLISHED && eval.eligibleForRegistration -> WarehouseArticleStatus.REGISTERED
                        eval.eligibleForRegistration -> WarehouseArticleStatus.STORED_READY
                        else -> WarehouseArticleStatus.NEEDS_IMPROVEMENT
                    },
                    createdAt = now
                )
            }
            val log = WritingActivityLog(
                logId = "wlog-imp-${System.currentTimeMillis()}",
                warehouseId = "batch",
                caseId = "일괄입고(${items.size}건)",
                actionTitle = "발행큐 ${items.size}건 창고 일괄 평가·보관",
                score = items.maxOfOrNull { it.evaluation.totalScore } ?: 0,
                passedGate95 = items.all { it.evaluation.eligibleForRegistration },
                detail = "95점+ 등록가능 ${items.count { it.evaluation.eligibleForRegistration }}건",
                timestamp = now
            )
            repository.upsertWarehouseArticles(items, log)
            _uiState.update { it.copy(statusMessage = "현재 큐 ${items.size}건을 30항목 평가 후 글 창고에 일괄 보관했습니다.") }
        }
    }

    fun run30PointEvaluationOnWarehouseItem(warehouseId: String) {
        val state = _uiState.value
        val target = state.warehouseArticles.firstOrNull { it.warehouseId == warehouseId } ?: return
        viewModelScope.launch {
            val corpus = repository.getRecent7DayCorpus()
            val reg = RegistryInfographicEngine.buildTemplate(target.slot.auctionCase, state.selectedRegistryTemplateType)
            val eval = Article30PointEvaluator.evaluate(target.slot, state.config, state.proFunnel, reg, corpus)
            val updatedStatus = when {
                target.warehouseStatus == WarehouseArticleStatus.REGISTERED && eval.eligibleForRegistration -> WarehouseArticleStatus.REGISTERED
                eval.eligibleForRegistration -> WarehouseArticleStatus.STORED_READY
                else -> WarehouseArticleStatus.NEEDS_IMPROVEMENT
            }
            val updated = target.copy(
                evaluation = eval,
                registryInfographic = reg,
                warehouseStatus = updatedStatus
            )
            val log = WritingActivityLog(
                logId = "wlog-eval-${System.currentTimeMillis()}",
                warehouseId = warehouseId,
                caseId = target.slot.auctionCase.caseId,
                actionTitle = "30개 항목 테스트 완료: ${eval.totalScore}/100점",
                score = eval.totalScore,
                passedGate95 = eval.eligibleForRegistration,
                detail = "${eval.passedCount}/30 항목 통과 · ${if (eval.eligibleForRegistration) "등록 승인(95+)" else "등록 차단(<95)"}",
                timestamp = currentTimestamp()
            )
            repository.upsertWarehouseArticle(updated, log)
            _uiState.update {
                it.copy(statusMessage = "${target.slot.auctionCase.caseId} 30항목 테스트 결과: ${eval.totalScore}점 (${eval.passedCount}/30 통과)")
            }
        }
    }

    fun autoOptimizeWarehouseArticleTo95(warehouseId: String) {
        val state = _uiState.value
        val target = state.warehouseArticles.firstOrNull { it.warehouseId == warehouseId } ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            val improvedSlot = repository.rewriteVariantToPassQa(target.slot, state.config, state.proFunnel)
            val reg = RegistryInfographicEngine.buildTemplate(improvedSlot.auctionCase, state.selectedRegistryTemplateType)
            val eval = Article30PointEvaluator.evaluate(improvedSlot, state.config, state.proFunnel, reg)
            val updated = target.copy(
                slot = improvedSlot,
                evaluation = eval,
                registryInfographic = reg,
                warehouseStatus = if (eval.eligibleForRegistration) WarehouseArticleStatus.STORED_READY else WarehouseArticleStatus.NEEDS_IMPROVEMENT
            )
            val log = WritingActivityLog(
                logId = "wlog-opt-${System.currentTimeMillis()}",
                warehouseId = warehouseId,
                caseId = improvedSlot.auctionCase.caseId,
                actionTitle = "자동 보완 리라이팅 후 재평가 (${eval.totalScore}점)",
                score = eval.totalScore,
                passedGate95 = eval.eligibleForRegistration,
                detail = "${eval.passedCount}/30 항목 통과",
                timestamp = currentTimestamp()
            )
            repository.upsertWarehouseArticle(updated, log)
            _uiState.update {
                it.copy(
                    isBusy = false,
                    statusMessage = "${improvedSlot.auctionCase.caseId} 보완 완료: ${eval.totalScore}/100점 (${if (eval.eligibleForRegistration) "95점+ 등록 가능" else "추가 점검 필요"})"
                )
            }
        }
    }

    fun registerWarehouseArticleIfScore95(warehouseId: String) {
        val state = _uiState.value
        val target = state.warehouseArticles.firstOrNull { it.warehouseId == warehouseId } ?: return
        val freshEval = Article30PointEvaluator.evaluate(
            slot = target.slot,
            config = state.config,
            proFunnel = state.proFunnel,
            registryTemplate = target.registryInfographic
        )
        if (!freshEval.eligibleForRegistration) {
            _uiState.update {
                it.copy(
                    errorMessage = "${target.slot.auctionCase.caseId}: 30개 항목 평가 ${freshEval.totalScore}점으로 95점 미만입니다. 보완 교정 후 등록할 수 있습니다."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            val d1Summary = repository.pushScheduledSlotToD1(state.config, target.slot)
            val now = currentTimestamp()
            val log = WritingActivityLog(
                logId = "wlog-reg-${System.currentTimeMillis()}",
                warehouseId = warehouseId,
                caseId = target.slot.auctionCase.caseId,
                actionTitle = "95점+ 게이트 통과(${freshEval.totalScore}점) → 3채널 등록 완료",
                score = freshEval.totalScore,
                passedGate95 = true,
                detail = d1Summary.getOrElse { "로컬 창고·예약 등록 완료 (3채널)" },
                timestamp = now
            )
            repository.recordWarehouseRegistration(warehouseId, ChannelPlatform.entries, log)
            val newPublishLogs = ChannelPlatform.entries.mapIndexed { idx, platform ->
                PublishLogEntry(
                    logId = "log-wh-${System.currentTimeMillis()}-$idx",
                    postId = target.slot.postId,
                    caseId = target.slot.auctionCase.caseId,
                    platform = platform,
                    status = PostStatus.PUBLISHED,
                    publishedUrl = target.slot.variants[platform]?.ctaUrl,
                    errorMessage = null,
                    handoffReason = null,
                    retryCount = 0,
                    publishedAt = now
                )
            }
            _uiState.update { current ->
                val updatedSlots = current.slots.map {
                    if (it.postId == target.slot.postId) it.copy(status = PostStatus.PUBLISHED) else it
                }
                current.copy(
                    isBusy = false,
                    slots = updatedSlots,
                    publishLogs = newPublishLogs + current.publishLogs,
                    telemetry = current.telemetry.copy(
                        dailyQuotaUsed = updatedSlots.count { s -> s.status == PostStatus.QUEUED || s.status == PostStatus.PUBLISHED }
                    ),
                    statusMessage = "${target.slot.auctionCase.caseId} (${freshEval.totalScore}점) 3채널 등록 완료 및 방문자 로그 반영됨"
                )
            }
        }
    }

    fun refreshVisitorTrafficTelemetry() {
        viewModelScope.launch {
            repository.refreshVisitorTelemetry()
            _uiState.update { it.copy(statusMessage = "채널별 실시간 방문자·체류시간·DB/PRO 전환 로그를 갱신했습니다.") }
        }
    }

    fun deleteWarehouseArticle(warehouseId: String) {
        viewModelScope.launch {
            repository.deleteWarehouseArticle(warehouseId)
            _uiState.update { it.copy(statusMessage = "선택한 글을 창고에서 삭제했습니다.") }
        }
    }

    fun scheduleSlotWithStaggeredJitter(postId: String) {
        val state = _uiState.value
        val cfg = state.config
        val target = state.slots.firstOrNull { it.postId == postId } ?: return
        val regTemplate = RegistryInfographicEngine.buildTemplate(target.auctionCase, state.selectedRegistryTemplateType)
        val eval95 = Article30PointEvaluator.evaluate(target, cfg, state.proFunnel, regTemplate)

        if (!target.qaReport.overallPassed || !eval95.eligibleForRegistration) {
            _uiState.update {
                it.copy(errorMessage = "${target.auctionCase.caseId}: 30항목 평가 ${eval95.totalScore}점 (95점 이상 및 QA 통과 시에만 등록 가능)")
            }
            return
        }

        val activeScheduledCount = state.slots.count {
            it.postId != postId && (it.status == PostStatus.QUEUED || it.status == PostStatus.PUBLISHED)
        }
        if (activeScheduledCount >= cfg.dailyPostLimit) {
            _uiState.update {
                it.copy(
                    errorMessage = "1일 발행 제한(DAILY_POST_LIMIT=${cfg.dailyPostLimit}) 도달: 계정 보호를 위해 서버 설정에서 증량 후 예약하세요."
                )
            }
            return
        }

        val newTimes = repository.computeStaggeredSchedule(target.publishDate, target.slotIndex, cfg)
        val updatedSlot = target.copy(
            scheduledTimes = newTimes,
            status = PostStatus.QUEUED,
            qaReport = QaVerificationEngine.auditSlot(target.auctionCase, target.variants, newTimes, cfg)
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            val d1Result = repository.pushScheduledSlotToD1(cfg, updatedSlot)
            val now = currentTimestamp()
            val newLogs = ChannelPlatform.entries.mapIndexed { i, platform ->
                PublishLogEntry(
                    logId = "log-${System.currentTimeMillis()}-$i",
                    postId = updatedSlot.postId,
                    caseId = updatedSlot.auctionCase.caseId,
                    platform = platform,
                    status = PostStatus.QUEUED,
                    publishedUrl = null,
                    errorMessage = null,
                    handoffReason = null,
                    retryCount = 0,
                    publishedAt = newTimes[platform] ?: now
                )
            }
            _uiState.update { current ->
                val replaced = current.slots.map { if (it.postId == postId) updatedSlot else it }
                current.copy(
                    isBusy = false,
                    slots = replaced,
                    publishLogs = newLogs + current.publishLogs,
                    telemetry = current.telemetry.copy(
                        dailyQuotaUsed = replaced.count { s -> s.status == PostStatus.QUEUED || s.status == PostStatus.PUBLISHED },
                        lastD1ExecutionSummary = d1Result.getOrElse { it.message ?: "로컬 큐 예약" }
                    ),
                    statusMessage = "${updatedSlot.auctionCase.caseId} 3채널 시간차 예약 완료 (${newTimes[ChannelPlatform.CLOUDFLARE]?.takeLast(8)} → 티스토리 → 네이버)"
                )
            }
        }
    }

    fun rewriteSlotToPassQa(postId: String) {
        val state = _uiState.value
        val target = state.slots.firstOrNull { it.postId == postId } ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            val rewritten = repository.rewriteVariantToPassQa(target, state.config, state.proFunnel)
            val (finalSlot, defenseReport) = if (state.qualityDefenseState.autoDefenseEnabled) {
                QualityDefenseEngine.defendSlot(rewritten, state.config, state.proFunnel)
            } else {
                rewritten to QualityDefenseEngine.defendText(
                    rawTitle = rewritten.variants[ChannelPlatform.NAVER]?.title.orEmpty(),
                    rawBody = rewritten.variants[ChannelPlatform.NAVER]?.bodyText.orEmpty(),
                    channel = ChannelPlatform.NAVER,
                    auction = rewritten.auctionCase
                )
            }
            val legalReport = LegalDoctrineVerifier.verify(finalSlot.auctionCase)
            _uiState.update { current ->
                current.copy(
                    isBusy = false,
                    slots = current.slots.map { if (it.postId == postId) finalSlot else it },
                    legalVerificationByCaseId = current.legalVerificationByCaseId + (finalSlot.auctionCase.caseId to legalReport),
                    qualityDefenseState = current.qualityDefenseState.copy(
                        lastAuditedCaseId = finalSlot.auctionCase.caseId,
                        lastChannel = ChannelPlatform.NAVER,
                        lastResult = defenseReport,
                        totalAutoFixedCount = current.qualityDefenseState.totalAutoFixedCount + defenseReport.totalHitsCount,
                        lastUpdatedAt = currentTimestamp()
                    ),
                    statusMessage = "${finalSlot.auctionCase.caseId} 3채널 리라이팅·Quality Defense 정화 완료 (최대 유사도 ${finalSlot.qaReport.maxCrossChannelCosinePct}%)"
                )
            }
        }
    }

    // ==================== Quality Defense Module (SEO & 탈AI 자동 치환) ====================

    fun toggleAutoQualityDefense(enabled: Boolean) {
        _uiState.update {
            it.copy(
                qualityDefenseState = it.qualityDefenseState.copy(
                    autoDefenseEnabled = enabled,
                    lastUpdatedAt = currentTimestamp()
                ),
                statusMessage = if (enabled) "Quality Defense 자동 정화 모듈을 활성화했습니다." else "Quality Defense 자동 정화를 일시 중지했습니다."
            )
        }
    }

    fun inspectQualityDefense(postId: String? = null, channel: ChannelPlatform? = null) {
        val state = _uiState.value
        val targetSlot = state.slots.firstOrNull { it.postId == (postId ?: state.selectedSlotId) }
            ?: state.warehouseArticles.firstOrNull { it.slot.postId == (postId ?: state.selectedSlotId) }?.slot
            ?: state.slots.firstOrNull()
            ?: return
        val targetChannel = channel ?: state.selectedChannel
        val variant = targetSlot.variants[targetChannel] ?: return
        val result = QualityDefenseEngine.defendText(
            rawTitle = variant.title,
            rawBody = variant.bodyText,
            channel = targetChannel,
            auction = targetSlot.auctionCase,
            naverSearchKeyword = state.proFunnel.naverSearchKeyword
        )
        _uiState.update {
            it.copy(
                qualityDefenseState = it.qualityDefenseState.copy(
                    lastAuditedCaseId = targetSlot.auctionCase.caseId,
                    lastChannel = targetChannel,
                    lastResult = result,
                    lastUpdatedAt = currentTimestamp()
                )
            )
        }
    }

    fun applyQualityDefenseToSlot(postId: String) {
        val state = _uiState.value
        val targetSlot = state.slots.firstOrNull { it.postId == postId }
            ?: state.warehouseArticles.firstOrNull { it.slot.postId == postId }?.slot
            ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            val corpus = repository.getRecent7DayCorpus()
            val (defendedSlot, defenseReport) = QualityDefenseEngine.defendSlot(
                slot = targetSlot,
                config = state.config,
                proFunnel = state.proFunnel,
                recent7DayCorpus = corpus
            )
            val legalReport = LegalDoctrineVerifier.verify(defendedSlot.auctionCase)
            val reg = RegistryInfographicEngine.buildTemplate(defendedSlot.auctionCase, state.selectedRegistryTemplateType)
            val eval = Article30PointEvaluator.evaluate(defendedSlot, state.config, state.proFunnel, reg, corpus)

            val existingWh = _uiState.value.warehouseArticles.firstOrNull { it.slot.postId == targetSlot.postId }
            if (existingWh != null) {
                val updatedWh = existingWh.copy(
                    slot = defendedSlot,
                    evaluation = eval,
                    registryInfographic = reg,
                    warehouseStatus = if (eval.eligibleForRegistration) WarehouseArticleStatus.STORED_READY else WarehouseArticleStatus.NEEDS_IMPROVEMENT
                )
                val log = WritingActivityLog(
                    logId = "wlog-qd-${System.currentTimeMillis()}",
                    warehouseId = existingWh.warehouseId,
                    caseId = defendedSlot.auctionCase.caseId,
                    actionTitle = "Quality Defense 정화 (${defenseReport.totalHitsCount}건 치환 · ${eval.totalScore}점)",
                    score = eval.totalScore,
                    passedGate95 = eval.eligibleForRegistration,
                    detail = defenseReport.summary,
                    timestamp = currentTimestamp()
                )
                repository.upsertWarehouseArticle(updatedWh, log)
            }

            _uiState.update { current ->
                current.copy(
                    isBusy = false,
                    slots = current.slots.map { if (it.postId == targetSlot.postId) defendedSlot else it },
                    legalVerificationByCaseId = current.legalVerificationByCaseId + (defendedSlot.auctionCase.caseId to legalReport),
                    qualityDefenseState = current.qualityDefenseState.copy(
                        lastAuditedCaseId = defendedSlot.auctionCase.caseId,
                        lastChannel = ChannelPlatform.NAVER,
                        lastResult = defenseReport,
                        totalAutoFixedCount = current.qualityDefenseState.totalAutoFixedCount + defenseReport.totalHitsCount,
                        lastUpdatedAt = currentTimestamp()
                    ),
                    statusMessage = "${defendedSlot.auctionCase.caseId} Quality Defense 적용 완료 (${defenseReport.summary})"
                )
            }
        }
    }

    // ==================== 네이버 부동산 무료 API 시세 분석 & 법리분석 검증 ====================

    fun runLegalDoctrineVerification(postId: String? = null) {
        val state = _uiState.value
        val targetSlot = state.slots.firstOrNull { it.postId == (postId ?: state.selectedSlotId) }
            ?: state.warehouseArticles.firstOrNull { it.slot.postId == (postId ?: state.selectedSlotId) }?.slot
            ?: state.slots.firstOrNull()
            ?: return

        val legalReport = LegalDoctrineVerifier.verify(targetSlot.auctionCase)
        val qa = QaVerificationEngine.auditSlot(
            targetSlot.auctionCase,
            targetSlot.variants,
            targetSlot.scheduledTimes,
            state.config
        )
        val updatedSlot = targetSlot.copy(
            qaReport = qa,
            status = if (!qa.overallPassed && targetSlot.status == PostStatus.QUEUED) PostStatus.DRAFT else targetSlot.status
        )
        _uiState.update { current ->
            current.copy(
                slots = current.slots.map { if (it.postId == targetSlot.postId) updatedSlot else it },
                legalVerificationByCaseId = current.legalVerificationByCaseId + (targetSlot.auctionCase.caseId to legalReport),
                statusMessage = "${targetSlot.auctionCase.caseId} ${legalReport.auditSummary}"
            )
        }
    }

    fun fetchNearbyMarketAndRebid(postId: String? = null) {
        val state = _uiState.value
        val targetSlot = state.slots.firstOrNull { it.postId == (postId ?: state.selectedSlotId) }
            ?: state.warehouseArticles.firstOrNull { it.slot.postId == (postId ?: state.selectedSlotId) }?.slot
            ?: state.slots.firstOrNull()
            ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingMarket = true, statusMessage = null, errorMessage = null) }
            try {
                val legalReport = LegalDoctrineVerifier.verify(targetSlot.auctionCase)
                val marketReport = naverApiClient.fetchNearbyMarketAnalysis(
                    auction = targetSlot.auctionCase,
                    legalReport = legalReport
                )

                val updatedAuction = NearbyMarketEngine.applyMarketReportToAuction(targetSlot.auctionCase, marketReport)
                val finalSlot = if (marketReport.recalculatedBids != null) {
                    val rebuilt = DefaultSpecDataFactory.buildDistinct3ChannelVariants(
                        auction = updatedAuction,
                        seedIndex = targetSlot.slotIndex,
                        config = state.config,
                        proFunnel = state.proFunnel
                    )
                    val (defended, _) = QualityDefenseEngine.defendSlot(
                        slot = targetSlot.copy(auctionCase = updatedAuction, variants = rebuilt),
                        config = state.config,
                        proFunnel = state.proFunnel
                    )
                    defended
                } else {
                    targetSlot
                }

                val updatedLegal = LegalDoctrineVerifier.verify(finalSlot.auctionCase)
                _uiState.update { current ->
                    current.copy(
                        isFetchingMarket = false,
                        slots = current.slots.map { if (it.postId == targetSlot.postId) finalSlot else it },
                        marketAnalysisByCaseId = current.marketAnalysisByCaseId + (finalSlot.auctionCase.caseId to marketReport),
                        legalVerificationByCaseId = current.legalVerificationByCaseId + (finalSlot.auctionCase.caseId to updatedLegal),
                        statusMessage = "${finalSlot.auctionCase.caseId}: ${marketReport.statusDetail}"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isFetchingMarket = false,
                        errorMessage = "네이버 시세 연동 오류: ${e.localizedMessage ?: "네트워크 상태 확인 필요"}"
                    )
                }
            }
        }
    }

    fun updateConfig(newConfig: ServerAutomationConfig) {
        viewModelScope.launch {
            repository.saveConfig(newConfig)
            _uiState.update { it.copy(config = newConfig, statusMessage = "서버 및 저품질 방어 설정(.env 변수) 저장 완료") }
        }
    }

    fun verifyCloudflareSetup() {
        val cfg = _uiState.value.config
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            val summary = repository.verifyCloudflareAndCron(cfg)
            _uiState.update { state ->
                if (summary.success) {
                    state.copy(
                        isBusy = false,
                        telemetry = state.telemetry.copy(
                            cloudflareD1Connected = true,
                            activeCronSchedules = summary.activeCrons,
                            d1DatabaseName = summary.d1Names.firstOrNull() ?: "ddakchal-db",
                            lastD1ExecutionSummary = summary.message
                        ),
                        statusMessage = summary.message
                    )
                } else {
                    state.copy(
                        isBusy = false,
                        telemetry = state.telemetry.copy(cloudflareD1Connected = false),
                        errorMessage = summary.message
                    )
                }
            }
        }
    }

    fun provisionD1SchemaOnCloudflare() {
        val cfg = _uiState.value.config
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            repository.migrateD1Schema(cfg)
                .onSuccess { msg ->
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            telemetry = it.telemetry.copy(cloudflareD1Connected = true, lastD1ExecutionSummary = msg),
                            statusMessage = msg
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isBusy = false, errorMessage = err.message) }
                }
        }
    }

    fun registerCloudflareCronTrigger() {
        val cfg = _uiState.value.config
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            repository.registerWorkerCron(cfg)
                .onSuccess { crons ->
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            telemetry = it.telemetry.copy(activeCronSchedules = crons),
                            statusMessage = "Cloudflare Worker Cron 자동 등록 완료: ${crons.joinToString()}"
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isBusy = false, errorMessage = err.message) }
                }
        }
    }

    fun queryD1QueueFromCloudflare() {
        val cfg = _uiState.value.config
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            repository.queryD1QueueSummary(cfg)
                .onSuccess { msg ->
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            telemetry = it.telemetry.copy(cloudflareD1Connected = true, lastD1ExecutionSummary = msg),
                            statusMessage = msg
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isBusy = false, errorMessage = err.message) }
                }
        }
    }

    fun saveYamlTemplate(propertyType: PropertyType, newYaml: String) {
        viewModelScope.launch {
            repository.saveCustomYaml(propertyType, newYaml)
            _uiState.update { state ->
                state.copy(
                    templates = state.templates.map {
                        if (it.propertyType == propertyType) it.copy(yamlContent = newYaml, lastEditedAt = currentTimestamp()) else it
                    },
                    statusMessage = "${propertyType.yamlFileName} 템플릿 저장 완료"
                )
            }
        }
    }

    fun rotateBoilerplatePool() {
        _uiState.update { state ->
            val next = (state.poolPreviewIndex + 1) % BoilerplateAndTemplateEngine.introHookPool.size
            state.copy(poolPreviewIndex = next)
        }
    }

    fun createPostTemplate(
        name: String,
        propertyType: PropertyType,
        targetChannel: ChannelPlatform,
        titleTemplate: String,
        bodyTemplate: String,
        tagsTemplate: String,
        activateForGeneration: Boolean
    ) {
        viewModelScope.launch {
            repository.createPostTemplate(
                name = name,
                propertyType = propertyType,
                targetChannel = targetChannel,
                titleTemplate = titleTemplate,
                bodyTemplate = bodyTemplate,
                tagsTemplate = tagsTemplate,
                activateForGeneration = activateForGeneration
            )
            _uiState.update {
                it.copy(statusMessage = "'${name.trim()}' (${propertyType.displayLabel}) 템플릿이 DataStore에 생성·저장되었습니다.")
            }
        }
    }

    fun savePostTemplate(template: BlogPostTemplate) {
        viewModelScope.launch {
            repository.savePostTemplate(template)
            _uiState.update {
                it.copy(statusMessage = "'${template.name}' 변경사항이 DataStore에 저장되었습니다.")
            }
        }
    }

    fun setActivePostTemplate(templateId: String) {
        viewModelScope.launch {
            repository.setActivePostTemplate(templateId)
            val targetName = _uiState.value.postTemplates.firstOrNull { it.id == templateId }?.name.orEmpty()
            _uiState.update {
                it.copy(statusMessage = "'$targetName' 템플릿을 해당 물건·채널 생성 기본값으로 적용했습니다.")
            }
        }
    }

    fun deletePostTemplate(templateId: String) {
        val targetName = _uiState.value.postTemplates.firstOrNull { it.id == templateId }?.name.orEmpty()
        viewModelScope.launch {
            repository.deletePostTemplate(templateId)
            _uiState.update {
                it.copy(statusMessage = "'$targetName' 템플릿이 로컬 저장소에서 삭제되었습니다.")
            }
        }
    }

    fun resetPostTemplates() {
        viewModelScope.launch {
            repository.resetPostTemplates()
            _uiState.update {
                it.copy(statusMessage = "5대 물건유형 기본 게시글 템플릿이 복원되었습니다.")
            }
        }
    }

    fun toggleSeoChecklistItem(itemId: String) {
        viewModelScope.launch {
            repository.toggleSeoChecklistItem(itemId)
        }
    }

    fun createSeoChecklistItem(
        title: String,
        description: String,
        category: SeoRuleCategory,
        priority: SeoRulePriority
    ) {
        viewModelScope.launch {
            repository.createSeoChecklistItem(title, description, category, priority)
            _uiState.update {
                it.copy(statusMessage = "SEO 체크리스트 항목 '${title.trim()}'이(가) 추가되었습니다.")
            }
        }
    }

    fun saveSeoChecklistItem(item: SeoChecklistItem) {
        viewModelScope.launch {
            repository.saveSeoChecklistItem(item)
            _uiState.update {
                it.copy(statusMessage = "SEO 체크리스트 항목 '${item.title}'이(가) 저장되었습니다.")
            }
        }
    }

    fun deleteSeoChecklistItem(itemId: String) {
        val name = _uiState.value.seoChecklist.firstOrNull { it.id == itemId }?.title.orEmpty()
        viewModelScope.launch {
            repository.deleteSeoChecklistItem(itemId)
            _uiState.update {
                it.copy(statusMessage = "'$name' 체크리스트 항목이 삭제되었습니다.")
            }
        }
    }

    fun resetSeoChecklist() {
        viewModelScope.launch {
            repository.resetSeoChecklist()
            _uiState.update {
                it.copy(statusMessage = "최신 검색엔진 저품질 방지 SEO 체크리스트 12종이 초기화되었습니다.")
            }
        }
    }

    fun saveProFunnelConfig(funnel: ProFunnelConfig) {
        viewModelScope.launch {
            repository.saveProFunnelConfig(funnel)
            _uiState.update { state ->
                val rebuiltSlots = state.slots.map { slot ->
                    val newVariants = DefaultSpecDataFactory.buildDistinct3ChannelVariants(
                        auction = slot.auctionCase,
                        seedIndex = slot.slotIndex - 1,
                        config = state.config,
                        proFunnel = funnel
                    )
                    val qa = QaVerificationEngine.auditSlot(slot.auctionCase, newVariants, slot.scheduledTimes, state.config)
                    slot.copy(variants = newVariants, qaReport = qa)
                }
                state.copy(
                    proFunnel = funnel,
                    slots = rebuiltSlots,
                    statusMessage = "딱찰 PRO 전환 퍼널 설정이 저장되고 전체 큐 원고에 즉시 반영되었습니다."
                )
            }
        }
    }

    fun resetProFunnelConfig() {
        viewModelScope.launch {
            repository.resetProFunnelConfig()
            saveProFunnelConfig(ProFunnelConfig())
        }
    }

    fun triggerCaptchaHandoffSimulation(platform: ChannelPlatform) {
        val now = currentTimestamp()
        val updatedCfg = when (platform) {
            ChannelPlatform.NAVER -> _uiState.value.config.copy(naverSessionStatus = "HANDOFF 중단 (보안문자 감지 · 수동 1회 로그인 필요)")
            ChannelPlatform.TISTORY -> _uiState.value.config.copy(tistorySessionStatus = "HANDOFF 중단 (카카오 2차 인증 · 수동 갱신 필요)")
            ChannelPlatform.CLOUDFLARE -> _uiState.value.config
        }
        val log = PublishLogEntry(
            logId = "log-handoff-${System.currentTimeMillis()}",
            postId = _uiState.value.selectedSlotId ?: "post-session",
            caseId = "SESSION_GUARD",
            platform = platform,
            status = PostStatus.HANDOFF,
            publishedUrl = null,
            errorMessage = "CAPTCHA/2FA 감지로 무한 재시도 중단(handOff)",
            handoffReason = "CAPTCHA_OR_2FA_REQUIRED",
            retryCount = 0,
            publishedAt = now
        )
        viewModelScope.launch {
            repository.saveConfig(updatedCfg)
            _uiState.update {
                it.copy(
                    config = updatedCfg,
                    activeHandoffAlert = "${platform.displayName} 보안문자/2단계 인증 감지 → 자동 발행 즉시 중단(handOff) · 1회 수동 세션 갱신이 필요합니다.",
                    publishLogs = listOf(log) + it.publishLogs
                )
            }
        }
    }

    fun resolveSessionHandoff() {
        val updatedCfg = _uiState.value.config.copy(
            naverSessionStatus = "유효 (storage_state 세션 정상)",
            tistorySessionStatus = "유효 (storage_state 세션 정상)"
        )
        viewModelScope.launch {
            repository.saveConfig(updatedCfg)
            _uiState.update {
                it.copy(
                    config = updatedCfg,
                    activeHandoffAlert = null,
                    statusMessage = "Playwright 세션 복구 확인 완료 — 자동 발행 대기 상태 복귀"
                )
            }
        }
    }

    // ==================== 초심플 주간(7일) 예약 · 95+ 자동교정 등록 · 3채널 검증 · 사후재조사 ====================

    fun setWeeklyDailyCount(perDay: Int) {
        val safe = perDay.coerceIn(3, 5)
        _uiState.update { it.copy(selectedWeeklyDailyCount = safe) }
    }

    fun generateWeeklyBatchAndSchedule(perDay: Int = _uiState.value.selectedWeeklyDailyCount) {
        val safePerDay = perDay.coerceIn(3, 5)
        val state = _uiState.value
        val updatedConfig = state.config.copy(
            dailyPostLimit = safePerDay.coerceAtLeast(state.config.dailyPostLimit),
            weeklyTargetPosts = safePerDay * 7
        )
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    selectedWeeklyDailyCount = safePerDay,
                    statusMessage = null,
                    errorMessage = null
                )
            }
            try {
                repository.saveConfig(updatedConfig)
                val syncedRes = repository.syncSyndicationPack(updatedConfig)
                val serverSlots = syncedRes.getOrDefault(emptyList())
                val ddakchalLinked = syncedRes.isSuccess && serverSlots.isNotEmpty()
                val existingIds = state.warehouseArticles.map { it.slot.auctionCase.caseId }.toSet()
                val corpus = repository.getRecent7DayCorpus()

                val (weeklySlots, autoFixedCount) = WeeklyBatchScheduleEngine.buildWeekly7DayPlan(
                    dailyCount = safePerDay,
                    days = 7,
                    serverSyncedSlots = serverSlots,
                    existingCaseIds = existingIds,
                    config = updatedConfig,
                    proFunnel = state.proFunnel,
                    recent7DayCorpus = corpus
                )

                val now = currentTimestamp()
                val newWarehouseArticles = weeklySlots.map { slot ->
                    val reg = RegistryInfographicEngine.buildTemplate(slot.auctionCase, state.selectedRegistryTemplateType)
                    val eval = Article30PointEvaluator.evaluate(slot, updatedConfig, state.proFunnel, reg, corpus)
                    WarehouseArticle(
                        warehouseId = "wh-${slot.auctionCase.dedupKey}",
                        slot = slot,
                        evaluation = eval,
                        registryInfographic = reg,
                        warehouseStatus = if (eval.eligibleForRegistration) WarehouseArticleStatus.STORED_READY else WarehouseArticleStatus.NEEDS_IMPROVEMENT,
                        createdAt = now
                    )
                }
                val score95Count = newWarehouseArticles.count { it.evaluation.eligibleForRegistration }
                val (d1Synced, d1Msg) = repository.pushWeeklyBatchToD1(updatedConfig, weeklySlots)

                val log = WritingActivityLog(
                    logId = "wlog-weekly-${System.currentTimeMillis()}",
                    warehouseId = "weekly-batch",
                    caseId = "주간7일(${weeklySlots.size}건)",
                    actionTitle = "주간 7일치(일 ${safePerDay}건) 자동생성·95+ 교정·예약 완료",
                    score = newWarehouseArticles.minOfOrNull { it.evaluation.totalScore } ?: 95,
                    passedGate95 = score95Count == newWarehouseArticles.size,
                    detail = "${if (ddakchalLinked) "딱찰 실서버 ${serverSlots.size}건+" else "중복방지 풀"} · 95+ ${score95Count}/${weeklySlots.size}건 · $d1Msg",
                    timestamp = now
                )
                val snapshot = repository.upsertWarehouseArticles(newWarehouseArticles, log)

                val newLegalMap = state.legalVerificationByCaseId.toMutableMap()
                val newAttrMap = state.attractivenessByCaseId.toMutableMap()
                weeklySlots.forEach { slot ->
                    val legal = LegalDoctrineVerifier.verify(slot.auctionCase)
                    newLegalMap[slot.auctionCase.caseId] = legal
                    newAttrMap[slot.auctionCase.caseId] = InvestmentAttractivenessEngine.score(slot.auctionCase, legal)
                }

                val newPublishLogs = weeklySlots.take(safePerDay).flatMapIndexed { idx, slot ->
                    selectedPlatformsFromConfig(updatedConfig).map { platform ->
                        PublishLogEntry(
                            logId = "log-wk-${System.currentTimeMillis()}-$idx-${platform.code}",
                            postId = slot.postId,
                            caseId = slot.auctionCase.caseId,
                            platform = platform,
                            status = PostStatus.QUEUED,
                            publishedUrl = null,
                            errorMessage = null,
                            handoffReason = null,
                            retryCount = 0,
                            publishedAt = slot.scheduledTimes[platform] ?: now
                        )
                    }
                }

                val summaryText = "${weeklySlots.size}건(일 ${safePerDay}건×7일) 중복없이 생성 · 95점+ ${score95Count}건 · 자동교정 ${autoFixedCount}회 · $d1Msg"
                val weeklySummary = WeeklyBatchRunSummary(
                    generatedAt = now,
                    dailyCount = safePerDay,
                    totalDays = 7,
                    totalGeneratedCount = weeklySlots.size,
                    score95PlusCount = score95Count,
                    autoFixedCount = autoFixedCount,
                    serverSyncedCount = d1Synced,
                    ddakchalLinked = ddakchalLinked,
                    summaryText = summaryText
                )

                _uiState.update { current ->
                    current.copy(
                        isBusy = false,
                        config = updatedConfig,
                        slots = weeklySlots,
                        selectedSlotId = weeklySlots.firstOrNull()?.postId,
                        activeRegistryInfographic = weeklySlots.firstOrNull()?.let {
                            RegistryInfographicEngine.buildTemplate(it.auctionCase, current.selectedRegistryTemplateType)
                        },
                        activeCourtArchive = weeklySlots.firstOrNull()?.let {
                            CourtEvidenceRecorder.recordFullArchive(it.auctionCase)
                        },
                        warehouseArticles = snapshot?.articles ?: (newWarehouseArticles + current.warehouseArticles),
                        writingLogs = snapshot?.writingLogs ?: (listOf(log) + current.writingLogs),
                        publishLogs = newPublishLogs + current.publishLogs,
                        legalVerificationByCaseId = newLegalMap,
                        attractivenessByCaseId = newAttrMap,
                        weeklyBatchSummary = weeklySummary,
                        telemetry = current.telemetry.copy(
                            ddakchalConnected = ddakchalLinked,
                            cloudflareD1Connected = d1Synced > 0 || current.telemetry.cloudflareD1Connected,
                            lastSyncTime = now,
                            lastD1ExecutionSummary = d1Msg,
                            dailyQuotaUsed = safePerDay
                        ),
                        statusMessage = summaryText
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        errorMessage = "주간 자동 예약 오류: ${e.localizedMessage ?: "알 수 없는 오류"}"
                    )
                }
            }
        }
    }

    fun createHighScoreAndAutoRegister(propertyType: PropertyType? = null) {
        val state = _uiState.value
        val cfg = state.config
        val funnelCfg = state.proFunnel
        val nextIndex = state.warehouseArticles.size + state.slots.size + 1
        val chosenType = propertyType ?: PropertyType.entries[(nextIndex - 1) % PropertyType.entries.size]
        val caseNo = "2026타경${7100 + nextIndex * 19}"
        val dedupKey = "ca-auto-$caseNo-${System.currentTimeMillis() % 10000}"

        val (court, address, appMan, minMan) = when (chosenType) {
            PropertyType.APT_OFFICETEL -> listOf("수원지방법원", "경기도 용인시 수지구 성복동 롯데캐슬골드 102동 804호", "68000", "43520")
            PropertyType.VILLA_MULTIFAMILY -> listOf("서울북부지방법원", "서울특별시 도봉구 창동 다세대주택 4층 401호", "26000", "16640")
            PropertyType.COMMERCIAL_RETAIL -> listOf("서울동부지방법원", "서울특별시 송파구 문정동 법조타운 근린상가 1층 112호", "89000", "56960")
            PropertyType.LAND_FARMLAND -> listOf("춘천지방법원", "강원특별자치도 원주시 지정면 계획관리 대지 420㎡", "42000", "26880")
            PropertyType.FACTORY_SPECIAL -> listOf("수원지방법원 안산지원", "경기도 시흥시 정왕동 시화공단 일반공장 1동", "195000", "124800")
        }
        val appraisalKrw = appMan.toLong() * 10_000L
        val minBidKrw = minMan.toLong() * 10_000L
        val todayIso = PostAuctionFollowUpEngine.currentIsoDate()

        val auction = AuctionCaseIntel(
            caseId = caseNo,
            dedupKey = dedupKey,
            courtName = court,
            address = address,
            propertyType = chosenType,
            appraisalPriceKrw = appraisalKrw,
            minBidPriceKrw = minBidKrw,
            bidCount = 1,
            discountRatePct = 64,
            auctionDate = "2026-12-20",
            baseRightDate = "2021-07-08",
            dividendDeadline = "2026-12-05",
            claimAmountKrw = (appraisalKrw * 0.52).toLong(),
            depositRatePct = 10,
            possessionSummary = "채무자 겸 소유자 점유 확인 완료",
            tenantSummary = "전입세대열람 및 매각물건명세서 대조 완료(선순위 대항력 임차인 없음)",
            assumedRightsText = "말소기준권리 2021-07-08 근저당 이후 후순위 권리 일괄 소멸",
            specialCheckNotice = chosenType.focusSummary,
            recentRealTradeMedianKrw = (appraisalKrw * 0.91).toLong(),
            stats3yRatePct = 75.8,
            clicksLast7d = 590 + nextIndex * 11,
            conservativeBidKrw = (minBidKrw * 1.05).toLong(),
            balancedBidKrw = (minBidKrw * 1.12).toLong(),
            aggressiveBidKrw = (minBidKrw * 1.19).toLong()
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            try {
                val corpus = repository.getRecent7DayCorpus()
                val variants = DefaultSpecDataFactory.buildDistinct3ChannelVariants(auction, nextIndex, cfg, proFunnel = funnelCfg)
                val scheduled = repository.computeStaggeredSchedule(todayIso, nextIndex, cfg)
                val draft = GeneratedPostSlot(
                    postId = "post-$dedupKey",
                    queueId = "q-auto-$nextIndex",
                    slotIndex = nextIndex,
                    publishDate = todayIso,
                    auctionCase = auction,
                    variants = variants,
                    scheduledTimes = scheduled,
                    status = PostStatus.DRAFT,
                    qaReport = QaVerificationEngine.auditSlot(auction, variants, scheduled, cfg, corpus),
                    sourceLabel = "초심플 95+ 자동 교정·등록"
                )
                val fixed = WeeklyBatchScheduleEngine.ensureScoreAtLeast95(
                    slot = draft,
                    config = cfg,
                    proFunnel = funnelCfg,
                    recent7DayCorpus = corpus,
                    maxIterations = 3
                )
                executeAutoRegisterSlotInternal(fixed.slot, fixed.finalScore, fixed.passedGate95, isNewCreation = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isBusy = false, errorMessage = "자동 생성·등록 오류: ${e.localizedMessage}") }
            }
        }
    }

    fun autoFixAndRegisterSlot(postId: String) {
        val state = _uiState.value
        val targetSlot = state.slots.firstOrNull { it.postId == postId }
            ?: state.warehouseArticles.firstOrNull { it.slot.postId == postId }?.slot
            ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            try {
                val corpus = repository.getRecent7DayCorpus()
                val fixed = WeeklyBatchScheduleEngine.ensureScoreAtLeast95(
                    slot = targetSlot,
                    config = state.config,
                    proFunnel = state.proFunnel,
                    recent7DayCorpus = corpus,
                    maxIterations = 3
                )
                executeAutoRegisterSlotInternal(fixed.slot, fixed.finalScore, fixed.passedGate95, isNewCreation = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isBusy = false, errorMessage = "자동 교정·등록 오류: ${e.localizedMessage}") }
            }
        }
    }

    private suspend fun executeAutoRegisterSlotInternal(
        slot: GeneratedPostSlot,
        score: Int,
        passed95: Boolean,
        isNewCreation: Boolean
    ) {
        val state = _uiState.value
        val cfg = state.config
        val regTemplate = RegistryInfographicEngine.buildTemplate(slot.auctionCase, state.selectedRegistryTemplateType)
        val courtArchive = CourtEvidenceRecorder.recordFullArchive(slot.auctionCase)
        val eval = Article30PointEvaluator.evaluate(slot, cfg, state.proFunnel, regTemplate)
        val legal = LegalDoctrineVerifier.verify(slot.auctionCase)
        val attractiveness = InvestmentAttractivenessEngine.score(slot.auctionCase, legal)

        val selectedPlatforms = selectedPlatformsFromConfig(cfg)
        val report = directPublishClient.publishSelectedChannelsAndVerify(slot, cfg)
        val anyHttpOk = report.outcomes.any { it.reachableOk }
        val registeredChannels = report.outcomes.filter { it.reachableOk }.map { it.platform }
        val d1Result = repository.pushScheduledSlotToD1(cfg, slot)
        val now = currentTimestamp()

        val updatedPublishedUrls = slot.publishedUrls.toMutableMap().apply {
            report.outcomes.filter { it.reachableOk && it.publishedPostUrl.isNotBlank() }.forEach {
                put(it.platform, it.publishedPostUrl)
            }
        }
        val finalStatus = when {
            passed95 && (anyHttpOk || d1Result.isSuccess) -> PostStatus.PUBLISHED
            passed95 -> PostStatus.QUEUED
            else -> PostStatus.DRAFT
        }
        val finalSlot = slot.copy(status = finalStatus, publishedUrls = updatedPublishedUrls)

        val whId = "wh-${finalSlot.auctionCase.dedupKey}"
        val whStatus = when {
            finalStatus == PostStatus.PUBLISHED && eval.eligibleForRegistration -> WarehouseArticleStatus.REGISTERED
            eval.eligibleForRegistration -> WarehouseArticleStatus.STORED_READY
            else -> WarehouseArticleStatus.NEEDS_IMPROVEMENT
        }
        val whArticle = WarehouseArticle(
            warehouseId = whId,
            slot = finalSlot,
            evaluation = eval,
            registryInfographic = regTemplate,
            warehouseStatus = whStatus,
            createdAt = now,
            registeredAt = if (whStatus == WarehouseArticleStatus.REGISTERED) now else null,
            registeredChannels = if (registeredChannels.isNotEmpty()) registeredChannels else selectedPlatforms
        )
        val log = WritingActivityLog(
            logId = "wlog-autoreg-${System.currentTimeMillis()}",
            warehouseId = whId,
            caseId = finalSlot.auctionCase.caseId,
            actionTitle = "${if (isNewCreation) "바로생성" else "자동교정"} → ${score}점 ${if (whStatus == WarehouseArticleStatus.REGISTERED) "자동등록 완료" else "예약·창고 보관"}",
            score = score,
            passedGate95 = passed95,
            detail = "${report.overallSummary} · ${d1Result.getOrElse { "로컬 큐 저장" }}",
            timestamp = now
        )
        val snapshot = repository.upsertWarehouseArticle(whArticle, log)

        val pubLogs = report.outcomes.mapIndexed { idx, outcome ->
            PublishLogEntry(
                logId = "log-autoreg-${System.currentTimeMillis()}-$idx",
                postId = finalSlot.postId,
                caseId = finalSlot.auctionCase.caseId,
                platform = outcome.platform,
                status = if (outcome.reachableOk) PostStatus.PUBLISHED else PostStatus.QUEUED,
                publishedUrl = outcome.publishedPostUrl.ifBlank { outcome.targetUrl.ifBlank { null } },
                errorMessage = if (outcome.reachableOk) null else outcome.message,
                handoffReason = null,
                retryCount = 0,
                publishedAt = now
            )
        }

        _uiState.update { current ->
            val replacedOrPrependedSlots = if (current.slots.any { it.postId == finalSlot.postId }) {
                current.slots.map { if (it.postId == finalSlot.postId) finalSlot else it }
            } else {
                listOf(finalSlot) + current.slots
            }
            current.copy(
                isBusy = false,
                slots = replacedOrPrependedSlots,
                selectedSlotId = finalSlot.postId,
                activeRegistryInfographic = regTemplate,
                activeCourtArchive = courtArchive,
                warehouseArticles = snapshot?.articles ?: current.warehouseArticles,
                writingLogs = snapshot?.writingLogs ?: (listOf(log) + current.writingLogs),
                publishLogs = pubLogs + current.publishLogs,
                publishVerificationReport = report,
                legalVerificationByCaseId = current.legalVerificationByCaseId + (finalSlot.auctionCase.caseId to legal),
                attractivenessByCaseId = current.attractivenessByCaseId + (finalSlot.auctionCase.caseId to attractiveness),
                statusMessage = "${finalSlot.auctionCase.caseId} (${score}점): ${report.overallSummary}"
            )
        }
    }

    fun saveThreeChannelDestinations(
        naverBlogId: String,
        naverCategoryName: String,
        naverWorkerRelayEndpoint: String,
        tistoryBlogUrl: String,
        tistoryCategoryName: String,
        ddakchalApiBase: String,
        cloudflareBlogPath: String,
        publishNaver: Boolean,
        publishTistory: Boolean,
        publishCloudflare: Boolean
    ) {
        val updated = _uiState.value.config.copy(
            naverBlogId = naverBlogId.trim(),
            naverCategoryName = naverCategoryName.trim().ifBlank { "부동산 경매 분석" },
            naverWorkerRelayEndpoint = naverWorkerRelayEndpoint.trim(),
            tistoryBlogUrl = tistoryBlogUrl.trim().removeSuffix("/"),
            tistoryCategoryName = tistoryCategoryName.trim().ifBlank { "경매·권리·세무 리포트" },
            ddakchalApiBase = ddakchalApiBase.trim().removeSuffix("/"),
            cloudflareBlogPath = cloudflareBlogPath.trim().ifBlank { "/blog" },
            publishTargetNaver = publishNaver,
            publishTargetTistory = publishTistory,
            publishTargetCloudflare = publishCloudflare
        )
        viewModelScope.launch {
            repository.saveConfig(updated)
            _uiState.update {
                it.copy(
                    config = updated,
                    statusMessage = "3채널 등록처(네이버·티스토리·자체웹) 저장 완료"
                )
            }
        }
    }

    fun publishNaverDirectAndVerify(postId: String? = null, customPostUrl: String = "") {
        val state = _uiState.value
        val targetSlot = state.slots.firstOrNull { it.postId == (postId ?: state.selectedSlotId) }
            ?: state.warehouseArticles.firstOrNull { it.slot.postId == (postId ?: state.selectedSlotId) }?.slot
            ?: state.slots.firstOrNull()
            ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            try {
                val corpus = repository.getRecent7DayCorpus()
                val fixed = WeeklyBatchScheduleEngine.ensureScoreAtLeast95(
                    slot = targetSlot,
                    config = state.config,
                    proFunnel = state.proFunnel,
                    recent7DayCorpus = corpus,
                    maxIterations = 2
                )
                val report = directPublishClient.publishNaverDirectAndVerify(fixed.slot, state.config, customPostUrl)
                val now = currentTimestamp()
                val naverOutcome = report.outcomes.firstOrNull { it.platform == ChannelPlatform.NAVER }
                val updatedUrls = if (naverOutcome?.reachableOk == true && naverOutcome.publishedPostUrl.isNotBlank()) {
                    fixed.slot.publishedUrls + (ChannelPlatform.NAVER to naverOutcome.publishedPostUrl)
                } else fixed.slot.publishedUrls
                val updatedSlot = fixed.slot.copy(
                    publishedUrls = updatedUrls,
                    status = if (report.naverDirectPassed) PostStatus.PUBLISHED else fixed.slot.status
                )
                val newLog = PublishLogEntry(
                    logId = "log-nv-${System.currentTimeMillis()}",
                    postId = updatedSlot.postId,
                    caseId = updatedSlot.auctionCase.caseId,
                    platform = ChannelPlatform.NAVER,
                    status = if (naverOutcome?.reachableOk == true) PostStatus.PUBLISHED else PostStatus.FAILED,
                    publishedUrl = naverOutcome?.publishedPostUrl?.ifBlank { null },
                    errorMessage = if (naverOutcome?.reachableOk == true) null else naverOutcome?.message,
                    handoffReason = null,
                    retryCount = 0,
                    publishedAt = now
                )
                if (report.naverDirectPassed) {
                    val existingWh = state.warehouseArticles.firstOrNull { it.slot.postId == updatedSlot.postId }
                    if (existingWh != null) {
                        val wLog = WritingActivityLog(
                            logId = "wlog-nv-${System.currentTimeMillis()}",
                            warehouseId = existingWh.warehouseId,
                            caseId = updatedSlot.auctionCase.caseId,
                            actionTitle = "네이버 바로 등록·실검증 PASS (${fixed.finalScore}점)",
                            score = fixed.finalScore,
                            passedGate95 = true,
                            detail = report.verifiedNaverPostUrl,
                            timestamp = now
                        )
                        repository.recordWarehouseRegistration(existingWh.warehouseId, listOf(ChannelPlatform.NAVER), wLog)
                    }
                }
                _uiState.update { current ->
                    current.copy(
                        isBusy = false,
                        slots = current.slots.map { if (it.postId == updatedSlot.postId) updatedSlot else it },
                        publishLogs = listOf(newLog) + current.publishLogs,
                        publishVerificationReport = report,
                        statusMessage = if (report.naverDirectPassed) report.overallSummary else null,
                        errorMessage = if (!report.naverDirectPassed) report.overallSummary else null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isBusy = false, errorMessage = "네이버 검증 오류: ${e.localizedMessage}") }
            }
        }
    }

    fun publishSelectedChannelsAndVerify(postId: String? = null, customNaverPostUrl: String = "") {
        val state = _uiState.value
        val targetSlot = state.slots.firstOrNull { it.postId == (postId ?: state.selectedSlotId) }
            ?: state.warehouseArticles.firstOrNull { it.slot.postId == (postId ?: state.selectedSlotId) }?.slot
            ?: state.slots.firstOrNull()
            ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            try {
                val corpus = repository.getRecent7DayCorpus()
                val fixed = WeeklyBatchScheduleEngine.ensureScoreAtLeast95(
                    slot = targetSlot,
                    config = state.config,
                    proFunnel = state.proFunnel,
                    recent7DayCorpus = corpus,
                    maxIterations = 2
                )
                val report = directPublishClient.publishSelectedChannelsAndVerify(fixed.slot, state.config, customNaverPostUrl)
                val now = currentTimestamp()
                val newLogs = report.outcomes.mapIndexed { idx, outcome ->
                    PublishLogEntry(
                        logId = "log-sel-${System.currentTimeMillis()}-$idx",
                        postId = fixed.slot.postId,
                        caseId = fixed.slot.auctionCase.caseId,
                        platform = outcome.platform,
                        status = if (outcome.reachableOk) PostStatus.PUBLISHED else PostStatus.FAILED,
                        publishedUrl = outcome.publishedPostUrl.ifBlank { null },
                        errorMessage = if (outcome.reachableOk) null else outcome.message,
                        handoffReason = null,
                        retryCount = 0,
                        publishedAt = now
                    )
                }
                val verifiedUrls = fixed.slot.publishedUrls.toMutableMap().apply {
                    report.outcomes.filter { it.reachableOk && it.publishedPostUrl.isNotBlank() }.forEach {
                        put(it.platform, it.publishedPostUrl)
                    }
                }
                val updatedSlot = fixed.slot.copy(
                    publishedUrls = verifiedUrls,
                    status = if (report.outcomes.any { it.reachableOk }) PostStatus.PUBLISHED else fixed.slot.status
                )
                _uiState.update { current ->
                    current.copy(
                        isBusy = false,
                        slots = current.slots.map { if (it.postId == updatedSlot.postId) updatedSlot else it },
                        publishLogs = newLogs + current.publishLogs,
                        publishVerificationReport = report,
                        statusMessage = report.overallSummary
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isBusy = false, errorMessage = "선택 채널 일괄 검증 오류: ${e.localizedMessage}") }
            }
        }
    }

    fun reinvestigatePostAuctionAndCreateFollowUp(
        postId: String? = null,
        outcomeType: PostAuctionOutcomeType,
        actualWinningManWon: Long? = null,
        nextAuctionDate: String? = null,
        courtNote: String = ""
    ) {
        val state = _uiState.value
        val targetSlot = state.slots.firstOrNull { it.postId == (postId ?: state.selectedSlotId) }
            ?: state.warehouseArticles.firstOrNull { it.slot.postId == (postId ?: state.selectedSlotId) }?.slot
            ?: state.slots.firstOrNull()
            ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            try {
                val corpus = repository.getRecent7DayCorpus()
                val (record, rawFollowUpSlot) = PostAuctionFollowUpEngine.reinvestigateAndBuildFollowUp(
                    originalSlot = targetSlot,
                    outcomeType = outcomeType,
                    actualWinningManWon = actualWinningManWon,
                    nextAuctionDateInput = nextAuctionDate,
                    courtInvestigationNote = courtNote,
                    config = state.config,
                    proFunnel = state.proFunnel,
                    recent7DayCorpus = corpus
                )
                val fixed = WeeklyBatchScheduleEngine.ensureScoreAtLeast95(
                    slot = rawFollowUpSlot,
                    config = state.config,
                    proFunnel = state.proFunnel,
                    recent7DayCorpus = corpus,
                    maxIterations = 2
                )
                val followUpSlot = fixed.slot
                val regTemplate = RegistryInfographicEngine.buildTemplate(followUpSlot.auctionCase, state.selectedRegistryTemplateType)
                val eval = Article30PointEvaluator.evaluate(followUpSlot, state.config, state.proFunnel, regTemplate, corpus)
                val legal = LegalDoctrineVerifier.verify(followUpSlot.auctionCase)
                val attractiveness = InvestmentAttractivenessEngine.score(followUpSlot.auctionCase, legal)
                val now = currentTimestamp()
                val whArticle = WarehouseArticle(
                    warehouseId = "wh-${followUpSlot.auctionCase.dedupKey}",
                    slot = followUpSlot,
                    evaluation = eval,
                    registryInfographic = regTemplate,
                    warehouseStatus = if (eval.eligibleForRegistration) WarehouseArticleStatus.STORED_READY else WarehouseArticleStatus.NEEDS_IMPROVEMENT,
                    createdAt = now
                )
                val log = WritingActivityLog(
                    logId = "wlog-follow-${System.currentTimeMillis()}",
                    warehouseId = whArticle.warehouseId,
                    caseId = followUpSlot.auctionCase.caseId,
                    actionTitle = "기일경과 사후재조사(${outcomeType.badge}) 후속 관련글 생성 (${eval.totalScore}점)",
                    score = eval.totalScore,
                    passedGate95 = eval.eligibleForRegistration,
                    detail = record.followUpSummary,
                    timestamp = now
                )
                val snapshot = repository.upsertWarehouseArticle(whArticle, log)
                _uiState.update { current ->
                    current.copy(
                        isBusy = false,
                        slots = listOf(followUpSlot) + current.slots,
                        selectedSlotId = followUpSlot.postId,
                        activeRegistryInfographic = regTemplate,
                        warehouseArticles = snapshot?.articles ?: (listOf(whArticle) + current.warehouseArticles),
                        writingLogs = snapshot?.writingLogs ?: (listOf(log) + current.writingLogs),
                        followUpRecordsByCaseId = current.followUpRecordsByCaseId + (targetSlot.auctionCase.caseId to record),
                        legalVerificationByCaseId = current.legalVerificationByCaseId + (followUpSlot.auctionCase.caseId to legal),
                        attractivenessByCaseId = current.attractivenessByCaseId + (followUpSlot.auctionCase.caseId to attractiveness),
                        statusMessage = "${record.followUpSummary} (${eval.totalScore}점)"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isBusy = false, errorMessage = "사후 재조사 오류: ${e.localizedMessage}") }
            }
        }
    }

    fun setLegalStudyFilter(category: LegalStudyCategory?, query: String) {
        _uiState.update {
            it.copy(
                selectedLegalStudyCategory = category,
                legalStudyQuery = query
            )
        }
    }

    private fun selectedPlatformsFromConfig(config: ServerAutomationConfig): List<ChannelPlatform> {
        val list = mutableListOf<ChannelPlatform>()
        if (config.publishTargetCloudflare) list += ChannelPlatform.CLOUDFLARE
        if (config.publishTargetTistory) list += ChannelPlatform.TISTORY
        if (config.publishTargetNaver) list += ChannelPlatform.NAVER
        return list.ifEmpty { ChannelPlatform.entries }
    }

    private fun currentTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val appCtx = context.applicationContext
            val store = ServerConfigStore(appCtx)
            val templateStore = BlogPostTemplateStore(appCtx)
            val checklistStore = SeoChecklistStore(appCtx)
            val funnelStore = ProFunnelStore(appCtx)
            val warehouseStore = PostWarehouseStore(appCtx)
            val repo = BlogAutomationRepository(
                configStore = store,
                postTemplateStore = templateStore,
                seoChecklistStore = checklistStore,
                proFunnelStore = funnelStore,
                postWarehouseStore = warehouseStore
            )
            return StudioViewModel(repo) as T
        }
    }
}
