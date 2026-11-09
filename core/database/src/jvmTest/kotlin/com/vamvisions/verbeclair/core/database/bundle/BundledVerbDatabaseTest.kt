package com.vamvisions.verbeclair.core.database.bundle

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.useReaderConnection
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.vamvisions.verbeclair.core.database.dataset.BundledFiles
import com.vamvisions.verbeclair.core.database.dataset.BundledMetadata
import com.vamvisions.verbeclair.core.database.dataset.DatasetJson
import com.vamvisions.verbeclair.core.database.inMemoryVerbDatabase
import com.vamvisions.verbeclair.core.database.verb.BUNDLE_BUILD
import com.vamvisions.verbeclair.core.database.verb.VERB_SCHEMA_VERSION
import com.vamvisions.verbeclair.core.database.verb.VerbDatabase
import com.vamvisions.verbeclair.core.database.verb.entity.MetadataKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import java.io.File
import java.util.zip.GZIPInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Checks the output of `buildBundledVerbDatabase` (design D3), which Gradle runs before this test. */
class BundledVerbDatabaseTest {
    private val filesDir =
        File(
            requireNotNull(System.getProperty("bundledVerbDatabaseDir")) {
                "bundledVerbDatabaseDir is not set; run the test through Gradle's jvmTest task"
            },
        )

    /** The shipped gzip, decompressed to a temporary file. */
    private val databaseFile: File =
        File.createTempFile("verbs", ".db").also { copy ->
            copy.deleteOnExit()
            GZIPInputStream(File(filesDir, BundledFiles.COMPRESSED_DATABASE).inputStream().buffered()).use { gzip ->
                copy.outputStream().use { gzip.copyTo(it) }
            }
        }

    @Test
    fun holdsEveryVerbOfDatasetV3() =
        runTest {
            val database =
                Room
                    .databaseBuilder<VerbDatabase>(name = databaseFile.absolutePath)
                    .setDriver(BundledSQLiteDriver())
                    .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                    .setQueryCoroutineContext(Dispatchers.IO)
                    .build()
            try {
                assertEquals(7_092, database.verbDao().count())
                val metadata = database.metadataDao()
                assertEquals("2026-10-08T09:26:22Z", metadata.get(MetadataKeys.LAST_UPDATED_AT))
                assertEquals(VERB_SCHEMA_VERSION.toString(), metadata.get(MetadataKeys.SCHEMA_VERSION))
                assertEquals(BUNDLE_BUILD.toString(), metadata.get(MetadataKeys.BUNDLE_BUILD))
                assertEquals("2026-10-08T09:26:22Z", metadata.get(MetadataKeys.CHECKPOINT))
            } finally {
                database.close()
            }
        }

    @Test
    fun schemaIdentityMatchesTheRuntimeDatabase() =
        runTest {
            val bundledHash = BundledSQLiteDriver().open(databaseFile.absolutePath).use { it.identityHash() }
            val runtime = inMemoryVerbDatabase()
            val runtimeHash =
                try {
                    runtime.verbDao().count() // opens the database, which writes Room's identity hash
                    runtime.useReaderConnection { connection ->
                        connection.usePrepared(IDENTITY_HASH_QUERY) {
                            it.step()
                            it.getText(0)
                        }
                    }
                } finally {
                    runtime.close()
                }
            assertEquals(runtimeHash, bundledHash)
        }

    /** The builder vacuums the database before compressing it (D3), so vacuuming it again saves nothing. */
    @Test
    fun shippedDatabaseIsVacuumed() {
        val vacuumed =
            File.createTempFile("verbs-vacuumed", ".db").apply {
                delete()
                deleteOnExit()
            }
        BundledSQLiteDriver().open(databaseFile.absolutePath).use { connection ->
            connection.execSQL("VACUUM INTO '${vacuumed.absolutePath}'")
        }

        assertEquals(vacuumed.length(), databaseFile.length())
    }

    @Test
    fun onlyTheBundledFilesAreShipped() {
        assertEquals(setOf(BundledFiles.COMPRESSED_DATABASE, BundledFiles.METADATA), filesDir.list()!!.toSet())
    }

    @Test
    fun metadataFileDescribesTheBundle() {
        val metadata = DatasetJson.decodeFromString<BundledMetadata>(File(filesDir, BundledFiles.METADATA).readText())
        assertEquals("2026-10-08T09:26:22Z", metadata.lastUpdatedAt)
        assertEquals(VERB_SCHEMA_VERSION, metadata.schemaVersion)
        assertEquals(BUNDLE_BUILD, metadata.bundleBuild)
        assertTrue(metadata.attribution.isNotBlank())
        assertEquals("https://creativecommons.org/licenses/by-sa/4.0/", metadata.datasetLicenseUrl)
    }

    private fun SQLiteConnection.identityHash(): String =
        prepare(IDENTITY_HASH_QUERY).use {
            it.step()
            it.getText(0)
        }

    private companion object {
        const val IDENTITY_HASH_QUERY = "SELECT identity_hash FROM room_master_table"
    }
}
