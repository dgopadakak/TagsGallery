package com.dgopadakak.tagsgallery.gallery.ui.elements

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dgopadakak.tagsgallery.core.compose.R as CoreR

@Composable
internal fun DeleteMediaConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(CoreR.string.remove_media_dialog_title)) },
        text = { Text(stringResource(CoreR.string.remove_media_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(CoreR.string.action_remove))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(CoreR.string.action_cancel))
            }
        }
    )
}
