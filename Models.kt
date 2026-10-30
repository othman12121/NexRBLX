package com.nexrblx.app

import org.json.JSONArray
import org.json.JSONObject

enum class Filter(val label: Int) {
    ALL(R.string.f_all),
    UNIVERSAL(R.string.f_universal),
    NOKEY(R.string.f_nokey),
    VERIFIED(R.string.f_verified),
    VIEWS(R.string.f_views)
}

data class ScriptItem(
    val id: String,
    val title: String,
    val gameName: String,
    val gameImage: String,
    val image: String,
    val views: Long,
    val createdAt: String,
    val updatedAt: String,
    val verified: Boolean,
    val hasKey: Boolean,
    val universal: Boolean,
    val patched: Boolean,
    val free: Boolean,
    val source: String,
    val url: String
) {
    val uid: String get() = "$source:$id"
    val thumb: String get() = if (image.isNotBlank()) image else gameImage

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("gameName", gameName)
        .put("gameImage", gameImage)
        .put("image", image)
        .put("views", views)
        .put("createdAt", createdAt)
        .put("updatedAt", updatedAt)
        .put("verified", verified)
        .put("hasKey", hasKey)
        .put("universal", universal)
        .put("patched", patched)
        .put("free", free)
        .put("source", source)
        .put("url", url)

    companion object {
        fun fromJson(o: JSONObject) = ScriptItem(
            id = o.optString("id"),
            title = o.optString("title"),
            gameName = o.optString("gameName"),
            gameImage = o.optString("gameImage"),
            image = o.optString("image"),
            views = o.optLong("views"),
            createdAt = o.optString("createdAt"),
            updatedAt = o.optString("updatedAt"),
            verified = o.optBoolean("verified"),
            hasKey = o.optBoolean("hasKey"),
            universal = o.optBoolean("universal"),
            patched = o.optBoolean("patched"),
            free = o.optBoolean("free", true),
            source = o.optString("source"),
            url = o.optString("url")
        )
    }
}

fun List<ScriptItem>.toJsonArray(): JSONArray {
    val a = JSONArray()
    forEach { a.put(it.toJson()) }
    return a
}

fun JSONArray.toScripts(): List<ScriptItem> =
    (0 until length()).map { ScriptItem.fromJson(getJSONObject(it)) }
