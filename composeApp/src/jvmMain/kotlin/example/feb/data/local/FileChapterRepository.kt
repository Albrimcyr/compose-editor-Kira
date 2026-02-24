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
    private val rootDir: Path,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ChapterRepository {

    // ─── JSON ────────────────────────────────────────────────────────────────────────────────────────────────────────

    private val json: Json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    // ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────

    private val mutex = Mutex()

    private val chaptersDir = rootDir.resolve("chapters")
    private val indexFile = rootDir.resolve("index.json")

    // to be implemented?

    override suspend fun loadAll(): List<Chapter> = withContext(dispatcher) {
        // test

    }


}