package com.vamvisions.verbeclair.core.data.di

import android.content.Context
import com.vamvisions.verbeclair.core.data.bundle.AndroidResourceFileOpener
import com.vamvisions.verbeclair.core.data.bundle.ResourceFileOpener
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

class AndroidPlatformPaths(context: Context) : PlatformPaths {
    override val verbDatabaseFile: String = context.getDatabasePath(VERB_DATABASE_NAME).absolutePath
    override val userDatabaseFile: String = context.getDatabasePath(USER_DATABASE_NAME).absolutePath
    override val dataStoreFile: String =
        File(context.filesDir, "datastore/$DATA_STORE_FILE_NAME").absolutePath
}

actual val platformModule: Module = module {
    single<PlatformPaths> { AndroidPlatformPaths(androidContext()) }
    single<ResourceFileOpener> { AndroidResourceFileOpener(androidContext()) }
}
