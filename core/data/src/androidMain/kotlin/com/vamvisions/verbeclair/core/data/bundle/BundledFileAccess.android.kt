package com.vamvisions.verbeclair.core.data.bundle

import android.content.Context
import okio.Path
import okio.Source
import okio.source
import java.io.FileOutputStream

internal actual fun syncFile(path: Path) {
    FileOutputStream(path.toFile(), true).use { it.fd.sync() }
}

/** Android has no per-file backup flag; exclusion is done through the app's backup rules. */
internal actual fun excludeFromBackup(path: Path) = Unit

/** Opens Compose resources packed as Android assets (`file:///android_asset/…`), streamed. */
class AndroidResourceFileOpener(private val context: Context) : ResourceFileOpener {
    override fun open(uri: String): Source {
        require(uri.startsWith(ASSET_URI_PREFIX)) { "Not an Android asset URI: $uri" }
        return context.assets.open(uri.removePrefix(ASSET_URI_PREFIX)).source()
    }

    private companion object {
        const val ASSET_URI_PREFIX = "file:///android_asset/"
    }
}
