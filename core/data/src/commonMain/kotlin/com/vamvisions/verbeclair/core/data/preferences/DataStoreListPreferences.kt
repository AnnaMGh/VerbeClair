package com.vamvisions.verbeclair.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.vamvisions.verbeclair.core.domain.preferences.ListPreferences
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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

/** A Preferences DataStore on the file at [path] (design D7). Only one may be active per file. */
fun createAppSettingsDataStore(
    path: String,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(scope = scope, produceFile = { path.toPath() })

/**
 * The verb-list choices and tab in the app-settings DataStore (design D7). Enum values are stored by
 * name; a value this version does not know is dropped, so the default is used. The search text is
 * never written.
 */
class DataStoreListPreferences(private val dataStore: DataStore<Preferences>) : ListPreferences {

    override val choices: Flow<ListChoices> = dataStore.data
        .map { preferences ->
            ListChoices(
                filters = ListFilters(
                    groups = preferences[Keys.GROUPS].orEmpty().mapNotNullTo(mutableSetOf()) { enumOrNull<VerbGroup>(it) },
                    auxiliaries = preferences[Keys.AUXILIARIES].orEmpty()
                        .mapNotNullTo(mutableSetOf()) { enumOrNull<AuxiliaryFilter>(it) },
                    reflexive = preferences[Keys.REFLEXIVE].orEmpty().mapNotNullTo(mutableSetOf()) { it.toBooleanStrictOrNull() },
                ),
                groupBy = enumOrNull<GroupBy>(preferences[Keys.GROUP_BY]) ?: ListChoices.Default.groupBy,
                sort = enumOrNull<SortOrder>(preferences[Keys.SORT]) ?: ListChoices.Default.sort,
            )
        }
        .distinctUntilChanged()

    override val tab: Flow<ListTab> = dataStore.data
        .map { preferences -> enumOrNull<ListTab>(preferences[Keys.TAB]) ?: ListTab.ALL }
        .distinctUntilChanged()

    override suspend fun setChoices(choices: ListChoices) {
        dataStore.edit { preferences ->
            preferences[Keys.GROUPS] = choices.filters.groups.mapTo(mutableSetOf()) { it.name }
            preferences[Keys.AUXILIARIES] = choices.filters.auxiliaries.mapTo(mutableSetOf()) { it.name }
            preferences[Keys.REFLEXIVE] = choices.filters.reflexive.mapTo(mutableSetOf()) { it.toString() }
            preferences[Keys.GROUP_BY] = choices.groupBy.name
            preferences[Keys.SORT] = choices.sort.name
        }
    }

    override suspend fun setTab(tab: ListTab) {
        dataStore.edit { preferences -> preferences[Keys.TAB] = tab.name }
    }

    internal object Keys {
        val GROUPS = stringSetPreferencesKey("list.filters.groups")
        val AUXILIARIES = stringSetPreferencesKey("list.filters.auxiliaries")
        val REFLEXIVE = stringSetPreferencesKey("list.filters.reflexive")
        val GROUP_BY = stringPreferencesKey("list.groupBy")
        val SORT = stringPreferencesKey("list.sort")
        val TAB = stringPreferencesKey("list.tab")
    }
}

private inline fun <reified T : Enum<T>> enumOrNull(name: String?): T? =
    enumValues<T>().firstOrNull { it.name == name }
