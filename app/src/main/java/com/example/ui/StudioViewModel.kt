package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.AiCaseSearchAdvisor
import com.example.ai.AiCaseSearchOutcome
import com.example.ai.AiEvaluationOutcome
import com.example.ai.AiSeoGeoAdvisor
import com.example.ai.FirebaseAiCaseSearchService
import com.example.ai.FirebaseAiSeoGeoService
import com.example.ai.GoogleAccountAuthHelper
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
import com.example.engine.ChannelIndexRiskEngine
import com.example.engine.CourtEvidenceRecorder
import com.example.engine.DefaultSpecDataFactory
import com.example.engine.HtmlExportEngine
import com.example.engine.InvestmentAttractivenessEngine
import com.example.engine.LegalDoctrineVerifier
import com.example.engine.NearbyMarketEngine
import com.example.engine.PostAuctionFollowUpEngine
import com.example.engine.PrivacySanitizerEngine
import com.example.engine.QaVerificationEngine
import com.example.engine.QualityDefenseEngine
import com.example.engine.RegistryInfographicEngine
import com.example.engine.SecurityAuditEngine
import com.example.engine.SeoGuideEngine
import com.example.engine.TopicSelectionEngine
import com.example.engine.WeeklyBatchScheduleEngine
import com.example.model.AdminPipelineStageStatus
import com.example.model.AdminPipelineStepLog
import com.example.model.AiCaseSearchUiState
import com.example.model.AiEvalUiState
import com.example.model.AuctionCaseIntel
import com.example.model.BlogPostTemplate
import com.example.model.ChannelPlatform
import com.example.model.ChannelProbeResult
import com.example.model.CourtFullArchiveRecord
import com.example.model.DdakchalFullDiagnosticReport
import com.example.model.FieldVisitRecord
import com.example.model.GeneratedPostSlot
import com.example.model.GoogleGeminiAccountState
import com.example.model.HtmlBatchBundleState
import com.example.model.InvestmentAttractivenessScore
import com.example.model.LegalDoctrineReport
import com.example.model.LegalStudyCategory
import com.example.model.LowQualityRiskLevel
import com.example.model.LowQualityRiskReport
import com.example.model.NaverAndMultiChannelPublishReport
import com.example.model.NaverAutoWriteMode
import com.example.model.NaverKeywordDiagnosisUiState
import com.example.model.NaverPublishStatusRecord
import com.example.model.NaverWriteMethodGuide
import com.example.model.NearbyMarketAnalysisReport
import com.example.model.PostAuctionFollowUpRecord
import com.example.model.PostAuctionOutcomeType
import com.example.model.PostStatus
import com.example.model.ProFunnelConfig
import com.example.model.VisitRecordSource
import com.example.model.PropertyTemplateYaml
import com.example.model.PropertyType
import com.example.model.PublishLogEntry
import com.example.model.QualityDefenseModuleState
import com.example.model.QueueAnalyticsSummary
import com.example.model.QueueSyncState
import com.example.model.RegistryInfographicTemplate
import com.example.model.RegistryInfographicTemplateType
import com.example.model.SecurityDashboardSummary
import com.example.model.SecurityGrade
import com.example.model.SeoChecklistItem
import com.example.model.SeoRuleCategory
import com.example.model.SeoRulePriority
import com.example.model.ServerAutomationConfig
import com.example.model.ServerDbDiscoveredItem
import com.example.model.ServerDbTableInfo
import com.example.model.ServerTelemetryState
import com.example.model.TodakAdminConsoleState
import com.example.model.TopicSelectionBoardState
import com.example.model.TopicSortCriteria
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    val publishLogs: List<PublishLogEntry> = emptyList(),
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
    val isMobileSimpleMode: Boolean = true,
    val queueSyncState: QueueSyncState = QueueSyncState.Idle,
    val queueAnalytics: QueueAnalyticsSummary = QueueAnalyticsSummary(
        totalSlots = 0,
        publishedCount = 0,
        queuedCount = 0,
        failedOrHandoffCount = 0,
        draftCount = 0,
        successRatePct = 0,
        failureRatePct = 0,
        dailySeries = emptyList(),
        channelStats = emptyList()
    ),
    val lowQualityRiskReport: LowQualityRiskReport? = null,
    val autoPublishPausedByRisk: Boolean = false,
    val autoPublishPauseReason: String? = null,
    val securityDashboard: SecurityDashboardSummary = SecurityAuditEngine.auditSecurity(
        config = ServerAutomationConfig(),
        slots = emptyList(),
        publishLogs = emptyList(),
        nowText = "초기 점검"
    ),
    val naverProbeResult: ChannelProbeResult? = null,
    val tistoryProbeResult: ChannelProbeResult? = null,
    val aiCaseSearchState: AiCaseSearchUiState = AiCaseSearchUiState.Idle,
    val naverKeywordDiagnosis: NaverKeywordDiagnosisUiState = NaverKeywordDiagnosisUiState.Idle,
    val htmlBatchBundle: HtmlBatchBundleState? = null,
    val topicSelectionBoard: TopicSelectionBoardState = TopicSelectionBoardState(),
    val ddakchalFullDiagnostic: DdakchalFullDiagnosticReport? = null,
    val googleAccount: GoogleGeminiAccountState = GoogleGeminiAccountState(),
    val naverPublishStatuses: List<NaverPublishStatusRecord> = emptyList(),
    val naverWriteGuides: List<NaverWriteMethodGuide> = emptyList(),
    val lastAiEngineUsed: String = "1순위 Google Gemini (Firebase AI)",
    val todakAdmin: TodakAdminConsoleState = TodakAdminConsoleState(),
    val pendingAutoPreviewSlotId: String? = null,
    val pendingAutoPreviewTab: Int = 0,
    val pendingAutoPreviewNonce: Long = 0L,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

