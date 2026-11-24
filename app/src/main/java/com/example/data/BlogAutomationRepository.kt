package com.example.data

import com.example.engine.BoilerplateAndTemplateEngine
import com.example.engine.DefaultSpecDataFactory
import com.example.engine.QaVerificationEngine
import com.example.engine.QualityDefenseEngine
import com.example.model.AuctionCaseIntel
import com.example.model.ChannelPlatform
import com.example.model.ChannelVariantContent
import com.example.model.GeneratedPostSlot
import com.example.model.InfographicCardSpec
import com.example.model.PostStatus
import com.example.model.ProFunnelConfig
import com.example.model.PropertyType
import com.example.model.PublishLogEntry
import com.example.model.ServerAutomationConfig
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

data class CloudflareCheckSummary(
    val success: Boolean,
    val message: String,
    val d1Names: List<String> = emptyList(),
    val activeCrons: List<String> = emptyList()
)

class BlogAutomationRepository(
    private val configStore: ServerConfigStore,
    private val api: CloudflareAndDdakchalApi = CloudflareAndDdakchalApi(),
    private val postTemplateStore: BlogPostTemplateStore? = null,
    private val seoChecklistStore: SeoChecklistStore? = null,
    private val proFunnelStore: ProFunnelStore? = null,
    private val postWarehouseStore: PostWarehouseStore? = null
) {
    val configFlow = configStore.configFlow
    val postTemplatesFlow = postTemplateStore?.templatesFlow
    val seoChecklistFlow = seoChecklistStore?.checklistFlow
    val proFunnelFlow = proFunnelStore?.funnelFlow
    val warehouseSnapshotFlow = postWarehouseStore?.snapshotFlow

    suspend fun saveConfig(config: ServerAutomationConfig) {
        configStore.saveConfig(config)
    }

    suspend fun saveCustomYaml(type: PropertyType, yamlContent: String) {
        configStore.saveCustomYaml(type, yamlContent)
    }

    suspend fun getCustomYamlOverrides(): Map<PropertyType, String> =
        configStore.getCustomYamlOverrides()

    suspend fun getRecent7DayCorpus(): List<String> =
        configStore.getRecent7DayCorpus()

    suspend fun savePostTemplate(template: com.example.model.BlogPostTemplate) {
        postTemplateStore?.saveTemplate(template)
    }

    suspend fun createPostTemplate(
        name: String,
        propertyType: PropertyType,
        targetChannel: ChannelPlatform,
        titleTemplate: String,
        bodyTemplate: String,
        tagsTemplate: String,
        activateForGeneration: Boolean
    ): com.example.model.BlogPostTemplate? = postTemplateStore?.createTemplate(
        name = name,
        propertyType = propertyType,
        targetChannel = targetChannel,
        titleTemplate = titleTemplate,
        bodyTemplate = bodyTemplate,
        tagsTemplate = tagsTemplate,
        activateForGeneration = activateForGeneration
    )

    suspend fun setActivePostTemplate(templateId: String) {
        postTemplateStore?.setActiveTemplate(templateId)
    }

    suspend fun deletePostTemplate(templateId: String) {
        postTemplateStore?.deleteTemplate(templateId)
    }

    suspend fun resetPostTemplates() {
        postTemplateStore?.resetToDefaults()
    }

    suspend fun toggleSeoChecklistItem(itemId: String) {
        seoChecklistStore?.toggleItem(itemId)
    }

    suspend fun createSeoChecklistItem(
        title: String,
        description: String,
        category: com.example.model.SeoRuleCategory,
        priority: com.example.model.SeoRulePriority
    ): com.example.model.SeoChecklistItem? = seoChecklistStore?.createItem(
        title = title,
        description = description,
        category = category,
        priority = priority
    )

    suspend fun saveSeoChecklistItem(item: com.example.model.SeoChecklistItem) {
        seoChecklistStore?.saveItem(item)
    }

    suspend fun deleteSeoChecklistItem(itemId: String) {
        seoChecklistStore?.deleteItem(itemId)
    }

    suspend fun resetSeoChecklist() {
        seoChecklistStore?.resetToDefaults()
    }

    suspend fun saveProFunnelConfig(funnel: ProFunnelConfig) {
        proFunnelStore?.saveConfig(funnel)
    }

    suspend fun resetProFunnelConfig() {
        proFunnelStore?.resetToDefaults()
    }

    suspend fun upsertWarehouseArticle(
        article: com.example.model.WarehouseArticle,
        log: com.example.model.WritingActivityLog? = null
    ) = postWarehouseStore?.upsertArticle(article, log)

    suspend fun upsertWarehouseArticles(
        articles: List<com.example.model.WarehouseArticle>,
        log: com.example.model.WritingActivityLog? = null
    ) = postWarehouseStore?.upsertArticles(articles, log)

    suspend fun recordWarehouseRegistration(
        warehouseId: String,
        channels: List<ChannelPlatform>,
        log: com.example.model.WritingActivityLog
    ) = postWarehouseStore?.recordRegistration(warehouseId, channels, log)

    suspend fun refreshVisitorTelemetry() = postWarehouseStore?.refreshVisitorTelemetry()

    suspend fun deleteWarehouseArticle(warehouseId: String) = postWarehouseStore?.deleteArticle(warehouseId)

    suspend fun resetWarehouse() = postWarehouseStore?.resetToDefaults()

    suspend fun triggerDaily5AndSync(config: ServerAutomationConfig): Result<List<GeneratedPostSlot>> {
        val genResult = api.triggerGenerateDaily5(
            apiBase = config.ddakchalApiBase,
            syncToken = config.crawlerSyncToken,
            limit = config.dailyPostLimit,
            minDelayMinutes = config.minDelayMinutes,
            maxDelayMinutes = config.maxDelayMinutes,
            enableExternalLinkNaver = config.enableExternalLinkNaver
        )
        if (!genResult.isSuccess) {
            return Result.failure(
                IllegalStateException("딱찰 생성 API 실패 (${genResult.errorMessage ?: genResult.statusCode})")
            )
        }
        return syncSyndicationPack(config)
    }

    suspend fun syncSyndicationPack(config: ServerAutomationConfig): Result<List<GeneratedPostSlot>> {
        val res = api.fetchSyndicationPack(config.ddakchalApiBase, config.crawlerSyncToken)
        if (!res.isSuccess || res.body.isBlank()) {
            return Result.failure(
                IllegalStateException("신디케이션 팩 조회 실패 (${res.errorMessage ?: res.statusCode})")
            )
        }
        val recentCorpus = configStore.getRecent7DayCorpus()
        return runCatching {
            parseSyndicationPackJson(res.body, config, recentCorpus)
        }
    }

    suspend fun verifyCloudflareAndCron(config: ServerAutomationConfig): CloudflareCheckSummary {
        if (config.cfAccountId.isBlank() || config.cfApiToken.isBlank()) {
            return CloudflareCheckSummary(false, "CF_ACCOUNT_ID 및 CF_API_TOKEN을 먼저 입력해 주세요.")
        }
        val d1Res = api.listCloudflareD1Databases(config.cfAccountId, config.cfApiToken)
        if (!d1Res.isSuccess) {
            return CloudflareCheckSummary(false, "Cloudflare 인증 실패: ${d1Res.errorMessage ?: d1Res.statusCode}")
        }

        val d1Names = mutableListOf<String>()
        runCatching {
            val root = JSONObject(d1Res.body)
            val arr = root.optJSONArray("result") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                val name = item.optString("name")
                val uuid = item.optString("uuid")
                if (name.isNotBlank()) d1Names += "$name (${uuid.take(8)}…)"
            }
        }

        val crons = mutableListOf<String>()
        if (config.cfWorkerScriptName.isNotBlank()) {
            val cronRes = api.getWorkerSchedules(
                config.cfAccountId,
                config.cfWorkerScriptName,
                config.cfApiToken
            )
            if (cronRes.isSuccess) {
                runCatching {
                    val root = JSONObject(cronRes.body)
                    val schedules = root.optJSONObject("result")?.optJSONArray("schedules") ?: JSONArray()
                    for (i in 0 until schedules.length()) {
                        crons += schedules.getJSONObject(i).optString("cron")
                    }
                }
            }
        }

        return CloudflareCheckSummary(
            success = true,
            message = "Cloudflare D1 ${d1Names.size}개 확인 · Worker Cron ${crons.size}개 활성",
            d1Names = d1Names,
            activeCrons = crons
        )
    }

    suspend fun migrateD1Schema(config: ServerAutomationConfig): Result<String> {
        if (config.cfAccountId.isBlank() || config.cfD1DatabaseId.isBlank() || config.cfApiToken.isBlank()) {
            return Result.failure(IllegalArgumentException("Cloudflare Account ID, D1 Database ID, API Token이 필요합니다."))
        }
        val sql = BoilerplateAndTemplateEngine.generateD1MigrationSql()
        val statements = sql.split(";")
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("--") }

        for (stmt in statements) {
            val res = api.executeCloudflareD1Query(
                accountId = config.cfAccountId,
                databaseId = config.cfD1DatabaseId,
                apiToken = config.cfApiToken,
                sql = "$stmt;"
            )
            if (!res.isSuccess) {
                return Result.failure(IllegalStateException("D1 마이그레이션 실패: ${res.errorMessage ?: res.body.take(140)}"))
            }
        }
        return Result.success("D1 generated_posts · publish_logs 테이블 초기화 완료 (${statements.size}개 DDL)")
    }

    suspend fun registerWorkerCron(config: ServerAutomationConfig): Result<List<String>> {
        if (config.cfAccountId.isBlank() || config.cfWorkerScriptName.isBlank() || config.cfApiToken.isBlank()) {
            return Result.failure(IllegalArgumentException("Cloudflare Account ID, Worker Script명, API Token을 입력해 주세요."))
        }
        val cron = config.cronExpression.trim().ifBlank { "17 8 * * *" }
        if (cron.startsWith("0 ")) {
            return Result.failure(IllegalArgumentException("Anti-Bot 수칙 위반: 정각(:00) 크론은 금지됩니다. 예: '17 8 * * *'"))
        }
        val res = api.putWorkerSchedules(
            accountId = config.cfAccountId,
            scriptName = config.cfWorkerScriptName,
            apiToken = config.cfApiToken,
            crons = listOf(cron)
        )
        return if (res.isSuccess) {
            Result.success(listOf(cron))
        } else {
            Result.failure(IllegalStateException("Cron 등록 실패: ${res.errorMessage ?: res.statusCode}"))
        }
    }

    suspend fun pushScheduledSlotToD1(
        config: ServerAutomationConfig,
        slot: GeneratedPostSlot
    ): Result<String> {
        if (config.cfAccountId.isBlank() || config.cfD1DatabaseId.isBlank() || config.cfApiToken.isBlank()) {
            return Result.failure(IllegalStateException("로컬 큐에만 예약 반영됨 (Cloudflare D1 키 미입력)"))
        }
        var inserted = 0
        for (platform in ChannelPlatform.entries) {
            val variant = slot.variants[platform] ?: continue
            val schedTime = slot.scheduledTimes[platform] ?: slot.publishDate
            val sql = """
                INSERT OR REPLACE INTO generated_posts
                (post_id, queue_id, case_id, property_type, blog_category, platform, title, body_html, body_markdown, meta_json, processed_image_urls, similarity_score, status, scheduled_for)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """.trimIndent()
            val params = listOf(
                "${slot.postId}-${platform.code.lowercase()}",
                slot.queueId,
                slot.auctionCase.caseId,
                slot.auctionCase.propertyType.code,
                slot.auctionCase.propertyType.blogCategory,
                platform.code,
                variant.title,
                variant.bodyHtml,
                variant.bodyText,
                JSONObject().apply {
                    put("tags", JSONArray(variant.tags))
                    put("metaDescription", variant.metaDescription)
                    put("jsonLd", variant.jsonLdSchema)
                }.toString(),
                JSONArray(variant.infographicCards.map { it.seoFilename }).toString(),
                slot.qaReport.maxCrossChannelCosinePct / 100.0,
                PostStatus.QUEUED.code,
                schedTime
            )
            val res = api.executeCloudflareD1Query(
                config.cfAccountId,
                config.cfD1DatabaseId,
                config.cfApiToken,
                sql,
                params
            )
            if (res.isSuccess) inserted++
        }
        configStore.appendRecent7DayCorpus(slot.variants.values.map { it.bodyText })
        return if (inserted == 3) {
            Result.success("Cloudflare D1 `generated_posts` 3채널 예약 저장 완료")
        } else {
            Result.failure(IllegalStateException("D1 저장 일부 실패 ($inserted/3건)"))
        }
    }

    suspend fun pushWeeklyBatchToD1(
        config: ServerAutomationConfig,
        slots: List<GeneratedPostSlot>
    ): Pair<Int, String> {
        if (config.cfAccountId.isBlank() || config.cfD1DatabaseId.isBlank() || config.cfApiToken.isBlank()) {
            configStore.appendRecent7DayCorpus(slots.flatMap { s -> s.variants.values.map { it.bodyText } })
            return 0 to "로컬 7일 주간예약 완료 (Cloudflare D1 키 미입력 · 로컬 창고 보관)"
        }
        var syncedCount = 0
        slots.forEach { slot ->
            val res = pushScheduledSlotToD1(config, slot)
            if (res.isSuccess) syncedCount++
        }
        return syncedCount to "Cloudflare D1 주간예약 ${syncedCount}/${slots.size}건 동기화 완료"
    }

    suspend fun queryD1QueueSummary(config: ServerAutomationConfig): Result<String> {
        if (config.cfAccountId.isBlank() || config.cfD1DatabaseId.isBlank() || config.cfApiToken.isBlank()) {
            return Result.failure(IllegalArgumentException("Cloudflare D1 설정을 먼저 입력해 주세요."))
        }
        val sql = "SELECT case_id, platform, status, scheduled_for, similarity_score FROM generated_posts ORDER BY created_at DESC LIMIT 15;"
        val res = api.executeCloudflareD1Query(config.cfAccountId, config.cfD1DatabaseId, config.cfApiToken, sql)
        return if (res.isSuccess) {
            val root = JSONObject(res.body)
            val results = root.optJSONArray("result")?.optJSONObject(0)?.optJSONArray("results") ?: JSONArray()
            Result.success("D1 최근 레코드 ${results.length()}건 조회 성공")
        } else {
            Result.failure(IllegalStateException("D1 쿼리 오류: ${res.errorMessage ?: res.statusCode}"))
        }
    }

    fun computeStaggeredSchedule(
        baseDate: String,
        slotIndex: Int,
        config: ServerAutomationConfig
    ): Map<ChannelPlatform, String> {
        val cal = Calendar.getInstance()
        val parts = baseDate.split("-")
        if (parts.size == 3) {
            cal.set(parts[0].toIntOrNull() ?: 2026, (parts[1].toIntOrNull() ?: 11) - 1, parts[2].toIntOrNull() ?: 22)
        }
        // Start between 08:11 and 13:47, strictly avoiding :00 minute/second
        val baseHour = (8 + (slotIndex - 1)).coerceAtMost(14)
        val jitterMin = Random.nextInt(7, 53)
        val jitterSec = Random.nextInt(11, 58)
        cal.set(Calendar.HOUR_OF_DAY, baseHour)
        cal.set(Calendar.MINUTE, jitterMin)
        cal.set(Calendar.SECOND, jitterSec)

        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
        val tCf = fmt.format(cal.time)

        val delayTistory = Random.nextInt(
            config.minDelayMinutes.coerceAtLeast(30),
            config.maxDelayMinutes.coerceAtLeast(config.minDelayMinutes + 10)
        )
        cal.add(Calendar.MINUTE, delayTistory)
        if (cal.get(Calendar.MINUTE) == 0) cal.add(Calendar.MINUTE, 3)
        val tTs = fmt.format(cal.time)

        val delayNaver = Random.nextInt(
            (config.minDelayMinutes + 25).coerceAtLeast(55),
            (config.maxDelayMinutes + 40).coerceAtLeast(100)
        )
        cal.add(Calendar.MINUTE, delayNaver)
        if (cal.get(Calendar.MINUTE) == 0) cal.add(Calendar.MINUTE, 7)
        val tNv = fmt.format(cal.time)

        return mapOf(
            ChannelPlatform.CLOUDFLARE to tCf,
            ChannelPlatform.TISTORY to tTs,
            ChannelPlatform.NAVER to tNv
        )
    }

    suspend fun rewriteVariantToPassQa(
        slot: GeneratedPostSlot,
        config: ServerAutomationConfig,
        proFunnel: ProFunnelConfig = ProFunnelConfig()
    ): GeneratedPostSlot {
        val saltTag = "[검수리라이팅-${System.currentTimeMillis() % 10000}]"
        var rebuilt = DefaultSpecDataFactory.buildDistinct3ChannelVariants(
            auction = slot.auctionCase,
            seedIndex = slot.slotIndex + Random.nextInt(1, 12),
            config = config,
            rewriteSalt = "",
            proFunnel = proFunnel
        )

        // Optional remote LLM rewrite if user entered LLM_API_ENDPOINT & LLM_API_KEY
        if (config.llmApiEndpoint.isNotBlank() && config.llmApiKey.isNotBlank()) {
            val naverPrompt = """
                당신은 딱찰 경매 블로그 작가입니다. 다음 사건의 핵심 수치(사건번호 ${slot.auctionCase.caseId}, 감정가 ${QaVerificationEngine.formatKrwShort(slot.auctionCase.appraisalPriceKrw)}, 최저가 ${QaVerificationEngine.formatKrwShort(slot.auctionCase.minBidPriceKrw)}, 말소기준권리 ${slot.auctionCase.baseRightDate})를 0% 오차 없이 유지하되, 티스토리·Cloudflare 글과 표현이 겹치지 않도록 1,400자 내외 친근한 Q&A 문체로 새로 작성하세요. 외부 URL은 절대 넣지 마세요.
            """.trimIndent()
            val llmRes = api.callCustomLlmRewrite(config.llmApiEndpoint, config.llmApiKey, naverPrompt)
            if (llmRes.isSuccess) {
                runCatching {
                    val content = JSONObject(llmRes.body)
                        .optJSONArray("choices")?.optJSONObject(0)
                        ?.optJSONObject("message")?.optString("content").orEmpty()
                    if (content.count { !it.isWhitespace() } >= 1200) {
                        val oldNv = rebuilt[ChannelPlatform.NAVER]!!
                        rebuilt = rebuilt.toMutableMap().apply {
                            put(
                                ChannelPlatform.NAVER,
                                oldNv.copy(
                                    bodyText = content,
                                    bodyHtml = "<div class='se-viewer'>${content.replace("\n", "<br/>")}</div>"
                                )
                            )
                        }
                    }
                }
            }
        } else {
            // Re-salt image hashes so every rewrite produces distinct MD5/SHA256
            rebuilt = rebuilt.mapValues { (_, variant) ->
                variant.copy(
                    infographicCards = variant.infographicCards.map { card ->
                        val seed = "${card.seoFilename}|$saltTag|${Random.nextInt()}"
                        card.copy(
                            md5Hash = QaVerificationEngine.md5Short(seed),
                            sha256Hash = QaVerificationEngine.sha256Short(seed)
                        )
                    }
                )
            }
        }

        val recent = configStore.getRecent7DayCorpus()
        val draftSlot = slot.copy(variants = rebuilt)
        val (defendedSlot, _) = QualityDefenseEngine.defendSlot(
            slot = draftSlot,
            config = config,
            proFunnel = proFunnel,
            recent7DayCorpus = recent
        )
        return defendedSlot.copy(
            status = if (defendedSlot.qaReport.overallPassed) PostStatus.QUEUED else PostStatus.DRAFT
        )
    }

    private fun parseSyndicationPackJson(
        rawJson: String,
        config: ServerAutomationConfig,
        recentCorpus: List<String>
    ): List<GeneratedPostSlot> {
        val root = JSONObject(rawJson)
        val items = root.optJSONArray("items") ?: return DefaultSpecDataFactory.buildInitialDaily5Slots(config)
        if (items.length() == 0) return DefaultSpecDataFactory.buildInitialDaily5Slots(config)

        val result = mutableListOf<GeneratedPostSlot>()
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val caseNo = item.optString("case_no", "2024타경${4500 + i}")
            val dedupKey = item.optString("auction_dedup_key", "ca-$caseNo")
            val publishDate = item.optString("publish_date", "2026-11-22")
            val slotIndex = item.optInt("slot_index", i + 1)
            val channelVariantsJson = item.optJSONObject("channelVariants")

            val nvObj = channelVariantsJson?.optJSONObject("naver")
            val catCode = nvObj?.optString("categoryCode").orEmpty()
            val propType = PropertyType.fromRaw(catCode.ifBlank { item.optString("title") })

            val auction = AuctionCaseIntel(
                caseId = caseNo,
                dedupKey = dedupKey,
                courtName = item.optString("court_name", "대법원 경매계"),
                address = item.optString("address", item.optString("title")),
                propertyType = propType,
                appraisalPriceKrw = item.optLong("appraisal_krw", 500_000_000L),
                minBidPriceKrw = item.optLong("min_bid_krw", 320_000_000L),
                winningPriceKrw = null,
                bidCount = item.optInt("bid_count", 2),
                discountRatePct = item.optInt("discount_rate_pct", 64),
                auctionDate = item.optString("auction_date", "2026-12-05"),
                baseRightDate = item.optString("base_right_date", "2021-06-15"),
                dividendDeadline = item.optString("dividend_deadline", "2025-11-30"),
                claimAmountKrw = item.optLong("claim_amount_krw", 240_000_000L),
                depositRatePct = item.optInt("deposit_rate_pct", 10),
                possessionSummary = item.optString("possession_summary", "현황조사서 확인 필요"),
                tenantSummary = item.optString("tenant_summary", "매각물건명세서 임차인 확인"),
                assumedRightsText = item.optString("assumed_rights_text", "말소기준권리 이후 소멸 여부 대조"),
                specialCheckNotice = propType.focusSummary,
                recentRealTradeMedianKrw = item.optLong("real_trade_median_krw", 460_000_000L),
                stats3yRatePct = item.optDouble("stats_3y_rate_pct", 72.5),
                clicksLast7d = item.optInt("clicks_7d", 420),
                conservativeBidKrw = item.optLong("conservative_bid_krw", 330_000_000L),
                balancedBidKrw = item.optLong("balanced_bid_krw", 350_000_000L),
                aggressiveBidKrw = item.optLong("aggressive_bid_krw", 370_000_000L)
            )

            val fallbackVariants = DefaultSpecDataFactory.buildDistinct3ChannelVariants(auction, i, config)
            val parsedVariants = if (channelVariantsJson != null) {
                ChannelPlatform.entries.associateWith { platform ->
                    val key = platform.code.lowercase()
                    val vObj = channelVariantsJson.optJSONObject(key)
                    val fb = fallbackVariants[platform]!!
                    if (vObj == null) fb else {
                        val bodyText = vObj.optString("bodyText").ifBlank { fb.bodyText }
                        val bodyHtml = vObj.optString("bodyHtml").ifBlank { fb.bodyHtml }
                        val cardsArr = vObj.optJSONArray("infographicCards")
                        val cards = if (cardsArr != null && cardsArr.length() in 4..7) {
                            (0 until cardsArr.length()).map { cIdx ->
                                val c = cardsArr.getJSONObject(cIdx)
                                val seed = "$caseNo|${platform.code}|$cIdx|${c.optString("headline")}"
                                InfographicCardSpec(
                                    cardIndex = c.optInt("cardIndex", cIdx + 1),
                                    cardType = c.optString("cardType", "CARD_${cIdx + 1}"),
                                    aspectRatio = c.optString("aspectRatio", platform.imageResolutionSpec),
                                    headline = c.optString("headline"),
                                    subline = c.optString("subline"),
                                    seoFilename = "$caseNo-${platform.code.lowercase()}-${cIdx + 1}.webp",
                                    altText = "$caseNo ${c.optString("headline")}",
                                    md5Hash = QaVerificationEngine.md5Short(seed),
                                    sha256Hash = QaVerificationEngine.sha256Short(seed),
                                    exifStripped = true
                                )
                            }
                        } else fb.infographicCards

                        fb.copy(
                            title = vObj.optString("title").ifBlank { fb.title },
                            bodyText = bodyText,
                            bodyHtml = bodyHtml,
                            ctaUrl = vObj.optString("ctaUrl").ifBlank { fb.ctaUrl },
                            infographicCards = cards
                        )
                    }
                }
            } else {
                fallbackVariants
            }

            val scheduled = computeStaggeredSchedule(publishDate, slotIndex, config)
            val qa = QaVerificationEngine.auditSlot(auction, parsedVariants, scheduled, config, recentCorpus)
            result += GeneratedPostSlot(
                postId = item.optString("id", "post-$dedupKey"),
                queueId = item.optString("id", "q-$slotIndex"),
                slotIndex = slotIndex,
                publishDate = publishDate,
                auctionCase = auction,
                variants = parsedVariants,
                scheduledTimes = scheduled,
                status = PostStatus.fromCode(item.optString("status", "QUEUED")),
                qaReport = qa,
                sourceLabel = "실서버 /syndication-pack"
            )
        }
        return result
    }
}
