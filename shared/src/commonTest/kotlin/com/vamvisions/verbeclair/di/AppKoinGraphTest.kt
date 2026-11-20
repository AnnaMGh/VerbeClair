package com.vamvisions.verbeclair.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vamvisions.verbeclair.core.data.di.PlatformPaths
import com.vamvisions.verbeclair.core.domain.favourite.FavouritesRepository
import com.vamvisions.verbeclair.core.domain.preferences.FirstStepsPreferences
import com.vamvisions.verbeclair.core.domain.preferences.ListPreferences
import com.vamvisions.verbeclair.core.domain.score.TestScoreRepository
import com.vamvisions.verbeclair.core.domain.sync.DataSyncTrigger
import com.vamvisions.verbeclair.core.domain.verb.VerbCatalog
import com.vamvisions.verbeclair.feature.verblist.presentation.VerbListViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

/**
 * Resolves every public binding of [appModules] on the platform the test runs on (Android device
 * and iOS simulator), so a missing or mistyped definition fails before the app starts.
 */
class AppKoinGraphTest {

    private lateinit var directory: String
    private lateinit var app: KoinApplication

    @BeforeTest
    fun setUp() {
        directory = createTestDirectory()
        val testOverrides =
            module {
                single<PlatformPaths> {
                    object : PlatformPaths {
                        override val verbDatabaseFile = "$directory/verbs.db"
                        override val userDatabaseFile = "$directory/user.db"
                        override val dataStoreFile = "$directory/app_settings.preferences_pb"
                        override val firstStepsFile = "$directory/first_steps.preferences_pb"
                    }
                }
            }
        app =
            koinApplication {
                allowOverride(true)
                withTestPlatform()
                modules(appModules + testOverrides)
            }
    }

    @AfterTest
    fun tearDown() {
        app.close()
        deleteTestDirectory(directory)
    }

    /**
     * Closes the card through the real [FirstStepsPreferences] binding and checks that the flag did not
     * land in the backed-up app-settings store (OB-1). It reads the stores the app wires, so it fails if
     * the binding points at the wrong file.
     */
    @Test
    fun closedFlagDoesNotLandInTheAppSettingsStore() =
        runTest {
            app.koin.get<FirstStepsPreferences>().close()

            val appSettings =
                app.koin
                    .get<DataStore<Preferences>>()
                    .data
                    .first()
            assertTrue(appSettings.asMap().isEmpty())
        }

    @Test
    fun resolvesTheDataLayer() {
        app.koin.get<VerbCatalog>()
        app.koin.get<FavouritesRepository>()
        app.koin.get<TestScoreRepository>()
        app.koin.get<ListPreferences>()
        app.koin.get<FirstStepsPreferences>()
        app.koin.get<DataSyncTrigger>()
    }

    /**
     * The ViewModel is created in a [ViewModelStore] that is cleared before the temp directory is
     * deleted, because its `init` starts reading the databases.
     */
    @Test
    fun resolvesTheVerbListViewModel() {
        val store = ViewModelStore()
        try {
            ViewModelProvider.create(
                store,
                viewModelFactory { initializer { app.koin.get<VerbListViewModel>() } },
            )[VerbListViewModel::class]
        } finally {
            store.clear()
        }
    }

    /** The verb database opens only after the bundled one is installed from the packed resources (D4). */
    @Test
    fun verbCatalogReadsTheInstalledBundledDatabase() =
        runTest(timeout = 3.minutes) {
            assertFalse(
                app.koin
                    .get<VerbCatalog>()
                    .isEmpty()
                    .first(),
            )
        }
}
