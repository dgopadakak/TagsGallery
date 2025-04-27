package com.dgopadakak.tagsgallery.tags.ui.body

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dgopadakak.tagsgallery.core.compose.ui.animatePlacement
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import com.dgopadakak.tagsgallery.tags.TagsViewModel
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TagsGrid(
    uiStateFlow: StateFlow<TagsViewModel.UiState>,
    onTagEdit: (Tag) -> Unit,
    onTagSelect: (Long) -> Unit
) {
    val uiState: TagsViewModel.UiState by uiStateFlow.collectAsState()
    val gridState = rememberLazyGridState()

    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp, start = 8.dp, end = 8.dp),
        state = gridState,
        columns = GridCells.Adaptive(160.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(uiState.tags, key = { it.id }) { tag ->
            TagCard(
                tag = tag,
                isSelectMode = uiState.selectedTagIds.isNotEmpty(),
                selected = uiState.selectedTagIds.contains(tag.id),
                onEdit = { onTagEdit(tag) },
                onSelect = { onTagSelect(tag.id) },
                isScrollInProgress = gridState.isScrollInProgress
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TagCard(
    tag: Tag,
    isSelectMode: Boolean,
    selected: Boolean,
    onEdit: () -> Unit,
    onSelect: () -> Unit,
    isScrollInProgress: Boolean
) {
    val baseColor = CardDefaults.cardColors().containerColor
    val tagColor = tag.color.colorLong?.let { Color(it) } ?: baseColor
    val blendedColor = lerp(baseColor, tagColor, 0.15f)

    val textStartPadding by animateDpAsState(
        targetValue = if (isSelectMode) 45.dp else 12.dp,
        animationSpec = tween(durationMillis = 200)
    )
    val textMaxWidth by animateDpAsState(
        targetValue = if (isSelectMode) 120.dp else 150.dp,
        animationSpec = tween(durationMillis = 200)
    )

    Card(
        modifier = Modifier
            .animatePlacement(enabled = !isScrollInProgress)
            .fillMaxWidth()
            .height(56.dp)
            .padding(4.dp)
            .combinedClickable(
                onClick = if (isSelectMode) onSelect else onEdit,
                onLongClick = onSelect
            ),
        colors = CardDefaults.cardColors(containerColor = blendedColor),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            // Полный путь к пакету из-за бага при обычном импорте: https://stackoverflow.com/questions/67975569/why-cant-i-use-animatedvisibility-in-a-boxscope
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelectMode,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(24.dp)
                        .background(
                            color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                modifier = Modifier
                    .padding(start = textStartPadding)
                    .width(textMaxWidth),
                text = tag.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
