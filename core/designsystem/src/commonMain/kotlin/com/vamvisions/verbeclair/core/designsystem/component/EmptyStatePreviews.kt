package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.vamvisions.verbeclair.core.designsystem.icon.VerbeClairIcons

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
