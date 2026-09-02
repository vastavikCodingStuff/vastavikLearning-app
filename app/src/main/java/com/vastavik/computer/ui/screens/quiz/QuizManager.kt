package com.vastavik.computer.ui.screens.quiz

import com.vastavik.computer.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class QuizQuestionData(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

object QuizManager {
    private var generatedQuestions: List<QuizQuestionData> = emptyList()

    fun getQuestions(): List<QuizQuestionData> = generatedQuestions

    fun generateQuiz(topic: String, count: Int, difficulty: String): String {
        val apiKey = BuildConfig.MISTRAL_API_KEY
        if (apiKey.isBlank()) return "MISTRAL_API_KEY not configured."

        return try {
            val url = URL("https://api.mistral.ai/v1/chat/completions")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            conn.doOutput = true
            conn.connectTimeout = 60000
            conn.readTimeout = 60000

            val prompt = """Generate exactly $count multiple choice questions (MCQs) on the topic "$topic" for Class 9-12 CBSE/ICSE Computer Science students. Difficulty: $difficulty.

Return ONLY a JSON array (no markdown, no explanation). Each object must have:
- "q": the question text
- "o": array of exactly 4 options
- "a": index of correct answer (0-3)

Example format:
[{"q":"What is...?","o":["A","B","C","D"],"a":1}]"""

            val body = JSONObject().apply {
                put("model", "mistral-small-latest")
                put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
                put("max_tokens", 4096)
                put("temperature", 0.2)
            }

            conn.outputStream.use { it.write(body.toString().toByteArray()) }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val response = stream.bufferedReader().use { it.readText() }

            if (responseCode in 200..299) {
                val json = JSONObject(response)
                val content = json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
                val cleaned = content.replace(Regex("```json\\s*"), "").replace(Regex("```\\s*"), "").trim()
                val arr = JSONArray(cleaned)
                generatedQuestions = (0 until arr.length()).map { i ->
                    val obj = arr.getJSONObject(i)
                    QuizQuestionData(
                        question = obj.getString("q"),
                        options = obj.getJSONArray("o").let { o -> (0 until o.length()).map { o.getString(it) } },
                        correctIndex = obj.getInt("a")
                    )
                }
                "ok"
            } else {
                "API Error ($responseCode)"
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
