package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=86-9492
@Preview(heightDp = 852)
@Composable
private fun NavigationRailVerbsLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairNavigationRail(
            selected = TopLevelDestination.Verbs,
            onSelect = {},
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=86-9492
@Preview(heightDp = 852)
@Composable
private fun NavigationRailVerbsDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairNavigationRail(
            selected = TopLevelDestination.Verbs,
            onSelect = {},
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=86-9505
@Preview(heightDp = 852)
@Composable
private fun NavigationRailTestsLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairNavigationRail(
            selected = TopLevelDestination.Tests,
            onSelect = {},
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=86-9505
@Preview(heightDp = 852)
@Composable
private fun NavigationRailTestsDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairNavigationRail(
            selected = TopLevelDestination.Tests,
            onSelect = {},
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=86-9518
@Preview(heightDp = 852)
@Composable
private fun NavigationRailSettingsLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairNavigationRail(
            selected = TopLevelDestination.Settings,
            onSelect = {},
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=86-9518
@Preview(heightDp = 852)
@Composable
private fun NavigationRailSettingsDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairNavigationRail(
            selected = TopLevelDestination.Settings,
            onSelect = {},
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=87-9534
@Preview(heightDp = 393)
@Composable
private fun NavigationRailLandscapeLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairNavigationRail(
            selected = TopLevelDestination.Verbs,
            onSelect = {},
            contentPadding = PaddingValues(vertical = 16.dp),
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=87-9534
@Preview(heightDp = 393)
@Composable
private fun NavigationRailLandscapeDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairNavigationRail(
            selected = TopLevelDestination.Verbs,
            onSelect = {},
            contentPadding = PaddingValues(vertical = 16.dp),
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=88-759
@Preview(heightDp = 1150)
@Composable
private fun NavigationRailTabletLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairNavigationRail(
            selected = TopLevelDestination.Verbs,
            onSelect = {},
            contentPadding = PaddingValues(top = 24.dp, bottom = 16.dp),
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=88-759
@Preview(heightDp = 1150)
@Composable
private fun NavigationRailTabletDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairNavigationRail(
            selected = TopLevelDestination.Verbs,
            onSelect = {},
            contentPadding = PaddingValues(top = 24.dp, bottom = 16.dp),
            windowInsets = WindowInsets(0),
        )
    }
}
