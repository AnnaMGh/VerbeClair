package com.vamvisions.verbeclair.core.data.bundle

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.vamvisions.verbeclair.core.data.di.PlatformPaths
import com.vamvisions.verbeclair.core.database.dataset.BundledMetadata
import com.vamvisions.verbeclair.core.database.verb.VERB_SCHEMA_VERSION
import com.vamvisions.verbeclair.core.database.verb.entity.MetadataEntity
import com.vamvisions.verbeclair.core.database.verb.entity.MetadataKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import okio.Buffer
import okio.ByteString
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.Source
import okio.buffer
import okio.gzip
import okio.use
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The A-1 install rule, on real files with tiny fixture databases (design D4). */
class BundledVerbDatabaseInstallerTest {
    private val fileSystem = FileSystem.SYSTEM
    private val directory: Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "installer-${Random.nextLong().toULong()}"
    private val paths =
        object : PlatformPaths {
            override val verbDatabaseFile = (directory / "verbs.db").toString()
            override val userDatabaseFile = (directory / "user.db").toString()
            override val dataStoreFile = (directory / "settings.preferences_pb").toString()
        }
    private val target = paths.verbDatabaseFile.toPath()
    private val temporary = "${paths.verbDatabaseFile}.tmp".toPath()

    init {
        fileSystem.createDirectories(directory)
    }

    @AfterTest
    fun deleteFiles() {
        fileSystem.deleteRecursively(directory)
    }

    @Test
    fun noLocalDatabaseInstallsTheBundledOne() =
        runTest {
            assertTrue(installer(bundle()).installIfNeeded())

            assertEquals(BUNDLED, installedFixture())
            assertEquals(BUNDLED_AT, installedMetadata(MetadataKeys.CHECKPOINT))
            assertFalse(fileSystem.exists(temporary))
        }

    @Test
    fun localDataNewerThanTheBundleIsKept() =
        runTest {
            writeLocal(checkpoint = "2026-10-09T08:00:00Z")

            assertFalse(installer(bundle()).installIfNeeded())

            assertEquals(LOCAL, installedFixture())
        }

    @Test
    fun localDataAsNewAsTheBundleIsKept() =
        runTest {
            writeLocal(checkpoint = BUNDLED_AT)

            assertFalse(installer(bundle()).installIfNeeded())

            assertEquals(LOCAL, installedFixture())
        }

    @Test
    fun checkpointsAreComparedAsInstantsNotText() =
        runTest {
            // As text "…22.5Z" sorts before "…22Z", which would wrongly replace newer local data.
            writeLocal(checkpoint = "2026-10-08T09:26:22.500Z")

            assertFalse(installer(bundle()).installIfNeeded())

            assertEquals(LOCAL, installedFixture())
        }

    @Test
    fun newerBundledDataReplacesTheLocalOne() =
        runTest {
            writeLocal(checkpoint = "2026-10-01T00:00:00Z")

            assertTrue(installer(bundle()).installIfNeeded())

            assertEquals(BUNDLED, installedFixture())
            assertEquals(BUNDLED_AT, installedMetadata(MetadataKeys.CHECKPOINT))
        }

    @Test
    fun differentSchemaVersionReplacesTheLocalOne() =
        runTest {
            writeLocal(checkpoint = "2026-10-09T08:00:00Z", schemaVersion = VERB_SCHEMA_VERSION + 1)

            assertTrue(installer(bundle()).installIfNeeded())

            assertEquals(BUNDLED, installedFixture())
        }

    @Test
    fun higherBundleBuildReplacesTheLocalOne() =
        runTest {
            writeLocal(checkpoint = "2026-10-09T08:00:00Z")

            assertTrue(installer(bundle(bundleBuild = BUNDLE_BUILD + 1)).installIfNeeded())

            assertEquals(BUNDLED, installedFixture())
        }

    /** A WAL left without its database (SQLite cleans side files up itself when it opens a readable one). */
    @Test
    fun leftoverSideFilesAreRemovedBeforeTheInstall() =
        runTest {
            val sideFiles = listOf("-wal", "-shm", "-journal").map { "${paths.verbDatabaseFile}$it".toPath() }
            sideFiles.forEach { file -> fileSystem.write(file) { writeUtf8("stale") } }

            assertTrue(installer(bundle()).installIfNeeded())

            sideFiles.forEach { assertFalse(fileSystem.exists(it), "$it is left next to the new database") }
        }

    @Test
    fun unparseableBundledDateKeepsAValidLocalCopy() {
        val bundled = BundledMetadata("not a date", VERB_SCHEMA_VERSION, BUNDLE_BUILD, "", "", JsonArray(emptyList()))
        val local = LocalMetadata(VERB_SCHEMA_VERSION, BUNDLE_BUILD, checkpoint = "2026-10-01T00:00:00Z")

        assertFalse(shouldInstall(bundled, local))
    }

    @Test
    fun unreadableLocalFileIsReplaced() =
        runTest {
            fileSystem.write(target) { writeUtf8("not a database") }

            assertTrue(installer(bundle()).installIfNeeded())

            assertEquals(BUNDLED, installedFixture())
        }

