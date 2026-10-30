package com.nexrblx.app

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ================================================================ shared rows / cards

@Composable
fun ScriptRow(vm: NexVM, s: StudioScript, showGame: Boolean = true, rank: Int? = null) {
    val ar = vm.ar
    NexCardBox(Modifier.fillMaxWidth(), onClick = { vm.push(Route.ScriptR(s.id)) }) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            if (rank != null) {
                Text(
                    "$rank",
                    color = NexNeon,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    modifier = Modifier.width(30.dp)
                )
            }
            Column(Modifier.weight(1f)) {
                if (showGame && s.gameId.isNotBlank()) {
                    Text(vm.gameName(s.gameId), fontSize = 11.sp, color = NexMuted)
                }
                Text(
                    s.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NexText,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    s.description(ar), fontSize = 12.sp, color = NexMuted,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Tag("v${s.version}")
                    if (s.category.isNotBlank()) Tag(s.category, NexNeon)
                    if (vm.isNew(s)) Tag(tr("NEW", "جديد"), NexGreen)
                }
            }
            if (vm.isFav("s:${s.id}")) {
                Icon(Icons.Filled.Favorite, null, tint = NexNeon, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun GameRow(vm: NexVM, g: Game, rank: Int? = null) {
    NexCardBox(Modifier.fillMaxWidth(), onClick = { vm.push(Route.GameR(g.id)) }) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (rank != null) {
                Text(
                    "$rank", color = NexNeon, fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp, modifier = Modifier.width(30.dp)
                )
            }
            Thumb(g.image, g.name, Modifier.size(56.dp))
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    g.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NexText,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    g.description(vm.ar), fontSize = 12.sp, color = NexMuted,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Tag(
                        if (g.kind == "template") tr("Template", "قالب") else tr("Game", "لعبة"),
                        if (g.kind == "template") NexNeon else NexPurple
                    )
                    if (g.category.isNotBlank()) Tag(g.category, NexMuted)
                }
            }
            if (vm.isFav("g:${g.id}")) {
                Icon(Icons.Filled.Favorite, null, tint = NexNeon, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun CodeRow(vm: NexVM, c: RedeemCode, showGame: Boolean = true) {
    NexCardBox(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                if (showGame) Text(vm.gameName(c.gameId), fontSize = 11.sp, color = NexMuted)
                Text(
                    c.code, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
                    fontSize = 16.sp, color = if (c.status == "expired") NexMuted else NexString
                )
                if (c.rewardText(vm.ar).isNotBlank()) {
                    Text(c.rewardText(vm.ar), fontSize = 12.sp, color = NexMuted, modifier = Modifier.padding(top = 2.dp))
                }
                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusTag(c.status)
                    if (c.added.isNotBlank()) Tag(c.added, NexMuted)
                }
            }
            CopyButton(c.code, tr("Copy", "نسخ"), filled = false)
        }
    }
}

// ================================================================ HOME

@Composable
fun HomeScreen(vm: NexVM) {
    val d = vm.data
    val latest = remember(d) { d.scripts.sortedByDescending { it.updated }.take(8) }
    val popular = remember(d) { d.scripts.sortedByDescending { it.popularity }.take(4) }
    val games = remember(d) { d.games.sortedByDescending { it.popularity }.take(10) }
    val newCodes = remember(d) { d.codes.filter { it.status == "new" }.take(3) }

    Column(Modifier.fillMaxSize()) {
        Header(
            title = "",
            actions = {
                IconButton(onClick = { vm.push(Route.Search) }) {
                    Icon(Icons.Filled.Search, null, tint = NexText)
                }
                IconButton(onClick = { vm.push(Route.Favorites) }) {
                    Icon(Icons.Filled.Favorite, null, tint = NexNeon)
                }
            }
        )
        if (vm.loading && d.scripts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NexPurple)
            }
            return@Column
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    NexLogo(34)
                    Text(
                        tr("Studio scripts, game codes & more", "سكربتات Studio وأكواد الألعاب والمزيد"),
                        color = NexMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp)
                    )
                    if (vm.source == "cache") {
                        Text(
                            tr("Offline mode – showing saved data", "وضع بدون إنترنت – عرض البيانات المحفوظة"),
                            color = NexAmber, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            item {
                SectionTitle(tr("Latest scripts", "أحدث السكربتات"), tr("See all", "عرض الكل")) {
                    vm.goTab(TabId.Library)
                }
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(latest, key = { it.id }) { s ->
                        NexCardBox(Modifier.width(230.dp), onClick = { vm.push(Route.ScriptR(s.id)) }) {
                            Column(Modifier.padding(14.dp)) {
                                Text(vm.gameName(s.gameId), fontSize = 11.sp, color = NexMuted)
                                Text(
                                    s.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NexText,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    s.description(vm.ar), fontSize = 12.sp, color = NexMuted, maxLines = 2,
                                    overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp)
                                )
                                Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Tag("v${s.version}")
                                    Tag(s.updated, NexMuted)
                                }
                            }
                        }
                    }
                }
            }
            item {
                SectionTitle(tr("Popular", "الأكثر شعبية"), tr("See all", "عرض الكل")) { vm.push(Route.Popular) }
            }
            items(popular, key = { "p" + it.id }) { s ->
                Box(Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) { ScriptRow(vm, s) }
            }
            item {
                SectionTitle(tr("Games & maps", "الألعاب والمابات"), tr("See all", "عرض الكل")) {
                    vm.goTab(TabId.Games)
                }
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(games, key = { it.id }) { g ->
                        Column(
                            Modifier.width(96.dp).bounceClick { vm.push(Route.GameR(g.id)) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Thumb(g.image, g.name, Modifier.size(96.dp))
                            Text(
                                g.name, fontSize = 12.sp, color = NexText, maxLines = 1,
                                overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
            if (newCodes.isNotEmpty()) {
                item {
                    SectionTitle(tr("New codes", "أكواد جديدة"), tr("See all", "عرض الكل")) {
                        vm.goTab(TabId.Codes)
                    }
                }
                items(newCodes, key = { "c" + it.gameId + it.code }) { c ->
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) { CodeRow(vm, c) }
                }
            }
        }
    }
}

// ================================================================ GAMES

@Composable
fun GamesScreen(vm: NexVM) {
    var kind by remember { mutableStateOf("all") }
    var sort by remember { mutableStateOf("popular") }
    val list = remember(vm.data, kind, sort) {
        val f = vm.data.games.filter { kind == "all" || it.kind == kind }
        when (sort) {
            "new" -> f.sortedByDescending { it.updated }
            "az" -> f.sortedBy { it.name.lowercase() }
            else -> f.sortedByDescending { it.popularity }
        }
    }
    Column(Modifier.fillMaxSize()) {
        Header(tr("Games", "الألعاب"), actions = {
            IconButton(onClick = { vm.push(Route.Search) }) { Icon(Icons.Filled.Search, null, tint = NexText) }
        })
        ChipRow {
            NexChip(tr("All", "الكل"), kind == "all") { kind = "all" }
            NexChip(tr("Games", "ألعاب"), kind == "game") { kind = "game" }
            NexChip(tr("Templates", "قوالب"), kind == "template") { kind = "template" }
        }
        Spacer(Modifier.height(8.dp))
        ChipRow {
            NexChip(tr("Popular", "الأشهر"), sort == "popular") { sort = "popular" }
            NexChip(tr("Newest", "الأحدث"), sort == "new") { sort = "new" }
            NexChip("A-Z", sort == "az") { sort = "az" }
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(list, key = { it.id }) { g -> GameRow(vm, g) }
        }
    }
}

// ================================================================ GAME DETAIL

@Composable
fun GameDetailScreen(vm: NexVM, id: String) {
    val ctx = LocalContext.current
    val g = vm.game(id)
    if (g == null) {
        Column { Header(tr("Not found", "غير موجود"), onBack = { vm.pop() }) }
        return
    }
    var sort by remember { mutableStateOf("new") }
    val scripts = remember(vm.data, id, sort) {
        val f = vm.data.scripts.filter { it.gameId == id }
        if (sort == "new") f.sortedByDescending { it.updated } else f.sortedByDescending { it.popularity }
    }
    val codes = remember(vm.data, id) { vm.data.codes.filter { it.gameId == id } }
    val fav = vm.isFav("g:$id")

    Column(Modifier.fillMaxSize()) {
        Header(g.name, onBack = { vm.pop() }, actions = {
            IconButton(onClick = { vm.toggleFav("g:$id") }) {
                Icon(
                    if (fav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, null,
                    tint = if (fav) NexNeon else NexText
                )
            }
        })
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Thumb(g.image, g.name, Modifier.fillMaxWidth().height(170.dp))
                    Text(
                        g.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = NexText,
                        modifier = Modifier.padding(top = 14.dp)
                    )
                    Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Tag(
                            if (g.kind == "template") tr("Template", "قالب") else tr("Game", "لعبة"),
                            if (g.kind == "template") NexNeon else NexPurple
                        )
                        if (g.category.isNotBlank()) Tag(g.category, NexMuted)
                        if (g.updated.isNotBlank()) Tag(g.updated, NexMuted)
                    }
                    Text(
                        g.description(vm.ar), fontSize = 14.sp, color = NexMuted,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    if (g.kind == "game") {
                        Row(
                            Modifier.padding(top = 14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            NexButton(tr("Codes (Beebom)", "الأكواد (Beebom)"), Modifier.weight(1f)) {
                                vm.push(
                                    Route.Web(
                                        "Beebom – ${g.name}",
                                        Config.BEEBOM_SEARCH + Uri.encode(g.name + " codes")
                                    )
                                )
                            }
                            NexButton(tr("ScriptBlox info", "معلومات ScriptBlox"), Modifier.weight(1f), filled = false) {
                                openUrl(ctx, Config.SCRIPTBLOX + Uri.encode(g.name))
                            }
                        }
                    }
                }
            }
            if (codes.isNotEmpty()) {
                item { SectionTitle(tr("Redeem codes", "أكواد المكافآت")) }
                items(codes, key = { "c" + it.code }) { c ->
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) { CodeRow(vm, c, showGame = false) }
                }
            }
            item { SectionTitle(tr("Studio scripts", "سكربتات Studio")) }
            item {
                ChipRow {
                    NexChip(tr("Newest", "الأحدث"), sort == "new") { sort = "new" }
                    NexChip(tr("Popular", "الأشهر"), sort == "popular") { sort = "popular" }
                }
            }
            if (scripts.isEmpty()) {
                item {
                    EmptyState(
                        if (g.kind == "game")
                            tr(
                                "No Studio scripts for this game. Studio scripts are for your own maps – see Library.",
                                "لا توجد سكربتات Studio لهذه اللعبة. سكربتات Studio لخرائطك أنت – شاهد المكتبة."
                            )
                        else tr("No scripts yet", "لا توجد سكربتات بعد")
                    )
                }
            } else {
                items(scripts, key = { "s" + it.id }) { s ->
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) { ScriptRow(vm, s, showGame = false) }
                }
            }
        }
    }
}

