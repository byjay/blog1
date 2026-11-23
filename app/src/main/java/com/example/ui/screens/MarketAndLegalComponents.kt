package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.QaVerificationEngine
import com.example.model.AuctionCaseIntel
import com.example.model.LegalDoctrineReport
import com.example.model.LegalVerificationSeverity
import com.example.model.MarketApiSourceStatus
import com.example.model.NearbyMarketAnalysisReport
import com.example.model.QualityDefenseModuleState
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
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TistoryAmber

@Composable
fun QualityDefensePanelCard(
    auction: AuctionCaseIntel,
    defenseState: QualityDefenseModuleState,
    isBusy: Boolean,
    onToggleAutoDefense: (Boolean) -> Unit,
    onRunQualityDefense: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lastRes = defenseState.lastResult?.takeIf {
        defenseState.lastAuditedCaseId.isBlank() || defenseState.lastAuditedCaseId == auction.caseId
    }
    val purityPct = lastRes?.humanToneScorePct ?: 100
    val hits = lastRes?.hits.orEmpty()

    StudioCard(
        modifier = modifier,
        borderColor = if (hits.isEmpty()) EmeraldPrimary.copy(alpha = 0.55f) else TistoryAmber.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Shield, contentDescription = "Quality Defense 품질 방어", tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("ViewModel Quality Defense (탈AI·금칙어 자동치환)", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Text(
                        "누적 자동정화 ${defenseState.totalAutoFixedCount}건 · 기준: 30년 베테랑 에세이 문체",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
            Switch(
                checked = defenseState.autoDefenseEnabled,
                onCheckedChange = onToggleAutoDefense,
                modifier = Modifier.testTag("switch_auto_quality_defense")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "사람 문체 순도 (${defenseState.lastChannel.displayName})",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = "${purityPct}%",
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                color = if (purityPct >= 90) MintSecondary else TistoryAmber
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (purityPct / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (purityPct >= 90) EmeraldPrimary else TistoryAmber,
            trackColor = NavyContainerHighest
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = lastRes?.summary ?: "원고 작성·리라이팅 시 SEO 민감 패턴(■·Q1~Q6·상용어미·이모지·개인정보)을 자동 감지·치환합니다.",
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary
        )

        if (hits.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            hits.forEach { hit ->
                Surface(
                    color = NavySurfaceElevated,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, TistoryAmber.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "${hit.category.label} (${hit.count}건 감지)",
                            style = MaterialTheme.typography.labelMedium,
                            color = TistoryAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "탐지: ${hit.matchedSnippet} → 치환: ${hit.replacementSnippet}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onRunQualityDefense,
            enabled = !isBusy,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_run_quality_defense")
        ) {
            Icon(Icons.Filled.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Quality Defense 즉시 탐지·사람문체 치환")
        }
    }
}

@Composable
fun NearbyMarketAnalysisCard(
    auction: AuctionCaseIntel,
    report: NearbyMarketAnalysisReport?,
    isFetching: Boolean,
    onFetchNaverMarket: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = report?.status ?: MarketApiSourceStatus.IDLE
    val statusColors = when (status) {
        MarketApiSourceStatus.LIVE_FULL,
        MarketApiSourceStatus.LIVE_LAND_ONLY -> MintSecondary to EmeraldContainer
        MarketApiSourceStatus.LIVE_SEARCH_ONLY -> CloudflareSky to CloudflareContainer
        MarketApiSourceStatus.API_KEY_REQUIRED -> TistoryAmber to AmberContainer
        MarketApiSourceStatus.RATE_LIMITED_OR_ERROR -> CrimsonAlert to CrimsonContainer
        MarketApiSourceStatus.IDLE -> TextSecondary to NavySurfaceElevated
    }

    StudioCard(
        modifier = modifier,
        borderColor = NaverGreen.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Apartment, contentDescription = "네이버 부동산 시세 분석", tint = NaverGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("네이버 무료 API 주변 시세 · 입찰가 재산정", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            }
            StatusChip(
                text = status.label,
                textColor = statusColors.first,
                backgroundColor = statusColors.second
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = report?.statusDetail ?: "네이버 개발자 검색API(local/blog)와 네이버 부동산 공개 매물(m.land.naver.com)을 조회해 인근 급매 호가 기준으로 적정 입찰가를 재산정합니다.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricMiniTile(
                label = "급매 최저 호가",
                value = report?.urgentAskMinKrw?.let { QaVerificationEngine.formatKrwShort(it) } ?: "조회 대기",
                accent = TistoryAmber,
                modifier = Modifier.weight(1f)
            )
            MetricMiniTile(
                label = "인근 호가 중위",
                value = report?.nearbyAskMedianKrw?.let { QaVerificationEngine.formatKrwShort(it) } ?: "조회 대기",
                accent = CloudflareSky,
                modifier = Modifier.weight(1f)
            )
            MetricMiniTile(
                label = "실거래 기준선",
                value = QaVerificationEngine.formatKrwShort(report?.realTradeBenchmarkKrw ?: auction.recentRealTradeMedianKrw),
                accent = MintSecondary,
                modifier = Modifier.weight(1f)
            )
        }

        report?.recalculatedBids?.let { bids ->
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = NavySurfaceElevated,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "시세·법리 부담 반영 3전략 적정입찰가 재산정 결과",
                        style = MaterialTheme.typography.labelLarge,
                        color = MintSecondary
                    )
                    Text(
                        text = "보수 ${QaVerificationEngine.formatKrwShort(bids.conservativeBidKrw)} · 적정 ${QaVerificationEngine.formatKrwShort(bids.balancedBidKrw)} · 공격 ${QaVerificationEngine.formatKrwShort(bids.aggressiveBidKrw)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFamily),
                        color = TextPrimary
                    )
                    Text(
                        text = "급매가 대비 안전마진 ${QaVerificationEngine.formatKrwShort(bids.urgentAskMarginKrw)} (${bids.urgentAskMarginPct}%)",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (bids.urgentAskMarginKrw >= 0L) MintSecondary else CrimsonAlert
                    )
                    Text(bids.safetyRuleNote, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }
        }

        if (report != null && report.comps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text("수집된 인근 네이버 부동산 실매물 (${report.comps.size}건)", style = MaterialTheme.typography.labelLarge, color = NaverGreen)
            Spacer(modifier = Modifier.height(4.dp))
            report.comps.take(4).forEach { comp ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${if (comp.isUrgentSale) "[급매] " else ""}${comp.title} (${comp.areaSqmLabel}, ${comp.floorInfo})",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${comp.tradeType} ${QaVerificationEngine.formatKrwShort(comp.priceKrw)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                        color = if (comp.isUrgentSale) TistoryAmber else MintSecondary
                    )
                }
            }
        }

        if (report != null && report.blogTrendTitles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("네이버 검색API 주변 시세 동향 (${report.blogTrendTitles.size}건)", style = MaterialTheme.typography.labelMedium, color = CloudflareSky)
            report.blogTrendTitles.take(3).forEach { title ->
                Text("• $title", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onFetchNaverMarket,
            enabled = !isFetching,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_fetch_naver_market"),
            colors = ButtonDefaults.buttonColors(containerColor = NaverGreen, contentColor = NavyBackground)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isFetching) "네이버 API 시세 조회 중..." else "네이버 무료 API 주변 시세 조회·입찰가 재산정")
        }
    }
}

@Composable
fun LegalDoctrineAuditCard(
    auction: AuctionCaseIntel,
    report: LegalDoctrineReport?,
    onRunLegalVerify: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeReport = report ?: com.example.engine.LegalDoctrineVerifier.verify(auction)
    val passed = activeReport.overallPassed

    StudioCard(
        modifier = modifier,
        borderColor = if (passed) EmeraldPrimary.copy(alpha = 0.55f) else CrimsonAlert
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Gavel, contentDescription = "법리분석 검증", tint = if (passed) EmeraldPrimary else CrimsonAlert, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("6대 법리·판례 검증 & QA 차단 게이트", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            }
            StatusChip(
                text = if (passed) "법리 PASS" else "모순 ${activeReport.criticalBlockCount}건 차단",
                textColor = if (passed) MintSecondary else CrimsonAlert,
                backgroundColor = if (passed) EmeraldContainer else CrimsonContainer
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(activeReport.auditSummary, style = MaterialTheme.typography.bodySmall, color = if (passed) TextSecondary else CrimsonAlert)
        Spacer(modifier = Modifier.height(10.dp))

        activeReport.checks.forEach { check ->
            val icon = when (check.severity) {
                LegalVerificationSeverity.VERIFIED_PASS -> Icons.Filled.CheckCircle
                LegalVerificationSeverity.CAUTION_NOTE -> Icons.Filled.Warning
                LegalVerificationSeverity.CRITICAL_BLOCK -> Icons.Filled.ErrorOutline
            }
            val tint = when (check.severity) {
                LegalVerificationSeverity.VERIFIED_PASS -> EmeraldPrimary
                LegalVerificationSeverity.CAUTION_NOTE -> TistoryAmber
                LegalVerificationSeverity.CRITICAL_BLOCK -> CrimsonAlert
            }
            Surface(
                color = NavySurfaceElevated,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, NavyBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(icon, contentDescription = check.severity.label, tint = tint, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(check.title, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                        }
                        if (check.estimatedBuyerBurdenKrw > 0L) {
                            Text(
                                text = "부담 ${QaVerificationEngine.formatKrwShort(check.estimatedBuyerBurdenKrw)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                color = tint
                            )
                        }
                    }
                    Text(check.statuteOrPrecedent, style = MaterialTheme.typography.labelSmall, color = CloudflareSky)
                    Text(check.findingDetail, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("실무 지침: ${check.actionGuidance}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onRunLegalVerify,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_run_legal_verify")
        ) {
            Icon(Icons.Filled.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("법리·판례 6대 검증 재실행")
        }
    }
}

@Composable
private fun MetricMiniTile(
    label: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = NavySurfaceElevated,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, NavyBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                color = accent,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
