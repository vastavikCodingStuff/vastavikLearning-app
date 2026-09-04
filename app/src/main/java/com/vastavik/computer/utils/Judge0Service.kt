package com.vastavik.computer.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.vastavik.computer.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Code execution service backed by self-hosted Judge0.
 * Endpoint: http://139.84.172.230:2358/submissions?base64_encoded=false&wait=true (wait=true = synchronous)
 */
object Judge0Service {

    const val JUDGE0_URL = "http://139.84.172.230:2358/submissions?base64_encoded=false&wait=true"

    fun languageIdFor(language: String): Int? = when (language.trim().lowercase()) {
        "java" -> 62
        "python", "py" -> 71
        "c++", "cpp" -> 54
        "javascript", "js" -> 63
        else -> null
    }

    data class ExecutionResult(
        val success: Boolean,
        val output: String,
        val executionTime: String? = null,
        val memoryKb: Int? = null,
        val statusDescription: String = ""
    )

    suspend fun runCode(languageId: Int, sourceCode: String, stdin: String = ""): ExecutionResult =
        withContext(Dispatchers.IO) {
            val conn = URL(JUDGE0_URL).openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Accept", "application/json")
                if (BuildConfig.JUDGE0_AUTH_TOKEN.isNotBlank()) conn.setRequestProperty("X-Auth-Token", BuildConfig.JUDGE0_AUTH_TOKEN)
                conn.doOutput = true
                conn.connectTimeout = 15000
                conn.readTimeout = 60000
                val body = JSONObject().apply {
                    put("language_id", languageId)
                    put("source_code", sourceCode)
                    put("stdin", stdin)
                }
                conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (code !in 200..299) {
                    return@withContext ExecutionResult(false, "Execution request failed with HTTP $code${if (response.isNotBlank()) ": $response" else ""}", statusDescription = "HTTP $code")
                }
                val data = JSONObject(response)
                val statusObj = data.optJSONObject("status")
                val statusId = statusObj?.optInt("id", -1) ?: -1
                val statusDesc = statusObj?.optString("description", "").orEmpty()
                val time = if (data.isNull("time")) null else data.optString("time", "").takeIf { it.isNotBlank() && it != "null" }
                val memory = if (data.isNull("memory")) null else data.optInt("memory").takeIf { it > 0 }
                if (statusId == 3) {
                    return@withContext ExecutionResult(true, data.optString("stdout", ""), time, memory, statusDesc.ifBlank { "Accepted" })
                }
                val err = when {
                    data.optString("compile_output", "").isNotBlank() -> data.optString("compile_output")
                    data.optString("stderr", "").isNotBlank() -> data.optString("stderr")
                    data.optString("message", "").isNotBlank() -> data.optString("message")
                    else -> statusDesc.ifBlank { "Execution Failed" }
                }
                ExecutionResult(false, err, time, memory, statusDesc.ifBlank { "Execution Failed" })
            } catch (e: Exception) {
                ExecutionResult(false, "Error: ${e.message ?: "Unknown network error"}", statusDescription = "Network Error")
            } finally {
                conn.disconnect()
            }
        }
}
