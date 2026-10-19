package com.vamvisions.verbeclair

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.presentation.window.WindowLayout
import com.vamvisions.verbeclair.feature.verblist.presentation.VerbListPreviewContent

// The app shell around the verb list, per supported configuration, at the size of its full Figma frame
// (status bar included, which the preview leaves empty). The list shows the frame's default rows.

@Composable
private fun ShellPreview(layout: WindowLayout, darkTheme: Boolean) {
    VerbeClairTheme(darkTheme = darkTheme) {
        AppShell(layout = layout) { modifier -> VerbListPreviewContent(layout, modifier) }
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-2
@Preview(name = "Phone portrait, light", widthDp = 393, heightDp = 852)
@Composable
private fun AppPhonePortraitLightPreview() = ShellPreview(WindowLayout.Compact, darkTheme = false)

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=13-913
@Preview(name = "Phone portrait, dark", widthDp = 393, heightDp = 852)
@Composable
private fun AppPhonePortraitDarkPreview() = ShellPreview(WindowLayout.Compact, darkTheme = true)

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=87-9531
@Preview(name = "Phone landscape, light", widthDp = 852, heightDp = 393)
@Composable
private fun AppPhoneLandscapeLightPreview() = ShellPreview(WindowLayout.PhoneLandscape, darkTheme = false)

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=90-12103
@Preview(name = "Phone landscape, dark", widthDp = 852, heightDp = 393)
@Composable
private fun AppPhoneLandscapeDarkPreview() = ShellPreview(WindowLayout.PhoneLandscape, darkTheme = true)

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=88-744
@Preview(name = "Tablet portrait, light", widthDp = 834, heightDp = 1194)
@Composable
private fun AppTabletPortraitLightPreview() = ShellPreview(WindowLayout.ListDetail, darkTheme = false)

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=90-12250
@Preview(name = "Tablet portrait, dark", widthDp = 834, heightDp = 1194)
@Composable
private fun AppTabletPortraitDarkPreview() = ShellPreview(WindowLayout.ListDetail, darkTheme = true)

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=89-1153
@Preview(name = "Tablet landscape, light", widthDp = 1194, heightDp = 834)
@Composable
private fun AppTabletLandscapeLightPreview() = ShellPreview(WindowLayout.ListDetail, darkTheme = false)

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=90-12336
@Preview(name = "Tablet landscape, dark", widthDp = 1194, heightDp = 834)
@Composable
private fun AppTabletLandscapeDarkPreview() = ShellPreview(WindowLayout.ListDetail, darkTheme = true)
