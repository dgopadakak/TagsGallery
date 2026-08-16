package com.dgopadakak.tagsgallery.core.local_storage.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dgopadakak.tagsgallery.core.local_storage.models.MediaTagCrossRef
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM Tag")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT * FROM Tag WHERE id = :tagId")
    suspend fun getTagById(tagId: Long): Tag?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTags(vararg tag: Tag)

    @Update
    suspend fun updateTag(tag: Tag)

    @Query(
        """
        SELECT mediaId
        FROM MediaTagCrossRef
        WHERE tagId IN (:tagIds)
        GROUP BY mediaId
        HAVING COUNT(DISTINCT tagId) = :requiredCount
        """
    )
    fun getMediaIdsByAllTags(tagIds: List<Long>, requiredCount: Int): Flow<List<String>>

    @Query(
        """
        SELECT DISTINCT mediaId
        FROM MediaTagCrossRef
        WHERE tagId IN (:tagIds)
        """
    )
    fun getMediaIdsByAnyTag(tagIds: List<Long>): Flow<List<String>>

    @Query(
        """
        SELECT DISTINCT mediaId
        FROM MediaTagCrossRef
        WHERE mediaId NOT IN (
            SELECT mediaId FROM MediaTagCrossRef WHERE tagId IN (:tagIds)
        )
        """
    )
    fun getMediaIdsExcludingTags(tagIds: List<Long>): Flow<List<String>>

    @Query("SELECT DISTINCT mediaId FROM MediaTagCrossRef")
    fun getAllMediaIds(): Flow<List<String>>

    @Transaction
    @Query(
        """
        SELECT Tag.id FROM Tag
        INNER JOIN MediaTagCrossRef ON Tag.id = MediaTagCrossRef.tagId
        WHERE MediaTagCrossRef.mediaId = :mediaId
        """
    )
    suspend fun getTagIdsForMedia(mediaId: String): List<Long>

    @Transaction
    suspend fun deleteAndInsertMediaTagCrossRefs(
        mediaIdsToDeleteCrossRefs: Set<String>,
        crossRefsToAdd: List<MediaTagCrossRef>
    ) {
        if (mediaIdsToDeleteCrossRefs.isNotEmpty()) {
            deleteMediaTagCrossRefsByMediaIds(mediaIdsToDeleteCrossRefs.toList())
        }
        insertMediaTagCrossRefs(crossRefsToAdd)
    }

    @Query("DELETE FROM MediaTagCrossRef WHERE mediaId = :mediaId")
    suspend fun deleteMediaTagCrossRefsByMediaId(mediaId: String)

    @Query("DELETE FROM MediaTagCrossRef WHERE mediaId IN (:mediaIds)")
    suspend fun deleteMediaTagCrossRefsByMediaIds(mediaIds: List<String>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMediaTagCrossRefs(crossRefs: List<MediaTagCrossRef>)

    /**
     * Удаляет теги вместе со связями и возвращает id медиа, оставшихся без единого тега.
     *
     * Транзакция нужна, несмотря на то что выборка здесь одна: между ней и удалением не должно
     * вклиниться сохранение с экрана Add. Иначе медиа, только что получившее новый тег, попало
     * бы в список осиротевших и лишилось разрешения на доступ, имея при этом тег
     */
    @Transaction
    suspend fun deleteTagsAndRelations(tagIds: List<Long>): List<String> {
        val orphanedMediaIds = getMediaIdsWithNoOtherTags(tagIds)
        tagIds.forEach { tagId ->
            deleteMediaTagCrossRefsByTagId(tagId)
            deleteTagById(tagId)
        }
        return orphanedMediaIds
    }

    /**
     * Медиа, у которых есть хотя бы один из переданных тегов и нет ни одного другого - то есть
     * те, что останутся без единого тега после удаления [tagIds].
     */
    @Query(
        """
        SELECT DISTINCT mediaId
        FROM MediaTagCrossRef
        WHERE tagId IN (:tagIds)
        AND mediaId NOT IN (
            SELECT mediaId FROM MediaTagCrossRef WHERE tagId NOT IN (:tagIds)
        )
        """
    )
    suspend fun getMediaIdsWithNoOtherTags(tagIds: List<Long>): List<String>

    @Query("DELETE FROM MediaTagCrossRef WHERE tagId = :tagId")
    suspend fun deleteMediaTagCrossRefsByTagId(tagId: Long)

    @Query("DELETE FROM Tag WHERE id = :tagId")
    suspend fun deleteTagById(tagId: Long)
}
