# Release Notes — Vastavik Computers v1.0.31

**Release Date:** September 6, 2026  
**Build:** v1.0.31 (Production Release)  
**Platform:** Android (minSdk 24, targetSdk 35)

---

## 🚀 Key Highlights in this Release

### 1. Code Editor Question Overview Dialog Overhaul
- **Structured 4-Section Problem Display:** When clicking the `?` question overview button in the Code Editor, problems are cleanly formatted and highlighted in 4 distinct sections:
  1. **Question:** Complete problem statement with topic and difficulty badges.
  2. **Explanation:** Deep conceptual breakdown, constraints, edge cases, and approach notes.
  3. **Input / Output:** Formatted representative input and expected output blocks.
  4. **Algorithm:** Step-by-step numbered algorithmic procedure.
- **Top Bar De-Cluttering:** Removed the top `Input (stdin)` text field and the `Generate New Problem via AI` bar from the main Code Editor view, providing an unobstructed coding surface.
- **Relocated AI Problem Generator Bar:** Moved the AI problem generator inside the bottom of the Question Overview dialog, equipped with:
  - An `OutlinedTextField` for custom challenge prompts or topics.
  - An inside-right microphone button for instant speech-to-text voice input.
  - A NeoBrutalistic Generate action button.
- **NeoBrutalistic Dynamic Run Stdin Dialog:** The Run FAB automatically checks code for interactive input statements (e.g. `Scanner`, `BufferedReader`, `input()`, `cin >>`, `readline`) or manual stdin pill toggle, prompting the student with an intuitive input dialog before sending execution to the compiler.

---

### 2. "Predict the Output" Comprehensive Suite
- **Multi-Question Solver (`PredictOutputSetScreen`):**
  - Interactive 5-question problem sets with `WindowInsets.safeDrawing` padding to prevent any overlap with Android status bar icons (battery, clock, network).
  - **Multi-Language Selector:** Instant switching between **Java**, **Python**, **C++**, and **JavaScript** with real-time code snippet conversion.
  - **3-Line Max Input Box:** Clean scrollable prediction input box strictly bounded to 3 lines.
  - **Mistral Small AI Verification:** Instant evaluation using Mistral Small (`mistral-small-latest`), rendering an accurate verdict, exact console output, step-by-step tracing, and a prominent bold green checkmark (`✔ PERFECT MATCH!`).
  - **Score Overview & Review:** Detailed question-by-question breakdown showing student prediction, actual output, status pill, and trace explanation.
- **Official 1-Inch Margin Review PDF (`PredictOutputPdf`):**
  - Strict 1-inch (72pt) safe margins on all 4 sides of standard A4 canvas.
  - Professional typography: Times New Roman serif for questions and code blocks, Roboto Slab serif for Mistral AI evaluation notes.
  - Instant generation and sharing via Android's secure `FileProvider`.
- **Integrated Practice Hub:**
  - One-tap "Solve Set" for opening the complete multi-question exam set solver with score summary, guaranteed exact outputs, full step-by-step line & function execution traces, and 1-inch margin PDF export.

---

## 🛠 Technical Details & Artifacts
- **Version Code:** 31
- **Version Name:** 1.0.31
- **Target SDK:** 35 (Android 15)
- **Minimum SDK:** 24 (Android 7.0)
- **Platform:** Android
- **Architecture:** Universal APK
- **Asset Packages:**
  - `vastavikLearning-v1.0.31.apk`
