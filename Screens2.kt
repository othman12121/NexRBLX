package com.nexrblx.app

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ================================================================ LIBRARY

@Composable
fun LibraryScreen(vm: NexVM) {
    var cat by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf("new") }
    val cats = remember(vm.data) { vm.data.scripts.map { it.category }.filter { it.isNotBlank() }.distinct().sorted() }
    val list = remember(vm.data, cat, sort) {
        val f = vm.data.scripts.filter { cat.isEmpty() || it.category == cat }
        if (sort == "new") f.sortedByDescending { it.updated } else f.sortedByDescending { it.popularity }
    }
    Column(Modifier.fillMaxSize()) {
        Header(tr("Script Library", "مكتبة السكربتات"), actions = {
            IconButton(onClick = { vm.push(Route.Search) }) { Icon(Icons.Filled.Search, null, tint = NexText) }
        })
        Text(
            tr("Lua scripts for your own Roblox Studio maps", "سكربتات Lua لخرائط Roblox Studio الخاصة بك"),
            color = NexMuted, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        ChipRow {
            NexChip(tr("All", "الكل"), cat.isEmpty()) { cat = "" }
            cats.forEach { c -> NexChip(c, cat == c) { cat = c } }
        }
        Spacer(Modifier.height(8.dp))
        ChipRow {
            NexChip(tr("Newest", "الأحدث"), sort == "new") { sort = "new" }
            NexChip(tr("Popular", "الأشهر"), sort == "popular") { sort = "popular" }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(list, key = { it.id }) { s -> ScriptRow(vm, s) }
        }
    }
}

// ================================================================ CODES

@Composable
fun CodesScreen(vm: NexVM) {
    var status by remember { mutableStateOf("all") }
    val list = remember(vm.data, status) {
        vm.data.codes.filter { status == "all" || it.status == status }
            .sortedWith(compareBy<RedeemCode> { it.status == "expired" }.thenByDescending { it.added })
    }
    Column(Modifier.fillMaxSize()) {
        Header(tr("Game Codes", "أكواد الألعاب"))
        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            item {
                NexCardBox(Modifier.fillMaxWidth(), onClick = {
                    vm.push(Route.Web("Beebom – Roblox Codes", Config.BEEBOM_HUB))
                }) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            tr("Codes for all Roblox games", "أكواد روبلوكس لجميع الألعاب"),
                            fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = NexText
                        )
                        Text(
                            tr(
                                "Browse Beebom's full, regularly-updated list right inside the app.",
                                "تصفّح قائمة Beebom الكاملة والمحدّثة باستمرار داخل التطبيق."
                            ),
                            fontSize = 13.sp, color = NexMuted, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )
                        NexButton(tr("Open Beebom list", "فتح قائمة Beebom")) {
                            vm.push(Route.Web("Beebom – Roblox Codes", Config.BEEBOM_HUB))
                        }
                    }
                }
            }
            item { SectionTitle(tr("My codes list", "قائمتي من الأكواد")) }
            item {
                Row(Modifier.padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NexChip(tr("All", "الكل"), status == "all") { status = "all" }
                    NexChip(tr("New", "جديد"), status == "new") { status = "new" }
                    NexChip(tr("Active", "فعّال"), status == "active") { status = "active" }
                    NexChip(tr("Expired", "منتهي"), status == "expired") { status = "expired" }
                }
            }
            if (list.isEmpty()) {
                item { EmptyState(tr("No codes here yet", "لا توجد أكواد هنا بعد")) }
            } else {
                items(list, key = { it.gameId + it.code }) { c ->
                    Column { CodeRow(vm, c); Spacer(Modifier.height(10.dp)) }
                }
            }
        }
    }
}

// ================================================================ SEARCH

@Composable
fun nexFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NexPurple,
    unfocusedBorderColor = NexBorder,
    focusedTextColor = NexText,
    unfocusedTextColor = NexText,
    cursorColor = NexNeon,
    focusedContainerColor = NexCard,
    unfocusedContainerColor = NexCard,
    focusedPlaceholderColor = NexMuted,
    unfocusedPlaceholderColor = NexMuted
)

