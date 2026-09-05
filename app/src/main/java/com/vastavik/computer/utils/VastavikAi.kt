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
 * Supported AI models in Vastavik AI:
 * - MISTRAL_GOD: "Mistral is GOD" (Primary default engine across the entire app)
 * - GEMINI_37_DEMI_GOD: "Gemini 3.7 flash is Demi-god" (Thinking disabled)
 * - GEMINI_36_HUMAN: "Gemini 3.6 flash is Human AI" (Thinking disabled, with 3.7 fallback)
 */
enum class AiEngineModel(
    val id: String,
    val displayName: String,
    val badge: String,
    val subtitle: String,
    val mistralModel: String
) {
    MISTRAL_GOD(
        id = "mistral-god",
        displayName = "GOD",
        badge = "GOD",
        subtitle = "Best and accurate performance",
        mistralModel = "mistral-large-latest"
    ),
    GEMINI_37_DEMI_GOD(
        id = "mistral-demi-god",
        displayName = "Demi-God",
        badge = "Demi-God",
        subtitle = "High speed & reasoning",
        mistralModel = "mistral-medium-latest"
    ),
    GEMINI_36_HUMAN(
        id = "mistral-human",
        displayName = "Human AI",
        badge = "Human AI",
        subtitle = "Compact everyday AI",
        mistralModel = "mistral-small-latest"
    );

    companion object {
        val DEFAULT = MISTRAL_GOD
    }
}

/**
 * Unified "Vastavik AI" client.
 *
 * In AI Chat:
 * - GOD: Mistral Large (`mistral-large-latest`)
 * - Demi-God: Mistral Medium (`mistral-medium-latest`)
 * - Human AI: Mistral Small (`mistral-small-latest`)
 *
 * All other app features (Practice, Coding, MCQ generator, Predict output, PYQs)
 * run using Mistral Small (`mistral-small-latest`).
 */
object VastavikAi {

    const val MODEL = "mistral-small-latest"
    const val GEMINI_PRIMARY = "gemini-3.6-flash"
    const val GEMINI_FALLBACK = "gemini-3.7-flash"

    private const val MISTRAL_ENDPOINT = "https://api.mistral.ai/v1/chat/completions"
    private const val GEMINI_BASE_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/"

    val ENDPOINT: String get() = MISTRAL_ENDPOINT

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
     * Standard chat entry point used by all pages across the app.
     * Always uses Mistral Small by default.
     */
    @Throws(VastavikAiException::class)
    suspend fun chat(
        systemPrompt: String,
        userPrompt: String,
        temperature: Double = 0.3,
        maxOutputTokens: Int = 1024
    ): String = chatWithModel(
        engineModel = AiEngineModel.GEMINI_36_HUMAN, // Mistral Small for all app features
        systemPrompt = systemPrompt,
        userPrompt = userPrompt,
        temperature = temperature,
        maxOutputTokens = maxOutputTokens
    )

    @Throws(VastavikAiException::class)
    suspend fun chat(
        userPrompt: String
    ): String = chat(
        systemPrompt = "You are an expert computer science teacher and AI assistant for Indian students.",
        userPrompt = userPrompt
    )

    /**
     * Dispatches chat request to the specific selected engine model:
     * - GOD: Mistral Large ("mistral-large-latest")
     * - Demi-God: Mistral Medium ("mistral-medium-latest")
     * - Human AI: Mistral Small ("mistral-small-latest")
     */
    @Throws(VastavikAiException::class)
    suspend fun chatWithModel(
        engineModel: AiEngineModel,
        systemPrompt: String,
        userPrompt: String,
        temperature: Double = 0.3,
        maxOutputTokens: Int = 1024
    ): String = withContext(Dispatchers.IO) {
        val composedSystem = if (systemPrompt.isBlank()) IDENTITY_GUARD else IDENTITY_GUARD + "\n" + systemPrompt

        when (engineModel) {
            AiEngineModel.MISTRAL_GOD -> {
                callMistralModelWithFallback("mistral-large-latest", composedSystem, userPrompt, temperature, maxOutputTokens, "GOD")
            }
            AiEngineModel.GEMINI_37_DEMI_GOD -> {
                callMistralModelWithFallback("mistral-medium-latest", composedSystem, userPrompt, temperature, maxOutputTokens, "Demi-God")
            }
            AiEngineModel.GEMINI_36_HUMAN -> {
                callMistralModelWithFallback("mistral-small-latest", composedSystem, userPrompt, temperature, maxOutputTokens, "Human AI")
            }
        }
    }

