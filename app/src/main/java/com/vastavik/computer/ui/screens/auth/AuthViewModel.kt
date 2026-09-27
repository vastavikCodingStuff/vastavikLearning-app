package com.vastavik.computer.ui.screens.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clerk.api.Clerk
import com.clerk.api.auth.types.MfaType
import com.clerk.api.auth.types.VerificationType
import com.clerk.api.network.serialization.errorMessage
import com.clerk.api.network.serialization.flatMap
import com.clerk.api.network.serialization.onFailure
import com.clerk.api.network.serialization.onSuccess
import com.clerk.api.signin.SignIn
import com.clerk.api.signin.sendMfaEmailCode
import com.clerk.api.signin.verifyMfaCode
import com.clerk.api.signup.sendCode
import com.clerk.api.signup.verifyCode
import com.clerk.api.sso.OAuthProvider
import com.clerk.api.sso.ResultType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.vastavik.computer.data.repository.AuthRepository
import com.vastavik.computer.data.repository.VastavikApiRepository
import com.vastavik.computer.utils.AdminSession
import com.vastavik.computer.utils.ClerkSessionBridge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
        if (clerkEnabled) observeClerkUser()
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

    fun signInWithGitHub(code: String) {
        if (code.isBlank()) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = "GitHub sign-in was cancelled")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSuccess = false)
        viewModelScope.launch {
            try {
                // Backend exchanges the code for a GitHub token (client secret lives
                // only on the server), provisions the user and returns backend JWTs.
                val apiRes = apiRepository.loginWithGitHub(code)
                if (apiRes.isSuccess && apiRes.getOrNull()?.success == true) {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                    return@launch
                }
                val friendly = githubErrorMessage(apiRes.exceptionOrNull(), apiRes.getOrNull()?.errorMessage)
                _uiState.value = _uiState.value.copy(isLoading = false, error = friendly)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "GitHub sign in failed")
            }
        }
    }

    /** Extracts FastAPI {"detail": "..."} messages from failed OAuth responses. */
    private fun githubErrorMessage(e: Throwable?, fallback: String?): String {
        if (e is retrofit2.HttpException) {
            if (e.code() == 501) {
                return "GitHub sign-in is temporarily unavailable. Please use email or Google instead."
            }
            val detail = try {
                val body = e.response()?.errorBody()?.string()
                    ?: return fallback ?: "GitHub sign in failed. Please try again."
                val json = org.json.JSONObject(body)
                when (val d = json.opt("detail")) {
                    is String -> d
                    is org.json.JSONArray -> d.optJSONObject(0)?.optString("msg")?.takeIf { it.isNotBlank() }
                    else -> null
                }
            } catch (_: Exception) { null }
            return detail ?: fallback ?: "GitHub sign in failed. Please try again."
        }
        return fallback ?: e?.message ?: "GitHub sign in failed. Please try again."
    }

    // ==========================================
    // Clerk auth (email/password + Google/GitHub OAuth + email OTP)
    // ==========================================

    val clerkEnabled: Boolean
        get() = ClerkSessionBridge.enabled

    private val _clerkOtpPrompt = MutableStateFlow<ClerkOtpPrompt?>(null)
    val clerkOtpPrompt = _clerkOtpPrompt.asStateFlow()

    private var pendingClerkAuth = false

    private fun observeClerkUser() {
        viewModelScope.launch {
            Clerk.userFlow.collect { user ->
                if (user != null && pendingClerkAuth) {
                    pendingClerkAuth = false
                    finishClerkAuth()
                }
            }
        }
    }

    private suspend fun finishClerkAuth() {
        ClerkSessionBridge.exchangeForBackendTokens(apiRepository)
        _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, error = null)
    }

    /** True when a backend JWT or a Clerk session is persisted (app-restart session restore). */
    suspend fun hasPersistedSession(): Boolean {
        if (!apiRepository.getAccessToken().isNullOrBlank()) return true
        if (clerkEnabled) {
            try {
                Clerk.isInitialized.first { it }
                if (Clerk.user != null) {
                    ClerkSessionBridge.exchangeForBackendTokens(apiRepository)
                    return true
                }
            } catch (_: Exception) {}
        }
        return false
    }

    private fun failClerk(message: String?) {
        pendingClerkAuth = false
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = message?.takeIf { it.isNotBlank() } ?: "Authentication failed. Please try again."
        )
    }

    fun signUpWithClerk(email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSuccess = false)
        viewModelScope.launch {
            try {
                Clerk.auth
                    .signUp {
                        this.email = email
                        this.password = password
                    }
                    .flatMap { it.sendCode { this.email = email } }
                    .onSuccess {
                        _clerkOtpPrompt.value = ClerkOtpPrompt(email = email, signUp = true)
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    .onFailure { failClerk(it.errorMessage) }
            } catch (e: Exception) {
                failClerk(e.message)
            }
        }
    }

    fun verifyClerkOtp(code: String) {
        val prompt = _clerkOtpPrompt.value ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSuccess = false)
        viewModelScope.launch {
            try {
                if (prompt.signUp) {
                    val signUp = Clerk.auth.currentSignUp
                    if (signUp == null) {
                        failClerk("Verification session expired. Please sign up again.")
                        _clerkOtpPrompt.value = null
                        return@launch
                    }
                    signUp.verifyCode(code, VerificationType.EMAIL)
                        .onSuccess {
                            _clerkOtpPrompt.value = null
                            finishClerkAuth()
                        }
                        .onFailure { failClerk(it.errorMessage) }
                } else {
                    val signIn = Clerk.auth.currentSignIn
                    if (signIn == null) {
                        failClerk("Verification session expired. Please sign in again.")
                        _clerkOtpPrompt.value = null
                        return@launch
                    }
                    signIn.verifyMfaCode(code, MfaType.EMAIL_CODE)
                        .onSuccess { updated ->
                            if (updated.status == SignIn.Status.COMPLETE) {
                                _clerkOtpPrompt.value = null
                                finishClerkAuth()
                            } else {
                                failClerk("Verification incomplete. Please try again.")
                            }
                        }
                        .onFailure { failClerk(it.errorMessage) }
                }
            } catch (e: Exception) {
                failClerk(e.message)
            }
        }
    }

    fun resendClerkOtp() {
        val prompt = _clerkOtpPrompt.value ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                if (prompt.signUp) {
                    Clerk.auth.currentSignUp
                        ?.sendCode { this.email = prompt.email }
                        ?.onSuccess { _uiState.value = _uiState.value.copy(isLoading = false) }
                        ?: failClerk("Verification session expired. Please sign up again.")
                } else {
                    Clerk.auth.currentSignIn
                        ?.sendMfaEmailCode()
                        ?.onSuccess { _uiState.value = _uiState.value.copy(isLoading = false) }
                        ?: failClerk("Verification session expired. Please sign in again.")
                }
            } catch (e: Exception) {
                failClerk(e.message)
            }
        }
    }

    fun cancelClerkOtp() {
        _clerkOtpPrompt.value = null
        _uiState.value = _uiState.value.copy(isLoading = false, error = null)
    }

    fun signInWithClerk(email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSuccess = false)
        viewModelScope.launch {
            try {
                Clerk.auth
                    .signInWithPassword {
                        identifier = email
                        this.password = password
                    }
                    .onSuccess { signIn ->
                        when (signIn.status) {
                            SignIn.Status.COMPLETE -> finishClerkAuth()
                            SignIn.Status.NEEDS_SECOND_FACTOR,
                            SignIn.Status.NEEDS_CLIENT_TRUST -> {
                                val mfa = signIn.sendMfaEmailCode()
                                mfa.onSuccess {
                                    _clerkOtpPrompt.value = ClerkOtpPrompt(email = email, signUp = false)
                                    _uiState.value = _uiState.value.copy(isLoading = false)
                                }
                                mfa.onFailure { failClerk(it.errorMessage) }
                            }
                            else -> failClerk("Sign-in needs additional verification. Please try again.")
                        }
                    }
                    .onFailure { failClerk(it.errorMessage) }
            } catch (e: Exception) {
                failClerk(e.message)
            }
        }
    }

    fun signInWithClerkOAuth(provider: OAuthProvider) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSuccess = false)
        pendingClerkAuth = true
        viewModelScope.launch {
            try {
                Clerk.auth
                    .signInWithOAuth(provider)
                    .onSuccess { result ->
                        val complete = when (result.resultType) {
                            ResultType.SIGN_IN -> result.signIn?.status == SignIn.Status.COMPLETE
                            ResultType.SIGN_UP -> result.signUp?.status == com.clerk.api.signup.SignUp.Status.COMPLETE
                            ResultType.UNKNOWN -> false
                        }
                        if (complete || Clerk.userFlow.value != null) {
                            pendingClerkAuth = false
                            finishClerkAuth()
                        } else {
                            // Redirect handled; the userFlow observer finishes the job.
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                    }
                    .onFailure { failClerk(it.errorMessage) }
            } catch (e: Exception) {
                failClerk(e.message)
            }
        }
    }

    fun signOut(context: Context? = null) {
        context?.let { AdminSession.setAdminLoggedIn(it, false) }
        try {
            authRepository.signOut()
        } catch (_: Exception) {}
        if (clerkEnabled) {
            viewModelScope.launch {
                try {
                    Clerk.auth.signOut()
                } catch (_: Exception) {}
            }
        }
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

/** Non-null while a Clerk email OTP step (sign-up verification or MFA) is pending. */
data class ClerkOtpPrompt(
    val email: String,
    val signUp: Boolean
)
