package com.dgopadakak.tagsgallery.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.staticCompositionLocalOf
import com.dgopadakak.tagsgallery.search.ui.fullscreen.FullscreenContentModel

val LocalWindowSizeClass = staticCompositionLocalOf<WindowSizeClass> {
    error("No WindowSizeClass provided")
}

val LocalFullScreenContentState = staticCompositionLocalOf<MutableState<FullscreenContentModel?>> {
    error("No fullscreen content state provided")
}
