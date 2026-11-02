package com.vamvisions.verbeclair.core.data.di

import com.vamvisions.verbeclair.core.data.bundle.IosResourceFileOpener
import com.vamvisions.verbeclair.core.data.bundle.ResourceFileOpener
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathDirectory
import platform.Foundation.NSUserDomainMask

/**
 * The replaceable verb database lives in Application Support (excluded from iCloud backup by the
 * installer, A-1). The user's own data and settings live in Documents, which is backed up.
 */
class IosPlatformPaths : PlatformPaths {
    override val verbDatabaseFile: String = "${directory(NSApplicationSupportDirectory)}/$VERB_DATABASE_NAME"
    override val userDatabaseFile: String = "${directory(NSDocumentDirectory)}/$USER_DATABASE_NAME"
    override val dataStoreFile: String = "${directory(NSDocumentDirectory)}/$DATA_STORE_FILE_NAME"
}

@OptIn(ExperimentalForeignApi::class)
private fun directory(directory: NSSearchPathDirectory): String {
    val url =
        NSFileManager.defaultManager.URLForDirectory(
            directory = directory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )
    return requireNotNull(url?.path) { "Cannot resolve the iOS directory $directory" }
}

actual val platformModule: Module =
    module {
        single<PlatformPaths> { IosPlatformPaths() }
        single<ResourceFileOpener> { IosResourceFileOpener() }
    }
