package com.dgopadakak.tagsgallery.core.local_storage.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.dgopadakak.tagsgallery.core.local_storage.R
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import com.dgopadakak.tagsgallery.core.local_storage.room.TagDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DatabaseInitializer(
    private val context: Context,
    private val tagDao: TagDao,
    private val dataStore: DataStore<Preferences>
) {
    private val firstLaunchKey = booleanPreferencesKey("first_launch")

    @Suppress("NullableBooleanElvis")
    suspend fun initializeIfNeeded() {
        val isFirstLaunch = dataStore.data
            .map { it[firstLaunchKey] ?: true }
            .first()

        if (isFirstLaunch) {
            tagDao.insertAllTags(
                Tag(name = context.getString(R.string.default_tag_dog), color = Tag.Color.NO_COLOR),
                Tag(name = context.getString(R.string.default_tag_forest), color = Tag.Color.GREEN),
                Tag(name = context.getString(R.string.default_tag_sky), color = Tag.Color.BLUE),
                Tag(name = context.getString(R.string.default_tag_sunset), color = Tag.Color.RED),
            )
            dataStore.edit { it[firstLaunchKey] = false }
        }
    }
}
