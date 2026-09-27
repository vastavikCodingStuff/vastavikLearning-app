package com.vastavik.computer.utils

import android.content.Context
import android.net.Uri
import com.vastavik.computer.BuildConfig
import java.security.SecureRandom

/**
 * GitHub OAuth web-application flow helpers.
 *
 * Flow:
 * 1. LoginScreen builds the authorize URL (client_id + redirect_uri + state).
 * 2. User approves in the browser.
 * 3. GitHub redirects to the app's custom scheme (default vastavik://oauth/github?code=..&state=..).
 * 4. MainActivity forwards the result through [GitHubOAuthBridge].
 * 5. AuthViewModel exchanges the single-use code at the backend
 *    (POST /api/v1/auth/oauth/github) which holds the client secret.
 */
object GitHubOAuth {

    const val PREFS_NAME = "github_oauth"
    private const val KEY_PENDING_STATE = "pending_state"

    val redirectUri: String
        get() = BuildConfig.GITHUB_OAUTH_REDIRECT_URI.ifBlank { "vastavik://oauth/github" }

    val isConfigured: Boolean
        get() = BuildConfig.GITHUB_CLIENT_ID.isNotBlank()

    /** Generates a fresh CSRF state token and persists it for verification. */
    fun newPendingState(context: Context): String {
        val bytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val state = bytes.joinToString("") { "%02x".format(it) }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PENDING_STATE, state)
            .apply()
        return state
    }

    /** Returns the stored state once (consume-on-read) and clears it. */
    fun consumePendingState(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val state = prefs.getString(KEY_PENDING_STATE, null)
        prefs.edit().remove(KEY_PENDING_STATE).apply()
        return state
    }

    /** Builds the GitHub authorize URL, or null when the client id is not configured. */
    fun buildAuthorizeUrl(state: String): String? {
        val clientId = BuildConfig.GITHUB_CLIENT_ID
        if (clientId.isBlank()) return null
        return "https://github.com/login/oauth/authorize" +
                "?client_id=${Uri.encode(clientId)}" +
                "&redirect_uri=${Uri.encode(redirectUri)}" +
                "&scope=${Uri.encode("read:user")}" +
                "&state=${Uri.encode(state)}"
    }
}

/**
 * Bridges the OAuth redirect from MainActivity to whichever screen is listening.
 * If no listener is registered yet (cold start straight into the redirect), the
 * result is stashed and replayed when the first listener subscribes.
 */
object GitHubOAuthBridge {

    private var pendingResult: Triple<String?, String?, String?>? = null

    var onResult: ((code: String?, state: String?, error: String?) -> Unit)? = null
        set(value) {
            field = value
            val pending = pendingResult
            if (value != null && pending != null) {
                pendingResult = null
                value(pending.first, pending.second, pending.third)
            }
        }

    /** Main thread only (called from onCreate / onNewIntent). */
    fun deliver(code: String?, state: String?, error: String?) {
        val listener = onResult
        if (listener != null) {
            listener(code, state, error)
        } else {
            pendingResult = Triple(code, state, error)
        }
    }
}
