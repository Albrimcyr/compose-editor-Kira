package example.feb.domain.usecase

import example.feb.domain.model.Chapter
import example.feb.domain.repository.ChapterRepository
import java.util.UUID

sealed interface RenameResult {
    data class  Success(val updated: Chapter) : RenameResult
    data object BlankTitle                    : RenameResult
    data object ChapterNotFound               : RenameResult
    data class  Error(val cause: Throwable)   : RenameResult
}

class RenameChapterUseCase(
    private val repository: ChapterRepository,
) {

    suspend operator fun invoke(
        id: UUID,
        rawTitle: String,
        currentChapters: List<Chapter>,
    ): RenameResult {

        val trimmed = rawTitle.trim()
        if (trimmed.isBlank()) return RenameResult.BlankTitle

        val current = currentChapters.firstOrNull { it.id == id }
            ?: return RenameResult.ChapterNotFound

        // Small optimization :)
        if (current.title == trimmed) return RenameResult.Success(current)

        val updated = current.copy(title = trimmed)
        return runCatching { repository.upsert(updated) }
            .fold(
                onSuccess = { RenameResult.Success(updated) },
                onFailure = { RenameResult.Error(it) },
            )
    }
}