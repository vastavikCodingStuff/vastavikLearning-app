# 🚀 Vastavik Learning App (Android)

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Platform-Android%20(SDK%2024--35)-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%26%20Material3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2F%20MVVM-FF6F00?style=for-the-badge)
![Style](https://img.shields.io/badge/Design-Neo--Brutalist%20UI-FFDD00?style=for-the-badge&labelColor=000000)

**A high-performance, interactive educational platform built with modern Jetpack Compose, featuring Neo-Brutalist design, real-time live classrooms, code execution, OCR exercise scanning, and AI-powered tutoring.**

</div>

---

## 📖 Overview

**Vastavik Learning App** is the student-facing Android client of the Vastavik education ecosystem. Built from the ground up using 100% Kotlin and Jetpack Compose, it brings together academic curriculum (ICSE, CBSE, and collegiate computer science), interactive coding environments, live video classrooms with real-time whiteboards, and intelligent AI study assistance—all wrapped in a bold, vibrant **Neo-Brutalist** design aesthetic.

---

## ✨ Key Features

### 🎨 Neo-Brutalist UI & Design System
- High-contrast color palettes, bold black outlines, hard offset drop shadows, and tactile button presses.
- Custom brutalist component suite: `BrutalistCard`, `BrutalistButton`, `BrutalistTextField`, `BrutalistTopBar`, etc.
- Responsive layouts engineered for fluid mobile navigation and distraction-free learning.

### 📚 Structured Learning Paths & Courses
- Interactive syllabus hierarchy with **Courses ➔ Parts ➔ Subparts ➔ Lessons**.
- Embedded video lessons with integrated YouTube playback and rich Markdown/HTML study notes.
- Lesson completion tracking, daily learning streaks, and gamified progress indicators.

### 💻 In-App Code Editor & OCR Exercises
- Syntax-highlighted code editor for practicing programming languages (Java, Python, C++, etc.).
- **OCR Exercise Scanning**: Scan handwritten or printed problem statements via camera/MLKit to instantly populate editor code templates.
- Coding challenges, PYQ (Previous Year Questions) archive, and downloadable solution sets.

### 🎙️ Real-Time Live Classes & Interactive Whiteboard
- Live meeting rooms powered by WebSockets / WebRTC signaling.
- Synchronized collaborative **Whiteboard** with brush sizing, multi-color palette, undo/redo, and live educator annotations.
- In-meeting participant list, live chat, raising hands, and screen-sharing support.

### 🤖 AI Study Assistant
- Multi-model intelligent tutoring integration (Google Gemini & Mistral AI).
- Context-aware code debugging, doubt clearing, and instant step-by-step concept explanations.
- **Mistral Problem Overviews**: Line-by-line code generation with integrated caching to prevent duplicate API calls and reduce latency.

### 📝 Quiz & Assessment Engine
- Dynamic timed quizzes categorized by board, subject, and difficulty (MCQs and open-ended queries).
- Instant review screens with score breakdowns, detailed explanations, and performance metrics.

### 🔄 In-App OTA Update System
- Self-hosted automatic update check and background APK downloader (`AppUpdater`).
- Direct in-app installation with release notes and mandatory update enforcement options.

### 🔐 Security & Secure API Integration
- Backend API authentication signed with HMAC-SHA256 request headers.
- Firebase Authentication (Email/Password, Google Sign-in) & Firestore cloud synchronization.
- Admin dashboard session controls and role-based access for privileged operations.

---

## 🏗️ Architecture & Tech Stack

```
com.vastavik.computer
├── data
│   ├── api          # Retrofit service, HMAC signature generator, AuthInterceptor
│   ├── model        # Domain models (Course, Quiz, User, Meeting, AppUpdateInfo)
│   ├── realtime     # WebSocket / WebRTC meeting clients & signaling
│   └── repository   # AuthRepository, FirestoreRepository, VastavikApiRepository
├── di               # Hilt Dependency Injection modules
├── ui
│   ├── components   # Neo-Brutalist reusable UI widgets, Whiteboard, Video Player
│   ├── navigation   # Jetpack Navigation Compose graph & deep links
│   ├── screens      # Auth, Home, Learning, Meeting, Editor, Quiz, Profile
│   └── theme        # Brutalism color definitions, typography, shapes
└── utils            # AppUpdater, AdminSession, Constants, Helpers
```

- **Language:** Kotlin 2.0+
- **UI Framework:** Jetpack Compose + Material 3
- **Dependency Injection:** Hilt (Dagger)
- **Asynchronous / Reactive:** Kotlin Coroutines & StateFlow / SharedFlow
- **Networking:** Retrofit 2, OkHttp 3, Kotlinx Serialization
- **Cloud & Auth:** Firebase Authentication, Cloud Firestore, Firebase Cloud Messaging (FCM)
- **Vision & Media:** Google MLKit (Text Recognition), CameraX, Media3 / ExoPlayer, YouTube Android Player
- **Real-Time Communication:** WebSockets / WebRTC

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio:** Ladybug (2024.2.1+) or newer
- **JDK:** Java 17 or higher
- **Android SDK:** Compile SDK 35, Min SDK 24 (Android 7.0+)

### Setup Instructions

1. **Clone the repository:**
   ```bash
   git clone https://github.com/vastavikCodingStuff/vastavikLearning-app.git
   cd vastavikLearning-app
   ```

2. **Add `google-services.json`:**
   - Place your Firebase `google-services.json` file inside the `app/` directory:
     ```
     vastavikLearning-app/
     └── app/
         └── google-services.json
     ```

3. **Configure `local.properties`:**
   Add backend connection and API keys to `local.properties` in the project root:
   ```properties
    # API Keys
    GEMINI_API_KEY=your_gemini_api_key_here
    # MISTRAL_API_KEY no longer used (Vastavik AI is powered by Google Gemini)
    # Judge0 self-hosted instance (Code Editor → "Run" button)
    JUDGE0_AUTH_TOKEN=your_judge0_auth_token_here

    # Backend Endpoint Configuration
    BACKEND_BASE_URL=https://vastavik-admin-backend.onrender.com
    API_KEY_ID=android-prod
    API_KEY_SECRET=your_hmac_secret_here
   ```

4. **Build & Run:**
   - Open the project in Android Studio.
   - Sync Gradle files.
   - Run on an emulator or connected physical device (`app` configuration).

   Or build via terminal:
   ```bash
   # Debug APK
   ./gradlew assembleDebug

   # Release APK
   ./gradlew assembleRelease
   ```

---

## 📄 License

This repository is proprietary software developed for the **Vastavik Education** platform. All rights reserved.

