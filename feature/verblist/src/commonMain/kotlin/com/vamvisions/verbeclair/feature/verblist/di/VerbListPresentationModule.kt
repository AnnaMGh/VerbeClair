package com.vamvisions.verbeclair.feature.verblist.di

import com.vamvisions.verbeclair.feature.verblist.presentation.VerbListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val verbListPresentationModule =
    module {
        viewModelOf(::VerbListViewModel)
    }
