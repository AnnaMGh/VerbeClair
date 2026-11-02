package com.vamvisions.verbeclair.feature.verblist.presentation

import com.vamvisions.verbeclair.core.domain.verb.GroupBy
import com.vamvisions.verbeclair.core.domain.verb.VerbListItem
import org.jetbrains.compose.resources.StringResource

/** One row of the verb list grid: a section header or a verb cell. */
sealed interface VerbListRowUi {
    val key: String

    data class Header(val section: VerbSection) : VerbListRowUi {
        override val key: String get() = "section-${section.id}"
    }

    /** A verb cell; [section] is the section it belongs to when the list is grouped. */
    data class Verb(val verb: VerbUi, val section: VerbSection? = null) : VerbListRowUi {
        override val key: String get() = verb.slug
    }
}

/** A verb cell (L-8): infinitive and matched form in original spelling, meta labels, score and buttons. */
data class VerbUi(
    val slug: String,
    val infinitive: String,
    val matchedForm: String?,
    /** Group, auxiliary, reflexive and frequency labels, joined with " · " on screen. */
    val meta: List<StringResource>,
    val score: Int? = null,
    val isFavourite: Boolean = false,
)

/** A section of a grouped list (L-5), in display order. */
data class VerbSection(val id: String, val title: StringResource)

fun VerbListItem.toUi(score: Int? = null, isFavourite: Boolean = false) =
    VerbUi(
        slug = slug,
        infinitive = infinitive,
        matchedForm = matchedForm,
        meta = metaLabels(),
        score = score,
        isFavourite = isFavourite,
    )

/** The meta line labels of the labels table (Notion section 6). */
fun VerbListItem.metaLabels(): List<StringResource> =
    listOf(group.label(), auxiliary.label(), reflexiveLabel(reflexive), frequencyLabel(frequency))

/** The section [this] verb belongs to for [groupBy], or null when the list is not grouped. */
fun VerbListItem.section(groupBy: GroupBy): VerbSection? =
    when (groupBy) {
        GroupBy.NONE -> null
        GroupBy.GROUP -> VerbSection("group-${group.name}", group.label())
        GroupBy.AUXILIARY -> VerbSection("auxiliary-${auxiliary.name}", auxiliary.label())
        GroupBy.REFLEXIVE -> VerbSection("reflexive-$reflexive", reflexiveLabel(reflexive))
    }
