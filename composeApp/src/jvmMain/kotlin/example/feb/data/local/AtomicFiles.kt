package example.feb.data.local

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.*
import java.nio.file.StandardOpenOption.*

internal object AtomicFiles {

    fun writeAtomic(target: Path, bytes: ByteArray) {

        Files.createDirectories(target.parent) // doesnt exist > create

        // ATOMIC (same folder), Unique temp file to avoid collide (future-proof!)
        val tmp = Files.createTempFile(target.parent, target.fileName.toString(), ".tmp")

        try {
            FileChannel.open(tmp, WRITE).use { ch ->
                ch.write(ByteBuffer.wrap(bytes))
                ch.force(true)
            }

            try {
                Files.move(tmp, target, ATOMIC_MOVE, REPLACE_EXISTING)
            } catch (_: Exception) {
                Files.move(tmp, target, REPLACE_EXISTING)
            }

            // Linux-related...? Could be improved later. Match folder-config (folders are weird)
            // FileChannel.open(target.parent, READ).use { it.force(true) }

        // exception any time? Nuke.
        } catch (e: Exception) {
            runCatching { Files.deleteIfExists(tmp) }
            throw e
        }
    }
}