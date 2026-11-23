package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.example.engine.ProAnalysisAndPhotoGuideEngine
import com.example.model.ProFunnelConfig
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.CloudflareContainer
import com.example.ui.theme.CloudflareSky
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.NaverGreen
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TistoryAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaidAppVsBlogBenchmarkCard(
    auctionCaseId: String,
    proFunnel: ProFunnelConfig = ProFunnelConfig()
) {
    var expanded by remember(auctionCaseId) { mutableStateOf(true) }
    val rows = remember { ProAnalysisAndPhotoGuideEngine.buildPaidAppVsBlogBenchmark() }

    StudioCard(
        borderColor = TistoryAmber.copy(alpha = 0.65f),
        modifier = Modifier.testTag("paid_app_vs_blog_benchmark_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.CompareArrows, contentDescription = "유료 경매앱 vs 일반 블로그 vs 딱찰 비교", tint = TistoryAmber)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "정보 고급화 비교: 일반 블로그 · 유료 경매앱 vs 딱찰",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "무료 블로그로 유료앱 초월 → 심화 실시간 정보는 딱찰 가입 유도",
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

        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatusChip(
                text = "채무자·임차인 비식별 PASS · 전 채널 딱찰 회원가입 유도 적용",
                textColor = MintSecondary,
                backgroundColor = EmeraldContainer
            )
            StatusChip(
                text = if (proFunnel.funnelEnabled) "가입 퍼널 활성 (${proFunnel.naverSearchKeyword})" else "퍼널 비활성",
                textColor = if (proFunnel.funnelEnabled) CloudflareSky else CrimsonAlert,
                backgroundColor = if (proFunnel.funnelEnabled) CloudflareContainer else AmberContainer
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rows.forEach { row ->
                    Surface(
                        color = NavyBackground,
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = row.dimension,
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "일반 블로그: ${row.generalBlogLevel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                            Text(
                                text = "유료 경매앱: ${row.paidAppLevel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "딱찰 무료 블로그: ${row.ddakchalFreeBlogLevel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MintSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "딱찰 회원가입(PRO): ${row.ddakchalSignupProLevel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TistoryAmber,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Surface(
                    color = NavySurfaceElevated,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(NavySurfaceElevated)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.VerifiedUser,
                                contentDescription = "3채널 회원가입 유도 검증",
                                tint = MintSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "채널별 딱찰 회원가입 전환 동선 ($auctionCaseId)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MintSecondary
                            )
                        }
                        Text(
                            text = "• Cloudflare 자체웹: /signup 딥링크 및 실시간 AI 경쟁률 티저",
                            style = MaterialTheme.typography.bodySmall,
                            color = CloudflareSky
                        )
                        Text(
                            text = "• Tistory: nofollow sponsored PRO 리서치 가입 박스",
                            style = MaterialTheme.typography.bodySmall,
                            color = TistoryAmber
                        )
                        Text(
                            text = "• Naver: 무링크('${proFunnel.naverSearchKeyword} $auctionCaseId' 검색 + 고정댓글 가입 유도)",
                            style = MaterialTheme.typography.bodySmall,
                            color = NaverGreen
                        )
                    }
                }
            }
        }
    }
}
