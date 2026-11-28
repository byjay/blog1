package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AiCaseSearchUiState
import com.example.model.ChannelPlatform
import com.example.model.ExposurePotentialGrade
import com.example.model.GeneratedPostSlot
import com.example.model.HtmlBatchBundleState
import com.example.model.NaverKeywordDiagnosisUiState
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
import com.example.ui.util.SafeClipboardHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiCaseSearchAndTestRegisterCard(
    slots: List<GeneratedPostSlot>,
    aiCaseSearchState: AiCaseSearchUiState,
    isBusy: Boolean,
    onSearchAndOptimizeCase: (String) -> Unit,
    onTestRegisterSearchedCase: () -> Unit,
    onGenerateThreeHtml: () -> Unit,
    naverKeywordDiagnosis: NaverKeywordDiagnosisUiState = NaverKeywordDiagnosisUiState.Idle,
    onDiagnoseNaverKeyword: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val defaultQuery = slots.firstOrNull()?.auctionCase?.caseId.orEmpty()
    var query by rememberSaveable { mutableStateOf(defaultQuery) }

    StudioCard(
        borderColor = CloudflareSky.copy(alpha = 0.55f),
        modifier = modifier.testTag("ai_case_search_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = "AI 사건검색 노출 최적화", tint = CloudflareSky)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "AI 사건검색 노출(GEO/AEO) · 테스트 등록",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "사건 검색 시 AI 브리핑·네이버 Cue·구글 검색에 인용되도록 FAQ·JSON-LD 생성 후 즉시 테스트 발행합니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("조회할 법원 사건번호 또는 지역·물건 입력") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_ai_case_search")
        )

        if (slots.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                slots.take(4).forEach { s ->
                    AssistChip(
                        onClick = { query = s.auctionCase.caseId },
                        label = {
                            Text(
                                text = "${s.auctionCase.caseId} (${s.auctionCase.propertyType.displayLabel})",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onSearchAndOptimizeCase(query) },
                enabled = !isBusy && aiCaseSearchState !is AiCaseSearchUiState.Searching,
                modifier = Modifier
                    .weight(1.25f)
                    .height(48.dp)
                    .testTag("btn_ai_case_search"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CloudflareContainer,
                    contentColor = CloudflareSky
                )
            ) {
                if (aiCaseSearchState is AiCaseSearchUiState.Searching) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = CloudflareSky)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI 검색·구조화중...")
                } else {
                    Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI 사건검색·노출 최적화", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            OutlinedButton(
                onClick = onGenerateThreeHtml,
                enabled = !isBusy,
                modifier = Modifier
                    .weight(0.95f)
                    .height(48.dp)
                    .testTag("btn_generate_3_html_from_search"),
                border = BorderStroke(1.dp, MintSecondary.copy(alpha = 0.65f))
            ) {
                Icon(Icons.Filled.Code, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("HTML 3개 생성", color = MintSecondary, maxLines = 1)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { onDiagnoseNaverKeyword(query) },
            enabled = !isBusy && naverKeywordDiagnosis !is NaverKeywordDiagnosisUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_diagnose_naver_keyword"),
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.65f))
        ) {
            if (naverKeywordDiagnosis is NaverKeywordDiagnosisUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MintSecondary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("네이버 검색 API 노출 진단중...", color = MintSecondary)
            } else {
                Icon(Icons.Filled.Search, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("네이버 블로그 노출 가능성 진단 (.env API)", color = MintSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        when (naverKeywordDiagnosis) {
            is NaverKeywordDiagnosisUiState.Idle, is NaverKeywordDiagnosisUiState.Loading -> Unit
            is NaverKeywordDiagnosisUiState.Error -> {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = CrimsonContainer,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, CrimsonAlert.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = naverKeywordDiagnosis.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = CrimsonAlert,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
            is NaverKeywordDiagnosisUiState.Success -> {
                val r = naverKeywordDiagnosis.report
                val accent = when (r.grade) {
                    ExposurePotentialGrade.BLUE_OCEAN -> MintSecondary
                    ExposurePotentialGrade.COMPETITIVE_WINNABLE -> CloudflareSky
                    ExposurePotentialGrade.CROWDED_NICHE -> com.example.ui.theme.TistoryAmber
                    ExposurePotentialGrade.SATURATED -> CrimsonAlert
                }
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = NavyBackground,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth().testTag("naver_keyword_diagnosis_result")
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "네이버 노출 진단 · '${r.keyword}'",
                                style = MaterialTheme.typography.labelLarge,
                                color = accent,
                                modifier = Modifier.weight(1f)
                            )
                            StatusChip(
                                text = "${r.exposureScore}점 · ${r.grade.label}",
                                textColor = accent,
                                backgroundColor = NavySurfaceElevated
                            )
                        }
                        Text(
                            text = r.competitionSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                        Text(
                            text = "추천 제목: ${r.recommendedTitlePattern}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MintSecondary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            r.recommendedSubKeywords.forEach { kw ->
                                StatusChip(
                                    text = "#$kw",
                                    textColor = CloudflareSky,
                                    backgroundColor = CloudflareContainer
                                )
                            }
                        }
                        r.actionChecklist.take(3).forEach { tip ->
                            Text("• $tip", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }
                }
            }
        }

        when (aiCaseSearchState) {
            is AiCaseSearchUiState.Idle -> Unit
            is AiCaseSearchUiState.Searching -> Unit
            is AiCaseSearchUiState.Error -> {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = CrimsonContainer,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, CrimsonAlert.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = aiCaseSearchState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = CrimsonAlert,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
            is AiCaseSearchUiState.Success -> {
                val pack = aiCaseSearchState.geoPack
                val slot = aiCaseSearchState.matchedSlot
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = NavyBackground,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${slot.auctionCase.caseId} · AI 검색 노출 패키지",
                                style = MaterialTheme.typography.labelLarge,
                                color = MintSecondary
                            )
                            StatusChip(
                                text = "GEO ${pack.estimatedGeoScore}점",
                                textColor = MintSecondary,
                                backgroundColor = EmeraldContainer
                            )
                        }
                        Text(
                            text = pack.hookSnippet,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = pack.directAnswerParagraph,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            pack.aiKeywords.forEach { kw ->
                                StatusChip(
                                    text = "#$kw",
                                    textColor = CloudflareSky,
                                    backgroundColor = CloudflareContainer
                                )
                            }
                        }
                        pack.faqPairs.forEach { qa ->
                            Surface(
                                color = NavySurfaceElevated,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Q. ${qa.question}", style = MaterialTheme.typography.labelMedium, color = CloudflareSky)
                                    Text("A. ${qa.answer}", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                }
                            }
                        }
                        Text(
                            text = "생성 모델: ${pack.modelUsed} · FAQPage JSON-LD 3채널 자동 주입됨",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Button(
                            onClick = onTestRegisterSearchedCase,
                            enabled = !isBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_ai_test_register"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = NavyBackground
                            )
                        ) {
                            Icon(Icons.Filled.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("이 사건 글 테스트 등록 · HTTP 검증 실행", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThreeHtmlGeneratorDialog(
    bundle: HtmlBatchBundleState,
    onSwitchMode: (threeCasesMode: Boolean, channel: ChannelPlatform) -> Unit,
    onDismiss: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var selectedIdx by remember(bundle.items) { mutableStateOf(0) }
    var selectedChannel by remember { mutableStateOf(ChannelPlatform.NAVER) }
    var bodyOnlyMode by remember { mutableStateOf(false) }
    var copiedBanner by remember(bundle.generatedAt) { mutableStateOf<String?>(null) }
    var copyFailedBanner by remember(bundle.generatedAt) { mutableStateOf(false) }

    val activeItem = bundle.items.getOrNull(selectedIdx) ?: bundle.items.firstOrNull()
    val activeHtmlText = remember(activeItem, bodyOnlyMode) {
        if (activeItem == null) "" else if (bodyOnlyMode) {
            activeItem.bodyOnlyHtml.ifBlank { activeItem.pureHtml }
        } else {
            activeItem.pureHtml
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = {
            Column {
                Text("순수 HTML 3개 생성 완료", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${bundle.modeLabel} · ${bundle.generatedAt}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MintSecondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!copiedBanner.isNullOrBlank()) {
                    Surface(
                        color = if (copyFailedBanner) CrimsonContainer else EmeraldContainer,
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(
                            1.dp,
                            if (copyFailedBanner) CrimsonAlert.copy(alpha = 0.6f) else EmeraldPrimary.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("html_copy_status_banner")
                    ) {
                        Text(
                            text = copiedBanner!!,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (copyFailedBanner) CrimsonAlert else MintSecondary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = bundle.threeCasesMode,
                        onClick = { onSwitchMode(true, selectedChannel) },
                        label = { Text("물건 3건 HTML", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("chip_html_3_cases")
                    )
                    FilterChip(
                        selected = !bundle.threeCasesMode,
                        onClick = { onSwitchMode(false, selectedChannel) },
                        label = { Text("1물건 3채널 HTML", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("chip_html_3_channels")
                    )
                    FilterChip(
                        selected = !bodyOnlyMode,
                        onClick = { bodyOnlyMode = false },
                        label = { Text("완전판 HTML", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("chip_html_full_doc")
                    )
                    FilterChip(
                        selected = bodyOnlyMode,
                        onClick = { bodyOnlyMode = true },
                        label = { Text("블로그 붙여넣기(<article>)", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("chip_html_body_only")
                    )
                }

                if (bundle.threeCasesMode) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ChannelPlatform.entries.forEach { ch ->
                            FilterChip(
                                selected = selectedChannel == ch,
                                onClick = {
                                    selectedChannel = ch
                                    onSwitchMode(true, ch)
                                },
                                label = { Text(ch.displayName, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    bundle.items.forEachIndexed { idx, item ->
                        FilterChip(
                            selected = selectedIdx == idx,
                            onClick = { selectedIdx = idx },
                            label = {
                                Text(
                                    text = "#${item.index} ${item.caseId} (${item.platform.code})",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.testTag("chip_html_item_$idx")
                        )
                    }
                }

                if (activeItem != null) {
                    Text(
                        text = "${activeItem.title} · ${activeItem.score}점 (${activeHtmlText.length}자)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MintSecondary
                    )
                    Surface(
                        color = NavyBackground,
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 170.dp, max = 280.dp)
                    ) {
                        SelectionContainer {
                            Text(
                                text = activeHtmlText,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFamily),
                                color = TextPrimary,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .verticalScroll(rememberScrollState())
                                    .testTag("html_preview_code_text")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val res = SafeClipboardHelper.copyPlainTextSafely(
                                    context = context,
                                    label = "#${activeItem.index} ${activeItem.caseId} HTML",
                                    text = activeHtmlText,
                                    composeClipboard = clipboard
                                )
                                copyFailedBanner = !res.success
                                copiedBanner = res.message
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_copy_single_html")
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("이 HTML 복사", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = {
                                val fileName = "${activeItem.caseId}_${activeItem.platform.code}.html"
                                val shared = SafeClipboardHelper.shareHtmlAsFile(context, fileName, activeHtmlText)
                                if (!shared) {
                                    copyFailedBanner = true
                                    copiedBanner = "HTML 파일 내보내기 앱을 찾을 수 없습니다. '이 HTML 복사'를 이용해 주세요."
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_share_single_html")
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(".html 저장·공유", maxLines = 1)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val allHtml = bundle.items.joinToString("\n\n<!-- ==================== NEXT POST ==================== -->\n\n") { item ->
                        if (bodyOnlyMode) item.bodyOnlyHtml.ifBlank { item.pureHtml } else item.pureHtml
                    }
                    val res = SafeClipboardHelper.copyPlainTextSafely(
                        context = context,
                        label = "3개 HTML 전체",
                        text = allHtml,
                        composeClipboard = clipboard
                    )
                    copyFailedBanner = !res.success
                    copiedBanner = res.message
                },
                modifier = Modifier.testTag("btn_copy_all_3_html"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("3개 HTML 전체 복사")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기", color = TextSecondary)
            }
        }
    )
}
