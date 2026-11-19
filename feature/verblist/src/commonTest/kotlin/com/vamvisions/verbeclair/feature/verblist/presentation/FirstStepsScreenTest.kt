package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isLessThan
import assertk.assertions.isLessThanOrEqualTo
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.domain.verb.GroupBy
import com.vamvisions.verbeclair.core.domain.verb.ListChoices
import com.vamvisions.verbeclair.core.domain.verb.ListTab
import com.vamvisions.verbeclair.core.domain.verb.SortOrder
import com.vamvisions.verbeclair.core.presentation.window.WindowLayout
import kotlin.math.abs
import kotlin.test.Test

/** The first steps card on phone portrait, phone landscape and tablet, and the tablet top 10 panel (OB-1). */
@OptIn(ExperimentalTestApi::class)
class FirstStepsScreenTest {
    private val actions = mutableListOf<VerbListAction>()
    private val opened = mutableListOf<String>()

    private val shown = VerbListState(favouritesCount = 12, firstStepsClosed = false, hasAnyAttempt = false)
    private val tabletShown = shown.copy(topVerbs = PreviewData.topVerbs)

    /** The screen state, kept here like the ViewModel does, so a tap changes the right pane and a test can clear it. */
    private var current by mutableStateOf(VerbListState())

    private fun ComposeUiTest.show(
        state: VerbListState,
        layout: WindowLayout = WindowLayout.Compact,
        rows: List<VerbListRowUi>? = PreviewData.default,
        fontScale: Float = 1f,
    ) {
        val (width, height) =
            when (layout) {
                WindowLayout.Compact -> 393.dp to 728.dp
                WindowLayout.PhoneLandscape -> 772.dp to 393.dp
                else -> 754.dp to 1150.dp
            }
        current = state
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                VerbeClairTheme {
                    Box(Modifier.requiredSize(width, height)) {
                        VerbListScreen(
                            state = current,
                            verbs = if (rows == null) previewLoadingItems() else previewItems(rows),
                            layout = layout,
                            onAction = { action ->
                                actions += action
                                when (action) {
                                    is VerbListAction.OnSelectVerb -> current = current.copy(selectedSlug = action.slug)
                                    VerbListAction.OnClearSelection -> current = current.copy(selectedSlug = null)
                                    else -> Unit
                                }
                            },
                            onVerbClick = { opened += it },
                            onTestClick = {},
                        )
                    }
                }
            }
        }
    }

    private val card = hasTestTag(FirstStepsTestTags.CARD)

    private fun ComposeUiTest.assertCardAbove(text: String) {
        val cardBottom = onNode(card).getBoundsInRoot().bottom
        val rowTop = onNodeWithText(text).getBoundsInRoot().top
        assertThat(cardBottom, "card bottom above $text").isLessThanOrEqualTo(rowTop)
    }

    @Test
    fun phonePortraitShowsTheCardAboveTheFirstVerb() =
        runComposeUiTest {
            show(shown)

            onNode(card).assertExists()
            onNodeWithText("New to French verbs?").assertExists()
            onNodeWithText("Start with the most common ones.").assertExists()
            assertCardAbove("abaisser")
        }

    @Test
    fun theButtonsAndTheCloseButtonSendTheirActions() =
        runComposeUiTest {
            show(shown)

            onNodeWithText("Test être").performClick()
            onNodeWithText("Most common first").performClick()
            onNodeWithContentDescription("Close tip").performClick()

            assertThat(actions).isEqualTo(
                listOf(
                    VerbListAction.OnFirstStepsTestEtre,
                    VerbListAction.OnFirstStepsMostCommon,
                    VerbListAction.OnFirstStepsClose,
                ),
            )
            assertThat(opened).isEmpty()
        }

    @Test
    fun theCardIsHiddenWhenTheRuleIsNotMet() =
        runComposeUiTest {
            show(shown.copy(firstStepsClosed = true))

            onNode(card).assertDoesNotExist()
        }

    @Test
    fun theCardIsHiddenInTheNoDataEmptyResultAndSkeletonStates() {
        runComposeUiTest {
            show(shown.copy(isEmptyDatabase = true), rows = emptyList())
            onNode(card).assertDoesNotExist()
        }
        runComposeUiTest {
            show(shown, rows = emptyList())
            onNode(card).assertDoesNotExist()
        }
        runComposeUiTest {
            show(shown, rows = null)
            onNode(card).assertDoesNotExist()
        }
    }

    @Test
    fun theCardIsHiddenInTheFavouritesTab() =
        runComposeUiTest {
            show(shown.copy(tab = ListTab.FAVOURITES), rows = PreviewData.favourites)

            onNode(card).assertDoesNotExist()
        }

    @Test
    fun theCardScrollsAwayWithTheList() =
        runComposeUiTest {
            show(shown)
            onNode(card).assertExists()

            onNodeWithTag(VerbListTestTags.GRID).performScrollToIndex(8)

            onNode(card).assertDoesNotExist()
        }

    @Test
    fun aGroupedListOrASortedListHidesTheCard() {
        runComposeUiTest {
            show(shown.copy(choices = ListChoices(groupBy = GroupBy.GROUP)), rows = PreviewData.grouped)
            onNode(card).assertDoesNotExist()
        }
        runComposeUiTest {
            show(shown.copy(choices = ListChoices(sort = SortOrder.MOST_FREQUENT)))
            onNode(card).assertDoesNotExist()
        }
    }

    @Test
    fun phoneLandscapeSpansBothColumnsWithCentredButtons() =
        runComposeUiTest {
            show(shown, layout = WindowLayout.PhoneLandscape)

            val grid = onNodeWithTag(VerbListTestTags.GRID).getBoundsInRoot()
            val cardBounds = onNode(card).getBoundsInRoot()
            val cardWidth = cardBounds.right - cardBounds.left
            val gridWidth = grid.right - grid.left
            assertThat(cardWidth, "card width spans the grid").isGreaterThan(gridWidth * 0.9f)
            assertCardAbove("abaisser")

            val first = onNodeWithText("Test être").getBoundsInRoot()
            val second = onNodeWithText("Most common first").getBoundsInRoot()
            val buttonsCentre = (first.left + second.right) / 2
            // The actions row sits inside the card padding (16 dp start, 8 dp end), so its centre is 4 dp to the right.
            val contentCentre = (cardBounds.left + cardBounds.right) / 2 + 4.dp
            assertThat(abs((buttonsCentre - contentCentre).value), "buttons are centred").isLessThan(2f)

            val title = onNodeWithText("New to French verbs?").getBoundsInRoot()
            assertThat(title.left - cardBounds.left, "title stays left-aligned").isLessThan(24.dp)
        }

    @Test
    fun phonePortraitButtonsAreLeftAligned() =
        runComposeUiTest {
            show(shown)

            val cardBounds = onNode(card).getBoundsInRoot()
            val first = onNodeWithText("Test être").getBoundsInRoot()
            assertThat(first.left - cardBounds.left, "first button starts at the left").isLessThan(48.dp)
        }

    @Test
    fun tabletShowsTheCardAndTheTopTenPanel() =
        runComposeUiTest {
            show(tabletShown, layout = WindowLayout.ListDetail, rows = PreviewData.tablet)

            onNode(card).assertExists()
            onNodeWithText("Does any of these catch your eye?").assertExists()
            PreviewData.topVerbs.forEach { verb ->
                onNode(hasTestTag(FirstStepsTestTags.panelVerb(verb.slug))).assertExists()
            }
            onNodeWithText("Choose a verb to see its conjugation").assertDoesNotExist()
        }

    @Test
    fun tappingAPanelVerbSelectsItAndOpensItsDetails() =
        runComposeUiTest {
            show(tabletShown, layout = WindowLayout.ListDetail, rows = PreviewData.tablet)

            onNode(hasTestTag(FirstStepsTestTags.panelVerb("aller"))).performClick()

            assertThat(opened).isEqualTo(listOf("aller"))
            assertThat(actions).isEqualTo(listOf<VerbListAction>(VerbListAction.OnSelectVerb("aller")))
            onNode(hasTestTag(FirstStepsTestTags.PANEL)).assertDoesNotExist()
            onNodeWithText("Choose a verb to see its conjugation").assertExists()
        }

    @Test
    fun tappingAListCellReplacesThePanel() =
        runComposeUiTest {
            show(tabletShown, layout = WindowLayout.ListDetail, rows = PreviewData.tablet)

            onNodeWithText("terminer").performClick()

            onNode(hasTestTag(FirstStepsTestTags.PANEL)).assertDoesNotExist()
            onNode(card).assertExists()
        }

    @Test
    fun clearingTheSelectionBringsThePanelBack() =
        runComposeUiTest {
            show(tabletShown.copy(selectedSlug = "terminer"), layout = WindowLayout.ListDetail, rows = PreviewData.tablet)
            onNode(hasTestTag(FirstStepsTestTags.PANEL)).assertDoesNotExist()

            runOnIdle { current = current.copy(selectedSlug = null) }

            onNode(hasTestTag(FirstStepsTestTags.PANEL)).assertExists()
        }

    @Test
    fun aClosedCardLeavesTheEmptyStateInTheRightPane() =
        runComposeUiTest {
            show(tabletShown.copy(firstStepsClosed = true), layout = WindowLayout.ListDetail, rows = PreviewData.tablet)

            onNode(card).assertDoesNotExist()
            onNode(hasTestTag(FirstStepsTestTags.PANEL)).assertDoesNotExist()
            onNode(
                hasText("Choose a verb to see its conjugation") and hasAnyAncestor(hasTestTag(VerbListTestTags.DETAIL_PANE)),
            ).assertExists()
        }

    @Test
    fun aSearchHidesTheCardAndThePanel() =
        runComposeUiTest {
            show(tabletShown.copy(query = "su"), layout = WindowLayout.ListDetail, rows = PreviewData.searchSu)

            onNode(card).assertDoesNotExist()
            onNode(hasTestTag(FirstStepsTestTags.PANEL)).assertDoesNotExist()
            onNodeWithText("Choose a verb to see its conjugation").assertExists()
        }

    @Test
    fun largeFontKeepsTheTitleClearOfTheCloseButton() =
        runComposeUiTest {
            show(shown, fontScale = 2f)

            val title = onNodeWithText("New to French verbs?").getBoundsInRoot()
            val close = onNodeWithContentDescription("Close tip").getBoundsInRoot()
            // The close button's 48 dp target overflows its 34 dp box by 7 dp on each side.
            assertThat(title.right, "title ends before the close button").isLessThanOrEqualTo(close.left + 8.dp)
        }
}
