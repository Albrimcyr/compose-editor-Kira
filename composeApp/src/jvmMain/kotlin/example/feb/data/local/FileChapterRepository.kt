package example.feb.data.local

import example.feb.domain.model.Chapter
import example.feb.domain.repository.ChapterRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.name


class FileChapterRepository(
    rootDir: Path,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val json: Json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }
) : ChapterRepository {

    // ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────

    private val mutex = Mutex() // LOCK

    private val chaptersDir = rootDir.resolve("chapters") // CHAPTERS FOLDER
    private val indexFile = rootDir.resolve("index.json") // INDEX FILE

    // ─── LOAD ALL ────────────────────────────────────────────────────────────────────────────────────────────────────
    override suspend fun loadAll(): List<Chapter> = withContext(ioDispatcher) {

        mutex.withLock {
            Files.createDirectories(chaptersDir)

            val index = readIndexOrNull()
            val orderedIds = index?.order.orEmpty()

            val loadedByIndex = orderedIds.mapNotNull { id -> readChapterOrNull(id) }

            // (OPTIONAL SELF HEALING - in case something breaks)
            val indexedSet = orderedIds.toHashSet() // O(1) instead of List.
            val orphans = Files.list(chaptersDir).use { stream ->
                stream.iterator().asSequence()
                    .filter { it.isRegularFile() && it.extension == "json" }
                    .mapNotNull { path ->
                        val id = path.name.removeSuffix(".json")
                        if (id in indexedSet) null else readChapterOrNull(id)
                    }
                    .toList()
            }

            loadedByIndex + orphans
        }
    }

    // ─── UPSERT ──────────────────────────────────────────────────────────────────────────────────────────────────────
    override suspend fun upsert(chapter: Chapter) = withContext(ioDispatcher) {
        mutex.withLock {
            Files.createDirectories(chaptersDir)

            writeChapter(chapter)

            val id = chapter.id.toString()
            val index = readIndexOrNull() ?: IndexDto(version = 1, order = emptyList())

            if (id !in index.order) {
                writeIndex(index.copy(order = index.order + id))
            }
        }
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────────────────────────────────────────────
    override suspend fun delete(id: UUID) = withContext(ioDispatcher) {
        mutex.withLock {

            val idStr = id.toString() // used thrice

            Files.deleteIfExists(chapterFile(idStr))
            val index = readIndexOrNull() ?: return@withLock
            if (idStr in index.order) {
                writeIndex(index.copy(order = index.order.filterNot { it == idStr }))
            }

        }
    }

    // ──────── Convert to / from JSON ─────────────────────────────────────────────────────────────────────────────────
    @Serializable private data class ChapterDto(val id: String, val title: String, val content: String)
    @Serializable private data class IndexDto(val version: Int, val order: List<String>)

    // ────── HELPERS INDEX ────────────────────────────────────────────────────────────────────────────────────────────
    private fun chapterFile(id: String): Path = chaptersDir.resolve("$id.json")

    private fun readIndexOrNull(): IndexDto? {
        if (!Files.exists(indexFile)) return null
        val text = Files.readString(indexFile, StandardCharsets.UTF_8)
        return json.decodeFromString(text)
    }

    private fun writeIndex(index: IndexDto) {
        val bytes = json.encodeToString(index).toByteArray(StandardCharsets.UTF_8)
        AtomicFiles.writeAtomic(indexFile, bytes)
    }

    // ────── HELPERS Chapters ─────────────────────────────────────────────────────────────────────────────────────────
    private fun readChapterOrNull(id: String): Chapter? {
        val file = chapterFile(id)
        if (!Files.exists(file)) return null
        val text = Files.readString(file, StandardCharsets.UTF_8)
        val dto = json.decodeFromString<ChapterDto>(text)
        return Chapter(UUID.fromString(dto.id), dto.title, dto.content)
    }

    private fun writeChapter(chapter: Chapter) {
        val dto = ChapterDto(chapter.id.toString(), chapter.title, chapter.content)
        val bytes = json.encodeToString(dto).toByteArray(StandardCharsets.UTF_8)
        AtomicFiles.writeAtomic(chapterFile(dto.id), bytes)
    }
}