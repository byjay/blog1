package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ServerConfigStore
import com.example.engine.QaVerificationEngine
import com.example.model.AdminPipelineStageStatus
import com.example.model.LowQualityRiskLevel
import com.example.model.PropertyType
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
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TistoryAmber

private val ADMIN_REGION_OPTIONS = listOf("서울", "경기", "인천", "부산", "대구", "대전", "광주", "제주")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodakAdminConsoleCard(
    uiState: StudioUiState,
    onToggleRegion: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectCrawlLimit: (Int) -> Unit,
    onSaveInlineAuth: (
        apiBase: String,
        syncToken: String,
        cfAccountId: String,
        cfD1DatabaseId: String,
        cfApiToken: String
    ) -> Unit,
    onDiscoverServerDb: () -> Unit,
    onRunCrawlEngineOnly: () -> Unit,
    onRunFullAdminPipeline: () -> Unit,
    onSyncQueueAndD1: () -> Unit,
    onGenerateFromDbItem: (String) -> Unit,
    onInspectSlot: (String) -> Unit,
    onNavigateWarehouse: () -> Unit,
    onNavigateQa: () -> Unit,
    onNavigateTemplates: () -> Unit,
    onNavigateServer: () -> Unit,
    onCaseLookupInputChange: (String) -> Unit = {},
    onLookupCaseAndAutoPreview: (caseInput: String, preferredTab: Int) -> Unit = { _, _ -> },
    onOpenHtmlLinkHub: () -> Unit = {},
    onOpenHtmlHubViewer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val admin = uiState.todakAdmin
    val config = uiState.config
    val missingToken = config.crawlerSyncToken.isBlank()

    var showAuthPanel by rememberSaveable { mutableStateOf(missingToken) }
    var showLogsAndTables by rememberSaveable { mutableStateOf(true) }

    var draftApiBase by remember { mutableStateOf(config.ddakchalApiBase) }
    var draftSyncToken by remember { mutableStateOf(config.crawlerSyncToken) }
    var draftCfAccountId by remember { mutableStateOf(config.cfAccountId) }
    var draftCfD1Id by remember { mutableStateOf(config.cfD1DatabaseId) }
    var draftCfToken by remember { mutableStateOf(config.cfApiToken) }

    LaunchedEffect(
        config.ddakchalApiBase,
        config.crawlerSyncToken,
        config.cfAccountId,
        config.cfD1DatabaseId,
        config.cfApiToken
    ) {
        draftApiBase = config.ddakchalApiBase
        draftSyncToken = config.crawlerSyncToken
        draftCfAccountId = config.cfAccountId
        draftCfD1Id = config.cfD1DatabaseId
        draftCfToken = config.cfApiToken
    }

    val ownerEmail = uiState.googleAccount.email.ifBlank { "cstarfirst2@gmail.com" }
    val riskReport = uiState.lowQualityRiskReport
    val riskText = when {
        uiState.autoPublishPausedByRisk -> "중지(위험)"
        riskReport?.overallRisk == LowQualityRiskLevel.HIGH_RISK -> "위험 높음"
        riskReport?.overallRisk == LowQualityRiskLevel.CAUTION -> "주의"
        else -> "안전"
    }
    val riskColor = when {
        uiState.autoPublishPausedByRisk || riskReport?.overallRisk == LowQualityRiskLevel.HIGH_RISK -> CrimsonAlert
        riskReport?.overallRisk == LowQualityRiskLevel.CAUTION -> TistoryAmber
        else -> MintSecondary
    }
    val trigger = uiState.telemetry.triggerStatus

    StudioCard(
        borderColor = CloudflareSky.copy(alpha = 0.72f),
        modifier = modifier.testTag("todak_top_admin_console_card")
    ) {
        // 1. Top Owner Admin Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.AdminPanelSettings,
                    contentDescription = "최상단 관리자 콘솔",
                    tint = CloudflareSky,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "딱찰 컨트롤타워 · 최상단 지휘 콘솔",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "총괄 관제: $ownerEmail · ${config.ddakchalApiBase}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CloudflareSky,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            StatusChip(
                text = if (uiState.telemetry.ddakchalConnected) {
                    "실서버 ${uiState.telemetry.apiLatencyMs ?: "--"}ms"
                } else {
                    if (missingToken) "토큰 필요" else "연동 대기"
                },
                textColor = if (uiState.telemetry.ddakchalConnected) MintSecondary else TistoryAmber,
                backgroundColor = if (uiState.telemetry.ddakchalConnected) EmeraldContainer else AmberContainer,
                dotColor = if (uiState.telemetry.ddakchalConnected) MintSecondary else TistoryAmber
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1.5 Graph-Based Real-Time Ddakchal Monitoring Dashboard (Collect/DB + Blog Status + Visitors)
        DdakchalTopGraphDashboardCard(
            uiState = uiState,
            onRefreshAll = onSyncQueueAndD1
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Real-time Master KPI Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(NavySurfaceElevated)
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricPair(
                label = "DB/크롤 매물",
                value = "${admin.discoveredItems.size}건",
                valueColor = CloudflareSky,
                modifier = Modifier.weight(1f)
            )
            MetricPair(
                label = "트리거 대기/완료",
                value = if (trigger != null) "${trigger.pendingCount}/${trigger.processedCount}" else "--",
                valueColor = MintSecondary,
                modifier = Modifier.weight(1f)
            )
            MetricPair(
                label = "활성 슬롯/창고",
                value = "${uiState.slots.size}/${uiState.warehouseArticles.size}",
                valueColor = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            MetricPair(
                label = "저품질 위험",
                value = riskText,
                valueColor = riskColor,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2.5 Instant Court Case Number Lookup -> Ddakchal Server Fetch -> Auto Preview
        Surface(
            color = NavyBackground,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.65f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_admin_case_lookup")
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Gavel,
                        contentDescription = "법원 사건 조회",
                        tint = MintSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "법원 사건번호 즉시 조회 → 사진·조사서·등기 자동 프리뷰",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MintSecondary
                    )
                }
                EasyMenuGuideStrip(
                    icon = Icons.Filled.Search,
                    easyGuide = "사건번호를 입력하면 법원 사진·현황조사서·매각물건명세서·등기부를 딱찰 서버에서 자동 수집해 칼분석으로 보여줍니다.",
                    accentColor = MintSecondary
                )
                OutlinedTextField(
                    value = admin.caseLookupInput,
                    onValueChange = onCaseLookupInputChange,
                    label = { Text("법원 사건번호 입력 (예: 2025타경6297)") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "사건번호 입력") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_admin_case_lookup")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onLookupCaseAndAutoPreview(admin.caseLookupInput, 0) },
                        enabled = !uiState.isBusy && !admin.isRunning && admin.caseLookupInput.isNotBlank(),
                        modifier = Modifier
                            .weight(1.25f)
                            .height(48.dp)
                            .testTag("btn_admin_case_lookup_preview"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = NavyBackground
                        )
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("조회 · 자동 프리뷰", fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = { onLookupCaseAndAutoPreview(admin.caseLookupInput, 2) },
                        enabled = !uiState.isBusy && !admin.isRunning && admin.caseLookupInput.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_admin_case_lookup_evidence"),
                        border = BorderStroke(1.dp, CloudflareSky.copy(alpha = 0.7f))
                    ) {
                        Text("사진·등기·방문", color = CloudflareSky, maxLines = 1)
                    }
                }
                if (admin.caseLookupStatus.isNotBlank()) {
                    Text(
                        text = admin.caseLookupStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (admin.caseLookupStatus.contains("실패") || admin.caseLookupStatus.contains("오류") || admin.caseLookupStatus.contains("찾을 수")) {
                            CrimsonAlert
                        } else {
                            MintSecondary
                        },
                        modifier = Modifier.testTag("text_admin_case_lookup_status")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Quick Admin Navigation Bar + Inline Auth Toggle
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { showAuthPanel = !showAuthPanel },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("btn_admin_toggle_auth"),
                border = BorderStroke(1.dp, if (missingToken) TistoryAmber else NavyBorder)
            ) {
                Icon(
                    imageVector = Icons.Filled.Key,
                    contentDescription = "서버 키 설정",
                    tint = if (missingToken) TistoryAmber else CloudflareSky,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (showAuthPanel) "서버키 접기" else "서버·D1 키")
            }
            Button(
                onClick = onOpenHtmlLinkHub,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("btn_admin_nav_html_hub"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CloudflareContainer,
                    contentColor = CloudflareSky
                )
            ) {
                Icon(Icons.Filled.Link, contentDescription = "HTML 링크함 열기", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                val totalLinkedCount = remember(uiState.slots, uiState.warehouseArticles) {
                    (uiState.slots.map { it.postId } + uiState.warehouseArticles.map { it.slot.postId }).distinct().size
                }
                Text("HTML 링크함($totalLinkedCount)", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onOpenHtmlHubViewer,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("btn_admin_open_html_hub"),
                border = BorderStroke(1.dp, MintSecondary.copy(alpha = 0.65f))
            ) {
                Icon(Icons.Filled.Public, contentDescription = "허브 HTML 바로 열기", tint = MintSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("허브 HTML", color = MintSecondary)
            }
            TextButton(
                onClick = onNavigateWarehouse,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("btn_admin_nav_warehouse")
            ) {
                Icon(Icons.Filled.Inventory2, contentDescription = "글 창고 이동", tint = TistoryAmber, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("창고(${uiState.warehouseArticles.size})", color = TistoryAmber)
            }
            TextButton(
                onClick = onNavigateQa,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("btn_admin_nav_qa")
            ) {
                Icon(Icons.AutoMirrored.Filled.FactCheck, contentDescription = "3채널 QA 검수 이동", tint = MintSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("3채널QA", color = MintSecondary)
            }
            TextButton(
                onClick = onNavigateTemplates,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("btn_admin_nav_templates")
            ) {
                Icon(Icons.Filled.Tune, contentDescription = "템플릿 설정 이동", tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("템플릿", color = TextSecondary)
            }
            TextButton(
                onClick = onNavigateServer,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("btn_admin_nav_server")
            ) {
                Icon(Icons.Filled.Security, contentDescription = "보안 및 CF 설정 이동", tint = CloudflareSky, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("보안·CF", color = CloudflareSky)
            }
        }

        // 4. Collapsible Inline Server / D1 Credential Panel
        AnimatedVisibility(visible = showAuthPanel) {
            Surface(
                color = NavyBackground,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, NavyBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "딱찰 서버 · Cloudflare D1 인증 키 (인라인 저장)",
                        style = MaterialTheme.typography.labelLarge,
                        color = CloudflareSky
                    )
                    OutlinedTextField(
                        value = draftApiBase,
                        onValueChange = { draftApiBase = it },
                        label = { Text("서버 주소 (DDAKCHAL_API_BASE)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_admin_api_base")
                    )
                    OutlinedTextField(
                        value = draftSyncToken,
                        onValueChange = { draftSyncToken = it },
                        label = { Text("CRAWLER_SYNC_TOKEN (${ServerConfigStore.maskSecret(draftSyncToken)})") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_admin_sync_token")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = draftCfAccountId,
                            onValueChange = { draftCfAccountId = it },
                            label = { Text("CF Account ID") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = draftCfD1Id,
                            onValueChange = { draftCfD1Id = it },
                            label = { Text("CF D1 DB ID") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = draftCfToken,
                        onValueChange = { draftCfToken = it },
                        label = { Text("CF_API_TOKEN (D1 탐색용 선택)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            onSaveInlineAuth(
                                draftApiBase,
                                draftSyncToken,
                                draftCfAccountId,
                                draftCfD1Id,
                                draftCfToken
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_admin_save_auth"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CloudflareSky,
                            contentColor = NavyBackground
                        )
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("관리자 서버·D1 키 저장", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Crawl Region & DB Query Filters
        Text(
            text = "크롤링 대상 지역 (다중 선택)",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ADMIN_REGION_OPTIONS.forEach { region ->
                val selected = region in admin.selectedRegions
                FilterChip(
                    selected = selected,
                    onClick = { onToggleRegion(region) },
                    label = { Text(region) },
                    modifier = Modifier.testTag("chip_admin_region_$region")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "물건 카테고리 · 검색어 · 수집 건수",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(
                selected = admin.selectedCategoryFilter.isBlank(),
                onClick = { onSelectCategory("") },
                label = { Text("전체") },
                modifier = Modifier.testTag("chip_admin_cat_all")
            )
            PropertyType.entries.forEach { type ->
                FilterChip(
                    selected = admin.selectedCategoryFilter == type.code,
                    onClick = { onSelectCategory(type.code) },
                    label = { Text(type.displayLabel) },
                    modifier = Modifier.testTag("chip_admin_cat_${type.code}")
                )
            }
            listOf(3, 5, 10).forEach { limitVal ->
                FilterChip(
                    selected = admin.crawlLimit == limitVal,
                    onClick = { onSelectCrawlLimit(limitVal) },
                    label = { Text("${limitVal}건") },
                    modifier = Modifier.testTag("chip_admin_limit_$limitVal")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = admin.searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("사건번호 또는 주소 키워드 (예: 2025타경6297, 강남)") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "DB 검색") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_admin_db_query")
        )

        Spacer(modifier = Modifier.height(10.dp))

        EasyMenuGuideStrip(
            icon = Icons.Filled.Storage,
            easyGuide = "지역·물건 종류를 고른 뒤 아래 초록 버튼을 누르면 딱찰 DB 탐색부터 크롤링·95점+ 글 생성까지 한 번에 실행됩니다.",
            accentColor = CloudflareSky
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 6. Primary Master Action + 3 Step Controls
        Button(
            onClick = onRunFullAdminPipeline,
            enabled = !uiState.isBusy && !admin.isRunning,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_admin_full_pipeline"),
            colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldPrimary,
                contentColor = NavyBackground
            )
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "DB 탐색 → 딱찰 엔진 크롤링 → 95+ 글 자동생성",
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onDiscoverServerDb,
                enabled = !uiState.isBusy && !admin.isRunning,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_admin_discover_db")
            ) {
                Icon(Icons.Filled.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("1. DB 찾기", maxLines = 1)
            }
            OutlinedButton(
                onClick = onRunCrawlEngineOnly,
                enabled = !uiState.isBusy && !admin.isRunning,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_admin_run_crawl")
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("2. 크롤링", maxLines = 1)
            }
            OutlinedButton(
                onClick = onSyncQueueAndD1,
                enabled = !uiState.isBusy && !admin.isRunning,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_admin_sync_queue")
            ) {
                Icon(Icons.Filled.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("큐 동기화", maxLines = 1)
            }
        }

        if (admin.isRunning) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = CloudflareSky,
                trackColor = NavySurfaceElevated
            )
            if (admin.currentActionLabel.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = admin.currentActionLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = CloudflareSky
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            color = NavyBackground,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, NavyBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = admin.summaryMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { showLogsAndTables = !showLogsAndTables },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            imageVector = if (showLogsAndTables) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = "상세 토글",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(if (showLogsAndTables) "접기" else "로그", color = TextSecondary)
                    }
                }

                AnimatedVisibility(visible = showLogsAndTables) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Pipeline Step Execution Logs
                        if (admin.stepLogs.isNotEmpty()) {
                            Text(
                                text = "파이프라인 실행 로그 (${admin.stepLogs.size}단계)",
                                style = MaterialTheme.typography.labelMedium,
                                color = CloudflareSky
                            )
                            admin.stepLogs.forEach { step ->
                                val (fg, bg) = when (step.status) {
                                    AdminPipelineStageStatus.SUCCESS -> MintSecondary to EmeraldContainer
                                    AdminPipelineStageStatus.WARN -> TistoryAmber to AmberContainer
                                    AdminPipelineStageStatus.ERROR -> CrimsonAlert to CrimsonContainer
                                    AdminPipelineStageStatus.RUNNING -> CloudflareSky to CloudflareContainer
                                    AdminPipelineStageStatus.IDLE -> TextSecondary to NavySurfaceElevated
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(MaterialTheme.shapes.small)
                                        .background(NavySurfaceElevated)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${step.stepIndex}. ${step.title}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${step.endpoint} · ${step.detail}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    StatusChip(
                                        text = if (step.httpCode > 0) "HTTP ${step.httpCode} · ${step.latencyMs}ms" else step.status.label,
                                        textColor = fg,
                                        backgroundColor = bg
                                    )
                                }
                            }
                        }

                        // Discovered DB / D1 Tables
                        if (admin.discoveredTables.isNotEmpty()) {
                            Text(
                                text = "발견된 서버 DB & D1 테이블 (${admin.discoveredTables.size}개)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MintSecondary
                            )
                            admin.discoveredTables.forEach { tbl ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(MaterialTheme.shapes.small)
                                        .background(NavySurfaceElevated)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${tbl.sourceName} (${tbl.tableOrEndpoint})",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = tbl.statusDetail,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    Text(
                                        text = "${tbl.rowCount}건",
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                                        color = if (tbl.rowCount > 0) MintSecondary else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Discovered Server DB Items (1-tap generate 95+ blog article from any discovered row)
        if (admin.discoveredItems.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "서버 DB 실매물 (${admin.discoveredItems.size}건 · 탭하여 95+ 글 생성)",
                style = MaterialTheme.typography.labelLarge,
                color = MintSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                admin.discoveredItems.take(6).forEach { item ->
                    Surface(
                        color = NavySurfaceElevated,
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_db_item_${item.caseId}")
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.caseId} · ${item.propertyType.displayLabel}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary
                                )
                                StatusChip(
                                    text = item.sourceOrigin,
                                    textColor = CloudflareSky,
                                    backgroundColor = CloudflareContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${item.courtName} · ${item.address}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "감정 ${QaVerificationEngine.formatKrwShort(item.appraisalKrw)} · 최저 ${QaVerificationEngine.formatKrwShort(item.minBidKrw)} · 사진 ${item.photoCount}장 · 방문 ${item.visitCount}건",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                color = MintSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onGenerateFromDbItem(item.recordId) },
                                    enabled = !uiState.isBusy && !admin.isRunning,
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .height(48.dp)
                                        .testTag("btn_admin_gen_item_${item.caseId}"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldPrimary,
                                        contentColor = NavyBackground
                                    )
                                ) {
                                    Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("글 생성 · 미리보기", fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                                OutlinedButton(
                                    onClick = { onInspectSlot(item.slot.postId) },
                                    modifier = Modifier
                                        .weight(0.7f)
                                        .height(48.dp)
                                ) {
                                    Text("QA 보기", maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
