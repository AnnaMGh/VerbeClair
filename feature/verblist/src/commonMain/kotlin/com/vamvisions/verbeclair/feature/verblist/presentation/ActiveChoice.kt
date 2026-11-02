package com.vamvisions.verbeclair.feature.verblist.presentation

import com.vamvisions.verbeclair.core.domain.verb.AuxiliaryFilter
import com.vamvisions.verbeclair.core.domain.verb.GroupBy
import com.vamvisions.verbeclair.core.domain.verb.ListChoices
import com.vamvisions.verbeclair.core.domain.verb.ListFilters
import com.vamvisions.verbeclair.core.domain.verb.SortOrder
import com.vamvisions.verbeclair.core.domain.verb.VerbGroup

/** One chip of the active choices row (L-6). Tapping it removes that choice. */
sealed interface ActiveChoice {
    data class Group(val group: VerbGroup) : ActiveChoice

    data class Auxiliary(val auxiliary: AuxiliaryFilter) : ActiveChoice

    data class Reflexive(val reflexive: Boolean) : ActiveChoice

    data class Grouping(val groupBy: GroupBy) : ActiveChoice

    data class Sort(val sort: SortOrder) : ActiveChoice
}

/** One chip per selected filter value, then the grouping, then the sort (L-6). */
fun ListChoices.activeChoices(): List<ActiveChoice> =
    buildList {
        VerbGroup.entries.filter { it in filters.groups }.forEach { add(ActiveChoice.Group(it)) }
        AuxiliaryFilter.entries.filter { it in filters.auxiliaries }.forEach { add(ActiveChoice.Auxiliary(it)) }
        // Same order as the sheet: Reflexive, then Non-reflexive.
        listOf(true, false).filter { it in filters.reflexive }.forEach { add(ActiveChoice.Reflexive(it)) }
        if (groupBy != GroupBy.NONE) add(ActiveChoice.Grouping(groupBy))
        if (sort != SortOrder.A_TO_Z) add(ActiveChoice.Sort(sort))
    }

/** The choices without [choice]: a filter value is deselected, grouping goes to none, sort back to A → Z. */
fun ListChoices.without(choice: ActiveChoice): ListChoices =
    when (choice) {
        is ActiveChoice.Group -> copy(filters = filters.copy(groups = filters.groups - choice.group))
        is ActiveChoice.Auxiliary -> copy(filters = filters.copy(auxiliaries = filters.auxiliaries - choice.auxiliary))
        is ActiveChoice.Reflexive -> copy(filters = filters.copy(reflexive = filters.reflexive - choice.reflexive))
        is ActiveChoice.Grouping -> copy(groupBy = GroupBy.NONE)
        is ActiveChoice.Sort -> copy(sort = SortOrder.A_TO_Z)
    }

/**
 * The choices after a list-controls [action] (L-5, L-6, L-9), or null when [action] does not change
 * them. Filter values toggle; grouping and sort are single choices; Reset restores the defaults.
 */
fun ListChoices.reduce(action: VerbListAction): ListChoices? =
    when (action) {
        is VerbListAction.OnToggleGroup -> {
            copy(filters = filters.copy(groups = filters.groups.toggle(action.group)))
        }

        is VerbListAction.OnToggleAuxiliary -> {
            copy(filters = filters.copy(auxiliaries = filters.auxiliaries.toggle(action.auxiliary)))
        }

        is VerbListAction.OnToggleReflexive -> {
            copy(filters = filters.copy(reflexive = filters.reflexive.toggle(action.reflexive)))
        }

        is VerbListAction.OnGroupBySelect -> {
            copy(groupBy = action.groupBy)
        }

        is VerbListAction.OnSortSelect -> {
            copy(sort = action.sort)
        }

        VerbListAction.OnReset -> {
            ListChoices.Default
        }

        is VerbListAction.OnRemoveChoice -> {
            without(action.choice)
        }

        VerbListAction.OnClearFilters -> {
            copy(filters = ListFilters())
        }

        else -> {
            null
        }
    }

private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value
