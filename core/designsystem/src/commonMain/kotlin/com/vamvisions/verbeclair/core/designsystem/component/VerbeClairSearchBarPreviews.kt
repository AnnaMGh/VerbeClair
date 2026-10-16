package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-45
@Preview(widthDp = 260)
@Composable
private fun SearchBarEmptyLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairSearchBar(query = "", onQueryChange = {}, modifier = Modifier.fillMaxWidth())
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-45
@Preview(widthDp = 260)
@Composable
private fun SearchBarEmptyDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairSearchBar(query = "", onQueryChange = {}, modifier = Modifier.fillMaxWidth())
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-55
@Preview(widthDp = 260)
@Composable
private fun SearchBarFilledLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairSearchBar(query = "su", onQueryChange = {}, modifier = Modifier.fillMaxWidth())
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-55
@Preview(widthDp = 260)
@Composable
private fun SearchBarFilledDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairSearchBar(query = "su", onQueryChange = {}, modifier = Modifier.fillMaxWidth())
    }
}
