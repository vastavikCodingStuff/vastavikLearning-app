# Graph Report - vastavikLearning-app  (2026-09-06)

## Corpus Check
- 125 files · ~94,858 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1141 nodes · 2399 edges · 71 communities (58 shown, 9 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 56 edges (avg confidence: 0.84)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `03cd3e94`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- brutalBorderColor
- AppModule
- AppNavHost
- BrutalCard
- MainActivity
- PracticeScreen.kt
- AppUpdater
- Resource
- CommonComponents.kt
- JSONObject
- QuizModel
- AuthViewModel
- MeetingClient
- WebRtcMeetingClient
- StudentSelection
- MeetingModels.kt
- CourseModel
- MeetingEvent
- MeetingViewModel
- QuizTakingScreen.kt
- VastavikApiRepository
- FirestoreRepository
- LocalMeetingClient
- WhiteboardTool
- DeviceSecurityChecker
- DebugLogBox
- ChatModel.kt
- SubscriptionModel.kt
- OnboardingViewModel
- Vastavik Learning App
- HomeScreen.kt
- DisabledFeature
- ChatViewModel
- ElementType
- MeetingComponents.kt
- AuthResponse
- BackendModels.kt
- DownloadProgressReceiver.kt
- NotificationDismissReceiver.kt
- VastavikApplication
- Vastavik Learning Platform — Master Backend Architecture & Implementation Blueprint
- WebRtcSignalType
- 🚀 Key Highlights in this Release
- graphify.md
- WebRtcSignalingClient
- VastavikApiService
- AiConversationCache
- CommonResponse
- TokenManager
- SettingsViewModel
- VastavikAiStreamer.kt
- WebRtcSignalingClient.kt
- LessonModel
- .safeApiCall
- AuthRepository
- .cleanOcrCode
- 🚀 Key Highlights in this Release
- AppModule.kt
- OcrExerciseScreen.kt
- QuizViewModel
- ProfileViewModel
- NoteResponse
- MarkdownContent.kt
- AppUpdateScreen.kt
- AuthInterceptor
- CodeEditorScreen.kt
- .executeCode

## God Nodes (most connected - your core abstractions)
1. `brutalBorderColor()` - 101 edges
2. `brutalShadowColor()` - 81 edges
3. `AppNavHost()` - 39 edges
4. `MeetingViewModel` - 34 edges
5. `VastavikApiService` - 33 edges
6. `WebRtcMeetingClient` - 30 edges
7. `VastavikApiRepository` - 30 edges
8. `AppUpdater` - 29 edges
9. `neoShape()` - 28 edges
10. `FirestoreRepository` - 27 edges

## Surprising Connections (you probably didn't know these)
- `NeoBrutalistWhiteboard()` --calls--> `Point`  [INFERRED]
  app/src/main/java/com/vastavik/computer/ui/components/Whiteboard.kt → app/src/main/java/com/vastavik/computer/data/model/MeetingModels.kt
- `Vastavik Learning App` --MONITORED_BY--> `Admin AI Diagnostics & Log Overlay`  [EXTRACTED]
  README.md → app/src/main/assets/RELEASE_NOTES_v1.0.25.md
- `Vastavik Learning App` --POWERED_BY--> `Multi-Tier AI Engine Hierarchy`  [EXTRACTED]
  README.md → app/src/main/assets/RELEASE_NOTES_v1.0.26.md
- `Vastavik Learning App` --UPDATED_BY--> `App Update & Verification System`  [EXTRACTED]
  README.md → app/src/main/assets/RELEASE_NOTES_v1.0.24.md
- `Vastavik Learning App` --INDEXED_BY--> `Graphify Knowledge Graph`  [EXTRACTED]
  README.md → .agents/rules/graphify.md

## Import Cycles
- None detected.

## Communities (71 total, 9 thin omitted)

### Community 0 - "brutalBorderColor"
Cohesion: 0.11
Nodes (34): DebugLogBoxOverlay(), Modifier, PromoData, PromoPopup(), Modifier, TelegramNotificationCard(), TelegramNotificationHost(), UnderDevelopmentBanner() (+26 more)

### Community 1 - "AppModule"
Cohesion: 0.28
Nodes (4): AppModule, com, OkHttpClient, Retrofit

### Community 2 - "AppNavHost"
Cohesion: 0.15
Nodes (20): AppNavHost(), ForgotPasswordScreen(), SplashScreen(), OcrExerciseScreen(), AccountDeletedScreen(), AdminDashboardScreen(), EditProfileScreen(), MyNotesScreen() (+12 more)

### Community 3 - "BrutalCard"
Cohesion: 0.22
Nodes (17): DoubtSolvingScreen(), BrutalPlanCard(), PaymentScreen(), BugReportScreen(), ProfileScreen(), Quadruple, BrutalBoxCard(), BrutalCard() (+9 more)

### Community 4 - "MainActivity"
Cohesion: 0.10
Nodes (15): Intent, MainActivity, VastavikTheme(), Context, StateFlow, TelegramBannerData, TelegramBannerType, APP_UPDATE (+7 more)

### Community 5 - "PracticeScreen.kt"
Cohesion: 0.10
Nodes (38): WaveformVisualizer(), BottomDevBanner(), callVastavikAiGenerateCode(), callVastavikAiGenerateMCQTopics(), callVastavikAiGeneratePredictOutput(), callVastavikAiGenerateQuestions(), CodingCard(), CodingContent() (+30 more)

### Community 6 - "AppUpdater"
Cohesion: 0.14
Nodes (9): android, AppUpdateInfo, DocumentSnapshot, AppUpdater, Bitmap, Context, Intent, StateFlow (+1 more)

### Community 7 - "Resource"
Cohesion: 0.08
Nodes (16): FirebaseMessagingService, Intent, MeetingForegroundService, MeetingNotificationManager, Error, Result, T, Loading (+8 more)

### Community 8 - "CommonComponents.kt"
Cohesion: 0.12
Nodes (35): appendFormattedText(), BottomNavItem(), ButtonVariant, Error, Outlined, Primary, Secondary, ChatBubble() (+27 more)

### Community 9 - "JSONObject"
Cohesion: 0.05
Nodes (37): isConnected, Response, WebSocket, PeerChatClient, StudentChatMessage, Context, ListenerRegistration, Response (+29 more)

### Community 10 - "QuizModel"
Cohesion: 0.14
Nodes (9): CodingChallenge, DocumentSnapshot, PYQModel, QuizModel, QuizQuestion, TestCase, StateFlow, ViewModel (+1 more)

### Community 11 - "AuthViewModel"
Cohesion: 0.13
Nodes (8): AuthUiState, AuthViewModel, Context, FirebaseUser, ViewModel, AdminSession, Context, FirebaseUser

### Community 12 - "MeetingClient"
Cohesion: 0.11
Nodes (5): ClassSession, StateFlow, MeetingClient, ClassLobbyCard(), LobbyScreen()

### Community 13 - "WebRtcMeetingClient"
Cohesion: 0.15
Nodes (3): WebRtcSignal, ListenerRegistration, WebRtcMeetingClient

### Community 14 - "StudentSelection"
Cohesion: 0.15
Nodes (7): BannerModel, DocumentSnapshot, PopularTopicModel, StudentSelection, HomeViewModel, StateFlow, ViewModel

### Community 15 - "MeetingModels.kt"
Cohesion: 0.13
Nodes (11): AuditLogEntry, LiveChatMessage, MediaState, OFF, ON, Point, Rect, ReplyPreview (+3 more)

### Community 16 - "CourseModel"
Cohesion: 0.12
Nodes (7): CourseModel, DocumentSnapshot, PartModel, SubpartModel, StateFlow, ViewModel, LearningViewModel

### Community 17 - "MeetingEvent"
Cohesion: 0.10
Nodes (21): AssignStarCast, ChatMessageSent, ClassStarted, EmojiReaction, FeatureToggle, Join, KickParticipant, Leave (+13 more)

### Community 18 - "MeetingViewModel"
Cohesion: 0.08
Nodes (9): ConnectionState, CONNECTED, CONNECTING, DISCONNECTED, FAILED, RECONNECTING, StateFlow, ViewModel (+1 more)

### Community 19 - "QuizTakingScreen.kt"
Cohesion: 0.27
Nodes (11): QuizManager, QuizQuestionData, callVastavikAiBrief(), containsCode(), AnnotatedString, Context, openPdf(), parseMarkdown() (+3 more)

### Community 20 - "VastavikApiRepository"
Cohesion: 0.13
Nodes (7): AppUpdateResponse, CurriculumResponse, LessonResponse, SearchResponse, UserProfileResponse, Result, VastavikApiRepository

### Community 21 - "FirestoreRepository"
Cohesion: 0.21
Nodes (4): FirestoreRepository, FirebaseFirestore, Flow, Constants

### Community 23 - "WhiteboardTool"
Cohesion: 0.17
Nodes (14): Color, Modifier, NeoBrutalistWhiteboard(), WhiteboardTool, ARROW, ELLIPSE, ERASER, HAND (+6 more)

### Community 24 - "DeviceSecurityChecker"
Cohesion: 0.32
Nodes (5): SecurityCheckScreen(), SecurityIssueCard(), DeviceSecurityChecker, Context, SecurityIssue

### Community 25 - "DebugLogBox"
Cohesion: 0.23
Nodes (6): DebugLogBox, Level, ERROR, INFO, WARN, LogEntry

### Community 26 - "ChatModel.kt"
Cohesion: 0.24
Nodes (4): ChatMessage, ChatSession, DocumentSnapshot, NoteModel

### Community 27 - "SubscriptionModel.kt"
Cohesion: 0.24
Nodes (4): DocumentSnapshot, SubscriptionModel, SubscriptionPlan, TransactionModel

### Community 28 - "OnboardingViewModel"
Cohesion: 0.14
Nodes (5): DocumentSnapshot, UserModel, StateFlow, ViewModel, OnboardingViewModel

### Community 29 - "Vastavik Learning App"
Cohesion: 0.22
Nodes (9): Admin AI Diagnostics & Log Overlay, Multi-Tier AI Engine Hierarchy, AI Model Selector Modal, App Update & Verification System, Graphify Knowledge Graph, Judge0 Code Execution Engine, Transparent Chat History Drawer, Two-Stage Swipe Navigation (+1 more)

### Community 30 - "HomeScreen.kt"
Cohesion: 0.18
Nodes (20): ArrowButton(), BannerAccent, DEV, PROMO, BannerCard(), BannerPage, BannersPagerOverlay(), androidx (+12 more)

### Community 31 - "DisabledFeature"
Cohesion: 0.22
Nodes (9): DisabledFeature, CAMERA, CAPTIONS, CHAT, EMOJI, MIC, RAISE_HAND, RECORDING (+1 more)

### Community 32 - "ChatViewModel"
Cohesion: 0.36
Nodes (4): ChatViewModel, ChatMessage, StateFlow, ViewModel

### Community 33 - "ElementType"
Cohesion: 0.25
Nodes (8): ElementType, ARROW, ELLIPSE, ERASER, LINE, PEN, RECTANGLE, TEXT

### Community 34 - "MeetingComponents.kt"
Cohesion: 0.23
Nodes (15): Participant, ParticipantRole, ADMIN, STARCAST, STUDENT, ChatBubble(), ControlButton(), formatTime() (+7 more)

### Community 35 - "AuthResponse"
Cohesion: 0.21
Nodes (5): AuthResponse, LoginRequest, OAuthGitHubRequest, OAuthGoogleRequest, SignupRequest

### Community 36 - "BackendModels.kt"
Cohesion: 0.18
Nodes (10): BannerItem, ChatHistoryItem, ChatRequest, ChatResponse, CourseItem, CreateOrderRequest, CreateOrderResponse, PartItem (+2 more)

### Community 37 - "DownloadProgressReceiver.kt"
Cohesion: 0.53
Nodes (4): DownloadProgressReceiver, BroadcastReceiver, Context, Intent

### Community 38 - "NotificationDismissReceiver.kt"
Cohesion: 0.53
Nodes (4): BroadcastReceiver, Context, Intent, NotificationDismissReceiver

### Community 44 - "Vastavik Learning Platform — Master Backend Architecture & Implementation Blueprint"
Cohesion: 0.07
Nodes (28): 1. Auth & Profiles, 1. Dynamic Route Maintenance Middleware (Inside Core Go Server), 1. Executive Summary & Hardware Budget (2GB RAM / 2 vCPU), 1. Rate Limiting Matrix, 1. SHA-256 Password Storage, 2. Courses & Curriculum, 2. Dual-Layer HMAC + Bearer Verification, 2. Exhaustive App Audit: Working vs. Mock vs. Broken vs. Missing (+20 more)

### Community 45 - "WebRtcSignalType"
Cohesion: 0.18
Nodes (10): StudentTypingEvent, WebRtcSignalType, ANSWER, ICE_CANDIDATE, MEDIA_UPDATE, OFFER, PEER_JOIN, PEER_LEAVE (+2 more)

### Community 46 - "🚀 Key Highlights in this Release"
Cohesion: 0.25
Nodes (7): 1. High-Performance Vastavik AI Engine (`gemini-3.6-flash`), 2. Real-Time Admin Diagnostics Banner Box, 3. Student Info / Profile UI Refinements, 4. App Update Enhancements & Download Cancellation Fixes, 🛠 Fixes & Internal Changes, 🚀 Key Highlights in this Release, Release Notes — Vastavik Computers v1.0.23

### Community 48 - "WebRtcSignalingClient"
Cohesion: 0.20
Nodes (7): isConnected, Response, WebSocket, WebRtcSignalingClient, payloadJson, senderId, signalType

### Community 49 - "VastavikApiService"
Cohesion: 0.27
Nodes (5): RefreshTokenRequest, VastavikApiService, Call, MultipartBody, RequestBody

### Community 50 - "AiConversationCache"
Cohesion: 0.38
Nodes (5): AiConversation, AiConversationCache, AiConversationSyncManager, ChatMessageData, Context

### Community 51 - "CommonResponse"
Cohesion: 0.22
Nodes (4): CommonResponse, DeviceVerifyRequest, FcmTokenRequest, VisitedRequest

### Community 53 - "SettingsViewModel"
Cohesion: 0.23
Nodes (8): androidx, ImageVector, SectionHeader(), SettingsRow(), SettingsScreen(), Flow, ViewModel, SettingsViewModel

### Community 54 - "VastavikAiStreamer.kt"
Cohesion: 0.33
Nodes (4): ApiConfig, Flow, VastavikAiStreamer, Exception

### Community 55 - "WebRtcSignalingClient.kt"
Cohesion: 0.39
Nodes (5): Response, TokenAuthenticator, Authenticator, Request, Route

### Community 56 - "LessonModel"
Cohesion: 0.08
Nodes (19): HmacUtil, LessonModel, androidx, Modifier, VastavikYouTubePlayer(), AbstractYouTubePlayerListener, LifecycleEventObserver, callVastavikAiInsight() (+11 more)

### Community 57 - ".safeApiCall"
Cohesion: 0.18
Nodes (6): CircuitBreaker, Result, T, RouteMaintenanceException, HomeCatalogResponse, PYQResponse

### Community 58 - "AuthRepository"
Cohesion: 0.21
Nodes (4): AuthRepository, FirebaseAuth, FirebaseUser, Flow

### Community 60 - "🚀 Key Highlights in this Release"
Cohesion: 0.22
Nodes (8): 1. Always-On Admin Access & Offline Diagnostics, 2. FastAPI Backend Networking & Dual REST/gRPC Integration, 3. High Authentication, JWT Refresh & Cryptographic HMAC Verification, 4. Resilient Circuit Breaking & Route Maintenance Protection, 5. Real-Time Streaming & WebSockets, 🚀 Key Highlights in this Release, Release Notes — Vastavik Computers v1.0.27, 🛠 Technical Details & Artifacts

### Community 61 - "AppModule.kt"
Cohesion: 0.29
Nodes (4): Context, FirebaseAuth, FirebaseFirestore, SharedPreferences

### Community 62 - "OcrExerciseScreen.kt"
Cohesion: 0.43
Nodes (5): imageProxyToBitmap(), Bitmap, OnImageCapturedCallback, ImageCaptureException, ImageProxy

### Community 66 - "MarkdownContent.kt"
Cohesion: 0.67
Nodes (5): AnnotatedString, Color, Modifier, MarkdownContent(), parseInlineMarkdown()

### Community 67 - "AppUpdateScreen.kt"
Cohesion: 0.60
Nodes (5): AppUpdateScreen(), ChangelogSplit, getChangelogSplit(), Modifier, TruncatedMarkdownChangelog()

### Community 68 - "AuthInterceptor"
Cohesion: 0.70
Nodes (3): AuthInterceptor, Response, Interceptor

### Community 69 - "CodeEditorScreen.kt"
Cohesion: 0.60
Nodes (4): CodeEditorScreen(), defaultCode(), highlightCode(), SyntaxColors

## Knowledge Gaps
- **133 isolated node(s):** `CourseItem`, `BannerItem`, `TopicItem`, `PartItem`, `SubpartItem` (+128 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 290 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **9 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `brutalBorderColor()` connect `brutalBorderColor` to `MeetingComponents.kt`, `AppNavHost`, `BrutalCard`, `PracticeScreen.kt`, `CodeEditorScreen.kt`, `AppUpdateScreen.kt`, `CommonComponents.kt`, `JSONObject`, `MeetingClient`, `QuizTakingScreen.kt`, `WhiteboardTool`, `OcrExerciseScreen.kt`, `HomeScreen.kt`?**
  _High betweenness centrality (0.138) - this node is a cross-community bridge._
- **Why does `brutalShadowColor()` connect `brutalBorderColor` to `MeetingComponents.kt`, `AppNavHost`, `BrutalCard`, `AppUpdateScreen.kt`, `PracticeScreen.kt`, `CommonComponents.kt`, `JSONObject`, `MeetingClient`, `QuizTakingScreen.kt`, `WhiteboardTool`, `OcrExerciseScreen.kt`, `HomeScreen.kt`?**
  _High betweenness centrality (0.106) - this node is a cross-community bridge._
- **Why does `LessonModel` connect `LessonModel` to `CourseModel`, `VastavikApiService`, `FirestoreRepository`, `CommonComponents.kt`?**
  _High betweenness centrality (0.090) - this node is a cross-community bridge._
- **What connects `CourseItem`, `BannerItem`, `TopicItem` to the rest of the system?**
  _133 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `brutalBorderColor` be split into smaller, more focused modules?**
  _Cohesion score 0.10799319727891156 - nodes in this community are weakly interconnected._
- **Should `MainActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.0967741935483871 - nodes in this community are weakly interconnected._
- **Should `PracticeScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.09574468085106383 - nodes in this community are weakly interconnected._