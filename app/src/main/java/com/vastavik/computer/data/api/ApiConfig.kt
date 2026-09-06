package com.vastavik.computer.data.api

import com.vastavik.computer.BuildConfig

object ApiConfig {
    const val CONNECT_TIMEOUT_SEC = 30L
    const val READ_TIMEOUT_SEC = 30L
    const val WRITE_TIMEOUT_SEC = 30L

    // Environment Presets
    const val URL_LOCAL_EMULATOR = "http://10.0.2.2:8000/"
    const val URL_RENDER_CLOUD = "https://vastaviklearning-backend-app.onrender.com/"
    const val URL_RAILWAY_CLOUD = "https://vastaviklearning-backend-app.up.railway.app/"
    const val URL_PRODUCTION = "https://api.vastaviklearning.com/"

    // Load-balanced candidates (Render + Railway). Order = default preference.
    val LOAD_BALANCED_URLS = listOf(URL_RENDER_CLOUD, URL_RAILWAY_CLOUD)

    val BASE_URL: String
        get() {
            val raw = BuildConfig.BACKEND_BASE_URL.ifBlank { URL_RENDER_CLOUD }
            return if (raw.endsWith("/")) raw else "$raw/"
        }

    val WS_BASE_URL: String
        get() {
            val base = BASE_URL.removeSuffix("/")
            return when {
                base.startsWith("https://") -> "wss://${base.removePrefix("https://")}"
                base.startsWith("http://") -> "ws://${base.removePrefix("http://")}"
                else -> "wss://$base"
            }
        }
}
