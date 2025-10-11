package com.dgopadakak.tagsgallery.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.dgopadakak.tagsgallery.R
import com.dgopadakak.tagsgallery.core.compose.models.FullscreenContentModel
import com.dgopadakak.tagsgallery.gallery.ui.GalleryScreen
import com.dgopadakak.tagsgallery.search.ui.SearchScreen
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
    fun ScreenForRoute(
        onFullscreenContentSelected: (FullscreenContentModel) -> Unit = { }
    ) {
        when (this) {
            TAGS -> TagsScreen()
            GALLERY -> GalleryScreen(LocalWindowSizeClass.current)
            SEARCH -> SearchScreen(onFullscreenContentSelected)
        }
    }
}
