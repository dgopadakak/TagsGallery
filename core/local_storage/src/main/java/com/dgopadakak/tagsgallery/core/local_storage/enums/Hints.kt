package com.dgopadakak.tagsgallery.core.local_storage.enums

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey

enum class Hints(
    val text: String,
    val prefKey: Preferences.Key<Boolean>
) {
    TAGS_MAIN_HINT("Tap to edit, hold to select", booleanPreferencesKey("tags_main_hint")),
    GALLERY_MAIN_HINT("Tap \"Add media\" to begin", booleanPreferencesKey("gallery_main_hint")),
    SEARCH_MAIN_HINT("Here you can search via tags", booleanPreferencesKey("search_main_hint"))
}