package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.ServerConfigStore
import com.example.ui.util.SafeClipboardHelper
import com.example.engine.BoilerplateAndTemplateEngine
import com.example.model.ChannelPlatform
import com.example.model.ServerAutomationConfig
import com.example.ui.StudioUiState
import com.example.ui.theme.CloudflareContainer
import com.example.ui.theme.CloudflareSky
import com.example.ui.theme.CrimsonAlert
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
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CloudflareServerScreen(
    uiState: StudioUiState,
    onSaveConfig: (ServerAutomationConfig) -> Unit,
    onVerifyCloudflare: () -> Unit,
    onMigrateD1: () -> Unit,
    onRegisterCron: () -> Unit,
    onQueryD1Queue: () -> Unit,
    onTriggerHandoffTest: (ChannelPlatform) -> Unit,
    onResolveHandoff: () -> Unit,
    onRefreshSecurityAudit: () -> Unit = {},
    onPurgePlaintextPw: () -> Unit = {},
    onProbeNaverAndTistory: () -> Unit = {},
    onSaveQuickSettings: (
        cloudflareSiteBase: String,
        cloudflareBlogPath: String,
        naverKeepLoggedIn: Boolean,
        tistoryKeepLoggedIn: Boolean,
        tistoryAutoDirectPublish: Boolean,
        grokFallbackEnabled: Boolean,
        grokModelName: String
    ) -> Unit = { _, _, _, _, _, _, _ -> },
    onRunHeartbeat: () -> Unit = {},
    onSyncGoogleAuthState: (String?) -> Unit = {},
    onSignOutGoogle: () -> Unit = {},
    onUpdateGrokConfig: (Boolean, String) -> Unit = { _, _ -> },
    onRunPerfectDdakchalDiag: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var draft by remember { mutableStateOf(uiState.config) }
    var showSecrets by remember { mutableStateOf(false) }
    var exportTab by remember { mutableStateOf("WRANGLER") } // WRANGLER | WORKER_TS | D1_SQL | SELECTORS | ENV
    var bundleCopyMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.config) {
        draft = uiState.config
    }

    val pwTransformation = if (showSecrets) VisualTransformation.None else PasswordVisualTransformation()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "환경설정 · 보안 현황 대시보드 · CF서버",
                subtitle = "상시 로그인 · aution.codeb.app · Google Gemini(1순위)/Grok(2순위) · 딱찰 D1/토큰 보안",
                icon = Icons.Filled.Security,
                easyGuide = "딱찰 서버·Cloudflare D1 키, 블로그 자동 로그인 유지, AI 모델을 안전하게 설정하고 진단합니다.",
                trailing = {
                    IconButton(
                        onClick = { showSecrets = !showSecrets },
                        modifier = Modifier.testTag("btn_toggle_secret_mask")
                    ) {
                        Icon(
                            imageVector = if (showSecrets) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "비밀키 표시 전환",
                            tint = CloudflareSky
                        )
                    }
                }
            )
        }

        item {
            QuickSettingsAndAlwaysLoginCard(
                config = uiState.config,
                isBusy = uiState.isBusy,
                onSaveQuickSettings = onSaveQuickSettings,
                onRunHeartbeat = onRunHeartbeat
            )
        }

        item {
            GoogleGeminiAndGrokPriorityCard(
                googleAccount = uiState.googleAccount,
                config = uiState.config,
                lastAiEngineUsed = uiState.lastAiEngineUsed,
                isBusy = uiState.isBusy,
                onSyncGoogleAuthState = onSyncGoogleAuthState,
                onSignOutGoogle = onSignOutGoogle,
                onUpdateGrokConfig = onUpdateGrokConfig
            )
        }

        item {
            DdakchalPerfectDiagnosticCard(
                report = uiState.ddakchalFullDiagnostic,
                isBusy = uiState.isBusy,
                onRunFullDiagnostic = onRunPerfectDdakchalDiag
            )
        }

        item {
            SecurityDashboardHeroCard(
                summary = uiState.securityDashboard,
                isBusy = uiState.isBusy,
                onRefreshAudit = onRefreshSecurityAudit,
                onPurgePlaintextPw = onPurgePlaintextPw
            )
        }

        item {
            TokenHygieneCard(
                summary = uiState.securityDashboard
            )
        }

        item {
            NaverTistoryLoginManagementCard(
                config = uiState.config,
                naverProbe = uiState.naverProbeResult,
                tistoryProbe = uiState.tistoryProbeResult,
                isBusy = uiState.isBusy,
                onProbeConnections = onProbeNaverAndTistory,
                onResolveHandoff = onResolveHandoff
            )
        }

        // Cloudflare Credentials & API Connection
        item {
            StudioCard(borderColor = CloudflareSky.copy(alpha = 0.5f)) {
                Text("1. Cloudflare & 딱찰 API 연결 설정", style = MaterialTheme.typography.titleSmall, color = CloudflareSky)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = draft.ddakchalApiBase,
                    onValueChange = { draft = draft.copy(ddakchalApiBase = it) },
                    label = { Text("DDAKCHAL_API_BASE") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = draft.crawlerSyncToken,
                    onValueChange = { draft = draft.copy(crawlerSyncToken = it) },
                    label = { Text("CRAWLER_SYNC_TOKEN (${ServerConfigStore.maskSecret(draft.crawlerSyncToken)})") },
                    visualTransformation = pwTransformation,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = draft.cfAccountId,
                    onValueChange = { draft = draft.copy(cfAccountId = it) },
                    label = { Text("CF_ACCOUNT_ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = draft.cfApiToken,
                    onValueChange = { draft = draft.copy(cfApiToken = it) },
                    label = { Text("CF_API_TOKEN (${ServerConfigStore.maskSecret(draft.cfApiToken)})") },
                    visualTransformation = pwTransformation,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = draft.cfD1DatabaseId,
                        onValueChange = { draft = draft.copy(cfD1DatabaseId = it) },
                        label = { Text("D1 Database ID") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = draft.cfWorkerScriptName,
                        onValueChange = { draft = draft.copy(cfWorkerScriptName = it) },
                        label = { Text("Worker Script") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = draft.cronExpression,
                    onValueChange = { draft = draft.copy(cronExpression = it) },
                    label = { Text("Worker Cron 스케줄 (정각 :00 금지, 예: 17 8 * * *)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onSaveConfig(draft) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_save_server_config"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("설정 기기 내 DataStore 저장")
                }
            }
        }

        // One-Click Cloudflare Remote Server Automation
        item {
            StudioCard {
                Text("2. 클라우드플레어 서버 내 원격 자동 세팅", style = MaterialTheme.typography.titleSmall, color = MintSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "최근 상태: ${uiState.telemetry.lastD1ExecutionSummary}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (uiState.telemetry.activeCronSchedules.isNotEmpty()) {
                    Text(
                        text = "활성 Cron: ${uiState.telemetry.activeCronSchedules.joinToString()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = CloudflareSky
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onVerifyCloudflare,
                        enabled = !uiState.isBusy,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_cf_verify")
                    ) {
                        Icon(Icons.Filled.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("D1·계정 검증", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = onQueryD1Queue,
                        enabled = !uiState.isBusy,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_cf_query_d1")
                    ) {
                        Icon(Icons.Filled.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("D1 큐 조회", maxLines = 1)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onMigrateD1,
                        enabled = !uiState.isBusy,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_cf_migrate_d1"),
                        colors = ButtonDefaults.buttonColors(containerColor = CloudflareContainer, contentColor = CloudflareSky)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("D1 스키마 자동생성", maxLines = 1)
                    }
                    Button(
                        onClick = onRegisterCron,
                        enabled = !uiState.isBusy,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_cf_register_cron"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldContainer, contentColor = MintSecondary)
                    ) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cron 자동등록", maxLines = 1)
                    }
                }
            }
        }

        // Anti-Bot Rate Limits & Playwright Session Control
        item {
            StudioCard {
                Text("3. 저품질 방지 제어 변수 & Playwright 세션", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "일일 최대 발행 수 (DAILY_POST_LIMIT): ${draft.dailyPostLimit}건 (주간 예상 ${draft.dailyPostLimit * 7}건 · 일 3~5건 권장)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MintSecondary
                )
                Slider(
                    value = draft.dailyPostLimit.toFloat().coerceIn(1f, 10f),
                    onValueChange = {
                        val nextDaily = it.roundToInt().coerceIn(1, 10)
                        draft = draft.copy(dailyPostLimit = nextDaily, weeklyTargetPosts = nextDaily * 7)
                    },
                    valueRange = 1f..10f,
                    steps = 8
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = draft.minDelayMinutes.toString(),
                        onValueChange = {
                            val v = it.filter(Char::isDigit).toIntOrNull() ?: 45
                            draft = draft.copy(minDelayMinutes = v)
                        },
                        label = { Text("MIN_DELAY(분)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = draft.maxDelayMinutes.toString(),
                        onValueChange = {
                            val v = it.filter(Char::isDigit).toIntOrNull() ?: 120
                            draft = draft.copy(maxDelayMinutes = v)
                        },
                        label = { Text("MAX_DELAY(분)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ENABLE_EXTERNAL_LINK_NAVER", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                        Text(
                            if (draft.enableExternalLinkNaver) "허용 (최하단 1회만 배치)" else "차단 (배너 이미지+프로필/고정댓글 유도 권장)",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (draft.enableExternalLinkNaver) TistoryAmber else MintSecondary
                        )
                    }
                    Switch(
                        checked = draft.enableExternalLinkNaver,
                        onCheckedChange = {
                            val updated = draft.copy(enableExternalLinkNaver = it)
                            draft = updated
                            onSaveConfig(updated)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("저품질 '위험 높음' 시 자동 발행 일시 중지", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                        Text(
                            if (draft.autoPauseOnHighRisk) "활성 (지수 급락·연속 실패 시 즉시 자동발행 차단)" else "해제 (위험 경고만 표시)",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (draft.autoPauseOnHighRisk) MintSecondary else TistoryAmber
                        )
                    }
                    Switch(
                        checked = draft.autoPauseOnHighRisk,
                        onCheckedChange = {
                            val updated = draft.copy(autoPauseOnHighRisk = it)
                            draft = updated
                            onSaveConfig(updated)
                        },
                        modifier = Modifier.testTag("switch_settings_auto_pause_risk")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = NavySurfaceElevated,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("세션 쿠키 상태 (storage_state / ego-browser)", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                        Text("• session_naver.json: ${draft.naverSessionStatus}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("• session_tistory.json: ${draft.tistorySessionStatus}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onTriggerHandoffTest(ChannelPlatform.NAVER) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("네이버 캡차 중단 테스트", maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = onResolveHandoff,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("세션 복구", maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // Optional LLM Endpoint & 3-Channel Destinations (.env + DataStore)
        item {
            StudioCard {
                Text("4. 3곳 블로그 등록처 · 포털 계정 & LLM 엔드포인트", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("① 네이버 블로그 등록 활성화", style = MaterialTheme.typography.labelLarge, color = MintSecondary)
                    Switch(
                        checked = draft.publishTargetNaver,
                        onCheckedChange = { draft = draft.copy(publishTargetNaver = it) }
                    )
                }
                OutlinedTextField(
                    value = draft.naverBlogId,
                    onValueChange = { draft = draft.copy(naverBlogId = it) },
                    label = { Text("NAVER_BLOG_ID (예: my_auction_blog)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = draft.naverCategoryName,
                        onValueChange = { draft = draft.copy(naverCategoryName = it) },
                        label = { Text("네이버 카테고리") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = draft.naverId,
                        onValueChange = { draft = draft.copy(naverId = it) },
                        label = { Text("NAVER_ID (마스킹)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = draft.naverWorkerRelayEndpoint,
                    onValueChange = { draft = draft.copy(naverWorkerRelayEndpoint = it) },
                    label = { Text("네이버 워커 릴레이 URL (선택)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("② 티스토리 등록 활성화", style = MaterialTheme.typography.labelLarge, color = TistoryAmber)
                    Switch(
                        checked = draft.publishTargetTistory,
                        onCheckedChange = { draft = draft.copy(publishTargetTistory = it) }
                    )
                }
                OutlinedTextField(
                    value = draft.tistoryBlogUrl,
                    onValueChange = { draft = draft.copy(tistoryBlogUrl = it) },
                    label = { Text("TISTORY_BLOG_URL (예: https://myblog.tistory.com)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = draft.tistoryCategoryName,
                        onValueChange = { draft = draft.copy(tistoryCategoryName = it) },
                        label = { Text("티스토리 카테고리") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = draft.tistoryId,
                        onValueChange = { draft = draft.copy(tistoryId = it) },
                        label = { Text("TISTORY_ID (카카오)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("③ Cloudflare 자체웹(aution.codeb.app) 등록 활성화", style = MaterialTheme.typography.labelLarge, color = CloudflareSky)
                    Switch(
                        checked = draft.publishTargetCloudflare,
                        onCheckedChange = { draft = draft.copy(publishTargetCloudflare = it) }
                    )
                }
                OutlinedTextField(
                    value = draft.cloudflareSiteBase,
                    onValueChange = { draft = draft.copy(cloudflareSiteBase = it) },
                    label = { Text("CLOUDFLARE_SITE_BASE (블로그 도메인)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = draft.cloudflareBlogPath,
                    onValueChange = { draft = draft.copy(cloudflareBlogPath = it) },
                    label = { Text("Cloudflare 블로그 경로 (기본 /blog)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onSaveConfig(draft) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("3채널 등록처 및 포털 설정 저장")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = draft.llmApiEndpoint,
                    onValueChange = { draft = draft.copy(llmApiEndpoint = it) },
                    label = { Text("LLM Endpoint (선택: OpenRouter/Worker AI)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = draft.llmApiKey,
                    onValueChange = { draft = draft.copy(llmApiKey = it) },
                    label = { Text("LLM_API_KEY (${ServerConfigStore.maskSecret(draft.llmApiKey)})") },
                    visualTransformation = pwTransformation,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Server Code & Config Exporter (wrangler.toml, Worker TS, D1 SQL, selectors.json, .env.blog)
        item {
            StudioCard {
                Text("5. Cloudflare 서버 자동 배포 번들 & .env 내보내기", style = MaterialTheme.typography.titleSmall, color = CloudflareSky)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "WRANGLER" to "wrangler.toml",
                        "WORKER_TS" to "worker.ts (웹+크론)",
                        "D1_SQL" to "0010_d1.sql",
                        "SELECTORS" to "selectors.json",
                        "ENV" to ".env.blog"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = exportTab == key,
                            onClick = { exportTab = key },
                            label = { Text(label) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                val codeText = when (exportTab) {
                    "WRANGLER" -> BoilerplateAndTemplateEngine.generateWranglerToml(draft)
                    "WORKER_TS" -> BoilerplateAndTemplateEngine.generateWorkerScheduledTs(draft)
                    "D1_SQL" -> BoilerplateAndTemplateEngine.generateD1MigrationSql()
                    "SELECTORS" -> BoilerplateAndTemplateEngine.generateSelectorsJson()
                    else -> BoilerplateAndTemplateEngine.generateEnvBlogMasked(draft, masked = !showSecrets)
                }
                Surface(
                    color = NavyBackground,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SelectionContainer {
                        Text(
                            text = codeText,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFamily),
                            color = TextPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                if (!bundleCopyMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = bundleCopyMessage!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = MintSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val res = SafeClipboardHelper.copyPlainTextSafely(
                            context = context,
                            label = "서버 설정(${exportTab})",
                            text = codeText,
                            composeClipboard = clipboard
                        )
                        bundleCopyMessage = res.message
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_copy_server_bundle")
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("선택한 서버 설정 코드 복사")
                }
            }
        }
    }
}
