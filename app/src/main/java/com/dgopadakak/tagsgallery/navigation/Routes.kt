package com.dgopadakak.tagsgallery.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.dgopadakak.tagsgallery.R
import com.dgopadakak.tagsgallery.gallery.ui.GalleryScreen
import com.dgopadakak.tagsgallery.search.SearchScreen
import com.dgopadakak.tagsgallery.tags.ui.TagsScreen

enum class Routes(
    val route: String,
    @param:StringRes val screenNameResId: Int,
    val icon: ImageVector
) {
    TAGS("tags", R.string.tags_screen_name, Icons.Default.Menu),
    GALLERY("gallery", R.string.gallery_screen_name, Icons.Default.AccountBox),
    SEARCH("search", R.string.search_screen_name, Icons.Default.Search);

    @Composable
    fun ScreenForRoute(windowSizeClass: WindowSizeClass) {
        when (this) {
            TAGS -> TagsScreen()
            GALLERY -> GalleryScreen(windowSizeClass)
            SEARCH -> SearchScreen()
        }
    }
}
