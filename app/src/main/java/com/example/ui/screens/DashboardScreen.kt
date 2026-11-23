package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.engine.CourtEvidenceRecorder
import com.example.engine.ProAnalysisAndPhotoGuideEngine
import com.example.engine.QaVerificationEngine
import com.example.model.GeneratedPostSlot
import com.example.model.PropertyType
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
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyContainerHighest
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TistoryAmber

@Composable
fun DashboardScreen(
    uiState: StudioUiState,
    onTriggerDaily5: () -> Unit,
    onInstantCreateAndStore: () -> Unit,
    onOpenWarehouse: () -> Unit,
    onRunAiEvalSelected: () -> Unit,
    onTestBusanProfitableLand: () -> Unit,
    onTestLowScoreDraft: () -> Unit,
    onApplyAiAutoFixes: (String) -> Unit,
    onSyncPack: () -> Unit,
    onInspectSlot: (String) -> Unit,
    onScheduleSlot: (String) -> Unit,
    onRewriteSlot: (String) -> Unit,
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

    val totalSlots = uiState.slots.size
    val passedQaCount = uiState.slots.count { it.qaReport.overallPassed }
    val funnelEquippedCount = uiState.slots.count { slot ->
        slot.variants.values.all { it.infographicCards.size >= 5 }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. 30-Year Veteran / 100+ Staff Brokerage Executive Control Header
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = NavySurface,
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.img_studio_hero),
                        contentDescription = "딱찰 30년 경매중개법인 총괄 관제 헤더",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(0.18f)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        NavyBackground.copy(alpha = 0.55f),
                                        NavySurface.copy(alpha = 0.95f)
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
                                text = "EST.1996 · 30년+ · 100인+ 전담법인",
                                textColor = MintSecondary,
                                backgroundColor = EmeraldContainer,
                                dotColor = EmeraldPrimary
                            )
                            StatusChip(
                                text = if (uiState.telemetry.cloudflareD1Connected) "자동화 가동" else "스튜디오 대기",
                                textColor = if (uiState.telemetry.cloudflareD1Connected) MintSecondary else CloudflareSky,
                                backgroundColor = if (uiState.telemetry.cloudflareD1Connected) EmeraldContainer else CloudflareContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Business, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "딱찰 경매법인 총괄 관제실",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "유료급 물건분석·사진가이드 무료 개방 → 딱찰 PRO 자동 전환",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val quotaRatio = (uiState.telemetry.dailyQuotaUsed.toFloat() /
                            uiState.config.dailyPostLimit.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("오늘 발행 할당", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                text = "${uiState.telemetry.dailyQuotaUsed} / ${uiState.config.dailyPostLimit}건",
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                                color = MintSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { quotaRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = EmeraldPrimary,
                            trackColor = NavyContainerHighest
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Primary One-Touch Execution Button
                        Button(
                            onClick = onTriggerDaily5,
                            enabled = !uiState.isBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_generate_daily_5"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = NavyBackground
                            )
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "원터치 일일 발행 · 3채널 자동 배정",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onInstantCreateAndStore,
                                enabled = !uiState.isBusy,
                                modifier = Modifier
                                    .weight(1.35f)
                                    .height(48.dp)
                                    .testTag("btn_dashboard_instant_warehouse"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CloudflareSky,
                                    contentColor = NavyBackground
                                )
                            ) {
                                Icon(Icons.Filled.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "글 바로 생성·창고보관",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            OutlinedButton(
                                onClick = onOpenWarehouse,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_open_warehouse_shortcut"),
                                border = BorderStroke(1.dp, TistoryAmber.copy(alpha = 0.55f))
                            ) {
                                Text(
                                    "창고(${uiState.warehouseArticles.size})·로그",
                                    color = TistoryAmber,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showAddDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_add_crawled_case"),
                                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f))
                            ) {
                                Icon(Icons.Filled.AddCircle, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("신규 물건 추가", color = MintSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            OutlinedButton(
                                onClick = onSyncPack,
                                enabled = !uiState.isBusy,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_sync_syndication"),
                                border = BorderStroke(1.dp, CloudflareSky.copy(alpha = 0.45f))
                            ) {
                                Icon(Icons.Filled.CloudSync, contentDescription = null, tint = CloudflareSky, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("서버 동기화", color = CloudflareSky, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }

        // 2. Core 3 Executive KPIs (At-a-Glance)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExecutiveKpiCard(
                    label = "오늘 물건 큐",
                    value = "${totalSlots}건",
                    sub = "한도 ${uiState.config.dailyPostLimit}건",
                    accent = CloudflareSky,
                    modifier = Modifier.weight(1f)
                )
                ExecutiveKpiCard(
                    label = "저품질 안전검수",
                    value = "$passedQaCount/$totalSlots",
                    sub = if (passedQaCount == totalSlots) "전원 PASS" else "교정 필요",
                    accent = if (passedQaCount == totalSlots) MintSecondary else TistoryAmber,
                    modifier = Modifier.weight(1f)
                )
                ExecutiveKpiCard(
                    label = "PRO 무료·전환",
                    value = if (uiState.proFunnel.funnelEnabled && funnelEquippedCount == totalSlots) "100%" else "${funnelEquippedCount}건",
                    sub = "사진가이드+CTA",
                    accent = TistoryAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 2.5 AI 30-item SEO/GEO Scoring & <95 Auto-Fix Suggestions Card + Busan <50M Land Test
        item {
            val activeCaseId = uiState.slots.firstOrNull { it.postId == uiState.selectedSlotId }?.auctionCase?.caseId
                ?: uiState.slots.firstOrNull()?.auctionCase?.caseId
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

        // Active CAPTCHA / 2FA handOff Alert (only shown when needed)
        if (!uiState.activeHandoffAlert.isNullOrBlank()) {
            item {
                StudioCard(borderColor = CrimsonAlert, containerColor = CrimsonContainer) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, contentDescription = "보안문자 중단 경보", tint = CrimsonAlert)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("계정 보호 자동 일시정지 (수동 1회 확인 필요)", style = MaterialTheme.typography.titleSmall, color = CrimsonAlert)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(uiState.activeHandoffAlert, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onResolveHandoff,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_resolve_handoff"),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                    ) {
                        Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("세션 확인 완료 · 발행 재개")
                    }
                }
            }
        }

        // 3. Simple Executive Queue List
        item {
            SectionHeader(
                title = "오늘의 자동 발행 물건 (${uiState.slots.size}건)",
                subtitle = "1순위 자체웹 → 2순위 티스토리 → 3순위 네이버 시간차 발행"
            )
        }

        items(uiState.slots, key = { it.postId }) { slot ->
            SimpleExecutiveSlotCard(
                slot = slot,
                funnelEnabled = uiState.proFunnel.funnelEnabled,
                onInspect = { onInspectSlot(slot.postId) },
                onSchedule = { onScheduleSlot(slot.postId) },
                onRewrite = { onRewriteSlot(slot.postId) }
            )
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
private fun ExecutiveKpiCard(
    label: String,
    value: String,
    sub: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = NavySurface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = accent,
                maxLines = 1
            )
            Text(sub, style = MaterialTheme.typography.labelSmall, color = TextMuted, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SimpleExecutiveSlotCard(
    slot: GeneratedPostSlot,
    funnelEnabled: Boolean,
    onInspect: () -> Unit,
    onSchedule: () -> Unit,
    onRewrite: () -> Unit
) {
    val qa = slot.qaReport
    val proReport = remember(slot.auctionCase.caseId, slot.auctionCase.minBidPriceKrw) {
        ProAnalysisAndPhotoGuideEngine.buildProReport(slot.auctionCase)
    }

    StudioCard(
        borderColor = if (qa.overallPassed) NavyBorder else CrimsonAlert,
        modifier = Modifier
            .clickable(onClick = onInspect)
            .testTag("slot_card_${slot.slotIndex}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text(
                    text = slot.auctionCase.caseId,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = slot.auctionCase.propertyType.displayLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = CloudflareSky,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            PostStatusBadge(status = slot.status)
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = slot.auctionCase.address,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${slot.auctionCase.courtName} · 매각일 ${slot.auctionCase.auctionDate}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(NavySurfaceElevated)
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricPair(
                label = "감정가",
                value = QaVerificationEngine.formatKrwShort(slot.auctionCase.appraisalPriceKrw)
            )
            MetricPair(
                label = "최저가 (${slot.auctionCase.discountRatePct}%)",
                value = QaVerificationEngine.formatKrwShort(slot.auctionCase.minBidPriceKrw),
                valueColor = MintSecondary
            )
            MetricPair(
                label = "안전마진",
                value = QaVerificationEngine.formatKrwShort(proReport.safetyMarginKrw),
                valueColor = CloudflareSky
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        val courtPhotos = remember(slot.auctionCase.caseId) {
            CourtEvidenceRecorder.recordFullArchive(slot.auctionCase).photos
        }
        CourtPhotoStripMini(
            auction = slot.auctionCase,
            photos = courtPhotos,
            onClickPhoto = onInspect
        )

        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatusChip(
                text = if (qa.overallPassed) "3채널 QA·유사도(${qa.maxCrossChannelCosinePct}%) PASS" else "검수 보완 필요",
                textColor = if (qa.overallPassed) MintSecondary else CrimsonAlert,
                backgroundColor = if (qa.overallPassed) EmeraldContainer else CrimsonContainer
            )
            if (funnelEnabled) {
                StatusChip(
                    text = "법원사진 ${courtPhotos.size}컷 · 문건 3종 · 등기/권리 기록",
                    textColor = TistoryAmber,
                    backgroundColor = AmberContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onInspect,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_inspect_${slot.slotIndex}")
            ) {
                Text("상세 · 사진가이드", maxLines = 1)
            }
            if (!qa.overallPassed) {
                Button(
                    onClick = onRewrite,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_rewrite_${slot.slotIndex}"),
                    colors = ButtonDefaults.buttonColors(containerColor = TistoryAmber, contentColor = NavyBackground)
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("중복 교정", maxLines = 1)
                }
            } else {
                Button(
                    onClick = onSchedule,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_schedule_${slot.slotIndex}"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("바로 예약", maxLines = 1)
                }
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
    var publishDate by remember { mutableStateOf("2026-11-22") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("신규 경매 물건 3채널 예약 생성", color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "유료급 권리·세무·현장 사진 가이드 무료 원고와 PRO 전환 퍼널이 자동 생성됩니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
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
