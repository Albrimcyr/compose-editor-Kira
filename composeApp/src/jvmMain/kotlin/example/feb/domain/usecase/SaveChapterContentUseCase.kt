package example.feb.domain.usecase

import example.feb.domain.model.Chapter
import example.feb.domain.repository.ChapterRepository
import java.util.UUID

class SaveChapterContentUseCase(
    private val repository: ChapterRepository,
) {
    suspend operator fun invoke(
        id: UUID,
        markdown: String,
        currentChapters: List<Chapter>,
    ): Result<Chapter> {

        val current = currentChapters.firstOrNull { it.id == id }
            ?: return Result.failure(NoSuchElementException("Chapter $id not found"))

        // Small optimization :)
        if (current.content == markdown) return Result.success(current)

        val updated = current.copy(content = markdown)
        return runCatching { repository.upsert(updated) }
            .map { updated }
    }
}