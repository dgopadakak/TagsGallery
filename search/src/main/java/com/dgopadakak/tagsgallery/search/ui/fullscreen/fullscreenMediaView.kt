package com.dgopadakak.tagsgallery.search.ui.fullscreen

import android.net.Uri
import android.util.Log
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
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
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
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

    BackHandler { onClose(); Log.i("IWTSI", "onClose") }

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

    // ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
        }
    }

    val playerState = remember { mutableStateOf(exoPlayer.isPlaying) }
    val isMuted = remember { mutableStateOf(false) }
    val duration = remember { mutableLongStateOf(0L) }
    val position = remember { mutableLongStateOf(0L) }

    // слушаем обновления позиции
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                duration.longValue = player.duration.coerceAtLeast(0L)
                position.longValue = player.currentPosition.coerceAtLeast(0L)
                playerState.value = player.isPlaying
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(modifier = modifier) {
        // само видео
        PlayerSurface(
            player = exoPlayer,
            modifier = Modifier.fillMaxSize()
        )

        // кастомные контролы
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(8.dp)
        ) {
            // таймлайн
            Slider(
                value = position.longValue.toFloat(),
                onValueChange = {
                    exoPlayer.seekTo(it.toLong())
                },
                valueRange = 0f..duration.longValue.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
//                IconButton(onClick = {        // TODO иконки + проверить расстояние перемотки
//                    exoPlayer.seekBack() // по дефолту 5s, можно настроить
//                }) {
//                    Icon(Icons.Default.Replay5, contentDescription = "Назад 5с")
//                }

                IconButton(onClick = {
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                }) {
                    if (playerState.value) {
                        Icon(Icons.Default.Pause, contentDescription = "Пауза")
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Плей")
                    }
                }

//                IconButton(onClick = {    // TODO иконки + проверить расстояние перемотки
//                    exoPlayer.seekForward() // по дефолту 5s
//                }) {
//                    Icon(Icons.Default.Forward5, contentDescription = "Вперёд 5с")
//                }

//                IconButton(onClick = { // TODO иконки
//                    isMuted.value = !isMuted.value
//                    exoPlayer.volume = if (isMuted.value) 0f else 1f
//                }) {
//                    if (isMuted.value) {
//                        Icon(Icons.Default.VolumeOff, contentDescription = "Без звука")
//                    } else {
//                        Icon(Icons.Default.VolumeUp, contentDescription = "Со звуком")
//                    }
//                }
            }
        }
    }
}

