package example.feb.ui.elements

import androidx.compose.runtime.Composable

import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DropdownMenuItem

import androidx.compose.material3.Icon
import androidx.compose.material3.Text

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Color.Companion.Red
import example.feb.ui.AppShapes


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
        shape = AppShapes.rounded12,

    ){
        DropdownMenuItem(
            text = { Text(text = "Edit") },
            leadingIcon = {Icon(Icons.Filled.Edit, contentDescription = "Edit")},
            onClick = {
                onEdit()
                onDismiss()
            }
        )
        HorizontalDivider()

        DropdownMenuItem(
            text = { Text(text = "Delete", color = Red) },
            leadingIcon = {Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Red)},
            onClick = {
                onDelete()
                onDismiss()
                },

            )


    }

}
