# Vastavik CodeOSS Extension Pack (Companion Module)

This companion module provides a dedicated runtime environment for running **VS Code OSS (code-server)** with a minimal **Ubuntu Linux** environment and integrated terminal directly on Android devices without root.

---

## 🌟 Key Architecture

1. **Modular Companion Pattern**:
   - Packaged as a separate APK (`com.vastavik.codeoss`) to keep the primary Vastavik Learning app lean (~72 MB).
   - If uninstalled, the main app's built-in NeoBrutalistic code editor remains 100% active and functional.
   - If installed, the main app's editor and Student Info page dynamically provide one-tap launching and synchronization.

2. **Minimal Ubuntu Environment ("As less features as possible")**:
   - Zero GUI/X11 or desktop bloat.
   - Stripped base rootfs containing `bash`, standard GNU coreutils, glibc, and runtime tools.
   - Runs completely in user-space using `proot` virtualization (no root required).
   - Real-time terminal output: code runs directly in this environment and displays results in the integrated terminal.

3. **Inter-App Communication**:
   - Accepts `com.vastavik.codeoss.OPEN_EDITOR` intent action with:
     - `extra_code`: Active source code from the main app.
     - `extra_language`: Selected programming language.
     - `extra_question`: Structured problem prompt and test cases.
   - Pinned mobile keyboard bar (`ESC`, `TAB`, `CTRL`, `ALT`, cursor arrows) for seamless phone-based coding.
