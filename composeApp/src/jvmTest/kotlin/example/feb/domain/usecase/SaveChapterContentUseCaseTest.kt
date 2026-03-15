package example.feb.domain.usecase

import example.feb.domain.model.Chapter
import example.feb.testdoubles.SpyChapterRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.UUID

class SaveChapterContentUseCaseTest {

    @Test
    fun `no repo call on the same content`() = runTest {

        // ── ZERO UPSERT CALLS ────────────────────────────────────────────────────────────────────────────────────────
        val id = UUID.fromString("00000000-0000-0000-0000-000000000001")

        val chapter = Chapter(id, "title", "Hello")
        val repo = SpyChapterRepository(seed = listOf(chapter))
        val sut = SaveChapterContentUseCase(repo)

        val result = sut(id = id, markdown = "Hello")

        Assertions.assertTrue(result.isSuccess)
        Assertions.assertEquals(chapter, result.getOrNull())
        Assertions.assertEquals(1, repo.findByIdCalls)
        Assertions.assertEquals(0, repo.upsertCalls) // !!
    }

}