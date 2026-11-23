package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.engine.BlogPostTemplateEngine
import com.example.model.BlogPostTemplate
import com.example.model.ChannelPlatform
import com.example.model.PropertyType
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

private val Context.blogTemplatesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ddakchal_post_templates"
)

class BlogPostTemplateStore(
    private val dataStore: DataStore<Preferences>
) {
    constructor(context: Context) : this(context.blogTemplatesDataStore)

    private object Keys {
        val TEMPLATES_JSON = stringPreferencesKey("blog_post_templates_json")
        val INITIALIZED = booleanPreferencesKey("blog_post_templates_initialized")
    }

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, BlogPostTemplate::class.java)
    private val listAdapter: JsonAdapter<List<BlogPostTemplate>> = moshi.adapter(listType)

    val templatesFlow: Flow<List<BlogPostTemplate>> = dataStore.data.map { prefs ->
        decodeOrSeed(prefs)
    }

    suspend fun getTemplatesSnapshot(): List<BlogPostTemplate> {
        val prefs = dataStore.data.first()
        return decodeOrSeed(prefs)
    }

    suspend fun createTemplate(
        name: String,
        propertyType: PropertyType,
        targetChannel: ChannelPlatform,
        titleTemplate: String,
        bodyTemplate: String,
        tagsTemplate: String,
        activateForGeneration: Boolean = true
    ): BlogPostTemplate {
        val newTemplate = BlogPostTemplate(
            id = "tpl-${propertyType.code.lowercase()}-${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            propertyType = propertyType,
            targetChannel = targetChannel,
            titleTemplate = titleTemplate.trim(),
            bodyTemplate = bodyTemplate.trim(),
            tagsTemplate = tagsTemplate.trim(),
            isPreset = false,
            isActiveForGeneration = activateForGeneration,
            updatedAt = nowLabel()
        )
        saveTemplate(newTemplate)
        return newTemplate
    }

    suspend fun saveTemplate(template: BlogPostTemplate) {
        val stamped = template.copy(
            name = template.name.trim(),
            titleTemplate = template.titleTemplate.trim(),
            bodyTemplate = template.bodyTemplate.trim(),
            tagsTemplate = template.tagsTemplate.trim(),
            updatedAt = nowLabel()
        )
        dataStore.edit { prefs ->
            val current = decodeOrSeed(prefs).toMutableList()
            val adjusted = if (stamped.isActiveForGeneration) {
                current.map { existing ->
                    if (existing.id != stamped.id &&
                        existing.propertyType == stamped.propertyType &&
                        existing.targetChannel == stamped.targetChannel
                    ) {
                        existing.copy(isActiveForGeneration = false)
                    } else {
                        existing
                    }
                }.toMutableList()
            } else {
                current
            }

            val idx = adjusted.indexOfFirst { it.id == stamped.id }
            if (idx >= 0) {
                adjusted[idx] = stamped
            } else {
                adjusted.add(0, stamped)
            }
            prefs[Keys.TEMPLATES_JSON] = listAdapter.toJson(adjusted)
            prefs[Keys.INITIALIZED] = true
        }
    }

    suspend fun setActiveTemplate(templateId: String) {
        dataStore.edit { prefs ->
            val current = decodeOrSeed(prefs)
            val target = current.firstOrNull { it.id == templateId } ?: return@edit
            val updated = current.map { item ->
                when {
                    item.id == templateId -> item.copy(isActiveForGeneration = true, updatedAt = nowLabel())
                    item.propertyType == target.propertyType && item.targetChannel == target.targetChannel ->
                        item.copy(isActiveForGeneration = false)
                    else -> item
                }
            }
            prefs[Keys.TEMPLATES_JSON] = listAdapter.toJson(updated)
            prefs[Keys.INITIALIZED] = true
        }
    }

    suspend fun deleteTemplate(templateId: String) {
        dataStore.edit { prefs ->
            val current = decodeOrSeed(prefs)
            val remaining = current.filterNot { it.id == templateId }
            prefs[Keys.TEMPLATES_JSON] = listAdapter.toJson(remaining)
            prefs[Keys.INITIALIZED] = true
        }
    }

    suspend fun resetToDefaults(): List<BlogPostTemplate> {
        val defaults = BlogPostTemplateEngine.defaultBlogPostTemplates()
        dataStore.edit { prefs ->
            prefs[Keys.TEMPLATES_JSON] = listAdapter.toJson(defaults)
            prefs[Keys.INITIALIZED] = true
        }
        return defaults
    }

    private fun decodeOrSeed(prefs: Preferences): List<BlogPostTemplate> {
        val initialized = prefs[Keys.INITIALIZED] ?: false
        val raw = prefs[Keys.TEMPLATES_JSON]
        if (!initialized && raw.isNullOrBlank()) {
            return BlogPostTemplateEngine.defaultBlogPostTemplates()
        }
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            listAdapter.fromJson(raw).orEmpty()
        }.getOrElse {
            BlogPostTemplateEngine.defaultBlogPostTemplates()
        }
    }

    private fun nowLabel(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(Date())
}
