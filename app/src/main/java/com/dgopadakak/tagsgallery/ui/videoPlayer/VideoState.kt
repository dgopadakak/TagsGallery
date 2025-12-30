package com.dgopadakak.tagsgallery.ui.videoPlayer

import java.io.Serializable

data class VideoState (
    val isPlaying: Boolean = true,
    val isMuted: Boolean,
    val position: Long = 0L
) : Serializable
