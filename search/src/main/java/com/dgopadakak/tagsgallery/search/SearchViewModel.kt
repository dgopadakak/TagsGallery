package com.dgopadakak.tagsgallery.search

import android.net.Uri
import androidx.compose.runtime.Stable
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

    @Stable
    data class SearchUiState(
        val tags: List<Tag> = emptyList(),
        val sortBy: SortVariant = SortVariant.DEFAULT_SORT_VARIANT,
        val filterBy: Tag.Color? = null,
        val foundedMediaUris: List<Uri> = emptyList()
    )

    private val _searchUiState = MutableStateFlow(SearchUiState())
    val searchUiState = _searchUiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllTags(),
                _searchUiState.map { it.filterBy }.distinctUntilChanged(),
                _searchUiState.map { it.sortBy }.distinctUntilChanged()
            ) { tagList, filterVariant, sortVariant ->
                val filteredTags =
                    filterVariant?.let { tagList.filter { it.color == filterVariant } } ?: tagList
                val sortedAndFilteredTags = when (sortVariant) {
                    SortVariant.NAME -> filteredTags.sortedBy { it.name }
                    SortVariant.DATE -> filteredTags.sortedBy { it.lastModified }
                    SortVariant.COLOR -> filteredTags.sortedBy { it.color.compareToken }
                }
                _searchUiState.update {
                    it.copy(
                        tags = sortedAndFilteredTags
                    )
                }
            }.collect {}
        }
    }

    ////////////////////////////// Для теста! Удалить. //////////////////////////////
    fun loadMediaForTag(tagId: Long) {
        viewModelScope.launch {
            repository.getTagWithMedia(tagId).collect { tagWithMedia ->
                _searchUiState.update { currentState ->
                    currentState.copy(foundedMediaUris = tagWithMedia?.media?.map { it.mediaId.toUri() } ?: emptyList<Uri>())
                }
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////

}
