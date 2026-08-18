package com.dgopadakak.tagsgallery.add.ui

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dgopadakak.tagsgallery.add.AddViewModel
import com.dgopadakak.tagsgallery.add.R
import com.dgopadakak.tagsgallery.add.ui.preview.MediaPreviewGrid
import com.dgopadakak.tagsgallery.add.ui.preview.MediaPreviewRow
import com.dgopadakak.tagsgallery.add.ui.tags.TagsSegment
import com.dgopadakak.tagsgallery.add.util.hasAnyTagsToSave
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import com.dgopadakak.tagsgallery.core.compose.R as CoreR

@Composable
fun AddScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: AddViewModel = hiltViewModel()
) {

    // TODO: избавиться от рекомпозиций данной функции из-за изменения в uiState не имеющих для нее
    //  значения параметров
    val uiState by viewModel.uiState.collectAsState()
    var isPhotoPickerActive by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        isPhotoPickerActive = false
        viewModel.addSelectedMedia(uris)
    }

    // На случай, если, например, из-за ошибки в PhotoPicker лямбда в photoPickerLauncher не вызовется
    LaunchedEffect(key1 = isPhotoPickerActive) {
        if (isPhotoPickerActive) {
            delay(500)
            isPhotoPickerActive = false
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
            onClickSave = { viewModel.onClickSave(context.contentResolver) },
            onClickReset = { viewModel.onClickReset() },
            onAddMediaClick = {
                if (!isPhotoPickerActive) {
                    isPhotoPickerActive = true
                    viewModel.setActiveUriForIndividualTags(null)
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageAndVideo
                        )
                    )
                }
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
                    text = stringResource(R.string.add_empty_state),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ButtonRow(
    uiStateFlow: StateFlow<AddViewModel.UiState>,
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
            Text(stringResource(R.string.action_add))
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onClickReset,
                enabled = uiState.selectedUris.isNotEmpty()
            ) {
                Text(stringResource(R.string.action_clear))
            }

            FilledTonalButton(
                onClick = onClickSave,
                enabled = hasAnyTagsToSave(
                    selectedCommonTagIds = uiState.selectedTagIds,
                    allIndividualAddedTagIds = uiState.perMediaAddedTagIds.values
                )
            ) {
                Text(stringResource(CoreR.string.action_save))
            }
        }
    }
}
