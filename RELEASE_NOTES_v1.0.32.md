# Release Notes — Vastavik Computers v1.0.32

**Release Date:** September 6, 2026  
**Build:** v1.0.32 (Production Release)  
**Platform:** Android (minSdk 24, targetSdk 35)

---

## 🚀 Key Highlights in this Release

### 1. Seamless Question Sync in Code Editor & Lockout Elimination
- **Guaranteed Question Availability:** Fixed the issue where clicking Open in Editor from a practice coding solution (e.g., Two Sum) loaded the code but left the ? button disabled with Question isn't available over here!.
- **CodeEditorSharedState Architecture:** Introduced a dedicated in-memory state bridge that reliably transfers source code, active language, and rich 4-part structured questions between screens (Practice, PYQ, and Chat) without hitting Jetpack Navigation URL length limits or URL-encoding corruption.
- **Always-Accessible Question Dialog:** Removed the restrictive disabled state and toast lockout on the ? icon in the TopAppBar. Clicking ? now always opens the Question Overview dialog.
- **Smart Problem Derivation from Source Code:** If code is opened or typed without an explicit question, deriveQuestionFromCode automatically extracts the problem title from class declarations, function names, or leading comments (e.g., public class TwoSum → Two Sum) and provides the complete 4-part structured breakdown.
- **Reactive State Lifecycle:** Added reactive LaunchedEffect synchronization for initialCode, initialLanguage, and initialQuestion so the editor instantly rebinds to new problems even when the screen is retained in the navigation backstack.

### 2. Practice & Predict Output Enhancements
- **Cleaned UI:** Removed the redundant Try Item button from the Practice tab.
- **Predict Output Deep Traces:** Guaranteed step-by-step line, function, and type traces for all predict-the-output evaluations.
- **Official 1-Inch Margin PDF Reports:** Enhanced PredictOutputPdf to render comprehensive question review cards including student answer, correct output, and full execution traces.

---

## 🛠 Technical Details & Artifacts
- **Version Code:** 32
- **Version Name:** 1.0.32
- **Target SDK:** 35 (Android 15)
- **Minimum SDK:** 24 (Android 7.0)
- **Platform:** Android
- **Architecture:** Universal APK
- **Asset Packages:**
  - astavikLearning-v1.0.32.apk
