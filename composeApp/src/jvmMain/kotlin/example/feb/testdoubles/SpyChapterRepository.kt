package example.feb.testdoubles

import example.feb.domain.model.Chapter
import example.feb.domain.repository.ChapterRepository
import java.util.UUID

class SpyChapterRepository(
    seed: List<Chapter> = emptyList(),
) : ChapterRepository {

    var loadAllCalls = 0
        private set
    var upsertCalls = 0
        private set
    var deleteCalls = 0
        private set

    var throwOnLoadAll: Throwable? = null
    var throwOnUpsert: Throwable? = null
    var throwOnDelete: Throwable? = null

    private val storage = LinkedHashMap<UUID, Chapter>().apply {
        seed.forEach { put(it.id, it) }
    }

    override suspend fun loadAll(): List<Chapter> {
        loadAllCalls++
        throwOnLoadAll?.let { throw it }
        return storage.values.toList()
    }

    override suspend fun upsert(chapter: Chapter) {
        upsertCalls++
        throwOnUpsert?.let { throw it }
        storage[chapter.id] = chapter
    }

    override suspend fun delete(id: UUID) {
        deleteCalls++
        throwOnDelete?.let { throw it }
        storage.remove(id)
    }

    fun get(id: UUID): Chapter? = storage[id]
}