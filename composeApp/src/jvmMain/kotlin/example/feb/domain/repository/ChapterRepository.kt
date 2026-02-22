package example.feb.domain.repository

import example.feb.domain.model.Chapter
import java.util.UUID

interface ChapterRepository {
    suspend fun loadAll(): List<Chapter>
    suspend fun upsert(chapter: Chapter)
    suspend fun delete(id: UUID)
}