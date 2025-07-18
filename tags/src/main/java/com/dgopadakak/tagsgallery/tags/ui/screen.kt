package com.dgopadakak.tagsgallery.tags.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dgopadakak.tagsgallery.core.compose.ui.ColorFilterRow
import com.dgopadakak.tagsgallery.core.compose.ui.ColorPickerRow
import com.dgopadakak.tagsgallery.core.compose.ui.SortVariantsRow
import com.dgopadakak.tagsgallery.core.local_storage.enums.Hints
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import com.dgopadakak.tagsgallery.tags.TagsViewModel
import com.dgopadakak.tagsgallery.tags.ui.body.TagsGrid
import com.dgopadakak.tagsgallery.tags.ui.header.HeaderRow
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TagsScreen(viewModel: TagsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var tagToEdit by remember { mutableStateOf<Tag?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    if (showEditDialog) {
        TagDialog(
            tag = tagToEdit,
            onDismiss = { showEditDialog = false; tagToEdit = null },
            onSave = { name, color ->
                if (tagToEdit == null) {
                    viewModel.saveNewTag(name, color)
                } else {
                    viewModel.updateTag(tagToEdit!!.copy(name = name, color = color))
                }
                showEditDialog = false
                tagToEdit = null
            }
        )
    }

    if (showDeleteDialog) {
        DeleteDialog(
            numOfTags = uiState.selectedTagIds.size,
            onAccept = { viewModel.deleteSelectedTags(); showDeleteDialog = false },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // Padding values внешнего Scaffold уже учтены в NavHost, внутренний не должен вносить изменений,
    //  так как точно не предназначен для отображения элементов, ограничивающих доступную область
    //  (таких как панель навигации).
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showEditDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Tag")
            }
        },
        // Необходимый хак, чтобы FAB знала высоту контента под ней и не улетала в космос
        bottomBar = { Spacer(Modifier.height(0.dp)) },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) {
        Column {
            HeaderRow(
                uiStateFlow = viewModel.uiState,
                onResetSelection = { viewModel.onResetSelection() },
                onAcceptDeletion = { showDeleteDialog = true }
            )

            SortVariantsRow(
                modifier = Modifier.padding(start = 12.dp),
                sortBy = uiState.sortBy,
                onSortVariantChanged = { viewModel.setSortBy(it) }
            )

            ColorFilterRow(
                modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
                filterBy = uiState.filterBy,
                onFilterVariantChanged = { viewModel.setFilterBy(it) }
            )

            TagsGrid(
                uiStateFlow = viewModel.uiState,
                onTagEdit = { tag ->
                    tagToEdit = tag
                    showEditDialog = true
                },
                onTagSelect = { id ->
                    viewModel.onTagSelect(id)
                }
            )
        }
    }

    LaunchedEffect(uiState.needToShowHint) {
        if (uiState.needToShowHint) {
            snackbarHostState.showSnackbar(Hints.TAGS_MAIN_HINT.text)
            viewModel.setHintShown()
        }
    }
}

@Composable
private fun TagDialog(
    tag: Tag?,
    onDismiss: () -> Unit,
    onSave: (String, Tag.Color) -> Unit
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
                if (tag != null) {
                    val date = Date(tag.lastModified)
                    val formatter = DateFormat.getDateTimeInstance(
                        DateFormat.DEFAULT, // Уровень детализации (можно SHORT, MEDIUM, LONG, FULL)
                        DateFormat.DEFAULT,
                        Locale.getDefault()
                    )
                    Text(
                        modifier = Modifier
                            .padding(top = 8.dp),
                        text = "Last update: ${formatter.format(date)}"
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onSave(name, color) }
            ) {
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

@Composable
private fun DeleteDialog(
    numOfTags: Int,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Tags?") },
        text = { Text("Selected tags ($numOfTags) will be deleted") },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
