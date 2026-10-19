package com.vamvisions.verbeclair.core.data.di

/**
 * Absolute file paths of the app's local stores (design D12). The verb database is replaceable
 * (A-1); the user database and the app settings survive every verb-database install.
 */
interface PlatformPaths {
    val verbDatabaseFile: String
    val userDatabaseFile: String
    val dataStoreFile: String
}
