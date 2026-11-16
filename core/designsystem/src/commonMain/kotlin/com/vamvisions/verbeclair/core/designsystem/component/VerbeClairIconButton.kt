package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Badge
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.generated.resources.Res
import com.vamvisions.verbeclair.core.designsystem.generated.resources.icon_button_badge
import com.vamvisions.verbeclair.core.designsystem.icon.VerbeClairIcons
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import org.jetbrains.compose.resources.stringResource

// Figma `Icon button` (5:39): the badge's top-left corner inside the 48 dp button.
private val BadgeOffsetX = 26.dp

/** Figma `Icon button` (5:39) styles. */
enum class IconButtonStyle {
    /** Transparent state layer: settings, back, list controls with no active filter. */
    Standard,

    /** `secondary-container` state layer: list controls when filters are active. */
    Tonal,
}

/**
 * Icon button (Figma 5:39): a 48 dp touch target with a 40 dp circular state layer, in the [IconButtonStyle]
 * styles, with an optional count badge in `error` at the top right of the icon (Notion L-6).
 *
 * [contentDescription] is the button's accessible label; when [badgeCount] is not null the count is added to it,
 * and the badge itself is hidden from screen readers so the count is read once.
 */
@Composable
fun VerbeClairIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: IconButtonStyle = IconButtonStyle.Standard,
    badgeCount: Int? = null,
) {
    val colors = MaterialTheme.colorScheme
    val label =
        if (badgeCount != null) {
            stringResource(Res.string.icon_button_badge, contentDescription, badgeCount)
        } else {
            contentDescription
        }
    // The badge is a sibling drawn over the button: inside the tonal button's circular container its top-right
    // corner would be clipped.
    Box(modifier) {
        when (style) {
            IconButtonStyle.Standard -> {
                IconButton(
                    onClick = onClick,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = colors.onSurfaceVariant),
                ) {
                    Icon(imageVector = icon, contentDescription = label)
                }
            }

            IconButtonStyle.Tonal -> {
                FilledTonalIconButton(
                    onClick = onClick,
                    colors =
                        IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = colors.secondaryContainer,
                            contentColor = colors.onSecondaryContainer,
                        ),
                ) {
                    Icon(imageVector = icon, contentDescription = label)
                }
            }
        }
        if (badgeCount != null) {
            Badge(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = BadgeOffsetX, y = VerbeClairTheme.spacing.space4),
                containerColor = colors.error,
                contentColor = colors.onError,
            ) {
                Text(
                    text = badgeCount.toString(),
                    modifier = Modifier.semantics { hideFromAccessibility() },
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-23
@Preview
@Composable
private fun IconButtonStandardLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairIconButton(icon = VerbeClairIcons.Tune, contentDescription = "List controls", onClick = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-23
@Preview
@Composable
private fun IconButtonStandardDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairIconButton(icon = VerbeClairIcons.Tune, contentDescription = "List controls", onClick = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-38
@Preview
@Composable
private fun IconButtonTonalLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairIconButton(
            icon = VerbeClairIcons.Tune,
            contentDescription = "List controls",
            onClick = {},
            style = IconButtonStyle.Tonal,
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-38
@Preview
@Composable
private fun IconButtonTonalDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairIconButton(
            icon = VerbeClairIcons.Tune,
            contentDescription = "List controls",
            onClick = {},
            style = IconButtonStyle.Tonal,
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-273
@Preview
@Composable
private fun IconButtonTonalWithBadgeLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairIconButton(
            icon = VerbeClairIcons.Tune,
            contentDescription = "List controls",
            onClick = {},
            style = IconButtonStyle.Tonal,
            badgeCount = 2,
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-273
@Preview
@Composable
private fun IconButtonTonalWithBadgeDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairIconButton(
            icon = VerbeClairIcons.Tune,
            contentDescription = "List controls",
            onClick = {},
            style = IconButtonStyle.Tonal,
            badgeCount = 2,
        )
    }
}
