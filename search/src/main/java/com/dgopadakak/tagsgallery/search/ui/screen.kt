package com.dgopadakak.tagsgallery.search.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.dgopadakak.tagsgallery.core.compose.models.FullscreenContentModel
import com.dgopadakak.tagsgallery.core.compose.ui.FullTagsSelectionView
import com.dgopadakak.tagsgallery.core.compose.ui.RemoveAllTagsIcon
import com.dgopadakak.tagsgallery.core.local_storage.enums.Hints
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import com.dgopadakak.tagsgallery.search.SearchViewModel
import com.dgopadakak.tagsgallery.search.util.getVideoDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onFullscreenContentSelected: (FullscreenContentModel) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isMediaSelectionMode = uiState.selectedMediaUris.isNotEmpty()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val scaffoldState = rememberBottomSheetScaffoldState(
        snackbarHostState = snackbarHostState
    )

    val selectionLockedHint = "Finish media selection to change filters"
    val sheetPeekHeight = 100.dp
    val halfScreenHeight = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height.toDp() / 2
    }

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
                    && uiState.selectedTagIds.isNotEmpty()) {
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
                                    viewModel.onTagToggle(tagId)
                                }
                            },
                            sortBy = uiState.sortBy,
                            onSortVariantChanged = { sortBy ->
                                if (!isMediaSelectionMode) {
                                    viewModel.setSortBy(sortBy)
                                }
                            },
                            filterBy = uiState.filterBy,
                            onFilterVariantChanged = { filterBy ->
                                if (!isMediaSelectionMode) {
                                    viewModel.setFilterBy(filterBy)
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
                                viewModel.onTagToggle(it)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (uiState.foundedMediaUris.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No media found for the selected tags")
            }
        } else {
            // ZoomableAsyncImage в FullScreenMediaView сам запрашивает лучшее качество
            val requestsList = uiState.foundedMediaUris.map { uriToConvert ->
                ImageRequest.Builder(context)
                    .data(uriToConvert)
                    .crossfade(true)
                    .memoryCacheKey(uriToConvert.toString())
                    .size(250)
                    .build()
            }
            val selectedUris = uiState.selectedMediaUris

            Box(modifier = Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(uiState.foundedMediaUris, key = { _, uri -> uri }) { index, uri ->
                        var itemBounds = Rect(0f, 0f, 0f, 0f)
                        SearchMediaPreviewItem(
                            modifier = Modifier
                                .onGloballyPositioned { coordinates ->
                                    itemBounds = coordinates.boundsInWindow()
                                },
                            uri = uri,
                            request = requestsList[index],
                            isSelected = uri in selectedUris,
                            onItemClick = {
                                if (isMediaSelectionMode) {
                                    viewModel.toggleMediaSelection(uri)
                                } else {
                                    onFullscreenContentSelected(
                                        FullscreenContentModel(
                                            startIndex = index,
                                            uris = uiState.foundedMediaUris,
                                            placeholderImgRequests = requestsList,
                                            startAnimationCoordinates = itemBounds
                                        )
                                    )
                                }
                            },
                            onItemLongClick = { viewModel.toggleMediaSelection(uri) }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isMediaSelectionMode,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp),
                    enter = slideInHorizontally(
                        initialOffsetX = { it / 2 },
                        animationSpec = tween(220)
                    ) + fadeIn(animationSpec = tween(220)),
                    exit = slideOutHorizontally(
                        targetOffsetX = { it / 2 },
                        animationSpec = tween(180)
                    ) + fadeOut(animationSpec = tween(180))
                ) {
                    SelectionActionBar(
                        selectedCount = selectedUris.size,
                        onBack = { viewModel.clearSelection() },
                        onShare = {
                            val uris = selectedUris.toList()
                            val shareIntent = if (uris.size == 1) {
                                Intent(Intent.ACTION_SEND).apply {
                                    type = context.contentResolver.getType(uris[0]) ?: "*/*"
                                    putExtra(Intent.EXTRA_STREAM, uris[0])
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                            } else {
                                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                    type = "*/*"
                                    putParcelableArrayListExtra(
                                        Intent.EXTRA_STREAM,
                                        ArrayList(uris)
                                    )
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                            }
                            context.startActivity(Intent.createChooser(shareIntent, null))
                        },
                        onDelete = { showDeleteConfirmDialog = true }
                    )
                }
            }

            if (showDeleteConfirmDialog) {
                DeleteMediaConfirmDialog(
                    onConfirm = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteSelectedMedia()
                    },
                    onDismiss = { showDeleteConfirmDialog = false }
                )
            }
        }
    }

    LaunchedEffect(key1 = uiState.needToShowHint) {
        if (uiState.needToShowHint) {
            snackbarHostState.showSnackbar(Hints.SEARCH_MAIN_HINT.text)
            viewModel.setHintShown()
        }
    }
}

@Composable
private fun SelectionActionBar(
    modifier: Modifier = Modifier,
    selectedCount: Int,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear selection"
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = selectedCount.toString(),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share"
                )
            }
            IconButton(onClick = onDelete) {
                RemoveAllTagsIcon(tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun DeleteMediaConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove from app?") },
        text = {
            Text(
                "This will remove the media from this app only. " +
                    "The file will stay on your device and won't be deleted."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Remove")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SearchMediaPreviewItem(
    modifier: Modifier = Modifier,
    uri: Uri,
    request: ImageRequest,
    isSelected: Boolean = false,
    onItemClick: (Uri) -> Unit,
    onItemLongClick: (Uri) -> Unit
) {
    val context = LocalContext.current
    val isVideo = remember(uri) {
        val type = context.contentResolver.getType(uri)
        type?.startsWith("video") == true
    }
    val duration = if (isVideo) {
        getVideoDuration(context, uri)
    } else {
        null
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick = { onItemClick(uri) },
                onLongClick = { onItemLongClick(uri) }
            )
    ) {
        AsyncImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        if (isSelected) {
            // TODO: сделать красивое выделение с анимацией (нечто вроде того, как это сделано в PhotoPicker)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.TopEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
        if (isVideo && duration != null) {
            VideoDurationOverlay(
                duration = duration,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
            )
        }
    }
}

@Composable
private fun VideoDurationOverlay(
    duration: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = duration,
            color = Color.White,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun SmallTagsRow(
    tags: List<Tag>,
    showRemoveIcon: Boolean,
    emptyStateText: String,
    onTagClick: (Long) -> Unit
) {
    if (tags.isNotEmpty()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items = tags, key = { it.id }) { tag ->
                val baseColor = MaterialTheme.colorScheme.surface
                val tagColor = tag.color.colorLong?.let { Color(it) } ?: baseColor
                val blendedColor = lerp(baseColor, tagColor, 0.2f)

                FilterChip(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    selected = false,
                    onClick = { onTagClick(tag.id) },
                    trailingIcon = if (showRemoveIcon) {
                        {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null
                            )
                        }
                    } else {
                        null
                    },
                    label = {
                        Text(
                            text = tag.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = blendedColor
                    )
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(emptyStateText)
        }
    }
}
