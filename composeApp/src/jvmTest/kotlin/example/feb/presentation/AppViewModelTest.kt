package example.feb.presentation

import example.feb.domain.model.Chapter
import example.feb.domain.usecase.*
import example.feb.testdoubles.SpyChapterRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    private val id1 = UUID.fromString("00000000-0000-0000-0000-000000000001")
    private val id2 = UUID.fromString("00000000-0000-0000-0000-000000000002")

    private fun createSut(
        repo: SpyChapterRepository,
        dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
    ) = AppViewModel(
        loadChapters       =    LoadChaptersUseCase(repo),
        addChapter         =    AddChapterUseCase(repo),
        renameChapter      =    RenameChapterUseCase(repo),
        deleteChapter      =    DeleteChapterUseCase(repo),
        saveChapterContent =    SaveChapterContentUseCase(repo),
        setChapterZoom     =    SetChapterZoomUseCase(repo),
        dispatcher         =    dispatcher,
    )

    // ── DELETE, SELECT, EXIT ─────────────────────────────────────────────────────────────────────────────────────────
    @Test
    fun `delete deletes, selecting next, exiting rename`() = runTest {
        val repo = SpyChapterRepository(
            seed = listOf(
                Chapter(id = id1, title = "First", content = "A"),
                Chapter(id = id2, title = "Second", content = "B"),
            )
        )
        val dispatcher = StandardTestDispatcher(testScheduler)
        val sut = createSut(repo, dispatcher)
        advanceUntilIdle()

        sut.onStartRenaming(id1)
        assertEquals(EditingState.Renaming(id1), sut.uiState.value.editingState)

        sut.onDeleteChapter(id1)
        advanceUntilIdle()

        val state = sut.uiState.value
        assertEquals(listOf(id2), state.chapters.map { it.id })
        assertEquals(id2, state.selectedId)
        assertEquals(EditingState.None, state.editingState)
        assertEquals(1, repo.deleteCalls)

        sut.dispose()
        advanceUntilIdle()
    }

}

