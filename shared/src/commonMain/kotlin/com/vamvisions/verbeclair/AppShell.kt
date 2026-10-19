package com.vamvisions.verbeclair

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.vamvisions.verbeclair.core.designsystem.component.TopLevelDestination
import com.vamvisions.verbeclair.core.designsystem.component.VerbeClairBottomBar
import com.vamvisions.verbeclair.core.designsystem.component.VerbeClairNavigationRail
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.presentation.window.WindowLayout
import com.vamvisions.verbeclair.core.presentation.window.currentWindowLayout
import com.vamvisions.verbeclair.feature.verblist.presentation.VerbListRoot

/** Test tags of the shell's navigation, for UI tests. */
object AppShellTestTags {
    const val BOTTOM_BAR = "appShell.bottomBar"
    const val RAIL = "appShell.rail"
}

/**
 * The app shell (design D12, Notion NV-1, NV-4): the bottom bar on [WindowLayout.Compact], the navigation rail
 * otherwise, with Verbs selected. Tests and Settings are not built yet, so tapping them keeps Verbs selected.
 *
 * The tree is the same in every layout (rail · column of content and bar), so [content] keeps its place in the
 * composition, and its saved state, when rotating or resizing switches between bar and rail.
 *
 * The root paints the theme background: the rail has no colour of its own and shows what is behind it, which would
 * otherwise be the light window background, also in dark mode.
 */
@Composable
fun AppShell(
    modifier: Modifier = Modifier,
    layout: WindowLayout = currentWindowLayout(),
    content: @Composable (Modifier) -> Unit = { contentModifier ->
        VerbListRoot(onVerbClick = {}, onTestClick = {}, modifier = contentModifier, layout = layout)
    },
) {
    val spacing = VerbeClairTheme.spacing
    Row(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (layout != WindowLayout.Compact) {
            VerbeClairNavigationRail(
                selected = TopLevelDestination.Verbs,
                onSelect = {},
                modifier = Modifier.testTag(AppShellTestTags.RAIL),
                // Figma PL-01 (87:9534) has 16 dp above the items; TP-01 / TL-01 (88:759) 24 dp.
                contentPadding = PaddingValues(
                    top = if (layout == WindowLayout.PhoneLandscape) spacing.space16 else spacing.space24,
                    bottom = spacing.space16,
                ),
            )
        }
        Column(Modifier.weight(1f)) {
            content(Modifier.weight(1f).fillMaxWidth())
            if (layout == WindowLayout.Compact) {
                VerbeClairBottomBar(
                    selected = TopLevelDestination.Verbs,
                    onSelect = {},
                    modifier = Modifier.testTag(AppShellTestTags.BOTTOM_BAR),
                )
            }
        }
    }
}
