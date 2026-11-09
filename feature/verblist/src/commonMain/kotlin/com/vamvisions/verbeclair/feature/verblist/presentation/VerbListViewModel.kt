package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.vamvisions.verbeclair.core.domain.favourite.FavouritesRepository
import com.vamvisions.verbeclair.core.domain.preferences.ListPreferences
import com.vamvisions.verbeclair.core.domain.result.onFailure
import com.vamvisions.verbeclair.core.domain.score.TestScoreRepository
import com.vamvisions.verbeclair.core.domain.sync.DataSyncTrigger
import com.vamvisions.verbeclair.core.domain.verb.ListChoices
import com.vamvisions.verbeclair.core.domain.verb.ListQuery
import com.vamvisions.verbeclair.core.domain.verb.ListTab
import com.vamvisions.verbeclair.core.domain.verb.VerbCatalog
import com.vamvisions.verbeclair.core.domain.verb.VerbListItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Screen 1 (design D10). Search text is debounced 300 ms; choices and tab come from [preferences]
 * and are written back on each change. The paged rows are joined with favourites and last scores.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class VerbListViewModel(
    private val catalog: VerbCatalog,
    private val favourites: FavouritesRepository,
    private val scores: TestScoreRepository,
    private val preferences: ListPreferences,
    private val syncTrigger: DataSyncTrigger,
) : ViewModel() {

    private val _state = MutableStateFlow(VerbListState())
    val state: StateFlow<VerbListState> = _state.asStateFlow()

    private val _events = Channel<VerbListEvent>(Channel.BUFFERED)
    val events: Flow<VerbListEvent> = _events.receiveAsFlow()

    /** Choices and tab are restored before the first query, so the list never loads twice at start-up. */
    private val restored = MutableStateFlow(false)

    /** Null until the favourites are first read, so a restored Favourites tab does not query an empty set. */
    private val favouriteSlugs: StateFlow<Set<String>?> =
        favourites
            .observeSlugs()
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Slugs of the rows loaded so far, so the last scores are read only for them (D6). */
    private val loadedSlugs = MutableStateFlow<Set<String>>(emptySet())

    private val debouncedQuery: Flow<String> =
        _state
            .map { it.query }
            .distinctUntilChanged()
            // A cleared search applies at once; typing waits for a 300 ms pause (L-3).
            .debounce { if (it.isBlank()) 0L else SEARCH_DEBOUNCE_MILLIS }

    private val listQuery: Flow<ListQuery> =
        combine(
            debouncedQuery,
            _state.map { it.choices to it.tab }.distinctUntilChanged(),
            restored,
            favouriteSlugs,
        ) { search, (choices, tab), isRestored, favouriteSlugs ->
            when {
                !isRestored -> {
                    null
                }

                tab == ListTab.FAVOURITES -> {
                    favouriteSlugs?.let {
                        ListQuery(
                            search = search,
                            choices = choices,
                            tab = tab,
                            favouriteSlugs = it,
                        )
                    }
                }

                // The All tab does not depend on favourites, so a star tap does not reload it.
                else -> {
                    ListQuery(search = search, choices = choices, tab = tab, favouriteSlugs = emptySet())
                }
            }
        }.filter { it != null }.map { it!! }.distinctUntilChanged()

    private val items: Flow<PagingData<SectionedItem>> =
        listQuery
            .flatMapLatest { query ->
                loadedSlugs.value = emptySet()
                catalog.pagedList(query).map { page ->
                    page.map { item ->
                        loadedSlugs.update { it + item.slug }
                        SectionedItem(item, item.section(query.choices.groupBy))
                    }
                }
            }.cachedIn(viewModelScope)

    private val loadedScores: Flow<Map<String, Int>> =
        loadedSlugs
            .debounce(SCORES_DEBOUNCE_MILLIS)
            .flatMapLatest { slugs -> scores.observeScores(slugs) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyMap())

    /** The grid rows: verb cells, with section headers when the list is grouped (L-5). */
    val verbs: Flow<PagingData<VerbListRowUi>> =
        combine(items, favouriteSlugs, loadedScores) { page, favouriteSlugs, scores ->
            page
                .map { (item, section) ->
                    val isFavourite = favouriteSlugs?.contains(item.slug) == true
                    VerbListRowUi.Verb(item.toUi(scores[item.slug], isFavourite), section)
                }.insertSeparators { before: VerbListRowUi.Verb?, after: VerbListRowUi.Verb? ->
                    val section = after?.section ?: return@insertSeparators null
                    if (before?.section == section) null else VerbListRowUi.Header(section)
                }
        }

    init {
        viewModelScope.launch {
            val choices = preferences.choices.first()
            val tab = preferences.tab.first()
            _state.update { it.copy(choices = choices, tab = tab) }
            restored.value = true
        }
        favourites
            .observeCount()
            .onEach { count -> _state.update { it.copy(favouritesCount = count) } }
            .launchIn(viewModelScope)
        syncTrigger.isSyncing
            .onEach { syncing -> _state.update { it.copy(isSyncing = syncing) } }
            .launchIn(viewModelScope)
        catalog
            .isEmpty()
            .distinctUntilChanged()
            .onEach { empty ->
                _state.update { it.copy(isEmptyDatabase = empty) }
                if (empty) requestSyncOnce()
            }.launchIn(viewModelScope)
    }

    fun onAction(action: VerbListAction) {
        _state.value.choices.reduce(action)?.let { choices ->
            // L-9: Clear filters also empties the search; grouping and sort are kept.
            if (action == VerbListAction.OnClearFilters) _state.update { it.copy(query = "") }
            updateChoices(choices)
            return
        }
        when (action) {
            is VerbListAction.OnQueryChange -> _state.update { it.copy(query = action.query) }
            VerbListAction.OnClearQuery -> _state.update { it.copy(query = "") }
            is VerbListAction.OnTabSelect -> selectTab(action.tab)
            VerbListAction.OnOpenSheet -> _state.update { it.copy(isSheetOpen = true) }
            VerbListAction.OnDismissSheet -> _state.update { it.copy(isSheetOpen = false) }
            is VerbListAction.OnToggleFavourite -> toggleFavourite(action.slug)
            is VerbListAction.OnSelectVerb -> _state.update { it.copy(selectedSlug = action.slug) }
            VerbListAction.OnClearSelection -> _state.update { it.copy(selectedSlug = null) }
            else -> Unit
        }
    }

    private fun selectTab(tab: ListTab) {
        _state.update { it.copy(tab = tab) }
        viewModelScope.launch { preferences.setTab(tab) }
    }

    private fun updateChoices(choices: ListChoices) {
        _state.update { it.copy(choices = choices) }
        viewModelScope.launch { preferences.setChoices(choices) }
    }

    private fun toggleFavourite(slug: String) {
        viewModelScope.launch {
            // The favourites flow re-emits on success, so the star updates by itself.
            val result = if (slug in favouriteSlugs.value.orEmpty()) favourites.remove(slug) else favourites.add(slug)
            result.onFailure { _events.send(VerbListEvent.FavouriteUpdateFailed) }
        }
    }

    private var syncRequested = false

    private fun requestSyncOnce() {
        if (syncRequested) return
        syncRequested = true
        syncTrigger.requestSync()
    }

    /** A list item with the section it belongs to for the query's grouping. */
    private data class SectionedItem(val item: VerbListItem, val section: VerbSection?)

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 300L
        const val SCORES_DEBOUNCE_MILLIS = 50L
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
