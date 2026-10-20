package com.vamvisions.verbeclair.core.data.bundle

import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.Source
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.posix.O_RDWR
import platform.posix.close
import platform.posix.fsync
import platform.posix.open

// okio's FileHandle.flush() only calls fflush on iOS, so the fsync is done here.
@OptIn(ExperimentalForeignApi::class)
internal actual fun syncFile(path: Path) {
    val descriptor = open(path.toString(), O_RDWR)
    if (descriptor < 0) throw IOException("Cannot open $path to sync it")
    try {
        if (fsync(descriptor) != 0) throw IOException("fsync failed for $path")
    } finally {
        close(descriptor)
    }
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun excludeFromBackup(path: Path) {
    val excluded =
        NSURL
            .fileURLWithPath(path.toString())
            .setResourceValue(true, forKey = NSURLIsExcludedFromBackupKey, error = null)
    if (!excluded) throw IOException("Cannot exclude $path from iCloud backup")
}

/** Opens Compose resources packed in the app bundle (`file://…` URIs), streamed. */
class IosResourceFileOpener : ResourceFileOpener {
    override fun open(uri: String): Source {
        val path = requireNotNull(NSURL.URLWithString(uri)?.path) { "Not a file URI: $uri" }
        return FileSystem.SYSTEM.source(path.toPath())
    }
}
