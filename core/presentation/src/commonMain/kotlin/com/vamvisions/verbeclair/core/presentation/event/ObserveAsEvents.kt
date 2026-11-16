package com.vamvisions.verbeclair.core.presentation.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Collects the one-off [events] of a ViewModel while the screen is at least started, so an event is not
 * handled in the background and is not lost on a configuration change (the ViewModel's channel keeps it).
 * [onEvent] suspends, so it can show a snackbar; the next event waits until it returns.
 */
@Composable
fun <T> ObserveAsEvents(events: Flow<T>, onEvent: suspend (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(events, lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            // Main.immediate runs each event on the main thread without a dispatch hop, so the collector
            // cannot be cancelled (STOPPED) between taking an event off the channel and handling it.
            withContext(Dispatchers.Main.immediate) {
                events.collect { currentOnEvent(it) }
            }
        }
    }
}
