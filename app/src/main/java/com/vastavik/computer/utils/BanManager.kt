package com.vastavik.computer.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.vastavik.computer.data.api.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object BanManager {
    private val _isBanned = MutableStateFlow(false)
    val isBanned = _isBanned.asStateFlow()

    private val _banReason = MutableStateFlow("Your account has been banned and deleted by the administrator.")
    val banReason = _banReason.asStateFlow()

    fun handleUserBanned(context: Context? = null, reason: String? = null) {
        _banReason.value = reason ?: "Your account has been banned and deleted by the administrator."
        _isBanned.value = true

        try {
            FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {}

        context?.let { ctx ->
            try {
                TokenManager(ctx).clearTokens()
                ctx.getSharedPreferences("user_profile", Context.MODE_PRIVATE).edit().clear().apply()
                ctx.getSharedPreferences("vastavik_auth_tokens", Context.MODE_PRIVATE).edit().clear().apply()
            } catch (_: Exception) {}
        }
    }

    fun clearBanState() {
        _isBanned.value = false
    }
}
