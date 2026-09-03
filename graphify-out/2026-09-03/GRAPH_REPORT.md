# Graph Report - vastavikLearning-app  (2026-09-03)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 627 nodes · 1154 edges · 36 communities (32 shown, 4 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 13 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `18206a7e`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- AppNavHost
- AppModule.kt
- CommonComponents.kt
- HomeScreen.kt
- Resource
- LearningViewModel
- ChatScreen.kt
- PracticeViewModel
- MainActivity
- Extensions.kt
- MeetingViewModel
- MeetingEvent
- QuizTakingScreen.kt
- PracticeScreen.kt
- WhiteboardTool
- MeetingModels.kt
- MeetingComponents.kt
- VastavikYouTubePlayer.kt
- DeviceSecurityChecker
- ChatModel.kt
- SubscriptionModel.kt
- AuthViewModel
- DisabledFeature
- ElementType
- ChatViewModel
- OcrExerciseScreen.kt
- MeetingChatPanel
- NotificationDismissReceiver.kt
- VastavikApplication
- Constants.kt
- Android application for students
- Color
- InClassScreen

## God Nodes (most connected - your core abstractions)
1. `AppNavHost()` - 36 edges
2. `MeetingViewModel` - 34 edges
3. `neoShape()` - 32 edges
4. `MeetingEvent` - 21 edges
5. `VastavikColors` - 18 edges
6. `neoCircleShape()` - 16 edges
7. `WhiteboardTool` - 15 edges
8. `Resource` - 14 edges
9. `AuthViewModel` - 13 edges
10. `OnboardingViewModel` - 12 edges

## Surprising Connections (you probably didn't know these)
- `NeoBrutalistWhiteboard()` --calls--> `Point`  [INFERRED]
  app/src/main/java/com/vastavik/computer/ui/components/Whiteboard.kt → app/src/main/java/com/vastavik/computer/data/model/MeetingModels.kt
- `MeetingViewModel` --references--> `WhiteboardState`  [EXTRACTED]
  app/src/main/java/com/vastavik/computer/ui/screens/meeting/MeetingViewModel.kt → app/src/main/java/com/vastavik/computer/data/model/MeetingModels.kt
- `NeoBrutalistWhiteboard()` --references--> `Viewport`  [EXTRACTED]
  app/src/main/java/com/vastavik/computer/ui/components/Whiteboard.kt → app/src/main/java/com/vastavik/computer/data/model/MeetingModels.kt
- `InClassScreen()` --calls--> `NeoBrutalistWhiteboard()`  [INFERRED]
  app/src/main/java/com/vastavik/computer/ui/screens/meeting/MeetingScreens.kt → app/src/main/java/com/vastavik/computer/ui/components/Whiteboard.kt
- `InClassScreen()` --calls--> `MeetingControlBar()`  [INFERRED]
  app/src/main/java/com/vastavik/computer/ui/screens/meeting/MeetingScreens.kt → app/src/main/java/com/vastavik/computer/ui/components/MeetingComponents.kt

## Import Cycles
- None detected.

## Communities (36 total, 4 thin omitted)

### Community 0 - "AppNavHost"
Cohesion: 0.08
Nodes (42): LessonModel, ClassSession, ClassLobbyCard(), AppNavHost(), ForgotPasswordScreen(), LoginScreen(), SignupScreen(), SplashScreen() (+34 more)

### Community 1 - "AppModule.kt"
Cohesion: 0.06
Nodes (21): DocumentSnapshot, UserModel, AppModule, Context, StateFlow, ViewModel, OnboardingViewModel, ViewModel (+13 more)

### Community 2 - "CommonComponents.kt"
Cohesion: 0.10
Nodes (36): appendFormattedText(), BottomNavItem(), ButtonVariant, Error, Outlined, Primary, Secondary, ChatBubble() (+28 more)

### Community 3 - "HomeScreen.kt"
Cohesion: 0.12
Nodes (31): PromoData, PromoPopup(), UnderDevelopmentBanner(), UnderDevelopmentData, Modifier, VastavikTopBar(), BottomNavBar(), ContinueLearningCard() (+23 more)

### Community 4 - "Resource"
Cohesion: 0.08
Nodes (15): FirebaseMessagingService, Intent, MeetingForegroundService, MeetingNotificationManager, Error, T, Loading, Resource (+7 more)

### Community 5 - "LearningViewModel"
Cohesion: 0.08
Nodes (14): BannerModel, DocumentSnapshot, PopularTopicModel, StudentSelection, CourseModel, DocumentSnapshot, PartModel, SubpartModel (+6 more)

### Community 6 - "ChatScreen.kt"
Cohesion: 0.12
Nodes (27): DuolingoPath(), Dp, Modifier, PathConnector(), PathNode(), PathNodeData, PathNodeState, Completed (+19 more)

### Community 7 - "PracticeViewModel"
Cohesion: 0.11
Nodes (11): CodingChallenge, DocumentSnapshot, PYQModel, QuizModel, QuizQuestion, TestCase, StateFlow, ViewModel (+3 more)

### Community 8 - "MainActivity"
Cohesion: 0.11
Nodes (10): Intent, MainActivity, Flow, ViewModel, SettingsViewModel, VastavikTheme(), Flow, ThemePreferences (+2 more)

### Community 9 - "Extensions.kt"
Cohesion: 0.09
Nodes (6): Chunked(), DebouncedState(), Context, T, openInBrowser(), shareText()

