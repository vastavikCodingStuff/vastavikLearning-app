package com.vastavik.computer.data.api

import com.vastavik.computer.BuildConfig

object ApiConfig {
    const val CONNECT_TIMEOUT_SEC = 60L
    const val READ_TIMEOUT_SEC = 60L
    const val WRITE_TIMEOUT_SEC = 60L

    // Cold-start retry settings
    const val COLD_START_MAX_RETRIES = 2
    const val COLD_START_RETRY_DELAY_MS = 2500L

    // Environment Presets
    const val URL_LOCAL_EMULATOR = "http://10.0.2.2:8000/"
    const val URL_RENDER_CLOUD = "https://vastaviklearning-backend-app.onrender.com/"
    const val URL_PRODUCTION = "https://api.vastaviklearning.com/"

    // Load-balanced candidates. Render is the live production backend.
    val LOAD_BALANCED_URLS = listOf(URL_RENDER_CLOUD)

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
