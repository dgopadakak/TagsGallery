package com.dgopadakak.tagsgallery.core.local_storage.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dgopadakak.tagsgallery.core.local_storage.models.MediaTagCrossRef
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import com.dgopadakak.tagsgallery.core.local_storage.models.TagWithMedia
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM Tag")
    fun getAllTags(): Flow<List<Tag>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag): Long

    @Update
    suspend fun updateTag(tag: Tag)

    @Transaction
    @Query("SELECT * FROM Tag WHERE id = :tagId")
    fun getTagWithMedia(tagId: Long): Flow<TagWithMedia?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMediaTagCrossRef(crossRef: MediaTagCrossRef)

    @Transaction
    suspend fun deleteTagAndRelations(tag: Tag) {
        deleteMediaTagCrossRefsByTagId(tag.id)
        deleteTag(tag)
    }

    @Query("DELETE FROM MediaTagCrossRef WHERE tagId = :tagId")
    suspend fun deleteMediaTagCrossRefsByTagId(tagId: Long)

    @Delete
    suspend fun deleteTag(tag: Tag)
}
