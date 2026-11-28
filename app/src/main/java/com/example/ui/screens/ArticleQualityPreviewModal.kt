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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
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
    onRefreshCourtEvidence: (caseId: String) -> Unit = {},
    onVerifySlot: (String) -> Unit = {},
    onSaveManualEdit: (
        postId: String,
        platform: ChannelPlatform,
        title: String,
        bodyText: String,
        pinnedComment: String
    ) -> Unit = { _, _, _, _, _ -> },
    onOpenHtmlArticleViewer: (postId: String, channel: ChannelPlatform) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var modalTab by remember(slot.postId, initialTabIndex) { mutableIntStateOf(initialTabIndex.coerceIn(0, 3)) }

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
                            text = "${slot.auctionCase.caseId} · 미리보기 · 검증·수정 · 등록",
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

                // 4 Workflow Step Tabs: 미리보기 -> 검증·수정 -> 사진·등기 -> 등록
                TabRow(
                    selectedTabIndex = modalTab,
                    containerColor = NavyBackground,
                    contentColor = EmeraldPrimary
                ) {
                    listOf("미리보기", "검증·수정(${evalResult.totalScore})", "사진·등기", "등록").forEachIndexed { idx, label ->
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
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        onOpenHtmlArticleViewer(slot.postId, activeVariant.platform)
                                    },
                                    modifier = Modifier
                                        .weight(0.95f)
                                        .height(48.dp)
                                        .testTag("btn_modal_open_html_viewer")
                                ) {
                                    Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("HTML링크", maxLines = 1)
                                }
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
                                        .weight(0.9f)
                                        .height(48.dp)
                                        .testTag("btn_modal_copy_html")
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("복사", maxLines = 1)
                                }
                                OutlinedButton(
                                    onClick = { modalTab = 1 },
                                    modifier = Modifier
                                        .weight(1.05f)
                                        .height(48.dp)
                                        .testTag("btn_modal_go_verify_edit"),
                                    border = BorderStroke(1.dp, CloudflareSky.copy(alpha = 0.75f))
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = null, tint = CloudflareSky, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("검증·수정", color = CloudflareSky, maxLines = 1)
                                }
                                Button(
                                    onClick = { modalTab = 3 },
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .height(48.dp)
                                        .testTag("btn_modal_go_publish"),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("등록 →", fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }

                    // Tab 1: Verify & Manual/Auto Edit
                    1 -> {
                        ArticleVerifyAndEditTab(
                            slot = slot,
                            uiState = uiState,
                            evalResult = evalResult,
                            courtArchive = courtArchive,
                            legalReport = uiState.legalVerificationByCaseId[slot.auctionCase.caseId],
                            onSelectChannel = onSelectChannel,
                            onRunVerify = onVerifySlot,
                            onAutoFixWithoutPublish = onAutoFixSlot,
                            onSaveManualEdit = onSaveManualEdit,
                            onGoToPreviewTab = { modalTab = 0 },
                            onGoToRegisterTab = { modalTab = 3 }
                        )
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
                                            text = "현재 품질 ${evalResult.totalScore}점(${evalResult.passedCount}/30) · 등록 버튼을 누르면 실제 채널 API로 전송 후 HTTP 응답과 라이브 URL을 실검증합니다.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { modalTab = 1 },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                            ) {
                                                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("검증·수정", maxLines = 1)
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    onDismiss()
                                                    onOpenSettings()
                                                },
                                                modifier = Modifier
                                                    .weight(1.2f)
                                                    .height(48.dp)
                                            ) {
                                                Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("블로그·Worker 설정", maxLines = 1)
                                            }
                                        }
                                    }
                                }

                            Button(
                                onClick = { onPublishSelectedChannelsAndVerify(slot.postId, "") },
                                enabled = !uiState.isBusy,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("btn_modal_test_publish_all"),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
                            ) {
                                Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("블로그 등록 실행 (선택 채널 · 실검증)", fontWeight = FontWeight.Bold)
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
                                        onOpenHtmlArticleViewer(slot.postId, activeVariant.platform)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_modal_register_open_html_link")
                                ) {
                                    Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("HTML 링크 보기", maxLines = 1)
                                }
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
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_modal_open_naver_write")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("네이버 원고복사 + 글쓰기창", maxLines = 1)
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
                                            if (outcome.publishedPostUrl.startsWith("http")) {
                                                OutlinedButton(
                                                    onClick = { SafeClipboardHelper.openExternalWebUrl(context, outcome.publishedPostUrl) },
                                                    modifier = Modifier.height(48.dp)
                                                ) {
                                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("발행된 글 열기 ↗", maxLines = 1)
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
    }
}

internal fun buildLocalPhotoDataUriMap(
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
