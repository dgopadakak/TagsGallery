package com.dgopadakak.tagsgallery.core.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag

@Composable
fun ColorPickerRow(
    modifier: Modifier = Modifier,
    selectedColor: Tag.Color,
    onSelectedColorChanged: (color: Tag.Color) -> Unit
) {
    LazyRow(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Text(
                modifier = Modifier
                    .padding(end = 10.dp),
                text = "Color:"
            )
        }
        items(Tag.Color.entries.toList()) { color->
            ColorChip(
                modifier = Modifier
                    .padding(horizontal = 2.dp),
                color = color,
                checked = selectedColor == color,
                onClick = { onSelectedColorChanged(color) }
            )
        }
    }
}

@Composable
private fun ColorChip(
    modifier: Modifier = Modifier,
    color: Tag.Color,
    checked: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment= Alignment.Center,
        modifier = modifier
            .size(30.dp)
            .border(
                width = 2.dp,
                color = if (checked)
                    if (color.colorLong != null)
                        lerp(Color.Gray, Color(color.colorLong!!), 0.6f)
                    else
                        LocalContentColor.current
                else
                    Color.Transparent,
                shape = CircleShape
            ),
    ) {
        if (color.colorLong == null) {
            Icon(
                modifier = Modifier
                    .size(22.dp)
                    .clickable(onClick = onClick),
                imageVector = Icons.Default.Close,
                contentDescription = null
            )
        } else {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(color = lerp(Color.Gray, Color(color.colorLong!!), 0.6f))
                    .clickable(onClick = onClick)
            )
        }
    }
}
