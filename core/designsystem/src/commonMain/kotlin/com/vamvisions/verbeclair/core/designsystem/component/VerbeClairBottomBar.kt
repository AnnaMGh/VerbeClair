package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.flag.flagBarBackground
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Bottom bar (Figma 49:128, Notion NV-1): Verbs, Tests and Settings with [selected] marked by a
 * `navigation-indicator` indicator. 80 dp high with a 1 dp `outline-variant` line on top, and opaque: the
 * background colour plus the Blue flag stripe rotated 90° (N-14). Items are announced as tabs with their state.
 */
@Composable
fun VerbeClairBottomBar(
    selected: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
) {
    val colors = MaterialTheme.colorScheme
    NavigationBar(
        modifier = modifier.flagBarBackground().topLine(colors.outlineVariant),
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        windowInsets = windowInsets,
    ) {
        TopLevelDestination.entries.forEach { destination ->
            val isSelected = destination == selected
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelect(destination) },
                icon = { Icon(imageVector = destination.icon, contentDescription = null) },
                label = { DestinationLabel(stringResource(destination.label), isSelected) },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.onSecondaryContainer,
                        selectedTextColor = colors.onSurface,
                        indicatorColor = VerbeClairTheme.extendedColors.navigationIndicator,
                        unselectedIconColor = colors.onSurfaceVariant,
                        unselectedTextColor = colors.onSurfaceVariant,
                    ),
            )
        }
    }
}

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
