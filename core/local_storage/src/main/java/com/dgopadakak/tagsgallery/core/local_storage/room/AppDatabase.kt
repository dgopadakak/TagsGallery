package com.dgopadakak.tagsgallery.core.local_storage.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import com.dgopadakak.tagsgallery.core.local_storage.models.MediaTagCrossRef

@Database(entities = [Tag::class, MediaTagCrossRef::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tagDao(): TagDao

    companion object {
        fun getInstance(context: Context, name: String): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                name
            ).build()
        }
    }
}
