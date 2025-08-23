package com.dgopadakak.tagsgallery.search.ui

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.dgopadakak.tagsgallery.core.compose.ui.FullTagsSelectionView
import com.dgopadakak.tagsgallery.core.local_storage.enums.Hints
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import com.dgopadakak.tagsgallery.search.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    fullscreenContentMutableState: MutableState<FullscreenContentModel?>,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scaffoldState = rememberBottomSheetScaffoldState(
        snackbarHostState = snackbarHostState
    )

    val sheetPeekHeight = 128.dp
    val halfScreenHeight = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height.toDp() / 2
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = sheetPeekHeight,
        sheetShape = RectangleShape,
        sheetContent = {
            val sheetState = scaffoldState.bottomSheetState
            val isExpanded = sheetState.targetValue == SheetValue.Expanded

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
                            onTagClick = { viewModel.onTagToggle(it) },
                            sortBy = uiState.sortBy,
                            onSortVariantChanged = { viewModel.setSortBy(it) },
                            filterBy = uiState.filterBy,
                            onFilterVariantChanged = { viewModel.setFilterBy(it) }
                        )
                    } else {
                        SmallTagsRow(
                            tags = uiState.allTags.filter { uiState.selectedTagIds.contains(it.id) }
                        ) {
                            viewModel.onTagToggle(it)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (uiState.foundedMediaUris.isEmpty()) {
            // TODO: показывать все доступные медиа
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No media found for the selected tags.")
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
                    var itemBounds: Rect? = null
                    SearchMediaPreviewItem(
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                itemBounds = coordinates.boundsInWindow()
                            },
                        uri = uri,
                        request = requestsList[index],
                        onItemClick = {
                            fullscreenContentMutableState.value = FullscreenContentModel(
                                startIndex = index,
                                placeholderImgRequests = requestsList,
                                startAnimationCoordinates = itemBounds!!
                            )
                        },
                        onItemLongClick = {
                            // TODO
                        }
                    )
                }
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
private fun SearchMediaPreviewItem(
    modifier: Modifier = Modifier,
    uri: Uri,
    request: ImageRequest,
    onItemClick: (Uri) -> Unit,
    onItemLongClick: (Uri) -> Unit
) {
    AsyncImage(
        model = request,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick = { onItemClick(uri) },
                onLongClick = { onItemLongClick(uri) }
            )
    )
}

@Composable
private fun SmallTagsRow(
    tags: List<Tag>,
    onTagClick: (Long) -> Unit
) {
    if (tags.isNotEmpty()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "Tap to remove tag, swipe up to add",
                fontSize = 12.sp
            )
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
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null
                            )
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
        }
    } else {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text("Swipe up here to choose tags for search")
        }
    }
}
