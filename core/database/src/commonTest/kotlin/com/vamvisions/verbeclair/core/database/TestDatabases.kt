package com.vamvisions.verbeclair.core.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.vamvisions.verbeclair.core.database.verb.VerbDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/** An in-memory database builder for the platform the test runs on. */
expect fun inMemoryVerbDatabaseBuilder(): RoomDatabase.Builder<VerbDatabase>

fun inMemoryVerbDatabase(): VerbDatabase =
    inMemoryVerbDatabaseBuilder()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
