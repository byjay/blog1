package com.example.ui.screens

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.engine.Article30PointEvaluator
import com.example.engine.HtmlExportEngine
import com.example.engine.RegistryInfographicEngine
import com.example.model.ChannelPlatform
import com.example.model.GeneratedPostSlot
import com.example.model.HtmlLinkFilter
import com.example.model.HtmlViewerTarget
import com.example.model.LinkedHtmlArticleEntry
import com.example.ui.StudioUiState
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.CloudflareContainer
import com.example.ui.theme.CloudflareSky
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
import com.example.ui.util.SafeClipboardHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HtmlLinkHubSheetModal(
    uiState: StudioUiState,
    onSelectFilter: (HtmlLinkFilter) -> Unit,
    onOpenHubViewer: () -> Unit,
    onOpenArticleViewer: (postId: String, channel: ChannelPlatform) -> Unit,
    onOpenVerifyAndEdit: (postId: String) -> Unit,
    onAutoFixAndRegister: (postId: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val catalog = remember(
        uiState.slots,
        uiState.warehouseArticles,
        uiState.publishLogs,
        uiState.config,
        uiState.proFunnel,
        uiState.selectedChannel
    ) {
        HtmlExportEngine.buildLinkedHtmlCatalog(
            slots = uiState.slots,
            warehouseArticles = uiState.warehouseArticles,
            publishLogs = uiState.publishLogs,
            config = uiState.config,
            proFunnel = uiState.proFunnel,
            preferredChannel = uiState.selectedChannel
        )
    }

    val filteredCatalog = remember(catalog, uiState.htmlLinkFilter) {
        when (uiState.htmlLinkFilter) {
            HtmlLinkFilter.ALL -> catalog
            HtmlLinkFilter.WEB_PUBLISHED -> catalog.filter { it.publishedUrls.isNotEmpty() }
            HtmlLinkFilter.LOCAL_ONLY -> catalog.filter { it.publishedUrls.isEmpty() }
        }
    }
    val publishedWebCount = remember(catalog) { catalog.count { it.publishedUrls.isNotEmpty() } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .testTag("modal_html_link_hub"),
            shape = RoundedCornerShape(16.dp),
            color = NavySurface,
            border = BorderStroke(1.dp, CloudflareSky.copy(alpha = 0.65f))
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Link,
                            contentDescription = "HTML 링크함",
                            tint = CloudflareSky,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "딱찰 HTML 링크함 · 생성 글 목차 허브",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "생성 글 ${catalog.size}건 · 웹URL 발행 ${publishedWebCount}건",
                                style = MaterialTheme.typography.labelSmall,
                                color = MintSecondary,
                                maxLines = 1
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_html_link_hub")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "HTML 링크함 닫기", tint = TextSecondary)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EasyMenuGuideStrip(
                        icon = Icons.Filled.Public,
                        easyGuide = "생성한 글을 목차 허브 HTML과 상세 HTML로 서로 연결해 보고, 실제 발행된 블로그 웹URL로 바로 이동합니다.",
                        accentColor = CloudflareSky
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenHubViewer,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                                .testTag("btn_open_html_hub_viewer"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = NavyBackground
                            )
                        ) {
                            Icon(Icons.Filled.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("목차 허브 HTML", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = {
                                val bundle = buildLinkedHtmlExportFiles(uiState, catalog)
                                SafeClipboardHelper.openHtmlBundleInBrowserOrShare(
                                    context = context,
                                    filesByName = bundle,
                                    entryFileName = "ddakchal_hub.html",
                                    openChooserOnly = false
                                )
                            },
                            enabled = catalog.isNotEmpty(),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_export_html_hub_browser")
                        ) {
                            Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("브라우저", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = {
                                val bundle = buildLinkedHtmlExportFiles(uiState, catalog)
                                SafeClipboardHelper.openHtmlBundleInBrowserOrShare(
                                    context = context,
                                    filesByName = bundle,
                                    entryFileName = "ddakchal_hub.html",
                                    openChooserOnly = true
                                )
                            },
                            enabled = catalog.isNotEmpty(),
                            modifier = Modifier
                                .weight(0.9f)
                                .height(48.dp)
                                .testTag("btn_share_html_hub_bundle")
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("공유", maxLines = 1)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HtmlLinkFilter.entries.forEach { filter ->
                            val count = when (filter) {
                                HtmlLinkFilter.ALL -> catalog.size
                                HtmlLinkFilter.WEB_PUBLISHED -> publishedWebCount
                                HtmlLinkFilter.LOCAL_ONLY -> catalog.size - publishedWebCount
                            }
                            FilterChip(
                                selected = uiState.htmlLinkFilter == filter,
                                onClick = { onSelectFilter(filter) },
                                label = { Text("${filter.label} ($count)") },
                                modifier = Modifier.testTag("chip_html_filter_${filter.name}")
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (filteredCatalog.isEmpty()) {
                        item(key = "html_hub_empty") {
                            Surface(
                                color = NavyBackground,
                                shape = MaterialTheme.shapes.medium,
                                border = BorderStroke(1.dp, NavyBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("card_html_hub_empty")
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "표시할 생성 글 HTML 링크가 없습니다",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "상단 컨트롤타워에서 법원 사건번호를 조회하거나 '1. DB 찾기' → '글 생성 · 미리보기'로 글을 생성하면, 목차 허브 HTML과 글별 상세 HTML 링크가 즉시 생성됩니다.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredCatalog, key = { it.postId }) { entry ->
                            LinkedHtmlEntryCard(
                                entry = entry,
                                isBusy = uiState.isBusy,
                                onOpenDetailHtml = { ch -> onOpenArticleViewer(entry.postId, ch) },
                                onOpenVerifyAndEdit = { onOpenVerifyAndEdit(entry.postId) },
                                onAutoFixAndRegister = { onAutoFixAndRegister(entry.postId) },
                                onOpenExternalUrl = { url ->
                                    SafeClipboardHelper.openExternalWebUrl(context, url)
                                },
                                onCopyUrl = { label, url ->
                                    SafeClipboardHelper.copyPlainTextSafely(
                                        context = context,
                                        label = label,
                                        text = url,
                                        composeClipboard = clipboard
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LinkedHtmlEntryCard(
    entry: LinkedHtmlArticleEntry,
    isBusy: Boolean,
    onOpenDetailHtml: (ChannelPlatform) -> Unit,
    onOpenVerifyAndEdit: () -> Unit,
    onAutoFixAndRegister: () -> Unit,
    onOpenExternalUrl: (String) -> Unit,
    onCopyUrl: (label: String, url: String) -> Unit
) {
    Surface(
        color = NavySurfaceElevated,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            1.dp,
            if (entry.publishedUrls.isNotEmpty()) EmeraldPrimary.copy(alpha = 0.6f) else NavyBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_html_link_${entry.caseId}")
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
                Text(
                    text = "${entry.caseId} · ${entry.propertyTypeLabel}",
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                    color = MintSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(
                        text = "${entry.score}점",
                        textColor = if (entry.score >= 95) MintSecondary else TistoryAmber,
                        backgroundColor = if (entry.score >= 95) EmeraldContainer else AmberContainer
                    )
                    PostStatusBadge(status = entry.status)
                }
            }

            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenDetailHtml(entry.defaultChannel) }
            )

            Text(
                text = "${entry.courtName} · ${entry.address} | 사진 ${entry.photoCount}컷 · 등기 ${entry.registryCount}건 · 방문 ${entry.visitCount}건",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { onOpenDetailHtml(entry.defaultChannel) },
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CloudflareSky,
                        contentColor = NavyBackground
                    )
                ) {
                    Icon(Icons.Filled.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("HTML 보기", fontWeight = FontWeight.Bold, maxLines = 1)
                }
                OutlinedButton(
                    onClick = onOpenVerifyAndEdit,
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("검증·수정", maxLines = 1)
                }
                ChannelPlatform.entries.forEach { ch ->
                    OutlinedButton(
                        onClick = { onOpenDetailHtml(ch) },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(ch.displayName, maxLines = 1)
                    }
                }
            }

            if (entry.publishedUrls.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    entry.publishedUrls.forEach { (platform, url) ->
                        OutlinedButton(
                            onClick = { onOpenExternalUrl(url) },
                            modifier = Modifier.height(48.dp),
                            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.7f))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${platform.displayName} 웹 ↗", color = MintSecondary, maxLines = 1)
                        }
                        TextButton(
                            onClick = { onCopyUrl("${platform.displayName} 웹 링크", url) },
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "링크 복사", modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("URL복사", maxLines = 1)
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "실제 웹URL 미발행 (예상: ${entry.cloudflareExpectedUrl})",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onAutoFixAndRegister,
                        enabled = !isBusy,
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = NavyBackground
                        )
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("95+ 등록", fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveHtmlLinkViewerModal(
    initialTarget: HtmlViewerTarget,
    uiState: StudioUiState,
    onOpenVerifyAndEdit: (postId: String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var currentTarget by remember(initialTarget) { mutableStateOf(initialTarget) }
    var historyStack by remember(initialTarget) { mutableStateOf<List<HtmlViewerTarget>>(emptyList()) }

    val navigateTo: (HtmlViewerTarget) -> Unit = { next ->
        if (next != currentTarget) {
            historyStack = historyStack + currentTarget
            currentTarget = next
        }
    }
    val navigateBack: () -> Unit = {
        if (historyStack.isNotEmpty()) {
            currentTarget = historyStack.last()
            historyStack = historyStack.dropLast(1)
        } else {
            onClose()
        }
    }

    BackHandler(onBack = navigateBack)

    val catalog = remember(
        uiState.slots,
        uiState.warehouseArticles,
        uiState.publishLogs,
        uiState.config,
        uiState.proFunnel,
        uiState.selectedChannel
    ) {
        HtmlExportEngine.buildLinkedHtmlCatalog(
            slots = uiState.slots,
            warehouseArticles = uiState.warehouseArticles,
            publishLogs = uiState.publishLogs,
            config = uiState.config,
            proFunnel = uiState.proFunnel,
            preferredChannel = uiState.selectedChannel
        )
    }

    val activeSlot: GeneratedPostSlot? = remember(currentTarget, uiState.slots, uiState.warehouseArticles) {
        when (val t = currentTarget) {
            HtmlViewerTarget.HubIndex -> null
            is HtmlViewerTarget.ArticleDetail ->
                uiState.slots.firstOrNull { it.postId == t.postId }
                    ?: uiState.warehouseArticles.firstOrNull { it.slot.postId == t.postId }?.slot
        }
    }

    var localDataUris by remember(activeSlot?.postId, activeSlot?.courtPhotoUrls) {
        mutableStateOf<Map<Int, String>>(emptyMap())
    }
    LaunchedEffect(activeSlot?.postId, activeSlot?.courtPhotoUrls) {
        val urls = activeSlot?.courtPhotoUrls.orEmpty()
        localDataUris = if (urls.isNotEmpty()) {
            withContext(Dispatchers.IO) { buildLocalPhotoDataUriMap(context, urls) }
        } else emptyMap()
    }

    val renderedHtml = remember(currentTarget, catalog, activeSlot, uiState.config, uiState.proFunnel, localDataUris) {
        when (val t = currentTarget) {
            HtmlViewerTarget.HubIndex -> {
                val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(Date())
                HtmlExportEngine.renderHubIndexHtml(
                    entries = catalog,
                    config = uiState.config,
                    generatedAt = now,
                    forExternalFileExport = false
                )
            }
            is HtmlViewerTarget.ArticleDetail -> {
                val slot = activeSlot
                if (slot == null) {
                    val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(Date())
                    HtmlExportEngine.renderHubIndexHtml(catalog, uiState.config, now, forExternalFileExport = false)
                } else {
                    val reg = RegistryInfographicEngine.buildTemplate(slot.auctionCase, uiState.selectedRegistryTemplateType)
                    val eval = Article30PointEvaluator.evaluate(slot, uiState.config, uiState.proFunnel, reg)
                    HtmlExportEngine.renderLinkedArticleDetailHtml(
                        slot = slot,
                        channel = t.channel,
                        config = uiState.config,
                        evaluation = eval,
                        localPhotoDataUris = localDataUris,
                        forExternalFileExport = false
                    )
                }
            }
        }
    }

    val latestNavigateTo by rememberUpdatedState(navigateTo)
    val latestContext by rememberUpdatedState(context)

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.97f)
                .fillMaxHeight(0.94f)
                .testTag("modal_interactive_html_viewer"),
            shape = RoundedCornerShape(16.dp),
            color = NavySurface,
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.65f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavySurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = navigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_viewer_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "이전 HTML", tint = TextPrimary)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (val t = currentTarget) {
                                HtmlViewerTarget.HubIndex -> "딱찰 HTML 목차 허브 (전체 ${catalog.size}건)"
                                is HtmlViewerTarget.ArticleDetail ->
                                    "${activeSlot?.auctionCase?.caseId ?: t.postId} · ${t.channel.displayName} HTML"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "앱 내 링크(ddakchal://) 및 실제 웹URL 양방향 이동 지원",
                            style = MaterialTheme.typography.labelSmall,
                            color = MintSecondary,
                            maxLines = 1
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_html_viewer")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "HTML 뷰어 닫기", tint = TextSecondary)
                    }
                }

                // Navigation & Export Strip
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { navigateTo(HtmlViewerTarget.HubIndex) },
                        enabled = currentTarget !is HtmlViewerTarget.HubIndex,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_viewer_go_hub")
                    ) {
                        Text("← 목록 허브", maxLines = 1)
                    }

                    val detailTarget = currentTarget as? HtmlViewerTarget.ArticleDetail
                    if (detailTarget != null && activeSlot != null) {
                        ChannelPlatform.entries.forEach { ch ->
                            FilterChip(
                                selected = detailTarget.channel == ch,
                                onClick = {
                                    navigateTo(HtmlViewerTarget.ArticleDetail(activeSlot.postId, ch))
                                },
                                label = { Text(ch.displayName) }
                            )
                        }

                        val liveUrl = activeSlot.publishedUrls[detailTarget.channel].orEmpty()
                            .ifBlank { activeSlot.publishedUrls.values.firstOrNull().orEmpty() }
                        if (liveUrl.isNotBlank()) {
                            Button(
                                onClick = { SafeClipboardHelper.openExternalWebUrl(context, liveUrl) },
                                modifier = Modifier.height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = NavyBackground
                                )
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("웹URL ↗", fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                onClose()
                                onOpenVerifyAndEdit(activeSlot.postId)
                            },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("btn_viewer_open_verify")
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("검증·수정·등록", maxLines = 1)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            SafeClipboardHelper.copyPlainTextSafely(
                                context = context,
                                label = "현재 HTML 코드",
                                text = renderedHtml,
                                composeClipboard = clipboard
                            )
                        },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("HTML 복사", maxLines = 1)
                    }
                }

                // Interactive WebView
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
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
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        val url = request?.url?.toString().orEmpty()
                                        return handleDdakchalWebLink(
                                            url = url,
                                            onNavigate = latestNavigateTo,
                                            context = latestContext
                                        )
                                    }
                                }
                            }
                        },
                        update = { webView ->
                            webView.loadDataWithBaseURL("https://ddakchal.local/", renderedHtml, "text/html", "utf-8", null)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("webview_interactive_html_hub")
                    )
                }
            }
        }
    }
}

internal fun parseDdakchalInternalLink(url: String): HtmlViewerTarget? {
    val trimmed = url.trim()
    if (trimmed.equals("ddakchal://hub", ignoreCase = true) ||
        trimmed.startsWith("ddakchal://hub/", ignoreCase = true)
    ) {
        return HtmlViewerTarget.HubIndex
    }
    if (trimmed.startsWith("ddakchal://post/", ignoreCase = true)) {
        val rest = trimmed.substringAfter("ddakchal://post/")
        val parts = rest.split("/").filter { it.isNotBlank() }
        val postId = parts.getOrNull(0) ?: return null
        val chCode = parts.getOrNull(1)
        val channel = ChannelPlatform.entries.firstOrNull {
            it.code.equals(chCode, ignoreCase = true)
        } ?: ChannelPlatform.CLOUDFLARE
        return HtmlViewerTarget.ArticleDetail(postId = postId, channel = channel)
    }
    return null
}

private fun handleDdakchalWebLink(
    url: String,
    onNavigate: (HtmlViewerTarget) -> Unit,
    context: Context
): Boolean {
    val internal = parseDdakchalInternalLink(url)
    if (internal != null) {
        onNavigate(internal)
        return true
    }
    if (url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true)) {
        SafeClipboardHelper.openExternalWebUrl(context, url)
        return true
    }
    return false
}

private fun buildLinkedHtmlExportFiles(
    uiState: StudioUiState,
    catalog: List<LinkedHtmlArticleEntry>
): Map<String, String> {
    val files = linkedMapOf<String, String>()
    val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(Date())
    files["ddakchal_hub.html"] = HtmlExportEngine.renderHubIndexHtml(
        entries = catalog,
        config = uiState.config,
        generatedAt = now,
        forExternalFileExport = true
    )
    val allSlotsById = (uiState.slots + uiState.warehouseArticles.map { it.slot }).associateBy { it.postId }
    catalog.forEach { entry ->
        val slot = allSlotsById[entry.postId] ?: return@forEach
        val reg = RegistryInfographicEngine.buildTemplate(slot.auctionCase, uiState.selectedRegistryTemplateType)
        val eval = Article30PointEvaluator.evaluate(slot, uiState.config, uiState.proFunnel, reg)
        ChannelPlatform.entries.forEach { ch ->
            val fileName = HtmlExportEngine.buildLinkedHtmlSlug(slot.auctionCase.caseId, slot.auctionCase.dedupKey, ch)
            files[fileName] = HtmlExportEngine.renderLinkedArticleDetailHtml(
                slot = slot,
                channel = ch,
                config = uiState.config,
                evaluation = eval,
                forExternalFileExport = true
            )
        }
    }
    return files
}
