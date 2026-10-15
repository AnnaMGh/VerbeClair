package com.vamvisions.verbeclair.core.designsystem.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme

// Figma Icons frame (4:2): an 11-column grid, icons tinted on-surface-variant, the filled star in the ending colour.
private const val ICONS_PER_ROW = 11

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun IconCatalog(modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        maxItemsInEachRow = ICONS_PER_ROW,
        horizontalArrangement = Arrangement.spacedBy(VerbeClairTheme.spacing.space24),
        verticalArrangement = Arrangement.spacedBy(VerbeClairTheme.spacing.space24),
    ) {
        VerbeClairIcons.all.forEach { (name, icon) ->
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (icon == VerbeClairIcons.StarFilled) {
                    VerbeClairTheme.extendedColors.ending
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=4-2
@Preview(widthDp = 528, heightDp = 192)
@Composable
private fun IconCatalogLightPreview() {
    VerbeClairTheme(darkTheme = false) { IconCatalog() }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=4-2
@Preview(widthDp = 528, heightDp = 192)
@Composable
private fun IconCatalogDarkPreview() {
    VerbeClairTheme(darkTheme = true) { IconCatalog() }
}
