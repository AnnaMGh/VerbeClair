package com.vamvisions.verbeclair.core.data.bundle

import androidx.room.RoomDatabase
import com.vamvisions.verbeclair.core.database.user.UserDatabase
import com.vamvisions.verbeclair.core.database.verb.VerbDatabase

/** A Room builder for a verb database file at [path], on the platform the test runs on. */
expect fun fileVerbDatabaseBuilder(path: String): RoomDatabase.Builder<VerbDatabase>

/** The real [ResourceFileOpener] of the platform the test runs on. */
expect fun testResourceFileOpener(): ResourceFileOpener

/** An in-memory verb database builder for the platform the test runs on. */
expect fun inMemoryVerbDatabaseBuilder(): RoomDatabase.Builder<VerbDatabase>

/** An in-memory user database builder for the platform the test runs on. */
expect fun inMemoryUserDatabaseBuilder(): RoomDatabase.Builder<UserDatabase>

/**
 * A directory for full-size test databases. On Android it is not the cache directory, which the
 * system empties when storage runs low, as a 156 MB install can cause on an emulator.
 */
expect fun largeTestFilesDirectory(): okio.Path
