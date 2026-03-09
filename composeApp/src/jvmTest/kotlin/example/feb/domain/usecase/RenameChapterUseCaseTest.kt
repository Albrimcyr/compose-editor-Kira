package example.feb.domain.usecase

import example.feb.domain.model.Chapter
import example.feb.testdoubles.SpyChapterRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.UUID

class RenameChapterUseCaseTest {

    private val id = UUID.fromString("00000000-0000-0000-0000-000000000001")

    @Test
    fun `no repo calls when trimmed`() = runTest {
        val chapter = Chapter(id, "testTitle", "x")
        val repo = SpyChapterRepository(seed = listOf(chapter))
        val sut = RenameChapterUseCase(repo)

        val result = sut(id = id, rawTitle = "  testTitle  ", currentChapters = listOf(chapter))

        Assertions.assertTrue(result is RenameResult.Success)
        Assertions.assertEquals(chapter, (result as RenameResult.Success).updated)
        Assertions.assertEquals(0, repo.upsertCalls)
    }

}