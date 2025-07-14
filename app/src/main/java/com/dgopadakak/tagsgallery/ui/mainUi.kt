package com.dgopadakak.tagsgallery.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dgopadakak.tagsgallery.navigation.Routes

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun MainScreen(
    windowSizeClass: WindowSizeClass
) {
    val navController = rememberNavController()
    // TODO: при расширении на другие платформы - сделать больше, чем 2 варианта UI навигации
    val useRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

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
                composable(route = Routes.TAGS.route, content = Routes.TAGS.screenMainFunction)
                composable(route = Routes.GALLERY.route, content = Routes.GALLERY.screenMainFunction)
                composable(route = Routes.SEARCH.route, content = Routes.SEARCH.screenMainFunction)
            }
        }
    }
}
