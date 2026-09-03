package com.vastavik.computer.data.api

import com.vastavik.computer.BuildConfig

object ApiConfig {
    const val CONNECT_TIMEOUT_SEC = 30L
    const val READ_TIMEOUT_SEC = 30L
    const val WRITE_TIMEOUT_SEC = 30L

    val BASE_URL: String
        get() {
            val raw = BuildConfig.BACKEND_BASE_URL
            return if (raw.endsWith("/")) raw else "$raw/"
        }
}
