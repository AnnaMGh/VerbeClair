package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.vamvisions.verbeclair.core.database.verb.VerbDatabase
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.presentation.window.WindowLayout
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The real ViewModel and catalogue over an in-memory verb database (task 7.5). The configuration
 * change on Android (save and restore) is covered by `VerbListRotationTest` in `androidDeviceTest`.
 */
@OptIn(ExperimentalTestApi::class)
class VerbListRootTest {
    private val database: VerbDatabase = seededVerbDatabase()
    private val viewModel = verbListViewModel(database)

    @AfterTest
    fun close() {
        database.close()
    }

    @Test
    fun typingSuShowsEtreWithSuis() = runComposeUiTest {
        setContent {
            VerbeClairTheme {
                VerbListRoot(onVerbClick = {}, onTestClick = {}, viewModel = viewModel, layout = WindowLayout.Compact)
            }
        }
        waitUntil(timeoutMillis = 5_000) { onAllNodes(hasText("verb00")).fetchSemanticsNodes().isNotEmpty() }

        onNode(hasSetTextAction()).performTextInput("su")

        waitUntil(timeoutMillis = 5_000) { onAllNodes(hasText("être suis")).fetchSemanticsNodes().isNotEmpty() }
        onNode(hasText("savoir su")).assertExists()
        onNode(hasText("suivre")).assertExists()
        onNode(hasText("verb00")).assertDoesNotExist()
    }

    @Test
    fun tapsAndTheLayoutReachTheScreen() = runComposeUiTest {
        val opened = mutableListOf<String>()
        val tested = mutableListOf<String>()
        setContent {
            VerbeClairTheme {
                VerbListRoot(
                    onVerbClick = { opened += it },
                    onTestClick = { tested += it },
                    viewModel = viewModel,
                    layout = WindowLayout.ListDetail,
                )
            }
        }
        waitUntil(timeoutMillis = 5_000) { onAllNodes(hasText("être")).fetchSemanticsNodes().isNotEmpty() }
        onNodeWithTag(VerbListTestTags.DETAIL_PANE).assertExists()

        onNode(hasText("être")).performClick()
        onNode(hasContentDescription("Start a test") and hasAnyAncestor(hasText("être"))).performClick()

        assertEquals(listOf("etre"), opened)
        assertEquals(listOf("etre"), tested)
    }

    /**
     * Rotating a phone switches between the Compact and PhoneLandscape structures. On iOS (and on an
     * Android resize without recreation) the composition is kept, so only the layout changes.
     */
    @Test
    fun switchingToLandscapeKeepsTheQueryAndTheFirstVisibleItem() = runComposeUiTest {
        var layout by mutableStateOf(WindowLayout.Compact)
        setContent {
            VerbeClairTheme {
                VerbListRoot(onVerbClick = {}, onTestClick = {}, viewModel = viewModel, layout = layout)
            }
        }
        waitUntil(timeoutMillis = 5_000) { onAllNodes(hasText("verb00")).fetchSemanticsNodes().isNotEmpty() }
        onNode(hasSetTextAction()).performTextInput("verb")
        waitUntil(timeoutMillis = 5_000) { onAllNodes(hasText("être")).fetchSemanticsNodes().isEmpty() }
        // An even index stays the first item of its line once the grid has 2 columns.
        onNodeWithTag(VerbListTestTags.GRID).performScrollToIndex(20)
        waitForIdle()
        onNode(hasText("verb20")).assertIsDisplayed()

        layout = WindowLayout.PhoneLandscape
        waitForIdle()

        assertEquals("verb", viewModel.state.value.query)
        onNode(hasSetTextAction() and hasText("verb")).assertExists()
        onNode(hasText("verb20")).assertIsDisplayed()
        onNode(hasText("verb00")).assertDoesNotExist()
    }
}
