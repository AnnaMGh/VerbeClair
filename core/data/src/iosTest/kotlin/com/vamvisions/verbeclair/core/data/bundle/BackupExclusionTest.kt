package com.vamvisions.verbeclair.core.data.bundle

import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.SYSTEM
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class BackupExclusionTest {
    @OptIn(ExperimentalForeignApi::class)
    @Test
    fun installedFileIsExcludedFromICloudBackup() {
        val file = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "backup-${Random.nextLong().toULong()}.db"
        FileSystem.SYSTEM.write(file) { writeUtf8("verbs") }
        try {
            excludeFromBackup(file)

            // A fresh URL, so the value is read from the file and not from a cached URL object.
            val values = NSURL.fileURLWithPath(file.toString())
                .resourceValuesForKeys(listOf(NSURLIsExcludedFromBackupKey), error = null)
            assertEquals(true, (values?.get(NSURLIsExcludedFromBackupKey) as? NSNumber)?.boolValue)
        } finally {
            FileSystem.SYSTEM.delete(file)
        }
    }
}
