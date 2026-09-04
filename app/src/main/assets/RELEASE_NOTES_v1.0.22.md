# Release Notes — Vastavik Computers v1.0.22

**Release Date:** September 4, 2026  
**Build:** v1.0.22 (Production Release)  
**Platform:** Android (minSdk 24, targetSdk 34)

---

## 🚀 Key Highlights in this Release

### 1. Unified "Vastavik AI" Engine (`gemini-3.6-flash`)
- **Reliable AI Integration:** Switched underlying AI model to `gemini-3.6-flash` for high throughput, low latency, and zero connection timeouts.
- **Consistent AI Branding:** Rebranded all assistant flows, chat headers, bot badges, and practice question generators to **Vastavik AI** (built on GLM 5.3 and running on Vastavik's dedicated VPS).
- **Strict Content Safety Guard:** Always-on system instructions ensure off-topic, explicit, or inappropriate requests are politely rejected with concise student-friendly redirects.
- **Simplified Error Messaging:** User-facing network or generation errors are cleanly reported as `"Error Connecting to the Server"`.
- **Admin Debug Log Banner:** Floating debug overlay (visible only in Admin mode) displays real-time execution logs, response times, HTTP statuses, and error traces for all AI calls.

---

### 2. Code Execution via Judge0 Server (`http://139.84.172.230:2358`)
- **Native Live Code Runner:** Replaced simulated code runner with a direct synchronous Judge0 integration (`wait=true`).
- **Standard Input (`stdin`) Support:** Added an interactive `Input (stdin)` text field in the Code Editor to support interactive programs (e.g., Java `Scanner`, Python `input()`).
- **Multi-Language Support:**
  - Java (OpenJDK): Language ID `62`
  - Python (3.x): Language ID `71`
  - C++ (GCC): Language ID `54`
  - JavaScript (Node.js): Language ID `63`
- **Execution Metadata & Status:** Displays runtime statistics (`time`, `memoryKb`) and status chips (`Accepted`, `Compilation Error`, `Time Limit Exceeded`, `Runtime Error`).
- **Authenticated Access:** Transmits the required `X-Auth-Token` header for secure communication with the self-hosted Judge0 VPS.

---

### 3. Practice Screen & Coding UI Improvements
- **Coding Card Options Menu:** Replaced scattered inline action buttons with a clean **Problem Overview** button alongside a 3-dot (`MoreVert`) context menu.
- **3-Dot Context Menu Options:**
  - **Editor (`<>` Icon):** Opens the live Code Editor pre-filled with the question title.
  - **Delete (Trash Icon & Red Text):** Removes custom AI-generated questions from the list and clears cached solutions.

---

### 4. Home Screen Paged Banner Overlay
- **Unified Swipable Banner:** Combined the "50% OFF Premium" promo popup and "App Under Development" message into a single paged overlay.
- **Controls & Interaction:**
  - Swipe left/right or use small arrow navigation buttons to switch pages.
  - Page indicator dots highlight active banner.
  - Tapping anywhere outside the banner card dismisses the overlay cleanly.

---

### 5. Student Profile & App Updates
- **Profile Header Chat Chip:** Removed duplicate inline chat box next to the avatar. Added a circular deep-green (`#10B981`) **Peer Chat** button pinned to the top-right of the student card header.
- **Splash Screen Optimization:** Removed background update checks and redirect logic from the Splash Screen to guarantee instant, seamless app launch.
- **Direct App Update "Later" Navigation:** Tapping "Later" on the App Update screen directly returns the student to the Home Screen.

---

## 🛠 Fixes & Technical Refactoring
- Removed all legacy `Mistral Small` API calls, models, configuration keys, and cache files (`MistralDiskCache` → `VastavikAiDiskCache`).
- Fixed Kotlin compiler escape sequences in `PracticeScreen.kt`.
- Updated `build.gradle.kts` fields and `local.properties` documentation.
- Ensured 100% build compatibility with JDK 24.
