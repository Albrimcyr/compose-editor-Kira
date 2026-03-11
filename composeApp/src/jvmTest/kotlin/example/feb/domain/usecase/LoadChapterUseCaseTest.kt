package example.feb.domain.usecase

import example.feb.domain.model.Chapter
import example.feb.testdoubles.SpyChapterRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class LoadChaptersUseCaseTest {

    @Test
    fun `returns markdown content unchanged`() = runTest {

        // ── TO HTML ──────────────────────────────────────────────────────────────────────────────────────────────────
        val id = UUID.fromString("00000000-0000-0000-0000-000000000001")

        val repo = SpyChapterRepository(
            seed = listOf(Chapter(id, "title", "__**hello**__"))
        )

        val sut = LoadChaptersUseCase(repo)
        val result = sut()

        assertTrue(result.isSuccess)
        val loadedChapter = result.getOrNull()!!
        assertEquals("__**hello**__", loadedChapter.single().content)
        assertEquals(1, repo.loadAllCalls)
    }

}