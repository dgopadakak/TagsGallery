package com.dgopadakak.tagsgallery.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dgopadakak.tagsgallery.gallery.ui.GalleryScreen
import com.dgopadakak.tagsgallery.search.SearchScreen
import com.dgopadakak.tagsgallery.tags.TagsScreen

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
            startDestination = "tags",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("tags") { TagsScreen() }
            composable("gallery") { GalleryScreen() }
            composable("search") { SearchScreen() }
        }
    }
}

@Composable
fun NavigationBar(navController: NavController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "tags",
            onClick = {
                if (currentRoute != "tags") {
                    navController.navigate("tags") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = { Icon(Icons.Default.Menu, contentDescription = "Tags") },
            label = { Text("Tags") }
        )
        NavigationBarItem(
            selected = currentRoute == "gallery",
            onClick = {
                if (currentRoute != "gallery") {
                    navController.navigate("gallery") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = { Icon(Icons.Default.AccountBox, contentDescription = "Gallery") },
            label = { Text("Gallery") }
        )
        NavigationBarItem(
            selected = currentRoute == "search",
            onClick = {
                if (currentRoute != "search") {
                    navController.navigate("search") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("Search") }
        )
    }
}
