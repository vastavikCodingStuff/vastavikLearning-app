package com.vastavik.computer.data.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor that automatically handles Render cloud cold starts.
 *
 * When Render wakes up from an idle sleep (free tier spins down after 15 mins),
 * initial requests may encounter a connection timeout, socket timeout,
 * or gateway 502/503/504 responses while the container spins up.
 *
 * This interceptor intercepts those transient failures and retries the request
 * with progressive delays, ensuring the user's operation succeeds seamlessly
 * without presenting an error dialog or blank screen.
 */
@Singleton
class ColdStartRetryInterceptor @Inject constructor() : Interceptor {

    companion object {
        private const val TAG = "ColdStartRetry"
        private val TRANSIENT_STATUS_CODES = setOf(502, 503, 504)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val isOneShot = request.body?.isOneShot() == true
        val maxRetries = if (isOneShot) 0 else ApiConfig.COLD_START_MAX_RETRIES

        var attempt = 0
        var lastException: IOException? = null

        while (attempt <= maxRetries) {
            try {
                if (attempt > 0) {
                    val delayMs = ApiConfig.COLD_START_RETRY_DELAY_MS * attempt
                    Log.i(TAG, "Render cold-start retry #$attempt for ${request.url} after ${delayMs}ms...")
                    Thread.sleep(delayMs)
                }

                val response = chain.proceed(request)

                // If Render edge proxy returned 502/503/504 while booting container
                if (response.code in TRANSIENT_STATUS_CODES && attempt < maxRetries) {
                    Log.w(TAG, "Received transient status ${response.code} from ${request.url}. Container may be cold-starting.")
                    response.close()
                    attempt++
                    continue
                }

                return response
            } catch (e: SocketTimeoutException) {
                lastException = e
                Log.w(TAG, "Socket timeout on attempt #$attempt for ${request.url}: ${e.message}")
            } catch (e: ConnectException) {
                lastException = e
                Log.w(TAG, "Connection failed on attempt #$attempt for ${request.url}: ${e.message}")
            } catch (e: UnknownHostException) {
                lastException = e
                Log.w(TAG, "Unknown host on attempt #$attempt for ${request.url}: ${e.message}")
            } catch (e: IOException) {
                lastException = e
                Log.w(TAG, "IO exception on attempt #$attempt for ${request.url}: ${e.message}")
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                throw IOException("Cold start retry interrupted", e)
            }

            attempt++
        }

        throw lastException ?: IOException("Failed after $maxRetries cold-start retry attempts")
    }
}
