package com.vamvisions.verbeclair.core.data.user

import androidx.room.useReaderConnection
import androidx.room.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import com.vamvisions.verbeclair.core.data.bundle.inMemoryUserDatabaseBuilder
import com.vamvisions.verbeclair.core.data.bundle.inMemoryVerbDatabaseBuilder
import com.vamvisions.verbeclair.core.data.verb.RoomVerbCatalog
import com.vamvisions.verbeclair.core.database.user.UserDatabase
import com.vamvisions.verbeclair.core.database.verb.VerbDatabase
import com.vamvisions.verbeclair.core.database.verb.entity.VerbEntity
import com.vamvisions.verbeclair.core.domain.result.DataError
import com.vamvisions.verbeclair.core.domain.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Favourites across the user and verb databases (user-data spec, design D6). */
@OptIn(ExperimentalTime::class)
class RoomFavouritesRepositoryTest {
    private val verbDatabase: VerbDatabase =
        inMemoryVerbDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    private val userDatabase: UserDatabase =
        inMemoryUserDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    private val repository = RoomFavouritesRepository(userDatabase, RoomVerbCatalog(verbDatabase))

    @AfterTest
    fun close() {
        verbDatabase.close()
        userDatabase.close()
    }

    @Test
    fun addingAFavouriteShowsAtOnce() =
        runTest {
            insertVerbs("etre", "parler")
            repository.observeCount().test {
                assertEquals(0, awaitItem())

                repository.add("etre")

                assertEquals(1, awaitItem())
                assertEquals(setOf("etre"), repository.observeSlugs().first())
            }
        }

    @Test
    fun removingAFavouriteShowsAtOnce() =
        runTest {
            insertVerbs("etre", "parler")
            repository.add("etre")
            repository.add("parler")
            repository.observeCount().test {
                assertEquals(2, awaitItem())

                repository.remove("etre")

                assertEquals(1, awaitItem())
                assertEquals(setOf("parler"), repository.observeSlugs().first())
            }
        }

    @Test
    fun aFavouriteStoresWhenItWasAdded() =
        runTest {
            val addedAt = Instant.fromEpochMilliseconds(1_791_000_000_000)
            val clock =
                object : Clock {
                    override fun now() = addedAt
                }
            insertVerbs("etre")

            RoomFavouritesRepository(userDatabase, RoomVerbCatalog(verbDatabase), clock).add("etre")

            val stored =
                userDatabase.useReaderConnection { connection ->
                    connection.usePrepared("SELECT added_at FROM favourites WHERE slug = 'etre'") {
                        it.step()
                        it.getLong(0)
                    }
                }
            assertEquals(addedAt.toEpochMilliseconds(), stored)
        }

    @Test
    fun changesSucceedWithAUnitResult() =
        runTest {
            assertEquals(Result.Success(Unit), repository.add("etre"))
            assertEquals(Result.Success(Unit), repository.remove("etre"))
        }

    @Test
    fun aFailingDatabaseGivesALocalErrorInsteadOfThrowing() =
        runTest {
            userDatabase.useWriterConnection { connection ->
                connection.usePrepared("DROP TABLE favourites") { it.step() }
            }

            assertEquals(Result.Error(DataError.Local.UNKNOWN), repository.add("etre"))
            assertEquals(Result.Error(DataError.Local.UNKNOWN), repository.remove("etre"))
        }

    @Test
    fun addingTwiceKeepsOneFavourite() =
        runTest {
            insertVerbs("etre")
            repository.add("etre")
            repository.add("etre")

            assertEquals(1, repository.observeCount().first())
        }

    @Test
    fun favouriteOfAMissingVerbIsHiddenButStoredAndComesBack() =
        runTest {
            insertVerbs("etre")
            repository.add("etre")
            repository.add("gone")

            repository.observeSlugs().test {
                assertEquals(setOf("etre"), awaitItem())
                assertEquals(
                    listOf("etre", "gone"),
                    userDatabase
                        .favouriteDao()
                        .observeSlugs()
                        .first()
                        .sorted(),
                )

                insertVerbs("gone")

                assertEquals(setOf("etre", "gone"), awaitItem())
            }
            assertEquals(2, repository.observeCount().first())
        }

    private suspend fun insertVerbs(vararg slugs: String) {
        verbDatabase.verbDao().insertVerbs(
            slugs.map { slug ->
                VerbEntity(slug, slug, "I", false, "AVOIR", 1.0, "2026-10-08T09:26:22Z", slug, slug)
            },
        )
    }
}
