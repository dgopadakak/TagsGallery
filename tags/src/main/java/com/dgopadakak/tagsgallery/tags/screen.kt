package com.dgopadakak.tagsgallery.tags

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dgopadakak.tagsgallery.core.local_storage.room.models.Tag

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsScreen(viewModel: TagsViewModel = hiltViewModel()) {
    val tags by viewModel.tags.collectAsState(initial = emptyList())

    var showDialog by remember { mutableStateOf(false) }
    var tagToEdit by remember { mutableStateOf<Tag?>(null) }

    if (showDialog) {
        TagDialog(
            tag = tagToEdit,
            onDismiss = { showDialog = false; tagToEdit = null },
            onSave = { name ->
                viewModel.saveTag(name)
                showDialog = false
                tagToEdit = null
            }
        )
    }

    Box {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                FlowRow(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tags.forEach { tag ->
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

@Composable
fun TagCard(tag: Tag, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tag.name,
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Tag")
            }
        }
    }
}

@Composable
fun TagDialog(tag: Tag?, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(tag?.name ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (tag == null) "Add Tag" else "Edit Tag") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tag Name") }
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onSave(name) }) {
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
