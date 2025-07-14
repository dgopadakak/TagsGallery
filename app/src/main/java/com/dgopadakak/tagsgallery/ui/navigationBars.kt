package com.dgopadakak.tagsgallery.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.dgopadakak.tagsgallery.navigation.Routes

@Composable
internal fun NavigationBar(navController: NavController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    NavigationBar {
        Routes.entries.forEach { screenRoute ->
            NavigationBarItem(
                selected = currentRoute == screenRoute.route,
                onClick = {
                    if (currentRoute != screenRoute.route) {
                        navController.navigate(screenRoute.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(screenRoute.icon, contentDescription = null) },
                label = { Text(stringResource(screenRoute.screenNameResId)) }
            )
        }
    }
}

@Composable
fun NavigationRail(navController: NavController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    NavigationRail(
        // Вместо этого отступы от Scaffold уже учтены в Row, в котором лежит этот компонент
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Routes.entries.forEach { screenRoute ->
            NavigationRailItem(
                selected = currentRoute == screenRoute.route,
                onClick = {
                    if (currentRoute != screenRoute.route) {
                        navController.navigate(screenRoute.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(screenRoute.icon, contentDescription = null) },
                label = { Text(stringResource(screenRoute.screenNameResId)) },
                alwaysShowLabel = false
            )
        }
    }
}
