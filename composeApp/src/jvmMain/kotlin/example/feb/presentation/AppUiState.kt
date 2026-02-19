package example.feb.presentation

import java.util.UUID

data class ChapterRowUi(
    val id: UUID,
    val title: String
)

sealed interface EditingState {
    data object None : EditingState
    data class Renaming(val id: UUID, val draft: String) : EditingState
}

data class AppUiState(

    // future-proof solution (just in case...)
    val isLoading: Boolean = true,
    val chapters: List<ChapterRowUi> = emptyList(),

    val selectedId: UUID? = null,
    val selectedTitle: String = "",
    val selectedContent: String = "",
    val hasSelection: Boolean = false,

    val editingState: EditingState = EditingState.None,

    val errorMessage: String? = null
)
