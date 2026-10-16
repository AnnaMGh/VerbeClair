package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.vamvisions.verbeclair.core.designsystem.icon.VerbeClairIcons

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-23
@Preview
@Composable
private fun IconButtonStandardLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairIconButton(icon = VerbeClairIcons.Tune, contentDescription = "List controls", onClick = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-23
@Preview
@Composable
private fun IconButtonStandardDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairIconButton(icon = VerbeClairIcons.Tune, contentDescription = "List controls", onClick = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-38
@Preview
@Composable
private fun IconButtonTonalLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairIconButton(
            icon = VerbeClairIcons.Tune,
            contentDescription = "List controls",
            onClick = {},
            style = IconButtonStyle.Tonal,
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-38
@Preview
@Composable
private fun IconButtonTonalDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairIconButton(
            icon = VerbeClairIcons.Tune,
            contentDescription = "List controls",
            onClick = {},
            style = IconButtonStyle.Tonal,
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-273
@Preview
@Composable
private fun IconButtonTonalWithBadgeLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairIconButton(
            icon = VerbeClairIcons.Tune,
            contentDescription = "List controls",
            onClick = {},
            style = IconButtonStyle.Tonal,
            badgeCount = 2,
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-273
@Preview
@Composable
private fun IconButtonTonalWithBadgeDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairIconButton(
            icon = VerbeClairIcons.Tune,
            contentDescription = "List controls",
            onClick = {},
            style = IconButtonStyle.Tonal,
            badgeCount = 2,
        )
    }
}