// ================================================================ SCRIPT DETAIL

@Composable
fun ScriptDetailScreen(vm: NexVM, id: String) {
    val ctx = LocalContext.current
    val s = vm.script(id)
    if (s == null) {
        Column { Header(tr("Not found", "غير موجود"), onBack = { vm.pop() }) }
        return
    }
    val g = vm.game(s.gameId)
    val fav = vm.isFav("s:$id")

    Column(Modifier.fillMaxSize()) {
        Header(s.title, onBack = { vm.pop() }, actions = {
            IconButton(onClick = {
                shareText(ctx, "${s.title} (v${s.version})\n\n${s.code}\n\n— NexRBLX")
            }) { Icon(Icons.Filled.Share, null, tint = NexText) }
            IconButton(onClick = { vm.toggleFav("s:$id") }) {
                Icon(
                    if (fav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, null,
                    tint = if (fav) NexNeon else NexText
                )
            }
        })
        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Thumb(g?.image ?: "", g?.name ?: s.title, Modifier.size(72.dp))
                    Column(Modifier.padding(start = 14.dp)) {
                        if (g != null) {
                            Text(
                                g.name, fontSize = 13.sp, color = NexPurple, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.bounceClick { vm.push(Route.GameR(g.id)) }
                            )
                        }
                        Text(s.title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = NexText)
                    }
                }
            }
            item {
                Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Tag(tr("Version", "الإصدار") + " ${s.version}")
                    Tag(tr("Updated", "التحديث") + " ${s.updated}", NexMuted)
                    if (s.category.isNotBlank()) Tag(s.category, NexNeon)
                }
                Text(
                    s.description(vm.ar), fontSize = 14.sp, color = NexMuted,
                    modifier = Modifier.padding(top = 12.dp)
                )
                if (s.place.isNotBlank()) {
                    Text(
                        tr("Where to put it", "مكان وضعه") + ": ${s.place}",
                        fontSize = 12.sp, color = NexAmber, modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            item {
                Row(
                    Modifier.padding(top = 16.dp, bottom = 8.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Lua", fontWeight = FontWeight.Bold, color = NexText, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    CopyButton(s.code, tr("Copy Script", "نسخ السكربت"))
                }
                LuaViewer(s.code, Modifier.fillMaxWidth())
                Text(
                    tr(
                        "For your own Roblox Studio places and authorized testing only.",
                        "للاستخدام في خرائط Roblox Studio الخاصة بك والاختبار المصرح به فقط."
                    ),
                    fontSize = 11.sp, color = NexMuted, modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}
