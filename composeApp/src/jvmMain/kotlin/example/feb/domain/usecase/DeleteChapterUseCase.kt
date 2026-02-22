package example.feb.domain.usecase

import example.feb.domain.repository.ChapterRepository
import java.util.UUID

class DeleteChapterUseCase(
    private val repository: ChapterRepository,
) {
    suspend operator fun invoke(id: UUID): Result<Unit> =
        runCatching { repository.delete(id) }
}