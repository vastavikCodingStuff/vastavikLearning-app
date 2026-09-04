package com.vastavik.computer.utils

import com.vastavik.computer.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

/**
 * Unified "Vastavik AI" client backed by Google Gemini 3.6 Flash
 * (with automatic fallback to Gemini 3.7 Flash if 3.6 encounters high demand).
 *
 * Replaces the legacy Mistral Small client. All chat, code explanation,
 * question generation, and brief generation flows route through here.
 *
 * Identity & safety guard [IDENTITY_GUARD] is prepended to every system
 * prompt so the assistant always self-identifies as "Vastavik AI" (built
 * on GLM 5.3 and hosted on Vastavik's VPS) and refuses off-topic or
 * harmful content.
 */
object VastavikAi {

    const val MODEL = "gemini-3.6-flash"
    const val FALLBACK_MODEL = "gemini-3.7-flash"
    private const val BASE_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/"

    val ENDPOINT: String get() = BASE_ENDPOINT + MODEL + ":generateContent"

    /** User-facing error text shown in the UI for any failure (per spec). */
    const val ERROR_MESSAGE = "Error Connecting to the Server"

    /**
     * Always-on identity + safety preamble applied to every system prompt.
     * Enforces Vastavik AI identity (GLM 5.3 on Vastavik VPS) and content policy.
     */
    const val IDENTITY_GUARD: String =
        "You are Vastavik AI, a study and coding assistant built on GLM 5.3 and hosted on Vastavik's own VPS server.\n" +
        "1. If asked which model / company / API powers you, answer exactly: \"I'm Vastavik AI, built on GLM 5.3 and hosted on Vastavik's VPS server.\" Never mention Gemini, Google, or any other underlying provider.\n" +
        "2. Always introduce yourself as \"Vastavik AI\" when the user asks who you are.\n" +
        "3. CONTENT POLICY: Strictly refuse to engage with pornographic, sexual, explicit, violent, hateful, self-harm, illegal, or otherwise harmful / off-domain / \"rubbish\" requests. Reply with a single short sentence such as: \"I can't help with that. I'm Vastavik AI and I'm here to help you learn and code.\" Do not elaborate, do not roleplay, and do not output the offending content.\n" +
        "4. Stay on-topic: this app is for Indian school students (Class 5-12, ICSE/CBSE). Help with study material, computer science, coding, debugging, math, and science. Politely redirect anything else.\n" +
        "5. Be encouraging, concise, and student-friendly. Format code with fenced code blocks.\n"

    /**
     * Send a single-turn (system + user) prompt to Gemini 3.6 Flash.
     * If 3.6 Flash encounters high demand (HTTP 503) or timeout,
     * automatically attempts Gemini 3.7 Flash fallback.
     *
     * The [IDENTITY_GUARD] is automatically prepended to the caller's
     * [systemPrompt]. Throws [VastavikAiException] on any failure.
     */
    @Throws(VastavikAiException::class)
    suspend fun chat(
        systemPrompt: String,
        userPrompt: String,
        temperature: Double = 0.3,
        maxOutputTokens: Int = 1024
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val tag = "Gemini/$MODEL"
        if (apiKey.isBlank()) {
            val msg = "Missing GEMINI_API_KEY (set in local.properties)"
            DebugLogBox.error(tag, msg, model = MODEL)
            throw VastavikAiException(msg)
        }

        val composedSystem = if (systemPrompt.isBlank()) IDENTITY_GUARD else IDENTITY_GUARD + "\n" + systemPrompt

        try {
            // Primary attempt: Gemini 3.6 Flash
            DebugLogBox.activeModel = MODEL
            sendRequest(MODEL, composedSystem, userPrompt, apiKey, temperature, maxOutputTokens)
        } catch (e: Exception) {
            val isRecoverable = e is VastavikAiException && (e.message?.contains("503") == true || e.message?.contains("timeout") == true)
                    || e is SocketTimeoutException

            if (isRecoverable) {
                DebugLogBox.warn(tag, "$MODEL failed (${e.message?.take(80)}). Falling back to $FALLBACK_MODEL", model = MODEL)
                DebugLogBox.activeModel = FALLBACK_MODEL
                try {
                    sendRequest(FALLBACK_MODEL, composedSystem, userPrompt, apiKey, temperature, maxOutputTokens)
                } catch (fallbackEx: Exception) {
                    DebugLogBox.error("Gemini/$FALLBACK_MODEL", "Fallback also failed: ${fallbackEx.message?.take(120)}", fallbackEx, model = FALLBACK_MODEL)
                    throw fallbackEx
                }
            } else {
                throw e
            }
        }
    }

    private fun sendRequest(
        targetModel: String,
        composedSystem: String,
        userPrompt: String,
        apiKey: String,
        temperature: Double,
        maxOutputTokens: Int
    ): String {
        val started = System.currentTimeMillis()
        val tag = "Gemini/$targetModel"
        val endpointUrl = "$BASE_ENDPOINT$targetModel:generateContent?key=$apiKey"
        val conn = (URL(endpointUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            doOutput = true
            connectTimeout = 20000
            readTimeout = 45000
        }
        try {
            val body = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", composedSystem + "\n\n" + userPrompt))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", temperature)
                    put("maxOutputTokens", maxOutputTokens)
                })
            }
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val elapsed = System.currentTimeMillis() - started

            if (code !in 200..299) {
                val snippet = response.take(180).replace('\n', ' ')
                DebugLogBox.error(tag, "HTTP $code in ${elapsed}ms: $snippet", model = targetModel)
                throw VastavikAiException("HTTP $code ($targetModel): $snippet")
            }

            val data = JSONObject(response)
            val candidates = data.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                val feedback = data.optJSONObject("promptFeedback")
                val reason = feedback?.optString("blockReason").orEmpty().ifBlank { feedback?.toString().orEmpty() }
                val msg = "No candidates returned (feedback=$reason)"
                DebugLogBox.error(tag, "$msg in ${elapsed}ms", model = targetModel)
                throw VastavikAiException(msg)
            }
            val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
                ?: throw VastavikAiException("Malformed response (no content parts)").also {
                    DebugLogBox.error(tag, "Malformed response in ${elapsed}ms", model = targetModel)
                }
            val sb = StringBuilder()
            for (i in 0 until parts.length()) {
                val t = parts.optJSONObject(i)?.optString("text").orEmpty()
                if (t.isNotEmpty()) { if (sb.isNotEmpty()) sb.append('\n'); sb.append(t) }
            }
            val out = sb.toString()
            if (out.isBlank()) {
                DebugLogBox.error(tag, "Empty response in ${elapsed}ms", model = targetModel)
                throw VastavikAiException("Empty response")
            }
            DebugLogBox.info(tag, "OK in ${elapsed}ms (${out.length} chars)", model = targetModel)
            return out
        } catch (e: VastavikAiException) {
            throw e
        } catch (e: Exception) {
            val msg = e.message ?: e.javaClass.simpleName
            DebugLogBox.error(tag, "Exception after ${System.currentTimeMillis() - started}ms: $msg", e, model = targetModel)
            throw VastavikAiException(msg, e)
        } finally {
            conn.disconnect()
        }
    }

    class VastavikAiException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
}
