package com.vamvisions.verbeclair.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.platform.app.InstrumentationRegistry
import com.vamvisions.verbeclair.core.database.verb.VerbDatabase

private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

actual fun inMemoryVerbDatabaseBuilder(): RoomDatabase.Builder<VerbDatabase> = Room.inMemoryDatabaseBuilder<VerbDatabase>(context)
