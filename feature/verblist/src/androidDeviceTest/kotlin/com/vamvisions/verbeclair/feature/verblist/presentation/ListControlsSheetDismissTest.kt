package com.vamvisions.verbeclair.feature.verblist.presentation

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.platform.app.InstrumentationRegistry
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.domain.verb.ListChoices
import com.vamvisions.verbeclair.feature.verblist.presentation.components.ListControlsSheet
import kotlin.test.Test
import kotlin.test.assertEquals

/** Closing the sheet only dismisses it: the choices made in it stay (L-5). */
@OptIn(ExperimentalTestApi::class, ExperimentalMaterial3Api::class)
class ListControlsSheetDismissTest {

    @Test
    fun backDismissesTheSheet() =
        runAndroidComposeUiTest<ComponentActivity> {
            val actions = mutableListOf<VerbListAction>()
            setContent {
                VerbeClairTheme { ListControlsSheet(choices = ListChoices.Default, onAction = { actions += it }) }
            }
            onNodeWithText("List options").assertExists()

            // The sheet is its own window, so the key goes to it rather than to the activity's dispatcher.
            InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            waitForIdle()

            assertEquals(listOf<VerbListAction>(VerbListAction.OnDismissSheet), actions)
        }
}
