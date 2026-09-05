package com.vastavik.computer.data.api

import com.google.firebase.auth.FirebaseAuth
import com.vastavik.computer.BuildConfig
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager? = null
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        // 1. Static API key headers
        val keyId = BuildConfig.API_KEY_ID.ifBlank { "vastavik_prod_v1" }
        val keySecret = BuildConfig.API_KEY_SECRET.ifBlank { "dev-secret-android-32bytes-hex-0000" }
        builder.header("x-api-key-id", keyId)
        builder.header("x-api-key-secret", keySecret)

        // 2. Authorization Header (JWT from TokenManager, with Firebase ID token fallback)
        val jwtToken = tokenManager?.getAccessToken()
        if (!jwtToken.isNullOrEmpty()) {
            builder.header("Authorization", "Bearer $jwtToken")
        } else {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                try {
                    val token = runBlocking { user.getIdToken(false).await().token }
                    if (!token.isNullOrEmpty()) {
                        builder.header("Authorization", "Bearer $token")
                    }
                } catch (_: Exception) {
                }
            }
        }

        // 3. HMAC-SHA256 signature (timestamp in seconds + UPPERCASE method + encoded path)
        try {
            val timestamp = (System.currentTimeMillis() / 1000).toString()
            val method = original.method.uppercase()
            val path = original.url.encodedPath
            val signature = HmacUtil.hmacSha256Hex(keySecret, "$timestamp$method$path")

            builder.header("x-timestamp", timestamp)
            builder.header("x-hmac", signature)
        } catch (_: Exception) {
        }

        return chain.proceed(builder.build())
    }
}
