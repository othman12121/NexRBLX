package com.nexrblx.app

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

enum class TabId { Home, Games, Library, Codes, More }

sealed class Route {
    data class Tab(val tab: TabId) : Route()
    data class GameR(val id: String) : Route()
    data class ScriptR(val id: String) : Route()
    data class Web(val title: String, val url: String) : Route()
    object Search : Route()
    object Popular : Route()
    object Favorites : Route()
    object Updates : Route()
    object Request : Route()
}

class NexVM(app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences("nex", Context.MODE_PRIVATE)

    var data by mutableStateOf(AppData.EMPTY)
        private set
    var loading by mutableStateOf(true)
        private set
    var source by mutableStateOf("")      // online | cache | bundled
        private set
    var ar by mutableStateOf(prefs.getBoolean("ar", Locale.getDefault().language == "ar"))
        private set
    var favs by mutableStateOf(prefs.getStringSet("favs", emptySet())!!.toSet())
        private set
    var history by mutableStateOf(
        (prefs.getString("hist", "") ?: "").split("\n").filter { it.isNotBlank() }
    )
        private set
    var lastSeen by mutableStateOf(prefs.getString("seen", "") ?: "")
        private set
    var stack by mutableStateOf<List<Route>>(listOf(Route.Tab(TabId.Home)))
        private set

    init {
        refresh()
    }

    // ---------- Data ----------
    fun refresh() {
        loading = true
        viewModelScope.launch {
            val (d, src) = withContext(Dispatchers.IO) { loadAll() }
            data = d
            source = src
            loading = false
            if (lastSeen.isBlank()) {
                val latest = d.scripts.maxOfOrNull { it.updated } ?: ""
                lastSeen = latest
                prefs.edit().putString("seen", latest).apply()
            }
        }
    }

    private fun loadAll(): Pair<AppData, String> {
        val ctx = getApplication<Application>()
        val cacheFile = File(ctx.filesDir, "data_cache.json")

        if (!Config.DATA_URL.contains("YOUR_USER")) {
            try {
                val text = download(Config.DATA_URL)
                val parsed = parseData(text)
                cacheFile.writeText(text)
                return parsed to "online"
            } catch (e: Exception) {
            }
        }

        val bundled = try {
            parseData(ctx.resources.openRawResource(R.raw.nex_data).bufferedReader().use { it.readText() })
        } catch (e: Exception) {
            AppData.EMPTY
        }
        val cached = try {
            if (cacheFile.exists()) parseData(cacheFile.readText()) else null
        } catch (e: Exception) {
            null
        }
        return if (cached != null && cached.version >= bundled.version) cached to "cache"
        else bundled to "bundled"
    }

    private fun download(u: String): String {
        val c = URL(u).openConnection() as HttpURLConnection
        c.connectTimeout = 8000
        c.readTimeout = 8000
        try {
            return c.inputStream.bufferedReader().use { it.readText() }
        } finally {
            c.disconnect()
        }
    }

    fun game(id: String): Game? = data.games.firstOrNull { it.id == id }
    fun script(id: String): StudioScript? = data.scripts.firstOrNull { it.id == id }
    fun gameName(id: String): String = game(id)?.name ?: ""

    // ---------- Settings ----------
    fun setArabic(value: Boolean) {
        ar = value
        prefs.edit().putBoolean("ar", value).apply()
    }

    // ---------- Favorites ----------
    fun isFav(key: String) = favs.contains(key)

    fun toggleFav(key: String) {
        favs = if (favs.contains(key)) favs - key else favs + key
        prefs.edit().putStringSet("favs", favs).apply()
    }

    // ---------- Search history ----------
    fun addHistory(q: String) {
        val t = q.trim()
        if (t.length < 2) return
        history = (listOf(t) + history.filter { !it.equals(t, ignoreCase = true) }).take(8)
        prefs.edit().putString("hist", history.joinToString("\n")).apply()
    }

    fun clearHistory() {
        history = emptyList()
        prefs.edit().putString("hist", "").apply()
    }

    // ---------- Updates ----------
    fun isNew(s: StudioScript) = lastSeen.isNotBlank() && s.updated > lastSeen

    fun markAllSeen() {
        val latest = data.scripts.maxOfOrNull { it.updated } ?: lastSeen
        lastSeen = latest
        prefs.edit().putString("seen", latest).apply()
    }

    // ---------- Navigation ----------
    fun push(r: Route) {
        if (stack.last() != r) stack = stack + r
    }

    fun goTab(t: TabId) {
        stack = listOf(Route.Tab(t))
    }

    /** returns false when nothing left to pop (the activity should finish) */
    fun pop(): Boolean {
        if (stack.size > 1) {
            stack = stack.dropLast(1)
            return true
        }
        val root = stack.first()
        if (root is Route.Tab && root.tab != TabId.Home) {
            goTab(TabId.Home)
            return true
        }
        return false
    }
}
