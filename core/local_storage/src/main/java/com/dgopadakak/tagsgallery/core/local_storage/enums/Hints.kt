package com.dgopadakak.tagsgallery.core.local_storage.enums

import androidx.annotation.StringRes
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.dgopadakak.tagsgallery.core.local_storage.R

enum class Hints(
    @field:StringRes val textRes: Int,
    val prefKey: Preferences.Key<Boolean>
) {
    TAGS_MAIN_HINT(R.string.tags_main_hint, booleanPreferencesKey("tags_main_hint")),
    GALLERY_MAIN_HINT(R.string.gallery_main_hint, booleanPreferencesKey("gallery_main_hint"))
}
