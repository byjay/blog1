package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ProFunnelConfig
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.proFunnelDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ddakchal_pro_funnel"
)

class ProFunnelStore(
    private val dataStore: DataStore<Preferences>
) {
    constructor(context: Context) : this(context.proFunnelDataStore)

    private object Keys {
        val PRO_FUNNEL_JSON = stringPreferencesKey("pro_funnel_config_json")
    }

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter: JsonAdapter<ProFunnelConfig> = moshi.adapter(ProFunnelConfig::class.java)

    val funnelFlow: Flow<ProFunnelConfig> = dataStore.data.map { prefs ->
        decodeOrDefault(prefs)
    }

    suspend fun getSnapshot(): ProFunnelConfig {
        val prefs = dataStore.data.first()
        return decodeOrDefault(prefs)
    }

    suspend fun saveConfig(config: ProFunnelConfig): ProFunnelConfig {
        val cleaned = config.copy(
            proLandingUrl = config.proLandingUrl.trim().ifBlank { "https://ddakchal.pages.dev/pro" },
            naverSearchKeyword = config.naverSearchKeyword.trim().ifBlank { "딱찰 경매" },
            teaserHeadline = config.teaserHeadline.trim().ifBlank { ProFunnelConfig().teaserHeadline },
            freeUnlockedItems = config.freeUnlockedItems.map { it.trim() }.filter { it.isNotBlank() }
                .ifEmpty { ProFunnelConfig().freeUnlockedItems },
            proLockedItems = config.proLockedItems.map { it.trim() }.filter { it.isNotBlank() }
                .ifEmpty { ProFunnelConfig().proLockedItems },
            naverPinnedCommentTemplate = config.naverPinnedCommentTemplate.trim()
                .ifBlank { ProFunnelConfig().naverPinnedCommentTemplate },
            updatedAt = nowLabel()
        )
        dataStore.edit { prefs ->
            prefs[Keys.PRO_FUNNEL_JSON] = adapter.toJson(cleaned)
        }
        return cleaned
    }

    suspend fun resetToDefaults(): ProFunnelConfig {
        val defaults = ProFunnelConfig(updatedAt = nowLabel())
        dataStore.edit { prefs ->
            prefs[Keys.PRO_FUNNEL_JSON] = adapter.toJson(defaults)
        }
        return defaults
    }

    private fun decodeOrDefault(prefs: Preferences): ProFunnelConfig {
        val raw = prefs[Keys.PRO_FUNNEL_JSON]
        if (raw.isNullOrBlank()) return ProFunnelConfig()
        return runCatching {
            adapter.fromJson(raw) ?: ProFunnelConfig()
        }.getOrElse {
            ProFunnelConfig()
        }
    }

    private fun nowLabel(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(Date())
}
