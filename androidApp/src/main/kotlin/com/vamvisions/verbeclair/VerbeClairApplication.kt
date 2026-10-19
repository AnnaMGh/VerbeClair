package com.vamvisions.verbeclair

import android.app.Application
import com.vamvisions.verbeclair.di.initKoin
import org.koin.android.ext.koin.androidContext

class VerbeClairApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@VerbeClairApplication)
        }
    }
}
