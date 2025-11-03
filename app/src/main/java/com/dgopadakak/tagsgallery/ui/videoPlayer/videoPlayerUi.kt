package com.dgopadakak.tagsgallery.ui.videoPlayer

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Forward5
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun VideoPlayerWithControls(
    uri: Uri,
    controlsVisible: MutableState<Boolean>,
    navBarOpenPadding: Dp,
    modifier: Modifier = Modifier
) {

    val videoUiState = rememberSaveable(uri) {
        mutableStateOf(VideoState())
    }


    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(5_000L)
            .setSeekForwardIncrementMs(5_000L)
            .build().apply {
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
                playWhenReady = videoUiState.value.isPlaying
                volume = if (videoUiState.value.isMuted) 0f else 1f
                seekTo(videoUiState.value.position)
            }
    }

    val aspectRatio = remember { mutableFloatStateOf(16f / 9f) }

    val controlsAlpha = animateFloatAsState(
        targetValue = if (controlsVisible.value) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )
    val hideByTimeoutJob = remember { mutableStateOf<Job?>(null) }

    val playerState = remember { mutableStateOf(exoPlayer.isPlaying) }
    val isMuted = remember { mutableStateOf(videoUiState.value.isMuted) }
    val duration = remember { mutableLongStateOf(0L) }
    val position = remember { mutableLongStateOf(0L) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                duration.longValue = player.duration.coerceAtLeast(0L)
                position.longValue = player.currentPosition.coerceAtLeast(0L)
                playerState.value = player.isPlaying
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    exoPlayer.seekTo(0)
                    exoPlayer.pause() // или сразу play(), если нужен автоповтор
                }
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.height != 0) {
                    aspectRatio.floatValue = videoSize.width.toFloat() / videoSize.height.toFloat()
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            videoUiState.value = VideoState(
                isPlaying = exoPlayer.isPlaying,
                isMuted = isMuted.value,
                position = exoPlayer.currentPosition
            )

            exoPlayer.removeListener(listener)
            exoPlayer.release()
            hideByTimeoutJob.value?.cancel()
        }
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            position.longValue = exoPlayer.currentPosition.coerceAtLeast(0L)
            delay(200L)
        }
    }

    fun restartHideTimer() {
        hideByTimeoutJob.value?.cancel()
        hideByTimeoutJob.value = scope.launch {
            delay(5000L)
            controlsVisible.value = false
        }
    }

    fun showControls() {
        controlsVisible.value = true
        restartHideTimer()
    }

    fun hideControls() {
        controlsVisible.value = false
        hideByTimeoutJob.value?.cancel()
    }

    LaunchedEffect(Unit) { restartHideTimer() }

    Box(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = null,
                onClick = if (controlsVisible.value) ::hideControls else ::showControls
            ),
        contentAlignment = Alignment.Center
    ) {
        if (aspectRatio.floatValue > 0f) {
            PlayerSurfaceFitted(
                player = exoPlayer,
                aspectRatio = aspectRatio.floatValue,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clickable(indication = null, interactionSource = null, onClick = ::showControls)
                .graphicsLayer { alpha = controlsAlpha.value }
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 16.dp + navBarOpenPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(position.longValue),
                    color = Color.White,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Slider(
                    value = position.longValue.toFloat(),
                    onValueChange = {
                        showControls()
                        exoPlayer.seekTo(it.toLong())
                    },
                    valueRange = 0f..duration.longValue.toFloat(),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = formatTime(duration.longValue),
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )

                IconButton(onClick = {
                    showControls()
                    isMuted.value = !isMuted.value
                    exoPlayer.volume = if (isMuted.value) 0f else 1f
                }) {
                    if (isMuted.value) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.VolumeOff,
                            tint = Color.White,
                            contentDescription = "Mute"
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.VolumeUp,
                            tint = Color.White,
                            contentDescription = "Unmute"
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    showControls()
                    exoPlayer.seekBack()
                }) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.Replay5,
                        tint = Color.White,
                        contentDescription = "Back 5s"
                    )
                }

                IconButton(onClick = {
                    showControls()
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                }) {
                    if (playerState.value) {
                        Icon(
                            modifier = Modifier.size(50.dp),
                            imageVector = Icons.Default.Pause,
                            tint = Color.White,
                            contentDescription = "Pause"
                        )
                    } else {
                        Icon(
                            modifier = Modifier.size(50.dp),
                            imageVector = Icons.Default.PlayArrow,
                            tint = Color.White,
                            contentDescription = "Play"
                        )
                    }
                }

                IconButton(onClick = {
                    showControls()
                    exoPlayer.seekForward()
                }) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.Forward5,
                        tint = Color.White,
                        contentDescription = "Forward 5s"
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerSurfaceFitted(
    player: ExoPlayer,
    aspectRatio: Float,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val parentWidth = constraints.maxWidth.toFloat()
        val parentHeight = constraints.maxHeight.toFloat()

        // ожидаемое соотношение сторон контейнера
        val containerRatio = parentWidth / parentHeight
        val videoModifier = if (aspectRatio > containerRatio) {
            // видео более широкое, чем контейнер -> ограничиваем по ширине
            Modifier
                .fillMaxWidth()
                .height((parentWidth / aspectRatio).toDp())
        } else {
            // видео более высокое -> ограничиваем по высоте
            Modifier
                .fillMaxHeight()
                .width((parentHeight * aspectRatio).toDp())
        }

        PlayerSurface(
            player = player,
            modifier = videoModifier
        )
    }


}

@Composable
private fun Float.toDp(): Dp {
    return with(LocalDensity.current) { this@toDp.toDp() }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val seconds = (totalSeconds % 60).toInt()
    val minutes = ((totalSeconds / 60) % 60).toInt()
    val hours = (totalSeconds / 3600).toInt()
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
