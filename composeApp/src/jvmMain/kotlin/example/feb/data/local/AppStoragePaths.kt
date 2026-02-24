package example.feb.data.local

import java.nio.file.Path
import java.nio.file.Paths

object AppStoragePaths {

    fun defaultAppDataDir(appName: String): Path {

        require(appName.isNotBlank()) { }

        val home = Paths.get(System.getProperty("user.home")
            ?: error("No home directory found"))

        val base = when (currentOs()) {

            OS.WINDOWS -> System.getenv("APPDATA")
                ?.takeIf { it.isNotBlank() }
                ?.let { Paths.get(it) }
                ?: home.resolve("AppData/Roaming")

            OS.MAC     -> home.resolve("Library/Application Support") // always the same.

            OS.LINUX   -> System.getenv("XDG_DATA_HOME")
                ?.takeIf { it.isNotBlank() }
                ?.let { Paths.get(it) }
                ?: home.resolve(".local/share")
        }

        return base.resolve(appName)
    }

    // ─── OS DETECTION SEPARATION ─────────────────────────────────────────────────────────────────────────────────────

    private enum class OS { WINDOWS, MAC, LINUX }

    private fun currentOs(): OS {

        val name = System.getProperty("os.name").lowercase()

        return when {
            name.contains("win") -> OS.WINDOWS
            name.contains("mac") -> OS.MAC
            else                         -> OS.LINUX
        }

    }
}