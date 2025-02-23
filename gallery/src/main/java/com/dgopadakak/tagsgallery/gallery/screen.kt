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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.video.VideoFrameDecoder
import com.dgopadakak.tagsgallery.core.compose.ui.TagsFlowRow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun GalleryScreen(viewModel: GalleryViewModel = hiltViewModel()) {

    val uiState by viewModel.state.collectAsState()

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
            PreviewRow(viewModel.state)
            TagsSegment(
                uiStateFlow = viewModel.state,
                onTagSelected = { id -> viewModel.onTagSelected(id) }
            )
        }
        ButtonBlock(
            uiStateFlow = viewModel.state,
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
private fun PreviewRow(     // FIXME: у изображений, не дотягивающихся до углов - не скругляются углы
    uiStateFlow: StateFlow<GalleryViewModel.UiState>
) {
    val selectedUris by remember {
        derivedStateOf { uiStateFlow.value.selectedUris }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        selectedUris.forEach { uri ->
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
    uiStateFlow: StateFlow<GalleryViewModel.UiState>,
    onTagSelected: (Long) -> Unit
) {
    val uiState by uiStateFlow.collectAsState()
    TagsFlowRow(
        modifier = modifier,
        tags = uiState.tags,
        selectedTagsIds = uiState.selectedTagIds,
        onTagClick = onTagSelected
    )
}

@Composable
private fun ButtonBlock(
    uiStateFlow: StateFlow<GalleryViewModel.UiState>,
    onClickSave: () -> Unit,
    onClickReset: () -> Unit,
    onAddMediaClick: () -> Unit
) {
    val uiState by uiStateFlow.collectAsState()
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
                enabled = uiState.selectedTagIds.isNotEmpty()
            ) {
                Text("Add")
            }
            Button(
                modifier = Modifier
                    .padding(start = 4.dp),
                onClick = onClickReset,
                enabled = uiState.selectedUris.isNotEmpty()
            ) {
                Text("Reset")
            }
        }
    }
}
