package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-64
@Preview
@Composable
private fun ButtonFilledLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairButton(text = "Clear filters", onClick = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-64
@Preview
@Composable
private fun ButtonFilledDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairButton(text = "Clear filters", onClick = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-72
@Preview
@Composable
private fun ButtonTonalLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairButton(text = "Clear filters", onClick = {}, style = ButtonStyle.Tonal)
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-72
@Preview
@Composable
private fun ButtonTonalDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairButton(text = "Clear filters", onClick = {}, style = ButtonStyle.Tonal)
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-80
@Preview
@Composable
private fun ButtonOutlinedLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairButton(text = "Clear filters", onClick = {}, style = ButtonStyle.Outlined)
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-80
@Preview
@Composable
private fun ButtonOutlinedDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairButton(text = "Clear filters", onClick = {}, style = ButtonStyle.Outlined)
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-88
@Preview
@Composable
private fun ButtonTextLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairButton(text = "Clear filters", onClick = {}, style = ButtonStyle.Text)
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-88
@Preview
@Composable
private fun ButtonTextDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairButton(text = "Clear filters", onClick = {}, style = ButtonStyle.Text)
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-211
@Preview
@Composable
private fun ButtonDisabledLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairButton(text = "Clear filters", onClick = {}, enabled = false)
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-211
@Preview
@Composable
private fun ButtonDisabledDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairButton(text = "Clear filters", onClick = {}, enabled = false)
    }
}
