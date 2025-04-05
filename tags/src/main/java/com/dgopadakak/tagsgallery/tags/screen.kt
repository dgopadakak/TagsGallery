package com.dgopadakak.tagsgallery.tags

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dgopadakak.tagsgallery.core.compose.ui.ColorFilterRow
import com.dgopadakak.tagsgallery.core.compose.ui.ColorPickerRow
import com.dgopadakak.tagsgallery.core.compose.ui.SortVariantsRow
import com.dgopadakak.tagsgallery.core.compose.ui.animatePlacement
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag

@Composable
fun TagsScreen(viewModel: TagsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

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
            sortBy = uiState.sortBy
        ) {
            viewModel.setSortBy(it)
        }

        ColorFilterRow(
            modifier = Modifier
                .padding(vertical = 4.dp),
            filterBy = uiState.filterBy
        ) {
            viewModel.setFilterBy(it)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val gridState = rememberLazyGridState()

            LazyVerticalGrid(
                modifier = Modifier.padding(8.dp),
                state = gridState,
                columns = GridCells.Adaptive(160.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.tags, key = { it.id }) { tag ->
                    TagCard(
                        tag = tag,
                        onEdit = {
                            tagToEdit = tag
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteTag(tag) },
                        isScrollInProgress = gridState.isScrollInProgress
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
    onDelete: () -> Unit,
    isScrollInProgress: Boolean
) {
    val baseColor = CardDefaults.cardColors().containerColor
    val tagColor = tag.color.colorLong?.let { Color(it) } ?: baseColor
    val blendedColor = lerp(baseColor, tagColor, 0.15f)

    Card(
        modifier = Modifier
            .animatePlacement(enabled = !isScrollInProgress)
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

    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (tag == null) "Add Tag" else "Edit Tag") },
        text = {
            Column {
                OutlinedTextField(
                    modifier = Modifier
                        .focusRequester(focusRequester),
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

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
