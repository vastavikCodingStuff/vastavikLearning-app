package com.vastavik.computer.data.api

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

object HmacUtil {
    private const val HMAC_ALGO = "HmacSHA256"

    fun hmacSha256(secret: String, data: String): String {
        val mac = Mac.getInstance(HMAC_ALGO)
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), HMAC_ALGO))
        val bytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun hmacSha256Hex(secret: String, message: String): String {
        val sha256Hmac = Mac.getInstance(HMAC_ALGO)
        val secretKey = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), HMAC_ALGO)
        sha256Hmac.init(secretKey)
        val hash = sha256Hmac.doFinal(message.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun extractVideoId(url: String?): String? {
        if (url.isNullOrBlank()) return null
        val trimmed = url.trim()
        // 1. Direct 11-char ID
        if (trimmed.length == 11 && !trimmed.contains("/") && !trimmed.contains("?") && !trimmed.contains("&")) {
            return trimmed
        }
        // 2. Standard watch URL (matches v= parameter regardless of order)
        val watchRegex = Regex("""[?&]v=([A-Za-z0-9_-]{11})""")
        watchRegex.find(trimmed)?.groupValues?.get(1)?.let { return it }

        // 3. youtu.be short URL
        val youtuBeRegex = Regex("""youtu\.be/([A-Za-z0-9_-]{11})""")
        youtuBeRegex.find(trimmed)?.groupValues?.get(1)?.let { return it }

        // 4. Shorts URL
        val shortsRegex = Regex("""youtube\.com/shorts/([A-Za-z0-9_-]{11})""")
        shortsRegex.find(trimmed)?.groupValues?.get(1)?.let { return it }

        // 5. Embed, live, and v URLs
        val pathRegex = Regex("""youtube\.com/(?:embed|live|v)/([A-Za-z0-9_-]{11})""")
        pathRegex.find(trimmed)?.groupValues?.get(1)?.let { return it }

        return null
    }
}
