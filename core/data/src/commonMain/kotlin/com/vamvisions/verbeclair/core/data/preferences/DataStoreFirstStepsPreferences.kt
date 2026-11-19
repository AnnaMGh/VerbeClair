package com.vamvisions.verbeclair.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.vamvisions.verbeclair.core.domain.preferences.FirstStepsPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * The first steps card's closed flag, in a DataStore file of its own that backup and restore leave out, so a
 * reinstall clears it (OB-1). The app-settings file is backed up, so the flag must not live there.
 */
class DataStoreFirstStepsPreferences(private val dataStore: DataStore<Preferences>) : FirstStepsPreferences {

    override val closed: Flow<Boolean> =
        dataStore.data.map { it[Keys.CLOSED] ?: false }.distinctUntilChanged()

    override suspend fun close() {
        dataStore.edit { it[Keys.CLOSED] = true }
    }

    internal object Keys {

        val CLOSED = booleanPreferencesKey("firstSteps.closed")
    }
}
