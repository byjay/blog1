package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Update
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.engine.InvestmentAttractivenessEngine
import com.example.engine.LegalTermStudyEngine
import com.example.engine.PostAuctionFollowUpEngine
import com.example.model.AuctionCaseIntel
import com.example.model.InvestmentAttractivenessScore
import com.example.model.LegalStudyCategory
import com.example.model.PostAuctionFollowUpRecord
import com.example.model.PostAuctionOutcomeType
import com.example.ui.theme.CloudflareSky
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyContainerHighest
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TistoryAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostAuctionFollowUpCard(
    auction: AuctionCaseIntel,
    followUpRecord: PostAuctionFollowUpRecord?,
    isBusy: Boolean,
    onReinvestigateAndCreateFollowUp: (
        outcomeType: PostAuctionOutcomeType,
        actualWinningManWon: Long?,
        nextAuctionDate: String?,
        courtNote: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPastDue = remember(auction.auctionDate) {
        PostAuctionFollowUpEngine.isAuctionDatePassed(auction.auctionDate)
    }
    var selectedOutcome by remember { mutableStateOf(PostAuctionOutcomeType.FAILED_DROP) }
    var winningManText by remember { mutableStateOf((auction.balancedBidKrw / 10_000L).toString()) }
    var nextDateText by remember { mutableStateOf("2026-12-28") }
    var courtNoteText by remember { mutableStateOf("") }

    StudioCard(
        modifier = modifier,
        borderColor = if (isPastDue) TistoryAmber.copy(alpha = 0.65f) else NavyBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Update, contentDescription = "사후 재조사", tint = TistoryAmber, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("매각기일 이후 사후 재조사 · 추가 관련글 등록", style = MaterialTheme.typography.titleSmall, color = TistoryAmber)
            }
            StatusChip(
                text = if (isPastDue) "기일 경과(${auction.auctionDate})" else "예정(${auction.auctionDate})",
                textColor = if (isPastDue) TistoryAmber else MintSecondary,
                backgroundColor = if (isPastDue) NavySurfaceElevated else EmeraldContainer
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "경매기일이 지난 물건을 재조회해 낙찰가·매각가율 또는 유찰(-20%)·기일변경·취하 관련 후속글을 생성·창고 등록합니다.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PostAuctionOutcomeType.entries.forEach { outcome ->
                FilterChip(
                    selected = selectedOutcome == outcome,
                    onClick = { selectedOutcome = outcome },
                    label = { Text(outcome.badge) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (selectedOutcome == PostAuctionOutcomeType.WINNING_BID) {
                OutlinedTextField(
                    value = winningManText,
                    onValueChange = { winningManText = it.filter(Char::isDigit) },
                    label = { Text("실제 낙찰가(만원)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            } else if (selectedOutcome == PostAuctionOutcomeType.FAILED_DROP || selectedOutcome == PostAuctionOutcomeType.RESCHEDULED) {
                OutlinedTextField(
                    value = nextDateText,
                    onValueChange = { nextDateText = it },
                    label = { Text("차회 매각기일") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = courtNoteText,
                onValueChange = { courtNoteText = it },
                label = { Text("법원 변동 메모(선택)") },
                singleLine = true,
                modifier = Modifier.weight(1.2f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                onReinvestigateAndCreateFollowUp(
                    selectedOutcome,
                    winningManText.toLongOrNull(),
                    nextDateText,
                    courtNoteText
                )
            },
            enabled = !isBusy,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_reinvestigate_followup"),
            colors = ButtonDefaults.buttonColors(containerColor = TistoryAmber, contentColor = NavyBackground)
        ) {
            Icon(Icons.Filled.Update, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("사후 재조사 · 추가 관련글 생성 및 창고 등록", fontWeight = FontWeight.Bold)
        }

        if (followUpRecord != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = NavyBackground,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(followUpRecord.followUpSummary, style = MaterialTheme.typography.labelLarge, color = MintSecondary)
                    Text(followUpRecord.predictionDiffNote, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    Text("기록 시각: ${followUpRecord.recordedAt}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }
        }
    }
}

@Composable
fun CompetitiveAttractivenessCard(
    score: InvestmentAttractivenessScore,
    modifier: Modifier = Modifier
) {
    var showCompetitors by remember { mutableStateOf(false) }
    val benchmarkRows = remember { InvestmentAttractivenessEngine.buildCompetitorComparisonRows() }

    StudioCard(modifier = modifier, borderColor = CloudflareSky.copy(alpha = 0.45f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Analytics, contentDescription = "투자 매력도", tint = CloudflareSky, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("콘텐츠·투자 매력도 & 경쟁 블로그 초월 지표", style = MaterialTheme.typography.titleSmall, color = CloudflareSky)
            }
            StatusChip(
                text = "${score.totalScore}/100점",
                textColor = MintSecondary,
                backgroundColor = EmeraldContainer
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (score.totalScore / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = EmeraldPrimary,
            trackColor = NavyContainerHighest
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(score.selectionVerdict, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
        Text(
            "세부(가격 ${score.priceCompetitivenessPts}/25 · 할인 ${score.dropDiscountPts}/20 · 권리 ${score.rightsClarityPts}/20 · 3년통계 ${score.stats3yPts}/15 · 수익비용 ${score.yieldCostPts}/10 · 수요 ${score.demandSearchPts}/10)",
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(score.disclaimer, style = MaterialTheme.typography.labelSmall, color = TextMuted)

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { showCompetitors = !showCompetitors },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(if (showCompetitors) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (showCompetitors) "5대 경매 블로그 비교표 접기" else "주 20건+ 경쟁 경매 블로그 5곳 비교 보기")
        }

        AnimatedVisibility(visible = showCompetitors) {
            Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                benchmarkRows.forEach { row ->
                    Surface(
                        color = NavyBackground,
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(row.blogName, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                                Text(row.weeklyPostsStat, style = MaterialTheme.typography.labelSmall, color = TistoryAmber)
                            }
                            Text("기존 방식: ${row.primaryFocus}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(row.ddakchalSuperiority, style = MaterialTheme.typography.bodySmall, color = MintSecondary)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LegalTermStudyCard(
    selectedCategory: LegalStudyCategory?,
    query: String,
    contextualTermId: String,
    onFilterChange: (LegalStudyCategory?, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val terms = remember(selectedCategory, query) {
        LegalTermStudyEngine.filterTerms(selectedCategory, query)
    }
    var expandedId by remember(contextualTermId) { mutableStateOf<String?>(contextualTermId) }

    StudioCard(modifier = modifier, borderColor = EmeraldPrimary.copy(alpha = 0.45f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Gavel, contentDescription = "법리 용어 공부", tint = MintSecondary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text("법리·용어 마스터 DB (글마다 1컷 자동 삽입)", style = MaterialTheme.typography.titleSmall, color = MintSecondary)
                Text("민법총칙·민사특별법·경매배당·부동산공시·도시정비·세법 핵심 판례 검색", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { onFilterChange(selectedCategory, it) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            label = { Text("법리 키워드·조문 검색 (예: 당해세, 멸실, 대위변제)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onFilterChange(null, query) },
                label = { Text("전체 (${LegalTermStudyEngine.masterTerms.size})") }
            )
            LegalStudyCategory.entries.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { onFilterChange(cat, query) },
                    label = { Text(cat.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        terms.forEach { term ->
            val isOpen = expandedId == term.codeId
            Surface(
                color = NavyBackground,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(
                    1.dp,
                    if (term.codeId == contextualTermId) EmeraldPrimary.copy(alpha = 0.6f) else NavyBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${term.codeId} · ${term.category.label}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                color = if (term.codeId == contextualTermId) MintSecondary else CloudflareSky
                            )
                            Text(term.termTitle, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                        }
                        OutlinedButton(
                            onClick = { expandedId = if (isOpen) null else term.codeId },
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(if (isOpen) "접기" else "공부")
                        }
                    }

                    AnimatedVisibility(visible = isOpen) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("근거 조문: ${term.statuteRef}", style = MaterialTheme.typography.labelMedium, color = TistoryAmber)
                            Text(term.coreDoctrine, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            Text("실무 함정: ${term.practicalTrap}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("블로그 삽입 1컷: ${term.blogStudyOneLiner}", style = MaterialTheme.typography.bodySmall, color = MintSecondary)
                            Text(term.apiJsonSnippet, style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily), color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}
