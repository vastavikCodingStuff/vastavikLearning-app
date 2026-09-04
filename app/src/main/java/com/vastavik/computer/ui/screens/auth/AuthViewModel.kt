package com.vastavik.computer.ui.screens.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.vastavik.computer.data.repository.AuthRepository
import com.vastavik.computer.utils.AdminSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private val _authState = MutableStateFlow<FirebaseUser?>(FirebaseAuth.getInstance().currentUser)
    val authState = _authState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.getAuthState().collect { user ->
                _authState.value = user
                AdminSession.update(user)
            }
        }
    }

    fun signIn(email: String, password: String, context: Context? = null) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSuccess = false)
        viewModelScope.launch {
            try {
                if (AdminSession.isAdminCredentials(email, password)) {
                    // Instantly set admin session locally so admin NEVER gets blocked by Firebase issues
                    context?.let { AdminSession.setAdminLoggedIn(it, true) }
                    AdminSession.update(null)

                    // Attempt background Firebase sync for admin without blocking if it errors
                    try {
                        try {
                            authRepository.signInWithEmail(email, password)
                        } catch (_: Exception) {
                            authRepository.signUpWithEmail(email, password)
                        }
                    } catch (_: Exception) {
                        // Ignore Firebase failures for admin; local session guarantees direct access
                    }

                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                    return@launch
                }

                if (email.trim().equals(AdminSession.ADMIN_EMAIL, ignoreCase = true)) {
                    throw IllegalStateException("Incorrect password for admin.")
                }

                authRepository.signInWithEmail(email, password)
                FirebaseAuth.getInstance().currentUser?.let { syncUserDocument(it) }
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Sign in failed")
            }
        }
    }

    private suspend fun syncUserDocument(user: FirebaseUser) {
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val docRef = db.collection(com.vastavik.computer.utils.Constants.COLLECTION_USERS).document(user.uid)
            val snap = docRef.get().await()
            if (!snap.exists()) {
                val displayName = user.displayName?.takeIf { it.isNotBlank() }
                    ?: user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                    ?: "Student"
                val newUser = com.vastavik.computer.data.model.UserModel(
                    uid = user.uid,
                    name = displayName,
                    email = user.email ?: "",
                    role = if (com.vastavik.computer.utils.AdminSession.isAdminCredentials(user.email ?: "", "")) "admin" else "student",
                    createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                )
                docRef.set(newUser.toMap()).await()
            } else {
                docRef.update("lastActiveDate", com.google.firebase.firestore.FieldValue.serverTimestamp())
            }
        } catch (e: Exception) {
            android.util.Log.w("AuthViewModel", "User document sync failed: ${e.message}")
        }
    }

    fun loginAsAdmin(context: Context) {
        signIn(AdminSession.ADMIN_EMAIL, AdminSession.ADMIN_PASSWORD, context)
    }

    fun signUp(email: String, password: String) {
        if (email.trim().equals(AdminSession.ADMIN_EMAIL, ignoreCase = true)) {
            _uiState.value = _uiState.value.copy(error = "This email is reserved. Students must use their own account.")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSuccess = false)
        viewModelScope.launch {
            try {
                authRepository.signUpWithEmail(email, password)
                FirebaseAuth.getInstance().currentUser?.let { syncUserDocument(it) }
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Sign up failed")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        if (idToken.isEmpty()) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = "Google sign-in was cancelled")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSuccess = false)
        viewModelScope.launch {
            try {
                authRepository.signInWithGoogle(idToken)
                FirebaseAuth.getInstance().currentUser?.let { syncUserDocument(it) }
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Google sign in failed")
            }
        }
    }

    fun signOut(context: Context? = null) {
        context?.let { AdminSession.setAdminLoggedIn(it, false) }
        try {
            authRepository.signOut()
        } catch (_: Exception) {}
        AdminSession.update(null)
    }

    fun sendPasswordReset(email: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                authRepository.sendPasswordResetEmail(email)
                _uiState.value = _uiState.value.copy(isLoading = false, showResetSent = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to send reset email")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val showResetSent: Boolean = false
)
