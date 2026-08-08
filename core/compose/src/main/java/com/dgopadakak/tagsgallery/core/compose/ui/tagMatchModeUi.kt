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
import com.dgopadakak.tagsgallery.core.local_storage.enums.TagMatchMode

@Composable
fun TagMatchModeRow(
    modifier: Modifier = Modifier,
    matchMode: TagMatchMode,
    onMatchModeChanged: (matchMode: TagMatchMode) -> Unit
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Text(
                text = "Search mode:"
            )
        }

        items(TagMatchMode.entries.toList()) { mode ->
            FilterChip(
                modifier = Modifier
                    .padding(start = 20.dp),
                selected = matchMode == mode,
                onClick = { onMatchModeChanged(mode) },
                label = { Text("$mode") }
            )
        }
    }
}
