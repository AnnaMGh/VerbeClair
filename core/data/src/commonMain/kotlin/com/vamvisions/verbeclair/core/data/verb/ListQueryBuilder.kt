package com.vamvisions.verbeclair.core.data.verb

import androidx.room.RoomRawQuery
import com.vamvisions.verbeclair.core.domain.verb.AuxiliaryFilter
import com.vamvisions.verbeclair.core.domain.verb.GroupBy
import com.vamvisions.verbeclair.core.domain.verb.ListQuery
import com.vamvisions.verbeclair.core.domain.verb.ListTab
import com.vamvisions.verbeclair.core.domain.verb.SortOrder
import com.vamvisions.verbeclair.core.domain.verb.normalizeKey

/**
 * Builds the verb-list SQL for a [ListQuery] (design D5): search, filters, tab, sections and sort
 * combine freely, so the query is assembled here and run through `@RawQuery`.
 */
internal object ListQueryBuilder {

    fun build(query: ListQuery, limit: Int? = null): RoomRawQuery {
        // Bound in SQL text order: the matched-form column first, then the WHERE conditions.
        val selectArguments = mutableListOf<Any>()
        val arguments = mutableListOf<Any>()
        val conditions = mutableListOf<String>()

        val search = normalizeKey(query.search.trim())
        val matchedForm =
            if (search.isEmpty()) {
                "NULL"
            } else {
                // Prefix match as an index range instead of LIKE, so the key indices are used.
                val upper = search + PREFIX_END
                conditions += """v.slug IN (
                SELECT slug FROM verbs WHERE infinitive_key >= ? AND infinitive_key < ?
                UNION
                SELECT slug FROM form_search WHERE key >= ? AND key < ?
            )"""
                arguments.addAll(listOf(search, upper, search, upper))
                // No form when the infinitive matched; else a form equal to the search, then display order (D-3).
                selectArguments.addAll(listOf(search, upper, search, upper, search))
                """CASE WHEN v.infinitive_key >= ? AND v.infinitive_key < ? THEN NULL ELSE (
                SELECT fs.form FROM form_search fs
                WHERE fs.slug = v.slug AND fs.key >= ? AND fs.key < ?
                ORDER BY fs.key = ? DESC, fs.display_order
                LIMIT 1
            ) END"""
            }

        val filters = query.choices.filters
        conditions.addIn("v.`group`", filters.groups.map { it.name }, arguments)
        conditions.addIn("v.auxiliary", filters.auxiliaries.flatMap { it.storedValues() }.distinct(), arguments)
        conditions.addIn("v.reflexive", filters.reflexive.map { if (it) 1L else 0L }, arguments)

        if (query.tab == ListTab.FAVOURITES) {
            if (query.favouriteSlugs.isEmpty()) {
                conditions += "0"
            } else {
                conditions.addIn(
                    "v.slug",
                    query.favouriteSlugs.toList(),
                    arguments,
                )
            }
        }

        val sql =
            buildString {
                append("SELECT v.slug, v.infinitive, v.`group`, v.reflexive, v.auxiliary, v.frequency, ")
                append(matchedForm).append(" AS matched_form FROM verbs v")
                if (conditions.isNotEmpty()) append(" WHERE ").append(conditions.joinToString(" AND "))
                append(" ORDER BY ")
                sectionOrder(query.choices.groupBy)?.let { append(it).append(", ") }
                append(sortOrder(query.choices.sort))
                append(", v.infinitive_key ASC, v.slug ASC")
                limit?.let { append(" LIMIT ").append(it) }
            }

        return RoomRawQuery(sql) { statement ->
            (selectArguments + arguments).forEachIndexed { index, value ->
                when (value) {
                    is String -> statement.bindText(index + 1, value)
                    is Long -> statement.bindLong(index + 1, value)
                    else -> error("Unsupported argument $value")
                }
            }
        }
    }

    /** Sections in the order of L-5: 1st → 2nd → 3rd; avoir → être → both; non-reflexive → reflexive. */
    private fun sectionOrder(groupBy: GroupBy): String? =
        when (groupBy) {
            GroupBy.NONE -> null
            GroupBy.GROUP -> "CASE v.`group` WHEN 'I' THEN 0 WHEN 'II' THEN 1 ELSE 2 END"
            GroupBy.AUXILIARY -> "CASE v.auxiliary WHEN 'AVOIR' THEN 0 WHEN 'ETRE' THEN 1 ELSE 2 END"
            GroupBy.REFLEXIVE -> "v.reflexive ASC"
        }

    private fun sortOrder(sort: SortOrder): String =
        when (sort) {
            SortOrder.A_TO_Z -> "v.sort_key ASC"
            SortOrder.Z_TO_A -> "v.sort_key DESC"
            SortOrder.MOST_FREQUENT -> "v.frequency DESC"
        }

    /** A verb with both auxiliaries matches an avoir and an être filter. */
    private fun AuxiliaryFilter.storedValues(): List<String> =
        when (this) {
            AuxiliaryFilter.AVOIR -> listOf("AVOIR", "BOTH")
            AuxiliaryFilter.ETRE -> listOf("ETRE", "BOTH")
        }

    /** Adds `column IN (?, …)`; an empty category does not filter. */
    private fun MutableList<String>.addIn(
        column: String,
        values: List<Any>,
        arguments: MutableList<Any>,
    ) {
        if (values.isEmpty()) return
        this += values.joinToString(prefix = "$column IN (", postfix = ")") { "?" }
        arguments.addAll(values)
    }

    /** Sorts after every character a normalised key can hold, so `key < q + PREFIX_END` is a prefix match. */
    private const val PREFIX_END = '￿'
}
