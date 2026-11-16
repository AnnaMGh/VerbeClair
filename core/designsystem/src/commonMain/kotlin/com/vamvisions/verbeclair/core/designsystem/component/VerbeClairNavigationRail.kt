package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.flag.flagRailBackground
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import org.jetbrains.compose.resources.stringResource

// Figma `Navigation rail` (86:9531): 80 dp wide, items 56 dp from the top.
private val RailWidth = 80.dp
private val RailTopPadding = 56.dp

/** Padding around the rail's items in the Figma component (86:9531): 56 dp at the top, 16 dp at the bottom. */
object VerbeClairNavigationRailDefaults {
    val contentPadding: PaddingValues
        @Composable get() = PaddingValues(top = RailTopPadding, bottom = VerbeClairTheme.spacing.space16)
}

/**
 * Navigation rail (Figma 86:9531, Notion NV-4): the bottom bar's destinations in an 80 dp column that fills the
 * window height, items from the top, with a 1 dp `outline-variant` line on its end side. It has no background of
 * its own: the Blue flag stripe sits behind it (N-14).
 *
 * Built from M3 [NavigationRailItem]s in a plain column because M3's `NavigationRail` fixes its own top padding and
 * item spacing, which differ from Figma's. [contentPadding] is the space around the items: the component's
 * default, or the screen's (Figma uses 16 dp at the top in phone landscape, 24 dp on tablets).
 */
@Composable
fun VerbeClairNavigationRail(
    selected: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = VerbeClairNavigationRailDefaults.contentPadding,
    windowInsets: WindowInsets = NavigationRailDefaults.windowInsets,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .flagRailBackground()
                .endLine(colors.outlineVariant)
                // The insets (a display cutout on the start side in landscape) widen the rail rather than narrowing
                // its 80 dp item column, so labels never wrap.
                .windowInsetsPadding(windowInsets)
                .width(RailWidth)
                .padding(contentPadding)
                .selectableGroup(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VerbeClairTheme.spacing.space16),
    ) {
        TopLevelDestination.entries.forEach { destination ->
            val isSelected = destination == selected
            NavigationRailItem(
                selected = isSelected,
                onClick = { onSelect(destination) },
                icon = { Icon(imageVector = destination.icon, contentDescription = null) },
                label = { DestinationLabel(stringResource(destination.label), isSelected) },
                colors =
                    NavigationRailItemDefaults.colors(
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
