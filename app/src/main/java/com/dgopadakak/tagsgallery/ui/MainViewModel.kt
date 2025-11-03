package com.dgopadakak.tagsgallery.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import com.dgopadakak.tagsgallery.core.compose.models.FullscreenContentModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    @Stable
    data class UiState(
        val fullscreenContent: FullscreenContentModel? = null,
        val fullscreenAnimated: Boolean = false,
        val savedIsPlaying: Boolean? = null,
        val savedIsSoundOn: Boolean? = null,    // TODO: рассмотреть возможность сохранения самого ExoPlayer
        val savedVideoTime: Long? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun setFullscreenContent(fullscreenContent: FullscreenContentModel?) {
        _uiState.update { it.copy(
            fullscreenContent = fullscreenContent,
            fullscreenAnimated = false,
            savedIsPlaying = null,
            savedIsSoundOn = null,
            savedVideoTime = null
        ) }
    }

    fun setAnimated(animated: Boolean) {
        _uiState.update { it.copy(fullscreenAnimated = animated) }
    }

    fun setSavedIsPlaying(isPlaying: Boolean?) {
        _uiState.update { it.copy(savedIsPlaying = isPlaying) }
    }


    fun setSavedIsSoundOn(isSoundOn: Boolean?) {
        _uiState.update { it.copy(savedIsSoundOn = isSoundOn) }
    }

    fun setSavedVideoTime(currentVideoTime: Long?) {
        _uiState.update { it.copy(savedVideoTime = currentVideoTime) }
    }
}
