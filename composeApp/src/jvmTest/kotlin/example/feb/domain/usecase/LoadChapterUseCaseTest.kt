package example.feb.domain.usecase

import example.feb.domain.model.Chapter
import example.feb.testdoubles.SpyChapterRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class LoadChaptersUseCaseTest {

    @Test
    fun `html conversion`() = runTest {

        // ── TO HTML ──────────────────────────────────────────────────────────────────────────────────────────────────
        val id = UUID.fromString("00000000-0000-0000-0000-000000000001")

        val repo = SpyChapterRepository(
            seed = listOf(Chapter(id, "title", "hello\n\nworld"))
        )

        val sut = LoadChaptersUseCase(repo)
        val result = sut()

        assertTrue(result.isSuccess)
        val loadedChapter = result.getOrNull()!!
        assertEquals("<p>hello</p><p>world</p>", loadedChapter.single().content)
        assertEquals(1, repo.loadAllCalls)
    }

}