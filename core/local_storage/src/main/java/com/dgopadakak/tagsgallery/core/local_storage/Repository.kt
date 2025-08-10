package com.dgopadakak.tagsgallery.core.local_storage

import android.net.Uri
import androidx.core.net.toUri
import com.dgopadakak.tagsgallery.core.local_storage.enums.Hints
import com.dgopadakak.tagsgallery.core.local_storage.models.MediaTagCrossRef
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import com.dgopadakak.tagsgallery.core.local_storage.preferences.PreferencesRepository
import com.dgopadakak.tagsgallery.core.local_storage.room.TagDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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

    suspend fun getTagIdsForMedia(mediaId: String): List<Long> = withContext(dispatcher) {
        tagDao.getTagIdsForMedia(mediaId)
    }

    /**
     * Функция, удаляющая все связи с медиа из БД. В результате в БД не останется упоминаний о медиа
     *
     * При реализации использования этого метода всегда необходимо освобождать разрешение
     * на перманентный доступ к медиа. Причина в том, что удаление связей с медиа для их
     * пересоздания происходит в функции [deleteAndInsertMediaTagCrossRefs], а эта функция должна
     * вызываться при окончательном удалении связей с медиа.
     */
    suspend fun deleteMediaTagCrossRefsByMediaId(mediaId: String) = withContext(dispatcher) {
        // TODO: учесть доку при использовании
        tagDao.deleteMediaTagCrossRefsByMediaId(mediaId)
    }

    /**
     * Функция, удаляющая все связи с медиа списка mediaIdsToDeleteCrossRefs из БД и создающая новые
     * связи, опираясь на crossRefsToAdd. Основной сценария использования: добавление новых медиа и,
     * опционально, редактирование связей старых путем предварительного удаления всех их старых
     * связей (то есть медиа фигурирует в обоих параметрах).
     *
     * Но возможна ситуация, кода медиа находится только в списке mediaIdsToDeleteCrossRefs. Это
     * означает, что его теги отредактированы таким образом, что добавлять нечего. Это нормальный
     * сценарий эквивалентный полному удалению связей с медиа. Но такие случаи надо отлавливать в
     * месте использования данного метода и, как и в случае с [deleteMediaTagCrossRefsByMediaId],
     * необходимо освобождать разрешение на перманентный доступ к медиа. То есть при реализации
     * использования этого метода надо проверить: останутся ли у него теги и освобождать разрешение
     * на перманентный доступ к медиа, если нет.
     */
    suspend fun deleteAndInsertMediaTagCrossRefs(
        mediaIdsToDeleteCrossRefs: Set<String>,
        crossRefsToAdd: List<MediaTagCrossRef>
    ) = withContext(dispatcher) {
        tagDao.deleteAndInsertMediaTagCrossRefs(
            mediaIdsToDeleteCrossRefs = mediaIdsToDeleteCrossRefs,
            crossRefsToAdd = crossRefsToAdd
        )
    }

    suspend fun isHintShown(hint: Hints): Boolean = withContext(dispatcher) {
        preferencesRepository.isHintShown(hint)
    }

    suspend fun setHintShown(hint: Hints) = withContext(dispatcher) {
        preferencesRepository.setHintShown(hint)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMediaUrisByAllTags(tagIdsFlow: Flow<List<Long>>): Flow<List<Uri>> {
        return tagIdsFlow.flatMapLatest { tagIds ->
            if (tagIds.isEmpty()) {
                tagDao.getAllMediaIds()
                    .map { ids -> ids.map { it.toUri() } }
                    .distinctUntilChanged()
            } else {
                tagDao.getMediaIdsByAllTags(tagIds, tagIds.size)
                    .map { ids -> ids.map { it.toUri() } }
                    .distinctUntilChanged()
            }
        }.distinctUntilChanged().flowOn(dispatcher)
    }
}
