package com.vastavik.computer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VastavikApplication : Application() {
    companion object {
        lateinit var instance: VastavikApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Immediately and asynchronously pre-warm Render cloud backend
        com.vastavik.computer.data.api.BackendWarmupManager.warmUpAsync()
    }
}