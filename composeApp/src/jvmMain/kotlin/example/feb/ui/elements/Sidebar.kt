package example.feb.ui.elements

import androidx.compose.foundation.BorderStroke
import java.util.UUID

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.onClick
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import example.feb.presentation.ChapterRowUi
import example.feb.presentation.EditingState
import example.feb.ui.AppColors
import example.feb.ui.AppShapes
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun Sidebar(
    chapters: List<ChapterRowUi>,
    modifier: Modifier = Modifier,
    selectedChapterID: UUID?,
    editingState: EditingState,
    onAddChapter: () -> Unit,
    onSelectChapter: (UUID) -> Unit,
    onEditChapter: (UUID) -> Unit,
    onRenameCommit: (UUID, String) -> Unit,
    onDeleteChapter: (UUID) -> Unit,
    colors: AppColors,

    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
) {

    var localSearch by remember { mutableStateOf (searchQuery) }

    LaunchedEffect(searchQuery) {
        if (searchQuery != localSearch) localSearch = searchQuery
    }

    LaunchedEffect(Unit) {
        snapshotFlow { localSearch }
            .debounce (200)
            .distinctUntilChanged()
            .collect { q -> onSearchQueryChange(q) }
    }


    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxHeight()
            .background(colors.sidebarColor)
            .padding(4.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(modifier = Modifier) {

            Row (
                verticalAlignment = Alignment.CenterVertically,
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

            Surface (Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .heightIn(min = 48.dp),
                shape = AppShapes.rounded12,
                color = colors.sidebarColor,
                border = BorderStroke(1.dp, colors.dividerColor))
            {

                BasicTextField(
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = colors.activeTextColor,
                        fontStyle = FontStyle.Italic,
                    ),
                    value = localSearch,
                    onValueChange = {localSearch = it},
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Row (modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {

                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Search",
                                    tint = colors.activeTextColor,
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(Modifier.width(10.dp))

                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart) {
                                    innerTextField()
                                }

                                if (localSearch.isNotEmpty()) {

                                    IconButton(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .pointerHoverIcon(PointerIcon.Default),
                                            onClick = {localSearch = ""},
                                        ){
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                "clear",
                                                tint = colors.activeTextColor,
                                            )
                                        }
                                }

                        }
                    },
                )

            }

        }


        HorizontalDivider(modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 4.dp),
            color = colors.dividerColor,
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

                ChapterItem(
                    id = chapter.id,
                    title = chapter.title,
                    isSelected = chapter.id == selectedChapterID,
                    isEditing = isEditing,
                    onSelect = { onSelectChapter(chapter.id) },
                    onEdit = { onEditChapter(chapter.id) },
                    onDelete = { onDeleteChapter(chapter.id) },
                    onRenameCommit = onRenameCommit,
                    colors = colors
                )

            }

        }
    }

}