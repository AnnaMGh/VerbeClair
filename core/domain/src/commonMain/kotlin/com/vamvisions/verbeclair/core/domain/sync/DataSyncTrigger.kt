package com.vamvisions.verbeclair.core.domain.sync

import kotlinx.coroutines.flow.StateFlow

/** Asks for a data sync (L-10). Sync is not built yet, so the only implementation does nothing. */
interface DataSyncTrigger {
    val isSyncing: StateFlow<Boolean>

    fun requestSync()
}
