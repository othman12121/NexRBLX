package com.nexrblx.app

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            NexTheme {
                NexApp(onExit = { finish() })
            }
        }
    }
}

@Composable
fun NexApp(onExit: () -> Unit) {
    val vm: NexVM = viewModel()
    val dir = if (vm.ar) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides dir, LocalAr provides vm.ar) {
        BackHandler { if (!vm.pop()) onExit() }
        val showBar = vm.stack.size == 1

        Scaffold(
            containerColor = NexBg,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = { if (showBar) NexBottomBar(vm) }
        ) { pad ->
            Box(
                Modifier
                    .padding(pad)
                    .then(if (!showBar) Modifier.navigationBarsPadding() else Modifier)
                    .fillMaxSize()
            ) {
                AnimatedContent(
                    targetState = vm.stack,
                    contentKey = { it.last() },
                    transitionSpec = {
                        val push = targetState.size > initialState.size
                        val t: ContentTransform = if (push) {
                            (slideInHorizontally(tween(260)) { it / 6 } + fadeIn(tween(260))) togetherWith
                                fadeOut(tween(160))
                        } else {
                            fadeIn(tween(220)) togetherWith
                                (slideOutHorizontally(tween(220)) { it / 6 } + fadeOut(tween(180)))
                        }
                        t
                    },
                    label = "nav"
                ) { st ->
                    RouteContent(vm, st.last())
                }
            }
        }
    }
}

@Composable
fun RouteContent(vm: NexVM, route: Route) {
    when (route) {
        is Route.Tab -> when (route.tab) {
            TabId.Home -> HomeScreen(vm)
            TabId.Games -> GamesScreen(vm)
            TabId.Library -> LibraryScreen(vm)
            TabId.Codes -> CodesScreen(vm)
            TabId.More -> MoreScreen(vm)
        }
        is Route.GameR -> GameDetailScreen(vm, route.id)
        is Route.ScriptR -> ScriptDetailScreen(vm, route.id)
        is Route.Web -> WebScreen(vm, route.title, route.url)
        is Route.Search -> SearchScreen(vm)
        is Route.Popular -> PopularScreen(vm)
        is Route.Favorites -> FavoritesScreen(vm)
        is Route.Updates -> UpdatesScreen(vm)
        is Route.Request -> RequestScreen(vm)
    }
}

private data class NavItem(val tab: TabId, val en: String, val ar: String, val icon: ImageVector)

private val navItems = listOf(
    NavItem(TabId.Home, "Home", "الرئيسية", Icons.Filled.Home),
    NavItem(TabId.Games, "Games", "الألعاب", Icons.Filled.PlayArrow),
    NavItem(TabId.Library, "Library", "المكتبة", Icons.Filled.Build),
    NavItem(TabId.Codes, "Codes", "الأكواد", Icons.Filled.CheckCircle),
    NavItem(TabId.More, "More", "المزيد", Icons.Filled.Menu)
)

@Composable
fun NexBottomBar(vm: NexVM) {
    val current = (vm.stack.first() as? Route.Tab)?.tab ?: TabId.Home
    NavigationBar(containerColor = NexCard) {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = current == item.tab,
                onClick = { vm.goTab(item.tab) },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(if (vm.ar) item.ar else item.en) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = NexNeon,
                    indicatorColor = NexPurple.copy(alpha = 0.35f),
                    unselectedIconColor = NexMuted,
                    unselectedTextColor = NexMuted
                )
            )
        }
    }
}
