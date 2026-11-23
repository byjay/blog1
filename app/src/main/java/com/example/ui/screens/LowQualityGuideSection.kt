package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.SeoGuideEngine
import com.example.model.LowQualityPolicyGuide
import com.example.model.SeoChecklistItem
import com.example.model.SeoRuleCategory
import com.example.model.SeoRulePriority
import com.example.ui.StudioUiState
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.CloudflareContainer
import com.example.ui.theme.CloudflareSky
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyContainerHighest
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.NaverGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TistoryAmber

@OptIn(ExperimentalLayoutApi::class)
fun LazyListScope.lowQualityGuideAndChecklistItems(
    uiState: StudioUiState,
    selectedCategory: SeoRuleCategory?,
    expandedGuideIds: Set<String>,
    onSelectCategory: (SeoRuleCategory?) -> Unit,
    onToggleGuideExpanded: (String) -> Unit,
    onToggleChecklistItem: (String) -> Unit,
    onRequestCreateItem: () -> Unit,
    onRequestEditItem: (SeoChecklistItem) -> Unit,
    onDeleteItem: (String) -> Unit,
    onResetChecklist: () -> Unit
) {
    val totalCount = uiState.seoChecklist.size
    val checkedCount = uiState.seoChecklist.count { it.isChecked }
    val uncheckedCritical = uiState.seoChecklist.count {
        !it.isChecked && it.priority == SeoRulePriority.CRITICAL
    }
    val ratio = if (totalCount == 0) 0f else (checkedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)

    // Summary & Index Health Guard Card
    item {
        StudioCard(
            borderColor = if (uncheckedCritical == 0) EmeraldPrimary.copy(alpha = 0.55f) else CrimsonAlert.copy(alpha = 0.7f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = "저품질 방어 지수",
                        tint = if (uncheckedCritical == 0) EmeraldPrimary else TistoryAmber
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "블로그 지수 유지 · 저품질 방어 현황",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = if (uncheckedCritical == 0) {
                                "필수 저품질 차단 수칙 전 항목 점검 완료"
                            } else {
                                "필수 차단 미점검 항목 ${uncheckedCritical}건 확인 필요"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uncheckedCritical == 0) MintSecondary else CrimsonAlert
                        )
                    }
                }
                IconButton(
                    onClick = onResetChecklist,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_reset_seo_checklist")
                ) {
                    Icon(
                        imageVector = Icons.Filled.RestartAlt,
                        contentDescription = "기본 SEO 체크리스트 초기화",
                        tint = CloudflareSky
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "최신 검색엔진 SEO 체크리스트 준수율",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "$checkedCount / $totalCount (${(ratio * 100).toInt()}%)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MintSecondary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (uncheckedCritical == 0) EmeraldPrimary else TistoryAmber,
                trackColor = NavyContainerHighest
            )

            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusChip(
                    text = if (!uiState.config.enableExternalLinkNaver) "네이버 외부URL 차단 ON" else "네이버 외부링크 1회 허용",
                    textColor = if (!uiState.config.enableExternalLinkNaver) MintSecondary else TistoryAmber,
                    backgroundColor = if (!uiState.config.enableExternalLinkNaver) EmeraldContainer else AmberContainer
                )
                StatusChip(
                    text = "일일 한도 ${uiState.config.dailyPostLimit}건",
                    textColor = CloudflareSky,
                    backgroundColor = CloudflareContainer
                )
                StatusChip(
                    text = "Cron ${uiState.config.cronExpression}",
                    textColor = TextSecondary,
                    backgroundColor = NavySurfaceElevated
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRequestCreateItem,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_add_seo_checklist_item"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = NavyBackground
                )
            ) {
                Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("새 SEO 체크리스트 항목 추가")
            }
        }
    }

    // Policy Guides Header
    item {
        SectionHeader(
            title = "검색엔진별 저품질 차단 · 지수 유지 정책 가이드",
            subtitle = "네이버 C-Rank·D.I.A.+ / 티스토리·다음 / 구글 E-E-A-T·GEO / 안티-봇 수칙"
        )
    }

    val guides = SeoGuideEngine.policyGuides()
    items(guides, key = { it.id }) { guide ->
        PolicyGuideAccordionCard(
            guide = guide,
            expanded = guide.id in expandedGuideIds,
            onToggleExpand = { onToggleGuideExpanded(guide.id) }
        )
    }

    // Interactive SEO Checklist Section Header & Category Filter
    item {
        SectionHeader(
            title = "최신 SEO · 저품질 방지 체크리스트 관리 (${checkedCount}/${totalCount})",
            subtitle = "항목 체크 상태와 커스텀 규칙은 기기 내 DataStore에 자동 저장됩니다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onSelectCategory(null) },
                label = { Text("전체 (${uiState.seoChecklist.size})") },
                modifier = Modifier.testTag("filter_seo_all")
            )
            SeoRuleCategory.entries.forEach { cat ->
                val count = uiState.seoChecklist.count { it.category == cat }
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { onSelectCategory(cat) },
                    label = { Text("${cat.label} ($count)") },
                    modifier = Modifier.testTag("filter_seo_${cat.name}")
                )
            }
        }
    }

    val filteredItems = if (selectedCategory == null) {
        uiState.seoChecklist
    } else {
        uiState.seoChecklist.filter { it.category == selectedCategory }
    }

    items(filteredItems, key = { it.id }) { item ->
        val liveNote = SeoGuideEngine.evaluateLiveAutoNote(item, uiState.config, uiState.slots)
        SeoChecklistItemCard(
            item = item,
            liveAutoNote = liveNote,
            onToggle = { onToggleChecklistItem(item.id) },
            onEdit = { onRequestEditItem(item) },
            onDelete = { onDeleteItem(item.id) }
        )
    }
}

