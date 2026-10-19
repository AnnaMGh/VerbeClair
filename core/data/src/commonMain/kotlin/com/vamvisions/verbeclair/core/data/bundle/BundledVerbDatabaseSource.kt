package com.vamvisions.verbeclair.core.data.bundle

import com.vamvisions.verbeclair.core.data.generated.resources.Res
import com.vamvisions.verbeclair.core.database.dataset.BundledFiles
import com.vamvisions.verbeclair.core.database.dataset.BundledMetadata
import com.vamvisions.verbeclair.core.database.dataset.DatasetJson
import okio.Source
import org.jetbrains.compose.resources.ExperimentalResourceApi

/** The bundled verb database shipped with the app (design D3). */
interface BundledVerbDatabaseSource {
    /** The small `verbs-meta.json` next to the database. */
    suspend fun metadata(): BundledMetadata

    /** Opens the gzip-compressed database for streaming. The caller closes it. */
    fun openCompressedDatabase(): Source
}

/** Opens a packed Compose resource file, given its [Res.getUri], as a stream (D4). */
fun interface ResourceFileOpener {
    fun open(uri: String): Source
}

/** Reads the bundled files from the `:core:data` Compose resources. */
@OptIn(ExperimentalResourceApi::class)
class ResourceBundledVerbDatabaseSource(
    private val opener: ResourceFileOpener,
) : BundledVerbDatabaseSource {
    override suspend fun metadata(): BundledMetadata =
        DatasetJson.decodeFromString(Res.readBytes("files/${BundledFiles.METADATA}").decodeToString())

    // Never Res.readBytes: the database is 28 MB compressed and 156 MB once decompressed.
    override fun openCompressedDatabase(): Source =
        opener.open(Res.getUri("files/${BundledFiles.COMPRESSED_DATABASE}"))
}
