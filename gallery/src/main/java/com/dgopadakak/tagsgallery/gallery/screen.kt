package com.dgopadakak.tagsgallery.gallery

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.video.videoFrameMillis
import com.dgopadakak.tagsgallery.core.compose.enums.SortVariant
import com.dgopadakak.tagsgallery.core.compose.ui.TagsSelectionView
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import kotlinx.coroutines.flow.StateFlow

@Composable
fun GalleryScreen(viewModel: GalleryViewModel = hiltViewModel()) {

    val uiState by viewModel.galleryMediaUiState.collectAsState()

    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        viewModel.addSelectedMedia(uris.toMutableList())
        uris.forEach { uri ->
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),     // На всякий случай для маленьких экранов
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (uiState.selectedUris.isEmpty()) {
            Text(
                text = "Select the media to apply the tags to"
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                PreviewRow(
                    galleryMediaUiStateStateFlow = viewModel.galleryMediaUiState,
                    onRemoveMediaClick = { uri -> viewModel.removeSelectedMedia(uri) }
                )
                TagsSegment(
                    galleryTagsUiStateFlow = viewModel.galleryTagsUiState,
                    onTagSelected = { id -> viewModel.onTagSelected(id) },
                    onSortVariantChanged = { sortVariant -> viewModel.setSortBy(sortVariant) },
                    onFilterVariantChanged = { filterVariant -> viewModel.setFilterBy(filterVariant) }
                )
            }
        }
        ButtonBlock(
            galleryMediaUiStateFlow = viewModel.galleryMediaUiState,
            galleryTagsUiStateFlow = viewModel.galleryTagsUiState,
            onClickSave = { viewModel.onClickSave() },
            onClickReset = { viewModel.onClickReset() },
            onAddMediaClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageAndVideo
                    )
                )
            }
        )
    }
}

@Composable
private fun PreviewRow(
    galleryMediaUiStateStateFlow: StateFlow<GalleryViewModel.GalleryMediaUiState>,
    onRemoveMediaClick: (Uri) -> Unit
) {
    val context = LocalContext.current
    val uiState by galleryMediaUiStateStateFlow.collectAsState()

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
            val isVideo = remember(uri) {
                val type = context.contentResolver.getType(uri)
                type?.startsWith("video") == true
            }

            // TODO: Посмотреть, почему тут не работает animatedPlacement, попробовать починить и
            //  и заюзать тут
            Box(
                modifier = Modifier
                    .size(previewSize)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(uri)
                        .apply {
                            if (isVideo) {
                                videoFrameMillis(1000L) // превью видео
                            }
                        }
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isVideo) {
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
                        .padding(4.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { onRemoveMediaClick(uri) }
                        .padding(4.dp) // Внутренний отступ для иконки
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Удалить",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TagsSegment(
    modifier: Modifier = Modifier,
    galleryTagsUiStateFlow: StateFlow<GalleryViewModel.GalleryTagsUiState>,
    onTagSelected: (Long) -> Unit,
    onSortVariantChanged: (SortVariant) -> Unit,
    onFilterVariantChanged: (Tag.Color?) -> Unit
) {
    val uiState by galleryTagsUiStateFlow.collectAsState()
    TagsSelectionView(
        modifier = modifier,
        tags = uiState.tags,
        selectedTagsIds = uiState.selectedTagIds,
        onTagClick = onTagSelected,
        sortBy = uiState.sortBy,
        onSortVariantChanged = onSortVariantChanged,
        filterBy = uiState.filterBy,
        onFilterVariantChanged = onFilterVariantChanged
    )
}

@Composable
private fun ButtonBlock(
    galleryMediaUiStateFlow: StateFlow<GalleryViewModel.GalleryMediaUiState>,
    galleryTagsUiStateFlow: StateFlow<GalleryViewModel.GalleryTagsUiState>,
    onClickSave: () -> Unit,
    onClickReset: () -> Unit,
    onAddMediaClick: () -> Unit
) {
    val galleryUiState by galleryTagsUiStateFlow.collectAsState()
    val previewUiState by galleryMediaUiStateFlow.collectAsState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onAddMediaClick
        ) {
            Text("Add media")
        }
        Row {
            Button(
                modifier = Modifier
                    .padding(end = 4.dp),
                onClick = onClickSave,
                enabled = galleryUiState.selectedTagIds.isNotEmpty()
            ) {
                Text("Apply")
            }
            Button(
                modifier = Modifier
                    .padding(start = 4.dp),
                onClick = onClickReset,
                enabled = previewUiState.selectedUris.isNotEmpty()
            ) {
                Text("Clear")
            }
        }
    }
}
