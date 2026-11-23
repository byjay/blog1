package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.engine.SeoGuideEngine
import com.example.model.SeoChecklistItem
import com.example.model.SeoRuleCategory
import com.example.model.SeoRulePriority
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val Context.seoChecklistDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ddakchal_seo_checklist"
)

class SeoChecklistStore(
    private val dataStore: DataStore<Preferences>
) {
    constructor(context: Context) : this(context.seoChecklistDataStore)

    private object Keys {
        val CHECKLIST_JSON = stringPreferencesKey("seo_checklist_json")
        val INITIALIZED = booleanPreferencesKey("seo_checklist_initialized")
    }

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, SeoChecklistItem::class.java)
    private val listAdapter: JsonAdapter<List<SeoChecklistItem>> = moshi.adapter(listType)

    val checklistFlow: Flow<List<SeoChecklistItem>> = dataStore.data.map { prefs ->
        decodeOrSeed(prefs)
    }

    suspend fun getSnapshot(): List<SeoChecklistItem> {
        val prefs = dataStore.data.first()
        return decodeOrSeed(prefs)
    }

    suspend fun toggleItem(itemId: String) {
        dataStore.edit { prefs ->
            val current = decodeOrSeed(prefs)
            val updated = current.map { item ->
                if (item.id == itemId) {
                    item.copy(isChecked = !item.isChecked, updatedAt = nowLabel())
                } else {
                    item
                }
            }
            prefs[Keys.CHECKLIST_JSON] = listAdapter.toJson(updated)
            prefs[Keys.INITIALIZED] = true
        }
    }

    suspend fun createItem(
        title: String,
        description: String,
        category: SeoRuleCategory,
        priority: SeoRulePriority
    ): SeoChecklistItem {
        val created = SeoChecklistItem(
            id = "chk-custom-${UUID.randomUUID().toString().take(8)}",
            title = title.trim(),
            description = description.trim(),
            category = category,
            priority = priority,
            isChecked = false,
            isCustom = true,
            ruleCode = "CUSTOM",
            updatedAt = nowLabel()
        )
        saveItem(created)
        return created
    }

    suspend fun saveItem(item: SeoChecklistItem) {
        val stamped = item.copy(
            title = item.title.trim(),
            description = item.description.trim(),
            updatedAt = nowLabel()
        )
        dataStore.edit { prefs ->
            val current = decodeOrSeed(prefs).toMutableList()
            val idx = current.indexOfFirst { it.id == stamped.id }
            if (idx >= 0) {
                current[idx] = stamped
            } else {
                current.add(0, stamped)
            }
            prefs[Keys.CHECKLIST_JSON] = listAdapter.toJson(current)
            prefs[Keys.INITIALIZED] = true
        }
    }

    suspend fun deleteItem(itemId: String) {
        dataStore.edit { prefs ->
            val current = decodeOrSeed(prefs)
            val remaining = current.filterNot { it.id == itemId }
            prefs[Keys.CHECKLIST_JSON] = listAdapter.toJson(remaining)
            prefs[Keys.INITIALIZED] = true
        }
    }

    suspend fun resetToDefaults(): List<SeoChecklistItem> {
        val defaults = SeoGuideEngine.defaultChecklistItems()
        dataStore.edit { prefs ->
            prefs[Keys.CHECKLIST_JSON] = listAdapter.toJson(defaults)
            prefs[Keys.INITIALIZED] = true
        }
        return defaults
    }

    private fun decodeOrSeed(prefs: Preferences): List<SeoChecklistItem> {
        val initialized = prefs[Keys.INITIALIZED] ?: false
        val raw = prefs[Keys.CHECKLIST_JSON]
        if (!initialized && raw.isNullOrBlank()) {
            return SeoGuideEngine.defaultChecklistItems()
        }
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            listAdapter.fromJson(raw).orEmpty()
        }.getOrElse {
            SeoGuideEngine.defaultChecklistItems()
        }
    }

    private fun nowLabel(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(Date())
}