### Community 10 - "MeetingViewModel"
Cohesion: 0.10
Nodes (4): StateFlow, ViewModel, MeetingViewModel, ConnectionState

### Community 11 - "MeetingEvent"
Cohesion: 0.10
Nodes (21): AssignStarCast, ChatMessageSent, ClassStarted, EmojiReaction, FeatureToggle, Join, KickParticipant, Leave (+13 more)

### Community 12 - "QuizTakingScreen.kt"
Cohesion: 0.23
Nodes (15): AnnotatedString, QuizManager, QuizQuestionData, callMistralBrief(), containsCode(), androidx, Context, openPdf() (+7 more)

### Community 13 - "PracticeScreen.kt"
Cohesion: 0.25
Nodes (16): CodingCard(), CodingContent(), CodingItem, Modifier, MCQCard(), MCQContent(), MCQItem, PracticeScreen() (+8 more)

### Community 14 - "WhiteboardTool"
Cohesion: 0.17
Nodes (14): Color, Modifier, NeoBrutalistWhiteboard(), WhiteboardTool, ARROW, ELLIPSE, ERASER, HAND (+6 more)

### Community 15 - "MeetingModels.kt"
Cohesion: 0.18
Nodes (9): AuditLogEntry, MediaState, OFF, ON, Point, Rect, Viewport, WhiteboardElement (+1 more)

### Community 16 - "MeetingComponents.kt"
Cohesion: 0.27
Nodes (12): Participant, ParticipantRole, ADMIN, STARCAST, STUDENT, ControlButton(), ImageVector, Modifier (+4 more)

### Community 17 - "VastavikYouTubePlayer.kt"
Cohesion: 0.26
Nodes (8): androidx, Modifier, VastavikYouTubePlayer(), AbstractYouTubePlayerListener, LifecycleEventObserver, Lifecycle, PlayerConstants, YouTubePlayer

### Community 18 - "DeviceSecurityChecker"
Cohesion: 0.32
Nodes (5): SecurityCheckScreen(), SecurityIssueCard(), DeviceSecurityChecker, Context, SecurityIssue

### Community 19 - "ChatModel.kt"
Cohesion: 0.24
Nodes (4): ChatMessage, ChatSession, DocumentSnapshot, NoteModel

### Community 20 - "SubscriptionModel.kt"
Cohesion: 0.24
Nodes (4): DocumentSnapshot, SubscriptionModel, SubscriptionPlan, TransactionModel

### Community 21 - "AuthViewModel"
Cohesion: 0.22
Nodes (3): AuthUiState, AuthViewModel, ViewModel

### Community 22 - "DisabledFeature"
Cohesion: 0.22
Nodes (9): DisabledFeature, CAMERA, CAPTIONS, CHAT, EMOJI, MIC, RAISE_HAND, RECORDING (+1 more)

### Community 23 - "ElementType"
Cohesion: 0.25
Nodes (8): ElementType, ARROW, ELLIPSE, ERASER, LINE, PEN, RECTANGLE, TEXT

### Community 24 - "ChatViewModel"
Cohesion: 0.39
Nodes (4): ChatViewModel, ChatMessage, StateFlow, ViewModel

### Community 25 - "OcrExerciseScreen.kt"
Cohesion: 0.43
Nodes (5): imageProxyToBitmap(), OnImageCapturedCallback, Bitmap, ImageCaptureException, ImageProxy

### Community 26 - "MeetingChatPanel"
Cohesion: 0.38
Nodes (5): LiveChatMessage, ReplyPreview, ChatBubble(), formatTime(), MeetingChatPanel()

### Community 27 - "NotificationDismissReceiver.kt"
Cohesion: 0.53
Nodes (4): Context, Intent, NotificationDismissReceiver, BroadcastReceiver

### Community 35 - "InClassScreen"
Cohesion: 0.60
Nodes (4): BrutalStudentTile(), InClassScreen(), androidx, SquareTopButton()

## Knowledge Gaps
- **66 isolated node(s):** `SyntaxColors`, `AssignStarCast`, `ChatMessageSent`, `ClassStarted`, `EmojiReaction` (+61 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **4 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `AppNavHost()` connect `AppNavHost` to `InClassScreen`, `HomeScreen.kt`, `ChatScreen.kt`, `MainActivity`, `QuizTakingScreen.kt`, `PracticeScreen.kt`, `DeviceSecurityChecker`?**
  _High betweenness centrality (0.198) - this node is a cross-community bridge._
- **Why does `MainActivity` connect `MainActivity` to `Resource`?**
  _High betweenness centrality (0.110) - this node is a cross-community bridge._
- **Why does `MeetingViewModel` connect `MeetingViewModel` to `AppNavHost`, `InClassScreen`, `WhiteboardTool`, `MeetingModels.kt`, `MeetingComponents.kt`, `MeetingChatPanel`?**
  _High betweenness centrality (0.092) - this node is a cross-community bridge._
- **What connects `SyntaxColors`, `AssignStarCast`, `ChatMessageSent` to the rest of the system?**
  _66 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `AppNavHost` be split into smaller, more focused modules?**
  _Cohesion score 0.08134920634920635 - nodes in this community are weakly interconnected._
- **Should `AppModule.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.058823529411764705 - nodes in this community are weakly interconnected._
- **Should `CommonComponents.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.10256410256410256 - nodes in this community are weakly interconnected._