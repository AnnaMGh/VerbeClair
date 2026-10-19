package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class NavigationComponentsTest {

    private val labels = mapOf(
        TopLevelDestination.Verbs to "Verbs",
        TopLevelDestination.Tests to "Tests",
        TopLevelDestination.Settings to "Settings",
    )

    private fun assertOneSelectedAndTapsReported(
        bar: @Composable (TopLevelDestination, (TopLevelDestination) -> Unit) -> Unit,
    ) {
        TopLevelDestination.entries.forEach { selected ->
            runComposeUiTest {
                val reported = mutableListOf<TopLevelDestination>()
                setContent { VerbeClairTheme { bar(selected) { reported += it } } }
                onAllNodes(isSelected()).assertCountEquals(1)
                onNodeWithText(labels.getValue(selected))
                    .assertIsSelected()
                    .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
                TopLevelDestination.entries.forEach { onNodeWithText(labels.getValue(it)).performClick() }
                assertEquals<List<TopLevelDestination>>(TopLevelDestination.entries, reported)
            }
        }
    }

    // Bottom bar (task 4.1)

    @Test
    fun bottomBarHasExactlyOneSelectedItemAndReportsTaps() = assertOneSelectedAndTapsReported { selected, onSelect ->
        VerbeClairBottomBar(selected = selected, onSelect = onSelect, windowInsets = WindowInsets(0))
    }

    // Navigation rail (task 4.2)

    @Test
    fun navigationRailHasExactlyOneSelectedItemAndReportsTaps() =
        assertOneSelectedAndTapsReported { selected, onSelect ->
            VerbeClairNavigationRail(selected = selected, onSelect = onSelect, windowInsets = WindowInsets(0))
        }

    /** A start inset (a cutout in landscape) widens the rail and keeps its 80 dp item column, so labels stay whole. */
    @Test
    fun navigationRailGrowsByItsStartInset() = runComposeUiTest {
        setContent {
            VerbeClairTheme {
                VerbeClairNavigationRail(
                    selected = TopLevelDestination.Verbs,
                    onSelect = {},
                    modifier = Modifier.testTag("rail"),
                    windowInsets = WindowInsets(left = 40.dp),
                )
            }
        }

        onNodeWithTag("rail").assertWidthIsEqualTo(120.dp)
    }

    @Test
    fun navigationRailItemsStartBelowItsContentPadding() = runComposeUiTest {
        setContent {
            VerbeClairTheme {
                VerbeClairNavigationRail(
                    selected = TopLevelDestination.Verbs,
                    onSelect = {},
                    contentPadding = PaddingValues(top = 100.dp),
                    windowInsets = WindowInsets(0),
                )
            }
        }

        val top = onNodeWithText("Verbs").getBoundsInRoot().top
        assertTrue(top >= 100.dp, "Verbs label at $top")
    }

    @Test
    fun bottomBarGrowsByItsBottomInset() = runComposeUiTest {
        setContent {
            VerbeClairTheme {
                VerbeClairBottomBar(
                    selected = TopLevelDestination.Verbs,
                    onSelect = {},
                    modifier = Modifier.testTag("bar"),
                    windowInsets = WindowInsets(bottom = 20.dp),
                )
            }
        }

        onNodeWithTag("bar").assertHeightIsEqualTo(100.dp)
    }
}
