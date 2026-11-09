package com.vamvisions.verbeclair.core.data.bundle

import androidx.room.Room
import androidx.room.RoomDatabase
import com.vamvisions.verbeclair.core.database.user.UserDatabase
import com.vamvisions.verbeclair.core.database.verb.VerbDatabase
import okio.SYSTEM

actual fun fileVerbDatabaseBuilder(path: String): RoomDatabase.Builder<VerbDatabase> = Room.databaseBuilder<VerbDatabase>(name = path)

actual fun testResourceFileOpener(): ResourceFileOpener = IosResourceFileOpener()

actual fun inMemoryVerbDatabaseBuilder(): RoomDatabase.Builder<VerbDatabase> = Room.inMemoryDatabaseBuilder<VerbDatabase>()

actual fun inMemoryUserDatabaseBuilder(): RoomDatabase.Builder<UserDatabase> = Room.inMemoryDatabaseBuilder<UserDatabase>()

actual fun largeTestFilesDirectory(): okio.Path = okio.FileSystem.SYSTEM_TEMPORARY_DIRECTORY
