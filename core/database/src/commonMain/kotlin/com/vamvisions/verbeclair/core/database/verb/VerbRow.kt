package com.vamvisions.verbeclair.core.database.verb

import androidx.room.ColumnInfo

/** One row of the paged list query (design D5). */
data class VerbRow(
    val slug: String,
    val infinitive: String,
    val group: String,
    val reflexive: Boolean,
    val auxiliary: String,
    val frequency: Double,
    @ColumnInfo(name = "matched_form") val matchedForm: String?,
)
