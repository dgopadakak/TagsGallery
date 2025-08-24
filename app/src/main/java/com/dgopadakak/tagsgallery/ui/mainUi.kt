package com.dgopadakak.tagsgallery.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dgopadakak.tagsgallery.navigation.LocalFullScreenContentState
import com.dgopadakak.tagsgallery.navigation.LocalWindowSizeClass
import com.dgopadakak.tagsgallery.navigation.Routes
import com.dgopadakak.tagsgallery.search.ui.fullscreen.FullScreenMediaView
import com.dgopadakak.tagsgallery.search.ui.fullscreen.FullscreenContentModel

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun MainScreen(
    windowSizeClass: WindowSizeClass
) {
    val navController = rememberNavController()
    // TODO: при расширении на другие платформы - сделать больше, чем 2 варианта UI навигации
    val useRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    val contentForFullScreen: MutableState<FullscreenContentModel?> =
        rememberSaveable { mutableStateOf(null) }

    CompositionLocalProvider(
        LocalWindowSizeClass provides windowSizeClass,
        LocalFullScreenContentState provides contentForFullScreen
    ) {
        // Box - правильный контейнер для накладываемых друг на друга Composable. Это первый уровень
        // иерархии Composable, так что без него происходят артефакты вроде залипания FullScreenMediaView
        Box {
            Scaffold(
                bottomBar = {
                    if (!useRail) {
                        NavigationBar(navController)
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail) {
                        NavigationRail(navController)
                    }

                    NavHost(
                        navController = navController,
                        startDestination = Routes.TAGS.route
                    ) {
                        composable(route = Routes.TAGS.route) { Routes.TAGS.ScreenForRoute() }
                        composable(route = Routes.GALLERY.route) { Routes.GALLERY.ScreenForRoute() }
                        composable(route = Routes.SEARCH.route) { Routes.SEARCH.ScreenForRoute() }
                    }
                }
            }

            contentForFullScreen.value?.let { content ->
                FullScreenMediaView(
                    content,
                    onClose = { contentForFullScreen.value = null }
                )
            }
        }
    }
}
