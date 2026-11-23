package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.engine.Article30PointEvaluator
import com.example.engine.QaVerificationEngine
import com.example.model.EvaluationPillar
import com.example.model.PropertyType
import com.example.model.WarehouseArticle
import com.example.model.WarehouseArticleStatus
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
import com.example.ui.theme.NavyContainerHighest
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TistoryAmber

@Composable
fun WarehouseAndVisitorDashboardScreen(
    uiState: StudioUiState,
    onInstantCreateAndStore: (PropertyType?) -> Unit,
    onImportQueueToWarehouse: () -> Unit,
    onRun30PointEval: (String) -> Unit,
    onAutoOptimizeTo95: (String) -> Unit,
    onRegisterIfScore95: (String) -> Unit,
    onDeleteWarehouseArticle: (String) -> Unit,
    onInspectArticleInQa: (String) -> Unit,
    onRefreshVisitorTelemetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubTab by rememberSaveable { mutableStateOf("WAREHOUSE") } // WAREHOUSE | VISITOR_LOGS
    var statusFilter by rememberSaveable { mutableStateOf<WarehouseArticleStatus?>(null) }
    var instantTargetType by rememberSaveable { mutableStateOf<PropertyType?>(null) }

    val readyCount = uiState.warehouseArticles.count { it.warehouseStatus == WarehouseArticleStatus.STORED_READY }
    val registeredCount = uiState.warehouseArticles.count { it.warehouseStatus == WarehouseArticleStatus.REGISTERED }
    val below95Count = uiState.warehouseArticles.count { !it.evaluation.eligibleForRegistration }

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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "글관리 창고 · 작성 및 방문자 로그 대시보드",
                subtitle = "버튼 1회로 글 생성·창고 보관 후 30개 항목 테스트(95점 이상만 등록 허용)"
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeSubTab == "WAREHOUSE",
                    onClick = { activeSubTab = "WAREHOUSE" },
                    label = { Text("글관리 창고 (${uiState.warehouseArticles.size})") },
                    modifier = Modifier.testTag("subtab_warehouse_mgmt")
                )
                FilterChip(
                    selected = activeSubTab == "VISITOR_LOGS",
                    onClick = { activeSubTab = "VISITOR_LOGS" },
                    label = { Text("작성·방문자 로그 (${uiState.visitorLogs.firstOrNull()?.totalViews ?: 0}명)") },
                    modifier = Modifier.testTag("subtab_visitor_logs")
                )
            }
        }

        if (activeSubTab == "WAREHOUSE") {
            item {
                StudioCard(borderColor = EmeraldPrimary.copy(alpha = 0.55f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Inventory2, contentDescription = "글관리 창고", tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("글 창고(Vault) & 30항목 95점+ 등록 게이트", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Text("생성된 모든 글은 창고에 영구 보관되며, 95점 이상만 발행 등록됩니다.", style = MaterialTheme.typography.bodySmall, color = MintSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(NavySurfaceElevated)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricPair("창고 보관", "${uiState.warehouseArticles.size}건", CloudflareSky, Modifier.weight(1f))
                        MetricPair("95점+ 대기", "${readyCount}건", MintSecondary, Modifier.weight(1f))
                        MetricPair("등록 완료", "${registeredCount}건", TistoryAmber, Modifier.weight(1f))
                        MetricPair("95점 미만", "${below95Count}건", if (below95Count > 0) CrimsonAlert else TextMuted, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = instantTargetType == null,
                            onClick = { instantTargetType = null },
                            label = { Text("물건 자동순환") }
                        )
                        PropertyType.entries.forEach { propType ->
                            FilterChip(
                                selected = instantTargetType == propType,
                                onClick = { instantTargetType = propType },
                                label = { Text(propType.displayLabel) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onInstantCreateAndStore(instantTargetType) },
                        enabled = !uiState.isBusy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_warehouse_instant_create"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
                    ) {
                        Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (instantTargetType == null) "버튼으로 글 바로 생성 · 창고 보관" else "${instantTargetType!!.displayLabel} 글 바로 생성 · 창고 보관",
                            fontWeight = FontWeight.Bold
                        )
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
                        Text("현재 발행큐 물건 일괄 평가·창고 입고")
                    }

                    Spacer(modifier = Modifier.height(10.dp))
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
                WarehouseArticleCard(
                    item = item,
                    isBusy = uiState.isBusy,
                    onRun30Eval = { onRun30PointEval(item.warehouseId) },
                    onAutoOptimize = { onAutoOptimizeTo95(item.warehouseId) },
                    onRegister95 = { onRegisterIfScore95(item.warehouseId) },
                    onInspectQa = { onInspectArticleInQa(item.slot.postId) },
                    onDelete = { onDeleteWarehouseArticle(item.warehouseId) }
                )
            }
        } else {
            // VISITOR & WRITING LOGS DASHBOARD
            val today = uiState.visitorLogs.firstOrNull()
            val totalWritten = uiState.writingLogs.size
            val passRatePct = if (uiState.warehouseArticles.isEmpty()) 100 else {
                (uiState.warehouseArticles.count { it.evaluation.eligibleForRegistration } * 100) / uiState.warehouseArticles.size
            }

            item {
                StudioCard(borderColor = CloudflareSky.copy(alpha = 0.55f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Assessment, contentDescription = "작성 및 방문자 로그", tint = CloudflareSky)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("실시간 방문자 · 작성 · 전환 성과 요약", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Text("누적 작성 ${totalWritten}건 · 95점+ 통과율 ${passRatePct}%", style = MaterialTheme.typography.bodySmall, color = MintSecondary)
                            }
                        }
                        OutlinedButton(
                            onClick = onRefreshVisitorTelemetry,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("btn_refresh_visitor_logs")
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("로그 갱신")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(NavySurfaceElevated)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricPair("오늘 방문자", "${today?.totalViews ?: 0}명", CloudflareSky, Modifier.weight(1f))
                        MetricPair("평균 체류", "${today?.avgDwellSec ?: 0}초", MintSecondary, Modifier.weight(1f))
                        MetricPair("DB / PRO", "${today?.dbInquiries ?: 0} / ${today?.proConversions ?: 0}", TistoryAmber, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("7일 채널별 방문자 추이 (네이버 · 자체웹 · 티스토리)", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))

                    val chartLogs = uiState.visitorLogs.take(7).reversed()
                    val maxTotal = (chartLogs.maxOfOrNull { it.totalViews } ?: 1).coerceAtLeast(1)

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        val barSlotWidth = size.width / chartLogs.size.coerceAtLeast(1)
                        val barWidth = (barSlotWidth * 0.56f).coerceAtLeast(14f)
                        chartLogs.forEachIndexed { idx, log ->
                            val x = idx * barSlotWidth + (barSlotWidth - barWidth) / 2f
                            val totalH = (log.totalViews.toFloat() / maxTotal.toFloat()) * (size.height - 16f)
                            val nvH = totalH * (log.naverViews.toFloat() / log.totalViews.coerceAtLeast(1))
                            val cfH = totalH * (log.cloudflareViews.toFloat() / log.totalViews.coerceAtLeast(1))
                            val tsH = (totalH - nvH - cfH).coerceAtLeast(0f)

                            var currentY = size.height
                            drawRoundRect(
                                color = NaverGreen,
                                topLeft = Offset(x, currentY - nvH),
                                size = Size(barWidth, nvH),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                            currentY -= nvH
                            drawRect(
                                color = CloudflareSky,
                                topLeft = Offset(x, currentY - cfH),
                                size = Size(barWidth, cfH)
                            )
                            currentY -= cfH
                            drawRoundRect(
                                color = TistoryAmber,
                                topLeft = Offset(x, currentY - tsH),
                                size = Size(barWidth, tsH),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("■ 네이버", style = MaterialTheme.typography.labelSmall, color = NaverGreen)
                        Text("■ 자체웹(CF)", style = MaterialTheme.typography.labelSmall, color = CloudflareSky)
                        Text("■ 티스토리", style = MaterialTheme.typography.labelSmall, color = TistoryAmber)
                    }
                }
            }

            item {
                SectionHeader(
                    title = "일일 방문자 · 애드포스트 · DB/PRO 전환 상세 로그",
                    subtitle = "최근 7일 채널 유입 및 수익 기여 기록"
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
                        StatusChip(
                            text = "총 ${vLog.totalViews}명 · 체류 ${vLog.avgDwellSec}초",
                            textColor = CloudflareSky,
                            backgroundColor = CloudflareContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "네이버 ${vLog.naverViews} · 자체웹 ${vLog.cloudflareViews} · 티스토리 ${vLog.tistoryViews}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        "매수대리 DB ${vLog.dbInquiries}건 · PRO 전환 ${vLog.proConversions}건 · 애드포스트 약 ${QaVerificationEngine.formatKrwShort(vLog.estimatedAdpostKrw)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MintSecondary
                    )
                }
            }

            item {
                SectionHeader(
                    title = "글 작성 · 30항목 평가 · 등록 감사 로그 (${uiState.writingLogs.size}건)",
                    subtitle = "95점 이상 게이트 통과 및 창고 보관 이력"
                )
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
                            text = "${wLog.score}점 ${if (wLog.passedGate95) "95+ PASS" else "차단"}",
                            textColor = if (wLog.passedGate95) MintSecondary else CrimsonAlert,
                            backgroundColor = if (wLog.passedGate95) EmeraldContainer else CrimsonContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(wLog.actionTitle, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Text("${wLog.detail} · ${wLog.timestamp}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WarehouseArticleCard(
    item: WarehouseArticle,
    isBusy: Boolean,
    onRun30Eval: () -> Unit,
    onAutoOptimize: () -> Unit,
    onRegister95: () -> Unit,
    onInspectQa: () -> Unit,
    onDelete: () -> Unit
) {
    var show30Details by remember(item.warehouseId) { mutableStateOf(false) }
    val eval = item.evaluation
    val eligible = eval.eligibleForRegistration
    val scoreColor = if (eligible) MintSecondary else CrimsonAlert

    StudioCard(
        borderColor = if (eligible) EmeraldPrimary.copy(alpha = 0.45f) else CrimsonAlert.copy(alpha = 0.7f),
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
                text = "${eval.totalScore}/100점",
                textColor = scoreColor,
                backgroundColor = if (eligible) EmeraldContainer else CrimsonContainer
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (eval.totalScore / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = scoreColor,
            trackColor = NavyContainerHighest
        )

        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            StatusChip(
                text = item.warehouseStatus.label,
                textColor = when (item.warehouseStatus) {
                    WarehouseArticleStatus.REGISTERED -> CloudflareSky
                    WarehouseArticleStatus.STORED_READY -> MintSecondary
                    WarehouseArticleStatus.NEEDS_IMPROVEMENT -> CrimsonAlert
                },
                backgroundColor = when (item.warehouseStatus) {
                    WarehouseArticleStatus.REGISTERED -> CloudflareContainer
                    WarehouseArticleStatus.STORED_READY -> EmeraldContainer
                    WarehouseArticleStatus.NEEDS_IMPROVEMENT -> CrimsonContainer
                }
            )
            StatusChip(
                text = "30항목 ${eval.passedCount}/30 통과 (${Article30PointEvaluator.REGISTRATION_MIN_SCORE}점+ 게이트)",
                textColor = TextSecondary,
                backgroundColor = NavySurfaceElevated
            )
            if (item.warehouseStatus == WarehouseArticleStatus.REGISTERED) {
                StatusChip(
                    text = "조회 ${item.accumulatedViews} · DB ${item.dbLeadCount} · PRO ${item.proClickCount}",
                    textColor = MintSecondary,
                    backgroundColor = EmeraldContainer
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "생성: ${item.createdAt}" + (item.registeredAt?.let { " · 등록: $it" } ?: ""),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { show30Details = !show30Details },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(if (show30Details) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("30항목 상세", maxLines = 1)
            }
            OutlinedButton(
                onClick = onRun30Eval,
                enabled = !isBusy,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("재평가", maxLines = 1)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "창고에서 삭제", tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onInspectQa,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("등기인포·원고", maxLines = 1)
            }
            if (!eligible) {
                Button(
                    onClick = onAutoOptimize,
                    enabled = !isBusy,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TistoryAmber, contentColor = NavyBackground)
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("95+ 보완", maxLines = 1)
                }
            } else {
                Button(
                    onClick = onRegister95,
                    enabled = !isBusy && eligible && item.warehouseStatus != WarehouseArticleStatus.REGISTERED,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_register_warehouse_${item.slot.auctionCase.caseId}"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (item.warehouseStatus == WarehouseArticleStatus.REGISTERED) "등록완료" else "95점+ 등록",
                        maxLines = 1
                    )
                }
            }
        }

        AnimatedVisibility(visible = show30Details) {
            Column(
                modifier = Modifier.padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EvaluationPillar.entries.forEach { pillar ->
                    val pillarItems = eval.items.filter { it.pillar == pillar }
                    val pillarEarned = pillarItems.sumOf { it.earnedPoints }
                    val pillarMax = pillarItems.sumOf { it.maxPoints }
                    Surface(
                        color = NavyBackground,
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(pillar.label, style = MaterialTheme.typography.labelLarge, color = CloudflareSky)
                                Text("$pillarEarned / $pillarMax 점", style = MaterialTheme.typography.labelMedium, color = MintSecondary)
                            }
                            pillarItems.forEach { crit ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = if (crit.passed) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
                                        contentDescription = null,
                                        tint = if (crit.passed) EmeraldPrimary else CrimsonAlert,
                                        modifier = Modifier.size(14.dp).padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${crit.orderIndex}. ${crit.title} (${crit.earnedPoints}/${crit.maxPoints}) — ${crit.detail}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (crit.passed) TextSecondary else CrimsonAlert
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
