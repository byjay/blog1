package com.example.ui.screens

import android.content.Intent
import android.webkit.WebView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import com.example.ui.util.SafeClipboardHelper
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.CourtEvidenceRecorder
import com.example.engine.Article30PointEvaluator
import com.example.engine.HtmlExportEngine
import com.example.engine.InfographicBitmapRenderer
import com.example.engine.InvestmentAttractivenessEngine
import com.example.engine.LegalDoctrineVerifier
import com.example.engine.LegalTermStudyEngine
import com.example.engine.ProAnalysisAndPhotoGuideEngine
import com.example.engine.RegistryAndMotionEngine
import com.example.engine.RegistryInfographicEngine
import com.example.model.ChannelPlatform
import com.example.model.LegalStudyCategory
import com.example.model.PostAuctionOutcomeType
import com.example.model.RegistryInfographicTemplateType
import com.example.model.VisitRecordSource
import com.example.ui.StudioUiState
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
fun ChannelQaScreen(
    uiState: StudioUiState,
    onSelectSlot: (String) -> Unit,
    onSelectChannel: (ChannelPlatform) -> Unit,
    onSelectRegistryTemplateType: (RegistryInfographicTemplateType) -> Unit,
    onRewriteSlot: (String) -> Unit,
    onScheduleSlot: (String) -> Unit,
    onToggleAutoQualityDefense: (Boolean) -> Unit = {},
    onRunQualityDefense: (String) -> Unit = {},
    onFetchNaverMarket: (String) -> Unit = {},
    onRunLegalVerify: (String) -> Unit = {},
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
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
    onPublishNaverDirectAndVerify: (String, String) -> Unit = { _, _ -> },
    onPublishSelectedChannelsAndVerify: (String, String) -> Unit = { _, _ -> },
    onReinvestigateFollowUp: (
        postId: String,
        outcomeType: PostAuctionOutcomeType,
        actualWinningManWon: Long?,
        nextAuctionDate: String?,
        courtNote: String
    ) -> Unit = { _, _, _, _, _ -> },
    onLegalStudyFilterChange: (LegalStudyCategory?, String) -> Unit = { _, _ -> },
    onAttachCourtPhoto: (postId: String, shotOrder: Int, uriString: String) -> Unit = { _, _, _ -> },
    onAddVisitRecord: (postId: String, source: VisitRecordSource, visitedAt: String, occupancyStatus: String, findingsNote: String) -> Unit = { _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val selectedSlot = uiState.slots.firstOrNull { it.postId == uiState.selectedSlotId }
        ?: uiState.warehouseArticles.firstOrNull { it.slot.postId == uiState.selectedSlotId }?.slot
        ?: uiState.slots.firstOrNull()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var previewMode by remember { mutableStateOf("WEB_PREVIEW") } // WEB_PREVIEW | BODY_TEXT | HTML | JSON_LD

    if (selectedSlot == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(NavyBackground)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("검수할 슬롯이 없습니다. 대시보드에서 일일 발행을 실행하세요.", color = TextSecondary)
        }
        return
    }

    val auction = selectedSlot.auctionCase
    val qa = selectedSlot.qaReport
    val activeVariant = selectedSlot.variants[uiState.selectedChannel]

    val proReport = remember(auction.caseId, auction.minBidPriceKrw) {
        ProAnalysisAndPhotoGuideEngine.buildProReport(auction)
    }
    val registryEntries = remember(auction.caseId, auction.baseRightDate) {
        RegistryAndMotionEngine.buildRegistryFacts(auction)
    }
    val motionScenes = remember(auction.caseId, uiState.proFunnel) {
        RegistryAndMotionEngine.build5SceneMotionGuide(auction, proReport, uiState.proFunnel)
    }
    val activeRegistryTemplate = uiState.activeRegistryInfographic?.takeIf {
        it.caseId == auction.caseId && it.templateType == uiState.selectedRegistryTemplateType
    } ?: remember(auction.caseId, uiState.selectedRegistryTemplateType) {
        RegistryInfographicEngine.buildTemplate(auction, uiState.selectedRegistryTemplateType)
    }
    val eval30 = remember(selectedSlot, uiState.config, uiState.proFunnel, activeRegistryTemplate) {
        Article30PointEvaluator.evaluate(selectedSlot, uiState.config, uiState.proFunnel, activeRegistryTemplate)
    }
    val activeCourtArchive = uiState.activeCourtArchive?.takeIf {
        it.caseId == auction.caseId &&
            it.visitRecords.size >= selectedSlot.visitRecords.size
    } ?: remember(auction.caseId, selectedSlot.courtPhotoUrls, selectedSlot.visitRecords) {
        CourtEvidenceRecorder.recordFullArchive(auction, selectedSlot.courtPhotoUrls, selectedSlot.visitRecords)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Horizontal Case Selector Strip
        item {
            SectionHeader(
                title = "정밀 물건분석 · 등기부 도식 · 3채널 QA 검수",
                subtitle = "사건별 배당·세무·등기부 해부·임장 사진·모션 콘티 및 저품질 0% 교차 검증"
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.slots.forEach { slot ->
                    FilterChip(
                        selected = slot.postId == selectedSlot.postId,
                        onClick = { onSelectSlot(slot.postId) },
                        label = {
                            Text(
                                "${slot.auctionCase.caseId} (${slot.qaReport.maxCrossChannelCosinePct}%)",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }
        }

        // 0. Court Full Archive: 5 Visible Site Photos + 3 Official Documents + Full Registry & Rights Analysis + Visit Logs
        item {
            CourtFullEvidenceArchiveCard(
                auction = auction,
                archive = activeCourtArchive,
                onSharePhotoPng = { photo ->
                    val shareUri = InfographicBitmapRenderer.renderCourtEvidencePhotoToShareUri(context, auction, photo)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, shareUri)
                        putExtra(Intent.EXTRA_TEXT, photo.blogCaption)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "${auction.caseId} 법원 현장사진 #${photo.shotOrder} 공유"))
                },
                onAttachPhotoUri = { shotOrder, uriString ->
                    onAttachCourtPhoto(selectedSlot.postId, shotOrder, uriString)
                },
                onAddVisitRecord = { source, visitedAt, occupancyStatus, findingsNote ->
                    onAddVisitRecord(selectedSlot.postId, source, visitedAt, occupancyStatus, findingsNote)
                }
            )
        }

        // 1. Pro-Grade Free Auction Analysis (Waterfall, LTV70/CapRate/ROE, BEP, Stress Test, 3-Tax)
        item {
            ProFreeAnalysisAccordionCard(auction = auction, report = proReport)
        }

        // 1-2. Paid App vs General Blog vs Ddakchal Benchmark + Signup Funnel Audit
        item {
            PaidAppVsBlogBenchmarkCard(
                auctionCaseId = auction.caseId,
                proFunnel = uiState.proFunnel
            )
        }

        // 1-3. Naver Free API Nearby Market Analysis & Recalculated Bids
        item {
            NearbyMarketAnalysisCard(
                auction = auction,
                report = uiState.marketAnalysisByCaseId[auction.caseId],
                isFetching = uiState.isFetchingMarket,
                onFetchNaverMarket = { onFetchNaverMarket(selectedSlot.postId) }
            )
        }

        // 1-4. Legal-Doctrine & Precedent Verification + QA Gate
        item {
            LegalDoctrineAuditCard(
                auction = auction,
                report = uiState.legalVerificationByCaseId[auction.caseId],
                onRunLegalVerify = { onRunLegalVerify(selectedSlot.postId) }
            )
        }

        // 1-5. ViewModel Quality Defense Module (SEO & Anti-AI Sanitization)
        item {
            QualityDefensePanelCard(
                auction = auction,
                defenseState = uiState.qualityDefenseState,
                isBusy = uiState.isBusy,
                onToggleAutoDefense = onToggleAutoQualityDefense,
                onRunQualityDefense = { onRunQualityDefense(selectedSlot.postId) }
            )
        }

        // 1-6. 3-Channel Destination Inputs & Direct Naver Publish + Live HTTP Verification
        item {
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

        // 1-7. Post-Auction Follow-Up Investigation & Related Article Generation
        item {
            PostAuctionFollowUpCard(
                auction = auction,
                followUpRecord = uiState.followUpRecordsByCaseId[auction.caseId],
                isBusy = uiState.isBusy,
                onReinvestigateAndCreateFollowUp = { outcome, winMan, nextDate, note ->
                    onReinvestigateFollowUp(selectedSlot.postId, outcome, winMan, nextDate, note)
                }
            )
        }

        // 1-8. Investment Attractiveness Score & 5-Competitor Benchmark Table
        item {
            val attractiveness = uiState.attractivenessByCaseId[auction.caseId] ?: remember(auction.caseId) {
                val legal = uiState.legalVerificationByCaseId[auction.caseId] ?: LegalDoctrineVerifier.verify(auction)
                InvestmentAttractivenessEngine.score(auction, legal)
            }
            CompetitiveAttractivenessCard(score = attractiveness)
        }

        // 1-9. Master LegalTech Study DB (1 contextual study note embedded per post + searchable cards)
        item {
            val contextualTerm = remember(auction.caseId) { LegalTermStudyEngine.selectContextualTerm(auction) }
            LegalTermStudyCard(
                selectedCategory = uiState.selectedLegalStudyCategory,
                query = uiState.legalStudyQuery,
                contextualTermId = contextualTerm.codeId,
                onFilterChange = onLegalStudyFilterChange
            )
        }

        // 2. Registry Deed (갑구·을구) Anatomy Timeline Diagram & Reader Infographic Template
        item {
            RegistryDeedDiagramCard(auction = auction, registryEntries = registryEntries)
        }
        item {
            RegistryInfographicTemplateCard(
                template = activeRegistryTemplate,
                selectedType = uiState.selectedRegistryTemplateType,
                onSelectType = onSelectRegistryTemplateType,
                onSharePng = { tpl ->
                    val shareUri = InfographicBitmapRenderer.renderRegistryTemplateToShareUri(context, tpl)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, shareUri)
                        putExtra(Intent.EXTRA_TEXT, tpl.headline)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "${auction.caseId} 등기부 인포그래픽 PNG 공유"))
                }
            )
        }

        // 3. Field Photo 5-Shot Guide (Paid-app grade unlocked for free)
        item {
            FieldPhotoGuideCard(
                propertyLabel = auction.propertyType.displayLabel,
                shots = proReport.photoGuideShots
            )
        }

        // 4. 5-Scene 65s Motion Graphic & Naver Clip Video Guide + Animated Previewer
        item {
            MotionGraphicGuideAndPlayerCard(auction = auction, scenes = motionScenes)
        }

        // 5. Cross-Channel Similarity Gate Card
        item {
            StudioCard(
                borderColor = if (qa.similarityPassed) EmeraldPrimary.copy(alpha = 0.5f) else CrimsonAlert
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "문서 유사도 검증 게이트 (<30% 필수 · 권장 <25%)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    StatusChip(
                        text = if (qa.similarityPassed) "PASS" else "30% 초과 차단",
                        textColor = if (qa.similarityPassed) MintSecondary else CrimsonAlert,
                        backgroundColor = if (qa.similarityPassed) EmeraldContainer else CrimsonContainer
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                qa.pairSimilarities.forEach { (pairName, pct) ->
                    val ratio = (pct / 100.0).toFloat().coerceIn(0f, 1f)
                    val barColor = when {
                        pct < 25.0 -> EmeraldPrimary
                        pct < 30.0 -> TistoryAmber
                        else -> CrimsonAlert
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(pairName, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("${pct}%", style = MaterialTheme.typography.labelMedium, color = barColor)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = barColor,
                        trackColor = NavyContainerHighest
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "5-gram Jaccard 최대: ${qa.maxCrossChannelJaccard5GramPct}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        "최근 7일 글 대비 최대: ${qa.recent7DayMaxSimilarityPct}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }

        // 6. Fact-Check & 5 Anti-Low-Quality Rules Summary + 30-Point 95+ Gate Badge
        item {
            StudioCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "팩트체크 0% 오차 & 5대 저품질 차단 검증",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatusChip(
                        text = "30항목 ${eval30.totalScore}점 ${if (eval30.eligibleForRegistration) "95+ PASS" else "95미만"}",
                        textColor = if (eval30.eligibleForRegistration) MintSecondary else CrimsonAlert,
                        backgroundColor = if (eval30.eligibleForRegistration) EmeraldContainer else CrimsonContainer
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                QaCheckRow(
                    passed = qa.factCheckPassed,
                    title = "금액·사건번호·말소기준권리 0% 오차 대조",
                    detail = if (qa.factCheckPassed) {
                        "${auction.caseId} · 말소기준권리 ${auction.baseRightDate} · 금액 전수 일치"
                    } else {
                        qa.factCheckMismatches.joinToString(" / ")
                    }
                )
                QaCheckRow(
                    passed = qa.forbiddenHits.isEmpty(),
                    title = "채무자·임차인 개인정보 비식별 & 금칙어 필터",
                    detail = if (qa.forbiddenHits.isEmpty()) "개인정보 노출(성명·연락처·전입일) 0건 · 금칙어 0건" else qa.forbiddenHits.joinToString(", ")
                )
                QaCheckRow(
                    passed = qa.externalLinkRuleOk,
                    title = "외부 링크 패널티 회피 (네이버 무링크 PRO 유도 / 티스토리 nofollow)",
                    detail = qa.externalLinkDetail
                )
                QaCheckRow(
                    passed = qa.imageCountAndHashOk,
                    title = "이미지 4~7장 · EXIF 삭제 · 채널별 해시 변형",
                    detail = qa.imageHashDetail
                )
                QaCheckRow(
                    passed = qa.jsonLdValid && qa.scheduleJitterValid,
                    title = "자체웹 JSON-LD(Article/FAQPage) & 정각 회피 지터",
                    detail = "JSON-LD=${if (qa.jsonLdValid) "정상" else "누락"} · 시간차 지터=${if (qa.scheduleJitterValid) "적용" else "정각위반"}"
                )
                QaCheckRow(
                    passed = qa.legalDoctrinePassed,
                    title = "6대 법리·판례 검증 (임차배당·2001다8677·유치권·지상권)",
                    detail = qa.legalDoctrineSummary
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onRewriteSlot(selectedSlot.postId) },
                        enabled = !uiState.isBusy,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_qa_rewrite")
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("저품질 교정 리라이팅", maxLines = 1)
                    }
                    Button(
                        onClick = { onScheduleSlot(selectedSlot.postId) },
                        enabled = !uiState.isBusy && qa.overallPassed,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_qa_schedule"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
                    ) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("시간차 예약 확정", maxLines = 1)
                    }
                }
            }
        }

        // 7. Channel Tabs (Naver / Tistory / Cloudflare) + Infographic Gallery + Pinned Comment Copy
        item {
            val selectedTabIdx = ChannelPlatform.entries.indexOf(uiState.selectedChannel).coerceAtLeast(0)
            Surface(
                color = NavySurface,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column {
                    TabRow(
                        selectedTabIndex = selectedTabIdx,
                        containerColor = NavySurfaceElevated,
                        contentColor = EmeraldPrimary
                    ) {
                        ChannelPlatform.entries.forEach { platform ->
                            val charCount = qa.charCountByChannel[platform] ?: 0
                            Tab(
                                selected = uiState.selectedChannel == platform,
                                onClick = { onSelectChannel(platform) },
                                text = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            platform.displayName,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (uiState.selectedChannel == platform) channelAccentColor(platform) else TextSecondary
                                        )
                                        Text("${charCount}자", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                }
                            )
                        }
                    }

                    if (activeVariant != null) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusChip(
                                    text = "${activeVariant.platform.staggerWindowLabel} · ${selectedSlot.scheduledTimes[activeVariant.platform] ?: ""}",
                                    textColor = channelAccentColor(activeVariant.platform),
                                    backgroundColor = NavySurfaceElevated
                                )
                                Text(
                                    text = "${qa.charCountByChannel[activeVariant.platform] ?: 0}자 (권장 1,200~2,500)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (qa.charCountOkByChannel[activeVariant.platform] == true) MintSecondary else TistoryAmber
                                )
                            }

                            if (activeVariant.proFunnelBadge.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                StatusChip(
                                    text = activeVariant.proFunnelBadge,
                                    textColor = TistoryAmber,
                                    backgroundColor = NavySurfaceElevated
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(activeVariant.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                activeVariant.tags.forEach { tag ->
                                    StatusChip(text = "#$tag", textColor = TextSecondary, backgroundColor = NavyBackground)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = previewMode == "WEB_PREVIEW",
                                    onClick = { previewMode = "WEB_PREVIEW" },
                                    label = { Text("웹 미리보기(완성글)") },
                                    modifier = Modifier.testTag("chip_qa_web_preview")
                                )
                                FilterChip(
                                    selected = previewMode == "BODY_TEXT",
                                    onClick = { previewMode = "BODY_TEXT" },
                                    label = { Text("본문 텍스트") }
                                )
                                FilterChip(
                                    selected = previewMode == "HTML",
                                    onClick = { previewMode = "HTML" },
                                    label = { Text("HTML 소스") }
                                )
                                if (activeVariant.platform == ChannelPlatform.CLOUDFLARE) {
                                    FilterChip(
                                        selected = previewMode == "JSON_LD",
                                        onClick = { previewMode = "JSON_LD" },
                                        label = { Text("JSON-LD") }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            val fullStandaloneHtml = remember(selectedSlot, activeVariant, uiState.config) {
                                HtmlExportEngine.renderStandaloneHtmlDocument(selectedSlot, activeVariant, uiState.config)
                            }
                            val renderedWebPreviewHtml = remember(selectedSlot, activeVariant, uiState.config, eval30) {
                                HtmlExportEngine.renderWebViewPreviewHtml(selectedSlot, activeVariant, uiState.config, eval30)
                            }
                            val displayedCode = when (previewMode) {
                                "HTML" -> fullStandaloneHtml
                                "JSON_LD" -> activeVariant.jsonLdSchema.ifBlank { "Cloudflare 전용 스키마" }
                                else -> activeVariant.bodyText
                            }

                            if (previewMode == "WEB_PREVIEW") {
                                Surface(
                                    color = NavyBackground,
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(1.dp, MintSecondary.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(520.dp)
                                ) {
                                    AndroidView(
                                        factory = { ctx ->
                                            WebView(ctx).apply {
                                                settings.javaScriptEnabled = false
                                                settings.loadWithOverviewMode = true
                                                settings.useWideViewPort = false
                                                settings.allowFileAccess = false
                                                settings.allowContentAccess = false
                                            }
                                        },
                                        update = { webView ->
                                            webView.loadDataWithBaseURL(null, renderedWebPreviewHtml, "text/html", "utf-8", null)
                                        },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .testTag("qa_webview_preview")
                                    )
                                }
                            } else {
                                Surface(
                                    color = NavyBackground,
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(1.dp, NavyBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    SelectionContainer {
                                        Text(
                                            text = displayedCode,
                                            style = if (previewMode == "BODY_TEXT") {
                                                MaterialTheme.typography.bodyMedium
                                            } else {
                                                MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFamily)
                                            },
                                            color = TextPrimary,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    val copyPayload = if (previewMode == "WEB_PREVIEW" || previewMode == "HTML") {
                                        fullStandaloneHtml
                                    } else {
                                        displayedCode
                                    }
                                    val copyLabel = if (previewMode == "WEB_PREVIEW" || previewMode == "HTML") {
                                        "${activeVariant.platform.displayName} 완성 HTML"
                                    } else {
                                        "${activeVariant.platform.displayName} 원고"
                                    }
                                    SafeClipboardHelper.copyPlainTextSafely(
                                        context = context,
                                        label = copyLabel,
                                        text = copyPayload,
                                        composeClipboard = clipboard
                                    )
                                },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (previewMode == "WEB_PREVIEW" || previewMode == "HTML") {
                                        "${activeVariant.platform.displayName} 완성 HTML 복사"
                                    } else {
                                        "${activeVariant.platform.displayName} 원고 복사"
                                    }
                                )
                            }

                            if (activeVariant.platform == ChannelPlatform.NAVER && activeVariant.pinnedCommentSnippet.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = NavySurfaceElevated,
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(1.dp, NaverGreen.copy(alpha = 0.55f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Comment, contentDescription = "네이버 고정댓글 PRO 전환 멘트", tint = NaverGreen, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("네이버 고정댓글 딱찰 PRO·매수대리 DB 유도 멘트", style = MaterialTheme.typography.labelLarge, color = NaverGreen)
                                        }
                                        Text(activeVariant.pinnedCommentSnippet, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                        OutlinedButton(
                                            onClick = {
                                                SafeClipboardHelper.copyPlainTextSafely(
                                                    context = context,
                                                    label = "네이버 고정댓글 멘트",
                                                    text = activeVariant.pinnedCommentSnippet,
                                                    composeClipboard = clipboard
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("btn_copy_naver_pinned_comment")
                                        ) {
                                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("네이버 고정댓글 전환 멘트 복사")
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            InfographicVisualGallerySection(
                                auction = auction,
                                variant = activeVariant,
                                report = proReport,
                                funnel = uiState.proFunnel,
                                onShareCardPng = { card ->
                                    val shareUri = InfographicBitmapRenderer.renderCardToShareUri(
                                        context = context,
                                        card = card,
                                        auction = auction,
                                        platform = activeVariant.platform,
                                        report = proReport,
                                        funnel = uiState.proFunnel
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, shareUri)
                                        putExtra(Intent.EXTRA_TEXT, card.altText)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "${auction.caseId} 인포그래픽 PNG 공유"))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QaCheckRow(
    passed: Boolean,
    title: String,
    detail: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (passed) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = if (passed) EmeraldPrimary else CrimsonAlert,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = if (passed) TextSecondary else CrimsonAlert)
        }
    }
}
