# Graph Report - vastavikLearning-app  (2026-09-04)

## Corpus Check
- 117 files · ~91,672 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 983 nodes · 2071 edges · 48 communities (39 shown, 5 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 21 edges (avg confidence: 0.84)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `03cd3e94`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- brutalBorderColor
- LessonModel
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
- FirestoreRepository.kt
- MeetingModels.kt
- CourseModel
- MeetingEvent
- MeetingViewModel
- QuizTakingScreen.kt
- VastavikYouTubePlayer.kt
- FirestoreRepository
- LocalMeetingClient
- WhiteboardTool
- DeviceSecurityChecker
- DebugLogBox
- ChatModel.kt
- SubscriptionModel.kt
- OnboardingViewModel
- Vastavik Learning App
- Participant
- DisabledFeature
- ChatViewModel
- ElementType
- QuizViewModel
- ProfileViewModel
- MarkdownContent.kt
- DownloadProgressReceiver.kt
- NotificationDismissReceiver.kt
- VastavikApplication
- Vastavik Learning Platform — Master Backend Architecture & Implementation Blueprint
- WebRtcSignalType
- 🚀 Key Highlights in this Release
- graphify.md

## God Nodes (most connected - your core abstractions)
1. `brutalBorderColor()` - 101 edges
2. `brutalShadowColor()` - 81 edges
3. `AppNavHost()` - 39 edges
4. `MeetingViewModel` - 34 edges
5. `WebRtcMeetingClient` - 30 edges
6. `AppUpdater` - 29 edges
7. `neoShape()` - 28 edges
8. `FirestoreRepository` - 27 edges
9. `MeetingClient` - 26 edges
10. `LocalMeetingClient` - 22 edges

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

## Communities (48 total, 5 thin omitted)

### Community 0 - "brutalBorderColor"
Cohesion: 0.07
Nodes (72): ArrowButton(), BannerAccent, DEV, PROMO, BannerCard(), BannerPage, BannersPagerOverlay(), androidx (+64 more)

### Community 1 - "LessonModel"
Cohesion: 0.06
Nodes (20): ApiConfig, AuthInterceptor, Response, VastavikApiService, LessonModel, AuthRepository, FirebaseAuth, FirebaseUser (+12 more)

### Community 2 - "AppNavHost"
Cohesion: 0.10
Nodes (33): AppNavHost(), ForgotPasswordScreen(), CodeEditorScreen(), defaultCode(), highlightCode(), SyntaxColors, imageProxyToBitmap(), Bitmap (+25 more)

### Community 3 - "JSONObject"
Cohesion: 0.13
Nodes (10): AiConversation, AiConversationCache, AiConversationSyncManager, ChatMessageData, Context, ExecutionResult, Judge0Service, Context (+2 more)

### Community 4 - "MainActivity"
Cohesion: 0.07
Nodes (26): Intent, MainActivity, Modifier, TelegramNotificationCard(), TelegramNotificationHost(), androidx, ImageVector, SectionHeader() (+18 more)

### Community 5 - "PracticeScreen.kt"
Cohesion: 0.10
Nodes (38): WaveformVisualizer(), BottomDevBanner(), callVastavikAiGenerateCode(), callVastavikAiGenerateMCQTopics(), callVastavikAiGeneratePredictOutput(), callVastavikAiGenerateQuestions(), CodingCard(), CodingContent() (+30 more)

### Community 6 - "AppUpdater"
Cohesion: 0.11
Nodes (15): android, AppUpdateInfo, DocumentSnapshot, AppUpdateScreen(), ChangelogSplit, getChangelogSplit(), Modifier, TruncatedMarkdownChangelog() (+7 more)

### Community 7 - "Resource"
Cohesion: 0.08
Nodes (16): FirebaseMessagingService, Intent, MeetingForegroundService, MeetingNotificationManager, Error, Loading, Resource, Success (+8 more)

### Community 8 - "CommonComponents.kt"
Cohesion: 0.11
Nodes (36): appendFormattedText(), BottomNavItem(), ButtonVariant, Error, Outlined, Primary, Secondary, ChatBubble() (+28 more)

### Community 9 - "ChatScreen.kt"
Cohesion: 0.08
Nodes (30): StudentChatMessage, Context, ListenerRegistration, Response, StateFlow, StudentConversationManager, WebSocketListener, callVastavikAiChat() (+22 more)

### Community 10 - "QuizModel"
Cohesion: 0.12
Nodes (9): CodingChallenge, DocumentSnapshot, PYQModel, QuizModel, QuizQuestion, TestCase, StateFlow, ViewModel (+1 more)

### Community 11 - "AuthViewModel"
Cohesion: 0.13
Nodes (8): AuthUiState, AuthViewModel, Context, FirebaseUser, ViewModel, AdminSession, Context, FirebaseUser

### Community 12 - "MeetingClient"
Cohesion: 0.10
Nodes (4): LiveChatMessage, ReplyPreview, StateFlow, MeetingClient

### Community 13 - "WebRtcMeetingClient"
Cohesion: 0.17
Nodes (3): WebRtcSignal, ListenerRegistration, WebRtcMeetingClient

### Community 14 - "FirestoreRepository.kt"
Cohesion: 0.19
Nodes (7): BannerModel, DocumentSnapshot, PopularTopicModel, StudentSelection, HomeViewModel, StateFlow, ViewModel

### Community 15 - "MeetingModels.kt"
Cohesion: 0.12
Nodes (14): AuditLogEntry, MediaState, OFF, ON, ParticipantRole, ADMIN, STARCAST, STUDENT (+6 more)

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
Cohesion: 0.23
Nodes (15): QuizManager, QuizQuestionData, callVastavikAiBrief(), containsCode(), androidx, AnnotatedString, Context, openPdf() (+7 more)

### Community 20 - "VastavikYouTubePlayer.kt"
Cohesion: 0.18
Nodes (9): HmacUtil, androidx, Modifier, VastavikYouTubePlayer(), AbstractYouTubePlayerListener, LifecycleEventObserver, Lifecycle, PlayerConstants (+1 more)

### Community 21 - "FirestoreRepository"
Cohesion: 0.21
Nodes (3): FirestoreRepository, FirebaseFirestore, Flow

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

### Community 30 - "Participant"
Cohesion: 0.36
Nodes (4): ClassSession, Participant, ClassLobbyCard(), LobbyScreen()

### Community 31 - "DisabledFeature"
Cohesion: 0.22
Nodes (9): DisabledFeature, CAMERA, CAPTIONS, CHAT, EMOJI, MIC, RAISE_HAND, RECORDING (+1 more)

### Community 32 - "ChatViewModel"
Cohesion: 0.36
Nodes (4): ChatViewModel, ChatMessage, StateFlow, ViewModel

### Community 33 - "ElementType"
Cohesion: 0.25
Nodes (8): ElementType, ARROW, ELLIPSE, ERASER, LINE, PEN, RECTANGLE, TEXT

### Community 36 - "MarkdownContent.kt"
Cohesion: 0.67
Nodes (5): AnnotatedString, Color, Modifier, MarkdownContent(), parseInlineMarkdown()

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

## Knowledge Gaps
- **122 isolated node(s):** `ADMIN`, `STARCAST`, `STUDENT`, `ON`, `OFF` (+117 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 256 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **5 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `brutalBorderColor()` connect `brutalBorderColor` to `AppNavHost`, `MainActivity`, `PracticeScreen.kt`, `AppUpdater`, `CommonComponents.kt`, `ChatScreen.kt`, `QuizTakingScreen.kt`, `WhiteboardTool`, `Participant`?**
  _High betweenness centrality (0.166) - this node is a cross-community bridge._
- **Why does `brutalShadowColor()` connect `brutalBorderColor` to `AppNavHost`, `MainActivity`, `PracticeScreen.kt`, `AppUpdater`, `CommonComponents.kt`, `ChatScreen.kt`, `QuizTakingScreen.kt`, `WhiteboardTool`, `Participant`?**
  _High betweenness centrality (0.117) - this node is a cross-community bridge._
- **Why does `AppUpdateInfo` connect `AppUpdater` to `FirestoreRepository`, `FirestoreRepository.kt`?**
  _High betweenness centrality (0.103) - this node is a cross-community bridge._
- **What connects `ADMIN`, `STARCAST`, `STUDENT` to the rest of the system?**
  _122 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `brutalBorderColor` be split into smaller, more focused modules?**
  _Cohesion score 0.06701030927835051 - nodes in this community are weakly interconnected._
- **Should `LessonModel` be split into smaller, more focused modules?**
  _Cohesion score 0.0611764705882353 - nodes in this community are weakly interconnected._
- **Should `AppNavHost` be split into smaller, more focused modules?**
  _Cohesion score 0.09647058823529411 - nodes in this community are weakly interconnected._