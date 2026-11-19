package com.vamvisions.verbeclair.core.data.di

/**
 * Absolute file paths of the app's local stores (design D12). The verb database is replaceable
 * (A-1); the user database and the app settings survive every verb-database install.
 */
interface PlatformPaths {
    val verbDatabaseFile: String
    val userDatabaseFile: String
    val dataStoreFile: String

    /** The first steps card's DataStore file. Backup and restore leave it out, so a reinstall clears it (OB-1). */
    val firstStepsFile: String
}
