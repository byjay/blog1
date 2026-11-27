package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DirectChannelPublishClient
import com.example.model.ChannelPlatform
import com.example.model.GeneratedPostSlot
import com.example.model.NaverAndMultiChannelPublishReport
import com.example.model.ServerAutomationConfig
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
fun ThreeChannelDestinationAndNaverVerifyCard(
    slot: GeneratedPostSlot,
    config: ServerAutomationConfig,
    report: NaverAndMultiChannelPublishReport?,
    isBusy: Boolean,
    onSaveDestinations: (
        naverBlogId: String,
        naverCategoryName: String,
        naverWorkerRelayEndpoint: String,
        tistoryBlogUrl: String,
        tistoryCategoryName: String,
        ddakchalApiBase: String,
        cloudflareBlogPath: String,
        publishNaver: Boolean,
        publishTistory: Boolean,
        publishCloudflare: Boolean
    ) -> Unit,
    onPublishNaverDirectAndVerify: (String) -> Unit,
    onPublishSelectedChannelsAndVerify: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var naverBlogId by remember { mutableStateOf(config.naverBlogId) }
    var naverCategory by remember { mutableStateOf(config.naverCategoryName) }
    var naverRelay by remember { mutableStateOf(config.naverWorkerRelayEndpoint) }
    var tistoryUrl by remember { mutableStateOf(config.tistoryBlogUrl) }
    var tistoryCategory by remember { mutableStateOf(config.tistoryCategoryName) }
    var cfBase by remember { mutableStateOf(config.cloudflareSiteBase) }
    var cfPath by remember { mutableStateOf(config.cloudflareBlogPath) }
    var enableNaver by remember { mutableStateOf(config.publishTargetNaver) }
    var enableTistory by remember { mutableStateOf(config.publishTargetTistory) }
    var enableCloudflare by remember { mutableStateOf(config.publishTargetCloudflare) }
    var customNaverPostUrl by remember { mutableStateOf("") }

    LaunchedEffect(config) {
        naverBlogId = config.naverBlogId
        naverCategory = config.naverCategoryName
        naverRelay = config.naverWorkerRelayEndpoint
        tistoryUrl = config.tistoryBlogUrl
        tistoryCategory = config.tistoryCategoryName
        cfBase = config.cloudflareSiteBase
        cfPath = config.cloudflareBlogPath
        enableNaver = config.publishTargetNaver
        enableTistory = config.publishTargetTistory
        enableCloudflare = config.publishTargetCloudflare
    }

    val naverVariant = slot.variants[ChannelPlatform.NAVER]
    val writeAssistUrl = remember(naverBlogId, naverVariant?.title, naverVariant?.bodyText) {
        DirectChannelPublishClient().buildNaverWriteAssistUrl(
            naverBlogId = naverBlogId,
            title = naverVariant?.title.orEmpty(),
            bodyText = naverVariant?.bodyText.orEmpty()
        )
    }

    fun persistDraft() {
        onSaveDestinations(
            naverBlogId,
            naverCategory,
            naverRelay,
            tistoryUrl,
            tistoryCategory,
            cfBase,
            cfPath,
            enableNaver,
            enableTistory,
            enableCloudflare
        )
    }

    StudioCard(
        modifier = modifier,
        borderColor = NaverGreen.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("3곳 블로그 등록처 & 네이버 바로 등록·실검증", style = MaterialTheme.typography.titleSmall, color = NaverGreen)
                Text(
                    "등록할 3개 채널 주소/카테고리를 입력·저장한 뒤 실제 HTTP 도달과 6대 안전 수칙을 검증합니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            StatusChip(
                text = if (report?.naverDirectPassed == true) "네이버 실검증 PASS" else "등록처 설정",
                textColor = if (report?.naverDirectPassed == true) MintSecondary else NaverGreen,
                backgroundColor = if (report?.naverDirectPassed == true) EmeraldContainer else NavySurfaceElevated
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1. Naver Blog Destination
        Surface(
            color = NavySurfaceElevated,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, if (enableNaver) NaverGreen.copy(alpha = 0.5f) else NavyBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("① 네이버 블로그 등록처", style = MaterialTheme.typography.labelLarge, color = NaverGreen)
                    Switch(checked = enableNaver, onCheckedChange = { enableNaver = it })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = naverBlogId,
                        onValueChange = { naverBlogId = it },
                        label = { Text("블로그 ID 또는 URL") },
                        placeholder = { Text("예: my_auction_blog") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("input_naver_blog_id")
                    )
                    OutlinedTextField(
                        value = naverCategory,
                        onValueChange = { naverCategory = it },
                        label = { Text("게시판 카테고리") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = naverRelay,
                    onValueChange = { naverRelay = it },
                    label = { Text("네이버 Playwright/워커 릴레이 URL (선택)") },
                    placeholder = { Text("미입력 시 딱찰 서버 릴레이 또는 모바일 글쓰기 연동") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Tistory Destination
        Surface(
            color = NavySurfaceElevated,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, if (enableTistory) TistoryAmber.copy(alpha = 0.5f) else NavyBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("② 티스토리 등록처", style = MaterialTheme.typography.labelLarge, color = TistoryAmber)
                    Switch(checked = enableTistory, onCheckedChange = { enableTistory = it })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tistoryUrl,
                        onValueChange = { tistoryUrl = it },
                        label = { Text("티스토리 주소") },
                        placeholder = { Text("https://myblog.tistory.com") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("input_tistory_blog_url")
                    )
                    OutlinedTextField(
                        value = tistoryCategory,
                        onValueChange = { tistoryCategory = it },
                        label = { Text("카테고리") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Cloudflare Self-Web Destination
        Surface(
            color = NavySurfaceElevated,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, if (enableCloudflare) CloudflareSky.copy(alpha = 0.5f) else NavyBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("③ Cloudflare 자체웹 등록처", style = MaterialTheme.typography.labelLarge, color = CloudflareSky)
                    Switch(checked = enableCloudflare, onCheckedChange = { enableCloudflare = it })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cfBase,
                        onValueChange = { cfBase = it },
                        label = { Text("Cloudflare 블로그 사이트 (aution.codeb.app)") },
                        singleLine = true,
                        modifier = Modifier.weight(1.4f)
                    )
                    OutlinedTextField(
                        value = cfPath,
                        onValueChange = { cfPath = it },
                        label = { Text("블로그 경로") },
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = { persistDraft() },
            enabled = !isBusy,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_save_3channel_destinations")
        ) {
            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("3채널 등록처 저장")
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = customNaverPostUrl,
            onValueChange = { customNaverPostUrl = it },
            label = { Text("발행 완료된 네이버 글 URL 실존 검증 (선택: logNo URL)") },
            placeholder = { Text("예: https://blog.naver.com/내아이디/223698104211") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    persistDraft()
                    onPublishNaverDirectAndVerify(customNaverPostUrl)
                },
                enabled = !isBusy,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_publish_naver_direct_verify"),
                colors = ButtonDefaults.buttonColors(containerColor = NaverGreen, contentColor = NavyBackground)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("네이버 바로 등록·실검증", fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Button(
                onClick = {
                    persistDraft()
                    onPublishSelectedChannelsAndVerify(customNaverPostUrl)
                },
                enabled = !isBusy,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_publish_selected_channels_verify"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
            ) {
                Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("선택 채널 일괄 등록·검증", fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    val fullNaverCopy = buildString {
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
                        label = "네이버 제목·본문·고정댓글",
                        text = fullNaverCopy,
                        composeClipboard = clipboard
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("제목·본문·고정댓글 복사", maxLines = 1)
            }
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(writeAssistUrl))
                    runCatching { context.startActivity(intent) }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("네이버 글쓰기 열기", maxLines = 1)
            }
        }

        if (report != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = NavyBackground,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(
                    1.dp,
                    if (report.naverDirectPassed) EmeraldPrimary.copy(alpha = 0.6f) else TistoryAmber.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "등록 및 검증 리포트",
                            tint = if (report.naverDirectPassed) MintSecondary else TistoryAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${report.caseId} 실시간 검증 (${report.attemptedAt})",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary
                        )
                    }
                    Text(report.overallSummary, style = MaterialTheme.typography.bodySmall, color = MintSecondary)

                    report.outcomes.forEach { outcome ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${outcome.platform.displayName} [${outcome.executedMethod}] HTTP ${outcome.httpStatusCode}",
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                                color = if (outcome.reachableOk) MintSecondary else CrimsonAlert
                            )
                            StatusChip(
                                text = if (outcome.reachableOk) "도달 OK" else "미연결/보완",
                                textColor = if (outcome.reachableOk) MintSecondary else CrimsonAlert,
                                backgroundColor = if (outcome.reachableOk) EmeraldContainer else CrimsonContainer
                            )
                        }
                        Text(outcome.message, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    report.verificationChecks.forEach { chk ->
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = if (chk.passed) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = if (chk.passed) EmeraldPrimary else CrimsonAlert,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(chk.title, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                                Text(chk.detail, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}
