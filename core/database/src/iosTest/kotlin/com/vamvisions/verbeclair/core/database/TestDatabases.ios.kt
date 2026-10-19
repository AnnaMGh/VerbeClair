package com.vamvisions.verbeclair.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import com.vamvisions.verbeclair.core.database.verb.VerbDatabase

actual fun inMemoryVerbDatabaseBuilder(): RoomDatabase.Builder<VerbDatabase> = Room.inMemoryDatabaseBuilder<VerbDatabase>()
