package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.engine.QaVerificationEngine
import com.example.model.EvaluationPillar
import com.example.model.PropertyType
import com.example.model.WarehouseArticle
import com.example.model.WarehouseArticleStatus
import com.example.ui.StudioUiState
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
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TistoryAmber

@Composable
fun WarehouseAndVisitorDashboardScreen(
    uiState: StudioUiState,
    onCreateHighScoreAndAutoRegister: (PropertyType?) -> Unit,
    onAutoFixAndRegisterSlot: (String) -> Unit,
    onImportQueueToWarehouse: () -> Unit,
    onRun30PointEval: (String) -> Unit,
    onDeleteWarehouseArticle: (String) -> Unit,
    onInspectArticleInQa: (String) -> Unit,
    onRefreshVisitorTelemetry: () -> Unit,
    onRefreshQueueCharts: () -> Unit = onRefreshVisitorTelemetry,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activeSubTab by rememberSaveable { mutableStateOf("WAREHOUSE") } // WAREHOUSE | VISITOR_LOGS
    var statusFilter by rememberSaveable { mutableStateOf<WarehouseArticleStatus?>(null) }

    val readyCount = uiState.warehouseArticles.count { it.warehouseStatus == WarehouseArticleStatus.STORED_READY }
    val registeredCount = uiState.warehouseArticles.count { it.warehouseStatus == WarehouseArticleStatus.REGISTERED }

    val filteredArticles = if (statusFilter == null) {
        uiState.warehouseArticles
    } else {
        uiState.warehouseArticles.filter { it.warehouseStatus == statusFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "글 창고 · 자동 등록",
                subtitle = "버튼 1회로 95점+ 자동 교정 후 선택 채널 바로 등록"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeSubTab == "WAREHOUSE",
                    onClick = { activeSubTab = "WAREHOUSE" },
                    label = { Text("창고 (${uiState.warehouseArticles.size})") },
                    modifier = Modifier.testTag("subtab_warehouse_mgmt")
                )
                FilterChip(
                    selected = activeSubTab == "VISITOR_LOGS",
                    onClick = { activeSubTab = "VISITOR_LOGS" },
                    label = { Text("방문자·작성 로그 (${uiState.visitorLogs.firstOrNull()?.totalViews ?: 0}명)") },
                    modifier = Modifier.testTag("subtab_visitor_logs")
                )
            }
        }

        if (activeSubTab == "WAREHOUSE") {
            item {
                StudioCard(borderColor = EmeraldPrimary.copy(alpha = 0.55f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(NavySurfaceElevated)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricPair("전체", "${uiState.warehouseArticles.size}건", CloudflareSky, Modifier.weight(1f))
                        MetricPair("등록 대기", "${readyCount}건", MintSecondary, Modifier.weight(1f))
                        MetricPair("등록 완료", "${registeredCount}건", TistoryAmber, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onCreateHighScoreAndAutoRegister(null) },
                        enabled = !uiState.isBusy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_warehouse_instant_create"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
                    ) {
                        Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("고득점(95+) 글 바로 생성 · 자동 등록", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onImportQueueToWarehouse,
                        enabled = !uiState.isBusy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_import_queue_to_warehouse")
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("예약큐 전체 창고 입고")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = statusFilter == null,
                            onClick = { statusFilter = null },
                            label = { Text("전체 (${uiState.warehouseArticles.size})") }
                        )
                        WarehouseArticleStatus.entries.forEach { st ->
                            val cnt = uiState.warehouseArticles.count { it.warehouseStatus == st }
                            FilterChip(
                                selected = statusFilter == st,
                                onClick = { statusFilter = st },
                                label = { Text("${st.label} ($cnt)") }
                            )
                        }
                    }
                }
            }

            items(filteredArticles, key = { it.warehouseId }) { item ->
                SimpleWarehouseArticleCard(
                    item = item,
                    isBusy = uiState.isBusy,
                    onAutoRegister = { onAutoFixAndRegisterSlot(item.slot.postId) },
                    onRun30Eval = { onRun30PointEval(item.warehouseId) },
                    onInspectQa = { onInspectArticleInQa(item.slot.postId) },
                    onDelete = { onDeleteWarehouseArticle(item.warehouseId) }
                )
            }
        } else {
            val today = uiState.visitorLogs.firstOrNull()
            item {
                StudioCard(borderColor = CloudflareSky.copy(alpha = 0.55f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Assessment, contentDescription = "방문자 로그", tint = CloudflareSky)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("방문자 · 작성 요약", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Text("오늘 ${today?.totalViews ?: 0}명 · DB ${today?.dbInquiries ?: 0}건", style = MaterialTheme.typography.bodySmall, color = MintSecondary)
                            }
                        }
                        OutlinedButton(
                            onClick = onRefreshVisitorTelemetry,
                            modifier = Modifier.height(48.dp).testTag("btn_refresh_visitor_logs")
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("갱신")
                        }
                    }
                }
            }

            item {
                QueueAnalyticsChartsCard(
                    analytics = uiState.queueAnalytics,
                    telemetry = uiState.telemetry,
                    queueSyncState = uiState.queueSyncState,
                    compactMode = true,
                    onRefreshServer = onRefreshVisitorTelemetry,
                    onRefreshQueue = onRefreshQueueCharts,
                    onOpenSettings = onOpenSettings
                )
            }

            items(uiState.visitorLogs, key = { it.dateLabel }) { vLog ->
                StudioCard(containerColor = NavySurfaceElevated) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(vLog.dateLabel, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        StatusChip("총 ${vLog.totalViews}명", CloudflareSky, CloudflareContainer)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "네이버 ${vLog.naverViews} · 자체웹 ${vLog.cloudflareViews} · 티스토리 ${vLog.tistoryViews} · 수익 ${QaVerificationEngine.formatKrwShort(vLog.estimatedAdpostKrw)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            items(uiState.writingLogs, key = { it.logId }) { wLog ->
                StudioCard(containerColor = NavySurface) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(wLog.caseId, style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily), color = TextPrimary)
                        StatusChip(
                            text = "${wLog.score}점",
                            textColor = if (wLog.passedGate95) MintSecondary else CrimsonAlert,
                            backgroundColor = if (wLog.passedGate95) EmeraldContainer else CrimsonContainer
                        )
                    }
                    Text(wLog.actionTitle, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    Text(wLog.timestamp, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SimpleWarehouseArticleCard(
    item: WarehouseArticle,
    isBusy: Boolean,
    onAutoRegister: () -> Unit,
    onRun30Eval: () -> Unit,
    onInspectQa: () -> Unit,
    onDelete: () -> Unit
) {
    var showDetails by remember(item.warehouseId) { mutableStateOf(false) }
    val eval = item.evaluation
    val eligible = eval.eligibleForRegistration

    StudioCard(
        borderColor = if (item.warehouseStatus == WarehouseArticleStatus.REGISTERED) EmeraldPrimary.copy(alpha = 0.55f) else NavyBorder,
        modifier = Modifier.testTag("warehouse_card_${item.slot.auctionCase.caseId}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.slot.auctionCase.caseId} · ${item.slot.auctionCase.propertyType.displayLabel}",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Text(
                    text = item.slot.auctionCase.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            StatusChip(
                text = "${eval.totalScore}점",
                textColor = if (eligible) MintSecondary else CrimsonAlert,
                backgroundColor = if (eligible) EmeraldContainer else CrimsonContainer
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onAutoRegister,
                enabled = !isBusy,
                modifier = Modifier
                    .weight(1.4f)
                    .height(48.dp)
                    .testTag("btn_register_warehouse_${item.slot.auctionCase.caseId}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (item.warehouseStatus == WarehouseArticleStatus.REGISTERED) CloudflareSky else EmeraldPrimary,
                    contentColor = NavyBackground
                )
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (item.warehouseStatus == WarehouseArticleStatus.REGISTERED) "등록완료 · 재검증" else "95+ 자동 등록",
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            OutlinedButton(
                onClick = { showDetails = !showDetails },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(if (showDetails) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (showDetails) "접기" else "상세 도구", maxLines = 1)
            }
        }

        AnimatedVisibility(visible = showDetails) {
            Column(
                modifier = Modifier.padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onInspectQa, modifier = Modifier.weight(1f).height(48.dp)) {
                        Text("원고·인포 확인", maxLines = 1)
                    }
                    OutlinedButton(onClick = onRun30Eval, enabled = !isBusy, modifier = Modifier.weight(1f).height(48.dp)) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("재평가", maxLines = 1)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "창고에서 삭제", tint = TextMuted)
                    }
                }

                EvaluationPillar.entries.forEach { pillar ->
                    val pillarItems = eval.items.filter { it.pillar == pillar }
                    val earned = pillarItems.sumOf { it.earnedPoints }
                    val max = pillarItems.sumOf { it.maxPoints }
                    Surface(
                        color = NavyBackground,
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(pillar.label, style = MaterialTheme.typography.labelMedium, color = CloudflareSky)
                                Text("$earned / $max 점", style = MaterialTheme.typography.labelSmall, color = MintSecondary)
                            }
                            pillarItems.filter { !it.passed }.forEach { crit ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${crit.title}: ${crit.detail}", style = MaterialTheme.typography.labelSmall, color = CrimsonAlert)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