class StudioViewModel(
    private val repository: BlogAutomationRepository,
    private val aiAdvisor: AiSeoGeoAdvisor = FirebaseAiSeoGeoService(),
    private val naverApiClient: NaverRealEstateApiClient = NaverRealEstateApiClient(),
    private val directPublishClient: DirectChannelPublishClient = DirectChannelPublishClient(),
    private val aiCaseSearchAdvisor: AiCaseSearchAdvisor = FirebaseAiCaseSearchService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()
    private val configSaveMutex = Mutex()

    init {
        val initialConfig = ServerAutomationConfig()
        val initialSlots = emptyList<GeneratedPostSlot>()
        val initialSeed = PostWarehouseStore.buildInitialSeed()
        val initialTopics = TopicSelectionEngine.rankCandidates(
            slots = initialSlots,
            config = initialConfig,
            proFunnel = ProFunnelConfig(),
            sortMode = TopicSortCriteria.BALANCED_4_PILLAR,
            nowText = currentTimestamp()
        )
        val initialLogs = _uiState.value.publishLogs
        val initialNaverStatuses = emptyList<NaverPublishStatusRecord>()
        val initialGuides = emptyList<NaverWriteMethodGuide>()
        val initialGoogleAccount = GoogleAccountAuthHelper.currentGoogleAccountState(
            primaryGeminiFirst = initialConfig.aiPrimaryGeminiFirst,
            grokFallbackEnabled = initialConfig.grokFallbackEnabled,
            grokModelName = initialConfig.grokModelName
        )
        val nowInit = currentTimestamp()
        val initialRiskReport = ChannelIndexRiskEngine.evaluate(
            config = initialConfig,
            slots = initialSlots,
            publishLogs = initialLogs,
            visitorLogs = initialSeed.visitorLogs,
            queueSyncState = QueueSyncState.Idle,
            naverKeywordReport = null,
            alreadyPausedByRisk = false,
            nowText = nowInit
        )

        _uiState.update {
            it.copy(
                slots = initialSlots,
                selectedSlotId = null,
                activeRegistryInfographic = null,
                activeCourtArchive = null,
                warehouseArticles = initialSeed.articles,
                writingLogs = initialSeed.writingLogs,
                visitorLogs = initialSeed.visitorLogs,
                legalVerificationByCaseId = emptyMap(),
                attractivenessByCaseId = emptyMap(),
                qualityDefenseState = QualityDefenseModuleState(
                    autoDefenseEnabled = true,
                    lastAuditedCaseId = "",
                    lastChannel = ChannelPlatform.NAVER,
                    lastResult = null,
                    lastUpdatedAt = nowInit
                ),
                telemetry = it.telemetry.copy(dailyQuotaUsed = 0),
                lowQualityRiskReport = initialRiskReport,
                autoPublishPausedByRisk = initialRiskReport.autoPublishPaused,
                autoPublishPauseReason = initialRiskReport.pauseReason,
                securityDashboard = SecurityAuditEngine.auditSecurity(
                    config = initialConfig,
                    slots = initialSlots,
                    publishLogs = it.publishLogs,
                    nowText = nowInit
                ),
                topicSelectionBoard = initialTopics,
                naverPublishStatuses = initialNaverStatuses,
                naverWriteGuides = initialGuides,
                googleAccount = initialGoogleAccount
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
                    val firstActive = reAuditedSlots.firstOrNull { it.postId == state.selectedSlotId } ?: reAuditedSlots.firstOrNull()
                    val nowText = currentTimestamp()
                    val riskReport = ChannelIndexRiskEngine.evaluate(
                        config = savedConfig,
                        slots = reAuditedSlots,
                        publishLogs = state.publishLogs,
                        visitorLogs = state.visitorLogs,
                        queueSyncState = state.queueSyncState,
                        naverKeywordReport = (state.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report,
                        alreadyPausedByRisk = state.autoPublishPausedByRisk,
                        nowText = nowText
                    )
                    state.copy(
                        config = savedConfig,
                        slots = reAuditedSlots,
                        lowQualityRiskReport = riskReport,
                        autoPublishPausedByRisk = riskReport.autoPublishPaused,
                        autoPublishPauseReason = riskReport.pauseReason,
                        securityDashboard = SecurityAuditEngine.auditSecurity(
                            config = savedConfig,
                            slots = reAuditedSlots,
                            publishLogs = state.publishLogs,
                            naverReachable = state.naverProbeResult?.reachableOk,
                            tistoryReachable = state.tistoryProbeResult?.reachableOk,
                            nowText = nowText
                        ),
                        topicSelectionBoard = TopicSelectionEngine.rankCandidates(
                            slots = reAuditedSlots,
                            config = savedConfig,
                            proFunnel = state.proFunnel,
                            sortMode = state.topicSelectionBoard.sortMode,
                            naverKeywordDiagnosis = (state.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report,
                            nowText = nowText
                        ),
                        naverPublishStatuses = buildNaverPublishStatuses(reAuditedSlots, state.publishLogs, savedConfig),
                        naverWriteGuides = directPublishClient.buildAlternateNaverWriteGuides(savedConfig, firstActive),
                        googleAccount = GoogleAccountAuthHelper.currentGoogleAccountState(
                            primaryGeminiFirst = savedConfig.aiPrimaryGeminiFirst,
                            grokFallbackEnabled = savedConfig.grokFallbackEnabled,
                            grokModelName = savedConfig.grokModelName,
                            statusOverride = state.googleAccount.lastSignInStatus.takeIf { state.googleAccount.isSignedIn }
                        )
                    )
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
                activeCourtArchive = targetSlot?.let {
                    CourtEvidenceRecorder.recordFullArchiveForSlot(it)
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

    fun attachCourtPhotoToSlot(
        postId: String,
        shotOrder: Int,
        photoUriString: String
    ) {
        val cleanUri = photoUriString.trim()
        if (cleanUri.isBlank()) return
        val safeIdx = (shotOrder - 1).coerceIn(0, 4)

        _uiState.update { state ->
            val targetSlot = state.slots.firstOrNull { it.postId == postId }
                ?: state.warehouseArticles.firstOrNull { it.slot.postId == postId }?.slot
                ?: return@update state
            val nextUrls = MutableList(maxOf(5, targetSlot.courtPhotoUrls.size)) { i ->
                targetSlot.courtPhotoUrls.getOrNull(i).orEmpty()
            }.apply {
                this[safeIdx] = cleanUri
            }
            val updatedSlot = targetSlot.copy(courtPhotoUrls = nextUrls)
            val updatedArchive = CourtEvidenceRecorder.recordFullArchiveForSlot(updatedSlot)
            state.copy(
                slots = state.slots.map { if (it.postId == postId) updatedSlot else it },
                warehouseArticles = state.warehouseArticles.map { art ->
                    if (art.slot.postId == postId) art.copy(slot = updatedSlot) else art
                },
                activeCourtArchive = if (state.selectedSlotId == postId) updatedArchive else state.activeCourtArchive,
                statusMessage = "${updatedSlot.auctionCase.caseId} 법원·현장 실사 사진 #${safeIdx + 1} 첨부 완료"
            )
        }
    }

    fun addFieldVisitRecord(
        postId: String,
        source: VisitRecordSource,
        visitedAt: String,
        occupancyStatus: String,
        findingsNote: String,
        attachedPhotoUri: String = ""
    ) {
        val cleanFindings = PrivacySanitizerEngine.sanitizeForPublicBlog(findingsNote.trim())
        if (cleanFindings.isBlank()) return
        val cleanOccupancy = PrivacySanitizerEngine.sanitizeForPublicBlog(occupancyStatus.trim().ifBlank { "현장 점유 확인" })
        val cleanDate = visitedAt.trim().ifBlank { PostAuctionFollowUpEngine.currentIsoDate() }

        _uiState.update { state ->
            val targetSlot = state.slots.firstOrNull { it.postId == postId }
                ?: state.warehouseArticles.firstOrNull { it.slot.postId == postId }?.slot
                ?: return@update state
            val newRecord = FieldVisitRecord(
                visitId = "visit-${System.currentTimeMillis()}",
                visitRound = "${targetSlot.visitRecords.size + 1}차 ${source.badgeText}",
                visitedAt = cleanDate,
                source = source,
                visitorLabel = "직접 현장 임장 기록",
                occupancyStatus = cleanOccupancy,
                findingsNote = cleanFindings,
                attachedPhotoUri = attachedPhotoUri.trim()
            )
            val updatedVisits = listOf(newRecord) + targetSlot.visitRecords
            val updatedSlot = targetSlot.copy(visitRecords = updatedVisits)
            val updatedArchive = CourtEvidenceRecorder.recordFullArchiveForSlot(updatedSlot)
            state.copy(
                slots = state.slots.map { if (it.postId == postId) updatedSlot else it },
                warehouseArticles = state.warehouseArticles.map { art ->
                    if (art.slot.postId == postId) art.copy(slot = updatedSlot) else art
                },
                activeCourtArchive = if (state.selectedSlotId == postId) updatedArchive else state.activeCourtArchive,
                statusMessage = "${updatedSlot.auctionCase.caseId} ${source.label} 방문기록 1건 추가 완료"
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

    fun setMobileSimpleMode(simple: Boolean) {
        _uiState.update { it.copy(isMobileSimpleMode = simple) }
    }

    fun toggleAutoMonitorPolling(enabled: Boolean) {
        _uiState.update {
            it.copy(
                telemetry = it.telemetry.copy(autoMonitorEnabled = enabled),
                statusMessage = if (enabled) "딱찰 서버 실시간 자동 모니터링을 켰습니다." else "실시간 자동 폴링을 일시 정지했습니다."
            )
        }
    }

    fun refreshDdakchalServerTelemetry(alsoSyncFeed: Boolean = true) {
        val cfg = _uiState.value.config
        if (cfg.ddakchalApiBase.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(telemetry = it.telemetry.copy(isPinging = true))
            }
            try {
                val triggerOutcome = repository.fetchTriggerStatusWithTelemetry(cfg)
                val now = currentTimestamp()
                val nowMs = System.currentTimeMillis()
                var mergedSlots = _uiState.value.slots
                var nextCursor = _uiState.value.telemetry.lastFeedCursorSince
                var latestLatency = triggerOutcome.latencyMs
                var latestHttpCode = triggerOutcome.httpStatusCode
                var latestError = triggerOutcome.errorMessage
                var connected = triggerOutcome.isSuccess

                if (alsoSyncFeed) {
                    val feedOutcome = repository.syncAnalysisFeedWithTelemetry(
                        config = cfg,
                        limit = 10,
                        since = nextCursor
                    )
                    if (feedOutcome.latencyMs > 0L) {
                        latestLatency = feedOutcome.latencyMs
                    }
                    if (feedOutcome.isSuccess) {
                        connected = true
                        latestHttpCode = feedOutcome.httpStatusCode
                        latestError = null
                        if (!feedOutcome.nextCursorSince.isNullOrBlank()) {
                            nextCursor = feedOutcome.nextCursorSince
                        }
                        val incoming = feedOutcome.data.orEmpty()
                        if (incoming.isNotEmpty()) {
                            val existingByKey = mergedSlots.associateBy { it.auctionCase.caseId }.toMutableMap()
                            incoming.forEach { slot ->
                                existingByKey[slot.auctionCase.caseId] = slot
                            }
                            mergedSlots = (incoming + mergedSlots.filter { old ->
                                incoming.none { inc -> inc.auctionCase.caseId == old.auctionCase.caseId }
                            })
                        }
                    } else if (!triggerOutcome.isSuccess) {
                        latestError = feedOutcome.errorMessage ?: triggerOutcome.errorMessage
                        latestHttpCode = feedOutcome.httpStatusCode
                    }
                }

                _uiState.update { current ->
                    val newLegal = current.legalVerificationByCaseId.toMutableMap()
                    val newAttr = current.attractivenessByCaseId.toMutableMap()
                    mergedSlots.forEach { slot ->
                        if (!newLegal.containsKey(slot.auctionCase.caseId)) {
                            val legal = LegalDoctrineVerifier.verify(slot.auctionCase)
                            newLegal[slot.auctionCase.caseId] = legal
                            newAttr[slot.auctionCase.caseId] = InvestmentAttractivenessEngine.score(slot.auctionCase, legal)
                        }
                    }
                    val updatedTrigger = triggerOutcome.data ?: current.telemetry.triggerStatus
                    current.copy(
                        slots = mergedSlots,
                        selectedSlotId = current.selectedSlotId ?: mergedSlots.firstOrNull()?.postId,
                        legalVerificationByCaseId = newLegal,
                        attractivenessByCaseId = newAttr,
                        telemetry = current.telemetry.copy(
                            ddakchalConnected = connected,
                            lastSyncTime = if (connected) now else current.telemetry.lastSyncTime,
                            apiLatencyMs = if (latestLatency > 0L) latestLatency else current.telemetry.apiLatencyMs,
                            lastSyncEpochMs = if (connected) nowMs else current.telemetry.lastSyncEpochMs,
                            lastHttpStatusCode = latestHttpCode,
                            lastErrorDetail = if (connected) null else latestError,
                            lastFeedCursorSince = nextCursor,
                            triggerStatus = updatedTrigger,
                            dailyQuotaUsed = mergedSlots.count { s ->
                                s.status == PostStatus.PUBLISHED || s.status == PostStatus.QUEUED
                            },
                            isPinging = false
                        )
                    )
                }

                // Sync real /api/v1/blog/queue via Retrofit without clobbering server ping status
                syncRealBlogQueueInternal(cfg = cfg, limit = 100, announceBanner = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        telemetry = it.telemetry.copy(
                            ddakchalConnected = false,
                            isPinging = false,
                            lastErrorDetail = e.localizedMessage ?: "서버 상태 확인 실패"
                        )
                    )
                }
            }
        }
    }

    fun syncRealBlogQueueWithRetrofit(limit: Int = 100, announceBanner: Boolean = true) {
        val cfg = _uiState.value.config
        viewModelScope.launch {
            syncRealBlogQueueInternal(cfg = cfg, limit = limit, announceBanner = announceBanner)
        }
    }

    private suspend fun syncRealBlogQueueInternal(
        cfg: ServerAutomationConfig,
        limit: Int,
        announceBanner: Boolean
    ) {
        _uiState.update { it.copy(queueSyncState = QueueSyncState.Loading) }
        val outcome = repository.syncBlogQueueWithRetrofit(cfg, limit)
        if (outcome.isSuccess && outcome.data != null) {
            val result = outcome.data
            _uiState.update { current ->
                val nextQueueState = if (result.items.isEmpty()) {
                    QueueSyncState.Empty(
                        message = "딱찰 서버 /api/v1/blog/queue 에 등록된 실데이터가 없습니다 (0건).",
                        syncedAt = result.syncedAt,
                        latencyMs = outcome.latencyMs,
                        sourceEndpoint = result.sourceEndpoint
                    )
                } else {
                    QueueSyncState.Loaded(
                        items = result.items,
                        analytics = result.analytics,
                        syncedAt = result.syncedAt,
                        latencyMs = outcome.latencyMs,
                        sourceEndpoint = result.sourceEndpoint
                    )
                }
                val riskReport = ChannelIndexRiskEngine.evaluate(
                    config = current.config,
                    slots = current.slots,
                    publishLogs = current.publishLogs,
                    visitorLogs = current.visitorLogs,
                    queueSyncState = nextQueueState,
                    naverKeywordReport = (current.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report,
                    alreadyPausedByRisk = current.autoPublishPausedByRisk,
                    nowText = currentTimestamp()
                )
                current.copy(
                    queueSyncState = nextQueueState,
                    queueAnalytics = result.analytics,
                    lowQualityRiskReport = riskReport,
                    autoPublishPausedByRisk = riskReport.autoPublishPaused,
                    autoPublishPauseReason = riskReport.pauseReason,
                    statusMessage = if (announceBanner) {
                        if (result.items.isEmpty()) {
                            "Retrofit /api/v1/blog/queue 조회 완료: 실데이터 0건"
                        } else {
                            "Retrofit /api/v1/blog/queue 실데이터 ${result.items.size}건 반영 (${outcome.latencyMs}ms)"
                        }
                    } else current.statusMessage
                )
            }
        } else {
            val tokenMissing = cfg.crawlerSyncToken.isBlank() || outcome.httpStatusCode in listOf(401, 403)
            val errMsg = outcome.errorMessage ?: "Retrofit /api/v1/blog/queue 조회 실패"
            _uiState.update { current ->
                current.copy(
                    queueSyncState = QueueSyncState.Error(
                        httpStatusCode = outcome.httpStatusCode,
                        message = errMsg,
                        tokenMissing = tokenMissing
                    ),
                    queueAnalytics = QueueAnalyticsSummary(
                        totalSlots = 0,
                        publishedCount = 0,
                        queuedCount = 0,
                        failedOrHandoffCount = 0,
                        draftCount = 0,
                        successRatePct = 0,
                        failureRatePct = 0,
                        dailySeries = emptyList(),
                        channelStats = emptyList()
                    ),
                    errorMessage = if (announceBanner) errMsg else current.errorMessage
                )
            }
        }
    }

    fun triggerCrawlAndSyncPipeline() {
        val cfg = _uiState.value.config
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    statusMessage = null,
                    errorMessage = null,
                    telemetry = it.telemetry.copy(isPinging = true)
                )
            }
            try {
                val outcome = repository.runCrawlAndSyncWithTelemetry(cfg, limit = cfg.dailyPostLimit.coerceAtLeast(5))
                val triggerOutcome = repository.fetchTriggerStatusWithTelemetry(cfg)
                val now = currentTimestamp()
                val nowMs = System.currentTimeMillis()
                if (outcome.isSuccess) {
                    val fetched = outcome.data.orEmpty()
                    _uiState.update { current ->
                        val nextSlots = if (fetched.isNotEmpty()) {
                            fetched + current.slots.filter { old ->
                                fetched.none { inc -> inc.auctionCase.caseId == old.auctionCase.caseId }
                            }
                        } else current.slots
                        val updatedTrigger = triggerOutcome.data ?: current.telemetry.triggerStatus
                        val newLegal = current.legalVerificationByCaseId.toMutableMap()
                        val newAttr = current.attractivenessByCaseId.toMutableMap()
                        fetched.forEach { slot ->
                            val legal = LegalDoctrineVerifier.verify(slot.auctionCase)
                            newLegal[slot.auctionCase.caseId] = legal
                            newAttr[slot.auctionCase.caseId] = InvestmentAttractivenessEngine.score(slot.auctionCase, legal)
                        }
                        val firstSlot = nextSlots.firstOrNull()
                        val nextTopicBoard = TopicSelectionEngine.rankCandidates(
                            slots = nextSlots,
                            config = current.config,
                            proFunnel = current.proFunnel,
                            sortMode = current.topicSelectionBoard.sortMode,
                            naverKeywordDiagnosis = (current.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report,
                            nowText = now
                        )
                        current.copy(
                            isBusy = false,
                            slots = nextSlots,
                            selectedSlotId = firstSlot?.postId,
                            activeCourtArchive = firstSlot?.let {
                                CourtEvidenceRecorder.recordFullArchiveForSlot(it)
                            } ?: current.activeCourtArchive,
                            activeRegistryInfographic = firstSlot?.let {
                                RegistryInfographicEngine.buildTemplate(it.auctionCase, current.selectedRegistryTemplateType)
                            } ?: current.activeRegistryInfographic,
                            legalVerificationByCaseId = newLegal,
                            attractivenessByCaseId = newAttr,
                            topicSelectionBoard = nextTopicBoard,
                            naverPublishStatuses = buildNaverPublishStatuses(nextSlots, current.publishLogs, current.config),
                            telemetry = current.telemetry.copy(
                                ddakchalConnected = true,
                                lastSyncTime = now,
                                apiLatencyMs = outcome.latencyMs,
                                lastSyncEpochMs = nowMs,
                                lastHttpStatusCode = outcome.httpStatusCode,
                                lastErrorDetail = null,
                                lastFeedCursorSince = outcome.nextCursorSince ?: current.telemetry.lastFeedCursorSince,
                                triggerStatus = updatedTrigger,
                                isPinging = false
                            ),
                            statusMessage = "딱찰 크롤링·트리거·신규 분석 피드 ${fetched.size}건 동기화 완료 (${outcome.latencyMs}ms)"
                        )
                    }
                    syncRealBlogQueueInternal(cfg = cfg, limit = 100, announceBanner = false)
                } else {
                    _uiState.update { current ->
                        current.copy(
                            isBusy = false,
                            telemetry = current.telemetry.copy(
                                ddakchalConnected = false,
                                apiLatencyMs = outcome.latencyMs.takeIf { it > 0L } ?: current.telemetry.apiLatencyMs,
                                lastHttpStatusCode = outcome.httpStatusCode,
                                lastErrorDetail = outcome.errorMessage,
                                isPinging = false
                            ),
                            errorMessage = outcome.errorMessage ?: "크롤링+동기화 실행 실패"
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            }
        }
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
                                lastSyncEpochMs = System.currentTimeMillis(),
                                lastHttpStatusCode = 200,
                                lastErrorDetail = null,
                                dailyQuotaUsed = fetchedSlots.count { s -> s.status == PostStatus.PUBLISHED || s.status == PostStatus.QUEUED }
                            ),
                            statusMessage = "딱찰 실서버 일일 ${fetchedSlots.size}건 생성 및 3채널 QA 검수 완료"
                        )
                    }
                    syncRealBlogQueueInternal(cfg = cfg, limit = 100, announceBanner = false)
                }.onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            telemetry = it.telemetry.copy(
                                ddakchalConnected = false,
                                lastErrorDetail = err.message
                            ),
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
            _uiState.update {
                it.copy(
                    isBusy = true,
                    statusMessage = null,
                    errorMessage = null,
                    telemetry = it.telemetry.copy(isPinging = true)
                )
            }
            try {
                val outcome = repository.syncSyndicationPackWithTelemetry(cfg, limit = cfg.dailyPostLimit.coerceAtLeast(5))
                val now = currentTimestamp()
                if (outcome.isSuccess && outcome.data != null) {
                    val fetchedSlots = outcome.data
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            slots = fetchedSlots,
                            selectedSlotId = fetchedSlots.firstOrNull()?.postId,
                            telemetry = it.telemetry.copy(
                                ddakchalConnected = true,
                                lastSyncTime = now,
                                apiLatencyMs = outcome.latencyMs,
                                lastSyncEpochMs = System.currentTimeMillis(),
                                lastHttpStatusCode = outcome.httpStatusCode,
                                lastErrorDetail = null,
                                lastFeedCursorSince = outcome.nextCursorSince ?: it.telemetry.lastFeedCursorSince,
                                isPinging = false
                            ),
                            statusMessage = "신디케이션 팩 ${fetchedSlots.size}건 동기화 완료 (${outcome.latencyMs}ms)"
                        )
                    }
                    syncRealBlogQueueInternal(cfg = cfg, limit = 100, announceBanner = false)
                } else {
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            telemetry = it.telemetry.copy(
                                ddakchalConnected = false,
                                apiLatencyMs = outcome.latencyMs.takeIf { l -> l > 0L } ?: it.telemetry.apiLatencyMs,
                                lastHttpStatusCode = outcome.httpStatusCode,
                                lastErrorDetail = outcome.errorMessage,
                                isPinging = false
                            ),
                            errorMessage = outcome.errorMessage ?: "신디케이션 팩 동기화 실패"
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
        val cleanCaseId = repository.normalizeCaseNumber(caseId).ifBlank { caseId.trim() }
        val cleanCourt = courtName.trim()
        val cleanAddress = address.trim()
        if (cleanCaseId.isBlank() || cleanCourt.isBlank() || cleanAddress.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "실제 사건번호, 관할 법원명, 소재지를 모두 입력해 주세요.")
            }
            return
        }
        val appraisalKrw = (appraisalManWon.coerceAtLeast(100L)) * 10_000L
        val minBidKrw = (minBidManWon.coerceAtLeast(50L)) * 10_000L
        val discountPct = ((minBidKrw.toDouble() / appraisalKrw.toDouble()) * 100).toInt().coerceIn(10, 100)
        val cfg = _uiState.value.config
        val nextIndex = _uiState.value.slots.size + 1

        val auction = AuctionCaseIntel(
            caseId = cleanCaseId,
            dedupKey = "ca-custom-${cleanCaseId.hashCode().toUInt()}",
            courtName = cleanCourt,
            address = cleanAddress,
            propertyType = propertyType,
            appraisalPriceKrw = appraisalKrw,
            minBidPriceKrw = minBidKrw,
            bidCount = 1,
            discountRatePct = discountPct,
            auctionDate = publishDate.trim().ifBlank { PostAuctionFollowUpEngine.currentIsoDate() },
            baseRightDate = baseRightDate.trim(),
            dividendDeadline = "",
            claimAmountKrw = (appraisalKrw * 0.55).toLong(),
            depositRatePct = 10,
            possessionSummary = "사용자 입력 사건 · 현장/서류 확인 대상",
            tenantSummary = "매각물건명세서 확인 필요",
            assumedRightsText = assumedRights.trim().ifBlank { "미입력" },
            specialCheckNotice = propertyType.focusSummary,
            recentRealTradeMedianKrw = (appraisalKrw * 0.92).toLong(),
            stats3yRatePct = 0.0,
            clicksLast7d = 0,
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
        val targetSlot = state.slots.firstOrNull {
            (propertyType == null && it.postId == state.selectedSlotId) ||
                (propertyType != null && it.auctionCase.propertyType == propertyType)
        } ?: state.slots.firstOrNull()

        if (targetSlot == null) {
            val discoveredCase = state.todakAdmin.discoveredItems.firstOrNull {
                propertyType == null || it.propertyType == propertyType
            } ?: state.todakAdmin.discoveredItems.firstOrNull()
            if (discoveredCase != null) {
                generatePostFromDiscoveredDbItem(discoveredCase.caseId)
                return
            }
            _uiState.update {
                it.copy(errorMessage = "창고에 보관할 실제 서버 사건이 없습니다. 상단 사건번호 조회 또는 딱찰 DB 탐색을 먼저 실행해 주세요.")
            }
            return
        }

        val cfg = state.config
        val funnelCfg = state.proFunnel
        val auction = targetSlot.auctionCase
        viewModelScope.launch {
            val corpus = repository.getRecent7DayCorpus()
            val fixed = WeeklyBatchScheduleEngine.ensureScoreAtLeast95(
                slot = targetSlot,
                config = cfg,
                proFunnel = funnelCfg,
                recent7DayCorpus = corpus,
                maxIterations = 3
            )
            val newSlot = fixed.slot
            val legalReport = LegalDoctrineVerifier.verify(newSlot.auctionCase)
            val regTemplate = RegistryInfographicEngine.buildTemplate(newSlot.auctionCase, state.selectedRegistryTemplateType)
            val courtArchive = CourtEvidenceRecorder.recordFullArchiveForSlot(newSlot)
            val evalResult = Article30PointEvaluator.evaluate(newSlot, cfg, funnelCfg, regTemplate, corpus)
            val now = currentTimestamp()
            val whArticle = WarehouseArticle(
                warehouseId = "wh-${newSlot.auctionCase.dedupKey}",
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
                actionTitle = "실사건 검수 → 창고 보관 (${evalResult.totalScore}점)",
                score = evalResult.totalScore,
                passedGate95 = evalResult.eligibleForRegistration,
                detail = "${evalResult.passedCount}/30 항목 통과 · ${legalReport.auditSummary}",
                timestamp = now
            )
            val snapshot = repository.upsertWarehouseArticle(whArticle, log)
            _uiState.update { current ->
                current.copy(
                    slots = current.slots.map { if (it.postId == newSlot.postId) newSlot else it },
                    selectedSlotId = newSlot.postId,
                    activeRegistryInfographic = regTemplate,
                    activeCourtArchive = courtArchive,
                    legalVerificationByCaseId = current.legalVerificationByCaseId + (newSlot.auctionCase.caseId to legalReport),
                    warehouseArticles = snapshot?.articles ?: (listOf(whArticle) + current.warehouseArticles.filterNot { it.warehouseId == whArticle.warehouseId }),
                    writingLogs = snapshot?.writingLogs ?: (listOf(log) + current.writingLogs),
                    statusMessage = "${newSlot.auctionCase.caseId} 실사건 검수·창고 보관 완료 (30개 항목 ${evalResult.totalScore}/100점)"
                )
            }
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
                            lastAiEngineUsed = report.modelName,
                            statusMessage = "${report.caseId} AI 30항목 채점 완료 (${report.modelName}): ${report.totalScore}/100점 (${report.overallVerdict})"
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
        val realBusanSlot = state.slots.firstOrNull {
            it.auctionCase.address.contains("부산") &&
                (it.auctionCase.propertyType == PropertyType.LAND_FARMLAND || it.auctionCase.minBidPriceKrw in 1L..50_000_000L)
        } ?: state.slots.firstOrNull { it.auctionCase.address.contains("부산") }

        if (realBusanSlot != null) {
            selectSlot(realBusanSlot.postId)
            evaluateSlotWithAi30Criteria(realBusanSlot.postId)
            return
        }

        val discoveredBusan = state.todakAdmin.discoveredItems.firstOrNull {
            it.address.contains("부산") || it.courtName.contains("부산")
        }
        if (discoveredBusan != null) {
            generatePostFromDiscoveredDbItem(discoveredBusan.caseId)
            return
        }

        if (cfg.ddakchalApiBase.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "딱찰 실서버에서 조회된 부산 물건이 없습니다. 상단 사건번호 조회 또는 DB 탐색으로 실제 물건을 먼저 불러오세요.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            val result = repository.runTodakCrawlEngineWithSteps(
                config = cfg,
                regions = listOf("부산"),
                categoryFilter = "LAND_FARMLAND",
                limit = 10
            )
            val matchedSlot = result.items.firstOrNull()?.slot
            if (matchedSlot == null) {
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        errorMessage = result.summaryMessage.ifBlank {
                            "실서버 피드에서 부산 토지 사건을 찾지 못했습니다."
                        }
                    )
                }
            } else {
                val regTemplate = RegistryInfographicEngine.buildTemplate(matchedSlot.auctionCase, state.selectedRegistryTemplateType)
                val courtArchive = CourtEvidenceRecorder.recordFullArchiveForSlot(matchedSlot)
                _uiState.update { cur ->
                    val deduped = listOf(matchedSlot) + cur.slots.filterNot { it.postId == matchedSlot.postId }
                    cur.copy(
                        isBusy = false,
                        slots = deduped,
                        selectedSlotId = matchedSlot.postId,
                        activeRegistryInfographic = regTemplate,
                        activeCourtArchive = courtArchive,
                        statusMessage = "${matchedSlot.auctionCase.caseId} 실서버 부산 물건 로드 완료 → AI 채점 실행"
                    )
                }
                evaluateSlotWithAi30Criteria(matchedSlot.postId)
            }
        }
    }

    fun createLowScoreDraftForAutoFixTest() {
        val state = _uiState.value
        val baseSlot = state.slots.firstOrNull { it.postId == state.selectedSlotId } ?: state.slots.firstOrNull()
        if (baseSlot == null) {
            _uiState.update {
                it.copy(errorMessage = "검증할 실제 사건이 없습니다. 상단에서 사건번호를 조회하거나 서버 DB를 탐색해 주세요.")
            }
            return
        }
        evaluateSlotWithAi30Criteria(baseSlot.postId)
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
            val archive = CourtEvidenceRecorder.recordFullArchiveForSlot(fixedSlot)
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
            evaluateLowQualityRiskAndPauseIfNeeded(announceBanner = false)
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
                val nextSlots = current.slots.map { if (it.postId == targetSlot.postId) defendedSlot else it }
                val riskReport = ChannelIndexRiskEngine.evaluate(
                    config = current.config,
                    slots = nextSlots,
                    publishLogs = current.publishLogs,
                    visitorLogs = current.visitorLogs,
                    queueSyncState = current.queueSyncState,
                    naverKeywordReport = (current.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report,
                    alreadyPausedByRisk = current.autoPublishPausedByRisk,
                    nowText = currentTimestamp()
                )
                current.copy(
                    isBusy = false,
                    slots = nextSlots,
                    legalVerificationByCaseId = current.legalVerificationByCaseId + (defendedSlot.auctionCase.caseId to legalReport),
                    lowQualityRiskReport = riskReport,
                    autoPublishPausedByRisk = riskReport.autoPublishPaused,
                    autoPublishPauseReason = riskReport.pauseReason,
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
        _uiState.update { state ->
            val nowText = currentTimestamp()
            val audited = SecurityAuditEngine.auditSecurity(
                config = newConfig,
                slots = state.slots,
                publishLogs = state.publishLogs,
                naverReachable = state.naverProbeResult?.reachableOk,
                tistoryReachable = state.tistoryProbeResult?.reachableOk,
                nowText = nowText
            )
            val riskReport = ChannelIndexRiskEngine.evaluate(
                config = newConfig,
                slots = state.slots,
                publishLogs = state.publishLogs,
                visitorLogs = state.visitorLogs,
                queueSyncState = state.queueSyncState,
                naverKeywordReport = (state.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report,
                alreadyPausedByRisk = state.autoPublishPausedByRisk,
                nowText = nowText
            )
            state.copy(
                config = newConfig,
                securityDashboard = audited,
                lowQualityRiskReport = riskReport,
                autoPublishPausedByRisk = riskReport.autoPublishPaused,
                autoPublishPauseReason = riskReport.pauseReason,
                statusMessage = "서버 및 저품질 방어 설정(.env 변수) 저장 완료"
            )
        }
        viewModelScope.launch {
            configSaveMutex.withLock {
                repository.saveConfig(_uiState.value.config)
            }
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
                val mergedLogs = listOf(log) + it.publishLogs
                val riskReport = ChannelIndexRiskEngine.evaluate(
                    config = updatedCfg,
                    slots = it.slots,
                    publishLogs = mergedLogs,
                    visitorLogs = it.visitorLogs,
                    queueSyncState = it.queueSyncState,
                    naverKeywordReport = (it.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report,
                    alreadyPausedByRisk = it.autoPublishPausedByRisk,
                    nowText = now
                )
                it.copy(
                    config = updatedCfg,
                    activeHandoffAlert = "${platform.displayName} 보안문자/2단계 인증 감지 → 자동 발행 즉시 중단(handOff) · 1회 수동 세션 갱신이 필요합니다.",
                    publishLogs = mergedLogs,
                    lowQualityRiskReport = riskReport,
                    autoPublishPausedByRisk = riskReport.autoPublishPaused,
                    autoPublishPauseReason = riskReport.pauseReason
                )
            }
        }
    }

    fun resolveSessionHandoff() {
        val updatedCfg = _uiState.value.config.copy(
            naverSessionStatus = "세션 복구 요청됨 (미검증 · 실제 발행/프로브 필요)",
            tistorySessionStatus = "세션 복구 요청됨 (미검증 · 실제 발행/프로브 필요)"
        )
        viewModelScope.launch {
            repository.saveConfig(updatedCfg)
            _uiState.update {
                val riskReport = ChannelIndexRiskEngine.evaluate(
                    config = updatedCfg,
                    slots = it.slots,
                    publishLogs = it.publishLogs,
                    visitorLogs = it.visitorLogs,
                    queueSyncState = it.queueSyncState,
                    naverKeywordReport = (it.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report,
                    alreadyPausedByRisk = it.autoPublishPausedByRisk,
                    nowText = currentTimestamp()
                )
                it.copy(
                    config = updatedCfg,
                    activeHandoffAlert = null,
                    lowQualityRiskReport = riskReport,
                    autoPublishPausedByRisk = riskReport.autoPublishPaused,
                    autoPublishPauseReason = riskReport.pauseReason,
                    statusMessage = "Playwright 세션 핸드오프 상태 해제 완료 — 실제 연결·발행 점검이 필요합니다."
                )
            }
        }
    }

    // ==================== Post-Publish Channel Index & Low-Quality Risk Guard ====================

    fun evaluateLowQualityRiskAndPauseIfNeeded(announceBanner: Boolean = true) {
        _uiState.update { state ->
            val wasPaused = state.autoPublishPausedByRisk
            val report = ChannelIndexRiskEngine.evaluate(
                config = state.config,
                slots = state.slots,
                publishLogs = state.publishLogs,
                visitorLogs = state.visitorLogs,
                queueSyncState = state.queueSyncState,
                naverKeywordReport = (state.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report,
                alreadyPausedByRisk = wasPaused,
                nowText = currentTimestamp()
            )
            val newlyPaused = !wasPaused && report.autoPublishPaused
            state.copy(
                lowQualityRiskReport = report,
                autoPublishPausedByRisk = report.autoPublishPaused,
                autoPublishPauseReason = report.pauseReason,
                errorMessage = if (newlyPaused) {
                    report.pauseReason ?: "저품질 위험도 '위험 높음'으로 자동 발행이 일시 중지되었습니다."
                } else state.errorMessage,
                statusMessage = if (!newlyPaused && announceBanner) {
                    "채널 지수 · 저품질 위험도 진단 완료: ${report.overallRisk.label} (${report.overallRiskScore}pt)"
                } else state.statusMessage
            )
        }
    }

    fun resumeAutoPublishAfterRiskMitigation() {
        val state = _uiState.value
        val freshReport = ChannelIndexRiskEngine.evaluate(
            config = state.config,
            slots = state.slots,
            publishLogs = state.publishLogs,
            visitorLogs = state.visitorLogs,
            queueSyncState = state.queueSyncState,
            naverKeywordReport = (state.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report,
            alreadyPausedByRisk = false,
            nowText = currentTimestamp()
        )
        _uiState.update { cur ->
            cur.copy(
                lowQualityRiskReport = freshReport.copy(
                    autoPublishPaused = false,
                    pauseReason = null
                ),
                autoPublishPausedByRisk = false,
                autoPublishPauseReason = null,
                errorMessage = null,
                statusMessage = "저품질 위험 조치 확인 후 자동 발행을 재개했습니다 (현재 위험도: ${freshReport.overallRisk.badgeText})."
            )
        }
    }

    fun toggleAutoPauseOnHighRisk(enabled: Boolean) {
        _uiState.update { state ->
            val updated = state.config.copy(autoPauseOnHighRisk = enabled)
            val report = ChannelIndexRiskEngine.evaluate(
                config = updated,
                slots = state.slots,
                publishLogs = state.publishLogs,
                visitorLogs = state.visitorLogs,
                queueSyncState = state.queueSyncState,
                naverKeywordReport = (state.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report,
                alreadyPausedByRisk = if (enabled) state.autoPublishPausedByRisk else false,
                nowText = currentTimestamp()
            )
            state.copy(
                config = updated,
                lowQualityRiskReport = report,
                autoPublishPausedByRisk = report.autoPublishPaused,
                autoPublishPauseReason = report.pauseReason,
                statusMessage = if (enabled) {
                    "저품질 위험도 '위험 높음' 시 자동 발행 자동 중지를 켰습니다."
                } else {
                    "저품질 위험도 자동 중지를 해제하고 수동 경고 모드로 전환했습니다."
                }
            )
        }
        viewModelScope.launch {
            configSaveMutex.withLock {
                repository.saveConfig(_uiState.value.config)
            }
        }
    }

    // ==================== 초심플 주간(7일) 예약 · 95+ 자동교정 등록 · 3채널 검증 · 사후재조사 ====================

    fun setWeeklyDailyCount(perDay: Int) {
        val safe = perDay.coerceIn(3, 5)
        _uiState.update { it.copy(selectedWeeklyDailyCount = safe) }
    }

    fun generateWeeklyBatchAndSchedule(perDay: Int = _uiState.value.selectedWeeklyDailyCount) {
        val state = _uiState.value
        if (state.autoPublishPausedByRisk) {
            _uiState.update {
                it.copy(
                    errorMessage = it.autoPublishPauseReason
                        ?: "저품질 위험도 높음으로 자동 발행이 일시 중지되었습니다. 원인 조치 후 '자동발행 재개'를 눌러 주세요."
                )
            }
            return
        }
        val safePerDay = perDay.coerceIn(3, 5)
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
                val discoveredRes: List<GeneratedPostSlot> = if (syncedRes.getOrDefault(emptyList()).isEmpty() && updatedConfig.ddakchalApiBase.isNotBlank()) {
                    repository.inspectServerDatabases(
                        config = updatedConfig,
                        categoryFilter = state.todakAdmin.selectedCategoryFilter,
                        searchQuery = state.todakAdmin.searchQuery,
                        limit = safePerDay * 7
                    ).items.map { it.slot }
                } else {
                    emptyList()
                }
                val serverSlots = (syncedRes.getOrDefault(emptyList()) + discoveredRes + state.slots)
                    .distinctBy { it.auctionCase.caseId }
                val ddakchalLinked = syncedRes.isSuccess && syncedRes.getOrDefault(emptyList()).isNotEmpty()
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
                if (weeklySlots.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            errorMessage = "주간 예약을 생성할 실제 서버 사건이 없습니다. 상단 사건번호 조회 또는 딱찰 DB 탐색·크롤링을 먼저 실행하세요."
                        )
                    }
                    return@launch
                }

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
                    val updatedSlotsList = weeklySlots
                    val mergedLogs = newPublishLogs + current.publishLogs
                    val riskReport = ChannelIndexRiskEngine.evaluate(
                        config = updatedConfig,
                        slots = updatedSlotsList,
                        publishLogs = mergedLogs,
                        visitorLogs = current.visitorLogs,
                        queueSyncState = current.queueSyncState,
                        naverKeywordReport = (current.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report,
                        alreadyPausedByRisk = current.autoPublishPausedByRisk,
                        nowText = now
                    )
                    current.copy(
                        isBusy = false,
                        config = updatedConfig,
                        slots = updatedSlotsList,
                        selectedSlotId = weeklySlots.firstOrNull()?.postId,
                        activeRegistryInfographic = weeklySlots.firstOrNull()?.let {
                            RegistryInfographicEngine.buildTemplate(it.auctionCase, current.selectedRegistryTemplateType)
                        },
                        activeCourtArchive = weeklySlots.firstOrNull()?.let {
                            CourtEvidenceRecorder.recordFullArchive(it.auctionCase, it.courtPhotoUrls, it.visitRecords)
                        },
                        warehouseArticles = snapshot?.articles ?: (newWarehouseArticles + current.warehouseArticles),
                        writingLogs = snapshot?.writingLogs ?: (listOf(log) + current.writingLogs),
                        publishLogs = mergedLogs,
                        legalVerificationByCaseId = newLegalMap,
                        attractivenessByCaseId = newAttrMap,
                        weeklyBatchSummary = weeklySummary,
                        lowQualityRiskReport = riskReport,
                        autoPublishPausedByRisk = riskReport.autoPublishPaused,
                        autoPublishPauseReason = riskReport.pauseReason,
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

    fun createHighScoreAndAutoRegister(@Suppress("UNUSED_PARAMETER") propertyType: PropertyType? = null) {
        val state = _uiState.value
        if (state.autoPublishPausedByRisk) {
            _uiState.update {
                it.copy(
                    errorMessage = it.autoPublishPauseReason
                        ?: "저품질 위험도 높음으로 자동 발행이 일시 중지되었습니다. 원인 조치 후 '자동발행 재개'를 눌러 주세요."
                )
            }
            return
        }
        val selected = state.slots.firstOrNull { it.postId == state.selectedSlotId } ?: state.slots.firstOrNull()
        if (selected != null) {
            autoFixAndRegisterSlot(selected.postId)
            return
        }
        val firstDiscovered = state.todakAdmin.discoveredItems.firstOrNull()
        if (firstDiscovered != null) {
            lookupCaseNumberFromTodakAndAutoPreview(firstDiscovered.caseId, preferredTabIndex = 0)
            return
        }
        _uiState.update {
            it.copy(errorMessage = "발행할 실제 사건이 없습니다. 상단에서 법원 사건번호를 입력해 불러오거나 딱찰 DB 탐색·크롤링을 먼저 실행하세요.")
        }
    }

    fun autoFixAndRegisterSlot(postId: String) {
        val state = _uiState.value
        if (state.autoPublishPausedByRisk) {
            _uiState.update {
                it.copy(
                    errorMessage = it.autoPublishPauseReason
                        ?: "저품질 위험도 높음으로 자동 발행이 일시 중지되었습니다. 원인 조치 후 '자동발행 재개'를 눌러 주세요."
                )
            }
            return
        }
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
        val courtArchive = CourtEvidenceRecorder.recordFullArchiveForSlot(slot)
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
            passed95 && anyHttpOk -> PostStatus.PUBLISHED
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
                status = if (outcome.reachableOk) PostStatus.PUBLISHED else PostStatus.FAILED,
                publishedUrl = outcome.publishedPostUrl.ifBlank { null },
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
            val mergedLogs = pubLogs + current.publishLogs
            val wasPaused = current.autoPublishPausedByRisk
            val riskReport = ChannelIndexRiskEngine.evaluate(
                config = current.config,
                slots = replacedOrPrependedSlots,
                publishLogs = mergedLogs,
                visitorLogs = current.visitorLogs,
                queueSyncState = current.queueSyncState,
                naverKeywordReport = (current.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report,
                alreadyPausedByRisk = wasPaused,
                nowText = now
            )
            val newlyPaused = !wasPaused && riskReport.autoPublishPaused
            current.copy(
                isBusy = false,
                slots = replacedOrPrependedSlots,
                selectedSlotId = finalSlot.postId,
                activeRegistryInfographic = regTemplate,
                activeCourtArchive = courtArchive,
                warehouseArticles = snapshot?.articles ?: current.warehouseArticles,
                writingLogs = snapshot?.writingLogs ?: (listOf(log) + current.writingLogs),
                publishLogs = mergedLogs,
                publishVerificationReport = report,
                naverPublishStatuses = buildNaverPublishStatuses(replacedOrPrependedSlots, mergedLogs, current.config),
                legalVerificationByCaseId = current.legalVerificationByCaseId + (finalSlot.auctionCase.caseId to legal),
                attractivenessByCaseId = current.attractivenessByCaseId + (finalSlot.auctionCase.caseId to attractiveness),
                lowQualityRiskReport = riskReport,
                autoPublishPausedByRisk = riskReport.autoPublishPaused,
                autoPublishPauseReason = riskReport.pauseReason,
                errorMessage = if (newlyPaused) riskReport.pauseReason else current.errorMessage,
                statusMessage = "${finalSlot.auctionCase.caseId} (${score}점): ${report.overallSummary}"
            )
        }

        // Best-effort reverse-sync queue item status to DDAKCHAL backend if token is configured
        val reverseSyncNote = if (cfg.crawlerSyncToken.isNotBlank() && cfg.ddakchalApiBase.isNotBlank()) {
            val syncStatus = when (finalStatus) {
                PostStatus.PUBLISHED -> "PUBLISHED"
                PostStatus.FAILED, PostStatus.HANDOFF -> "FAILED"
                else -> "DRAFT_SAVED"
            }
            val reverseOutcome = repository.reverseSyncQueueStatus(
                config = cfg,
                itemId = finalSlot.queueId,
                status = syncStatus,
                blogId = cfg.naverBlogId.ifBlank { null },
                blogPostUrl = updatedPublishedUrls.values.firstOrNull(),
                lastError = report.outcomes.firstOrNull { !it.reachableOk }?.message
            )
            reverseOutcome.data ?: reverseOutcome.errorMessage
        } else null

        if (!reverseSyncNote.isNullOrBlank()) {
            _uiState.update { current ->
                current.copy(
                    statusMessage = "${finalSlot.auctionCase.caseId} (${score}점): ${report.overallSummary} · $reverseSyncNote"
                )
            }
        }
    }

    fun saveThreeChannelDestinations(
        naverBlogId: String,
        naverCategoryName: String,
        naverWorkerRelayEndpoint: String,
        tistoryBlogUrl: String,
        tistoryCategoryName: String,
        cloudflareSiteBaseInput: String,
        cloudflareBlogPath: String,
        publishNaver: Boolean,
        publishTistory: Boolean,
        publishCloudflare: Boolean
    ) {
        val cleanSiteBase = cloudflareSiteBaseInput.trim().removeSuffix("/").ifBlank { "https://aution.codeb.app" }
        _uiState.update { state ->
            val updated = state.config.copy(
                naverBlogId = naverBlogId.trim(),
                naverCategoryName = naverCategoryName.trim().ifBlank { "부동산 경매 분석" },
                naverWorkerRelayEndpoint = naverWorkerRelayEndpoint.trim(),
                tistoryBlogUrl = tistoryBlogUrl.trim().removeSuffix("/"),
                tistoryCategoryName = tistoryCategoryName.trim().ifBlank { "경매·권리·세무 리포트" },
                cloudflareSiteBase = cleanSiteBase,
                cloudflareBlogPath = cloudflareBlogPath.trim().ifBlank { "/blog" },
                publishTargetNaver = publishNaver,
                publishTargetTistory = publishTistory,
                publishTargetCloudflare = publishCloudflare
            )
            state.copy(
                config = updated,
                naverWriteGuides = directPublishClient.buildAlternateNaverWriteGuides(updated, state.slots.firstOrNull()),
                statusMessage = "3채널 등록처(네이버·티스토리·${cleanSiteBase}) 저장 완료"
            )
        }
        viewModelScope.launch {
            configSaveMutex.withLock {
                repository.saveConfig(_uiState.value.config)
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
                if (state.config.crawlerSyncToken.isNotBlank() && state.config.ddakchalApiBase.isNotBlank()) {
                    repository.reverseSyncQueueStatus(
                        config = state.config,
                        itemId = updatedSlot.queueId,
                        status = if (report.naverDirectPassed) "PUBLISHED" else "FAILED",
                        blogId = state.config.naverBlogId.ifBlank { null },
                        blogPostUrl = report.verifiedNaverPostUrl.ifBlank { null },
                        lastError = if (report.naverDirectPassed) null else naverOutcome?.message
                    )
                }
                _uiState.update { current ->
                    val nextSlots = current.slots.map { if (it.postId == updatedSlot.postId) updatedSlot else it }
                    val nextLogs = listOf(newLog) + current.publishLogs
                    val wasPaused = current.autoPublishPausedByRisk
                    val riskReport = ChannelIndexRiskEngine.evaluate(
                        config = current.config,
                        slots = nextSlots,
                        publishLogs = nextLogs,
                        visitorLogs = current.visitorLogs,
                        queueSyncState = current.queueSyncState,
                        naverKeywordReport = (current.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report,
                        alreadyPausedByRisk = wasPaused,
                        nowText = now
                    )
                    val newlyPaused = !wasPaused && riskReport.autoPublishPaused
                    current.copy(
                        isBusy = false,
                        slots = nextSlots,
                        publishLogs = nextLogs,
                        publishVerificationReport = report,
                        naverPublishStatuses = buildNaverPublishStatuses(nextSlots, nextLogs, current.config),
                        lowQualityRiskReport = riskReport,
                        autoPublishPausedByRisk = riskReport.autoPublishPaused,
                        autoPublishPauseReason = riskReport.pauseReason,
                        statusMessage = if (report.naverDirectPassed && !newlyPaused) report.overallSummary else null,
                        errorMessage = when {
                            newlyPaused -> riskReport.pauseReason
                            !report.naverDirectPassed -> report.overallSummary
                            else -> null
                        }
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
                if (state.config.crawlerSyncToken.isNotBlank() && state.config.ddakchalApiBase.isNotBlank()) {
                    val anyOk = report.outcomes.any { it.reachableOk }
                    repository.reverseSyncQueueStatus(
                        config = state.config,
                        itemId = updatedSlot.queueId,
                        status = if (anyOk) "PUBLISHED" else "FAILED",
                        blogId = state.config.naverBlogId.ifBlank { null },
                        blogPostUrl = verifiedUrls.values.firstOrNull(),
                        lastError = if (anyOk) null else report.overallSummary
                    )
                }
                _uiState.update { current ->
                    val nextSlots = current.slots.map { if (it.postId == updatedSlot.postId) updatedSlot else it }
                    val nextLogs = newLogs + current.publishLogs
                    val wasPaused = current.autoPublishPausedByRisk
                    val riskReport = ChannelIndexRiskEngine.evaluate(
                        config = current.config,
                        slots = nextSlots,
                        publishLogs = nextLogs,
                        visitorLogs = current.visitorLogs,
                        queueSyncState = current.queueSyncState,
                        naverKeywordReport = (current.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report,
                        alreadyPausedByRisk = wasPaused,
                        nowText = now
                    )
                    val newlyPaused = !wasPaused && riskReport.autoPublishPaused
                    current.copy(
                        isBusy = false,
                        slots = nextSlots,
                        publishLogs = nextLogs,
                        publishVerificationReport = report,
                        naverPublishStatuses = buildNaverPublishStatuses(nextSlots, nextLogs, current.config),
                        lowQualityRiskReport = riskReport,
                        autoPublishPausedByRisk = riskReport.autoPublishPaused,
                        autoPublishPauseReason = riskReport.pauseReason,
                        errorMessage = if (newlyPaused) riskReport.pauseReason else current.errorMessage,
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

    fun refreshSecurityAudit() {
        _uiState.update { state ->
            val audited = SecurityAuditEngine.auditSecurity(
                config = state.config,
                slots = state.slots,
                publishLogs = state.publishLogs,
                naverReachable = state.naverProbeResult?.reachableOk,
                tistoryReachable = state.tistoryProbeResult?.reachableOk,
                nowText = currentTimestamp()
            )
            state.copy(
                securityDashboard = audited,
                statusMessage = "보안 감사 완료: ${audited.overallGrade.label} (${audited.overallScore}점 · 외부유출 ${audited.externalLeakCount}건)"
            )
        }
    }

    fun purgeLegacyPlaintextPasswords() {
        val currentCfg = _uiState.value.config
        val sanitized = currentCfg.copy(
            naverPw = "",
            tistoryPw = "",
            naverSessionStatus = "비밀번호 미저장 · 워커 릴레이/세션 핸드오프 모드",
            tistorySessionStatus = "비밀번호 미저장 · 카카오 2FA 세션 핸드오프 모드"
        )
        viewModelScope.launch {
            repository.saveConfig(sanitized)
            _uiState.update { state ->
                val audited = SecurityAuditEngine.auditSecurity(
                    config = sanitized,
                    slots = state.slots,
                    publishLogs = state.publishLogs,
                    naverReachable = state.naverProbeResult?.reachableOk,
                    tistoryReachable = state.tistoryProbeResult?.reachableOk,
                    nowText = currentTimestamp()
                )
                state.copy(
                    config = sanitized,
                    securityDashboard = audited,
                    statusMessage = "평문 비밀번호를 모두 소거하고 비번 미저장 보안 모드로 전환했습니다 (${audited.overallScore}점)"
                )
            }
        }
    }

    fun probeNaverAndTistoryConnections() {
        val cfg = _uiState.value.config
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            try {
                val naverProbe = directPublishClient.probeNaverConnection(cfg)
                val tistoryProbe = directPublishClient.probeTistoryConnection(cfg)
                _uiState.update { state ->
                    val audited = SecurityAuditEngine.auditSecurity(
                        config = state.config,
                        slots = state.slots,
                        publishLogs = state.publishLogs,
                        naverReachable = naverProbe.reachableOk,
                        tistoryReachable = tistoryProbe.reachableOk,
                        nowText = currentTimestamp()
                    )
                    state.copy(
                        isBusy = false,
                        naverProbeResult = naverProbe,
                        tistoryProbeResult = tistoryProbe,
                        securityDashboard = audited,
                        statusMessage = "채널 주소·세션 점검 완료 — 네이버 HTTP ${naverProbe.httpStatusCode} / 티스토리 HTTP ${tistoryProbe.httpStatusCode}"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isBusy = false, errorMessage = "채널 접속 점검 오류: ${e.localizedMessage}") }
            }
        }
    }

    fun searchCaseWithAiAndOptimize(queryInput: String) {
        val query = queryInput.trim().ifBlank {
            _uiState.value.slots.firstOrNull()?.auctionCase?.caseId.orEmpty()
        }
        if (query.isBlank()) {
            _uiState.update {
                it.copy(
                    aiCaseSearchState = AiCaseSearchUiState.Error("", "검색할 실제 사건번호 또는 물건 키워드를 입력해 주세요."),
                    errorMessage = "검색할 실제 사건번호 또는 물건 키워드를 입력해 주세요."
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    aiCaseSearchState = AiCaseSearchUiState.Searching(query),
                    errorMessage = null,
                    statusMessage = null
                )
            }
            try {
                val state = _uiState.value
                var matchedSlot = state.slots.firstOrNull { slot ->
                    slot.auctionCase.caseId.contains(query, ignoreCase = true) ||
                        slot.auctionCase.address.contains(query, ignoreCase = true) ||
                        slot.auctionCase.courtName.contains(query, ignoreCase = true) ||
                        slot.auctionCase.propertyType.displayLabel.contains(query, ignoreCase = true)
                } ?: state.warehouseArticles.firstOrNull { wh ->
                    wh.slot.auctionCase.caseId.contains(query, ignoreCase = true) ||
                        wh.slot.auctionCase.address.contains(query, ignoreCase = true)
                }?.slot

                if (matchedSlot == null && state.config.ddakchalApiBase.isNotBlank()) {
                    val normalizedCase = repository.normalizeCaseNumber(query)
                    if (normalizedCase.isNotBlank()) {
                        val lookup = repository.lookupCourtCaseFromTodakServer(state.config, normalizedCase)
                        matchedSlot = lookup.slot
                    } else {
                        val assetOutcome = repository.fetchSingleAssetWithTelemetry(state.config, query, refresh = true)
                        if (assetOutcome.isSuccess && assetOutcome.data != null) {
                            matchedSlot = assetOutcome.data
                        }
                    }
                }

                if (matchedSlot == null) {
                    val msg = "실제 큐 및 딱찰 실서버에서 '$query' 사건을 찾지 못했습니다."
                    _uiState.update {
                        it.copy(
                            aiCaseSearchState = AiCaseSearchUiState.Error(query, msg),
                            errorMessage = msg
                        )
                    }
                    return@launch
                }

                when (val outcome = aiCaseSearchAdvisor.optimizeCaseForAiSearch(matchedSlot, query, state.config)) {
                    is AiCaseSearchOutcome.Success -> {
                        val enrichedSlot = FirebaseAiCaseSearchService.applyGeoPackToSlot(
                            slot = matchedSlot,
                            pack = outcome.pack,
                            config = state.config
                        )
                        val auditedQa = QaVerificationEngine.auditSlot(
                            auctionCase = enrichedSlot.auctionCase,
                            variants = enrichedSlot.variants,
                            scheduledTimes = enrichedSlot.scheduledTimes,
                            config = state.config,
                            recent7DayCorpus = repository.getRecent7DayCorpus()
                        )
                        val finalSlot = enrichedSlot.copy(qaReport = auditedQa)
                        _uiState.update { cur ->
                            val replaced = if (cur.slots.any { it.postId == finalSlot.postId }) {
                                cur.slots.map { if (it.postId == finalSlot.postId) finalSlot else it }
                            } else {
                                listOf(finalSlot) + cur.slots
                            }
                            cur.copy(
                                slots = replaced,
                                selectedSlotId = finalSlot.postId,
                                aiCaseSearchState = AiCaseSearchUiState.Success(finalSlot, outcome.pack),
                                lastAiEngineUsed = outcome.pack.modelUsed,
                                topicSelectionBoard = TopicSelectionEngine.rankCandidates(
                                    slots = replaced,
                                    config = cur.config,
                                    proFunnel = cur.proFunnel,
                                    sortMode = cur.topicSelectionBoard.sortMode,
                                    nowText = currentTimestamp()
                                ),
                                statusMessage = "${finalSlot.auctionCase.caseId} AI 검색 노출(GEO ${outcome.pack.estimatedGeoScore}점 · ${outcome.pack.modelUsed}) 최적화 완료"
                            )
                        }
                    }
                    is AiCaseSearchOutcome.Error -> {
                        _uiState.update { cur ->
                            cur.copy(
                                aiCaseSearchState = AiCaseSearchUiState.Error(query, outcome.message),
                                errorMessage = outcome.message
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val msg = "AI 사건검색 처리 실패: ${e.localizedMessage ?: "알 수 없는 오류"}"
                _uiState.update {
                    it.copy(
                        aiCaseSearchState = AiCaseSearchUiState.Error(query, msg),
                        errorMessage = msg
                    )
                }
            }
        }
    }

    fun testRegisterAiSearchedCase() {
        val searchState = _uiState.value.aiCaseSearchState
        val targetSlotId = if (searchState is AiCaseSearchUiState.Success) {
            searchState.matchedSlot.postId
        } else {
            _uiState.value.selectedSlotId ?: _uiState.value.slots.firstOrNull()?.postId
        } ?: return
        autoFixAndRegisterSlot(targetSlotId)
    }

    fun generateThreePureHtmlPosts(
        threeCasesMode: Boolean = true,
        preferredChannel: ChannelPlatform = _uiState.value.selectedChannel
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, errorMessage = null, statusMessage = null) }
            try {
                val state = _uiState.value
                val recentCorpus = repository.getRecent7DayCorpus()
                val sourceSlots = state.slots
                if (sourceSlots.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            errorMessage = "HTML을 생성할 실제 사건이 없습니다. 먼저 사건번호 조회 또는 딱찰 서버 동기화를 실행해 주세요."
                        )
                    }
                    return@launch
                }
                val ensuredSlots = sourceSlots.take(3).map { slot ->
                    val eval = Article30PointEvaluator.evaluate(
                        slot = slot,
                        config = state.config,
                        proFunnel = state.proFunnel,
                        recent7DayCorpus = recentCorpus
                    )
                    if (eval.totalScore >= 95) {
                        slot
                    } else {
                        WeeklyBatchScheduleEngine.ensureScoreAtLeast95(
                            slot = slot,
                            config = state.config,
                            proFunnel = state.proFunnel,
                            recent7DayCorpus = recentCorpus,
                            maxIterations = 2
                        ).slot
                    }
                }
                val bundle = HtmlExportEngine.buildThreePureHtmlBatch(
                    slots = ensuredSlots,
                    threeCasesMode = threeCasesMode,
                    preferredChannel = preferredChannel,
                    config = state.config,
                    nowText = currentTimestamp()
                )
                _uiState.update { cur ->
                    cur.copy(
                        isBusy = false,
                        htmlBatchBundle = bundle,
                        statusMessage = "순수 HTML ${bundle.items.size}건 생성 완료 (${bundle.modeLabel})"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isBusy = false, errorMessage = "HTML 생성 오류: ${e.localizedMessage}") }
            }
        }
    }

    fun dismissHtmlBatchBundle() {
        _uiState.update { it.copy(htmlBatchBundle = null) }
    }

    /**
     * .env (BuildConfig.NAVER_CLIENT_ID / NAVER_CLIENT_SECRET)에 설정된
     * 네이버 검색 API 키를 사용하여 특정 경매 키워드의 블로그 상위 노출 가능성을 진단한다.
     */
    fun diagnoseNaverKeywordExposure(rawKeyword: String = "") {
        val state = _uiState.value
        val activeSlot = state.slots.firstOrNull { it.postId == state.selectedSlotId }
            ?: state.slots.firstOrNull()
        val resolvedKeyword = rawKeyword.trim().ifBlank {
            if (activeSlot != null) {
                val region = activeSlot.auctionCase.address.split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .joinToString(" ")
                "$region ${activeSlot.auctionCase.caseId} 경매".trim()
            } else {
                ""
            }
        }
        if (resolvedKeyword.isBlank()) {
            _uiState.update {
                it.copy(
                    naverKeywordDiagnosis = NaverKeywordDiagnosisUiState.Error(
                        keyword = "",
                        message = "진단할 경매 키워드를 입력해 주세요."
                    ),
                    errorMessage = "진단할 경매 키워드를 입력해 주세요."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    naverKeywordDiagnosis = NaverKeywordDiagnosisUiState.Loading(resolvedKeyword),
                    errorMessage = null,
                    statusMessage = null
                )
            }
            try {
                when (val outcome = naverApiClient.diagnoseKeywordBlogExposure(keyword = resolvedKeyword)) {
                    is NaverRealEstateApiClient.NaverKeywordExposureOutcome.Success -> {
                        val report = outcome.report
                        _uiState.update { cur ->
                            val wasPaused = cur.autoPublishPausedByRisk
                            val riskReport = ChannelIndexRiskEngine.evaluate(
                                config = cur.config,
                                slots = cur.slots,
                                publishLogs = cur.publishLogs,
                                visitorLogs = cur.visitorLogs,
                                queueSyncState = cur.queueSyncState,
                                naverKeywordReport = report,
                                alreadyPausedByRisk = wasPaused,
                                nowText = currentTimestamp()
                            )
                            val newlyPaused = !wasPaused && riskReport.autoPublishPaused
                            cur.copy(
                                naverKeywordDiagnosis = NaverKeywordDiagnosisUiState.Success(report),
                                lowQualityRiskReport = riskReport,
                                autoPublishPausedByRisk = riskReport.autoPublishPaused,
                                autoPublishPauseReason = riskReport.pauseReason,
                                errorMessage = if (newlyPaused) riskReport.pauseReason else cur.errorMessage,
                                statusMessage = "'${report.keyword}' 네이버 노출 진단: ${report.exposureScore}점 (${report.grade.label})"
                            )
                        }
                    }
                    is NaverRealEstateApiClient.NaverKeywordExposureOutcome.Failure -> {
                        _uiState.update { cur ->
                            cur.copy(
                                naverKeywordDiagnosis = NaverKeywordDiagnosisUiState.Error(
                                    keyword = resolvedKeyword,
                                    message = outcome.message,
                                    apiKeyMissing = outcome.apiKeyMissing
                                ),
                                errorMessage = outcome.message
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val msg = "네이버 노출 진단 오류: ${e.localizedMessage ?: "네트워크 상태 확인 필요"}"
                _uiState.update { cur ->
                    cur.copy(
                        naverKeywordDiagnosis = NaverKeywordDiagnosisUiState.Error(
                            keyword = resolvedKeyword,
                            message = msg
                        ),
                        errorMessage = msg
                    )
                }
            }
        }
    }

    fun clearNaverKeywordDiagnosis() {
        _uiState.update { it.copy(naverKeywordDiagnosis = NaverKeywordDiagnosisUiState.Idle) }
    }

    // ==================== 4-Pillar Topic Selection Engine ====================

    fun evaluateAndRankTopics(sortMode: TopicSortCriteria = _uiState.value.topicSelectionBoard.sortMode) {
        val state = _uiState.value
        val diagnosis = (state.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report
        val board = TopicSelectionEngine.rankCandidates(
            slots = state.slots,
            config = state.config,
            proFunnel = state.proFunnel,
            sortMode = sortMode,
            naverKeywordDiagnosis = diagnosis,
            nowText = currentTimestamp()
        )
        _uiState.update { cur ->
            cur.copy(
                topicSelectionBoard = board,
                statusMessage = "4대 기준(${sortMode.label}) 글감 ${board.candidates.size}건 선정 분석 완료"
            )
        }
    }

    fun applyTopSelectedTopicsToQueue(count: Int = 3) {
        val state = _uiState.value
        val rankedIds = state.topicSelectionBoard.candidates.take(count).map { it.postId }
        if (rankedIds.isEmpty()) return
        val slotById = state.slots.associateBy { it.postId }
        val topSlots = rankedIds.mapNotNull { slotById[it] }
        val restSlots = state.slots.filterNot { it.postId in rankedIds }
        val reordered = (topSlots + restSlots).mapIndexed { index, slot ->
            slot.copy(slotIndex = index + 1)
        }
        val diagnosis = (state.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report
        val refreshedBoard = TopicSelectionEngine.rankCandidates(
            slots = reordered,
            config = state.config,
            proFunnel = state.proFunnel,
            sortMode = state.topicSelectionBoard.sortMode,
            naverKeywordDiagnosis = diagnosis,
            nowText = currentTimestamp()
        )
        _uiState.update { cur ->
            cur.copy(
                slots = reordered,
                selectedSlotId = reordered.firstOrNull()?.postId ?: cur.selectedSlotId,
                topicSelectionBoard = refreshedBoard,
                statusMessage = "선정 글감 상위 ${topSlots.size}건을 오늘 우선 발행 슬롯으로 승격했습니다."
            )
        }
    }

    fun selectAndAutoRegisterTopic(postId: String) {
        selectSlot(postId)
        autoFixAndRegisterSlot(postId)
    }

    // ==================== Comprehensive DDAKCHAL Server Audit ====================

    fun runPerfectDdakchalServerDiagnostic() {
        val state = _uiState.value
        val sampleCaseId = state.slots.firstOrNull()?.auctionCase?.caseId
            ?: state.slots.firstOrNull()?.auctionCase?.dedupKey
            ?: state.todakAdmin.discoveredItems.firstOrNull()?.caseId
            ?: ""
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, errorMessage = null, statusMessage = null) }
            try {
                val report = repository.runComprehensiveDdakchalDiagnostic(state.config, sampleCaseId)
                _uiState.update { cur ->
                    cur.copy(
                        isBusy = false,
                        ddakchalFullDiagnostic = report,
                        telemetry = cur.telemetry.copy(
                            ddakchalConnected = report.overallConnected,
                            lastSyncTime = report.checkedAt,
                            lastErrorDetail = if (report.overallConnected) null else report.recommendation
                        ),
                        statusMessage = "딱찰 서버 정밀 점검 완료: ${report.healthGradeLabel} (${report.overallScorePct}%)"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isBusy = false, errorMessage = "딱찰 서버 정밀 점검 오류: ${e.localizedMessage}")
                }
            }
        }
    }

    // ==================== Naver Publish Status Tracker & Write Modes ====================

    fun refreshNaverPublishStatuses() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            try {
                val currentList = buildNaverPublishStatuses(state.slots, state.publishLogs, state.config)
                val verifiedList = currentList.map { rec ->
                    if (rec.publishedUrl.startsWith("http")) {
                        val (ok, code) = directPublishClient.verifyUrlReachable(rec.publishedUrl)
                        rec.copy(
                            isRegisteredOk = ok,
                            httpStatusCode = code,
                            statusLabel = if (ok) "네이버 실URL 도달(HTTP $code)" else "URL 확인 필요(HTTP $code)",
                            detailNote = if (ok) "실시간 HTTP GET 검증 완료" else "로그인·비공개 또는 URL 확인 필요",
                            updatedAt = currentTimestamp()
                        )
                    } else rec
                }
                val okCount = verifiedList.count { it.isRegisteredOk }
                _uiState.update { cur ->
                    cur.copy(
                        isBusy = false,
                        naverPublishStatuses = verifiedList,
                        statusMessage = "네이버 글 등록 상태 점검 완료 (${okCount}/${verifiedList.size}건 실존 확인)"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isBusy = false, errorMessage = "네이버 등록 상태 확인 오류: ${e.localizedMessage}") }
            }
        }
    }

    fun updateNaverWriteMode(mode: NaverAutoWriteMode) {
        _uiState.update { cur ->
            val updated = cur.config.copy(naverWriteMode = mode)
            cur.copy(
                config = updated,
                naverWriteGuides = directPublishClient.buildAlternateNaverWriteGuides(updated, cur.slots.firstOrNull()),
                statusMessage = "네이버 글쓰기 자동화 방식을 '${mode.title}'(으)로 전환했습니다."
            )
        }
        viewModelScope.launch {
            configSaveMutex.withLock {
                repository.saveConfig(_uiState.value.config)
            }
        }
    }

    // ==================== Quick Settings, Always-Logged-In & Google Gemini/Grok Routing ====================

    fun saveQuickSettings(
        cloudflareSiteBase: String,
        cloudflareBlogPath: String,
        naverKeepLoggedIn: Boolean,
        tistoryKeepLoggedIn: Boolean,
        tistoryAutoDirectPublish: Boolean,
        grokFallbackEnabled: Boolean,
        grokModelName: String
    ) {
        val cleanSite = cloudflareSiteBase.trim().removeSuffix("/").ifBlank { "https://aution.codeb.app" }
        val cleanPath = cloudflareBlogPath.trim().let { if (it.startsWith("/")) it else "/$it" }.ifBlank { "/blog" }
        _uiState.update { cur ->
            val updated = cur.config.copy(
                cloudflareSiteBase = cleanSite,
                cloudflareBlogPath = cleanPath,
                naverKeepLoggedIn = naverKeepLoggedIn,
                tistoryKeepLoggedIn = tistoryKeepLoggedIn,
                tistoryAutoDirectPublish = tistoryAutoDirectPublish,
                grokFallbackEnabled = grokFallbackEnabled,
                grokModelName = grokModelName.trim().ifBlank { "grok-3-mini" },
                naverSessionStatus = if (naverKeepLoggedIn) "항상 로그인 유지 중 (비번 미저장 · 세션 핸드오프)" else "수동 로그인 모드",
                tistorySessionStatus = when {
                    tistoryKeepLoggedIn && tistoryAutoDirectPublish -> "항상 로그인 · 바로 등록 활성"
                    tistoryKeepLoggedIn -> "항상 로그인 유지 중"
                    else -> "수동 로그인 모드"
                }
            )
            cur.copy(
                config = updated,
                statusMessage = "환경설정 저장 완료 ($cleanSite$cleanPath · 네이버·티스토리 상시 로그인/바로 등록 반영)"
            )
        }
        viewModelScope.launch {
            configSaveMutex.withLock {
                repository.saveConfig(_uiState.value.config)
            }
        }
    }

    fun runAlwaysLoggedInHeartbeat() {
        val cfg = _uiState.value.config
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, statusMessage = null, errorMessage = null) }
            try {
                val (nvProbe, tsProbe, cfProbe) = directPublishClient.probeAlwaysLoggedInHeartbeat(cfg)
                val updatedCfg = cfg.copy(
                    naverSessionStatus = if (cfg.naverKeepLoggedIn) {
                        "항상 로그인 유지 중 · ${nvProbe.detailMessage}"
                    } else nvProbe.detailMessage,
                    tistorySessionStatus = if (cfg.tistoryKeepLoggedIn && cfg.tistoryAutoDirectPublish) {
                        "항상 로그인 · 바로 등록 활성 · ${tsProbe.detailMessage}"
                    } else tsProbe.detailMessage
                )
                repository.saveConfig(updatedCfg)
                _uiState.update { cur ->
                    val audited = SecurityAuditEngine.auditSecurity(
                        config = updatedCfg,
                        slots = cur.slots,
                        publishLogs = cur.publishLogs,
                        naverReachable = nvProbe.reachableOk,
                        tistoryReachable = tsProbe.reachableOk,
                        nowText = currentTimestamp()
                    )
                    cur.copy(
                        isBusy = false,
                        config = updatedCfg,
                        naverProbeResult = nvProbe,
                        tistoryProbeResult = tsProbe,
                        securityDashboard = audited,
                        statusMessage = "상시 연결 점검: 네이버 HTTP ${nvProbe.httpStatusCode} · 티스토리 HTTP ${tsProbe.httpStatusCode} · ${cfProbe.targetUrl} HTTP ${cfProbe.httpStatusCode}"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isBusy = false, errorMessage = "채널 상시 연결 점검 오류: ${e.localizedMessage}") }
            }
        }
    }

    fun syncGoogleAuthState(statusOverride: String? = null) {
        val cfg = _uiState.value.config
        val accountState = GoogleAccountAuthHelper.currentGoogleAccountState(
            primaryGeminiFirst = cfg.aiPrimaryGeminiFirst,
            grokFallbackEnabled = cfg.grokFallbackEnabled,
            grokModelName = cfg.grokModelName,
            statusOverride = statusOverride
        )
        _uiState.update { cur ->
            cur.copy(
                googleAccount = accountState,
                lastAiEngineUsed = accountState.activePrimaryEngineLabel,
                statusMessage = statusOverride ?: cur.statusMessage
            )
        }
    }

    fun signOutGoogle(context: Context) {
        viewModelScope.launch {
            GoogleAccountAuthHelper.signOutGoogle(context)
            syncGoogleAuthState("Google 계정 로그아웃 완료 (Firebase App Check 기본 모드)")
        }
    }

    private fun buildNaverPublishStatuses(
        slots: List<GeneratedPostSlot>,
        logs: List<PublishLogEntry>,
        config: ServerAutomationConfig
    ): List<NaverPublishStatusRecord> {
        val naverLogsByPost = logs.filter { it.platform == ChannelPlatform.NAVER }.groupBy { it.postId }
        val methodLabel = config.naverWriteMode.shortBadge
        val relayConfigured = config.naverWorkerRelayEndpoint.isNotBlank()
        return slots.take(8).map { slot ->
            val latestLog = naverLogsByPost[slot.postId]?.firstOrNull()
            val publishedUrl = slot.publishedUrls[ChannelPlatform.NAVER].orEmpty()
                .ifBlank { latestLog?.publishedUrl.orEmpty() }
            val hasVerifiedPostUrl = publishedUrl.isNotBlank() &&
                latestLog?.status != PostStatus.FAILED &&
                latestLog?.status != PostStatus.HANDOFF &&
                (slot.status == PostStatus.PUBLISHED || latestLog?.status == PostStatus.PUBLISHED)
            val statusText = when {
                latestLog?.status == PostStatus.HANDOFF -> "세션 중단 (캡차 확인)"
                latestLog?.status == PostStatus.FAILED -> "등록 실패 (${latestLog.errorMessage ?: "재시도 필요"})"
                hasVerifiedPostUrl -> "등록 완료 · 실URL 확인"
                !relayConfigured -> "워커 미연동 · 발행 대기"
                slot.status == PostStatus.QUEUED -> "예약 대기 ($methodLabel)"
                else -> "미발행 · 대기 중"
            }
            val title = slot.variants[ChannelPlatform.NAVER]?.title ?: slot.auctionCase.address
            NaverPublishStatusRecord(
                postId = slot.postId,
                caseId = slot.auctionCase.caseId,
                title = title,
                statusLabel = statusText,
                isRegisteredOk = hasVerifiedPostUrl,
                methodUsed = methodLabel,
                httpStatusCode = if (hasVerifiedPostUrl) 200 else 0,
                publishedUrl = publishedUrl,
                detailNote = latestLog?.errorMessage ?: when {
                    hasVerifiedPostUrl -> "실제 발행 글 URL 확인됨"
                    !relayConfigured -> "NAVER_WORKER_RELAY_ENDPOINT 미연동 · 수동 URL 입력 또는 릴레이 연동 필요"
                    else -> "발행 이력 대기 중"
                },
                updatedAt = latestLog?.publishedAt ?: slot.publishDate
            )
        }
    }

    // ==================== Todak / Ddakchal Top Owner Admin Console ====================

    fun toggleTodakAdminRegion(region: String) {
        _uiState.update { state ->
            val current = state.todakAdmin.selectedRegions
            val next = if (region in current) {
                current.filterNot { it == region }
            } else {
                current + region
            }
            state.copy(todakAdmin = state.todakAdmin.copy(selectedRegions = next))
        }
    }

    fun setTodakAdminCategory(category: String) {
        _uiState.update { state ->
            state.copy(todakAdmin = state.todakAdmin.copy(selectedCategoryFilter = category.trim()))
        }
    }

    fun setTodakAdminSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(todakAdmin = state.todakAdmin.copy(searchQuery = query))
        }
    }

    fun setTodakAdminCrawlLimit(limit: Int) {
        _uiState.update { state ->
            state.copy(todakAdmin = state.todakAdmin.copy(crawlLimit = limit.coerceIn(1, 20)))
        }
    }

    fun applyInlineServerAuthAndSave(
        apiBase: String,
        syncToken: String,
        cfAccountId: String,
        cfD1DatabaseId: String,
        cfApiToken: String
    ) {
        val cleanBase = apiBase.trim().removeSuffix("/").ifBlank { "https://ddakchal.pages.dev" }
        val updated = _uiState.value.config.copy(
            ddakchalApiBase = cleanBase,
            crawlerSyncToken = syncToken.trim(),
            cfAccountId = cfAccountId.trim(),
            cfD1DatabaseId = cfD1DatabaseId.trim(),
            cfApiToken = cfApiToken.trim()
        )
        updateConfig(updated)
        _uiState.update { state ->
            state.copy(
                todakAdmin = state.todakAdmin.copy(
                    summaryMessage = "서버 인증 키 저장 완료 ($cleanBase · 토큰 ${if (updated.crawlerSyncToken.isNotBlank()) "준비됨" else "미입력"})"
                )
            )
        }
    }

    fun discoverTodakServerDb() {
        val state = _uiState.value
        val cfg = state.config
        val admin = state.todakAdmin
        viewModelScope.launch {
            _uiState.update { cur ->
                cur.copy(
                    isBusy = true,
                    errorMessage = null,
                    statusMessage = null,
                    todakAdmin = cur.todakAdmin.copy(
                        isRunning = true,
                        currentActionLabel = "1단계: 딱찰 서버 DB & D1 탐색 중…"
                    )
                )
            }
            try {
                val result = repository.inspectServerDatabases(
                    config = cfg,
                    categoryFilter = admin.selectedCategoryFilter,
                    searchQuery = admin.searchQuery,
                    limit = admin.crawlLimit.coerceAtLeast(10)
                )
                val now = currentTimestamp()
                val anyConnected = result.items.isNotEmpty() ||
                    result.d1TableNames.isNotEmpty() ||
                    result.triggerStatus != null ||
                    result.tables.any { it.httpStatusCode in 200..299 }
                val firstHttpCode = result.stepLogs.firstOrNull { it.httpCode > 0 }?.httpCode ?: 0
                val totalLatency = result.stepLogs.sumOf { it.latencyMs }
                _uiState.update { cur ->
                    cur.copy(
                        isBusy = false,
                        telemetry = cur.telemetry.copy(
                            ddakchalConnected = anyConnected,
                            cloudflareD1Connected = result.d1TableNames.isNotEmpty() || cur.telemetry.cloudflareD1Connected,
                            lastSyncTime = if (anyConnected) now else cur.telemetry.lastSyncTime,
                            apiLatencyMs = if (totalLatency > 0L) totalLatency else cur.telemetry.apiLatencyMs,
                            lastHttpStatusCode = firstHttpCode,
                            lastErrorDetail = if (anyConnected) null else result.summaryMessage,
                            triggerStatus = result.triggerStatus ?: cur.telemetry.triggerStatus
                        ),
                        todakAdmin = cur.todakAdmin.copy(
                            isRunning = false,
                            currentActionLabel = "",
                            discoveredTables = result.tables,
                            discoveredItems = result.items,
                            stepLogs = result.stepLogs,
                            d1TablesRaw = result.d1TableNames,
                            lastUpdatedAt = now,
                            summaryMessage = result.summaryMessage
                        ),
                        statusMessage = if (anyConnected) result.summaryMessage else null,
                        errorMessage = if (!anyConnected) result.summaryMessage else null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val err = "서버 DB 탐색 오류: ${e.localizedMessage ?: "알 수 없는 오류"}"
                _uiState.update { cur ->
                    cur.copy(
                        isBusy = false,
                        todakAdmin = cur.todakAdmin.copy(
                            isRunning = false,
                            currentActionLabel = "",
                            summaryMessage = err
                        ),
                        errorMessage = err
                    )
                }
            }
        }
    }

    fun runTodakCrawlEngineOnly() {
        val state = _uiState.value
        val cfg = state.config
        val admin = state.todakAdmin
        viewModelScope.launch {
            _uiState.update { cur ->
                cur.copy(
                    isBusy = true,
                    errorMessage = null,
                    statusMessage = null,
                    todakAdmin = cur.todakAdmin.copy(
                        isRunning = true,
                        currentActionLabel = "2단계: 딱찰 크롤링 엔진 실행 중…"
                    )
                )
            }
            try {
                val crawlResult = repository.runTodakCrawlEngineWithSteps(
                    config = cfg,
                    regions = admin.selectedRegions,
                    categoryFilter = admin.selectedCategoryFilter,
                    limit = admin.crawlLimit
                )
                val now = currentTimestamp()
                _uiState.update { cur ->
                    val mergedItems = (crawlResult.items + cur.todakAdmin.discoveredItems)
                        .distinctBy { it.dedupKey }
                    cur.copy(
                        isBusy = false,
                        telemetry = cur.telemetry.copy(
                            ddakchalConnected = crawlResult.isSuccess,
                            lastSyncTime = if (crawlResult.isSuccess) now else cur.telemetry.lastSyncTime,
                            apiLatencyMs = if (crawlResult.latencyMs > 0L) crawlResult.latencyMs else cur.telemetry.apiLatencyMs,
                            lastHttpStatusCode = crawlResult.httpStatusCode,
                            lastErrorDetail = if (crawlResult.isSuccess) null else crawlResult.summaryMessage,
                            triggerStatus = crawlResult.triggerStatus ?: cur.telemetry.triggerStatus
                        ),
                        todakAdmin = cur.todakAdmin.copy(
                            isRunning = false,
                            currentActionLabel = "",
                            discoveredItems = mergedItems,
                            stepLogs = crawlResult.stepLogs,
                            lastUpdatedAt = now,
                            summaryMessage = crawlResult.summaryMessage
                        ),
                        statusMessage = if (crawlResult.isSuccess) crawlResult.summaryMessage else null,
                        errorMessage = if (!crawlResult.isSuccess) crawlResult.summaryMessage else null
                    )
                }
                if (crawlResult.isSuccess) {
                    syncRealBlogQueueInternal(cfg = cfg, limit = 100, announceBanner = false)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val err = "크롤링 엔진 오류: ${e.localizedMessage ?: "알 수 없는 오류"}"
                _uiState.update { cur ->
                    cur.copy(
                        isBusy = false,
                        todakAdmin = cur.todakAdmin.copy(
                            isRunning = false,
                            currentActionLabel = "",
                            summaryMessage = err
                        ),
                        errorMessage = err
                    )
                }
            }
        }
    }

    fun runTodakFullAdminPipeline() {
        val state = _uiState.value
        val cfg = state.config
        val admin = state.todakAdmin
        viewModelScope.launch {
            _uiState.update { cur ->
                cur.copy(
                    isBusy = true,
                    errorMessage = null,
                    statusMessage = null,
                    todakAdmin = cur.todakAdmin.copy(
                        isRunning = true,
                        currentActionLabel = "전체 파이프라인: 서버 DB 탐색 → 크롤링 엔진 → 95+ 글 자동생성 중…"
                    )
                )
            }
            try {
                // Step 1: Discover Server DB & D1
                val dbResult = repository.inspectServerDatabases(
                    config = cfg,
                    categoryFilter = admin.selectedCategoryFilter,
                    searchQuery = admin.searchQuery,
                    limit = admin.crawlLimit.coerceAtLeast(10)
                )
                // Step 2: Run Live Crawler & Triggers Engine
                val crawlResult = repository.runTodakCrawlEngineWithSteps(
                    config = cfg,
                    regions = admin.selectedRegions,
                    categoryFilter = admin.selectedCategoryFilter,
                    limit = admin.crawlLimit
                )

                // Step 3: Merge live DB + newly crawled items (deduped)
                val mergedItems = (crawlResult.items + dbResult.items)
                    .distinctBy { it.dedupKey }

                val corpus = repository.getRecent7DayCorpus()
                val now = currentTimestamp()
                val targetItems = mergedItems.take(admin.crawlLimit.coerceIn(1, 10))
                val generatedSlots = mutableListOf<GeneratedPostSlot>()
                val warehouseArticlesToSave = mutableListOf<WarehouseArticle>()
                val newLegalMap = mutableMapOf<String, LegalDoctrineReport>()
                val newAttrMap = mutableMapOf<String, InvestmentAttractivenessScore>()
                var d1SyncedCount = 0

                val latestState = _uiState.value
                targetItems.forEachIndexed { idx, dbItem ->
                    val (enrichedSlot, whArticle, legal, attractiveness) = buildEnriched95SlotFromServerSlot(
                        rawSlot = dbItem.slot,
                        slotIndex = idx + 1,
                        config = cfg,
                        proFunnel = latestState.proFunnel,
                        postTemplates = latestState.postTemplates,
                        selectedRegTemplateType = latestState.selectedRegistryTemplateType,
                        recentCorpus = corpus,
                        nowText = now
                    )
                    generatedSlots += enrichedSlot
                    warehouseArticlesToSave += whArticle
                    newLegalMap[enrichedSlot.auctionCase.caseId] = legal
                    newAttrMap[enrichedSlot.auctionCase.caseId] = attractiveness

                    if (cfg.cfAccountId.isNotBlank() && cfg.cfD1DatabaseId.isNotBlank() && cfg.cfApiToken.isNotBlank()) {
                        if (repository.pushScheduledSlotToD1(cfg, enrichedSlot).isSuccess) {
                            d1SyncedCount++
                        }
                    }
                }

                if (warehouseArticlesToSave.isNotEmpty()) {
                    val summaryLog = WritingActivityLog(
                        logId = "wlog-todak-pipe-${System.currentTimeMillis()}",
                        warehouseId = warehouseArticlesToSave.first().warehouseId,
                        caseId = warehouseArticlesToSave.first().slot.auctionCase.caseId,
                        actionTitle = "딱찰 관리자 DB→크롤링→95+ 일괄생성 (${generatedSlots.size}건)",
                        score = warehouseArticlesToSave.maxOf { it.evaluation.totalScore },
                        passedGate95 = warehouseArticlesToSave.all { it.evaluation.eligibleForRegistration },
                        detail = "DB ${dbResult.items.size}건 · 크롤링 ${crawlResult.items.size}건 · 95+ 생성 ${generatedSlots.size}건" +
                            if (d1SyncedCount > 0) " · D1 ${d1SyncedCount}건 반영" else "",
                        timestamp = now
                    )
                    repository.upsertWarehouseArticles(warehouseArticlesToSave, summaryLog)
                }

                val combinedLogs = mutableListOf<AdminPipelineStepLog>().apply {
                    addAll(dbResult.stepLogs)
                    crawlResult.stepLogs.forEach { step ->
                        add(step.copy(stepIndex = size + 1))
                    }
                    add(
                        AdminPipelineStepLog(
                            stepIndex = size + 1,
                            title = "DB 기준 95+ 3채널 원고 생성·창고 저장",
                            endpoint = "Article30PointEvaluator + Warehouse",
                            status = when {
                                generatedSlots.isNotEmpty() -> AdminPipelineStageStatus.SUCCESS
                                crawlResult.isSuccess || dbResult.items.isNotEmpty() -> AdminPipelineStageStatus.WARN
                                else -> AdminPipelineStageStatus.ERROR
                            },
                            httpCode = if (generatedSlots.isNotEmpty()) 200 else crawlResult.httpStatusCode,
                            latencyMs = 0L,
                            detail = if (generatedSlots.isNotEmpty()) {
                                "95+ 검수 완료 ${generatedSlots.size}건 생성" +
                                    if (d1SyncedCount > 0) " · CF D1 ${d1SyncedCount}건 저장" else ""
                            } else {
                                "서버 DB/크롤링 매물이 0건이라 글을 생성하지 않았습니다 (인증·엔드포인트 확인 필요)"
                            }
                        )
                    )
                }

                val pipelineOk = generatedSlots.isNotEmpty() || crawlResult.isSuccess || dbResult.d1TableNames.isNotEmpty()
                val summaryText = when {
                    generatedSlots.isNotEmpty() ->
                        "딱찰 마스터 완료: DB ${dbResult.items.size}건 + 크롤링 ${crawlResult.items.size}건 → 95+ 글 ${generatedSlots.size}건 생성!"
                    pipelineOk ->
                        "서버 연동 확인(${dbResult.tables.size}테이블) · 조건에 맞는 신규 매물이 0건입니다."
                    else ->
                        "딱찰 서버 연동 실패: ${crawlResult.summaryMessage}"
                }

                _uiState.update { cur ->
                    val nextSlots = if (generatedSlots.isNotEmpty()) {
                        generatedSlots + cur.slots.filter { old ->
                            generatedSlots.none { g -> g.auctionCase.dedupKey == old.auctionCase.dedupKey }
                        }
                    } else cur.slots
                    val firstGenerated = generatedSlots.firstOrNull() ?: nextSlots.firstOrNull()
                    val nextWh = if (warehouseArticlesToSave.isNotEmpty()) {
                        warehouseArticlesToSave + cur.warehouseArticles.filter { old ->
                            warehouseArticlesToSave.none { w -> w.warehouseId == old.warehouseId }
                        }
                    } else cur.warehouseArticles
                    val nextTopics = TopicSelectionEngine.rankCandidates(
                        slots = nextSlots,
                        config = cur.config,
                        proFunnel = cur.proFunnel,
                        sortMode = cur.topicSelectionBoard.sortMode,
                        naverKeywordDiagnosis = (cur.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report,
                        nowText = now
                    )
                    cur.copy(
                        isBusy = false,
                        slots = nextSlots,
                        selectedSlotId = firstGenerated?.postId ?: cur.selectedSlotId,
                        warehouseArticles = nextWh,
                        activeCourtArchive = firstGenerated?.let {
                            CourtEvidenceRecorder.recordFullArchiveForSlot(it)
                        } ?: cur.activeCourtArchive,
                        activeRegistryInfographic = firstGenerated?.let {
                            RegistryInfographicEngine.buildTemplate(it.auctionCase, cur.selectedRegistryTemplateType)
                        } ?: cur.activeRegistryInfographic,
                        legalVerificationByCaseId = cur.legalVerificationByCaseId + newLegalMap,
                        attractivenessByCaseId = cur.attractivenessByCaseId + newAttrMap,
                        topicSelectionBoard = nextTopics,
                        naverPublishStatuses = buildNaverPublishStatuses(nextSlots, cur.publishLogs, cur.config),
                        telemetry = cur.telemetry.copy(
                            ddakchalConnected = pipelineOk,
                            cloudflareD1Connected = d1SyncedCount > 0 || dbResult.d1TableNames.isNotEmpty() || cur.telemetry.cloudflareD1Connected,
                            lastSyncTime = if (pipelineOk) now else cur.telemetry.lastSyncTime,
                            apiLatencyMs = crawlResult.latencyMs.takeIf { it > 0L } ?: cur.telemetry.apiLatencyMs,
                            lastHttpStatusCode = crawlResult.httpStatusCode,
                            lastErrorDetail = if (pipelineOk) null else summaryText,
                            triggerStatus = crawlResult.triggerStatus ?: dbResult.triggerStatus ?: cur.telemetry.triggerStatus
                        ),
                        todakAdmin = cur.todakAdmin.copy(
                            isRunning = false,
                            currentActionLabel = "",
                            discoveredTables = dbResult.tables,
                            discoveredItems = mergedItems,
                            generatedPostIds = generatedSlots.map { it.postId },
                            stepLogs = combinedLogs,
                            d1TablesRaw = dbResult.d1TableNames,
                            lastUpdatedAt = now,
                            summaryMessage = summaryText
                        ),
                        statusMessage = if (pipelineOk) summaryText else null,
                        errorMessage = if (!pipelineOk) summaryText else null
                    )
                }
                if (pipelineOk) {
                    syncRealBlogQueueInternal(cfg = cfg, limit = 100, announceBanner = false)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val err = "전체 파이프라인 실행 오류: ${e.localizedMessage ?: "알 수 없는 오류"}"
                _uiState.update { cur ->
                    cur.copy(
                        isBusy = false,
                        todakAdmin = cur.todakAdmin.copy(
                            isRunning = false,
                            currentActionLabel = "",
                            summaryMessage = err
                        ),
                        errorMessage = err
                    )
                }
            }
        }
    }

    fun generatePostFromDiscoveredDbItem(recordId: String) {
        val state = _uiState.value
        val item = state.todakAdmin.discoveredItems.firstOrNull {
            it.recordId == recordId || it.dedupKey == recordId || it.caseId == recordId
        } ?: return
        viewModelScope.launch {
            _uiState.update { cur ->
                cur.copy(
                    isBusy = true,
                    errorMessage = null,
                    statusMessage = null,
                    todakAdmin = cur.todakAdmin.copy(
                        isRunning = true,
                        currentActionLabel = "${item.caseId} DB 기준 95+ 3채널 원고 생성 중…"
                    )
                )
            }
            try {
                val corpus = repository.getRecent7DayCorpus()
                val now = currentTimestamp()
                val (enrichedSlot, whArticle, legal, attractiveness) = buildEnriched95SlotFromServerSlot(
                    rawSlot = item.slot,
                    slotIndex = state.slots.size + 1,
                    config = state.config,
                    proFunnel = state.proFunnel,
                    postTemplates = state.postTemplates,
                    selectedRegTemplateType = state.selectedRegistryTemplateType,
                    recentCorpus = corpus,
                    nowText = now
                )
                val log = WritingActivityLog(
                    logId = "wlog-dbgen-${System.currentTimeMillis()}",
                    warehouseId = whArticle.warehouseId,
                    caseId = enrichedSlot.auctionCase.caseId,
                    actionTitle = "서버 DB 단건 → 95+ 원고 생성 (${whArticle.evaluation.totalScore}점)",
                    score = whArticle.evaluation.totalScore,
                    passedGate95 = whArticle.evaluation.eligibleForRegistration,
                    detail = "${item.sourceOrigin} · ${enrichedSlot.auctionCase.address}",
                    timestamp = now
                )
                val snapshot = repository.upsertWarehouseArticle(whArticle, log)
                val d1Res = if (state.config.cfAccountId.isNotBlank() &&
                    state.config.cfD1DatabaseId.isNotBlank() &&
                    state.config.cfApiToken.isNotBlank()
                ) {
                    repository.pushScheduledSlotToD1(state.config, enrichedSlot)
                } else null

                _uiState.update { cur ->
                    val nextSlots = listOf(enrichedSlot) + cur.slots.filter {
                        it.auctionCase.dedupKey != enrichedSlot.auctionCase.dedupKey
                    }
                    val nextTopics = TopicSelectionEngine.rankCandidates(
                        slots = nextSlots,
                        config = cur.config,
                        proFunnel = cur.proFunnel,
                        sortMode = cur.topicSelectionBoard.sortMode,
                        naverKeywordDiagnosis = (cur.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report,
                        nowText = now
                    )
                    cur.copy(
                        isBusy = false,
                        slots = nextSlots,
                        selectedSlotId = enrichedSlot.postId,
                        warehouseArticles = snapshot?.articles ?: (listOf(whArticle) + cur.warehouseArticles),
                        writingLogs = snapshot?.writingLogs ?: (listOf(log) + cur.writingLogs),
                        activeCourtArchive = CourtEvidenceRecorder.recordFullArchiveForSlot(enrichedSlot),
                        activeRegistryInfographic = RegistryInfographicEngine.buildTemplate(
                            enrichedSlot.auctionCase,
                            cur.selectedRegistryTemplateType
                        ),
                        legalVerificationByCaseId = cur.legalVerificationByCaseId + (enrichedSlot.auctionCase.caseId to legal),
                        attractivenessByCaseId = cur.attractivenessByCaseId + (enrichedSlot.auctionCase.caseId to attractiveness),
                        topicSelectionBoard = nextTopics,
                        naverPublishStatuses = buildNaverPublishStatuses(nextSlots, cur.publishLogs, cur.config),
                        todakAdmin = cur.todakAdmin.copy(
                            isRunning = false,
                            currentActionLabel = "",
                            generatedPostIds = (listOf(enrichedSlot.postId) + cur.todakAdmin.generatedPostIds).distinct(),
                            lastUpdatedAt = now,
                            summaryMessage = "${enrichedSlot.auctionCase.caseId} DB 기준 ${whArticle.evaluation.totalScore}점 3채널 글 생성 완료" +
                                if (d1Res?.isSuccess == true) " (D1 반영)" else ""
                        ),
                        statusMessage = "${enrichedSlot.auctionCase.caseId} DB 기반 95+ 3채널 글 생성 및 창고 저장 완료 (${whArticle.evaluation.totalScore}점)"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val err = "DB 기준 글 생성 오류: ${e.localizedMessage ?: "알 수 없는 오류"}"
                _uiState.update { cur ->
                    cur.copy(
                        isBusy = false,
                        todakAdmin = cur.todakAdmin.copy(isRunning = false, currentActionLabel = "", summaryMessage = err),
                        errorMessage = err
                    )
                }
            }
        }
    }

    fun setTodakCaseLookupInput(input: String) {
        _uiState.update { state ->
            state.copy(todakAdmin = state.todakAdmin.copy(caseLookupInput = input))
        }
    }

    fun consumePendingAutoPreview() {
        _uiState.update { state ->
            if (state.pendingAutoPreviewSlotId == null) state
            else state.copy(pendingAutoPreviewSlotId = null)
        }
    }

    /**
     * 법원 사건번호 입력 시 딱찰 서버에서 경매 사진·등기부(갑구/을구)·문서·방문기록을 불러와
     * 95+ 점수로 칼분석 보강 후 자동으로 프리뷰 모달(ArticleQualityPreviewModal)을 연다.
     */
    fun lookupCaseNumberFromTodakAndAutoPreview(
        rawCaseInput: String = _uiState.value.todakAdmin.caseLookupInput,
        preferredTabIndex: Int = 0
    ) {
        val cleanInput = rawCaseInput.trim()
        if (cleanInput.isBlank()) {
            _uiState.update { cur ->
                cur.copy(
                    todakAdmin = cur.todakAdmin.copy(
                        caseLookupStatus = "사건번호를 입력해 주세요. (예: 2025타경6297)"
                    ),
                    errorMessage = "사건번호를 입력해 주세요. (예: 2025타경6297)"
                )
            }
            return
        }
        val state = _uiState.value
        val cfg = state.config
        viewModelScope.launch {
            _uiState.update { cur ->
                cur.copy(
                    isBusy = true,
                    errorMessage = null,
                    statusMessage = null,
                    todakAdmin = cur.todakAdmin.copy(
                        caseLookupInput = cleanInput,
                        isRunning = true,
                        currentActionLabel = "딱찰 서버에서 '$cleanInput' 사진·등기·방문기록 조회 중…",
                        caseLookupStatus = "딱찰 서버 조회 중 ($cleanInput)…"
                    )
                )
            }
            try {
                val lookup = repository.lookupCourtCaseFromTodakServer(cfg, cleanInput)
                val now = currentTimestamp()
                val fetchedSlot = lookup.slot
                if (!lookup.isSuccess || fetchedSlot == null) {
                    _uiState.update { cur ->
                        cur.copy(
                            isBusy = false,
                            telemetry = cur.telemetry.copy(
                                lastHttpStatusCode = lookup.httpStatusCode,
                                lastErrorDetail = lookup.message
                            ),
                            todakAdmin = cur.todakAdmin.copy(
                                isRunning = false,
                                currentActionLabel = "",
                                stepLogs = lookup.stepLogs,
                                lastUpdatedAt = now,
                                caseLookupStatus = lookup.message,
                                summaryMessage = lookup.message
                            ),
                            errorMessage = lookup.message
                        )
                    }
                    return@launch
                }

                val corpus = repository.getRecent7DayCorpus()
                val latestState = _uiState.value
                val (enrichedSlot, whArticle, legal, attractiveness) = buildEnriched95SlotFromServerSlot(
                    rawSlot = fetchedSlot,
                    slotIndex = latestState.slots.size + 1,
                    config = cfg,
                    proFunnel = latestState.proFunnel,
                    postTemplates = latestState.postTemplates,
                    selectedRegTemplateType = latestState.selectedRegistryTemplateType,
                    recentCorpus = corpus,
                    nowText = now
                )

                val log = WritingActivityLog(
                    logId = "wlog-case-lookup-${System.currentTimeMillis()}",
                    warehouseId = whArticle.warehouseId,
                    caseId = enrichedSlot.auctionCase.caseId,
                    actionTitle = "사건조회 자동프리뷰 · 칼분석 (${whArticle.evaluation.totalScore}점)",
                    score = whArticle.evaluation.totalScore,
                    passedGate95 = whArticle.evaluation.eligibleForRegistration,
                    detail = "${lookup.message} · ${enrichedSlot.auctionCase.address}",
                    timestamp = now
                )
                val snapshot = repository.upsertWarehouseArticle(whArticle, log)
                val archive = CourtEvidenceRecorder.recordFullArchiveForSlot(enrichedSlot)
                val discoveredItem = ServerDbDiscoveredItem(
                    recordId = enrichedSlot.postId,
                    dedupKey = enrichedSlot.auctionCase.dedupKey,
                    caseId = enrichedSlot.auctionCase.caseId,
                    courtName = enrichedSlot.auctionCase.courtName,
                    address = enrichedSlot.auctionCase.address,
                    propertyType = enrichedSlot.auctionCase.propertyType,
                    appraisalKrw = enrichedSlot.auctionCase.appraisalPriceKrw,
                    minBidKrw = enrichedSlot.auctionCase.minBidPriceKrw,
                    photoCount = enrichedSlot.courtPhotoUrls.count { it.isNotBlank() },
                    visitCount = archive.visitRecords.size,
                    hasAnalysisData = enrichedSlot.ddakchalAnalysisData != null || archive.sharpAnalysis != null,
                    sourceOrigin = "사건번호 즉시조회",
                    updatedAt = enrichedSlot.serverUpdatedAt.ifBlank { now },
                    slot = enrichedSlot
                )

                _uiState.update { cur ->
                    val nextSlots = listOf(enrichedSlot) + cur.slots.filter {
                        it.auctionCase.dedupKey != enrichedSlot.auctionCase.dedupKey &&
                            !it.auctionCase.caseId.equals(enrichedSlot.auctionCase.caseId, ignoreCase = true)
                    }
                    val nextDiscovered = listOf(discoveredItem) + cur.todakAdmin.discoveredItems.filter {
                        it.dedupKey != discoveredItem.dedupKey
                    }
                    val nextTopics = TopicSelectionEngine.rankCandidates(
                        slots = nextSlots,
                        config = cur.config,
                        proFunnel = cur.proFunnel,
                        sortMode = cur.topicSelectionBoard.sortMode,
                        naverKeywordDiagnosis = (cur.naverKeywordDiagnosis as? NaverKeywordDiagnosisUiState.Success)?.report,
                        nowText = now
                    )
                    val readyStatus = "${enrichedSlot.auctionCase.caseId} 자동 프리뷰 준비 완료 (사진 ${archive.photos.count { it.imageUrl.isNotBlank() }}실사 · 등기 ${archive.registryGapGu.size + archive.registryEulGu.size}건 · 방문 ${archive.visitRecords.size}건 · 품질 ${whArticle.evaluation.totalScore}점)"
                    cur.copy(
                        isBusy = false,
                        slots = nextSlots,
                        selectedSlotId = enrichedSlot.postId,
                        warehouseArticles = snapshot?.articles ?: (listOf(whArticle) + cur.warehouseArticles),
                        writingLogs = snapshot?.writingLogs ?: (listOf(log) + cur.writingLogs),
                        activeCourtArchive = archive,
                        activeRegistryInfographic = RegistryInfographicEngine.buildTemplate(
                            enrichedSlot.auctionCase,
                            cur.selectedRegistryTemplateType
                        ),
                        legalVerificationByCaseId = cur.legalVerificationByCaseId + (enrichedSlot.auctionCase.caseId to legal),
                        attractivenessByCaseId = cur.attractivenessByCaseId + (enrichedSlot.auctionCase.caseId to attractiveness),
                        topicSelectionBoard = nextTopics,
                        naverPublishStatuses = buildNaverPublishStatuses(nextSlots, cur.publishLogs, cur.config),
                        telemetry = cur.telemetry.copy(
                            ddakchalConnected = true,
                            lastSyncTime = now,
                            apiLatencyMs = lookup.latencyMs.takeIf { it > 0L } ?: cur.telemetry.apiLatencyMs,
                            lastHttpStatusCode = lookup.httpStatusCode,
                            lastErrorDetail = null
                        ),
                        todakAdmin = cur.todakAdmin.copy(
                            isRunning = false,
                            currentActionLabel = "",
                            discoveredItems = nextDiscovered,
                            generatedPostIds = (listOf(enrichedSlot.postId) + cur.todakAdmin.generatedPostIds).distinct(),
                            stepLogs = lookup.stepLogs,
                            lastUpdatedAt = now,
                            caseLookupStatus = readyStatus,
                            summaryMessage = readyStatus
                        ),
                        pendingAutoPreviewSlotId = enrichedSlot.postId,
                        pendingAutoPreviewTab = preferredTabIndex.coerceIn(0, 3),
                        pendingAutoPreviewNonce = cur.pendingAutoPreviewNonce + 1L,
                        statusMessage = readyStatus
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val err = "사건번호 조회 오류: ${e.localizedMessage ?: "알 수 없는 오류"}"
                _uiState.update { cur ->
                    cur.copy(
                        isBusy = false,
                        todakAdmin = cur.todakAdmin.copy(
                            isRunning = false,
                            currentActionLabel = "",
                            caseLookupStatus = err,
                            summaryMessage = err
                        ),
                        errorMessage = err
                    )
                }
            }
        }
    }

    private data class Enriched95Bundle(
        val slot: GeneratedPostSlot,
        val warehouseArticle: WarehouseArticle,
        val legal: LegalDoctrineReport,
        val attractiveness: InvestmentAttractivenessScore
    )

    private fun buildEnriched95SlotFromServerSlot(
        rawSlot: GeneratedPostSlot,
        slotIndex: Int,
        config: ServerAutomationConfig,
        proFunnel: ProFunnelConfig,
        postTemplates: List<BlogPostTemplate>,
        selectedRegTemplateType: RegistryInfographicTemplateType,
        recentCorpus: List<String>,
        nowText: String
    ): Enriched95Bundle {
        val auction = rawSlot.auctionCase
        val activeTemplatesForType = postTemplates.filter {
            it.propertyType == auction.propertyType && it.isActiveForGeneration && !it.isPreset
        }
        val templatedVariants = if (activeTemplatesForType.isNotEmpty()) {
            rawSlot.variants.mapValues { (platform, variant) ->
                val matched = activeTemplatesForType.firstOrNull { it.targetChannel == platform }
                if (matched != null) {
                    BlogPostTemplateEngine.applyTemplateToVariant(variant, matched, auction, config, proFunnel)
                } else variant
            }
        } else {
            rawSlot.variants
        }
        val scheduled = repository.computeStaggeredSchedule(
            rawSlot.publishDate.ifBlank { PostAuctionFollowUpEngine.currentIsoDate() },
            slotIndex,
            config
        )
        val draft = rawSlot.copy(
            slotIndex = slotIndex,
            variants = templatedVariants,
            scheduledTimes = scheduled,
            sourceLabel = rawSlot.sourceLabel.ifBlank { "딱찰 DB 자동생성" }
        )
        val fixed = WeeklyBatchScheduleEngine.ensureScoreAtLeast95(
            slot = draft,
            config = config,
            proFunnel = proFunnel,
            recent7DayCorpus = recentCorpus,
            maxIterations = 3
        )
        val finalSlot = fixed.slot.copy(
            status = if (fixed.passedGate95) PostStatus.QUEUED else PostStatus.DRAFT
        )
        val regTemplate = RegistryInfographicEngine.buildTemplate(finalSlot.auctionCase, selectedRegTemplateType)
        val eval = Article30PointEvaluator.evaluate(finalSlot, config, proFunnel, regTemplate, recentCorpus)
        val legal = LegalDoctrineVerifier.verify(finalSlot.auctionCase)
        val attractiveness = InvestmentAttractivenessEngine.score(finalSlot.auctionCase, legal)
        val whArticle = WarehouseArticle(
            warehouseId = "wh-${finalSlot.auctionCase.dedupKey}",
            slot = finalSlot,
            evaluation = eval,
            registryInfographic = regTemplate,
            warehouseStatus = if (eval.eligibleForRegistration) WarehouseArticleStatus.STORED_READY else WarehouseArticleStatus.NEEDS_IMPROVEMENT,
            createdAt = nowText
        )
        return Enriched95Bundle(
            slot = finalSlot,
            warehouseArticle = whArticle,
            legal = legal,
            attractiveness = attractiveness
        )
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
