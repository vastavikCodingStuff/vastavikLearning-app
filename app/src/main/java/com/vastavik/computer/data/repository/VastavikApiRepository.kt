package com.vastavik.computer.data.repository

import com.vastavik.computer.data.api.ApiConfig
import com.vastavik.computer.data.api.CircuitBreaker.safeApiCall
import com.vastavik.computer.data.api.TokenManager
import com.vastavik.computer.data.api.VastavikApiService
import com.vastavik.computer.data.api.model.*
import com.vastavik.computer.data.model.LessonModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VastavikApiRepository @Inject constructor(
    private val api: VastavikApiService,
    private val tokenManager: TokenManager
) {

    // ==========================================
    // Lessons & Curriculum
    // ==========================================

    suspend fun getLesson(lessonId: String): LessonModel = api.getLesson(lessonId)

    suspend fun getLessons(courseId: String, partId: String, subpartId: String): List<LessonModel> =
        api.getLessons(courseId, partId, subpartId)

    suspend fun getLessonV1(lessonId: String): Result<LessonResponse> = safeApiCall {
        api.getLessonV1(lessonId)
    }

    suspend fun getHomeCatalog(): Result<HomeCatalogResponse> = safeApiCall {
        api.getHomeCatalog()
    }

    suspend fun getCurriculum(courseId: String): Result<CurriculumResponse> = safeApiCall {
        api.getCurriculum(courseId)
    }

    suspend fun markPartVisited(courseId: String, partId: String): Result<CommonResponse> = safeApiCall {
        api.markPartVisited(VisitedRequest(courseId, partId))
    }

    // ==========================================
    // Authentication
    // ==========================================

    suspend fun signup(
        email: String,
        password: String,
        name: String,
        board: String = "ICSE",
        language: String = "Java",
        referralCode: String? = null,
        shareToken: String? = null,
        deviceFingerprint: String? = null,
        deviceName: String? = null,
        platform: String? = null
    ): Result<AuthResponse> = safeApiCall {
        val res = api.signup(SignupRequest(email, password, name, board, language, referralCode, shareToken, deviceFingerprint, deviceName, platform))
        if (res.success && res.accessToken != null && res.refreshToken != null) {
            tokenManager.saveTokens(res.accessToken, res.refreshToken)
        }
        res
    }

    suspend fun login(
        email: String,
        password: String,
        deviceFingerprint: String? = null
    ): Result<AuthResponse> = safeApiCall {
        val res = api.login(LoginRequest(email, password, deviceFingerprint))
        if (res.success && res.accessToken != null && res.refreshToken != null) {
            tokenManager.saveTokens(res.accessToken, res.refreshToken)
        }
        res
    }

    suspend fun loginWithGoogle(idToken: String): Result<AuthResponse> = safeApiCall {
        val res = api.loginWithGoogle(OAuthGoogleRequest(idToken))
        if (res.success && res.accessToken != null && res.refreshToken != null) {
            tokenManager.saveTokens(res.accessToken, res.refreshToken)
        }
        res
    }

    suspend fun loginWithGitHub(code: String): Result<AuthResponse> = safeApiCall {
        val res = api.loginWithGitHub(OAuthGitHubRequest(code))
        if (res.success && res.accessToken != null && res.refreshToken != null) {
            tokenManager.saveTokens(res.accessToken, res.refreshToken)
        }
        res
    }

    suspend fun getUserProfile(): Result<UserProfileResponse> = safeApiCall {
        api.getUserProfile()
    }

    fun logout() {
        tokenManager.clearTokens()
    }

    /**
     * Send a batch of activity log entries to the backend. Returns true on
     * confirmed 2xx response, false otherwise. Best-effort fire-and-forget.
     */
    suspend fun logActivity(payloadJson: String): Boolean {
        return try {
            val raw = okhttp3.RequestBody.create("application/json".toMediaTypeOrNull(), payloadJson)
            val req = okhttp3.Request.Builder()
                .url("${ApiConfig.BASE_URL.removeSuffix("/")}/api/v1/activity/log")
                .post(raw)
                .build()
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            client.newCall(req).execute().use { it.isSuccessful }
        } catch (_: Exception) {
            false
        }
    }

    // ==========================================
    // AI Chat & Code Runner
    // ==========================================

    suspend fun sendAiChat(
        prompt: String,
        model: String = "mistral-god",
        history: List<ChatHistoryItem> = emptyList()
    ): Result<ChatResponse> = safeApiCall {
        api.sendAiChat(ChatRequest(prompt = prompt, model = model, history = history))
    }

    suspend fun executeCode(
        language: String,
        sourceCode: String,
        stdin: String = ""
    ): Result<CodeExecutionResponse> = safeApiCall {
        api.executeCode(CodeExecutionRequest(language = language, sourceCode = sourceCode, stdin = stdin))
    }

    suspend fun cleanOcrCode(rawOcrText: String, language: String = "java"): Result<OcrCleanResponse> = safeApiCall {
        api.cleanOcrCode(OcrCleanRequest(rawOcrText = rawOcrText, language = language))
    }

    // ==========================================
    // Notes & PYQ & Search
    // ==========================================

    suspend fun listNotes(): Result<List<NoteResponse>> = safeApiCall {
        api.listNotes()
    }

    suspend fun createNote(title: String, content: String, tag: String = "General"): Result<NoteResponse> = safeApiCall {
        api.createNote(NoteCreateRequest(title = title, content = content, tag = tag))
    }

    suspend fun deleteNote(noteId: String): Result<CommonResponse> = safeApiCall {
        api.deleteNote(noteId)
    }

    suspend fun getPyqs(
        board: String? = null,
        year: String? = null,
        subject: String? = null,
        grade: String? = null,
        source: String? = null
    ): Result<List<PYQResponse>> = safeApiCall {
        api.getPyqs(board, year, subject, grade, source)
    }

    suspend fun searchCatalog(query: String): Result<SearchResponse> = safeApiCall {
        api.searchCatalog(query)
    }

    // ==========================================
    // Practice Sir
    // ==========================================

    suspend fun getMcqs(
        subject: String? = null,
        topic: String? = null,
        difficulty: String? = null,
        source: String? = null
    ): Result<List<MCQItemDto>> = safeApiCall {
        api.getMcqs(subject, topic, difficulty, source)
    }

    suspend fun getCodingExercises(
        language: String? = null,
        difficulty: String? = null,
        source: String? = null
    ): Result<List<CodingExerciseDto>> = safeApiCall {
        api.getCodingExercises(language, difficulty, source)
    }

    suspend fun getPredictOutputSets(
        topic: String? = null,
        source: String? = null
    ): Result<List<PredictOutputSetDto>> = safeApiCall {
        api.getPredictOutputSets(topic, source)
    }

    suspend fun getQuizzes(
        subject: String? = null,
        courseId: String? = null
    ): Result<List<QuizSetDto>> = safeApiCall {
        api.getQuizzes(subject, courseId)
    }

    // ==========================================
    // Progress & Completion
    // ==========================================

    suspend fun getCourseProgress(courseId: String): Result<CourseProgressDto> = safeApiCall {
        api.getCourseProgress(courseId)
    }

    suspend fun getProgressSummary(): Result<ProgressSummaryDto> = safeApiCall {
        api.getProgressSummary()
    }

    // ==========================================
    // AI Chat Sessions
    // ==========================================

    suspend fun getAiSessions(): Result<List<AiSessionDto>> = safeApiCall {
        api.getAiSessions()
    }

    suspend fun getAiSessionMessages(sessionId: String): Result<Map<String, Any>> = safeApiCall {
        api.getAiSessionMessages(sessionId)
    }

    suspend fun deleteAiSession(sessionId: String): Result<CommonResponse> = safeApiCall {
        api.deleteAiSession(sessionId)
    }


    // ==========================================
    // Profile Update
    // ==========================================

    suspend fun updateUserProfile(request: UpdateProfileRequest): Result<UserProfileResponse> = safeApiCall {
        api.updateUserProfile(request)
    }

    // ==========================================
    // Payments
    // ==========================================

    suspend fun createPaymentOrder(planId: String = "monthly_pro", couponCode: String? = null): Result<CreateOrderResponseV2> = safeApiCall {
        api.createPaymentOrder(CreateOrderRequestV2(planId, couponCode))
    }

    suspend fun verifyPayment(razorpayOrderId: String, razorpayPaymentId: String, razorpaySignature: String): Result<CommonResponse> = safeApiCall {
        api.verifyPayment(VerifyPaymentRequest(razorpayOrderId, razorpayPaymentId, razorpaySignature))
    }

    suspend fun getPricingQuote(): Result<PricingQuote> = safeApiCall { api.getPricingQuote() }

    suspend fun generateReferralCode(): Result<Map<String, Any>> = safeApiCall { api.generateReferralCode() }

    suspend fun getReferralStatus(): Result<ReferralStatusResponse> = safeApiCall { api.getReferralStatus() }

    suspend fun generateShareToken(): Result<ShareCreateResponse> = safeApiCall { api.generateShareToken() }

    suspend fun getShareStatus(): Result<ShareStatusResponse> = safeApiCall { api.getShareStatus() }

    suspend fun trackShareClick(token: String, userAgent: String = ""): Result<Map<String, Any>> = safeApiCall {
        api.trackShareClick(token, mapOf("user_agent" to userAgent))
    }

    suspend fun redeemCoupon(code: String): Result<CouponRedeemResponse> = safeApiCall {
        api.redeemCoupon(CouponRedeemRequest(code))
    }

    suspend fun getCreditsBalance(): Result<CreditsBalanceResponse> = safeApiCall { api.getCreditsBalance() }

    suspend fun getPaymentHistory(): Result<List<Map<String, Any>>> = safeApiCall {
        api.getPaymentHistory()
    }

    // ==========================================
    // System & Updates
    // ==========================================

    suspend fun checkAppUpdate(): Result<AppUpdateResponse> = safeApiCall {
        api.checkAppUpdate()
    }

    suspend fun registerFcmToken(token: String): Result<CommonResponse> = safeApiCall {
        api.registerFcmToken(FcmTokenRequest(token))
    }

    // ==========================================
    // Multipart Uploads
    // ==========================================

    suspend fun submitDoubt(
        title: String,
        question: String,
        subject: String,
        file: File?
    ): Result<Map<String, Any>> = safeApiCall {
        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val questionPart = question.toRequestBody("text/plain".toMediaTypeOrNull())
        val subjectPart = subject.toRequestBody("text/plain".toMediaTypeOrNull())

        val filePart = file?.let {
            val mediaType = when (it.extension.lowercase()) {
                "png" -> "image/png"
                "mp4" -> "video/mp4"
                else -> "image/jpeg"
            }.toMediaTypeOrNull()
            val reqFile = it.asRequestBody(mediaType)
            MultipartBody.Part.createFormData("file", it.name, reqFile)
        }

        api.submitDoubt(titlePart, questionPart, subjectPart, filePart)
    }

    suspend fun submitBugReport(
        title: String,
        description: String,
        category: String,
        deviceDiagnostics: String,
        mediaFiles: List<File>?
    ): Result<Map<String, Any>> = safeApiCall {
        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val descPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val catPart = category.toRequestBody("text/plain".toMediaTypeOrNull())
        val diagPart = deviceDiagnostics.toRequestBody("text/plain".toMediaTypeOrNull())

        val mediaParts = mediaFiles?.mapNotNull { f ->
            if (!f.exists()) null
            else {
                val mediaType = when (f.extension.lowercase()) {
                    "png" -> "image/png"
                    "mp4" -> "video/mp4"
                    else -> "image/jpeg"
                }.toMediaTypeOrNull()
                val reqFile = f.asRequestBody(mediaType)
                MultipartBody.Part.createFormData("media", f.name, reqFile)
            }
        }

        api.submitBugReport(titlePart, descPart, catPart, diagPart, mediaParts)
    }
}
