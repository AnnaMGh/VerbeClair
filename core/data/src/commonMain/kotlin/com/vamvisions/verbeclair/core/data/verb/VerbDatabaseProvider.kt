package com.vamvisions.verbeclair.core.data.verb

import com.vamvisions.verbeclair.core.database.verb.VerbDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Opens the verb database once, on first use, after the bundled-database installer has run (design
 * D4). It suspends instead of blocking, so the screen shows its skeleton while a first-launch install
 * decompresses the bundled file.
 */
class VerbDatabaseProvider(private val open: suspend () -> VerbDatabase) {
    private val mutex = Mutex()
    private var database: VerbDatabase? = null

    suspend fun get(): VerbDatabase = database ?: mutex.withLock {
        database ?: open().also { database = it }
    }

    fun close() {
        database?.close()
    }
}
