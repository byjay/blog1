package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ArticleEvaluationResult
import com.example.model.ChannelPlatform
import com.example.model.CourtFullArchiveRecord
import com.example.model.EvaluationPillar
import com.example.model.GeneratedPostSlot
import com.example.model.LegalDoctrineReport
import com.example.ui.StudioUiState
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.CloudflareSky
import com.example.ui.theme.CrimsonAlert
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

@Composable
fun ArticleVerifyAndEditTab(
    slot: GeneratedPostSlot,
    uiState: StudioUiState,
    evalResult: ArticleEvaluationResult,
    courtArchive: CourtFullArchiveRecord,
    legalReport: LegalDoctrineReport?,
    onSelectChannel: (ChannelPlatform) -> Unit,
    onRunVerify: (postId: String) -> Unit,
    onAutoFixWithoutPublish: (postId: String) -> Unit,
    onSaveManualEdit: (
        postId: String,
        platform: ChannelPlatform,
        title: String,
        bodyText: String,
        pinnedComment: String
    ) -> Unit,
    onGoToPreviewTab: () -> Unit,
    onGoToRegisterTab: () -> Unit
) {
    val selectedChannel = uiState.selectedChannel
    val activeVariant = slot.variants[selectedChannel]
        ?: slot.variants[ChannelPlatform.CLOUDFLARE]
        ?: slot.variants.values.first()

    var showOnlyFailed by remember { mutableStateOf(false) }
    var showEditor by remember(slot.postId) { mutableStateOf(true) }
    var draftTitle by remember(slot.postId, activeVariant.platform) { mutableStateOf(activeVariant.title) }
    var draftBody by remember(slot.postId, activeVariant.platform) { mutableStateOf(activeVariant.bodyText) }
    var draftPinned by remember(slot.postId, activeVariant.platform) { mutableStateOf(activeVariant.pinnedCommentSnippet) }

    LaunchedEffect(activeVariant.title, activeVariant.bodyText, activeVariant.pinnedCommentSnippet) {
        draftTitle = activeVariant.title
        draftBody = activeVariant.bodyText
        draftPinned = activeVariant.pinnedCommentSnippet
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        EasyMenuGuideStrip(
            icon = Icons.Filled.Verified,
            easyGuide = "생성된 글을 30항목·법리기준으로 검증한 뒤, 자동 보완하거나 제목·본문을 직접 수정해 저장하고 등록 단계로 넘깁니다.",
            accentColor = EmeraldPrimary
        )

        // Score & Verification Summary
        Surface(
            color = if (evalResult.eligibleForRegistration) EmeraldContainer else AmberContainer,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(
                1.dp,
                if (evalResult.eligibleForRegistration) EmeraldPrimary else TistoryAmber
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (evalResult.eligibleForRegistration) "등록 합격선(95+) 통과" else "품질 수정 권장 (목표 95점+)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "${evalResult.totalScore}/100점",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                        color = if (evalResult.eligibleForRegistration) MintSecondary else TistoryAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
                LinearProgressIndicator(
                    progress = { (evalResult.totalScore / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (evalResult.eligibleForRegistration) EmeraldPrimary else TistoryAmber,
                    trackColor = NavyContainerHighest
                )
                val legalLabel = when {
                    legalReport == null -> "검증 대기"
                    legalReport.overallPassed -> "법리 통과"
                    else -> "주의 ${legalReport.criticalBlockCount + legalReport.cautionCount}건"
                }
                Text(
                    text = "30항목 통과 ${evalResult.passedCount}/${evalResult.totalCriteriaCount}개 · 법리검증: $legalLabel · 사진 ${courtArchive.photos.size}컷 · 등기 ${courtArchive.registryGapGu.size + courtArchive.registryEulGu.size}건",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Verify + AutoFix Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onRunVerify(slot.postId) },
                enabled = !uiState.isBusy,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_modal_run_verify"),
                border = BorderStroke(1.dp, CloudflareSky.copy(alpha = 0.7f))
            ) {
                Icon(Icons.Filled.Verified, contentDescription = null, tint = CloudflareSky, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("다시 검증", color = CloudflareSky, maxLines = 1)
            }
            Button(
                onClick = { onAutoFixWithoutPublish(slot.postId) },
                enabled = !uiState.isBusy,
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
                    .testTag("btn_modal_autofix_only"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
            ) {
                Icon(Icons.Filled.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("95점+ 자동 보완", fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }

        // Direct Article Revision Editor (제목·본문·고정댓글 수정 후 재검증)
        Surface(
            color = NavySurfaceElevated,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, CloudflareSky.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_manual_edit_article")
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Edit, contentDescription = "원고 직접 수정", tint = CloudflareSky, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "검증 후 원고 직접 수정 (${activeVariant.platform.displayName})",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                    }
                    TextButton(
                        onClick = { showEditor = !showEditor },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            imageVector = if (showEditor) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = "수정창 토글"
                        )
                        Text(if (showEditor) "접기" else "수정열기")
                    }
                }

                AnimatedVisibility(visible = showEditor) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ChannelPlatform.entries.forEach { platform ->
                                FilterChip(
                                    selected = selectedChannel == platform,
                                    onClick = { onSelectChannel(platform) },
                                    label = { Text(platform.displayName) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = draftTitle,
                            onValueChange = { draftTitle = it },
                            label = { Text("글 제목 (${activeVariant.platform.displayName})") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_edit_article_title")
                        )

                        OutlinedTextField(
                            value = draftBody,
                            onValueChange = { draftBody = it },
                            label = { Text("본문 내용 (${draftBody.length}자)") },
                            minLines = 5,
                            maxLines = 10,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_edit_article_body")
                        )

                        OutlinedTextField(
                            value = draftPinned,
                            onValueChange = { draftPinned = it },
                            label = { Text("고정댓글 · 프로 전환 CTA") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_edit_article_pinned")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onSaveManualEdit(
                                        slot.postId,
                                        activeVariant.platform,
                                        draftTitle,
                                        draftBody,
                                        draftPinned
                                    )
                                },
                                enabled = !uiState.isBusy && draftTitle.isNotBlank() && draftBody.isNotBlank(),
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(48.dp)
                                    .testTag("btn_save_edited_article"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CloudflareSky,
                                    contentColor = NavyBackground
                                )
                            ) {
                                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("수정 저장·재검증", fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = {
                                    onSaveManualEdit(
                                        slot.postId,
                                        activeVariant.platform,
                                        draftTitle,
                                        draftBody,
                                        draftPinned
                                    )
                                    onGoToPreviewTab()
                                },
                                enabled = !uiState.isBusy && draftTitle.isNotBlank() && draftBody.isNotBlank(),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_save_and_preview")
                            ) {
                                Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("수정 후 미리보기", maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // Next Step Primary CTA -> Register Tab
        Button(
            onClick = onGoToRegisterTab,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_go_to_register_step"),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("검증·수정 완료 → 블로그 등록 단계로", fontWeight = FontWeight.Bold)
        }

        // 5 Pillar Breakdown
        EvaluationPillar.entries.forEach { pillar ->
            val pillarItems = evalResult.items.filter { it.pillar == pillar }
            val earned = pillarItems.sumOf { it.earnedPoints }
            val maxPts = pillarItems.sumOf { it.maxPoints }.coerceAtLeast(1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${pillar.label} (만점 ${maxPts}점)", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                Text("$earned / $maxPts", style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily), color = MintSecondary)
            }
            LinearProgressIndicator(
                progress = { (earned.toFloat() / maxPts.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = CloudflareSky,
                trackColor = NavyContainerHighest
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !showOnlyFailed,
                onClick = { showOnlyFailed = false },
                label = { Text("전체 30항목") }
            )
            FilterChip(
                selected = showOnlyFailed,
                onClick = { showOnlyFailed = true },
                label = { Text("미충족 항목만 (${evalResult.items.count { !it.passed }})") }
            )
        }

        val shownItems = if (showOnlyFailed) evalResult.items.filter { !it.passed } else evalResult.items
        shownItems.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = if (item.passed) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    tint = if (item.passed) EmeraldPrimary else CrimsonAlert,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${item.orderIndex}. ${item.title} (${item.earnedPoints}/${item.maxPoints}점)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        item.detail,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.passed) TextMuted else CrimsonAlert
                    )
                }
            }
        }
    }
}
