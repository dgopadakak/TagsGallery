package com.dgopadakak.tagsgallery.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dgopadakak.tagsgallery.core.compose.models.FullscreenContentModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    @Stable
    data class UiState(
        val fullscreenContent: FullscreenContentModel? = null,
        val fullscreenAnimated: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _volumeKeyEvents = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 0
    )
    val volumeKeyEvents: SharedFlow<Unit> = _volumeKeyEvents

    fun setFullscreenContent(fullscreenContent: FullscreenContentModel?) {
        _uiState.update { it.copy(
            fullscreenContent = fullscreenContent,
            fullscreenAnimated = false
        ) }
    }

    fun setAnimated(animated: Boolean) {
        _uiState.update { it.copy(fullscreenAnimated = animated) }
    }

    fun onVolumeKeyPressed() {
        viewModelScope.launch {
            _volumeKeyEvents.emit(Unit)
        }
    }
}
