package com.dgopadakak.tagsgallery.search.ui.fullscreen

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun FullScreenMediaView(
    contentModel: FullscreenContentModel,
    onClose: () -> Unit
) {

    val pagerState = rememberPagerState(initialPage = contentModel.startIndex) {
        contentModel.placeholderImgRequests.size
    }

    val screenHeight = LocalWindowInfo.current.containerSize.height.toFloat()
    val closingAnimDuration = 150

    val offsetY = remember { Animatable(0f) }
    val animProgress = remember { Animatable(0f) }
    val backgroundAnimClosing = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val backgroundAlphaByPosition = (1f - (abs(offsetY.value) / 1000f).coerceIn(0f, 0.7f))
    val fullCalculatedBackgroundAlfa =
        backgroundAlphaByPosition * animProgress.value * backgroundAnimClosing.value

    BackHandler { onClose() }

    LaunchedEffect(Unit) {
        animProgress.animateTo(
            1f,
            animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = fullCalculatedBackgroundAlfa))
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    scope.launch {
                        offsetY.snapTo(offsetY.value + delta)
                    }
                },
                onDragStopped = { velocity ->
                    scope.launch {
                        // Если большое смещение или быстрый свайп
                        if (abs(offsetY.value) > 450f || abs(velocity) > 2000f) {
                            val sign = if (offsetY.value >= 0f) 1 else -1
                            val target = sign * screenHeight
                            launch {
                                offsetY.animateTo(
                                    targetValue = target,
                                    animationSpec = tween(
                                        durationMillis = closingAnimDuration,
                                        easing = LinearEasing
                                    )
                                )
                                onClose()
                            }
                            launch {
                                backgroundAnimClosing.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(
                                        durationMillis = closingAnimDuration,
                                        easing = LinearEasing
                                    )
                                )
                            }
                        } else {
                            offsetY.animateTo(
                                0f,
                                spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                            )
                        }
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // TODO: изменить анимацию листания
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            // вычисляем начальные параметры из startAnimationCoordinates
            val startRect = contentModel.startAnimationCoordinates

            val screenWidthPx = LocalWindowInfo.current.containerSize.width
            val screenHeightPx = LocalWindowInfo.current.containerSize.height

            val startWidth = startRect.width
            val startHeight = startRect.height
            val startCenterX = startRect.center.x
            val startCenterY = startRect.center.y

            // вычисляем scale и offset на основе прогресса
            val scX = lerp(startWidth / screenWidthPx, 1f, animProgress.value)
            val scY = lerp(startHeight / screenHeightPx, 1f, animProgress.value)
            val transX = lerp(startCenterX - screenWidthPx / 2, 0f, animProgress.value)
            val transY = lerp(startCenterY - screenHeightPx / 2, 0f, animProgress.value)

            val animatedModifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, offsetY.value.roundToInt()) }
                .graphicsLayer {
                    scaleX = scX
                    scaleY = scY
                    translationX = transX
                    translationY = transY
                }

            val uri = contentModel.uris[page]
            val isVideo = remember(uri) {
                val type = context.contentResolver.getType(uri)
                type?.startsWith("video") == true
            }

            if (isVideo) {
                VideoPlayerWithControls(
                    uri = uri,
                    modifier = animatedModifier
                )
            } else {
                ZoomableAsyncImage(
                    model = contentModel.placeholderImgRequests[page],
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = animatedModifier
                )
            }
        }
    }
}

@Composable
private fun VideoPlayerWithControls(
    uri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(5_000L)
            .setSeekForwardIncrementMs(5_000L)
            .build().apply {
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
                volume = 0f
            }
    }

    val playerState = remember { mutableStateOf(exoPlayer.isPlaying) }
    val isMuted = remember { mutableStateOf(true) }
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
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            position.longValue = exoPlayer.currentPosition.coerceAtLeast(0L)
            delay(200L)
        }
    }

    Box(modifier = modifier) {
        PlayerSurface(
            player = exoPlayer,
            modifier = Modifier.fillMaxSize()
        )

        // TODO: скрывать контролы по таймауту и по клику в любую часть экрана
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 16.dp)
                .navigationBarsPadding()
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
                    isMuted.value = !isMuted.value
                    exoPlayer.volume = if (isMuted.value) 0f else 1f
                }) {
                    if (isMuted.value) {
                        Icon(imageVector = Icons.AutoMirrored.Default.VolumeOff, tint = Color.White, contentDescription = "Без звука")
                    } else {
                        Icon(imageVector = Icons.AutoMirrored.Default.VolumeUp, tint = Color.White, contentDescription = "Со звуком")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    exoPlayer.seekBack()
                }) {
                    Icon(modifier = Modifier.size(32.dp), imageVector = Icons.Default.Replay5, tint = Color.White, contentDescription = "Назад 5с")
                }

                IconButton(onClick = {
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                }) {
                    if (playerState.value) {
                        Icon(modifier = Modifier.size(50.dp), imageVector = Icons.Default.Pause, tint = Color.White, contentDescription = "Пауза")
                    } else {
                        Icon(modifier = Modifier.size(50.dp), imageVector = Icons.Default.PlayArrow, tint = Color.White, contentDescription = "Плей")
                    }
                }

                IconButton(onClick = {
                    exoPlayer.seekForward()
                }) {
                    Icon(modifier = Modifier.size(32.dp), imageVector = Icons.Default.Forward5, tint = Color.White, contentDescription = "Вперёд 5с")
                }
            }
        }
    }
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
