package com.dgopadakak.tagsgallery.core.local_storage.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dgopadakak.tagsgallery.core.local_storage.room.models.Tag
import com.dgopadakak.tagsgallery.core.local_storage.room.models.MediaTagCrossRef

@Database(entities = [Tag::class, MediaTagCrossRef::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tags_gallery_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
