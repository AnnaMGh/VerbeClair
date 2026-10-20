package com.vamvisions.verbeclair.di

import com.vamvisions.verbeclair.core.data.di.coreDataModule
import com.vamvisions.verbeclair.core.data.di.platformModule
import com.vamvisions.verbeclair.feature.verblist.di.verbListPresentationModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/** Every Koin module of the app, one per layer (design D12). */
val appModules: List<Module> =
    listOf(
        platformModule,
        coreDataModule,
        verbListPresentationModule,
    )

fun initKoin(config: KoinAppDeclaration = {}) {
    startKoin {
        config()
        modules(appModules)
    }
}
