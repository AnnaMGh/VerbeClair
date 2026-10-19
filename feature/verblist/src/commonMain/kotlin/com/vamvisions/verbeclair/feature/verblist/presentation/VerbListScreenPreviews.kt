package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.vamvisions.verbeclair.core.designsystem.component.BottomSheetHandle
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.domain.verb.ListChoices
import com.vamvisions.verbeclair.core.domain.verb.ListTab
import com.vamvisions.verbeclair.core.presentation.window.WindowLayout
import com.vamvisions.verbeclair.feature.verblist.presentation.components.ListControlsContent
import kotlin.time.Duration

// Screen previews, one per Figma state, in light and dark (design D13). Sizes are the Figma frames
// without the status bar and the bottom bar or rail, which the app shell draws.

@Composable
private fun ScreenPreview(darkTheme: Boolean, content: @Composable () -> Unit) {
    VerbeClairTheme(darkTheme = darkTheme) { content() }
}

private val GroupedChoices = PreviewData.groupedChoices

/** The list controls sheet as drawn by [components.ListControlsSheet], without the modal window. */
@Composable
private fun SheetFrame(choices: ListChoices) {
    val radius = VerbeClairTheme.radius.xl
    Column(
        Modifier.fillMaxWidth().background(
            MaterialTheme.colorScheme.surfaceContainerLow,
            RoundedCornerShape(topStart = radius, topEnd = radius),
        ),
    ) {
        BottomSheetHandle(Modifier.fillMaxWidth())
        ListControlsContent(choices = choices, onAction = {})
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-2
@Preview(widthDp = 393, heightDp = 728)
@Composable
private fun L01DefaultLightPreview() {
    ScreenPreview(darkTheme = false) {
        VerbListScreen(
            state = VerbListState(favouritesCount = 12),
            verbs = previewItems(PreviewData.default),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=13-913
@Preview(widthDp = 393, heightDp = 728)
@Composable
private fun L01DefaultDarkPreview() {
    ScreenPreview(darkTheme = true) {
        VerbListScreen(
            state = VerbListState(favouritesCount = 12),
            verbs = previewItems(PreviewData.default),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-138
@Preview(widthDp = 393, heightDp = 728)
@Composable
private fun L02SearchSuLightPreview() {
    ScreenPreview(darkTheme = false) {
        VerbListScreen(
            state = VerbListState(query = "su", favouritesCount = 12),
            verbs = previewItems(PreviewData.searchSu),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=13-936
@Preview(widthDp = 393, heightDp = 728)
@Composable
private fun L02SearchSuDarkPreview() {
    ScreenPreview(darkTheme = true) {
        VerbListScreen(
            state = VerbListState(query = "su", favouritesCount = 12),
            verbs = previewItems(PreviewData.searchSu),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-259
@Preview(widthDp = 393, heightDp = 728)
@Composable
private fun L03GroupedLightPreview() {
    ScreenPreview(darkTheme = false) {
        VerbListScreen(
            state = VerbListState(choices = GroupedChoices, favouritesCount = 12),
            verbs = previewItems(PreviewData.grouped),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=13-957
@Preview(widthDp = 393, heightDp = 728)
@Composable
private fun L03GroupedDarkPreview() {
    ScreenPreview(darkTheme = true) {
        VerbListScreen(
            state = VerbListState(choices = GroupedChoices, favouritesCount = 12),
            verbs = previewItems(PreviewData.grouped),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-405
@Preview(widthDp = 393)
@Composable
private fun L04ListControlsSheetLightPreview() {
    ScreenPreview(darkTheme = false) {
        SheetFrame(choices = ListChoices(filters = GroupedChoices.filters))
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=13-985
@Preview(widthDp = 393)
@Composable
private fun L04ListControlsSheetDarkPreview() {
    ScreenPreview(darkTheme = true) {
        SheetFrame(choices = ListChoices(filters = GroupedChoices.filters))
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-595
@Preview(widthDp = 393, heightDp = 728)
@Composable
private fun L05EmptyResultLightPreview() {
    ScreenPreview(darkTheme = false) {
        VerbListScreen(
            state = VerbListState(query = "xyz", favouritesCount = 12),
            verbs = previewItems(emptyList()),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=13-1045
@Preview(widthDp = 393, heightDp = 728)
@Composable
private fun L05EmptyResultDarkPreview() {
    ScreenPreview(darkTheme = true) {
        VerbListScreen(
            state = VerbListState(query = "xyz", favouritesCount = 12),
            verbs = previewItems(emptyList()),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-687
@Preview(widthDp = 393, heightDp = 728)
@Composable
private fun L06NoDataLightPreview() {
    ScreenPreview(darkTheme = false) {
        VerbListScreen(
            state = VerbListState(isEmptyDatabase = true, isSyncing = true, favouritesCount = 12),
            verbs = previewItems(emptyList()),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=13-1057
@Preview(widthDp = 393, heightDp = 728)
@Composable
private fun L06NoDataDarkPreview() {
    ScreenPreview(darkTheme = true) {
        VerbListScreen(
            state = VerbListState(isEmptyDatabase = true, isSyncing = true, favouritesCount = 12),
            verbs = previewItems(emptyList()),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-762
@Preview(widthDp = 393, heightDp = 728)
@Composable
private fun L07SkeletonLightPreview() {
    ScreenPreview(darkTheme = false) {
        VerbListScreen(
            state = VerbListState(favouritesCount = 12),
            verbs = previewLoadingItems(),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
            skeletonDelay = Duration.ZERO,
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=13-1070
@Preview(widthDp = 393, heightDp = 728)
@Composable
private fun L07SkeletonDarkPreview() {
    ScreenPreview(darkTheme = true) {
        VerbListScreen(
            state = VerbListState(favouritesCount = 12),
            verbs = previewLoadingItems(),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
            skeletonDelay = Duration.ZERO,
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=55-3940
@Preview(widthDp = 393, heightDp = 728)
@Composable
private fun L08FavouritesLightPreview() {
    ScreenPreview(darkTheme = false) {
        VerbListScreen(
            state = VerbListState(tab = ListTab.FAVOURITES, favouritesCount = 3),
            verbs = previewItems(PreviewData.favourites),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=55-4034
@Preview(widthDp = 393, heightDp = 728)
@Composable
private fun L08FavouritesDarkPreview() {
    ScreenPreview(darkTheme = true) {
        VerbListScreen(
            state = VerbListState(tab = ListTab.FAVOURITES, favouritesCount = 3),
            verbs = previewItems(PreviewData.favourites),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=55-4047
@Preview(widthDp = 393, heightDp = 728)
@Composable
private fun L09NoFavouritesLightPreview() {
    ScreenPreview(darkTheme = false) {
        VerbListScreen(
            state = VerbListState(tab = ListTab.FAVOURITES),
            verbs = previewItems(emptyList()),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=55-4119
@Preview(widthDp = 393, heightDp = 728)
@Composable
private fun L09NoFavouritesDarkPreview() {
    ScreenPreview(darkTheme = true) {
        VerbListScreen(
            state = VerbListState(tab = ListTab.FAVOURITES),
            verbs = previewItems(emptyList()),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=87-9531
@Preview(widthDp = 772, heightDp = 393)
@Composable
private fun PL01LandscapeLightPreview() {
    ScreenPreview(darkTheme = false) {
        VerbListScreen(
            state = VerbListState(favouritesCount = 12),
            verbs = previewItems(PreviewData.default),
            layout = WindowLayout.PhoneLandscape,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=90-12103
@Preview(widthDp = 772, heightDp = 393)
@Composable
private fun PL01LandscapeDarkPreview() {
    ScreenPreview(darkTheme = true) {
        VerbListScreen(
            state = VerbListState(favouritesCount = 12),
            verbs = previewItems(PreviewData.default),
            layout = WindowLayout.PhoneLandscape,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=88-744
@Preview(widthDp = 754, heightDp = 1150)
@Composable
private fun TP01TabletPortraitLightPreview() {
    ScreenPreview(darkTheme = false) {
        VerbListScreen(
            state = VerbListState(favouritesCount = 12, selectedSlug = "terminer"),
            verbs = previewItems(PreviewData.tablet),
            layout = WindowLayout.ListDetail,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=90-12250
@Preview(widthDp = 754, heightDp = 1150)
@Composable
private fun TP01TabletPortraitDarkPreview() {
    ScreenPreview(darkTheme = true) {
        VerbListScreen(
            state = VerbListState(favouritesCount = 12, selectedSlug = "terminer"),
            verbs = previewItems(PreviewData.tablet),
            layout = WindowLayout.ListDetail,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=89-1153
@Preview(widthDp = 1114, heightDp = 790)
@Composable
private fun TL01TabletLandscapeLightPreview() {
    ScreenPreview(darkTheme = false) {
        VerbListScreen(
            state = VerbListState(favouritesCount = 12, selectedSlug = "terminer"),
            verbs = previewItems(PreviewData.tablet),
            layout = WindowLayout.ListDetail,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=90-12336
@Preview(widthDp = 1114, heightDp = 790)
@Composable
private fun TL01TabletLandscapeDarkPreview() {
    ScreenPreview(darkTheme = true) {
        VerbListScreen(
            state = VerbListState(favouritesCount = 12, selectedSlug = "terminer"),
            verbs = previewItems(PreviewData.tablet),
            layout = WindowLayout.ListDetail,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=87-9531
// No frame shows a grouped list in landscape; PL-01's annotation asks for full-width sticky section headers.
@Preview(widthDp = 772, heightDp = 393)
@Composable
private fun PL01LandscapeGroupedLightPreview() {
    ScreenPreview(darkTheme = false) {
        VerbListScreen(
            state = VerbListState(choices = GroupedChoices, favouritesCount = 12),
            verbs = previewItems(PreviewData.grouped),
            layout = WindowLayout.PhoneLandscape,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=90-12103
// No frame shows a grouped list in landscape; PL-01's annotation asks for full-width sticky section headers.
@Preview(widthDp = 772, heightDp = 393)
@Composable
private fun PL01LandscapeGroupedDarkPreview() {
    ScreenPreview(darkTheme = true) {
        VerbListScreen(
            state = VerbListState(choices = GroupedChoices, favouritesCount = 12),
            verbs = previewItems(PreviewData.grouped),
            layout = WindowLayout.PhoneLandscape,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Largest system font size (task 9.2): no frame exists; these use the default-size frames.

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-2
@Preview(widthDp = 393, heightDp = 728, fontScale = 2f)
@Composable
private fun L01DefaultFontScale2Preview() {
    ScreenPreview(darkTheme = false) {
        VerbListScreen(
            state = VerbListState(favouritesCount = 12),
            verbs = previewItems(PreviewData.default),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-138
@Preview(widthDp = 393, heightDp = 728, fontScale = 2f)
@Composable
private fun L02SearchSuFontScale2Preview() {
    ScreenPreview(darkTheme = false) {
        VerbListScreen(
            state = VerbListState(query = "su", favouritesCount = 12),
            verbs = previewItems(PreviewData.searchSu),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// French and Romanian (task 9.3): no frame exists; these use the English frames.

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-259
@Preview(widthDp = 393, heightDp = 728, locale = "fr")
@Composable
private fun L03GroupedFrenchPreview() {
    ScreenPreview(darkTheme = false) {
        VerbListScreen(
            state = VerbListState(choices = GroupedChoices, favouritesCount = 12),
            verbs = previewItems(PreviewData.grouped),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-259
@Preview(widthDp = 393, heightDp = 728, locale = "ro")
@Composable
private fun L03GroupedRomanianPreview() {
    ScreenPreview(darkTheme = false) {
        VerbListScreen(
            state = VerbListState(choices = GroupedChoices, favouritesCount = 12),
            verbs = previewItems(PreviewData.grouped),
            layout = WindowLayout.Compact,
            onAction = {},
            onVerbClick = {},
            onTestClick = {},
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-405
@Preview(widthDp = 393, locale = "fr")
@Composable
private fun L04ListControlsSheetFrenchPreview() {
    ScreenPreview(darkTheme = false) {
        SheetFrame(choices = ListChoices(filters = GroupedChoices.filters))
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-405
@Preview(widthDp = 393, locale = "ro")
@Composable
private fun L04ListControlsSheetRomanianPreview() {
    ScreenPreview(darkTheme = false) {
        SheetFrame(choices = ListChoices(filters = GroupedChoices.filters))
    }
}
