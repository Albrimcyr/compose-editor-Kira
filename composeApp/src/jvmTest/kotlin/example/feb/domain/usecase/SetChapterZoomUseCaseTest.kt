package example.feb.domain.usecase

import example.feb.domain.model.Chapter
import example.feb.testdoubles.SpyChapterRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.UUID

class SetChapterZoomUseCaseTest {

    private val id = UUID.fromString("00000000-0000-0000-0000-000000000001")

    // ── NO REPO CALL ON SAME ZOOM ────────────────────────────────────────────────────────────────────────────────────
    @Test
    fun `no repo call on the same zoom`() = runTest {
        val chapter = Chapter(id, "title", "content", zoomPercent = 150)
        val repo    = SpyChapterRepository(seed = listOf(chapter))
        val sut     = SetChapterZoomUseCase(repo)

        val result  = sut(id = id, zoomPercent = 150)

        Assertions.assertTrue(result.isSuccess)
        Assertions.assertEquals(chapter, result.getOrNull())
        Assertions.assertEquals(1, repo.findByIdCalls)
        Assertions.assertEquals(0, repo.upsertCalls) // no IO
    }

    // ── CHAPTER NOT FOUND ────────────────────────────────────────────────────────────────────────────────────────────
    @Test
    fun `returns failure when chapter not found`() = runTest {
        val repo    = SpyChapterRepository()
        val sut     = SetChapterZoomUseCase(repo)

        val result  = sut(id = id, zoomPercent = 150)

        Assertions.assertTrue(result.isFailure)
        Assertions.assertEquals(1, repo.findByIdCalls)
        Assertions.assertEquals(0, repo.upsertCalls) // no IO
    }

    // ── REPO EXCEPTION ───────────────────────────────────────────────────────────────────────────────────────────────
    @Test
    fun `repo exception returns failure`() = runTest {
        val chapter         = Chapter(id, "title", "content", zoomPercent = 100)
        val repo            = SpyChapterRepository(seed = listOf(chapter)).apply {
            throwOnUpsert   = IllegalStateException("Disk full!!!")
        }
        val sut = SetChapterZoomUseCase(repo)

        val result = sut(id = id, zoomPercent = 200)

        Assertions.assertTrue(result.isFailure)
        Assertions.assertEquals("Disk full!!!", result.exceptionOrNull()?.message)
        Assertions.assertEquals(1, repo.findByIdCalls)
        Assertions.assertEquals(1, repo.upsertCalls) // no IO
    }

}