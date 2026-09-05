# Graph Report - vastavikLearning-app  (2026-09-06)

## Corpus Check
- 133 files · ~112,864 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1216 nodes · 2531 edges · 71 communities (55 shown, 12 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 56 edges (avg confidence: 0.84)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `d039152b`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- brutalBorderColor
- AppModule
- R
- VastavikAiDiskCache
- MainActivity
- PracticeScreen.kt
- AppUpdater
- VastavikApiRepository
- CommonComponents.kt
- StudentConversationManager
- QuizModel
- AuthViewModel
- MeetingClient
- WebRtcMeetingClient
- CourseModel
- MeetingModels.kt
- LearningViewModel
- MeetingEvent
- MeetingViewModel
- QuizTakingScreen.kt
- .safeApiCall
- FirestoreRepository
- LocalMeetingClient
- PeerChatClient
- DeviceSecurityChecker
- DebugLogBox
- ChatModel.kt
- SubscriptionModel.kt
- OnboardingViewModel
- Vastavik Learning App
- LiveChatMessage
- DisabledFeature
- ChatViewModel
- ElementType
- OcrExerciseScreen.kt
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
- JSONObject
- TokenAuthenticator.kt
- neoShape
- 🚀 Key Highlights in this Release
- WhiteboardTool
- .cleanOcrCode
- 🚀 Key Highlights in this Release
- WebSocketListener
- ProfileViewModel
- QuizViewModel
- 🚀 Key Highlights in this Release
- NoteResponse
- 🚀 Key Highlights in this Release
- .createPaymentOrder
- RazorpayPaymentMethod
- Resource
- .checkAppUpdate

## God Nodes (most connected - your core abstractions)
1. `brutalBorderColor()` - 107 edges
2. `brutalShadowColor()` - 83 edges
3. `AppNavHost()` - 40 edges
4. `MeetingViewModel` - 34 edges
5. `VastavikApiService` - 33 edges
6. `WebRtcMeetingClient` - 30 edges
7. `VastavikApiRepository` - 30 edges
8. `AppUpdater` - 29 edges
9. `FirestoreRepository` - 27 edges
10. `MeetingClient` - 26 edges

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

## Communities (71 total, 12 thin omitted)

### Community 0 - "brutalBorderColor"
Cohesion: 0.06
Nodes (89): ArrowButton(), BannerAccent, DEV, PROMO, BannerCard(), BannerPage, BannersPagerOverlay(), androidx (+81 more)

### Community 1 - "AppModule"
Cohesion: 0.09
Nodes (15): AuthInterceptor, Response, AuthRepository, FirebaseAuth, FirebaseUser, Flow, AppModule, com (+7 more)

### Community 2 - "R"
Cohesion: 0.11
Nodes (14): FirebaseMessagingService, Intent, MeetingForegroundService, MeetingNotificationManager, Canvas, Context, Paint, PredictOutputPdf (+6 more)

### Community 4 - "MainActivity"
Cohesion: 0.07
Nodes (23): Intent, MainActivity, Modifier, TelegramNotificationCard(), TelegramNotificationHost(), VastavikTheme(), Context, StateFlow (+15 more)

### Community 5 - "PracticeScreen.kt"
Cohesion: 0.07
Nodes (55): callVastavikAiChat(), ChatBubbleRow(), ChatMessage, ChatSyntaxColors, highlightCode(), androidx, Color, com (+47 more)

### Community 6 - "AppUpdater"
Cohesion: 0.10
Nodes (18): android, AppUpdateInfo, DocumentSnapshot, AnnotatedString, Color, Modifier, MarkdownContent(), parseInlineMarkdown() (+10 more)

### Community 7 - "VastavikApiRepository"
Cohesion: 0.22
Nodes (3): PYQResponse, LessonModel, VastavikApiRepository

### Community 8 - "CommonComponents.kt"
Cohesion: 0.11
Nodes (36): appendFormattedText(), BottomNavItem(), ButtonVariant, Error, Outlined, Primary, Secondary, ChatBubble() (+28 more)

### Community 9 - "StudentConversationManager"
Cohesion: 0.33
Nodes (5): StudentChatMessage, Context, ListenerRegistration, StateFlow, StudentConversationManager

### Community 10 - "QuizModel"
Cohesion: 0.13
Nodes (9): CodingChallenge, DocumentSnapshot, PYQModel, QuizModel, QuizQuestion, TestCase, StateFlow, ViewModel (+1 more)

### Community 11 - "AuthViewModel"
Cohesion: 0.13
Nodes (8): AuthUiState, AuthViewModel, Context, FirebaseUser, ViewModel, AdminSession, Context, FirebaseUser

### Community 12 - "MeetingClient"
Cohesion: 0.11
Nodes (4): ClassSession, Participant, StateFlow, MeetingClient

### Community 13 - "WebRtcMeetingClient"
Cohesion: 0.17
Nodes (3): WebRtcSignal, ListenerRegistration, WebRtcMeetingClient

### Community 14 - "CourseModel"
Cohesion: 0.14
Nodes (8): BannerModel, DocumentSnapshot, PopularTopicModel, StudentSelection, CourseModel, HomeViewModel, StateFlow, ViewModel

### Community 15 - "MeetingModels.kt"
Cohesion: 0.12
Nodes (14): AuditLogEntry, MediaState, OFF, ON, ParticipantRole, ADMIN, STARCAST, STUDENT (+6 more)

### Community 16 - "LearningViewModel"
Cohesion: 0.12
Nodes (6): DocumentSnapshot, PartModel, SubpartModel, StateFlow, ViewModel, LearningViewModel

### Community 17 - "MeetingEvent"
Cohesion: 0.10
Nodes (21): AssignStarCast, ChatMessageSent, ClassStarted, EmojiReaction, FeatureToggle, Join, KickParticipant, Leave (+13 more)

### Community 18 - "MeetingViewModel"
Cohesion: 0.08
Nodes (9): ConnectionState, CONNECTED, CONNECTING, DISCONNECTED, FAILED, RECONNECTING, StateFlow, ViewModel (+1 more)

### Community 19 - "QuizTakingScreen.kt"
Cohesion: 0.21
Nodes (19): QuizManager, QuizQuestionData, callVastavikAiBrief(), containsCode(), drawPdfPageFrame(), androidx, AnnotatedString, Canvas (+11 more)

### Community 20 - ".safeApiCall"
Cohesion: 0.12
Nodes (8): Result, T, CurriculumResponse, HomeCatalogResponse, LessonResponse, SearchResponse, UserProfileResponse, Result

### Community 21 - "FirestoreRepository"
Cohesion: 0.13
Nodes (6): FirestoreRepository, FirebaseFirestore, Flow, ViewModel, VideoLessonViewModel, Constants

### Community 23 - "PeerChatClient"
Cohesion: 0.18
Nodes (7): ApiConfig, isConnected, Response, WebSocket, PeerChatClient, message, sender

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

### Community 31 - "DisabledFeature"
Cohesion: 0.22
Nodes (9): DisabledFeature, CAMERA, CAPTIONS, CHAT, EMOJI, MIC, RAISE_HAND, RECORDING (+1 more)

### Community 32 - "ChatViewModel"
Cohesion: 0.36
Nodes (4): ChatViewModel, ChatMessage, StateFlow, ViewModel

### Community 33 - "ElementType"
Cohesion: 0.25
Nodes (8): ElementType, ARROW, ELLIPSE, ERASER, LINE, PEN, RECTANGLE, TEXT

### Community 34 - "OcrExerciseScreen.kt"
Cohesion: 0.43
Nodes (5): imageProxyToBitmap(), Bitmap, OnImageCapturedCallback, ImageCaptureException, ImageProxy

### Community 35 - "AuthResponse"
Cohesion: 0.21
Nodes (5): AuthResponse, LoginRequest, OAuthGitHubRequest, OAuthGoogleRequest, SignupRequest

### Community 36 - "BackendModels.kt"
Cohesion: 0.18
Nodes (10): BannerItem, ChatHistoryItem, ChatRequest, ChatResponse, CodeExecutionRequest, CodeExecutionResponse, CourseItem, PartItem (+2 more)

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
Cohesion: 0.22
Nodes (9): WebRtcSignalType, ANSWER, ICE_CANDIDATE, MEDIA_UPDATE, OFFER, PEER_JOIN, PEER_LEAVE, SCREEN_SHARE_START (+1 more)

### Community 46 - "🚀 Key Highlights in this Release"
Cohesion: 0.25
Nodes (7): 1. High-Performance Vastavik AI Engine (`gemini-3.6-flash`), 2. Real-Time Admin Diagnostics Banner Box, 3. Student Info / Profile UI Refinements, 4. App Update Enhancements & Download Cancellation Fixes, 🛠 Fixes & Internal Changes, 🚀 Key Highlights in this Release, Release Notes — Vastavik Computers v1.0.23

### Community 48 - "WebRtcSignalingClient"
Cohesion: 0.22
Nodes (7): isConnected, Response, WebSocket, WebRtcSignalingClient, payloadJson, senderId, signalType

### Community 49 - "VastavikApiService"
Cohesion: 0.23
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

### Community 54 - "JSONObject"
Cohesion: 0.23
Nodes (6): CircuitBreaker, RouteMaintenanceException, Flow, VastavikAiStreamer, Exception, JSONObject

### Community 55 - "TokenAuthenticator.kt"
Cohesion: 0.48
Nodes (5): Response, TokenAuthenticator, Authenticator, Request, Route

### Community 56 - "neoShape"
Cohesion: 0.08
Nodes (27): HmacUtil, androidx, Modifier, VastavikYouTubePlayer(), AbstractYouTubePlayerListener, LifecycleEventObserver, AccountDeletedScreen(), EditProfileScreen() (+19 more)

### Community 57 - "🚀 Key Highlights in this Release"
Cohesion: 0.20
Nodes (9): 1. Quiz PDF Safe-Area Formatter & Typography Overhaul, 2. Background GenZ Teacher Notifications & Duplicate Alert Suppression, 3. Predict the Output In-Place NeoBrutalistic Dialog, 4. Code Editor: Voice Prompting & 4-Part Structured Problem Generator, 5. Board PYQs with Authentic Color Banners & AI Search, 6. AI Engine Tiering & AI Chat Architecture Fixes, 🚀 Key Highlights in this Release, Release Notes — Vastavik Computers v1.0.29 (+1 more)

### Community 58 - "WhiteboardTool"
Cohesion: 0.17
Nodes (14): Color, Modifier, NeoBrutalistWhiteboard(), WhiteboardTool, ARROW, ELLIPSE, ERASER, HAND (+6 more)

### Community 60 - "🚀 Key Highlights in this Release"
Cohesion: 0.22
Nodes (8): 1. Always-On Admin Access & Offline Diagnostics, 2. FastAPI Backend Networking & Dual REST/gRPC Integration, 3. High Authentication, JWT Refresh & Cryptographic HMAC Verification, 4. Resilient Circuit Breaking & Route Maintenance Protection, 5. Real-Time Streaming & WebSockets, 🚀 Key Highlights in this Release, Release Notes — Vastavik Computers v1.0.27, 🛠 Technical Details & Artifacts

### Community 61 - "WebSocketListener"
Cohesion: 0.48
Nodes (3): Response, WebSocket, WebSocketListener

### Community 64 - "🚀 Key Highlights in this Release"
Cohesion: 0.25
Nodes (7): 1. Razorpay Integration & Complete PhonePe Direct Removal, 2. Official Tax Invoice & Payment Receipt PDF Generator, 3. Ninja Samurai Celebration Mode, 4. Unrestricted Administrator Access, 🚀 Key Highlights in this Release, Release Notes — Vastavik Computers v1.0.30, 🛠 Technical Details & Artifacts

### Community 66 - "🚀 Key Highlights in this Release"
Cohesion: 0.33
Nodes (5): 1. Code Editor Question Overview Dialog Overhaul, 2. "Predict the Output" Comprehensive Suite, 🚀 Key Highlights in this Release, Release Notes — Vastavik Computers v1.0.31, 🛠 Technical Details & Artifacts

### Community 68 - "RazorpayPaymentMethod"
Cohesion: 0.40
Nodes (5): RazorpayPaymentMethod, CARD, NETBANKING, UPI_AUTOPAY, UPI_STANDARD

### Community 69 - "Resource"
Cohesion: 0.07
Nodes (26): CodeEditorScreen(), defaultCode(), getStructuredSections(), highlightCode(), parseProblemSections(), ProblemSection, SyntaxColors, getBoardColor() (+18 more)

## Knowledge Gaps
- **152 isolated node(s):** `CourseItem`, `BannerItem`, `TopicItem`, `PartItem`, `SubpartItem` (+147 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 316 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **12 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `brutalBorderColor()` connect `brutalBorderColor` to `OcrExerciseScreen.kt`, `MainActivity`, `Resource`, `PracticeScreen.kt`, `AppUpdater`, `CommonComponents.kt`, `QuizTakingScreen.kt`, `WhiteboardTool`?**
  _High betweenness centrality (0.166) - this node is a cross-community bridge._
- **Why does `brutalShadowColor()` connect `brutalBorderColor` to `OcrExerciseScreen.kt`, `MainActivity`, `PracticeScreen.kt`, `AppUpdater`, `CommonComponents.kt`, `QuizTakingScreen.kt`, `WhiteboardTool`?**
  _High betweenness centrality (0.095) - this node is a cross-community bridge._
- **Why does `LessonModel` connect `VastavikApiRepository` to `CommonComponents.kt`, `LearningViewModel`, `VastavikApiService`, `FirestoreRepository`, `neoShape`?**
  _High betweenness centrality (0.078) - this node is a cross-community bridge._
- **What connects `CourseItem`, `BannerItem`, `TopicItem` to the rest of the system?**
  _152 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `brutalBorderColor` be split into smaller, more focused modules?**
  _Cohesion score 0.05619834710743802 - nodes in this community are weakly interconnected._
- **Should `AppModule` be split into smaller, more focused modules?**
  _Cohesion score 0.0915915915915916 - nodes in this community are weakly interconnected._
- **Should `R` be split into smaller, more focused modules?**
  _Cohesion score 0.1051693404634581 - nodes in this community are weakly interconnected._