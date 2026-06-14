package com.dgopadakak.tagsgallery.gallery.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.util.Locale
import java.util.concurrent.TimeUnit

internal fun getVideoDuration(context: Context, videoUri: Uri): String {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(context, videoUri)
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return formatDuration(durationStr?.toLongOrNull() ?: 0)
    } catch (e: Exception) {
        e.printStackTrace()
        return "00:00"
    } finally {
        retriever.release()
    }
}

private fun formatDuration(milliseconds: Long): String {
    if (milliseconds <= 0) {
        return "00:00"
    }

    val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60

    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
