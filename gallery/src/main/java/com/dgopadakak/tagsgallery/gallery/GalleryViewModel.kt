package com.dgopadakak.tagsgallery.gallery

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
    data class GalleryUiState(
        val tags: List<Tag> = emptyList(),
        val selectedTagIds: List<Long> = emptyList(),
        val selectedUris: List<Uri> = emptyList(),
        val sortBy: SortVariant = SortVariant.DEFAULT_SORT_VARIANT,
        val filterBy: Tag.Color? = null,
        val activeEditIndividualTags: Uri? = null,
        val perMediaAddedTagIds: Map<Uri, List<Long>> = mapOf(),
        val perMediaRemovedTagIds: Map<Uri, List<Long>> = mapOf()
    )

    private val _galleryUiState = MutableStateFlow(GalleryUiState())
    val galleryUiState = _galleryUiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Очистка от несуществующих id в случае их удаления на экране Tags
            repository.getAllTags().collect { tagList ->
                val existingIds = tagList.map { it.id }.toSet()
                val updatedSelectedIds = _galleryUiState.value.selectedTagIds.filter { it in existingIds }
                val updatedPerMediaAddedTagIds = _galleryUiState.value.perMediaAddedTagIds.mapValues {
                    it.value.filter { it in existingIds }
                }
                val updatedPerMediaRemovedTagIds = _galleryUiState.value.perMediaRemovedTagIds.mapValues {
                    it.value.filter { it in existingIds }
                }
                _galleryUiState.update { currentState ->
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
                _galleryUiState.map { it.filterBy }.distinctUntilChanged(),
                _galleryUiState.map { it.sortBy }.distinctUntilChanged()
            ) { tagList, filterVariant, sortVariant ->
                val filteredTags =
                    filterVariant?.let { tagList.filter { it.color == filterVariant } } ?: tagList
                val sortedAndFilteredTags = when (sortVariant) {
                    SortVariant.NAME -> filteredTags.sortedBy { it.name }
                    SortVariant.DATE -> filteredTags.sortedBy { it.lastModified }
                    SortVariant.COLOR -> filteredTags.sortedBy { it.color.compareToken }
                }
                _galleryUiState.update { currentState ->
                    currentState.copy(tags = sortedAndFilteredTags)
                }
            }.collect {}
        }
    }

    fun addSelectedMedia(uris: List<Uri>) {
        _galleryUiState.update { currentState ->
            val updatedUris = (currentState.selectedUris + uris).distinct()
            currentState.copy(selectedUris = updatedUris)
        }
    }

    fun removeSelectedMedia(uri: Uri) {
        _galleryUiState.update { currentState ->
            currentState.copy(
                selectedUris = currentState.selectedUris - uri,
                activeEditIndividualTags = null,
                perMediaAddedTagIds = currentState.perMediaAddedTagIds - uri,
                perMediaRemovedTagIds = currentState.perMediaRemovedTagIds - uri
            )
        }
        if (_galleryUiState.value.selectedUris.isEmpty()) {
            resetScreen()
        }
    }

    fun onTagSelected(id: Long) {
        val isAddition = !_galleryUiState.value.selectedTagIds.contains(id)
        _galleryUiState.update { currentState ->
            currentState.copy(
                selectedTagIds = if (isAddition) {
                    currentState.selectedTagIds + id
                } else {
                    currentState.selectedTagIds - id
                },
                perMediaAddedTagIds = currentState.perMediaAddedTagIds.mapValues {
                    it.value.filter { it != id }
                },
                perMediaRemovedTagIds = currentState.perMediaRemovedTagIds.mapValues {
                    it.value.filter { it != id }
                }
            )
        }
    }

    fun onPerMediaTagToggle(uri: Uri, tagId: Long) {
        val isCommonSelected = _galleryUiState.value.selectedTagIds.contains(tagId)
        if (isCommonSelected) {
            val removed = arrayListOf<Long>()
            _galleryUiState.value.perMediaRemovedTagIds.getOrDefault(uri, defaultValue = null)?.forEach { id ->
                removed.add(id)
            }
            if (removed.contains(tagId)) removed.remove(tagId) else removed.add(tagId)
            _galleryUiState.update { currentState ->
                currentState.copy(
                    perMediaRemovedTagIds = currentState.perMediaRemovedTagIds + mapOf(Pair(uri, removed))
                )
            }
        } else {
            val added = arrayListOf<Long>()
            _galleryUiState.value.perMediaAddedTagIds.getOrDefault(uri, defaultValue = null)?.forEach { id ->
                added.add(id)
            }
            if (added.contains(tagId)) added.remove(tagId) else added.add(tagId)
            _galleryUiState.update { currentState ->
                currentState.copy(
                    perMediaAddedTagIds = currentState.perMediaAddedTagIds + mapOf(Pair(uri, added))
                )
            }
        }
    }

    fun setSortBy(sortBy: SortVariant) {
        _galleryUiState.update { it.copy(sortBy = sortBy) }
    }

    fun setFilterBy(filterBy: Tag.Color?) {
        _galleryUiState.update { it.copy(filterBy = filterBy) }
    }

    fun setActiveUriForIndividualTags(uri: Uri?) {
        _galleryUiState.update { currentState ->
            currentState.copy(
                activeEditIndividualTags = if (currentState.activeEditIndividualTags == uri) {
                    null
                } else {
                    uri
                }
            )
        }
    }

    fun onClickSave() {
        with(_galleryUiState.value) {
            selectedUris.forEach { uri ->
                saveMediaTags(
                    mediaId = uri.toString(),
                    selectedTagIds = calculateFinalTagIds(
                        selectedCommonTagIds = selectedTagIds,
                        individualAddedTagIds = perMediaAddedTagIds.getOrDefault(uri, emptyList()),
                        individualRemovedTagIds = perMediaRemovedTagIds.getOrDefault(uri, emptyList())
                    )
                )
            }
        }
        resetScreen()
    }

    private fun saveMediaTags(mediaId: String, selectedTagIds: List<Long>) {
        viewModelScope.launch {
            selectedTagIds.forEach { tagId ->
                repository.insertMediaTagCrossRef(MediaTagCrossRef(mediaId, tagId))
            }
        }
    }

    fun onClickReset() {
        resetScreen()
    }

    private fun resetScreen() {
        _galleryUiState.update { currentState ->
            currentState.copy(
                selectedTagIds = emptyList(),
                selectedUris = emptyList(),
                perMediaAddedTagIds = emptyMap(),
                perMediaRemovedTagIds = emptyMap()
            )
        }
        setSortBy(SortVariant.DEFAULT_SORT_VARIANT)
        setFilterBy(null)
    }
}
