package example.feb.domain.usecase

import example.feb.domain.model.Chapter
import example.feb.domain.repository.ChapterRepository
import java.util.UUID

class AddChapterUseCase(
    private val repository: ChapterRepository,
) {
    suspend operator fun invoke(): Result<Chapter> {
        val chapter = Chapter(
            id      = UUID.randomUUID(),
            title   = "New chapter",
            content = "",
        )
        return runCatching { repository.upsert(chapter) }
            .map { chapter }
    }
}