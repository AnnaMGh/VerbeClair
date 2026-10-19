package com.vamvisions.verbeclair.core.domain.score

import kotlinx.coroutines.flow.Flow

/** Result of one checked line of a test (T-9). */
enum class LineResult { CORRECT, CORRECT_NO_ACCENTS, FAILED }

/**
 * Kind of a stored attempt. A [REVIEW] attempt holds the cards of one verb checked in one review
 * session. The latest attempt of either kind sets the last test score (RV-4).
 */
enum class AttemptKind { TEST, REVIEW }

/**
 * Whole percentage of [correctLines] out of [checkedLines], rounded half up (glossary, *Last test
 * score*): 2 of 3 → 67, 79.5% → 80. Null when no line was checked.
 */
fun scorePercent(correctLines: Int, checkedLines: Int): Int? {
    if (checkedLines <= 0) return null
    // round(100 × c / t) half up, in integers: floor((200c + t) / 2t)
    return ((200L * correctLines + checkedLines) / (2L * checkedLines)).toInt()
}

/** The score of one attempt's lines. Lines correct without accents count as correct. */
fun lastTestScore(lines: List<LineResult>): Int? =
    scorePercent(correctLines = lines.count { it != LineResult.FAILED }, checkedLines = lines.size)

interface TestScoreRepository {
    /** The last test score of each of [slugs] that has one. */
    fun observeScores(slugs: Set<String>): Flow<Map<String, Int>>
}
