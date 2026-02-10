package example.feb

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp


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

    // row
    // [ text / text-field - button in the box  - button in the box ]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(
                if (isSelected)
                    MaterialTheme.colorScheme.outline
                else
                    MaterialTheme.colorScheme.secondaryContainer
            )
            // to avoid space exiting the text-field
            .clickable(enabled = !isEditing) { onSelect() },
        verticalAlignment = Alignment.CenterVertically
    ) {



        if (isEditing) {
            TextField(
                value = draftTitle ?: "",
                onValueChange = onDraftChange,
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
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

            Row {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .clickable { onEdit() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("EDIT")
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("DEL")
                }
            }
        }


    }
}
