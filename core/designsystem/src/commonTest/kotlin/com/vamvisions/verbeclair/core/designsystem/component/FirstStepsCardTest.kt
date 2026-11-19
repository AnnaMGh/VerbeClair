package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isGreaterThanOrEqualTo
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import kotlin.test.Test

/** The First steps card (Figma 151:248): texts, the three callbacks, the close label and the two alignments. */
@OptIn(ExperimentalTestApi::class)
class FirstStepsCardTest {
    private class Taps {
        var primary = 0
        var secondary = 0
        var close = 0
    }

    private fun cardTest(alignment: CardActionsAlignment = CardActionsAlignment.Start, block: ComposeUiTest.(Taps) -> Unit) =
        runComposeUiTest {
            val taps = Taps()
            setContent {
                VerbeClairTheme {
                    FirstStepsCard(
                        title = "New to French verbs?",
                        body = "Start with the most common ones.",
                        primaryLabel = "Test être",
                        secondaryLabel = "Most common first",
                        closeLabel = "Close tip",
                        onPrimaryClick = { taps.primary++ },
                        onSecondaryClick = { taps.secondary++ },
                        onCloseClick = { taps.close++ },
                        actionsAlignment = alignment,
                    )
                }
            }
            block(taps)
        }

    @Test
    fun showsTheTitleTheBodyTheTwoButtonsAndTheCloseLabel() =
        cardTest {
            onNodeWithText("New to French verbs?").assertExists()
            onNodeWithText("Start with the most common ones.").assertExists()
            onNodeWithText("Test être").assertExists()
            onNodeWithText("Most common first").assertExists()
            onNodeWithContentDescription("Close tip").assertExists()
        }

    @Test
    fun eachControlReportsOnlyItsOwnTap() =
        cardTest { taps ->
            onNodeWithText("Test être").performClick()
            assertThat(listOf(taps.primary, taps.secondary, taps.close)).isEqualTo(listOf(1, 0, 0))

            onNodeWithText("Most common first").performClick()
            assertThat(listOf(taps.primary, taps.secondary, taps.close)).isEqualTo(listOf(1, 1, 0))

            onNodeWithContentDescription("Close tip").performClick()
            assertThat(listOf(taps.primary, taps.secondary, taps.close)).isEqualTo(listOf(1, 1, 1))
        }

    /** The icon box is 34 dp in Figma; the close target around it is at least 44 dp. */
    @Test
    fun theCloseTargetIsAtLeast44dp() =
        cardTest {
            val touch = onNodeWithContentDescription("Close tip").fetchSemanticsNode().touchBoundsInRoot
            val minimum = with(density) { 44.dp.toPx() }

            assertThat(touch.width).isGreaterThanOrEqualTo(minimum)
            assertThat(touch.height).isGreaterThanOrEqualTo(minimum)
        }

    @Test
    fun theButtonsStartAtTheLeftEdgeByDefaultAndAreCentredOtherwise() {
        var startLeft = 0.dp
        var centreLeft = 0.dp
        cardTest(CardActionsAlignment.Start) { startLeft = onNodeWithText("Test être").getBoundsInRoot().left }
        cardTest(CardActionsAlignment.Center) { centreLeft = onNodeWithText("Test être").getBoundsInRoot().left }

        assertThat(centreLeft.value).isGreaterThan(startLeft.value)
    }
}
