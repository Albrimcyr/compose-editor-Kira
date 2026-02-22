package example.feb

import example.feb.domain.usecase.*

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import example.feb.data.InMemoryChapterRepository
import example.feb.presentation.AppViewModel

fun main() = application {
    val repository = InMemoryChapterRepository()

    val viewModel = AppViewModel(
        loadChapters       = LoadChaptersUseCase(repository),
        addChapter         = AddChapterUseCase(repository),
        renameChapter      = RenameChapterUseCase(repository),
        deleteChapter      = DeleteChapterUseCase(repository),
        saveChapterContent = SaveChapterContentUseCase(repository),
    )

    Window(
        onCloseRequest = {
            viewModel.dispose()
            exitApplication()
        },
        title = "Book",
    ) {
        App(viewModel)
    }
}