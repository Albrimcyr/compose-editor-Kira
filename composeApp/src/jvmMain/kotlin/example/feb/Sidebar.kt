package example.feb

import java.util.UUID;

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.material3.Divider
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Sidebar(
    chapters: List<Chapter>,
    modifier: Modifier = Modifier,
    selectedChapterID: UUID?,
    editingChapterID: UUID?,
    onAddChapter: () -> Unit,
    onSelectChapter: (UUID) -> Unit,
    onEditChapter: (UUID) -> Unit,
    onRenameCommit: () -> Unit,
    onDeleteChapter: (UUID) -> Unit,
    onEditDraftChange: (String) -> Unit,
    editingDraftTitle: String?,
) {

    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.primaryContainer),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row (
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        )

        {
            Text(
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(0.5f)
                    .padding(12.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                text = "Chapters")

            ExtendedFloatingActionButton(
                text = {
                    Text(
                        text = "Add Chapter",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add"
                    )
                },
                onClick = onAddChapter,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    hoveredElevation = 0.dp,
                    focusedElevation = 0.dp
                ),
                modifier = Modifier.padding(8.dp)
            )


        }


        Divider()

        LazyColumn (
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = chapters,
                key = {it.id}
            ) {
                    chapter ->

                ChapterItem(

                    title = chapter.title,
                    isSelected = chapter.id == selectedChapterID,
                    isEditing = chapter.id == editingChapterID,

                    onSelect = { onSelectChapter(chapter.id) },
                    onEdit = { onEditChapter(chapter.id) },

                    draftTitle = if (chapter.id == editingChapterID) editingDraftTitle else null,
                    onDraftChange = onEditDraftChange,

                    onRenameCommit = onRenameCommit,

                    onDelete = { onDeleteChapter(chapter.id) }
                )

                Divider()

            }

        }
    }

}