@Composable
fun SearchScreen(vm: NexVM) {
    var q by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    val t = q.trim().lowercase()
    val games = remember(vm.data, t) {
        if (t.length < 2) emptyList()
        else vm.data.games.filter { it.name.lowercase().contains(t) || it.category.lowercase().contains(t) }
    }
    val scripts = remember(vm.data, t) {
        if (t.length < 2) emptyList()
        else vm.data.scripts.filter {
            it.title.lowercase().contains(t) || it.description(false).lowercase().contains(t) ||
                it.descAr.contains(t) || it.category.lowercase().contains(t) || it.code.lowercase().contains(t)
        }
    }
    val codes = remember(vm.data, t) {
        if (t.length < 2) emptyList()
        else vm.data.codes.filter { it.code.lowercase().contains(t) || vm.gameName(it.gameId).lowercase().contains(t) }
    }

    Column(Modifier.fillMaxSize()) {
        Header(tr("Search", "بحث"), onBack = { vm.pop() })
        OutlinedTextField(
            value = q,
            onValueChange = { q = it },
            placeholder = { Text(tr("Search maps, scripts, codes…", "ابحث عن ماب أو سكربت أو كود…")) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, null, tint = NexMuted) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { vm.addHistory(q) }),
            colors = nexFieldColors(),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).focusRequester(focus)
        )
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (t.length < 2) {
                if (vm.history.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                tr("Recent searches", "عمليات البحث الأخيرة"), color = NexText,
                                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { vm.clearHistory() }) {
                                Text(tr("Clear", "مسح"), color = NexPurple)
                            }
                        }
                    }
                    items(vm.history, key = { "h$it" }) { h ->
                        NexCardBox(Modifier.fillMaxWidth(), onClick = { q = h }) {
                            Text(h, color = NexText, modifier = Modifier.padding(14.dp))
                        }
                    }
                } else {
                    item { EmptyState(tr("Type at least 2 letters", "اكتب حرفين على الأقل")) }
                }
            } else if (games.isEmpty() && scripts.isEmpty() && codes.isEmpty()) {
                item { EmptyState(tr("No results", "لا توجد نتائج")) }
            } else {
                if (games.isNotEmpty()) {
                    item { Text(tr("Games & maps", "الألعاب والمابات"), color = NexMuted, fontSize = 13.sp) }
                    items(games, key = { "g" + it.id }) { g ->
                        Column { GameRow(vm, g) }
                    }
                }
                if (scripts.isNotEmpty()) {
                    item { Text(tr("Scripts", "السكربتات"), color = NexMuted, fontSize = 13.sp) }
                    items(scripts, key = { "s" + it.id }) { s -> ScriptRow(vm, s) }
                }
                if (codes.isNotEmpty()) {
                    item { Text(tr("Codes", "الأكواد"), color = NexMuted, fontSize = 13.sp) }
                    items(codes, key = { "c" + it.gameId + it.code }) { c -> CodeRow(vm, c) }
                }
            }
        }
    }
    // save the query to history when leaving results via a click is handled by submit; keep it simple
}

// ================================================================ POPULAR

@Composable
fun PopularScreen(vm: NexVM) {
    var tab by remember { mutableStateOf("scripts") }
    val scripts = remember(vm.data) { vm.data.scripts.sortedByDescending { it.popularity } }
    val games = remember(vm.data) { vm.data.games.sortedByDescending { it.popularity } }
    Column(Modifier.fillMaxSize()) {
        Header(tr("Popular Scripts", "السكربتات الأكثر شعبية"), onBack = { vm.pop() })
        ChipRow {
            NexChip(tr("Scripts", "السكربتات"), tab == "scripts") { tab = "scripts" }
            NexChip(tr("Games", "الألعاب"), tab == "games") { tab = "games" }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (tab == "scripts") {
                items(scripts.size) { i -> ScriptRow(vm, scripts[i], rank = i + 1) }
            } else {
                items(games.size) { i -> GameRow(vm, games[i], rank = i + 1) }
            }
        }
    }
}

// ================================================================ FAVORITES

