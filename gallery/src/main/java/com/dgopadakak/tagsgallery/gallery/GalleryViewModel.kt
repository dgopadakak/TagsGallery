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

    data class UiState(
        val tags: List<Tag> = emptyList(),
        val selectedTagIds: List<Long> = emptyList(),
        val selectedUris: List<Uri> = emptyList()
    )
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllTags()
                .collect { tags ->
                    _state.update { it.copy(tags = tags) }
                }
        }
    }

    fun addSelectedMedia(uris: List<Uri>) {
        // Из-за использования derivedStateOf обновлять список нужно, создавая новый список, а не изменяя старый
        // FIXME: но это все равно не работает, возможно новый список не создается и мы все еще работаем с тем же?
        _state.update { currentState ->
            val updatedUris = (currentState.selectedUris + uris).distinct() // Гарантированно создаем новый список

            currentState.copy(selectedUris = updatedUris)
        }
    }

    fun removeSelectedMedia(uri: Uri) {
        // TODO
    }

    fun onTagSelected(id: Long) {
        if (_state.value.selectedTagIds.contains(id)) {
            _state.update { currentState ->
                currentState.copy(
                    selectedTagIds = currentState.selectedTagIds - id
                )
            }
        } else {
            _state.update { currentState ->
                currentState.copy(
                    selectedTagIds = currentState.selectedTagIds + id
                )
            }
        }
    }

    fun onClickSave() {
        _state.value.selectedUris.forEach { uri ->
            saveMediaTags(uri.toString(), _state.value.selectedTagIds)
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
        _state.update { currentUiState ->
            currentUiState.copy(
                selectedUris = emptyList(),
                selectedTagIds = emptyList()
            )
        }
    }
}
