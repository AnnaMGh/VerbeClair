package com.vamvisions.verbeclair.core.domain.verb

/** Verb group as returned by the API (`I`, `II`, `III`). */
enum class VerbGroup { I, II, III }

/** Auxiliary of the compound tenses as returned by the API. [BOTH] matches an avoir and an être filter. */
enum class Auxiliary { AVOIR, ETRE, BOTH }

/** One row of the verb list (the data of `GET /verbs`). */
data class VerbListItem(
    val slug: String,
    val infinitive: String,
    val group: VerbGroup,
    val reflexive: Boolean,
    val auxiliary: Auxiliary,
    val frequency: Double,
    /** The simple form that matched the search, in original spelling; null when the infinitive matched or there is no search. */
    val matchedForm: String? = null,
)
