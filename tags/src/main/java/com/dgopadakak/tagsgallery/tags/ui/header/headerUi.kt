package com.dgopadakak.tagsgallery.tags.ui.header

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.dgopadakak.tagsgallery.tags.R
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
                MainHeaderRow()
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
private fun MainHeaderRow() {
    val context = LocalContext.current
    val githubUrl = "https://github.com/dgopadakak/TagsGallery"

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Tags",
            fontSize = 22.sp
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    context.startActivity(Intent(Intent.ACTION_VIEW, githubUrl.toUri()))
                }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_github),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                modifier = Modifier.padding(start = 6.dp),
                text = "GitHub",
                fontSize = 14.sp,
                textDecoration = TextDecoration.Underline
            )
            Icon(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(14.dp),
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "View on GitHub"
            )
        }
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
