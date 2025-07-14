package com.dgopadakak.tagsgallery.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavBackStackEntry
import com.dgopadakak.tagsgallery.R
import com.dgopadakak.tagsgallery.gallery.ui.GalleryScreen
import com.dgopadakak.tagsgallery.search.SearchScreen
import com.dgopadakak.tagsgallery.tags.ui.TagsScreen

enum class Routes(
    val route: String,
    @param:StringRes val screenNameResId: Int,
    val icon: ImageVector,
    val screenMainFunction: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) {
    TAGS("tags", R.string.tags_screen_name, Icons.Default.Menu, { TagsScreen() }),
    GALLERY("gallery", R.string.gallery_screen_name, Icons.Default.AccountBox, { GalleryScreen() }),
    SEARCH("search", R.string.search_screen_name, Icons.Default.Search, { SearchScreen() })
}