@Composable
private fun PolicyGuideAccordionCard(
    guide: LowQualityPolicyGuide,
    expanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val accent = seoCategoryColor(guide.category)
    StudioCard(
        borderColor = accent.copy(alpha = 0.45f),
        modifier = Modifier
            .clickable(onClick = onToggleExpand)
            .testTag("policy_guide_card_${guide.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                StatusChip(
                    text = guide.category.label,
                    textColor = accent,
                    backgroundColor = NavySurfaceElevated
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = guide.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Text(
                    text = guide.engineFocus,
                    style = MaterialTheme.typography.labelSmall,
                    color = accent
                )
            }
            IconButton(
                onClick = onToggleExpand,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "가이드 접기" else "가이드 펼치기",
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = guide.summary,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = EmeraldContainer.copy(alpha = 0.35f),
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("지수 유지 핵심 정책", style = MaterialTheme.typography.labelLarge, color = MintSecondary)
                        }
                        guide.keyPolicies.forEach { policy ->
                            Text("• $policy", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                    }
                }

                Surface(
                    color = CrimsonContainer.copy(alpha = 0.35f),
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, CrimsonAlert.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("저품질·누락 유발 주의 트리거", style = MaterialTheme.typography.labelLarge, color = CrimsonAlert)
                        }
                        guide.penaltyTriggers.forEach { trigger ->
                            Text("• $trigger", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeoChecklistItemCard(
    item: SeoChecklistItem,
    liveAutoNote: String?,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = if (item.priority == SeoRulePriority.CRITICAL) CrimsonAlert else CloudflareSky
    val priorityBg = if (item.priority == SeoRulePriority.CRITICAL) CrimsonContainer else CloudflareContainer

    StudioCard(
        borderColor = if (item.isChecked) EmeraldPrimary.copy(alpha = 0.4f) else priorityColor.copy(alpha = 0.6f),
        containerColor = if (item.isChecked) NavySurface else NavySurfaceElevated,
        modifier = Modifier
            .clickable(onClick = onToggle)
            .testTag("seo_checklist_card_${item.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = EmeraldPrimary,
                    checkmarkColor = NavyBackground,
                    uncheckedColor = TextSecondary
                ),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("checkbox_seo_${item.id}")
            )
            Column(modifier = Modifier.weight(1f)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StatusChip(
                        text = item.category.label,
                        textColor = seoCategoryColor(item.category),
                        backgroundColor = NavyBackground
                    )
                    StatusChip(
                        text = item.priority.label,
                        textColor = priorityColor,
                        backgroundColor = priorityBg
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.isChecked) TextPrimary else TistoryAmber
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (!liveAutoNote.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MintSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = liveAutoNote,
                            style = MaterialTheme.typography.labelSmall,
                            color = MintSecondary
                        )
                    }
                }
            }
            Column {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_edit_seo_${item.id}")
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "체크리스트 수정", tint = CloudflareSky)
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_delete_seo_${item.id}")
                ) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "체크리스트 삭제", tint = CrimsonAlert)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeoChecklistEditorDialog(
    initialItem: SeoChecklistItem?,
    defaultCategory: SeoRuleCategory,
    onDismiss: () -> Unit,
    onCreate: (title: String, description: String, category: SeoRuleCategory, priority: SeoRulePriority) -> Unit,
    onSaveExisting: (SeoChecklistItem) -> Unit
) {
    var title by remember(initialItem) { mutableStateOf(initialItem?.title ?: "") }
    var description by remember(initialItem) { mutableStateOf(initialItem?.description ?: "") }
    var category by remember(initialItem) { mutableStateOf(initialItem?.category ?: defaultCategory) }
    var priority by remember(initialItem) { mutableStateOf(initialItem?.priority ?: SeoRulePriority.CRITICAL) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = {
            Text(
                text = if (initialItem == null) "새 SEO 체크리스트 추가" else "SEO 체크리스트 편집",
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMsg = null
                    },
                    label = { Text("규칙 제목") },
                    placeholder = { Text("예: 네이버 스마트블록 체류시간용 소제목 5개 유지") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_seo_title")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        errorMsg = null
                    },
                    label = { Text("상세 기준 및 점검 내용") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_seo_desc")
                )
                Text("검색엔진/정책 분류", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SeoRuleCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.label, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
                Text("중요도", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SeoRulePriority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.label, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
                if (!errorMsg.isNullOrBlank()) {
                    Text(errorMsg!!, style = MaterialTheme.typography.bodySmall, color = CrimsonAlert)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        title.isBlank() -> errorMsg = "규칙 제목을 입력해 주세요."
                        description.isBlank() -> errorMsg = "상세 점검 내용을 입력해 주세요."
                        else -> {
                            if (initialItem == null) {
                                onCreate(title, description, category, priority)
                            } else {
                                onSaveExisting(
                                    initialItem.copy(
                                        title = title,
                                        description = description,
                                        category = category,
                                        priority = priority
                                    )
                                )
                            }
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.testTag("btn_save_seo_item"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = NavyBackground)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = TextSecondary)
            }
        }
    )
}

private fun seoCategoryColor(category: SeoRuleCategory): Color = when (category) {
    SeoRuleCategory.NAVER -> NaverGreen
    SeoRuleCategory.TISTORY -> TistoryAmber
    SeoRuleCategory.GOOGLE_GEO -> CloudflareSky
    SeoRuleCategory.ANTI_BOT -> MintSecondary
    SeoRuleCategory.CUSTOM -> TextPrimary
}
