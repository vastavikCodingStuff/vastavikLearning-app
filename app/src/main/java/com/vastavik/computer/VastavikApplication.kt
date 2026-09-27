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
        // Clerk auth (email/password, Google/GitHub OAuth, email OTP) — initialized
        // only when a publishable key is configured; otherwise the legacy auth stands.
        if (BuildConfig.CLERK_PUBLISHABLE_KEY.isNotBlank()) {
            try {
                com.clerk.api.Clerk.initialize(this, publishableKey = BuildConfig.CLERK_PUBLISHABLE_KEY)
            } catch (e: Exception) {
                android.util.Log.w("VastavikApplication", "Clerk init failed: ${e.message}")
            }
        }
        // Immediately and asynchronously pre-warm Render cloud backend
        com.vastavik.computer.data.api.BackendWarmupManager.warmUpAsync()
    }
}