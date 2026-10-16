package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme

// Figma `Button` (5:89) leading icon size.
private val LeadingIconSize = 18.dp

/**
 * Figma `Button` (5:89) styles. Figma's Disabled style is any style with `enabled = false`: Material 3's disabled
 * colours (`on-surface` at 12% behind `on-surface` at 38%) are the Figma values.
 */
enum class ButtonStyle {
    /** `primary` container, `on-primary` label: the main action of empty and error states. */
    Filled,

    /** `secondary-container` container, `on-secondary-container` label. */
    Tonal,

    /** 1 dp `outline` border, `primary` label. */
    Outlined,

    /** `primary` label, no container: Reset in the list-controls sheet. */
    Text,
}

/** Button (Figma 5:89): 40 dp high, fully rounded, Label/Large label and an optional 18 dp leading icon. */
@Composable
fun VerbeClairButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Filled,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val colors = MaterialTheme.colorScheme
    val shape = VerbeClairTheme.radius.full
    val content: @Composable RowScope.() -> Unit = {
        if (leadingIcon != null) {
            Icon(imageVector = leadingIcon, contentDescription = null, modifier = Modifier.size(LeadingIconSize))
            Spacer(Modifier.width(VerbeClairTheme.spacing.space8))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
    when (style) {
        ButtonStyle.Filled -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
            content = content,
        )
        ButtonStyle.Tonal -> FilledTonalButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = colors.secondaryContainer,
                contentColor = colors.onSecondaryContainer,
            ),
            content = content,
        )
        ButtonStyle.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
            border = if (enabled) BorderStroke(1.dp, colors.outline) else ButtonDefaults.outlinedButtonBorder(false),
            content = content,
        )
        ButtonStyle.Text -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.textButtonColors(contentColor = colors.primary),
            content = content,
        )
    }
}
