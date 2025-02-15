package com.dgopadakak.tagsgallery.core.local_storage

import com.dgopadakak.tagsgallery.core.local_storage.models.MediaTagCrossRef
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import com.dgopadakak.tagsgallery.core.local_storage.models.TagWithMedia
import com.dgopadakak.tagsgallery.core.local_storage.room.TagDao
import kotlinx.coroutines.flow.Flow

/**
 * По сути это класс-обертка, он создан для того, чтобы не взаимодействовать напрямую с TagDao, что
 * будет полезно при миграции проекта на KMP. Так как TagDao будет только для Android, а на других
 * платформах данный класс будет работать с другими источниками данных.
 */
class Repository(private val tagDao: TagDao) {

    fun getAllTags(): Flow<List<Tag>> = tagDao.getAllTags()

    suspend fun insertTag(tag: Tag): Long = tagDao.insertTag(tag)

    suspend fun updateTag(tag: Tag) = tagDao.updateTag(tag)

    suspend fun deleteTag(tag: Tag) = tagDao.deleteTag(tag)

    fun getTagWithMedia(tagId: Long): Flow<TagWithMedia?> = tagDao.getTagWithMedia(tagId)

    suspend fun insertMediaTagCrossRef(crossRef: MediaTagCrossRef) =
        tagDao.insertMediaTagCrossRef(crossRef)
}
