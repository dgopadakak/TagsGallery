package com.dgopadakak.tagsgallery.core.local_storage.util

import android.content.ContentResolver
import android.net.Uri

fun ContentResolver.uriExists(uri: Uri): Boolean {
    return try {
        openFileDescriptor(uri, "r")?.use { true } ?: false
    } catch (_: Exception) {
        false
    }
}

fun ContentResolver.hasPersistedReadPermission(uri: Uri): Boolean {
    return persistedUriPermissions.any {
        it.uri == uri && it.isReadPermission
    }
}
