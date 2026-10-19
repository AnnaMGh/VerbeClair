package com.vamvisions.verbeclair.di

import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.KoinApplication
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID

actual fun KoinApplication.withTestPlatform() = Unit

@OptIn(ExperimentalForeignApi::class)
actual fun createTestDirectory(): String {
    val path = NSTemporaryDirectory() + "koin-graph-" + NSUUID().UUIDString
    NSFileManager.defaultManager.createDirectoryAtPath(path, withIntermediateDirectories = true, attributes = null, error = null)
    return path
}

@OptIn(ExperimentalForeignApi::class)
actual fun deleteTestDirectory(path: String) {
    NSFileManager.defaultManager.removeItemAtPath(path, error = null)
}
