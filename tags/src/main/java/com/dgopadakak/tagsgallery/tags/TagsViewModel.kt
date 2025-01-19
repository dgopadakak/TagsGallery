package com.dgopadakak.tagsgallery.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dgopadakak.tagsgallery.core.local_storage.room.TagDao
import com.dgopadakak.tagsgallery.core.local_storage.room.models.Tag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(
    private val tagDao: TagDao
) : ViewModel() {
    val tags = tagDao.getAllTags().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun saveTag(name: String) {
        viewModelScope.launch {
            tagDao.insertTag(Tag(name = name))
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            tagDao.deleteTag(tag)
        }
    }
}
