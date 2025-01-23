package com.dgopadakak.tagsgallery.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dgopadakak.tagsgallery.core.local_storage.room.TagDao
import com.dgopadakak.tagsgallery.core.local_storage.room.models.Tag
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
    private val tagDao: TagDao
) : ViewModel() {
    private val _sortBy = MutableStateFlow(SortVariant.NAME)
    val sortBy = _sortBy.asStateFlow()

    val tags = combine(tagDao.getAllTags(), _sortBy) { tagList, sortVariant ->
        when (sortVariant) {
            SortVariant.NAME -> tagList.sortedBy { it.name }
            SortVariant.DATE -> tagList.sortedBy { it.lastModified }
        }
    }.stateIn(  // TODO: почитать подробнее
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    fun saveTag(id: Long?, name: String) {
        viewModelScope.launch {
            if (id == null) {
                tagDao.insertTag(
                    Tag(
                        name = name,
                        lastModified = System.currentTimeMillis()
                    )
                )
            } else {
                tagDao.updateTag(
                    Tag(
                        id = id,
                        name = name,
                        lastModified = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            tagDao.deleteTag(tag)
        }
    }

    fun setSortBy(sortBy: SortVariant) {
        _sortBy.value = sortBy
    }
}
