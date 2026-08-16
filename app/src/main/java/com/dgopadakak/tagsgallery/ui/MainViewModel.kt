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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
        val tagsEditorState: TagsEditorState? = null
    )

    /**
     * [initialTagIds] - связи медиа на момент открытия редактора. Нужны, чтобы отличить
     * сохранение без изменений и не платить за него инвалидацией БД
     */
    @Stable
    data class TagsEditorState(
        val mediaUri: Uri,
        val selectedTagIds: Set<Long>,
        val initialTagIds: Set<Long>,
        val confirmingRemoval: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private var tagsEditorRequest: Job? = null

    private val _volumeKeyEvents = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 0
    )
    val volumeKeyEvents: SharedFlow<Unit> = _volumeKeyEvents

    /**
     * Теги нужны только диалогу редактирования, поэтому лежат отдельно от [uiState]: иначе
     * правка тегов на экране Tags рекомпозировала бы весь корневой экран
     */
    val allTags: StateFlow<List<Tag>> = repository.getAllTags()
        .map { tagList -> tagList.sortedBy { it.name } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            allTags.collect { tagList ->
                val existingIds = tagList.mapTo(HashSet()) { it.id }
                _uiState.update { currentState ->
                    currentState.copy(
                        // Очистка от несуществующих id в случае их удаления на экране Tags:
                        // внешних ключей у MediaTagCrossRef нет, так что мертвый id ушел бы в БД
                        tagsEditorState = currentState.tagsEditorState?.let { editor ->
                            editor.copy(
                                selectedTagIds = editor.selectedTagIds
                                    .filterTo(HashSet()) { it in existingIds },
                                initialTagIds = editor.initialTagIds
                                    .filterTo(HashSet()) { it in existingIds }
                            )
                        }
                    )
                }
            }
        }
    }

    fun setFullscreenContent(fullscreenContent: FullscreenContentModel?) {
        cancelTagsEditorRequest()
        _uiState.update { it.copy(
            fullscreenContent = fullscreenContent,
            fullscreenAnimated = false,
            isMuted = true,
            tagsEditorState = null
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

    /**
     * Единственная защита от опоздавшего ответа - отмена запроса. Проверять состояние после
     * ответа смысла нет: подменить редактор может и запрос за медиа из той же подборки (свайп и
     * повторный тап, закрытие просмотрщика и открытие заново), а по состоянию такой ответ от
     * актуального не отличить.
     *
     * Поэтому [cancelTagsEditorRequest] обязан звать каждый путь, гасящий редактор в момент,
     * когда запрос может быть в полете, - то есть приходящий не из самого запроса:
     * [setFullscreenContent], [dismissTagsEditor] и повторный [openTagsEditor]. В
     * [saveTagsEditor] отмена не нужна: запрос там в полете не бывает, потому что нажать Save
     * можно только при открытом диалоге, а он модальный и до иконки Edit касания не пропускает
     */
    fun openTagsEditor(mediaUri: Uri) {
        cancelTagsEditorRequest()
        tagsEditorRequest = viewModelScope.launch {
            val linkedTagIds = repository.getTagIdsForMedia(mediaUri.toString()).toSet()
            _uiState.update { currentState ->
                currentState.copy(
                    tagsEditorState = TagsEditorState(
                        mediaUri = mediaUri,
                        selectedTagIds = linkedTagIds,
                        initialTagIds = linkedTagIds
                    )
                )
            }
        }
    }

    /**
     * Отмены достаточно без сверки поколений: и она, и применение результата идут на главном
     * диспетчере, так что проскочить мимо нее ответ не может
     */
    private fun cancelTagsEditorRequest() {
        tagsEditorRequest?.cancel()
        tagsEditorRequest = null
    }

    fun toggleTagsEditorTag(tagId: Long) {
        _uiState.update { currentState ->
            val editor = currentState.tagsEditorState ?: return@update currentState
            val updatedSelection = if (tagId in editor.selectedTagIds) {
                editor.selectedTagIds - tagId
            } else {
                editor.selectedTagIds + tagId
            }
            currentState.copy(tagsEditorState = editor.copy(selectedTagIds = updatedSelection))
        }
    }

    fun saveTagsEditor() {
        val editor = _uiState.value.tagsEditorState ?: return
        // Медиа без единой связи в приложении не существует, поэтому пустой выбор - это не
        // сохранение, а удаление медиа из приложения, и спрашиваем мы о нем отдельно
        if (editor.selectedTagIds.isEmpty()) {
            _uiState.update { it.copy(tagsEditorState = editor.copy(confirmingRemoval = true)) }
            return
        }
        // Перезапись теми же связями стоила бы инвалидации БД, а на ней галерея прогоняет
        // uriExists() по всему списку медиа
        if (editor.selectedTagIds == editor.initialTagIds) {
            _uiState.update { it.copy(tagsEditorState = null) }
            return
        }
        viewModelScope.launch {
            val mediaId = editor.mediaUri.toString()
            repository.deleteAndInsertMediaTagCrossRefs(
                mediaIdsToDeleteCrossRefs = setOf(mediaId),
                crossRefsToAdd = editor.selectedTagIds.map { MediaTagCrossRef(mediaId, it) }
            )
            _uiState.update { currentState ->
                // Пока шла запись, редактор могли закрыть и открыть заново на другом медиа -
                // гасить чужой редактор нельзя, вместе с ним пропал бы несохраненный выбор
                if (currentState.tagsEditorState?.mediaUri != editor.mediaUri) {
                    currentState
                } else {
                    currentState.copy(tagsEditorState = null)
                }
            }
        }
    }

    fun dismissTagsEditor() {
        cancelTagsEditorRequest()
        _uiState.update { it.copy(tagsEditorState = null) }
    }

    fun dismissTagsEditorRemoval() {
        _uiState.update { currentState ->
            val editor = currentState.tagsEditorState ?: return@update currentState
            currentState.copy(tagsEditorState = editor.copy(confirmingRemoval = false))
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