    @Test
    fun leftoverOfAKilledInstallIsDiscardedAndTheInstallRedone() =
        runTest {
            fileSystem.write(temporary) { writeUtf8("half a database") }

            assertTrue(installer(bundle()).installIfNeeded())

            assertEquals(BUNDLED, installedFixture())
            assertFalse(fileSystem.exists(temporary))
        }

    @Test
    fun leftoverOfAKilledInstallIsDiscardedWhenTheLocalOneIsKept() =
        runTest {
            writeLocal(checkpoint = "2026-10-09T08:00:00Z")
            fileSystem.write(temporary) { writeUtf8("half a database") }

            assertFalse(installer(bundle()).installIfNeeded())

            assertFalse(fileSystem.exists(temporary))
        }

    @Test
    fun userDatabaseIsNeverTouched() =
        runTest {
            val userDatabase = paths.userDatabaseFile.toPath()
            fileSystem.write(userDatabase) { writeUtf8("favourites") }
            val before = fileSystem.metadata(userDatabase).lastModifiedAtMillis
            writeLocal(checkpoint = "2026-10-01T00:00:00Z")

            assertTrue(installer(bundle()).installIfNeeded())

            assertEquals("favourites", fileSystem.read(userDatabase) { readUtf8() })
            assertEquals(before, fileSystem.metadata(userDatabase).lastModifiedAtMillis)
        }

    @Test
    fun packedResourcesHoldTheBundledDatabase() =
        runTest {
            val source = ResourceBundledVerbDatabaseSource(testResourceFileOpener())

            assertEquals("2026-10-08T09:26:22Z", source.metadata().lastUpdatedAt)
            val header =
                source
                    .openCompressedDatabase()
                    .gzip()
                    .buffer()
                    .use { it.readByteString(16) }
            assertEquals(SQLITE_HEADER, header)
        }

    private fun installer(bundle: BundledVerbDatabaseSource) = BundledVerbDatabaseInstaller(paths, bundle, fileSystem)

    /** A bundle whose database is a fixture marked [BUNDLED], gzip-compressed like the real one. */
    private suspend fun bundle(bundleBuild: Int = BUNDLE_BUILD): BundledVerbDatabaseSource {
        val file = directory / "bundled-fixture.db"
        writeFixture(file, BUNDLED, BUNDLED_AT, VERB_SCHEMA_VERSION, bundleBuild)
        val compressed =
            Buffer()
                .also { buffer ->
                    (buffer as okio.Sink).gzip().buffer().use { it.write(fileSystem.read(file) { readByteString() }) }
                }.readByteString()
        fileSystem.delete(file)
        val metadata = BundledMetadata(BUNDLED_AT, VERB_SCHEMA_VERSION, bundleBuild, "", "", JsonArray(emptyList()))
        return FakeBundle(metadata, compressed)
    }

    private suspend fun writeLocal(checkpoint: String, schemaVersion: Int = VERB_SCHEMA_VERSION) =
        writeFixture(target, LOCAL, checkpoint, schemaVersion, BUNDLE_BUILD)

    private suspend fun writeFixture(
        file: Path,
        fixture: String,
        checkpoint: String,
        schemaVersion: Int,
        bundleBuild: Int,
    ) {
        val database =
            fileVerbDatabaseBuilder(file.toString())
                .setDriver(BundledSQLiteDriver())
                .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
        try {
            database.metadataDao().put(
                listOf(
                    MetadataEntity(FIXTURE_KEY, fixture),
                    MetadataEntity(MetadataKeys.LAST_UPDATED_AT, checkpoint),
                    MetadataEntity(MetadataKeys.CHECKPOINT, checkpoint),
                    MetadataEntity(MetadataKeys.SCHEMA_VERSION, schemaVersion.toString()),
                    MetadataEntity(MetadataKeys.BUNDLE_BUILD, bundleBuild.toString()),
                ),
            )
        } finally {
            database.close()
        }
    }

    /** Opens the installed file with Room, which also proves it is a valid, decompressed database. */
    private suspend fun installedMetadata(key: String): String? {
        val database =
            fileVerbDatabaseBuilder(paths.verbDatabaseFile)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
        return try {
            database.metadataDao().get(key)
        } finally {
            database.close()
        }
    }

    private suspend fun installedFixture() = installedMetadata(FIXTURE_KEY)

    private class FakeBundle(private val metadata: BundledMetadata, private val compressed: ByteString) :
        BundledVerbDatabaseSource {
        override suspend fun metadata() = metadata

        override fun openCompressedDatabase(): Source = Buffer().write(compressed)
    }

    private companion object {
        const val FIXTURE_KEY = "fixture"
        const val BUNDLED = "bundled"
        const val LOCAL = "local"
        const val BUNDLED_AT = "2026-10-08T09:26:22Z"
        const val BUNDLE_BUILD = 1
        val SQLITE_HEADER = ByteString.of(*"SQLite format 3\u0000".encodeToByteArray())
    }
}
