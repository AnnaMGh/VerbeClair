package com.vamvisions.verbeclair.core.data.di

import org.koin.core.module.Module

/** Binds [PlatformPaths] and the resource file opener of the current platform. */
expect val platformModule: Module
