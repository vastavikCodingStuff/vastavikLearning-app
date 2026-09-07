package com.vastavik.computer.data.api.model

import com.google.gson.annotations.SerializedName

// ==========================================
// Authentication Models
// ==========================================

data class SignupRequest(
    val email: String,
    val password: String,
    val name: String,
    val board: String = "ICSE",
    val language: String = "Java"
)

data class LoginRequest(
    val email: String,
    val password: String,
    @SerializedName("device_fingerprint") val deviceFingerprint: String? = null
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class OAuthGoogleRequest(
    @SerializedName("id_token") val idToken: String
)

data class OAuthGitHubRequest(
    val code: String
)

data class DeviceVerifyRequest(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("is_rooted") val isRooted: Boolean = false,
    @SerializedName("is_emulator") val isEmulator: Boolean = false
)

data class AuthResponse(
    val success: Boolean = false,
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    val name: String? = null,
    val email: String? = null,
    val role: String? = null,
    @SerializedName("error_message") val errorMessage: String? = null
)

data class UserProfileResponse(
    @SerializedName("user_id") val userId: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "student",
    @SerializedName("is_premium") val isPremium: Boolean = false,
    val board: String? = "ICSE",
    @SerializedName("student_class") val studentClass: String? = "Class 10",
    @SerializedName("preferred_language") val preferredLanguage: String? = "Java",
    val languages: List<String> = listOf("Java", "Python", "JavaScript", "SQL"),
    @SerializedName("streak_count") val streakCount: Int = 0,
    @SerializedName("lessons_completed") val lessonsCompleted: Int = 0,
    @SerializedName("completion_rate") val completionRate: Double = 0.0,
    @SerializedName("payment_details") val paymentDetails: List<Map<String, Any>> = emptyList()
)

// ==========================================
// Catalog & Curriculum Models
// ==========================================

data class HomeCatalogResponse(
    val courses: List<CourseItem> = emptyList(),
    val banners: List<BannerItem> = emptyList(),
    @SerializedName("popular_topics") val popularTopics: List<TopicItem> = emptyList()
)

data class CourseItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    @SerializedName("icon_name") val iconName: String = "code",
    val color: Long = 0xFF4F46E5L,
    val order: Int = 0
)

data class BannerItem(
    val id: String = "",
    val title: String = "",
    @SerializedName("image_url") val imageUrl: String = "",
    @SerializedName("target_route") val targetRoute: String = ""
)

data class TopicItem(
    val id: String = "",
    val name: String = "",
    val tag: String = ""
)

data class CurriculumResponse(
    @SerializedName("course_id") val courseId: String = "",
    val parts: List<PartItem> = emptyList()
)

data class PartItem(
    @SerializedName("part_id") val partId: String = "",
    val title: String = "",
    val order: Int = 0,
    val subparts: List<SubpartItem> = emptyList()
)

data class SubpartItem(
    @SerializedName("subpart_id") val subpartId: String = "",
    val title: String = "",
    @SerializedName("lesson_id") val lessonId: String = ""
)

data class LessonResponse(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    @SerializedName("youtube_url") val youtubeUrl: String = "",
    @SerializedName("youtube_video_id") val youtubeVideoId: String = "",
    @SerializedName("duration_sec") val durationSec: Int = 0,
    @SerializedName("whiteboard_image_url") val whiteboardImageUrl: String = "",
    @SerializedName("code_sample") val codeSample: String = "",
    val notes: String = "",
    @SerializedName("is_premium") val isPremium: Boolean = false,
    val order: Int = 0,
    @SerializedName("video_format") val videoFormat: String = "screen_recording",
    @SerializedName("shorts_url") val shortsUrl: String? = null
)

data class VisitedRequest(
    @SerializedName("course_id") val courseId: String,
    @SerializedName("part_id") val partId: String
)

// ==========================================
// AI Chat Models
// ==========================================

data class ChatRequest(
    val prompt: String,
    val model: String = "mistral-god",
    val history: List<ChatHistoryItem> = emptyList()
)

data class ChatHistoryItem(
    val role: String, // "user" or "assistant"
    val content: String
)

data class ChatResponse(
    val reply: String = "",
    @SerializedName("model_used") val modelUsed: String = "mistral-god",
    @SerializedName("is_fallback") val isFallback: Boolean = false
)

// ==========================================
// Code Runner Models
// ==========================================

data class CodeExecutionRequest(
    val language: String, // "java", "python", "cpp", "javascript"
    @SerializedName("source_code") val sourceCode: String,
    val stdin: String = ""
)

data class CodeExecutionResponse(
    val success: Boolean = false,
    val stdout: String? = null,
    val stderr: String? = null,
    @SerializedName("execution_time") val executionTime: String? = null,
    @SerializedName("memory_kb") val memoryKb: Int? = null,
    @SerializedName("status_description") val statusDescription: String = ""
)

data class OcrCleanRequest(
    @SerializedName("raw_ocr_text") val rawOcrText: String,
    val language: String = "java"
)

