package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.ChannelPlatform
import com.example.model.ChannelProbeResult
import com.example.model.SecurityDashboardSummary
import com.example.model.SecurityGrade
import com.example.model.ServerAutomationConfig
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
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TistoryAmber

@Composable
fun SecurityGrade.accentColor(): Color = when (this) {
    SecurityGrade.S -> EmeraldPrimary
    SecurityGrade.A -> CloudflareSky
    SecurityGrade.B -> TistoryAmber
    SecurityGrade.C -> CrimsonAlert
}

@Composable
fun SecurityGrade.containerColor(): Color = when (this) {
    SecurityGrade.S -> EmeraldContainer
    SecurityGrade.A -> CloudflareContainer
    SecurityGrade.B -> AmberContainer
    SecurityGrade.C -> CrimsonContainer
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SecurityDashboardHeroCard(
    summary: SecurityDashboardSummary,
    isBusy: Boolean,
    onRefreshAudit: () -> Unit,
    onPurgePlaintextPw: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradeColor = summary.overallGrade.accentColor()

    StudioCard(
        borderColor = gradeColor.copy(alpha = 0.65f),
        modifier = modifier.testTag("security_dashboard_hero_card")
    ) {
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
                    imageVector = Icons.Filled.Shield,
                    contentDescription = "보안 현황 대시보드",
                    tint = gradeColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "보안 현황 대시보드",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "외부 토큰 노출 0건 · 비번 미저장 계정 등급 모니터",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            StatusChip(
                text = summary.overallGrade.label,
                textColor = gradeColor,
                backgroundColor = summary.overallGrade.containerColor(),
                dotColor = gradeColor
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(NavyBackground)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(94.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(88.dp)
                        .testTag("chart_security_score_gauge")
                ) {
                    val strokePx = 14f
                    val inset = strokePx / 2f
                    val arcSize = Size(size.width - strokePx, size.height - strokePx)
                    drawArc(
                        color = NavySurfaceElevated,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                    val sweep = (summary.overallScore.coerceIn(0, 100) / 100f) * 270f
                    drawArc(
                        color = gradeColor,
                        startAngle = 135f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = summary.overallGrade.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = gradeColor
                    )
                    Text(
                        text = "${summary.overallScore}점",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                        color = TextPrimary
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusChip(
                        text = if (summary.externalLeakCount == 0) "외부노출 0건" else "노출경고 ${summary.externalLeakCount}건",
                        textColor = if (summary.externalLeakCount == 0) MintSecondary else CrimsonAlert,
                        backgroundColor = if (summary.externalLeakCount == 0) EmeraldContainer else CrimsonContainer
                    )
                    StatusChip(
                        text = if (summary.legacyPlaintextPwDetected) "평문비번 잔존" else "비번 미저장 준수",
                        textColor = if (summary.legacyPlaintextPwDetected) CrimsonAlert else CloudflareSky,
                        backgroundColor = if (summary.legacyPlaintextPwDetected) CrimsonContainer else CloudflareContainer
                    )
                }
                Text(
                    text = summary.recommendationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
                Text(
                    text = "최종 감사: ${summary.lastAuditedAt}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        if (summary.leakedTokensDetail.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = CrimsonContainer,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, CrimsonAlert.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("토큰 노출 패턴 감지 내역", style = MaterialTheme.typography.labelLarge, color = CrimsonAlert)
                    }
                    summary.leakedTokensDetail.take(4).forEach { leak ->
                        Text("• $leak", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Channel / Account Security Grade Bars
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "블로그 계정 · 인프라별 보안 등급 분포",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary
            )
            summary.accountGrades.forEach { acc ->
                val barColor = acc.grade.accentColor()
                val fill = (acc.score / 100f).coerceIn(0.05f, 1f)
                Surface(
                    color = NavySurfaceElevated,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${acc.channelName} (${acc.grade.name} · ${acc.score}점)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = barColor
                                )
                                Text(
                                    text = acc.targetAddress,
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            StatusChip(
                                text = if (acc.zeroPasswordCompliant) "Zero-PW" else "PW소거필요",
                                textColor = if (acc.zeroPasswordCompliant) MintSecondary else CrimsonAlert,
                                backgroundColor = if (acc.zeroPasswordCompliant) EmeraldContainer else CrimsonContainer
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(NavyBackground)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fill)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(barColor)
                            )
                        }
                        Text(
                            text = "${acc.sessionStatus} · ${acc.findings.joinToString(" / ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onRefreshAudit,
                enabled = !isBusy,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_refresh_security_audit")
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("보안 재점검", maxLines = 1)
            }
            if (summary.legacyPlaintextPwDetected) {
                Button(
                    onClick = onPurgePlaintextPw,
                    enabled = !isBusy,
                    modifier = Modifier
                        .weight(1.25f)
                        .height(48.dp)
                        .testTag("btn_purge_plaintext_pw"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonAlert,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("평문 비번 즉시 소거", maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun TokenHygieneCard(
    summary: SecurityDashboardSummary,
    modifier: Modifier = Modifier
) {
    StudioCard(
        borderColor = CloudflareSky.copy(alpha = 0.45f),
        modifier = modifier.testTag("token_hygiene_card")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Key, contentDescription = "API 토큰 관리", tint = CloudflareSky)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("API 토큰 외부노출 차단 관리", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text("토큰은 단말 DataStore에만 격리 보관되며 Git·원고 본문에 절대 삽입되지 않습니다.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        summary.tokenItems.forEach { item ->
            Surface(
                color = NavyBackground,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, if (item.leakCount > 0) CrimsonAlert else NavyBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.keyName,
                            style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                            color = CloudflareSky
                        )
                        StatusChip(
                            text = item.maskedPreview,
                            textColor = if (item.leakCount > 0) CrimsonAlert else if (item.isConfigured) MintSecondary else TextMuted,
                            backgroundColor = if (item.leakCount > 0) CrimsonContainer else if (item.isConfigured) EmeraldContainer else NavySurfaceElevated
                        )
                    }
                    Text(item.label, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    Text(item.statusNote, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun NaverTistoryLoginManagementCard(
    config: ServerAutomationConfig,
    naverProbe: ChannelProbeResult?,
    tistoryProbe: ChannelProbeResult?,
    isBusy: Boolean,
    onProbeConnections: () -> Unit,
    onResolveHandoff: () -> Unit,
    modifier: Modifier = Modifier
) {
    StudioCard(
        borderColor = NaverGreen.copy(alpha = 0.5f),
        modifier = modifier.testTag("naver_tistory_login_mgmt_card")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Lock, contentDescription = "계정·주소 로그인 관리", tint = MintSecondary)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("네이버·티스토리 비번 미저장 로그인 & 주소 관리", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text(
                    text = "포털 비밀번호를 앱에 저장하지 않고 워커 릴레이·세션 핸드오프·URL 검증으로 안전하게 연동합니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1. Naver guidance & probe
        Surface(
            color = NavySurfaceElevated,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, NaverGreen.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("① 네이버 블로그 로그인·세션 관리 방법", style = MaterialTheme.typography.labelLarge, color = NaverGreen)
                Text(
                    text = "• 아래 입력란에 블로그 ID(예: my_auction_blog)와 네이버 워커 릴레이 URL(또는 딱찰 CRAWLER_SYNC_TOKEN)만 저장하세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
                Text(
                    text = "• 2단계 인증/캡차 발생 시 비밀번호 입력 대신 외부 브라우저 로그인으로 발급된 storage_state 쿠키를 릴레이 서버에서 사용합니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "현재 대상: ${if (config.naverBlogId.isNotBlank()) "https://blog.naver.com/${config.naverBlogId}" else "블로그 ID 미입력"} · 상태: ${config.naverSessionStatus}",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                    color = MintSecondary
                )
                naverProbe?.let { p ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (p.reachableOk) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (p.reachableOk) MintSecondary else TistoryAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "[${p.checkedAt}] ${p.detailMessage}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (p.reachableOk) MintSecondary else TistoryAmber
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Tistory guidance & probe
        Surface(
            color = NavySurfaceElevated,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, TistoryAmber.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("② 티스토리 블로그 주소·카카오 로그인 관리 방법", style = MaterialTheme.typography.labelLarge, color = TistoryAmber)
                Text(
                    text = "• TISTORY_BLOG_URL에 본인 블로그 주소(예: https://내블로그.tistory.com 또는 2차 도메인)를 입력하면 HTTPS 주소로 자동 정규화됩니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
                Text(
                    text = "• 카카오계정 2차 인증을 유지한 채 서버 Playwright storage_state 또는 릴레이로 발행하고 아래 검증 버튼으로 HTTP 200 도달을 확인합니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "현재 주소: ${config.tistoryBlogUrl.ifBlank { "미입력" }} · 카카오ID: ${config.tistoryId.ifBlank { "세션토큰 모드" }}",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                    color = TistoryAmber
                )
                tistoryProbe?.let { p ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (p.reachableOk) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (p.reachableOk) MintSecondary else CrimsonAlert,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "[${p.checkedAt}] ${p.detailMessage}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (p.reachableOk) MintSecondary else CrimsonAlert
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onProbeConnections,
                enabled = !isBusy,
                modifier = Modifier
                    .weight(1.3f)
                    .height(48.dp)
                    .testTag("btn_probe_naver_tistory"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CloudflareContainer,
                    contentColor = CloudflareSky
                )
            ) {
                Icon(Icons.Filled.Http, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("네이버·티스토리 주소·릴레이 점검", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(
                onClick = onResolveHandoff,
                enabled = !isBusy,
                modifier = Modifier
                    .weight(0.85f)
                    .height(48.dp)
                    .testTag("btn_security_resolve_handoff")
            ) {
                Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("세션복구", maxLines = 1)
            }
        }
    }
}
