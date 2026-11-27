package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.engine.BoilerplateAndTemplateEngine
import com.example.model.BlogPostTemplate
import com.example.model.ChannelPlatform
import com.example.model.ProFunnelConfig
import com.example.model.PropertyTemplateYaml
import com.example.model.PropertyType
import com.example.model.SeoChecklistItem
import com.example.model.SeoRuleCategory
import com.example.model.SeoRulePriority
import com.example.ui.StudioUiState
import com.example.ui.theme.CloudflareSky
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TemplatesAndRulesScreen(
    uiState: StudioUiState,
    onSaveYaml: (PropertyType, String) -> Unit,
    onRotateBoilerplate: () -> Unit,
    onCreatePostTemplate: (
        name: String,
        propertyType: PropertyType,
        targetChannel: ChannelPlatform,
        titleTemplate: String,
        bodyTemplate: String,
        tagsTemplate: String,
        activateForGeneration: Boolean
    ) -> Unit,
    onSavePostTemplate: (BlogPostTemplate) -> Unit,
    onActivatePostTemplate: (String) -> Unit,
    onDeletePostTemplate: (String) -> Unit,
    onResetPostTemplates: () -> Unit,
    onToggleSeoChecklistItem: (String) -> Unit,
    onCreateSeoChecklistItem: (String, String, SeoRuleCategory, SeoRulePriority) -> Unit,
    onSaveSeoChecklistItem: (SeoChecklistItem) -> Unit,
    onDeleteSeoChecklistItem: (String) -> Unit,
    onResetSeoChecklist: () -> Unit,
    onSaveProFunnel: (ProFunnelConfig) -> Unit,
    onResetProFunnel: () -> Unit,
    onCrawlAndSync: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    var editingYaml by remember { mutableStateOf<PropertyTemplateYaml?>(null) }
    var activeSubTab by rememberSaveable { mutableStateOf("POST_TEMPLATES") } // POST_TEMPLATES | LOW_QUALITY_GUIDE | MARKETING_RESEARCH | YAML_AND_RULES
    var selectedPropertyFilter by remember { mutableStateOf<PropertyType?>(null) }
    var showPostEditorDialog by remember { mutableStateOf(false) }
    var editingPostTemplate by remember { mutableStateOf<BlogPostTemplate?>(null) }
    var duplicateEditorMode by remember { mutableStateOf(false) }

    var selectedSeoCategory by remember { mutableStateOf<SeoRuleCategory?>(null) }
    var expandedGuideIds by remember { mutableStateOf(setOf("guide-naver-crank-dia")) }
    var showSeoEditorDialog by remember { mutableStateOf(false) }
    var editingSeoItem by remember { mutableStateOf<SeoChecklistItem?>(null) }

    val introSample = BoilerplateAndTemplateEngine.introHookPool[
        uiState.poolPreviewIndex % BoilerplateAndTemplateEngine.introHookPool.size
    ]
    val transSample = BoilerplateAndTemplateEngine.transitionPhrasePool[
        uiState.poolPreviewIndex % BoilerplateAndTemplateEngine.transitionPhrasePool.size
    ]
    val ctaSample = BoilerplateAndTemplateEngine.ctaClosingPool[
        uiState.poolPreviewIndex % BoilerplateAndTemplateEngine.ctaClosingPool.size
    ]
    val seoCheckedCount = uiState.seoChecklist.count { it.isChecked }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "물건별 템플릿 · 저품질 방지 가이드 스튜디오",
                subtitle = "DataStore 로컬 템플릿 · 검색엔진 저품질 차단 가이드 및 SEO 체크리스트"
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeSubTab == "POST_TEMPLATES",
                    onClick = { activeSubTab = "POST_TEMPLATES" },
                    label = { Text("게시글 템플릿 (${uiState.postTemplates.size})") },
                    modifier = Modifier.testTag("subtab_post_templates")
                )
                FilterChip(
                    selected = activeSubTab == "LOW_QUALITY_GUIDE",
                    onClick = { activeSubTab = "LOW_QUALITY_GUIDE" },
                    label = { Text("저품질 방지 가이드 ($seoCheckedCount/${uiState.seoChecklist.size})") },
                    modifier = Modifier.testTag("subtab_low_quality_guide")
                )
                FilterChip(
                    selected = activeSubTab == "MARKETING_RESEARCH",
                    onClick = { activeSubTab = "MARKETING_RESEARCH" },
                    label = { Text("마케팅·수익 리서치") },
                    modifier = Modifier.testTag("subtab_marketing_research")
                )
                FilterChip(
                    selected = activeSubTab == "YAML_AND_RULES",
                    onClick = { activeSubTab = "YAML_AND_RULES" },
                    label = { Text("YAML·규칙·로그") },
                    modifier = Modifier.testTag("subtab_yaml_rules")
                )
            }
        }

        if (activeSubTab == "POST_TEMPLATES") {
            postTemplateManagerItems(
                templates = uiState.postTemplates,
                selectedFilter = selectedPropertyFilter,
                onSelectFilter = { selectedPropertyFilter = it },
                onRequestCreate = {
                    editingPostTemplate = null
                    duplicateEditorMode = false
                    showPostEditorDialog = true
                },
                onRequestEdit = { tpl ->
                    editingPostTemplate = tpl
                    duplicateEditorMode = false
                    showPostEditorDialog = true
                },
                onRequestDuplicate = { tpl ->
                    editingPostTemplate = tpl
                    duplicateEditorMode = true
                    showPostEditorDialog = true
                },
                onActivateTemplate = onActivatePostTemplate,
                onDeleteTemplate = onDeletePostTemplate,
                onResetDefaults = onResetPostTemplates,
                onCrawlAndSync = onCrawlAndSync,
                onOpenLowQualityGuide = { activeSubTab = "LOW_QUALITY_GUIDE" },
                isBusy = uiState.isBusy,
                ddakchalConnected = uiState.telemetry.ddakchalConnected,
                apiLatencyMs = uiState.telemetry.apiLatencyMs
            )
        } else if (activeSubTab == "LOW_QUALITY_GUIDE") {
            lowQualityGuideAndChecklistItems(
                uiState = uiState,
                selectedCategory = selectedSeoCategory,
                expandedGuideIds = expandedGuideIds,
                onSelectCategory = { selectedSeoCategory = it },
                onToggleGuideExpanded = { id ->
                    expandedGuideIds = if (id in expandedGuideIds) {
                        expandedGuideIds - id
                    } else {
                        expandedGuideIds + id
                    }
                },
                onToggleChecklistItem = onToggleSeoChecklistItem,
                onRequestCreateItem = {
                    editingSeoItem = null
                    showSeoEditorDialog = true
                },
                onRequestEditItem = { item ->
                    editingSeoItem = item
                    showSeoEditorDialog = true
                },
                onDeleteItem = onDeleteSeoChecklistItem,
                onResetChecklist = onResetSeoChecklist
            )
        } else if (activeSubTab == "MARKETING_RESEARCH") {
            item {
                ProFunnelSettingsCard(
                    funnel = uiState.proFunnel,
                    onSaveFunnel = onSaveProFunnel,
                    onResetFunnel = onResetProFunnel
                )
            }
            item {
                NaverMarketingResearchAndSimulatorSection()
            }
        } else {
        // 5 Property Type YAML Cards
        items(uiState.templates, key = { it.propertyType.code }) { tpl ->
            StudioCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PropertyTypeBadge(propertyType = tpl.propertyType)
                    Text(
                        text = tpl.propertyType.yamlFileName,
                        style = MaterialTheme.typography.labelSmall,
                        color = CloudflareSky
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "필수 강조: ${tpl.propertyType.focusSummary}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SEO 패턴: ${tpl.propertyType.seoKeywordPattern}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MintSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = NavyBackground,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tpl.yamlContent,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFamily),
                        color = TextSecondary,
                        maxLines = 8,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { clipboard.setText(AnnotatedString(tpl.yamlContent)) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("YAML 복사")
                    }
                    Button(
                        onClick = { editingYaml = tpl },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_edit_yaml_${tpl.propertyType.code}"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("템플릿 편집")
                    }
                }
            }
        }

        // 15+ Dynamic Boilerplate Pool Card
        item {
            StudioCard(borderColor = CloudflareSky.copy(alpha = 0.45f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "15+ 다이나믹 문구 풀 (고정 멘트 반복 차단)",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = "인사말 ${BoilerplateAndTemplateEngine.introHookPool.size}종 · CTA ${BoilerplateAndTemplateEngine.ctaClosingPool.size}종 순환 샘플",
                            style = MaterialTheme.typography.bodySmall,
                            color = CloudflareSky
                        )
                    }
                    OutlinedButton(
                        onClick = onRotateBoilerplate,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_rotate_pool")
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("조합 전환")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                PoolSampleRow("도입부 #${uiState.poolPreviewIndex + 1}", introSample)
                Spacer(modifier = Modifier.height(6.dp))
                PoolSampleRow("전환구", transSample)
                Spacer(modifier = Modifier.height(6.dp))
                PoolSampleRow("마무리 CTA #${uiState.poolPreviewIndex + 1}", ctaSample)
            }
        }

        // 5 Anti-Low-Quality Rules Reference
        item {
            StudioCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Shield, contentDescription = null, tint = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("검색엔진 저품질 원천 차단 5대 강제 수칙", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                val rules = listOf(
                    "1. 외부 링크 회피: 네이버 본문 직접 URL 금지(하단 배너+프로필 유도) / 티스토리 rel=\"nofollow sponsored\"",
                    "2. 발행 주기 랜덤화: 정각(:00) 금지 + 지정시간대 랜덤 지터 + 일일 한도(DAILY_POST_LIMIT=${uiState.config.dailyPostLimit})",
                    "3. 고정 문구 금지: 15종 이상 인사말·전환·CTA 풀 매 발행 시 문맥 변형",
                    "4. 이미지 중복 방지: 채널별 MD5/SHA-256 해시·EXIF 삭제·4~7장 제한 + 텍스트 1,200~2,500자",
                    "5. 유사도 게이트: 3채널 및 최근 7일 글 대비 Cosine/5-gram 30% 이상 시 자동 폐기 후 재생성"
                )
                rules.forEach { rule ->
                    Text(
                        text = rule,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { activeSubTab = "LOW_QUALITY_GUIDE" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_open_low_quality_guide")
                ) {
                    Icon(Icons.Filled.Shield, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("저품질 방지 가이드 · SEO 체크리스트 열기")
                }
            }
        }

        // Publish Logs Audit Feed
        item {
            SectionHeader(
                title = "발행·세션 감사 로그 (publish_logs)",
                subtitle = "3채널 발행 URL 및 CAPTCHA handOff 이력"
            )
        }
        items(uiState.publishLogs, key = { it.logId }) { log ->
            StudioCard(containerColor = NavySurfaceElevated) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${log.caseId} · ${log.platform.displayName}",
                        style = MaterialTheme.typography.titleSmall,
                        color = channelAccentColor(log.platform)
                    )
                    PostStatusBadge(status = log.status)
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (!log.publishedUrl.isNullOrBlank()) {
                    Text(log.publishedUrl, style = MaterialTheme.typography.labelSmall, color = MintSecondary)
                }
                if (!log.errorMessage.isNullOrBlank()) {
                    Text(log.errorMessage, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                }
                Text(
                    text = "${log.publishedAt} · retry=${log.retryCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
        }
    }

    if (showPostEditorDialog) {
        PostTemplateEditorDialog(
            initialTemplate = editingPostTemplate,
            isDuplicateMode = duplicateEditorMode,
            defaultPropertyType = selectedPropertyFilter ?: PropertyType.APT_OFFICETEL,
            onDismiss = { showPostEditorDialog = false },
            onSaveNew = onCreatePostTemplate,
            onSaveExisting = onSavePostTemplate
        )
    }

    if (showSeoEditorDialog) {
        SeoChecklistEditorDialog(
            initialItem = editingSeoItem,
            defaultCategory = selectedSeoCategory ?: SeoRuleCategory.NAVER,
            onDismiss = { showSeoEditorDialog = false },
            onCreate = onCreateSeoChecklistItem,
            onSaveExisting = onSaveSeoChecklistItem
        )
    }

    editingYaml?.let { tpl ->
        var yamlDraft by remember(tpl.propertyType) { mutableStateOf(tpl.yamlContent) }
        AlertDialog(
            onDismissRequest = { editingYaml = null },
            containerColor = NavySurfaceElevated,
            title = { Text("${tpl.propertyType.yamlFileName} 편집", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = yamlDraft,
                    onValueChange = { yamlDraft = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFamily)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveYaml(tpl.propertyType, yamlDraft)
                        editingYaml = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
                ) {
                    Text("저장")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingYaml = null }) {
                    Text("취소", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun PoolSampleRow(label: String, text: String) {
    Surface(
        color = NavyBackground,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, NavyBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MintSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
        }
    }
}
