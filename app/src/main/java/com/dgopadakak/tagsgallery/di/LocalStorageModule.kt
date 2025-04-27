package com.dgopadakak.tagsgallery.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.dgopadakak.tagsgallery.core.local_storage.Repository
import com.dgopadakak.tagsgallery.core.local_storage.preferences.PreferencesRepository
import com.dgopadakak.tagsgallery.core.local_storage.room.AppDatabase
import com.dgopadakak.tagsgallery.core.local_storage.room.TagDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LocalStorageModule {

    @Provides
    fun provideRepository(tagDao: TagDao, preferencesRepository: PreferencesRepository): Repository {
        return Repository(
            tagDao = tagDao,
            preferencesRepository = preferencesRepository,
            dispatcher = Dispatchers.IO
        )
    }

    @Provides
    fun provideTagDao(database: AppDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun providePreferencesRepository(dataStore: DataStore<Preferences>): PreferencesRepository {
        return PreferencesRepository(dataStore = dataStore)
    }

    @Provides
    @Singleton
    fun provideHintsDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
