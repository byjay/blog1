package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.engine.BlogPostTemplateEngine
import com.example.model.BlogPostTemplate
import com.example.model.ChannelPlatform
import com.example.model.PropertyType
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.CloudflareContainer
import com.example.ui.theme.CloudflareSky
import com.example.ui.theme.CrimsonAlert
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

@OptIn(ExperimentalLayoutApi::class)
fun LazyListScope.postTemplateManagerItems(
    templates: List<BlogPostTemplate>,
    selectedFilter: PropertyType?,
    onSelectFilter: (PropertyType?) -> Unit,
    onRequestCreate: () -> Unit,
    onRequestEdit: (BlogPostTemplate) -> Unit,
    onRequestDuplicate: (BlogPostTemplate) -> Unit,
    onActivateTemplate: (String) -> Unit,
    onDeleteTemplate: (String) -> Unit,
    onResetDefaults: () -> Unit
) {
    item {
        StudioCard(borderColor = EmeraldPrimary.copy(alpha = 0.45f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "물건별 블로그 게시글 템플릿 (DataStore 로컬 저장)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "물건 유형·채널별 제목·본문·태그 양식을 직접 만들고 즉시 수정·적용합니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                IconButton(
                    onClick = onResetDefaults,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_reset_post_templates")
                ) {
                    Icon(
                        imageVector = Icons.Filled.RestartAlt,
                        contentDescription = "기본 템플릿 5종 복원",
                        tint = CloudflareSky
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onRequestCreate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_create_post_template"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = NavyBackground
                )
            ) {
                Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("새 물건별 게시글 템플릿 생성")
            }
        }
    }

    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { onSelectFilter(null) },
                label = { Text("전체 (${templates.size})") },
                modifier = Modifier.testTag("filter_tpl_all")
            )
            PropertyType.entries.forEach { type ->
                val count = templates.count { it.propertyType == type }
                FilterChip(
                    selected = selectedFilter == type,
                    onClick = { onSelectFilter(type) },
                    label = { Text("${type.displayLabel} ($count)") },
                    modifier = Modifier.testTag("filter_tpl_${type.code}")
                )
            }
        }
    }

    val filtered = if (selectedFilter == null) {
        templates
    } else {
        templates.filter { it.propertyType == selectedFilter }
    }

    if (filtered.isEmpty()) {
        item {
            StudioCard(containerColor = NavySurfaceElevated) {
                Text(
                    text = "선택한 물건 유형에 등록된 게시글 템플릿이 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onRequestCreate,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("이 유형의 새 템플릿 만들기")
                }
            }
        }
    } else {
        items(filtered, key = { it.id }) { tpl ->
            BlogPostTemplateCard(
                template = tpl,
                onEdit = { onRequestEdit(tpl) },
                onDuplicate = { onRequestDuplicate(tpl) },
                onActivate = { onActivateTemplate(tpl.id) },
                onDelete = { onDeleteTemplate(tpl.id) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BlogPostTemplateCard(
    template: BlogPostTemplate,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onActivate: () -> Unit,
    onDelete: () -> Unit
) {
    val nonSpaceCount = remember(template.bodyTemplate) {
        template.bodyTemplate.count { !it.isWhitespace() }
    }
    val lengthOk = nonSpaceCount in 1100..2500

    StudioCard(
        borderColor = if (template.isActiveForGeneration) EmeraldPrimary.copy(alpha = 0.65f) else NavyBorder,
        modifier = Modifier.testTag("post_template_card_${template.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (template.isActiveForGeneration) {
                Spacer(modifier = Modifier.width(8.dp))
                StatusChip(
                    text = "생성 적용 중",
                    textColor = MintSecondary,
                    backgroundColor = EmeraldContainer,
                    dotColor = EmeraldPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PropertyTypeBadge(propertyType = template.propertyType)
            StatusChip(
                text = template.targetChannel.displayName,
                textColor = channelAccentColor(template.targetChannel),
                backgroundColor = NavySurfaceElevated
            )
            StatusChip(
                text = "본문 ${nonSpaceCount}자",
                textColor = if (lengthOk) CloudflareSky else TistoryAmber,
                backgroundColor = if (lengthOk) CloudflareContainer else AmberContainer
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "제목: ${template.titleTemplate}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MintSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            color = NavyBackground,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, NavyBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = template.bodyTemplate,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(10.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "태그: ${template.tagsTemplate}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "저장: ${template.updatedAt.ifBlank { "기본" }}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onEdit,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_edit_post_template_${template.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldContainer,
                    contentColor = MintSecondary
                )
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("편집", maxLines = 1)
            }
            OutlinedButton(
                onClick = onDuplicate,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("복제", maxLines = 1)
            }
            if (!template.isActiveForGeneration) {
                OutlinedButton(
                    onClick = onActivate,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_activate_post_template_${template.id}"),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("적용", color = MintSecondary, maxLines = 1)
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("btn_delete_post_template_${template.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "${template.name} 삭제",
                    tint = CrimsonAlert
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostTemplateEditorDialog(
    initialTemplate: BlogPostTemplate?,
    isDuplicateMode: Boolean,
    defaultPropertyType: PropertyType,
    onDismiss: () -> Unit,
    onSaveNew: (
        name: String,
        propertyType: PropertyType,
        targetChannel: ChannelPlatform,
        titleTemplate: String,
        bodyTemplate: String,
        tagsTemplate: String,
        activateForGeneration: Boolean
    ) -> Unit,
    onSaveExisting: (BlogPostTemplate) -> Unit
) {
    val isCreate = initialTemplate == null || isDuplicateMode
    var name by remember(initialTemplate, isDuplicateMode) {
        mutableStateOf(
            when {
                initialTemplate == null -> ""
                isDuplicateMode -> "${initialTemplate.name} (복사본)"
                else -> initialTemplate.name
            }
        )
    }
    var propertyType by remember(initialTemplate) {
        mutableStateOf(initialTemplate?.propertyType ?: defaultPropertyType)
    }
    var targetChannel by remember(initialTemplate) {
        mutableStateOf(initialTemplate?.targetChannel ?: ChannelPlatform.NAVER)
    }
    var titleTemplate by remember(initialTemplate) {
        mutableStateOf(
            initialTemplate?.titleTemplate
                ?: "{지역} {물건유형} 경매 {사건번호} 최저가 {최저가}({할인율}%) 핵심 분석"
        )
    }
    var bodyTemplate by remember(initialTemplate) {
        mutableStateOf(
            initialTemplate?.bodyTemplate ?: buildString {
                appendLine("{법원} {사건번호} ({주소}) {물건유형} 경매 브리핑입니다.")
                appendLine("감정가 {감정가}, 최저매각가 {최저가}({할인율}%), 입찰보증금 {보증금}, 말소기준권리는 {말소기준권리}입니다.")
                appendLine()
                appendLine("1. 시세 및 3개년 낙찰통계")
                appendLine("실거래 중위가 {실거래중위가} · 권역 3개년 낙찰가율 {3개년낙찰률}% 기준 안전마진을 확인하세요.")
                appendLine()
                appendLine("2. 권리분석 및 특수 점검")
                appendLine("점유: {점유현황} / 임차: {임차현황} / 인수: {인수권리}")
                appendLine("유형별 필수 점검: {특수체크}")
                appendLine()
                appendLine("3. 3전략 입찰가 가이드")
                appendLine("보수 {보수입찰가} · 적정 {적정입찰가} · 공격 {공격입찰가} (매각기일 {매각기일})")
            }.trim()
        )
    }
    var tagsTemplate by remember(initialTemplate) {
        mutableStateOf(initialTemplate?.tagsTemplate ?: "딱찰,{지역}경매,{사건번호},{물건유형},권리분석")
    }
    var activateForGeneration by remember(initialTemplate) {
        mutableStateOf(initialTemplate?.isActiveForGeneration ?: true)
    }
    var showRenderedPreview by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val sampleAuction = remember(propertyType) {
        BlogPostTemplateEngine.sampleAuctionForType(propertyType)
    }
    val renderedTitle = remember(titleTemplate, sampleAuction) {
        BlogPostTemplateEngine.renderText(titleTemplate, sampleAuction)
    }
    val renderedBody = remember(bodyTemplate, sampleAuction) {
        BlogPostTemplateEngine.renderText(bodyTemplate, sampleAuction)
    }
    val renderedCharCount = remember(renderedBody) {
        renderedBody.count { !it.isWhitespace() }
    }
    val hasRequiredFactTokens = remember(titleTemplate, bodyTemplate) {
        val combined = "$titleTemplate\n$bodyTemplate"
        listOf("{사건번호}", "{감정가}", "{최저가}", "{말소기준권리}").all { combined.contains(it) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = {
            Text(
                text = if (isCreate) "새 게시글 템플릿 생성" else "게시글 템플릿 편집",
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
                    value = name,
                    onValueChange = {
                        name = it
                        validationError = null
                    },
                    label = { Text("템플릿 이름") },
                    placeholder = { Text("예: 강남 아파트 실거주 Q&A 양식") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_template_name")
                )

                Text("물건 유형 (PropertyType)", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PropertyType.entries.forEach { type ->
                        FilterChip(
                            selected = propertyType == type,
                            onClick = { propertyType = type },
                            label = { Text(type.displayLabel, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                Text("발행 채널", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ChannelPlatform.entries.forEach { ch ->
                        FilterChip(
                            selected = targetChannel == ch,
                            onClick = { targetChannel = ch },
                            label = { Text(ch.displayName, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = titleTemplate,
                    onValueChange = {
                        titleTemplate = it
                        validationError = null
                    },
                    label = { Text("제목 템플릿") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_template_title")
                )

                Text("치환 변수 원터치 삽입 (본문에 추가)", style = MaterialTheme.typography.labelMedium, color = MintSecondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BlogPostTemplateEngine.SUPPORTED_PLACEHOLDERS.forEach { item ->
                        AssistChip(
                            onClick = {
                                bodyTemplate = if (bodyTemplate.isBlank()) item.token else "$bodyTemplate ${item.token}"
                            },
                            label = {
                                Text(
                                    item.token,
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily)
                                )
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = bodyTemplate,
                    onValueChange = {
                        bodyTemplate = it
                        validationError = null
                    },
                    label = { Text("본문 템플릿") },
                    minLines = 7,
                    maxLines = 12,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_template_body")
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusChip(
                        text = "예상 본문 ${renderedCharCount}자 (권장 1,200~2,500)",
                        textColor = if (renderedCharCount in 1200..2500) MintSecondary else TistoryAmber,
                        backgroundColor = if (renderedCharCount in 1200..2500) EmeraldContainer else AmberContainer
                    )
                    StatusChip(
                        text = if (hasRequiredFactTokens) "4대 팩트 토큰 포함" else "필수 팩트 토큰 일부 미포함",
                        textColor = if (hasRequiredFactTokens) CloudflareSky else TistoryAmber,
                        backgroundColor = if (hasRequiredFactTokens) CloudflareContainer else AmberContainer
                    )
                }

                OutlinedTextField(
                    value = tagsTemplate,
                    onValueChange = { tagsTemplate = it },
                    label = { Text("SEO 태그 (쉼표로 구분)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_template_tags")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("해당 물건·채널 생성 시 기본 적용", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                        Text(
                            "활성화 시 크롤링 물건 예약 생성에 이 템플릿을 우선 사용합니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = activateForGeneration,
                        onCheckedChange = { activateForGeneration = it },
                        modifier = Modifier.testTag("switch_template_active")
                    )
                }

                OutlinedButton(
                    onClick = { showRenderedPreview = !showRenderedPreview },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("toggle_template_preview")
                ) {
                    Icon(Icons.Filled.Preview, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (showRenderedPreview) "미리보기 닫기" else "실제 물건 데이터 치환 미리보기")
                }

                if (showRenderedPreview) {
                    Surface(
                        color = NavyBackground,
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.dp, CloudflareSky.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "샘플 사건: ${sampleAuction.caseId} (${sampleAuction.propertyType.displayLabel})",
                                style = MaterialTheme.typography.labelSmall,
                                color = CloudflareSky
                            )
                            Text(
                                text = renderedTitle,
                                style = MaterialTheme.typography.titleSmall,
                                color = MintSecondary
                            )
                            Text(
                                text = renderedBody,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }

                if (!validationError.isNullOrBlank()) {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = CrimsonAlert
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        name.isBlank() -> validationError = "템플릿 이름을 입력해 주세요."
                        titleTemplate.isBlank() -> validationError = "제목 템플릿을 입력해 주세요."
                        bodyTemplate.isBlank() -> validationError = "본문 템플릿을 입력해 주세요."
                        else -> {
                            if (initialTemplate == null || isDuplicateMode) {
                                onSaveNew(
                                    name,
                                    propertyType,
                                    targetChannel,
                                    titleTemplate,
                                    bodyTemplate,
                                    tagsTemplate,
                                    activateForGeneration
                                )
                            } else {
                                onSaveExisting(
                                    initialTemplate.copy(
                                        name = name,
                                        propertyType = propertyType,
                                        targetChannel = targetChannel,
                                        titleTemplate = titleTemplate,
                                        bodyTemplate = bodyTemplate,
                                        tagsTemplate = tagsTemplate,
                                        isActiveForGeneration = activateForGeneration
                                    )
                                )
                            }
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.testTag("btn_save_post_template"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = NavyBackground
                )
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("DataStore 저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = TextSecondary)
            }
        }
    )
}
