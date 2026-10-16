package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-33
@Preview(widthDp = 393)
@Composable
private fun VerbCellDefaultLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbCell(
            infinitive = "aller",
            meta = "3rd group · être · non-reflexive · very common",
            isFavourite = false,
            onClick = {},
            onTestClick = {},
            onFavouriteClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-33
@Preview(widthDp = 393)
@Composable
private fun VerbCellDefaultDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbCell(
            infinitive = "aller",
            meta = "3rd group · être · non-reflexive · very common",
            isFavourite = false,
            onClick = {},
            onTestClick = {},
            onFavouriteClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-40
@Preview(widthDp = 393)
@Composable
private fun VerbCellMatchedFormLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbCell(
            infinitive = "être",
            meta = "3rd group · avoir · non-reflexive · very common",
            matchedForm = "suis",
            isFavourite = false,
            onClick = {},
            onTestClick = {},
            onFavouriteClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-40
@Preview(widthDp = 393)
@Composable
private fun VerbCellMatchedFormDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbCell(
            infinitive = "être",
            meta = "3rd group · avoir · non-reflexive · very common",
            matchedForm = "suis",
            isFavourite = false,
            onClick = {},
            onTestClick = {},
            onFavouriteClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-199
@Preview(widthDp = 393)
@Composable
private fun VerbCellScoreAndFavouriteLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbCell(
            infinitive = "être",
            meta = "3rd group · avoir · non-reflexive · very common",
            matchedForm = "suis",
            score = "80%",
            isFavourite = true,
            onClick = {},
            onTestClick = {},
            onFavouriteClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-199
@Preview(widthDp = 393)
@Composable
private fun VerbCellScoreAndFavouriteDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbCell(
            infinitive = "être",
            meta = "3rd group · avoir · non-reflexive · very common",
            matchedForm = "suis",
            score = "80%",
            isFavourite = true,
            onClick = {},
            onTestClick = {},
            onFavouriteClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-40
@Preview(widthDp = 393)
@Composable
private fun VerbCellLongInfinitiveWithMatchedFormLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbCell(
            infinitive = "se désintéresser",
            meta = "1st group · être · reflexive · uncommon",
            matchedForm = "désintéressions",
            score = "80%",
            isFavourite = false,
            onClick = {},
            onTestClick = {},
            onFavouriteClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-40
@Preview(widthDp = 393)
@Composable
private fun VerbCellLongInfinitiveWithMatchedFormDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbCell(
            infinitive = "se désintéresser",
            meta = "1st group · être · reflexive · uncommon",
            matchedForm = "désintéressions",
            score = "80%",
            isFavourite = false,
            onClick = {},
            onTestClick = {},
            onFavouriteClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=88-1053
@Preview(widthDp = 360)
@Composable
private fun VerbCellSelectedTabletLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbCell(
            infinitive = "terminer",
            meta = "1st group · avoir · non-reflexive · common",
            score = "80%",
            isFavourite = false,
            selected = true,
            onClick = {},
            onTestClick = {},
            onFavouriteClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=88-1053
@Preview(widthDp = 360)
@Composable
private fun VerbCellSelectedTabletDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbCell(
            infinitive = "terminer",
            meta = "1st group · avoir · non-reflexive · common",
            score = "80%",
            isFavourite = false,
            selected = true,
            onClick = {},
            onTestClick = {},
            onFavouriteClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=87-9584
@Preview(widthDp = 386)
@Composable
private fun VerbCellLandscapeColumnLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbCell(
            infinitive = "abaisser",
            meta = "1st group · avoir · non-reflexive · uncommon",
            isFavourite = false,
            onClick = {},
            onTestClick = {},
            onFavouriteClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=87-9584
@Preview(widthDp = 386)
@Composable
private fun VerbCellLandscapeColumnDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbCell(
            infinitive = "abaisser",
            meta = "1st group · avoir · non-reflexive · uncommon",
            isFavourite = false,
            onClick = {},
            onTestClick = {},
            onFavouriteClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=88-1032
@Preview(widthDp = 360)
@Composable
private fun VerbCellTabletPaneLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbCell(
            infinitive = "tenir",
            meta = "3rd group · avoir · non-reflexive · very common",
            isFavourite = false,
            onClick = {},
            onTestClick = {},
            onFavouriteClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=88-1032
@Preview(widthDp = 360)
@Composable
private fun VerbCellTabletPaneDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbCell(
            infinitive = "tenir",
            meta = "3rd group · avoir · non-reflexive · very common",
            isFavourite = false,
            onClick = {},
            onTestClick = {},
            onFavouriteClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-199
@Preview(widthDp = 393, fontScale = 2f)
@Composable
private fun VerbCellLargeFontLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbCell(
            infinitive = "être",
            meta = "3rd group · avoir · non-reflexive · very common",
            matchedForm = "suis",
            score = "80%",
            isFavourite = true,
            onClick = {},
            onTestClick = {},
            onFavouriteClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-199
@Preview(widthDp = 393, fontScale = 2f)
@Composable
private fun VerbCellLargeFontDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbCell(
            infinitive = "être",
            meta = "3rd group · avoir · non-reflexive · very common",
            matchedForm = "suis",
            score = "80%",
            isFavourite = true,
            onClick = {},
            onTestClick = {},
            onFavouriteClick = {},
        )
    }
}
