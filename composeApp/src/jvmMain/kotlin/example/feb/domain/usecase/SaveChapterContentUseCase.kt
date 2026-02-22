package example.feb.domain.usecase

import example.feb.domain.model.Chapter
import example.feb.domain.repository.ChapterRepository
import java.util.UUID

class SaveChapterContentUseCase(
    private val repository: ChapterRepository,
) {
    suspend operator fun invoke(
        id: UUID,
        html: String,
        currentChapters: List<Chapter>,
    ): Result<Chapter> {

        val current = currentChapters.firstOrNull { it.id == id }
            ?: return Result.failure(NoSuchElementException("Chapter $id not found"))

        // Small optimization :)
        if (current.content == html) return Result.success(current)

        val updated = current.copy(content = html)
        return runCatching { repository.upsert(updated) }
            .map { updated }
    }
}