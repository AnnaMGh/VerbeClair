package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.domain.verb.ListTab
import com.vamvisions.verbeclair.core.presentation.window.WindowLayout
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Screen 1 in the Compact structure (task 7.1). */
@OptIn(ExperimentalTestApi::class)
class VerbListScreenTest {
    private val actions = mutableListOf<VerbListAction>()
    private val opened = mutableListOf<String>()
    private val tested = mutableListOf<String>()

    private fun ComposeUiTest.show(
        state: VerbListState,
        rows: List<VerbListRowUi>? = PreviewData.tablet,
        layout: WindowLayout = WindowLayout.Compact,
        loadingNextPage: Boolean = false,
    ) {
        setContent {
            VerbeClairTheme {
                VerbListScreen(
                    state = state,
                    verbs =
                        when {
                            rows == null -> previewLoadingItems()
                            loadingNextPage -> loadingNextPageItems(rows)
                            else -> previewItems(rows)
                        },
                    layout = layout,
                    onAction = { actions += it },
                    onVerbClick = { opened += it },
                    onTestClick = { tested += it },
                )
            }
        }
    }

    private fun cellOf(infinitive: String) = hasAnyAncestor(hasText(infinitive, substring = true))

    @Test
    fun tappingACellOpensThatVerb() =
        runComposeUiTest {
            show(VerbListState())

            onNodeWithText("terminer").performClick()

            assertEquals(listOf("terminer"), opened)
            assertTrue(tested.isEmpty())
        }

    @Test
    fun scoreShowsOnePercentSign() =
        runComposeUiTest {
            show(VerbListState())

            onNodeWithText("80%").assertExists()
            onNodeWithText("80%%").assertDoesNotExist()
        }

    @Test
    fun tappingTheTestButtonOnlyStartsATest() =
        runComposeUiTest {
            show(VerbListState())

            onTerminerButton("Start a test").performClick()

            assertEquals(listOf("terminer"), tested)
            assertTrue(opened.isEmpty())
            assertTrue(actions.none { it is VerbListAction.OnToggleFavourite })
        }

    @Test
    fun tappingTheStarOnlyTogglesTheFavourite() =
        runComposeUiTest {
            show(VerbListState())

            onTerminerButton("Add to favourites").performClick()

            assertEquals(listOf<VerbListAction>(VerbListAction.OnToggleFavourite("terminer")), actions)
            assertTrue(opened.isEmpty())
            assertTrue(tested.isEmpty())
        }

    @Test
    fun clearButtonEmptiesTheSearch() =
        runComposeUiTest {
            show(VerbListState(query = "su"), PreviewData.searchSu)

            onNodeWithContentDescription("Clear search").performClick()

            assertEquals(listOf<VerbListAction>(VerbListAction.OnQueryChange("")), actions)
        }

    @Test
    fun emptyResultOffersClearFilters() =
        runComposeUiTest {
            show(VerbListState(query = "xyz"), emptyList())

            onNodeWithText("No verbs match your search or filters").assertExists()
            onNodeWithText("Clear filters").performClick()

            assertEquals(listOf<VerbListAction>(VerbListAction.OnClearFilters), actions)
        }

    @Test
    fun listHasNoClearFiltersButton() =
        runComposeUiTest {
            show(VerbListState())

            onNodeWithText("Clear filters").assertDoesNotExist()
        }

    @Test
    fun noDataHasNoButton() =
        runComposeUiTest {
            show(VerbListState(isEmptyDatabase = true), emptyList())

            onNodeWithText("No verbs yet").assertExists()
            onNodeWithText("Clear filters").assertDoesNotExist()
        }

    @Test
    fun noFavouritesHasNoButton() =
        runComposeUiTest {
            show(VerbListState(tab = ListTab.FAVOURITES), emptyList())

            onNodeWithText("No favourites yet").assertExists()
            onNodeWithText("Clear filters").assertDoesNotExist()
        }

    @Test
    fun favouritesThatDoNotMatchShowTheEmptyResult() =
        runComposeUiTest {
            show(VerbListState(tab = ListTab.FAVOURITES, favouritesCount = 3, query = "xyz"), emptyList())

            onNodeWithText("No verbs match your search or filters").assertExists()
            onNodeWithText("No favourites yet").assertDoesNotExist()
        }

    @Test
    fun tappingATabSelectsIt() =
        runComposeUiTest {
            show(VerbListState(favouritesCount = 3))

            onNodeWithText("Favourites (3)").performClick()
            onNodeWithText("All").performClick()

            assertEquals(
                listOf(VerbListAction.OnTabSelect(ListTab.FAVOURITES), VerbListAction.OnTabSelect(ListTab.ALL)),
                actions.filterIsInstance<VerbListAction.OnTabSelect>(),
            )
        }

    @Test
    fun skeletonWaitsForTheDelay() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            show(VerbListState(), rows = null)

            mainClock.advanceTimeBy(100)
            onNodeWithTag(VerbListTestTags.SKELETON).assertDoesNotExist()

            mainClock.advanceTimeBy(100)
            onNodeWithTag(VerbListTestTags.SKELETON).assertExists()
        }

    @Test
    fun nextPageSkeletonWaitsForTheDelay() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            show(VerbListState(), rows = PreviewData.tablet.take(2), loadingNextPage = true)

            mainClock.advanceTimeBy(100)
            onAllNodesWithTag(VerbListTestTags.NEXT_PAGE_SKELETON).assertCountEquals(0)

            mainClock.advanceTimeBy(100)
            onAllNodesWithTag(VerbListTestTags.NEXT_PAGE_SKELETON).assertCountEquals(1)
        }

    @Test
    fun loadedListShowsNoSkeleton() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            show(VerbListState())

            mainClock.advanceTimeBy(300)
            onNodeWithTag(VerbListTestTags.SKELETON).assertDoesNotExist()
            onNodeWithText("terminer").assertExists()
        }

    @Test
    fun badgeCountsTheSelectedFilters() =
        runComposeUiTest {
            show(VerbListState(choices = PreviewData.groupedChoices))

            onNode(hasContentDescription("List controls, 2")).assertExists()
            onNodeWithText("By group").assertExists()
            onNodeWithText("Most frequent").performClick()

            assertEquals(1, actions.size)
            assertTrue(actions.single() is VerbListAction.OnRemoveChoice)
        }

    /** [rows] loaded, with the next page still loading. */
    @Composable
    private fun loadingNextPageItems(rows: List<VerbListRowUi>): LazyPagingItems<VerbListRowUi> {
        val start = LoadState.NotLoading(endOfPaginationReached = true)
        return flowOf(PagingData.from(rows, LoadStates(LoadState.NotLoading(false), start, LoadState.Loading)))
            .collectAsLazyPagingItems()
    }

    private fun ComposeUiTest.onTerminerButton(label: String) =
        onNode(hasContentDescription(label) and cellOf("terminer"))
}