data class OcrCleanResponse(
    @SerializedName("cleaned_code") val cleanedCode: String = "",
    @SerializedName("corrections_applied") val correctionsApplied: List<String> = emptyList()
)

// ==========================================
// Notes Models
// ==========================================

data class NoteCreateRequest(
    val title: String,
    val content: String,
    val tag: String = "General"
)

data class NoteResponse(
    val id: String = "",
    val uid: String = "",
    val title: String = "",
    val content: String = "",
    val tag: String = "General",
    @SerializedName("created_at") val createdAt: String = ""
)

// ==========================================
// Past Year Questions (PYQs)
// ==========================================

data class PYQResponse(
    val id: String = "",
    val board: String = "ICSE",
    val year: String = "2023",
    val subject: String = "",
    val question: String = "",
    val solution: String = "",
    val marks: Int = 0
)

// ==========================================
// Search Models
// ==========================================

data class SearchResponse(
    val query: String = "",
    val courses: List<CourseItem> = emptyList(),
    val topics: List<TopicItem> = emptyList(),
    val lessons: List<LessonResponse> = emptyList()
)

// ==========================================
// Payments Models
// ==========================================

data class CreateOrderRequest(
    @SerializedName("plan_id") val planId: String,
    val amount: Double
)

data class CreateOrderResponse(
    @SerializedName("order_id") val orderId: String = "",
    val amount: Double = 0.0,
    val currency: String = "INR",
    val checksum: String = "",
    @SerializedName("payment_url") val paymentUrl: String? = null
)

// ==========================================
// System, Updates & Common Models
// ==========================================

data class CommonResponse(
    val success: Boolean = false,
    val message: String = ""
)

data class FcmTokenRequest(
    @SerializedName("fcm_token") val fcmToken: String
)

data class AppUpdateResponse(
    @SerializedName("version_name") val versionName: String = "",
    @SerializedName("version_code") val versionCode: Int = 0,
    @SerializedName("download_url") val downloadUrl: String = "",
    val changelog: String = "",
    @SerializedName("is_mandatory") val isMandatory: Boolean = false
)

// ==========================================
// Practice Sir Models
// ==========================================

data class MCQItemDto(
    val id: String = "",
    val title: String = "",
    val sub: String = "",
    val question: String? = null,
    val options: List<String> = emptyList(),
    @SerializedName("correct_index") val correctIndex: Int = 0,
    val explanation: String? = null,
    val subject: String = "Java",
    val topic: String = "OOP",
    val difficulty: String = "Easy",
    val source: String = "sir"
)

data class CodingExerciseDto(
    val id: String = "",
    val title: String = "",
    val difficulty: String = "Easy",
    val topic: String = "Arrays",
    val language: String = "java",
    val description: String? = null,
    @SerializedName("starter_code") val starterCode: String? = null,
    @SerializedName("solution_code") val solutionCode: String? = null,
    @SerializedName("test_cases") val testCases: List<Map<String, String>> = emptyList(),
    val source: String = "sir"
)

data class PredictOutputSetDto(
    val id: String = "",
    @SerializedName("set_number") val setNumber: Int = 1,
    val title: String = "",
    val topic: String = "",
    @SerializedName("question_count") val questionCount: String = "10 Questions",
    val difficulty: String = "Easy",
    @SerializedName("code_snippet") val codeSnippet: String = "",
    @SerializedName("expected_output") val expectedOutput: String? = null,
    val source: String = "sir"
)

data class QuizSetDto(
    val id: String = "",
    val title: String = "",
    val subject: String = "Java",
    @SerializedName("question_count") val questionCount: Int = 10,
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("course_id") val courseId: String? = null
)

// ==========================================
// Course Progress Models
// ==========================================

data class CourseProgressDto(
    @SerializedName("course_id") val courseId: String = "",
    @SerializedName("course_title") val courseTitle: String = "",
    @SerializedName("total_parts") val totalParts: Int = 0,
    @SerializedName("completed_parts") val completedParts: Int = 0,
    @SerializedName("completion_percent") val completionPercent: Double = 0.0,
    @SerializedName("visited_part_ids") val visitedPartIds: List<String> = emptyList()
)

data class ProgressSummaryDto(
    @SerializedName("total_courses_enrolled") val totalCoursesEnrolled: Int = 0,
    @SerializedName("overall_completion_percent") val overallCompletionPercent: Double = 0.0,
    val courses: List<CourseProgressDto> = emptyList()
)

// ==========================================
// AI Session Models
// ==========================================

data class AiSessionDto(
    @SerializedName("session_id") val sessionId: String = "",
    val title: String? = "AI Tutoring Session",
    @SerializedName("model_used") val modelUsed: String = "mistral-god",
    @SerializedName("message_count") val messageCount: Int = 0,
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("updated_at") val updatedAt: String = ""
)

data class UpdateProfileRequest(
    val name: String? = null,
    @SerializedName("student_class") val studentClass: String? = null,
    val board: String? = null,
    @SerializedName("preferred_language") val preferredLanguage: String? = null,
    val languages: List<String>? = null
)
