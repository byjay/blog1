package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.ChannelIndexHealth
import com.example.model.ChannelPlatform
import com.example.model.LowQualityRiskLevel
import com.example.model.LowQualityRiskReport
import com.example.model.QueueAnalyticsSummary
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
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TistoryAmber
import org.json.JSONArray
import org.json.JSONObject

object RechartsDashboardHtmlBuilder {

    fun buildPayloadJson(
        analytics: QueueAnalyticsSummary,
        syncedAt: String,
        sourceEndpoint: String
    ): String {
        val cleanEndpoint = sourceEndpoint.substringBefore("?")
        val dailyArr = JSONArray().apply {
            analytics.dailySeries.forEach { pt ->
                put(
                    JSONObject().apply {
                        put("date", pt.dateLabel)
                        put("published", pt.publishedCount)
                        put("queued", pt.queuedCount)
                        put("failed", pt.failedCount)
                    }
                )
            }
        }
        val statusArr = JSONArray().apply {
            put(JSONObject().apply { put("name", "발행완료"); put("value", analytics.publishedCount); put("color", "#10B981") })
            put(JSONObject().apply { put("name", "예약대기"); put("value", analytics.queuedCount); put("color", "#38BDF8") })
            put(JSONObject().apply { put("name", "초안검수"); put("value", analytics.draftCount); put("color", "#F59E0B") })
            put(JSONObject().apply { put("name", "실패·중단"); put("value", analytics.failedOrHandoffCount); put("color", "#F43F5E") })
        }
        val channelArr = JSONArray().apply {
            analytics.channelStats.forEach { st ->
                put(
                    JSONObject().apply {
                        put("channel", st.platform.displayName)
                        put("published", st.publishedCount)
                        put("queued", st.queuedCount)
                        put("failed", st.failedCount)
                    }
                )
            }
        }
        return JSONObject().apply {
            put("totalSlots", analytics.totalSlots)
            put("successRatePct", analytics.successRatePct)
            put("failureRatePct", analytics.failureRatePct)
            put("syncedAt", syncedAt)
            put("sourceEndpoint", cleanEndpoint)
            put("daily", dailyArr)
            put("statusPie", statusArr)
            put("channels", channelArr)
        }.toString()
    }

