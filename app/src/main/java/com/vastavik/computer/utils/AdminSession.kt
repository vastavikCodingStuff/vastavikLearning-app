package com.vastavik.computer.utils

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdminSession {
    const val ADMIN_EMAIL = "admin@admin.admin"
    const val ADMIN_PASSWORD = "admin@admin"

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin = _isAdmin.asStateFlow()

    fun update(user: FirebaseUser?) {
        _isAdmin.value = user?.email?.equals(ADMIN_EMAIL, ignoreCase = true) == true
    }

    fun isAdminCredentials(email: String, password: String): Boolean =
        email.trim().equals(ADMIN_EMAIL, ignoreCase = true) && password == ADMIN_PASSWORD
}
