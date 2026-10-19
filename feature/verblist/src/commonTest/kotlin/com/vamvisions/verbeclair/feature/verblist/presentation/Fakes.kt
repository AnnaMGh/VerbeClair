package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.vamvisions.verbeclair.core.domain.favourite.FavouritesRepository
import com.vamvisions.verbeclair.core.domain.preferences.ListPreferences
import com.vamvisions.verbeclair.core.domain.score.TestScoreRepository
import com.vamvisions.verbeclair.core.domain.sync.DataSyncTrigger
import com.vamvisions.verbeclair.core.domain.verb.ListChoices
import com.vamvisions.verbeclair.core.domain.verb.ListQuery
import com.vamvisions.verbeclair.core.domain.verb.ListTab
import com.vamvisions.verbeclair.core.domain.verb.VerbCatalog
import com.vamvisions.verbeclair.core.domain.verb.VerbListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** Returns [verbs] for every query (already in the requested order) and records each query. */
class FakeVerbCatalog(var verbs: List<VerbListItem> = emptyList()) : VerbCatalog {
    val queries = mutableListOf<ListQuery>()
    val empty = MutableStateFlow(false)

    override fun pagedList(query: ListQuery): Flow<PagingData<VerbListItem>> {
        queries += query
        val result = if (query.tab == ListTab.FAVOURITES) verbs.filter { it.slug in query.favouriteSlugs } else verbs
        return flowOf(PagingData.from(result, COMPLETE))
    }

    override fun isEmpty(): Flow<Boolean> = empty

    override fun existingSlugs(slugs: Set<String>): Flow<Set<String>> = flowOf(slugs)

    private companion object {
        /** The whole list in one page, as Room reports it, so separators see the list start. */
        val COMPLETE = LoadState.NotLoading(endOfPaginationReached = true)
            .let { LoadStates(refresh = LoadState.NotLoading(false), prepend = it, append = it) }
    }
}

/** Favourites in memory; with `loaded = false` no slugs are emitted until [loaded] is set, like a slow database. */
class FakeFavouritesRepository(initial: Set<String> = emptySet(), loaded: Boolean = true) : FavouritesRepository {
    val slugs = MutableStateFlow(initial)
    val loaded = MutableStateFlow(loaded)
    val added = mutableListOf<String>()
    val removed = mutableListOf<String>()

    override fun observeSlugs(): Flow<Set<String>> =
        combine(loaded, slugs) { isLoaded, slugs -> if (isLoaded) slugs else null }.filterNotNull()

    override fun observeCount(): Flow<Int> = slugs.map { it.size }

    override suspend fun add(slug: String) {
        added += slug
        slugs.value += slug
    }

    override suspend fun remove(slug: String) {
        removed += slug
        slugs.value -= slug
    }
}

class FakeTestScoreRepository(private val scores: Map<String, Int> = emptyMap()) : TestScoreRepository {
    override fun observeScores(slugs: Set<String>): Flow<Map<String, Int>> =
        flowOf(scores.filterKeys { it in slugs })
}

class FakeListPreferences(
    choices: ListChoices = ListChoices.Default,
    tab: ListTab = ListTab.ALL,
) : ListPreferences {
    val storedChoices = MutableStateFlow(choices)
    val storedTab = MutableStateFlow(tab)

    override val choices: Flow<ListChoices> = storedChoices
    override val tab: Flow<ListTab> = storedTab

    override suspend fun setChoices(choices: ListChoices) {
        storedChoices.value = choices
    }

    override suspend fun setTab(tab: ListTab) {
        storedTab.value = tab
    }
}

class FakeDataSyncTrigger : DataSyncTrigger {
    var requests = 0
    override val isSyncing: StateFlow<Boolean> = MutableStateFlow(false)

    override fun requestSync() {
        requests++
    }
}
