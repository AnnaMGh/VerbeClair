package com.vamvisions.verbeclair.core.data.bundle

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READWRITE
import com.vamvisions.verbeclair.core.data.di.PlatformPaths
import com.vamvisions.verbeclair.core.database.dataset.BundledMetadata
import com.vamvisions.verbeclair.core.database.verb.entity.MetadataKeys
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.gzip
import okio.use
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Installs the bundled verb database in place of the local one (A-1, design D4). It runs once at
 * start-up, before the verb database is opened, and never touches the user database.
 */
class BundledVerbDatabaseInstaller(
    private val paths: PlatformPaths,
    private val bundle: BundledVerbDatabaseSource,
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    /** Returns `true` when the bundled database was installed, `false` when the local one was kept. */
    suspend fun installIfNeeded(): Boolean =
        withContext(ioDispatcher) {
            val target = paths.verbDatabaseFile.toPath()
            val temporary = "${paths.verbDatabaseFile}$TEMPORARY_SUFFIX".toPath()
            // A copy cut short by a killed app is never resumed: it is redone from the start.
            fileSystem.delete(temporary)

            val bundled = bundle.metadata()
            if (!shouldInstall(bundled, readLocalMetadata(target))) return@withContext false

            install(target, temporary)
            true
        }

    private fun install(target: Path, temporary: Path) {
        target.parent?.let(fileSystem::createDirectories)
        bundle.openCompressedDatabase().gzip().use { compressed ->
            fileSystem.write(temporary, mustCreate = true) { writeAll(compressed) }
        }
        syncFile(temporary)
        // An old WAL would otherwise be replayed onto the new file.
        SIDE_FILE_SUFFIXES.forEach { fileSystem.delete("$target$it".toPath()) }
        fileSystem.atomicMove(temporary, target)
        excludeFromBackup(target)
    }

    /** The local copy's metadata, or `null` when there is no usable local database. */
    private fun readLocalMetadata(target: Path): LocalMetadata? {
        if (!fileSystem.exists(target)) return null
        return try {
            // Without SQLITE_OPEN_CREATE, so a missing file is never created here.
            BundledSQLiteDriver().open(target.toString(), SQLITE_OPEN_READWRITE).use { it.readMetadata() }
        } catch (_: Exception) {
            null // Unreadable or not a verb database: install the bundled one.
        }
    }

    private fun SQLiteConnection.readMetadata(): LocalMetadata {
        val values = mutableMapOf<String, String>()
        prepare("SELECT `key`, value FROM metadata").use { statement ->
            while (statement.step()) values[statement.getText(0)] = statement.getText(1)
        }
        return LocalMetadata(
            schemaVersion = values[MetadataKeys.SCHEMA_VERSION]?.toIntOrNull(),
            bundleBuild = values[MetadataKeys.BUNDLE_BUILD]?.toIntOrNull(),
            checkpoint = values[MetadataKeys.CHECKPOINT],
        )
    }

    private companion object {
        const val TEMPORARY_SUFFIX = ".tmp"
        val SIDE_FILE_SUFFIXES = listOf("-wal", "-shm", "-journal")
    }
}

/** What the installer needs from a local verb database's `metadata`; `null` when missing or unreadable. */
internal data class LocalMetadata(
    val schemaVersion: Int?,
    val bundleBuild: Int?,
    val checkpoint: String?,
)

/** The A-1 rule: install when any condition holds, otherwise keep the local database. */
internal fun shouldInstall(bundled: BundledMetadata, local: LocalMetadata?): Boolean {
    if (local == null) return true
    if (local.schemaVersion != bundled.schemaVersion) return true
    val localBuild = local.bundleBuild ?: return true
    if (bundled.bundleBuild > localBuild) return true
    val checkpoint = local.checkpoint?.toInstantOrNull() ?: return true
    val bundledAt = bundled.lastUpdatedAt.toInstantOrNull() ?: return false
    return bundledAt > checkpoint
}

// Compared as instants, not strings: "…22.5Z" sorts before "…22Z" as text.
@OptIn(ExperimentalTime::class)
private fun String.toInstantOrNull(): Instant? =
    try {
        Instant.parse(this)
    } catch (_: IllegalArgumentException) {
        null
    }

/** Forces the file's bytes to storage before it is renamed into place. */
internal expect fun syncFile(path: Path)

/** Keeps the replaceable verb database out of iCloud backup (iOS only, A-1). */
internal expect fun excludeFromBackup(path: Path)
