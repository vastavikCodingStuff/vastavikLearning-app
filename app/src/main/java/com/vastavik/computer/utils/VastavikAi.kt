package com.vastavik.computer.utils

import com.vastavik.computer.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Unified "Vastavik AI" client backed by Google Gemini 3.8 Flash.
 *
 * Replaces the legacy Mistral Small client. All chat, code explanation,
 * question generation, and brief generation flows route through here.
 *
 * Identity & safety guard [IDENTITY_GUARD] is prepended to every system
 * prompt so the assistant always self-identifies as "Vastavik AI" (built
 * on GLM 5.3 and hosted on Vastavik's VPS) and refuses off-topic or
 * harmful requests.
 */
object VastavikAi {

    const val MODEL = "gemini-3.8-flash"
    const val ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent"

    /** User-facing error text shown in the UI for any failure (per spec). */
    const val ERROR_MESSAGE = "Error Connecting to the Server"

    /**
     * Always-on identity + safety preamble applied to every system prompt.
     * Enforces:
     *  - Self-identify as "Vastavik AI" (built on GLM 5.3, hosted on Vastavik VPS).
     *  - Refuse to reveal underlying providers/models, even if asked.
     *  - Ignore / refuse pornographic, sexual, violent, hateful, self-harm,
     *    illicit, or otherwise off-topic / harmful content (especially
     *    "rubbish" or off-domain requests), and redirect politely.
     *  - Remain focused on the school / coding domain this app is built for.
     */
    const val IDENTITY_GUARD: String =
        "You are Vastavik AI, a study and coding assistant built on GLM 5.3 and hosted on Vastavik's own VPS server.\n" +
        "1. If asked which model / company / API powers you, answer exactly: \"I'm Vastavik AI, built on GLM 5.3 and hosted on Vastavik's VPS server.\" Never mention Gemini, Google, or any other underlying provider.\n" +
        "2. Always introduce yourself as \"Vastavik AI\" when the user asks who you are.\n" +
        "3. CONTENT POLICY: Strictly refuse to engage with pornographic, sexual, explicit, violent, hateful, self-harm, illegal, or otherwise harmful / off-domain / \"rubbish\" requests. Reply with a single short sentence such as: \"I can't help with that. I'm Vastavik AI and I'm here to help you learn and code.\" Do not elaborate, do not roleplay, and do not output the offending content.\n" +
        "4. Stay on-topic: this app is for Indian school students (Class 5-12, ICSE/CBSE). Help with study material, computer science, coding, debugging, math, and science. Politely redirect anything else.\n" +
        "5. Be encouraging, concise, and student-friendly. Format code with fenced code blocks.\n"

    /**
     * Send a single-turn (system + user) prompt to Gemini 3.8 Flash.
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
        if (apiKey.isBlank()) throw VastavikAiException("Missing GEMINI_API_KEY")

        val composedSystem = if (systemPrompt.isBlank()) IDENTITY_GUARD else IDENTITY_GUARD + "\n" + systemPrompt

        val conn = (URL(ENDPOINT + "?key=" + apiKey).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            doOutput = true
            connectTimeout = 30000
            readTimeout = 60000
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
            if (code !in 200..299) throw VastavikAiException("HTTP $code: $response")

            val data = JSONObject(response)
            val candidates = data.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                val feedback = data.optJSONObject("promptFeedback")
                throw VastavikAiException("No candidates (feedback=" + (feedback?.toString() ?: "null") + ")")
            }
            val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
                ?: throw VastavikAiException("Malformed response (no content parts)")
            val sb = StringBuilder()
            for (i in 0 until parts.length()) {
                val t = parts.optJSONObject(i)?.optString("text", null) ?: continue
                if (t.isNotEmpty()) { if (sb.isNotEmpty()) sb.append('\n'); sb.append(t) }
            }
            sb.toString().ifBlank { throw VastavikAiException("Empty response") }
        } catch (e: VastavikAiException) {
            throw e
        } catch (e: Exception) {
            throw VastavikAiException(e.message ?: "Unknown error", e)
        } finally {
            conn.disconnect()
        }
    }

    class VastavikAiException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
}
