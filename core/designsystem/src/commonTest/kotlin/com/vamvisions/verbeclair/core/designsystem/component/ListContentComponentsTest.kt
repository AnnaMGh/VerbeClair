package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.vamvisions.verbeclair.core.designsystem.icon.VerbeClairIcons
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ListContentComponentsTest {

    private class Taps {
        var cell = 0
        var test = 0
        var favourite = 0
    }

    private fun verbCellTest(
        isFavourite: Boolean = false,
        matchedForm: String? = null,
        score: String? = null,
        block: ComposeUiTest.(Taps) -> Unit,
    ) = runComposeUiTest {
        val taps = Taps()
        setContent {
            VerbeClairTheme {
                VerbCell(
                    infinitive = "être",
                    meta = "3rd group · avoir · non-reflexive · very common",
                    isFavourite = isFavourite,
                    onClick = { taps.cell++ },
                    onTestClick = { taps.test++ },
                    onFavouriteClick = { taps.favourite++ },
                    matchedForm = matchedForm,
                    score = score,
                )
            }
        }
        block(taps)
    }

    // Verb cell (task 3.1)

    @Test
    fun matchedFormFollowsTheInfinitiveInOneText() = verbCellTest(matchedForm = "suis") {
        onNodeWithText("être suis").assertExists()
    }

    @Test
    fun scoreIsShownOnlyWhenGiven() {
        verbCellTest(score = "80%") { onNodeWithText("80%").assertExists() }
        verbCellTest(score = null) { onNodeWithText("80%").assertDoesNotExist() }
    }

    @Test
    fun favouriteLabelFollowsTheFavouriteState() {
        verbCellTest(isFavourite = false) {
            onNodeWithContentDescription("Add to favourites").assertExists()
            onNodeWithContentDescription("Remove from favourites").assertDoesNotExist()
        }
        verbCellTest(isFavourite = true) {
            onNodeWithContentDescription("Remove from favourites").assertExists()
            onNodeWithContentDescription("Add to favourites").assertDoesNotExist()
        }
    }

    @Test
    fun buttonsReportOnlyTheirOwnTaps() = verbCellTest { taps ->
        onNodeWithContentDescription("Add to favourites").performClick()
        onNodeWithContentDescription("Start a test").performClick()
        assertEquals(1, taps.favourite)
        assertEquals(1, taps.test)
        assertEquals(0, taps.cell, "a button tap is never a cell tap")
        onNodeWithText("être").performClick()
        assertEquals(1, taps.cell)
        assertEquals(1, taps.favourite, "a cell tap is never a favourite tap")
        assertEquals(1, taps.test, "a cell tap is never a test tap")
    }

    // Section header (task 3.2)

    @Test
    fun sectionHeaderWithoutACountShowsOnlyTheUppercaseTitle() = runComposeUiTest {
        setContent { VerbeClairTheme { SectionHeader(title = "1st group") } }
        onNodeWithText("1ST GROUP").assertExists()
        onNodeWithText("verbs", substring = true).assertDoesNotExist()
    }

    @Test
    fun sectionHeaderShowsTheUppercaseTitleAndTheCount() = runComposeUiTest {
        setContent { VerbeClairTheme { SectionHeader(title = "1st group", count = "5,684 verbs") } }
        onNodeWithText("1ST GROUP").assertExists()
        onNodeWithText("5,684 verbs").assertExists()
    }

    // Skeleton (task 3.3)

    @Test
    fun skeletonIsHiddenFromScreenReaders() = runComposeUiTest {
        setContent { VerbeClairTheme { Box(Modifier.testTag(TAG)) { VerbCellSkeleton() } } }
        assertNothingForScreenReaders()
    }

    // Empty state (task 3.4)

    @Test
    fun emptyStateShowsTheActionOnlyWhenGiven() {
        runComposeUiTest {
            setContent {
                VerbeClairTheme {
                    EmptyState(
                        icon = VerbeClairIcons.SearchOff,
                        title = "No verbs match your search or filters",
                        body = "Try another spelling.",
                        action = { VerbeClairButton(text = "Clear filters", onClick = {}) },
                    )
                }
            }
            onNodeWithText("Clear filters").assertExists()
        }
        runComposeUiTest {
            setContent {
                VerbeClairTheme {
                    EmptyState(
                        icon = VerbeClairIcons.CloudDownload,
                        title = "No verbs yet",
                        body = "Connect to the internet to download them.",
                    )
                }
            }
            onNodeWithText("No verbs yet").assertExists()
            onNodeWithText("Connect to the internet to download them.").assertExists()
            onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.OnClick)).assertCountEquals(0)
        }
    }

    @Test
    fun selectedVerbCellIsAnnouncedAsSelected() = runComposeUiTest {
        setContent {
            VerbeClairTheme {
                Column {
                    VerbCell("terminer", "1st group", false, {}, {}, {}, selected = true)
                    VerbCell("tenir", "3rd group", false, {}, {}, {})
                }
            }
        }
        onNode(hasText("terminer") and SemanticsMatcher.expectValue(SemanticsProperties.Selected, true)).assertExists()
        onNode(hasText("tenir") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)).assertDoesNotExist()
    }

    @Test
    fun emptyStateWithoutBodyShowsOnlyTheTitle() = runComposeUiTest {
        setContent {
            VerbeClairTheme {
                EmptyState(
                    icon = VerbeClairIcons.BookOpen,
                    title = "Choose a verb to see its conjugation",
                    body = null,
                )
            }
        }
        onNodeWithText("Choose a verb to see its conjugation").assertExists()
        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true).assertCountEquals(1)
    }

    @Test
    fun emptyStateIconIsHiddenFromScreenReaders() = runComposeUiTest {
        setContent {
            VerbeClairTheme {
                EmptyState(
                    icon = VerbeClairIcons.SearchOff,
                    title = "Verb unavailable",
                    body = "Try again later.",
                    tone = EmptyStateTone.Error,
                )
            }
        }
        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription), useUnmergedTree = true)
            .assertCountEquals(0)
        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Role), useUnmergedTree = true)
            .assertCountEquals(0)
    }

    // Linear progress (task 3.5)

    @Test
    fun linearProgressIsAnIndeterminateProgressBar() = runComposeUiTest {
        setContent { VerbeClairTheme { VerbeClairLinearProgress() } }
        onAllNodes(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertCountEquals(1)
    }

    @Test
    fun sheetHandleIsHiddenFromScreenReaders() = runComposeUiTest {
        setContent { VerbeClairTheme { Box(Modifier.testTag(TAG)) { BottomSheetHandle() } } }
        assertNothingForScreenReaders()
    }

    /**
     * Asserts that nothing under the [TAG] container is exposed to screen readers. `background(color, shape)` adds a
     * `Shape` property for tests only; accessibility services never read it.
     */
    private fun ComposeUiTest.assertNothingForScreenReaders() {
        fun SemanticsNode.descendants(): List<SemanticsNode> = children.flatMap { listOf(it) + it.descendants() }
        val exposed = onNodeWithTag(TAG, useUnmergedTree = true).fetchSemanticsNode().descendants()
            .flatMap { node -> node.config.map { it.key } }
            .filter { it != SemanticsProperties.Shape }
        assertTrue(exposed.isEmpty(), "no semantics for screen readers, found $exposed")
    }

    private companion object {
        const val TAG = "container"
    }
}
