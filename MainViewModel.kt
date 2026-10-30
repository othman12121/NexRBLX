package com.nexrblx.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val store = Store(app)

    val latest = MutableStateFlow(store.latest())
    val popular = MutableStateFlow(store.popular())
    val favorites = MutableStateFlow(store.favorites())
    val loading = MutableStateFlow(false)
    val offline = MutableStateFlow(false)
    val lastUpdate = MutableStateFlow(store.lastUpdate)
    val selected = MutableStateFlow<ScriptItem?>(null)

    val query = MutableStateFlow("")
    val filter = MutableStateFlow(Filter.ALL)
    val results = MutableStateFlow<List<ScriptItem>>(emptyList())
    val searching = MutableStateFlow(false)
    val searchError = MutableStateFlow(false)
    val canMore = MutableStateFlow(false)

    private var page = 1
    private var searchJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            loading.value = true
            try {
                val l = Api.latest()
                val p = Api.popular()
                if (l.isNotEmpty()) {
                    latest.value = l
                    store.saveLatest(l)
                }
                if (p.isNotEmpty()) {
                    popular.value = p
                    store.savePopular(p)
                }
                val now = System.currentTimeMillis()
                store.lastUpdate = now
                lastUpdate.value = now
                offline.value = false
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                offline.value = true
            } finally {
                loading.value = false
            }
        }
    }

    fun search(append: Boolean = false) {
        searchJob?.cancel()
        val target = if (append) page + 1 else 1
        searchJob = viewModelScope.launch {
            searching.value = true
            searchError.value = false
            try {
                val r = Api.browse(query.value, filter.value, target)
                val sorted = if (filter.value == Filter.VIEWS) {
                    r.sortedByDescending { it.views }
                } else {
                    r.sortedByDescending { it.updatedAt }
                }
                results.value = if (append) (results.value + sorted).distinctBy { it.uid } else sorted
                page = target
                canMore.value = r.size >= 20
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                searchError.value = true
                if (!append) {
                    results.value = offlineSearch()
                    canMore.value = false
                }
            } finally {
                searching.value = false
            }
        }
    }

    private fun offlineSearch(): List<ScriptItem> {
        val q = query.value.trim().lowercase()
        val pool = (latest.value + popular.value + favorites.value).distinctBy { it.uid }
        return pool
            .filter { q.isEmpty() || it.title.lowercase().contains(q) || it.gameName.lowercase().contains(q) }
            .filter {
                when (filter.value) {
                    Filter.UNIVERSAL -> it.universal
                    Filter.NOKEY -> !it.hasKey
                    Filter.VERIFIED -> it.verified
                    else -> true
                }
            }
    }

    fun openGame(name: String) {
        query.value = name
        filter.value = Filter.ALL
    }

    fun open(item: ScriptItem) {
        selected.value = item
    }

    fun toggleFav(item: ScriptItem) {
        val cur = favorites.value
        val next = if (cur.any { it.uid == item.uid }) {
            cur.filter { it.uid != item.uid }
        } else {
            listOf(item) + cur
        }
        favorites.value = next
        store.saveFavorites(next)
    }
}
