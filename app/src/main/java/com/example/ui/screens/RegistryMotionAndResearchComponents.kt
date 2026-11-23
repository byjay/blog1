package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.engine.RegistryAndMotionEngine
import com.example.model.AuctionCaseIntel
import com.example.model.MotionGraphicScene
import com.example.model.RegistryRightEntry
import com.example.model.RegistrySection
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
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun RegistryDeedDiagramCard(
    auction: AuctionCaseIntel,
    registryEntries: List<RegistryRightEntry>
) {
    var expanded by remember(auction.caseId) { mutableStateOf(true) }

    StudioCard(
        borderColor = TistoryAmber.copy(alpha = 0.5f),
        modifier = Modifier.testTag("registry_deed_diagram_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Gavel, contentDescription = "등기부등본 갑구·을구 권리 해부", tint = TistoryAmber)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "등기부등본 갑구·을구 권리 해부 도식",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "${auction.caseId} · 말소기준 ${auction.baseRightDate} 기준 소멸·인수 타임라인",
                        style = MaterialTheme.typography.bodySmall,
                        color = TistoryAmber
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
                registryEntries.forEach { entry ->
                    val badgeColor = when {
                        entry.isAssumedRisk -> CrimsonAlert
                        entry.isBaseRight -> TistoryAmber
                        else -> MintSecondary
                    }
                    val badgeBg = when {
                        entry.isAssumedRisk -> CrimsonContainer
                        entry.isBaseRight -> AmberContainer
                        else -> EmeraldContainer
                    }
                    val sectionColor = when (entry.section) {
                        RegistrySection.GAP_GU -> CloudflareSky
                        RegistrySection.EUL_GU -> TistoryAmber
                        RegistrySection.TENANT_COURT -> MintSecondary
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(badgeColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            color = NavyBackground,
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(
                                width = if (entry.isBaseRight || entry.isAssumedRisk) 1.5.dp else 1.dp,
                                color = if (entry.isBaseRight || entry.isAssumedRisk) badgeColor.copy(alpha = 0.7f) else NavyBorder
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${entry.orderNo} · ${entry.registeredDate}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                                        color = sectionColor
                                    )
                                    StatusChip(
                                        text = entry.outcomeLabel,
                                        textColor = badgeColor,
                                        backgroundColor = badgeBg
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(entry.rightTitle, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Text(entry.amountText, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MotionGraphicGuideAndPlayerCard(
    auction: AuctionCaseIntel,
    scenes: List<MotionGraphicScene>
) {
    if (scenes.isEmpty()) return
    val clipboard = LocalClipboardManager.current
    var expanded by remember(auction.caseId) { mutableStateOf(true) }
    var activeSceneIdx by remember(auction.caseId) { mutableIntStateOf(0) }
    var isPlaying by remember(auction.caseId) { mutableStateOf(false) }

    LaunchedEffect(isPlaying, activeSceneIdx) {
        if (isPlaying) {
            delay(2800L)
            activeSceneIdx = (activeSceneIdx + 1) % scenes.size
        }
    }

    val currentScene = scenes[activeSceneIdx.coerceIn(0, scenes.lastIndex)]
    val infiniteTransition = rememberInfiniteTransition(label = "motionPreview")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseBar"
    )

    StudioCard(
        borderColor = CloudflareSky.copy(alpha = 0.55f),
        modifier = Modifier.testTag("motion_graphic_guide_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.MovieCreation, contentDescription = "5장면 모션 그래픽 및 동영상 제작 가이드", tint = CloudflareSky)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "65초 5장면 모션 그래픽 · 네이버 클립 제작 가이드",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "동영상 삽입 글 체류시간 +38% · 타인 영상 재사용 금지 고유 콘티",
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    scenes.forEachIndexed { idx, scene ->
                        FilterChip(
                            selected = idx == activeSceneIdx,
                            onClick = {
                                activeSceneIdx = idx
                                isPlaying = false
                            },
                            label = { Text("장면 ${scene.sceneNumber} (${scene.durationSec}s)") }
                        )
                    }
                }

                Surface(
                    color = NavyBackground,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, CloudflareSky.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(NavyBackground, NavySurfaceElevated, CloudflareContainer.copy(alpha = 0.6f))
                                )
                            )
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val activeFraction = (activeSceneIdx + 1).toFloat() / scenes.size.toFloat()
                            val barWidth = (size.width - 32.dp.toPx()) * if (isPlaying) pulseProgress else activeFraction
                            drawRoundRect(
                                color = EmeraldPrimary.copy(alpha = 0.55f),
                                topLeft = Offset(16.dp.toPx(), size.height - 20.dp.toPx()),
                                size = Size(barWidth.coerceAtLeast(24f), 6.dp.toPx()),
                                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusChip(
                                    text = "SCENE ${currentScene.sceneNumber}/5 · ${currentScene.timeRange}",
                                    textColor = CloudflareSky,
                                    backgroundColor = CloudflareContainer
                                )
                                Button(
                                    onClick = { isPlaying = !isPlaying },
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("btn_motion_play_toggle"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isPlaying) TistoryAmber else EmeraldPrimary,
                                        contentColor = NavyBackground
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = if (isPlaying) "미리보기 일시정지" else "5장면 미리보기 재생",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isPlaying) "정지" else "재생")
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = currentScene.sceneTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "화면 자막: ${currentScene.onScreenCaption}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MintSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = "모션 연출: ${currentScene.motionEffect}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TistoryAmber,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Surface(
                    color = NavySurfaceElevated,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("성우/TTS 나레이션 대본", style = MaterialTheme.typography.labelMedium, color = CloudflareSky)
                        Text(currentScene.narrationScript, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        Text("사용 소스: ${currentScene.visualAssetHint}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }

                val fullStoryboardText = scenes.joinToString("\n\n") { s ->
                    "[장면 ${s.sceneNumber} | ${s.timeRange}] ${s.sceneTitle}\n- 연출: ${s.motionEffect}\n- 자막: ${s.onScreenCaption}\n- 나레이션: ${s.narrationScript}\n- 소스: ${s.visualAssetHint}"
                }
                OutlinedButton(
                    onClick = { clipboard.setText(AnnotatedString(fullStoryboardText)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_copy_motion_storyboard")
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("5장면 콘티·나레이션 대본 전체 복사")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NaverMarketingResearchAndSimulatorSection() {
    val insights = remember { RegistryAndMotionEngine.naverTopBlogResearchInsights() }
    var dailyVisitors by remember { mutableFloatStateOf(2500f) }
    var cpmKrw by remember { mutableFloatStateOf(2600f) }
    var dbConversionPct by remember { mutableFloatStateOf(0.45f) }
    var proConversionPct by remember { mutableFloatStateOf(1.2f) }

    val forecast = remember(dailyVisitors, cpmKrw, dbConversionPct, proConversionPct) {
        RegistryAndMotionEngine.simulateMarketingRevenue(
            dailyVisitors = dailyVisitors.roundToInt(),
            cpmKrw = cpmKrw.roundToInt(),
            dbConversionPct = dbConversionPct.toDouble(),
            proConversionPct = proConversionPct.toDouble()
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StudioCard(
            borderColor = EmeraldPrimary.copy(alpha = 0.55f),
            modifier = Modifier.testTag("marketing_revenue_simulator_card")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.TrendingUp, contentDescription = "방문자 및 수익화 시뮬레이터", tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "방문자수 · 애드포스트(CPM) · 매수대리DB · 딱찰PRO 수익 계산기",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "네이버 부동산 전문 블로그 100+ · 사건 300+ 심층 분석 벤치마크 기반",
                        style = MaterialTheme.typography.bodySmall,
                        color = MintSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(NavySurfaceElevated)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricPair(
                    label = "애드포스트(월)",
                    value = QaVerificationEngine.formatKrwShort(forecast.monthlyAdpostKrw),
                    valueColor = CloudflareSky,
                    modifier = Modifier.weight(1f)
                )
                MetricPair(
                    label = "매수대리DB (${forecast.monthlyDbLeads}건)",
                    value = QaVerificationEngine.formatKrwShort(forecast.monthlyDbRevenueKrw),
                    valueColor = TistoryAmber,
                    modifier = Modifier.weight(1f)
                )
                MetricPair(
                    label = "딱찰PRO (${forecast.monthlyProConversions}건)",
                    value = QaVerificationEngine.formatKrwShort(forecast.monthlyProRevenueKrw),
                    valueColor = MintSecondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = EmeraldContainer,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("월 예상 결합 마케팅 순수익", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                    Text(
                        text = QaVerificationEngine.formatKrwShort(forecast.totalMonthlyRevenueKrw),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MintSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            SimulatorSliderRow(
                label = "일일 블로그 방문자수",
                valueText = "${forecast.dailyVisitors}명/일 (월 ${forecast.dailyVisitors * 30}명)",
                value = dailyVisitors,
                range = 300f..10000f,
                onValueChange = { dailyVisitors = (it / 100f).roundToInt() * 100f }
            )
            SimulatorSliderRow(
                label = "애드포스트 부동산 CPM",
                valueText = "${forecast.cpmKrw}원 / 1,000노출",
                value = cpmKrw,
                range = 1500f..4000f,
                onValueChange = { cpmKrw = (it / 100f).roundToInt() * 100f }
            )
            SimulatorSliderRow(
                label = "매수대리 상담 DB 전환율",
                valueText = "${String.format("%.2f", dbConversionPct)}%",
                value = dbConversionPct,
                range = 0.1f..1.5f,
                onValueChange = { dbConversionPct = it }
            )
            SimulatorSliderRow(
                label = "딱찰 PRO 유료 전환율",
                valueText = "${String.format("%.1f", proConversionPct)}%",
                value = proConversionPct,
                range = 0.2f..3.0f,
                onValueChange = { proConversionPct = it }
            )
        }

        PaidAppVsBlogBenchmarkCard(auctionCaseId = "대표 물건 벤치마크")

        insights.forEach { insight ->
            StudioCard(
                borderColor = CloudflareSky.copy(alpha = 0.4f),
                containerColor = NavySurface
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Analytics, contentDescription = insight.pillar, tint = CloudflareSky, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(insight.pillar, style = MaterialTheme.typography.labelMedium, color = CloudflareSky)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(insight.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow {
                    StatusChip(
                        text = insight.metricHighlight,
                        textColor = MintSecondary,
                        backgroundColor = EmeraldContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                insight.actionRules.forEach { rule ->
                    Text(
                        text = "- $rule",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulatorSliderRow(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                color = TextPrimary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = EmeraldPrimary,
                activeTrackColor = EmeraldPrimary
            )
        )
    }
}
