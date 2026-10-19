package com.vamvisions.verbeclair.core.data.sync

import com.vamvisions.verbeclair.core.domain.sync.DataSyncTrigger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Data sync is not built yet (L-10): a request does nothing and nothing is ever syncing. */
class NoOpDataSyncTrigger : DataSyncTrigger {
    override val isSyncing: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()

    override fun requestSync() = Unit
}
