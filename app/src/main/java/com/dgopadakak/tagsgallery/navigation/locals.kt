package com.dgopadakak.tagsgallery.navigation

import android.net.Uri
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.staticCompositionLocalOf

val LocalWindowSizeClass = staticCompositionLocalOf<WindowSizeClass> {
    error("No WindowSizeClass provided")
}

val LocalFullScreenContentState = staticCompositionLocalOf<MutableState<Uri?>> {
    error("No fullscreen content state provided")
}
