package com.dgopadakak.tagsgallery.gallery.ui.tags

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dgopadakak.tagsgallery.core.compose.enums.SortVariant
import com.dgopadakak.tagsgallery.core.compose.ui.EditModeWarningIcon
import com.dgopadakak.tagsgallery.core.compose.ui.FullTagsSelectionView
import com.dgopadakak.tagsgallery.core.compose.ui.SimpleTagsSelectionView
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import com.dgopadakak.tagsgallery.gallery.GalleryViewModel
import com.dgopadakak.tagsgallery.gallery.util.calculateFinalTagIds
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun TagsSegment(
    uiStateFlow: StateFlow<GalleryViewModel.UiState>,
    onCommonTagSelected: (Long) -> Unit,
    onSortVariantChanged: (SortVariant) -> Unit,
    onFilterVariantChanged: (Tag.Color?) -> Unit,
    onIndividualTagToggle: (Uri, Long) -> Unit,
    onIndividualTagAccept: () -> Unit,
) {

    val uiState by uiStateFlow.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        AnimatedContent(
            modifier = Modifier
                .fillMaxSize(),
            targetState = uiState.activeEditIndividualTags,
            contentKey = { it == null },
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
                Column {
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = "Tap on media to edit tags individually",
                        fontSize = 12.sp
                    )
                    if (uiState.alreadySavedMedia.isNotEmpty()) {
                        Row(
                            modifier = Modifier.padding(start = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EditModeWarningIcon(iconSize = 12.dp)
                            Text(
                                modifier = Modifier.padding(start = 4.dp),
                                text = "- already saved media, tags will be overwritten",
                                fontSize = 12.sp
                            )
                        }
                    }
                    FullTagsSelectionView(
                        modifier = Modifier.padding(start = 8.dp),
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
    Column {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable(onClick = onClickAccept)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                modifier = Modifier
                    .padding(start = 8.dp),
                text = "Save and return to common tags",
                color = MaterialTheme.colorScheme.primary,
                fontFamily = MaterialTheme.typography.labelLarge.fontFamily,
                fontWeight = MaterialTheme.typography.labelLarge.fontWeight,
                fontSize = MaterialTheme.typography.labelLarge.fontSize,
                letterSpacing = MaterialTheme.typography.labelLarge.letterSpacing
            )
        }
        SimpleTagsSelectionView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp),
            tags = tags,
            selectedTagsIds = calculateFinalTagIds(
                selectedCommonTagIds = selectedCommonTagIds,
                individualAddedTagIds = individualAddedTagIds,
                individualRemovedTagIds = individualRemovedTagIds
            ),
            onTagClick = onTagToggle
        )
    }
}
