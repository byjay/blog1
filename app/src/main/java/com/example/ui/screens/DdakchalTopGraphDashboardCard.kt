package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.ChannelPlatform
import com.example.model.PostStatus
import com.example.ui.StudioUiState
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.CloudflareContainer
import com.example.ui.theme.CloudflareSky
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.NaverGreen
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TistoryAmber

data class ChannelBarMetric(
    val platform: ChannelPlatform,
    val label: String,
    val published: Int,
    val queued: Int,
    val failed: Int,
    val total: Int
)

data class VisitorPointMetric(
    val label: String,
    val views: Int,
    val dbInquiries: Int
)

data class DdakchalTopGraphMetrics(
    val serverConnected: Boolean,
    val latencyMs: Long?,
    val dbItemCount: Int,
    val d1TableCount: Int,
    val triggerPendingCount: Int,
    val triggerProcessedCount: Int,
    val triggerFailedCount: Int,
    val snapshotCount: Int,
    val dbUpdatedAt: String,
    val totalArticlesCount: Int,
    val publishedCount: Int,
    val queuedCount: Int,
    val failedCount: Int,
    val successRatePct: Int,
    val channelBars: List<ChannelBarMetric>,
    val totalVisitors: Int,
    val totalDbInquiries: Int,
    val visitorSeries: List<VisitorPointMetric>
)

fun computeDdakchalTopGraphMetrics(uiState: StudioUiState): DdakchalTopGraphMetrics {
    val admin = uiState.todakAdmin
    val trigger = uiState.telemetry.triggerStatus
    val queueAnalytics = uiState.queueAnalytics

    val channelBars = ChannelPlatform.entries.map { platform ->
        val stat = queueAnalytics?.channelStats?.firstOrNull { it.platform == platform }
        if (stat != null && (stat.publishedCount + stat.queuedCount + stat.failedCount) > 0) {
            ChannelBarMetric(
                platform = platform,
                label = when (platform) {
                    ChannelPlatform.NAVER -> "네이버"
                    ChannelPlatform.TISTORY -> "티스토리"
                    ChannelPlatform.CLOUDFLARE -> "자체웹"
                },
                published = stat.publishedCount,
                queued = stat.queuedCount,
                failed = stat.failedCount,
                total = stat.publishedCount + stat.queuedCount + stat.failedCount
            )
        } else {
            val platformLogs = uiState.publishLogs.filter { it.platform == platform }
            val publishedByLogs = platformLogs.filter { it.status == PostStatus.PUBLISHED }.map { it.postId }.toSet()
            val failedByLogs = platformLogs.filter { it.status == PostStatus.FAILED || it.status == PostStatus.HANDOFF }.map { it.postId }.toSet()
            var chPub = 0
            var chQ = 0
            var chFail = 0
            uiState.slots.forEach { slot ->
                when {
                    slot.publishedUrls.containsKey(platform) || slot.status == PostStatus.PUBLISHED || slot.postId in publishedByLogs -> chPub++
                    slot.postId in failedByLogs || slot.status == PostStatus.FAILED || slot.status == PostStatus.HANDOFF -> chFail++
                    else -> chQ++
                }
            }
            ChannelBarMetric(
                platform = platform,
                label = when (platform) {
                    ChannelPlatform.NAVER -> "네이버"
                    ChannelPlatform.TISTORY -> "티스토리"
                    ChannelPlatform.CLOUDFLARE -> "자체웹"
                },
                published = chPub,
                queued = chQ,
                failed = chFail,
                total = chPub + chQ + chFail
            )
        }
    }

    val totalSlots = queueAnalytics?.totalSlots?.takeIf { it > 0 } ?: uiState.slots.size
    val publishedCount = queueAnalytics?.publishedCount?.takeIf { queueAnalytics.totalSlots > 0 }
        ?: uiState.slots.count { it.status == PostStatus.PUBLISHED }
    val queuedCount = queueAnalytics?.queuedCount?.takeIf { queueAnalytics.totalSlots > 0 }
        ?: uiState.slots.count { it.status == PostStatus.QUEUED || it.status == PostStatus.DRAFT }
    val failedCount = queueAnalytics?.failedOrHandoffCount?.takeIf { queueAnalytics.totalSlots > 0 }
        ?: uiState.slots.count { it.status == PostStatus.FAILED || it.status == PostStatus.HANDOFF }
    val decided = (publishedCount + failedCount).coerceAtLeast(1)
    val successRate = when {
        queueAnalytics != null && queueAnalytics.totalSlots > 0 -> queueAnalytics.successRatePct
        publishedCount + failedCount > 0 -> ((publishedCount * 100f) / decided).toInt().coerceIn(0, 100)
        else -> 0
    }

    val totalVisitors = uiState.visitorLogs.sumOf { it.totalViews }
    val totalInquiries = uiState.visitorLogs.sumOf { it.dbInquiries }
    val visitorSeries = uiState.visitorLogs.take(7).reversed().map { v ->
        VisitorPointMetric(
            label = v.dateLabel.takeLast(5).ifBlank { "-" },
            views = v.totalViews,
            dbInquiries = v.dbInquiries
        )
    }

    return DdakchalTopGraphMetrics(
        serverConnected = uiState.telemetry.ddakchalConnected,
        latencyMs = uiState.telemetry.apiLatencyMs,
        dbItemCount = admin.discoveredItems.size,
        d1TableCount = maxOf(admin.d1TablesRaw.size, admin.discoveredTables.size),
        triggerPendingCount = trigger?.pendingCount ?: 0,
        triggerProcessedCount = trigger?.processedCount ?: 0,
        triggerFailedCount = trigger?.failedCount ?: 0,
        snapshotCount = trigger?.snapshotsCount ?: 0,
        dbUpdatedAt = admin.lastUpdatedAt.ifBlank { uiState.telemetry.lastSyncTime },
        totalArticlesCount = totalSlots,
        publishedCount = publishedCount,
        queuedCount = queuedCount,
        failedCount = failedCount,
        successRatePct = successRate,
        channelBars = channelBars,
        totalVisitors = totalVisitors,
        totalDbInquiries = totalInquiries,
        visitorSeries = visitorSeries
    )
}

