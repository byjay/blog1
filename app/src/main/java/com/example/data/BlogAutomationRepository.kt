package com.example.data

import com.example.engine.BoilerplateAndTemplateEngine
import com.example.engine.DefaultSpecDataFactory
import com.example.engine.HtmlExportEngine
import com.example.engine.QaVerificationEngine
import com.example.engine.QualityDefenseEngine
import com.example.model.AdminPipelineStageStatus
import com.example.model.AdminPipelineStepLog
import com.example.model.AuctionCaseIntel
import com.example.model.ChannelPlatform
import com.example.model.ChannelPublishStat
import com.example.model.ChannelVariantContent
import com.example.model.DailyPublishVolumePoint
import com.example.model.DdakchalBlogAnalysisData
import com.example.model.DdakchalEndpointCheckItem
import com.example.model.DdakchalFreshness
import com.example.model.DdakchalFullDiagnosticReport
import com.example.model.DdakchalKeyMetricsChartData
import com.example.model.DdakchalQaPair
import com.example.model.DdakchalRiskOpportunity
import com.example.model.DdakchalTriggerEventSummary
import com.example.model.DdakchalTriggerStatusSnapshot
import com.example.model.FieldVisitRecord
import com.example.model.GeneratedPostSlot
import com.example.model.InfographicCardSpec
import com.example.model.PostStatus
import com.example.model.ProFunnelConfig
import com.example.model.PropertyType
import com.example.model.PublishLogEntry
import com.example.model.QueueAnalyticsSummary
import com.example.model.ServerAutomationConfig
import com.example.model.ServerDbDiscoveredItem
import com.example.model.ServerDbTableInfo
import com.example.model.VisitRecordSource
import com.example.data.DdakchalRetrofitClientFactory.Companion.toDomainRecord
import com.example.engine.PrivacySanitizerEngine
import com.example.model.CourtDocKind
import com.example.model.CourtDocumentRecord
import com.example.model.DdakchalQueueItemRecord
import com.example.model.QueueSyncResult
import com.example.model.RegistryNodeTone
import com.example.model.RegistrySection
import com.example.model.RegistryVisualNode
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

data class CloudflareCheckSummary(
    val success: Boolean,
    val message: String,
    val d1Names: List<String> = emptyList(),
    val activeCrons: List<String> = emptyList()
)

data class DdakchalSyncOutcome<T>(
    val isSuccess: Boolean,
    val data: T? = null,
    val latencyMs: Long = 0L,
    val httpStatusCode: Int = 0,
    val errorMessage: String? = null,
    val nextCursorSince: String? = null
)

data class ServerDbDiscoveryResult(
    val tables: List<ServerDbTableInfo>,
    val items: List<ServerDbDiscoveredItem>,
    val d1TableNames: List<String>,
    val triggerStatus: DdakchalTriggerStatusSnapshot?,
    val stepLogs: List<AdminPipelineStepLog>,
    val summaryMessage: String
)

data class TodakCrawlEngineRunResult(
    val isSuccess: Boolean,
    val items: List<ServerDbDiscoveredItem>,
    val triggerStatus: DdakchalTriggerStatusSnapshot?,
    val stepLogs: List<AdminPipelineStepLog>,
    val httpStatusCode: Int,
    val latencyMs: Long,
    val summaryMessage: String
)

data class TodakCaseLookupResult(
    val isSuccess: Boolean,
    val normalizedCaseId: String,
    val slot: GeneratedPostSlot? = null,
    val stepLogs: List<AdminPipelineStepLog> = emptyList(),
    val httpStatusCode: Int = 0,
    val latencyMs: Long = 0L,
    val message: String = ""
)

