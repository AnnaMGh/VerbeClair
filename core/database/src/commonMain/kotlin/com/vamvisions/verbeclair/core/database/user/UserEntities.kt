package com.vamvisions.verbeclair.core.database.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/** A favourite verb (A-7). Linked to verb data by slug only, so it survives every verb-database install. */
@Entity(tableName = "favourites")
data class FavouriteEntity(
    @PrimaryKey val slug: String,
    @ColumnInfo(name = "added_at") val addedAt: Long,
)

/**
 * One test of one verb, or the cards of one verb checked in one review session (T-9, RV-4). Times
 * are epoch milliseconds.
 */
@Entity(
    tableName = "test_attempts",
    indices = [Index("slug", "started_at")],
)
data class TestAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val slug: String,
    /** `TEST` or `REVIEW`. The latest attempt of either kind sets the last test score. */
    val kind: String,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "selected_tenses") val selectedTenses: List<SelectedTense>,
)

/** A tense picked for an attempt, by its API codes. */
@Serializable
data class SelectedTense(val mood: String, val tense: String)

/** One checked line of an attempt, linked to verb data by codes and person, never by row IDs. */
@Entity(
    tableName = "attempt_lines",
    foreignKeys = [ForeignKey(TestAttemptEntity::class, ["id"], ["attempt_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("attempt_id")],
)
data class AttemptLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "attempt_id") val attemptId: Long,
    @ColumnInfo(name = "mood_code") val moodCode: String,
    @ColumnInfo(name = "tense_code") val tenseCode: String,
    /** Null for a tense with no person. */
    val person: String?,
    @ColumnInfo(name = "typed_text") val typedText: String,
    /** `CORRECT`, `CORRECT_NO_ACCENTS` or `FAILED`. */
    val result: String,
)

/** Line counts of a verb's latest attempt; the score is computed from them in Kotlin (design D6). */
data class AttemptCounts(
    val slug: String,
    val correct: Int,
    val total: Int,
)
