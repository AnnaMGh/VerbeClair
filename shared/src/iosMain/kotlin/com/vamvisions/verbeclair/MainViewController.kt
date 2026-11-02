package com.vamvisions.verbeclair

import androidx.compose.ui.window.ComposeUIViewController
import com.vamvisions.verbeclair.di.initKoin
import org.koin.mp.KoinPlatformTools

fun MainViewController() =
    ComposeUIViewController(
        configure = {
            // The view controller can be recreated; Koin must only be started once per process.
            if (KoinPlatformTools.defaultContext().getOrNull() == null) {
                initKoin()
            }
        },
    ) { App() }
