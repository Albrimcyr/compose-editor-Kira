package example.feb.domain.usecase

import example.feb.domain.model.Chapter
import example.feb.domain.repository.ChapterRepository
import example.feb.domain.text.HtmlContentNormalizer


class LoadChaptersUseCase(
    private val repository: ChapterRepository,
) {

    suspend operator fun invoke(): Result<List<Chapter>> =
        runCatching { repository.loadAll() }
            .map { chapters -> chapters.map(::normalizeIfNeeded) }

    private fun normalizeIfNeeded(chapter: Chapter): Chapter {
        val normalized = HtmlContentNormalizer.normalizeToHtml(chapter.content)
        return if (normalized == chapter.content) chapter
        else chapter.copy(content = normalized)
    }

}