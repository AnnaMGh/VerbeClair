package com.vamvisions.verbeclair.core.designsystem.flag

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class FlagBackgroundTest {

    private fun assertFlagIsNotInSemantics(flag: @androidx.compose.runtime.Composable Modifier.() -> Modifier) =
        runComposeUiTest {
            setContent {
                VerbeClairTheme {
                    Box(Modifier.size(200.dp).testTag(TAG).flag())
                }
            }
            val node = onNodeWithTag(TAG, useUnmergedTree = true).fetchSemanticsNode()
            assertTrue(node.children.isEmpty(), "the flag adds no semantics children")
            assertEquals(
                listOf(SemanticsProperties.TestTag),
                node.config.map { it.key },
                "the flag adds no semantics properties (no content description, no role)",
            )
        }

    @Test
    fun screenBackgroundIsHiddenFromScreenReaders() = assertFlagIsNotInSemantics { flagScreenBackground() }

    @Test
    fun barBackgroundIsHiddenFromScreenReaders() = assertFlagIsNotInSemantics { flagBarBackground() }

    @Test
    fun railBackgroundIsHiddenFromScreenReaders() = assertFlagIsNotInSemantics { flagRailBackground() }

    @Test
    fun stripeCoversTheAreaKeepingItsAspectRatio() {
        // A wide 100 × 50 stripe on a square 200 × 200 area: the height decides, so it is drawn 400 × 200.
        assertEquals(Size(400f, 200f), coverSize(Size(100f, 50f), Size(200f, 200f), rotated = false))
    }

    @Test
    fun rotatedStripeCoversTheAreaWithItsSidesSwapped() {
        // Rotated by 90°, the 100 × 50 stripe is 50 wide and 100 tall on screen; a 400 × 80 bar needs it 8× larger.
        val drawn = coverSize(Size(100f, 50f), Size(400f, 80f), rotated = true)
        assertEquals(Size(800f, 400f), drawn)
        assertTrue(drawn.height >= 400f && drawn.width >= 80f, "after rotation the stripe covers the whole bar")
    }

    @Test
    fun onlyTheBarStripeIsRotated() {
        assertTrue(FlagPlacement.Bar.rotated, "bar stripe rotated by 90° (Figma 95:211)")
        assertEquals(false, FlagPlacement.Screen.rotated)
        assertEquals(false, FlagPlacement.Rail.rotated)
    }

    private companion object {
        const val TAG = "flag"
    }
}
