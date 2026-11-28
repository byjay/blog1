package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.webkit.WebView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.DirectChannelPublishClient
import com.example.engine.Article30PointEvaluator
import com.example.engine.CourtEvidenceRecorder
import com.example.engine.HtmlExportEngine
import com.example.engine.InfographicBitmapRenderer
import com.example.engine.RegistryInfographicEngine
import com.example.model.ChannelPlatform
import com.example.model.EvaluationPillar
import com.example.model.GeneratedPostSlot
import com.example.model.VisitRecordSource
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
import com.example.ui.util.SafeClipboardHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArticleQualityPreviewModal(
    slot: GeneratedPostSlot,
    uiState: StudioUiState,
    initialTabIndex: Int = 0,
    onDismiss: () -> Unit,
    onSelectChannel: (ChannelPlatform) -> Unit,
    onAttachCourtPhoto: (postId: String, shotOrder: Int, uriString: String) -> Unit,
    onAddVisitRecord: (postId: String, source: VisitRecordSource, visitedAt: String, occupancyStatus: String, findingsNote: String) -> Unit,
    onAutoFixSlot: (String) -> Unit,
    onPublishSelectedChannelsAndVerify: (String, String) -> Unit,
    onPublishNaverDirectAndVerify: (String, String) -> Unit,
    onOpenSettings: () -> Unit,
    onRefreshCourtEvidence: (caseId: String) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var modalTab by remember(slot.postId, initialTabIndex) { mutableIntStateOf(initialTabIndex.coerceIn(0, 3)) }
    var showOnlyFailed by remember { mutableStateOf(false) }

    val selectedChannel = uiState.selectedChannel
    val activeVariant = slot.variants[selectedChannel]
        ?: slot.variants[ChannelPlatform.CLOUDFLARE]
        ?: slot.variants.values.first()

    val regTemplate = remember(slot.auctionCase.caseId, slot.auctionCase.baseRightDate) {
        RegistryInfographicEngine.buildTemplate(slot.auctionCase, uiState.selectedRegistryTemplateType)
    }
    val evalResult = remember(slot, uiState.config, uiState.proFunnel, regTemplate) {
        Article30PointEvaluator.evaluate(slot, uiState.config, uiState.proFunnel, regTemplate)
    }
    val courtArchive = remember(slot.postId, slot.courtPhotoUrls, slot.visitRecords, slot.serverRegistryGapGu, slot.serverRegistryEulGu, slot.serverCourtDocuments) {
        CourtEvidenceRecorder.recordFullArchiveForSlot(slot)
    }

    var localDataUris by remember(slot.postId, slot.courtPhotoUrls) {
        mutableStateOf<Map<Int, String>>(emptyMap())
    }
    LaunchedEffect(slot.postId, slot.courtPhotoUrls) {
        val encoded = withContext(Dispatchers.IO) {
            buildLocalPhotoDataUriMap(context, slot.courtPhotoUrls)
        }
        localDataUris = encoded
    }

    val previewHtml = remember(slot, activeVariant, uiState.config, evalResult, localDataUris) {
        HtmlExportEngine.renderWebViewPreviewHtml(
            slot = slot,
            variant = activeVariant,
            config = uiState.config,
            evaluation = evalResult,
            localPhotoDataUris = localDataUris
        )
    }
    val pureStandaloneHtml = remember(slot, activeVariant, uiState.config) {
        HtmlExportEngine.renderStandaloneHtmlDocument(slot, activeVariant, uiState.config)
    }
    val realPhotoCount = courtArchive.photos.count { it.imageUrl.isNotBlank() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .testTag("article_quality_preview_modal"),
            shape = RoundedCornerShape(16.dp),
            color = NavySurface,
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.55f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavySurfaceElevated)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${slot.auctionCase.caseId} · 글 품질 · 실제 미리보기 · 테스트 등록",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "품질 ${evalResult.totalScore}점(${evalResult.passedCount}/30) · 사진 ${courtArchive.photos.size}컷(실물 ${realPhotoCount}) · 등기 ${courtArchive.registryGapGu.size + courtArchive.registryEulGu.size} · 방문 ${courtArchive.visitRecords.size}건",
                            style = MaterialTheme.typography.labelSmall,
                            color = MintSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_quality_preview_modal")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "미리보기 닫기", tint = TextSecondary)
                    }
                }

                // 4 Sub-tabs
                TabRow(
                    selectedTabIndex = modalTab,
                    containerColor = NavyBackground,
                    contentColor = EmeraldPrimary
                ) {
                    listOf("글 미리보기", "품질 ${evalResult.totalScore}점", "사진·등기·방문", "1건 테스트등록").forEachIndexed { idx, label ->
                        Tab(
                            selected = modalTab == idx,
                            onClick = { modalTab = idx },
                            text = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.testTag("modal_tab_$idx")
                        )
                    }
                }

                when (modalTab) {
                    // Tab 0: Real Rendered Blog Post WebView Preview
                    0 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                                        label = { Text(platform.displayName) },
                                        modifier = Modifier.testTag("modal_channel_${platform.code}")
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, NavyBorder),
                                color = NavyBackground
                            ) {
                                AndroidView(
                                    factory = { ctx ->
                                        WebView(ctx).apply {
                                            settings.javaScriptEnabled = false
                                            settings.loadWithOverviewMode = true
                                            settings.useWideViewPort = false
                                            settings.allowFileAccess = false
                                            settings.allowContentAccess = false
                                        }
                                    },
                                    update = { webView ->
                                        webView.loadDataWithBaseURL(null, previewHtml, "text/html", "utf-8", null)
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("webview_article_preview")
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        SafeClipboardHelper.copyPlainTextSafely(
                                            context = context,
                                            label = "${activeVariant.platform.displayName} 완성 HTML",
                                            text = pureStandaloneHtml,
                                            composeClipboard = clipboard
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_modal_copy_html")
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("완성 HTML 복사", maxLines = 1)
                                }
                                Button(
                                    onClick = { modalTab = 3 },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_modal_go_publish"),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("1건 테스트 등록 →", fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }

                    // Tab 1: 30-Point Quality Breakdown + Court Case Basics Gate
                    1 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            EasyMenuGuideStrip(
                                icon = Icons.Filled.Verified,
                                easyGuide = "30가지 검색·신뢰 기준을 자동 채점합니다. 95점 미만이면 '자동 품질 보완' 버튼 한 번으로 바로 끌어올립니다.",
                                accentColor = EmeraldPrimary
                            )
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
                                            text = if (evalResult.eligibleForRegistration) "자동 등록 합격선(95+) 통과" else "품질 보완 권장 (목표 95점+)",
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
                                    Text(
                                        text = "통과 ${evalResult.passedCount}/${evalResult.totalCriteriaCount}개 · 기본증빙(사진 ${courtArchive.photos.size}컷 · 등기 ${courtArchive.registryGapGu.size + courtArchive.registryEulGu.size}건 · 방문기록 ${courtArchive.visitRecords.size}건) HTML 자동 포함",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
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

                            if (!evalResult.eligibleForRegistration) {
                                Button(
                                    onClick = { onAutoFixSlot(slot.postId) },
                                    enabled = !uiState.isBusy,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("btn_modal_autofix_95"),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
                                ) {
                                    Icon(Icons.Filled.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("95점+ 자동 품질 보완 실행", fontWeight = FontWeight.Bold)
                                }
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
                                        Text("${item.orderIndex}. ${item.title} (${item.earnedPoints}/${item.maxPoints}점)", style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
                                        Text(item.detail, style = MaterialTheme.typography.labelSmall, color = if (item.passed) TextMuted else CrimsonAlert)
                                    }
                                }
                            }
                        }
                    }

                    // Tab 2: Photos + Registry Extract + Visit Records (Court Case Basics)
                    2 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            EasyMenuGuideStrip(
                                icon = Icons.Filled.FolderOpen,
                                easyGuide = "법원과 딱찰 서버가 제공하는 현장사진·현황조사서·매각물건명세서·등기부·4축 칼분석 전체 자료입니다.",
                                accentColor = MintSecondary
                            )
                            CourtFullEvidenceArchiveCard(
                                auction = slot.auctionCase,
                                archive = courtArchive,
                                onSharePhotoPng = { photo ->
                                    val shareUri = InfographicBitmapRenderer.renderCourtEvidencePhotoToShareUri(
                                        context,
                                        slot.auctionCase,
                                        photo
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, shareUri)
                                        putExtra(Intent.EXTRA_TEXT, photo.blogCaption)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(shareIntent, "${slot.auctionCase.caseId} 법원 현장사진 #${photo.shotOrder} 공유")
                                    )
                                },
                                onAttachPhotoUri = { shotOrder, uriString ->
                                    onAttachCourtPhoto(slot.postId, shotOrder, uriString)
                                },
                                onAddVisitRecord = { src, date, occupancy, findings ->
                                    onAddVisitRecord(slot.postId, src, date, occupancy, findings)
                                },
                                onRefreshCourtEvidence = {
                                    onRefreshCourtEvidence(slot.auctionCase.caseId)
                                }
                            )
                        }
                    }

                    // Tab 3: Direct 1-Post Test Registration & Live Verification
                    else -> {
                        val naverVariant = slot.variants[ChannelPlatform.NAVER]
                        val writeAssistUrl = remember(uiState.config.naverBlogId, naverVariant?.title, naverVariant?.bodyText) {
                            DirectChannelPublishClient().buildNaverWriteAssistUrl(
                                naverBlogId = uiState.config.naverBlogId,
                                title = naverVariant?.title.orEmpty(),
                                bodyText = naverVariant?.bodyText.orEmpty()
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = NavySurfaceElevated,
                                shape = MaterialTheme.shapes.medium,
                                border = BorderStroke(1.dp, CloudflareSky.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("등록 대상 채널 & 준비 상태", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val cfTarget = uiState.config.cloudflareSiteBase.ifBlank { "https://aution.codeb.app" }
                                        StatusChip(
                                            text = "자체웹: $cfTarget",
                                            textColor = CloudflareSky,
                                            backgroundColor = CloudflareContainer
                                        )
                                        StatusChip(
                                            text = if (uiState.config.tistoryBlogUrl.isNotBlank()) "티스토리: ${uiState.config.tistoryBlogUrl}" else "티스토리 미입력",
                                            textColor = TistoryAmber,
                                            backgroundColor = AmberContainer
                                        )
                                        StatusChip(
                                            text = if (uiState.config.naverBlogId.isNotBlank()) "네이버: ${uiState.config.naverBlogId}" else "네이버 ID 미입력",
                                            textColor = NaverGreen,
                                            backgroundColor = EmeraldContainer
                                        )
                                    }
                                    Text(
                                        text = "주의: 가짜 성공을 반환하지 않습니다. 실제 Worker/D1 또는 릴레이가 연결되지 않은 채널은 HTTP 상태와 함께 미연결 사유가 정직하게 표기됩니다.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            onDismiss()
                                            onOpenSettings()
                                        },
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("블로그 계정 · Worker · API 설정 열기")
                                    }
                                }
                            }

                            Button(
                                onClick = { onPublishSelectedChannelsAndVerify(slot.postId, "") },
                                enabled = !uiState.isBusy,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("btn_modal_test_publish_all"),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
                            ) {
                                Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("선택 채널 1건 테스트 등록 · 실검증 실행", fontWeight = FontWeight.Bold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onPublishNaverDirectAndVerify(slot.postId, "") },
                                    enabled = !uiState.isBusy,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_modal_test_publish_naver"),
                                    colors = ButtonDefaults.buttonColors(containerColor = NaverGreen, contentColor = NavyBackground)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("네이버 릴레이 등록", fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                                OutlinedButton(
                                    onClick = {
                                        val fullCopy = buildString {
                                            appendLine(naverVariant?.title.orEmpty())
                                            appendLine()
                                            appendLine(naverVariant?.bodyText.orEmpty())
                                            val pinned = naverVariant?.pinnedCommentSnippet
                                            if (!pinned.isNullOrBlank()) {
                                                appendLine()
                                                appendLine("[고정댓글] $pinned")
                                            }
                                        }
                                        SafeClipboardHelper.copyPlainTextSafely(
                                            context = context,
                                            label = "네이버 원고 + 고정댓글",
                                            text = fullCopy,
                                            composeClipboard = clipboard
                                        )
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(writeAssistUrl)))
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_modal_open_naver_write")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("원고복사+글쓰기창", maxLines = 1)
                                }
                            }

                            val report = uiState.publishVerificationReport
                            if (report != null) {
                                Surface(
                                    color = NavyBackground,
                                    shape = MaterialTheme.shapes.medium,
                                    border = BorderStroke(
                                        1.dp,
                                        if (report.naverDirectPassed) EmeraldPrimary else TistoryAmber
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "${report.caseId} 등록 실검증 결과 (${report.attemptedAt})",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = TextPrimary
                                        )
                                        Text(report.overallSummary, style = MaterialTheme.typography.bodySmall, color = MintSecondary)
                                        report.outcomes.forEach { outcome ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${outcome.platform.displayName} (HTTP ${outcome.httpStatusCode})",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = if (outcome.reachableOk) MintSecondary else CrimsonAlert
                                                )
                                                StatusChip(
                                                    text = if (outcome.reachableOk) "등록 OK" else "미연동/실패",
                                                    textColor = if (outcome.reachableOk) MintSecondary else CrimsonAlert,
                                                    backgroundColor = if (outcome.reachableOk) EmeraldContainer else CrimsonContainer
                                                )
                                            }
                                            Text(outcome.message, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
}

private fun buildLocalPhotoDataUriMap(
    context: Context,
    urls: List<String>
): Map<Int, String> {
    if (urls.isEmpty()) return emptyMap()
    val result = mutableMapOf<Int, String>()
    urls.forEachIndexed { idx, uriStr ->
        if (uriStr.startsWith("content://", ignoreCase = true) || uriStr.startsWith("file://", ignoreCase = true)) {
            runCatching {
                val uri = Uri.parse(uriStr)
                val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream, null, opts) ?: return@use
                    val baos = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.JPEG, 76, baos)
                    bmp.recycle()
                    val b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                    result[idx + 1] = "data:image/jpeg;base64,$b64"
                }
            }
        }
    }
    return result
}
