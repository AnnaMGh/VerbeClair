package com.vamvisions.verbeclair.core.data.bundle

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.platform.app.InstrumentationRegistry
import com.vamvisions.verbeclair.core.database.user.UserDatabase
import com.vamvisions.verbeclair.core.database.verb.VerbDatabase
import okio.Path.Companion.toPath

private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

actual fun fileVerbDatabaseBuilder(path: String): RoomDatabase.Builder<VerbDatabase> =
    Room.databaseBuilder<VerbDatabase>(context, path)

actual fun testResourceFileOpener(): ResourceFileOpener = AndroidResourceFileOpener(context)

actual fun inMemoryVerbDatabaseBuilder(): RoomDatabase.Builder<VerbDatabase> =
    Room.inMemoryDatabaseBuilder<VerbDatabase>(context)

actual fun inMemoryUserDatabaseBuilder(): RoomDatabase.Builder<UserDatabase> =
    Room.inMemoryDatabaseBuilder<UserDatabase>(context)

actual fun largeTestFilesDirectory(): okio.Path = context.filesDir.absolutePath.toPath()
