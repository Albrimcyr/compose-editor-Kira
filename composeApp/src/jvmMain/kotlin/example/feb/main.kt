package example.feb

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import example.feb.data.InMemoryChapterRepository
import example.feb.presentation.AppViewModel

fun main() = application {
    val viewModel = AppViewModel(InMemoryChapterRepository())

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