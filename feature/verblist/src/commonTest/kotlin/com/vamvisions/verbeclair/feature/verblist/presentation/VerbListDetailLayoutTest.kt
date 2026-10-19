package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.presentation.window.WindowLayout
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The tablet list–detail structure (task 7.4, Figma TP-01 / TL-01). */
@OptIn(ExperimentalTestApi::class)
class VerbListDetailLayoutTest {
    private val opened = mutableListOf<String>()

    /** 834 × 1194 dp minus the 80 dp rail; selection is kept in local state like the ViewModel does. */
    private fun ComposeUiTest.showTablet() {
        setContent {
            var state by remember { mutableStateOf(VerbListState()) }
            VerbeClairTheme {
                Box(Modifier.requiredSize(754.dp, 1150.dp)) {
                    VerbListScreen(
                        state = state,
                        verbs = previewItems(PreviewData.tablet),
                        layout = WindowLayout.ListDetail,
                        onAction = { action ->
                            when (action) {
                                is VerbListAction.OnSelectVerb -> state = state.copy(selectedSlug = action.slug)
                                VerbListAction.OnClearSelection -> state = state.copy(selectedSlug = null)
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

    private val selected = SemanticsMatcher.expectValue(SemanticsProperties.Selected, true)

    @Test
    fun tappingACellSelectsItAndOpensTheVerb() = runComposeUiTest {
        showTablet()
        onNode(hasText("terminer") and selected).assertDoesNotExist()

        onNodeWithText("terminer").performClick()

        onNode(hasText("terminer") and selected).assertExists()
        assertEquals(listOf("terminer"), opened)
    }

    @Test
    fun rightPaneShowsTheEmptyStateWithOrWithoutASelection() = runComposeUiTest {
        showTablet()
        val emptyState = hasText("Choose a verb to see its conjugation") and hasAnyAncestor(hasTestTag(VerbListTestTags.DETAIL_PANE))
        onNode(emptyState).assertExists()

        onNodeWithText("terminer").performClick()

        onNode(emptyState).assertExists()
    }

    @Test
    fun listPaneIs360DpWide() = runComposeUiTest {
        showTablet()

        val grid = onNodeWithTag(VerbListTestTags.GRID).getBoundsInRoot()

        assertTrue(abs((grid.right - grid.left - 360.dp).value) < 1f, "List pane width ${grid.right - grid.left}")
    }
}
