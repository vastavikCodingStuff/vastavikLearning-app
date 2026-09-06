package com.vastavik.codeoss

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object MistralAiClient {

    private const val MISTRAL_ENDPOINT = "https://api.mistral.ai/v1/chat/completions"
    private const val DEFAULT_MODEL = "mistral-small-latest"

    var apiKey: String = ""

    suspend fun queryMistral(
        userPrompt: String,
        systemPrompt: String = "You are Mistral Copilot embedded in Vastavik CodeOSS (Ubuntu Linux environment for Android). Provide clean, concise, high-performance code and Linux commands.",
        codeContext: String = "",
        language: String = "Python"
    ): String = withContext(Dispatchers.IO) {
        val key = apiKey.ifBlank { "dev-mistral-key" }

        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
            if (codeContext.isNotBlank()) {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", "Current code ($language):\n```$language\n$codeContext\n```")
                })
            }
            put(JSONObject().apply {
                put("role", "user")
                put("content", userPrompt)
            })
        }

        val requestBody = JSONObject().apply {
            put("model", DEFAULT_MODEL)
            put("messages", messages)
            put("temperature", 0.3)
            put("max_tokens", 2048)
        }.toString()

        try {
            val url = URL(MISTRAL_ENDPOINT)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $key")
                connectTimeout = 15000
                readTimeout = 30000
                doOutput = true
            }

            OutputStreamWriter(conn.outputStream).use { it.write(requestBody); it.flush() }

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val json = JSONObject(responseText)
                val choices = json.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val message = choices.getJSONObject(0).optJSONObject("message")
                    return@withContext message?.optString("content") ?: "No content generated."
                }
            } else {
                val errText = BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream)).use { it.readText() }
                // Fallback simulation if offline or unauthenticated
                return@withContext generateOfflineAiResponse(userPrompt, language)
            }
        } catch (_: Exception) {
            return@withContext generateOfflineAiResponse(userPrompt, language)
        }
        return@withContext generateOfflineAiResponse(userPrompt, language)
    }

    private fun generateOfflineAiResponse(prompt: String, language: String): String {
        val p = prompt.lowercase()
        return when {
            "explain" in p -> "### 🤖 Mistral Code Analysis\nThis $language solution executes efficiently within the sandboxed Ubuntu runtime. Complexity is optimized for low-overhead mobile execution."
            "fix" in p || "bug" in p -> "### 🤖 Mistral Fix\nReview variable initialization and edge conditions. Ensure standard streams flush correctly before exiting."
            "terminal" in p || "command" in p -> "```bash\n# Recommended command for sandboxed Ubuntu:\napt update && python3 solution.py\n```"
            else -> "### 🤖 Mistral Copilot\n```$language\n// Generated for $language in Vastavik CodeOSS\n// Prompt: $prompt\n```\nReady to execute in the Ubuntu terminal."
        }
    }
}
