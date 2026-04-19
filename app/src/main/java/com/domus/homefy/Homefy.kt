package com.domus.homefy

import android.app.Application
import com.domus.homefy.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Homefy : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@Homefy)
            modules(appModule)
        }
    }
}