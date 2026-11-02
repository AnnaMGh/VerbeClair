package com.vamvisions.verbeclair.core.presentation.window

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

/** The adaptive structure of a screen, by the N-15 thresholds (design D8). */
enum class WindowLayout {
    /** Width < 600 dp: phone portrait, with the bottom bar. */
    Compact,

    /** Width ≥ 600 dp and height < 480 dp: phone landscape, rail and one pane. */
    PhoneLandscape,

    /** Width ≥ 600 dp and height ≥ 480 dp: tablets and unfolded foldables, rail and list–detail. */
    ListDetail,
}

/** The [WindowLayout] of a window in [sizeClass]. */
fun windowLayoutOf(sizeClass: WindowSizeClass): WindowLayout =
    when {
        !sizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND) -> WindowLayout.Compact
        !sizeClass.isHeightAtLeastBreakpoint(HEIGHT_DP_MEDIUM_LOWER_BOUND) -> WindowLayout.PhoneLandscape
        else -> WindowLayout.ListDetail
    }

/** The [WindowLayout] of the current window; it changes when the window is rotated or resized. */
@Composable
fun currentWindowLayout(): WindowLayout = windowLayoutOf(currentWindowAdaptiveInfo().windowSizeClass)

/** Medium width (600–839 dp), where back first clears a list–detail selection (N-15). */
fun isMediumWidth(sizeClass: WindowSizeClass): Boolean =
    sizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND) &&
        !sizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)

/** Whether the current window has Medium width. */
@Composable
fun currentWindowIsMediumWidth(): Boolean = isMediumWidth(currentWindowAdaptiveInfo().windowSizeClass)
