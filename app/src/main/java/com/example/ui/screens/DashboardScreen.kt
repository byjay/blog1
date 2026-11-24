package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.engine.Article30PointEvaluator
import com.example.engine.QaVerificationEngine
import com.example.engine.RegistryInfographicEngine
import com.example.model.ChannelPlatform
import com.example.model.GeneratedPostSlot
import com.example.model.PostStatus
import com.example.model.PropertyType
import com.example.ui.StudioUiState
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    uiState: StudioUiState,
    onSelectWeeklyDailyCount: (Int) -> Unit,
    onGenerateWeeklyBatch: (Int) -> Unit,
    onCreateHighScoreAndAutoRegister: () -> Unit,
    onAutoFixAndRegisterSlot: (String) -> Unit,
    onSaveThreeChannelDestinations: (
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
    onPublishNaverDirectAndVerify: (String, String) -> Unit,
    onPublishSelectedChannelsAndVerify: (String, String) -> Unit,
    onOpenWarehouse: () -> Unit,
    onRunAiEvalSelected: () -> Unit,
    onTestBusanProfitableLand: () -> Unit,
    onTestLowScoreDraft: () -> Unit,
    onApplyAiAutoFixes: (String) -> Unit,
    onSyncPack: () -> Unit,
    onInspectSlot: (String) -> Unit,
    onResolveHandoff: () -> Unit,
    onAddCustomProperty: (
        caseId: String,
        courtName: String,
        address: String,
        propertyType: PropertyType,
        appraisalManWon: Long,
        minBidManWon: Long,
        baseRightDate: String,
        assumedRights: String,
        publishDate: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showDestinationSetup by rememberSaveable { mutableStateOf(false) }
    var showAdvancedTools by rememberSaveable { mutableStateOf(false) }

    val selectedSlot = uiState.slots.firstOrNull { it.postId == uiState.selectedSlotId }
        ?: uiState.slots.firstOrNull()
    val dailyCount = uiState.selectedWeeklyDailyCount.coerceIn(3, 5)
    val queuedOrPublishedCount = uiState.slots.count {
        it.status == PostStatus.QUEUED || it.status == PostStatus.PUBLISHED
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Ultra-Simple Hero: Weekly 7-Day Scheduling + Instant High-Score Auto-Register
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = NavySurface,
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f))
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.img_studio_hero),
                        contentDescription = "딱찰 주간 자동 예약 스튜디오",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(0.14f)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        NavyBackground.copy(alpha = 0.55f),
                                        NavySurface.copy(alpha = 0.96f)
                                    )
                                )
                            )
                    )
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusChip(
                                text = "주 1회 예약 · 95점+ 자동등록",
                                textColor = MintSecondary,
                                backgroundColor = EmeraldContainer,
                                dotColor = EmeraldPrimary
                            )
                            StatusChip(
                                text = if (uiState.telemetry.ddakchalConnected) "딱찰 실연동" else "중복방지 풀 대기",
                                textColor = if (uiState.telemetry.ddakchalConnected) MintSecondary else CloudflareSky,
                                backgroundColor = if (uiState.telemetry.ddakchalConnected) EmeraldContainer else CloudflareContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "생성 · 고득점 자동교정 · 주간예약",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "주 1회만 누르면 하루 3~5건씩(주 21~35건) 중복 없이 95점+로 교정해 예약·등록합니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .background(NavySurfaceElevated)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricPair("예약·완료", "${queuedOrPublishedCount}건", MintSecondary, Modifier.weight(1f))
                            MetricPair("주간 목표", "${dailyCount * 7}건/주", CloudflareSky, Modifier.weight(1f))
                            MetricPair("창고 보관", "${uiState.warehouseArticles.size}건", TistoryAmber, Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("하루 발행량 선택 (7일 분산)", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(3, 4, 5).forEach { count ->
                                FilterChip(
                                    selected = dailyCount == count,
                                    onClick = { onSelectWeeklyDailyCount(count) },
                                    label = { Text("일 ${count}건 (주 ${count * 7}건)") },
                                    modifier = Modifier.weight(1f).testTag("chip_daily_$count")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onGenerateWeeklyBatch(dailyCount) },
                            enabled = !uiState.isBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_generate_weekly_7d"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = NavyBackground
                            )
                        ) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "딱찰 연동 7일치(${dailyCount * 7}건) 생성 · 95+ 예약",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onCreateHighScoreAndAutoRegister,
                            enabled = !uiState.isBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_instant_highscore_register"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CloudflareSky,
                                contentColor = NavyBackground
                            )
                        ) {
                            Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "글 1건 바로 생성 → 95+ 교정 → 선택 채널 자동 등록",
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (uiState.weeklyBatchSummary != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = NavyBackground,
                                shape = MaterialTheme.shapes.small,
                                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = uiState.weeklyBatchSummary.summaryText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MintSecondary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Compact 3-Channel Destination Bar (Expandable inline)
        item {
            StudioCard(borderColor = NaverGreen.copy(alpha = 0.4f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("등록 채널 3곳 연결 상태", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        val nvLabel = uiState.config.naverBlogId.ifBlank { "네이버 미입력" }
                        val tsLabel = uiState.config.tistoryBlogUrl.ifBlank { "티스토리 미입력" }
                        Text(
                            text = "네이버($nvLabel) · 티스토리($tsLabel) · 자체웹",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    OutlinedButton(
                        onClick = { showDestinationSetup = !showDestinationSetup },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_toggle_3channel_setup")
                    ) {
                        Text(if (showDestinationSetup) "접기" else "등록처 입력")
                    }
                }

                AnimatedVisibility(visible = showDestinationSetup && selectedSlot != null) {
                    if (selectedSlot != null) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            ThreeChannelDestinationAndNaverVerifyCard(
                                slot = selectedSlot,
                                config = uiState.config,
                                report = uiState.publishVerificationReport,
                                isBusy = uiState.isBusy,
                                onSaveDestinations = onSaveThreeChannelDestinations,
                                onPublishNaverDirectAndVerify = { url ->
                                    onPublishNaverDirectAndVerify(selectedSlot.postId, url)
                                },
                                onPublishSelectedChannelsAndVerify = { url ->
                                    onPublishSelectedChannelsAndVerify(selectedSlot.postId, url)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Active CAPTCHA / 2FA handOff Alert (only when triggered)
        if (!uiState.activeHandoffAlert.isNullOrBlank()) {
            item {
                StudioCard(borderColor = CrimsonAlert, containerColor = CrimsonContainer) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, contentDescription = "세션 중단 경보", tint = CrimsonAlert)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("계정 보호 일시정지 (세션 확인 필요)", style = MaterialTheme.typography.titleSmall, color = CrimsonAlert)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(uiState.activeHandoffAlert, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onResolveHandoff,
                        modifier = Modifier.height(48.dp).testTag("btn_resolve_handoff"),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                    ) {
                        Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("세션 복구")
                    }
                }
            }
        }

        // 3. Clean Queue List (Only essentials visible)
        item {
            SectionHeader(
                title = "자동 예약 · 등록 목록 (${uiState.slots.size}건)",
                subtitle = "일일 ${dailyCount}건 분산 · 95점+ 통과 원고만 등록"
            )
        }

        items(uiState.slots, key = { it.postId }) { slot ->
            SimpleQueueRowCard(
                slot = slot,
                config = uiState.config,
                proFunnel = uiState.proFunnel,
                isBusy = uiState.isBusy,
                onRegisterNow = { onAutoFixAndRegisterSlot(slot.postId) },
                onInspect = { onInspectSlot(slot.postId) }
            )
        }

        // 4. Collapsible Advanced Tools (Hidden by default to keep screen ultra-simple)
        item {
            OutlinedButton(
                onClick = { showAdvancedTools = !showAdvancedTools },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_toggle_advanced_dashboard")
            ) {
                Icon(
                    imageVector = if (showAdvancedTools) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (showAdvancedTools) "고급 분석 · 수동 추가 도구 접기" else "고급 분석 · AI 채점 · 수동 물건 추가 열기")
            }

            AnimatedVisibility(visible = showAdvancedTools) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.weight(1f).height(48.dp).testTag("btn_add_crawled_case")
                        ) {
                            Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("물건 직접 추가", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = onSyncPack,
                            enabled = !uiState.isBusy,
                            modifier = Modifier.weight(1f).height(48.dp).testTag("btn_sync_syndication")
                        ) {
                            Icon(Icons.Filled.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("딱찰 동기화", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = onOpenWarehouse,
                            modifier = Modifier.weight(1f).height(48.dp).testTag("btn_open_warehouse_shortcut")
                        ) {
                            Icon(Icons.Filled.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("창고열기", maxLines = 1)
                        }
                    }

                    val activeCaseId = selectedSlot?.auctionCase?.caseId
                    AiSeoGeoEvaluatorDashboardCard(
                        selectedCaseId = activeCaseId,
                        aiEvalState = uiState.aiEvalState,
                        isBusy = uiState.isBusy,
                        onRunAiEvalSelected = onRunAiEvalSelected,
                        onTestBusanProfitableLand = onTestBusanProfitableLand,
                        onTestLowScoreDraft = onTestLowScoreDraft,
                        onApplyAiAutoFixes = onApplyAiAutoFixes
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddCrawledCaseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { caseId, court, addr, type, appMan, minMan, baseDate, rights, pubDate ->
                onAddCustomProperty(caseId, court, addr, type, appMan, minMan, baseDate, rights, pubDate)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun SimpleQueueRowCard(
    slot: GeneratedPostSlot,
    config: com.example.model.ServerAutomationConfig,
    proFunnel: com.example.model.ProFunnelConfig,
    isBusy: Boolean,
    onRegisterNow: () -> Unit,
    onInspect: () -> Unit
) {
    val scoreResult = remember(slot.postId, slot.variants, slot.qaReport) {
        val reg = RegistryInfographicEngine.buildTemplate(slot.auctionCase)
        Article30PointEvaluator.evaluate(slot, config, proFunnel, reg)
    }
    val scoreColor = if (scoreResult.eligibleForRegistration) MintSecondary else TistoryAmber

    StudioCard(
        borderColor = if (slot.status == PostStatus.PUBLISHED) EmeraldPrimary.copy(alpha = 0.55f) else NavyBorder,
        modifier = Modifier
            .clickable(onClick = onInspect)
            .testTag("slot_card_${slot.slotIndex}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${slot.auctionCase.caseId} · ${slot.auctionCase.propertyType.displayLabel}",
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
                    color = TextPrimary
                )
                Text(
                    text = slot.auctionCase.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            StatusChip(
                text = "${scoreResult.totalScore}점",
                textColor = scoreColor,
                backgroundColor = if (scoreResult.eligibleForRegistration) EmeraldContainer else NavySurfaceElevated
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "예약일 ${slot.publishDate} · 최저 ${QaVerificationEngine.formatKrwShort(slot.auctionCase.minBidPriceKrw)}",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )
            PostStatusBadge(status = slot.status)
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onRegisterNow,
                enabled = !isBusy,
                modifier = Modifier
                    .weight(1.3f)
                    .height(48.dp)
                    .testTag("btn_schedule_${slot.slotIndex}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (slot.status == PostStatus.PUBLISHED) CloudflareSky else EmeraldPrimary,
                    contentColor = NavyBackground
                )
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (slot.status == PostStatus.PUBLISHED) "재검증·등록" else "95+ 자동 등록",
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            OutlinedButton(
                onClick = onInspect,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_inspect_${slot.slotIndex}")
            ) {
                Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("상세", maxLines = 1)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCrawledCaseDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        caseId: String,
        courtName: String,
        address: String,
        propertyType: PropertyType,
        appraisalManWon: Long,
        minBidManWon: Long,
        baseRightDate: String,
        assumedRights: String,
        publishDate: String
    ) -> Unit
) {
    var caseId by remember { mutableStateOf("2026타경3318") }
    var courtName by remember { mutableStateOf("서울중앙지방법원") }
    var address by remember { mutableStateOf("서울특별시 서초구 반포동 아크로리버파크") }
    var selectedType by remember { mutableStateOf(PropertyType.APT_OFFICETEL) }
    var appraisalMan by remember { mutableStateOf("320000") }
    var minBidMan by remember { mutableStateOf("204800") }
    var baseRightDate by remember { mutableStateOf("2021-04-19") }
    var assumedRights by remember { mutableStateOf("말소기준권리 이후 후순위 소멸, 관리비 체납 현장 확인") }
    var publishDate by remember { mutableStateOf("2026-11-23") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("신규 경매 물건 추가", color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PropertyType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.displayLabel, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
                OutlinedTextField(value = caseId, onValueChange = { caseId = it }, label = { Text("사건번호") }, singleLine = true)
                OutlinedTextField(value = courtName, onValueChange = { courtName = it }, label = { Text("법원명") }, singleLine = true)
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("소재지/물건명") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = appraisalMan,
                        onValueChange = { appraisalMan = it.filter(Char::isDigit) },
                        label = { Text("감정가(만원)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minBidMan,
                        onValueChange = { minBidMan = it.filter(Char::isDigit) },
                        label = { Text("최저가(만원)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = baseRightDate,
                        onValueChange = { baseRightDate = it },
                        label = { Text("말소기준권리일") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = publishDate,
                        onValueChange = { publishDate = it },
                        label = { Text("발행예약일") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(value = assumedRights, onValueChange = { assumedRights = it }, label = { Text("인수권리 및 특이사항") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        caseId,
                        courtName,
                        address,
                        selectedType,
                        appraisalMan.toLongOrNull() ?: 320000L,
                        minBidMan.toLongOrNull() ?: 204800L,
                        baseRightDate,
                        assumedRights,
                        publishDate
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
            ) {
                Text("생성 및 예약")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = TextSecondary)
            }
        }
    )
}
