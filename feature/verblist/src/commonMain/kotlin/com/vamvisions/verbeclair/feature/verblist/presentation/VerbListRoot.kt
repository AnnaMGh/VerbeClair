package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.vamvisions.verbeclair.core.designsystem.component.VerbeClairSnackbar
import com.vamvisions.verbeclair.core.designsystem.icon.VerbeClairIcons
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.presentation.event.ObserveAsEvents
import com.vamvisions.verbeclair.core.presentation.window.WindowLayout
import com.vamvisions.verbeclair.core.presentation.window.currentWindowIsMediumWidth
import com.vamvisions.verbeclair.core.presentation.window.currentWindowLayout
import com.vamvisions.verbeclair.feature.verblist.generated.resources.Res
import com.vamvisions.verbeclair.feature.verblist.generated.resources.favourite_update_failed
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

// Figma `Snackbar` (7:144) is 361 dp wide.
private val SnackbarMaxWidth = 361.dp

/** Test tag of the snackbar host, for UI tests. */
const val VERB_LIST_SNACKBAR_TAG = "verbList.snackbar"

/**
 * Screen 1 wired to its ViewModel (design D10). The grid state is hoisted and saved here, so rotating
 * or resizing (which changes the screen's structure) keeps the scroll position. One-off [VerbListEvent]s
 * are shown as a snackbar at the bottom of the content, above the bottom bar on phones.
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

    val snackbarHostState = remember { SnackbarHostState() }
    val favouriteFailedMessage = stringResource(Res.string.favourite_update_failed)

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            VerbListEvent.FavouriteUpdateFailed -> {
                snackbarHostState.showSnackbar(favouriteFailedMessage, duration = SnackbarDuration.Short)
            }

            is VerbListEvent.OpenTestSetup -> {
                onTestClick(event.slug)
            }
        }
    }

    Box(modifier) {
        VerbListScreen(
            state = state,
            verbs = verbs,
            layout = layout,
            onAction = viewModel::onAction,
            onVerbClick = onVerbClick,
            onTestClick = onTestClick,
            gridState = gridState,
            isMediumWidth = isMediumWidth,
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    // The bottom bar (Compact) already sits below this box and takes the navigation inset.
                    .then(
                        if (layout ==
                            WindowLayout.Compact
                        ) {
                            Modifier
                        } else {
                            Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        },
                    ).padding(VerbeClairTheme.spacing.space16)
                    .widthIn(max = SnackbarMaxWidth)
                    .testTag(VERB_LIST_SNACKBAR_TAG),
        ) { data ->
            VerbeClairSnackbar(message = data.visuals.message, icon = VerbeClairIcons.Alert)
        }
    }
}
