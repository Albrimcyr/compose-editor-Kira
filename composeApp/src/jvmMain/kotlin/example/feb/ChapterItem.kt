package example.feb

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ChapterItem(
    title: String,
    isSelected: Boolean,
    isEditing: Boolean,

    draftTitle: String?,
    onDraftChange: (String) -> Unit,

    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onRenameCommit: () -> Unit
) {

    var menuExpanded by remember { mutableStateOf(false) }
    val selectedColor = Color(0xFF9BDBD4)
    val roundedShape = RoundedCornerShape(12.dp)

    // Surface -> Row
    // [ text / text-field - button in the box  - button in the box ]

    Surface(
        shape = roundedShape,
        color = if (isSelected) selectedColor else MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = if (isSelected) 3.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(roundedShape)
            .clickable(enabled = !isEditing) { onSelect() },
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(
                    if (isSelected)
                        selectedColor
                    else
                        MaterialTheme.colorScheme.secondaryContainer
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {


            if (isEditing) {
                OutlinedTextField(
                    value = draftTitle ?: "",
                    onValueChange = onDraftChange,
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .onPreviewKeyEvent() {
                            if (it.key == Key.Enter) {
                                onRenameCommit()
                                true
                            } else false
                        }
                )
            } else {
                Text(
                    text = title,
                    maxLines = 1,
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
                        shape = roundedShape,
                        onClick = {menuExpanded = true},
                    ){
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            "more"
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
