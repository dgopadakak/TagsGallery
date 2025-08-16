package com.dgopadakak.tagsgallery.search.ui

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
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
        contentModel.uriList.size
    }


    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val alpha = 1f - (abs(offsetY.value) / 1000f).coerceIn(0f, 0.7f)

    BackHandler { onClose(); Log.i("IWTSI", "onClose") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = alpha))
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
                    if (abs(offsetY.value) > 400f || abs(velocity) > 2000f) {
                        onClose()
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
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            ZoomableAsyncImage(
                model = contentModel.uriList[page],
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, offsetY.value.roundToInt()) }
            )
        }
    }
}

data class FullscreenContentModel(
    val startIndex: Int,
    val uriList: List<Uri>
)
