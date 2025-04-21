package com.dgopadakak.tagsgallery.gallery.ui.tags

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import com.dgopadakak.tagsgallery.core.compose.enums.SortVariant
import com.dgopadakak.tagsgallery.core.compose.ui.FullTagsSelectionView
import com.dgopadakak.tagsgallery.core.compose.ui.SimpleTagsSelectionView
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import com.dgopadakak.tagsgallery.gallery.GalleryViewModel
import com.dgopadakak.tagsgallery.gallery.util.calculateFinalTagIds
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun TagsSegment(
    modifier: Modifier = Modifier,
    uiStateFlow: StateFlow<GalleryViewModel.GalleryUiState>,
    onCommonTagSelected: (Long) -> Unit,
    onSortVariantChanged: (SortVariant) -> Unit,
    onFilterVariantChanged: (Tag.Color?) -> Unit,
    onIndividualTagToggle: (Uri, Long) -> Unit,
    onIndividualTagAccept: () -> Unit,
) {
    val uiState by uiStateFlow.collectAsState()

    val activeMediaUri = uiState.activeEditIndividualTags
    AnimatedContent(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds(),
        targetState = activeMediaUri,
        transitionSpec = {
            if (targetState != null) {
                (slideInVertically { -it } + fadeIn()).togetherWith(slideOutVertically { it } + fadeOut())
            } else {
                (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
            }.using(SizeTransform(clip = false))
        }
    ) { uri ->
        if (uri != null) {
            IndividualTagsSelector(
                tags = uiState.tags.sortedBy { it.name },
                selectedCommonTagIds = uiState.selectedTagIds,
                individualAddedTagIds = uiState.perMediaAddedTagIds.getOrDefault(uri, emptyList()),
                individualRemovedTagIds = uiState.perMediaRemovedTagIds.getOrDefault(uri, emptyList()),
                onTagToggle = { tagId -> onIndividualTagToggle(uri, tagId) },
                onClickAccept = onIndividualTagAccept
            )
        } else {
            FullTagsSelectionView(
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
    }
}

@Composable
private fun IndividualTagsSelector(
    tags: List<Tag>,
    selectedCommonTagIds: List<Long>,
    individualAddedTagIds: List<Long>,
    individualRemovedTagIds: List<Long>,
    onTagToggle: (Long) -> Unit,
    onClickAccept: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
    ) {
        Text(text = "Edit tags list individual for selected media")
        SimpleTagsSelectionView(
            modifier = Modifier
                .fillMaxWidth(),
            tags = tags,
            selectedTagsIds = calculateFinalTagIds(
                selectedCommonTagIds = selectedCommonTagIds,
                individualAddedTagIds = individualAddedTagIds,
                individualRemovedTagIds = individualRemovedTagIds
            ),
            onTagClick = onTagToggle
        )
        Button(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            onClick = onClickAccept
        ) {
            Text(text = "Accept")
        }
    }
}
