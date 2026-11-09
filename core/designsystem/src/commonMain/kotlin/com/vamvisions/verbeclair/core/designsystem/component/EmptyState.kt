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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.icon.VerbeClairIcons
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
 * Title/Large, an optional [body] in Body/Medium and an optional [action], normally a Filled [VerbeClairButton].
 * Without a [body], no body line or its spacing is drawn. The icon is decoration and is hidden from screen readers.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String?,
    modifier: Modifier = Modifier,
    tone: EmptyStateTone = EmptyStateTone.Neutral,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val spacing = VerbeClairTheme.spacing
    val (circleColor, iconColor) =
        when (tone) {
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
            if (body != null) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        action?.invoke()
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-120
@Preview(widthDp = 329)
@Composable
private fun EmptyStateNeutralWithActionLightPreview() {
    ComponentPreview(darkTheme = false) {
        EmptyState(
            icon = VerbeClairIcons.SearchOff,
            title = "No verbs match your search or filters",
            body = "Try another spelling, or clear the filters to see all 7,092 verbs.",
            action = { VerbeClairButton(text = "Clear filters", onClick = {}) },
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-120
@Preview(widthDp = 329)
@Composable
private fun EmptyStateNeutralWithActionDarkPreview() {
    ComponentPreview(darkTheme = true) {
        EmptyState(
            icon = VerbeClairIcons.SearchOff,
            title = "No verbs match your search or filters",
            body = "Try another spelling, or clear the filters to see all 7,092 verbs.",
            action = { VerbeClairButton(text = "Clear filters", onClick = {}) },
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-742
@Preview(widthDp = 393)
@Composable
private fun EmptyStateNeutralNoActionLightPreview() {
    ComponentPreview(darkTheme = false) {
        EmptyState(
            icon = VerbeClairIcons.CloudDownload,
            title = "No verbs yet",
            body = "Connect to the internet to download them.",
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-742
@Preview(widthDp = 393)
@Composable
private fun EmptyStateNeutralNoActionDarkPreview() {
    ComponentPreview(darkTheme = true) {
        EmptyState(
            icon = VerbeClairIcons.CloudDownload,
            title = "No verbs yet",
            body = "Connect to the internet to download them.",
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-134
@Preview(widthDp = 329)
@Composable
private fun EmptyStateErrorLightPreview() {
    ComponentPreview(darkTheme = false) {
        EmptyState(
            icon = VerbeClairIcons.SearchOff,
            title = "No verbs match your search or filters",
            body = "Try another spelling, or clear the filters to see all 7,092 verbs.",
            tone = EmptyStateTone.Error,
            action = { VerbeClairButton(text = "Clear filters", onClick = {}) },
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-134
@Preview(widthDp = 329)
@Composable
private fun EmptyStateErrorDarkPreview() {
    ComponentPreview(darkTheme = true) {
        EmptyState(
            icon = VerbeClairIcons.SearchOff,
            title = "No verbs match your search or filters",
            body = "Try another spelling, or clear the filters to see all 7,092 verbs.",
            tone = EmptyStateTone.Error,
            action = { VerbeClairButton(text = "Clear filters", onClick = {}) },
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-135
@Preview(widthDp = 329)
@Composable
private fun EmptyStateNeutralNoBodyLightPreview() {
    ComponentPreview(darkTheme = false) {
        EmptyState(
            icon = VerbeClairIcons.BookOpen,
            title = "Choose a verb to see its conjugation",
            body = null,
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-135
@Preview(widthDp = 329)
@Composable
private fun EmptyStateNeutralNoBodyDarkPreview() {
    ComponentPreview(darkTheme = true) {
        EmptyState(
            icon = VerbeClairIcons.BookOpen,
            title = "Choose a verb to see its conjugation",
            body = null,
        )
    }
}