class BlogAutomationRepository(
    private val configStore: ServerConfigStore,
    private val api: CloudflareAndDdakchalApi = CloudflareAndDdakchalApi(),
    private val postTemplateStore: BlogPostTemplateStore? = null,
    private val seoChecklistStore: SeoChecklistStore? = null,
    private val proFunnelStore: ProFunnelStore? = null,
    private val postWarehouseStore: PostWarehouseStore? = null,
    private val retrofitServiceFactory: DdakchalRetrofitClientFactory = DdakchalRetrofitClientFactory(api.okHttpClient)
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
        val outcome = syncSyndicationPackWithTelemetry(config)
        return if (outcome.isSuccess && outcome.data != null) {
            Result.success(outcome.data)
        } else {
            Result.failure(
                IllegalStateException(outcome.errorMessage ?: "신디케이션 팩 조회 실패 (${outcome.httpStatusCode})")
            )
        }
    }

    suspend fun syncSyndicationPackWithTelemetry(
        config: ServerAutomationConfig,
        limit: Int = 5,
        date: String? = null
    ): DdakchalSyncOutcome<List<GeneratedPostSlot>> {
        val res = api.fetchSyndicationPack(
            apiBase = config.ddakchalApiBase,
            syncToken = config.crawlerSyncToken,
            limit = limit,
            date = date,
            enableExternalLinkNaver = config.enableExternalLinkNaver
        )
        if (!res.isSuccess || res.body.isBlank()) {
            return DdakchalSyncOutcome(
                isSuccess = false,
                latencyMs = res.latencyMs,
                httpStatusCode = res.statusCode,
                errorMessage = formatHttpFailure("신디케이션 팩 조회", res)
            )
        }
        val recentCorpus = configStore.getRecent7DayCorpus()
        return runCatching {
            val slots = parseSyndicationPackJson(res.body, config, recentCorpus, sourceLabel = "실서버 /syndication-pack")
            val latestCursor = slots.mapNotNull { it.serverUpdatedAt.ifBlank { null } }.maxOrNull()
            DdakchalSyncOutcome(
                isSuccess = true,
                data = slots,
                latencyMs = res.latencyMs,
                httpStatusCode = res.statusCode,
                nextCursorSince = latestCursor
            )
        }.getOrElse { err ->
            DdakchalSyncOutcome(
                isSuccess = false,
                latencyMs = res.latencyMs,
                httpStatusCode = res.statusCode,
                errorMessage = err.message ?: "신디케이션 팩 파싱 오류"
            )
        }
    }

    suspend fun syncAnalysisFeedWithTelemetry(
        config: ServerAutomationConfig,
        limit: Int = 10,
        since: String? = null,
        category: String? = null
    ): DdakchalSyncOutcome<List<GeneratedPostSlot>> {
        val res = api.fetchAnalysisFeed(
            apiBase = config.ddakchalApiBase,
            syncToken = config.crawlerSyncToken,
            limit = limit,
            since = since,
            category = category,
            enableExternalLinkNaver = config.enableExternalLinkNaver
        )
        if (!res.isSuccess || res.body.isBlank()) {
            return DdakchalSyncOutcome(
                isSuccess = false,
                latencyMs = res.latencyMs,
                httpStatusCode = res.statusCode,
                errorMessage = formatHttpFailure("분석 피드 조회", res)
            )
        }
        val recentCorpus = configStore.getRecent7DayCorpus()
        return runCatching {
            val slots = parseAnalysisFeedJson(res.body, config, recentCorpus)
            val latestCursor = slots.mapNotNull { it.serverUpdatedAt.ifBlank { null } }.maxOrNull()
            DdakchalSyncOutcome(
                isSuccess = true,
                data = slots,
                latencyMs = res.latencyMs,
                httpStatusCode = res.statusCode,
                nextCursorSince = latestCursor
            )
        }.getOrElse { err ->
            DdakchalSyncOutcome(
                isSuccess = false,
                latencyMs = res.latencyMs,
                httpStatusCode = res.statusCode,
                errorMessage = err.message ?: "분석 피드 파싱 오류"
            )
        }
    }

    suspend fun fetchSingleAssetWithTelemetry(
        config: ServerAutomationConfig,
        keyOrCaseNo: String,
        refresh: Boolean = false
    ): DdakchalSyncOutcome<GeneratedPostSlot> {
        val res = api.fetchAssetByKey(
            apiBase = config.ddakchalApiBase,
            syncToken = config.crawlerSyncToken,
            keyOrCaseNo = keyOrCaseNo,
            refresh = refresh,
            enableExternalLinkNaver = config.enableExternalLinkNaver
        )
        if (!res.isSuccess || res.body.isBlank()) {
            return DdakchalSyncOutcome(
                isSuccess = false,
                latencyMs = res.latencyMs,
                httpStatusCode = res.statusCode,
                errorMessage = formatHttpFailure("단건 자산 조회", res)
            )
        }
        val recentCorpus = configStore.getRecent7DayCorpus()
        return runCatching {
            val slot = parseSingleAssetJson(res.body, config, recentCorpus)
                ?: throw IllegalStateException("유효한 단건 자산 항목이 없습니다.")
            DdakchalSyncOutcome(
                isSuccess = true,
                data = slot,
                latencyMs = res.latencyMs,
                httpStatusCode = res.statusCode,
                nextCursorSince = slot.serverUpdatedAt.ifBlank { null }
            )
        }.getOrElse { err ->
            DdakchalSyncOutcome(
                isSuccess = false,
                latencyMs = res.latencyMs,
                httpStatusCode = res.statusCode,
                errorMessage = err.message ?: "단건 자산 파싱 오류"
            )
        }
    }

    suspend fun fetchTriggerStatusWithTelemetry(
        config: ServerAutomationConfig
    ): DdakchalSyncOutcome<DdakchalTriggerStatusSnapshot> {
        val res = api.fetchTriggerStatus(config.ddakchalApiBase, config.crawlerSyncToken)
        if (!res.isSuccess || res.body.isBlank()) {
            return DdakchalSyncOutcome(
                isSuccess = false,
                latencyMs = res.latencyMs,
                httpStatusCode = res.statusCode,
                errorMessage = formatHttpFailure("서버 상태 조회", res)
            )
        }
        return runCatching {
            val snapshot = parseTriggerStatusJson(res.body)
            DdakchalSyncOutcome(
                isSuccess = true,
                data = snapshot,
                latencyMs = res.latencyMs,
                httpStatusCode = res.statusCode
            )
        }.getOrElse { err ->
            DdakchalSyncOutcome(
                isSuccess = false,
                latencyMs = res.latencyMs,
                httpStatusCode = res.statusCode,
                errorMessage = err.message ?: "트리거 상태 파싱 오류"
            )
        }
    }

    suspend fun runCrawlAndSyncWithTelemetry(
        config: ServerAutomationConfig,
        regions: List<String> = emptyList(),
        maxBatch: Int = 15,
        limit: Int = 5
    ): DdakchalSyncOutcome<List<GeneratedPostSlot>> {
        val crawlRes = api.triggerCrawlAndSync(
            apiBase = config.ddakchalApiBase,
            syncToken = config.crawlerSyncToken,
            regions = regions,
            runCrawl = true,
            maxBatch = maxBatch,
            limit = limit,
            enableExternalLinkNaver = config.enableExternalLinkNaver
        )
        if (!crawlRes.isSuccess) {
            return DdakchalSyncOutcome(
                isSuccess = false,
                latencyMs = crawlRes.latencyMs,
                httpStatusCode = crawlRes.statusCode,
                errorMessage = "크롤링+동기화 실패 (${crawlRes.errorMessage ?: crawlRes.statusCode})"
            )
        }
        val recentCorpus = configStore.getRecent7DayCorpus()
        val parsedDirect = runCatching {
            val root = JSONObject(crawlRes.body)
            val dataObj = root.optJSONObject("data") ?: root
            if (dataObj.has("items") && (dataObj.optJSONArray("items")?.length() ?: 0) > 0) {
                parseAuctionItemsFromJson(
                    rawJson = crawlRes.body,
                    config = config,
                    recentCorpus = recentCorpus,
                    sourceLabel = "실서버 /crawl-and-sync",
                    allowDummyFallback = false
                )
            } else emptyList()
        }.getOrDefault(emptyList())

        if (parsedDirect.isNotEmpty()) {
            return DdakchalSyncOutcome(
                isSuccess = true,
                data = parsedDirect,
                latencyMs = crawlRes.latencyMs,
                httpStatusCode = crawlRes.statusCode,
                nextCursorSince = parsedDirect.mapNotNull { it.serverUpdatedAt.ifBlank { null } }.maxOrNull()
            )
        }
        return syncAnalysisFeedWithTelemetry(config, limit = limit.coerceAtLeast(10))
    }

    /**
     * 딱찰 서버 DB(/assets/feed, /analysis-feed, /assets/{key}, /triggers/status, /queue)와
     * Cloudflare D1 테이블(sqlite_master)을 직접 탐색해 실데이터 레코드를 반환한다.
     */
    suspend fun inspectServerDatabases(
        config: ServerAutomationConfig,
        categoryFilter: String = "",
        searchQuery: String = "",
        limit: Int = 20
    ): ServerDbDiscoveryResult {
        val apiBase = config.ddakchalApiBase.trim().removeSuffix("/")
        val token = config.crawlerSyncToken.trim()
        val recentCorpus = configStore.getRecent7DayCorpus()
        val tables = mutableListOf<ServerDbTableInfo>()
        val stepLogs = mutableListOf<AdminPipelineStepLog>()
        val discoveredByKey = linkedMapOf<String, ServerDbDiscoveredItem>()
        val d1Tables = mutableListOf<String>()

        fun addSlots(slots: List<GeneratedPostSlot>, origin: String) {
            slots.forEach { slot ->
                val dedup = slot.auctionCase.dedupKey.ifBlank { slot.auctionCase.caseId }
                if (!discoveredByKey.containsKey(dedup)) {
                    discoveredByKey[dedup] = ServerDbDiscoveredItem(
                        recordId = slot.postId,
                        dedupKey = dedup,
                        caseId = slot.auctionCase.caseId,
                        courtName = slot.auctionCase.courtName,
                        address = slot.auctionCase.address,
                        propertyType = slot.auctionCase.propertyType,
                        appraisalKrw = slot.auctionCase.appraisalPriceKrw,
                        minBidKrw = slot.auctionCase.minBidPriceKrw,
                        photoCount = slot.courtPhotoUrls.count { it.isNotBlank() },
                        visitCount = slot.visitRecords.size,
                        hasAnalysisData = slot.ddakchalAnalysisData != null,
                        sourceOrigin = origin,
                        updatedAt = slot.serverUpdatedAt.ifBlank { slot.publishDate },
                        slot = slot
                    )
                }
            }
        }

        // 1) Optional Direct Case/Key Asset Lookup when search query looks specific
        val cleanQuery = searchQuery.trim()
        if (cleanQuery.isNotBlank()) {
            val singleRes = api.fetchAssetByKey(
                apiBase = apiBase,
                syncToken = token,
                keyOrCaseNo = cleanQuery,
                refresh = false,
                enableExternalLinkNaver = config.enableExternalLinkNaver
            )
            val parsedSingle = if (singleRes.isSuccess && singleRes.body.isNotBlank()) {
                runCatching { parseSingleAssetJson(singleRes.body, config, recentCorpus) }.getOrNull()
            } else null
            if (parsedSingle != null) {
                addSlots(listOf(parsedSingle), "DB /assets/$cleanQuery")
            }
            stepLogs += AdminPipelineStepLog(
                stepIndex = 1,
                title = "사건/키워드 단건 DB 조회",
                endpoint = "/api/v1/blog/assets/$cleanQuery",
                status = if (parsedSingle != null) AdminPipelineStageStatus.SUCCESS else AdminPipelineStageStatus.WARN,
                httpCode = singleRes.statusCode,
                latencyMs = singleRes.latencyMs,
                detail = if (parsedSingle != null) {
                    "단건 매칭 성공: ${parsedSingle.auctionCase.caseId}"
                } else {
                    formatHttpFailure("단건 조회", singleRes)
                }
            )
        }

        // 2) GET /api/v1/blog/assets/feed (Raw Crawler Asset DB)
        val assetsFeedRes = api.fetchAssetsFeed(
            apiBase = apiBase,
            syncToken = token,
            category = categoryFilter.ifBlank { null },
            limit = limit.coerceIn(1, 50),
            flush = true,
            query = cleanQuery.ifBlank { null }
        )
        val assetsSlots = if (assetsFeedRes.isSuccess && assetsFeedRes.body.isNotBlank()) {
            runCatching {
                parseAuctionItemsFromJson(
                    rawJson = assetsFeedRes.body,
                    config = config,
                    recentCorpus = recentCorpus,
                    sourceLabel = "실서버 DB /assets/feed",
                    allowDummyFallback = false
                )
            }.getOrDefault(emptyList())
        } else emptyList()
        addSlots(assetsSlots, "DB /assets/feed")
        tables += ServerDbTableInfo(
            sourceName = "딱찰 크롤러 자산 DB",
            tableOrEndpoint = "/api/v1/blog/assets/feed",
            rowCount = assetsSlots.size,
            httpStatusCode = assetsFeedRes.statusCode,
            sampleColumns = listOf("case_no", "auction_dedup_key", "photo_urls", "visit_records", "appraisal_krw"),
            statusDetail = if (assetsFeedRes.isSuccess) "${assetsSlots.size}건 수신 (${assetsFeedRes.latencyMs}ms)" else formatHttpFailure("자산 피드 DB", assetsFeedRes)
        )
        stepLogs += AdminPipelineStepLog(
            stepIndex = stepLogs.size + 1,
            title = "딱찰 자산 DB 탐색 (/assets/feed)",
            endpoint = "/api/v1/blog/assets/feed",
            status = when {
                assetsFeedRes.isSuccess && assetsSlots.isNotEmpty() -> AdminPipelineStageStatus.SUCCESS
                assetsFeedRes.isSuccess -> AdminPipelineStageStatus.WARN
                else -> AdminPipelineStageStatus.ERROR
            },
            httpCode = assetsFeedRes.statusCode,
            latencyMs = assetsFeedRes.latencyMs,
            detail = if (assetsFeedRes.isSuccess) "자산 DB ${assetsSlots.size}건 로드" else formatHttpFailure("자산 DB", assetsFeedRes)
        )

        // 3) GET /api/v1/blog/analysis-feed (Analyzed Blog Intelligence DB)
        val analysisRes = api.fetchAnalysisFeed(
            apiBase = apiBase,
            syncToken = token,
            limit = limit.coerceIn(1, 30),
            category = categoryFilter.ifBlank { null },
            enableExternalLinkNaver = config.enableExternalLinkNaver
        )
        val analysisSlots = if (analysisRes.isSuccess && analysisRes.body.isNotBlank()) {
            runCatching { parseAnalysisFeedJson(analysisRes.body, config, recentCorpus) }.getOrDefault(emptyList())
        } else emptyList()
        addSlots(analysisSlots, "DB /analysis-feed")
        tables += ServerDbTableInfo(
            sourceName = "딱찰 분석 리포트 DB",
            tableOrEndpoint = "/api/v1/blog/analysis-feed",
            rowCount = analysisSlots.size,
            httpStatusCode = analysisRes.statusCode,
            sampleColumns = listOf("blog_analysis_data", "channelVariants", "min_bid_krw", "base_right_date"),
            statusDetail = if (analysisRes.isSuccess) "${analysisSlots.size}건 수신 (${analysisRes.latencyMs}ms)" else formatHttpFailure("분석 피드 DB", analysisRes)
        )
        stepLogs += AdminPipelineStepLog(
            stepIndex = stepLogs.size + 1,
            title = "딱찰 분석 DB 탐색 (/analysis-feed)",
            endpoint = "/api/v1/blog/analysis-feed",
            status = when {
                analysisRes.isSuccess && analysisSlots.isNotEmpty() -> AdminPipelineStageStatus.SUCCESS
                analysisRes.isSuccess -> AdminPipelineStageStatus.WARN
                else -> AdminPipelineStageStatus.ERROR
            },
            httpCode = analysisRes.statusCode,
            latencyMs = analysisRes.latencyMs,
            detail = if (analysisRes.isSuccess) "분석 DB ${analysisSlots.size}건 로드" else formatHttpFailure("분석 DB", analysisRes)
        )

        // 4) GET /api/v1/blog/triggers/status
        val triggerOutcome = fetchTriggerStatusWithTelemetry(config)
        val triggerSnapshot = triggerOutcome.data
        tables += ServerDbTableInfo(
            sourceName = "아웃박스 트리거·스냅샷 큐",
            tableOrEndpoint = "/api/v1/blog/triggers/status",
            rowCount = triggerSnapshot?.snapshotsCount ?: 0,
            httpStatusCode = triggerOutcome.httpStatusCode,
            sampleColumns = listOf("pending", "processed", "snapshotsCount", "todayQueueCount"),
            statusDetail = if (triggerSnapshot != null) {
                "대기 ${triggerSnapshot.pendingCount} · 처리 ${triggerSnapshot.processedCount} · 스냅샷 ${triggerSnapshot.snapshotsCount} · 금일큐 ${triggerSnapshot.todayQueueCount}"
            } else {
                triggerOutcome.errorMessage ?: "트리거 상태 확인 실패"
            }
        )

        // 5) Cloudflare D1 Table Discovery (sqlite_master) when CF keys are configured
        if (config.cfAccountId.isNotBlank() && config.cfD1DatabaseId.isNotBlank() && config.cfApiToken.isNotBlank()) {
            val schemaRes = api.executeCloudflareD1Query(
                accountId = config.cfAccountId,
                databaseId = config.cfD1DatabaseId,
                apiToken = config.cfApiToken,
                sql = "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name;"
            )
            if (schemaRes.isSuccess && schemaRes.body.isNotBlank()) {
                runCatching {
                    val root = JSONObject(schemaRes.body)
                    val arr = root.optJSONArray("result")?.optJSONObject(0)?.optJSONArray("results") ?: JSONArray()
                    val safeNameRegex = Regex("^[A-Za-z0-9_]+$")
                    for (i in 0 until arr.length()) {
                        val tName = arr.optJSONObject(i)?.optString("name").orEmpty().trim()
                        if (tName.isNotBlank() && safeNameRegex.matches(tName)) {
                            d1Tables += tName
                            val countRes = api.executeCloudflareD1Query(
                                accountId = config.cfAccountId,
                                databaseId = config.cfD1DatabaseId,
                                apiToken = config.cfApiToken,
                                sql = "SELECT COUNT(*) AS cnt FROM $tName;"
                            )
                            val rowCnt = if (countRes.isSuccess) {
                                runCatching {
                                    JSONObject(countRes.body)
                                        .optJSONArray("result")?.optJSONObject(0)
                                        ?.optJSONArray("results")?.optJSONObject(0)
                                        ?.optInt("cnt", 0) ?: 0
                                }.getOrDefault(0)
                            } else 0
                            tables += ServerDbTableInfo(
                                sourceName = "Cloudflare D1 테이블",
                                tableOrEndpoint = "d1://$tName",
                                rowCount = rowCnt,
                                httpStatusCode = countRes.statusCode,
                                sampleColumns = listOf(tName),
                                statusDetail = "D1 실테이블 ${rowCnt}행 확인"
                            )
                        }
                    }
                }
            }
            stepLogs += AdminPipelineStepLog(
                stepIndex = stepLogs.size + 1,
                title = "Cloudflare D1 DB 테이블 탐색",
                endpoint = "D1:${config.cfD1DatabaseId.take(8)}…",
                status = if (schemaRes.isSuccess) AdminPipelineStageStatus.SUCCESS else AdminPipelineStageStatus.ERROR,
                httpCode = schemaRes.statusCode,
                latencyMs = schemaRes.latencyMs,
                detail = if (schemaRes.isSuccess) "D1 테이블 ${d1Tables.size}개 발견 (${d1Tables.joinToString()})" else formatHttpFailure("D1 조회", schemaRes)
            )
        }

        // Apply optional client-side query / category filtering
        val filteredItems = discoveredByKey.values.filter { item ->
            val catOk = categoryFilter.isBlank() ||
                item.propertyType.code.equals(categoryFilter, ignoreCase = true) ||
                item.propertyType.blogCategory.contains(categoryFilter, ignoreCase = true)
            val qOk = cleanQuery.isBlank() ||
                item.caseId.contains(cleanQuery, ignoreCase = true) ||
                item.address.contains(cleanQuery, ignoreCase = true) ||
                item.courtName.contains(cleanQuery, ignoreCase = true) ||
                item.dedupKey.contains(cleanQuery, ignoreCase = true)
            catOk && qOk
        }

        val anyFeedOk = assetsFeedRes.isSuccess || analysisRes.isSuccess || d1Tables.isNotEmpty()
        val summary = when {
            !anyFeedOk && token.isBlank() -> "CRAWLER_SYNC_TOKEN 미입력 또는 인증 필요 — 관리자 키 입력 후 DB 조회를 실행해 주세요."
            !anyFeedOk -> "서버 DB 접근 실패 (자산 HTTP ${assetsFeedRes.statusCode} · 분석 HTTP ${analysisRes.statusCode}) — 토큰·엔드포인트 확인 필요"
            filteredItems.isEmpty() -> "DB 연결 성공 (테이블 ${tables.size}개) · 조건에 맞는 레코드 0건"
            else -> "서버 DB 탐색 완료: 테이블/피드 ${tables.size}개 · 실데이터 ${filteredItems.size}건 확보"
        }

        return ServerDbDiscoveryResult(
            tables = tables,
            items = filteredItems,
            d1TableNames = d1Tables,
            triggerStatus = triggerSnapshot,
            stepLogs = stepLogs,
            summaryMessage = summary
        )
    }

    /**
     * 딱찰 크롤링 엔진(POST /crawl-and-sync + POST /triggers/run)을 실제 가동하고
     * 신규 크롤링된 매물을 DB에서 회수한다.
     */
    suspend fun runTodakCrawlEngineWithSteps(
        config: ServerAutomationConfig,
        regions: List<String>,
        categoryFilter: String = "",
        limit: Int = 5
    ): TodakCrawlEngineRunResult {
        val apiBase = config.ddakchalApiBase.trim().removeSuffix("/")
        val token = config.crawlerSyncToken.trim()
        val recentCorpus = configStore.getRecent7DayCorpus()
        val stepLogs = mutableListOf<AdminPipelineStepLog>()
        val itemsByKey = linkedMapOf<String, ServerDbDiscoveredItem>()

        // Step A: POST /api/v1/blog/crawl-and-sync (Live Crawler Execution)
        val crawlRes = api.triggerCrawlAndSync(
            apiBase = apiBase,
            syncToken = token,
            regions = regions,
            runCrawl = true,
            maxBatch = 15,
            limit = limit.coerceIn(1, 20),
            enableExternalLinkNaver = config.enableExternalLinkNaver
        )
        val directCrawledSlots = if (crawlRes.isSuccess && crawlRes.body.isNotBlank()) {
            runCatching {
                parseAuctionItemsFromJson(
                    rawJson = crawlRes.body,
                    config = config,
                    recentCorpus = recentCorpus,
                    sourceLabel = "실서버 /crawl-and-sync",
                    allowDummyFallback = false
                )
            }.getOrDefault(emptyList())
        } else emptyList()

        directCrawledSlots.forEach { slot ->
            val key = slot.auctionCase.dedupKey.ifBlank { slot.auctionCase.caseId }
            itemsByKey[key] = ServerDbDiscoveredItem(
                recordId = slot.postId,
                dedupKey = key,
                caseId = slot.auctionCase.caseId,
                courtName = slot.auctionCase.courtName,
                address = slot.auctionCase.address,
                propertyType = slot.auctionCase.propertyType,
                appraisalKrw = slot.auctionCase.appraisalPriceKrw,
                minBidKrw = slot.auctionCase.minBidPriceKrw,
                photoCount = slot.courtPhotoUrls.count { it.isNotBlank() },
                visitCount = slot.visitRecords.size,
                hasAnalysisData = slot.ddakchalAnalysisData != null,
                sourceOrigin = "엔진 /crawl-and-sync",
                updatedAt = slot.serverUpdatedAt.ifBlank { slot.publishDate },
                slot = slot
            )
        }
        stepLogs += AdminPipelineStepLog(
            stepIndex = 1,
            title = "딱찰 크롤링 엔진 실행 (${regions.joinToString().ifBlank { "전국" }})",
            endpoint = "/api/v1/blog/crawl-and-sync",
            status = when {
                crawlRes.isSuccess -> AdminPipelineStageStatus.SUCCESS
                else -> AdminPipelineStageStatus.ERROR
            },
            httpCode = crawlRes.statusCode,
            latencyMs = crawlRes.latencyMs,
            detail = if (crawlRes.isSuccess) {
                "크롤러 가동 성공 (HTTP ${crawlRes.statusCode}) · 즉시 반환 ${directCrawledSlots.size}건"
            } else {
                formatHttpFailure("크롤링 엔진", crawlRes)
            }
        )

        // Step B: POST /api/v1/blog/triggers/run (Process Outbox Triggers into Analyzed Snapshots)
        val triggerRunRes = api.runTriggers(
            apiBase = apiBase,
            syncToken = token,
            maxBatch = 15,
            autoFillDailyQueue = true,
            enableExternalLinkNaver = config.enableExternalLinkNaver
        )
        stepLogs += AdminPipelineStepLog(
            stepIndex = 2,
            title = "아웃박스 트리거·AI 스냅샷 생성",
            endpoint = "/api/v1/blog/triggers/run",
            status = if (triggerRunRes.isSuccess) AdminPipelineStageStatus.SUCCESS else AdminPipelineStageStatus.WARN,
            httpCode = triggerRunRes.statusCode,
            latencyMs = triggerRunRes.latencyMs,
            detail = if (triggerRunRes.isSuccess) {
                "트리거 배치 처리 완료 (HTTP ${triggerRunRes.statusCode})"
            } else {
                formatHttpFailure("트리거 처리", triggerRunRes)
            }
        )

        // Step C: Fetch fresh records from /analysis-feed & /assets/feed after crawl
        val feedOutcome = syncAnalysisFeedWithTelemetry(
            config = config,
            limit = limit.coerceAtLeast(10),
            category = categoryFilter.ifBlank { null }
        )
        val feedSlots = feedOutcome.data.orEmpty()
        feedSlots.forEach { slot ->
            val key = slot.auctionCase.dedupKey.ifBlank { slot.auctionCase.caseId }
            if (!itemsByKey.containsKey(key)) {
                itemsByKey[key] = ServerDbDiscoveredItem(
                    recordId = slot.postId,
                    dedupKey = key,
                    caseId = slot.auctionCase.caseId,
                    courtName = slot.auctionCase.courtName,
                    address = slot.auctionCase.address,
                    propertyType = slot.auctionCase.propertyType,
                    appraisalKrw = slot.auctionCase.appraisalPriceKrw,
                    minBidKrw = slot.auctionCase.minBidPriceKrw,
                    photoCount = slot.courtPhotoUrls.count { it.isNotBlank() },
                    visitCount = slot.visitRecords.size,
                    hasAnalysisData = slot.ddakchalAnalysisData != null,
                    sourceOrigin = "신규 /analysis-feed",
                    updatedAt = slot.serverUpdatedAt.ifBlank { slot.publishDate },
                    slot = slot
                )
            }
        }
        stepLogs += AdminPipelineStepLog(
            stepIndex = 3,
            title = "크롤링 후 최신 분석 피드 수거",
            endpoint = "/api/v1/blog/analysis-feed",
            status = when {
                feedOutcome.isSuccess && feedSlots.isNotEmpty() -> AdminPipelineStageStatus.SUCCESS
                feedOutcome.isSuccess -> AdminPipelineStageStatus.WARN
                else -> AdminPipelineStageStatus.ERROR
            },
            httpCode = feedOutcome.httpStatusCode,
            latencyMs = feedOutcome.latencyMs,
            detail = if (feedOutcome.isSuccess) {
                "최신 분석 피드 ${feedSlots.size}건 수거 완료"
            } else {
                feedOutcome.errorMessage ?: "피드 수거 실패"
            }
        )

        val triggerSnapshot = fetchTriggerStatusWithTelemetry(config).data
        val anyOk = crawlRes.isSuccess || feedOutcome.isSuccess
        val totalItems = itemsByKey.values.toList()
        val summary = if (anyOk) {
            "크롤링 엔진 완료: 신규·분석 매물 ${totalItems.size}건 확보 (${crawlRes.latencyMs}ms)"
        } else {
            "크롤링 엔진 실행 실패: ${formatHttpFailure("crawl-and-sync", crawlRes)}"
        }

        return TodakCrawlEngineRunResult(
            isSuccess = anyOk,
            items = totalItems,
            triggerStatus = triggerSnapshot,
            stepLogs = stepLogs,
            httpStatusCode = if (crawlRes.statusCode > 0) crawlRes.statusCode else feedOutcome.httpStatusCode,
            latencyMs = crawlRes.latencyMs + triggerRunRes.latencyMs + feedOutcome.latencyMs,
            summaryMessage = summary
        )
    }

    fun normalizeCaseNumber(rawInput: String): String {
        val compact = rawInput.trim().replace(Regex("\\s+"), "")
        val match = Regex("""(\d{4}타경\d+)""").find(compact)
        return match?.groupValues?.getOrNull(1) ?: compact
    }

    /**
     * 사건번호(예: 2025타경6297) 입력 시 딱찰 서버에서 경매 사진, 등기부(갑구·을구),
     * 법원 문서(매각물건명세서·현황조사서·감정평가서), 방문기록을 실시간 조회한다.
     * 더미 데이터 폴백 없음 — 401/403/404 또는 0건 시 정확한 HTTP 오류를 그대로 반환한다.
     */
    suspend fun lookupCourtCaseFromTodakServer(
        config: ServerAutomationConfig,
        rawCaseInput: String
    ): TodakCaseLookupResult {
        val normalizedCase = normalizeCaseNumber(rawCaseInput)
        if (normalizedCase.isBlank()) {
            return TodakCaseLookupResult(
                isSuccess = false,
                normalizedCaseId = "",
                message = "법원 사건번호를 입력해 주세요. (예: 2025타경6297)"
            )
        }
        val apiBase = config.ddakchalApiBase.trim().removeSuffix("/")
        val token = config.crawlerSyncToken.trim()
        val recentCorpus = configStore.getRecent7DayCorpus()
        val stepLogs = mutableListOf<AdminPipelineStepLog>()

        // 1) GET /api/v1/blog/assets/{caseNo}?refresh=true
        val byKeyRes = api.fetchAssetByKey(
            apiBase = apiBase,
            syncToken = token,
            keyOrCaseNo = normalizedCase,
            refresh = true,
            enableExternalLinkNaver = config.enableExternalLinkNaver
        )
        var primarySlot: GeneratedPostSlot? = if (byKeyRes.isSuccess && byKeyRes.body.isNotBlank()) {
            runCatching {
                parseSingleAssetResponseToSlot(
                    rawJson = byKeyRes.body,
                    config = config,
                    recentCorpus = recentCorpus,
                    sourceLabel = "딱찰 사건조회 /assets/$normalizedCase"
                )
            }.getOrNull()
        } else null

        stepLogs += AdminPipelineStepLog(
            stepIndex = 1,
            title = "사건번호 단건 조회 (/assets/$normalizedCase)",
            endpoint = "/api/v1/blog/assets/$normalizedCase?refresh=true",
            status = when {
                byKeyRes.isSuccess && primarySlot != null -> AdminPipelineStageStatus.SUCCESS
                byKeyRes.isSuccess -> AdminPipelineStageStatus.WARN
                else -> AdminPipelineStageStatus.ERROR
            },
            httpCode = byKeyRes.statusCode,
            latencyMs = byKeyRes.latencyMs,
            detail = if (primarySlot != null) {
                "사진 ${primarySlot.courtPhotoUrls.size}건 · 방문 ${primarySlot.visitRecords.size}건 · 문건 ${primarySlot.serverCourtDocuments.size}종 · 등기 ${primarySlot.serverRegistryGapGu.size + primarySlot.serverRegistryEulGu.size}건 수신"
            } else {
                formatHttpFailure("단건 조회", byKeyRes)
            }
        )

        var lastHttpCode = byKeyRes.statusCode
        var totalLatency = byKeyRes.latencyMs

        // 2) Complementary or fallback query via /api/v1/blog/assets/feed?q={caseNo}&flush=true
        val needsFeedFallback = primarySlot == null ||
            primarySlot.courtPhotoUrls.isEmpty() ||
            primarySlot.visitRecords.isEmpty() ||
            primarySlot.serverCourtDocuments.none { it.docKind == CourtDocKind.STATUS_SURVEY }

        var feedMatchedSlot: GeneratedPostSlot? = null
        if (needsFeedFallback) {
            val feedRes = api.fetchAssetsFeed(
                apiBase = apiBase,
                syncToken = token,
                limit = 20,
                flush = true,
                query = normalizedCase
            )
            totalLatency += feedRes.latencyMs
            if (lastHttpCode !in 200..299 && feedRes.statusCode > 0) {
                lastHttpCode = feedRes.statusCode
            }
            if (feedRes.isSuccess && feedRes.body.isNotBlank()) {
                val list = runCatching {
                    parseAuctionItemsFromJson(
                        rawJson = feedRes.body,
                        config = config,
                        recentCorpus = recentCorpus,
                        sourceLabel = "딱찰 사건피드 /assets/feed",
                        allowDummyFallback = false
                    )
                }.getOrDefault(emptyList())
                feedMatchedSlot = list.firstOrNull { candidate ->
                    normalizeCaseNumber(candidate.auctionCase.caseId).equals(normalizedCase, ignoreCase = true) ||
                        candidate.auctionCase.dedupKey.contains(normalizedCase, ignoreCase = true)
                }
            }
            stepLogs += AdminPipelineStepLog(
                stepIndex = stepLogs.size + 1,
                title = "자산 피드 교차 조회 (/assets/feed?q=$normalizedCase)",
                endpoint = "/api/v1/blog/assets/feed",
                status = when {
                    feedRes.isSuccess && feedMatchedSlot != null -> AdminPipelineStageStatus.SUCCESS
                    feedRes.isSuccess -> AdminPipelineStageStatus.WARN
                    else -> AdminPipelineStageStatus.ERROR
                },
                httpCode = feedRes.statusCode,
                latencyMs = feedRes.latencyMs,
                detail = if (feedMatchedSlot != null) {
                    "피드 매칭 성공 (사진 ${feedMatchedSlot.courtPhotoUrls.size}건 · 방문 ${feedMatchedSlot.visitRecords.size}건)"
                } else {
                    formatHttpFailure("자산 피드 검색", feedRes)
                }
            )
        }

        // 3) Secondary fallback on /api/v1/blog/analysis-feed
        var analysisMatchedSlot: GeneratedPostSlot? = null
        if (primarySlot == null && feedMatchedSlot == null) {
            val analysisRes = api.fetchAnalysisFeed(
                apiBase = apiBase,
                syncToken = token,
                limit = 20,
                enableExternalLinkNaver = config.enableExternalLinkNaver
            )
            totalLatency += analysisRes.latencyMs
            if (lastHttpCode !in 200..299 && analysisRes.statusCode > 0) {
                lastHttpCode = analysisRes.statusCode
            }
            if (analysisRes.isSuccess && analysisRes.body.isNotBlank()) {
                val list = runCatching {
                    parseAnalysisFeedJson(analysisRes.body, config, recentCorpus)
                }.getOrDefault(emptyList())
                analysisMatchedSlot = list.firstOrNull { candidate ->
                    normalizeCaseNumber(candidate.auctionCase.caseId).equals(normalizedCase, ignoreCase = true) ||
                        candidate.auctionCase.dedupKey.contains(normalizedCase, ignoreCase = true)
                }
            }
            stepLogs += AdminPipelineStepLog(
                stepIndex = stepLogs.size + 1,
                title = "분석 피드 조회 (/analysis-feed)",
                endpoint = "/api/v1/blog/analysis-feed",
                status = when {
                    analysisRes.isSuccess && analysisMatchedSlot != null -> AdminPipelineStageStatus.SUCCESS
                    analysisRes.isSuccess -> AdminPipelineStageStatus.WARN
                    else -> AdminPipelineStageStatus.ERROR
                },
                httpCode = analysisRes.statusCode,
                latencyMs = analysisRes.latencyMs,
                detail = if (analysisMatchedSlot != null) {
                    "분석 피드 사건 매칭 완료"
                } else {
                    formatHttpFailure("분석 피드 검색", analysisRes)
                }
            )
        }

        val mergedSlot = mergeLookupSlots(primarySlot, feedMatchedSlot, analysisMatchedSlot)
        if (mergedSlot == null) {
            val errMsg = when {
                lastHttpCode == 401 || lastHttpCode == 403 ->
                    "딱찰 서버 인증 실패 (HTTP $lastHttpCode) · CRAWLER_SYNC_TOKEN 확인 필요"
                lastHttpCode == 404 ->
                    "딱찰 서버에서 사건번호 '$normalizedCase' 데이터를 찾을 수 없습니다 (HTTP 404)"
                lastHttpCode in 200..299 ->
                    "딱찰 서버 응답(HTTP $lastHttpCode)에 '$normalizedCase' 일치 사건이 없습니다"
                else ->
                    "딱찰 서버 조회 실패 (HTTP $lastHttpCode) · 네트워크 및 서버 설정을 확인하세요"
            }
            return TodakCaseLookupResult(
                isSuccess = false,
                normalizedCaseId = normalizedCase,
                slot = null,
                stepLogs = stepLogs,
                httpStatusCode = lastHttpCode,
                latencyMs = totalLatency,
                message = errMsg
            )
        }

        val finalStatus = if (byKeyRes.isSuccess) byKeyRes.statusCode else lastHttpCode
        val regCount = mergedSlot.serverRegistryGapGu.size + mergedSlot.serverRegistryEulGu.size
        val docCount = mergedSlot.serverCourtDocuments.size
        val summary = "딱찰 서버 '${mergedSlot.auctionCase.caseId}' 조회 완료: 경매사진 ${mergedSlot.courtPhotoUrls.size}건 · 공식문건 ${docCount}종 · 방문기록 ${mergedSlot.visitRecords.size}건 · 등기 ${regCount}건"
        return TodakCaseLookupResult(
            isSuccess = true,
            normalizedCaseId = mergedSlot.auctionCase.caseId,
            slot = mergedSlot,
            stepLogs = stepLogs,
            httpStatusCode = finalStatus,
            latencyMs = totalLatency,
            message = summary
        )
    }

    private fun mergeLookupSlots(
        primary: GeneratedPostSlot?,
        feed: GeneratedPostSlot?,
        analysis: GeneratedPostSlot?
    ): GeneratedPostSlot? {
        val base = primary ?: feed ?: analysis ?: return null
        val extraPhotos = (listOfNotNull(primary, feed, analysis).flatMap { it.courtPhotoUrls }).distinct()
        val extraVisits = (listOfNotNull(primary, feed, analysis).flatMap { it.visitRecords }).distinctBy { it.visitId }
        val gapGu = primary?.serverRegistryGapGu?.takeIf { it.isNotEmpty() }
            ?: feed?.serverRegistryGapGu?.takeIf { it.isNotEmpty() }
            ?: analysis?.serverRegistryGapGu.orEmpty()
        val eulGu = primary?.serverRegistryEulGu?.takeIf { it.isNotEmpty() }
            ?: feed?.serverRegistryEulGu?.takeIf { it.isNotEmpty() }
            ?: analysis?.serverRegistryEulGu.orEmpty()
        val docs = primary?.serverCourtDocuments?.takeIf { it.isNotEmpty() }
            ?: feed?.serverCourtDocuments?.takeIf { it.isNotEmpty() }
            ?: analysis?.serverCourtDocuments.orEmpty()
        val analysisData = primary?.ddakchalAnalysisData
            ?: analysis?.ddakchalAnalysisData
            ?: feed?.ddakchalAnalysisData
        return base.copy(
            courtPhotoUrls = extraPhotos,
            visitRecords = extraVisits,
            serverRegistryGapGu = gapGu,
            serverRegistryEulGu = eulGu,
            serverCourtDocuments = docs,
            ddakchalAnalysisData = analysisData
        )
    }

    private fun parseSingleAssetResponseToSlot(
        rawJson: String,
        config: ServerAutomationConfig,
        recentCorpus: List<String>,
        sourceLabel: String
    ): GeneratedPostSlot? {
        val root = JSONObject(rawJson)
        val dataObj = root.optJSONObject("data")
        val candidatesArr = dataObj?.optJSONArray("items") ?: root.optJSONArray("items")
        val itemObj = when {
            dataObj?.optJSONObject("item") != null -> dataObj.optJSONObject("item")
            dataObj?.optJSONObject("asset") != null -> dataObj.optJSONObject("asset")
            root.optJSONObject("item") != null -> root.optJSONObject("item")
            root.optJSONObject("asset") != null -> root.optJSONObject("asset")
            candidatesArr != null && candidatesArr.length() > 0 -> candidatesArr.optJSONObject(0)
            dataObj != null && (dataObj.has("case_no") || dataObj.has("caseNo") || dataObj.has("auction_dedup_key")) -> dataObj
            root.has("case_no") || root.has("caseNo") || root.has("auction_dedup_key") -> root
            else -> null
        } ?: return null
        return parseSingleAuctionItem(itemObj, 0, config, recentCorpus, sourceLabel)
    }

    suspend fun reverseSyncQueueStatus(
        config: ServerAutomationConfig,
        itemId: String,
        status: String,
        blogId: String? = null,
        blogPostUrl: String? = null,
        lastError: String? = null
    ): DdakchalSyncOutcome<String> {
        if (config.ddakchalApiBase.isBlank() || config.crawlerSyncToken.isBlank() || itemId.isBlank()) {
            return DdakchalSyncOutcome(
                isSuccess = false,
                errorMessage = "역동기화 생략 (API 주소 또는 동기화 토큰 미설정)"
            )
        }
        val res = api.updateQueueItemStatus(
            apiBase = config.ddakchalApiBase,
            syncToken = config.crawlerSyncToken,
            itemId = itemId,
            status = status,
            blogId = blogId,
            blogPostUrl = blogPostUrl,
            lastError = lastError
        )
        return DdakchalSyncOutcome(
            isSuccess = res.isSuccess,
            data = if (res.isSuccess) "큐 상태($status) 역동기화 완료" else null,
            latencyMs = res.latencyMs,
            httpStatusCode = res.statusCode,
            errorMessage = if (res.isSuccess) null else formatHttpFailure("역동기화", res)
        )
    }

    /**
     * 딱찰 운영 서버(https://ddakchal.pages.dev)와 Cloudflare D1/Worker 연동 상태를
     * 6개 항목으로 전수 실측 점검한다.
     */
    suspend fun runComprehensiveDdakchalDiagnostic(
        config: ServerAutomationConfig,
        sampleCaseId: String = ""
    ): DdakchalFullDiagnosticReport {
        val nowText = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())
        val apiBase = config.ddakchalApiBase.trim().removeSuffix("/")
        val token = config.crawlerSyncToken.trim()
        val hasValidBase = apiBase.startsWith("https://")
        val hasToken = token.isNotBlank() && !token.contains("placeholder", ignoreCase = true)

        val checks = mutableListOf<DdakchalEndpointCheckItem>()

        // 1. Base URL HTTPS & Dual Auth Headers
        checks += DdakchalEndpointCheckItem(
            id = "auth_config",
            name = "1. HTTPS · 이중 인증 헤더",
            endpointPath = apiBase.ifBlank { "(미입력)" },
            method = "CONFIG",
            passed = hasValidBase && hasToken,
            httpStatusCode = if (hasValidBase && hasToken) 200 else 0,
            latencyMs = 0L,
            detailMessage = when {
                !hasValidBase -> "DDAKCHAL_API_BASE가 https:// 주소가 아닙니다."
                !hasToken -> "CRAWLER_SYNC_TOKEN 미설정 (Authorization: Bearer + X-Crawler-Sync-Token 필요)"
                else -> "Bearer + X-Crawler-Sync-Token 마스킹(${ServerConfigStore.maskSecret(token)}) 준비 완료"
            },
            checkedSpec = "HTTPS + Dual Auth"
        )

        if (!hasValidBase) {
            return DdakchalFullDiagnosticReport(
                overallConnected = false,
                overallScorePct = 0,
                healthGradeLabel = "기본 URL 미설정",
                apiBase = apiBase,
                authHeaderReady = false,
                checks = checks,
                recommendation = "보안·CF 탭에서 DDAKCHAL_API_BASE(https://ddakchal.pages.dev)를 먼저 저장해 주세요.",
                checkedAt = nowText
            )
        }

        // 2. GET /api/v1/blog/triggers/status
        val triggerRes = api.fetchTriggerStatus(apiBase, token)
        val triggerParsed = if (triggerRes.isSuccess && triggerRes.body.isNotBlank()) {
            runCatching { parseTriggerStatusJson(triggerRes.body) }.getOrNull()
        } else null
        checks += DdakchalEndpointCheckItem(
            id = "triggers_status",
            name = "2. 트리거·스냅샷 큐 상태",
            endpointPath = "/api/v1/blog/triggers/status",
            method = "GET",
            passed = triggerRes.isSuccess && triggerParsed != null,
            httpStatusCode = triggerRes.statusCode,
            latencyMs = triggerRes.latencyMs,
            detailMessage = if (triggerRes.isSuccess && triggerParsed != null) {
                "대기 ${triggerParsed.pendingCount} · 처리 ${triggerParsed.processedCount} · 스냅샷 ${triggerParsed.snapshotsCount} · 금일큐 ${triggerParsed.todayQueueCount}"
            } else {
                formatHttpFailure("트리거 상태", triggerRes)
            },
            checkedSpec = "D1 Outbox Triggers"
        )

        // 3. GET /api/v1/blog/analysis-feed?limit=5&enableExternalLinkNaver=false
        val feedRes = api.fetchAnalysisFeed(
            apiBase = apiBase,
            syncToken = token,
            limit = 5,
            enableExternalLinkNaver = config.enableExternalLinkNaver
        )
        var discoveredCaseKey: String? = null
        var feedCount = 0
        var analysisDataCount = 0
        var naverUrlLeakCount = 0
        if (feedRes.isSuccess && feedRes.body.isNotBlank()) {
            runCatching {
                val root = JSONObject(feedRes.body)
                val dataObj = root.optJSONObject("data") ?: root
                val items = dataObj.optJSONArray("items") ?: root.optJSONArray("items") ?: JSONArray()
                feedCount = items.length()
                val urlRegex = Regex("""https?://[^\s"'<>]+""", RegexOption.IGNORE_CASE)
                for (i in 0 until items.length()) {
                    val item = items.optJSONObject(i) ?: continue
                    if (discoveredCaseKey == null) {
                        discoveredCaseKey = item.optString("auction_dedup_key")
                            .ifBlank { item.optString("case_no") }
                            .ifBlank { null }
                    }
                    if (item.has("blog_analysis_data")) analysisDataCount++
                    if (!config.enableExternalLinkNaver) {
                        val nv = item.optJSONObject("channelVariants")?.optJSONObject("naver")
                        val bodyTxt = nv?.optString("bodyText").orEmpty()
                        val bodyHtml = nv?.optString("bodyHtml").orEmpty()
                        if (urlRegex.containsMatchIn(bodyTxt) || urlRegex.containsMatchIn(bodyHtml)) {
                            naverUrlLeakCount++
                        }
                    }
                }
            }
        }
        val feedPassed = feedRes.isSuccess && feedRes.body.isNotBlank() && (config.enableExternalLinkNaver || naverUrlLeakCount == 0)
        checks += DdakchalEndpointCheckItem(
            id = "analysis_feed",
            name = "3. 증분 분석 피드 & 네이버 외부URL 차단",
            endpointPath = "/api/v1/blog/analysis-feed?enableExternalLinkNaver=${config.enableExternalLinkNaver}",
            method = "GET",
            passed = feedPassed,
            httpStatusCode = feedRes.statusCode,
            latencyMs = feedRes.latencyMs,
            detailMessage = if (feedRes.isSuccess) {
                buildString {
                    append("실데이터 ${feedCount}건 · blog_analysis_data ${analysisDataCount}건")
                    if (!config.enableExternalLinkNaver) {
                        append(if (naverUrlLeakCount == 0) " · 네이버 외부URL 0건(안전)" else " · 경고: 네이버 외부URL ${naverUrlLeakCount}건 포함")
                    }
                }
            } else {
                formatHttpFailure("분석 피드", feedRes)
            },
            checkedSpec = "Incremental Feed + Low-Quality Guard"
        )

        // 4. GET /api/v1/blog/syndication-pack?limit=3 (strict, no dummy fallback)
        val packRes = api.fetchSyndicationPack(
            apiBase = apiBase,
            syncToken = token,
            limit = 3,
            enableExternalLinkNaver = config.enableExternalLinkNaver
        )
        var packItemCount = 0
        var variantsCompleteCount = 0
        if (packRes.isSuccess && packRes.body.isNotBlank()) {
            runCatching {
                val root = JSONObject(packRes.body)
                val dataObj = root.optJSONObject("data") ?: root
                val items = dataObj.optJSONArray("items") ?: root.optJSONArray("items") ?: JSONArray()
                packItemCount = items.length()
                for (i in 0 until items.length()) {
                    val item = items.optJSONObject(i) ?: continue
                    if (discoveredCaseKey == null) {
                        discoveredCaseKey = item.optString("auction_dedup_key")
                            .ifBlank { item.optString("case_no") }
                            .ifBlank { null }
                    }
                    val cv = item.optJSONObject("channelVariants")
                    if (cv != null && cv.has("naver") && cv.has("tistory") && cv.has("cloudflare")) {
                        variantsCompleteCount++
                    }
                }
            }
        }
        checks += DdakchalEndpointCheckItem(
            id = "syndication_pack",
            name = "4. 3채널 신디케이션 팩 정합성",
            endpointPath = "/api/v1/blog/syndication-pack?limit=3",
            method = "GET",
            passed = packRes.isSuccess && packItemCount > 0,
            httpStatusCode = packRes.statusCode,
            latencyMs = packRes.latencyMs,
            detailMessage = if (packRes.isSuccess) {
                "팩 ${packItemCount}건 수신 · 3채널 변형 완비 ${variantsCompleteCount}건"
            } else {
                formatHttpFailure("신디케이션 팩", packRes)
            },
            checkedSpec = "3-Channel ChannelVariants Parity"
        )

        // 5. GET /api/v1/blog/assets/{caseNo}
        val lookupKey = discoveredCaseKey ?: sampleCaseId.trim()
        if (lookupKey.isBlank()) {
            checks += DdakchalEndpointCheckItem(
                id = "single_asset",
                name = "5. 단건 최신 자산 1:1 매칭",
                endpointPath = "/api/v1/blog/assets/{caseNo}",
                method = "GET",
                passed = false,
                httpStatusCode = 0,
                latencyMs = 0L,
                detailMessage = "실서버 피드에 조회 가능한 실사건이 없어 단건 검증을 건너뛰었습니다.",
                checkedSpec = "1:1 Asset Lookup"
            )
        } else {
            val assetRes = api.fetchAssetByKey(
                apiBase = apiBase,
                syncToken = token,
                keyOrCaseNo = lookupKey,
                refresh = false,
                enableExternalLinkNaver = config.enableExternalLinkNaver
            )
            val assetParsedOk = assetRes.isSuccess && assetRes.body.isNotBlank() && runCatching {
                val recentCorpus = configStore.getRecent7DayCorpus()
                parseSingleAssetJson(assetRes.body, config, recentCorpus) != null
            }.getOrDefault(false)
            checks += DdakchalEndpointCheckItem(
                id = "single_asset",
                name = "5. 단건 최신 자산 1:1 매칭",
                endpointPath = "/api/v1/blog/assets/$lookupKey",
                method = "GET",
                passed = assetParsedOk,
                httpStatusCode = assetRes.statusCode,
                latencyMs = assetRes.latencyMs,
                detailMessage = if (assetParsedOk) {
                    "사건/키 '$lookupKey' 단건 자산·수치 매칭 검증 완료"
                } else if (assetRes.isSuccess) {
                    "응답 수신되었으나 자산 스키마(case_no/appraisal_krw) 누락"
                } else {
                    formatHttpFailure("단건 자산($lookupKey)", assetRes)
                },
                checkedSpec = "1:1 Asset Lookup"
            )
        }

        // 6. Reverse Sync Ready + Optional Cloudflare D1 & Worker Cron
        val cfConfigured = config.cfAccountId.isNotBlank() && config.cfApiToken.isNotBlank()
        val cfCheck = if (cfConfigured) verifyCloudflareAndCron(config) else null
        val cfAndSyncOk = hasToken && (!cfConfigured || cfCheck?.success == true)
        checks += DdakchalEndpointCheckItem(
            id = "reverse_sync_cf",
            name = "6. 역동기화 PUT & Cloudflare D1",
            endpointPath = "/api/v1/blog/queue/{id} + CF D1",
            method = "PUT/CF",
            passed = cfAndSyncOk,
            optional = !cfConfigured,
            httpStatusCode = if (cfAndSyncOk) 200 else 0,
            latencyMs = 0L,
            detailMessage = buildString {
                append(if (hasToken) "발행 역동기화 토큰 준비됨" else "역동기화 토큰 미설정")
                append(" · ")
                if (cfConfigured && cfCheck != null) {
                    append(cfCheck.message)
                } else {
                    append("CF D1/Cron 키 미입력(선택)")
                }
            },
            checkedSpec = "Reverse Status Sync & D1"
        )

        val mandatory = checks.filterNot { it.optional }
        val passedMandatory = mandatory.count { it.passed }
        val totalMandatory = mandatory.size.coerceAtLeast(1)
        val bonusOptional = checks.count { it.optional && it.passed }
        val scorePct = (((passedMandatory.toDouble() / totalMandatory.toDouble()) * 90.0) +
            (if (checks.count { it.optional } == 0) 10.0 else bonusOptional * 10.0)).roundToInt().coerceIn(0, 100)

        val authFailed = checks.any { it.httpStatusCode == 401 || it.httpStatusCode == 403 }
        val coreFeedConnected = checks.any { (it.id == "triggers_status" || it.id == "analysis_feed" || it.id == "syndication_pack") && it.passed }

        val gradeLabel = when {
            authFailed -> "토큰 인증 실패 (HTTP 401/403)"
            !hasToken -> "인증 토큰 미입력"
            scorePct >= 90 -> "완벽 연동 (S)"
            scorePct >= 70 -> "핵심 연동 정상 (A)"
            coreFeedConnected -> "일부 엔드포인트 점검 필요 (B)"
            else -> "서버 연결 장애 (점검 필요)"
        }

        val failedNames = checks.filter { !it.passed && !it.optional }.map { it.name }
        val recommendation = when {
            !hasToken -> "보안·CF 탭에서 CRAWLER_SYNC_TOKEN을 입력·저장한 뒤 다시 전수 점검해 주세요."
            authFailed -> "딱찰 서버가 토큰을 거부했습니다(401/403). CRAWLER_SYNC_TOKEN 값을 최신 운영 토큰으로 재저장해 주세요."
            failedNames.isEmpty() -> "딱찰 운영 서버 전 엔드포인트(${checks.count { it.passed }}/${checks.size})가 지연 없이 완벽 연동 중입니다."
            else -> "미통과 항목(${failedNames.joinToString(", ")})을 확인하고 필요 시 크롤러 재실행 또는 D1 설정을 보완해 주세요."
        }

        return DdakchalFullDiagnosticReport(
            overallConnected = coreFeedConnected && !authFailed,
            overallScorePct = scorePct,
            healthGradeLabel = gradeLabel,
            apiBase = apiBase,
            authHeaderReady = hasToken,
            checks = checks,
            recommendation = recommendation,
            checkedAt = nowText
        )
    }

    private fun formatHttpFailure(label: String, res: HttpCallResult): String {
        return when (res.statusCode) {
            401, 403 -> "$label 인증 거부 (HTTP ${res.statusCode}) — CRAWLER_SYNC_TOKEN 확인 필요"
            404 -> "$label 경로 없음 (HTTP 404)"
            in 500..599 -> "$label 서버 오류 (HTTP ${res.statusCode})"
            -1 -> "$label 네트워크 연결 실패 (${res.errorMessage ?: "타임아웃/DNS"})"
            else -> "$label 실패 (${res.errorMessage ?: "HTTP ${res.statusCode}"})"
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
            val enrichedHtml = HtmlExportEngine.renderArticleBodySnippet(slot, variant, config)
            val params = listOf(
                "${slot.postId}-${platform.code.lowercase()}",
                slot.queueId,
                slot.auctionCase.caseId,
                slot.auctionCase.propertyType.code,
                slot.auctionCase.propertyType.blogCategory,
                platform.code,
                variant.title,
                enrichedHtml,
                variant.bodyText,
                JSONObject().apply {
                    put("slug", slot.auctionCase.dedupKey)
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

    fun parseSyndicationPackJson(
        rawJson: String,
        config: ServerAutomationConfig,
        recentCorpus: List<String>,
        sourceLabel: String = "실서버 /syndication-pack"
    ): List<GeneratedPostSlot> = parseAuctionItemsFromJson(
        rawJson = rawJson,
        config = config,
        recentCorpus = recentCorpus,
        sourceLabel = sourceLabel,
        allowDummyFallback = false
    )

    fun parseAnalysisFeedJson(
        rawJson: String,
        config: ServerAutomationConfig,
        recentCorpus: List<String>
    ): List<GeneratedPostSlot> = parseAuctionItemsFromJson(
        rawJson = rawJson,
        config = config,
        recentCorpus = recentCorpus,
        sourceLabel = "실서버 /analysis-feed",
        allowDummyFallback = false
    )

    fun parseSingleAssetJson(
        rawJson: String,
        config: ServerAutomationConfig,
        recentCorpus: List<String>
    ): GeneratedPostSlot? {
        val root = JSONObject(rawJson)
        val dataObj = root.optJSONObject("data") ?: root
        val itemObj = dataObj.optJSONObject("item") ?: dataObj.optJSONObject("asset") ?: dataObj
        if (!itemObj.has("case_no") && !itemObj.has("auction_dedup_key") && !itemObj.has("appraisal_krw")) {
            return null
        }
        return parseSingleAuctionItem(itemObj, 0, config, recentCorpus, "실서버 /assets")
    }

    fun parseTriggerStatusJson(rawJson: String): DdakchalTriggerStatusSnapshot {
        val root = JSONObject(rawJson)
        val dataObj = root.optJSONObject("data") ?: root
        val triggersObj = dataObj.optJSONObject("triggers") ?: dataObj
        val snapshotsObj = dataObj.optJSONObject("snapshots") ?: JSONObject()
        val queueObj = dataObj.optJSONObject("queue") ?: JSONObject()
        val eventsArr = dataObj.optJSONArray("recentEvents")
            ?: dataObj.optJSONArray("events")
            ?: JSONArray()

        val events = (0 until minOf(eventsArr.length(), 10)).mapNotNull { idx ->
            val ev = eventsArr.optJSONObject(idx) ?: return@mapNotNull null
            DdakchalTriggerEventSummary(
                eventId = ev.optString("id", "ev-$idx"),
                caseNo = ev.optString("case_no", ev.optString("dedup_key", "-")),
                eventType = ev.optString("event_type", "SYNC"),
                status = ev.optString("status", "DONE"),
                createdAt = ev.optString("created_at", "")
            )
        }
        return DdakchalTriggerStatusSnapshot(
            pendingCount = triggersObj.optInt("pending", triggersObj.optInt("pending_count", 0)),
            processedCount = triggersObj.optInt("processed", triggersObj.optInt("processed_count", 0)),
            failedCount = triggersObj.optInt("failed", triggersObj.optInt("failed_count", 0)),
            snapshotsCount = snapshotsObj.optInt("count", dataObj.optInt("snapshotsCount", 0)),
            latestSnapshotAt = snapshotsObj.optString("latestAt", dataObj.optString("latestSnapshotAt", "")),
            todayQueueCount = queueObj.optInt("todayCount", dataObj.optInt("todayQueueCount", 0)),
            generatedPostsCount = queueObj.optInt("generatedPostsCount", dataObj.optInt("generatedPostsCount", 0)),
            checkedAt = SimpleDateFormat("HH:mm:ss", Locale.KOREA).format(Date()),
            recentEvents = events
        )
    }

    fun computeQueueAnalytics(
        slots: List<GeneratedPostSlot>,
        publishLogs: List<PublishLogEntry>,
        triggerStatus: DdakchalTriggerStatusSnapshot? = null
    ): QueueAnalyticsSummary {
        val totalSlots = slots.size
        val publishedCount = slots.count { it.status == PostStatus.PUBLISHED }
        val queuedCount = slots.count { it.status == PostStatus.QUEUED }
        val failedOrHandoffCount = slots.count { it.status == PostStatus.FAILED || it.status == PostStatus.HANDOFF }
        val draftCount = slots.count { it.status == PostStatus.DRAFT }

        val decidedCount = (publishedCount + failedOrHandoffCount).coerceAtLeast(1)
        val totalOrOne = totalSlots.coerceAtLeast(1)
        val successRatePct = if (publishedCount + failedOrHandoffCount > 0) {
            ((publishedCount * 100.0) / decidedCount).roundToInt().coerceIn(0, 100)
        } else {
            ((publishedCount * 100.0) / totalOrOne).roundToInt().coerceIn(0, 100)
        }
        val failureRatePct = if (publishedCount + failedOrHandoffCount > 0) {
            ((failedOrHandoffCount * 100.0) / decidedCount).roundToInt().coerceIn(0, 100)
        } else {
            0
        }

        // Daily publish volume grouped by publishDate (latest up to 7 dates)
        val groupedByDate = slots.groupBy { it.publishDate.ifBlank { "미정" } }
        val dailySeries = groupedByDate.entries
            .sortedBy { it.key }
            .takeLast(7)
            .map { (date, daySlots) ->
                DailyPublishVolumePoint(
                    dateLabel = if (date.length >= 10) date.substring(5, 10) else date,
                    publishedCount = daySlots.count { it.status == PostStatus.PUBLISHED },
                    queuedCount = daySlots.count { it.status == PostStatus.QUEUED || it.status == PostStatus.DRAFT },
                    failedCount = daySlots.count { it.status == PostStatus.FAILED || it.status == PostStatus.HANDOFF }
                )
            }

        // Channel-level distribution across Naver / Tistory / Cloudflare combining logs + slot states
        val channelStats = ChannelPlatform.entries.map { platform ->
            val platformLogs = publishLogs.filter { it.platform == platform }
            val logPublishedPosts = platformLogs.filter { it.status == PostStatus.PUBLISHED }.map { it.postId }.toSet()
            val logFailedPosts = platformLogs.filter { it.status == PostStatus.FAILED || it.status == PostStatus.HANDOFF }.map { it.postId }.toSet()

            var chPublished = 0
            var chQueued = 0
            var chFailed = 0
            slots.forEach { slot ->
                when {
                    slot.publishedUrls.containsKey(platform) || slot.status == PostStatus.PUBLISHED || slot.postId in logPublishedPosts -> {
                        chPublished++
                    }
                    slot.postId in logFailedPosts || slot.status == PostStatus.FAILED || slot.status == PostStatus.HANDOFF -> {
                        chFailed++
                    }
                    slot.status == PostStatus.QUEUED || slot.status == PostStatus.DRAFT -> {
                        chQueued++
                    }
                }
            }
            ChannelPublishStat(
                platform = platform,
                publishedCount = chPublished,
                queuedCount = chQueued,
                failedCount = chFailed
            )
        }

        return QueueAnalyticsSummary(
            totalSlots = totalSlots,
            publishedCount = publishedCount,
            queuedCount = queuedCount,
            failedOrHandoffCount = failedOrHandoffCount,
            draftCount = draftCount,
            successRatePct = successRatePct,
            failureRatePct = failureRatePct,
            dailySeries = dailySeries,
            channelStats = channelStats
        )
    }

    suspend fun syncBlogQueueWithRetrofit(
        config: ServerAutomationConfig,
        limit: Int = 100
    ): DdakchalSyncOutcome<QueueSyncResult> {
        val cleanBase = config.ddakchalApiBase.trim().removeSuffix("/")
        if (!cleanBase.startsWith("https://") && !cleanBase.startsWith("http://")) {
            return DdakchalSyncOutcome(
                isSuccess = false,
                httpStatusCode = 0,
                errorMessage = "DDAKCHAL_API_BASE URL이 올바르지 않습니다."
            )
        }
        val endpointUrl = "$cleanBase/api/v1/blog/queue"
        val startNs = System.nanoTime()
        return try {
            val service = retrofitServiceFactory.create(cleanBase, config.crawlerSyncToken)
            val response = kotlinx.coroutines.withContext(api.coroutineDispatcher) {
                service.getBlogQueueCall(limit = limit.coerceIn(1, 200)).execute()
            }
            val latencyMs = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)
            if (!response.isSuccessful) {
                val errBody = runCatching { response.errorBody()?.string().orEmpty().take(160) }.getOrDefault("")
                val msg = when (response.code()) {
                    401, 403 -> "발행큐 인증 실패 (HTTP ${response.code()}) — 설정·보안에서 CRAWLER_SYNC_TOKEN을 입력해 주세요."
                    404 -> "발행큐 엔드포인트 없음 (HTTP 404: $endpointUrl)"
                    in 500..599 -> "딱찰 서버 오류 (HTTP ${response.code()})"
                    else -> "발행큐 조회 실패 (HTTP ${response.code()} ${errBody})"
                }
                return DdakchalSyncOutcome(
                    isSuccess = false,
                    latencyMs = latencyMs,
                    httpStatusCode = response.code(),
                    errorMessage = msg
                )
            }
            val envelope = response.body()
            val rawItems = envelope?.data?.items ?: envelope?.items.orEmpty()
            val mappedItems = rawItems.mapIndexed { idx, dto -> dto.toDomainRecord(idx) }
            val analytics = computeQueueAnalyticsFromQueueItems(mappedItems)
            val syncedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())
            DdakchalSyncOutcome(
                isSuccess = true,
                data = QueueSyncResult(
                    items = mappedItems,
                    analytics = analytics,
                    syncedAt = syncedAt,
                    sourceEndpoint = endpointUrl
                ),
                latencyMs = latencyMs,
                httpStatusCode = response.code()
            )
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            val latencyMs = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)
            DdakchalSyncOutcome(
                isSuccess = false,
                latencyMs = latencyMs,
                httpStatusCode = -1,
                errorMessage = "Retrofit /api/v1/blog/queue 연결 오류: ${e.localizedMessage ?: "네트워크 확인 필요"}"
            )
        }
    }

    fun computeQueueAnalyticsFromQueueItems(items: List<DdakchalQueueItemRecord>): QueueAnalyticsSummary {
        val totalSlots = items.size
        val publishedCount = items.count { it.status == PostStatus.PUBLISHED }
        val queuedCount = items.count { it.status == PostStatus.QUEUED }
        val failedOrHandoffCount = items.count { it.status == PostStatus.FAILED || it.status == PostStatus.HANDOFF }
        val draftCount = items.count { it.status == PostStatus.DRAFT }

        val decidedCount = (publishedCount + failedOrHandoffCount).coerceAtLeast(1)
        val successRatePct = if (publishedCount + failedOrHandoffCount > 0) {
            ((publishedCount * 100.0) / decidedCount).roundToInt().coerceIn(0, 100)
        } else 0
        val failureRatePct = if (publishedCount + failedOrHandoffCount > 0) {
            ((failedOrHandoffCount * 100.0) / decidedCount).roundToInt().coerceIn(0, 100)
        } else 0

        val dailySeries = items
            .groupBy { it.publishDate.ifBlank { "미정" } }
            .entries
            .sortedBy { it.key }
            .takeLast(7)
            .map { (date, dayItems) ->
                DailyPublishVolumePoint(
                    dateLabel = if (date.length >= 10) date.substring(5, 10) else date,
                    publishedCount = dayItems.count { it.status == PostStatus.PUBLISHED },
                    queuedCount = dayItems.count { it.status == PostStatus.QUEUED || it.status == PostStatus.DRAFT },
                    failedCount = dayItems.count { it.status == PostStatus.FAILED || it.status == PostStatus.HANDOFF }
                )
            }

        val channelStats = ChannelPlatform.entries.map { platform ->
            val chItems = items.filter { it.platform == platform }
            ChannelPublishStat(
                platform = platform,
                publishedCount = chItems.count { it.status == PostStatus.PUBLISHED },
                queuedCount = chItems.count { it.status == PostStatus.QUEUED || it.status == PostStatus.DRAFT },
                failedCount = chItems.count { it.status == PostStatus.FAILED || it.status == PostStatus.HANDOFF }
            )
        }

        return QueueAnalyticsSummary(
            totalSlots = totalSlots,
            publishedCount = publishedCount,
            queuedCount = queuedCount,
            failedOrHandoffCount = failedOrHandoffCount,
            draftCount = draftCount,
            successRatePct = successRatePct,
            failureRatePct = failureRatePct,
            dailySeries = dailySeries,
            channelStats = channelStats
        )
    }

    private fun parseAuctionItemsFromJson(
        rawJson: String,
        config: ServerAutomationConfig,
        recentCorpus: List<String>,
        sourceLabel: String,
        @Suppress("UNUSED_PARAMETER") allowDummyFallback: Boolean
    ): List<GeneratedPostSlot> {
        val root = JSONObject(rawJson)
        val dataObj = root.optJSONObject("data") ?: root
        val items = dataObj.optJSONArray("items")
            ?: root.optJSONArray("items")
            ?: return emptyList()

        if (items.length() == 0) {
            return emptyList()
        }

        val result = mutableListOf<GeneratedPostSlot>()
        for (i in 0 until items.length()) {
            val item = items.optJSONObject(i) ?: continue
            parseSingleAuctionItem(item, i, config, recentCorpus, sourceLabel)?.let { result += it }
        }
        return result
    }

    private fun parseSingleAuctionItem(
        item: JSONObject,
        index: Int,
        config: ServerAutomationConfig,
        recentCorpus: List<String>,
        sourceLabel: String
    ): GeneratedPostSlot? {
        val caseNo = item.optString("case_no").ifBlank { item.optString("caseNo") }.trim()
        if (caseNo.isBlank()) return null
        val dedupKey = item.optString("auction_dedup_key").ifBlank { item.optString("dedupKey") }.ifBlank { "ca-$caseNo" }
        val publishDate = item.optString("publish_date").ifBlank {
            item.optString("updated_at").take(10).ifBlank { "2026-11-22" }
        }
        val slotIndex = item.optInt("slot_index", index + 1)
        val channelVariantsJson = item.optJSONObject("channelVariants")
        val nvObj = channelVariantsJson?.optJSONObject("naver")
        val titleText = item.optString("title")

        val explicitPropertyType = item.optString("property_type")
        val naverCategoryCode = nvObj?.optString("categoryCode").orEmpty()
        val rawCategory = item.optString("category")
        val propType = resolvePropertyType(
            explicitPropertyType = explicitPropertyType,
            naverCategoryCode = naverCategoryCode,
            rawCategory = rawCategory,
            title = titleText
        )

        val photoUrls = parseCourtPhotosField(item)
        val visitRecords = parseVisitRecordsField(item, caseNo)
        val (serverGapGu, serverEulGu) = parseRegistryNodesField(item, caseNo)
        val serverCourtDocs = parseCourtDocumentsField(item, caseNo, item.optString("court_name").ifBlank { "대법원 경매계" })
        val analysisData = parseBlogAnalysisData(item, photoUrls.size)

        val appraisalKrw = item.optLong("appraisal_krw", item.optLong("appraisalKrw", 0L))
        val minBidKrw = item.optLong("min_bid_krw", item.optLong("minBidKrw", 0L))
        val computedDiscount = if (appraisalKrw > 0L && minBidKrw > 0L) {
            ((minBidKrw.toDouble() / appraisalKrw.toDouble()) * 100.0).toInt().coerceIn(1, 100)
        } else 0
        val realTradeKrw = item.optLong("real_trade_median_krw", 0L).let {
            if (it > 0L) it else (appraisalKrw * 0.90).toLong()
        }

        val auction = AuctionCaseIntel(
            caseId = caseNo,
            dedupKey = dedupKey,
            courtName = item.optString("court_name").ifBlank { item.optString("courtName") }.ifBlank { "관할 법원 미기재" },
            address = item.optString("address").ifBlank { titleText.ifBlank { "$caseNo 소재지 미기재" } },
            propertyType = propType,
            appraisalPriceKrw = appraisalKrw,
            minBidPriceKrw = minBidKrw,
            winningPriceKrw = null,
            bidCount = item.optInt("bid_count", 1),
            discountRatePct = item.optInt("discount_rate_pct", computedDiscount),
            auctionDate = item.optString("auction_date"),
            baseRightDate = item.optString("base_right_date"),
            dividendDeadline = item.optString("dividend_deadline"),
            claimAmountKrw = item.optLong("claim_amount_krw", (appraisalKrw * 0.48).toLong()),
            depositRatePct = item.optInt("deposit_rate_pct", 10),
            possessionSummary = item.optString("possession_summary").ifBlank { "서버 미제공" },
            tenantSummary = item.optString("tenant_summary").ifBlank { "서버 미제공" },
            assumedRightsText = item.optString("assumed_rights_text").ifBlank { "서버 미제공" },
            specialCheckNotice = propType.focusSummary,
            recentRealTradeMedianKrw = realTradeKrw,
            stats3yRatePct = item.optDouble("stats_3y_rate_pct", 0.0),
            clicksLast7d = item.optInt("clicks_7d", 0),
            conservativeBidKrw = item.optLong("conservative_bid_krw", (minBidKrw * 1.03).toLong()),
            balancedBidKrw = item.optLong("balanced_bid_krw", (minBidKrw * 1.09).toLong()),
            aggressiveBidKrw = item.optLong("aggressive_bid_krw", (minBidKrw * 1.15).toLong())
        )

        val fallbackVariants = DefaultSpecDataFactory.buildDistinct3ChannelVariants(
            auction = auction,
            seedIndex = index,
            config = config,
            serverGapGu = serverGapGu,
            serverEulGu = serverEulGu,
            serverDocuments = serverCourtDocs,
            visitRecords = visitRecords
        )
        val regionTag = auction.address.split(" ").take(2).joinToString(" ").ifBlank { auction.courtName }
        val urlStripRegex = Regex("""https?://[^\s"'<>]+""", RegexOption.IGNORE_CASE)

        val parsedVariants = if (channelVariantsJson != null) {
            ChannelPlatform.entries.associateWith { platform ->
                val key = platform.code.lowercase()
                val vObj = channelVariantsJson.optJSONObject(key)
                val fb = fallbackVariants[platform]!!
                if (vObj == null) fb else {
                    var bodyText = vObj.optString("bodyText").ifBlank { fb.bodyText }
                    var bodyHtml = vObj.optString("bodyHtml").ifBlank { fb.bodyHtml }
                    if (platform == ChannelPlatform.NAVER && !config.enableExternalLinkNaver) {
                        bodyText = bodyText.replace(urlStripRegex, "[프로필 링크 참조]")
                        bodyHtml = bodyHtml.replace(urlStripRegex, "#")
                    }
                    val cardsArr = vObj.optJSONArray("infographicCards")
                    val cards = if (cardsArr != null && cardsArr.length() in 4..7) {
                        (0 until cardsArr.length()).map { cIdx ->
                            val c = cardsArr.getJSONObject(cIdx)
                            val seed = "$caseNo|${platform.code}|$cIdx|${c.optString("headline")}|${photoUrls.getOrNull(cIdx).orEmpty()}"
                            InfographicCardSpec(
                                cardIndex = c.optInt("cardIndex", cIdx + 1),
                                cardType = c.optString("cardType", "CARD_${cIdx + 1}"),
                                aspectRatio = c.optString("aspectRatio", platform.imageResolutionSpec),
                                headline = c.optString("headline").ifBlank { "법원 실사진 및 권리분석 ${cIdx + 1}" },
                                subline = c.optString("subline"),
                                seoFilename = "$caseNo-${platform.code.lowercase()}-${cIdx + 1}.webp",
                                altText = "$caseNo $regionTag ${c.optString("headline")}".trim(),
                                md5Hash = QaVerificationEngine.md5Short(seed),
                                sha256Hash = QaVerificationEngine.sha256Short(seed),
                                exifStripped = true
                            )
                        }
                    } else if (photoUrls.isNotEmpty()) {
                        fb.infographicCards.mapIndexed { cIdx, card ->
                            val photoRef = photoUrls.getOrNull(cIdx % photoUrls.size).orEmpty()
                            val seed = "$caseNo|${platform.code}|$cIdx|$photoRef"
                            card.copy(
                                seoFilename = "$caseNo-${platform.code.lowercase()}-${cIdx + 1}.webp",
                                altText = "$caseNo $regionTag 법원 현장사진 ${cIdx + 1}",
                                md5Hash = QaVerificationEngine.md5Short(seed),
                                sha256Hash = QaVerificationEngine.sha256Short(seed),
                                exifStripped = true
                            )
                        }
                    } else fb.infographicCards

                    val parsedTags = vObj.optJSONArray("tags")?.let { arr ->
                        (0 until arr.length()).mapNotNull { t -> arr.optString(t).takeIf { it.isNotBlank() } }
                    }.orEmpty().ifEmpty { fb.tags }

                    fb.copy(
                        categoryCode = vObj.optString("categoryCode").ifBlank { fb.categoryCode },
                        categoryLabel = vObj.optString("categoryLabel").ifBlank { fb.categoryLabel },
                        title = vObj.optString("title").ifBlank { fb.title },
                        bodyText = bodyText,
                        bodyHtml = bodyHtml,
                        metaDescription = vObj.optString("metaDescription").ifBlank { fb.metaDescription },
                        tags = parsedTags,
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
        val rawStatus = item.optString("status", "QUEUED")
        val normalizedStatus = when (rawStatus.uppercase()) {
            "READY", "QUEUED" -> PostStatus.QUEUED
            "DRAFT_SAVED", "DRAFT" -> PostStatus.DRAFT
            "PUBLISHED" -> PostStatus.PUBLISHED
            "FAILED" -> PostStatus.FAILED
            "HANDOFF" -> PostStatus.HANDOFF
            else -> PostStatus.fromCode(rawStatus)
        }

        return GeneratedPostSlot(
            postId = item.optString("id").ifBlank { "post-$dedupKey" },
            queueId = item.optString("id").ifBlank { "q-$slotIndex" },
            slotIndex = slotIndex,
            publishDate = publishDate,
            auctionCase = auction,
            variants = parsedVariants,
            scheduledTimes = scheduled,
            status = normalizedStatus,
            qaReport = qa,
            sourceLabel = sourceLabel,
            ddakchalAnalysisData = analysisData,
            courtPhotoUrls = photoUrls,
            visitRecords = visitRecords,
            serverUpdatedAt = item.optString("updated_at"),
            serverRegistryGapGu = serverGapGu,
            serverRegistryEulGu = serverEulGu,
            serverCourtDocuments = serverCourtDocs
        )
    }

    private fun resolvePropertyType(
        explicitPropertyType: String,
        naverCategoryCode: String,
        rawCategory: String,
        title: String
    ): PropertyType {
        if (explicitPropertyType.isNotBlank()) {
            return PropertyType.fromRaw(explicitPropertyType)
        }
        if (naverCategoryCode.isNotBlank()) {
            return PropertyType.fromRaw(naverCategoryCode)
        }
        return when (rawCategory.trim().uppercase()) {
            "LAND" -> PropertyType.LAND_FARMLAND
            "FACTORY" -> PropertyType.FACTORY_SPECIAL
            "COMMERCIAL" -> PropertyType.COMMERCIAL_RETAIL
            "OFFICETEL" -> PropertyType.APT_OFFICETEL
            "HOUSING" -> if (title.contains("빌라") || title.contains("다세대") || title.contains("다가구")) {
                PropertyType.VILLA_MULTIFAMILY
            } else {
                PropertyType.APT_OFFICETEL
            }
            else -> PropertyType.fromRaw(title)
        }
    }

    private fun parseCourtPhotosField(item: JSONObject): List<String> {
        val out = linkedSetOf<String>()
        listOf("photo_urls", "court_photos", "photos", "images", "court_images", "appraisal_photos", "survey_photos", "r2_photos", "attachments").forEach { key ->
            val arr = optJsonArrayFlexible(item, key)
            if (arr != null) {
                for (idx in 0 until arr.length()) {
                    when (val entry = arr.opt(idx)) {
                        is String -> entry.trim().takeIf { it.isNotBlank() }?.let { out += it }
                        is JSONObject -> {
                            val url = entry.optString("imageUrl")
                                .ifBlank { entry.optString("url") }
                                .ifBlank { entry.optString("photo_url") }
                                .ifBlank { entry.optString("image_url") }
                                .ifBlank { entry.optString("r2_url") }
                                .ifBlank { entry.optString("src") }
                                .trim()
                            if (url.isNotBlank()) out += url
                        }
                    }
                }
            } else {
                val single = item.optString(key).trim()
                if (single.startsWith("http://") || single.startsWith("https://")) {
                    out += single
                }
            }
        }
        return out.toList()
    }

    private fun optJsonArrayFlexible(item: JSONObject, key: String): JSONArray? {
        val rawVal = item.opt(key) ?: return null
        return when (rawVal) {
            is JSONArray -> rawVal
            is String -> if (rawVal.trim().startsWith("[")) runCatching { JSONArray(rawVal) }.getOrNull() else null
            else -> null
        }
    }

    private fun parseRegistryNodesField(
        item: JSONObject,
        caseNo: String
    ): Pair<List<RegistryVisualNode>, List<RegistryVisualNode>> {
        val gapGuList = mutableListOf<RegistryVisualNode>()
        val eulGuList = mutableListOf<RegistryVisualNode>()

        fun parseArray(arr: JSONArray?, defaultSection: RegistrySection) {
            if (arr == null) return
            for (idx in 0 until arr.length()) {
                val obj = arr.optJSONObject(idx) ?: continue
                val secRaw = obj.optString("section", "").uppercase()
                val section = when {
                    secRaw.contains("EUL") || secRaw.contains("을구") -> RegistrySection.EUL_GU
                    secRaw.contains("TENANT") || secRaw.contains("임차") -> RegistrySection.TENANT_COURT
                    secRaw.contains("GAP") || secRaw.contains("갑구") -> RegistrySection.GAP_GU
                    else -> defaultSection
                }
                val isBase = obj.optBoolean("isBaseRight", obj.optBoolean("is_base_right", false))
                val isAssumed = obj.optBoolean("isAssumedRisk", obj.optBoolean("is_assumed_risk", false))
                val tone = when {
                    isBase -> RegistryNodeTone.BASE_RIGHT_ANCHOR
                    isAssumed -> RegistryNodeTone.ASSUMED_DANGER
                    section == RegistrySection.GAP_GU && obj.optString("statusBadge", "").contains("소유") -> RegistryNodeTone.NEUTRAL_OWNERSHIP
                    else -> RegistryNodeTone.EXTINGUISHED_SAFE
                }
                val title = PrivacySanitizerEngine.sanitizeForPublicBlog(
                    obj.optString("title", obj.optString("rightTitle", obj.optString("right_type", "등기 권리")))
                )
                val holder = PrivacySanitizerEngine.sanitizeForPublicBlog(
                    obj.optString("holderOrCreditor", obj.optString("creditor", obj.optString("holder", "등기권리자(비식별)")))
                )
                val explanation = PrivacySanitizerEngine.sanitizeForPublicBlog(
                    obj.optString("plainExplanation", obj.optString("note", obj.optString("outcome", title)))
                )
                val node = RegistryVisualNode(
                    nodeId = obj.optString("nodeId", "$caseNo-${section.name}-$idx"),
                    section = section,
                    rankOrder = obj.optString("rankOrder", obj.optString("order_no", "${idx + 1}번")),
                    registeredDate = obj.optString("registeredDate", obj.optString("registered_date", "")),
                    title = title,
                    holderOrCreditor = holder,
                    amountOrShare = obj.optString("amountOrShare", obj.optString("amount_text", "-")),
                    statusBadge = obj.optString("statusBadge", obj.optString("status", if (isAssumed) "인수 주의" else "소멸")),
                    plainExplanation = explanation,
                    nodeTone = tone,
                    isBaseRight = isBase,
                    isAssumedRisk = isAssumed
                )
                if (section == RegistrySection.GAP_GU) gapGuList += node else eulGuList += node
            }
        }

        parseArray(optJsonArrayFlexible(item, "registry_gap_gu"), RegistrySection.GAP_GU)
        parseArray(optJsonArrayFlexible(item, "registry_eul_gu"), RegistrySection.EUL_GU)
        parseArray(optJsonArrayFlexible(item, "registry_entries"), RegistrySection.EUL_GU)
        parseArray(optJsonArrayFlexible(item, "rights_history"), RegistrySection.EUL_GU)
        return gapGuList to eulGuList
    }

    private fun parseCourtDocumentsField(
        item: JSONObject,
        caseNo: String,
        courtName: String
    ): List<CourtDocumentRecord> {
        val docs = mutableListOf<CourtDocumentRecord>()

        fun parseDocObj(obj: JSONObject, fallbackKind: CourtDocKind): CourtDocumentRecord? {
            val kindRaw = obj.optString("docKind", obj.optString("kind", obj.optString("type", ""))).uppercase()
            val kind = when {
                kindRaw.contains("SALE") || kindRaw.contains("명세") -> CourtDocKind.SALE_SPECIFICATION
                kindRaw.contains("STATUS") || kindRaw.contains("SURVEY") || kindRaw.contains("현황") -> CourtDocKind.STATUS_SURVEY
                kindRaw.contains("APPRAISAL") || kindRaw.contains("감정") -> CourtDocKind.APPRAISAL_REPORT
                else -> fallbackKind
            }
            val highlight = PrivacySanitizerEngine.sanitizeForPublicBlog(
                obj.optString("summaryHighlight", obj.optString("summary", "")).trim()
            )
            val linesArr = obj.optJSONArray("fullRecordLines") ?: obj.optJSONArray("lines")
            val lines = if (linesArr != null) {
                (0 until linesArr.length()).mapNotNull { i ->
                    linesArr.optString(i).trim().takeIf { it.isNotBlank() }?.let {
                        PrivacySanitizerEngine.sanitizeForPublicBlog(it)
                    }
                }
            } else emptyList()
            val riskNote = PrivacySanitizerEngine.sanitizeForPublicBlog(
                obj.optString("riskFlagNote", obj.optString("risk_note", "")).trim()
            )
            if (highlight.isBlank() && lines.isEmpty()) return null
            return CourtDocumentRecord(
                docKind = kind,
                referenceNo = obj.optString("referenceNo", "$caseNo-${kind.title}"),
                issuedDate = obj.optString("issuedDate", obj.optString("issued_date", "")),
                issuer = obj.optString("issuer", courtName),
                summaryHighlight = highlight.ifBlank { lines.firstOrNull().orEmpty() },
                fullRecordLines = lines.ifEmpty { listOf(highlight) },
                riskFlagNote = riskNote.ifBlank { "법원 공고 원문 기준 검증 완료" }
            )
        }

        fun addFlatSummaryDoc(
            keys: List<String>,
            kind: CourtDocKind,
            issuerLabel: String
        ) {
            if (docs.any { it.docKind == kind }) return
            val rawText = keys.firstNotNullOfOrNull { k ->
                item.optString(k).trim().takeIf { it.isNotBlank() && !it.startsWith("{") && !it.startsWith("[") }
            } ?: return
            val clean = PrivacySanitizerEngine.sanitizeForPublicBlog(rawText)
            docs += CourtDocumentRecord(
                docKind = kind,
                referenceNo = "$caseNo-${kind.title}",
                issuedDate = item.optString("updated_at").take(10),
                issuer = issuerLabel,
                summaryHighlight = clean,
                fullRecordLines = clean.lines().filter { it.isNotBlank() }.ifEmpty { listOf(clean) },
                riskFlagNote = "법원 공고·딱찰 서버 제공 원문 기준 검증"
            )
        }

        optJsonArrayFlexible(item, "court_documents")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                parseDocObj(obj, CourtDocKind.SALE_SPECIFICATION)?.let { docs += it }
            }
        }
        item.optJSONObject("sale_specification")?.let { obj ->
            parseDocObj(obj, CourtDocKind.SALE_SPECIFICATION)?.let { docs += it }
        }
        item.optJSONObject("status_survey")?.let { obj ->
            parseDocObj(obj, CourtDocKind.STATUS_SURVEY)?.let { docs += it }
        }
        item.optJSONObject("appraisal_report")?.let { obj ->
            parseDocObj(obj, CourtDocKind.APPRAISAL_REPORT)?.let { docs += it }
        }
        addFlatSummaryDoc(
            keys = listOf("status_survey_summary", "survey_findings", "현황조사서", "status_survey"),
            kind = CourtDocKind.STATUS_SURVEY,
            issuerLabel = "$courtName 집행관실"
        )
        addFlatSummaryDoc(
            keys = listOf("sale_spec_summary", "매각물건명세서", "sale_specification"),
            kind = CourtDocKind.SALE_SPECIFICATION,
            issuerLabel = courtName
        )
        addFlatSummaryDoc(
            keys = listOf("appraisal_summary", "감정평가서", "appraisal_report"),
            kind = CourtDocKind.APPRAISAL_REPORT,
            issuerLabel = "법원 지정 감정평가법인"
        )
        return docs.distinctBy { it.docKind }
    }

    private fun parseStringArrayField(item: JSONObject, key: String): List<String> {
        val arr = optJsonArrayFlexible(item, key) ?: return emptyList()
        return (0 until arr.length())
            .mapNotNull { idx -> arr.optString(idx).trim().takeIf { it.isNotBlank() } }
    }

    private fun parseVisitRecordsField(item: JSONObject, caseNo: String): List<FieldVisitRecord> {
        val arr = optJsonArrayFlexible(item, "visit_records")
            ?: optJsonArrayFlexible(item, "status_survey_visits")
            ?: optJsonArrayFlexible(item, "bailiff_visits")
            ?: optJsonArrayFlexible(item, "survey_visits")
            ?: optJsonArrayFlexible(item, "visits")

        val parsed = if (arr != null) {
            (0 until arr.length()).mapNotNull { idx ->
                val obj = arr.optJSONObject(idx) ?: return@mapNotNull null
                val round = obj.optString("visitRound", obj.optString("round", "${idx + 1}차 방문")).trim()
                val visitedAt = obj.optString("visitedAt", obj.optString("visited_at", "")).trim()
                val findings = obj.optString("findingsNote", obj.optString("findings", obj.optString("note", ""))).trim()
                if (findings.isBlank() && visitedAt.isBlank()) return@mapNotNull null
                val srcRaw = obj.optString("source", "").uppercase()
                val src = when {
                    srcRaw.contains("RESIDENT") || round.contains("전입") -> VisitRecordSource.RESIDENT_CHECK
                    srcRaw.contains("MANAGE") || round.contains("관리") -> VisitRecordSource.MANAGEMENT_OFFICE
                    srcRaw.contains("IMJANG") || srcRaw.contains("FIELD") || round.contains("임장") -> VisitRecordSource.FIELD_IMJANG
                    else -> VisitRecordSource.BAILIFF_SURVEY
                }
                FieldVisitRecord(
                    visitId = obj.optString("visitId", "$caseNo-srv-visit-$idx"),
                    visitRound = round.ifBlank { "${idx + 1}차 조사" },
                    visitedAt = visitedAt.ifBlank { "현황조사서 기준일" },
                    source = src,
                    visitorLabel = obj.optString("visitorLabel", obj.optString("visitor", "법원 집행관")),
                    occupancyStatus = obj.optString("occupancyStatus", obj.optString("occupancy", "현황 확인")),
                    findingsNote = findings.ifBlank { "현황조사 방문 기록 확인" },
                    attachedPhotoUri = obj.optString("attachedPhotoUri", obj.optString("photo_url", ""))
                )
            }
        } else emptyList()

        if (parsed.isNotEmpty()) return parsed

        // Derive a real visit log directly from server-provided status_survey / survey_findings if present
        val surveyObj = item.optJSONObject("status_survey")
        val surveySummary = surveyObj?.optString("summaryHighlight", surveyObj.optString("summary", ""))?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: item.optString("status_survey_summary").trim().takeIf { it.isNotBlank() }
            ?: item.optString("survey_findings").trim().takeIf { it.isNotBlank() }
            ?: item.optString("현황조사서").trim().takeIf { it.isNotBlank() }
        if (!surveySummary.isNullOrBlank()) {
            val cleanNote = PrivacySanitizerEngine.sanitizeForPublicBlog(surveySummary)
            val surveyDate = surveyObj?.optString("issuedDate", surveyObj.optString("issued_date", ""))
                ?.trim()?.ifBlank { "법원 현황조사서 기준일" } ?: "법원 현황조사서 기준일"
            return listOf(
                FieldVisitRecord(
                    visitId = "$caseNo-srv-status-survey",
                    visitRound = "법원 집행관 현황조사서",
                    visitedAt = surveyDate,
                    source = VisitRecordSource.BAILIFF_SURVEY,
                    visitorLabel = surveyObj?.optString("issuer", "법원 집행관실")?.ifBlank { "법원 집행관실" } ?: "법원 집행관실",
                    occupancyStatus = PrivacySanitizerEngine.sanitizeForPublicBlog(
                        item.optString("possession_summary").ifBlank { "현황조사서 기재" }
                    ),
                    findingsNote = cleanNote
                )
            )
        }
        return emptyList()
    }

    private fun parseBlogAnalysisData(item: JSONObject, photoCount: Int): DdakchalBlogAnalysisData? {
        val raw = item.opt("blog_analysis_data") ?: return null
        val obj = when (raw) {
            is JSONObject -> raw
            is String -> if (raw.trim().startsWith("{")) runCatching { JSONObject(raw) }.getOrNull() else null
            else -> null
        } ?: return null

        val summaryLines = obj.optJSONArray("summary3Lines")?.let { arr ->
            (0 until arr.length()).mapNotNull { idx -> arr.optString(idx).takeIf { it.isNotBlank() } }
        }.orEmpty()

        val chartObj = obj.optJSONObject("keyMetricsChartData")
        val chartData = chartObj?.let {
            DdakchalKeyMetricsChartData(
                appraisalKrw = it.optLong("appraisalKrw", it.optLong("appraisal_krw", 0L)),
                minBidKrw = it.optLong("minBidKrw", it.optLong("min_bid_krw", 0L)),
                realTradeMedianKrw = it.optLong("realTradeMedianKrw", it.optLong("real_trade_median_krw", 0L)),
                conservativeBidKrw = it.optLong("conservativeBidKrw", it.optLong("conservative_bid_krw", 0L)),
                balancedBidKrw = it.optLong("balancedBidKrw", it.optLong("balanced_bid_krw", 0L)),
                aggressiveBidKrw = it.optLong("aggressiveBidKrw", it.optLong("aggressive_bid_krw", 0L)),
                discountRatePct = it.optInt("discountRatePct", it.optInt("discount_rate_pct", 0)),
                stats3yRatePct = it.optDouble("stats3yRatePct", it.optDouble("stats_3y_rate_pct", 0.0))
            )
        }

        val riskObj = obj.optJSONObject("riskAndOpportunity")
        val riskOpportunity = riskObj?.let {
            DdakchalRiskOpportunity(
                riskSummary = it.optString("riskSummary", it.optString("risk", "")),
                opportunitySummary = it.optString("opportunitySummary", it.optString("opportunity", "")),
                evictionDifficulty = it.optString("evictionDifficulty", ""),
                tenantRiskNote = it.optString("tenantRiskNote", "")
            )
        }

        val qaArr = obj.optJSONArray("qaBlocks")
        val qaBlocks = if (qaArr != null) {
            (0 until qaArr.length()).mapNotNull { idx ->
                val q = qaArr.optJSONObject(idx) ?: return@mapNotNull null
                val question = q.optString("question", q.optString("q", ""))
                val answer = q.optString("answer", q.optString("a", ""))
                if (question.isBlank() && answer.isBlank()) null else DdakchalQaPair(question, answer)
            }
        } else emptyList()

        val freshObj = obj.optJSONObject("freshness")
        val freshness = freshObj?.let {
            DdakchalFreshness(
                updatedAt = it.optString("updatedAt", item.optString("updated_at")),
                source = it.optString("source", "ddakchal-crawler-d1"),
                courtPhotoCount = it.optInt("courtPhotoCount", photoCount)
            )
        } ?: DdakchalFreshness(
            updatedAt = item.optString("updated_at"),
            courtPhotoCount = photoCount
        )

        return DdakchalBlogAnalysisData(
            hookHeadline = obj.optString("hookHeadline"),
            summary3Lines = summaryLines,
            keyMetricsChartData = chartData,
            riskAndOpportunity = riskOpportunity,
            qaBlocks = qaBlocks,
            freshness = freshness
        )
    }
}

