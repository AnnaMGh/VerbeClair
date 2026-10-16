package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-107
@Preview(widthDp = 329)
@Composable
private fun RadioRowSelectedLightPreview() {
    ComponentPreview(darkTheme = false) {
        RadioRow(label = "A → Z", selected = true, onClick = {}, modifier = Modifier.fillMaxWidth())
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-107
@Preview(widthDp = 329)
@Composable
private fun RadioRowSelectedDarkPreview() {
    ComponentPreview(darkTheme = true) {
        RadioRow(label = "A → Z", selected = true, onClick = {}, modifier = Modifier.fillMaxWidth())
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-112
@Preview(widthDp = 329)
@Composable
private fun RadioRowNotSelectedLightPreview() {
    ComponentPreview(darkTheme = false) {
        RadioRow(label = "Z → A", selected = false, onClick = {}, modifier = Modifier.fillMaxWidth())
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-112
@Preview(widthDp = 329)
@Composable
private fun RadioRowNotSelectedDarkPreview() {
    ComponentPreview(darkTheme = true) {
        RadioRow(label = "Z → A", selected = false, onClick = {}, modifier = Modifier.fillMaxWidth())
    }
}
