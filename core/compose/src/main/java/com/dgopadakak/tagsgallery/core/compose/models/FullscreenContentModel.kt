package com.dgopadakak.tagsgallery.core.compose.models

import android.net.Uri
import androidx.compose.ui.geometry.Rect
import coil3.request.ImageRequest

data class FullscreenContentModel(
    val startIndex: Int,
    val uris: List<Uri>,
    val placeholderImgRequests: List<ImageRequest>,
    val startAnimationCoordinates: Rect
)