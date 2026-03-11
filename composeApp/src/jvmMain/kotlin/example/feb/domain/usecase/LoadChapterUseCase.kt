package example.feb.domain.usecase

import example.feb.domain.model.Chapter
import example.feb.domain.repository.ChapterRepository


class LoadChaptersUseCase(
    private val repository: ChapterRepository,
) {
    suspend operator fun invoke(): Result<List<Chapter>> =
        runCatching { repository.loadAll() }
}