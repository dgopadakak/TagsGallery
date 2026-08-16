package com.dgopadakak.tagsgallery.core.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun EditModeWarningIcon(
    modifier: Modifier = Modifier,
    iconSize: Dp = 14.dp
) {
    Box(
        modifier = modifier
            .background(
                color = Color(237,153,21),
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            tint = Color.Black,
            contentDescription = "Edit mode warning",
            modifier = Modifier
                .align(Alignment.Center)
                .padding(2.dp)
                .size(iconSize)
        )
    }
}

@Composable
fun RemoveAllTagsIcon(
    modifier: Modifier = Modifier,
    tagIconSize: Dp = 20.dp,
    badgeIconSize: Dp = 12.dp,
    tint: Color = Color.White
) {
    TagsIconWithBadge(
        badge = Icons.Default.Close,
        modifier = modifier,
        tagIconSize = tagIconSize,
        badgeIconSize = badgeIconSize,
        tint = tint
    )
}

@Composable
fun EditTagsIcon(
    modifier: Modifier = Modifier,
    tagIconSize: Dp = 20.dp,
    badgeIconSize: Dp = 12.dp,
    tint: Color = Color.White
) {
    TagsIconWithBadge(
        badge = Icons.Default.Edit,
        modifier = modifier,
        tagIconSize = tagIconSize,
        badgeIconSize = badgeIconSize,
        tint = tint
    )
}

/**
 * Общая основа иконок действий над списком тегов медиа: сами теги плюс бейдж с действием.
 * Геометрия одна на всех, чтобы кнопки в одном ряду не разъезжались
 */
@Composable
private fun TagsIconWithBadge(
    badge: ImageVector,
    modifier: Modifier,
    tagIconSize: Dp,
    badgeIconSize: Dp,
    tint: Color
) {
    Box(
        modifier = modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Bookmarks,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(tagIconSize)
        )
        Icon(
            imageVector = badge,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(badgeIconSize)
                .background(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = CircleShape
                )
        )
    }
}
