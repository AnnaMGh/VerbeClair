package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.vamvisions.verbeclair.core.designsystem.component.BottomSheetHandle
import com.vamvisions.verbeclair.core.designsystem.component.EmptyState
import com.vamvisions.verbeclair.core.designsystem.component.IconButtonStyle
import com.vamvisions.verbeclair.core.designsystem.component.ListTabs
import com.vamvisions.verbeclair.core.designsystem.component.SectionHeader
import com.vamvisions.verbeclair.core.designsystem.component.VerbCell
import com.vamvisions.verbeclair.core.designsystem.component.VerbCellSkeleton
import com.vamvisions.verbeclair.core.designsystem.component.VerbeClairButton
import com.vamvisions.verbeclair.core.designsystem.component.VerbeClairIconButton
import com.vamvisions.verbeclair.core.designsystem.component.VerbeClairLinearProgress
import com.vamvisions.verbeclair.core.designsystem.component.VerbeClairSearchBar
import com.vamvisions.verbeclair.core.designsystem.flag.flagScreenBackground
import com.vamvisions.verbeclair.core.designsystem.icon.VerbeClairIcons
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.domain.verb.ListChoices
import com.vamvisions.verbeclair.core.domain.verb.ListTab
import com.vamvisions.verbeclair.core.presentation.window.WindowLayout
import com.vamvisions.verbeclair.feature.verblist.generated.resources.Res
import com.vamvisions.verbeclair.feature.verblist.generated.resources.detail_empty_title
import com.vamvisions.verbeclair.feature.verblist.generated.resources.empty_result_action
import com.vamvisions.verbeclair.feature.verblist.generated.resources.empty_result_body
import com.vamvisions.verbeclair.feature.verblist.generated.resources.empty_result_title
import com.vamvisions.verbeclair.feature.verblist.generated.resources.list_controls
import com.vamvisions.verbeclair.feature.verblist.generated.resources.no_data_body
import com.vamvisions.verbeclair.feature.verblist.generated.resources.no_data_title
import com.vamvisions.verbeclair.feature.verblist.generated.resources.no_favourites_body
import com.vamvisions.verbeclair.feature.verblist.generated.resources.no_favourites_title
import com.vamvisions.verbeclair.feature.verblist.generated.resources.score_percent
import com.vamvisions.verbeclair.feature.verblist.presentation.components.ActiveChoicesRow
import com.vamvisions.verbeclair.feature.verblist.presentation.components.ListControlsContent
import com.vamvisions.verbeclair.feature.verblist.presentation.components.ListControlsSheet
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import com.vamvisions.verbeclair.core.designsystem.component.ListTab as TabUi

// Figma TP-01 / TL-01 (88:781): the list pane of the list–detail layout.
private val ListPaneWidth = 360.dp

// Figma PL-01 (87:9563): the tabs in the single top row of phone landscape.
private val LandscapeTabsWidth = 272.dp

/** A page shows its skeleton only when it takes longer than this to load (N-2). */
val SkeletonDelay: Duration = 150.milliseconds

private const val SKELETON_ROWS = 12

/** Test tags of the screen's regions, for UI tests. */
object VerbListTestTags {
    const val GRID = "verbList.grid"
    const val SKELETON = "verbList.skeleton"
    const val NEXT_PAGE_SKELETON = "verbList.nextPageSkeleton"
    const val DETAIL_PANE = "verbList.detailPane"

    fun sectionHeader(sectionId: String) = "verbList.section.$sectionId"
}

