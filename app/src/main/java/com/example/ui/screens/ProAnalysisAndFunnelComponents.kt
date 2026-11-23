package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.engine.QaVerificationEngine
import com.example.model.AuctionCaseIntel
import com.example.model.ChannelPlatform
import com.example.model.ChannelVariantContent
import com.example.model.FieldPhotoGuideShot
import com.example.model.InfographicCardSpec
import com.example.model.ProAuctionAnalysisReport
import com.example.model.ProFunnelConfig
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
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TistoryAmber

@Composable
fun ProFreeAnalysisAccordionCard(
    auction: AuctionCaseIntel,
    report: ProAuctionAnalysisReport
) {
    var expanded by remember(auction.caseId) { mutableStateOf(true) }

    StudioCard(
        borderColor = EmeraldPrimary.copy(alpha = 0.55f),
        modifier = Modifier.testTag("pro_free_analysis_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.AccountBalance, contentDescription = "유료급 무료 물건분석", tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "30년·100인 법인 유료급 무료 물건분석",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "배당 워터폴 · LTV70/ROE · 세무 3안 · 명도 시뮬레이션",
                        style = MaterialTheme.typography.bodySmall,
                        color = MintSecondary
                    )
                }
            }
            IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "접기" else "펼치기",
                    tint = TextSecondary
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(NavySurfaceElevated)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricPair(
                        label = "시세 안전마진",
                        value = "${QaVerificationEngine.formatKrwShort(report.safetyMarginKrw)} (${report.safetyMarginPct}%)",
                        valueColor = MintSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricPair(
                        label = "LTV70 실투자금",
                        value = QaVerificationEngine.formatKrwShort(report.netEquityRequiredKrw),
                        valueColor = CloudflareSky,
                        modifier = Modifier.weight(1f)
                    )
                    MetricPair(
                        label = "CapRate / ROE",
                        value = "${report.estimatedCapRatePct}% / ${report.estimated12mRoiPct}%",
                        valueColor = TistoryAmber,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(NavySurfaceElevated)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricPair(
                        label = "손익분기 매도가(BEP)",
                        value = QaVerificationEngine.formatKrwShort(report.breakevenResalePriceKrw),
                        valueColor = MintSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricPair(
                        label = "차회 유찰예상(80%)",
                        value = QaVerificationEngine.formatKrwShort(report.nextDropMinBidKrw),
                        valueColor = CloudflareSky,
                        modifier = Modifier.weight(1f)
                    )
                    MetricPair(
                        label = "금리 +1%p 월부담",
                        value = "+${QaVerificationEngine.formatKrwShort(report.rateStressMonthlyDeltaKrw)}/월",
                        valueColor = TistoryAmber,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "명도 예상: ${report.evictionRiskLevel} · 약 ${report.estimatedEvictionDays}일 · 예비비 ${QaVerificationEngine.formatKrwShort(report.estimatedEvictionReserveKrw)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (report.privacyMaskedConfirmed) {
                        StatusChip(
                            text = "개인정보 100% 비식별 PASS",
                            textColor = MintSecondary,
                            backgroundColor = EmeraldContainer
                        )
                    }
                }

                Text("4단계 배당 워터폴", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    report.dividendWaterfall.forEach { step ->
                        val isRisk = step.absorbedByBuyerKrw > 0L
                        Surface(
                            color = if (isRisk) CrimsonContainer.copy(alpha = 0.45f) else NavyBackground,
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(1.dp, if (isRisk) CrimsonAlert else NavyBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${step.priorityRank}순위 · ${step.creditorLabel}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                    Text(
                                        "채권 ${QaVerificationEngine.formatKrwShort(step.claimAmountKrw)} / 배당 ${QaVerificationEngine.formatKrwShort(step.distributedKrw)}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                        color = TextMuted
                                    )
                                }
                                StatusChip(
                                    text = step.statusTag,
                                    textColor = if (isRisk) CrimsonAlert else MintSecondary,
                                    backgroundColor = if (isRisk) CrimsonContainer else EmeraldContainer
                                )
                            }
                        }
                    }
                }

                Text("명의별 세무 3안 비교", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    report.taxScenarios.forEach { tax ->
                        Surface(
                            color = NavyBackground,
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(1.dp, NavyBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(tax.entityType, style = MaterialTheme.typography.labelMedium, color = CloudflareSky)
                                    Text("취득세 ${tax.acquisitionTaxPct}", style = MaterialTheme.typography.labelSmall, color = TistoryAmber)
                                }
                                Text(tax.capitalGainsTaxNote, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("추천: ${tax.recommendedWhen}", style = MaterialTheme.typography.labelSmall, color = MintSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FieldPhotoGuideCard(
    propertyLabel: String,
    shots: List<FieldPhotoGuideShot>
) {
    val clipboard = LocalClipboardManager.current
    var expanded by remember { mutableStateOf(true) }

    StudioCard(
        borderColor = CloudflareSky.copy(alpha = 0.5f),
        modifier = Modifier.testTag("field_photo_guide_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.CameraAlt, contentDescription = "현장 임장 사진 가이드", tint = CloudflareSky)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "$propertyLabel 현장 임장 사진 5컷 촬영 가이드",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "유료 임장앱 유료체크 항목 100% 공개 · 블로그 캡션 복사 지원",
                        style = MaterialTheme.typography.bodySmall,
                        color = CloudflareSky
                    )
                }
            }
            IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "접기" else "펼치기",
                    tint = TextSecondary
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                shots.forEach { shot ->
                    Surface(
                        color = NavyBackground,
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "컷 ${shot.shotNumber}. ${shot.targetTitle}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MintSecondary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { clipboard.setText(AnnotatedString(shot.blogCaptionTemplate)) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = "사진 캡션 복사", tint = CloudflareSky, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text("촬영 포인트: ${shot.checkPoint}", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("적신호: ${shot.redFlagSignal}", style = MaterialTheme.typography.labelSmall, color = CrimsonAlert)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfographicVisualGallerySection(
    auction: AuctionCaseIntel,
    variant: ChannelVariantContent,
    report: ProAuctionAnalysisReport,
    funnel: ProFunnelConfig,
    onShareCardPng: (InfographicCardSpec) -> Unit
) {
    val cards = variant.infographicCards
    if (cards.isEmpty()) return
    var selectedIdx by remember(variant.platform, auction.caseId) { mutableIntStateOf(0) }
    val activeCard = cards[selectedIdx.coerceIn(0, cards.lastIndex)]
    val accent = channelAccentColor(variant.platform)
    val aspect = when (variant.platform) {
        ChannelPlatform.CLOUDFLARE -> 1200f / 630f
        ChannelPlatform.TISTORY -> 1200f / 675f
        ChannelPlatform.NAVER -> 1.15f
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "채널 전용 인포그래픽 스튜디오 (${cards.size}종 · EXIF 삭제 · 실시간 렌더링)",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            cards.forEachIndexed { idx, card ->
                FilterChip(
                    selected = idx == selectedIdx,
                    onClick = { selectedIdx = idx },
                    label = { Text("${card.cardIndex}. ${shortCardLabel(card.cardType)}") }
                )
            }
        }

        // Visual Canvas-backed Preview Card
        Surface(
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.5.dp, accent.copy(alpha = 0.7f)),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspect)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(NavyBackground, NavySurface, EmeraldContainer.copy(alpha = 0.65f))
                        )
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    drawRoundRect(
                        color = accent.copy(alpha = 0.25f),
                        topLeft = Offset(12.dp.toPx(), 12.dp.toPx()),
                        size = Size(w - 24.dp.toPx(), h - 24.dp.toPx()),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                    )
                    // Visual bar accent at bottom
                    val barY = h - 38.dp.toPx()
                    drawRoundRect(
                        color = EmeraldPrimary.copy(alpha = 0.7f),
                        topLeft = Offset(24.dp.toPx(), barY),
                        size = Size((w - 48.dp.toPx()) * 0.68f, 6.dp.toPx()),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                    drawRoundRect(
                        color = CloudflareSky.copy(alpha = 0.6f),
                        topLeft = Offset(24.dp.toPx() + (w - 48.dp.toPx()) * 0.7f, barY),
                        size = Size((w - 48.dp.toPx()) * 0.3f, 6.dp.toPx()),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusChip(
                            text = "EST.1996 · 30년+ · 100인+ 법인",
                            textColor = MintSecondary,
                            backgroundColor = EmeraldContainer
                        )
                        Text(
                            text = "${activeCard.aspectRatio} · #${activeCard.cardIndex}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                            color = accent
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = activeCard.headline,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        val visualDetail = when (activeCard.cardType) {
                            "THUMBNAIL_SUMMARY" -> "감정 ${QaVerificationEngine.formatKrwShort(auction.appraisalPriceKrw)} → 최저 ${QaVerificationEngine.formatKrwShort(auction.minBidPriceKrw)} (안전마진 ${report.safetyMarginPct}%)"
                            "COURT_DOCS_RIGHTS" -> "말소기준 ${auction.baseRightDate} · ${report.evictionRiskLevel}"
                            "STATS_3Y_AND_TRADES" -> "실거래 ${QaVerificationEngine.formatKrwShort(auction.recentRealTradeMedianKrw)} · CapRate ${report.estimatedCapRatePct}% · ROE ${report.estimated12mRoiPct}%"
                            "BID_TAX_WATERFALL" -> "보수 ${QaVerificationEngine.formatKrwShort(auction.conservativeBidKrw)} · 균형 ${QaVerificationEngine.formatKrwShort(auction.balancedBidKrw)} · 세무 3안"
                            "FIELD_PHOTO_GUIDE" -> report.photoGuideShots.take(3).joinToString(" / ") { "${it.shotNumber}.${it.targetTitle}" }
                            else -> "네이버 '${funnel.naverSearchKeyword} ${auction.caseId}' · ${funnel.proLockedItems.firstOrNull().orEmpty()}"
                        }
                        Text(
                            text = visualDetail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MintSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "EXIF 삭제 · MD5:${activeCard.md5Hash} · SHA256:${activeCard.sha256Hash}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                        color = TextMuted
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = activeCard.seoFilename,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Button(
                onClick = { onShareCardPng(activeCard) },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("btn_share_infographic_png"),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = NavyBackground)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("PNG 공유")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProFunnelSettingsCard(
    funnel: ProFunnelConfig,
    onSaveFunnel: (ProFunnelConfig) -> Unit,
    onResetFunnel: () -> Unit
) {
    var showEditor by remember { mutableStateOf(false) }

    StudioCard(borderColor = TistoryAmber.copy(alpha = 0.6f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.RocketLaunch, contentDescription = "PRO 전환 퍼널 설정", tint = TistoryAmber)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "딱찰 PRO 유료 전환 퍼널 관리",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "모든 블로그 글 하단에 저품질 없이 유료 고급정보로 유도",
                        style = MaterialTheme.typography.bodySmall,
                        color = TistoryAmber
                    )
                }
            }
            IconButton(onClick = onResetFunnel, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.RestartAlt, contentDescription = "퍼널 기본값 복원", tint = CloudflareSky)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            StatusChip(
                text = if (funnel.funnelEnabled) "전 글 PRO 퍼널 ON" else "퍼널 비활성",
                textColor = if (funnel.funnelEnabled) MintSecondary else CrimsonAlert,
                backgroundColor = if (funnel.funnelEnabled) EmeraldContainer else CrimsonContainer
            )
            StatusChip(
                text = "네이버 검색어: ${funnel.naverSearchKeyword}",
                textColor = CloudflareSky,
                backgroundColor = CloudflareContainer
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("랜딩 URL: ${funnel.proLandingUrl}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text("티저 문구: ${funnel.teaserHeadline}", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { showEditor = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_edit_pro_funnel")
        ) {
            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("PRO 전환 URL · 티저 · 고정댓글 편집")
        }
    }

    if (showEditor) {
        ProFunnelEditorDialog(
            initial = funnel,
            onDismiss = { showEditor = false },
            onSave = { updated ->
                onSaveFunnel(updated)
                showEditor = false
            }
        )
    }
}

@Composable
private fun ProFunnelEditorDialog(
    initial: ProFunnelConfig,
    onDismiss: () -> Unit,
    onSave: (ProFunnelConfig) -> Unit
) {
    var enabled by remember(initial) { mutableStateOf(initial.funnelEnabled) }
    var landingUrl by remember(initial) { mutableStateOf(initial.proLandingUrl) }
    var searchKeyword by remember(initial) { mutableStateOf(initial.naverSearchKeyword) }
    var headline by remember(initial) { mutableStateOf(initial.teaserHeadline) }
    var lockedText by remember(initial) { mutableStateOf(initial.proLockedItems.joinToString("\n")) }
    var pinnedComment by remember(initial) { mutableStateOf(initial.naverPinnedCommentTemplate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("딱찰 PRO 유료 전환 퍼널 설정", color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("모든 블로그 글 하단 PRO 유도 자동 삽입", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
                OutlinedTextField(
                    value = landingUrl,
                    onValueChange = { landingUrl = it },
                    label = { Text("딱찰 PRO 랜딩 URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = searchKeyword,
                    onValueChange = { searchKeyword = it },
                    label = { Text("네이버 무링크 검색 유도 키워드") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = headline,
                    onValueChange = { headline = it },
                    label = { Text("티저 배너 헤드라인") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lockedText,
                    onValueChange = { lockedText = it },
                    label = { Text("딱찰 PRO 전용 잠금 고급정보 (줄바꿈 구분)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pinnedComment,
                    onValueChange = { pinnedComment = it },
                    label = { Text("네이버 고정댓글 템플릿 ({사건번호}, {검색어})") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        initial.copy(
                            funnelEnabled = enabled,
                            proLandingUrl = landingUrl,
                            naverSearchKeyword = searchKeyword,
                            teaserHeadline = headline,
                            proLockedItems = lockedText.lines().map { it.trim() }.filter { it.isNotBlank() },
                            naverPinnedCommentTemplate = pinnedComment
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("저장 및 원고 반영")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = TextSecondary)
            }
        }
    )
}

private fun shortCardLabel(cardType: String): String = when (cardType) {
    "THUMBNAIL_SUMMARY" -> "핵심요약"
    "COURT_DOCS_RIGHTS" -> "배당워터폴"
    "STATS_3Y_AND_TRADES" -> "수익률·통계"
    "BID_TAX_WATERFALL" -> "입찰·세무3안"
    "FIELD_PHOTO_GUIDE" -> "임장사진5컷"
    "PRO_FUNNEL_CTA_BANNER" -> "PRO전환배너"
    else -> cardType
}
