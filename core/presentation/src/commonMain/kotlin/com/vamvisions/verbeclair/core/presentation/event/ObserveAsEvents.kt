package com.vamvisions.verbeclair.core.presentation.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Collects the one-off [events] of a ViewModel while the screen is at least started, so an event is not
 * handled in the background and is not lost on a configuration change (the ViewModel's channel keeps it).
 * [onEvent] suspends, so it can show a snackbar; the next event waits until it returns.
 */
@Composable
fun <T> ObserveAsEvents(events: Flow<T>, onEvent: suspend (T) -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(events, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            // repeatOnLifecycle waits for this child, so the collection lasts the whole STARTED period.
            events.onEach { currentOnEvent(it) }.launchIn(this)
        }
    }
}
