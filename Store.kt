package com.nexrblx.app

import android.content.Context
import org.json.JSONArray
import java.io.File

/** Offline cache + favorites, stored as small JSON files. */
class Store(ctx: Context) {
    private val dir: File = ctx.filesDir
    private val prefs = ctx.getSharedPreferences("nex", Context.MODE_PRIVATE)

    private fun read(name: String): List<ScriptItem> = try {
        val f = File(dir, name)
        if (f.exists()) JSONArray(f.readText()).toScripts() else emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    private fun write(name: String, list: List<ScriptItem>) {
        try {
            File(dir, name).writeText(list.toJsonArray().toString())
        } catch (e: Exception) {
            // ignore disk errors, cache is optional
        }
    }

    fun latest() = read("latest.json")
    fun saveLatest(l: List<ScriptItem>) = write("latest.json", l)
    fun popular() = read("popular.json")
    fun savePopular(l: List<ScriptItem>) = write("popular.json", l)
    fun favorites() = read("favorites.json")
    fun saveFavorites(l: List<ScriptItem>) = write("favorites.json", l)

    var lastUpdate: Long
        get() = prefs.getLong("last", 0L)
        set(v) {
            prefs.edit().putLong("last", v).apply()
        }
}