    /**
     * Calls Mistral API with tiered fallback.
     */
    private fun callMistralModelWithFallback(
        targetModel: String,
        composedSystem: String,
        userPrompt: String,
        temperature: Double,
        maxOutputTokens: Int,
        tag: String
    ): String {
        val apiKey = try {
            BuildConfig.MISTRAL_API_KEY
        } catch (_: Throwable) {
            ""
        }

        if (apiKey.isBlank()) {
            DebugLogBox.warn("Mistral", "Mistral API key not configured. Failing over to Gemini", model = tag)
            return callGeminiWithFallback(GEMINI_PRIMARY, composedSystem, userPrompt, temperature, maxOutputTokens)
        }

        DebugLogBox.activeModel = tag

        // 1. Primary target model attempt
        try {
            return sendMistralRequest(targetModel, composedSystem, userPrompt, apiKey, temperature, maxOutputTokens)
        } catch (e: Exception) {
            DebugLogBox.warn("Mistral", "$targetModel returned ${e.message?.take(60)}. Trying fallback", model = tag)
            // 2. Fallbacks depending on requested tier
            if (targetModel != "mistral-small-latest") {
                try {
                    return sendMistralRequest("mistral-small-latest", composedSystem, userPrompt, apiKey, temperature, maxOutputTokens)
                } catch (e2: Exception) {
                    DebugLogBox.warn("Mistral", "mistral-small-latest failed: ${e2.message?.take(60)}", model = tag)
                }
            }
            try {
                return sendMistralRequest("ministral-8b-latest", composedSystem, userPrompt, apiKey, temperature, maxOutputTokens)
            } catch (fallback2: Exception) {
                try {
                    return sendMistralRequest("open-mistral-7b", composedSystem, userPrompt, apiKey, temperature, maxOutputTokens)
                } catch (fallback3: Exception) {
                    DebugLogBox.error("Mistral", "All Mistral models failed. Failing over to Gemini 3.6", fallback3, model = tag)
                }
            }
        }

        // 3. Failover to Gemini if Mistral completely fails
        return callGeminiWithFallback(GEMINI_PRIMARY, composedSystem, userPrompt, temperature, maxOutputTokens)
    }

