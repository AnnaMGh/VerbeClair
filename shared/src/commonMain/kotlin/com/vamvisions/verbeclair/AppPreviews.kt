package com.vamvisions.verbeclair

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// App root with the VerbeClair theme and flag background, per supported configuration.
// No screen is implemented yet, so each preview points at the Figma Screens page.

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=3-65
@Preview(name = "Phone portrait, light", widthDp = 393, heightDp = 852)
@Composable
private fun AppPhonePortraitLightPreview() = App(darkTheme = false)

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=3-65
@Preview(name = "Phone portrait, dark", widthDp = 393, heightDp = 852)
@Composable
private fun AppPhonePortraitDarkPreview() = App(darkTheme = true)

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=3-65
@Preview(name = "Phone landscape, light", widthDp = 852, heightDp = 393)
@Composable
private fun AppPhoneLandscapeLightPreview() = App(darkTheme = false)

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=3-65
@Preview(name = "Phone landscape, dark", widthDp = 852, heightDp = 393)
@Composable
private fun AppPhoneLandscapeDarkPreview() = App(darkTheme = true)

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=3-65
@Preview(name = "Tablet portrait, light", widthDp = 834, heightDp = 1194)
@Composable
private fun AppTabletPortraitLightPreview() = App(darkTheme = false)

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=3-65
@Preview(name = "Tablet portrait, dark", widthDp = 834, heightDp = 1194)
@Composable
private fun AppTabletPortraitDarkPreview() = App(darkTheme = true)

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=3-65
@Preview(name = "Tablet landscape, light", widthDp = 1194, heightDp = 834)
@Composable
private fun AppTabletLandscapeLightPreview() = App(darkTheme = false)

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=3-65
@Preview(name = "Tablet landscape, dark", widthDp = 1194, heightDp = 834)
@Composable
private fun AppTabletLandscapeDarkPreview() = App(darkTheme = true)
