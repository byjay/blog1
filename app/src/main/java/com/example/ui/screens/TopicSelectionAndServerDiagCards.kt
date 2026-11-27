package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.DdakchalFullDiagnosticReport
import com.example.model.NaverAutoWriteMode
import com.example.model.NaverPublishStatusRecord
import com.example.model.NaverWriteMethodGuide
import com.example.model.TopicCandidateRanking
import com.example.model.TopicSelectionBoardState
import com.example.model.TopicSortCriteria
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
import com.example.ui.util.SafeClipboardHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopicSelectionAdvisorCard(
    board: TopicSelectionBoardState,
    isBusy: Boolean,
    onSelectSortCriteria: (TopicSortCriteria) -> Unit,
    onRankTopics: () -> Unit,
    onApplyTopTopics: () -> Unit,
    onSelectAndAutoRegister: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    StudioCard(
        borderColor = EmeraldPrimary.copy(alpha = 0.5f),
        modifier = modifier.testTag("card_topic_selection_4pillar")
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
                Icon(Icons.Filled.AutoAwesome, contentDescription = "4대 기준 글감 선정", tint = MintSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "4대 기준 글감 자동 선정",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "글 품질 · SEO 적합성 · AI 노출(GEO) · 인기 매물 종합 랭킹",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            if (board.lastEvaluatedAt.isNotBlank()) {
                StatusChip(
                    text = "${board.candidates.size}건",
                    textColor = MintSecondary,
                    backgroundColor = EmeraldContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TopicSortCriteria.entries.forEach { criteria ->
                FilterChip(
                    selected = board.sortMode == criteria,
                    onClick = { onSelectSortCriteria(criteria) },
                    label = { Text(criteria.label, style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("chip_topic_sort_${criteria.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onRankTopics,
                enabled = !isBusy,
                modifier = Modifier
                    .weight(1.1f)
                    .height(48.dp)
                    .testTag("btn_rank_topics_4pillar"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = NavyBackground
                )
            ) {
                Icon(Icons.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("4대 기준 선정 실행", fontWeight = FontWeight.Bold, maxLines = 1)
            }
            OutlinedButton(
                onClick = onApplyTopTopics,
                enabled = !isBusy && board.candidates.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_apply_top3_topics")
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("상위 3건 우선 반영", maxLines = 1)
            }
        }

        if (board.summaryBanner.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = board.summaryBanner,
                style = MaterialTheme.typography.labelSmall,
                color = CloudflareSky
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        if (board.candidates.isEmpty()) {
            Text(
                text = "선정 가능한 경매 슬롯이 없습니다. 딱찰 피드 동기화 후 다시 실행하세요.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                board.candidates.take(4).forEach { candidate ->
                    TopicCandidateRow(
                        candidate = candidate,
                        isBusy = isBusy,
                        onSelectAndAutoRegister = { onSelectAndAutoRegister(candidate.postId) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopicCandidateRow(
    candidate: TopicCandidateRanking,
    isBusy: Boolean,
    onSelectAndAutoRegister: () -> Unit
) {
    val accent = when {
        candidate.totalTopicScore >= 88 -> EmeraldPrimary
        candidate.totalTopicScore >= 76 -> CloudflareSky
        else -> TistoryAmber
    }
    Surface(
        color = NavyBackground,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("topic_candidate_row_${candidate.rank}")
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusChip(
                            text = "#${candidate.rank} ${candidate.tierLabel}",
                            textColor = accent,
                            backgroundColor = NavySurfaceElevated
                        )
                        if (candidate.isFromLiveServer) {
                            StatusChip(
                                text = "딱찰 실서버",
                                textColor = MintSecondary,
                                backgroundColor = EmeraldContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${candidate.caseId} · ${candidate.propertyTypeLabel}",
                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
                        color = TextPrimary
                    )
                    Text(
                        text = candidate.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${candidate.totalTopicScore}점",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = accent
                    )
                    Text("종합 선정", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ScoreBarMini("글품질", candidate.qualityScore, EmeraldPrimary, Modifier.weight(1f))
                ScoreBarMini("SEO", candidate.seoFitScore, NaverGreen, Modifier.weight(1f))
                ScoreBarMini("AI노출", candidate.aiExposureScore, CloudflareSky, Modifier.weight(1f))
                ScoreBarMini("인기", candidate.popularityScore, TistoryAmber, Modifier.weight(1f))
            }

            Text(
                text = "선정 사유: ${candidate.selectionReasonSummary}",
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )
            Text(
                text = "추천 앵글: ${candidate.recommendedAngle}",
                style = MaterialTheme.typography.labelSmall,
                color = MintSecondary
            )
            if (candidate.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    candidate.tags.take(4).forEach { tag ->
                        StatusChip(
                            text = tag,
                            textColor = TextSecondary,
                            backgroundColor = NavySurfaceElevated
                        )
                    }
                }
            }

            Button(
                onClick = onSelectAndAutoRegister,
                enabled = !isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_register_topic_${candidate.rank}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CloudflareContainer,
                    contentColor = CloudflareSky
                )
            ) {
                Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("이 글감 바로등록 (95+ 교정·발행)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ScoreBarMini(
    label: String,
    score: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val pct = (score.coerceIn(0, 100) / 100f)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(
                text = "$score",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                color = color
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(NavySurfaceElevated)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct.coerceAtLeast(0.08f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(999.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun DdakchalPerfectDiagnosticCard(
    report: DdakchalFullDiagnosticReport?,
    isBusy: Boolean,
    onRunFullDiagnostic: () -> Unit,
    modifier: Modifier = Modifier
) {
    StudioCard(
        borderColor = CloudflareSky.copy(alpha = 0.55f),
        modifier = modifier.testTag("card_ddakchal_perfect_diagnostic")
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
                Icon(Icons.Filled.NetworkCheck, contentDescription = "딱찰 서버 정밀 점검", tint = CloudflareSky)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("딱찰(DDAKCHAL) 서버 연동 정밀 점검", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Text(
                        text = "트리거 큐 · 분석피드 · 신디케이션 · 단건자산 · 역동기화 · CF D1 6종 검증",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            if (report != null) {
                StatusChip(
                    text = "${report.overallScorePct}%",
                    textColor = if (report.overallConnected) MintSecondary else TistoryAmber,
                    backgroundColor = if (report.overallConnected) EmeraldContainer else AmberContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onRunFullDiagnostic,
            enabled = !isBusy,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_run_perfect_ddakchal_diag"),
            colors = ButtonDefaults.buttonColors(
                containerColor = CloudflareContainer,
                contentColor = CloudflareSky
            )
        ) {
            Icon(Icons.Filled.CloudDone, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("딱찰 서버 완벽 점검 실행", fontWeight = FontWeight.Bold)
        }

        if (report != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = NavyBackground,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, if (report.overallConnected) EmeraldPrimary.copy(alpha = 0.55f) else TistoryAmber),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = report.healthGradeLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (report.overallConnected) MintSecondary else TistoryAmber
                        )
                        Text(
                            text = report.checkedAt,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                            color = TextMuted
                        )
                    }
                    Text(
                        text = "API Base: ${report.apiBase} · 인증헤더 ${if (report.authHeaderReady) "장착됨" else "미입력"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                        color = TextSecondary
                    )
                    Text(
                        text = "진단 권고: ${report.recommendation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                report.checks.forEach { chk ->
                    val statusColor = when {
                        chk.passed -> MintSecondary
                        chk.optional -> TistoryAmber
                        else -> CrimsonAlert
                    }
                    Surface(
                        color = NavySurfaceElevated,
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = when {
                                        chk.passed -> Icons.Filled.CheckCircle
                                        chk.optional -> Icons.Filled.Warning
                                        else -> Icons.Filled.ErrorOutline
                                    },
                                    contentDescription = chk.name,
                                    tint = statusColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${chk.name} (${chk.method})",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = chk.endpointPath,
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                        color = CloudflareSky,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = chk.detailMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (chk.httpStatusCode > 0) "HTTP ${chk.httpStatusCode}" else "규격체크",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                    color = statusColor
                                )
                                if (chk.latencyMs > 0) {
                                    Text(
                                        text = "${chk.latencyMs}ms",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                        color = TextMuted
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

@Composable
fun NaverPublishStatusAndMethodsCard(
    publishStatuses: List<NaverPublishStatusRecord>,
    writeGuides: List<NaverWriteMethodGuide>,
    activeMode: NaverAutoWriteMode,
    isBusy: Boolean,
    onRefreshPublishStatuses: () -> Unit,
    onSelectWriteMode: (NaverAutoWriteMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val composeClipboard = LocalClipboardManager.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Live Naver Publish Status Tracker
        StudioCard(
            borderColor = NaverGreen.copy(alpha = 0.55f),
            modifier = Modifier.testTag("card_naver_publish_status")
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
                    Icon(Icons.Filled.FactCheck, contentDescription = "네이버 글등록 상태", tint = NaverGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("네이버 블로그 글 등록 실시간 상태", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text(
                            text = "사건별 발행 여부 · 사용 방식 · 등록 URL 실존 HTTP 응답 확인",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                IconButton(
                    onClick = onRefreshPublishStatuses,
                    enabled = !isBusy,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_refresh_naver_publish_status")
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "네이버 등록 상태 새로고침", tint = NaverGreen)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRefreshPublishStatuses,
                enabled = !isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_verify_naver_live_urls"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldContainer,
                    contentColor = MintSecondary
                )
            ) {
                Icon(Icons.Filled.Http, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("네이버 등록 글 실URL 재확인 (${publishStatuses.size}건)")
            }

            Spacer(modifier = Modifier.height(10.dp))
            if (publishStatuses.isEmpty()) {
                Text(
                    text = "추적 중인 네이버 글 슬롯이 없습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    publishStatuses.take(6).forEach { rec ->
                        val statusColor = when {
                            rec.isRegisteredOk -> MintSecondary
                            rec.statusLabel.contains("실패") || rec.statusLabel.contains("중단") -> CrimsonAlert
                            else -> TistoryAmber
                        }
                        Surface(
                            color = NavyBackground,
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${rec.caseId} · ${rec.methodUsed}",
                                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                                        color = TextPrimary
                                    )
                                    StatusChip(
                                        text = rec.statusLabel,
                                        textColor = statusColor,
                                        backgroundColor = NavySurfaceElevated
                                    )
                                }
                                Text(
                                    text = rec.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${rec.detailNote} · ${rec.updatedAt}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                if (rec.publishedUrl.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = rec.publishedUrl,
                                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                            color = CloudflareSky,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                SafeClipboardHelper.copyPlainTextSafely(
                                                    context = context,
                                                    label = "네이버 글 주소",
                                                    text = rec.publishedUrl,
                                                    composeClipboard = composeClipboard
                                                )
                                            },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(Icons.Filled.ContentCopy, contentDescription = "네이버 URL 복사", tint = MintSecondary, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = {
                                                runCatching {
                                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(rec.publishedUrl)))
                                                }
                                            },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "네이버 글 열기", tint = NaverGreen, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. 4 Alternative Automated Naver Write Methods Guide & Switcher
        StudioCard(
            borderColor = CloudflareSky.copy(alpha = 0.5f),
            modifier = Modifier.testTag("card_naver_write_methods")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.EditNote, contentDescription = "네이버 자동 글쓰기 대안", tint = NaverGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("다른 자동 네이버 블로그 글쓰기 방식 4가지", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Text(
                        text = "네이버 공식 Write Open API는 2020년 종료되어 아래 4가지 대안 중 선택해 자동 발행합니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                writeGuides.forEach { guide ->
                    val isSelected = guide.mode == activeMode || guide.isActive
                    val borderCol = if (isSelected) NaverGreen else NavyBorder
                    Surface(
                        color = if (isSelected) EmeraldContainer.copy(alpha = 0.35f) else NavyBackground,
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderCol),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { onSelectWriteMode(guide.mode) }
                            .testTag("naver_write_method_${guide.mode.name}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = guide.mode.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) MintSecondary else TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                StatusChip(
                                    text = if (isSelected) "사용중 · ${guide.readinessLabel}" else guide.mode.shortBadge,
                                    textColor = if (isSelected) MintSecondary else CloudflareSky,
                                    backgroundColor = NavySurfaceElevated
                                )
                            }
                            Text(
                                text = guide.mode.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = guide.mode.prosCons,
                                style = MaterialTheme.typography.labelSmall,
                                color = TistoryAmber
                            )
                            guide.setupSteps.forEachIndexed { idx, step ->
                                Text(
                                    text = "${idx + 1}. $step",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onSelectWriteMode(guide.mode) },
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Text(if (isSelected) "현재 선택됨" else "이 방식으로 전환", maxLines = 1)
                                }
                                if (guide.quickActionUrl.startsWith("http")) {
                                    OutlinedButton(
                                        onClick = {
                                            runCatching {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(guide.quickActionUrl)))
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("연동창 열기", maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
