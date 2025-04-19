package com.dgopadakak.tagsgallery.gallery

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dgopadakak.tagsgallery.core.compose.enums.SortVariant
import com.dgopadakak.tagsgallery.core.local_storage.Repository
import com.dgopadakak.tagsgallery.core.local_storage.models.MediaTagCrossRef
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
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
            combine(
                repository.getAllTags(),
                _galleryUiState.map { it.filterBy }.distinctUntilChanged(),
                _galleryUiState.map { it.sortBy }.distinctUntilChanged()
            ) { tagList, filterVariant, sortVariant ->
                val filteredTags =
                    filterVariant?.let { tagList.filter { it.color == filterVariant } } ?: tagList
                val sortedTags = when (sortVariant) {
                    SortVariant.NAME -> filteredTags.sortedBy { it.name }
                    SortVariant.DATE -> filteredTags.sortedBy { it.lastModified }
                    SortVariant.COLOR -> filteredTags.sortedBy { it.color.compareToken }
                }
                // Очистка от несуществующих id
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
                        tags = sortedTags,
                        selectedTagIds = updatedSelectedIds,
                        perMediaAddedTagIds = updatedPerMediaAddedTagIds,
                        perMediaRemovedTagIds = updatedPerMediaRemovedTagIds
                    )
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
                perMediaRemovedTagIds = currentState.perMediaAddedTagIds - uri
            )
        }
        if (_galleryUiState.value.selectedUris.isEmpty()) {
            resetScreen()
        }
    }

    fun onTagSelected(id: Long) {
        // TODO: обеспечить влияние на perMediaAddedTags и perMediaRemovedTags
        _galleryUiState.update { currentState ->
            currentState.copy(
                selectedTagIds = if (_galleryUiState.value.selectedTagIds.contains(id)) {
                    currentState.selectedTagIds - id
                } else {
                    currentState.selectedTagIds + id
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
        if (_galleryUiState.value.activeEditIndividualTags == null || uri == null) {
            _galleryUiState.update { currentState ->
                currentState.copy(
                    activeEditIndividualTags = uri
                )
            }
        }
    }

    fun onClickSave() {
        // TODO: обеспечить сохранение с учетом perMediaAddedTags и perMediaRemovedTags
        _galleryUiState.value.selectedUris.forEach { uri ->
            saveMediaTags(uri.toString(), _galleryUiState.value.selectedTagIds)
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
