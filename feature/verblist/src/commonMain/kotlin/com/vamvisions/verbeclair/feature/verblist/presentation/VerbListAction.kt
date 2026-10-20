package com.vamvisions.verbeclair.feature.verblist.presentation

import com.vamvisions.verbeclair.core.domain.verb.AuxiliaryFilter
import com.vamvisions.verbeclair.core.domain.verb.GroupBy
import com.vamvisions.verbeclair.core.domain.verb.ListTab
import com.vamvisions.verbeclair.core.domain.verb.SortOrder
import com.vamvisions.verbeclair.core.domain.verb.VerbGroup

/** User actions on Screen 1 (design D10). Cell and test taps go to the navigation callbacks instead. */
sealed interface VerbListAction {
    data class OnQueryChange(val query: String) : VerbListAction

    data object OnClearQuery : VerbListAction

    data class OnTabSelect(val tab: ListTab) : VerbListAction

    data object OnOpenSheet : VerbListAction

    data object OnDismissSheet : VerbListAction

    data class OnToggleGroup(val group: VerbGroup) : VerbListAction

    data class OnToggleAuxiliary(val auxiliary: AuxiliaryFilter) : VerbListAction

    data class OnToggleReflexive(val reflexive: Boolean) : VerbListAction

    data class OnGroupBySelect(val groupBy: GroupBy) : VerbListAction

    data class OnSortSelect(val sort: SortOrder) : VerbListAction

    data object OnReset : VerbListAction

    data class OnRemoveChoice(val choice: ActiveChoice) : VerbListAction

    data object OnClearFilters : VerbListAction

    data class OnToggleFavourite(val slug: String) : VerbListAction

    data class OnSelectVerb(val slug: String) : VerbListAction

    data object OnClearSelection : VerbListAction
}
