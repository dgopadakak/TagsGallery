package com.dgopadakak.tagsgallery.gallery

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dgopadakak.tagsgallery.core.compose.enums.SortVariant
import com.dgopadakak.tagsgallery.core.local_storage.Repository
import com.dgopadakak.tagsgallery.core.local_storage.models.MediaTagCrossRef
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import com.dgopadakak.tagsgallery.gallery.util.calculateFinalTagIds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    @Stable
    data class UiState(
        val allTags: List<Tag> = emptyList(),
        val sortedFilteredTags: List<Tag> = emptyList(),
        val selectedTagIds: List<Long> = emptyList(),
        val selectedUris: List<Uri> = emptyList(),
        val sortBy: SortVariant = SortVariant.DEFAULT_SORT_VARIANT,
        val filterBy: Tag.Color? = null,
        val activeEditIndividualTags: Uri? = null,
        val perMediaAddedTagIds: Map<Uri, List<Long>> = emptyMap(),
        val perMediaRemovedTagIds: Map<Uri, List<Long>> = emptyMap(),
        val alreadySavedMedia: Set<Uri> = emptySet()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Очистка от несуществующих id в случае их удаления на экране Tags
            repository.getAllTags().collect { tagList ->
                val existingIds = tagList.map { it.id }.toSet()
                val updatedSelectedIds = _uiState.value.selectedTagIds.filter { it in existingIds }
                val updatedPerMediaAddedTagIds = _uiState.value.perMediaAddedTagIds.mapValues { mapEntry ->
                    mapEntry.value.filter { it in existingIds }
                }
                val updatedPerMediaRemovedTagIds = _uiState.value.perMediaRemovedTagIds.mapValues { mapEntry ->
                    mapEntry.value.filter { it in existingIds }
                }
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedTagIds = updatedSelectedIds,
                        perMediaAddedTagIds = updatedPerMediaAddedTagIds,
                        perMediaRemovedTagIds = updatedPerMediaRemovedTagIds
                    )
                }
            }
        }

        viewModelScope.launch {
            combine(
                repository.getAllTags(),
                _uiState.map { it.filterBy }.distinctUntilChanged(),
                _uiState.map { it.sortBy }.distinctUntilChanged()
            ) { tagList, filterVariant, sortVariant ->
                val filteredTags =
                    filterVariant?.let { tagList.filter { it.color == filterVariant } } ?: tagList
                val sortedAndFilteredTags = when (sortVariant) {
                    SortVariant.NAME -> filteredTags.sortedBy { it.name }
                    SortVariant.DATE -> filteredTags.sortedBy { it.lastModified }
                    SortVariant.COLOR -> filteredTags.sortedBy { it.color.compareToken }
                }
                _uiState.update { currentState ->
                    currentState.copy(
                        allTags = tagList,
                        sortedFilteredTags = sortedAndFilteredTags
                    )
                }
            }.collect {}
        }
    }

    fun addSelectedMedia(uris: List<Uri>) = viewModelScope.launch {
        val updatedAlreadyAddedMedia = _uiState.value.alreadySavedMedia.toMutableSet()
        val updatedPerMediaAddedTagIds = _uiState.value.perMediaAddedTagIds.toMutableMap()
        uris.forEach { uri ->
            if (!_uiState.value.alreadySavedMedia.contains(uri)) {
                val alreadyAddedTagsId = repository.getTagIdsForMedia(uri.toString())
                if (alreadyAddedTagsId.isNotEmpty()) {
                    updatedAlreadyAddedMedia.add(uri)
                    updatedPerMediaAddedTagIds += uri to alreadyAddedTagsId.filter { !_uiState.value.selectedTagIds.contains(it) }
                }
            }
        }
        _uiState.update { currentState ->
            currentState.copy(
                selectedUris = (currentState.selectedUris + uris).distinct(),
                alreadySavedMedia = updatedAlreadyAddedMedia,
                perMediaAddedTagIds = updatedPerMediaAddedTagIds
            )
        }
    }

    fun removeSelectedMedia(uri: Uri) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedUris = currentState.selectedUris - uri,
                activeEditIndividualTags = null,
                perMediaAddedTagIds = currentState.perMediaAddedTagIds - uri,
                perMediaRemovedTagIds = currentState.perMediaRemovedTagIds - uri,
                alreadySavedMedia = currentState.alreadySavedMedia - uri
            )
        }
        if (_uiState.value.selectedUris.isEmpty()) {
            resetScreen()
        }
    }

    fun onTagSelected(id: Long) {
        val isAddition = !_uiState.value.selectedTagIds.contains(id)
        _uiState.update { currentState ->
            currentState.copy(
                selectedTagIds = if (isAddition) {
                    currentState.selectedTagIds + id
                } else {
                    currentState.selectedTagIds - id
                },
                perMediaAddedTagIds = currentState.perMediaAddedTagIds.mapValues { mapEntry ->
                    mapEntry.value.filter { it != id }
                },
                perMediaRemovedTagIds = currentState.perMediaRemovedTagIds.mapValues { mapEntry ->
                    mapEntry.value.filter { it != id }
                }
            )
        }
    }

    fun onPerMediaTagToggle(uri: Uri, tagId: Long) {
        val isCommonSelected = _uiState.value.selectedTagIds.contains(tagId)
        if (isCommonSelected) {
            val removed = ArrayList(_uiState.value.perMediaRemovedTagIds.getOrDefault(uri, emptyList()))
            if (removed.contains(tagId)) removed.remove(tagId) else removed.add(tagId)
            _uiState.update { currentState ->
                currentState.copy(
                    perMediaRemovedTagIds = currentState.perMediaRemovedTagIds + mapOf(uri to removed)
                )
            }
        } else {
            val added = ArrayList(_uiState.value.perMediaAddedTagIds.getOrDefault(uri, emptyList()))
            if (added.contains(tagId)) added.remove(tagId) else added.add(tagId)
            _uiState.update { currentState ->
                currentState.copy(
                    perMediaAddedTagIds = currentState.perMediaAddedTagIds + mapOf(uri to added)
                )
            }
        }
    }

    fun setSortBy(sortBy: SortVariant) {
        _uiState.update { it.copy(sortBy = sortBy) }
    }

    fun setFilterBy(filterBy: Tag.Color?) {
        _uiState.update { it.copy(filterBy = filterBy) }
    }

    fun setActiveUriForIndividualTags(uri: Uri?) {
        _uiState.update { currentState ->
            currentState.copy(
                activeEditIndividualTags = if (currentState.activeEditIndividualTags == uri) {
                    null
                } else {
                    uri
                }
            )
        }
    }

    fun onClickSave(contentResolver: ContentResolver) = viewModelScope.launch {
        with(_uiState.value) {
            alreadySavedMedia.forEach { uri ->
                // FIXME: такое удаление приводит к рекомпозиции на экране Search: объединить все в одну транзакцию
                repository.deleteMediaTagCrossRefsByMediaId(uri.toString())
            }
            val allMediaTagCrossRefs = arrayListOf<MediaTagCrossRef>()
            selectedUris.forEach { uri ->
                val finalTagIds = calculateFinalTagIds(
                    selectedCommonTagIds = selectedTagIds,
                    individualAddedTagIds = perMediaAddedTagIds.getOrDefault(uri, emptyList()),
                    individualRemovedTagIds = perMediaRemovedTagIds.getOrDefault(uri, emptyList())
                )

                takeOrReleasePersistableUriPermissionIfNeeded(
                    mediaUri = uri,
                    selectedTagIds = finalTagIds,
                    contentResolver = contentResolver
                )

                allMediaTagCrossRefs += finalTagIds.map { MediaTagCrossRef(uri.toString(), it) }
            }
            repository.insertMediaTagCrossRefs(allMediaTagCrossRefs)
        }
        resetScreen()
    }

    private fun takeOrReleasePersistableUriPermissionIfNeeded(
        mediaUri: Uri,
        selectedTagIds: List<Long>,
        contentResolver: ContentResolver
    ) {
        if (!_uiState.value.alreadySavedMedia.contains(mediaUri)) {
            contentResolver.takePersistableUriPermission(
                mediaUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } else if (selectedTagIds.isEmpty()) {
            // Удаление возможно и путем пустого предобавления - в этом случае тоже отпускаем разрешение
            contentResolver.releasePersistableUriPermission(
                mediaUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    fun onClickReset() {
        resetScreen()
    }

    private fun resetScreen() {
        _uiState.value = UiState(   // Только теги, подтянутые из БД и должны сохраниться
            allTags = _uiState.value.allTags,
            sortedFilteredTags = _uiState.value.sortedFilteredTags
        )
    }
}
