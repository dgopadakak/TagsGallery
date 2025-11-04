package com.dgopadakak.tagsgallery.ui

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.video.videoFrameMillis
import com.dgopadakak.tagsgallery.core.compose.models.FullscreenContentModel
import com.dgopadakak.tagsgallery.ui.videoPlayer.VideoPlayerWithControls
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun FullScreenMediaView(
    contentModel: FullscreenContentModel,
    windowInsetsControllerCompat: WindowInsetsControllerCompat,
    alreadyAnimated: Boolean,
    onAnimated: () -> Unit,
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
    val inset = WindowInsets.navigationBars.asPaddingValues()
    val navBarPadding = remember { mutableStateOf(PaddingValues(0.dp)) }
    LaunchedEffect(inset.calculateBottomPadding()) {
        if (controlsVisible.value && inset.calculateBottomPadding() > navBarPadding.value.calculateBottomPadding())
            navBarPadding.value = PaddingValues(
                start = 0.dp,
                top = 0.dp,
                end = 0.dp,
                bottom = inset.calculateBottomPadding()
            )
    }
    LaunchedEffect(inset.calculateLeftPadding(LayoutDirection.Ltr)) {
        if (controlsVisible.value && inset.calculateLeftPadding(LayoutDirection.Ltr) > navBarPadding.value.calculateLeftPadding(LayoutDirection.Ltr))
            navBarPadding.value = PaddingValues(
                start = inset.calculateLeftPadding(LayoutDirection.Ltr),
                top = 0.dp,
                end = 0.dp,
                bottom = 0.dp
            )
    }
    LaunchedEffect(inset.calculateRightPadding(LayoutDirection.Ltr)) {
        if (controlsVisible.value && inset.calculateRightPadding(LayoutDirection.Ltr) > navBarPadding.value.calculateRightPadding(LayoutDirection.Ltr))
            navBarPadding.value = PaddingValues(
                start = 0.dp,
                top = 0.dp,
                end = inset.calculateRightPadding(LayoutDirection.Ltr),
                bottom = 0.dp
            )
    }

    BackHandler { onClose() }

    LaunchedEffect(Unit) {
        if (alreadyAnimated) {
            animProgress.snapTo(1f)
        } else {
            animProgress.animateTo(
                1f,
                animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing)
            )
            onAnimated()
        }
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
                if (pagerState.currentPage == page) {
                    VideoPlayerWithControls(
                        uri = uri,
                        controlsVisible = controlsVisible,
                        navBarOpenPadding = navBarPadding.value,
                        modifier = animatedModifier
                    )
                } else {
                    // Превью для пролистывания
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(uri)
                            .videoFrameMillis(1000L)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = animatedModifier
                    )
                }
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
