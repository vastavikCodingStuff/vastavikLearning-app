# Release Notes — Vastavik Computers v1.0.26

**Release Date:** September 4, 2026  
**Build:** v1.0.26 (Production Release)  
**Platform:** Android (minSdk 24, targetSdk 35)

---

## 🚀 Key Highlights in this Release

### 1. Minimalist AI Model Tags: GOD, Demi-God, and Human AI
- **Header Removal & Pure Tags:** Completely eliminated repetitive and overflowing heading text (`"Mistral is GOD"`, `"Gemini 3.7 flash is Demi-god"`, `"Gemini 3.6 flash is Human AI"`) in the AI selection dialog. Replaced with crisp, prominent colored tags:
  - 👑 **`GOD`** (Amber tag)
  - ⚡ **`Demi-God`** (Royal Blue tag)
  - 👤 **`Human AI`** (Emerald Green tag)
- **Top Bar Model Picker:** Top search/picker box on the chat screen now displays only the model TAG chip and dropdown arrow for a clean, modern aesthetic without any text overflow.
- **Task-Oriented Subtitles:** Removed underlying model provider names (`Mistral Small •`, `Google Gemini 3.7 Flash •`, `Google Gemini 3.6 Flash •`). The subtitle lines now focus purely on performance tasks:
  - **GOD:** `Best and accurate performance`
  - **Demi-God:** `High speed & reasoning`
  - **Human AI:** `Compact everyday AI`

---

### 2. Elimination of Badge Stretching & Vertical Word-Wrap Bug
- **Fixed Layout Constraints:** Removed cramped text rows that caused tags in selected cards to stretch vertically into single-character columns (`D-e-m-i-g-o-d` / `H-u-m-a-n A-I`). Tags are now rendered cleanly in their own dedicated containers with ample horizontal breathing room.

---

### 3. Screen Dimming & Background Color Fix
- **No Background Color Tinting:** Removed the grey/dim overlay when opening the Chats history drawer (`showSidebar`). The main chat screen stays bright, clean, and retains its true background color without turning dark.
- **Touch-Outside Dismissal:** Tapping the transparent region outside the sidebar smoothly dismisses the drawer without any ripple or visual distortion.

---

### 4. Intuitive Two-Stage Left-Scroll Navigation
- **First Left-Scroll / Swipe:** If the Chats history drawer is open, swiping left or right immediately dismisses and removes the Chats dialog.
- **Second Left-Scroll / Swipe:** Once the Chats dialog is dismissed, swiping smoothly navigates directly to the **Practice** screen (`onNavigate("practice")`).
- **Omnipresent Drag Detection:** Horizontal drag gestures are detected consistently whether touching the sidebar, the transparent overlay, or the main chat canvas.

---

## 🛠 Technical Details & Artifacts
- **Version Code:** 26
- **Version Name:** 1.0.26
- **Target SDK:** 35 (Android 15)
- **Minimum SDK:** 24 (Android 7.0)
- **Platform:** Android
- **Architecture:** Universal APK
- **Asset Packages:**
  - `vastavikLearning-v1.0.26.apk`
  - `vastavikLearning.apk`
  - `app-v1.0.26-debug.apk`
  - `app-debug.apk`
