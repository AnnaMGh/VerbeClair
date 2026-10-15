package com.vamvisions.verbeclair.core.designsystem.flag

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme

// Bar height used by the Figma bar previews (task 7.3: an 80 dp bottom bar).
private val PreviewBarHeight = 80.dp

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=95-213
@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun FlagScreenBackgroundLightPreview() {
    VerbeClairTheme(darkTheme = false) { Box(Modifier.fillMaxSize().flagScreenBackground()) }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=95-213
@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun FlagScreenBackgroundDarkPreview() {
    VerbeClairTheme(darkTheme = true) { Box(Modifier.fillMaxSize().flagScreenBackground()) }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=95-211
@Preview(widthDp = 393, heightDp = 80)
@Composable
private fun FlagBarBackgroundLightPreview() {
    VerbeClairTheme(darkTheme = false) {
        Box(Modifier.fillMaxWidth().height(PreviewBarHeight).flagBarBackground())
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=95-211
@Preview(widthDp = 393, heightDp = 80)
@Composable
private fun FlagBarBackgroundDarkPreview() {
    VerbeClairTheme(darkTheme = true) {
        Box(Modifier.fillMaxWidth().height(PreviewBarHeight).flagBarBackground())
    }
}
