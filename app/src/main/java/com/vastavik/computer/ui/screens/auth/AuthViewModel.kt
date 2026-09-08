package com.vastavik.computer.ui.screens.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.vastavik.computer.data.repository.AuthRepository
import com.vastavik.computer.data.repository.VastavikApiRepository
import com.vastavik.computer.utils.AdminSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val apiRepository: VastavikApiRepository
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

                    // Attempt background sync for admin
                    try {
                        val fp = context?.let { com.vastavik.computer.utils.DeviceFingerprint.get(it) }
                        apiRepository.login(email, password, fp)
                    } catch (_: Exception) {}
                    try {
                        authRepository.signInWithEmail(email, password)
                    } catch (_: Exception) {}

                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                    return@launch
                }

                if (email.trim().equals(AdminSession.ADMIN_EMAIL, ignoreCase = true)) {
                    throw IllegalStateException("Incorrect password for admin.")
                }

                // 1. Primary: Login via production Backend API (eliminates Firebase 500 error)
                val deviceFp = context?.let { com.vastavik.computer.utils.DeviceFingerprint.get(it) }
                val apiResult = apiRepository.login(email.trim(), password, deviceFp)
                if (apiResult.isSuccess && apiResult.getOrNull()?.success == true) {
                    // Background sync with Firebase if reachable
                    try {
                        authRepository.signInWithEmail(email.trim(), password)
                        FirebaseAuth.getInstance().currentUser?.let { syncUserDocument(it) }
                    } catch (_: Exception) {}
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                    return@launch
                }

                // 2. Fallback: Firebase Auth SDK
                try {
                    authRepository.signInWithEmail(email.trim(), password)
                    FirebaseAuth.getInstance().currentUser?.let { syncUserDocument(it) }
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                } catch (fbErr: Exception) {
                    val rawMsg = fbErr.message ?: ""
                    val cleanMsg = when {
                        rawMsg.contains("500") || rawMsg.contains("internal", ignoreCase = true) ->
                            apiResult.getOrNull()?.errorMessage ?: "Invalid email or password. Please try again."
                        rawMsg.contains("user-not-found") || rawMsg.contains("wrong-password") || rawMsg.contains("INVALID_LOGIN_CREDENTIALS") ->
                            "Invalid email or password."
                        else ->
                            apiResult.getOrNull()?.errorMessage ?: fbErr.message ?: "Sign in failed. Please check your credentials."
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, error = cleanMsg)
                }
            } catch (e: Exception) {
                val cleanError = if (e.message?.contains("500") == true) "Unable to connect to authentication service." else (e.message ?: "Sign in failed")
                _uiState.value = _uiState.value.copy(isLoading = false, error = cleanError)
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

    fun signUp(email: String, password: String, name: String = "", board: String = "ICSE", context: Context? = null) {
        if (email.trim().equals(AdminSession.ADMIN_EMAIL, ignoreCase = true)) {
            _uiState.value = _uiState.value.copy(error = "This email is reserved. Students must use their own account.")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSuccess = false)
        viewModelScope.launch {
            try {
                val studentName = name.ifBlank {
                    email.substringBefore("@").replaceFirstChar { it.uppercase() }
                }

                val pendingPrefs = context?.getSharedPreferences("growth_attribution", Context.MODE_PRIVATE)
                val pendingRef = pendingPrefs?.getString("pending_referral_code", null)
                val pendingShare = pendingPrefs?.getString("pending_share_token", null)
                val deviceFp = context?.let { com.vastavik.computer.utils.DeviceFingerprint.get(it) }
                val deviceName = context?.let { com.vastavik.computer.utils.DeviceFingerprint.getDeviceName() }
                val platform = com.vastavik.computer.utils.DeviceFingerprint.getPlatform()

                val apiResult = apiRepository.signup(
                    email = email.trim(),
                    password = password,
                    name = studentName,
                    board = board,
                    referralCode = pendingRef,
                    shareToken = pendingShare,
                    deviceFingerprint = deviceFp,
                    deviceName = deviceName,
                    platform = platform
                )

                pendingPrefs?.edit()?.remove("pending_referral_code")?.remove("pending_share_token")?.apply()

                if (apiResult.isSuccess && apiResult.getOrNull()?.success == true) {
                    // Background Firebase Auth registration if available
                    try {
                        authRepository.signUpWithEmail(email.trim(), password)
                        FirebaseAuth.getInstance().currentUser?.let { syncUserDocument(it) }
                    } catch (_: Exception) {}
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                    return@launch
                }

                // 2. Fallback: Firebase Auth SDK
                try {
                    authRepository.signUpWithEmail(email.trim(), password)
                    FirebaseAuth.getInstance().currentUser?.let { syncUserDocument(it) }
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                } catch (fbErr: Exception) {
                    val rawMsg = fbErr.message ?: ""
                    val cleanMsg = when {
                        rawMsg.contains("500") || rawMsg.contains("internal", ignoreCase = true) ->
                            apiResult.getOrNull()?.errorMessage ?: "Sign up encountered a server error. Please try again in a few moments."
                        rawMsg.contains("email-already-in-use") ->
                            "An account with this email address already exists. Please log in."
                        else ->
                            apiResult.getOrNull()?.errorMessage ?: fbErr.message ?: "Sign up failed"
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, error = cleanMsg)
                }
            } catch (e: Exception) {
                val cleanError = if (e.message?.contains("500") == true) "Registration temporarily unavailable. Please try again." else (e.message ?: "Sign up failed")
                _uiState.value = _uiState.value.copy(isLoading = false, error = cleanError)
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
                // Try backend Google OAuth
                val apiRes = apiRepository.loginWithGoogle(idToken)
                if (apiRes.isSuccess && apiRes.getOrNull()?.success == true) {
                    try {
                        authRepository.signInWithGoogle(idToken)
                        FirebaseAuth.getInstance().currentUser?.let { syncUserDocument(it) }
                    } catch (_: Exception) {}
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                    return@launch
                }

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
