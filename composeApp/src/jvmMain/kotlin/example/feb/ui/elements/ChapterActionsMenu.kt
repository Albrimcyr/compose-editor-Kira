package example.feb.ui.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable

import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DropdownMenuItem

import androidx.compose.material3.Icon
import androidx.compose.material3.Text

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.unit.dp
import example.feb.ui.AppColors
import example.feb.ui.AppShapes


@Composable
fun ChapterActionsMenu(
    expanded: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    colors: AppColors,
    ){

        Column(Modifier.padding(vertical = 0.dp)) {

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismiss,
                shape = AppShapes.rounded12,
                containerColor = colors.whiteColor,
            ){
                Surface (modifier = Modifier.padding(3.dp), shape = AppShapes.rounded12, color = colors.whiteColor) {
                    DropdownMenuItem(
                        text = { Text(text = "Edit") },
                        leadingIcon = {Icon(Icons.Filled.Edit, contentDescription = "Edit")},
                        onClick = {
                            onEdit()
                            onDismiss()
                        },
                    )
                }

                Surface (modifier = Modifier.padding(3.dp), shape = AppShapes.rounded12, color = colors.whiteColor) {
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
        }
}
