package com.vamvisions.verbeclair.feature.verblist.presentation

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isDisplayed
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
            // The sheet animates in and its window takes focus afterwards; a slow emulator needs time for both.
            waitUntil(timeoutMillis = SETTLE_MILLIS) { onNodeWithText("List options").isDisplayed() }
            waitForIdle()

            // The sheet is its own window, so Back goes to it rather than to the activity's dispatcher. Espresso
            // would wait for a window with focus and give up after 10 s, which a starved CI emulator can exceed.
            // A key sent before the sheet's window has focus is lost, so send it again until the sheet reacts.
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            repeat(BACK_ATTEMPTS) {
                if (actions.isNotEmpty()) return@repeat
                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                runCatching { waitUntil(timeoutMillis = BACK_RETRY_MILLIS) { actions.isNotEmpty() } }
            }

            assertEquals(VerbListAction.OnDismissSheet, actions.first())
        }

    private companion object {
        const val SETTLE_MILLIS = 20_000L
        const val BACK_ATTEMPTS = 8
        const val BACK_RETRY_MILLIS = 2_500L
    }
}
