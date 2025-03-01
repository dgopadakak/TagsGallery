package com.dgopadakak.tagsgallery.gallery

import android.net.Uri
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

    data class GalleryTagsUiState(
        val tags: List<Tag> = emptyList(),
        val selectedTagIds: List<Long> = emptyList(),
        val sortBy: SortVariant = SortVariant.DEFAULT_SORT_VARIANT,
        val filterBy: Tag.Color? = null,
    )

    data class GalleryMediaUiState(
        val selectedUris: List<Uri> = emptyList()
    )

    private val _galleryTagsUiState = MutableStateFlow(GalleryTagsUiState())
    val galleryTagsUiState = _galleryTagsUiState.asStateFlow()

    private val _galleryMediaUiState = MutableStateFlow(GalleryMediaUiState())
    val galleryMediaUiState = _galleryMediaUiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllTags(),
                _galleryTagsUiState.map { it.filterBy }.distinctUntilChanged(),
                _galleryTagsUiState.map { it.sortBy }.distinctUntilChanged()
            ) { tagList, filterVariant, sortVariant ->
                val filteredTags =
                    filterVariant?.let { tagList.filter { it.color == filterVariant } } ?: tagList
                val sortedTags = when (sortVariant) {
                    SortVariant.NAME -> filteredTags.sortedBy { it.name }
                    SortVariant.DATE -> filteredTags.sortedBy { it.lastModified }
                    SortVariant.COLOR -> filteredTags.sortedBy { it.color.compareToken }
                }
                _galleryTagsUiState.update { it.copy(tags = sortedTags) }
            }.collect {}
        }
    }

    fun addSelectedMedia(uris: List<Uri>) {
        _galleryMediaUiState.update { currentState ->
            val updatedUris = (currentState.selectedUris + uris).distinct()
            currentState.copy(selectedUris = updatedUris)
        }
    }

    fun removeSelectedMedia(uri: Uri) {
        // TODO
    }

    fun onTagSelected(id: Long) {
        if (_galleryTagsUiState.value.selectedTagIds.contains(id)) {
            _galleryTagsUiState.update { currentState ->
                currentState.copy(
                    selectedTagIds = currentState.selectedTagIds - id
                )
            }
        } else {
            _galleryTagsUiState.update { currentState ->
                currentState.copy(
                    selectedTagIds = currentState.selectedTagIds + id
                )
            }
        }
    }

    fun onClickSave() {
        _galleryMediaUiState.value.selectedUris.forEach { uri ->
            saveMediaTags(uri.toString(), _galleryTagsUiState.value.selectedTagIds)
        }
        removeAllSelected()
    }

    fun onClickReset() {
        removeAllSelected()
    }

    private fun saveMediaTags(mediaId: String, selectedTagIds: List<Long>) {
        viewModelScope.launch {
            selectedTagIds.forEach { tagId ->
                repository.insertMediaTagCrossRef(MediaTagCrossRef(mediaId, tagId))
            }
        }
    }

    fun setSortBy(sortBy: SortVariant) {
        _galleryTagsUiState.update { it.copy(sortBy = sortBy) }
    }

    fun setFilterBy(filterBy: Tag.Color?) {
        _galleryTagsUiState.update { it.copy(filterBy = filterBy) }
    }

    private fun removeAllSelected() {
        _galleryMediaUiState.update { it.copy(selectedUris = emptyList()) }
        _galleryTagsUiState.update { it.copy(selectedTagIds = emptyList()) }
        setSortBy(SortVariant.DEFAULT_SORT_VARIANT)
        setFilterBy(null)
    }
}
