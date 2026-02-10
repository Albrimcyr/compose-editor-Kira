package example.feb

import java.util.UUID

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel


data class Chapter(
    val id: UUID,
    val title: String
)

// basic view model (in case I want to expand)
class AppViewModel {

    var chapters by mutableStateOf(listOf<Chapter>())
        private set

    var selectedChapterID by mutableStateOf<UUID?>(null)
        private set

    var editingChapterID by mutableStateOf<UUID?>(null)
        private set

    var editingDraftTitle by mutableStateOf<String?>(null)
        private set

    fun addChapter(){
        chapters = chapters + Chapter(
            id = UUID.randomUUID(),
            title = "New chapter",
        )
    }

    fun selectChapter(id: UUID) {
        selectedChapterID = id
        editingChapterID = null
        editingDraftTitle = null
    }

    fun startEditing(id: UUID) {
        val chapter = chapters.first { it.id == id }
        editingChapterID = id
        editingDraftTitle = chapter.title
    }

    fun changeDraft(text: String) {
        editingDraftTitle = text
    }

    fun commitRename() {
        val id = editingChapterID ?: return
        val newTitle = editingDraftTitle ?: return

        chapters = chapters.map { chapter ->
            if (chapter.id == id) chapter.copy(title = newTitle) else chapter
        }

        editingChapterID = null
        editingDraftTitle = null
    }

    fun deleteChapter(id: UUID) {
        chapters = chapters.filter { it.id != id }

        if (selectedChapterID == id) {
            selectedChapterID = null
        }

        if (editingChapterID == id) {
            editingChapterID = null
            editingDraftTitle = null
        }
    }

    fun selectedChapter(): Chapter? =
        chapters.firstOrNull { it.id == selectedChapterID }

}

// UI
@Composable
fun App() {
    MaterialTheme {

        val viewModel = remember {AppViewModel()}
        val chapters = viewModel.chapters
        val selectedChapterID = viewModel.selectedChapterID
        val editingChapterID = viewModel.editingChapterID
        val editingDraftTitle = viewModel.editingDraftTitle
        val selectedChapter = viewModel.selectedChapter()


        Row(modifier = Modifier.fillMaxSize()) {

            Sidebar(
                modifier = Modifier.weight(0.2f),
                chapters = chapters,
                selectedChapterID = selectedChapterID,
                editingChapterID = editingChapterID,
                editingDraftTitle = editingDraftTitle,

                onAddChapter = viewModel::addChapter,
                onSelectChapter = viewModel::selectChapter,
                onEditChapter = viewModel::startEditing,
                onEditDraftChange = viewModel::changeDraft,
                onRenameCommit = viewModel::commitRename,
                onDeleteChapter = viewModel::deleteChapter
            )


            Box(
                modifier = Modifier
                    .fillMaxHeight().weight(0.8f),

                contentAlignment = Alignment.Center
            ) {
                MainContent(selectedChapter = selectedChapter)
            }


        }
    }
}
