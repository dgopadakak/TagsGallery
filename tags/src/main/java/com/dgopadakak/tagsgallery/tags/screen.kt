package com.dgopadakak.tagsgallery.tags

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.hilt.navigation.compose.hiltViewModel
import com.dgopadakak.tagsgallery.core.compose.ui.SortVariantsRow
import com.dgopadakak.tagsgallery.core.local_storage.room.models.Tag
import kotlinx.coroutines.launch

@Composable
fun TagsScreen(viewModel: TagsViewModel = hiltViewModel()) {
    val tags by viewModel.tags.collectAsState(initial = emptyList())
    val sortBy by viewModel.sortBy.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var tagToEdit by remember { mutableStateOf<Tag?>(null) }

    if (showDialog) {
        TagDialog(
            tag = tagToEdit,
            onDismiss = { showDialog = false; tagToEdit = null },
            onSave = { id, name, color ->
                viewModel.saveTag(id, name, color)
                showDialog = false
                tagToEdit = null
            }
        )
    }

    Column {
        SortVariantsRow(
            sortBy = sortBy
        ) {
            viewModel.setSortBy(it)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tags, key = { it.id }) { tag ->
                    TagCard(
                        tag = tag,
                        onEdit = {
                            tagToEdit = tag
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteTag(tag) }
                    )
                }
            }

            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onClick = { showDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Tag")
            }
        }
    }
}

@Composable
fun TagCard(
    tag: Tag,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val baseColor = MaterialTheme.colorScheme.surfaceContainerLow
    val tagColor = tag.color.colorLong?.let { Color(it) } ?: baseColor
    val blendedColor = lerp(baseColor, tagColor, 0.15f) // 15% смешения

    Card(
        modifier = Modifier
            .animatePlacement()
            .fillMaxSize()
            .padding(4.dp)
            .clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(containerColor = blendedColor),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box {
                Text(
                    modifier = Modifier.width(130.dp),
                    text = tag.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Tag")
            }
        }
    }
}

@Composable
fun TagDialog(
    tag: Tag?,
    onDismiss: () -> Unit,
    onSave: (Long?, String, Tag.Color) -> Unit
) {
    var name by remember { mutableStateOf(tag?.name ?: "") }
    var color by remember { mutableStateOf(tag?.color ?: Tag.Color.NO_COLOR) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (tag == null) "Add Tag" else "Edit Tag") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tag Name") }
                )
                ColorPickerRow(
                    modifier = Modifier
                        .padding( top = 16.dp),
                    selectedColor = color
                ) { color = it }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onSave(tag?.id, name, color) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ColorPickerRow(
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
                        Color(color.colorLong!!)
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
                    .background(color = Color(color.colorLong!!))
                    .clickable(onClick = onClick)
            )
        }
    }
}

fun Modifier.animatePlacement(): Modifier = composed {  // TODO: разобрать
    val scope = rememberCoroutineScope()
    var targetOffset by remember { mutableStateOf(IntOffset.Zero) }
    var animatable by remember {
        mutableStateOf<Animatable<IntOffset, AnimationVector2D>?>(null)
    }
    this
        .onPlaced {
            // Calculate the position in the parent layout
            targetOffset = it.positionInParent().round()
        }
        .offset {
            // Animate to the new target offset when alignment changes.
            val anim =
                animatable
                    ?: Animatable(targetOffset, IntOffset.VectorConverter).also {
                        animatable = it
                    }
            if (anim.targetValue != targetOffset) {
                scope.launch {
                    anim.animateTo(targetOffset, spring(stiffness = StiffnessMediumLow))
                }
            }
            // Offset the child in the opposite direction to the targetOffset, and slowly catch
            // up to zero offset via an animation to achieve an overall animated movement.
            animatable?.let { it.value - targetOffset } ?: IntOffset.Zero
        }
}
