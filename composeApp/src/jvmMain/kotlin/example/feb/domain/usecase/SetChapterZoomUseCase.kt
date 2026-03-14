package example.feb.domain.usecase

import example.feb.domain.model.Chapter
import example.feb.domain.repository.ChapterRepository
import java.util.UUID

class SetChapterZoomUseCase(
    private val repository: ChapterRepository,
) {
    suspend operator fun invoke(
        id: UUID,
        zoomPercent: Int,
    ): Result<Chapter> {
        val current = repository.findById(id)
            ?: return Result.failure(NoSuchElementException("Chapter $id not found"))

        // Small optimization: skip IO if nothing actually changed
        if (current.zoomPercent == zoomPercent) return Result.success(current)

        val updated = current.copy(zoomPercent = zoomPercent)
        return runCatching { repository.upsert(updated) }
            .map { updated }
    }
}