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
    data class GalleryTagsUiState(
        val tags: List<Tag> = emptyList(),
        val selectedTagIds: List<Long> = emptyList(),
        val sortBy: SortVariant = SortVariant.DEFAULT_SORT_VARIANT,
        val filterBy: Tag.Color? = null,
        val activeEditIndividualTags: Uri? = null,      // TODO: дублирование в обоих UiState, подумать над исправлением (возможно, объединить стейты)
        val perMediaAddedTagIds: Map<Uri, List<Long>> = mapOf(),
        val perMediaRemovedTagIds: Map<Uri, List<Long>> = mapOf()
    )

    @Stable
    data class GalleryMediaUiState(
        val selectedUris: List<Uri> = emptyList(),
        val activeEditIndividualTags: Uri? = null,
        val perMediaAddedTagsNum: Map<Uri, Int> = mapOf(),
        val perMediaRemovedTagsNum: Map<Uri, Int> = mapOf(),
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
                // Очистка selectedTagIds от несуществующих id
                val existingIds = tagList.map { it.id }.toSet()
                val updatedSelectedIds = _galleryTagsUiState.value.selectedTagIds.filter { it in existingIds }
                _galleryTagsUiState.update {
                    it.copy(
                        tags = sortedTags,
                        selectedTagIds = updatedSelectedIds
                    )
                }
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
        _galleryMediaUiState.update { currentState ->
            currentState.copy(
                selectedUris = currentState.selectedUris - uri,
                activeEditIndividualTags = null,
                perMediaAddedTagsNum = currentState.perMediaAddedTagsNum - uri,
                perMediaRemovedTagsNum = currentState.perMediaRemovedTagsNum - uri
            )
        }
        _galleryTagsUiState.update { currentState ->
            currentState.copy(
                activeEditIndividualTags = null,
                perMediaAddedTagIds = currentState.perMediaAddedTagIds - uri,
                perMediaRemovedTagIds = currentState.perMediaAddedTagIds - uri
            )
        }
        if (_galleryMediaUiState.value.selectedUris.isEmpty()) {
            resetScreen()
        }
    }

    fun onTagSelected(id: Long) {
        // TODO: обеспечить влияние на perMediaAddedTags и perMediaRemovedTags
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

    fun onPerMediaTagToggle(uri: Uri, tagId: Long) {
        val isCommonSelected = _galleryTagsUiState.value.selectedTagIds.contains(tagId)
        if (isCommonSelected) {
            val removed = arrayListOf<Long>()
            _galleryTagsUiState.value.perMediaRemovedTagIds.getOrDefault(uri, defaultValue = null)?.forEach { id ->
                removed.add(id)
            }
            if (removed.contains(tagId)) removed.remove(tagId) else removed.add(tagId)
            _galleryTagsUiState.update { currentState ->
                currentState.copy(
                    perMediaRemovedTagIds = currentState.perMediaRemovedTagIds + mapOf(Pair(uri, removed))
                )
            }
            _galleryMediaUiState.update { currentState ->
                currentState.copy(
                    perMediaRemovedTagsNum = currentState.perMediaRemovedTagsNum + mapOf(Pair(uri, removed.size))
                )
            }
        } else {
            val added = arrayListOf<Long>()
            _galleryTagsUiState.value.perMediaAddedTagIds.getOrDefault(uri, defaultValue = null)?.forEach { id ->
                added.add(id)
            }
            if (added.contains(tagId)) added.remove(tagId) else added.add(tagId)
            _galleryTagsUiState.update { currentState ->
                currentState.copy(
                    perMediaAddedTagIds = currentState.perMediaAddedTagIds + mapOf(Pair(uri, added))
                )
            }
            _galleryMediaUiState.update { currentState ->
                currentState.copy(
                    perMediaAddedTagsNum = currentState.perMediaAddedTagsNum + mapOf(Pair(uri, added.size))
                )
            }
        }
    }

    fun setSortBy(sortBy: SortVariant) {
        _galleryTagsUiState.update { it.copy(sortBy = sortBy) }
    }

    fun setFilterBy(filterBy: Tag.Color?) {
        _galleryTagsUiState.update { it.copy(filterBy = filterBy) }
    }

    fun setActiveUriForIndividualTags(uri: Uri?) {
        if (_galleryMediaUiState.value.activeEditIndividualTags == null || uri == null) {
            _galleryMediaUiState.update { currentState ->
                currentState.copy(
                    activeEditIndividualTags = uri
                )
            }
            _galleryTagsUiState.update { currentSTate ->
                currentSTate.copy(
                    activeEditIndividualTags = uri
                )
            }
        }
    }

    fun onClickSave() {
        // TODO: обеспечить сохранение с учетом perMediaAddedTags и perMediaRemovedTags
        _galleryMediaUiState.value.selectedUris.forEach { uri ->
            saveMediaTags(uri.toString(), _galleryTagsUiState.value.selectedTagIds)
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
        _galleryMediaUiState.update { currentState ->
            currentState.copy(
                selectedUris = emptyList(),
                perMediaAddedTagsNum = emptyMap(),
                perMediaRemovedTagsNum = emptyMap()
            )
        }
        _galleryTagsUiState.update { currentState ->
            currentState.copy(
                selectedTagIds = emptyList(),
                perMediaAddedTagIds = emptyMap(),
                perMediaRemovedTagIds = emptyMap()
            )
        }
        setSortBy(SortVariant.DEFAULT_SORT_VARIANT)
        setFilterBy(null)
    }
}
