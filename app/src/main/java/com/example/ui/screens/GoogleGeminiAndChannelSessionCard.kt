package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.ai.GoogleAccountAuthHelper
import com.example.model.GoogleGeminiAccountState
import com.example.model.ServerAutomationConfig
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoogleGeminiAndGrokPriorityCard(
    googleAccount: GoogleGeminiAccountState,
    config: ServerAutomationConfig,
    lastAiEngineUsed: String,
    isBusy: Boolean,
    onSyncGoogleAuthState: (String?) -> Unit,
    onSignOutGoogle: () -> Unit,
    onUpdateGrokConfig: (Boolean, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var grokEnabled by remember(config.grokFallbackEnabled) { mutableStateOf(config.grokFallbackEnabled) }
    var grokModel by remember(config.grokModelName) { mutableStateOf(config.grokModelName) }
    var isSigningIn by remember { mutableStateOf(false) }

    val grokKeyConfigured = BuildConfig.GROK_API_KEY.isNotBlank() &&
        !BuildConfig.GROK_API_KEY.contains("DEFAULT_VALUE", ignoreCase = true)

    StudioCard(
        borderColor = CloudflareSky.copy(alpha = 0.6f),
        modifier = modifier.testTag("card_ai_priority_google_grok")
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
                Icon(Icons.Filled.AutoAwesome, contentDescription = "AI 우선순위 엔진", tint = CloudflareSky)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("AI 엔진 우선순위: 1순위 Google Gemini → 2순위 Grok", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Text(
                        text = "안티그라비티가 아닌 본인 Google 계정 기반 Gemini 사용량을 최우선 호출하고 장애 시 Grok이 자동 보조합니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            StatusChip(
                text = if (googleAccount.isSignedIn) "Google 연결됨" else "로그인 대기",
                textColor = if (googleAccount.isSignedIn) MintSecondary else TistoryAmber,
                backgroundColor = if (googleAccount.isSignedIn) EmeraldContainer else AmberContainer
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Priority 1: Google Account + Gemini
        Surface(
            color = NavyBackground,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, if (googleAccount.isSignedIn) EmeraldPrimary else NavyBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = if (googleAccount.isSignedIn) Icons.Filled.CheckCircle else Icons.Filled.AccountCircle,
                        contentDescription = null,
                        tint = if (googleAccount.isSignedIn) MintSecondary else CloudflareSky,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = googleAccount.activePrimaryEngineLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (googleAccount.isSignedIn) MintSecondary else TextPrimary
                    )
                }
                if (googleAccount.email.isNotBlank()) {
                    Text(
                        text = "연결 계정: ${googleAccount.displayName.ifBlank { "Google 사용자" }} (${googleAccount.email})",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFamily),
                        color = CloudflareSky
                    )
                }
                Text(
                    text = googleAccount.lastSignInStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "최근 실제 실행 엔진: $lastAiEngineUsed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MintSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (!isSigningIn) {
                                isSigningIn = true
                                scope.launch {
                                    val result = GoogleAccountAuthHelper.signInWithGoogleInteractive(context)
                                    isSigningIn = false
                                    result.onSuccess { user ->
                                        onSyncGoogleAuthState("Google 계정(${user.email ?: user.uid}) 로그인 완료 · Gemini 1순위 활성")
                                    }.onFailure { err ->
                                        onSyncGoogleAuthState("Google 로그인 안내: ${err.localizedMessage ?: "취소 또는 OAuth 설정 필요"}")
                                    }
                                }
                            }
                        },
                        enabled = !isBusy && !isSigningIn,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("btn_google_signin_gemini"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CloudflareContainer,
                            contentColor = CloudflareSky
                        )
                    ) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isSigningIn -> "Google 인증 중..."
                                googleAccount.isSignedIn -> "Google 계정 전환"
                                else -> "Google 계정 로그인 (Gemini 우선)"
                            },
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                    if (googleAccount.isSignedIn) {
                        OutlinedButton(
                            onClick = onSignOutGoogle,
                            modifier = Modifier
                                .weight(0.75f)
                                .height(48.dp)
                                .testTag("btn_google_signout")
                        ) {
                            Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("로그아웃", maxLines = 1)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Priority 2: Grok API Backup
        Surface(
            color = NavySurfaceElevated,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, NavyBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("2순위: Grok API 보조 자동 전환", style = MaterialTheme.typography.labelLarge, color = TistoryAmber)
                        Text(
                            text = if (grokKeyConfigured) {
                                "GROK_API_KEY 시크릿 감지됨 · Gemini 오류/할당 초과 시 자동 보조"
                            } else {
                                "GROK_API_KEY 미설정 — AI Studio Secrets 패널에 등록 시 즉시 활성화"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (grokKeyConfigured) MintSecondary else TextSecondary
                        )
                    }
                    Switch(
                        checked = grokEnabled,
                        onCheckedChange = { checked ->
                            grokEnabled = checked
                            onUpdateGrokConfig(checked, grokModel)
                        },
                        modifier = Modifier.testTag("switch_grok_fallback")
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("grok-3-mini", "grok-3-latest", "grok-2-latest").forEach { modelOpt ->
                        FilterChip(
                            selected = grokModel == modelOpt,
                            onClick = {
                                grokModel = modelOpt
                                onUpdateGrokConfig(grokEnabled, modelOpt)
                            },
                            label = { Text(modelOpt, style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickSettingsAndAlwaysLoginCard(
    config: ServerAutomationConfig,
    isBusy: Boolean,
    onSaveQuickSettings: (
        cloudflareSiteBase: String,
        cloudflareBlogPath: String,
        naverKeepLoggedIn: Boolean,
        tistoryKeepLoggedIn: Boolean,
        tistoryAutoDirectPublish: Boolean,
        grokFallbackEnabled: Boolean,
        grokModelName: String
    ) -> Unit,
    onRunHeartbeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var siteBase by remember(config.cloudflareSiteBase) { mutableStateOf(config.cloudflareSiteBase) }
    var blogPath by remember(config.cloudflareBlogPath) { mutableStateOf(config.cloudflareBlogPath) }
    var naverKeep by remember(config.naverKeepLoggedIn) { mutableStateOf(config.naverKeepLoggedIn) }
    var tistoryKeep by remember(config.tistoryKeepLoggedIn) { mutableStateOf(config.tistoryKeepLoggedIn) }
    var tistoryDirect by remember(config.tistoryAutoDirectPublish) { mutableStateOf(config.tistoryAutoDirectPublish) }

    LaunchedEffect(config) {
        siteBase = config.cloudflareSiteBase
        blogPath = config.cloudflareBlogPath
        naverKeep = config.naverKeepLoggedIn
        tistoryKeep = config.tistoryKeepLoggedIn
        tistoryDirect = config.tistoryAutoDirectPublish
    }

    StudioCard(
        borderColor = EmeraldPrimary.copy(alpha = 0.6f),
        modifier = modifier.testTag("card_quick_settings_session")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Settings, contentDescription = "환경설정 빠른 관리", tint = MintSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("환경설정 빠른 관리 (상시 로그인 · CF 도메인)", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Text(
                        text = "상단 톱니바퀴(⚙️) 또는 하단 '설정·보안' 탭에서 언제든 바로 열 수 있습니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Cloudflare Blog Site Domain (aution.codeb.app)
        Surface(
            color = NavyBackground,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, CloudflareSky.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.CloudQueue, contentDescription = null, tint = CloudflareSky, modifier = Modifier.size(16.dp))
                    Text("Cloudflare 자체 블로그 발행 도메인 (데이터 API와 분리)", style = MaterialTheme.typography.labelLarge, color = CloudflareSky)
                }
                Text(
                    text = "실제 블로그 발행: ${siteBase.trim().removeSuffix("/")}${blogPath.trim()} · 딱찰 데이터 API: ${config.ddakchalApiBase}",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                    color = MintSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = siteBase,
                        onValueChange = { siteBase = it },
                        label = { Text("CF 블로그 도메인") },
                        singleLine = true,
                        modifier = Modifier.weight(1.5f).testTag("input_quick_cf_site_base")
                    )
                    OutlinedTextField(
                        value = blogPath,
                        onValueChange = { blogPath = it },
                        label = { Text("경로") },
                        singleLine = true,
                        modifier = Modifier.weight(0.75f).testTag("input_quick_cf_blog_path")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Persistent Naver & Tistory Always-Logged-In + Direct Publish
        Surface(
            color = NavySurfaceElevated,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, NaverGreen.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("네이버 항상 로그인 상태 유지 (자동 등록)", style = MaterialTheme.typography.labelLarge, color = NaverGreen)
                        Text("비번 저장 없이 브라우저·워커 세션 쿠키로 로그인 상태를 지속합니다.", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Switch(
                        checked = naverKeep,
                        onCheckedChange = { naverKeep = it },
                        modifier = Modifier.testTag("switch_naver_keep_login")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("티스토리 항상 로그인 상태 유지", style = MaterialTheme.typography.labelLarge, color = TistoryAmber)
                        Text("카카오·티스토리 세션을 유지해 발행 시 재로그인 없이 연결합니다.", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Switch(
                        checked = tistoryKeep,
                        onCheckedChange = { tistoryKeep = it },
                        modifier = Modifier.testTag("switch_tistory_keep_login")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("티스토리 즉시 바로 자동 등록", style = MaterialTheme.typography.labelLarge, color = MintSecondary)
                        Text("검수 95점+ 통과 즉시 티스토리 릴레이/발행 큐로 바로 송신합니다.", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Switch(
                        checked = tistoryDirect,
                        onCheckedChange = { tistoryDirect = it },
                        modifier = Modifier.testTag("switch_tistory_auto_direct")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://nid.naver.com/nidlogin.login")))
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("네이버 로그인창", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tistory.com/auth/login")))
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("티스토리 로그인창", maxLines = 1)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    onSaveQuickSettings(
                        siteBase,
                        blogPath,
                        naverKeep,
                        tistoryKeep,
                        tistoryDirect,
                        config.grokFallbackEnabled,
                        config.grokModelName
                    )
                },
                enabled = !isBusy,
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
                    .testTag("btn_save_quick_settings"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = NavyBackground
                )
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("환경설정 즉시 저장", fontWeight = FontWeight.Bold, maxLines = 1)
            }
            OutlinedButton(
                onClick = onRunHeartbeat,
                enabled = !isBusy,
                modifier = Modifier
                    .weight(0.95f)
                    .height(48.dp)
                    .testTag("btn_always_login_heartbeat")
            ) {
                Icon(Icons.Filled.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("상시 연결 점검", maxLines = 1)
            }
        }
    }
}
