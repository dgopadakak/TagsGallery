package com.dgopadakak.tagsgallery.gallery

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.video.VideoFrameDecoder
import com.dgopadakak.tagsgallery.core.compose.enums.SortVariant
import com.dgopadakak.tagsgallery.core.compose.ui.TagsSelectionView
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import kotlinx.coroutines.flow.StateFlow

@Composable
fun GalleryScreen(viewModel: GalleryViewModel = hiltViewModel()) {

    val uiState by viewModel.galleryMediaUiState.collectAsState()

    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(        // TODO: Постараться найти способ передать в PhotoPicker уже выбранные медиа
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
            .fillMaxSize(),
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
                PreviewRow(viewModel.galleryMediaUiState)
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

// FIXME: у изображений, не дотягивающихся до углов - не скругляются углы
// FIXME: хранить ScrollState где-то типо в скоупе viewModel. То же с картинками, а то при перехода
//  с экрана на экран все грузится заново
@Composable
private fun PreviewRow(
    galleryMediaUiStateStateFlow: StateFlow<GalleryViewModel.GalleryMediaUiState>
) {
    val selectedUris by galleryMediaUiStateStateFlow.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        selectedUris.selectedUris.forEach { uri ->
            val imageLoader = ImageLoader.Builder(LocalContext.current)
                .components {
                    add(VideoFrameDecoder.Factory())
                }
                .build()
            AsyncImage(
                modifier = Modifier
                    .height(180.dp)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(4.dp)),
                model = uri,
                imageLoader = imageLoader,
                contentDescription = "Media file selected by user"
            )
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
                Text("Add")
            }
            Button(
                modifier = Modifier
                    .padding(start = 4.dp),
                onClick = onClickReset,
                enabled = previewUiState.selectedUris.isNotEmpty()
            ) {
                Text("Reset")
            }
        }
    }
}
