package example.feb.data

import example.feb.domain.model.Chapter
import java.util.UUID

// local implementation (for now)
class InMemoryChapterRepository(
    seed: List<Chapter> = emptyList() // starting empty list
) : ChapterRepository {

    // Map is easier since it O(1) instead of O(n)
    private val storage: LinkedHashMap<UUID, Chapter> =
        LinkedHashMap<UUID, Chapter>().apply {
            seed.forEach { put(it.id, it) }
        }

    // convert data for Interface
    override suspend fun loadAll(): List<Chapter> =
        storage.values.toList()

    // update or insert
    override suspend fun upsert(chapter: Chapter) {
        storage[chapter.id] = chapter
    }

    // delete by ID
    override suspend fun delete(id: UUID) {
        storage.remove(id)
    }
}
