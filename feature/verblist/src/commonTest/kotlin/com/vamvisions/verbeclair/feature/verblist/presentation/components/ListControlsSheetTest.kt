package com.vamvisions.verbeclair.feature.verblist.presentation.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.domain.verb.ListChoices
import com.vamvisions.verbeclair.feature.verblist.presentation.reduce
import kotlin.test.Test

/** The list controls sheet (task 7.2), with its choices changed by the ViewModel's reducer. */
@OptIn(ExperimentalTestApi::class)
class ListControlsSheetTest {

    private fun ComposeUiTest.showSheet() {
        setContent {
            var choices by remember { mutableStateOf(ListChoices.Default) }
            VerbeClairTheme {
                ListControlsContent(choices = choices, onAction = { action ->
                    choices.reduce(action)?.let { choices = it }
                })
            }
        }
    }

    /** A chip or radio row; field titles share some labels ("Auxiliary", "Reflexive") but are not clickable. */
    private fun ComposeUiTest.chip(label: String) = onNode(hasText(label) and hasClickAction()).performScrollTo()

    /** The Reflexive filter chip, which comes before the Group by option of the same name. */
    private fun ComposeUiTest.filterChip(label: String) = onAllNodes(hasText(label) and hasClickAction())[0].performScrollTo()

    @Test
    fun filterChipsAreMultiSelectWithinACategory() =
        runComposeUiTest {
            showSheet()

            chip("1st group").performClick()
            chip("2nd group").performClick()

            chip("1st group").assertIsSelected()
            chip("2nd group").assertIsSelected()
            chip("3rd group").assertIsNotSelected()

            chip("1st group").performClick()
            chip("1st group").assertIsNotSelected()
            chip("2nd group").assertIsSelected()
        }

    @Test
    fun auxiliaryAndReflexiveChipsToggle() =
        runComposeUiTest {
            showSheet()

            chip("avoir").performClick()
            chip("être").performClick()
            filterChip("Reflexive").performClick()
            chip("Non-reflexive").performClick()

            chip("avoir").assertIsSelected()
            chip("être").assertIsSelected()
            filterChip("Reflexive").assertIsSelected()
            chip("Non-reflexive").assertIsSelected()

            chip("avoir").performClick()
            filterChip("Reflexive").performClick()
            chip("avoir").assertIsNotSelected()
            filterChip("Reflexive").assertIsNotSelected()
        }

    @Test
    fun groupByIsSingleSelect() =
        runComposeUiTest {
            showSheet()
            chip("None").assertIsSelected()

            chip("Auxiliary").performClick()
            chip("None").assertIsNotSelected()
            chip("Auxiliary").assertIsSelected()

            chip("Group").performClick()
            chip("Auxiliary").assertIsNotSelected()
            chip("Group").assertIsSelected()
        }

    @Test
    fun sortIsSingleSelect() =
        runComposeUiTest {
            showSheet()
            chip("A → Z").assertIsSelected()

            chip("Most frequent").performClick()

            chip("Most frequent").assertIsSelected()
            chip("A → Z").assertIsNotSelected()
            chip("Z → A").assertIsNotSelected()
        }

    @Test
    fun resetRestoresTheDefaults() =
        runComposeUiTest {
            showSheet()
            chip("être").performClick()
            chip("Non-reflexive").performClick()
            chip("2nd group").performClick()
            chip("Auxiliary").performClick()
            chip("Z → A").performClick()

            onNodeWithText("Reset").performClick()

            chip("être").assertIsNotSelected()
            chip("Non-reflexive").assertIsNotSelected()
            chip("2nd group").assertIsNotSelected()
            chip("None").assertIsSelected()
            chip("A → Z").assertIsSelected()
        }
}
