package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.room.RoomDatabase
import com.vamvisions.verbeclair.core.database.verb.VerbDatabase

/** An in-memory verb database builder for the platform the test runs on. */
expect fun inMemoryVerbDatabaseBuilder(): RoomDatabase.Builder<VerbDatabase>
