package com.vamvisions.verbeclair.core.domain.preferences

import com.vamvisions.verbeclair.core.domain.verb.ListChoices
import com.vamvisions.verbeclair.core.domain.verb.ListTab
import kotlinx.coroutines.flow.Flow

/** The remembered verb-list choices and tab (L-7, L-12). The search text is never stored. */
interface ListPreferences {
    val choices: Flow<ListChoices>
    val tab: Flow<ListTab>

    suspend fun setChoices(choices: ListChoices)

    suspend fun setTab(tab: ListTab)
}
