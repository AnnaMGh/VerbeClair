package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme

/**
 * Section header (Figma 7:45, Notion L-5): a row on `surface-container` with the section [title] in uppercase
 * Label/Overline `primary` and the section's verb [count] in Label/Medium `on-surface-variant`. Opaque, so it can
 * be used as a sticky header. [count] is optional, and grouped verb lists leave it out (as the L-03 instance 10:339
 * does).
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    count: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    val spacing = VerbeClairTheme.spacing
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.surfaceContainer)
                .padding(horizontal = spacing.space16, vertical = spacing.space8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space8),
    ) {
        Text(
            text = title.uppercase(),
            modifier = Modifier.weight(1f),
            style = VerbeClairTheme.typography.labelOverline,
            color = colors.primary,
        )
        if (count != null) {
            Text(text = count, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
        }
    }
}
