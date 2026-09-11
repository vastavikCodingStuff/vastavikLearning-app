package com.vastavik.computer.data.api

import com.vastavik.computer.data.api.model.*
import com.vastavik.computer.data.model.LessonModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface VastavikApiService {

    // ==========================================
    // Authentication
    // ==========================================

    @POST("api/v1/auth/signup")
    suspend fun signup(@Body request: SignupRequest): AuthResponse

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): AuthResponse

    @POST("api/v1/auth/refresh")
    fun refreshTokenSync(@Body request: RefreshTokenRequest): Call<AuthResponse>

    @POST("api/v1/auth/oauth/google")
    suspend fun loginWithGoogle(@Body request: OAuthGoogleRequest): AuthResponse

    @POST("api/v1/auth/oauth/github")
    suspend fun loginWithGitHub(@Body request: OAuthGitHubRequest): AuthResponse

    @POST("api/v1/auth/device-verify")
    suspend fun verifyDevice(@Body request: DeviceVerifyRequest): CommonResponse

    @GET("api/v1/user/profile")
    suspend fun getUserProfile(): UserProfileResponse

    @PUT("api/v1/user/profile")
    suspend fun updateUserProfile(@Body request: UpdateProfileRequest): UserProfileResponse

    // ==========================================
    // Catalog & Curriculum
    // ==========================================

    @GET("api/v1/catalog/home")
    suspend fun getHomeCatalog(@Query("force") force: Boolean = false): HomeCatalogResponse

    @GET("api/v1/courses/{courseId}/curriculum")
    suspend fun getCurriculum(@Path("courseId") courseId: String, @Query("force") force: Boolean = false): CurriculumResponse

    @GET("api/v1/lessons/{lessonId}")
    suspend fun getLessonV1(@Path("lessonId") lessonId: String): LessonResponse

    @POST("api/v1/progress/visited")
    suspend fun markPartVisited(@Body request: VisitedRequest): CommonResponse

    @GET("api/v1/courses/{courseId}/progress")
    suspend fun getCourseProgress(@Path("courseId") courseId: String): CourseProgressDto

    @GET("api/v1/progress/summary")
    suspend fun getProgressSummary(): ProgressSummaryDto

    // Legacy endpoint compatibility
    @GET("api/lessons/{lessonId}")
    suspend fun getLesson(@Path("lessonId") lessonId: String): LessonModel

    @GET("api/courses/{courseId}/parts/{partId}/subparts/{subpartId}/lessons")
    suspend fun getLessons(
        @Path("courseId") courseId: String,
        @Path("partId") partId: String,
        @Path("subpartId") subpartId: String
    ): List<LessonModel>

    // ==========================================
    // AI Chat
    // ==========================================

    @POST("api/v1/ai/chat")
    suspend fun sendAiChat(@Body request: ChatRequest): ChatResponse

    @GET("api/v1/ai/sessions")
    suspend fun getAiSessions(): List<AiSessionDto>

    @GET("api/v1/ai/sessions/{sessionId}")
    suspend fun getAiSessionMessages(@Path("sessionId") sessionId: String): Map<String, Any>

    @DELETE("api/v1/ai/sessions/{sessionId}")
    suspend fun deleteAiSession(@Path("sessionId") sessionId: String): CommonResponse

    // ==========================================
    // Practice Sir
    // ==========================================

    @GET("api/v1/practice/mcq")
    suspend fun getMcqs(
        @Query("subject") subject: String? = null,
        @Query("topic") topic: String? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("source") source: String? = null
    ): List<MCQItemDto>

    @GET("api/v1/practice/coding")
    suspend fun getCodingExercises(
        @Query("language") language: String? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("source") source: String? = null
    ): List<CodingExerciseDto>

    @GET("api/v1/practice/predict-output")
    suspend fun getPredictOutputSets(
        @Query("topic") topic: String? = null,
        @Query("source") source: String? = null
    ): List<PredictOutputSetDto>

    @GET("api/v1/practice/quiz")
    suspend fun getQuizzes(
        @Query("subject") subject: String? = null,
        @Query("course_id") courseId: String? = null
    ): List<QuizSetDto>

    @POST("api/v1/practice/submit")
    suspend fun submitPracticeAttempt(@Body request: PracticeSubmitRequest): CommonResponse

    @GET("api/v1/practice/history")
    suspend fun getPracticeHistory(): List<PracticeAttemptItem>

    // ==========================================
    // Code Runner & OCR
    // ==========================================

    @POST("api/v1/code/execute")
    suspend fun executeCode(@Body request: CodeExecutionRequest): CodeExecutionResponse

    @POST("api/v1/code/clean-ocr")
    suspend fun cleanOcrCode(@Body request: OcrCleanRequest): OcrCleanResponse

    // ==========================================
    // Notes
    // ==========================================

    @GET("api/v1/notes")
    suspend fun listNotes(): List<NoteResponse>

    @POST("api/v1/notes")
    suspend fun createNote(@Body request: NoteCreateRequest): NoteResponse

    @DELETE("api/v1/notes/{noteId}")
    suspend fun deleteNote(@Path("noteId") noteId: String): CommonResponse

    // ==========================================
    // Past Year Questions (PYQ)
    // ==========================================

    @GET("api/v1/pyqs")
    suspend fun getPyqs(
        @Query("board") board: String? = null,
        @Query("year") year: String? = null,
        @Query("subject") subject: String? = null,
        @Query("grade") grade: String? = null,
        @Query("source") source: String? = null
    ): List<PYQResponse>

    // ==========================================
    // Search
    // ==========================================

    @GET("api/v1/search")
    suspend fun searchCatalog(@Query("q") query: String): SearchResponse

    @GET("api/v1/search/history")
    suspend fun getSearchHistory(): List<SearchHistoryItem>

    // ==========================================
    // Payments
    // ==========================================

    @POST("api/v1/payments/create-order")
    suspend fun createPaymentOrder(@Body request: CreateOrderRequestV2): CreateOrderResponseV2

    @POST("api/v1/payments/verify")
    suspend fun verifyPayment(@Body request: VerifyPaymentRequest): CommonResponse

    @GET("api/v1/payments/history")
    suspend fun getPaymentHistory(): List<Map<String, Any>>

    // ==========================================
    // Growth (Referral, Share, Coupon, Pricing)
    // ==========================================

    @POST("api/v1/referral/generate")
    suspend fun generateReferralCode(): Map<String, Any>

    @GET("api/v1/referral/status")
    suspend fun getReferralStatus(): ReferralStatusResponse

    @POST("api/v1/share/generate")
    suspend fun generateShareToken(): ShareCreateResponse

    @GET("api/v1/share/status")
    suspend fun getShareStatus(): ShareStatusResponse

    @POST("api/v1/share/track/{token}")
    suspend fun trackShareClick(@Path("token") token: String, @Body body: Map<String, String>): Map<String, Any>

    @POST("api/v1/coupon/redeem")
    suspend fun redeemCoupon(@Body request: CouponRedeemRequest): CouponRedeemResponse

    @GET("api/v1/pricing/quote")
    suspend fun getPricingQuote(): PricingQuote

    @GET("api/v1/credits/balance")
    suspend fun getCreditsBalance(): CreditsBalanceResponse

    // ==========================================
    // System & Updates
    // ==========================================

    @GET("api/v1/system/app-update")
    suspend fun checkAppUpdate(): AppUpdateResponse

    @POST("api/v1/notifications/token")
    suspend fun registerFcmToken(@Body request: FcmTokenRequest): CommonResponse

    // ==========================================
    // Multipart File Uploads
    // ==========================================

    @Multipart
    @POST("api/v1/doubts/submit")
    suspend fun submitDoubt(
        @Part("title") title: RequestBody,
        @Part("question") question: RequestBody,
        @Part("subject") subject: RequestBody,
        @Part file: MultipartBody.Part?
    ): Map<String, Any>

    @Multipart
    @POST("api/v1/system/bug-report")
    suspend fun submitBugReport(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("category") category: RequestBody,
        @Part("device_diagnostics") diagnostics: RequestBody,
        @Part media: List<MultipartBody.Part>?
    ): Map<String, Any>
}
