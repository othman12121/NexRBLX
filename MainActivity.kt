package com.nexrblx.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleBackgroundRefresh()
        setContent {
            NexTheme {
                NexApp()
            }
        }
    }

    private fun scheduleBackgroundRefresh() {
        val request = PeriodicWorkRequestBuilder<RefreshWorker>(6, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "nex_refresh",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}

fun NavController.goTab(route: String) {
    navigate(route) {
        popUpTo("home") { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun NexApp(vm: MainViewModel = viewModel()) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val tabs = listOf(
        Triple("home", R.string.nav_home, Icons.Filled.Home),
        Triple("search", R.string.nav_search, Icons.Filled.Search),
        Triple("favs", R.string.nav_favs, Icons.Filled.Favorite),
        Triple("about", R.string.nav_about, Icons.Filled.Info)
    )

    Scaffold(
        containerColor = NexBg,
        bottomBar = {
            if (route != "detail") {
                NavigationBar(containerColor = NexCard) {
                    tabs.forEach { (r, label, icon) ->
                        NavigationBarItem(
                            selected = route == r,
                            onClick = { nav.goTab(r) },
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(stringResource(label)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NexNeon,
                                selectedTextColor = NexNeon,
                                indicatorColor = NexCard2,
                                unselectedIconColor = NexMuted,
                                unselectedTextColor = NexMuted
                            )
                        )
                    }
                }
            }
        }
    ) { pad ->
        NavHost(nav, startDestination = "home", modifier = Modifier.padding(pad)) {
            composable("home") { HomeScreen(vm, nav) }
            composable("search") { SearchScreen(vm, nav) }
            composable("favs") { FavoritesScreen(vm, nav) }
            composable("about") { AboutScreen(vm) }
            composable("detail") { DetailScreen(vm, nav) }
        }
    }
}
