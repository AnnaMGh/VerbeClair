package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.presentation.window.WindowLayout
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/** The phone landscape structure (task 7.3, Figma PL-01). */
@OptIn(ExperimentalTestApi::class)
class VerbListAdaptiveLayoutTest {

    private fun ComposeUiTest.showLandscape(
        state: VerbListState,
        rows: List<VerbListRowUi>,
        width: Dp = 772.dp,
    ) {
        setContent {
            VerbeClairTheme {
                // 852 × 393 dp minus the 80 dp rail drawn by the app shell.
                Box(Modifier.requiredSize(width, 393.dp)) {
                    VerbListScreen(
                        state = state,
                        verbs = previewItems(rows),
                        layout = WindowLayout.PhoneLandscape,
                        onAction = {},
                        onVerbClick = {},
                        onTestClick = {},
                    )
                }
            }
        }
    }

    @Test
    fun cellsFlowInTwoColumns() =
        runComposeUiTest {
            showLandscape(VerbListState(), PreviewData.default)

            val first = onNodeWithText("abaisser").getBoundsInRoot()
            val second = onNodeWithText("abandonner").getBoundsInRoot()
            val third = onNodeWithText("abattre").getBoundsInRoot()

            assertTrue(abs((first.top - second.top).value) < 1f, "abaisser and abandonner share a row")
            // Bounds are relative to the window; the forced 772 dp area may be offset in a narrower one.
            assertTrue(abs((second.left - first.left - 772.dp / 2).value) < 1f, "abandonner starts the second column")
            assertTrue(
                third.top > first.top && abs((third.left - first.left).value) < 1f,
                "abattre starts the next row",
            )
        }

    @Test
    fun sectionHeaderSpansBothColumns() =
        runComposeUiTest {
            showLandscape(VerbListState(choices = PreviewData.groupedChoices), PreviewData.grouped)

            val header = onNodeWithTag(VerbListTestTags.sectionHeader("group-I")).getBoundsInRoot()

            assertTrue(
                abs((header.right - header.left - 772.dp).value) < 1f,
                "Header width ${header.right - header.left}",
            )
            onNodeWithText("search a verb…", ignoreCase = true).assertExists()
            onNodeWithText("All").assertExists()
        }

    /** Figma PL-01 (87:9557): search, then the 272 dp tabs, in one row above the list. */
    @Test
    fun searchAndTabsShareTheTopRow() =
        runComposeUiTest {
            showLandscape(VerbListState(), PreviewData.default)

            val search = onNodeWithText("search a verb…", ignoreCase = true, useUnmergedTree = true).getBoundsInRoot()
            val all = onNodeWithText("All").getBoundsInRoot()

            assertTrue(
                all.top < search.bottom && search.top < all.bottom,
                "search $search and tabs $all overlap vertically",
            )
            assertTrue(all.left > search.right, "tabs follow the search")
            assertTrue(abs((all.right - all.left - 272.dp / 2).value) < 1f, "Tab width ${all.right - all.left}")
        }

    /** Figma PL-01 (87:9641): every cell of a line has the two-line height, so both columns' dividers line up. */
    @Test
    fun oneLineCellsTakeTheTwoLineHeight() =
        runComposeUiTest {
            // Wide enough that every meta line fits on one line, so only the two-line minimum sets the height.
            showLandscape(VerbListState(), PreviewData.default, width = 1600.dp)

            val cell = onNodeWithText("abaisser").getBoundsInRoot()

            assertTrue(cell.bottom - cell.top >= 90.dp, "Cell height ${cell.bottom - cell.top}")
        }
}
