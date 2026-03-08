package com.dgopadakak.tagsgallery.search

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dgopadakak.tagsgallery.core.compose.enums.SortVariant
import com.dgopadakak.tagsgallery.core.local_storage.Repository
import com.dgopadakak.tagsgallery.core.local_storage.enums.Hints
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import com.dgopadakak.tagsgallery.core.local_storage.util.hasPersistedReadPermission
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: Repository,
    @ApplicationContext applicationContext: Context
) : ViewModel() {

    @Stable
    data class UiState(
        val allTags: List<Tag> = emptyList(),
        val sortedFilteredTags: List<Tag> = emptyList(),
        val sortBy: SortVariant = SortVariant.DEFAULT_SORT_VARIANT,
        val filterBy: Tag.Color? = null,
        val selectedTagIds: List<Long> = emptyList(),
        val foundedMediaUris: List<Uri> = emptyList(),
        val selectedMediaUris: Set<Uri> = emptySet(),
        val needToShowHint: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val contentResolver: ContentResolver = applicationContext.contentResolver

    init {
        viewModelScope.launch {
            // Очистка от несуществующих id в случае их удаления на экране Tags
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
                val sortedAndFilteredTags = when (sortVariant) {
                    SortVariant.NAME -> filteredTags.sortedBy { it.name }
                    SortVariant.DATE -> filteredTags.sortedBy { it.lastModified }
                    SortVariant.COLOR -> filteredTags.sortedBy { it.color.compareToken }
                }
                _uiState.update {
                    it.copy(
                        allTags = tagList,
                        sortedFilteredTags = sortedAndFilteredTags
                    )
                }
            }.collect {}
        }

        viewModelScope.launch {
            repository.getMediaUrisByAllTags(
                tagIdsFlow = _uiState.map { it.selectedTagIds }.distinctUntilChanged(),
                contentResolver = contentResolver
            ).collect { mediaUris ->
                _uiState.update { it.copy(foundedMediaUris = mediaUris) }
            }
        }

        viewModelScope.launch {
            if (!repository.isHintShown(Hints.SEARCH_MAIN_HINT)) {
                _uiState.update { currentState ->
                    currentState.copy(
                        needToShowHint = true
                    )
                }
            }
        }
    }

    fun setHintShown() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    needToShowHint = false
                )
            }
            repository.setHintShown(Hints.SEARCH_MAIN_HINT)
        }
    }

    fun onTagToggle(tagId: Long) {
        val updatedSelection = if (_uiState.value.selectedTagIds.contains(tagId)) {
            _uiState.value.selectedTagIds - tagId
        } else {
            _uiState.value.selectedTagIds + tagId
        }
        _uiState.update { it.copy(selectedTagIds = updatedSelection) }
    }

    fun setSortBy(sortBy: SortVariant) {
        _uiState.update { it.copy(sortBy = sortBy) }
    }

    fun setFilterBy(filterBy: Tag.Color?) {
        _uiState.update { it.copy(filterBy = filterBy) }
    }

    fun toggleMediaSelection(uri: Uri) {
        _uiState.update { state ->
            val current = state.selectedMediaUris
            state.copy(
                selectedMediaUris = if (uri in current) current - uri else current + uri
            )
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedMediaUris = emptySet()) }
    }

    fun deleteSelectedMedia() {
        viewModelScope.launch {
            val toDelete = _uiState.value.selectedMediaUris
            toDelete.forEach { uri ->   // TODO: вместо цикличного удаления во ViewModel - доработать Repository
                repository.deleteMediaTagCrossRefsByMediaId(uri.toString())
                if (contentResolver.hasPersistedReadPermission(uri)) {
                    contentResolver.releasePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            }
            _uiState.update { it.copy(selectedMediaUris = emptySet()) }
        }
    }
}
