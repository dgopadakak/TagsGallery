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
        NavigationBarItem(
            selected = currentRoute == Routes.TAGS.route,
            onClick = {
                if (currentRoute != Routes.TAGS.route) {
                    navController.navigate(Routes.TAGS.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = { Icon(Routes.TAGS.icon, contentDescription = null) },
            label = { Text(stringResource(Routes.TAGS.screenNameResId)) }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.GALLERY.route,
            onClick = {
                if (currentRoute != Routes.GALLERY.route) {
                    navController.navigate(Routes.GALLERY.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = { Icon(Routes.GALLERY.icon, contentDescription = null) },
            label = { Text(stringResource(Routes.GALLERY.screenNameResId)) }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.SEARCH.route,
            onClick = {
                if (currentRoute != Routes.SEARCH.route) {
                    navController.navigate(Routes.SEARCH.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = { Icon(Routes.SEARCH.icon, contentDescription = null) },
            label = { Text(stringResource(Routes.SEARCH.screenNameResId)) }
        )
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
        NavigationRailItem(
            selected = currentRoute == Routes.TAGS.route,
            onClick = {
                if (currentRoute != Routes.TAGS.route) {
                    navController.navigate(Routes.TAGS.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = { Icon(Routes.TAGS.icon, contentDescription = null) },
            label = { Text(stringResource(Routes.TAGS.screenNameResId)) },
            alwaysShowLabel = false
        )
        NavigationRailItem(
            selected = currentRoute == Routes.GALLERY.route,
            onClick = {
                if (currentRoute != Routes.GALLERY.route) {
                    navController.navigate(Routes.GALLERY.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = { Icon(Routes.GALLERY.icon, contentDescription = null) },
            label = { Text(stringResource(Routes.GALLERY.screenNameResId)) },
            alwaysShowLabel = false
        )
        NavigationRailItem(
            selected = currentRoute == Routes.SEARCH.route,
            onClick = {
                if (currentRoute != Routes.SEARCH.route) {
                    navController.navigate(Routes.SEARCH.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = { Icon(Routes.SEARCH.icon, contentDescription = null) },
            label = { Text(stringResource(Routes.SEARCH.screenNameResId)) },
            alwaysShowLabel = false
        )
    }
}
