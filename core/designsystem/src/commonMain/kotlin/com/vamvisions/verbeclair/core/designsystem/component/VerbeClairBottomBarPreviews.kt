package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-62
@Preview(widthDp = 393)
@Composable
private fun BottomBarVerbsLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairBottomBar(
            selected = TopLevelDestination.Verbs,
            onSelect = {},
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-62
@Preview(widthDp = 393)
@Composable
private fun BottomBarVerbsDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairBottomBar(
            selected = TopLevelDestination.Verbs,
            onSelect = {},
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-84
@Preview(widthDp = 393)
@Composable
private fun BottomBarTestsLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairBottomBar(
            selected = TopLevelDestination.Tests,
            onSelect = {},
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-84
@Preview(widthDp = 393)
@Composable
private fun BottomBarTestsDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairBottomBar(
            selected = TopLevelDestination.Tests,
            onSelect = {},
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-106
@Preview(widthDp = 393)
@Composable
private fun BottomBarSettingsLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairBottomBar(
            selected = TopLevelDestination.Settings,
            onSelect = {},
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-106
@Preview(widthDp = 393)
@Composable
private fun BottomBarSettingsDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairBottomBar(
            selected = TopLevelDestination.Settings,
            onSelect = {},
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-106
@Preview(widthDp = 393, fontScale = 2f)
@Composable
private fun BottomBarLargeFontLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairBottomBar(
            selected = TopLevelDestination.Settings,
            onSelect = {},
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-106
@Preview(widthDp = 393, fontScale = 2f)
@Composable
private fun BottomBarLargeFontDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairBottomBar(
            selected = TopLevelDestination.Settings,
            onSelect = {},
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-106
@Preview(widthDp = 393, locale = "fr")
@Composable
private fun BottomBarFrenchLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairBottomBar(
            selected = TopLevelDestination.Settings,
            onSelect = {},
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-106
@Preview(widthDp = 393, locale = "fr")
@Composable
private fun BottomBarFrenchDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairBottomBar(
            selected = TopLevelDestination.Settings,
            onSelect = {},
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-106
@Preview(widthDp = 393, locale = "ro")
@Composable
private fun BottomBarRomanianLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairBottomBar(
            selected = TopLevelDestination.Settings,
            onSelect = {},
            windowInsets = WindowInsets(0),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-106
@Preview(widthDp = 393, locale = "ro")
@Composable
private fun BottomBarRomanianDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairBottomBar(
            selected = TopLevelDestination.Settings,
            onSelect = {},
            windowInsets = WindowInsets(0),
        )
    }
}
