package com.vastavik.computer.data.api

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-app load balancer that pings Render and Railway backends,
 * then always routes requests to whichever responds fastest.
 *
 * Health checks run every HEALTH_CHECK_INTERVAL_MS in the background.
 * Falls back gracefully: if one backend is down the other is used.
 */
@Singleton
class BackendLoadBalancer @Inject constructor() {

    companion object {
        private const val TAG = "BackendLoadBalancer"
        private const val HEALTH_CHECK_INTERVAL_MS = 30_000L
        private const val PING_TIMEOUT_SEC = 5L
        private const val HEALTH_PATH = "health"
    }

    // Dedicated lightweight client just for pings (no auth, short timeout)
    private val pingClient = OkHttpClient.Builder()
        .connectTimeout(PING_TIMEOUT_SEC, TimeUnit.SECONDS)
        .readTimeout(PING_TIMEOUT_SEC, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Holds the currently best base URL
    private val _bestUrl = AtomicReference(ApiConfig.LOAD_BALANCED_URLS.first())
    val bestBaseUrl: String get() = _bestUrl.get()

    init {
        scope.launch { runHealthChecks() }
    }

    private suspend fun runHealthChecks() {
        while (true) {
            pickBestUrl()
            delay(HEALTH_CHECK_INTERVAL_MS)
        }
    }

    /** Pings all candidates concurrently and sets the fastest responding URL. */
    fun pickBestUrl() {
        val candidates = ApiConfig.LOAD_BALANCED_URLS
        var bestLatency = Long.MAX_VALUE
        var bestCandidate = _bestUrl.get()

        candidates.forEach { baseUrl ->
            val latency = ping(baseUrl)
            Log.d(TAG, "Ping $baseUrl -> ${if (latency == Long.MAX_VALUE) "UNREACHABLE" else "${latency}ms"}")
            if (latency < bestLatency) {
                bestLatency = latency
                bestCandidate = baseUrl
            }
        }

        _bestUrl.set(bestCandidate)
        Log.i(TAG, "Selected backend: $bestCandidate (${bestLatency}ms)")
    }

    /** Returns latency in ms, or Long.MAX_VALUE if unreachable. */
    private fun ping(baseUrl: String): Long {
        return try {
            val url = "${baseUrl.trimEnd('/')}/$HEALTH_PATH"
            val request = Request.Builder().url(url).get().build()
            val start = System.currentTimeMillis()
            pingClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) System.currentTimeMillis() - start
                else Long.MAX_VALUE
            }
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }
}
