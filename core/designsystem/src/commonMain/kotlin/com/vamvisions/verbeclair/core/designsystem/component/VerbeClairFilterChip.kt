package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.icon.VerbeClairIcons

// Figma `Chip` (5:101) check icon size.
private val CheckIconSize = 18.dp

/**
 * Filter chip (Figma 5:101, Filter off / Filter on): 32 dp high, radius `sm`. Not selected: a 1 dp
 * `outline-variant` border. Selected: a `secondary-container` container and a check icon.
 *
 * Taps are reported in both states, so a screen can toggle a value (list-controls sheet, Notion L-5) or remove an
 * active choice (active choices row, L-6). Screen readers announce the selected state.
 */
@Composable
fun VerbeClairFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, style = MaterialTheme.typography.labelLarge) },
        modifier = modifier,
        leadingIcon =
            if (selected) {
                { Icon(imageVector = VerbeClairIcons.Check, contentDescription = null, Modifier.size(CheckIconSize)) }
            } else {
                null
            },
        shape = MaterialTheme.shapes.small,
        colors =
            FilterChipDefaults.filterChipColors(
                containerColor = Color.Transparent,
                labelColor = colors.onSurfaceVariant,
                selectedContainerColor = colors.secondaryContainer,
                selectedLabelColor = colors.onSecondaryContainer,
                selectedLeadingIconColor = colors.onSecondaryContainer,
            ),
        border =
            FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = colors.outlineVariant,
                selectedBorderColor = Color.Transparent,
                borderWidth = 1.dp,
                selectedBorderWidth = 0.dp,
            ),
    )
}
