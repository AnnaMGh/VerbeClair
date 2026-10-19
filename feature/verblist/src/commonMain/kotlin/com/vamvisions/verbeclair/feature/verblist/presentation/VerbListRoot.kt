package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.vamvisions.verbeclair.core.presentation.window.WindowLayout
import com.vamvisions.verbeclair.core.presentation.window.currentWindowIsMediumWidth
import com.vamvisions.verbeclair.core.presentation.window.currentWindowLayout
import org.koin.compose.viewmodel.koinViewModel

/**
 * Screen 1 wired to its ViewModel (design D10). The grid state is hoisted and saved here, so rotating
 * or resizing (which changes the screen's structure) keeps the scroll position.
 */
@Composable
fun VerbListRoot(
    onVerbClick: (String) -> Unit,
    onTestClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VerbListViewModel = koinViewModel(),
    layout: WindowLayout = currentWindowLayout(),
    isMediumWidth: Boolean = currentWindowIsMediumWidth(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val verbs = viewModel.verbs.collectAsLazyPagingItems()
    val gridState = rememberLazyGridState()
    VerbListScreen(
        state = state,
        verbs = verbs,
        layout = layout,
        onAction = viewModel::onAction,
        onVerbClick = onVerbClick,
        onTestClick = onTestClick,
        modifier = modifier,
        gridState = gridState,
        isMediumWidth = isMediumWidth,
    )
}
