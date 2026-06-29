package com.jksalcedo.tend

import android.app.Application
import com.jksalcedo.tend.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TendApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TendApp)
            modules(appModule)
        }
    }
}