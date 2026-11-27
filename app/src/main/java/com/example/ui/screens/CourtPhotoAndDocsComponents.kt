package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsWalk
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.engine.CourtEvidenceRecorder
import com.example.engine.InfographicBitmapRenderer
import com.example.engine.PostAuctionFollowUpEngine
import com.example.model.AuctionCaseIntel
import com.example.model.CourtFullArchiveRecord
import com.example.model.CourtPhotoEvidence
import com.example.model.VisitRecordSource
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.CloudflareContainer
import com.example.ui.theme.CloudflareSky
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
            val hasRealPhoto = photo.imageUrl.isNotBlank()
            Surface(
                modifier = Modifier
                    .width(132.dp)
                    .height(88.dp)
                    .clickable(onClick = onClickPhoto)
                    .testTag("mini_court_photo_${auction.caseId}_${photo.shotOrder}"),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    1.dp,
                    if (hasRealPhoto) EmeraldPrimary.copy(alpha = 0.8f) else CloudflareSky.copy(alpha = 0.5f)
                ),
                color = NavyBackground
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (hasRealPhoto) {
                        AsyncImage(
                            model = photo.imageUrl,
                            contentDescription = "${auction.caseId} ${photo.title}",
                            placeholder = painterResource(id = drawableRes),
                            error = painterResource(id = drawableRes),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = drawableRes),
                            contentDescription = "${auction.caseId} ${photo.title}",
                            contentScale = ContentScale.Crop,
                            colorFilter = shotToneFilter(idx),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = if (hasRealPhoto) "실사 #${photo.shotOrder}" else "법원컷 #${photo.shotOrder}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hasRealPhoto) EmeraldPrimary else MintSecondary,
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
    onSharePhotoPng: (CourtPhotoEvidence) -> Unit,
    onAttachPhotoUri: ((shotOrder: Int, uriString: String) -> Unit)? = null,
    onAddVisitRecord: ((source: VisitRecordSource, visitedAt: String, occupancyStatus: String, findingsNote: String) -> Unit)? = null
) {
    var expanded by remember(auction.caseId) { mutableStateOf(true) }
    var activeTab by remember(auction.caseId) { mutableIntStateOf(0) } // 0: 현장사진 | 1: 문건 | 2: 등기·권리 | 3: 방문기록
    var selectedShotIdx by remember(auction.caseId) { mutableIntStateOf(0) }
    var showAddVisitForm by remember(auction.caseId) { mutableStateOf(false) }
    var newVisitSource by remember(auction.caseId) { mutableStateOf(VisitRecordSource.FIELD_IMJANG) }
    var newVisitDate by remember(auction.caseId) { mutableStateOf(PostAuctionFollowUpEngine.currentIsoDate()) }
    var newVisitOccupancy by remember(auction.caseId) { mutableStateOf("") }
    var newVisitFindings by remember(auction.caseId) { mutableStateOf("") }

    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val drawableRes = InfographicBitmapRenderer.drawableResForPropertyType(auction.propertyType)

    val safeIdx = selectedShotIdx.coerceIn(0, (archive.photos.size - 1).coerceAtLeast(0))
    val activePhoto = archive.photos.getOrNull(safeIdx)
    val realPhotoCount = archive.photos.count { it.imageUrl.isNotBlank() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null && activePhoto != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            onAttachPhotoUri?.invoke(activePhoto.shotOrder, uri.toString())
        }
    }

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
                        text = "법원 사건 기록 (현장사진 · 등기부 · 문건 · 방문기록)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "${archive.caseId} · 사진 ${archive.photos.size}컷(실물 ${realPhotoCount}건) · 등기 ${archive.registryGapGu.size + archive.registryEulGu.size}건 · 방문기록 ${archive.visitRecords.size}건",
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
                        label = { Text("현장사진 (${archive.photos.size}컷)") },
                        modifier = Modifier.testTag("tab_court_photos")
                    )
                    FilterChip(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        label = { Text("방문기록 (${archive.visitRecords.size}건)") },
                        modifier = Modifier.testTag("tab_court_visits")
                    )
                    FilterChip(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        label = { Text("등기부 갑/을구 (${archive.registryGapGu.size + archive.registryEulGu.size})") },
                        modifier = Modifier.testTag("tab_court_rights")
                    )
                    FilterChip(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        label = { Text("공식 문건 (${archive.documents.size}종)") },
                        modifier = Modifier.testTag("tab_court_docs")
                    )
                }

                when (activeTab) {
                    0 -> {
                        if (activePhoto != null) {
                            val hasRealUrl = activePhoto.imageUrl.isNotBlank()
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                border = BorderStroke(
                                    1.5.dp,
                                    if (hasRealUrl) EmeraldPrimary else CloudflareSky.copy(alpha = 0.65f)
                                ),
                                color = NavyBackground,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4f / 3f)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (hasRealUrl) {
                                        AsyncImage(
                                            model = activePhoto.imageUrl,
                                            contentDescription = "${archive.caseId} ${activePhoto.title}",
                                            placeholder = painterResource(id = drawableRes),
                                            error = painterResource(id = drawableRes),
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(id = drawableRes),
                                            contentDescription = "${archive.caseId} ${activePhoto.title}",
                                            contentScale = ContentScale.Crop,
                                            colorFilter = shotToneFilter(safeIdx),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(
                                                        Color.Black.copy(alpha = 0.45f),
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.84f)
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
                                            text = if (hasRealUrl) "실사 연결됨" else "가이드 샘플 · 실사 첨부 권장",
                                            textColor = if (hasRealUrl) MintSecondary else TistoryAmber,
                                            backgroundColor = if (hasRealUrl) EmeraldContainer else AmberContainer
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
                                        label = { Text("${if (p.imageUrl.isNotBlank()) "★ " else ""}사진 #${p.shotOrder}: ${p.title}") }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (onAttachPhotoUri != null) {
                                    OutlinedButton(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("btn_attach_court_photo"),
                                        border = BorderStroke(1.dp, EmeraldPrimary)
                                    ) {
                                        Icon(Icons.Filled.AddAPhoto, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("실사 첨부", color = MintSecondary, maxLines = 1)
                                    }
                                }
                                Button(
                                    onClick = { onSharePhotoPng(activePhoto) },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(48.dp)
                                        .testTag("btn_share_court_photo_png"),
                                    colors = ButtonDefaults.buttonColors(containerColor = CloudflareSky, contentColor = NavyBackground)
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("컷#${activePhoto.shotOrder} PNG 공유", maxLines = 1)
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

                    3 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "법원 현황조사 방문 · 전입/관리실 탐문 · 현장 임장 (${archive.visitRecords.size}건)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MintSecondary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (onAddVisitRecord != null) {
                                    OutlinedButton(
                                        onClick = { showAddVisitForm = !showAddVisitForm },
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("btn_toggle_add_visit"),
                                        border = BorderStroke(1.dp, EmeraldPrimary)
                                    ) {
                                        Icon(Icons.Filled.AddLocationAlt, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (showAddVisitForm) "닫기" else "+ 방문기록 추가", maxLines = 1)
                                    }
                                }
                            }

                            AnimatedVisibility(visible = showAddVisitForm && onAddVisitRecord != null) {
                                Surface(
                                    color = NavySurfaceElevated,
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.6f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("새 임장 · 탐문 방문기록 등록", style = MaterialTheme.typography.labelLarge, color = MintSecondary)
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            VisitRecordSource.entries.forEach { src ->
                                                FilterChip(
                                                    selected = newVisitSource == src,
                                                    onClick = { newVisitSource = src },
                                                    label = { Text(src.badgeText) }
                                                )
                                            }
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = newVisitDate,
                                                onValueChange = { newVisitDate = it },
                                                label = { Text("방문일자") },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f)
                                            )
                                            OutlinedTextField(
                                                value = newVisitOccupancy,
                                                onValueChange = { newVisitOccupancy = it },
                                                label = { Text("점유·현황 요약") },
                                                placeholder = { Text("예: 공실·열쇠인수 필요") },
                                                singleLine = true,
                                                modifier = Modifier
                                                    .weight(1.3f)
                                                    .testTag("input_visit_occupancy")
                                            )
                                        }
                                        OutlinedTextField(
                                            value = newVisitFindings,
                                            onValueChange = { newVisitFindings = it },
                                            label = { Text("현장 확인 내용 (체납관리비·진입로·특이사항)") },
                                            placeholder = { Text("예: 관리실 확인 결과 공용관리비 18만원 체납, 주차 정상") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("input_visit_findings")
                                        )
                                        Button(
                                            onClick = {
                                                if (newVisitFindings.isNotBlank()) {
                                                    onAddVisitRecord?.invoke(
                                                        newVisitSource,
                                                        newVisitDate,
                                                        newVisitOccupancy,
                                                        newVisitFindings
                                                    )
                                                    newVisitOccupancy = ""
                                                    newVisitFindings = ""
                                                    showAddVisitForm = false
                                                }
                                            },
                                            enabled = newVisitFindings.isNotBlank(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("btn_save_visit_record"),
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
                                        ) {
                                            Text("방문기록 저장 (원고 · HTML 자동 반영)", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            archive.visitRecords.forEach { visit ->
                                Surface(
                                    color = NavyBackground,
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(1.dp, NavyBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.DirectionsWalk, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(visit.visitRound, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                            }
                                            StatusChip(
                                                text = visit.source.badgeText,
                                                textColor = if (visit.source == VisitRecordSource.FIELD_IMJANG) MintSecondary else CloudflareSky,
                                                backgroundColor = if (visit.source == VisitRecordSource.FIELD_IMJANG) EmeraldContainer else CloudflareContainer
                                            )
                                        }
                                        Text(
                                            text = "방문일 ${visit.visitedAt} · ${visit.visitorLabel} · ${visit.occupancyStatus}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TistoryAmber
                                        )
                                        Text(visit.findingsNote, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
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
                                        Text("등기부등본 권리분석 종합 판정", style = MaterialTheme.typography.labelLarge, color = MintSecondary)
                                    }
                                    Text(archive.rightsVerdictSummary, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                    Text(archive.assumedCostSummary, style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily), color = TistoryAmber)
                                }
                            }

                            Text("갑구 (소유권 · 압류 · 경매개시) ${archive.registryGapGu.size}건", style = MaterialTheme.typography.labelLarge, color = CloudflareSky)
                            archive.registryGapGu.forEach { node ->
                                Surface(
                                    color = NavyBackground,
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(1.dp, NavyBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("[${node.rankOrder}] ${node.title}", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                                            StatusChip(text = node.statusBadge, textColor = MintSecondary, backgroundColor = EmeraldContainer)
                                        }
                                        Text("등기일: ${node.registeredDate} · ${node.holderOrCreditor} · ${node.amountOrShare}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Text(node.plainExplanation, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                }
                            }
                            Text("을구 · 임차 (근저당 · 전세 · 대항력) ${archive.registryEulGu.size}건", style = MaterialTheme.typography.labelLarge, color = TistoryAmber)
                            archive.registryEulGu.forEach { node ->
                                Surface(
                                    color = NavyBackground,
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(1.dp, NavyBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("[${node.rankOrder}] ${node.title}", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                                            StatusChip(text = node.statusBadge, textColor = TistoryAmber, backgroundColor = AmberContainer)
                                        }
                                        Text("설정일: ${node.registeredDate} · ${node.holderOrCreditor} · ${node.amountOrShare}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Text(node.plainExplanation, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Direct court auction portal lookup with automatic caseId copy
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(auction.caseId))
                        runCatching {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(CourtEvidenceRecorder.buildCourtAuctionSearchUrl()))
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_open_courtauction"),
                    border = BorderStroke(1.dp, CloudflareSky.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = CloudflareSky, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("대법원 경매정보 열기 (${auction.caseId} 자동 복사)", color = CloudflareSky, maxLines = 1)
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
