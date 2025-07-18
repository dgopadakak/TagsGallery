package com.dgopadakak.tagsgallery.gallery.ui.preview

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.dgopadakak.tagsgallery.gallery.GalleryViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.max

@Composable
fun MediaPreviewGrid(
    uiStateStateFlow: StateFlow<GalleryViewModel.UiState>,
    onPreviewClick: (Uri) -> Unit,
    onRemoveMediaClick: (Uri) -> Unit
) {
    val uiState by uiStateStateFlow.collectAsState()

    val previewSize = 120.dp
    val previewPadding = 8.dp
    val tagPanelMinWidth = 320.dp

    // Ложное срабатывание. Есть в списке проблем линтера.
    // https://googlesamples.github.io/android-custom-lint-rules/checks/UnusedBoxWithConstraintsScope.md.html
    @SuppressLint("UnusedBoxWithConstraintsScope")
    BoxWithConstraints {
        val availableWidth = maxWidth - tagPanelMinWidth - 2 * previewPadding
        val columns = max(1, (availableWidth / (previewSize + previewPadding)).toInt())
        val gridWidth = columns * (previewSize + previewPadding)

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .width(gridWidth)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(previewPadding),
            horizontalArrangement = Arrangement.spacedBy(previewPadding),
            contentPadding = PaddingValues(previewPadding)
        ) {
            items(uiState.selectedUris) { uri ->
                MediaPreview(
                    uri = uri,
                    previewSize = previewSize,
                    isActiveForIndividualTagsEdit = uiState.activeEditIndividualTags == uri,
                    individualAddedTagsNum = uiState.perMediaAddedTagIds[uri]?.size ?: 0,
                    individualRemovedTagsNum = uiState.perMediaRemovedTagIds[uri]?.size ?: 0,
                    onPreviewClick = { onPreviewClick(uri) },
                    onRemoveMediaClick = { onRemoveMediaClick(uri) }
                )
            }
        }
    }
}
