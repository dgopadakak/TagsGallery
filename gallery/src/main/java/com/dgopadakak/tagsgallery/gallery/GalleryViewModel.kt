package com.dgopadakak.tagsgallery.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dgopadakak.tagsgallery.core.local_storage.room.TagDao
import com.dgopadakak.tagsgallery.core.local_storage.models.MediaTagCrossRef
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val tagDao: TagDao
) : ViewModel() {

    val tags: Flow<List<Tag>> = tagDao.getAllTags().stateIn(  // TODO: почитать подробнее
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    fun saveMediaTags(mediaId: String, selectedTagIds: List<Long>) {
        viewModelScope.launch {
            selectedTagIds.forEach { tagId ->
                tagDao.insertMediaTagCrossRef(MediaTagCrossRef(mediaId, tagId))
            }
        }
    }
}
