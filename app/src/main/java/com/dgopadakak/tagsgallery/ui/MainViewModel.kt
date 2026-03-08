package com.dgopadakak.tagsgallery.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dgopadakak.tagsgallery.core.compose.models.FullscreenContentModel
import com.dgopadakak.tagsgallery.core.local_storage.Repository
import com.dgopadakak.tagsgallery.core.local_storage.util.hasPersistedReadPermission
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    @param:ApplicationContext private val appContext: Context
) : ViewModel() {
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

    fun deleteMedia(uri: Uri) {
        viewModelScope.launch {
            val mediaId = uri.toString()
            repository.deleteMediaTagCrossRefsByMediaId(mediaId)

            val contentResolver = appContext.contentResolver
            if (contentResolver.hasPersistedReadPermission(uri)) {
                contentResolver.releasePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
    }
}
