package com.vamvisions.verbeclair.core.database.dataset

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * `verbs-meta.json`, shipped next to the bundled `verbs.db` (A-1, A-2, design D3). The installer
 * compares it with the local copy's `metadata` without opening the bundled database.
 */
@Serializable
data class BundledMetadata(
    val lastUpdatedAt: String,
    val schemaVersion: Int,
    val bundleBuild: Int,
    val attribution: String,
    val datasetLicenseUrl: String,
    val sources: JsonElement,
)

/**
 * File names of the bundled database and its metadata, under the Compose resources `files/` folder.
 * The database is shipped gzip-compressed and decompressed by the installer (design D3, D4).
 */
object BundledFiles {
    const val COMPRESSED_DATABASE = "verbs.db.gzip"
    const val METADATA = "verbs-meta.json"
}
