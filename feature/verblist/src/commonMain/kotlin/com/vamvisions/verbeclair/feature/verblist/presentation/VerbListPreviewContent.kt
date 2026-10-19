package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vamvisions.verbeclair.core.presentation.window.WindowLayout

/**
 * Screen 1 with the rows of its default Figma frame for [layout], for the app shell's previews:
 * L-01 / PL-01 (A → Z) on phones, TP-01 / TL-01 (terminer selected) in the list–detail layout.
 */
@Composable
fun VerbListPreviewContent(layout: WindowLayout, modifier: Modifier = Modifier) {
    val isListDetail = layout == WindowLayout.ListDetail
    VerbListScreen(
        state = VerbListState(favouritesCount = 12, selectedSlug = if (isListDetail) "terminer" else null),
        verbs = previewItems(if (isListDetail) PreviewData.tablet else PreviewData.default),
        layout = layout,
        onAction = {},
        onVerbClick = {},
        onTestClick = {},
        modifier = modifier,
    )
}
