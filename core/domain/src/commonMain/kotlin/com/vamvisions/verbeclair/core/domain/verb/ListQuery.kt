package com.vamvisions.verbeclair.core.domain.verb

/** The auxiliary filter values of the list controls (L-5). A verb with [Auxiliary.BOTH] matches both. */
enum class AuxiliaryFilter { AVOIR, ETRE }

enum class GroupBy { NONE, GROUP, AUXILIARY, REFLEXIVE }

enum class SortOrder { A_TO_Z, Z_TO_A, MOST_FREQUENT }

enum class ListTab { ALL, FAVOURITES }

/** Multi-select filters: OR inside a category, AND across categories. An empty category does not filter. */
data class ListFilters(
    val groups: Set<VerbGroup> = emptySet(),
    val auxiliaries: Set<AuxiliaryFilter> = emptySet(),
    val reflexive: Set<Boolean> = emptySet(),
) {
    /** Number of selected filter values, shown in the list-controls badge (L-6). */
    val count: Int get() = groups.size + auxiliaries.size + reflexive.size
}

/** Filters, grouping and sort of the verb list. They are remembered across launches (L-7). */
data class ListChoices(
    val filters: ListFilters = ListFilters(),
    val groupBy: GroupBy = GroupBy.NONE,
    val sort: SortOrder = SortOrder.A_TO_Z,
) {
    val isDefault: Boolean get() = this == Default

    companion object {
        val Default = ListChoices()
    }
}

/**
 * Everything the list query needs. On the Favourites tab, only [favouriteSlugs] are listed; the
 * favourites live in another database, so the caller passes the current set (design D5).
 */
data class ListQuery(
    val search: String = "",
    val choices: ListChoices = ListChoices.Default,
    val tab: ListTab = ListTab.ALL,
    val favouriteSlugs: Set<String> = emptySet(),
)
