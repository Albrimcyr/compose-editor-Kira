package example.feb

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import java.util.UUID

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.unit.dp
import java.awt.Cursor
import androidx.compose.ui.input.pointer.pointerHoverIcon

//  - - - - -
//  model
//  - - - - -


data class Chapter(
    val id: UUID,
    val title: String,
    val content: String
)


//  - - - - -
//  editing state
//  - - - - -


sealed class EditingState {
    object None : EditingState()
    data class Editing(val id: UUID, val draft: String) : EditingState()
}



//  - - - - -
//  basic ViewModel
//  UI State + logic
//  - - - - -

class AppViewModel {

    // state
    var chapters by mutableStateOf(listOf<Chapter>())
        private set

    var selectedChapterID by mutableStateOf<UUID?>(null)
        private set

    var editingState by mutableStateOf<EditingState>(EditingState.None)
        private set

    // actions
    fun addChapter(){
        val newChapter = Chapter(
            id = UUID.randomUUID(),
            title = "New chapter",
            content = ""
        )
        chapters = chapters + newChapter
        selectedChapterID = newChapter.id
        editingState = EditingState.None
    }

    fun selectChapter(id: UUID) {
        if (chapters.none { it.id == id }) {return}
        selectedChapterID = id
        editingState = EditingState.None
    }

    fun startEditing(id: UUID) {
        val chapter = chapters.firstOrNull { it.id == id } ?: return
        editingState = EditingState.Editing(id, chapter.title)
    }

    fun changeDraft(text: String) {
        val current = editingState
        if (current is EditingState.Editing) {
            editingState = current.copy(draft = text)
        }
    }

    fun commitRename() {
        val current = editingState

        if (current is EditingState.Editing) {

            chapters = chapters.map { chapter ->
                if (chapter.id == current.id) chapter.copy(title = current.draft) else chapter
            }

        }


        editingState = EditingState.None
    }

    fun deleteChapter(id: UUID) {
        chapters = chapters.filter { it.id != id }

        if (selectedChapterID == id) {
            selectedChapterID = null
        }

        val current = editingState
        if (current is EditingState.Editing && current.id == id) {
            editingState = EditingState.None
        }
    }

    fun selectedChapter(): Chapter? =
        chapters.firstOrNull { it.id == selectedChapterID }

    fun cleanSelection() {
        editingState = EditingState.None
    }

    fun changeChapterContent(id: UUID, text: String) {
        if (chapters.none { it.id == id }) return

        chapters = chapters.map {chapter ->
            if (chapter.id == id) chapter.copy(content = text) else chapter
        }
    }

}

// UI layer
@Composable
fun App() {
    MaterialTheme {

        val viewModel = remember {AppViewModel()}

        var split by remember { mutableStateOf(0.20f) }
        val minSplit = 0.10f
        val maxSplit = 0.50f

        BoxWithConstraints(){

            val totalWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)

            Row(modifier = Modifier
                .fillMaxSize()
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when {

                            event.key == Key.Escape -> {
                                viewModel.cleanSelection()
                                true
                            }

                            event.key == Key.Delete && viewModel.selectedChapterID != null -> {
                                viewModel.selectedChapterID?.let { viewModel.deleteChapter(it) }
                                true
                            }

                            else -> false
                        }
                    } else false
                }
            ) {

                // // // // //  //
                // LEFT PART    //
                // // // // //  //

                Sidebar(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(this@BoxWithConstraints.maxWidth * split),
                    chapters = viewModel.chapters,
                    selectedChapterID = viewModel.selectedChapterID,
                    editingState = viewModel.editingState,

                    onAddChapter = viewModel::addChapter,
                    onSelectChapter = viewModel::selectChapter,
                    onEditChapter = viewModel::startEditing,
                    onEditDraftChange = viewModel::changeDraft,
                    onRenameCommit = viewModel::commitRename,
                    onDeleteChapter = viewModel::deleteChapter
                )


                // // // // //  //
                // DIVIDER      //
                // // // // //  //


                val dragState = rememberDraggableState { delta ->
                    val deltaFraction = delta / totalWidthPx
                    split = (split + deltaFraction).coerceIn(minSplit, maxSplit)
                }

                val divColor = Color(0x61666666)

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = dragState,
                        )
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)))
                        .background(divColor)
                )


                // // // // //  //
                // RIGHT PART   //
                // // // // //  //

                Box(
                    modifier = Modifier.fillMaxHeight().weight(1f)
                ) {

                    val selected = viewModel.selectedChapter()

                    MainContent(
                        selectedChapter = selected,
                        onContentChange = { newText ->
                            selected?.id?.let { viewModel.changeChapterContent(it, newText) }
                        }
                    )
                }
            }

        }
    }
}
