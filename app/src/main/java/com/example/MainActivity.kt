package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.GoogleAccountAuthHelper
import com.example.ai.configureAppCheck
import com.example.ui.StudioViewModel
import com.example.ui.screens.ChannelQaScreen
import com.example.ui.screens.CloudflareServerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DdakchalTopStatusBar
import com.example.ui.screens.HtmlLinkHubSheetModal
import com.example.ui.screens.InteractiveHtmlLinkViewerModal
import com.example.ui.screens.TemplatesAndRulesScreen
import com.example.ui.screens.WarehouseAndVisitorDashboardScreen
import com.example.ui.theme.DdakchalStudioTheme
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import android.graphics.Color as AndroidColor

private enum class StudioTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("dashboard", "발행큐", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    WAREHOUSE("warehouse", "창고·로그", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
    QA_INSPECT("qa_inspect", "3채널QA", Icons.AutoMirrored.Filled.FactCheck, Icons.AutoMirrored.Outlined.FactCheck),
    TEMPLATES("templates", "템플릿", Icons.Filled.Tune, Icons.Outlined.Tune),
    SERVER("server", "설정·보안", Icons.Filled.Security, Icons.Outlined.Security)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure Firebase App Check with launch Intent before any Compose UI or ViewModels
        configureAppCheck(this, intent)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(NavySurface.toArgb())
        )
        setContent {
            DdakchalStudioTheme {
                DdakchalStudioApp()
            }
        }
    }
}

