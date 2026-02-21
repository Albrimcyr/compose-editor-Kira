package example.feb.presentation

import example.feb.domain.model.Chapter
import java.util.UUID


// UI-only protection for the sidebar list. Derived from [AppUIState.chapters]
// Just in case.

data class ChapterRowUi(
    val id: UUID,
    val title: String
)

sealed interface EditingState {
    data object None : EditingState
    data class Renaming(val id: UUID) : EditingState
}


//  SINGLE source of truth for the UI. Stores only state. Else is derived to avoid desynchronization. And for clarity.
//  Could be improved later.

data class AppUiState(

    val isLoading: Boolean = true,
    val chapters: List<Chapter> = emptyList(),
    val selectedId: UUID? = null,
    val editingState: EditingState = EditingState.None,
    val isDarkTheme: Boolean = true,
    val errorMessage: String? = null
){

    //  can be heavy to compute O(n) with 1000+ chapters, might be changed to derived cache later ,
    val selected: Chapter?
        get() = selectedId?.let { id ->
            chapters.firstOrNull { it.id == id }
        }

    val hasSelection: Boolean
        get() = selected != null

    val selectedTitle: String
        get() = selected?.title.orEmpty()

    val selectedContent: String
        get() = selected?.content.orEmpty()

    val chapterRows: List<ChapterRowUi> get() = chapters.map { ChapterRowUi(it.id, it.title) }

}
