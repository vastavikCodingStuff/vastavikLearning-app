package com.vastavik.computer.data.api

import com.google.firebase.auth.FirebaseAuth
import com.vastavik.computer.BuildConfig
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        // Static API key headers
        builder.addHeader("x-api-key-id", BuildConfig.API_KEY_ID)
        builder.addHeader("x-api-key-secret", BuildConfig.API_KEY_SECRET)

        // Firebase ID token if available
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            try {
                val token = runBlocking { user.getIdToken(false).await().token }
                if (!token.isNullOrEmpty()) {
                    builder.addHeader("Authorization", "Bearer $token")
                }
            } catch (_: Exception) {
            }
        }

        // HMAC — timestamp + method + path
        try {
            val timestamp = System.currentTimeMillis().toString()
            val data = timestamp + original.method + original.url.encodedPath
            val hmac = HmacUtil.hmacSha256(BuildConfig.API_KEY_SECRET, data)
            builder.addHeader("x-timestamp", timestamp)
            builder.addHeader("x-hmac", hmac)
        } catch (_: Exception) {
        }

        return chain.proceed(builder.build())
    }
}
