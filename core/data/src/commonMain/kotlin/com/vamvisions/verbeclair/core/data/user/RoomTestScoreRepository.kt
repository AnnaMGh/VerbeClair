package com.vamvisions.verbeclair.core.data.user

import com.vamvisions.verbeclair.core.database.user.UserDatabase
import com.vamvisions.verbeclair.core.domain.score.TestScoreRepository
import com.vamvisions.verbeclair.core.domain.score.scorePercent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** Last test scores from the user database: the latest attempt of each verb, test or review session (design D6). */
class RoomTestScoreRepository(database: UserDatabase) : TestScoreRepository {
    private val dao = database.attemptDao()

    override fun observeScores(slugs: Set<String>): Flow<Map<String, Int>> {
        if (slugs.isEmpty()) return flowOf(emptyMap())
        return dao.observeLatestAttemptCounts(slugs).map { rows ->
            rows.mapNotNull { row -> scorePercent(row.correct, row.total)?.let { row.slug to it } }.toMap()
        }
    }
}
