package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.vamvisions.verbeclair.core.domain.verb.Auxiliary
import com.vamvisions.verbeclair.core.domain.verb.AuxiliaryFilter
import com.vamvisions.verbeclair.core.domain.verb.GroupBy
import com.vamvisions.verbeclair.core.domain.verb.ListChoices
import com.vamvisions.verbeclair.core.domain.verb.ListFilters
import com.vamvisions.verbeclair.core.domain.verb.ListTab
import com.vamvisions.verbeclair.core.domain.verb.SortOrder
import com.vamvisions.verbeclair.core.domain.verb.VerbGroup
import com.vamvisions.verbeclair.core.domain.verb.VerbListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class VerbListViewModelTest {
    private val catalog = FakeVerbCatalog(listOf(ARRIVER, ALLER))
    private val favourites = FakeFavouritesRepository()
    private val scores = FakeTestScoreRepository(mapOf("aller" to 92))
    private val preferences = FakeListPreferences()
    private val sync = FakeDataSyncTrigger()
    private val firstSteps = FakeFirstStepsPreferences()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = VerbListViewModel(catalog, favourites, scores, preferences, sync, firstSteps)

    /** Collects the rows as the screen does, so the list query runs. */
    private fun TestScope.collectRows(viewModel: VerbListViewModel) {
        backgroundScope.launch { viewModel.verbs.collect {} }
    }

    @Test
    fun typingWithinTheDebounceRunsOneQuery() =
        runTest {
            val viewModel = viewModel()
            collectRows(viewModel)
            advanceUntilIdle()

            viewModel.onAction(VerbListAction.OnQueryChange("s"))
            advanceTimeBy(100)
            viewModel.onAction(VerbListAction.OnQueryChange("su"))
            advanceTimeBy(100)
            viewModel.onAction(VerbListAction.OnQueryChange("sui"))
            advanceTimeBy(299)
            assertEquals(listOf(""), catalog.queries.map { it.search })

            advanceTimeBy(2)
            assertEquals(listOf("", "sui"), catalog.queries.map { it.search })
        }

    @Test
    fun restoredFavouritesTabWaitsForTheFavourites() =
        runTest {
            val slowFavourites = FakeFavouritesRepository(setOf("aller"), loaded = false)
            preferences.storedTab.value = ListTab.FAVOURITES
            val viewModel = VerbListViewModel(catalog, slowFavourites, scores, preferences, sync, firstSteps)
            collectRows(viewModel)
            advanceUntilIdle()
            assertTrue(catalog.queries.isEmpty())

            slowFavourites.loaded.value = true
            advanceUntilIdle()
            assertEquals(listOf(setOf("aller")), catalog.queries.map { it.favouriteSlugs })
        }

    @Test
    fun allTabDoesNotWaitForTheFavourites() =
        runTest {
            val slowFavourites = FakeFavouritesRepository(setOf("aller"), loaded = false)
            val viewModel = VerbListViewModel(catalog, slowFavourites, scores, preferences, sync, firstSteps)
            collectRows(viewModel)
            advanceUntilIdle()

            assertEquals(listOf(ListTab.ALL), catalog.queries.map { it.tab })
        }

    @Test
    fun activeChoicesAndBadgeFollowTheChoices() =
        runTest {
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.onAction(VerbListAction.OnToggleAuxiliary(AuxiliaryFilter.ETRE))
            viewModel.onAction(VerbListAction.OnToggleReflexive(false))
            viewModel.onAction(VerbListAction.OnGroupBySelect(GroupBy.GROUP))
            viewModel.onAction(VerbListAction.OnSortSelect(SortOrder.MOST_FREQUENT))
            assertEquals(2, viewModel.state.value.filterCount)
            assertEquals(
                listOf(
                    ActiveChoice.Auxiliary(AuxiliaryFilter.ETRE),
                    ActiveChoice.Reflexive(false),
                    ActiveChoice.Grouping(GroupBy.GROUP),
                    ActiveChoice.Sort(SortOrder.MOST_FREQUENT),
                ),
                viewModel.state.value.activeChoices,
            )

            viewModel.onAction(VerbListAction.OnRemoveChoice(ActiveChoice.Auxiliary(AuxiliaryFilter.ETRE)))
            assertEquals(1, viewModel.state.value.filterCount)
            assertEquals(3, viewModel.state.value.activeChoices.size)

            viewModel.onAction(VerbListAction.OnRemoveChoice(ActiveChoice.Sort(SortOrder.MOST_FREQUENT)))
            assertEquals(SortOrder.A_TO_Z, viewModel.state.value.choices.sort)
        }

    @Test
    fun sheetOpensAndCloses() =
        runTest {
            val viewModel = viewModel()

            viewModel.onAction(VerbListAction.OnOpenSheet)
            assertTrue(viewModel.state.value.isSheetOpen)

            viewModel.onAction(VerbListAction.OnDismissSheet)
            assertFalse(viewModel.state.value.isSheetOpen)
        }

    @Test
    fun selectionIsSetAndCleared() =
        runTest {
            val viewModel = viewModel()

            viewModel.onAction(VerbListAction.OnSelectVerb("aller"))
            assertEquals("aller", viewModel.state.value.selectedSlug)

            viewModel.onAction(VerbListAction.OnClearSelection)
            assertNull(viewModel.state.value.selectedSlug)
        }

    @Test
    fun resetRestoresTheDefaults() =
        runTest {
            val viewModel = viewModel()
            advanceUntilIdle()
            viewModel.onAction(VerbListAction.OnToggleGroup(VerbGroup.I))
            viewModel.onAction(VerbListAction.OnGroupBySelect(GroupBy.AUXILIARY))
            viewModel.onAction(VerbListAction.OnSortSelect(SortOrder.Z_TO_A))

            viewModel.onAction(VerbListAction.OnReset)

            assertEquals(ListChoices.Default, viewModel.state.value.choices)
            assertTrue(
                viewModel.state.value.activeChoices
                    .isEmpty(),
            )
            advanceUntilIdle()
            assertEquals(ListChoices.Default, preferences.storedChoices.value)
        }

    @Test
    fun clearFiltersKeepsGroupingAndSort() =
        runTest {
            val viewModel = viewModel()
            advanceUntilIdle()
            viewModel.onAction(VerbListAction.OnQueryChange("xyz"))
            viewModel.onAction(VerbListAction.OnToggleGroup(VerbGroup.II))
            viewModel.onAction(VerbListAction.OnToggleReflexive(true))
            viewModel.onAction(VerbListAction.OnGroupBySelect(GroupBy.GROUP))
            viewModel.onAction(VerbListAction.OnSortSelect(SortOrder.Z_TO_A))

            viewModel.onAction(VerbListAction.OnClearFilters)

            val state = viewModel.state.value
            assertEquals("", state.query)
            assertEquals(ListFilters(), state.choices.filters)
            assertEquals(GroupBy.GROUP, state.choices.groupBy)
            assertEquals(SortOrder.Z_TO_A, state.choices.sort)
        }

    @Test
    fun tabAndChoicesAreRestoredButNotTheSearch() =
        runTest {
            val stored = ListChoices(sort = SortOrder.MOST_FREQUENT, groupBy = GroupBy.REFLEXIVE)
            preferences.storedChoices.value = stored
            preferences.storedTab.value = ListTab.FAVOURITES

            val viewModel = viewModel()
            collectRows(viewModel)
            advanceUntilIdle()

            viewModel.state.test {
                val state = awaitItem()
                assertEquals(stored, state.choices)
                assertEquals(ListTab.FAVOURITES, state.tab)
                assertEquals("", state.query)
            }
            // The first query already uses the restored choices.
            assertEquals(stored, catalog.queries.first().choices)
        }

    @Test
    fun changesAreWrittenToThePreferences() =
        runTest {
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.onAction(VerbListAction.OnSortSelect(SortOrder.MOST_FREQUENT))
            viewModel.onAction(VerbListAction.OnTabSelect(ListTab.FAVOURITES))
            viewModel.onAction(VerbListAction.OnQueryChange("su"))
            advanceUntilIdle()

            assertEquals(SortOrder.MOST_FREQUENT, preferences.storedChoices.value.sort)
            assertEquals(ListTab.FAVOURITES, preferences.storedTab.value)
        }

    @Test
    fun togglingAFavouriteCallsTheRepository() =
        runTest {
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.onAction(VerbListAction.OnToggleFavourite("aller"))
            advanceUntilIdle()
            assertEquals(listOf("aller"), favourites.added)
            assertEquals(1, viewModel.state.value.favouritesCount)

            viewModel.onAction(VerbListAction.OnToggleFavourite("aller"))
            advanceUntilIdle()
            assertEquals(listOf("aller"), favourites.removed)
            assertEquals(0, viewModel.state.value.favouritesCount)
        }

    @Test
    fun aFailedFavouriteChangeEmitsAnEventAndKeepsTheStar() =
        runTest {
            favourites.failing = true
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.events.test {
                viewModel.onAction(VerbListAction.OnToggleFavourite("aller"))

                assertEquals(VerbListEvent.FavouriteUpdateFailed, awaitItem())
            }
            assertEquals(0, viewModel.state.value.favouritesCount)
        }

    @Test
    fun aFailedRemovalEmitsAnEventAndKeepsTheStarFilled() =
        runTest {
            favourites.slugs.value = setOf("aller")
            favourites.failing = true
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.events.test {
                viewModel.onAction(VerbListAction.OnToggleFavourite("aller"))

                assertEquals(VerbListEvent.FavouriteUpdateFailed, awaitItem())
            }
            assertEquals(1, viewModel.state.value.favouritesCount)
        }

    @Test
    fun aSuccessfulFavouriteChangeEmitsNoEvent() =
        runTest {
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.events.test {
                viewModel.onAction(VerbListAction.OnToggleFavourite("aller"))
                advanceUntilIdle()

                expectNoEvents()
            }
        }

    @Test
    fun emptyDatabaseRequestsOneSync() =
        runTest {
            catalog.empty.value = true
            val viewModel = viewModel()
            advanceUntilIdle()
            assertTrue(viewModel.state.value.isEmptyDatabase)

            catalog.empty.value = false
            advanceUntilIdle()
            catalog.empty.value = true
            advanceUntilIdle()

            assertEquals(1, sync.requests)
        }

    @Test
    fun anExistingDatabaseFillsTheTopVerbsFromTheCatalog() =
        runTest {
            val viewModel = viewModel()
            advanceUntilIdle()

            assertEquals(
                listOf("arriver", "aller"),
                viewModel.state.value.topVerbs
                    .map { it.slug },
            )
        }

    @Test
    fun topVerbsAreReadAgainWhenTheVerbsArriveAfterASync() =
        runTest {
            catalog.verbs = emptyList()
            catalog.empty.value = true
            val viewModel = viewModel()
            advanceUntilIdle()
            assertTrue(
                viewModel.state.value.topVerbs
                    .isEmpty(),
            )

            catalog.verbs = listOf(ARRIVER, ALLER)
            catalog.empty.value = false
            advanceUntilIdle()

            assertEquals(
                listOf("arriver", "aller"),
                viewModel.state.value.topVerbs
                    .map { it.slug },
            )
        }

    @Test
    fun anExistingDatabaseRequestsNoSync() =
        runTest {
            catalog.empty.value = false
            val viewModel = viewModel()
            advanceUntilIdle()

            assertFalse(viewModel.state.value.isEmptyDatabase)
            assertEquals(0, sync.requests)
        }

    @Test
    fun scoresAreReadOnlyForTheRowsOfTheCurrentQuery() =
        runTest {
            val viewModel = viewModel()
            collectRows(viewModel)
            advanceUntilIdle()
            viewModel.verbs.asSnapshot()
            advanceUntilIdle()
            assertEquals(setOf("arriver", "aller"), scores.requested.last())

            catalog.verbs = listOf(ALLER)
            viewModel.onAction(VerbListAction.OnQueryChange("all"))
            advanceTimeBy(400)
            viewModel.verbs.asSnapshot()
            advanceUntilIdle()

            assertEquals(setOf("aller"), scores.requested.last())
        }

    @Test
    fun rowsCarryFavouritesScoresAndSectionHeaders() =
        runTest {
            favourites.slugs.value = setOf("aller")
            val viewModel = viewModel()
            advanceUntilIdle()
            viewModel.onAction(VerbListAction.OnGroupBySelect(GroupBy.GROUP))
            collectRows(viewModel)
            advanceUntilIdle()
            // Scores are read for the rows loaded so far, so they arrive just after the first page.
            viewModel.verbs.asSnapshot()
            advanceUntilIdle()

            val rows = viewModel.verbs.asSnapshot()

            assertEquals(listOf("section-group-I", "arriver", "section-group-III", "aller"), rows.map { it.key })
            val aller = (rows.last() as VerbListRowUi.Verb).verb
            assertTrue(aller.isFavourite)
            assertEquals(92, aller.score)
        }

    private companion object {
        val ARRIVER = VerbListItem("arriver", "arriver", VerbGroup.I, false, Auxiliary.ETRE, 500.0)
        val ALLER = VerbListItem("aller", "aller", VerbGroup.III, false, Auxiliary.ETRE, 1_300.0)
    }

    @Test
    fun firstStepsAreHiddenUntilBothFlowsHaveEmitted() =
        runTest {
            assertFalse(VerbListState().showFirstSteps)
            assertFalse(VerbListState(firstStepsClosed = false).showFirstSteps)
            assertFalse(VerbListState(hasAnyAttempt = false).showFirstSteps)
            assertTrue(VerbListState(firstStepsClosed = false, hasAnyAttempt = false).showFirstSteps)
        }

    @Test
    fun freshInstallShowsTheFirstSteps() =
        runTest {
            val viewModel = viewModel()
            advanceUntilIdle()

            assertTrue(viewModel.state.value.showFirstSteps)
        }

    @Test
    fun searchingHidesTheFirstStepsAndClearingBringsThemBack() =
        runTest {
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.onAction(VerbListAction.OnQueryChange("su"))
            assertFalse(viewModel.state.value.showFirstSteps)
            viewModel.onAction(VerbListAction.OnClearQuery)
            assertTrue(viewModel.state.value.showFirstSteps)
        }

    @Test
    fun aFilterHidesTheFirstSteps() =
        runTest {
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.onAction(VerbListAction.OnToggleReflexive(false))
            assertFalse(viewModel.state.value.showFirstSteps)
        }

    @Test
    fun favouritesTabHidesTheFirstSteps() =
        runTest {
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.onAction(VerbListAction.OnTabSelect(ListTab.FAVOURITES))
            assertFalse(viewModel.state.value.showFirstSteps)
        }

    @Test
    fun emptyDatabaseHidesTheFirstSteps() =
        runTest {
            catalog.empty.value = true
            val viewModel = viewModel()
            advanceUntilIdle()

            assertFalse(viewModel.state.value.showFirstSteps)
        }

    @Test
    fun aClosedCardStaysHidden() =
        runTest {
            val closed = FakeFirstStepsPreferences(closed = true)
            val viewModel = VerbListViewModel(catalog, favourites, scores, preferences, sync, closed)
            advanceUntilIdle()

            assertFalse(viewModel.state.value.showFirstSteps)
        }

    @Test
    fun theFirstAttemptHidesTheFirstSteps() =
        runTest {
            val viewModel = viewModel()
            advanceUntilIdle()
            assertTrue(viewModel.state.value.showFirstSteps)

            scores.hasAny.value = true
            assertFalse(viewModel.state.value.showFirstSteps)
        }

    @Test
    fun importedAttemptsHideTheFirstSteps() =
        runTest {
            val imported = FakeTestScoreRepository(hasAnyAttempt = true)
            val viewModel = VerbListViewModel(catalog, favourites, imported, preferences, sync, firstSteps)
            advanceUntilIdle()

            assertFalse(viewModel.state.value.showFirstSteps)
        }

    @Test
    fun groupingAndSortHideTheFirstStepsUntilTheirChipsAreRemoved() =
        runTest {
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.onAction(VerbListAction.OnGroupBySelect(GroupBy.GROUP))
            assertFalse(viewModel.state.value.showFirstSteps)
            viewModel.onAction(VerbListAction.OnRemoveChoice(ActiveChoice.Grouping(GroupBy.GROUP)))
            assertTrue(viewModel.state.value.showFirstSteps)

            viewModel.onAction(VerbListAction.OnSortSelect(SortOrder.Z_TO_A))
            assertFalse(viewModel.state.value.showFirstSteps)
            viewModel.onAction(VerbListAction.OnReset)
            assertTrue(viewModel.state.value.showFirstSteps)
        }

    @Test
    fun closingTheCardStoresTheFlagAndHidesIt() =
        runTest {
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.onAction(VerbListAction.OnFirstStepsClose)
            advanceUntilIdle()

            assertTrue(firstSteps.storedClosed.value)
            assertFalse(viewModel.state.value.showFirstSteps)
        }

    @Test
    fun testEtreRaisesTheTestSetupEventForEtre() =
        runTest {
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.events.test {
                viewModel.onAction(VerbListAction.OnFirstStepsTestEtre)
                assertEquals(VerbListEvent.OpenTestSetup("etre"), awaitItem())
            }
            assertNull(viewModel.state.value.selectedSlug)
        }

    @Test
    fun mostCommonFirstSortsByFrequencyAndHidesTheCardUntilTheChipIsRemoved() =
        runTest {
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.onAction(VerbListAction.OnFirstStepsMostCommon)

            assertEquals(SortOrder.MOST_FREQUENT, viewModel.state.value.choices.sort)
            assertEquals(listOf(ActiveChoice.Sort(SortOrder.MOST_FREQUENT)), viewModel.state.value.activeChoices)
            assertEquals(SortOrder.MOST_FREQUENT, preferences.storedChoices.value.sort)
            assertFalse(viewModel.state.value.showFirstSteps)

            viewModel.onAction(VerbListAction.OnRemoveChoice(ActiveChoice.Sort(SortOrder.MOST_FREQUENT)))
            assertTrue(viewModel.state.value.showFirstSteps)
        }
}
