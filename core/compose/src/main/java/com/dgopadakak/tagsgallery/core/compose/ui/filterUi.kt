package com.dgopadakak.tagsgallery.core.compose.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag

@Composable
fun ColorFilterRow(
    modifier: Modifier = Modifier,
    filterBy: Tag.Color?,
    onFilterVariantChanged: (filterVariant: Tag.Color?) -> Unit
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth(),
         verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Text(
                modifier = Modifier
                    .padding(end = 4.dp),
                text = "Filter tags by:"
            )
        }

        items(Tag.Color.entries.toList()) { color ->
            ColorChip(
                modifier = Modifier
                    .padding(horizontal = 4.dp),
                color = color,
                checked = filterBy == color,
                onClick = {
                    if (filterBy != color) {
                        onFilterVariantChanged(color)
                    } else {
                        onFilterVariantChanged(null)
                    }
                }
            )
        }
    }
}
