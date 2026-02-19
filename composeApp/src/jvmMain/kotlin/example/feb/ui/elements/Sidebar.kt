package example.feb.ui.elements

import java.util.UUID;

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import example.feb.presentation.ChapterRowUi
import example.feb.presentation.EditingState
import example.feb.ui.AppColors
import example.feb.ui.AppShapes

@Composable
fun Sidebar(
    chapters: List<ChapterRowUi>,
    modifier: Modifier = Modifier,
    selectedChapterID: UUID?,
    editingState: EditingState,
    onAddChapter: () -> Unit,
    onSelectChapter: (UUID) -> Unit,
    onEditChapter: (UUID) -> Unit,
    onRenameCommit: () -> Unit,
    onDeleteChapter: (UUID) -> Unit,
    onEditDraftChange: (String) -> Unit,
    colors: AppColors
) {

    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxHeight()
            .background(colors.sidebarColor)
            .padding(4.dp),

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
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = colors.activeTextColor,
                text = "Chapters")

            ExtendedFloatingActionButton(
                text = {
                    Text(
                        text = "Add",
                        color = colors.activeTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                shape = AppShapes.rounded12,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        tint = colors.activeTextColor
                    )
                },
                onClick = onAddChapter,
                containerColor = colors.selectionColor,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    hoveredElevation = 0.dp,
                    focusedElevation = 0.dp
                ),
                modifier = Modifier.padding(8.dp)
            )


        }


        HorizontalDivider(modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 4.dp),
            color = colors.selectionColor,
        )


        LazyColumn (
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(8.dp),
        ) {
            items(
                items = chapters,
                key = {it.id}
            ) { chapter ->

                val isEditing =
                    editingState is EditingState.Renaming &&
                            editingState.id == chapter.id

                val draft =
                    (editingState as? EditingState.Renaming)?.draft

                ChapterItem(

                    title = chapter.title,
                    isSelected = chapter.id == selectedChapterID,
                    isEditing = isEditing,

                    onSelect = { onSelectChapter(chapter.id) },
                    onEdit = { onEditChapter(chapter.id) },

                    draftTitle = if (isEditing) draft else null,
                    onDraftChange = onEditDraftChange,

                    onRenameCommit = onRenameCommit,

                    onDelete = { onDeleteChapter(chapter.id) },

                    colors = colors,
                )

            }

        }
    }

}