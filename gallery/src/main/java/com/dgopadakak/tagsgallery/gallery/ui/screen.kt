package com.dgopadakak.tagsgallery.gallery.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dgopadakak.tagsgallery.gallery.GalleryViewModel
import com.dgopadakak.tagsgallery.gallery.ui.preview.MediaPreviewGrid
import com.dgopadakak.tagsgallery.gallery.ui.preview.MediaPreviewRow
import com.dgopadakak.tagsgallery.gallery.ui.tags.TagsSegment
import kotlinx.coroutines.flow.StateFlow

@Composable
fun GalleryScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: GalleryViewModel = hiltViewModel()
) {

    // TODO: избавиться от рекомпозиций данной функции из-за изменения в uiState не имеющих для нее
    //  значения параметров
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        viewModel.addSelectedMedia(uris)
        // TODO: убрать, реализовать в data-слое
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
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ButtonRow(
            uiStateFlow = viewModel.uiState,
            onClickSave = { viewModel.onClickSave() },
            onClickReset = { viewModel.onClickReset() },
            onAddMediaClick = {
                viewModel.setActiveUriForIndividualTags(null)
                // TODO: реализовать защиту от многократного нажатия
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageAndVideo
                    )
                )
            }
        )
        if (uiState.selectedUris.isNotEmpty()) {
            if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
                Column {
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
            } else {
                Row {
                    MediaPreviewGrid(
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
        } else {
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(0.75f),
                    text = "Click \"Add\" to start",
                    textAlign = TextAlign.Center
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
            .fillMaxWidth()
            .padding(all = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(
            onClick = onAddMediaClick
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Add")
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onClickReset,
                enabled = uiState.selectedUris.isNotEmpty()
            ) {
                Text("Clear")
            }

            FilledTonalButton(
                onClick = onClickSave,
                enabled = uiState.selectedTagIds.isNotEmpty()
            ) {
                Text("Save")
            }
        }
    }
}
