package com.vamvisions.verbeclair.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.vamvisions.verbeclair.core.data.bundle.BundledVerbDatabaseInstaller
import com.vamvisions.verbeclair.core.data.bundle.BundledVerbDatabaseSource
import com.vamvisions.verbeclair.core.data.bundle.ResourceBundledVerbDatabaseSource
import com.vamvisions.verbeclair.core.data.preferences.DataStoreFirstStepsPreferences
import com.vamvisions.verbeclair.core.data.preferences.DataStoreListPreferences
import com.vamvisions.verbeclair.core.data.preferences.createAppSettingsDataStore
import com.vamvisions.verbeclair.core.data.sync.NoOpDataSyncTrigger
import com.vamvisions.verbeclair.core.data.user.RoomFavouritesRepository
import com.vamvisions.verbeclair.core.data.user.RoomTestScoreRepository
import com.vamvisions.verbeclair.core.data.verb.RoomVerbCatalog
import com.vamvisions.verbeclair.core.data.verb.VerbDatabaseProvider
import com.vamvisions.verbeclair.core.database.user.UserDatabase
import com.vamvisions.verbeclair.core.domain.favourite.FavouritesRepository
import com.vamvisions.verbeclair.core.domain.preferences.FirstStepsPreferences
import com.vamvisions.verbeclair.core.domain.preferences.ListPreferences
import com.vamvisions.verbeclair.core.domain.score.TestScoreRepository
import com.vamvisions.verbeclair.core.domain.sync.DataSyncTrigger
import com.vamvisions.verbeclair.core.domain.verb.VerbCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose

private val AppSettingsScope = named("appSettingsScope")

/** The local stores and repositories (design D12). Paths come from [PlatformPaths] in `platformModule`. */
val coreDataModule =
    module {
        single<BundledVerbDatabaseSource> { ResourceBundledVerbDatabaseSource(get()) }
        single { BundledVerbDatabaseInstaller(get(), get()) }

        // Opened on first use, only after the installer has run (D4).
        single {
            val installer = get<BundledVerbDatabaseInstaller>()
            val builder = verbDatabaseBuilder(get<PlatformPaths>().verbDatabaseFile)
            VerbDatabaseProvider {
                installer.installIfNeeded()
                builder.configured().build()
            }
        } onClose { it?.close() }

        single<UserDatabase> {
            userDatabaseBuilder(get<PlatformPaths>().userDatabaseFile).configured().build()
        } onClose { it?.close() }

        single(AppSettingsScope) { CoroutineScope(Dispatchers.IO + SupervisorJob()) } onClose { it?.cancel() }
        single<DataStore<Preferences>> {
            createAppSettingsDataStore(get<PlatformPaths>().dataStoreFile, get(AppSettingsScope))
        }

        // Repositories and preferences over those stores.
        single<VerbCatalog> { RoomVerbCatalog(get<VerbDatabaseProvider>()::get) }
        single<FavouritesRepository> { RoomFavouritesRepository(get(), get()) }
        singleOf(::RoomTestScoreRepository) { bind<TestScoreRepository>() }
        singleOf(::DataStoreListPreferences) { bind<ListPreferences>() }
        singleOf(::DataStoreFirstStepsPreferences) { bind<FirstStepsPreferences>() }

        singleOf(::NoOpDataSyncTrigger) { bind<DataSyncTrigger>() }
    }

private fun <T : RoomDatabase> RoomDatabase.Builder<T>.configured(): RoomDatabase.Builder<T> =
    setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO)
