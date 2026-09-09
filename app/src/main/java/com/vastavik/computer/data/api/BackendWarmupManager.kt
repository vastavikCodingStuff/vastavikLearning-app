package com.vastavik.computer.data.api

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Proactive Backend Warm-up Manager.
 *
 * Render cloud free tier instances spin down after 15 minutes of inactivity.
 * When the user launches the app, this manager immediately and asynchronously
 * fires a lightweight GET /health request to wake up the Render container in the background.
 * By the time the user navigates past the splash/home screen, the backend is fully booted.
 */
object BackendWarmupManager {

    private const val TAG = "BackendWarmupManager"
    private const val WARMUP_TIMEOUT_SEC = 60L

    private val _isWarm = MutableStateFlow(false)
    val isWarm: StateFlow<Boolean> = _isWarm

    private val _isWarming = MutableStateFlow(false)
    val isWarming: StateFlow<Boolean> = _isWarming

    private val warmupInitiated = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Dedicated client with 60s timeout for cold-start tolerance
    private val warmupClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(WARMUP_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(WARMUP_TIMEOUT_SEC, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Non-blocking call to trigger backend pre-warming on app launch.
     */
    fun warmUpAsync(force: Boolean = false) {
        if (!force && warmupInitiated.getAndSet(true)) {
            Log.d(TAG, "Backend warm-up already initiated.")
            return
        }

        _isWarming.value = true
        scope.launch {
            val url = "${ApiConfig.URL_RENDER_CLOUD.trimEnd('/')}/health"
            Log.i(TAG, "Initiating proactive backend warm-up at $url...")
            val startTime = System.currentTimeMillis()

            try {
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .header("User-Agent", "VastavikLearning-Android-Prewarm")
                    .build()

                warmupClient.newCall(request).execute().use { response ->
                    val elapsed = System.currentTimeMillis() - startTime
                    if (response.isSuccessful) {
                        _isWarm.value = true
                        Log.i(TAG, "Backend is warm! Ping responded in ${elapsed}ms with status ${response.code}")
                    } else {
                        Log.w(TAG, "Warmup returned non-200 status: ${response.code} in ${elapsed}ms")
                    }
                }
            } catch (e: Exception) {
                val elapsed = System.currentTimeMillis() - startTime
                Log.w(TAG, "Warmup request encountered exception after ${elapsed}ms: ${e.message}")
            } finally {
                _isWarming.value = false
            }
        }
    }
}
