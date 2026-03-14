package example.feb.presentation

import example.feb.domain.model.Chapter
import java.util.UUID


// ─── UI ONLY ───────────────────────────────────────────────────────
// Protection for the sidebar list. Derived from [AppUIState.chapters]
// Just in case.

data class ChapterRowUi(
    val id:     UUID,
    val title:  String
)

sealed interface EditingState {
    data object None                    : EditingState
    data class  Renaming(val id: UUID)  : EditingState
}

data class ZoomUiState(
    val zoomPercent: Int,
    val isAtMin: Boolean,
    val isAtMax: Boolean,
    val onIncrease: () -> Unit,
    val onDecrease: () -> Unit,
)


// ─── SINGLE SOURCE OF TRUTH ──────────────────────────────────────────────────────────────────────────────────────────
//  Stores only state.
//  Else is derived to avoid desynchronization. And for clarity.
//  chapterRows Could be improved later to map.

data class AppUiState(

    // MAIN
    val isLoading: Boolean = true,
    val chapters: List<Chapter> = emptyList(),
    val selectedId: UUID? = null,
    val editingState: EditingState = EditingState.None,

    // ERROR
    val errorMessage: String? = null,

    // SEARCH
    val searchQuery: String = "",

    // UI FLAGS
    val isDarkTheme: Boolean = true,
    val isToolbarVisible: Boolean = true,

    // DRAFT
    val draftChapterId: UUID? = null,
    val draftMarkdown: String = "",
    val isDraftDirty: Boolean = false,

    // STATS
    val contentStats: ContentStats = ContentStats.Empty,

    ){

    // ─── DERIVED ─────────────────────────────────────────────────────────────────────────────

    // SEARCH
    private fun matchesSearch (chapter: Chapter, q: String): Boolean {
        if (q.isBlank()) return true
        val query = q.trim()
        return chapter.title.contains(query, ignoreCase = true) ||
               chapter.content.contains(query, ignoreCase = true)

    }

    val filteredChapters: List<Chapter>
        get() = chapters.filter { matchesSearch(it, searchQuery) }

    // SELECTION
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

    // ZOOM (per chapter)
    val selectedZoomPercent: Int
        get() = selected?.zoomPercent ?: 100 // fallback

    val isZoomAtMin: Boolean
        get() = selectedZoomPercent <= 100

    val isZoomAtMax: Boolean
        get() = selectedZoomPercent >= 300

}
