package com.vamvisions.verbeclair.core.presentation.window

import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass
import kotlin.test.Test
import kotlin.test.assertEquals

/** The N-15 thresholds, on window sizes classified the way `currentWindowAdaptiveInfo()` does. */
class WindowLayoutTest {

    @Test
    fun phonePortraitIsCompact() = assertLayout(WindowLayout.Compact, 393, 852)

    @Test
    fun phoneLandscapeIsPhoneLandscape() = assertLayout(WindowLayout.PhoneLandscape, 852, 393)

    @Test
    fun tabletPortraitIsListDetail() = assertLayout(WindowLayout.ListDetail, 834, 1194)

    @Test
    fun tabletLandscapeIsListDetail() = assertLayout(WindowLayout.ListDetail, 1194, 834)

    @Test
    fun justUnder600WideIsCompact() = assertLayout(WindowLayout.Compact, 599, 900)

    @Test
    fun justUnder480HighIsPhoneLandscape() = assertLayout(WindowLayout.PhoneLandscape, 700, 479)

    @Test
    fun exactly600By480IsListDetail() = assertLayout(WindowLayout.ListDetail, 600, 480)

    @Test
    fun mediumWidthIsFrom600To839() {
        val medium = listOf(600 to 900, 834 to 1194, 839 to 600)
        val notMedium = listOf(599 to 900, 840 to 600, 1194 to 834)
        medium.forEach { (w, h) -> kotlin.test.assertTrue(isMediumWidth(sizeClass(w, h)), "$w × $h dp") }
        notMedium.forEach { (w, h) -> kotlin.test.assertFalse(isMediumWidth(sizeClass(w, h)), "$w × $h dp") }
    }

    private fun sizeClass(widthDp: Int, heightDp: Int) =
        WindowSizeClass.BREAKPOINTS_V1.computeWindowSizeClass(widthDp, heightDp)

    private fun assertLayout(
        expected: WindowLayout,
        widthDp: Int,
        heightDp: Int,
    ) {
        val sizeClass = WindowSizeClass.BREAKPOINTS_V1.computeWindowSizeClass(widthDp, heightDp)
        assertEquals(expected, windowLayoutOf(sizeClass), "$widthDp × $heightDp dp")
    }
}
