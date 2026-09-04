# Release Notes — Vastavik Computers v1.0.24

**Release Date:** September 4, 2026  
**Build:** v1.0.24 (Production Release)  
**Platform:** Android (minSdk 24, targetSdk 35)

---

## 🚀 Key Highlights in this Release

### 1. App Update UI: Two-Way Swipe Navigation
- **Horizontal Pager Integration:** Added fluid horizontal swipe gestures across the App Update screen powered by Jetpack Compose `HorizontalPager`.
- **Bidirectional Screen Transition:** Swipe left from **Latest Update** to inspect full **All Updates History**, and swipe right to return to the active update card.
- **Synchronized Neo-Brutalist Tabs:** Pinned sticky header tabs dynamically reflect the active page state with smooth animated transitions.

---

### 2. Compact Changelog Preview with "Read More" Toggle
- **Platform-Level Truncation:** Release notes across both update tabs are truncated right at the `Platform` specification line by default.
- **Expandable Read More / Read Less:** A tap on "Read More ▼" expands the full release notes, and "Read Less ▲" safely collapses it back.
- **Optimal Button Visibility:** Eliminates extreme vertical overflow, guaranteeing the **"Download & Install Update"** and **"Later"** action buttons remain visible on-screen without requiring excessive scrolling.

---

### 3. Download Cancellation & APK Integrity Guard
- **Strict Package Verification:** Integrated Android `PackageManager.getPackageArchiveInfo()` validation directly into `hasUsableApk()`.
- **Automatic Partial File Purging:** Completely eliminated the bug where cancelling a download left partial data (e.g. 1MB) and falsely displayed "Install Update Now". Partial files are automatically purged from storage immediately upon cancel.
- **Deterministic Action Button:** When a download is cancelled or interrupted, the button reliably resets to **"Download & Install Update"** in primary blue.

---

### 4. Notification Improvements & Drawer Actions
- **Standardized Notification Title:** Updated the ongoing download notification title to:
  ```
  Downloading Vastavik v1.0.24 Update
  ```
- **Instant Drawer Cancellation:** Tapping "Cancel" directly inside the Android system notification drawer immediately halts the network stream, dismisses the notification, wipes partial APK storage, and updates the in-app screen state.

---

### 5. High-Performance Vastavik AI Engine & Admin Diagnostics
- **Gemini 3.6 Flash Engine:** Powered by Google `gemini-3.6-flash` with instant `gemini-3.7-flash` fallback for 100% uptime and zero 503 capacity delays.
- **Floating Admin Error Banner Box:** Real-time floating diagnostic banner in Admin Mode detailing file origin (`VastavikAi.kt`), target model, and HTTP response traces on any AI generation failure.
- **Clean Student Info Profile Layout:** Centered student avatar picture and updated action icons to deep emerald green (`#059669`).

---

## 🛠 Technical Details & Artifacts
- **Version Code:** 24
- **Version Name:** 1.0.24
- **Target SDK:** 35 (Android 15)
- **Minimum SDK:** 24 (Android 7.0)
- **Platform:** Android
- **Architecture:** Universal APK
- **Asset Packages:**
  - `vastavikLearning-v1.0.24.apk`
  - `vastavikLearning.apk`
  - `app-v1.0.24-debug.apk`
  - `app-debug.apk`
