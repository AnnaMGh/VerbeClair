package com.vamvisions.verbeclair.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.vamvisions.verbeclair.core.domain.verb.AuxiliaryFilter
import com.vamvisions.verbeclair.core.domain.verb.GroupBy
import com.vamvisions.verbeclair.core.domain.verb.ListChoices
import com.vamvisions.verbeclair.core.domain.verb.ListFilters
import com.vamvisions.verbeclair.core.domain.verb.ListTab
import com.vamvisions.verbeclair.core.domain.verb.SortOrder
import com.vamvisions.verbeclair.core.domain.verb.VerbGroup
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
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The remembered list choices on a real DataStore file (design D7, L-7, L-12). */
class DataStoreListPreferencesTest {
    private val fileSystem = FileSystem.SYSTEM
    private val directory: Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "settings-${Random.nextLong().toULong()}"
    private val file = (directory / "app_settings.preferences_pb").toString()

    private val choices = ListChoices(
        filters = ListFilters(
            groups = setOf(VerbGroup.I, VerbGroup.III),
            auxiliaries = setOf(AuxiliaryFilter.ETRE),
            reflexive = setOf(false),
        ),
        groupBy = GroupBy.GROUP,
        sort = SortOrder.MOST_FREQUENT,
    )

    @AfterTest
    fun deleteFiles() {
        fileSystem.deleteRecursively(directory)
    }

    @Test
    fun choicesAndTabAreReadBackFromANewDataStoreOnTheSameFile() = runTest {
        withDataStore { dataStore ->
            val preferences = DataStoreListPreferences(dataStore)
            preferences.setChoices(choices)
            preferences.setTab(ListTab.FAVOURITES)
        }

        withDataStore { dataStore ->
            val preferences = DataStoreListPreferences(dataStore)
            assertEquals(choices, preferences.choices.first())
            assertEquals(ListTab.FAVOURITES, preferences.tab.first())
        }
    }

    @Test
    fun onlyTheChoiceAndTabKeysAreStored() = runTest {
        withDataStore { dataStore ->
            val preferences = DataStoreListPreferences(dataStore)
            preferences.setChoices(choices)
            preferences.setTab(ListTab.ALL)

            val storedKeys = dataStore.data.first().asMap().keys.map { it.name }.toSet()
            assertEquals(
                setOf("list.filters.groups", "list.filters.auxiliaries", "list.filters.reflexive", "list.groupBy", "list.sort", "list.tab"),
                storedKeys,
            )
        }
    }

    @Test
    fun anEmptyFileGivesTheDefaults() = runTest {
        withDataStore { dataStore ->
            val preferences = DataStoreListPreferences(dataStore)
            assertEquals(ListChoices.Default, preferences.choices.first())
            assertEquals(ListTab.ALL, preferences.tab.first())
        }
    }

    @Test
    fun resetChoicesAreStoredAsDefaults() = runTest {
        withDataStore { dataStore ->
            val preferences = DataStoreListPreferences(dataStore)
            preferences.setChoices(choices)
            preferences.setChoices(ListChoices.Default)

            assertEquals(ListChoices.Default, preferences.choices.first())
        }
    }

    @Test
    fun unknownStoredValuesFallBackToTheDefaults() = runTest {
        withDataStore { dataStore ->
            dataStore.edit {
                it[stringSetPreferencesKey("list.filters.groups")] = setOf("I", "IV")
                it[stringSetPreferencesKey("list.filters.reflexive")] = setOf("maybe", "true")
                it[stringPreferencesKey("list.sort")] = "BY_LENGTH"
                it[stringPreferencesKey("list.tab")] = "RECENT"
            }
            val preferences = DataStoreListPreferences(dataStore)

            assertEquals(
                ListChoices(filters = ListFilters(groups = setOf(VerbGroup.I), reflexive = setOf(true))),
                preferences.choices.first(),
            )
            assertEquals(ListTab.ALL, preferences.tab.first())
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
