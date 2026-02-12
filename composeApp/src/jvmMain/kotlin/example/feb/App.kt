package example.feb

import java.util.UUID

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment

//  - - - - -
//  model
//  - - - - -


data class Chapter(
    val id: UUID,
    val title: String
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
        chapters = chapters + Chapter(
            id = UUID.randomUUID(),
            title = "New chapter",
        )
    }

    fun selectChapter(id: UUID) {
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

}

// UI layer
@Composable
fun App() {
    MaterialTheme {

        val viewModel = remember {AppViewModel()}

        Row(modifier = Modifier.fillMaxSize()) {

            Sidebar(
                modifier = Modifier.weight(0.2f),
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


            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.8f),

                contentAlignment = Alignment.Center
            ) {
                MainContent(selectedChapter = viewModel.selectedChapter())
            }


        }
    }
}
