package com.dgopadakak.tagsgallery.search

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dgopadakak.tagsgallery.core.compose.enums.SortVariant
import com.dgopadakak.tagsgallery.core.local_storage.Repository
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
import androidx.core.net.toUri

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    data class SearchTagsUiState(
        val tags: List<Tag> = emptyList(),
        val selectedTagIds: List<Long> = emptyList(),
        val sortBy: SortVariant = SortVariant.DEFAULT_SORT_VARIANT,
        val filterBy: Tag.Color? = null,
    )

    data class SearchMediaUiState(
        val foundedMediaUris: List<Uri> = emptyList()
    )

    private val _searchTagsUiState = MutableStateFlow(SearchTagsUiState())
    val searchTagsUiState = _searchTagsUiState.asStateFlow()

    private val _searchMediaUiState = MutableStateFlow(SearchMediaUiState())
    val searchMediaUiState = _searchMediaUiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllTags(),
                _searchTagsUiState.map { it.filterBy }.distinctUntilChanged(),
                _searchTagsUiState.map { it.sortBy }.distinctUntilChanged()
            ) { tagList, filterVariant, sortVariant ->
                val filteredTags =
                    filterVariant?.let { tagList.filter { it.color == filterVariant } } ?: tagList
                val sortedTags = when (sortVariant) {
                    SortVariant.NAME -> filteredTags.sortedBy { it.name }
                    SortVariant.DATE -> filteredTags.sortedBy { it.lastModified }
                    SortVariant.COLOR -> filteredTags.sortedBy { it.color.compareToken }
                }
                // Очистка selectedTagIds от несуществующих id
                val existingIds = tagList.map { it.id }.toSet()
                val updatedSelectedIds = _searchTagsUiState.value.selectedTagIds.filter { it in existingIds }
                _searchTagsUiState.update {
                    it.copy(
                        tags = sortedTags,
                        selectedTagIds = updatedSelectedIds
                    )
                }
            }.collect {}
        }
    }

    ////////////////////////////// Для теста! Удалить. //////////////////////////////
    fun loadMediaForTag(tagId: Long) {
        viewModelScope.launch {
            repository.getTagWithMedia(tagId).collect { tagWithMedia ->
                _searchMediaUiState.update { currentState ->
                    currentState.copy(foundedMediaUris = tagWithMedia?.media?.map { it.mediaId.toUri() } ?: emptyList<Uri>())
                }
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////

}
