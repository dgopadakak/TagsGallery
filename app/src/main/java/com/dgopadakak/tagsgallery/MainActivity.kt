package com.dgopadakak.tagsgallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.view.WindowCompat
import com.dgopadakak.tagsgallery.ui.MainScreen
import com.dgopadakak.tagsgallery.ui.theme.TagsGalleryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TagsGalleryTheme {
                MainScreen(
                    windowSizeClass = calculateWindowSizeClass(this),
                    windowInsetsControllerCompat = WindowCompat.getInsetsController(window, window.decorView)
                )
            }
        }
    }
}
