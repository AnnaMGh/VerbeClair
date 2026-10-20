package com.vamvisions.verbeclair.core.data.verb

import androidx.paging.PagingSource
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.vamvisions.verbeclair.core.data.bundle.BundledVerbDatabaseInstaller
import com.vamvisions.verbeclair.core.data.bundle.ResourceBundledVerbDatabaseSource
import com.vamvisions.verbeclair.core.data.bundle.fileVerbDatabaseBuilder
import com.vamvisions.verbeclair.core.data.bundle.largeTestFilesDirectory
import com.vamvisions.verbeclair.core.data.bundle.testResourceFileOpener
import com.vamvisions.verbeclair.core.data.di.PlatformPaths
import com.vamvisions.verbeclair.core.database.verb.VerbDatabase
import com.vamvisions.verbeclair.core.database.verb.VerbRow
import com.vamvisions.verbeclair.core.domain.verb.ListQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path
import okio.SYSTEM
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * The N-2 budgets on the full bundled database (task 4.6), timed on the Room paging source the list
 * collects (count + first load of 3 pages):
 * - the first page right after the database is opened, cold, in under 1 s;
 * - a "su" search on the open database in under 100 ms (median of 5 runs after one warm-up; every
 *   run is printed for the PR).
 */
class BundledCatalogPerformanceTest {
    private val fileSystem = FileSystem.SYSTEM
    private val directory: Path = largeTestFilesDirectory() / "performance-${Random.nextLong().toULong()}"
    private val paths =
        object : PlatformPaths {
            override val verbDatabaseFile = (directory / "verbs.db").toString()
            override val userDatabaseFile = (directory / "user.db").toString()
            override val dataStoreFile = (directory / "settings.preferences_pb").toString()
        }
    private var database: VerbDatabase? = null

    @AfterTest
    fun deleteFiles() {
        database?.close()
        fileSystem.deleteRecursively(directory)
    }

    @Test
    fun firstPageAndSearchAreWithinBudget() =
        runTest(timeout = 3.minutes) {
            fileSystem.createDirectories(directory)
            BundledVerbDatabaseInstaller(paths, ResourceBundledVerbDatabaseSource(testResourceFileOpener()))
                .installIfNeeded()

            val (firstPage, firstPageTime) =
                measure {
                    val opened =
                        fileVerbDatabaseBuilder(paths.verbDatabaseFile)
                            .setDriver(BundledSQLiteDriver())
                            .setQueryCoroutineContext(Dispatchers.IO)
                            .build()
                            .also { database = it }
                    opened.firstLoad(ListQuery())
                }
            val opened = checkNotNull(database)
            opened.firstLoad(SEARCH) // warm-up
            val searchRuns = List(SEARCH_RUNS) { measure { opened.firstLoad(SEARCH) } }
            val searchTime = searchRuns.map { it.second }.sorted()[SEARCH_RUNS / 2]

            println(
                "Bundled catalogue timings: first page after open $firstPageTime; \"su\" search median $searchTime " +
                    "(runs ${searchRuns.joinToString { it.second.toString() }})",
            )
            assertEquals(INITIAL_LOAD, firstPage.data.size)
            assertEquals(
                "suis",
                searchRuns
                    .first()
                    .first.data
                    .single { it.slug == "etre" }
                    .matchedForm,
            )
            assertTrue(firstPageTime < 1.seconds, "First page took $firstPageTime (budget 1 s)")
            assertTrue(searchTime < 100.milliseconds, "\"su\" search took $searchTime (budget 100 ms)")
        }

    private suspend fun VerbDatabase.firstLoad(query: ListQuery): PagingSource.LoadResult.Page<Int, VerbRow> {
        val result =
            verbDao()
                .pagedList(ListQueryBuilder.build(query))
                .load(PagingSource.LoadParams.Refresh(key = null, loadSize = INITIAL_LOAD, placeholdersEnabled = false))
        return assertIs<PagingSource.LoadResult.Page<Int, VerbRow>>(result)
    }

    private suspend fun <T> measure(block: suspend () -> T): Pair<T, Duration> {
        val mark = TimeSource.Monotonic.markNow()
        val result = block()
        return result to mark.elapsedNow()
    }

    private companion object {
        /** Paging's default first load: three pages. */
        const val INITIAL_LOAD = RoomVerbCatalog.PAGE_SIZE * 3
        const val SEARCH_RUNS = 5
        val SEARCH = ListQuery(search = "su")
    }
}
