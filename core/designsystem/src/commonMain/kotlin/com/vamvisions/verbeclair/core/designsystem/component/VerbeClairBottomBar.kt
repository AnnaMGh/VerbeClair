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
