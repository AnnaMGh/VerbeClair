package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.presentation.window.WindowLayout
import kotlin.test.Test
import kotlin.test.assertEquals

/** On Medium width, back first clears the list–detail selection (task 7.4, N-15). */
@OptIn(ExperimentalTestApi::class)
class VerbListBackTest {

    @Test
    fun backClearsTheSelectionOnMediumWidth() =
        runAndroidComposeUiTest<ComponentActivity> {
            var state by mutableStateOf(VerbListState(selectedSlug = "terminer"))
            setContent {
                VerbeClairTheme {
                    VerbListScreen(
                        state = state,
                        verbs = previewItems(PreviewData.tablet),
                        layout = WindowLayout.ListDetail,
                        onAction = {
                            if (it ==
                                VerbListAction.OnClearSelection
                            ) {
                                state = state.copy(selectedSlug = null)
                            }
                        },
                        onVerbClick = {},
                        onTestClick = {},
                        isMediumWidth = true,
                    )
                }
            }
            waitForIdle()

            runOnUiThread { activity!!.onBackPressedDispatcher.onBackPressed() }
            waitForIdle()

            assertEquals(null, state.selectedSlug)
        }

    @Test
    fun rootPassesTheMediumWidthToTheScreen() =
        runAndroidComposeUiTest<ComponentActivity> {
            val database = seededVerbDatabase()
            val viewModel = verbListViewModel(database)
            try {
                setContent {
                    VerbeClairTheme {
                        VerbListRoot(
                            onVerbClick = {},
                            onTestClick = {},
                            viewModel = viewModel,
                            layout = WindowLayout.ListDetail,
                            isMediumWidth = true,
                        )
                    }
                }
                waitUntil(timeoutMillis = 5_000) { onAllNodes(hasText("être")).fetchSemanticsNodes().isNotEmpty() }
                // The first steps card and the top 10 panel also show "être"; the tap goes to the cell in the list.
                onNode(hasText("être") and hasAnyAncestor(hasTestTag(VerbListTestTags.GRID))).performClick()
                assertEquals("etre", viewModel.state.value.selectedSlug)
                waitForIdle() // the back handler is enabled once the screen shows the selection

                runOnUiThread { activity!!.onBackPressedDispatcher.onBackPressed() }
                waitForIdle()

                assertEquals(null, viewModel.state.value.selectedSlug)
            } finally {
                database.close()
            }
        }
}
