package com.dgopadakak.tagsgallery.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dgopadakak.tagsgallery.core.compose.models.FullscreenContentModel
import com.dgopadakak.tagsgallery.core.local_storage.Repository
import com.dgopadakak.tagsgallery.core.local_storage.models.MediaTagCrossRef
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
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
        val isMuted: Boolean = true,
        val allTags: List<Tag> = emptyList(),
        val tagsEditor: TagsEditorState? = null
    )

    @Stable
    data class TagsEditorState(
        val mediaUri: Uri,
        val selectedTagIds: Set<Long>,
        val confirmingRemoval: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _volumeKeyEvents = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 0
    )
    val volumeKeyEvents: SharedFlow<Unit> = _volumeKeyEvents

    init {
        viewModelScope.launch {
            repository.getAllTags().collect { tagList ->
                val existingIds = tagList.mapTo(HashSet()) { it.id }
                _uiState.update { currentState ->
                    currentState.copy(
                        allTags = tagList.sortedBy { it.name },
                        // Очистка от несуществующих id в случае их удаления на экране Tags:
                        // внешних ключей у MediaTagCrossRef нет, так что мертвый id ушел бы в БД
                        tagsEditor = currentState.tagsEditor?.let { editor ->
                            editor.copy(
                                selectedTagIds = editor.selectedTagIds
                                    .filterTo(HashSet()) { it in existingIds }
                            )
                        }
                    )
                }
            }
        }
    }

    fun setFullscreenContent(fullscreenContent: FullscreenContentModel?) {
        _uiState.update { it.copy(
            fullscreenContent = fullscreenContent,
            fullscreenAnimated = false,
            isMuted = true,
            tagsEditor = null
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

    fun openTagsEditor(mediaUri: Uri) {
        viewModelScope.launch {
            val linkedTagIds = repository.getTagIdsForMedia(mediaUri.toString()).toSet()
            _uiState.update { currentState ->
                currentState.copy(
                    tagsEditor = TagsEditorState(
                        mediaUri = mediaUri,
                        selectedTagIds = linkedTagIds
                    )
                )
            }
        }
    }

    fun toggleTagsEditorTag(tagId: Long) {
        _uiState.update { currentState ->
            val editor = currentState.tagsEditor ?: return@update currentState
            val updatedSelection = if (tagId in editor.selectedTagIds) {
                editor.selectedTagIds - tagId
            } else {
                editor.selectedTagIds + tagId
            }
            currentState.copy(tagsEditor = editor.copy(selectedTagIds = updatedSelection))
        }
    }

    fun saveTagsEditor() {
        val editor = _uiState.value.tagsEditor ?: return
        // Медиа без единой связи в приложении не существует, поэтому пустой выбор - это не
        // сохранение, а удаление медиа из приложения, и спрашиваем мы о нем отдельно
        if (editor.selectedTagIds.isEmpty()) {
            _uiState.update { it.copy(tagsEditor = editor.copy(confirmingRemoval = true)) }
            return
        }
        viewModelScope.launch {
            val mediaId = editor.mediaUri.toString()
            repository.deleteAndInsertMediaTagCrossRefs(
                mediaIdsToDeleteCrossRefs = setOf(mediaId),
                crossRefsToAdd = editor.selectedTagIds.map { MediaTagCrossRef(mediaId, it) }
            )
            _uiState.update { it.copy(tagsEditor = null) }
        }
    }

    fun dismissTagsEditor() {
        _uiState.update { it.copy(tagsEditor = null) }
    }

    fun dismissTagsEditorRemoval() {
        _uiState.update { currentState ->
            val editor = currentState.tagsEditor ?: return@update currentState
            currentState.copy(tagsEditor = editor.copy(confirmingRemoval = false))
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
