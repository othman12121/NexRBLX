package com.nexrblx.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Reads public metadata only (title, game, image, dates, views).
 * The script code field is never read, stored or shown.
 */
object Api {
    private const val SB = "https://scriptblox.com"
    private const val RS = "https://api.roscripts.io/v1"
    private const val RS_SITE = "https://roscripts.io"

    private fun get(url: String): String {
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.connectTimeout = 15000
            c.readTimeout = 20000
            c.setRequestProperty("User-Agent", "NexRBLX/1.0 (Android)")
            c.setRequestProperty("Accept", "application/json")
            if (c.responseCode !in 200..299) throw IOException("HTTP " + c.responseCode)
            return c.inputStream.bufferedReader().use { it.readText() }
        } finally {
            c.disconnect()
        }
    }

    private fun JSONObject?.s(name: String): String =
        if (this == null || isNull(name)) "" else optString(name)

    private fun abs(p: String, base: String): String = when {
        p.startsWith("http") -> p
        p.startsWith("//") -> "https:$p"
        p.startsWith("/") -> base + p
        else -> ""
    }

    private fun parseSB(json: String): List<ScriptItem> {
        val arr = JSONObject(json).getJSONObject("result").getJSONArray("scripts")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val g = o.optJSONObject("game")
            val created = o.s("createdAt")
            ScriptItem(
                id = o.s("_id"),
                title = o.s("title"),
                gameName = g.s("name"),
                gameImage = abs(g.s("imageUrl"), SB),
                image = abs(o.s("image"), SB),
                views = o.optLong("views"),
                createdAt = created,
                updatedAt = o.s("updatedAt").ifBlank { created },
                verified = o.optBoolean("verified"),
                hasKey = o.optBoolean("key"),
                universal = o.optBoolean("isUniversal"),
                patched = o.optBoolean("isPatched"),
                free = o.s("scriptType") != "paid",
                source = "scriptblox",
                url = SB + "/script/" + o.s("slug")
            )
        }
    }

    private fun parseRS(json: String): List<ScriptItem> {
        val arr = JSONObject(json).getJSONObject("result").getJSONArray("scripts")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val g = o.optJSONObject("game")
            val updated = o.s("updatedAt")
            val img = listOf("thumbnail", "thumbnailUrl", "image")
                .map { abs(o.s(it), RS_SITE) }
                .firstOrNull { it.isNotBlank() } ?: ""
            ScriptItem(
                id = o.s("id"),
                title = o.s("title"),
                gameName = g.s("name"),
                gameImage = abs(g.s("image"), RS_SITE),
                image = img,
                views = o.optLong("views"),
                createdAt = o.s("createdAt").ifBlank { updated },
                updatedAt = updated,
                verified = o.optBoolean("verified"),
                hasKey = o.optBoolean("key"),
                universal = o.optBoolean("universal") || o.optBoolean("isUniversal"),
                patched = o.optBoolean("patched") || o.optBoolean("isPatched"),
                free = o.s("scriptType") != "paid",
                source = "roscripts",
                url = RS_SITE
            )
        }
    }

    private suspend fun both(sb: String, rs: String): List<ScriptItem> = coroutineScope {
        val a = async(Dispatchers.IO) { runCatching { parseSB(get(SB + sb)) } }
        val b = async(Dispatchers.IO) { runCatching { parseRS(get(RS + rs)) } }
        val ra = a.await()
        val rb = b.await()
        if (ra.isFailure && rb.isFailure) throw (ra.exceptionOrNull() ?: IOException("failed"))
        (ra.getOrDefault(emptyList()) + rb.getOrDefault(emptyList())).distinctBy { it.uid }
    }

    suspend fun latest(): List<ScriptItem> =
        both("/api/script/fetch?max=20", "/scripts?max=20")
            .sortedByDescending { it.updatedAt }

    suspend fun popular(): List<ScriptItem> =
        both(
            "/api/script/fetch?max=20&sortBy=views&order=desc",
            "/scripts?max=20&sortBy=views&order=desc"
        ).sortedByDescending { it.views }

    suspend fun browse(q: String, f: Filter, page: Int): List<ScriptItem> {
        val extra = when (f) {
            Filter.ALL -> ""
            Filter.UNIVERSAL -> "&universal=1"
            Filter.NOKEY -> "&key=0"
            Filter.VERIFIED -> "&verified=1"
            Filter.VIEWS -> "&sortBy=views&order=desc"
        } + "&page=$page"
        val text = q.trim()
        return if (text.isEmpty()) {
            both("/api/script/fetch?max=20$extra", "/scripts?max=20$extra")
        } else {
            val enc = URLEncoder.encode(text, "UTF-8")
            both("/api/script/search?q=$enc&max=20$extra", "/scripts/search?q=$enc&max=20$extra")
        }
    }
}
