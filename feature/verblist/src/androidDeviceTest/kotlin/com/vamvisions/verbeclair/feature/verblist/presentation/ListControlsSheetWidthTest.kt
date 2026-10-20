package com.vamvisions.verbeclair.feature.verblist.presentation

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.domain.verb.ListChoices
import com.vamvisions.verbeclair.feature.verblist.presentation.components.ListControlsSheet
import com.vamvisions.verbeclair.feature.verblist.presentation.components.SheetMaxWidth
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The sheet keeps its phone design on wide windows (N-15): at most 640 dp wide and centred. The phone
 * emulator is turned to landscape so that the window is wider than 640 dp.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalMaterial3Api::class)
class ListControlsSheetWidthTest {

    @Test
    fun sheetIsAtMost640DpWideAndCentred() =
        runAndroidComposeUiTest<ComponentActivity> {
            activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            awaitLandscapeWidth()
            var windowWidth = 0.dp
            setContent {
                val density = LocalDensity.current
                windowWidth =
                    with(density) {
                        LocalWindowInfo.current.containerSize.width
                            .toDp()
                    }
                VerbeClairTheme {
                    ListControlsSheet(choices = ListChoices.Default, onAction = {}, modifier = Modifier.testTag(SHEET))
                }
            }
            waitForIdle()

            assertTrue(windowWidth > SheetMaxWidth, "The window ($windowWidth) must be wider than the sheet's maximum")
            val bounds = onNodeWithTag(SHEET).getBoundsInRoot()
            val width = bounds.right - bounds.left
            assertTrue(width <= SheetMaxWidth, "Sheet width $width")
            assertTrue(
                abs((bounds.left - (windowWidth - bounds.right)).value) <= 1f,
                "Sheet not centred: $bounds in $windowWidth",
            )
            activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

    /** The rotation is asynchronous and may recreate the activity: wait until the window is really wide. */
    private fun AndroidComposeUiTest<ComponentActivity>.awaitLandscapeWidth() {
        waitUntil(timeoutMillis = 10_000) {
            val widthDp = activity?.resources?.configuration?.screenWidthDp ?: 0
            widthDp.dp > SheetMaxWidth
        }
        waitForIdle()
    }

    private companion object {
        const val SHEET = "sheet"
    }
}
