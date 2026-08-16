package com.dgopadakak.tagsgallery.ui

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dgopadakak.tagsgallery.core.compose.ui.SimpleTagsSelectionView
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag

/**
 * Правка списка тегов медиа, открытого в просмотрщике. Выбор копится локально в состоянии
 * редактора и уходит в БД только по Save
 */
@Composable
internal fun EditTagsDialog(
    tags: List<Tag>,
    selectedTagIds: Set<Long>,
    onTagClick: (Long) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit tags") },
        text = {
            SimpleTagsSelectionView(
                // Высота ограничена, чтобы длинный список тегов скроллился внутри диалога,
                // а не растягивал его на весь экран поверх медиа
                modifier = Modifier.heightIn(max = 320.dp),
                tags = tags,
                selectedTagsIds = remember(selectedTagIds) { selectedTagIds.toList() },
                onTagClick = onTagClick
            )
        },
        confirmButton = {
            TextButton(onClick = onSave) {
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
