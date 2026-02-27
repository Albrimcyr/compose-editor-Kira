package example.feb.domain.usecase

import example.feb.testdoubles.SpyChapterRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AddChapterUseCaseTest {

    // ── SUCCESSFUL ADD ───────────────────────────────────────────────────────────────────────────────────────────────
    @Test
    fun `adds a new chapter and persists it`() = runTest {
        val repo = SpyChapterRepository()
        val sut = AddChapterUseCase(repo)                  // system under test

        val result = sut()

        assertTrue(result.isSuccess)                                   // expected success

        val chapter = result.getOrNull()!!                             // ADD Chapter
        assertEquals("New chapter", chapter.title)  // title
        assertEquals("", chapter.content)           // blank
        assertEquals(1, repo.upsertCalls)           // how many times called?
        assertEquals(chapter, repo.get(chapter.id)) // actually in storage?
    }

    // ── DIFFERENT UUID/ID ────────────────────────────────────────────────────────────────────────────────────────────
    @Test
    fun `two chapters = two different IDs`() = runTest {
        val repo = SpyChapterRepository()
        val sut = AddChapterUseCase(repo)                   // system under test

        val a = sut().getOrNull()!!                                    // ADD Chapter A
        val b = sut().getOrNull()!!                                    // ADD Chapter B

        assertNotEquals(a.id, b.id)               // different?
        assertEquals(2, repo.upsertCalls)           // how many times called?
    }
}