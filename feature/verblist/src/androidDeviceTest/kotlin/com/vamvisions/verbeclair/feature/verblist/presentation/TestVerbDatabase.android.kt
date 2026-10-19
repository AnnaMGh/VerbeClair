package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.platform.app.InstrumentationRegistry
import com.vamvisions.verbeclair.core.database.verb.VerbDatabase

actual fun inMemoryVerbDatabaseBuilder(): RoomDatabase.Builder<VerbDatabase> =
    Room.inMemoryDatabaseBuilder<VerbDatabase>(InstrumentationRegistry.getInstrumentation().targetContext)
