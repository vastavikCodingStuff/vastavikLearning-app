# Graph Report - vastavikLearning-app  (2026-09-04)

## Corpus Check
- 125 files · ~94,524 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1134 nodes · 2393 edges · 60 communities (48 shown, 8 thin omitted)
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
- JSONObject
- MainActivity
- PracticeScreen.kt
- AppUpdater
- Resource
- CommonComponents.kt
- ChatScreen.kt
- QuizModel
- AuthViewModel
- MeetingClient
- WebRtcMeetingClient
- StudentSelection
- MeetingModels.kt
- LessonModel
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
- CourseModel
- Vastavik Learning App
- ClassSession
- DisabledFeature
- ChatViewModel
- ElementType
- PeerChatClient
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
- LearningViewModel
- VastavikAiStreamer.kt
- TokenAuthenticator.kt
- VideoLessonViewModel.kt
- Judge0Service
- .createPaymentOrder
- .cleanOcrCode

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

## Communities (60 total, 8 thin omitted)

### Community 0 - "brutalBorderColor"
Cohesion: 0.06
Nodes (74): Participant, ArrowButton(), BannerAccent, DEV, PROMO, BannerCard(), BannerPage, BannersPagerOverlay() (+66 more)

### Community 1 - "AppModule"
Cohesion: 0.07
Nodes (17): AuthInterceptor, Response, AuthRepository, FirebaseAuth, FirebaseUser, Flow, AppModule, com (+9 more)

### Community 2 - "AppNavHost"
Cohesion: 0.06
Nodes (42): HmacUtil, androidx, Modifier, VastavikYouTubePlayer(), AbstractYouTubePlayerListener, LifecycleEventObserver, AppNavHost(), ForgotPasswordScreen() (+34 more)

### Community 3 - "JSONObject"
Cohesion: 0.28
Nodes (3): Context, VastavikAiDiskCache, JSONObject

### Community 4 - "MainActivity"
Cohesion: 0.07
Nodes (26): Intent, MainActivity, Modifier, TelegramNotificationCard(), TelegramNotificationHost(), androidx, ImageVector, SectionHeader() (+18 more)

### Community 5 - "PracticeScreen.kt"
Cohesion: 0.10
Nodes (38): WaveformVisualizer(), BottomDevBanner(), callVastavikAiGenerateCode(), callVastavikAiGenerateMCQTopics(), callVastavikAiGeneratePredictOutput(), callVastavikAiGenerateQuestions(), CodingCard(), CodingContent() (+30 more)

### Community 6 - "AppUpdater"
Cohesion: 0.09
Nodes (20): android, AppUpdateInfo, DocumentSnapshot, AnnotatedString, Color, Modifier, MarkdownContent(), parseInlineMarkdown() (+12 more)

### Community 7 - "Resource"
Cohesion: 0.08
Nodes (16): FirebaseMessagingService, Intent, MeetingForegroundService, MeetingNotificationManager, Error, Result, T, Loading (+8 more)

### Community 8 - "CommonComponents.kt"
Cohesion: 0.11
Nodes (36): appendFormattedText(), BottomNavItem(), ButtonVariant, Error, Outlined, Primary, Secondary, ChatBubble() (+28 more)

### Community 9 - "ChatScreen.kt"
Cohesion: 0.08
Nodes (30): StudentChatMessage, Context, ListenerRegistration, Response, StateFlow, WebSocket, StudentConversationManager, WebSocketListener (+22 more)

### Community 10 - "QuizModel"
Cohesion: 0.09
Nodes (11): CodingChallenge, DocumentSnapshot, PYQModel, QuizModel, QuizQuestion, TestCase, StateFlow, ViewModel (+3 more)

### Community 11 - "AuthViewModel"
Cohesion: 0.13
Nodes (8): AuthUiState, AuthViewModel, Context, FirebaseUser, ViewModel, AdminSession, Context, FirebaseUser

### Community 12 - "MeetingClient"
Cohesion: 0.10
Nodes (4): LiveChatMessage, ReplyPreview, StateFlow, MeetingClient

### Community 13 - "WebRtcMeetingClient"
Cohesion: 0.16
Nodes (3): WebRtcSignal, ListenerRegistration, WebRtcMeetingClient

### Community 14 - "StudentSelection"
Cohesion: 0.15
Nodes (7): BannerModel, DocumentSnapshot, PopularTopicModel, StudentSelection, HomeViewModel, StateFlow, ViewModel

### Community 15 - "MeetingModels.kt"
Cohesion: 0.12
Nodes (14): AuditLogEntry, MediaState, OFF, ON, ParticipantRole, ADMIN, STARCAST, STUDENT (+6 more)

### Community 16 - "LessonModel"
Cohesion: 0.14
Nodes (3): DocumentSnapshot, LessonModel, SubpartModel

### Community 17 - "MeetingEvent"
Cohesion: 0.10
Nodes (21): AssignStarCast, ChatMessageSent, ClassStarted, EmojiReaction, FeatureToggle, Join, KickParticipant, Leave (+13 more)

### Community 18 - "MeetingViewModel"
Cohesion: 0.08
Nodes (9): ConnectionState, CONNECTED, CONNECTING, DISCONNECTED, FAILED, RECONNECTING, StateFlow, ViewModel (+1 more)

