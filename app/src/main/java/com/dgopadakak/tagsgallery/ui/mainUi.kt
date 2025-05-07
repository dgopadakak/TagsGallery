package com.dgopadakak.tagsgallery.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dgopadakak.tagsgallery.navigation.Routes

@Composable
internal fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TAGS.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = Routes.TAGS.route, content = Routes.TAGS.screenMainFunction)
            composable(route = Routes.GALLERY.route, content = Routes.GALLERY.screenMainFunction)
            composable(route = Routes.SEARCH.route, content = Routes.SEARCH.screenMainFunction)
        }
    }
}

@Composable
fun NavigationBar(navController: NavController) {
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
