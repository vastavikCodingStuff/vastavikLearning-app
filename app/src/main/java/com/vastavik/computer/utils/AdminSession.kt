package com.vastavik.computer.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdminSession {
    const val ADMIN_EMAIL = "admin@admin.admin"
    const val ADMIN_PASSWORD = "admin@admin"
    private const val PREFS_NAME = "vastavik_admin_session"
    private const val KEY_IS_ADMIN = "is_admin_logged_in"
    private const val KEY_ENGINE_LOGS_ENABLED = "engine_logs_enabled"

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin = _isAdmin.asStateFlow()

    private val _isEngineLogsEnabled = MutableStateFlow(true)
    val isEngineLogsEnabled = _isEngineLogsEnabled.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedAdmin = prefs.getBoolean(KEY_IS_ADMIN, false)
        val firebaseUser = try { FirebaseAuth.getInstance().currentUser } catch (_: Exception) { null }
        val isFirebaseAdmin = firebaseUser?.email?.equals(ADMIN_EMAIL, ignoreCase = true) == true
        _isAdmin.value = savedAdmin || isFirebaseAdmin
        _isEngineLogsEnabled.value = prefs.getBoolean(KEY_ENGINE_LOGS_ENABLED, true)
    }

    fun setAdminLoggedIn(context: Context, loggedIn: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_ADMIN, loggedIn).apply()
        _isAdmin.value = loggedIn
    }

    fun setEngineLogsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ENGINE_LOGS_ENABLED, enabled).apply()
        _isEngineLogsEnabled.value = enabled
    }

    fun update(user: FirebaseUser?) {
        if (_isAdmin.value && user == null) {
            // Keep local admin session active
            return
        }
        _isAdmin.value = user?.email?.equals(ADMIN_EMAIL, ignoreCase = true) == true
    }

    fun isAdminCredentials(email: String, password: String): Boolean =
        email.trim().equals(ADMIN_EMAIL, ignoreCase = true) && password == ADMIN_PASSWORD
}
