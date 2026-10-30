package com.nexrblx.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/* ---------- helpers ---------- */

private fun compact(n: Long): String = when {
    n >= 1_000_000 -> String.format(Locale.US, "%.1fM", n / 1_000_000.0)
    n >= 1_000 -> String.format(Locale.US, "%.1fK", n / 1_000.0)
    else -> n.toString()
}

private fun formatTime(ms: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(ms))

private fun sourceName(i: ScriptItem) = if (i.source == "scriptblox") "ScriptBlox" else "RoScripts"

private fun topGames(list: List<ScriptItem>): List<String> =
    list.map { it.gameName }
        .filter { it.isNotBlank() }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .take(12)
        .map { it.key }

@Composable
private fun gameLabel(i: ScriptItem): String =
    if (i.gameName.isNotBlank()) i.gameName else stringResource(R.string.universal)

/* ---------- small components ---------- */

@Composable
fun Logo(size: Int = 24) {
    Row {
        Text("Nex", fontSize = size.sp, fontWeight = FontWeight.ExtraBold, color = NexText)
        Text("RBLX", fontSize = size.sp, fontWeight = FontWeight.ExtraBold, color = NexNeon)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = NexText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun Badge(text: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(NexPurple.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) { Text(text, fontSize = 11.sp, color = NexPurple) }
}

@Composable
private fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) NexPurple else NexCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, color = if (selected) Color.White else NexMuted, fontSize = 13.sp)
    }
}

@Composable
private fun Banner(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x33FBBF24))
            .padding(12.dp)
    ) { Text(text, color = Color(0xFFFBBF24), fontSize = 13.sp) }
}

@Composable
private fun Thumb(item: ScriptItem, modifier: Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NexCard2),
        contentAlignment = Alignment.Center
    ) {
        if (item.thumb.isNotBlank()) {
            AsyncImage(
                model = item.thumb,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                item.title.take(1).uppercase(),
                color = NexPurple,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ScriptCard(item: ScriptItem, fav: Boolean, onClick: () -> Unit, onFav: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NexCard)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Thumb(item, Modifier.size(64.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.title,
                    color = NexText,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    gameLabel(item),
                    color = NexMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Badge(sourceName(item))
                    Text(
                        compact(item.views) + " · " + item.updatedAt.take(10),
                        fontSize = 11.sp,
                        color = NexMuted
                    )
                }
            }
            IconButton(onClick = onFav) {
                Icon(
                    if (fav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    tint = if (fav) NexNeon else NexMuted
                )
            }
        }
    }
}

@Composable
private fun Attribution() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(stringResource(R.string.powered_sb), color = NexMuted, fontSize = 12.sp)
        Text(stringResource(R.string.data_rs), color = NexMuted, fontSize = 12.sp)
        Text(stringResource(R.string.dev_by), color = NexPurple, fontSize = 12.sp)
    }
}

/* ---------- Home ---------- */

@Composable
fun HomeScreen(vm: MainViewModel, nav: NavController) {
    val latest by vm.latest.collectAsState()
    val popular by vm.popular.collectAsState()
    val favs by vm.favorites.collectAsState()
    val loading by vm.loading.collectAsState()
    val offline by vm.offline.collectAsState()
    val last by vm.lastUpdate.collectAsState()
    val favIds = favs.map { it.uid }.toSet()
    val games = remember(latest, popular) { topGames(latest + popular) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Logo(28)
                IconButton(onClick = { vm.refresh() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, tint = NexPurple)
                }
            }
        }
        if (loading) item {
            LinearProgressIndicator(Modifier.fillMaxWidth(), color = NexNeon, trackColor = NexCard)
        }
        if (offline) item { Banner(stringResource(R.string.offline_banner)) }
        if (last > 0) item {
            Text(stringResource(R.string.last_update, formatTime(last)), color = NexMuted, fontSize = 12.sp)
        }
        if (games.isNotEmpty()) {
            item { SectionTitle(stringResource(R.string.games)) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(games) { g ->
                        Chip(g, false) {
                            vm.openGame(g)
                            nav.goTab("search")
                        }
                    }
                }
            }
        }
        item { SectionTitle(stringResource(R.string.latest)) }
        items(latest.take(10)) { s ->
            ScriptCard(
                s, s.uid in favIds,
                onClick = { vm.open(s); nav.navigate("detail") },
                onFav = { vm.toggleFav(s) }
            )
        }
        item { SectionTitle(stringResource(R.string.popular)) }
        items(popular.take(10)) { s ->
            ScriptCard(
                s, s.uid in favIds,
                onClick = { vm.open(s); nav.navigate("detail") },
                onFav = { vm.toggleFav(s) }
            )
        }
        if (latest.isEmpty() && popular.isEmpty() && !loading) item {
            Text(
                stringResource(R.string.no_results),
                color = NexMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        item { Attribution() }
    }
}

/* ---------- Search ---------- */

