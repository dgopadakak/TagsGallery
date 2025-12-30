package com.dgopadakak.tagsgallery

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.view.WindowCompat
import com.dgopadakak.tagsgallery.ui.MainScreen
import com.dgopadakak.tagsgallery.ui.MainViewModel
import com.dgopadakak.tagsgallery.ui.theme.TagsGalleryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
            keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        ) {
            viewModel.onVolumeKeyPressed()
        }
        return super.onKeyDown(keyCode, event)
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TagsGalleryTheme {
                MainScreen(
                    windowSizeClass = calculateWindowSizeClass(this),
                    windowInsetsControllerCompat = WindowCompat.getInsetsController(window, window.decorView),
                    viewModel = viewModel
                )
            }
        }
    }
}
