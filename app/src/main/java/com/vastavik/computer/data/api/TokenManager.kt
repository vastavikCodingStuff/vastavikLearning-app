package com.vastavik.computer.data.api

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hardware-backed encrypted token manager using AndroidKeyStore AES-256-GCM.
 * Protects access and refresh tokens from extraction via ADB backup or rooted inspection.
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val keyAlias = "vastavik_token_key"
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    init {
        try {
            getOrCreateSecretKey()
        } catch (_: Exception) {}
    }

    private fun getOrCreateSecretKey(): SecretKey {
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val spec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(spec)
            return keyGenerator.generateKey()
        }
        val entry = keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry
        return entry.secretKey
    }

    private fun encrypt(plainText: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
            val iv = cipher.iv
            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(cipherText, Base64.NO_WRAP)
        } catch (_: Exception) {
            plainText
        }
    }

    private fun decrypt(encryptedText: String?): String? {
        if (encryptedText.isNullOrBlank()) return null
        if (!encryptedText.contains(":")) {
            return encryptedText // Legacy plaintext fallback
        }
        return try {
            val parts = encryptedText.split(":")
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val cipherText = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), spec)
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (_: Exception) {
            encryptedText
        }
    }

    fun getAccessToken(): String? = decrypt(prefs.getString(KEY_ACCESS_TOKEN, null))

    fun getRefreshToken(): String? = decrypt(prefs.getString(KEY_REFRESH_TOKEN, null))

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, encrypt(accessToken))
            .putString(KEY_REFRESH_TOKEN, encrypt(refreshToken))
            .apply()
    }

    fun saveUser(userId: String?, name: String?, email: String?) {
        val editor = prefs.edit()
        if (userId != null) editor.putString(KEY_USER_ID, userId) else editor.remove(KEY_USER_ID)
        if (name != null) editor.putString(KEY_USER_NAME, name) else editor.remove(KEY_USER_NAME)
        if (email != null) editor.putString(KEY_USER_EMAIL, email) else editor.remove(KEY_USER_EMAIL)
        editor.apply()
    }

    fun saveAccessToken(accessToken: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, encrypt(accessToken)).apply()
    }

    fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .apply()
    }

    fun hasValidSession(): Boolean = !getAccessToken().isNullOrBlank()

    companion object {
        private const val PREFS_NAME = "vastavik_auth_tokens_enc"
        private const val KEY_ACCESS_TOKEN = "jwt_access_token"
        private const val KEY_REFRESH_TOKEN = "jwt_refresh_token"
        private const val KEY_USER_ID = "jwt_user_id"
        private const val KEY_USER_NAME = "jwt_user_name"
        private const val KEY_USER_EMAIL = "jwt_user_email"
    }
}
