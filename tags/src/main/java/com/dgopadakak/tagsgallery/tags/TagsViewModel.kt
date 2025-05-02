package com.dgopadakak.tagsgallery.tags

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dgopadakak.tagsgallery.core.compose.enums.SortVariant
import com.dgopadakak.tagsgallery.core.local_storage.Repository
import com.dgopadakak.tagsgallery.core.local_storage.enums.Hints
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

    @Stable
    data class UiState(
        val sortBy: SortVariant = SortVariant.NAME,
        val filterBy: Tag.Color? = null,
        val tags: List<Tag> = emptyList(),
        val selectedTagIds: List<Long> = emptyList(),
        val needToShowHint: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Очистка от несуществующих id в случае их удаления
        viewModelScope.launch {
            repository.getAllTags().collect { tagList ->
                val existingIds = tagList.map { it.id }.toSet()
                val updatedSelectedIds = _uiState.value.selectedTagIds.filter { it in existingIds }
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedTagIds = updatedSelectedIds
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
                val filteredAndSortedTags = when (sortVariant) {
                    SortVariant.NAME -> filteredTags.sortedBy { it.name }
                    SortVariant.DATE -> filteredTags.sortedBy { it.lastModified }
                    SortVariant.COLOR -> filteredTags.sortedBy { it.color.compareToken }
                }
                _uiState.update { it.copy(tags = filteredAndSortedTags) }
            }.collect {}
        }

        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    needToShowHint = !repository.isHintShown(Hints.TAGS_MAIN_HINT)
                )
            }
        }
    }

    fun saveNewTag(name: String, color: Tag.Color) {
        viewModelScope.launch {
            repository.insertTag(
                Tag(
                    name = name.trim(),
                    color = color
                )
            )
        }
    }

    fun updateTag(tag: Tag) {
        viewModelScope.launch {
            val oldTag = repository.getTagById(tag.id)
            oldTag?.let {
                if (it.name != tag.name.trim() || it.color != tag.color) {
                    repository.updateTag(
                        oldTag.copy(
                            name = tag.name.trim(),
                            lastModified = System.currentTimeMillis(),
                            color = tag.color
                        )
                    )
                }
            }
        }
    }

    fun onTagSelect(tagId: Long) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedTagIds = if (currentState.selectedTagIds.contains(tagId)) {
                    currentState.selectedTagIds - tagId
                } else {
                    currentState.selectedTagIds + tagId
                }
            )
        }
    }

    fun onResetSelection() {
        _uiState.update { it.copy(selectedTagIds = emptyList()) }
    }

    fun deleteSelectedTags() {
        viewModelScope.launch {
            repository.deleteTagsAndRelations(_uiState.value.selectedTagIds)
        }
    }

    fun setSortBy(sortBy: SortVariant) {
        _uiState.update { it.copy(sortBy = sortBy) }
    }

    fun setFilterBy(filterBy: Tag.Color?) {
        _uiState.update { it.copy(filterBy = filterBy) }
    }

    fun setHintShown() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    needToShowHint = false
                )
            }
            repository.setHintShown(Hints.TAGS_MAIN_HINT)
        }
    }
}
