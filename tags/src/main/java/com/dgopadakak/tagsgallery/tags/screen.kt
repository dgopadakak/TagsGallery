package com.dgopadakak.tagsgallery.tags

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
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

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn {
            items(tags) { tag ->
                TagItem(
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

@Composable
fun TagItem(tag: Tag, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(tag.name, style = MaterialTheme.typography.bodyLarge)
        Row {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
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
