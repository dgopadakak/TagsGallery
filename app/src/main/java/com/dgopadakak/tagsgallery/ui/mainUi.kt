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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dgopadakak.tagsgallery.navigation.LocalWindowSizeClass
import com.dgopadakak.tagsgallery.navigation.Routes

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun MainScreen(
    windowSizeClass: WindowSizeClass,
    windowInsetsControllerCompat: WindowInsetsControllerCompat,
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    // TODO: при расширении на другие платформы - сделать больше, чем 2 варианта UI навигации
    val useRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    val uiState by viewModel.uiState.collectAsState()

    CompositionLocalProvider(
        LocalWindowSizeClass provides windowSizeClass
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
                        composable(route = Routes.SEARCH.route) { Routes.SEARCH.ScreenForRoute { viewModel.setFullscreenContent(it) } }
                    }
                }
            }

            uiState.fullscreenContent?.let { content ->
                FullScreenMediaView(
                    contentModel = content,
                    windowInsetsControllerCompat = windowInsetsControllerCompat,
                    alreadyAnimated = uiState.fullscreenAnimated,
                    onAnimated = { viewModel.setAnimated(true) },
                    onClose = {
                        windowInsetsControllerCompat.show(WindowInsetsCompat.Type.systemBars())
                        viewModel.setFullscreenContent(null)
                    }
                )
            }
        }
    }
}
