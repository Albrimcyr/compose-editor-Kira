package example.feb

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable

import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DropdownMenuItem

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme

import androidx.compose.ui.Modifier

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuDefaults.itemColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.unit.dp



@Composable
fun ChapterActionsMenu(
    expanded: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    ){

//    val DeleteHoverBg = Color(0x1AFF0000)
//    val DeletePressedBg = Color(0x33FF0000) Change back later?

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),

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
