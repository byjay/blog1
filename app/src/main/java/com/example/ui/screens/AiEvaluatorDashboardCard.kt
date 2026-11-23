package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AiAutoFixSuggestion
import com.example.model.AiEvalUiState
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
import com.example.ui.theme.NavyContainerHighest
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TistoryAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiSeoGeoEvaluatorDashboardCard(
    selectedCaseId: String?,
    aiEvalState: AiEvalUiState,
    isBusy: Boolean,
    onRunAiEvalSelected: () -> Unit,
    onTestBusanProfitableLand: () -> Unit,
    onTestLowScoreDraft: () -> Unit,
    onApplyAiAutoFixes: (String) -> Unit
) {
    var showCriteriaList by remember { mutableStateOf(false) }

    StudioCard(
        borderColor = CloudflareSky.copy(alpha = 0.65f),
        modifier = Modifier.testTag("ai_seo_geo_dashboard_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Psychology, contentDescription = "AI 30항목 SEO/GEO 채점", tint = CloudflareSky)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "AI 30개 SEO/GEO 자동 채점 · 95점 미만 수정 제안",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "Firebase AI (Gemini 3.8 Flash) 실시간 검수 + 부산 5천만원 미만 수익성 토지 테스트",
                        style = MaterialTheme.typography.bodySmall,
                        color = CloudflareSky
                    )
                }
            }
            StatusChip(text = "30항목·95+", textColor = MintSecondary, backgroundColor = EmeraldContainer)
        }

        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onTestBusanProfitableLand,
            enabled = !isBusy && aiEvalState !is AiEvalUiState.Evaluating,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_test_busan_land_ai"),
            colors = ButtonDefaults.buttonColors(containerColor = TistoryAmber, contentColor = NavyBackground)
        ) {
            Icon(Icons.Filled.Landscape, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "부산 5천만 미만 수익성 토지(2025타경101915) 생성·AI평가",
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
            Button(
                onClick = onRunAiEvalSelected,
                enabled = !isBusy && selectedCaseId != null && aiEvalState !is AiEvalUiState.Evaluating,
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
                    .testTag("btn_run_ai_30_eval"),
                colors = ButtonDefaults.buttonColors(containerColor = CloudflareSky, contentColor = NavyBackground)
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("현재 글 AI 채점", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(
                onClick = onTestLowScoreDraft,
                enabled = !isBusy && aiEvalState !is AiEvalUiState.Evaluating,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_test_low_score_fix"),
                border = BorderStroke(1.dp, CrimsonAlert.copy(alpha = 0.6f))
            ) {
                Text("95점 미만 테스트", color = CrimsonAlert, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        when (aiEvalState) {
            is AiEvalUiState.Idle -> {
                Text(
                    text = "위 버튼을 누르면 글 작성 완료 후 30개 SEO/GEO 기준을 AI가 채점하고, 95점 미만 시 수정 제안을 즉시 표시합니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            is AiEvalUiState.Evaluating -> {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${aiEvalState.caseId} 원고 · 법원 현장사진·등기부 기록 30항목 AI 채점 중...",
                        style = MaterialTheme.typography.bodySmall,
                        color = CloudflareSky
                    )
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = CloudflareSky,
                        trackColor = NavyContainerHighest
                    )
                }
            }

            is AiEvalUiState.Success -> {
                val report = aiEvalState.report
                val passed95 = report.totalScore >= 95
                val accent = if (passed95) MintSecondary else CrimsonAlert
                Surface(
                    color = NavySurfaceElevated,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${report.caseId} · ${report.overallVerdict}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "모델 ${report.modelName} · 통과 ${report.passedCount}/30 항목 · ${report.evaluatedAt}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                            StatusChip(
                                text = "${report.totalScore}/100점",
                                textColor = accent,
                                backgroundColor = if (passed95) EmeraldContainer else CrimsonContainer
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (report.totalScore / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = accent,
                            trackColor = NavyContainerHighest
                        )

                        Text(report.aiSummary, style = MaterialTheme.typography.bodySmall, color = TextPrimary)

                        if (report.autoFixSuggestions.isNotEmpty()) {
                            AutoFixSuggestionsSection(
                                caseId = report.caseId,
                                needsFix95 = !passed95,
                                suggestions = report.autoFixSuggestions,
                                isBusy = isBusy,
                                onApplyFix = { onApplyAiAutoFixes(report.caseId) }
                            )
                        }

                        OutlinedButton(
                            onClick = { showCriteriaList = !showCriteriaList },
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(if (showCriteriaList) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showCriteriaList) "30개 SEO/GEO 채점 항목 닫기" else "30개 SEO/GEO 채점 항목 전체 보기 (${report.passedCount}/30)")
                        }

                        AnimatedVisibility(visible = showCriteriaList) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                report.criteriaResults.forEach { item ->
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(
                                            imageVector = if (item.passed) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
                                            contentDescription = null,
                                            tint = if (item.passed) EmeraldPrimary else CrimsonAlert,
                                            modifier = Modifier.size(14.dp).padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${item.orderIndex}. ${item.title} (${item.earnedPoints}/${item.maxPoints}) — ${item.detail}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (item.passed) TextSecondary else CrimsonAlert
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            is AiEvalUiState.Error -> {
                Surface(
                    color = CrimsonContainer.copy(alpha = 0.45f),
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, CrimsonAlert),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Filled.Warning, contentDescription = "AI 채점 오류", tint = CrimsonAlert, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${aiEvalState.caseId} AI 원격 호출 오류 (로컬 30항목 기준 ${aiEvalState.baselineScore}점)", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            }
                            StatusChip(
                                text = "${aiEvalState.baselineScore}/100",
                                textColor = if (aiEvalState.baselineScore >= 95) MintSecondary else CrimsonAlert,
                                backgroundColor = NavyBackground
                            )
                        }
                        Text("원인: ${aiEvalState.message}", style = MaterialTheme.typography.bodySmall, color = CrimsonAlert)
                        if (aiEvalState.baselineSuggestions.isNotEmpty()) {
                            AutoFixSuggestionsSection(
                                caseId = aiEvalState.caseId,
                                needsFix95 = aiEvalState.baselineScore < 95,
                                suggestions = aiEvalState.baselineSuggestions,
                                isBusy = isBusy,
                                onApplyFix = { onApplyAiAutoFixes(aiEvalState.caseId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoFixSuggestionsSection(
    caseId: String,
    needsFix95: Boolean,
    suggestions: List<AiAutoFixSuggestion>,
    isBusy: Boolean,
    onApplyFix: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = if (needsFix95) "95점 미만 자동 수정 제안 (${suggestions.size}건)" else "추가 품질 고도화 제안 (${suggestions.size}건)",
            style = MaterialTheme.typography.labelLarge,
            color = if (needsFix95) CrimsonAlert else TistoryAmber
        )
        suggestions.forEach { fix ->
            Surface(
                color = NavyBackground,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, if (needsFix95) CrimsonAlert.copy(alpha = 0.55f) else NavyBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "[#${fix.criterionOrder}] ${fix.issueTitle}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (needsFix95) CrimsonAlert else TistoryAmber,
                            modifier = Modifier.weight(1f)
                        )
                        StatusChip(
                            text = fix.targetChannel.displayName,
                            textColor = CloudflareSky,
                            backgroundColor = CloudflareContainer
                        )
                    }
                    Text(fix.suggestedFix, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    if (fix.improvedSnippet.isNotBlank()) {
                        Text(
                            text = "적용 예시: ${fix.improvedSnippet}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                            color = MintSecondary
                        )
                    }
                }
            }
        }

        Button(
            onClick = onApplyFix,
            enabled = !isBusy,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_apply_ai_auto_fix"),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("$caseId 수정 제안 자동 적용 → 95점+ 재채점", fontWeight = FontWeight.Bold)
        }
    }
}
