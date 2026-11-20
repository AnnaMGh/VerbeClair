package com.vamvisions.verbeclair.feature.verblist.presentation

import com.vamvisions.verbeclair.core.domain.verb.ListChoices
import com.vamvisions.verbeclair.core.domain.verb.ListTab

/**
 * Screen 1 state (design D10). The paged rows are exposed separately by the ViewModel, because
 * `PagingData` does not belong in a data class state.
 */
data class VerbListState(
    val query: String = "",
    val choices: ListChoices = ListChoices.Default,
    val tab: ListTab = ListTab.ALL,
    val favouritesCount: Int = 0,
    val isSheetOpen: Boolean = false,
    /** The verb highlighted in the tablet list–detail layout (N-15). */
    val selectedSlug: String? = null,
    val isEmptyDatabase: Boolean = false,
    val isSyncing: Boolean = false,
    /** Whether the first steps card was closed; null until the setting is first read (OB-1). */
    val firstStepsClosed: Boolean? = null,
    /** Whether any test or review attempt is saved; null until first read (OB-1). */
    val hasAnyAttempt: Boolean? = null,
    /** The 10 most frequent non-reflexive verbs for the tablet top 10 panel (OB-1); empty until loaded. */
    val topVerbs: List<VerbUi> = emptyList(),
) {

    /**
     * The first steps card rules that the ViewModel can know (OB-1): All tab, empty search, no active choice (filter, grouping or sort),
     * verbs downloaded, card not closed and no attempt saved. False until both flows have emitted, so the
     * card does not flash at start-up. The screen also needs the first page loaded and not empty.
     */
    val showFirstSteps: Boolean
        get() =
            tab == ListTab.ALL &&
                query.isBlank() &&
                activeChoices.isEmpty() &&
                !isEmptyDatabase &&
                firstStepsClosed == false &&
                hasAnyAttempt == false

    /** Number of selected filter values, shown in the list-controls badge (L-6). */
    val filterCount: Int get() = choices.filters.count

    /** The chips of the active choices row (L-6); empty with the default choices. */
    val activeChoices: List<ActiveChoice> get() = choices.activeChoices()
}