    fun buildHtml(
        analytics: QueueAnalyticsSummary,
        syncedAt: String,
        sourceEndpoint: String
    ): String {
        val safeJson = buildPayloadJson(analytics, syncedAt, sourceEndpoint)
            .replace("</", "<\\/")

        return """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
              <meta charset="utf-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0" />
              <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body {
                  background: #0B111E;
                  color: #F1F5F9;
                  font-family: -apple-system, BlinkMacSystemFont, "Noto Sans KR", sans-serif;
                  padding: 8px;
                }
                .chart-card {
                  background: #131C2E;
                  border: 1px solid #24324D;
                  border-radius: 10px;
                  padding: 10px;
                  margin-bottom: 10px;
                }
                .chart-title {
                  font-size: 12px;
                  font-weight: 700;
                  color: #E2E8F0;
                  margin-bottom: 6px;
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                }
                .chart-sub {
                  font-size: 10px;
                  color: #94A3B8;
                }
                .fallback-box {
                  font-size: 11px;
                  color: #94A3B8;
                  line-height: 1.5;
                  padding: 6px 4px;
                }
                .badge-ok { color: #34D399; font-weight: 700; }
                .badge-warn { color: #38BDF8; font-weight: 700; }
                .badge-err { color: #FB7185; font-weight: 700; }
              </style>
              <script crossorigin src="https://unpkg.com/react@18.3.1/umd/react.production.min.js"></script>
              <script crossorigin src="https://unpkg.com/react-dom@18.3.1/umd/react-dom.production.min.js"></script>
              <script crossorigin src="https://unpkg.com/prop-types@15.8.1/prop-types.min.js"></script>
              <script crossorigin src="https://unpkg.com/recharts@2.12.7/umd/Recharts.js"></script>
            </head>
            <body>
              <div id="recharts-root"></div>
              <script>
                window.__DDAKCHAL_QUEUE_DATA__ = $safeJson;
                (function () {
                  var data = window.__DDAKCHAL_QUEUE_DATA__;
                  var rootEl = document.getElementById("recharts-root");
                  function renderOfflineSummary(reason) {
                    var dailyRows = (data.daily || []).map(function(d) {
                      return d.date + ": 완료 " + d.published + " / 대기 " + d.queued + " / 실패 " + d.failed;
                    }).join(" · ");
                    var chRows = (data.channels || []).map(function(c) {
                      return c.channel + "(완료 " + c.published + ", 대기 " + c.queued + ", 실패 " + c.failed + ")";
                    }).join(" · ");
                    rootEl.innerHTML =
                      '<div class="chart-card">' +
                        '<div class="chart-title"><span>Recharts 실시간 큐 요약 (' + reason + ')</span><span class="chart-sub">' + (data.syncedAt || '') + '</span></div>' +
                        '<div class="fallback-box">' +
                          '<div>총 <span class="badge-warn">' + data.totalSlots + '건</span> · 성공률 <span class="badge-ok">' + data.successRatePct + '%</span> · 실패율 <span class="badge-err">' + data.failureRatePct + '%</span></div>' +
                          '<div style="margin-top:4px;">일일 추이: ' + (dailyRows || '데이터 없음') + '</div>' +
                          '<div style="margin-top:4px;">채널 분포: ' + (chRows || '데이터 없음') + '</div>' +
                        '</div>' +
                      '</div>';
                  }

                  if (!window.React || !window.ReactDOM || !window.Recharts) {
                    renderOfflineSummary("오프라인/CDN 미연결");
                    return;
                  }

                  try {
                    var e = React.createElement;
                    var R = window.Recharts;
                    var pieData = (data.statusPie || []).filter(function(item) { return item.value > 0; });
                    if (pieData.length === 0) {
                      pieData = [{ name: "큐 데이터 없음", value: 1, color: "#334155" }];
                    }

                    function DashboardApp() {
                      return e("div", null,
                        e("div", { className: "chart-card" },
                          e("div", { className: "chart-title" },
                            e("span", null, "1. 일일 발행량 추이 (Recharts BarChart)"),
                            e("span", { className: "chart-sub" }, "총 " + data.totalSlots + "건")
                          ),
                          e(R.ResponsiveContainer, { width: "100%", height: 155 },
                            e(R.BarChart, { data: data.daily || [], margin: { top: 6, right: 8, left: -22, bottom: 2 } },
                              e(R.CartesianGrid, { strokeDasharray: "3 3", stroke: "#24324D" }),
                              e(R.XAxis, { dataKey: "date", stroke: "#94A3B8", fontSize: 10 }),
                              e(R.YAxis, { stroke: "#94A3B8", fontSize: 10, allowDecimals: false }),
                              e(R.Tooltip, { contentStyle: { backgroundColor: "#0F172A", borderColor: "#334155", fontSize: "11px" } }),
                              e(R.Legend, { wrapperStyle: { fontSize: "10px" } }),
                              e(R.Bar, { dataKey: "published", name: "발행완료", stackId: "q", fill: "#10B981", radius: [2, 2, 0, 0] }),
                              e(R.Bar, { dataKey: "queued", name: "대기·초안", stackId: "q", fill: "#38BDF8" }),
                              e(R.Bar, { dataKey: "failed", name: "실패·중단", stackId: "q", fill: "#F43F5E", radius: [2, 2, 0, 0] })
                            )
                          )
                        ),
                        e("div", { className: "chart-card" },
                          e("div", { className: "chart-title" },
                            e("span", null, "2. 성공 · 실패율 분포 (Recharts PieChart)"),
                            e("span", { className: "badge-ok" }, "성공 " + data.successRatePct + "% / 실패 " + data.failureRatePct + "%")
                          ),
                          e(R.ResponsiveContainer, { width: "100%", height: 150 },
                            e(R.PieChart, null,
                              e(R.Pie, {
                                data: pieData,
                                dataKey: "value",
                                nameKey: "name",
                                cx: "50%",
                                cy: "50%",
                                innerRadius: 34,
                                outerRadius: 54,
                                paddingAngle: 2
                              },
                              pieData.map(function(entry, idx) {
                                return e(R.Cell, { key: "cell-" + idx, fill: entry.color });
                              })),
                              e(R.Tooltip, { contentStyle: { backgroundColor: "#0F172A", borderColor: "#334155", fontSize: "11px" } }),
                              e(R.Legend, { wrapperStyle: { fontSize: "10px" } })
                            )
                          )
                        ),
                        e("div", { className: "chart-card", style: { marginBottom: 0 } },
                          e("div", { className: "chart-title" },
                            e("span", null, "3. 채널별 발행 분포 (Recharts Horizontal Bar)"),
                            e("span", { className: "chart-sub" }, data.syncedAt || "")
                          ),
                          e(R.ResponsiveContainer, { width: "100%", height: 150 },
                            e(R.BarChart, { layout: "vertical", data: data.channels || [], margin: { top: 4, right: 12, left: 8, bottom: 2 } },
                              e(R.CartesianGrid, { strokeDasharray: "3 3", stroke: "#24324D" }),
                              e(R.XAxis, { type: "number", stroke: "#94A3B8", fontSize: 10, allowDecimals: false }),
                              e(R.YAxis, { type: "category", dataKey: "channel", stroke: "#E2E8F0", fontSize: 10, width: 72 }),
                              e(R.Tooltip, { contentStyle: { backgroundColor: "#0F172A", borderColor: "#334155", fontSize: "11px" } }),
                              e(R.Legend, { wrapperStyle: { fontSize: "10px" } }),
                              e(R.Bar, { dataKey: "published", name: "완료", stackId: "ch", fill: "#10B981" }),
                              e(R.Bar, { dataKey: "queued", name: "대기", stackId: "ch", fill: "#38BDF8" }),
                              e(R.Bar, { dataKey: "failed", name: "실패", stackId: "ch", fill: "#F43F5E" })
                            )
                          )
                        )
                      );
                    }

                    if (ReactDOM.createRoot) {
                      ReactDOM.createRoot(rootEl).render(e(DashboardApp));
                    } else {
                      ReactDOM.render(e(DashboardApp), rootEl);
                    }
                  } catch (err) {
                    renderOfflineSummary("렌더 예외");
                  }
                })();
              </script>
            </body>
            </html>
        """.trimIndent()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RechartsQueueWebView(
    analytics: QueueAnalyticsSummary,
    syncedAt: String,
    sourceEndpoint: String,
    compactMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val htmlContent = remember(analytics, syncedAt, sourceEndpoint) {
        RechartsDashboardHtmlBuilder.buildHtml(analytics, syncedAt, sourceEndpoint)
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(if (compactMode) 480.dp else 520.dp)
            .clip(MaterialTheme.shapes.small)
            .testTag("webview_recharts_queue"),
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(AndroidColor.parseColor("#0B111E"))
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean = true
                }
                tag = htmlContent.hashCode()
                loadDataWithBaseURL(
                    "https://appassets.androidplatform.net/",
                    htmlContent,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        update = { webView ->
            val nextHash = htmlContent.hashCode()
            if (webView.tag != nextHash) {
                webView.tag = nextHash
                webView.loadDataWithBaseURL(
                    "https://appassets.androidplatform.net/",
                    htmlContent,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LowQualityRiskDashboardCard(
    report: LowQualityRiskReport?,
    autoPublishPaused: Boolean,
    pauseReason: String?,
    autoPauseEnabled: Boolean,
    isBusy: Boolean,
    onEvaluateRisk: () -> Unit,
    onResumeAutoPublish: () -> Unit,
    onToggleAutoPause: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    compactMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val overallRisk = report?.overallRisk ?: LowQualityRiskLevel.SAFE
    val borderAccent = when {
        autoPublishPaused || overallRisk == LowQualityRiskLevel.HIGH_RISK -> CrimsonAlert
        overallRisk == LowQualityRiskLevel.CAUTION -> TistoryAmber
        else -> EmeraldPrimary.copy(alpha = 0.5f)
    }

    StudioCard(
        borderColor = borderAccent,
        modifier = modifier.testTag("low_quality_risk_dashboard_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = when {
                        autoPublishPaused || overallRisk == LowQualityRiskLevel.HIGH_RISK -> Icons.Filled.Warning
                        overallRisk == LowQualityRiskLevel.CAUTION -> Icons.Filled.Shield
                        else -> Icons.Filled.HealthAndSafety
                    },
                    contentDescription = "저품질 위험도 대시보드",
                    tint = when {
                        autoPublishPaused || overallRisk == LowQualityRiskLevel.HIGH_RISK -> CrimsonAlert
                        overallRisk == LowQualityRiskLevel.CAUTION -> TistoryAmber
                        else -> MintSecondary
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "채널별 지수 변화 · 저품질 위험도 대시보드",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = if (report != null) {
                            "위험점수 ${report.overallRiskScore}pt · 최근 점검 ${report.evaluatedAt}"
                        } else {
                            "발행 후 지수 변동·오류율·유입 저하를 실시간 감시합니다."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            StatusChip(
                text = overallRisk.badgeText,
                textColor = when (overallRisk) {
                    LowQualityRiskLevel.SAFE -> MintSecondary
                    LowQualityRiskLevel.CAUTION -> TistoryAmber
                    LowQualityRiskLevel.HIGH_RISK -> CrimsonAlert
                },
                backgroundColor = when (overallRisk) {
                    LowQualityRiskLevel.SAFE -> EmeraldContainer
                    LowQualityRiskLevel.CAUTION -> AmberContainer
                    LowQualityRiskLevel.HIGH_RISK -> CrimsonContainer
                }
            )
        }

        if (autoPublishPaused) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = CrimsonContainer,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, CrimsonAlert),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("banner_auto_publish_paused_risk")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.PauseCircle,
                            contentDescription = "자동 발행 일시 중지됨",
                            tint = CrimsonAlert,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "저품질 위험도 '위험 높음' — 자동 발행 일시 중지됨",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonAlert
                        )
                    }
                    Text(
                        text = pauseReason ?: "채널 지수 하락 또는 연속 발행 오류로 자동 발행이 보호 차원에서 일시 중지되었습니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onResumeAutoPublish,
                            enabled = !isBusy,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                                .testTag("btn_resume_auto_publish_risk"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = NavyBackground
                            )
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("조치 후 자동발행 재개", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .weight(0.9f)
                                .height(48.dp)
                        ) {
                            Text("세션·규칙 설정", maxLines = 1)
                        }
                    }
                }
            }
        }

        if (report != null && report.channelHealthList.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                report.channelHealthList.forEach { ch ->
                    ChannelIndexHealthRow(health = ch, compactMode = compactMode)
                }
            }

            if (report.triggeredAlerts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = NavyBackground,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, TistoryAmber.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("감지된 지수 이상·위험 경보", style = MaterialTheme.typography.labelLarge, color = TistoryAmber)
                        report.triggeredAlerts.forEach { alert ->
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = TistoryAmber, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(alert, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                        }
                    }
                }
            }

            if (report.recoveryActions.isNotEmpty() && !compactMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = NavySurfaceElevated,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("권장 복구 · 저품질 방어 액션", style = MaterialTheme.typography.labelMedium, color = MintSecondary)
                        report.recoveryActions.forEach { act ->
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(act, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("위험도 '높음' 시 자동 발행 자동 중지", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                Text(
                    text = if (autoPauseEnabled) "활성 (지수 급락·연속 실패 시 즉시 차단)" else "비활성 (수동 경고만 표시)",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (autoPauseEnabled) MintSecondary else TistoryAmber
                )
            }
            Switch(
                checked = autoPauseEnabled,
                onCheckedChange = onToggleAutoPause,
                modifier = Modifier.testTag("switch_auto_pause_on_high_risk")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onEvaluateRisk,
            enabled = !isBusy,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_evaluate_low_quality_risk")
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("채널 지수 · 저품질 위험도 즉시 재진단")
        }
    }
}

@Composable
private fun ChannelIndexHealthRow(
    health: ChannelIndexHealth,
    compactMode: Boolean
) {
    val channelColor = when (health.platform) {
        ChannelPlatform.NAVER -> NaverGreen
        ChannelPlatform.TISTORY -> TistoryAmber
        ChannelPlatform.CLOUDFLARE -> CloudflareSky
    }
    val riskColor = when (health.riskLevel) {
        LowQualityRiskLevel.SAFE -> MintSecondary
        LowQualityRiskLevel.CAUTION -> TistoryAmber
        LowQualityRiskLevel.HIGH_RISK -> CrimsonAlert
    }
    val riskBg = when (health.riskLevel) {
        LowQualityRiskLevel.SAFE -> EmeraldContainer
        LowQualityRiskLevel.CAUTION -> AmberContainer
        LowQualityRiskLevel.HIGH_RISK -> CrimsonContainer
    }
    val deltaText = if (health.indexDelta >= 0) "+${health.indexDelta}p" else "${health.indexDelta}p"

    Surface(
        color = NavyBackground,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, if (health.riskLevel == LowQualityRiskLevel.HIGH_RISK) CrimsonAlert.copy(alpha = 0.7f) else NavyBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = health.platform.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        color = channelColor
                    )
                    Icon(
                        imageVector = if (health.indexDelta >= 0) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                        contentDescription = null,
                        tint = if (health.indexDelta >= 0) MintSecondary else CrimsonAlert,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "지수 ${health.currentIndexScore} (${deltaText})",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                        color = TextPrimary
                    )
                }
                StatusChip(
                    text = health.riskLevel.badgeText,
                    textColor = riskColor,
                    backgroundColor = riskBg
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "완료 ${health.publishedCount} · 실패 ${health.failedCount} (오류율 ${health.errorRatePct}%)",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                    color = TextSecondary
                )
                Text(
                    text = "노출 ${health.exposureScore}점 · 체류 ${health.avgDwellSec}초",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                    color = CloudflareSky
                )
            }

            if (!compactMode || health.riskLevel != LowQualityRiskLevel.SAFE) {
                Text(
                    text = health.riskFactors.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (health.riskLevel == LowQualityRiskLevel.SAFE) TextMuted else riskColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
