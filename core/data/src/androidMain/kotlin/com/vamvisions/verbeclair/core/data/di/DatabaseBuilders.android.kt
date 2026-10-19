package com.vamvisions.verbeclair.core.data.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.vamvisions.verbeclair.core.database.user.UserDatabase
import com.vamvisions.verbeclair.core.database.verb.VerbDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.scope.Scope

internal actual fun Scope.verbDatabaseBuilder(path: String): RoomDatabase.Builder<VerbDatabase> =
    Room.databaseBuilder<VerbDatabase>(androidContext(), path)

internal actual fun Scope.userDatabaseBuilder(path: String): RoomDatabase.Builder<UserDatabase> =
    Room.databaseBuilder<UserDatabase>(androidContext(), path)
