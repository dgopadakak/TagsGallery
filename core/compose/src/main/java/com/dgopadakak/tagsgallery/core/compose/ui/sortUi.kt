package com.dgopadakak.tagsgallery.core.compose.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dgopadakak.tagsgallery.core.compose.enums.SortVariant

@Composable
fun SortVariantsRow(
    modifier: Modifier = Modifier,
    sortBy: SortVariant,
    onSortVariantChanged: (sortVariant: SortVariant) -> Unit
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Text(
                modifier = Modifier
                    .padding(start = 12.dp),
                text = "Sort tags by:"
            )
        }

        items(SortVariant.entries.toList()) { sortVariant ->
            FilterChip(
                modifier = Modifier
                    .padding(start = 20.dp),
                selected = sortBy == sortVariant,
                onClick = { onSortVariantChanged(sortVariant) },
                label = { Text(text = "$sortVariant") }
            )
        }
    }
}
