# Release Notes — Vastavik Computers v1.0.23

**Release Date:** September 4, 2026  
**Build:** v1.0.23 (Production Release)  
**Platform:** Android (minSdk 24, targetSdk 35)

---

## 🚀 Key Highlights in this Release

### 1. High-Performance Vastavik AI Engine (`gemini-3.6-flash`)
- **Resolved Service Disruption:** Successfully fixed AI connection timeouts and HTTP 503 capacity errors by switching the primary backend model to Google `gemini-3.6-flash`.
- **Intelligent Fallback Architecture:** Implemented automatic, transparent fallback to `gemini-3.7-flash` if `gemini-3.6-flash` ever experiences temporary demand spikes or rate limits.
- **Identity & Safety Guardrails:** Maintained strict system instruction guards preserving the Vastavik AI identity (GLM 5.3 hosted on Vastavik VPS) while firmly declining off-domain or inappropriate student inputs.

---

### 2. Real-Time Admin Diagnostics Banner Box
- **Live Failure Root Cause Analysis:** When operating in Admin Mode, any failed AI call immediately displays a dedicated floating banner box above the bottom bar.
- **File & Model Provenance:** Clearly details the originating file (`VastavikAi.kt`), target model (`gemini-3.6-flash`), endpoint, and exact HTTP/JSON response text.
- **In-Chat Diagnostic Blocks:** On AI generation errors, admin chats render an informative diagnostic code block instead of generic failure messages for instant debugging.
- **Expandable Log Inspection:** Admins can expand the bottom banner to view a scrollable chronological history of all AI calls with timestamps, latency, and response status.

---

### 3. Student Info / Profile UI Refinements
- **Centered Avatar Layout:** Completely removed the duplicate chat icon from beside the student avatar picture, restoring a balanced, clean, and centered profile card.
- **Deep Green Header Action:** Updated the top-right Peer Chat chip in the student profile card to deep green (`#059669`) with white brutalist styling.
- **Unified Notification Theming:** Synced the profile top-bar notification button with the deep green (`#059669`) theme for visual harmony.

---

## 🛠 Fixes & Internal Changes
- Bumped `versionCode` to `23` and `versionName` to `"1.0.23"`.
- Added `app-v1.0.23-debug.apk` and `vastavikLearning-v1.0.23.apk` to `app/src/main/assets/`.
- Updated `AppUpdater.kt` fallback changelog viewer with v1.0.23 metadata.
