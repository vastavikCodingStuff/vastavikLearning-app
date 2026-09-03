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

    fun extractVideoId(url: String): String? {
        if (url.length == 11 && !url.contains("/") && !url.contains("?")) return url
        val regex = Regex("""(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/embed/)([A-Za-z0-9_-]{11})""")
        return regex.find(url)?.groupValues?.get(1)
    }
}
