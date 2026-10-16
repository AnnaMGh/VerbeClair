package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-151
@Preview(widthDp = 393)
@Composable
private fun LinearProgressLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairLinearProgress()
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-151
@Preview(widthDp = 393)
@Composable
private fun LinearProgressDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairLinearProgress()
    }
}