@Composable
fun DdakchalStudioApp() {
    val context = LocalContext.current
    val viewModel: StudioViewModel = viewModel(
        factory = StudioViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var activeTab by rememberSaveable { mutableStateOf(StudioTab.DASHBOARD) }
    val snackbarHostState = remember { SnackbarHostState() }

    if (activeTab != StudioTab.DASHBOARD) {
        BackHandler { activeTab = StudioTab.DASHBOARD }
    }

    LaunchedEffect(uiState.statusMessage, uiState.errorMessage) {
        val msg = uiState.errorMessage ?: uiState.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBanners()
        }
    }

    LaunchedEffect(Unit) {
        runCatching {
            GoogleAccountAuthHelper.attemptSilentGoogleSignIn(context)
        }
        viewModel.syncGoogleAuthState()
    }

    LaunchedEffect(
        uiState.telemetry.autoMonitorEnabled,
        uiState.config.ddakchalApiBase,
        uiState.config.crawlerSyncToken
    ) {
        if (!uiState.telemetry.autoMonitorEnabled || uiState.config.ddakchalApiBase.isBlank()) {
            return@LaunchedEffect
        }
        viewModel.refreshDdakchalServerTelemetry(alsoSyncFeed = true)
        while (isActive && uiState.telemetry.autoMonitorEnabled) {
            delay(30_000L)
            viewModel.refreshDdakchalServerTelemetry(alsoSyncFeed = false)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 700.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = NavyBackground,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = NavySurface,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        StudioTab.entries.forEach { tab ->
                            val selected = activeTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { activeTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MintSecondary,
                                    selectedTextColor = MintSecondary,
                                    indicatorColor = EmeraldContainer,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = NavySurface,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        StudioTab.entries.forEach { tab ->
                            val selected = activeTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { activeTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MintSecondary,
                                    selectedTextColor = MintSecondary,
                                    indicatorColor = EmeraldContainer,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("rail_${tab.route}")
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f).fillMaxSize()) {
                    DdakchalTopStatusBar(
                        telemetry = uiState.telemetry,
                        isMobileSimpleMode = uiState.isMobileSimpleMode,
                        onToggleMobileMode = viewModel::setMobileSimpleMode,
                        onRefreshPing = { viewModel.refreshDdakchalServerTelemetry(alsoSyncFeed = true) },
                        onToggleAutoMonitor = viewModel::toggleAutoMonitorPolling,
                        securitySummary = uiState.securityDashboard,
                        onOpenSecurityDashboard = { activeTab = StudioTab.SERVER },
                        onOpenSettings = { activeTab = StudioTab.SERVER }
                    )

                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        when (activeTab) {
                            StudioTab.DASHBOARD -> DashboardScreen(
                                uiState = uiState,
                                onSelectWeeklyDailyCount = viewModel::setWeeklyDailyCount,
                                onGenerateWeeklyBatch = viewModel::generateWeeklyBatchAndSchedule,
                                onCreateHighScoreAndAutoRegister = { viewModel.createHighScoreAndAutoRegister() },
                                onAutoFixAndRegisterSlot = viewModel::autoFixAndRegisterSlot,
                                onSaveThreeChannelDestinations = viewModel::saveThreeChannelDestinations,
                                onPublishNaverDirectAndVerify = { postId, url ->
                                    viewModel.publishNaverDirectAndVerify(postId, url)
                                },
                                onPublishSelectedChannelsAndVerify = { postId, url ->
                                    viewModel.publishSelectedChannelsAndVerify(postId, url)
                                },
                                onOpenWarehouse = { activeTab = StudioTab.WAREHOUSE },
                                onOpenTemplates = { activeTab = StudioTab.TEMPLATES },
                                onOpenSecurityDashboard = { activeTab = StudioTab.SERVER },
                                onOpenSettings = { activeTab = StudioTab.SERVER },
                                onToggleMobileMode = viewModel::setMobileSimpleMode,
                                onCrawlAndSync = viewModel::triggerCrawlAndSyncPipeline,
                                onRefreshServerTelemetry = { viewModel.refreshDdakchalServerTelemetry(alsoSyncFeed = true) },
                                onRefreshQueueCharts = { viewModel.syncRealBlogQueueWithRetrofit(announceBanner = true) },
                                onEvaluateRisk = { viewModel.evaluateLowQualityRiskAndPauseIfNeeded(announceBanner = true) },
                                onResumeAutoPublish = viewModel::resumeAutoPublishAfterRiskMitigation,
                                onToggleAutoPauseOnHighRisk = viewModel::toggleAutoPauseOnHighRisk,
                                onSearchCaseWithAi = viewModel::searchCaseWithAiAndOptimize,
                                onDiagnoseNaverKeyword = viewModel::diagnoseNaverKeywordExposure,
                                onTestRegisterAiSearchedCase = viewModel::testRegisterAiSearchedCase,
                                onGenerateThreePureHtml = viewModel::generateThreePureHtmlPosts,
                                onDismissHtmlBatch = viewModel::dismissHtmlBatchBundle,
                                onSelectTopicSort = viewModel::evaluateAndRankTopics,
                                onRankTopics = { viewModel.evaluateAndRankTopics() },
                                onApplyTopTopics = { viewModel.applyTopSelectedTopicsToQueue(3) },
                                onSelectAndRegisterTopic = viewModel::selectAndAutoRegisterTopic,
                                onRunPerfectDdakchalDiag = viewModel::runPerfectDdakchalServerDiagnostic,
                                onRefreshNaverPublishStatus = viewModel::refreshNaverPublishStatuses,
                                onSelectNaverWriteMode = viewModel::updateNaverWriteMode,
                                onToggleAdminRegion = viewModel::toggleTodakAdminRegion,
                                onSelectAdminCategory = viewModel::setTodakAdminCategory,
                                onAdminSearchQueryChange = viewModel::setTodakAdminSearchQuery,
                                onSelectAdminCrawlLimit = viewModel::setTodakAdminCrawlLimit,
                                onSaveAdminInlineAuth = viewModel::applyInlineServerAuthAndSave,
                                onDiscoverServerDb = viewModel::discoverTodakServerDb,
                                onRunTodakCrawlOnly = viewModel::runTodakCrawlEngineOnly,
                                onRunTodakFullPipeline = viewModel::runTodakFullAdminPipeline,
                                onGenerateFromDbItem = viewModel::generatePostFromDiscoveredDbItem,
                                onCaseLookupInputChange = viewModel::setTodakCaseLookupInput,
                                onLookupCaseAndAutoPreview = viewModel::lookupCaseNumberFromTodakAndAutoPreview,
                                onConsumeAutoPreview = viewModel::consumePendingAutoPreview,
                                onOpenHtmlLinkHub = viewModel::openHtmlLinkHub,
                                onOpenHtmlHubViewer = viewModel::openHtmlHubViewer,
                                onOpenHtmlArticleViewer = viewModel::openHtmlArticleViewer,
                                onVerifySlot = viewModel::verifySlotBeforeRegistration,
                                onAutoFixSlotForPreview = viewModel::autoFixSlotForPreview,
                                onSaveManualEdit = viewModel::updateSlotVariantContent,
                                onOpenQaTab = { activeTab = StudioTab.QA_INSPECT },
                                onRunAiEvalSelected = { viewModel.evaluateSlotWithAi30Criteria() },
                                onTestBusanProfitableLand = viewModel::createAndEvaluateBusanProfitableLandTest,
                                onTestLowScoreDraft = viewModel::createLowScoreDraftForAutoFixTest,
                                onApplyAiAutoFixes = viewModel::applyAiAutoFixesToArticle,
                                onSyncPack = viewModel::syncSyndicationPackOnly,
                                onInspectSlot = { postId ->
                                    viewModel.selectSlot(postId)
                                    activeTab = StudioTab.QA_INSPECT
                                },
                                onResolveHandoff = viewModel::resolveSessionHandoff,
                                onSelectChannel = viewModel::selectChannel,
                                onAttachCourtPhoto = viewModel::attachCourtPhotoToSlot,
                                onAddVisitRecord = viewModel::addFieldVisitRecord,
                                onAddCustomProperty = viewModel::addCustomCrawledPropertyAndSchedule
                            )
                            StudioTab.WAREHOUSE -> WarehouseAndVisitorDashboardScreen(
                                uiState = uiState,
                                onCreateHighScoreAndAutoRegister = viewModel::createHighScoreAndAutoRegister,
                                onAutoFixAndRegisterSlot = viewModel::autoFixAndRegisterSlot,
                                onImportQueueToWarehouse = viewModel::importQueueSlotsToWarehouse,
                                onRun30PointEval = viewModel::run30PointEvaluationOnWarehouseItem,
                                onDeleteWarehouseArticle = viewModel::deleteWarehouseArticle,
                                onInspectArticleInQa = { postId ->
                                    viewModel.selectSlot(postId)
                                    activeTab = StudioTab.QA_INSPECT
                                },
                                onRefreshVisitorTelemetry = viewModel::refreshVisitorTrafficTelemetry,
                                onRefreshQueueCharts = { viewModel.syncRealBlogQueueWithRetrofit(announceBanner = true) },
                                onOpenSettings = { activeTab = StudioTab.SERVER }
                            )
                            StudioTab.QA_INSPECT -> ChannelQaScreen(
                                uiState = uiState,
                                onSelectSlot = viewModel::selectSlot,
                                onSelectChannel = viewModel::selectChannel,
                                onSelectRegistryTemplateType = viewModel::selectRegistryTemplateType,
                                onRewriteSlot = viewModel::rewriteSlotToPassQa,
                                onScheduleSlot = viewModel::scheduleSlotWithStaggeredJitter,
                                onToggleAutoQualityDefense = viewModel::toggleAutoQualityDefense,
                                onRunQualityDefense = viewModel::applyQualityDefenseToSlot,
                                onFetchNaverMarket = { postId -> viewModel.fetchNearbyMarketAndRebid(postId) },
                                onRunLegalVerify = { postId -> viewModel.runLegalDoctrineVerification(postId) },
                                onSaveThreeChannelDestinations = viewModel::saveThreeChannelDestinations,
                                onPublishNaverDirectAndVerify = { postId, url ->
                                    viewModel.publishNaverDirectAndVerify(postId, url)
                                },
                                onPublishSelectedChannelsAndVerify = { postId, url ->
                                    viewModel.publishSelectedChannelsAndVerify(postId, url)
                                },
                                onReinvestigateFollowUp = { postId, outcome, winMan, nextDate, note ->
                                    viewModel.reinvestigatePostAuctionAndCreateFollowUp(
                                        postId = postId,
                                        outcomeType = outcome,
                                        actualWinningManWon = winMan,
                                        nextAuctionDate = nextDate,
                                        courtNote = note
                                    )
                                },
                                onLegalStudyFilterChange = viewModel::setLegalStudyFilter,
                                onAttachCourtPhoto = viewModel::attachCourtPhotoToSlot,
                                onAddVisitRecord = viewModel::addFieldVisitRecord,
                                onRefreshCourtEvidence = { caseId ->
                                    viewModel.lookupCaseNumberFromTodakAndAutoPreview(caseId, 2)
                                }
                            )
                            StudioTab.TEMPLATES -> TemplatesAndRulesScreen(
                                uiState = uiState,
                                onSaveYaml = viewModel::saveYamlTemplate,
                                onRotateBoilerplate = viewModel::rotateBoilerplatePool,
                                onCreatePostTemplate = viewModel::createPostTemplate,
                                onSavePostTemplate = viewModel::savePostTemplate,
                                onActivatePostTemplate = viewModel::setActivePostTemplate,
                                onDeletePostTemplate = viewModel::deletePostTemplate,
                                onResetPostTemplates = viewModel::resetPostTemplates,
                                onToggleSeoChecklistItem = viewModel::toggleSeoChecklistItem,
                                onCreateSeoChecklistItem = viewModel::createSeoChecklistItem,
                                onSaveSeoChecklistItem = viewModel::saveSeoChecklistItem,
                                onDeleteSeoChecklistItem = viewModel::deleteSeoChecklistItem,
                                onResetSeoChecklist = viewModel::resetSeoChecklist,
                                onSaveProFunnel = viewModel::saveProFunnelConfig,
                                onResetProFunnel = viewModel::resetProFunnelConfig,
                                onCrawlAndSync = viewModel::triggerCrawlAndSyncPipeline
                            )
                            StudioTab.SERVER -> CloudflareServerScreen(
                                uiState = uiState,
                                onSaveConfig = viewModel::updateConfig,
                                onVerifyCloudflare = viewModel::verifyCloudflareSetup,
                                onMigrateD1 = viewModel::provisionD1SchemaOnCloudflare,
                                onRegisterCron = viewModel::registerCloudflareCronTrigger,
                                onQueryD1Queue = viewModel::queryD1QueueFromCloudflare,
                                onTriggerHandoffTest = viewModel::triggerCaptchaHandoffSimulation,
                                onResolveHandoff = viewModel::resolveSessionHandoff,
                                onRefreshSecurityAudit = viewModel::refreshSecurityAudit,
                                onPurgePlaintextPw = viewModel::purgeLegacyPlaintextPasswords,
                                onProbeNaverAndTistory = viewModel::probeNaverAndTistoryConnections,
                                onSaveQuickSettings = viewModel::saveQuickSettings,
                                onRunHeartbeat = viewModel::runAlwaysLoggedInHeartbeat,
                                onSyncGoogleAuthState = viewModel::syncGoogleAuthState,
                                onSignOutGoogle = { viewModel.signOutGoogle(context) },
                                onUpdateGrokConfig = { enabled, model ->
                                    viewModel.updateConfig(
                                        uiState.config.copy(
                                            grokFallbackEnabled = enabled,
                                            grokModelName = model
                                        )
                                    )
                                },
                                onRunPerfectDdakchalDiag = viewModel::runPerfectDdakchalServerDiagnostic
                            )
                        }

                        if (uiState.isBusy) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = EmeraldPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showHtmlLinkHub) {
        HtmlLinkHubSheetModal(
            uiState = uiState,
            onSelectFilter = viewModel::setHtmlLinkFilter,
            onOpenHubViewer = viewModel::openHtmlHubViewer,
            onOpenArticleViewer = viewModel::openHtmlArticleViewer,
            onOpenVerifyAndEdit = { postId ->
                viewModel.closeHtmlLinkHub()
                activeTab = StudioTab.DASHBOARD
                viewModel.requestOpenPreviewModal(postId, tabIndex = 1)
            },
            onAutoFixAndRegister = viewModel::autoFixAndRegisterSlot,
            onDismiss = viewModel::closeHtmlLinkHub
        )
    }

    val htmlViewerTarget = uiState.activeHtmlViewerTarget
    if (htmlViewerTarget != null) {
        InteractiveHtmlLinkViewerModal(
            initialTarget = htmlViewerTarget,
            uiState = uiState,
            onOpenVerifyAndEdit = { postId ->
                viewModel.closeHtmlViewer()
                viewModel.closeHtmlLinkHub()
                activeTab = StudioTab.DASHBOARD
                viewModel.requestOpenPreviewModal(postId, tabIndex = 1)
            },
            onClose = viewModel::closeHtmlViewer
        )
    }
}
