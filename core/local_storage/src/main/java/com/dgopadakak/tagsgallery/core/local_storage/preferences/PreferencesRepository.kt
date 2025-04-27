package com.dgopadakak.tagsgallery.core.local_storage.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.dgopadakak.tagsgallery.core.local_storage.enums.Hints
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PreferencesRepository(private val dataStore: DataStore<Preferences>) {

    @Suppress("NullableBooleanElvis")
    suspend fun isHintShown(hint: Hints): Boolean {
        return dataStore.data
            .map {
                it[hint.prefKey] ?: false
            }
            .first()
    }

    suspend fun setHintShown(hint: Hints) {
        dataStore.edit {
            it[hint.prefKey] = true
        }
    }
}
