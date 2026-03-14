package example.feb.presentation

import example.feb.domain.model.Chapter
import java.util.UUID


// ─── UI ONLY ───────────────────────────────────────────────────────
// Protection for the sidebar list. Derived from [AppUIState.chapters]
// Just in case.

data class ChapterRowUi(
    val id: UUID,
    val title: String
)

sealed interface EditingState {
    data object None : EditingState
    data class Renaming(val id: UUID) : EditingState
}


// ─── SINGLE SOURCE OF TRUTH FOR THE UI ───────────────────────────────────────────────────────
//  Stores only state.
//  Else is derived to avoid desynchronization. And for clarity.
//  chapterRows Could be improved later to map.

data class AppUiState(

    val isLoading: Boolean = true,
    val chapters: List<Chapter> = emptyList(),
    val selectedId: UUID? = null,
    val editingState: EditingState = EditingState.None,

    val errorMessage: String? = null,
    val searchQuery: String = "",

    // UI FLAGS
    val isDarkTheme: Boolean = true,
    val isToolbarVisible: Boolean = true,

    // EDITOR
    val editorFontSizeSp: Int = 16,
    val draftChapterId: UUID? = null,
    val draftMarkdown: String = "",
    val isDraftDirty: Boolean = false,

    // STATS
    val contentStats: ContentStats = ContentStats.Empty,

    ){

    // ─── DERIVED ─────────────────────────────────────────────────────────────────────────────

    private fun matchesSearch (chapter: Chapter, q: String): Boolean {
        if (q.isBlank()) return true
        val query = q.trim()
        return chapter.title.contains(query, ignoreCase = true) ||
               chapter.content.contains(query, ignoreCase = true)

    }

    val filteredChapters: List<Chapter>
        get() = chapters.filter { matchesSearch(it, searchQuery) }

    val selected: Chapter?
        get() = selectedId?.let { id -> chapters.firstOrNull { it.id == id } }

    val selectedTitle: String
        get() = selected?.title.orEmpty()

    val selectedContent: String
        get() {
            val id = selectedId ?: return ""
            return if (draftChapterId == id) draftMarkdown else selected?.content.orEmpty()
        }

    val chapterRows: List<ChapterRowUi>
        get() = filteredChapters.map { ChapterRowUi(it.id, it.title) }

}
