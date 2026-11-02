package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.vamvisions.verbeclair.core.designsystem.icon.VerbeClairIcons
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class InputComponentsTest {

    // Search bar (task 2.1)

    @Test
    fun emptySearchBarShowsThePlaceholderAndNoClearButton() =
        runComposeUiTest {
            setContent { VerbeClairTheme { VerbeClairSearchBar(query = "", onQueryChange = {}) } }
            onNodeWithText("Search a verb…").assertExists()
            onNodeWithContentDescription("Clear search").assertDoesNotExist()
        }

    @Test
    fun clearButtonEmptiesAFilledSearchBar() =
        runComposeUiTest {
            var reported: String? = null
            setContent { VerbeClairTheme { VerbeClairSearchBar(query = "su", onQueryChange = { reported = it }) } }
            onNodeWithText("su").assertExists()
            onNodeWithContentDescription("Clear search").assertHasClickAction().performClick()
            assertEquals("", reported)
        }

    // Icon button (task 2.2)

    @Test
    fun badgeShowsTheCountAndTheLabelIncludesIt() =
        runComposeUiTest {
            setContent {
                VerbeClairTheme {
                    VerbeClairIconButton(
                        icon = VerbeClairIcons.Tune,
                        contentDescription = "List controls",
                        onClick = {},
                        style = IconButtonStyle.Tonal,
                        badgeCount = 2,
                    )
                }
            }
            val badge = onNodeWithText("2", useUnmergedTree = true).fetchSemanticsNode()
            assertTrue(
                SemanticsProperties.HideFromAccessibility in badge.config,
                "the count is read with the button label, not a second time on the badge",
            )
            onNodeWithContentDescription("List controls, 2").assertHasClickAction()
        }

    @Test
    fun noBadgeWithoutACount() =
        runComposeUiTest {
            setContent {
                VerbeClairTheme {
                    VerbeClairIconButton(
                        icon = VerbeClairIcons.Tune,
                        contentDescription = "List controls",
                        onClick = {},
                    )
                }
            }
            onNodeWithText("2", useUnmergedTree = true).assertDoesNotExist()
            onNodeWithContentDescription("List controls").assertHasClickAction()
        }

    // Button (task 2.3)

    @Test
    fun disabledButtonIgnoresTaps() =
        runComposeUiTest {
            var clicks = 0
            setContent {
                VerbeClairTheme { VerbeClairButton(text = "Clear filters", onClick = { clicks++ }, enabled = false) }
            }
            onNodeWithText("Clear filters").assertIsNotEnabled().performClick()
            assertEquals(0, clicks)
        }

    @Test
    fun enabledButtonReportsTaps() =
        runComposeUiTest {
            var clicks = 0
            setContent {
                VerbeClairTheme {
                    VerbeClairButton(text = "Reset", onClick = { clicks++ }, style = ButtonStyle.Text)
                }
            }
            onNodeWithText("Reset").performClick()
            assertEquals(1, clicks)
        }

    // Filter chip (task 2.4)

    @Test
    fun filterChipReflectsSelectionAndReportsTapsInBothStates() =
        runComposeUiTest {
            var clicks = 0
            setContent {
                VerbeClairTheme {
                    Column {
                        VerbeClairFilterChip(label = "1st group", selected = false, onClick = { clicks++ })
                        VerbeClairFilterChip(label = "être", selected = true, onClick = { clicks++ })
                    }
                }
            }
            onNodeWithText("1st group").assertIsNotSelected().performClick()
            onNodeWithText("être").assertIsSelected().performClick()
            assertEquals(2, clicks)
        }

    // Radio row (task 2.5)

    @Test
    fun radioRowIsOneSelectableRadioButton() =
        runComposeUiTest {
            var clicks = 0
            setContent { VerbeClairTheme { RadioRow(label = "A → Z", selected = true, onClick = { clicks++ }) } }
            onNodeWithText("A → Z")
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
                .assertIsSelected()
                .performClick()
            assertEquals(1, clicks)
        }

    @Test
    fun unselectedRadioRowReportsTapsOnItsLabel() =
        runComposeUiTest {
            var clicks = 0
            setContent {
                VerbeClairTheme {
                    RadioRow(
                        label = "Most frequent",
                        selected = false,
                        onClick = { clicks++ },
                    )
                }
            }
            onNodeWithText("Most frequent").assertIsNotSelected().performClick()
            assertEquals(1, clicks)
        }

    // List tabs (task 2.6)

    @Test
    fun favouritesTabShowsTheCountAndTapsAreReported() =
        runComposeUiTest {
            var reported: ListTab? = null
            setContent {
                VerbeClairTheme { ListTabs(selected = ListTab.All, favouritesCount = 12, onSelect = { reported = it }) }
            }
            onNodeWithText("All").assertIsSelected()
            onNodeWithText("Favourites (12)").assertIsNotSelected().performClick()
            assertEquals(ListTab.Favourites, reported)
        }
}
