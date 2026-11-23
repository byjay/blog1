package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.engine.Article30PointEvaluator
import com.example.engine.DefaultSpecDataFactory
import com.example.engine.RegistryInfographicEngine
import com.example.model.ChannelPlatform
import com.example.model.ProFunnelConfig
import com.example.model.RegistryInfographicTemplateType
import com.example.model.ServerAutomationConfig
import com.example.model.VisitorTrafficLog
import com.example.model.WarehouseArticle
import com.example.model.WarehouseArticleStatus
import com.example.model.WritingActivityLog
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.warehouseDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ddakchal_article_warehouse"
)

data class WarehouseSnapshot(
    val articles: List<WarehouseArticle>,
    val writingLogs: List<WritingActivityLog>,
    val visitorLogs: List<VisitorTrafficLog>
)

class PostWarehouseStore(
    private val dataStore: DataStore<Preferences>
) {
    constructor(context: Context) : this(context.warehouseDataStore)

    private object Keys {
        val WAREHOUSE_JSON = stringPreferencesKey("warehouse_payload_json")
    }

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter: JsonAdapter<WarehouseSnapshot> = moshi.adapter(WarehouseSnapshot::class.java)

    val snapshotFlow: Flow<WarehouseSnapshot> = dataStore.data.map { prefs ->
        decodeOrSeed(prefs)
    }

    suspend fun getSnapshot(): WarehouseSnapshot = decodeOrSeed(dataStore.data.first())

    suspend fun upsertArticle(
        article: WarehouseArticle,
        activityLog: WritingActivityLog? = null
    ): WarehouseSnapshot {
        var result = getSnapshot()
        dataStore.edit { prefs ->
            val current = decodeOrSeed(prefs)
            val updatedArticles = listOf(article) + current.articles.filterNot {
                it.warehouseId == article.warehouseId || it.slot.postId == article.slot.postId
            }
            val updatedLogs = if (activityLog != null) {
                (listOf(activityLog) + current.writingLogs).take(60)
            } else {
                current.writingLogs
            }
            val updatedVisitorLogs = if (article.warehouseStatus == WarehouseArticleStatus.REGISTERED) {
                bumpTodayVisitors(current.visitorLogs, naverAdd = 180, tsAdd = 95, cfAdd = 120, dbAdd = 1, proAdd = 2)
            } else {
                current.visitorLogs
            }
            result = WarehouseSnapshot(updatedArticles, updatedLogs, updatedVisitorLogs)
            prefs[Keys.WAREHOUSE_JSON] = adapter.toJson(result)
        }
        return result
    }

    suspend fun upsertArticles(
        articles: List<WarehouseArticle>,
        activityLog: WritingActivityLog? = null
    ): WarehouseSnapshot {
        var result = getSnapshot()
        dataStore.edit { prefs ->
            val current = decodeOrSeed(prefs)
            val incomingIds = articles.map { it.slot.postId }.toSet()
            val merged = articles + current.articles.filterNot { it.slot.postId in incomingIds }
            val updatedLogs = if (activityLog != null) {
                (listOf(activityLog) + current.writingLogs).take(60)
            } else {
                current.writingLogs
            }
            result = WarehouseSnapshot(merged, updatedLogs, current.visitorLogs)
            prefs[Keys.WAREHOUSE_JSON] = adapter.toJson(result)
        }
        return result
    }

    suspend fun recordRegistration(
        warehouseId: String,
        channels: List<ChannelPlatform>,
        activityLog: WritingActivityLog
    ): WarehouseSnapshot {
        var result = getSnapshot()
        dataStore.edit { prefs ->
            val current = decodeOrSeed(prefs)
            val now = nowTimestamp()
            val updatedArticles = current.articles.map { item ->
                if (item.warehouseId == warehouseId && item.evaluation.eligibleForRegistration) {
                    item.copy(
                        warehouseStatus = WarehouseArticleStatus.REGISTERED,
                        registeredAt = now,
                        registeredChannels = channels,
                        accumulatedViews = item.accumulatedViews + 410,
                        dbLeadCount = item.dbLeadCount + 2,
                        proClickCount = item.proClickCount + 14
                    )
                } else {
                    item
                }
            }
            val updatedLogs = (listOf(activityLog) + current.writingLogs).take(60)
            val updatedVisitorLogs = bumpTodayVisitors(
                current.visitorLogs,
                naverAdd = 220,
                tsAdd = 110,
                cfAdd = 140,
                dbAdd = 2,
                proAdd = 4
            )
            result = WarehouseSnapshot(updatedArticles, updatedLogs, updatedVisitorLogs)
            prefs[Keys.WAREHOUSE_JSON] = adapter.toJson(result)
        }
        return result
    }

    suspend fun refreshVisitorTelemetry(
        naverDelta: Int = 145,
        tistoryDelta: Int = 78,
        cloudflareDelta: Int = 112
    ): WarehouseSnapshot {
        var result = getSnapshot()
        dataStore.edit { prefs ->
            val current = decodeOrSeed(prefs)
            val updatedVisitors = bumpTodayVisitors(
                current.visitorLogs,
                naverAdd = naverDelta,
                tsAdd = tistoryDelta,
                cfAdd = cloudflareDelta,
                dbAdd = 1,
                proAdd = 2
            )
            result = current.copy(visitorLogs = updatedVisitors)
            prefs[Keys.WAREHOUSE_JSON] = adapter.toJson(result)
        }
        return result
    }

    suspend fun deleteArticle(warehouseId: String): WarehouseSnapshot {
        var result = getSnapshot()
        dataStore.edit { prefs ->
            val current = decodeOrSeed(prefs)
            result = current.copy(articles = current.articles.filterNot { it.warehouseId == warehouseId })
            prefs[Keys.WAREHOUSE_JSON] = adapter.toJson(result)
        }
        return result
    }

    suspend fun resetToDefaults(): WarehouseSnapshot {
        val seeded = buildInitialSeed()
        dataStore.edit { prefs ->
            prefs[Keys.WAREHOUSE_JSON] = adapter.toJson(seeded)
        }
        return seeded
    }

    private fun decodeOrSeed(prefs: Preferences): WarehouseSnapshot {
        val raw = prefs[Keys.WAREHOUSE_JSON]
        if (raw.isNullOrBlank()) return buildInitialSeed()
        return runCatching {
            adapter.fromJson(raw)?.takeIf { it.articles.isNotEmpty() || it.visitorLogs.isNotEmpty() }
                ?: buildInitialSeed()
        }.getOrElse {
            buildInitialSeed()
        }
    }

    private fun bumpTodayVisitors(
        logs: List<VisitorTrafficLog>,
        naverAdd: Int,
        tsAdd: Int,
        cfAdd: Int,
        dbAdd: Int,
        proAdd: Int
    ): List<VisitorTrafficLog> {
        if (logs.isEmpty()) return defaultVisitorLogs()
        val first = logs.first()
        val nv = first.naverViews + naverAdd
        val ts = first.tistoryViews + tsAdd
        val cf = first.cloudflareViews + cfAdd
        val total = nv + ts + cf
        val adpost = ((total.toLong() * 16L / 10L) * 2600L) / 1000L
        val updatedFirst = first.copy(
            naverViews = nv,
            tistoryViews = ts,
            cloudflareViews = cf,
            totalViews = total,
            avgDwellSec = (first.avgDwellSec + 4).coerceAtMost(265),
            dbInquiries = first.dbInquiries + dbAdd,
            proConversions = first.proConversions + proAdd,
            estimatedAdpostKrw = adpost
        )
        return listOf(updatedFirst) + logs.drop(1)
    }

    companion object {
        fun buildInitialSeed(): WarehouseSnapshot {
            val defaultConfig = ServerAutomationConfig()
            val defaultFunnel = ProFunnelConfig()
            val initialSlots = DefaultSpecDataFactory.buildInitialDaily5Slots(defaultConfig, defaultFunnel)

            val articles = initialSlots.mapIndexed { idx, slot ->
                val regInfographic = RegistryInfographicEngine.buildTemplate(
                    auction = slot.auctionCase,
                    templateType = RegistryInfographicTemplateType.COMBINED_EXPERT_BOARD
                )
                val eval = Article30PointEvaluator.evaluate(
                    slot = slot,
                    config = defaultConfig,
                    proFunnel = defaultFunnel,
                    registryTemplate = regInfographic
                )
                val status = when {
                    idx == 0 && eval.eligibleForRegistration -> WarehouseArticleStatus.REGISTERED
                    eval.eligibleForRegistration -> WarehouseArticleStatus.STORED_READY
                    else -> WarehouseArticleStatus.NEEDS_IMPROVEMENT
                }
                WarehouseArticle(
                    warehouseId = "wh-${slot.auctionCase.dedupKey}",
                    slot = slot,
                    evaluation = eval,
                    registryInfographic = regInfographic,
                    warehouseStatus = status,
                    createdAt = "2026-11-22 08:${10 + idx}:15",
                    registeredAt = if (status == WarehouseArticleStatus.REGISTERED) "2026-11-22 10:29:15" else null,
                    registeredChannels = if (status == WarehouseArticleStatus.REGISTERED) ChannelPlatform.entries else emptyList(),
                    accumulatedViews = if (status == WarehouseArticleStatus.REGISTERED) 1_420 else 0,
                    dbLeadCount = if (status == WarehouseArticleStatus.REGISTERED) 5 else 0,
                    proClickCount = if (status == WarehouseArticleStatus.REGISTERED) 43 else 0
                )
            }

            val writingLogs = articles.mapIndexed { idx, art ->
                WritingActivityLog(
                    logId = "wlog-init-${idx + 1}",
                    warehouseId = art.warehouseId,
                    caseId = art.slot.auctionCase.caseId,
                    actionTitle = if (art.warehouseStatus == WarehouseArticleStatus.REGISTERED) {
                        "창고 입고 → 30항목(${art.evaluation.totalScore}점) 통과 → 3채널 등록 완료"
                    } else {
                        "창고 자동 보관 → 30항목(${art.evaluation.totalScore}점) 등록 대기"
                    },
                    score = art.evaluation.totalScore,
                    passedGate95 = art.evaluation.eligibleForRegistration,
                    detail = "${art.evaluation.passedCount}/30 항목 통과 · 갑구/을구/소유권 도식 포함",
                    timestamp = art.createdAt
                )
            }

            return WarehouseSnapshot(
                articles = articles,
                writingLogs = writingLogs,
                visitorLogs = defaultVisitorLogs()
            )
        }

        private fun defaultVisitorLogs(): List<VisitorTrafficLog> = listOf(
            VisitorTrafficLog("11/22 (오늘)", 1_840, 620, 910, 3_370, 208, 14, 38, 14_019L),
            VisitorTrafficLog("11/21 (어제)", 1_690, 580, 840, 3_110, 196, 12, 31, 12_937L),
            VisitorTrafficLog("11/20", 1_520, 510, 760, 2_790, 191, 9, 27, 11_606L),
            VisitorTrafficLog("11/19", 1_410, 495, 710, 2_615, 184, 8, 22, 10_878L),
            VisitorTrafficLog("11/18", 1_280, 440, 645, 2_365, 179, 7, 19, 9_838L),
            VisitorTrafficLog("11/17", 1_190, 410, 590, 2_190, 172, 6, 17, 9_110L),
            VisitorTrafficLog("11/16", 1_050, 380, 520, 1_950, 168, 5, 14, 8_112L)
        )

        private fun nowTimestamp(): String =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())
    }
}
