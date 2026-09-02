package com.vastavik.computer.data.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import kotlinx.serialization.Serializable

@Serializable
data class QuizQuestion(
    val question: String = "",
    val options: List<String> = emptyList(),
    val answerIndex: Int = 0,
    val explanation: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "question" to question,
        "options" to options,
        "answerIndex" to answerIndex,
        "explanation" to explanation
    )
}

@Serializable
data class QuizModel(
    val id: String = "",
    val title: String = "",
    val topic: String = "",
    val questions: List<QuizQuestion> = emptyList(),
    val difficulty: String = "Medium",
    val timeLimitMinutes: Int = 30,
    val createdAt: String = "",
    val type: String = "mcq",
    val subject: String = "General"
) {
    fun toMap(): Map<String, Any> = mapOf(
        "title" to title,
        "topic" to topic,
        "questions" to questions.map { it.toMap() },
        "difficulty" to difficulty,
        "timeLimitMinutes" to timeLimitMinutes,
        "createdAt" to if (createdAt.isEmpty()) FieldValue.serverTimestamp() else createdAt,
        "type" to type,
        "subject" to subject
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): QuizModel {
            val data = doc.data ?: return QuizModel(id = doc.id)
            val questionsData = (data["questions"] as? List<Map<String, Any>>) ?: emptyList()
            return QuizModel(
                id = doc.id,
                title = data["title"] as? String ?: "",
                topic = data["topic"] as? String ?: "",
                questions = questionsData.map { q ->
                    QuizQuestion(
                        question = q["question"] as? String ?: "",
                        options = (q["options"] as? List<String>) ?: emptyList(),
                        answerIndex = (q["answerIndex"] as? Number)?.toInt() ?: 0,
                        explanation = q["explanation"] as? String ?: ""
                    )
                },
                difficulty = data["difficulty"] as? String ?: "Medium",
                timeLimitMinutes = (data["timeLimitMinutes"] as? Number)?.toInt() ?: 30,
                createdAt = data["createdAt"]?.toString() ?: "",
                type = data["type"] as? String ?: "mcq",
                subject = data["subject"] as? String ?: "General"
            )
        }
    }
}

@Serializable
data class CodingChallenge(
    val id: String = "",
    val title: String = "",
    val topic: String = "",
    val description: String = "",
    val difficulty: String = "Easy",
    val starterCode: Map<String, String> = emptyMap(),
    val testCases: List<TestCase> = emptyList(),
    val createdAt: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "title" to title,
        "topic" to topic,
        "description" to description,
        "difficulty" to difficulty,
        "starterCode" to starterCode,
        "testCases" to testCases.map { it.toMap() },
        "createdAt" to if (createdAt.isEmpty()) FieldValue.serverTimestamp() else createdAt
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): CodingChallenge {
            val data = doc.data ?: return CodingChallenge(id = doc.id)
            val testCasesData = (data["testCases"] as? List<Map<String, Any>>) ?: emptyList()
            val starterCodeData = (data["starterCode"] as? Map<String, String>) ?: emptyMap()
            return CodingChallenge(
                id = doc.id,
                title = data["title"] as? String ?: "",
                topic = data["topic"] as? String ?: "",
                description = data["description"] as? String ?: "",
                difficulty = data["difficulty"] as? String ?: "Easy",
                starterCode = starterCodeData,
                testCases = testCasesData.map { tc ->
                    TestCase(
                        input = tc["input"] as? String ?: "",
                        expectedOutput = tc["expectedOutput"] as? String ?: ""
                    )
                },
                createdAt = data["createdAt"]?.toString() ?: ""
            )
        }
    }
}

@Serializable
data class TestCase(
    val input: String = "",
    val expectedOutput: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "input" to input,
        "expectedOutput" to expectedOutput
    )
}

@Serializable
data class PYQModel(
    val id: String = "",
    val year: Int = 2023,
    val board: String = "ICSE",
    val subject: String = "",
    val questionText: String = "",
    val solutionText: String = "",
    val imageUrl: String = "",
    val createdAt: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "year" to year,
        "board" to board,
        "subject" to subject,
        "questionText" to questionText,
        "solutionText" to solutionText,
        "imageUrl" to imageUrl,
        "createdAt" to if (createdAt.isEmpty()) FieldValue.serverTimestamp() else createdAt
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): PYQModel {
            val data = doc.data ?: return PYQModel(id = doc.id)
            return PYQModel(
                id = doc.id,
                year = (data["year"] as? Number)?.toInt() ?: 2023,
                board = data["board"] as? String ?: "ICSE",
                subject = data["subject"] as? String ?: "",
                questionText = data["questionText"] as? String ?: "",
                solutionText = data["solutionText"] as? String ?: "",
                imageUrl = data["imageUrl"] as? String ?: "",
                createdAt = data["createdAt"]?.toString() ?: ""
            )
        }
    }
}
