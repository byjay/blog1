package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.engine.Article30PointEvaluator
import com.example.engine.CourtEvidenceRecorder
import com.example.engine.PostAuctionFollowUpEngine
import com.example.engine.QaVerificationEngine
import com.example.engine.RegistryInfographicEngine
import com.example.model.ChannelPlatform
import com.example.model.DdakchalBlogAnalysisData
import com.example.model.GeneratedPostSlot
import com.example.model.NaverAutoWriteMode
import com.example.model.PostStatus
import com.example.model.PropertyType
import com.example.model.TopicSortCriteria
import com.example.model.VisitRecordSource
import com.example.ui.StudioUiState
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.CloudflareContainer
import com.example.ui.theme.CloudflareSky
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.NaverGreen
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TistoryAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    uiState: StudioUiState,
    onSelectWeeklyDailyCount: (Int) -> Unit,
    onGenerateWeeklyBatch: (Int) -> Unit,
    onCreateHighScoreAndAutoRegister: () -> Unit,
    onAutoFixAndRegisterSlot: (String) -> Unit,
    onSaveThreeChannelDestinations: (
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
    ) -> Unit,
    onPublishNaverDirectAndVerify: (String, String) -> Unit,
    onPublishSelectedChannelsAndVerify: (String, String) -> Unit,
    onOpenWarehouse: () -> Unit,
    onOpenTemplates: () -> Unit = {},
    onOpenSecurityDashboard: () -> Unit = {},
    onOpenSettings: () -> Unit = onOpenSecurityDashboard,
    onToggleMobileMode: (Boolean) -> Unit = {},
    onCrawlAndSync: () -> Unit = {},
    onRefreshServerTelemetry: () -> Unit = {},
    onRefreshQueueCharts: () -> Unit = onRefreshServerTelemetry,
    onEvaluateRisk: () -> Unit = {},
    onResumeAutoPublish: () -> Unit = {},
    onToggleAutoPauseOnHighRisk: (Boolean) -> Unit = {},
    onSearchCaseWithAi: (String) -> Unit = {},
    onDiagnoseNaverKeyword: (String) -> Unit = {},
    onTestRegisterAiSearchedCase: () -> Unit = {},
    onGenerateThreePureHtml: (Boolean, ChannelPlatform) -> Unit = { _, _ -> },
    onDismissHtmlBatch: () -> Unit = {},
    onSelectTopicSort: (TopicSortCriteria) -> Unit = {},
    onRankTopics: () -> Unit = {},
    onApplyTopTopics: () -> Unit = {},
    onSelectAndRegisterTopic: (String) -> Unit = {},
    onRunPerfectDdakchalDiag: () -> Unit = {},
    onRefreshNaverPublishStatus: () -> Unit = {},
    onSelectNaverWriteMode: (NaverAutoWriteMode) -> Unit = {},
    onToggleAdminRegion: (String) -> Unit = {},
    onSelectAdminCategory: (String) -> Unit = {},
    onAdminSearchQueryChange: (String) -> Unit = {},
    onSelectAdminCrawlLimit: (Int) -> Unit = {},
    onSaveAdminInlineAuth: (
        apiBase: String,
        syncToken: String,
        cfAccountId: String,
        cfD1DatabaseId: String,
        cfApiToken: String
    ) -> Unit = { _, _, _, _, _ -> },
    onDiscoverServerDb: () -> Unit = {},
    onRunTodakCrawlOnly: () -> Unit = {},
    onRunTodakFullPipeline: () -> Unit = {},
    onGenerateFromDbItem: (String) -> Unit = {},
    onCaseLookupInputChange: (String) -> Unit = {},
    onLookupCaseAndAutoPreview: (caseInput: String, preferredTab: Int) -> Unit = { _, _ -> },
    onConsumeAutoPreview: () -> Unit = {},
    onOpenHtmlLinkHub: () -> Unit = {},
    onOpenHtmlHubViewer: () -> Unit = {},
    onOpenHtmlArticleViewer: (String, ChannelPlatform) -> Unit = { _, _ -> },
    onVerifySlot: (String) -> Unit = {},
    onAutoFixSlotForPreview: (String) -> Unit = onAutoFixAndRegisterSlot,
    onSaveManualEdit: (String, ChannelPlatform, String, String, String) -> Unit = { _, _, _, _, _ -> },
    onOpenQaTab: () -> Unit = {},
    onRunAiEvalSelected: () -> Unit,
    onTestBusanProfitableLand: () -> Unit,
    onTestLowScoreDraft: () -> Unit,
    onApplyAiAutoFixes: (String) -> Unit,
    onSyncPack: () -> Unit,
    onInspectSlot: (String) -> Unit,
    onResolveHandoff: () -> Unit,
    onSelectChannel: (ChannelPlatform) -> Unit = {},
    onAttachCourtPhoto: (postId: String, shotOrder: Int, uriString: String) -> Unit = { _, _, _ -> },
    onAddVisitRecord: (postId: String, source: VisitRecordSource, visitedAt: String, occupancyStatus: String, findingsNote: String) -> Unit = { _, _, _, _, _ -> },
    onAddCustomProperty: (
        caseId: String,
        courtName: String,
        address: String,
        propertyType: PropertyType,
        appraisalManWon: Long,
        minBidManWon: Long,
        baseRightDate: String,
        assumedRights: String,
        publishDate: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showDestinationSetup by rememberSaveable { mutableStateOf(false) }
    var showAdvancedTools by rememberSaveable { mutableStateOf(false) }
    var previewModalSlotId by rememberSaveable { mutableStateOf<String?>(null) }
    var previewModalInitialTab by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(uiState.pendingAutoPreviewNonce, uiState.pendingAutoPreviewSlotId) {
        val targetSlotId = uiState.pendingAutoPreviewSlotId
        if (uiState.pendingAutoPreviewNonce > 0L && !targetSlotId.isNullOrBlank()) {
            previewModalInitialTab = uiState.pendingAutoPreviewTab
            previewModalSlotId = targetSlotId
            onConsumeAutoPreview()
        }
    }

    val selectedSlot = uiState.slots.firstOrNull { it.postId == uiState.selectedSlotId }
        ?: uiState.slots.firstOrNull()
    val dailyCount = uiState.selectedWeeklyDailyCount.coerceIn(3, 5)
    val queuedOrPublishedCount = uiState.slots.count {
        it.status == PostStatus.QUEUED || it.status == PostStatus.PUBLISHED
    }
    val publishedCount = uiState.slots.count { it.status == PostStatus.PUBLISHED }
    val progressRatio = if (uiState.slots.isEmpty()) 0f else {
        (queuedOrPublishedCount.toFloat() / uiState.slots.size.toFloat()).coerceIn(0f, 1f)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==================== TOP-LEVEL OWNER ADMIN CONSOLE (TODAK / DDAKCHAL) ====================
        item(key = "todak_top_admin_console") {
            TodakAdminConsoleCard(
                uiState = uiState,
                onToggleRegion = onToggleAdminRegion,
                onSelectCategory = onSelectAdminCategory,
                onSearchQueryChange = onAdminSearchQueryChange,
                onSelectCrawlLimit = onSelectAdminCrawlLimit,
                onSaveInlineAuth = onSaveAdminInlineAuth,
                onDiscoverServerDb = onDiscoverServerDb,
                onRunCrawlEngineOnly = onRunTodakCrawlOnly,
                onRunFullAdminPipeline = onRunTodakFullPipeline,
                onSyncQueueAndD1 = onRefreshQueueCharts,
                onGenerateFromDbItem = onGenerateFromDbItem,
                onInspectSlot = onInspectSlot,
                onNavigateWarehouse = onOpenWarehouse,
                onNavigateQa = onOpenQaTab,
                onNavigateTemplates = onOpenTemplates,
                onNavigateServer = onOpenSettings,
                onCaseLookupInputChange = onCaseLookupInputChange,
                onLookupCaseAndAutoPreview = onLookupCaseAndAutoPreview,
                onOpenHtmlLinkHub = onOpenHtmlLinkHub,
                onOpenHtmlHubViewer = onOpenHtmlHubViewer
            )
        }

        if (uiState.isMobileSimpleMode) {
            // ==================== APP-ICON MOBILE AUTO-PROGRESS MODE ====================
            item {
                StudioCard(
                    borderColor = EmeraldPrimary.copy(alpha = 0.55f),
                    modifier = Modifier.testTag("mobile_simple_progress_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "모바일 자동 진행 모니터",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = if (uiState.telemetry.ddakchalConnected) {
                                    "딱찰 실서버 자동감시 · 응답 ${uiState.telemetry.apiLatencyMs ?: "--"}ms"
                                } else {
                                    "자동 준비 완료 · 대기 슬롯 ${uiState.slots.size}건 검수됨"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.telemetry.ddakchalConnected) MintSecondary else TextSecondary
                            )
                        }
                        OutlinedButton(
                            onClick = { onToggleMobileMode(false) },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("btn_switch_to_precision_mode")
                        ) {
                            Icon(Icons.Filled.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("정밀 대시", maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = EmeraldPrimary,
                        trackColor = NavySurfaceElevated
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "자동화 진행률 ${(progressRatio * 100).toInt()}% (완료 $publishedCount · 예약 ${queuedOrPublishedCount - publishedCount})",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                            color = MintSecondary
                        )
                        Text(
                            text = "일 ${dailyCount}건×7일",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                            color = CloudflareSky
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    // App-Icon 3x2 Grid
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AppIconActionTile(
                                icon = Icons.Filled.Bolt,
                                label = "95+ 바로등록",
                                subLabel = "1건 자동교정",
                                accent = EmeraldPrimary,
                                container = EmeraldContainer,
                                enabled = !uiState.isBusy,
                                onClick = onCreateHighScoreAndAutoRegister,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_instant_highscore_register")
                            )
                            AppIconActionTile(
                                icon = Icons.Filled.CalendarMonth,
                                label = "주간 7일예약",
                                subLabel = "주 ${dailyCount * 7}건",
                                accent = CloudflareSky,
                                container = CloudflareContainer,
                                enabled = !uiState.isBusy,
                                onClick = { onGenerateWeeklyBatch(dailyCount) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_generate_weekly_7d")
                            )
                            AppIconActionTile(
                                icon = Icons.Filled.CloudSync,
                                label = "크롤링·상위",
                                subLabel = "실서버 피드",
                                accent = NaverGreen,
                                container = EmeraldContainer,
                                enabled = !uiState.isBusy,
                                onClick = onCrawlAndSync,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_app_tile_crawl_sync")
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AppIconActionTile(
                                icon = Icons.Filled.Inventory2,
                                label = "글 창고",
                                subLabel = "${uiState.warehouseArticles.size}건 보관",
                                accent = TistoryAmber,
                                container = AmberContainer,
                                enabled = true,
                                onClick = onOpenWarehouse,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_open_warehouse_shortcut")
                            )
                            AppIconActionTile(
                                icon = Icons.Filled.Tune,
                                label = "템플릿·SEO",
                                subLabel = "물건별 ${uiState.postTemplates.size}종",
                                accent = MintSecondary,
                                container = NavySurfaceElevated,
                                enabled = true,
                                onClick = onOpenTemplates,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_app_tile_templates")
                            )
                            AppIconActionTile(
                                icon = Icons.Filled.Analytics,
                                label = "정밀 분석",
                                subLabel = "상세 대시보드",
                                accent = CloudflareSky,
                                container = NavySurfaceElevated,
                                enabled = true,
                                onClick = { onToggleMobileMode(false) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_app_tile_precision")
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AppIconActionTile(
                                icon = Icons.Filled.Security,
                                label = "보안 대시",
                                subLabel = uiState.securityDashboard.overallGrade.label,
                                accent = uiState.securityDashboard.overallGrade.accentColor(),
                                container = uiState.securityDashboard.overallGrade.containerColor(),
                                enabled = true,
                                onClick = onOpenSecurityDashboard,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tile_open_security")
                            )
                            AppIconActionTile(
                                icon = Icons.Filled.Code,
                                label = "순수 HTML 3개",
                                subLabel = "단독 HTML 생성",
                                accent = MintSecondary,
                                container = EmeraldContainer,
                                enabled = !uiState.isBusy,
                                onClick = { onGenerateThreePureHtml(true, uiState.selectedChannel) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_generate_3_html")
                            )
                            AppIconActionTile(
                                icon = Icons.Filled.Settings,
                                label = "환경설정",
                                subLabel = "계정·CF·AI",
                                accent = CloudflareSky,
                                container = CloudflareContainer,
                                enabled = true,
                                onClick = onOpenSettings,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tile_open_settings")
                            )
                        }
                    }
                }
            }

            item {
                LowQualityRiskDashboardCard(
                    report = uiState.lowQualityRiskReport,
                    autoPublishPaused = uiState.autoPublishPausedByRisk,
                    pauseReason = uiState.autoPublishPauseReason,
                    autoPauseEnabled = uiState.config.autoPauseOnHighRisk,
                    isBusy = uiState.isBusy,
                    onEvaluateRisk = onEvaluateRisk,
                    onResumeAutoPublish = onResumeAutoPublish,
                    onToggleAutoPause = onToggleAutoPauseOnHighRisk,
                    onOpenSettings = onOpenSettings,
                    compactMode = true
                )
            }

            item {
                TopicSelectionAdvisorCard(
                    board = uiState.topicSelectionBoard,
                    isBusy = uiState.isBusy,
                    onSelectSortCriteria = onSelectTopicSort,
                    onRankTopics = onRankTopics,
                    onApplyTopTopics = onApplyTopTopics,
                    onSelectAndAutoRegister = onSelectAndRegisterTopic
                )
            }

            item {
                AiCaseSearchAndTestRegisterCard(
                    slots = uiState.slots,
                    aiCaseSearchState = uiState.aiCaseSearchState,
                    isBusy = uiState.isBusy,
                    onSearchAndOptimizeCase = onSearchCaseWithAi,
                    onTestRegisterSearchedCase = onTestRegisterAiSearchedCase,
                    onGenerateThreeHtml = { onGenerateThreePureHtml(true, uiState.selectedChannel) },
                    naverKeywordDiagnosis = uiState.naverKeywordDiagnosis,
                    onDiagnoseNaverKeyword = onDiagnoseNaverKeyword
                )
            }

            item {
                NaverPublishStatusAndMethodsCard(
                    publishStatuses = uiState.naverPublishStatuses,
                    writeGuides = uiState.naverWriteGuides,
                    activeMode = uiState.config.naverWriteMode,
                    isBusy = uiState.isBusy,
                    onRefreshPublishStatuses = onRefreshNaverPublishStatus,
                    onSelectWriteMode = onSelectNaverWriteMode
                )
            }

            item {
                DdakchalPerfectDiagnosticCard(
                    report = uiState.ddakchalFullDiagnostic,
                    isBusy = uiState.isBusy,
                    onRunFullDiagnostic = onRunPerfectDdakchalDiag
                )
            }

            item {
                QueueAnalyticsChartsCard(
                    analytics = uiState.queueAnalytics,
                    telemetry = uiState.telemetry,
                    queueSyncState = uiState.queueSyncState,
                    compactMode = true,
                    onRefreshServer = onRefreshServerTelemetry,
                    onRefreshQueue = onRefreshQueueCharts,
                    onOpenSettings = onOpenSettings
                )
            }
        } else {
            // ==================== PRECISION DETAILED DASHBOARD MODE ====================
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = NavySurface,
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f))
                ) {
                    Box {
                        Image(
                            painter = painterResource(id = R.drawable.img_studio_hero),
                            contentDescription = "딱찰 주간 자동 예약 스튜디오",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .matchParentSize()
                                .alpha(0.14f)
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            NavyBackground.copy(alpha = 0.55f),
                                            NavySurface.copy(alpha = 0.96f)
                                        )
                                    )
                                )
                        )
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusChip(
                                    text = "정밀 대시보드 · 95점+ 자동등록",
                                    textColor = MintSecondary,
                                    backgroundColor = EmeraldContainer,
                                    dotColor = EmeraldPrimary
                                )
                                StatusChip(
                                    text = if (uiState.telemetry.ddakchalConnected) {
                                        "딱찰 연결 (${uiState.telemetry.apiLatencyMs ?: "--"}ms)"
                                    } else {
                                        "로컬 풀 대기"
                                    },
                                    textColor = if (uiState.telemetry.ddakchalConnected) MintSecondary else CloudflareSky,
                                    backgroundColor = if (uiState.telemetry.ddakchalConnected) EmeraldContainer else CloudflareContainer
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "생성 · 고득점 자동교정 · 주간예약",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = "주 1회만 누르면 하루 3~5건씩(주 21~35건) 중복 없이 95점+로 교정해 예약·등록합니다.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.small)
                                    .background(NavySurfaceElevated)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MetricPair("예약·완료", "${queuedOrPublishedCount}건", MintSecondary, Modifier.weight(1f))
                                MetricPair("주간 목표", "${dailyCount * 7}건/주", CloudflareSky, Modifier.weight(1f))
                                MetricPair("창고 보관", "${uiState.warehouseArticles.size}건", TistoryAmber, Modifier.weight(1f))
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("하루 발행량 선택 (7일 분산)", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(3, 4, 5).forEach { count ->
                                    FilterChip(
                                        selected = dailyCount == count,
                                        onClick = { onSelectWeeklyDailyCount(count) },
                                        label = { Text("일 ${count}건 (주 ${count * 7}건)") },
                                        modifier = Modifier.weight(1f).testTag("chip_daily_$count")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onGenerateWeeklyBatch(dailyCount) },
                                enabled = !uiState.isBusy,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("btn_generate_weekly_7d"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = NavyBackground
                                )
                            ) {
                                Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "딱찰 연동 7일치(${dailyCount * 7}건) 생성 · 95+ 예약",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onCreateHighScoreAndAutoRegister,
                                    enabled = !uiState.isBusy,
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .height(48.dp)
                                        .testTag("btn_instant_highscore_register"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CloudflareSky,
                                        contentColor = NavyBackground
                                    )
                                ) {
                                    Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("95+ 바로 등록", fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                                OutlinedButton(
                                    onClick = onCrawlAndSync,
                                    enabled = !uiState.isBusy,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_precision_crawl_and_sync")
                                ) {
                                    Icon(Icons.Filled.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("크롤링·피드", maxLines = 1)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onGenerateThreePureHtml(true, uiState.selectedChannel) },
                                    enabled = !uiState.isBusy,
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .height(48.dp)
                                        .testTag("btn_precision_generate_3_html"),
                                    border = BorderStroke(1.dp, MintSecondary.copy(alpha = 0.65f))
                                ) {
                                    Icon(Icons.Filled.Code, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("순수 HTML 3개 생성", color = MintSecondary, maxLines = 1)
                                }
                                OutlinedButton(
                                    onClick = onOpenSecurityDashboard,
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .height(48.dp)
                                        .testTag("btn_precision_open_security")
                                ) {
                                    Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("보안(${uiState.securityDashboard.overallGrade.name})", maxLines = 1)
                                }
                            }

                            if (uiState.weeklyBatchSummary != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = NavyBackground,
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = uiState.weeklyBatchSummary.summaryText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MintSecondary,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Low Quality Risk & Channel Index Monitoring Card
            item {
                LowQualityRiskDashboardCard(
                    report = uiState.lowQualityRiskReport,
                    autoPublishPaused = uiState.autoPublishPausedByRisk,
                    pauseReason = uiState.autoPublishPauseReason,
                    autoPauseEnabled = uiState.config.autoPauseOnHighRisk,
                    isBusy = uiState.isBusy,
                    onEvaluateRisk = onEvaluateRisk,
                    onResumeAutoPublish = onResumeAutoPublish,
                    onToggleAutoPause = onToggleAutoPauseOnHighRisk,
                    onOpenSettings = onOpenSettings,
                    compactMode = false
                )
            }

            // Precision Queue Analytics Charts
            item {
                QueueAnalyticsChartsCard(
                    analytics = uiState.queueAnalytics,
                    telemetry = uiState.telemetry,
                    queueSyncState = uiState.queueSyncState,
                    compactMode = false,
                    onRefreshServer = onRefreshServerTelemetry,
                    onRefreshQueue = onRefreshQueueCharts,
                    onOpenSettings = onOpenSettings
                )
            }

            // Live DDAKCHAL Server Trigger & Analysis Data Precision Card
            item {
                DdakchalPrecisionTelemetryCard(
                    uiState = uiState,
                    selectedSlot = selectedSlot,
                    onRefreshTelemetry = onRefreshServerTelemetry,
                    onCrawlAndSync = onCrawlAndSync
                )
            }

            // AI Case Search GEO Optimization + Immediate Test Registration
            item {
                AiCaseSearchAndTestRegisterCard(
                    slots = uiState.slots,
                    aiCaseSearchState = uiState.aiCaseSearchState,
                    isBusy = uiState.isBusy,
                    onSearchAndOptimizeCase = onSearchCaseWithAi,
                    onTestRegisterSearchedCase = onTestRegisterAiSearchedCase,
                    onGenerateThreeHtml = { onGenerateThreePureHtml(true, uiState.selectedChannel) },
                    naverKeywordDiagnosis = uiState.naverKeywordDiagnosis,
                    onDiagnoseNaverKeyword = onDiagnoseNaverKeyword
                )
            }

            item {
                TopicSelectionAdvisorCard(
                    board = uiState.topicSelectionBoard,
                    isBusy = uiState.isBusy,
                    onSelectSortCriteria = onSelectTopicSort,
                    onRankTopics = onRankTopics,
                    onApplyTopTopics = onApplyTopTopics,
                    onSelectAndAutoRegister = onSelectAndRegisterTopic
                )
            }

            item {
                NaverPublishStatusAndMethodsCard(
                    publishStatuses = uiState.naverPublishStatuses,
                    writeGuides = uiState.naverWriteGuides,
                    activeMode = uiState.config.naverWriteMode,
                    isBusy = uiState.isBusy,
                    onRefreshPublishStatuses = onRefreshNaverPublishStatus,
                    onSelectWriteMode = onSelectNaverWriteMode
                )
            }

            item {
                DdakchalPerfectDiagnosticCard(
                    report = uiState.ddakchalFullDiagnostic,
                    isBusy = uiState.isBusy,
                    onRunFullDiagnostic = onRunPerfectDdakchalDiag
                )
            }
        }

        // 2. Compact 3-Channel Destination Bar (Expandable inline)
        item {
            StudioCard(borderColor = NaverGreen.copy(alpha = 0.4f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("등록 채널 3곳 연결 상태", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        val nvLabel = uiState.config.naverBlogId.ifBlank { "네이버 미입력" }
                        val tsLabel = uiState.config.tistoryBlogUrl.ifBlank { "티스토리 미입력" }
                        Text(
                            text = "네이버($nvLabel) · 티스토리($tsLabel) · 자체웹",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    OutlinedButton(
                        onClick = { showDestinationSetup = !showDestinationSetup },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_toggle_3channel_setup")
                    ) {
                        Text(if (showDestinationSetup) "접기" else "등록처 입력")
                    }
                }

                AnimatedVisibility(visible = showDestinationSetup && selectedSlot != null) {
                    if (selectedSlot != null) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            ThreeChannelDestinationAndNaverVerifyCard(
                                slot = selectedSlot,
                                config = uiState.config,
                                report = uiState.publishVerificationReport,
                                isBusy = uiState.isBusy,
                                onSaveDestinations = onSaveThreeChannelDestinations,
                                onPublishNaverDirectAndVerify = { url ->
                                    onPublishNaverDirectAndVerify(selectedSlot.postId, url)
                                },
                                onPublishSelectedChannelsAndVerify = { url ->
                                    onPublishSelectedChannelsAndVerify(selectedSlot.postId, url)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Active CAPTCHA / 2FA handOff Alert (only when triggered)
        if (!uiState.activeHandoffAlert.isNullOrBlank()) {
            item {
                StudioCard(borderColor = CrimsonAlert, containerColor = CrimsonContainer) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, contentDescription = "세션 중단 경보", tint = CrimsonAlert)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("계정 보호 일시정지 (세션 확인 필요)", style = MaterialTheme.typography.titleSmall, color = CrimsonAlert)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(uiState.activeHandoffAlert, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onResolveHandoff,
                        modifier = Modifier.height(48.dp).testTag("btn_resolve_handoff"),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                    ) {
                        Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("세션 복구")
                    }
                }
            }
        }

        // First-Post Quality Preview + Court Basics + 1-Tap Test Publish Card
        if (selectedSlot != null) {
            item {
                val archivePreview = remember(
                    selectedSlot.postId,
                    selectedSlot.courtPhotoUrls,
                    selectedSlot.visitRecords,
                    selectedSlot.serverRegistryGapGu,
                    selectedSlot.serverRegistryEulGu,
                    selectedSlot.serverCourtDocuments
                ) {
                    CourtEvidenceRecorder.recordFullArchiveForSlot(selectedSlot)
                }
                val realPhotos = archivePreview.photos.count { it.imageUrl.isNotBlank() }
                StudioCard(
                    borderColor = CloudflareSky.copy(alpha = 0.65f),
                    modifier = Modifier.testTag("first_publish_guide_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (publishedCount == 0) {
                                    "첫 글 품질 확인 · 사건 증빙 · 1건 테스트 등록"
                                } else {
                                    "선택 사건 글 품질 · 증빙 · 즉시 테스트 (${selectedSlot.auctionCase.caseId})"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = CloudflareSky
                            )
                            Text(
                                text = "실제 렌더링된 블로그 글, 법원 사진(${realPhotos}/${archivePreview.photos.size} 실사), 등기부 갑/을구, 방문기록(${archivePreview.visitRecords.size}건)을 보고 바로 테스트 발행하세요.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                previewModalInitialTab = 0
                                previewModalSlotId = selectedSlot.postId
                            },
                            modifier = Modifier
                                .weight(1.05f)
                                .height(48.dp)
                                .testTag("btn_open_first_article_preview"),
                            colors = ButtonDefaults.buttonColors(containerColor = CloudflareSky, contentColor = NavyBackground)
                        ) {
                            Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("미리보기", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = {
                                previewModalInitialTab = 1
                                previewModalSlotId = selectedSlot.postId
                            },
                            modifier = Modifier
                                .weight(1.05f)
                                .height(48.dp)
                                .testTag("btn_open_court_basics_modal"),
                            border = BorderStroke(1.dp, MintSecondary.copy(alpha = 0.7f))
                        ) {
                            Icon(Icons.Filled.Tune, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("검증·수정", color = MintSecondary, maxLines = 1)
                        }
                        Button(
                            onClick = {
                                previewModalInitialTab = 3
                                previewModalSlotId = selectedSlot.postId
                            },
                            modifier = Modifier
                                .weight(0.95f)
                                .height(48.dp)
                                .testTag("btn_open_first_test_publish"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("등록", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }

        // 3. Clean Queue List (Only real server/user cases)
        item {
            SectionHeader(
                title = "실매물 예약 · 등록 목록 (${uiState.slots.size}건)",
                subtitle = "가짜/더미 데이터 0건 · 딱찰 서버 조회 및 실사건만 연결",
                icon = Icons.Filled.Gavel,
                easyGuide = "카드를 눌러 완성된 블로그 글을 미리보고, 한 번에 95점+ 교정·예약 등록할 수 있습니다."
            )
        }

        if (uiState.slots.isEmpty()) {
            item(key = "empty_real_cases_card") {
                StudioCard(
                    borderColor = CloudflareSky.copy(alpha = 0.45f),
                    modifier = Modifier.testTag("empty_real_cases_card")
                ) {
                    Text(
                        text = "불러온 실제 법원 사건이 아직 없습니다",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "상단 '법원 사건번호 즉시 조회'에 실제 사건번호(예: 2025타경6297)를 입력하거나 '1. DB 찾기'를 실행하면 경매 사진·조사서·등기부·방문기록을 불러와 칼분석 프리뷰를 자동으로 엽니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        } else {
            items(uiState.slots, key = { it.postId }) { slot ->
                SimpleQueueRowCard(
                    slot = slot,
                    config = uiState.config,
                    proFunnel = uiState.proFunnel,
                    isBusy = uiState.isBusy,
                    onPreviewQuality = {
                        previewModalInitialTab = 0
                        previewModalSlotId = slot.postId
                    },
                    onRegisterNow = { onAutoFixAndRegisterSlot(slot.postId) },
                    onInspect = { onInspectSlot(slot.postId) }
                )
            }
        }

        // 4. Collapsible Advanced Tools (Hidden by default to keep screen ultra-simple)
        item {
            OutlinedButton(
                onClick = { showAdvancedTools = !showAdvancedTools },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_toggle_advanced_dashboard")
            ) {
                Icon(
                    imageVector = if (showAdvancedTools) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (showAdvancedTools) "고급 분석 · 수동 추가 도구 접기" else "고급 분석 · AI 채점 · 수동 물건 추가 열기")
            }

            AnimatedVisibility(visible = showAdvancedTools) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.weight(1f).height(48.dp).testTag("btn_add_crawled_case")
                        ) {
                            Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("물건 직접 추가", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = onSyncPack,
                            enabled = !uiState.isBusy,
                            modifier = Modifier.weight(1f).height(48.dp).testTag("btn_sync_syndication")
                        ) {
                            Icon(Icons.Filled.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("딱찰 동기화", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = onOpenWarehouse,
                            modifier = Modifier.weight(1f).height(48.dp).testTag("btn_open_warehouse_shortcut")
                        ) {
                            Icon(Icons.Filled.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("창고열기", maxLines = 1)
                        }
                    }

                    val activeCaseId = selectedSlot?.auctionCase?.caseId
                    AiSeoGeoEvaluatorDashboardCard(
                        selectedCaseId = activeCaseId,
                        aiEvalState = uiState.aiEvalState,
                        isBusy = uiState.isBusy,
                        onRunAiEvalSelected = onRunAiEvalSelected,
                        onTestBusanProfitableLand = onTestBusanProfitableLand,
                        onTestLowScoreDraft = onTestLowScoreDraft,
                        onApplyAiAutoFixes = onApplyAiAutoFixes
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddCrawledCaseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { caseId, court, addr, type, appMan, minMan, baseDate, rights, pubDate ->
                onAddCustomProperty(caseId, court, addr, type, appMan, minMan, baseDate, rights, pubDate)
                showAddDialog = false
            }
        )
    }

    val htmlBundle = uiState.htmlBatchBundle
    if (htmlBundle != null) {
        ThreeHtmlGeneratorDialog(
            bundle = htmlBundle,
            onSwitchMode = onGenerateThreePureHtml,
            onDismiss = onDismissHtmlBatch
        )
    }

    val modalSlot = previewModalSlotId?.let { targetId ->
        uiState.slots.firstOrNull { it.postId == targetId }
            ?: uiState.warehouseArticles.firstOrNull { it.slot.postId == targetId }?.slot
    }
    if (modalSlot != null) {
        ArticleQualityPreviewModal(
            slot = modalSlot,
            uiState = uiState,
            initialTabIndex = previewModalInitialTab,
            onDismiss = { previewModalSlotId = null },
            onSelectChannel = onSelectChannel,
            onAttachCourtPhoto = onAttachCourtPhoto,
            onAddVisitRecord = onAddVisitRecord,
            onAutoFixSlot = onAutoFixSlotForPreview,
            onPublishSelectedChannelsAndVerify = onPublishSelectedChannelsAndVerify,
            onPublishNaverDirectAndVerify = onPublishNaverDirectAndVerify,
            onOpenSettings = onOpenSettings,
            onRefreshCourtEvidence = { caseId -> onLookupCaseAndAutoPreview(caseId, 2) },
            onVerifySlot = onVerifySlot,
            onSaveManualEdit = onSaveManualEdit,
            onOpenHtmlArticleViewer = onOpenHtmlArticleViewer
        )
    }
}

@Composable
private fun AppIconActionTile(
    icon: ImageVector,
    label: String,
    subLabel: String,
    accent: Color,
    container: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(92.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(enabled = enabled, onClick = onClick),
        color = NavySurfaceElevated,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, accent.copy(alpha = if (enabled) 0.5f else 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(container),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DdakchalPrecisionTelemetryCard(
    uiState: StudioUiState,
    selectedSlot: GeneratedPostSlot?,
    onRefreshTelemetry: () -> Unit,
    onCrawlAndSync: () -> Unit
) {
    val telemetry = uiState.telemetry
    val trigger = telemetry.triggerStatus
    val analysis: DdakchalBlogAnalysisData? = selectedSlot?.ddakchalAnalysisData

    StudioCard(
        borderColor = EmeraldPrimary.copy(alpha = 0.45f),
        modifier = Modifier.testTag("ddakchal_precision_telemetry_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("딱찰(DDAKCHAL) 크롤러 · 트리거 · 실데이터 정밀 관제", style = MaterialTheme.typography.titleSmall, color = MintSecondary)
                Text(
                    text = "엔드포인트: ${uiState.config.ddakchalApiBase} · 응답 ${telemetry.apiLatencyMs ?: "--"}ms · HTTP ${telemetry.lastHttpStatusCode ?: "--"}",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            OutlinedButton(
                onClick = onRefreshTelemetry,
                enabled = !telemetry.isPinging,
                modifier = Modifier.height(48.dp).testTag("btn_refresh_precision_telemetry")
            ) {
                Text("피드 갱신", maxLines = 1)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(NavySurfaceElevated)
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricPair("트리거 대기", "${trigger?.pendingCount ?: 0}건", TistoryAmber, Modifier.weight(1f))
            MetricPair("처리 완료", "${trigger?.processedCount ?: 0}건", MintSecondary, Modifier.weight(1f))
            MetricPair("스냅샷", "${trigger?.snapshotsCount ?: 0}건", CloudflareSky, Modifier.weight(1f))
            MetricPair("실패", "${trigger?.failedCount ?: 0}건", CrimsonAlert, Modifier.weight(1f))
        }

        if (!telemetry.lastFeedCursorSince.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "증분 커서(since): ${telemetry.lastFeedCursorSince}",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                color = CloudflareSky
            )
        }

        if (selectedSlot != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = NavyBackground,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, NavyBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "선택 물건 정밀 분석 (${selectedSlot.auctionCase.caseId})",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = "법원 사진", tint = CloudflareSky, modifier = Modifier.size(14.dp))
                            Text(
                                text = "R2 사진 ${selectedSlot.courtPhotoUrls.size}컷",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                color = CloudflareSky
                            )
                        }
                    }

                    if (analysis != null) {
                        if (analysis.hookHeadline.isNotBlank()) {
                            Text(
                                text = "후킹 헤드라인: ${analysis.hookHeadline}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MintSecondary
                            )
                        }
                        analysis.summary3Lines.take(3).forEachIndexed { idx, line ->
                            Text(
                                text = "• ${idx + 1}: $line",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        analysis.riskAndOpportunity?.let { ro ->
                            if (ro.riskSummary.isNotBlank() || ro.opportunitySummary.isNotBlank()) {
                                Text(
                                    text = "기회: ${ro.opportunitySummary} | 리스크: ${ro.riskSummary}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TistoryAmber
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "출처: ${selectedSlot.sourceLabel} · 실거래 중위 ${QaVerificationEngine.formatKrwShort(selectedSlot.auctionCase.recentRealTradeMedianKrw)} · 적정입찰 ${QaVerificationEngine.formatKrwShort(selectedSlot.auctionCase.balancedBidKrw)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        if (!telemetry.lastErrorDetail.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "최근 API 알림: ${telemetry.lastErrorDetail}",
                style = MaterialTheme.typography.labelSmall,
                color = CrimsonAlert
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SimpleQueueRowCard(
    slot: GeneratedPostSlot,
    config: com.example.model.ServerAutomationConfig,
    proFunnel: com.example.model.ProFunnelConfig,
    isBusy: Boolean,
    onPreviewQuality: () -> Unit,
    onRegisterNow: () -> Unit,
    onInspect: () -> Unit
) {
    val scoreResult = remember(slot.postId, slot.variants, slot.qaReport) {
        val reg = RegistryInfographicEngine.buildTemplate(slot.auctionCase)
        Article30PointEvaluator.evaluate(slot, config, proFunnel, reg)
    }
    val archiveSummary = remember(
        slot.postId,
        slot.courtPhotoUrls,
        slot.visitRecords,
        slot.serverRegistryGapGu,
        slot.serverRegistryEulGu,
        slot.serverCourtDocuments
    ) {
        CourtEvidenceRecorder.recordFullArchiveForSlot(slot)
    }
    val realPhotoCount = archiveSummary.photos.count { it.imageUrl.isNotBlank() }
    val scoreColor = if (scoreResult.eligibleForRegistration) MintSecondary else TistoryAmber

    StudioCard(
        borderColor = if (slot.status == PostStatus.PUBLISHED) EmeraldPrimary.copy(alpha = 0.55f) else NavyBorder,
        modifier = Modifier
            .clickable(onClick = onInspect)
            .testTag("slot_card_${slot.slotIndex}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${slot.auctionCase.caseId} · ${slot.auctionCase.propertyType.displayLabel}",
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
                    color = TextPrimary
                )
                Text(
                    text = slot.auctionCase.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            StatusChip(
                text = "${scoreResult.totalScore}점",
                textColor = scoreColor,
                backgroundColor = if (scoreResult.eligibleForRegistration) EmeraldContainer else NavySurfaceElevated
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "예약일 ${slot.publishDate} · 최저 ${QaVerificationEngine.formatKrwShort(slot.auctionCase.minBidPriceKrw)}",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )
            PostStatusBadge(status = slot.status)
        }

        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StatusChip(
                text = "사진 ${realPhotoCount}/${archiveSummary.photos.size}컷",
                textColor = if (realPhotoCount > 0) MintSecondary else CloudflareSky,
                backgroundColor = if (realPhotoCount > 0) EmeraldContainer else CloudflareContainer
            )
            StatusChip(
                text = "등기부 갑${archiveSummary.registryGapGu.size}·을${archiveSummary.registryEulGu.size}",
                textColor = TistoryAmber,
                backgroundColor = AmberContainer
            )
            StatusChip(
                text = "방문기록 ${archiveSummary.visitRecords.size}건",
                textColor = MintSecondary,
                backgroundColor = NavySurfaceElevated
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = onPreviewQuality,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_preview_quality_${slot.slotIndex}"),
                border = BorderStroke(1.dp, CloudflareSky.copy(alpha = 0.7f))
            ) {
                Icon(Icons.Filled.Visibility, contentDescription = null, tint = CloudflareSky, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("미리보기", color = CloudflareSky, maxLines = 1)
            }
            Button(
                onClick = onRegisterNow,
                enabled = !isBusy,
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
                    .testTag("btn_schedule_${slot.slotIndex}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (slot.status == PostStatus.PUBLISHED) CloudflareSky else EmeraldPrimary,
                    contentColor = NavyBackground
                )
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (slot.status == PostStatus.PUBLISHED) "재검증" else "95+ 등록",
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            OutlinedButton(
                onClick = onInspect,
                modifier = Modifier
                    .weight(0.75f)
                    .height(48.dp)
                    .testTag("btn_inspect_${slot.slotIndex}")
            ) {
                Text("상세", maxLines = 1)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCrawledCaseDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        caseId: String,
        courtName: String,
        address: String,
        propertyType: PropertyType,
        appraisalManWon: Long,
        minBidManWon: Long,
        baseRightDate: String,
        assumedRights: String,
        publishDate: String
    ) -> Unit
) {
    var caseId by remember { mutableStateOf("") }
    var courtName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(PropertyType.APT_OFFICETEL) }
    var appraisalMan by remember { mutableStateOf("") }
    var minBidMan by remember { mutableStateOf("") }
    var baseRightDate by remember { mutableStateOf("") }
    var assumedRights by remember { mutableStateOf("") }
    var publishDate by remember { mutableStateOf(PostAuctionFollowUpEngine.currentIsoDate()) }

    val appraisalVal = appraisalMan.toLongOrNull() ?: 0L
    val minBidVal = minBidMan.toLongOrNull() ?: 0L
    val canSubmit = caseId.isNotBlank() &&
        courtName.isNotBlank() &&
        address.isNotBlank() &&
        appraisalVal > 0L &&
        minBidVal > 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("실제 경매 물건 수동 입력", color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PropertyType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.displayLabel, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = caseId,
                    onValueChange = { caseId = it },
                    label = { Text("사건번호 (필수)") },
                    placeholder = { Text("법원 실제 사건번호 입력") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = courtName,
                    onValueChange = { courtName = it },
                    label = { Text("관할 법원명 (필수)") },
                    placeholder = { Text("예: 부산지방법원 동부지원") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("소재지/물건명 (필수)") },
                    placeholder = { Text("등기부 기준 실제 소재지 입력") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = appraisalMan,
                        onValueChange = { appraisalMan = it.filter(Char::isDigit) },
                        label = { Text("감정가(만원)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minBidMan,
                        onValueChange = { minBidMan = it.filter(Char::isDigit) },
                        label = { Text("최저가(만원)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = baseRightDate,
                        onValueChange = { baseRightDate = it },
                        label = { Text("말소기준권리일") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = publishDate,
                        onValueChange = { publishDate = it },
                        label = { Text("발행예약일") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = assumedRights,
                    onValueChange = { assumedRights = it },
                    label = { Text("인수권리 및 특이사항") },
                    placeholder = { Text("매각물건명세서 확인 내역 입력") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        caseId.trim(),
                        courtName.trim(),
                        address.trim(),
                        selectedType,
                        appraisalVal,
                        minBidVal,
                        baseRightDate.trim().ifBlank { "확인 필요" },
                        assumedRights.trim().ifBlank { "등기부·매각물건명세서 실데이터 대조 필요" },
                        publishDate.trim()
                    )
                },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
            ) {
                Text("생성 및 예약")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = TextSecondary)
            }
        }
    )
}
