package example.feb.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import example.feb.ui.AppColors
import example.feb.ui.AppShapes
import java.util.UUID


@Composable
fun ChapterItem(
    id: UUID,
    title: String,
    isSelected: Boolean,
    isEditing: Boolean,

    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onRenameCommit: (UUID, String) -> Unit,

    colors: AppColors
) {

    var menuExpanded by remember { mutableStateOf(false) }

    // Surface -> Row
    // [ text / text-field - button in the box  - button in the box ]

    var localText by remember(isEditing, id) { mutableStateOf(title) }
    LaunchedEffect(isEditing, title, id) {
        if (isEditing) localText = title
    }

    Surface(
        shape = AppShapes.rounded12,
        color = if (isSelected) colors.selectionColor else colors.sidebarColor,
        tonalElevation = if (isSelected) 3.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.rounded12)
            .clickable(enabled = !isEditing) { onSelect() },
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(
                    if (isSelected)
                        colors.selectionColor
                    else
                        colors.sidebarColor
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {


            if (isEditing) {
                OutlinedTextField(
                    value = localText,
                    onValueChange = { localText = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.activeTextColor,
                        unfocusedTextColor = colors.grayedTextColor
                    ),
                    shape = AppShapes.rounded12,
                    modifier = Modifier
                        .weight(1f)
                        .onPreviewKeyEvent() {
                            if (it.key == Key.Enter) {
                                onRenameCommit(id, localText)
                                true
                            } else false
                        }
                )
            } else {
                Text(
                    text = title,
                    maxLines = 1,
                    color = if (isSelected) colors.activeTextColor else colors.grayedTextColor,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                )
            }

            if (isSelected && !isEditing) {
                Box(
                    modifier = Modifier,
                ){
                    IconButton(
                        shape = AppShapes.rounded12,
                        onClick = {menuExpanded = true},
                    ){
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            "more",
                            tint = colors.activeTextColor,
                        )
                    }

                    ChapterActionsMenu(
                        expanded = menuExpanded,
                        onDismiss = { menuExpanded = false },
                        onEdit = onEdit,
                        onDelete = onDelete,
                    )

                }
            }

    }



    }
}
