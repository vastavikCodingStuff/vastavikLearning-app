# Release Notes — Vastavik Computers v1.0.32

**Release Date:** September 6, 2026  
**Build:** v1.0.32 (Production Release)  
**Platform:** Android (minSdk 24, targetSdk 35)

---

## 🚀 Key Highlights in this Release

### 1. Code Editor Question Overview & Synchronization Overhaul
- **Full Problem Synchronization from Practice:** Fixed the critical issue where clicking Open in Editor on practice coding solutions (such as *Two Sum*) loaded the source code but left the ? button disabled with Question isn't available over here!. The entire generated solution is now seamlessly synchronized.
- **In-Memory Cross-Screen Bridge (CodeEditorSharedState):** Implemented a dedicated high-capacity in-memory data bridge to transfer code, language, and structured question payloads across screens without hitting Android Jetpack Navigation Compose URL length limits or URL-encoding corruption.
- **Always-Accessible ? Dialog:** Removed the restrictive disabled state and toast lockout on the ? icon in the TopAppBar. Clicking ? now always opens the Question Overview dialog.
- **Structured 4-Section Problem Architecture:** When viewing any question in the dialog, it is presented in 4 distinct high-contrast NeoBrutalistic cards:
  1. **Question:** Complete challenge prompt, constraints, topic, and difficulty pill.
  2. **Explanation:** Comprehensive conceptual logic, algorithmic approach, and constraints.
  3. **Input / Output:** Concrete sample test cases with expected outputs.
  4. **Algorithm:** Clear numbered step-by-step algorithmic steps.
- **Smart Automatic Problem Derivation (deriveQuestionFromCode):** If code is opened or typed without an explicit question, the editor automatically inspects class declarations, comments, and function signatures (e.g. public class TwoSum → Two Sum) and dynamically synthesizes the 4-part structured challenge.
- **De-Cluttered Top Bar:** Removed the top Input (stdin) text field and the Generate AI expander bar from the editor screen, providing an unobstructed coding canvas.
- **Relocated AI Problem Generator:** Placed directly at the bottom of the Question Overview dialog (height expanded to 0.84f), featuring an input text box with an embedded microphone icon on the right for voice input (matching AI Chat) and a NeoBrutalistic Generate button.
- **Runtime Interactive Stdin Dialog:** When tapping Run, the editor automatically checks if the code uses input methods (Scanner, BufferedReader, input(), cin, eadline) or manual stdin, prompting the user with an intuitive NeoBrutalistic stdin dialog before running.

---

### 2. Predict the Output Comprehensive Suite & Practice Clean-up
- **Removed Try Item:** Completely eliminated the redundant Try Item button from the Practice screen cards, streamlining the interface.
- **Canonical Execution & Step-by-Step Line Traces:**
  - Implemented deep step-by-step execution traces for all Predict Output questions, breaking down:
    1. **Variables & Data Types** (initial states and scopes)
    2. **Line-by-Line & Function Execution Tracing** (loops, conditions, recursion stacks, operator precedence)
    3. **Final Exact Console Output**
  - Resolved submission evaluations: questions are now evaluated with 100% populated actual outputs and full trace explanations (zero N/A outputs).
- **Official 1-Inch Margin Review PDF (PredictOutputPdf):**
  - Implemented strict 1-inch (72pt) safe margins on all 4 sides of the standard A4 page.
  - Question review cards display question snippets, student answers, correct expected outputs, and complete step-by-step line/function traces.

---

### 3. In-App Update Download & Completion Experience
- **100% Download State:** Once the update download reaches 100%, the progress bar is removed.
- **Completion Header:** The title smoothly transitions to Done downloading Vastavikv1.0.32 Update.
- **Instant Install Now Action:** The Cancel button is replaced by a high-visibility, primary Install Now button for immediate 1-tap package installation.

---

## 🛠 Technical Details & Artifacts
- **Version Code:** 32
- **Version Name:** 1.0.32
- **Target SDK:** 35 (Android 15)
- **Minimum SDK:** 24 (Android 7.0)
- **Platform:** Android
- **Architecture:** Universal APK
- **Release Assets:**
  - `vastavikLearning-v1.0.32.apk`
