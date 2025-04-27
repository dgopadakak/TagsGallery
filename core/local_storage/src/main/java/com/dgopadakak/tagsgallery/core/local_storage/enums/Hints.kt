package com.dgopadakak.tagsgallery.core.local_storage.enums

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey

enum class Hints(
    val text: String,
    val prefKey: Preferences.Key<Boolean>
) {
    TAGS_MAIN_HINT("Tap tag to edit, hold to select and delete", booleanPreferencesKey("tags_main_hint")),
    SEARCH_MAIN_HINT("Here you can search for added media using tags", booleanPreferencesKey("search_main_hint"))
}