@Composable
fun FavoritesScreen(vm: NexVM) {
    val favScripts = vm.data.scripts.filter { vm.favs.contains("s:${it.id}") }
    val favGames = vm.data.games.filter { vm.favs.contains("g:${it.id}") }
    Column(Modifier.fillMaxSize()) {
        Header(tr("Favorites", "المفضلة"), onBack = { vm.pop() })
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (favScripts.isEmpty() && favGames.isEmpty()) {
                item {
                    EmptyState(
                        tr(
                            "Tap the heart on any script or game to save it. Saved items work offline.",
                            "اضغط على القلب في أي سكربت أو لعبة لحفظها. العناصر المحفوظة تعمل بدون إنترنت."
                        )
                    )
                }
            }
            if (favScripts.isNotEmpty()) {
                item { Text(tr("Scripts", "السكربتات"), color = NexMuted, fontSize = 13.sp) }
                items(favScripts, key = { "s" + it.id }) { s -> ScriptRow(vm, s) }
            }
            if (favGames.isNotEmpty()) {
                item { Text(tr("Games", "الألعاب"), color = NexMuted, fontSize = 13.sp) }
                items(favGames, key = { "g" + it.id }) { g -> GameRow(vm, g) }
            }
        }
    }
}

// ================================================================ UPDATES

@Composable
fun UpdatesScreen(vm: NexVM) {
    val list = remember(vm.data) { vm.data.scripts.sortedByDescending { it.updated } }
    val newCount = list.count { vm.isNew(it) }
    Column(Modifier.fillMaxSize()) {
        Header(tr("Updates", "التحديثات"), onBack = { vm.pop() }, actions = {
            IconButton(onClick = { vm.refresh() }) { Icon(Icons.Filled.Refresh, null, tint = NexText) }
        })
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (newCount > 0) tr("$newCount new since your last visit", "$newCount جديد منذ آخر زيارة")
                else tr("You're up to date", "أنت على آخر تحديث"),
                color = if (newCount > 0) NexGreen else NexMuted, fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            if (newCount > 0) {
                TextButton(onClick = { vm.markAllSeen() }) {
                    Text(tr("Mark all seen", "تحديد الكل كمقروء"), color = NexPurple)
                }
            }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(list, key = { it.id }) { s -> ScriptRow(vm, s) }
        }
    }
}

// ================================================================ REQUEST

@Composable
fun RequestScreen(vm: NexVM) {
    val ctx = LocalContext.current
    var type by remember { mutableStateOf("Script") }
    var name by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        Header(tr("Request Script", "طلب سكربت"), onBack = { vm.pop() })
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                ChipRow {
                    NexChip(tr("Studio script", "سكربت Studio"), type == "Script") { type = "Script" }
                    NexChip(tr("Game / map", "لعبة / ماب"), type == "Game") { type = "Game" }
                    NexChip(tr("Codes", "أكواد"), type == "Codes") { type = "Codes" }
                }
            }
            item {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(tr("Name", "الاسم")) }, singleLine = true,
                    colors = nexFieldColors(), modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = details, onValueChange = { details = it },
                    label = { Text(tr("Details", "التفاصيل")) },
                    colors = nexFieldColors(), minLines = 4, modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                NexButton(tr("Send request", "إرسال الطلب"), Modifier.fillMaxWidth()) {
                    val title = "[$type] ${name.trim()}"
                    val body = details.trim() + "\n\n— NexRBLX app"
                    if (name.isBlank()) return@NexButton
                    if (Config.REQUEST_REPO.contains("YOUR_")) {
                        shareText(ctx, "$title\n$body")
                    } else {
                        openUrl(
                            ctx,
                            "https://github.com/${Config.REQUEST_REPO}/issues/new?title=${Uri.encode(title)}&body=${Uri.encode(body)}"
                        )
                    }
                }
            }
            item {
                Text(
                    tr(
                        "Requests are for your own Studio maps and authorized use. Cheats or exploits for other people's games are not accepted.",
                        "الطلبات لخرائط Studio الخاصة بك والاستخدام المصرح به. لا تُقبل غش أو استغلال ثغرات ألعاب الآخرين."
                    ),
                    color = NexMuted, fontSize = 12.sp
                )
            }
        }
    }
}

// ================================================================ MORE

@Composable
private fun MoreItem(title: String, subtitle: String, onClick: () -> Unit) {
    NexCardBox(Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = NexText, fontSize = 15.sp)
            Text(subtitle, color = NexMuted, fontSize = 12.sp)
        }
    }
}

