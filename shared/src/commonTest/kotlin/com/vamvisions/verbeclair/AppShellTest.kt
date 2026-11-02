package com.vamvisions.verbeclair

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
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
import kotlin.test.assertTrue

/** The bottom bar on Compact, the rail otherwise, with Verbs selected (task 8.2, NV-1, NV-4). */
@OptIn(ExperimentalTestApi::class)
class AppShellTest {

    @Test
    fun compactShowsTheBottomBarWithVerbsSelected() =
        runComposeUiTest {
            setContent { VerbeClairTheme { AppShell(layout = WindowLayout.Compact, content = {}) } }

            onNodeWithTag(AppShellTestTags.BOTTOM_BAR).assertExists()
            onNodeWithTag(AppShellTestTags.RAIL).assertDoesNotExist()
            onNodeWithText("Verbs").assertIsSelected()
        }

    @Test
    fun landscapeAndTabletShowTheRailWithVerbsSelected() =
        runComposeUiTest {
            var layout by mutableStateOf(WindowLayout.PhoneLandscape)
            setContent { VerbeClairTheme { AppShell(layout = layout, content = {}) } }

            onNodeWithTag(AppShellTestTags.RAIL).assertExists()
            onNodeWithTag(AppShellTestTags.BOTTOM_BAR).assertDoesNotExist()
            onNodeWithText("Verbs").assertIsSelected()

            layout = WindowLayout.ListDetail
            onNodeWithTag(AppShellTestTags.RAIL).assertExists()
            onNodeWithTag(AppShellTestTags.BOTTOM_BAR).assertDoesNotExist()
        }

    /** The rail shows the dark theme background in dark mode, not the window's light one (Figma 90:12103). */
    @Test
    fun railIsDarkInDarkTheme() =
        runComposeUiTest {
            setContent {
                VerbeClairTheme(
                    darkTheme = true,
                ) { AppShell(layout = WindowLayout.PhoneLandscape, content = {}) }
            }

            val pixels = onNodeWithTag(AppShellTestTags.RAIL).captureToImage().toPixelMap()
            // Below the items, where only the background and the faint flag stripe are drawn.
            val below = pixels[pixels.width / 2, pixels.height - 2]
            assertTrue(below.luminance() < 0.3f, "rail background luminance ${below.luminance()}")
        }

    /** Figma PL-01 (87:9534) has 16 dp above the rail items, TP-01 / TL-01 (88:759) 24 dp. */
    @Test
    fun railItemsSitLowerOnTablets() =
        runComposeUiTest {
            var layout by mutableStateOf(WindowLayout.PhoneLandscape)
            setContent { VerbeClairTheme { AppShell(layout = layout, content = {}) } }
            val landscapeTop = onNodeWithText("Verbs").getBoundsInRoot().top

            layout = WindowLayout.ListDetail
            waitForIdle()
            val tabletTop = onNodeWithText("Verbs").getBoundsInRoot().top

            assertTrue(
                abs((tabletTop - landscapeTop - 8.dp).value) < 0.5f,
                "landscape $landscapeTop, tablet $tabletTop",
            )
        }

    @Test
    fun tappingTestsKeepsVerbsSelected() =
        runComposeUiTest {
            setContent { VerbeClairTheme { AppShell(layout = WindowLayout.Compact, content = {}) } }

            onNodeWithText("Tests").performClick()

            onNodeWithText("Verbs").assertIsSelected()
        }

    /** Switching between bar and rail keeps the content in place, so its saved state survives a rotation. */
    @Test
    fun switchingBetweenBarAndRailKeepsTheContentState() =
        runComposeUiTest {
            var layout by mutableStateOf(WindowLayout.Compact)
            setContent {
                VerbeClairTheme {
                    AppShell(layout = layout) { modifier ->
                        var taps by rememberSaveable { mutableIntStateOf(0) }
                        Box(modifier) { Text("taps $taps", Modifier.clickable { taps++ }) }
                    }
                }
            }
            onNodeWithText("taps 0").performClick()
            onNode(hasText("taps 1")).assertExists()

            layout = WindowLayout.PhoneLandscape

            onNode(hasText("taps 1")).assertExists()
        }
}
