package com.vamvisions.verbeclair.core.data.di

import com.vamvisions.verbeclair.core.data.bundle.IosResourceFileOpener
import com.vamvisions.verbeclair.core.data.bundle.ResourceFileOpener
import com.vamvisions.verbeclair.core.data.bundle.excludeFromBackup
import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathDirectory
import platform.Foundation.NSUserDomainMask

/**
 * The replaceable verb database lives in Application Support (excluded from iCloud backup by the
 * installer, A-1). The user's own data and settings live in Documents, which is backed up. The first steps
 * flag lives in an Application Support folder that is excluded from iCloud backup, so a reinstall clears it.
 */
class IosPlatformPaths : PlatformPaths {
    override val verbDatabaseFile: String = "${directory(NSApplicationSupportDirectory)}/$VERB_DATABASE_NAME"
    override val userDatabaseFile: String = "${directory(NSDocumentDirectory)}/$USER_DATABASE_NAME"
    override val dataStoreFile: String = "${directory(NSDocumentDirectory)}/$DATA_STORE_FILE_NAME"
    override val firstStepsFile: String = "${notBackedUpDirectory()}/$FIRST_STEPS_FILE_NAME"
}

private fun notBackedUpDirectory(): String {
    val path = "${directory(NSApplicationSupportDirectory)}/$FIRST_STEPS_DIRECTORY_NAME"
    FileSystem.SYSTEM.createDirectories(path.toPath())
    excludeFromBackup(path.toPath())
    return path
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

private const val FIRST_STEPS_DIRECTORY_NAME = "FirstSteps"

actual val platformModule: Module =
    module {
        single<PlatformPaths> { IosPlatformPaths() }
        single<ResourceFileOpener> { IosResourceFileOpener() }
    }
