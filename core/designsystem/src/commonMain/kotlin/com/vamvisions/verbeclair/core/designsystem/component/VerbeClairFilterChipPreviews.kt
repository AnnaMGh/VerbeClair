package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-92
@Preview
@Composable
private fun FilterChipOffLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairFilterChip(label = "1st group", selected = false, onClick = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-92
@Preview
@Composable
private fun FilterChipOffDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairFilterChip(label = "1st group", selected = false, onClick = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-97
@Preview
@Composable
private fun FilterChipOnLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairFilterChip(label = "être", selected = true, onClick = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-97
@Preview
@Composable
private fun FilterChipOnDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairFilterChip(label = "être", selected = true, onClick = {})
    }
}