@Composable
fun DdakchalTopGraphDashboardCard(
    uiState: StudioUiState,
    onRefreshAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics = remember(
        uiState.telemetry,
        uiState.todakAdmin.discoveredItems,
        uiState.todakAdmin.discoveredTables,
        uiState.todakAdmin.d1TablesRaw,
        uiState.todakAdmin.lastUpdatedAt,
        uiState.slots,
        uiState.publishLogs,
        uiState.queueAnalytics,
        uiState.visitorLogs
    ) {
        computeDdakchalTopGraphMetrics(uiState)
    }

    Surface(
        color = NavyBackground,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, CloudflareSky.copy(alpha = 0.55f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_ddakchal_top_graph_dashboard")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header with icon + plain explanation + 1-tap refresh icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = CloudflareContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.QueryStats,
                                contentDescription = "딱찰 실시간 현황 그래프",
                                tint = CloudflareSky,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "딱찰 실시간 현황 그래프",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "수집 DB · 블로그 글 수 · 방문자를 한눈에 봅니다",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(
                        text = if (metrics.serverConnected) "연결 ${metrics.latencyMs ?: "--"}ms" else "동기화 대기",
                        textColor = if (metrics.serverConnected) MintSecondary else TistoryAmber,
                        backgroundColor = if (metrics.serverConnected) EmeraldContainer else AmberContainer,
                        dotColor = if (metrics.serverConnected) MintSecondary else TistoryAmber
                    )
                    IconButton(
                        onClick = onRefreshAll,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_refresh_top_graphs")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "딱찰 그래프 새로고침",
                            tint = CloudflareSky
                        )
                    }
                }
            }

            // 1. 딱찰 경매 수집 · DB 업데이트 그래프
            GraphSectionPanel(
                icon = Icons.Filled.Storage,
                iconTint = CloudflareSky,
                title = "1. 경매 수집 · DB 현황",
                badgeText = if (metrics.dbUpdatedAt.isNotBlank()) "갱신 ${metrics.dbUpdatedAt.takeLast(8)}" else "조회 전",
                badgeColor = CloudflareSky,
                easyHint = "딱찰 서버와 D1 DB에 저장된 실제 매물·처리 건수입니다."
            ) {
                val barRows = listOf(
                    Triple("수집 매물", metrics.dbItemCount, CloudflareSky),
                    Triple("트리거 완료", metrics.triggerProcessedCount, MintSecondary),
                    Triple("대기/실패", metrics.triggerPendingCount + metrics.triggerFailedCount, TistoryAmber),
                    Triple("스냅샷/테이블", metrics.snapshotCount + metrics.d1TableCount, EmeraldPrimary)
                )
                val maxBarVal = barRows.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    barRows.forEach { (label, count, color) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                modifier = Modifier.width(74.dp),
                                maxLines = 1
                            )
                            Canvas(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(12.dp)
                            ) {
                                val trackHeight = size.height
                                drawRoundRect(
                                    color = Color(0xFF1E293B),
                                    cornerRadius = CornerRadius(6f, 6f),
                                    size = Size(size.width, trackHeight)
                                )
                                val ratio = (count.toFloat() / maxBarVal.toFloat()).coerceIn(0f, 1f)
                                if (count > 0 && ratio > 0f) {
                                    drawRoundRect(
                                        color = color,
                                        cornerRadius = CornerRadius(6f, 6f),
                                        size = Size((size.width * ratio).coerceAtLeast(8f), trackHeight)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${count}건",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (count > 0) color else TextMuted,
                                modifier = Modifier.width(42.dp)
                            )
                        }
                    }
                }
            }

            // 2. 블로그 발행 상태 & 채널별 글 수 그래프 + 성공률 도넛 게이지
            GraphSectionPanel(
                icon = Icons.AutoMirrored.Filled.Article,
                iconTint = MintSecondary,
                title = "2. 블로그 발행 상태 · 채널별 글 수",
                badgeText = "총 ${metrics.totalArticlesCount}건 · 성공 ${metrics.successRatePct}%",
                badgeColor = MintSecondary,
                easyHint = "네이버·티스토리·자체웹에 발행된 글과 예약 대기 글을 비교합니다."
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val stacked = maxWidth < 330.dp
                    val donutBlock: @Composable (Modifier) -> Unit = { m ->
                        Column(
                            modifier = m,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(84.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeW = 11.dp.toPx()
                                    val inset = strokeW / 2f
                                    val arcSize = Size(size.width - strokeW, size.height - strokeW)
                                    drawArc(
                                        color = Color(0xFF1E293B),
                                        startAngle = 0f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        topLeft = Offset(inset, inset),
                                        size = arcSize,
                                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                                    )
                                    val sweep = (metrics.successRatePct / 100f) * 360f
                                    if (sweep > 0f) {
                                        drawArc(
                                            color = EmeraldPrimary,
                                            startAngle = -90f,
                                            sweepAngle = sweep,
                                            useCenter = false,
                                            topLeft = Offset(inset, inset),
                                            size = arcSize,
                                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${metrics.successRatePct}%",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MintSecondary
                                    )
                                    Text(
                                        text = "발행률",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "완료 ${metrics.publishedCount} · 대기 ${metrics.queuedCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    val channelBarsBlock: @Composable (Modifier) -> Unit = { m ->
                        val maxChannelTotal = metrics.channelBars.maxOfOrNull { it.total }?.coerceAtLeast(1) ?: 1
                        Column(
                            modifier = m,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(74.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                metrics.channelBars.forEach { bar ->
                                    val barColor = when (bar.platform) {
                                        ChannelPlatform.NAVER -> NaverGreen
                                        ChannelPlatform.TISTORY -> TistoryAmber
                                        ChannelPlatform.CLOUDFLARE -> CloudflareSky
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "${bar.total}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = JetBrainsMonoFamily,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (bar.total > 0) barColor else TextMuted
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Canvas(
                                            modifier = Modifier
                                                .width(26.dp)
                                                .height(46.dp)
                                        ) {
                                            drawRoundRect(
                                                color = Color(0xFF1E293B),
                                                cornerRadius = CornerRadius(8f, 8f),
                                                size = size
                                            )
                                            val hRatio = (bar.total.toFloat() / maxChannelTotal.toFloat()).coerceIn(0f, 1f)
                                            if (bar.total > 0) {
                                                val h = (size.height * hRatio).coerceAtLeast(8f)
                                                drawRoundRect(
                                                    color = barColor,
                                                    topLeft = Offset(0f, size.height - h),
                                                    size = Size(size.width, h),
                                                    cornerRadius = CornerRadius(8f, 8f)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = bar.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                LegendMiniChip(color = NaverGreen, label = "완료 ${metrics.publishedCount}")
                                LegendMiniChip(color = TistoryAmber, label = "예약 ${metrics.queuedCount}")
                                LegendMiniChip(color = CrimsonAlert, label = "점검 ${metrics.failedCount}")
                            }
                        }
                    }

                    if (stacked) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            donutBlock(Modifier)
                            channelBarsBlock(Modifier.fillMaxWidth())
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            donutBlock(Modifier.width(100.dp))
                            channelBarsBlock(Modifier.weight(1f))
                        }
                    }
                }
            }

            // 3. 방문자 수 & 유입 추이 그래프
            GraphSectionPanel(
                icon = Icons.Filled.Groups,
                iconTint = TistoryAmber,
                title = "3. 방문자 수 · 문의 추이",
                badgeText = "누적 ${metrics.totalVisitors}명 · DB ${metrics.totalDbInquiries}건",
                badgeColor = TistoryAmber,
                easyHint = "발행된 블로그 글에 들어온 실제 방문자와 문의 전환 추이입니다."
            ) {
                if (metrics.visitorSeries.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(NavyBackground)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "방문자 데이터 대기",
                            tint = TistoryAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "실제 방문자 로그가 아직 0건입니다. 상단 새로고침 또는 창고·로그 탭에서 실시간 유입을 동기화하세요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                } else {
                    val points = metrics.visitorSeries
                    val maxViews = points.maxOfOrNull { it.views }?.coerceAtLeast(1) ?: 1
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                        ) {
                            val padX = 14f
                            val padTop = 10f
                            val padBottom = 10f
                            val usableW = (size.width - padX * 2).coerceAtLeast(1f)
                            val usableH = (size.height - padTop - padBottom).coerceAtLeast(1f)

                            val coords = points.mapIndexed { idx, pt ->
                                val x = if (points.size <= 1) {
                                    padX + usableW / 2f
                                } else {
                                    padX + usableW * (idx.toFloat() / (points.size - 1).toFloat())
                                }
                                val y = padTop + usableH * (1f - (pt.views.toFloat() / maxViews.toFloat()).coerceIn(0f, 1f))
                                Offset(x, y)
                            }

                            if (coords.size >= 2) {
                                val linePath = Path().apply {
                                    moveTo(coords.first().x, coords.first().y)
                                    for (i in 1 until coords.size) {
                                        lineTo(coords[i].x, coords[i].y)
                                    }
                                }
                                val fillPath = Path().apply {
                                    addPath(linePath)
                                    lineTo(coords.last().x, size.height - padBottom)
                                    lineTo(coords.first().x, size.height - padBottom)
                                    close()
                                }
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        listOf(TistoryAmber.copy(alpha = 0.35f), Color.Transparent)
                                    )
                                )
                                drawPath(
                                    path = linePath,
                                    color = TistoryAmber,
                                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                                )
                            }
                            coords.forEach { pt ->
                                drawCircle(
                                    color = NavyBackground,
                                    radius = 7f,
                                    center = pt
                                )
                                drawCircle(
                                    color = MintSecondary,
                                    radius = 4.5f,
                                    center = pt
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            points.forEach { pt ->
                                Text(
                                    text = "${pt.label}(${pt.views})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                    color = TextMuted,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GraphSectionPanel(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    badgeText: String,
    badgeColor: Color,
    easyHint: String,
    content: @Composable () -> Unit
) {
    Surface(
        color = NavySurfaceElevated,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, NavyBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StatusChip(
                    text = badgeText,
                    textColor = badgeColor,
                    backgroundColor = NavyBackground
                )
            }
            content()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = easyHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LegendMiniChip(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
