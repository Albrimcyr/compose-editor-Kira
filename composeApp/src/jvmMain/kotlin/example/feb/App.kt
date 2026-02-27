package example.feb

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import example.feb.presentation.AppViewModel
import java.awt.Cursor

import androidx.compose.runtime.Composable

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.input.key.onKeyEvent

import example.feb.ui.colorsFor
import example.feb.ui.elements.MainContent
import example.feb.ui.elements.Sidebar

@Composable
fun App(viewModel: AppViewModel) {

    MaterialTheme {

        val uiState by viewModel.uiState.collectAsState() // subscribed

        val colors = colorsFor(uiState.isDarkTheme)

        var showError by remember(uiState.errorMessage) { mutableStateOf(uiState.errorMessage != null) }

        var split by remember { mutableStateOf(0.40f) }
        val minSplit = 0.10f
        val maxSplit = 0.50f

        BoxWithConstraints{
            val totalWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)

            Row(modifier = Modifier
                .fillMaxSize()
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when (event.key) {
                            Key.Escape -> {viewModel.onEsc(); true}
                            Key.Delete -> {viewModel.onDel(); true}
                            else -> false
                        }
                    } else false
                }
            ) {

                // ── LEFT PART ────────────────────────────────────────────────────────────────────────────────────────

                if (showError) {
                    AlertDialog(
                        onDismissRequest = { showError = false },
                        title = { Text("Error!!!") },
                        text = { Text(uiState.errorMessage ?: "") },
                        confirmButton = {
                            Button(onClick = { showError = false }) {
                                Text("OK")
                            }
                        }
                    )
                }

                Sidebar(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(this@BoxWithConstraints.maxWidth * split),

                    colors = colors,

                    chapters = uiState.chapterRows,
                    selectedChapterID = uiState.selectedId,
                    editingState = uiState.editingState,

                    onAddChapter = viewModel::onAddChapter,
                    onSelectChapter = viewModel::onSelectChapter,
                    onEditChapter = viewModel::onStartRenaming,
                    onRenameCommit = viewModel::onRenameCommit,
                    onDeleteChapter = viewModel::onDeleteChapter
                )


                // ── DIVIDER ──────────────────────────────────────────────────────────────────────────────────────────


                val dragState = rememberDraggableState { delta ->
                    val deltaFraction = delta / totalWidthPx
                    split = (split + deltaFraction).coerceIn(minSplit, maxSplit)
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(3.dp)
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = dragState,
                        )
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)))
                        .background(colors.mainDividerColor)
                )


                // ── RIGHT PART ───────────────────────────────────────────────────────────────────────────────────────

                Box(
                    modifier = Modifier.fillMaxHeight().weight(1f)
                ) {

                    MainContent(
                        selectedId = uiState.selectedId,
                        title = uiState.selectedTitle,
                        content = uiState.selectedContent,
                        onContentChange = viewModel::onContentChange,

                        colors = colors,
                        isDarkTheme = uiState.isDarkTheme,
                        onToggleTheme = viewModel::onToggleTheme
                    )
                }
            }

        }
    }
}
