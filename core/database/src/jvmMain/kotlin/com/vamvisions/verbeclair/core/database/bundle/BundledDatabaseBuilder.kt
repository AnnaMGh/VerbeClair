@file:OptIn(ExperimentalSerializationApi::class)

package com.vamvisions.verbeclair.core.database.bundle

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.vamvisions.verbeclair.core.database.dataset.BundledFiles
import com.vamvisions.verbeclair.core.database.dataset.BundledMetadata
import com.vamvisions.verbeclair.core.database.dataset.DatasetFile
import com.vamvisions.verbeclair.core.database.dataset.DatasetJson
import com.vamvisions.verbeclair.core.database.dataset.VerbDetailsRecord
import com.vamvisions.verbeclair.core.database.verb.BUNDLE_BUILD
import com.vamvisions.verbeclair.core.database.verb.MetadataEntity
import com.vamvisions.verbeclair.core.database.verb.MetadataKeys
import com.vamvisions.verbeclair.core.database.verb.VERB_SCHEMA_VERSION
import com.vamvisions.verbeclair.core.database.verb.VerbDatabase
import com.vamvisions.verbeclair.core.database.verb.toEntities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Builds the bundled verb database on the build machine (design D3), with the same [VerbDatabase]
 * definition and the same dataset → entity mapping the app uses at runtime.
 *
 * Arguments: the dataset `.json.gz` file and the output folder, which receives
 * [BundledFiles.COMPRESSED_DATABASE] and [BundledFiles.METADATA].
 */
fun main(args: Array<String>) {
    require(args.size == 2) { "Usage: BundledDatabaseBuilder <dataset.json.gz> <output dir>" }
    buildBundledDatabase(dataset = File(args[0]), outputDir = File(args[1]))
}

private const val BATCH_SIZE = 200

fun buildBundledDatabase(dataset: File, outputDir: File) {
    // Files from an earlier build would otherwise be packed as resources too.
    outputDir.deleteRecursively()
    outputDir.mkdirs()
    // Built uncompressed next to the output, then shipped only as its gzip (design D3).
    val databaseFile = File(outputDir, "verbs.db")
    deleteDatabaseFiles(databaseFile, includingDatabase = true)

    val database = Room.databaseBuilder<VerbDatabase>(name = databaseFile.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

    val header = try {
        val batch = ArrayList<VerbDetailsRecord>(BATCH_SIZE)
        fun flush() {
            if (batch.isEmpty()) return
            val entities = batch.map { it.toEntities() }
            runBlocking { database.verbDao().replace(entities) }
            batch.clear()
        }
        val strategy = StreamingDatasetStrategy { verb ->
            batch += verb
            if (batch.size == BATCH_SIZE) flush()
        }
        val header = GZIPInputStream(dataset.inputStream().buffered()).use { DatasetJson.decodeFromStream(strategy, it) }
        flush()
        runBlocking {
            database.metadataDao().put(
                listOf(
                    MetadataEntity(MetadataKeys.LAST_UPDATED_AT, header.lastUpdatedAt),
                    MetadataEntity(MetadataKeys.SCHEMA_VERSION, VERB_SCHEMA_VERSION.toString()),
                    MetadataEntity(MetadataKeys.BUNDLE_BUILD, BUNDLE_BUILD.toString()),
                    MetadataEntity(MetadataKeys.CHECKPOINT, header.lastUpdatedAt),
                ),
            )
        }
        header
    } finally {
        database.close()
    }

    BundledSQLiteDriver().open(databaseFile.absolutePath).use { it.execSQL("VACUUM") }
    GZIPOutputStream(File(outputDir, BundledFiles.COMPRESSED_DATABASE).outputStream().buffered()).use { gzip ->
        databaseFile.inputStream().use { it.copyTo(gzip) }
    }
    // Only the gzip is shipped: no database, no journal, and no `.lck` file left by Room on the JVM.
    deleteDatabaseFiles(databaseFile, includingDatabase = true)

    val metadata = BundledMetadata(
        lastUpdatedAt = header.lastUpdatedAt,
        schemaVersion = VERB_SCHEMA_VERSION,
        bundleBuild = BUNDLE_BUILD,
        attribution = header.attribution,
        datasetLicenseUrl = header.datasetLicenseUrl,
        sources = header.sources,
    )
    File(outputDir, BundledFiles.METADATA).outputStream().use { MetadataJson.encodeToStream(metadata, it) }
}

private val MetadataJson = Json { prettyPrint = true }

private fun deleteDatabaseFiles(databaseFile: File, includingDatabase: Boolean) {
    val suffixes = listOf("-journal", "-wal", "-shm", ".lck") + if (includingDatabase) listOf("") else emptyList()
    suffixes.forEach { File(databaseFile.path + it).delete() }
}

/** The dataset fields other than `verbs`. */
private class DatasetHeader(
    val lastUpdatedAt: String,
    val attribution: String,
    val datasetLicenseUrl: String,
    val sources: JsonElement,
)

/**
 * Decodes [DatasetFile] but hands each verb to [onVerb] as soon as it is read, so the 220 MB
 * dataset is never held in memory at once.
 */
private class StreamingDatasetStrategy(
    private val onVerb: (VerbDetailsRecord) -> Unit,
) : DeserializationStrategy<DatasetHeader> {
    override val descriptor: SerialDescriptor = DatasetFile.serializer().descriptor

    override fun deserialize(decoder: Decoder): DatasetHeader = decoder.decodeStructure(descriptor) {
        var lastUpdatedAt: String? = null
        var attribution: String? = null
        var datasetLicenseUrl: String? = null
        var sources: JsonElement? = null
        while (true) {
            val index = decodeElementIndex(descriptor)
            if (index == CompositeDecoder.DECODE_DONE) break
            when (descriptor.getElementName(index)) {
                "lastUpdatedAt" -> lastUpdatedAt = decodeStringElement(descriptor, index)
                "attribution" -> attribution = decodeStringElement(descriptor, index)
                "datasetLicenseUrl" -> datasetLicenseUrl = decodeStringElement(descriptor, index)
                "sources" -> sources = decodeSerializableElement(descriptor, index, JsonElement.serializer())
                "verbs" -> decodeSerializableElement(descriptor, index, StreamingVerbs(onVerb))
                // Fields this builder does not need are still read, to keep the stream in sync.
                else -> decodeSerializableElement(descriptor, index, JsonElement.serializer())
            }
        }
        DatasetHeader(
            lastUpdatedAt = requireNotNull(lastUpdatedAt) { "Dataset has no lastUpdatedAt" },
            attribution = requireNotNull(attribution) { "Dataset has no attribution" },
            datasetLicenseUrl = requireNotNull(datasetLicenseUrl) { "Dataset has no datasetLicenseUrl" },
            sources = requireNotNull(sources) { "Dataset has no sources" },
        )
    }
}

/** Reads the `verbs` array element by element. */
private class StreamingVerbs(
    private val onVerb: (VerbDetailsRecord) -> Unit,
) : DeserializationStrategy<Unit> {
    private val verbSerializer = VerbDetailsRecord.serializer()
    override val descriptor: SerialDescriptor = ListSerializer(verbSerializer).descriptor

    override fun deserialize(decoder: Decoder) = decoder.decodeStructure(descriptor) {
        while (true) {
            val index = decodeElementIndex(descriptor)
            if (index == CompositeDecoder.DECODE_DONE) break
            onVerb(decodeSerializableElement(descriptor, index, verbSerializer))
        }
    }
}
