package com.dgopadakak.tagsgallery.core.local_storage

import com.dgopadakak.tagsgallery.core.local_storage.models.MediaTagCrossRef
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import com.dgopadakak.tagsgallery.core.local_storage.models.TagWithMedia
import com.dgopadakak.tagsgallery.core.local_storage.room.TagDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * По сути это класс-посредник, он создан для того, чтобы не взаимодействовать напрямую с TagDao,
 * что будет полезно при миграции проекта на KMP. Так как TagDao будет только для Android, а на
 * других платформах данный класс будет работать с другими источниками данных.
 */
class Repository(
    private val tagDao: TagDao,
    private val dispatcher: CoroutineDispatcher
) {

    fun getAllTags(): Flow<List<Tag>> = tagDao
        .getAllTags()
        .flowOn(dispatcher)

    suspend fun getTagById(tagId: Long): Tag? = withContext(Dispatchers.IO) {
        tagDao.getTagById(tagId)
    }

    suspend fun insertTag(tag: Tag): Long = withContext(Dispatchers.IO) {
        tagDao.insertTag(tag)
    }

    suspend fun updateTag(tag: Tag) = withContext(Dispatchers.IO) {
        tagDao.updateTag(tag)
    }

    suspend fun deleteTagAndRelations(tag: Tag) = withContext(Dispatchers.IO) {
        tagDao.deleteTagAndRelations(tag)
    }

    fun getTagWithMedia(tagId: Long): Flow<TagWithMedia?> = tagDao
        .getTagWithMedia(tagId)
        .flowOn(Dispatchers.IO)

    suspend fun insertMediaTagCrossRef(crossRef: MediaTagCrossRef) = withContext(Dispatchers.IO) {
        tagDao.insertMediaTagCrossRef(crossRef)
    }
}
