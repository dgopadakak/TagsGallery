package com.dgopadakak.tagsgallery.core.local_storage.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dgopadakak.tagsgallery.core.local_storage.room.models.Tag
import com.dgopadakak.tagsgallery.core.local_storage.room.models.TagWithMedia
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM Tag")
    fun getAllTags(): Flow<List<Tag>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag): Long

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Transaction
    @Query("SELECT * FROM Tag WHERE id = :tagId")
    fun getTagWithMedia(tagId: Long): Flow<TagWithMedia>
}
