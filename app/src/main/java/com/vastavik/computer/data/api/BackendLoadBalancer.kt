package com.vastavik.computer.data.api

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-app load balancer that pings live backends,
 * routes requests to the fastest responsive endpoint,
 * and seamlessly handles cold-start detection and warm-up.
 */
@Singleton
class BackendLoadBalancer @Inject constructor() {

    companion object {
        private const val TAG = "BackendLoadBalancer"
        private const val HEALTH_CHECK_INTERVAL_MS = 30_000L
        private const val PING_TIMEOUT_SEC = 10L
        private const val HEALTH_PATH = "health"
    }

    // Dedicated lightweight client for health checks
    private val pingClient = OkHttpClient.Builder()
        .connectTimeout(PING_TIMEOUT_SEC, TimeUnit.SECONDS)
        .readTimeout(PING_TIMEOUT_SEC, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Holds the currently best base URL
    private val _bestUrl = AtomicReference(ApiConfig.LOAD_BALANCED_URLS.first())
    val bestBaseUrl: String get() = _bestUrl.get()

    init {
        BackendWarmupManager.warmUpAsync()
        scope.launch { runHealthChecks() }
    }

    private suspend fun runHealthChecks() {
        while (true) {
            pickBestUrlAsync()
            delay(HEALTH_CHECK_INTERVAL_MS)
        }
    }

    /** Pings all candidates concurrently and sets the fastest responding URL. */
    fun pickBestUrl() {
        scope.launch { pickBestUrlAsync() }
    }

    private suspend fun pickBestUrlAsync() {
        val candidates = ApiConfig.LOAD_BALANCED_URLS
        if (candidates.isEmpty()) return

        val deferredResults = candidates.map { baseUrl ->
            scope.async {
                val latency = ping(baseUrl)
                baseUrl to latency
            }
        }

        val results = deferredResults.awaitAll()
        var bestLatency = Long.MAX_VALUE
        var bestCandidate = _bestUrl.get()

        for ((baseUrl, latency) in results) {
            Log.d(TAG, "Ping $baseUrl -> ${if (latency == Long.MAX_VALUE) "COLD/UNREACHABLE" else "${latency}ms"}")
            if (latency < bestLatency) {
                bestLatency = latency
                bestCandidate = baseUrl
            }
        }

        _bestUrl.set(bestCandidate)
        if (bestLatency != Long.MAX_VALUE) {
            Log.i(TAG, "Selected active backend: $bestCandidate (${bestLatency}ms)")
        } else {
            Log.w(TAG, "All backends cold or unreachable. Triggering proactive warm-up...")
            BackendWarmupManager.warmUpAsync(force = true)
        }
    }

    /** Returns latency in ms, or Long.MAX_VALUE if unreachable. */
    private fun ping(baseUrl: String): Long {
        return try {
            val url = "${baseUrl.trimEnd('/')}/$HEALTH_PATH"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "VastavikLearning-HealthCheck")
                .get()
                .build()
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
