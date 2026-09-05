# Release Notes — Vastavik Computers v1.0.29

**Release Date:** September 6, 2026  
**Build:** v1.0.29 (Production Release)  
**Platform:** Android (minSdk 24, targetSdk 35)

---

## 🚀 Key Highlights in this Release

### 1. Quiz PDF Safe-Area Formatter & Typography Overhaul
- **1-Inch Safe Margin Guarantee:** Standardized A4 PDF generation with a strict 72-point (1-inch) margin on all four boundaries (72pt left, 523pt right, 72pt top, 760pt bottom), preventing any text clipping horizontally or vertically.
- **Accurate Font Pairings:** Questions and multiple-choice options are formatted with classic **Times New Roman**, while AI step-by-step review explanations utilize Google Font **Roboto Slab**.
- **Dynamic Text Wrapping & Multi-Page Breaks:** Added mathematical text measurement and wrapping logic that automatically calculates vertical line offsets and breaks to new framed pages before overflowing page margins.

---

### 2. Background GenZ Teacher Notifications & Duplicate Alert Suppression
- **WorkManager Engagement Worker:** Added `VastavikEngagementWorker` running periodically in the background even when the app is completely closed or killed, delivering rotating GenZ-tone study motivation prompts for daily quizzes, output prediction, and coding streaks.
- **Duplicate Alert Suppression:** Suppressed floating in-app popup banners (`TelegramNotificationManager.showUpdateAlert`) when an Android system notification is posted, eliminating dual notification clutter.
- **Boot Persistence:** Added `RECEIVE_BOOT_COMPLETED` permission and periodic WorkManager constraints so study notifications persist after device reboots.

---

### 3. Predict the Output In-Place NeoBrutalistic Dialog
- **Modal Code Workspace:** Tapping "Solve Set" in Predict the Output now opens an in-place NeoBrutalistic dialog right inside Practice Screen without navigating away.
- **Syntax-Highlighted Code:** Code snippets are highlighted with language tokens (keywords, numbers, strings, comments).
- **Mistral AI Verification:** Students enter their predicted output and click "Verify Output", triggering Mistral AI to evaluate accuracy, show the actual program output, and give a comprehensive step-by-step variable trace.

---

### 4. Code Editor: Voice Prompting & 4-Part Structured Problem Generator
- **Dynamic Question Indicator:** The top-right `?` icon is grayed out when no problem is loaded, displaying an instant toast *"Question isn't available over here!"* upon tap.
- **Voice & Text Problem Generator:** Built a NeoBrutalistic AI Problem Generator bar equipped with speech-to-text mic button and prompt input.
- **Strict 4-Part Structure:** Generated problems follow a standardized curriculum structure: **1. Question**, **2. Explanation**, **3. Input / Output**, **4. Algorithm**, beautifully styled with colored badges and NeoBrutalistic cards.

---

### 5. Board PYQs with Authentic Color Banners & AI Search
- **Official Board Color Banners:** Distinct, prominent color banners with high-contrast white text:
  - **ICSE:** Cobalt Blue (`#1E40AF`)
  - **ISC:** Royal Purple (`#6D28D9`)
  - **CBSE:** Crimson Red (`#B91C1C`)
  - **WB Board:** Emerald Green (`#047857`)
- **Class 10 & 12 Filter:** Instant tab toggle between Class 10 and Class 12 board questions.
- **AI Paper Search & Direct Solve:** Search any year, topic, or chapter using voice or text via Mistral AI, view official marking schemes, and tap "Solve in Editor" to immediately launch into the Code Editor.

---

### 6. AI Engine Tiering & AI Chat Architecture Fixes
- **Tiered AI Engines:**
  - **GOD:** Mistral Large (`mistral-large-latest`) — Peak intelligence & deep reasoning.
  - **Demi-God:** Mistral Medium (`mistral-medium-latest`) — High-speed balanced reasoning.
  - **Human AI:** Mistral Small (`mistral-small-latest`) — Lightweight, everyday answers.
- **Sidebar Touch Interception Fix:** Reordered chat sidebar layers so tapping chat history items or delete menus is never swallowed by background scrims.
- **Immediate Disk Persistence:** All AI chats and user messages persist instantly to disk cache (`AiConversationCache`).

---

## 🛠 Technical Details & Artifacts
- **Version Code:** 29
- **Version Name:** 1.0.29
- **Target SDK:** 35 (Android 15)
- **Minimum SDK:** 24 (Android 7.0)
- **Platform:** Android
- **Architecture:** Universal APK
- **Asset Packages:**
  - `vastavikLearning-v1.0.29.apk`
