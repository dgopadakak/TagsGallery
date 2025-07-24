package com.dgopadakak.tagsgallery.gallery.ui.preview

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.video.videoFrameMillis
import com.dgopadakak.tagsgallery.core.compose.ui.EditModeWarningIcon

@Composable
internal fun MediaPreview(
    uri: Uri,
    previewSize: Dp,
    isAlreadySaved: Boolean,
    isActiveForIndividualTagsEdit: Boolean,
    individualAddedTagsNum: Int,
    individualRemovedTagsNum: Int,
    onPreviewClick: () -> Unit,
    onRemoveMediaClick: () -> Unit
) {
    val context = LocalContext.current
    val borderColor by animateColorAsState(
        if (isActiveForIndividualTagsEdit){
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        }
    )
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
            .border(4.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable{ onPreviewClick() }
    ) {
        if (isVideo && isActiveForIndividualTagsEdit) {
            AnimatedVideoPreview(uri)
        } else {
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
        }

        if (isVideo && !isActiveForIndividualTagsEdit) {
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
                .padding(6.dp)
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
                    .padding(6.dp)
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

        if (isAlreadySaved) {
            EditModeWarningIcon(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp),
                iconSize = 14.dp
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun AnimatedVideoPreview(
    uri: Uri
) {
    val context = LocalContext.current

    val mediaItem = remember(uri) {
        MediaItem.fromUri(uri)
    }

    val player = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(mediaItem)
            prepare()
            volume = 0f
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }.apply {
        seekTo(0)
        playWhenReady = true
    }

    DisposableEffect(uri) {
        onDispose { player.release() }
    }

    // TODO: исследована куча альтернатив, которые позволяют сделать crop, но все они отброшены либо
    //  по причине сложности/невозможности установки surfaceType. Если будет обновление, позволяющее
    //  использовать crop в рамках PlayerSurface - заюзать.
    PlayerSurface(
        player = player,
        // Важен именно этот surfaceType для работоспособности анимации затухания при навигации
        surfaceType = SURFACE_TYPE_TEXTURE_VIEW
    )
}