@Composable
fun SearchScreen(vm: MainViewModel, nav: NavController) {
    val query by vm.query.collectAsState()
    val filter by vm.filter.collectAsState()
    val results by vm.results.collectAsState()
    val searching by vm.searching.collectAsState()
    val error by vm.searchError.collectAsState()
    val canMore by vm.canMore.collectAsState()
    val favs by vm.favorites.collectAsState()
    val favIds = favs.map { it.uid }.toSet()

    LaunchedEffect(query, filter) {
        delay(350)
        vm.search()
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { vm.query.value = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.search_hint), color = NexMuted) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NexPurple,
                    unfocusedBorderColor = NexLine,
                    focusedTextColor = NexText,
                    unfocusedTextColor = NexText,
                    cursorColor = NexNeon,
                    focusedContainerColor = NexCard,
                    unfocusedContainerColor = NexCard
                )
            )
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Filter.values().toList()) { f ->
                    Chip(stringResource(f.label), f == filter) { vm.filter.value = f }
                }
            }
            Spacer(Modifier.height(10.dp))
            if (searching) {
                LinearProgressIndicator(Modifier.fillMaxWidth(), color = NexNeon, trackColor = NexCard)
            }
            if (error) Banner(stringResource(R.string.search_offline))
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(results) { s ->
                ScriptCard(
                    s, s.uid in favIds,
                    onClick = { vm.open(s); nav.navigate("detail") },
                    onFav = { vm.toggleFav(s) }
                )
            }
            if (canMore && !searching) item {
                Button(
                    onClick = { vm.search(append = true) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NexPurple)
                ) { Text(stringResource(R.string.load_more)) }
            }
            if (!searching && results.isEmpty()) item {
                Text(
                    stringResource(R.string.no_results),
                    color = NexMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            item { Attribution() }
        }
    }
}

/* ---------- Favorites ---------- */

@Composable
fun FavoritesScreen(vm: MainViewModel, nav: NavController) {
    val favs by vm.favorites.collectAsState()
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { SectionTitle(stringResource(R.string.nav_favs)) }
        if (favs.isEmpty()) item {
            Text(stringResource(R.string.no_favs), color = NexMuted)
        }
        items(favs) { s ->
            ScriptCard(
                s, true,
                onClick = { vm.open(s); nav.navigate("detail") },
                onFav = { vm.toggleFav(s) }
            )
        }
        item { Attribution() }
    }
}

/* ---------- About ---------- */

@Composable
private fun SourceCard(title: String, host: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NexCard)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = NexText, fontWeight = FontWeight.SemiBold)
            Text(host, color = NexPurple, fontSize = 13.sp)
        }
    }
}

@Composable
fun AboutScreen(vm: MainViewModel) {
    val ui = LocalUriHandler.current
    val last by vm.lastUpdate.collectAsState()
    val loading by vm.loading.collectAsState()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Logo(32)
        Text(stringResource(R.string.dev_by), color = NexPurple, fontWeight = FontWeight.SemiBold)
        Text(stringResource(R.string.version, "1.0.0"), color = NexMuted, fontSize = 12.sp)
        SectionTitle(stringResource(R.string.about_sources))
        SourceCard(stringResource(R.string.powered_sb), "scriptblox.com") { ui.openUri("https://scriptblox.com") }
        SourceCard(stringResource(R.string.data_rs), "roscripts.io") { ui.openUri("https://roscripts.io") }
        Text(stringResource(R.string.about_note), color = NexMuted, fontSize = 13.sp)
        Text(stringResource(R.string.bg_note), color = NexMuted, fontSize = 13.sp)
        if (last > 0) {
            Text(stringResource(R.string.last_update, formatTime(last)), color = NexMuted, fontSize = 12.sp)
        }
        Button(
            onClick = { vm.refresh() },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NexPurple)
        ) { Text(stringResource(R.string.refresh_now)) }
    }
}

/* ---------- Details ---------- */

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = NexMuted, fontSize = 14.sp)
        Text(value, color = NexText, fontSize = 14.sp)
    }
}

@Composable
fun DetailScreen(vm: MainViewModel, nav: NavController) {
    val current by vm.selected.collectAsState()
    val favs by vm.favorites.collectAsState()
    val ui = LocalUriHandler.current
    val item = current
    if (item == null) {
        LaunchedEffect(Unit) { nav.popBackStack() }
        return
    }
    val fav = favs.any { it.uid == item.uid }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(NexCard2)
        ) {
            if (item.thumb.isNotBlank()) {
                AsyncImage(
                    model = item.thumb,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            IconButton(
                onClick = { nav.popBackStack() },
                modifier = Modifier
                    .padding(8.dp)
                    .background(Color(0x99000000), CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = Color.White
                )
            }
        }
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(item.title, color = NexText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(gameLabel(item), color = NexMuted, fontSize = 15.sp)
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (item.verified) Badge(stringResource(R.string.verified))
                if (item.universal) Badge(stringResource(R.string.universal))
                Badge(stringResource(if (item.hasKey) R.string.key_required else R.string.no_key))
                if (item.patched) Badge(stringResource(R.string.patched))
                Badge(stringResource(if (item.free) R.string.free else R.string.paid))
            }
            Spacer(Modifier.height(4.dp))
            InfoRow(stringResource(R.string.source), sourceName(item))
            InfoRow(stringResource(R.string.added), item.createdAt.take(10))
            InfoRow(stringResource(R.string.updated), item.updatedAt.take(10))
            InfoRow(stringResource(R.string.views), compact(item.views))
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { ui.openUri(item.url) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NexPurple)
            ) { Text(stringResource(R.string.open_original)) }
            OutlinedButton(
                onClick = { vm.toggleFav(item) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    if (fav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    tint = NexNeon
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(if (fav) R.string.remove_fav else R.string.add_fav),
                    color = NexText
                )
            }
            Text(
                stringResource(if (item.source == "scriptblox") R.string.powered_sb else R.string.data_rs),
                color = NexMuted,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
