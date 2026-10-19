package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.StateRestorationTester
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.vamvisions.verbeclair.core.database.verb.VerbDatabase
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.presentation.window.WindowLayout
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Rotating an Android phone is a configuration change: the composition is rebuilt from saved state
 * while the ViewModel survives, and the layout switches to PhoneLandscape (task 7.5). Android only,
 * because `StateRestorationTester` is not implemented on iOS, where rotation keeps the composition.
 */
@OptIn(ExperimentalTestApi::class)
class VerbListRotationTest {
    private val database: VerbDatabase = seededVerbDatabase()
    private val viewModel = verbListViewModel(database)

    @AfterTest
    fun close() {
        database.close()
    }

    @Test
    fun rotatingKeepsTheQueryAndTheFirstVisibleItem() = runAndroidComposeUiTest<ComponentActivity> {
        val restoration = StateRestorationTester(this)
        var layout by mutableStateOf(WindowLayout.Compact)
        restoration.setContent {
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

        restoration.emulateSaveAndRestore()
        layout = WindowLayout.PhoneLandscape
        waitForIdle()

        assertEquals("verb", viewModel.state.value.query)
        onNode(hasSetTextAction() and hasText("verb")).assertExists()
        onNode(hasText("verb20")).assertIsDisplayed()
        onNode(hasText("verb00")).assertDoesNotExist()
    }
}
