package com.dgopadakak.tagsgallery.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dgopadakak.tagsgallery.core.compose.enums.SortVariant
import com.dgopadakak.tagsgallery.core.local_storage.Repository
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private val _sortBy = MutableStateFlow(SortVariant.NAME)
    val sortBy = _sortBy.asStateFlow()
    private val _filterBy = MutableStateFlow<Tag.Color?>(null)
    val filterBy = _filterBy.asStateFlow()

    val tags = combine(
        flow = combine(
            flow = repository.getAllTags(),
            flow2 =  _filterBy
        ) { tagList, filterVariant ->
            if (filterVariant == null) {
                tagList
            } else {
                tagList.filter { it.color == filterVariant }
            }
        },
        flow2 =  _sortBy
    ) { tagList, sortVariant ->
        when (sortVariant) {
            SortVariant.NAME -> tagList.sortedBy { it.name }
            SortVariant.DATE -> tagList.sortedBy { it.lastModified }
            SortVariant.COLOR -> tagList.sortedBy { it.color.compareToken }
        }
    }.stateIn(  // TODO: почитать подробнее
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    fun saveTag(id: Long?, name: String, color: Tag.Color) {
        viewModelScope.launch {
            if (id == null) {
                repository.insertTag(
                    Tag(
                        name = name.trim(),
                        lastModified = System.currentTimeMillis(),
                        color = color
                    )
                )
            } else {
                repository.updateTag(
                    Tag(
                        id = id,
                        name = name.trim(),
                        lastModified = System.currentTimeMillis(),
                        color = color
                    )
                )
            }
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            repository.deleteTag(tag)
        }
    }

    fun setSortBy(sortBy: SortVariant) {
        _sortBy.value = sortBy
    }

    fun setFilterBy(filterBy: Tag.Color?) {
        _filterBy.value = filterBy
    }
}
