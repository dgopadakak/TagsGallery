package com.dgopadakak.tagsgallery.gallery.ui.preview

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.video.videoFrameMillis
import com.dgopadakak.tagsgallery.gallery.GalleryViewModel
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun MediaPreviewRow(
    uiStateStateFlow: StateFlow<GalleryViewModel.GalleryUiState>,
    onPreviewClick: (Uri) -> Unit,
    onRemoveMediaClick: (Uri) -> Unit
) {
    val uiState by uiStateStateFlow.collectAsState()

    val previewSize = 120.dp
    val previewRows = 2
    val previewPadding = 8.dp

    LazyHorizontalGrid(
        modifier = Modifier
            .height(previewSize * previewRows + previewPadding * previewRows)
            .fillMaxWidth(),
        rows = GridCells.Fixed(2),
        contentPadding = PaddingValues(previewPadding),
        verticalArrangement = Arrangement.spacedBy(previewPadding),
        horizontalArrangement = Arrangement.spacedBy(previewPadding)
    ) {
        items(uiState.selectedUris) { uri ->
            MediaPreview(
                uri = uri,
                previewSize = previewSize,
                isActiveForIndividualTagsEdit = uiState.activeEditIndividualTags == uri,
                individualAddedTagsNum = uiState.perMediaAddedTagIds.getOrDefault(uri, emptyList()).size,
                individualRemovedTagsNum = uiState.perMediaRemovedTagIds.getOrDefault(uri, emptyList()).size,
                onPreviewClick = { onPreviewClick(uri) },
                onRemoveMediaClick = { onRemoveMediaClick(uri) }
            )
        }
    }
}

@Composable
private fun MediaPreview(
    uri: Uri,
    previewSize: Dp,
    isActiveForIndividualTagsEdit: Boolean,
    individualAddedTagsNum: Int,
    individualRemovedTagsNum: Int,
    onPreviewClick: () -> Unit,
    onRemoveMediaClick: () -> Unit
) {
    val context = LocalContext.current
    val isVideo = remember(uri) {
        val type = context.contentResolver.getType(uri)
        type?.startsWith("video") == true
    }

    // TODO: Посмотреть, почему тут не работает animatedPlacement, попробовать починить и
    //  и использовать тут
    Box(
        modifier = Modifier
            .size(previewSize)
            .clip(RoundedCornerShape(8.dp))
            .clickable{ onPreviewClick() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(uri)
                .apply {
                    if (isVideo) {
                        videoFrameMillis(1000L) // Выбор кадра для превью видео
                    }
                }
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (isVideo) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Video",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable { onRemoveMediaClick() }
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Удалить",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }

        if (individualAddedTagsNum != 0 || individualRemovedTagsNum != 0) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (individualAddedTagsNum != 0) {
                    Text(
                        text = "+$individualAddedTagsNum",
                        fontSize = 8.sp,
                        color = Color.White,
                        lineHeight = 8.sp
                    )
                }
                if (individualRemovedTagsNum != 0) {
                    Text(
                        text = "-$individualRemovedTagsNum",
                        fontSize = 8.sp,
                        color = Color.White,
                        lineHeight = 8.sp
                    )
                }
            }
        }
    }
}
