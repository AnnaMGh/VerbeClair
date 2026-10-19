package com.vamvisions.verbeclair.di

import androidx.test.platform.app.InstrumentationRegistry
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import java.io.File
import java.nio.file.Files

private val targetContext get() = InstrumentationRegistry.getInstrumentation().targetContext

actual fun KoinApplication.withTestPlatform() {
    androidContext(targetContext)
}

// Not the cache directory: the system empties it when storage runs low, as the 156 MB verb
// database install can cause on an emulator.
actual fun createTestDirectory(): String =
    Files.createTempDirectory(targetContext.filesDir.toPath(), "koin-graph").toFile().absolutePath

actual fun deleteTestDirectory(path: String) {
    File(path).deleteRecursively()
}
