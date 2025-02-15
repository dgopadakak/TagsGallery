package com.dgopadakak.tagsgallery.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dgopadakak.tagsgallery.core.local_storage.room.TagDao
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val tagDao: TagDao
) : ViewModel() {

    ////////////////////////////// Для теста! Удалить. //////////////////////////////
    val tags: Flow<List<Tag>> = tagDao.getAllTags()
    private val _savedMediaUris = MutableStateFlow<List<String>?>(emptyList())
    val testSavedMediaUris = _savedMediaUris.asStateFlow()

    fun loadMediaForTag(tagId: Long) {
        viewModelScope.launch {
            tagDao.getTagWithMedia(tagId).collect { tagWithMedia ->
                _savedMediaUris.value = tagWithMedia?.media?.map { it.mediaId }
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////

}
