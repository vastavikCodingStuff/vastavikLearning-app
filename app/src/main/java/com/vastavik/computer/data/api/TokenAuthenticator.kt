package com.vastavik.computer.data.api

import com.vastavik.computer.data.api.model.RefreshTokenRequest
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiServiceProvider: Provider<VastavikApiService>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite loops if refresh fails
        if (response.request.header("Retry-Count") != null) {
            return null
        }

        val refreshToken = tokenManager.getRefreshToken() ?: return null

        // Synchronously call refresh endpoint
        return try {
            val refreshCall = apiServiceProvider.get().refreshTokenSync(
                RefreshTokenRequest(refreshToken = refreshToken)
            ).execute()

            if (refreshCall.isSuccessful && refreshCall.body()?.success == true) {
                val newAccessToken = refreshCall.body()?.accessToken
                if (!newAccessToken.isNullOrEmpty()) {
                    tokenManager.saveAccessToken(newAccessToken)

                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccessToken")
                        .header("Retry-Count", "1")
                        .build()
                } else {
                    null
                }
            } else {
                // Token expired completely: log out user
                tokenManager.clearTokens()
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
