package example.feb.data

import example.feb.domain.model.Chapter
import example.feb.domain.repository.ChapterRepository
import java.util.UUID

// local implementation (for now)
class InMemoryChapterRepository(
    seed: List<Chapter> = emptyList() // starting empty list
) : ChapterRepository {

    private val storage: LinkedHashMap<UUID, Chapter> =
        LinkedHashMap<UUID, Chapter>().apply {
            seed.forEach { put(it.id, it) }
        }

    override suspend fun loadAll(): List<Chapter> =
        storage.values.toList()

    override suspend fun upsert(chapter: Chapter) {
        storage[chapter.id] = chapter
    }

    override suspend fun delete(id: UUID) {
        storage.remove(id)
    }
}