package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.vamvisions.verbeclair.core.domain.favourite.FavouritesRepository
import com.vamvisions.verbeclair.core.domain.preferences.FirstStepsPreferences
import com.vamvisions.verbeclair.core.domain.preferences.ListPreferences
import com.vamvisions.verbeclair.core.domain.result.DataError
import com.vamvisions.verbeclair.core.domain.result.EmptyResult
import com.vamvisions.verbeclair.core.domain.result.Result
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

    override suspend fun topVerbs(limit: Int): List<VerbListItem> = verbs.filterNot { it.reflexive }.take(limit)

    override fun isEmpty(): Flow<Boolean> = empty

    override fun existingSlugs(slugs: Set<String>): Flow<Set<String>> = flowOf(slugs)

    private companion object {
        /** The whole list in one page, as Room reports it, so separators see the list start. */
        val COMPLETE =
            LoadState
                .NotLoading(endOfPaginationReached = true)
                .let { LoadStates(refresh = LoadState.NotLoading(false), prepend = it, append = it) }
    }
}

/**
 * Favourites in memory; with `loaded = false` no slugs are emitted until [loaded] is set, like a slow database,
 * and with `failing = true` every change fails like a full disk and stores nothing.
 */
class FakeFavouritesRepository(
    initial: Set<String> = emptySet(),
    loaded: Boolean = true,
    var failing: Boolean = false,
) : FavouritesRepository {
    val slugs = MutableStateFlow(initial)
    val loaded = MutableStateFlow(loaded)
    val added = mutableListOf<String>()
    val removed = mutableListOf<String>()

    override fun observeSlugs(): Flow<Set<String>> =
        combine(loaded, slugs) { isLoaded, slugs -> if (isLoaded) slugs else null }.filterNotNull()

    override fun observeCount(): Flow<Int> = slugs.map { it.size }

    override suspend fun add(slug: String): EmptyResult<DataError.Local> {
        if (failing) return Result.Error(DataError.Local.DISK_FULL)
        added += slug
        slugs.value += slug
        return Result.Success(Unit)
    }

    override suspend fun remove(slug: String): EmptyResult<DataError.Local> {
        if (failing) return Result.Error(DataError.Local.DISK_FULL)
        removed += slug
        slugs.value -= slug
        return Result.Success(Unit)
    }
}

class FakeTestScoreRepository(
    private val scores: Map<String, Int> = emptyMap(),
    hasAnyAttempt: Boolean = false,
    loaded: Boolean = true,
) : TestScoreRepository {
    val hasAny = MutableStateFlow(hasAnyAttempt)

    /** With `loaded = false` [observeHasAnyAttempt] emits nothing until this is set, like a slow database. */
    val loaded = MutableStateFlow(loaded)

    /** The slugs of every call, in order. */
    val requested = mutableListOf<Set<String>>()

    override fun observeScores(slugs: Set<String>): Flow<Map<String, Int>> {
        requested += slugs
        return flowOf(scores.filterKeys { it in slugs })
    }

    override fun observeHasAnyAttempt(): Flow<Boolean> =
        combine(loaded, hasAny) { isLoaded, any -> if (isLoaded) any else null }.filterNotNull()
}

/** With `loaded = false` [closed] emits nothing until [loaded] is set, like a DataStore that is still reading. */
class FakeFirstStepsPreferences(closed: Boolean = false, loaded: Boolean = true) : FirstStepsPreferences {
    val storedClosed = MutableStateFlow(closed)
    val loaded = MutableStateFlow(loaded)

    override val closed: Flow<Boolean> =
        combine(this.loaded, storedClosed) { isLoaded, value -> if (isLoaded) value else null }.filterNotNull()

    override suspend fun close() {
        storedClosed.value = true
    }
}

class FakeListPreferences(choices: ListChoices = ListChoices.Default, tab: ListTab = ListTab.ALL) : ListPreferences {
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
