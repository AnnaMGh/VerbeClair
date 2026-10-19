package com.vamvisions.verbeclair.core.data.sync

import kotlin.test.Test
import kotlin.test.assertFalse

class NoOpDataSyncTriggerTest {
    @Test
    fun requestingASyncNeverStartsSyncing() {
        val trigger = NoOpDataSyncTrigger()

        trigger.requestSync()

        assertFalse(trigger.isSyncing.value)
    }
}
