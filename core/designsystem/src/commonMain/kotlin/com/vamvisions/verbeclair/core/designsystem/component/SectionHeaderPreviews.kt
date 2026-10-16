package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-45
@Preview(widthDp = 393)
@Composable
private fun SectionHeaderLightPreview() {
    ComponentPreview(darkTheme = false) {
        SectionHeader(title = "1st group", count = "5,684 verbs")
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-45
@Preview(widthDp = 393)
@Composable
private fun SectionHeaderDarkPreview() {
    ComponentPreview(darkTheme = true) {
        SectionHeader(title = "1st group", count = "5,684 verbs")
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-339
@Preview(widthDp = 393)
@Composable
private fun SectionHeaderTitleOnlyLightPreview() {
    ComponentPreview(darkTheme = false) {
        SectionHeader(title = "1st group", count = null)
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-339
@Preview(widthDp = 393)
@Composable
private fun SectionHeaderTitleOnlyDarkPreview() {
    ComponentPreview(darkTheme = true) {
        SectionHeader(title = "1st group", count = null)
    }
}
