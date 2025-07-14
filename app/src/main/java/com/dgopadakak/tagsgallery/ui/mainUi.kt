package com.dgopadakak.tagsgallery.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dgopadakak.tagsgallery.navigation.Routes

@Composable
internal fun MainScreen() {
    val navController = rememberNavController()
    val configuration = LocalConfiguration.current

    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600
    val useRail = isLandscape || isTablet

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
