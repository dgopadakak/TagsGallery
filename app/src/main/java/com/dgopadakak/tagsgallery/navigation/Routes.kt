package com.dgopadakak.tagsgallery.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Sell
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.dgopadakak.tagsgallery.R
import com.dgopadakak.tagsgallery.core.compose.models.FullscreenContentModel
import com.dgopadakak.tagsgallery.add.ui.AddScreen
import com.dgopadakak.tagsgallery.gallery.ui.GalleryScreen
import com.dgopadakak.tagsgallery.tags.ui.TagsScreen

enum class Routes(
    val route: String,
    @param:StringRes val screenNameResId: Int,
    val icon: ImageVector
) {
    TAGS("tags", R.string.tags_screen_name, Icons.Default.Sell),
    ADD("add", R.string.add_screen_name, Icons.Default.AddPhotoAlternate),
    GALLERY("gallery", R.string.gallery_screen_name, Icons.Default.PhotoLibrary);

    @Composable
    fun ScreenForRoute(
        onFullscreenContentSelected: (FullscreenContentModel) -> Unit = { }
    ) {
        when (this) {
            TAGS -> TagsScreen()
            ADD -> AddScreen(LocalWindowSizeClass.current)
            GALLERY -> GalleryScreen(
                onFullscreenContentSelected = onFullscreenContentSelected,
                windowSizeClass = LocalWindowSizeClass.current
            )
        }
    }
}
