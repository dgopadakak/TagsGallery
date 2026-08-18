package com.dgopadakak.tagsgallery.gallery.ui.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag

@Composable
internal fun SmallTagsRow(
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
            Text(
                modifier = Modifier.fillMaxWidth(0.85f),
                text = emptyStateText,
                textAlign = TextAlign.Center
            )
        }
    }
}
