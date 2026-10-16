package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme

// Figma `Empty state` (7:135) illustration sizes.
private val IllustrationSize = 96.dp
private val IllustrationIconSize = 40.dp

/** Figma `Empty state` (7:135) tones. */
enum class EmptyStateTone {
    /** `primary-container` circle: no result (Notion L-9), no data (L-10). */
    Neutral,

    /** `error-container` circle: verb unavailable, page failed to open. */
    Error,
}

/**
 * Empty state (Figma 7:135): a centred column with a 96 dp circle holding a 40 dp [icon], the [title] in
 * Title/Large, the [body] in Body/Medium and an optional [action], normally a Filled [VerbeClairButton]. The icon is
 * decoration and is hidden from screen readers.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    tone: EmptyStateTone = EmptyStateTone.Neutral,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val spacing = VerbeClairTheme.spacing
    val (circleColor, iconColor) = when (tone) {
        EmptyStateTone.Neutral -> colors.primaryContainer to colors.onPrimaryContainer
        EmptyStateTone.Error -> colors.errorContainer to colors.onErrorContainer
    }
    Column(
        modifier = modifier.padding(spacing.space24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.space16),
    ) {
        Box(
            modifier = Modifier.size(IllustrationSize).background(circleColor, VerbeClairTheme.radius.full),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(IllustrationIconSize),
                tint = iconColor,
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.space8),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        action?.invoke()
    }
}