### Community 19 - "QuizTakingScreen.kt"
Cohesion: 0.23
Nodes (15): QuizManager, QuizQuestionData, callVastavikAiBrief(), containsCode(), androidx, AnnotatedString, Context, openPdf() (+7 more)

### Community 20 - "VastavikApiRepository"
Cohesion: 0.11
Nodes (11): Result, T, AppUpdateResponse, CurriculumResponse, HomeCatalogResponse, LessonResponse, PYQResponse, SearchResponse (+3 more)

### Community 21 - "FirestoreRepository"
Cohesion: 0.14
Nodes (6): PartModel, DocumentSnapshot, UserModel, FirestoreRepository, FirebaseFirestore, Flow

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

### Community 28 - "CourseModel"
Cohesion: 0.18
Nodes (4): CourseModel, StateFlow, ViewModel, OnboardingViewModel

### Community 29 - "Vastavik Learning App"
Cohesion: 0.22
Nodes (9): Admin AI Diagnostics & Log Overlay, Multi-Tier AI Engine Hierarchy, AI Model Selector Modal, App Update & Verification System, Graphify Knowledge Graph, Judge0 Code Execution Engine, Transparent Chat History Drawer, Two-Stage Swipe Navigation (+1 more)

### Community 30 - "ClassSession"
Cohesion: 0.67
Nodes (3): ClassSession, ClassLobbyCard(), LobbyScreen()

### Community 31 - "DisabledFeature"
Cohesion: 0.22
Nodes (9): DisabledFeature, CAMERA, CAPTIONS, CHAT, EMOJI, MIC, RAISE_HAND, RECORDING (+1 more)

### Community 32 - "ChatViewModel"
Cohesion: 0.36
Nodes (4): ChatViewModel, ChatMessage, StateFlow, ViewModel

### Community 33 - "ElementType"
Cohesion: 0.25
Nodes (8): ElementType, ARROW, ELLIPSE, ERASER, LINE, PEN, RECTANGLE, TEXT

### Community 34 - "PeerChatClient"
Cohesion: 0.18
Nodes (7): ApiConfig, isConnected, Response, WebSocket, PeerChatClient, message, sender

### Community 35 - "AuthResponse"
Cohesion: 0.17
Nodes (6): AuthResponse, LoginRequest, OAuthGitHubRequest, OAuthGoogleRequest, RefreshTokenRequest, SignupRequest

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
Cohesion: 0.20
Nodes (7): isConnected, Response, WebSocket, WebRtcSignalingClient, payloadJson, senderId, signalType

### Community 49 - "VastavikApiService"
Cohesion: 0.23
Nodes (6): NoteCreateRequest, NoteResponse, VastavikApiService, Call, MultipartBody, RequestBody

### Community 50 - "AiConversationCache"
Cohesion: 0.38
Nodes (5): AiConversation, AiConversationCache, AiConversationSyncManager, ChatMessageData, Context

### Community 51 - "CommonResponse"
Cohesion: 0.22
Nodes (4): CommonResponse, DeviceVerifyRequest, FcmTokenRequest, VisitedRequest

### Community 53 - "LearningViewModel"
Cohesion: 0.28
Nodes (3): StateFlow, ViewModel, LearningViewModel

### Community 54 - "VastavikAiStreamer.kt"
Cohesion: 0.29
Nodes (5): CircuitBreaker, RouteMaintenanceException, Flow, VastavikAiStreamer, Exception

### Community 55 - "TokenAuthenticator.kt"
Cohesion: 0.48
Nodes (5): Response, TokenAuthenticator, Authenticator, Request, Route

## Knowledge Gaps
- **128 isolated node(s):** `CourseItem`, `BannerItem`, `TopicItem`, `PartItem`, `SubpartItem` (+123 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 284 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **8 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `brutalBorderColor()` connect `brutalBorderColor` to `AppNavHost`, `MainActivity`, `PracticeScreen.kt`, `AppUpdater`, `CommonComponents.kt`, `ChatScreen.kt`, `QuizTakingScreen.kt`, `WhiteboardTool`, `ClassSession`?**
  _High betweenness centrality (0.142) - this node is a cross-community bridge._
- **Why does `brutalShadowColor()` connect `brutalBorderColor` to `AppNavHost`, `MainActivity`, `PracticeScreen.kt`, `AppUpdater`, `CommonComponents.kt`, `ChatScreen.kt`, `QuizTakingScreen.kt`, `WhiteboardTool`, `ClassSession`?**
  _High betweenness centrality (0.110) - this node is a cross-community bridge._
- **Why does `LessonModel` connect `LessonModel` to `AppNavHost`, `CommonComponents.kt`, `VastavikApiService`, `TokenManager`, `LearningViewModel`, `FirestoreRepository`, `VideoLessonViewModel.kt`?**
  _High betweenness centrality (0.091) - this node is a cross-community bridge._
- **What connects `CourseItem`, `BannerItem`, `TopicItem` to the rest of the system?**
  _128 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `brutalBorderColor` be split into smaller, more focused modules?**
  _Cohesion score 0.06404040404040404 - nodes in this community are weakly interconnected._
- **Should `AppModule` be split into smaller, more focused modules?**
  _Cohesion score 0.07293868921775898 - nodes in this community are weakly interconnected._
- **Should `AppNavHost` be split into smaller, more focused modules?**
  _Cohesion score 0.06418219461697723 - nodes in this community are weakly interconnected._