package com.vamvisions.verbeclair.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path
import okio.SYSTEM
import kotlin.random.Random
import kotlin.test.Test

/** The first steps card's closed flag on a real DataStore file (user-data spec, OB-1). */
class DataStoreFirstStepsPreferencesTest {
    private val fileSystem = FileSystem.SYSTEM
    private val directory: Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "first-steps-${Random.nextLong().toULong()}"
    private val file = (directory / "app_settings.preferences_pb").toString()

    @Test
    fun anEmptyFileMeansNotClosed() =
        runTest {
            inFreshDirectory {
                withDataStore { assertThat(DataStoreFirstStepsPreferences(it).closed.first()).isFalse() }
            }
        }

    @Test
    fun closedSurvivesANewDataStoreOnTheSameFile() =
        runTest {
            inFreshDirectory {
                withDataStore { DataStoreFirstStepsPreferences(it).close() }

                withDataStore { assertThat(DataStoreFirstStepsPreferences(it).closed.first()).isTrue() }
            }
        }

    @Test
    fun theFlagHasItsOwnKeyApartFromTheListChoices() =
        runTest {
            inFreshDirectory {
                withDataStore { dataStore ->
                    DataStoreFirstStepsPreferences(dataStore).close()

                    val keys =
                        dataStore.data
                            .first()
                            .asMap()
                            .keys
                            .map { it.name }
                            .toSet()
                    assertThat(keys).isEqualTo(setOf("firstSteps.closed"))
                }
            }
        }

    private suspend fun inFreshDirectory(block: suspend () -> Unit) {
        try {
            block()
        } finally {
            fileSystem.deleteRecursively(directory)
        }
    }

    /** Opens a DataStore on [file] and closes it afterwards, so another one may open the same file. */
    private suspend fun withDataStore(block: suspend (DataStore<Preferences>) -> Unit) {
        val job = Job()
        try {
            block(createAppSettingsDataStore(file, CoroutineScope(Dispatchers.IO + job)))
        } finally {
            job.cancelAndJoin()
        }
    }
}
