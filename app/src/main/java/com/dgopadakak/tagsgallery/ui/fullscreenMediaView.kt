package com.dgopadakak.tagsgallery.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.video.videoFrameMillis
import android.net.Uri
import com.dgopadakak.tagsgallery.core.compose.models.FullscreenContentModel
import com.dgopadakak.tagsgallery.core.compose.ui.RemoveAllTagsIcon
import com.dgopadakak.tagsgallery.ui.util.NavBarPaddingEditor
import com.dgopadakak.tagsgallery.ui.util.StatusBarPaddingEditor
import com.dgopadakak.tagsgallery.ui.videoPlayer.VideoPlayerWithControls
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun FullScreenMediaView(
    contentModel: FullscreenContentModel,
    windowInsetsControllerCompat: WindowInsetsControllerCompat,
    alreadyAnimated: Boolean,
    volumeKeyEvents: SharedFlow<Unit>,
    isMuted: Boolean,
    onAnimated: () -> Unit,
    onSetMuted: (Boolean) -> Unit,
    onDeleteMedia: (Uri) -> Unit,
    onClose: () -> Unit
) {

    val pagerState = rememberPagerState(initialPage = contentModel.startIndex) {
        contentModel.placeholderImgRequests.size
    }

    val screenHeight = LocalWindowInfo.current.containerSize.height.toFloat()
    val closingAnimDuration = 150

    val isSwipe = remember { mutableStateOf(false) }
    val offsetY = remember { Animatable(0f) }
    val animProgress = remember { Animatable(0f) }
    val backgroundAnimClosing = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showDeleteMediaDialog by remember { mutableStateOf(false) }

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

    val navBarPadding = remember { mutableStateOf(PaddingValues(0.dp)) }
    NavBarPaddingEditor(navBarPadding, controlsVisible)
    val statusBarPadding = remember { mutableStateOf(PaddingValues(0.dp)) }
    StatusBarPaddingEditor(statusBarPadding, controlsVisible)

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
                onDragStarted = { isSwipe.value = true },
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
                            isSwipe.value = false
                            offsetY.animateTo(
                                0f,
                                spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                            )
                        }
                    }
                }
            )
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
                        volumeKeyEvents = volumeKeyEvents,
                        isMuted = isMuted,
                        onSetMuted = onSetMuted,
                        modifier = animatedModifier
                    )
                } else {
                    // Превью для пролистывания
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(uri)
                            .videoFrameMillis(0L)
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

        TopQuickActions(
            visible = controlsVisible.value,
            isSwipe = isSwipe.value,
            statusBarPadding = statusBarPadding.value,
            navBarPadding = navBarPadding.value,
            onBack = onClose,
            onShare = {
                val uri = contentModel.uris[pagerState.currentPage]
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = context.contentResolver.getType(uri)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(shareIntent, "Поделиться через")
                context.startActivity(chooser)
            },
            onClearTags = { showDeleteMediaDialog = true }
        )

        if (showDeleteMediaDialog) {
            DeleteMediaConfirmDialog(
                onConfirm = {
                    showDeleteMediaDialog = false
                    onDeleteMedia(contentModel.uris[pagerState.currentPage])
                },
                onDismiss = { showDeleteMediaDialog = false }
            )
        }
    }
}

@Composable
private fun TopQuickActions(
    visible: Boolean,
    isSwipe: Boolean,
    statusBarPadding: PaddingValues,
    navBarPadding: PaddingValues,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onClearTags: () -> Unit
) {
    val alpha1 = animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )
    val alpha2 = animateFloatAsState(
        targetValue = if (!isSwipe) 1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha1.value * alpha2.value }
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(statusBarPadding)
            .padding(if (navBarPadding.calculateBottomPadding() == 0.dp) navBarPadding else PaddingValues())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                tint = Color.White,
                contentDescription = "Back"
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onShare) {
            Icon(
                imageVector = Icons.Default.Share,
                tint = Color.White,
                contentDescription = "Share"
            )
        }

        IconButton(onClick = onClearTags) {
            RemoveAllTagsIcon()
        }
    }
}

@Composable
private fun DeleteMediaConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove from app?") },
        text = {
            Text(
                "This will remove the media from this app only. " +
                    "The file will stay on your device and won't be deleted."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Remove")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
