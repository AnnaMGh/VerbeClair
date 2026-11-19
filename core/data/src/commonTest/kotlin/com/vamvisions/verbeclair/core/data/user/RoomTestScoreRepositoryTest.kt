package com.vamvisions.verbeclair.core.data.user

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.vamvisions.verbeclair.core.data.bundle.inMemoryUserDatabaseBuilder
import com.vamvisions.verbeclair.core.database.user.UserDatabase
import com.vamvisions.verbeclair.core.database.user.entity.AttemptLineEntity
import com.vamvisions.verbeclair.core.database.user.entity.SelectedTense
import com.vamvisions.verbeclair.core.database.user.entity.TestAttemptEntity
import com.vamvisions.verbeclair.core.domain.score.AttemptKind
import com.vamvisions.verbeclair.core.domain.score.LineResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The last test score rule: latest attempt, test or review session (user-data spec, glossary *Last test score*, RV-4). */
class RoomTestScoreRepositoryTest {
    private val database: UserDatabase =
        inMemoryUserDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    private val repository = RoomTestScoreRepository(database)

    @AfterTest
    fun close() = database.close()

    @Test
    fun neverTestedVerbHasNoScore() =
        runTest {
            assertEquals(emptyMap(), scores("terminer"))
        }

    @Test
    fun latestAttemptCounts() =
        runTest {
            attempt("terminer", at = 1, correct = 6, failed = 4)
            attempt("terminer", at = 2, correct = 8, failed = 2)

            assertEquals(mapOf("terminer" to 80), scores("terminer"))
        }

    @Test
    fun scoreIsRoundedHalfUp() =
        runTest {
            attempt("terminer", at = 1, correct = 2, failed = 1)

            assertEquals(mapOf("terminer" to 67), scores("terminer"))
        }

    @Test
    fun laterReviewSessionSetsTheScore() =
        runTest {
            attempt("terminer", at = 1, correct = 8, failed = 2)
            attempt("terminer", at = 2, correct = 5, failed = 5, kind = AttemptKind.REVIEW)

            assertEquals(mapOf("terminer" to 50), scores("terminer"))
        }

    @Test
    fun reviewSessionAloneGivesAScore() =
        runTest {
            attempt("terminer", at = 1, correct = 5, failed = 5, kind = AttemptKind.REVIEW)

            assertEquals(mapOf("terminer" to 50), scores("terminer"))
        }

    @Test
    fun laterTestAfterAReviewSessionSetsTheScore() =
        runTest {
            attempt("terminer", at = 1, correct = 5, failed = 5, kind = AttemptKind.REVIEW)
            attempt("terminer", at = 2, correct = 8, failed = 2)

            assertEquals(mapOf("terminer" to 80), scores("terminer"))
        }

    @Test
    fun reviewSessionOfSeveralTensesCountsAsOneAttempt() =
        runTest {
            // One review session, two cards of avoir: Présent 5 of 6, Imparfait 4 of 6.
            database.attemptDao().insert(
                TestAttemptEntity(
                    slug = "avoir",
                    kind = AttemptKind.REVIEW.name,
                    startedAt = 1,
                    selectedTenses =
                        listOf(
                            SelectedTense("INDICATIF", "PRESENT"),
                            SelectedTense("INDICATIF", "IMPARFAIT"),
                        ),
                ),
                lines("PRESENT", correct = 5, failed = 1) + lines("IMPARFAIT", correct = 4, failed = 2),
            )

            assertEquals(mapOf("avoir" to 75), scores("avoir"))
        }

    @Test
    fun linesCorrectWithoutAccentsCountAsCorrect() =
        runTest {
            attempt("terminer", at = 1, correct = 9, noAccents = 1)

            assertEquals(mapOf("terminer" to 100), scores("terminer"))
        }

    @Test
    fun scoresAreKeptPerVerbAndOnlyForTheRequestedSlugs() =
        runTest {
            attempt("terminer", at = 1, correct = 8, failed = 2)
            attempt("etre", at = 2, correct = 1, failed = 1)
            attempt("aller", at = 3, correct = 3)

            assertEquals(mapOf("terminer" to 80, "etre" to 50), scores("terminer", "etre", "parler"))
        }

    @Test
    fun aNewAttemptUpdatesTheObservedScore() =
        runTest {
            attempt("terminer", at = 1, correct = 6, failed = 4)
            assertEquals(mapOf("terminer" to 60), scores("terminer"))

            attempt("terminer", at = 2, correct = 10)

            assertEquals(mapOf("terminer" to 100), scores("terminer"))
        }

    @Test
    fun noAttemptStoredMeansNoAttempt() =
        runTest {
            assertThat(repository.observeHasAnyAttempt().first()).isFalse()
        }

    @Test
    fun oneTestAttemptCounts() =
        runTest {
            attempt("terminer", at = 1, correct = 1)

            assertThat(repository.observeHasAnyAttempt().first()).isTrue()
        }

    @Test
    fun oneReviewAttemptCounts() =
        runTest {
            attempt("terminer", at = 1, correct = 1, kind = AttemptKind.REVIEW)

            assertThat(repository.observeHasAnyAttempt().first()).isTrue()
        }

    /** An import writes the same rows as a test, whatever the verb. */
    @Test
    fun anImportedAttemptOfAnyVerbCounts() =
        runTest {
            database.attemptDao().insert(
                TestAttemptEntity(
                    slug = "verb-from-another-device",
                    kind = AttemptKind.TEST.name,
                    startedAt = 1,
                    selectedTenses = listOf(SelectedTense("INDICATIF", "PRESENT")),
                ),
                emptyList(),
            )

            assertThat(repository.observeHasAnyAttempt().first()).isTrue()
        }

    @Test
    fun theFirstInsertIsObserved() =
        runTest {
            repository.observeHasAnyAttempt().test {
                assertThat(awaitItem()).isFalse()

                attempt("terminer", at = 1, correct = 1)

                assertThat(awaitItem()).isTrue()
                cancelAndIgnoreRemainingEvents()
            }
        }

    private suspend fun scores(vararg slugs: String) = repository.observeScores(slugs.toSet()).first()

    private suspend fun attempt(
        slug: String,
        at: Long,
        correct: Int = 0,
        noAccents: Int = 0,
        failed: Int = 0,
        kind: AttemptKind = AttemptKind.TEST,
    ) {
        database.attemptDao().insert(
            TestAttemptEntity(
                slug = slug,
                kind = kind.name,
                startedAt = at,
                selectedTenses = listOf(SelectedTense("INDICATIF", "PRESENT")),
            ),
            lines("PRESENT", correct, noAccents, failed),
        )
    }

    private fun lines(
        tense: String,
        correct: Int = 0,
        noAccents: Int = 0,
        failed: Int = 0,
    ): List<AttemptLineEntity> {
        val results =
            List(correct) { LineResult.CORRECT } +
                List(noAccents) { LineResult.CORRECT_NO_ACCENTS } +
                List(failed) { LineResult.FAILED }
        return results.mapIndexed { index, result ->
            AttemptLineEntity(
                attemptId = 0,
                moodCode = "INDICATIF",
                tenseCode = tense,
                person = "P$index",
                typedText = "termine",
                result = result.name,
            )
        }
    }
}
