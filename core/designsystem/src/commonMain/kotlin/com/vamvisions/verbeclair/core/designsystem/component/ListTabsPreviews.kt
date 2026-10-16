package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-131
@Preview(widthDp = 393)
@Composable
private fun ListTabsAllLightPreview() {
    ComponentPreview(darkTheme = false) {
        ListTabs(selected = ListTab.All, favouritesCount = 12, onSelect = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-131
@Preview(widthDp = 393)
@Composable
private fun ListTabsAllDarkPreview() {
    ComponentPreview(darkTheme = true) {
        ListTabs(selected = ListTab.All, favouritesCount = 12, onSelect = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-138
@Preview(widthDp = 393)
@Composable
private fun ListTabsFavouritesLightPreview() {
    ComponentPreview(darkTheme = false) {
        ListTabs(selected = ListTab.Favourites, favouritesCount = 12, onSelect = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-138
@Preview(widthDp = 393)
@Composable
private fun ListTabsFavouritesDarkPreview() {
    ComponentPreview(darkTheme = true) {
        ListTabs(selected = ListTab.Favourites, favouritesCount = 12, onSelect = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=88-808
@Preview(widthDp = 360)
@Composable
private fun ListTabsTabletPaneLightPreview() {
    ComponentPreview(darkTheme = false) {
        ListTabs(selected = ListTab.All, favouritesCount = 12, onSelect = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=88-808
@Preview(widthDp = 360)
@Composable
private fun ListTabsTabletPaneDarkPreview() {
    ComponentPreview(darkTheme = true) {
        ListTabs(selected = ListTab.All, favouritesCount = 12, onSelect = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=87-9563
@Preview(widthDp = 272)
@Composable
private fun ListTabsLandscapeTopBarLightPreview() {
    ComponentPreview(darkTheme = false) {
        ListTabs(selected = ListTab.All, favouritesCount = 12, onSelect = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=87-9563
@Preview(widthDp = 272)
@Composable
private fun ListTabsLandscapeTopBarDarkPreview() {
    ComponentPreview(darkTheme = true) {
        ListTabs(selected = ListTab.All, favouritesCount = 12, onSelect = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-131
@Preview(widthDp = 393, fontScale = 2f)
@Composable
private fun ListTabsLargeFontLightPreview() {
    ComponentPreview(darkTheme = false) {
        ListTabs(selected = ListTab.All, favouritesCount = 12, onSelect = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-131
@Preview(widthDp = 393, fontScale = 2f)
@Composable
private fun ListTabsLargeFontDarkPreview() {
    ComponentPreview(darkTheme = true) {
        ListTabs(selected = ListTab.All, favouritesCount = 12, onSelect = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=87-9563
@Preview(widthDp = 272, locale = "fr")
@Composable
private fun ListTabsFrenchLightPreview() {
    ComponentPreview(darkTheme = false) {
        ListTabs(selected = ListTab.Favourites, favouritesCount = 1234, onSelect = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=87-9563
@Preview(widthDp = 272, locale = "fr")
@Composable
private fun ListTabsFrenchDarkPreview() {
    ComponentPreview(darkTheme = true) {
        ListTabs(selected = ListTab.Favourites, favouritesCount = 1234, onSelect = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=87-9563
@Preview(widthDp = 272, locale = "ro")
@Composable
private fun ListTabsRomanianLightPreview() {
    ComponentPreview(darkTheme = false) {
        ListTabs(selected = ListTab.Favourites, favouritesCount = 1234, onSelect = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=87-9563
@Preview(widthDp = 272, locale = "ro")
@Composable
private fun ListTabsRomanianDarkPreview() {
    ComponentPreview(darkTheme = true) {
        ListTabs(selected = ListTab.Favourites, favouritesCount = 1234, onSelect = {})
    }
}
