package com.dgopadakak.tagsgallery.tags

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

@HiltViewModel
class TagsViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    data class UiState(
        val sortBy: SortVariant = SortVariant.NAME,
        val filterBy: Tag.Color? = null,
        val tags: List<Tag> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllTags(),
                _uiState.map { it.filterBy }.distinctUntilChanged(),
                _uiState.map { it.sortBy }.distinctUntilChanged()
            ) { tagList, filterVariant, sortVariant ->
                val filteredTags =
                    filterVariant?.let { tagList.filter { it.color == filterVariant } } ?: tagList
                val sortedTags = when (sortVariant) {
                    SortVariant.NAME -> filteredTags.sortedBy { it.name }
                    SortVariant.DATE -> filteredTags.sortedBy { it.lastModified }
                    SortVariant.COLOR -> filteredTags.sortedBy { it.color.compareToken }
                }
                _uiState.update { it.copy(tags = sortedTags) }
            }.collect {}
        }
    }

    fun saveTag(id: Long?, name: String, color: Tag.Color) {
        viewModelScope.launch {
            if (id == null) {
                repository.insertTag(
                    Tag(
                        name = name.trim(),
                        color = color
                    )
                )
            } else {
                val oldTag = _uiState.value.tags.find { it.id == id }
                oldTag?.let {
                    if (it.name != name.trim() || it.color != color) {
                        repository.updateTag(
                            Tag(
                                id = id,
                                name = name.trim(),
                                color = color
                            )
                        )
                    }
                }
            }
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            repository.deleteTagAndRelations(tag)
        }
    }

    fun setSortBy(sortBy: SortVariant) {
        _uiState.update { it.copy(sortBy = sortBy) }
    }

    fun setFilterBy(filterBy: Tag.Color?) {
        _uiState.update { it.copy(filterBy = filterBy) }
    }
}
