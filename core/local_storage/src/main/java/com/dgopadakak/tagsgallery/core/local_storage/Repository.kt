package com.dgopadakak.tagsgallery.core.local_storage

import com.dgopadakak.tagsgallery.core.local_storage.enums.Hints
import com.dgopadakak.tagsgallery.core.local_storage.models.MediaTagCrossRef
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import com.dgopadakak.tagsgallery.core.local_storage.models.TagWithMedia
import com.dgopadakak.tagsgallery.core.local_storage.preferences.PreferencesRepository
import com.dgopadakak.tagsgallery.core.local_storage.room.TagDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class Repository(
    private val tagDao: TagDao,
    private val preferencesRepository: PreferencesRepository,
    private val dispatcher: CoroutineDispatcher
) {

    fun getAllTags(): Flow<List<Tag>> = tagDao
        .getAllTags()
        .flowOn(dispatcher)

    suspend fun getTagById(tagId: Long): Tag? = withContext(dispatcher) {
        tagDao.getTagById(tagId)
    }

    suspend fun insertTag(tag: Tag) = withContext(dispatcher) {
        tagDao.insertAllTags(tag)
    }

    suspend fun updateTag(tag: Tag) = withContext(dispatcher) {
        tagDao.updateTag(tag)
    }

    suspend fun deleteTagsAndRelations(tagIds: List<Long>) = withContext(dispatcher) {
        tagDao.deleteTagsAndRelations(tagIds)
    }

    fun getTagWithMedia(tagId: Long): Flow<TagWithMedia?> = tagDao
        .getTagWithMedia(tagId)
        .flowOn(dispatcher)

    suspend fun getTagIdsForMedia(mediaId: String): List<Long> = withContext(dispatcher) {
        tagDao.getTagIdsForMedia(mediaId)
    }

    suspend fun deleteMediaTagCrossRefsByMediaId(mediaId: String) = withContext(dispatcher) {
        // TODO: если это не удаление всех связей с медиа из-за передобавления тегов, то нужно
        //  освобождать разрешение на перманентный доступ к медиа
        tagDao.deleteMediaTagCrossRefsByMediaId(mediaId)
    }

    suspend fun insertMediaTagCrossRef(crossRef: MediaTagCrossRef) = withContext(dispatcher) {
        tagDao.insertMediaTagCrossRef(crossRef)
    }

    suspend fun isHintShown(hint: Hints): Boolean = withContext(dispatcher) {
        preferencesRepository.isHintShown(hint)
    }

    suspend fun setHintShown(hint: Hints) = withContext(dispatcher) {
        preferencesRepository.setHintShown(hint)
    }
}
