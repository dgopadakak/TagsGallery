package com.dgopadakak.tagsgallery.core.local_storage

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.dgopadakak.tagsgallery.core.local_storage.enums.Hints
import com.dgopadakak.tagsgallery.core.local_storage.enums.TagMatchMode
import com.dgopadakak.tagsgallery.core.local_storage.models.MediaTagCrossRef
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import com.dgopadakak.tagsgallery.core.local_storage.preferences.PreferencesRepository
import com.dgopadakak.tagsgallery.core.local_storage.room.TagDao
import com.dgopadakak.tagsgallery.core.local_storage.util.hasPersistedReadPermission
import com.dgopadakak.tagsgallery.core.local_storage.util.uriExists
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class MediaSearchCriteria(
    val tagIds: List<Long>,
    val matchMode: TagMatchMode
)

class Repository(
    private val tagDao: TagDao,
    private val preferencesRepository: PreferencesRepository,
    private val dispatcher: CoroutineDispatcher
) {

    fun getAllTags(): Flow<List<Tag>> = tagDao
        .getAllTags()
        .flowOn(dispatcher)

    fun getAllMediaIds(): Flow<List<String>> = tagDao
        .getAllMediaIds()
        .distinctUntilChanged()
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

    /**
     * Функция, удаляющая теги вместе со связями с медиа.
     *
     * В отличие от [deleteMediaTagCrossRefsByMediaId] и [deleteMediaTagCrossRefsByMediaIds],
     * разрешение на перманентный доступ освобождается здесь же: удаление тега может оставить
     * медиа без единой связи, и такое медиа больше не попадет ни в одну выборку из
     * MediaTagCrossRef - а значит и самоочистка в [getMediaUris] его не найдет. Вызывающему
     * делать с разрешениями ничего не нужно.
     *
     * Освобождение вынесено за транзакцию сознательно: держать binder-вызовы под блокировкой
     * записи в БД нельзя. Ценой этого остается узкое окно между коммитом и освобождением, в
     * которое теоретически может попасть сохранение с экрана Add
     */
    suspend fun deleteTagsAndRelations(
        tagIds: List<Long>,
        contentResolver: ContentResolver
    ) = withContext(dispatcher) {
        val orphanedMediaIds = tagDao.deleteTagsAndRelations(tagIds)

        // Один запрос к системе вместо вызова на каждое медиа: persistedUriPermissions - это
        // binder-вызов, возвращающий весь список грантов, а осиротеть может пара тысяч медиа
        val persistedReadUris = contentResolver.persistedUriPermissions
            .filter { it.isReadPermission }
            .mapTo(HashSet()) { it.uri }

        orphanedMediaIds.forEach { mediaId ->
            val mediaUri = mediaId.toUri()
            if (mediaUri in persistedReadUris) {
                contentResolver.releasePersistableUriPermission(
                    mediaUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
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
        tagDao.deleteMediaTagCrossRefsByMediaId(mediaId)
    }

    /**
     * Функция, удаляющая все связи со списком медиа из БД. В результате в БД не останется
     * упоминаний об этом списке медиа
     *
     * При реализации использования этого метода всегда необходимо освобождать разрешение
     * на перманентный доступ к медиа. Причина в том, что удаление связей с медиа для их
     * пересоздания происходит в функции [deleteAndInsertMediaTagCrossRefs], а эта функция должна
     * вызываться при окончательном удалении связей с медиа.
     */
    suspend fun deleteMediaTagCrossRefsByMediaIds(mediaIds: List<String>) = withContext(dispatcher) {
        if (mediaIds.isNotEmpty()) {
            tagDao.deleteMediaTagCrossRefsByMediaIds(mediaIds)
        }
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
    fun getMediaUris(
        criteriaFlow: Flow<MediaSearchCriteria>,
        contentResolver: ContentResolver
    ): Flow<List<Uri>> {
        return criteriaFlow.flatMapLatest { (tagIds, matchMode) ->
            // При пустом выборе тегов показываем все медиа независимо от режима,
            // чтобы избежать пустого результата.
            val mediaIdsFlow = if (tagIds.isEmpty()) {
                tagDao.getAllMediaIds().distinctUntilChanged()
            } else {
                when (matchMode) {
                    TagMatchMode.ALL ->
                        tagDao.getMediaIdsByAllTags(tagIds, tagIds.size).distinctUntilChanged()

                    TagMatchMode.ANY ->
                        tagDao.getMediaIdsByAnyTag(tagIds).distinctUntilChanged()

                    TagMatchMode.EXCLUDE ->
                        tagDao.getMediaIdsExcludingTags(tagIds).distinctUntilChanged()
                }
            }

            mediaIdsFlow.map { mediaIds ->
                val validUris = mutableListOf<Uri>()
                val invalidMediaIds = mutableListOf<String>()

                mediaIds.forEach { mediaId ->
                    val uri = mediaId.toUri()
                    if (contentResolver.uriExists(uri)) {
                        validUris += uri
                    } else {
                        invalidMediaIds += mediaId
                    }
                }

                // Чистим БД и безопасно отпускаем разрешение
                invalidMediaIds.forEach {
                    tagDao.deleteMediaTagCrossRefsByMediaId(it)
                    if (contentResolver.hasPersistedReadPermission(it.toUri())) {
                        contentResolver.releasePersistableUriPermission(
                            it.toUri(),
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }
                }

                validUris
            }
        }.distinctUntilChanged().flowOn(dispatcher)
    }
}