@Composable
fun MoreScreen(vm: NexVM) {
    val d = vm.data
    val site = if (d.website.isNotBlank()) d.website else Config.BEEBOM_HUB
    val siteName = if (d.websiteName.isNotBlank()) d.websiteName else "Beebom"
    val newCount = d.scripts.count { vm.isNew(it) }

    Column(Modifier.fillMaxSize()) {
        Header(tr("More", "المزيد"))
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                MoreItem(tr("Popular Scripts", "الأكثر شعبية"), tr("Top scripts and games", "أفضل السكربتات والألعاب")) {
                    vm.push(Route.Popular)
                }
            }
            item {
                MoreItem(tr("Favorites", "المفضلة"), tr("${vm.favs.size} saved – works offline", "${vm.favs.size} محفوظ – يعمل بدون إنترنت")) {
                    vm.push(Route.Favorites)
                }
            }
            item {
                MoreItem(
                    tr("Updates", "التحديثات"),
                    if (newCount > 0) tr("$newCount new", "$newCount جديد") else tr("Latest changes", "آخر التغييرات")
                ) { vm.push(Route.Updates) }
            }
            item {
                MoreItem(tr("Search", "بحث"), tr("Maps, scripts and codes", "المابات والسكربتات والأكواد")) {
                    vm.push(Route.Search)
                }
            }
            item {
                MoreItem(tr("Request Script", "طلب سكربت"), tr("Ask for a new script or game", "اطلب سكربت أو لعبة جديدة")) {
                    vm.push(Route.Request)
                }
            }
            item {
                MoreItem(siteName, tr("Open the website inside the app", "افتح الموقع داخل التطبيق")) {
                    vm.push(Route.Web(siteName, site))
                }
            }
            item {
                NexCardBox(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(tr("Language", "اللغة"), fontWeight = FontWeight.Bold, color = NexText, fontSize = 15.sp)
                        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NexChip("العربية", vm.ar) { vm.setArabic(true) }
                            NexChip("English", !vm.ar) { vm.setArabic(false) }
                        }
                    }
                }
            }
            item {
                NexCardBox(Modifier.fillMaxWidth(), onClick = { vm.refresh() }) {
                    Column(Modifier.padding(14.dp)) {
                        Text(tr("Refresh data", "تحديث البيانات"), fontWeight = FontWeight.Bold, color = NexText, fontSize = 15.sp)
                        val src = when (vm.source) {
                            "online" -> tr("Online – up to date", "متصل – محدّث")
                            "cache" -> tr("Offline – saved copy", "بدون إنترنت – نسخة محفوظة")
                            else -> tr("Built-in data", "بيانات مدمجة")
                        }
                        Text(
                            "$src · v${d.version} · ${d.scripts.size} ${tr("scripts", "سكربت")} · ${d.games.size} ${tr("games", "لعبة")}",
                            color = NexMuted, fontSize = 12.sp
                        )
                    }
                }
            }
            item {
                Text(
                    "NexRBLX 0.2.0 · " + tr(
                        "Not affiliated with Roblox Corporation.",
                        "غير تابع لشركة Roblox."
                    ),
                    color = NexMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

// ================================================================ IN-APP WEB

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebScreen(vm: NexVM, title: String, url: String) {
    val ctx = LocalContext.current
    val target = url
    var web by remember { mutableStateOf<WebView?>(null) }
    var progress by remember { mutableStateOf(0) }

    BackHandler {
        val w = web
        if (w != null && w.canGoBack()) w.goBack() else vm.pop()
    }
    DisposableEffect(Unit) {
        onDispose {
            web?.destroy()
            web = null
        }
    }

    Column(Modifier.fillMaxSize()) {
        Header(title, onBack = { vm.pop() }, actions = {
            TextButton(onClick = { openUrl(ctx, web?.url ?: url) }) {
                Text(tr("Browser", "المتصفح"), color = NexPurple)
            }
        })
        if (progress in 1..99) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                color = NexNeon,
                trackColor = NexCard,
                modifier = Modifier.fillMaxWidth()
            )
        }
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { c ->
                WebView(c).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    setBackgroundColor(0xFF07040F.toInt())
                    webViewClient = WebViewClient()
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress
                        }
                    }
                    loadUrl(target)
                    web = this
                }
            }
        )
    }
}
