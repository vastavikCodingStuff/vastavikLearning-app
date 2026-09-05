package com.vastavik.computer.data.api

import org.json.JSONObject
import retrofit2.HttpException

/**
 * Handles graceful circuit breaking and route maintenance handling.
 * If the backend returns HTTP 503 Service Unavailable, extracts the
 * user-friendly maintenance message.
 */
object CircuitBreaker {

    suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.success(apiCall())
        } catch (e: HttpException) {
            if (e.code() == 503) {
                val errorJson = try {
                    e.response()?.errorBody()?.string()
                } catch (_: Exception) {
                    null
                }
                val message = try {
                    JSONObject(errorJson ?: "").optString(
                        "message",
                        "This feature is undergoing scheduled maintenance."
                    )
                } catch (_: Exception) {
                    "This feature is undergoing scheduled maintenance. Other services remain fully operational."
                }
                Result.failure(RouteMaintenanceException(message))
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class RouteMaintenanceException(message: String) : Exception(message)
