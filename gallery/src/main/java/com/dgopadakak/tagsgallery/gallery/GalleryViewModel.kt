package com.dgopadakak.tagsgallery.gallery

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dgopadakak.tagsgallery.core.local_storage.Repository
import com.dgopadakak.tagsgallery.core.local_storage.models.MediaTagCrossRef
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    data class GalleryUiState(      // TODO: переименовать как-то так: tagsUiState
        val tags: List<Tag> = emptyList(),
        val selectedTagIds: List<Long> = emptyList()
    )

    data class PreviewUiState(      // TODO: переименовать как-то так: mediaUiState
        val selectedUris: List<Uri> = emptyList()
    )

    private val _galleryState = MutableStateFlow(GalleryUiState())
    val galleryState = _galleryState.asStateFlow()

    private val _previewState = MutableStateFlow(PreviewUiState())
    val previewState = _previewState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllTags()
                .collect { tags ->
                    _galleryState.update { it.copy(tags = tags) }
                }
        }
    }

    fun addSelectedMedia(uris: List<Uri>) {
        _previewState.update { currentState ->
            val updatedUris = (currentState.selectedUris + uris).distinct()
            currentState.copy(selectedUris = updatedUris)
        }
    }

    fun removeSelectedMedia(uri: Uri) {
        // TODO
    }

    fun onTagSelected(id: Long) {
        if (_galleryState.value.selectedTagIds.contains(id)) {
            _galleryState.update { currentState ->
                currentState.copy(
                    selectedTagIds = currentState.selectedTagIds - id
                )
            }
        } else {
            _galleryState.update { currentState ->
                currentState.copy(
                    selectedTagIds = currentState.selectedTagIds + id
                )
            }
        }
    }

    fun onClickSave() {
        _previewState.value.selectedUris.forEach { uri ->
            saveMediaTags(uri.toString(), _galleryState.value.selectedTagIds)
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

    private fun removeAllSelected() {
        _previewState.update { it.copy(selectedUris = emptyList()) }
        _galleryState.update { it.copy(selectedTagIds = emptyList()) }
    }
}
