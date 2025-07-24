package com.dgopadakak.tagsgallery.gallery.ui.preview

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dgopadakak.tagsgallery.gallery.GalleryViewModel
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun MediaPreviewRow(
    uiStateStateFlow: StateFlow<GalleryViewModel.UiState>,
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
                isAlreadySaved = uiState.alreadySavedMedia.contains(uri),
                isActiveForIndividualTagsEdit = uiState.activeEditIndividualTags == uri,
                individualAddedTagsNum = uiState.perMediaAddedTagIds.getOrDefault(uri, emptyList()).size,
                individualRemovedTagsNum = uiState.perMediaRemovedTagIds.getOrDefault(uri, emptyList()).size,
                onPreviewClick = { onPreviewClick(uri) },
                onRemoveMediaClick = { onRemoveMediaClick(uri) }
            )
        }
    }
}
