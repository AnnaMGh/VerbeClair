package com.vamvisions.verbeclair.core.domain.verb

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

/** The local, offline verb catalogue (verb-data). */
interface VerbCatalog {
    /** The list for [query], page by page, each verb at most once. */
    fun pagedList(query: ListQuery): Flow<PagingData<VerbListItem>>

    /** True while the local verb database holds no verb (L-10). */
    fun isEmpty(): Flow<Boolean>

    /** The subset of [slugs] that exist in the local verb database. */
    fun existingSlugs(slugs: Set<String>): Flow<Set<String>>
}
