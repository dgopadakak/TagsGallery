package com.dgopadakak.tagsgallery.gallery.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dgopadakak.tagsgallery.core.compose.models.FullscreenContentModel
import com.dgopadakak.tagsgallery.core.compose.ui.FullTagsSelectionView
import com.dgopadakak.tagsgallery.core.local_storage.enums.Hints
import com.dgopadakak.tagsgallery.gallery.GalleryViewModel
import com.dgopadakak.tagsgallery.gallery.ui.elements.ActionBarLayout
import com.dgopadakak.tagsgallery.gallery.ui.elements.DeleteMediaConfirmDialog
import com.dgopadakak.tagsgallery.gallery.ui.elements.GalleryMediaGridContent
import com.dgopadakak.tagsgallery.gallery.ui.elements.SmallTagsRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onFullscreenContentSelected: (FullscreenContentModel) -> Unit,
    windowSizeClass: WindowSizeClass,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isMediaSelectionMode = uiState.selectedMediaUris.isNotEmpty()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val selectionLockedHint = "Finish media selection to change filters"
    val sheetPeekHeight = 100.dp
    val halfScreenHeight = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height.toDp() / 2
    }
    val isCompactWidth = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    if (isCompactWidth) {
        CompactGalleryContent(
            uiState = uiState,
            isMediaSelectionMode = isMediaSelectionMode,
            selectionLockedHint = selectionLockedHint,
            sheetPeekHeight = sheetPeekHeight,
            halfScreenHeight = halfScreenHeight,
            onFullscreenContentSelected = onFullscreenContentSelected,
            onToggleTag = viewModel::onTagToggle,
            onSetSortBy = viewModel::setSortBy,
            onSetFilterBy = viewModel::setFilterBy,
            onToggleMediaSelection = viewModel::toggleMediaSelection,
            onClearSelection = viewModel::clearSelection,
            onDeleteRequested = { showDeleteConfirmDialog = true }
        )
    } else {
        WideGalleryContent(
            uiState = uiState,
            isMediaSelectionMode = isMediaSelectionMode,
            selectionLockedHint = selectionLockedHint,
            onFullscreenContentSelected = onFullscreenContentSelected,
            onToggleTag = viewModel::onTagToggle,
            onSetSortBy = viewModel::setSortBy,
            onSetFilterBy = viewModel::setFilterBy,
            onToggleMediaSelection = viewModel::toggleMediaSelection,
            onClearSelection = viewModel::clearSelection,
            onDeleteRequested = { showDeleteConfirmDialog = true }
        )
    }
    if (showDeleteConfirmDialog) {
        DeleteMediaConfirmDialog(
            onConfirm = {
                viewModel.deleteSelectedMedia()
                showDeleteConfirmDialog = false
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

    LaunchedEffect(key1 = uiState.needToShowHint) {
        if (uiState.needToShowHint) {
            snackbarHostState.showSnackbar(Hints.GALLERY_MAIN_HINT.text)
            viewModel.setHintShown()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactGalleryContent(
    uiState: GalleryViewModel.UiState,
    isMediaSelectionMode: Boolean,
    selectionLockedHint: String,
    sheetPeekHeight: androidx.compose.ui.unit.Dp,
    halfScreenHeight: androidx.compose.ui.unit.Dp,
    onFullscreenContentSelected: (FullscreenContentModel) -> Unit,
    onToggleTag: (Long) -> Unit,
    onSetSortBy: (com.dgopadakak.tagsgallery.core.compose.enums.SortVariant) -> Unit,
    onSetFilterBy: (com.dgopadakak.tagsgallery.core.local_storage.models.Tag.Color?) -> Unit,
    onToggleMediaSelection: (android.net.Uri) -> Unit,
    onClearSelection: () -> Unit,
    onDeleteRequested: () -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    LaunchedEffect(isMediaSelectionMode) {
        if (isMediaSelectionMode) {
            scaffoldState.bottomSheetState.partialExpand()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = sheetPeekHeight,
        sheetSwipeEnabled = !isMediaSelectionMode,
        sheetShape = RectangleShape,
        sheetDragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth().clickable { },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                if (scaffoldState.bottomSheetState.targetValue == SheetValue.PartiallyExpanded
                    && uiState.selectedTagIds.isNotEmpty()
                ) {
                    val hintText = if (isMediaSelectionMode) {
                        selectionLockedHint
                    } else {
                        "Tap to remove tag, swipe up to add"
                    }
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = hintText,
                        fontSize = 14.sp
                    )
                }
            }
        },
        sheetContent = {
            val isExpanded = scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(halfScreenHeight)
            ) {
                Crossfade(targetState = isExpanded, label = "BottomSheetContent") { expanded ->
                    if (expanded) {
                        FullTagsSelectionView(
                            modifier = Modifier.padding(start = 8.dp),
                            tags = uiState.sortedFilteredTags,
                            selectedTagsIds = uiState.selectedTagIds,
                            onTagClick = { tagId ->
                                if (!isMediaSelectionMode) {
                                    onToggleTag(tagId)
                                }
                            },
                            sortBy = uiState.sortBy,
                            onSortVariantChanged = { sortBy ->
                                if (!isMediaSelectionMode) {
                                    onSetSortBy(sortBy)
                                }
                            },
                            filterBy = uiState.filterBy,
                            onFilterVariantChanged = { filterBy ->
                                if (!isMediaSelectionMode) {
                                    onSetFilterBy(filterBy)
                                }
                            }
                        )
                    } else {
                        SmallTagsRow(
                            tags = uiState.allTags.filter { uiState.selectedTagIds.contains(it.id) },
                            showRemoveIcon = !isMediaSelectionMode,
                            emptyStateText = if (isMediaSelectionMode) {
                                selectionLockedHint
                            } else {
                                "Swipe up here to choose tags for search"
                            }
                        ) {
                            if (!isMediaSelectionMode) {
                                onToggleTag(it)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        GalleryMediaGridContent(
            uiState = uiState,
            isMediaSelectionMode = isMediaSelectionMode,
            onFullscreenContentSelected = onFullscreenContentSelected,
            onToggleMediaSelection = onToggleMediaSelection,
            onClearSelection = onClearSelection,
            onDeleteRequested = onDeleteRequested,
            innerPadding = innerPadding,
            actionBarLayout = ActionBarLayout.VerticalEnd
        )
    }
}

@Composable
private fun WideGalleryContent(
    uiState: GalleryViewModel.UiState,
    isMediaSelectionMode: Boolean,
    selectionLockedHint: String,
    onFullscreenContentSelected: (FullscreenContentModel) -> Unit,
    onToggleTag: (Long) -> Unit,
    onSetSortBy: (com.dgopadakak.tagsgallery.core.compose.enums.SortVariant) -> Unit,
    onSetFilterBy: (com.dgopadakak.tagsgallery.core.local_storage.models.Tag.Color?) -> Unit,
    onToggleMediaSelection: (android.net.Uri) -> Unit,
    onClearSelection: () -> Unit,
    onDeleteRequested: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        val tagsPanelMinWidth = 320.dp
        val tagsPanelPreferredWidth = maxWidth * 0.34f
        val tagsPanelWidth = tagsPanelPreferredWidth.coerceAtLeast(tagsPanelMinWidth)

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(tagsPanelWidth)
            ) {
                FullTagsSelectionView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 8.dp, end = 8.dp),
                    tags = uiState.sortedFilteredTags,
                    selectedTagsIds = uiState.selectedTagIds,
                    onTagClick = { tagId ->
                        if (!isMediaSelectionMode) {
                            onToggleTag(tagId)
                        }
                    },
                    sortBy = uiState.sortBy,
                    onSortVariantChanged = { sortBy ->
                        if (!isMediaSelectionMode) {
                            onSetSortBy(sortBy)
                        }
                    },
                    filterBy = uiState.filterBy,
                    onFilterVariantChanged = { filterBy ->
                        if (!isMediaSelectionMode) {
                            onSetFilterBy(filterBy)
                        }
                    }
                )

                if (isMediaSelectionMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = selectionLockedHint
                        )
                    }
                }
            }
            GalleryMediaGridContent(
                uiState = uiState,
                isMediaSelectionMode = isMediaSelectionMode,
                onFullscreenContentSelected = onFullscreenContentSelected,
                onToggleMediaSelection = onToggleMediaSelection,
                onClearSelection = onClearSelection,
                onDeleteRequested = onDeleteRequested,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(end = 8.dp),
                actionBarLayout = ActionBarLayout.HorizontalBottom
            )
        }
    }
}
