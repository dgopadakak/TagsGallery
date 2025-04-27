package com.dgopadakak.tagsgallery.tags.ui.header

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dgopadakak.tagsgallery.tags.TagsViewModel
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HeaderRow(
    uiStateFlow: StateFlow<TagsViewModel.UiState>,
    onResetSelection: () -> Unit,
    onAcceptDeletion: () -> Unit
) {
    val uiState by uiStateFlow.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clipToBounds()
    ) {
        AnimatedContent(
            targetState = uiState.selectedTagIds.isEmpty(),
            transitionSpec = {
                if (targetState) {
                    (slideInVertically { it }).togetherWith(slideOutVertically { -it })
                } else {
                    (slideInVertically { -it }).togetherWith(slideOutVertically { it })
                }.using(SizeTransform(clip = false))
            }
        ) { notSelectionMode ->
            if (notSelectionMode) {
                LimitsAndAdHeaderRow()
            } else {
                TagSelectionModeHeaderRow(
                    numOfSelectedTags = uiState.selectedTagIds.size,
                    onResetSelection = onResetSelection,
                    onAcceptDeletion = onAcceptDeletion
                )
            }
        }
    }
}

@Composable
fun LimitsAndAdHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 8.dp),
            text = "TODO: Limits And Ad"
        )
    }
}

@Composable
fun TagSelectionModeHeaderRow(
    numOfSelectedTags: Int,
    onResetSelection: () -> Unit,
    onAcceptDeletion: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onResetSelection
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Reset selected tags"
                )
            }
            Text(
                modifier = Modifier
                    .padding(start = 8.dp),
                text = "$numOfSelectedTags",
                fontSize = 22.sp
            )
        }
        IconButton(
            onClick = onAcceptDeletion
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Accept deletion"
            )
        }
    }
}
