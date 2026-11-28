package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.QueueAnalyticsSummary
import com.example.model.QueueSyncState
import com.example.model.SecurityDashboardSummary
import com.example.model.ServerTelemetryState
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
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TistoryAmber

@Composable
fun DdakchalTopStatusBar(
    telemetry: ServerTelemetryState,
    isMobileSimpleMode: Boolean,
    onToggleMobileMode: (Boolean) -> Unit,
    onRefreshPing: () -> Unit,
    onToggleAutoMonitor: (Boolean) -> Unit,
    securitySummary: SecurityDashboardSummary? = null,
    onOpenSecurityDashboard: () -> Unit = {},
    onOpenSettings: () -> Unit = onOpenSecurityDashboard,
    modifier: Modifier = Modifier
) {
    val hasError = !telemetry.ddakchalConnected && !telemetry.lastErrorDetail.isNullOrBlank()
    val statusDotColor = when {
        telemetry.isPinging -> CloudflareSky
        telemetry.ddakchalConnected -> EmeraldPrimary
        hasError -> CrimsonAlert
        else -> TistoryAmber
    }
    val statusLabel = when {
        telemetry.isPinging -> "컨트롤타워 동기화"
        telemetry.ddakchalConnected -> "컨트롤타워 · 연결됨"
        hasError -> "컨트롤타워 · 점검"
        else -> "컨트롤타워 · 대기"
    }
    val latencyText = telemetry.apiLatencyMs?.let { "${it}ms" } ?: "--ms"
    val latencyColor = when (val ms = telemetry.apiLatencyMs) {
        null -> TextMuted
        in 0..350 -> MintSecondary
        in 351..900 -> TistoryAmber
        else -> CrimsonAlert
    }
    val shortSyncTime = telemetry.lastSyncTime
        .substringAfter(" ")
        .ifBlank { telemetry.lastSyncTime }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ddakchal_top_status_bar"),
        color = NavySurface,
        border = BorderStroke(1.dp, NavyBorder),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggleAutoMonitor(!telemetry.autoMonitorEnabled) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusDotColor)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (telemetry.ddakchalConnected) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                            contentDescription = "딱찰 API 상태",
                            tint = statusDotColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Surface(
                            color = NavySurfaceElevated,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, latencyColor.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Speed,
                                    contentDescription = "서버 응답 속도",
                                    tint = latencyColor,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = latencyText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                    color = latencyColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    Text(
                        text = buildString {
                            append("동기화 $shortSyncTime")
                            telemetry.triggerStatus?.let { tr ->
                                append(" · 트리거 대기 ${tr.pendingCount}/완료 ${tr.processedCount}")
                            }
                            if (telemetry.autoMonitorEnabled) append(" · 자동감시")
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (securitySummary != null) {
                    val gradeColor = securitySummary.overallGrade.accentColor()
                    Surface(
                        color = securitySummary.overallGrade.containerColor(),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, gradeColor.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .clickable(onClick = onOpenSecurityDashboard)
                            .testTag("btn_top_security_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = "보안 등급 대시보드 열기",
                                tint = gradeColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = securitySummary.overallGrade.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = gradeColor,
                                maxLines = 1
                            )
                        }
                    }
                }

                Surface(
                    color = if (isMobileSimpleMode) EmeraldContainer else CloudflareContainer,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isMobileSimpleMode) EmeraldPrimary.copy(alpha = 0.6f) else CloudflareSky.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onToggleMobileMode(!isMobileSimpleMode) }
                        .testTag("btn_toggle_mobile_precision_mode")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isMobileSimpleMode) Icons.Filled.Apps else Icons.Filled.Analytics,
                            contentDescription = "보기 모드 전환",
                            tint = if (isMobileSimpleMode) MintSecondary else CloudflareSky,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isMobileSimpleMode) "간편진행" else "정밀대시",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isMobileSimpleMode) MintSecondary else CloudflareSky,
                            maxLines = 1
                        )
                    }
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_top_settings")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "환경설정 열기",
                        tint = CloudflareSky
                    )
                }

                IconButton(
                    onClick = onRefreshPing,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_top_ping_ddakchal")
                ) {
                    if (telemetry.isPinging) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = CloudflareSky,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "딱찰 서버 즉시 상태확인",
                            tint = MintSecondary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QueueAnalyticsChartsCard(
    analytics: QueueAnalyticsSummary,
    telemetry: ServerTelemetryState,
    queueSyncState: QueueSyncState = QueueSyncState.Idle,
    compactMode: Boolean = false,
    onRefreshServer: (() -> Unit)? = null,
    onRefreshQueue: () -> Unit = onRefreshServer ?: {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    StudioCard(
        borderColor = CloudflareSky.copy(alpha = 0.5f),
        modifier = modifier.testTag("queue_analytics_charts_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "실서버 /api/v1/blog/queue · Recharts 실시간 차트",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Text(
                    text = "일일 발행량 · 성공/실패율 · 채널별 분포 (Retrofit 실데이터 전용)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            IconButton(
                onClick = onRefreshQueue,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("btn_sync_queue_charts")
            ) {
                Icon(
                    imageVector = Icons.Filled.Sync,
                    contentDescription = "Retrofit /api/v1/blog/queue 동기화",
                    tint = CloudflareSky
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (queueSyncState) {
            is QueueSyncState.Loading -> {
                Surface(
                    color = NavyBackground,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, CloudflareSky.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("queue_state_loading")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = CloudflareSky,
                            strokeWidth = 2.5.dp
                        )
                        Column {
                            Text(
                                text = "Retrofit GET /api/v1/blog/queue 조회 중...",
                                style = MaterialTheme.typography.labelLarge,
                                color = CloudflareSky
                            )
                            Text(
                                text = "더미 데이터를 사용하지 않고 실서버 응답을 대기하고 있습니다.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            is QueueSyncState.Error -> {
                Surface(
                    color = CrimsonContainer,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, CrimsonAlert),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("queue_state_error")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (queueSyncState.httpStatusCode > 0) {
                                    "실서버 /queue 조회 실패 (HTTP ${queueSyncState.httpStatusCode})"
                                } else {
                                    "실서버 /queue 네트워크 오류"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonAlert
                            )
                        }
                        Text(
                            text = queueSyncState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onRefreshQueue,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_retry_retrofit_queue"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CloudflareSky,
                                    contentColor = NavyBackground
                                )
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retrofit 재시도", fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                            if (queueSyncState.tokenMissing || queueSyncState.httpStatusCode in listOf(401, 403)) {
                                OutlinedButton(
                                    onClick = onOpenSettings,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_queue_open_settings")
                                ) {
                                    Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("토큰 설정", maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }

            is QueueSyncState.Empty -> {
                Surface(
                    color = NavyBackground,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("queue_state_empty")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CloudQueue, contentDescription = null, tint = TistoryAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "실서버 /api/v1/blog/queue 큐가 비어 있습니다 (0건)",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "${queueSyncState.message} (확인 시각 ${queueSyncState.syncedAt} · ${queueSyncState.latencyMs}ms)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        OutlinedButton(
                            onClick = onRefreshQueue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_refresh_empty_queue")
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retrofit /queue 다시 불러오기")
                        }
                    }
                }
            }

            is QueueSyncState.Loaded -> {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusChip(
                        text = "실큐 ${queueSyncState.analytics.totalSlots}건 (${queueSyncState.latencyMs}ms)",
                        textColor = CloudflareSky,
                        backgroundColor = CloudflareContainer
                    )
                    StatusChip(
                        text = "성공률 ${queueSyncState.analytics.successRatePct}% (${queueSyncState.analytics.publishedCount}건)",
                        textColor = MintSecondary,
                        backgroundColor = EmeraldContainer
                    )
                    StatusChip(
                        text = "예약대기 ${queueSyncState.analytics.queuedCount}건",
                        textColor = TistoryAmber,
                        backgroundColor = AmberContainer
                    )
                    StatusChip(
                        text = "실패율 ${queueSyncState.analytics.failureRatePct}% (${queueSyncState.analytics.failedOrHandoffCount}건)",
                        textColor = if (queueSyncState.analytics.failedOrHandoffCount > 0) CrimsonAlert else TextSecondary,
                        backgroundColor = if (queueSyncState.analytics.failedOrHandoffCount > 0) CrimsonContainer else NavySurfaceElevated
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                RechartsQueueWebView(
                    analytics = queueSyncState.analytics,
                    syncedAt = queueSyncState.syncedAt,
                    sourceEndpoint = queueSyncState.sourceEndpoint,
                    compactMode = compactMode
                )
            }

            is QueueSyncState.Idle -> {
                if (analytics.totalSlots == 0) {
                    Surface(
                        color = NavyBackground,
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("queue_state_idle")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "실서버 /api/v1/blog/queue 데이터 동기화 대기 중",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = "가짜 초기 데이터 없이 Retrofit으로 딱찰 실서버 큐를 조회해 Recharts 그래프를 그립니다.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Button(
                                onClick = onRefreshQueue,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_load_retrofit_queue"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CloudflareSky,
                                    contentColor = NavyBackground
                                )
                            ) {
                                Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("실서버 /queue 불러오기 (Recharts)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusChip(
                            text = "전체 ${analytics.totalSlots}건",
                            textColor = CloudflareSky,
                            backgroundColor = CloudflareContainer
                        )
                        StatusChip(
                            text = "성공률 ${analytics.successRatePct}% (${analytics.publishedCount}건)",
                            textColor = MintSecondary,
                            backgroundColor = EmeraldContainer
                        )
                        StatusChip(
                            text = "실패율 ${analytics.failureRatePct}% (${analytics.failedOrHandoffCount}건)",
                            textColor = if (analytics.failedOrHandoffCount > 0) CrimsonAlert else TextSecondary,
                            backgroundColor = if (analytics.failedOrHandoffCount > 0) CrimsonContainer else NavySurfaceElevated
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    RechartsQueueWebView(
                        analytics = analytics,
                        syncedAt = telemetry.lastSyncTime,
                        sourceEndpoint = "/api/v1/blog/queue",
                        compactMode = compactMode
                    )
                }
            }
        }
    }
}

