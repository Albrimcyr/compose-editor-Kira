package example.feb.domain.usecase

import example.feb.domain.model.Chapter
import example.feb.testdoubles.SpyChapterRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class DeleteChapterUseCaseTest {

    private val id = UUID.fromString("00000000-0000-0000-0000-000000000001") // UUID simulation

    // ── SUCCESSFUL DELETE ────────────────────────────────────────────────────────────────────────────────────────────
    @Test
    fun `deletes existing chapter`() = runTest {
        val repo = SpyChapterRepository(seed = listOf(Chapter(id, "title", "content")))
        val sut = DeleteChapterUseCase(repo)

        val result = sut(id)

        assertTrue      (result.isSuccess)                                                 // successful delete
        assertEquals    (1, repo.deleteCalls)                           // one call
        assertNull      (repo.get(id))                                                     // no ID found
    }

    // ── DEMO BASIC TEST ──────────────────────────────────────────────────────────────────────────────────────────────
    @Test
    fun `repo exception returns failure`() = runTest {
        val repo = SpyChapterRepository().apply {
            throwOnDelete = IllegalStateException("Error!")
        }
        val sut = DeleteChapterUseCase(repo)

        val result = sut(id)

        assertTrue      (result.isFailure)
        assertEquals    ("Error!", result.exceptionOrNull()?.message)
        assertEquals    (1, repo.deleteCalls)
    }
}