/**
 * Screen 1, the verb list (design D9), in the structure of [layout] (D8):
 * - [WindowLayout.Compact]: top bar, tabs row, active choices row, 1 column;
 * - [WindowLayout.PhoneLandscape]: one top row (search · tabs · controls), active choices row, 2 columns;
 * - [WindowLayout.ListDetail]: a 360 dp list pane in the portrait structure, a divider and the right pane.
 *
 * Cell taps report the slug to [onVerbClick] and test taps to [onTestClick]; the star only toggles the favourite.
 * In the list–detail layout on Medium width ([isMediumWidth]), back first clears the selection (N-15).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerbListScreen(
    state: VerbListState,
    verbs: LazyPagingItems<VerbListRowUi>,
    layout: WindowLayout,
    onAction: (VerbListAction) -> Unit,
    onVerbClick: (String) -> Unit,
    onTestClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    isMediumWidth: Boolean = false,
    skeletonDelay: Duration = SkeletonDelay,
) {
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = layout == WindowLayout.ListDetail && isMediumWidth && state.selectedSlug != null,
        onBackCompleted = { onAction(VerbListAction.OnClearSelection) },
    )
    val onCellClick: (String) -> Unit = { slug ->
        onAction(VerbListAction.OnSelectVerb(slug))
        onVerbClick(slug)
    }
    val screenModifier =
        modifier
            .fillMaxSize()
            .flagScreenBackground()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.End))
    when (layout) {
        WindowLayout.ListDetail -> {
            Row(screenModifier) {
                ListPane(
                    state = state,
                    verbs = verbs,
                    columns = 1,
                    combinedTopRow = false,
                    selectedSlug = state.selectedSlug,
                    // The rail and the right pane are next to the list; the list scrolls above the navigation bar.
                    bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                    gridState = gridState,
                    skeletonDelay = skeletonDelay,
                    onAction = onAction,
                    onCellClick = onCellClick,
                    onTestClick = onTestClick,
                    modifier = Modifier.width(ListPaneWidth).fillMaxHeight(),
                )
                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DetailPlaceholder(Modifier.weight(1f).fillMaxHeight().testTag(VerbListTestTags.DETAIL_PANE))
            }
        }

        else -> {
            ListPane(
                state = state,
                verbs = verbs,
                columns = if (layout == WindowLayout.PhoneLandscape) 2 else 1,
                combinedTopRow = layout == WindowLayout.PhoneLandscape,
                selectedSlug = null,
                bottomPadding =
                    if (layout == WindowLayout.PhoneLandscape) {
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    } else {
                        0.dp
                    },
                gridState = gridState,
                skeletonDelay = skeletonDelay,
                onAction = onAction,
                onCellClick = onCellClick,
                onTestClick = onTestClick,
                modifier = screenModifier,
                aboveBottomBar = layout == WindowLayout.Compact,
            )
        }
    }
    if (state.isSheetOpen) {
        ListControlsSheet(choices = state.choices, onAction = onAction)
    }
}

@Composable
private fun ListPane(
    state: VerbListState,
    verbs: LazyPagingItems<VerbListRowUi>,
    columns: Int,
    combinedTopRow: Boolean,
    selectedSlug: String?,
    bottomPadding: Dp,
    gridState: LazyGridState,
    skeletonDelay: Duration,
    onAction: (VerbListAction) -> Unit,
    onCellClick: (String) -> Unit,
    onTestClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    aboveBottomBar: Boolean = false,
) {
    Column(modifier) {
        if (combinedTopRow) {
            LandscapeTopRow(state, onAction)
        } else {
            TopBar(state, onAction)
            Tabs(state, onAction, Modifier.fillMaxWidth())
        }
        if (state.isEmptyDatabase && state.isSyncing) {
            VerbeClairLinearProgress(Modifier.fillMaxWidth())
        }
        if (!state.isEmptyDatabase && state.activeChoices.isNotEmpty()) {
            ActiveChoicesRow(
                choices = state.activeChoices,
                onRemove = { onAction(VerbListAction.OnRemoveChoice(it)) },
            )
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            ListContent(
                state = state,
                verbs = verbs,
                columns = columns,
                selectedSlug = selectedSlug,
                bottomPadding = bottomPadding,
                gridState = gridState,
                skeletonDelay = skeletonDelay,
                onAction = onAction,
                onCellClick = onCellClick,
                onTestClick = onTestClick,
                aboveBottomBar = aboveBottomBar,
            )
        }
    }
}

/** Search box and list-controls button (Figma 10:9): 16 dp start, 4 dp end and gap, 8 dp above and below. */
@Composable
private fun TopBar(state: VerbListState, onAction: (VerbListAction) -> Unit) {
    val spacing = VerbeClairTheme.spacing
    Row(
        modifier =
            Modifier.fillMaxWidth().padding(
                start = spacing.space16,
                end = spacing.space4,
                top = spacing.space8,
                bottom = spacing.space8,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Search(state, onAction, Modifier.weight(1f))
        Spacer(Modifier.size(spacing.space4))
        ListControlsButton(state, onAction)
    }
}

/** Phone landscape: search, tabs and list controls in one row (Figma PL-01 87:9557). */
@Composable
private fun LandscapeTopRow(state: VerbListState, onAction: (VerbListAction) -> Unit) {
    val spacing = VerbeClairTheme.spacing
    Row(
        modifier =
            Modifier.fillMaxWidth().padding(
                start = spacing.space16,
                end = spacing.space8,
                top = spacing.space8,
                bottom = spacing.space4,
            ),
        horizontalArrangement = Arrangement.spacedBy(spacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Search(state, onAction, Modifier.weight(1f))
        Tabs(state, onAction, Modifier.width(LandscapeTabsWidth))
        ListControlsButton(state, onAction)
    }
}

@Composable
private fun Search(
    state: VerbListState,
    onAction: (VerbListAction) -> Unit,
    modifier: Modifier,
) {
    VerbeClairSearchBar(
        query = state.query,
        onQueryChange = { onAction(VerbListAction.OnQueryChange(it)) },
        modifier = modifier,
    )
}

/** Standard with no filter; Tonal with a badge holding the number of selected filter values (L-6). */
@Composable
private fun ListControlsButton(state: VerbListState, onAction: (VerbListAction) -> Unit) {
    val filterCount = state.filterCount
    VerbeClairIconButton(
        icon = VerbeClairIcons.Tune,
        contentDescription = stringResource(Res.string.list_controls),
        onClick = { onAction(VerbListAction.OnOpenSheet) },
        style = if (filterCount > 0) IconButtonStyle.Tonal else IconButtonStyle.Standard,
        badgeCount = filterCount.takeIf { it > 0 },
    )
}

@Composable
private fun Tabs(
    state: VerbListState,
    onAction: (VerbListAction) -> Unit,
    modifier: Modifier,
) {
    ListTabs(
        selected = if (state.tab == ListTab.FAVOURITES) TabUi.Favourites else TabUi.All,
        favouritesCount = state.favouritesCount,
        onSelect = { tab ->
            onAction(VerbListAction.OnTabSelect(if (tab == TabUi.Favourites) ListTab.FAVOURITES else ListTab.ALL))
        },
        modifier = modifier,
    )
}

/** The list area: no data, skeleton, empty result, no favourites, or the grid. */
@Composable
private fun ListContent(
    state: VerbListState,
    verbs: LazyPagingItems<VerbListRowUi>,
    columns: Int,
    selectedSlug: String?,
    bottomPadding: Dp,
    gridState: LazyGridState,
    skeletonDelay: Duration,
    onAction: (VerbListAction) -> Unit,
    onCellClick: (String) -> Unit,
    onTestClick: (String) -> Unit,
    aboveBottomBar: Boolean,
) {
    val refresh = verbs.loadState.refresh
    when {
        state.isEmptyDatabase -> {
            CentredEmptyState(aboveBottomBar) {
                EmptyState(
                    icon = VerbeClairIcons.CloudDownload,
                    title = stringResource(Res.string.no_data_title),
                    body = stringResource(Res.string.no_data_body),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        verbs.itemCount == 0 && refresh is LoadState.Loading -> {
            FirstPageSkeleton(columns, skeletonDelay)
        }

        verbs.itemCount == 0 &&
            refresh is LoadState.NotLoading &&
            state.tab == ListTab.FAVOURITES &&
            state.favouritesCount == 0 -> {
            CentredEmptyState(aboveBottomBar) {
                EmptyState(
                    icon = VerbeClairIcons.Star,
                    title = stringResource(Res.string.no_favourites_title),
                    body = stringResource(Res.string.no_favourites_body),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        verbs.itemCount == 0 && refresh is LoadState.NotLoading -> {
            CentredEmptyState(aboveBottomBar) {
                EmptyState(
                    icon = VerbeClairIcons.SearchOff,
                    title = stringResource(Res.string.empty_result_title),
                    body = stringResource(Res.string.empty_result_body),
                    modifier = Modifier.fillMaxWidth(),
                    action = {
                        VerbeClairButton(
                            text = stringResource(Res.string.empty_result_action),
                            onClick = { onAction(VerbListAction.OnClearFilters) },
                        )
                    },
                )
            }
        }

        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                state = gridState,
                modifier = Modifier.fillMaxSize().testTag(VerbListTestTags.GRID),
                contentPadding = PaddingValues(bottom = bottomPadding),
            ) {
                verbRows(verbs, columns, selectedSlug, skeletonDelay, onAction, onCellClick, onTestClick)
            }
        }
    }
}

/**
 * The rows: each section header is a full-width sticky header, and the verbs between two headers are
 * one run of items. Rows are looked up with `peek` (no load); a cell reads its row with `get`, which
 * lets Paging load the next page.
 */
private fun LazyGridScope.verbRows(
    verbs: LazyPagingItems<VerbListRowUi>,
    columns: Int,
    selectedSlug: String?,
    skeletonDelay: Duration,
    onAction: (VerbListAction) -> Unit,
    onCellClick: (String) -> Unit,
    onTestClick: (String) -> Unit,
) {
    val count = verbs.itemCount
    var index = 0
    while (index < count) {
        val row = verbs.peek(index)
        if (row is VerbListRowUi.Header) {
            val headerIndex = index
            stickyHeader(key = row.key, contentType = ROW_HEADER) {
                val header = verbs[headerIndex] as? VerbListRowUi.Header ?: row
                SectionHeader(
                    title = stringResource(header.section.title),
                    modifier = Modifier.fillMaxWidth().testTag(VerbListTestTags.sectionHeader(header.section.id)),
                )
            }
            index++
        } else {
            val start = index
            while (index < count && verbs.peek(index) !is VerbListRowUi.Header) index++
            items(
                count = index - start,
                key = { offset -> verbs.peek(start + offset)?.key ?: "row-${start + offset}" },
                contentType = { ROW_VERB },
            ) { offset ->
                val verb = (verbs[start + offset] as? VerbListRowUi.Verb)?.verb
                if (verb == null) {
                    if (shownAfter(skeletonDelay)) VerbCellSkeleton(Modifier.fillMaxWidth())
                } else {
                    VerbRow(
                        verb = verb,
                        selected = verb.slug == selectedSlug,
                        onClick = { onCellClick(verb.slug) },
                        onTestClick = { onTestClick(verb.slug) },
                        onFavouriteClick = { onAction(VerbListAction.OnToggleFavourite(verb.slug)) },
                        // The cells of a line share its height in Figma PL-01 (87:9641): a one-line cell grows to
                        // the two-line height and centres its content, so the dividers of both columns line up.
                        modifier = if (columns > 1) Modifier.heightIn(min = twoLineCellHeight()) else Modifier,
                    )
                }
            }
        }
    }
    if (verbs.loadState.append is LoadState.Loading) {
        items(count = columns, contentType = { ROW_SKELETON }) {
            if (shownAfter(skeletonDelay)) {
                VerbCellSkeleton(Modifier.fillMaxWidth().testTag(VerbListTestTags.NEXT_PAGE_SKELETON))
            }
        }
    }
}

@Composable
private fun VerbRow(
    verb: VerbUi,
    selected: Boolean,
    onClick: () -> Unit,
    onTestClick: () -> Unit,
    onFavouriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val meta = verb.meta.map { stringResource(it) }.joinToString(META_SEPARATOR)
    VerbCell(
        modifier = modifier,
        infinitive = verb.infinitive,
        meta = meta,
        isFavourite = verb.isFavourite,
        onClick = onClick,
        onTestClick = onTestClick,
        onFavouriteClick = onFavouriteClick,
        matchedForm = verb.matchedForm,
        score = verb.score?.let { stringResource(Res.string.score_percent, it) },
        selected = selected,
    )
}

/** The height of a verb cell whose meta takes two lines: padding, title, gap and two body lines (Figma 7:41). */
@Composable
private fun twoLineCellHeight(): Dp {
    val spacing = VerbeClairTheme.spacing
    val title = VerbeClairTheme.typography.titleVerb.lineHeight
    val body = MaterialTheme.typography.bodyMedium.lineHeight
    return with(LocalDensity.current) { spacing.space12 * 2 + title.toDp() + spacing.space2 + body.toDp() * 2 }
}

/** Whether [delay] has passed since this first composed, so a page that loads faster shows no skeleton (N-2). */
@Composable
private fun shownAfter(delay: Duration): Boolean {
    var visible by remember { mutableStateOf(delay <= Duration.ZERO) }
    LaunchedEffect(delay) {
        delay(delay)
        visible = true
    }
    return visible
}

/** Skeleton rows, shown only when the first page takes longer than [delay] (N-2, Figma L-07). */
@Composable
private fun FirstPageSkeleton(columns: Int, delay: Duration) {
    if (shownAfter(delay)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize().testTag(VerbListTestTags.SKELETON),
            userScrollEnabled = false,
        ) {
            items(count = SKELETON_ROWS * columns) { VerbCellSkeleton(Modifier.fillMaxWidth()) }
        }
    }
}

/** An empty state in the middle of the list area (Figma L-05, L-06, L-09 content: 32 dp sides, 48 dp below). */
@Composable
private fun CentredEmptyState(aboveBottomBar: Boolean, content: @Composable () -> Unit) {
    val spacing = VerbeClairTheme.spacing
    // Figma 10:651 centres the state in a content area with 48 dp bottom padding. In L-05 / L-06 / L-09 that area
    // runs on under the 80 dp bottom bar, which leaves 80 - 48 = 32 dp more above than below the visible list area.
    val vertical =
        if (aboveBottomBar) {
            Modifier.padding(top = BottomBarHeight - spacing.space48)
        } else {
            Modifier.padding(bottom = spacing.space48)
        }
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = spacing.space32).then(vertical),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/** The shell's bottom bar height (Figma 52:3195), which the Compact empty states are centred against. */
private val BottomBarHeight = 80.dp

/** The tablet right pane until verb details exist: the Neutral empty state, title only (N-15). */
@Composable
private fun DetailPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        CentredEmptyState(aboveBottomBar = false) {
            EmptyState(
                icon = VerbeClairIcons.BookOpen,
                title = stringResource(Res.string.detail_empty_title),
                body = null,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private const val ROW_HEADER = "header"
private const val ROW_VERB = "verb"
private const val ROW_SKELETON = "skeleton"

// Screen previews, one per Figma state, in light and dark (design D13). Sizes are the Figma frames
// without the status bar and the bottom bar or rail, which the app shell draws.

@Composable
private fun ScreenPreview(darkTheme: Boolean, content: @Composable () -> Unit) {
    VerbeClairTheme(darkTheme = darkTheme) { content() }
}

private val GroupedChoices = PreviewData.groupedChoices

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
