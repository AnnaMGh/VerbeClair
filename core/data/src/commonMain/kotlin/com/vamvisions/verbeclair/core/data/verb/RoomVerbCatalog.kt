package com.vamvisions.verbeclair.core.data.verb

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.vamvisions.verbeclair.core.database.verb.VerbDatabase
import com.vamvisions.verbeclair.core.database.verb.VerbRow
import com.vamvisions.verbeclair.core.domain.verb.Auxiliary
import com.vamvisions.verbeclair.core.domain.verb.ListQuery
import com.vamvisions.verbeclair.core.domain.verb.VerbCatalog
import com.vamvisions.verbeclair.core.domain.verb.VerbGroup
import com.vamvisions.verbeclair.core.domain.verb.VerbListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * The verb catalogue read from the local verb database (verb-data, design D5). The database is
 * awaited inside each flow, so collecting waits for the bundled-database install (D4).
 */
class RoomVerbCatalog(private val database: suspend () -> VerbDatabase) : VerbCatalog {

    constructor(database: VerbDatabase) : this({ database })

    override fun pagedList(query: ListQuery): Flow<PagingData<VerbListItem>> = flow {
        val dao = database().verbDao()
        emitAll(
            Pager(PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false)) {
                dao.pagedList(ListQueryBuilder.build(query))
            }.flow,
        )
    }.map { page -> page.map { it.toListItem() } }

    override fun isEmpty(): Flow<Boolean> = flow { emitAll(database().verbDao().observeIsEmpty()) }

    override fun existingSlugs(slugs: Set<String>): Flow<Set<String>> =
        if (slugs.isEmpty()) {
            flowOf(emptySet())
        } else {
            flow { emitAll(database().verbDao().observeExistingSlugs(slugs)) }.map { it.toSet() }
        }

    internal companion object {
        const val PAGE_SIZE = 60
    }
}

internal fun VerbRow.toListItem() = VerbListItem(
    slug = slug,
    infinitive = infinitive,
    group = VerbGroup.valueOf(group),
    reflexive = reflexive,
    auxiliary = Auxiliary.valueOf(auxiliary),
    frequency = frequency,
    matchedForm = matchedForm,
)
