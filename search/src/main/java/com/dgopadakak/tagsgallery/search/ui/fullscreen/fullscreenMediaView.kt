package com.dgopadakak.tagsgallery.search.ui.fullscreen

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun FullScreenMediaView(
    contentModel: FullscreenContentModel,
    windowInsetsControllerCompat: WindowInsetsControllerCompat,
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

    // Механизм управления системными барами
    val controlsVisible = remember { mutableStateOf(true) }
    LaunchedEffect(controlsVisible.value) {
        if (controlsVisible.value) {
            windowInsetsControllerCompat.show(WindowInsetsCompat.Type.systemBars())
        } else {
            windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.systemBars())
            windowInsetsControllerCompat.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    // Механизм учета размера только открытого NavBar TODO: распространить на все приложение, чтоб при закрытии просмотрщика все приложение не прыгало
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val navBarPadding = remember { mutableStateOf(0.dp) }
    LaunchedEffect(bottomInset) {
        if (controlsVisible.value && bottomInset > navBarPadding.value) navBarPadding.value = bottomInset
    }

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
                    controlsVisible = controlsVisible,
                    navBarOpenPadding = navBarPadding.value,
                    modifier = animatedModifier
                )
            } else {
                ZoomableAsyncImage(
                    model = contentModel.placeholderImgRequests[page],
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = animatedModifier,
                    onClick = { controlsVisible.value = !controlsVisible.value }
                )
            }
        }
    }
}

@Composable
private fun VideoPlayerWithControls(
    uri: Uri,
    controlsVisible: MutableState<Boolean>,
    navBarOpenPadding: Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(5_000L)
            .setSeekForwardIncrementMs(5_000L)
            .build().apply {
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
                volume = 0f
                play()
            }
    }

    val controlsAlpha = animateFloatAsState(
        targetValue = if (controlsVisible.value) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )
    val hideByTimeoutJob = remember { mutableStateOf<Job?>(null) }

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

    LaunchedEffect(Unit) { restartHideTimer() } // TODO: просто разобраться почему при долистывании до видео не срабатывает (так и должно быть, срабатывает при прямом открытии видео)

    Box(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = null,
                onClick = if (controlsVisible.value) ::hideControls else ::showControls
            )
    ) {
        PlayerSurface(      // FIXME: пережимает соотношение сторон
            player = exoPlayer,
            modifier = Modifier.fillMaxSize()
        )

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
                    showControls()
                    exoPlayer.seekBack()
                }) {
                    Icon(modifier = Modifier.size(32.dp), imageVector = Icons.Default.Replay5, tint = Color.White, contentDescription = "Назад 5с")
                }

                IconButton(onClick = {
                    showControls()
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                }) {
                    if (playerState.value) {
                        Icon(modifier = Modifier.size(50.dp), imageVector = Icons.Default.Pause, tint = Color.White, contentDescription = "Пауза")
                    } else {
                        Icon(modifier = Modifier.size(50.dp), imageVector = Icons.Default.PlayArrow, tint = Color.White, contentDescription = "Плей")
                    }
                }

                IconButton(onClick = {
                    showControls()
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
