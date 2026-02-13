package example.feb

import androidx.compose.runtime.Composable

import androidx.compose.material.DropdownMenu
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DropdownMenuItem

import androidx.compose.material3.Icon
import androidx.compose.material3.Text

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.Color.Companion.Red
import org.jetbrains.skia.Color

@Composable
fun ChapterActionsMenu(
    expanded: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    ){

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ){
        DropdownMenuItem(
            text = { Text(text = "Edit") },
            leadingIcon = {Icon(Icons.Filled.Edit, contentDescription = "Edit")},
            onClick = {
                onEdit()
                onDismiss()
            }
        )

        DropdownMenuItem(
            text = { Text(text = "Delete", color = Red) },
            leadingIcon = {Icon(Icons.Filled.Delete, contentDescription = "Delete")},
            onClick = {
                onDelete()
                onDismiss()
            }
        )
    }


}
