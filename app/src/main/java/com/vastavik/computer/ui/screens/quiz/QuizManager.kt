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

    suspend fun generateQuiz(topic: String, count: Int, difficulty: String): String {
        val prompt = """Generate exactly $count multiple choice questions (MCQs) on the topic "$topic" for Class 9-12 CBSE/ICSE Computer Science students. Difficulty: $difficulty.

Return ONLY a JSON array (no markdown, no explanation). Each object must have:
- "q": the question text
- "o": array of exactly 4 options
- "a": index of correct answer (0-3)

Example format:
[{"q":"What is...?","o":["A","B","C","D"],"a":1}]"""

        return try {
            val content = com.vastavik.computer.utils.VastavikAi.chat(
                systemPrompt = "You are Vastavik AI. Generate Class 9-12 CBSE/ICSE Computer Science MCQs. Always return strict JSON only.",
                userPrompt = prompt,
                temperature = 0.2,
                maxOutputTokens = 4096
            )
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
        } catch (e: Exception) {
            com.vastavik.computer.utils.VastavikAi.ERROR_MESSAGE
        }
    }
}
