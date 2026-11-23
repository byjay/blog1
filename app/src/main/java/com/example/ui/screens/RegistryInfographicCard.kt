package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.RegistryInfographicTemplate
import com.example.model.RegistryInfographicTemplateType
import com.example.model.RegistryNodeTone
import com.example.model.RegistryVisualNode
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
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TistoryAmber

@Composable
fun RegistryInfographicTemplateCard(
    template: RegistryInfographicTemplate,
    selectedType: RegistryInfographicTemplateType,
    onSelectType: (RegistryInfographicTemplateType) -> Unit,
    onSharePng: (RegistryInfographicTemplate) -> Unit
) {
    StudioCard(
        borderColor = CloudflareSky.copy(alpha = 0.6f),
        modifier = Modifier.testTag("registry_infographic_template_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.AccountBalance, contentDescription = "등기부등본 인포그래픽 템플릿", tint = CloudflareSky)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "독자용 등기부등본 인포그래픽 템플릿",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "갑구(소유권·압류) · 을구(근저당·말소기준) · 소유권 현황 시각화",
                        style = MaterialTheme.typography.bodySmall,
                        color = CloudflareSky
                    )
                }
            }
            StatusChip(
                text = template.renderAspectRatio,
                textColor = MintSecondary,
                backgroundColor = EmeraldContainer
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RegistryInfographicTemplateType.entries.forEach { type ->
                FilterChip(
                    selected = type == selectedType,
                    onClick = { onSelectType(type) },
                    label = { Text(type.label) },
                    modifier = Modifier.testTag("chip_reg_tpl_${type.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(template.headline, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Text(template.subHeadline, style = MaterialTheme.typography.bodySmall, color = MintSecondary)

        Spacer(modifier = Modifier.height(10.dp))
        // Ownership Status Summary Card
        Surface(
            color = NavyBackground,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, CloudflareSky.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.HomeWork, contentDescription = "소유권 현황", tint = CloudflareSky, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("소유권 현황 요약", style = MaterialTheme.typography.labelLarge, color = CloudflareSky)
                }
                Text(template.ownershipSummaryCard.currentOwnerLabel, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("${template.ownershipSummaryCard.acquisitionCause} · ${template.ownershipSummaryCard.coOwnerOrShareNote}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("점검 포인트: ${template.ownershipSummaryCard.ownershipRiskNote}", style = MaterialTheme.typography.labelSmall, color = TistoryAmber)
            }
        }

        if (selectedType != RegistryInfographicTemplateType.EULGU_MORTGAGE_WATERFALL) {
            Spacer(modifier = Modifier.height(10.dp))
            Text("갑구 (소유권 변동 · 세금 압류 · 경매개시)", style = MaterialTheme.typography.labelLarge, color = CloudflareSky)
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                template.gapGuNodes.forEach { node -> RegistryVisualNodeRow(node) }
            }
        }

        if (selectedType != RegistryInfographicTemplateType.OWNERSHIP_CHAIN_AND_GAPGU) {
            Spacer(modifier = Modifier.height(10.dp))
            Text("을구 · 임차 (근저당 · 말소기준권리 · 인수 주의)", style = MaterialTheme.typography.labelLarge, color = TistoryAmber)
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                template.eulGuNodes.forEach { node -> RegistryVisualNodeRow(node) }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            color = NavySurfaceElevated,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.FactCheck, contentDescription = "독자 체크 요약", tint = MintSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("독자용 3줄 핵심 요약", style = MaterialTheme.typography.labelLarge, color = MintSecondary)
                }
                Text(template.cancellationBaselineSummary, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                Text(template.buyerTakeoverSummary, style = MaterialTheme.typography.bodySmall, color = TistoryAmber)
                template.readerChecklist.forEach { line ->
                    Text(line, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = { onSharePng(template) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_share_registry_infographic_png"),
            colors = ButtonDefaults.buttonColors(containerColor = CloudflareSky, contentColor = NavyBackground)
        ) {
            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("등기부 갑구·을구·소유권 인포그래픽 PNG 공유")
        }
    }
}

@Composable
private fun RegistryVisualNodeRow(node: RegistryVisualNode) {
    val (accent, badgeBg) = when (node.nodeTone) {
        RegistryNodeTone.NEUTRAL_OWNERSHIP -> CloudflareSky to CloudflareContainer
        RegistryNodeTone.BASE_RIGHT_ANCHOR -> TistoryAmber to AmberContainer
        RegistryNodeTone.EXTINGUISHED_SAFE -> MintSecondary to EmeraldContainer
        RegistryNodeTone.ASSUMED_DANGER -> CrimsonAlert to CrimsonContainer
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 12.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            color = NavyBackground,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(
                width = if (node.isBaseRight || node.isAssumedRisk) 1.5.dp else 1.dp,
                color = if (node.isBaseRight || node.isAssumedRisk) accent.copy(alpha = 0.7f) else NavyBorder
            ),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${node.rankOrder} · ${node.registeredDate}",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                        color = accent
                    )
                    StatusChip(text = node.statusBadge, textColor = accent, backgroundColor = badgeBg)
                }
                Text("${node.title} (${node.holderOrCreditor})", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
                Text(node.amountOrShare, style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily), color = TextMuted)
                Text(node.plainExplanation, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}
