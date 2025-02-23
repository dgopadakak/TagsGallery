package com.dgopadakak.tagsgallery.core.compose.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsFlowRow(
    modifier: Modifier = Modifier,
    tags: List<Tag>,
    selectedTagsIds: List<Long>,
    onTagClick: (Long) -> Unit
) {
    FlowRow(
        modifier = modifier
    ) {
        tags.forEach { tag ->
            FilterChip(
                modifier = Modifier.padding(2.dp),
                selected = selectedTagsIds.contains(tag.id),
                onClick = { onTagClick(tag.id) },
                label = { Text(tag.name) }
            )
        }
    }
}
