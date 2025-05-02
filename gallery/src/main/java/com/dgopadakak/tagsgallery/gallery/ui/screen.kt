package com.dgopadakak.tagsgallery.gallery.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dgopadakak.tagsgallery.gallery.GalleryViewModel
import com.dgopadakak.tagsgallery.gallery.ui.preview.MediaPreviewRow
import com.dgopadakak.tagsgallery.gallery.ui.tags.TagsSegment
import kotlinx.coroutines.flow.StateFlow

@Composable
fun GalleryScreen(viewModel: GalleryViewModel = hiltViewModel()) {

    // TODO: избавиться от рекомпозиций данной функции из-за изменения в uiState не имеющих для нее
    //  значения параметров
    val uiState by viewModel.uiState.collectAsState()

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
            .padding(top = 8.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),     // На всякий случай для маленьких экранов
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        ButtonRow(
            uiStateFlow = viewModel.uiState,
            onClickSave = { viewModel.onClickSave() },
            onClickReset = { viewModel.onClickReset() },
            onAddMediaClick = {
                viewModel.setActiveUriForIndividualTags(null)
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageAndVideo
                    )
                )
            }
        )
        if (uiState.selectedUris.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                MediaPreviewRow(
                    uiStateStateFlow = viewModel.uiState,
                    onPreviewClick = { uri -> viewModel.setActiveUriForIndividualTags(uri) },
                    onRemoveMediaClick = { uri -> viewModel.removeSelectedMedia(uri) }
                )

                TagsSegment(
                    uiStateFlow = viewModel.uiState,
                    onCommonTagSelected = { id -> viewModel.onTagSelected(id) },
                    onSortVariantChanged = { sortVariant -> viewModel.setSortBy(sortVariant) },
                    onFilterVariantChanged = { filterVariant -> viewModel.setFilterBy(filterVariant) },
                    onIndividualTagToggle = { uri, tagId -> viewModel.onPerMediaTagToggle(uri, tagId) },
                    onIndividualTagAccept = { viewModel.setActiveUriForIndividualTags(null) }
                )
            }
        }
    }
}

@Composable
private fun ButtonRow(
    uiStateFlow: StateFlow<GalleryViewModel.UiState>,
    onClickSave: () -> Unit,
    onClickReset: () -> Unit,
    onAddMediaClick: () -> Unit
) {
    val uiState by uiStateFlow.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            modifier = Modifier
                .padding(start = 8.dp),
            onClick = onAddMediaClick
        ) {
            Text("Add media")
        }
        Row {
            Button(
                modifier = Modifier
                    .padding(end = 8.dp),
                onClick = onClickReset,
                enabled = uiState.selectedUris.isNotEmpty()
            ) {
                Text("Clear all")
            }
            Button(
                modifier = Modifier
                    .padding(end = 8.dp),
                onClick = onClickSave,
                enabled = uiState.selectedTagIds.isNotEmpty()
            ) {
                Text("Save")
            }
        }
    }
}
