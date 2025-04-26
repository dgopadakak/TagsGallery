package com.dgopadakak.tagsgallery.tags.ui.header

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import com.dgopadakak.tagsgallery.tags.TagsViewModel
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HeaderRow(
    uiStateFlow: StateFlow<TagsViewModel.UiState>
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
                TagSelectionModeHeaderRow()
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
fun TagSelectionModeHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 8.dp),
            text = "TODO: Selection Management"
        )
    }
}
