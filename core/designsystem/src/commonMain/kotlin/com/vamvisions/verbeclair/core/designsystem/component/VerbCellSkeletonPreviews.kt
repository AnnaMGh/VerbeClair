package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-49
@Preview(widthDp = 393)
@Composable
private fun VerbCellSkeletonLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbCellSkeleton()
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-49
@Preview(widthDp = 393)
@Composable
private fun VerbCellSkeletonDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbCellSkeleton()
    }
}