    private fun sendMistralRequest(
        modelName: String,
        composedSystem: String,
        userPrompt: String,
        apiKey: String,
        temperature: Double,
        maxOutputTokens: Int
    ): String {
        val started = System.currentTimeMillis()
        val tag = "Mistral/$modelName"
        val conn = (URL(MISTRAL_ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $apiKey")
            doOutput = true
            connectTimeout = 20000
            readTimeout = 45000
        }

        try {
            val body = JSONObject().apply {
                put("model", modelName)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", composedSystem)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userPrompt)
                    })
                })
                put("temperature", temperature)
                put("max_tokens", maxOutputTokens)
            }
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val elapsed = System.currentTimeMillis() - started

            if (code !in 200..299) {
                val snippet = response.take(180).replace('\n', ' ')
                DebugLogBox.error(tag, "HTTP $code in ${elapsed}ms: $snippet", model = "Mistral is GOD")
                throw VastavikAiException("HTTP $code ($modelName): $snippet")
            }

            val data = JSONObject(response)
            val choices = data.optJSONArray("choices")
            if (choices == null || choices.length() == 0) {
                val msg = "No choices returned from Mistral"
                DebugLogBox.error(tag, "$msg in ${elapsed}ms", model = "Mistral is GOD")
                throw VastavikAiException(msg)
            }

            val choice = choices.getJSONObject(0)
            val messageObj = choice.optJSONObject("message")
            val content = messageObj?.optString("content").orEmpty()

            if (content.isBlank()) {
                DebugLogBox.error(tag, "Empty response in ${elapsed}ms", model = "Mistral is GOD")
                throw VastavikAiException("Empty response from Mistral")
            }

            DebugLogBox.info(tag, "OK in ${elapsed}ms (${content.length} chars)", model = "Mistral is GOD")
            return content
        } catch (e: VastavikAiException) {
            throw e
        } catch (e: Exception) {
            val msg = e.message ?: e.javaClass.simpleName
            DebugLogBox.error(tag, "Exception after ${System.currentTimeMillis() - started}ms: $msg", e, model = "Mistral is GOD")
            throw VastavikAiException(msg, e)
        } finally {
            conn.disconnect()
        }
    }

    private fun callGeminiWithFallback(
        primaryModel: String,
        composedSystem: String,
        userPrompt: String,
        temperature: Double,
        maxOutputTokens: Int
    ): String {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Throwable) { "" }
        val tag = "Gemini/$primaryModel"
        if (apiKey.isBlank()) {
            val msg = "Missing GEMINI_API_KEY (set in local.properties)"
            DebugLogBox.error(tag, msg, model = primaryModel)
            throw VastavikAiException(msg)
        }

        try {
            DebugLogBox.activeModel = if (primaryModel.contains("3.6")) "Gemini 3.6 flash is Human AI" else "Gemini 3.7 flash is Demi-god"
            return sendGeminiRequest(primaryModel, composedSystem, userPrompt, apiKey, temperature, maxOutputTokens)
        } catch (e: Exception) {
            val isRecoverable = e is VastavikAiException && (e.message?.contains("503") == true || e.message?.contains("timeout") == true)
                    || e is SocketTimeoutException

            if (isRecoverable && primaryModel != GEMINI_FALLBACK) {
                DebugLogBox.warn(tag, "$primaryModel failed (${e.message?.take(80)}). Falling back to $GEMINI_FALLBACK", model = primaryModel)
                DebugLogBox.activeModel = "Gemini 3.7 flash is Demi-god"
                try {
                    return sendGeminiRequest(GEMINI_FALLBACK, composedSystem, userPrompt, apiKey, temperature, maxOutputTokens)
                } catch (fallbackEx: Exception) {
                    DebugLogBox.error("Gemini/$GEMINI_FALLBACK", "Fallback also failed: ${fallbackEx.message?.take(120)}", fallbackEx, model = GEMINI_FALLBACK)
                    throw fallbackEx
                }
            } else {
                throw e
            }
        }
    }

    private fun callGemini(
        targetModel: String,
        composedSystem: String,
        userPrompt: String,
        temperature: Double,
        maxOutputTokens: Int
    ): String {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Throwable) { "" }
        val tag = "Gemini/$targetModel"
        if (apiKey.isBlank()) {
            val msg = "Missing GEMINI_API_KEY (set in local.properties)"
            DebugLogBox.error(tag, msg, model = targetModel)
            throw VastavikAiException(msg)
        }
        DebugLogBox.activeModel = if (targetModel.contains("3.7")) "Gemini 3.7 flash is Demi-god" else "Gemini 3.6 flash is Human AI"
        return sendGeminiRequest(targetModel, composedSystem, userPrompt, apiKey, temperature, maxOutputTokens)
    }

    private fun sendGeminiRequest(
        targetModel: String,
        composedSystem: String,
        userPrompt: String,
        apiKey: String,
        temperature: Double,
        maxOutputTokens: Int
    ): String {
        val started = System.currentTimeMillis()
        val tag = "Gemini/$targetModel"
        val endpointUrl = "$GEMINI_BASE_ENDPOINT$targetModel:generateContent?key=$apiKey"
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
                    // Disable thinking tokens completely for both models
                    put("thinkingConfig", JSONObject().apply {
                        if (targetModel.contains("3.6")) {
                            put("thinkingLevel", "minimal")
                        } else {
                            put("thinkingBudget", 0)
                        }
                    })
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
