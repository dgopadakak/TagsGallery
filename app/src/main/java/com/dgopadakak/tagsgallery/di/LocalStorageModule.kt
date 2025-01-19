package com.dgopadakak.tagsgallery.di

import android.content.Context
import com.dgopadakak.tagsgallery.core.local_storage.room.AppDatabase
import com.dgopadakak.tagsgallery.core.local_storage.room.TagDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LocalStorageModule {
    @Provides
    fun provideTagsDao(database: AppDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }
}
