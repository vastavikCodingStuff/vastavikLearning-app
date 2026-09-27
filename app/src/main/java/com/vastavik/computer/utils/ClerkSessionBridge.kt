package com.vastavik.computer.utils

import com.clerk.api.Clerk
import com.clerk.api.network.serialization.ClerkResult
import com.vastavik.computer.BuildConfig
import com.vastavik.computer.data.repository.VastavikApiRepository
import kotlinx.coroutines.flow.first

/**
 * Exchanges a Clerk session token for the backend's own JWTs so every existing
 * API call keeps working unchanged after a Clerk-based sign-in/sign-up.
 */
object ClerkSessionBridge {

    val enabled: Boolean
        get() = BuildConfig.CLERK_PUBLISHABLE_KEY.isNotBlank()

    suspend fun exchangeForBackendTokens(apiRepository: VastavikApiRepository): Boolean {
        if (!enabled) return false
        return try {
            Clerk.isInitialized.first { it }
            if (Clerk.session == null) return false
            when (val result = Clerk.auth.getToken()) {
                is ClerkResult.Success -> {
                    val res = apiRepository.loginWithClerk(result.value)
                    res.getOrNull()?.success == true
                }
                is ClerkResult.Failure -> false
            }
        } catch (e: Exception) {
            android.util.Log.w("ClerkSessionBridge", "exchange failed: ${e.message}")
            false
        }
    }
}
