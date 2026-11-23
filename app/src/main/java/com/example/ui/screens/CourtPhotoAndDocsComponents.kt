package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.engine.InfographicBitmapRenderer
import com.example.model.AuctionCaseIntel
import com.example.model.CourtFullArchiveRecord
import com.example.model.CourtPhotoEvidence
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
fun CourtPhotoStripMini(
    auction: AuctionCaseIntel,
    photos: List<CourtPhotoEvidence>,
    onClickPhoto: () -> Unit = {}
) {
    val drawableRes = InfographicBitmapRenderer.drawableResForPropertyType(auction.propertyType)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        photos.forEachIndexed { idx, photo ->
            Surface(
                modifier = Modifier
                    .width(126.dp)
                    .height(86.dp)
                    .clickable(onClick = onClickPhoto)
                    .testTag("mini_court_photo_${auction.caseId}_${photo.shotOrder}"),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, CloudflareSky.copy(alpha = 0.5f)),
                color = NavyBackground
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = "${auction.caseId} ${photo.title}",
                        contentScale = ContentScale.Crop,
                        colorFilter = shotToneFilter(idx),
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "법원사진 #${photo.shotOrder}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MintSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = photo.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CourtFullEvidenceArchiveCard(
    auction: AuctionCaseIntel,
    archive: CourtFullArchiveRecord,
    onSharePhotoPng: (CourtPhotoEvidence) -> Unit
) {
    var expanded by remember(auction.caseId) { mutableStateOf(true) }
    var activeTab by remember(auction.caseId) { mutableIntStateOf(0) } // 0: 현장사진5컷 | 1: 문건3종 | 2: 등기·권리전체
    var selectedShotIdx by remember(auction.caseId) { mutableIntStateOf(0) }
    val clipboard = LocalClipboardManager.current
    val drawableRes = InfographicBitmapRenderer.drawableResForPropertyType(auction.propertyType)

    StudioCard(
        borderColor = MintSecondary.copy(alpha = 0.6f),
        modifier = Modifier.testTag("court_full_archive_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.FolderOpen, contentDescription = "법원 제공 전체 기록관", tint = MintSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "법원 제공 현장사진 · 문건 · 등기 · 권리분석 전체 기록",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "${archive.caseId} · 사진 ${archive.photos.size}컷 · 문건 ${archive.documents.size}종 · 갑구/을구 전수 기록",
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
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusChip(text = archive.profitabilityBadge, textColor = MintSecondary, backgroundColor = EmeraldContainer)
                    StatusChip(text = "${archive.areaSqmLabel} · ${archive.landCategoryLabel}", textColor = CloudflareSky, backgroundColor = CloudflareContainer)
                    StatusChip(text = archive.zoningLabel, textColor = TistoryAmber, backgroundColor = AmberContainer)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        label = { Text("법원 현장사진 (${archive.photos.size}컷)") },
                        modifier = Modifier.testTag("tab_court_photos")
                    )
                    FilterChip(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        label = { Text("공식 문건 (${archive.documents.size}종)") },
                        modifier = Modifier.testTag("tab_court_docs")
                    )
                    FilterChip(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        label = { Text("등기·권리 전체기록") },
                        modifier = Modifier.testTag("tab_court_rights")
                    )
                }

                when (activeTab) {
                    0 -> {
                        val safeIdx = selectedShotIdx.coerceIn(0, (archive.photos.size - 1).coerceAtLeast(0))
                        val activePhoto = archive.photos.getOrNull(safeIdx)
                        if (activePhoto != null) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                border = BorderStroke(1.5.dp, CloudflareSky.copy(alpha = 0.65f)),
                                color = NavyBackground,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4f / 3f)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Image(
                                        painter = painterResource(id = drawableRes),
                                        contentDescription = "${archive.caseId} ${activePhoto.title}",
                                        contentScale = ContentScale.Crop,
                                        colorFilter = shotToneFilter(safeIdx),
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(
                                                        Color.Black.copy(alpha = 0.45f),
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.82f)
                                                    )
                                                )
                                            )
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        StatusChip(
                                            text = activePhoto.courtSourceLabel,
                                            textColor = Color.White,
                                            backgroundColor = Color.Black.copy(alpha = 0.72f)
                                        )
                                        StatusChip(
                                            text = "EXIF 삭제",
                                            textColor = MintSecondary,
                                            backgroundColor = EmeraldContainer
                                        )
                                    }
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "컷 ${activePhoto.shotOrder}. ${activePhoto.title} (${activePhoto.captureAngle})",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = activePhoto.observationNote,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFE2E8F0)
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Warning, contentDescription = null, tint = TistoryAmber, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "점검: ${activePhoto.redFlagCheck}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TistoryAmber
                                            )
                                        }
                                    }
                                }
                            }

                            CourtPhotoStripMini(
                                auction = auction,
                                photos = archive.photos,
                                onClickPhoto = {}
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                archive.photos.forEachIndexed { idx, p ->
                                    FilterChip(
                                        selected = idx == safeIdx,
                                        onClick = { selectedShotIdx = idx },
                                        label = { Text("사진 #${p.shotOrder}: ${p.title}") }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onSharePhotoPng(activePhoto) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_share_court_photo_png"),
                                    colors = ButtonDefaults.buttonColors(containerColor = CloudflareSky, contentColor = NavyBackground)
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("현장사진 #${activePhoto.shotOrder} PNG 공유", maxLines = 1)
                                }
                                IconButton(
                                    onClick = { clipboard.setText(AnnotatedString(activePhoto.blogCaption)) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = "사진 기록 캡션 복사", tint = MintSecondary)
                                }
                            }
                        }
                    }

                    1 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            archive.documents.forEach { doc ->
                                Surface(
                                    color = NavyBackground,
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(1.dp, NavyBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.Description, contentDescription = null, tint = CloudflareSky, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(doc.docKind.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                            }
                                            StatusChip(text = doc.referenceNo, textColor = MintSecondary, backgroundColor = EmeraldContainer)
                                        }
                                        Text("${doc.issuer} · 기준일 ${doc.issuedDate}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Text(doc.summaryHighlight, style = MaterialTheme.typography.bodySmall, color = MintSecondary, fontWeight = FontWeight.Medium)
                                        doc.fullRecordLines.forEach { line ->
                                            Text(line, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        }
                                        Text("유의사항: ${doc.riskFlagNote}", style = MaterialTheme.typography.labelSmall, color = TistoryAmber)
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = NavySurfaceElevated,
                                shape = MaterialTheme.shapes.small,
                                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Gavel, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("권리분석 종합 판정", style = MaterialTheme.typography.labelLarge, color = MintSecondary)
                                    }
                                    Text(archive.rightsVerdictSummary, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                    Text(archive.assumedCostSummary, style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily), color = TistoryAmber)
                                }
                            }

                            Text("갑구 전체 기록 (${archive.registryGapGu.size}건)", style = MaterialTheme.typography.labelLarge, color = CloudflareSky)
                            archive.registryGapGu.forEach { node ->
                                Text("• [${node.rankOrder}] ${node.title} (${node.registeredDate}) — ${node.statusBadge}: ${node.plainExplanation}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Text("을구 · 임차 전체 기록 (${archive.registryEulGu.size}건)", style = MaterialTheme.typography.labelLarge, color = TistoryAmber)
                            archive.registryEulGu.forEach { node ->
                                Text("• [${node.rankOrder}] ${node.title} (${node.registeredDate}) — ${node.statusBadge}: ${node.plainExplanation}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun shotToneFilter(index: Int): ColorFilter? {
    val matrix = ColorMatrix()
    when (index % 5) {
        1 -> matrix.setToScale(0.95f, 1.02f, 1.08f, 1f)
        2 -> matrix.setToScale(1.06f, 0.98f, 0.94f, 1f)
        3 -> matrix.setToSaturation(1.12f)
        4 -> matrix.setToScale(0.92f, 1.04f, 1.02f, 1f)
        else -> return null
    }
    return ColorFilter.colorMatrix(matrix)
}
