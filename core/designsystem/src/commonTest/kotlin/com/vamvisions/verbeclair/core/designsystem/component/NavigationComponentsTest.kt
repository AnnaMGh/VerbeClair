package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
