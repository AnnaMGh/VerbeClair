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
import androidx.compose.ui.tooling.preview.Preview
import com.vamvisions.verbeclair.core.designsystem.component.TopLevelDestination
import com.vamvisions.verbeclair.core.designsystem.component.VerbeClairBottomBar
import com.vamvisions.verbeclair.core.designsystem.component.VerbeClairNavigationRail
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.presentation.window.WindowLayout
import com.vamvisions.verbeclair.core.presentation.window.currentWindowLayout
import com.vamvisions.verbeclair.feature.verblist.presentation.VerbListPreviewContent
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
                contentPadding =
                    PaddingValues(
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

// The app shell around the verb list, per supported configuration, at the size of its full Figma frame
// (status bar included, which the preview leaves empty). The list shows the frame's default rows.

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-2
@Preview(name = "Phone portrait, light", widthDp = 393, heightDp = 852)
@Composable
private fun AppPhonePortraitLightPreview() =
    VerbeClairTheme(darkTheme = false) {
        AppShell(layout = WindowLayout.Compact) { modifier -> VerbListPreviewContent(WindowLayout.Compact, modifier) }
    }

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=13-913
@Preview(name = "Phone portrait, dark", widthDp = 393, heightDp = 852)
@Composable
private fun AppPhonePortraitDarkPreview() =
    VerbeClairTheme(darkTheme = true) {
        AppShell(layout = WindowLayout.Compact) { modifier -> VerbListPreviewContent(WindowLayout.Compact, modifier) }
    }

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=87-9531
@Preview(name = "Phone landscape, light", widthDp = 852, heightDp = 393)
@Composable
private fun AppPhoneLandscapeLightPreview() =
    VerbeClairTheme(darkTheme = false) {
        AppShell(
            layout = WindowLayout.PhoneLandscape,
        ) { modifier -> VerbListPreviewContent(WindowLayout.PhoneLandscape, modifier) }
    }

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=90-12103
@Preview(name = "Phone landscape, dark", widthDp = 852, heightDp = 393)
@Composable
private fun AppPhoneLandscapeDarkPreview() =
    VerbeClairTheme(darkTheme = true) {
        AppShell(
            layout = WindowLayout.PhoneLandscape,
        ) { modifier -> VerbListPreviewContent(WindowLayout.PhoneLandscape, modifier) }
    }

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=88-744
@Preview(name = "Tablet portrait, light", widthDp = 834, heightDp = 1194)
@Composable
private fun AppTabletPortraitLightPreview() =
    VerbeClairTheme(darkTheme = false) {
        AppShell(
            layout = WindowLayout.ListDetail,
        ) { modifier -> VerbListPreviewContent(WindowLayout.ListDetail, modifier) }
    }

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=90-12250
@Preview(name = "Tablet portrait, dark", widthDp = 834, heightDp = 1194)
@Composable
private fun AppTabletPortraitDarkPreview() =
    VerbeClairTheme(darkTheme = true) {
        AppShell(
            layout = WindowLayout.ListDetail,
        ) { modifier -> VerbListPreviewContent(WindowLayout.ListDetail, modifier) }
    }

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=89-1153
@Preview(name = "Tablet landscape, light", widthDp = 1194, heightDp = 834)
@Composable
private fun AppTabletLandscapeLightPreview() =
    VerbeClairTheme(darkTheme = false) {
        AppShell(
            layout = WindowLayout.ListDetail,
        ) { modifier -> VerbListPreviewContent(WindowLayout.ListDetail, modifier) }
    }

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=90-12336
@Preview(name = "Tablet landscape, dark", widthDp = 1194, heightDp = 834)
@Composable
private fun AppTabletLandscapeDarkPreview() =
    VerbeClairTheme(darkTheme = true) {
        AppShell(
            layout = WindowLayout.ListDetail,
        ) { modifier -> VerbListPreviewContent(WindowLayout.ListDetail, modifier) }
    }
