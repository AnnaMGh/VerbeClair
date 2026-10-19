package com.vamvisions.verbeclair.core.data.di

import androidx.room.RoomDatabase
import com.vamvisions.verbeclair.core.database.user.UserDatabase
import com.vamvisions.verbeclair.core.database.verb.VerbDatabase
import org.koin.core.scope.Scope

/** A Room builder for the verb database file at [path]; Android needs the context from Koin. */
internal expect fun Scope.verbDatabaseBuilder(path: String): RoomDatabase.Builder<VerbDatabase>

/** A Room builder for the user database file at [path]; Android needs the context from Koin. */
internal expect fun Scope.userDatabaseBuilder(path: String): RoomDatabase.Builder<UserDatabase>
