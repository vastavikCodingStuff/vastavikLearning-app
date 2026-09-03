package com.vastavik.computer.ui.screens.auth

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

    fun signIn(email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                // Auto-provision the admin account on first ever login (Firebase throws
                // "no user record" until it exists); students hit normal sign-in.
                if (AdminSession.isAdminCredentials(email, password)) {
                    try {
                        authRepository.signInWithEmail(email, password)
                    } catch (_: Exception) {
                        authRepository.signUpWithEmail(email, password)
                    }
                } else if (email.trim().equals(AdminSession.ADMIN_EMAIL, ignoreCase = true)) {
                    throw IllegalStateException("This email is reserved. Students must use their own account.")
                } else {
                    authRepository.signInWithEmail(email, password)
                }
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Sign in failed")
            }
        }
    }

    fun signUp(email: String, password: String) {
        if (email.trim().equals(AdminSession.ADMIN_EMAIL, ignoreCase = true)) {
            _uiState.value = _uiState.value.copy(error = "This email is reserved. Students must use their own account.")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                authRepository.signUpWithEmail(email, password)
                _uiState.value = _uiState.value.copy(isLoading = false)
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
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                authRepository.signInWithGoogle(idToken)
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Google sign in failed")
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
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
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showResetSent: Boolean = false
)
