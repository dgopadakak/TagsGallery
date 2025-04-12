package com.dgopadakak.tagsgallery.gallery.ui.tags

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.dgopadakak.tagsgallery.core.compose.enums.SortVariant
import com.dgopadakak.tagsgallery.core.compose.ui.TagsSelectionView
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import com.dgopadakak.tagsgallery.gallery.GalleryViewModel
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun TagsSegment(
    modifier: Modifier = Modifier,
    galleryTagsUiStateFlow: StateFlow<GalleryViewModel.GalleryTagsUiState>,
    onCommonTagSelected: (Long) -> Unit,
    onSortVariantChanged: (SortVariant) -> Unit,
    onFilterVariantChanged: (Tag.Color?) -> Unit,
    onIndividualTagToggle: (Uri, Long) -> Unit,
    onIndividualTagAccept: () -> Unit,
) {
    val uiState by galleryTagsUiStateFlow.collectAsState()

    val activeMediaUri = uiState.activeEditIndividualTags
    if (activeMediaUri != null) {
        IndividualTagsSelector(
            selectedCommonTagIds = uiState.selectedTagIds,
            individualAddedTagIds = uiState.perMediaAddedTagIds.getOrDefault(activeMediaUri, emptyList()),
            individualRemovedTagIds = uiState.perMediaRemovedTagIds.getOrDefault(activeMediaUri, emptyList()),
            onTagToggle = { tagId -> onIndividualTagToggle(activeMediaUri, tagId) },
            onOkClick = { onIndividualTagAccept }
        )
    }

    TagsSelectionView(
        modifier = modifier,
        tags = uiState.tags,
        selectedTagsIds = uiState.selectedTagIds,
        onTagClick = onCommonTagSelected,
        sortBy = uiState.sortBy,
        onSortVariantChanged = onSortVariantChanged,
        filterBy = uiState.filterBy,
        onFilterVariantChanged = onFilterVariantChanged
    )
}

@Composable
private fun IndividualTagsSelector(
    selectedCommonTagIds: List<Long>,
    individualAddedTagIds: List<Long>,
    individualRemovedTagIds: List<Long>,
    onTagToggle: (Long) -> Unit,
    onOkClick: () -> Unit
) {

}
