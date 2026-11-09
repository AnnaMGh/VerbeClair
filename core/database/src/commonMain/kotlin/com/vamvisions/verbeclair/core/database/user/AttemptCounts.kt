package com.vamvisions.verbeclair.core.database.user

import androidx.room.ColumnInfo

/** Line counts of a verb's latest attempt; the score is computed from them in Kotlin (design D6). */
data class AttemptCounts(
    val slug: String,
    val correct: Int,
    val total: Int,
)
