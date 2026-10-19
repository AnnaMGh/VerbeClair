package com.vamvisions.verbeclair.di

import org.koin.core.KoinApplication

/** Adds what the platform needs to build the graph (the Android context on Android). */
expect fun KoinApplication.withTestPlatform()

/** Creates an empty temporary directory and returns its absolute path. */
expect fun createTestDirectory(): String

expect fun deleteTestDirectory(path: String)
