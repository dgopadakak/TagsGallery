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
        val fullscreenAnimated: Boolean = false,
        val isMuted: Boolean = true
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
            fullscreenAnimated = false,
            isMuted = true
        ) }
    }

    fun setAnimated(animated: Boolean) {
        _uiState.update { it.copy(fullscreenAnimated = animated) }
    }

    fun setMuted(muted: Boolean) {
        _uiState.update { it.copy(isMuted = muted) }
    }

    /**
     * Этот метод не устанавливает isMuted напрямую, так как в нем мы не можем проверить, какой тип
     * медиа отображается в данный момент, а unmute при просмотре фото неуместен. Поэтому через этот
     * метод при помощи [volumeKeyEvents] информация о нажатии попадает в просмотрщик, а уже из него
     * может быть вызван метод [setMuted], который и установит isMuted.
     */
    fun onVolumeKeyPressed() {
        viewModelScope.launch {
            _volumeKeyEvents.emit(Unit)
        }
    }
}
