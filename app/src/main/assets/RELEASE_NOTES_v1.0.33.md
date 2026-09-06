# Release Notes — Vastavik Computers v1.0.33

**Release Date:** September 6, 2026  
**Build:** v1.0.33 (Production Release)  
**Platform:** Android (minSdk 24, targetSdk 35)

---

## 🚀 Key Highlights in this Release

### 1. CodeOSS (VS Code OSS) & Minimal Ubuntu Linux Terminal Extension Pack
- **Companion APK Architecture (`com.vastavik.codeoss`):** Designed as an optional, modular companion APK to keep the core Vastavik app fast, responsive, and lightweight (~72 MB) rather than ballooning past 350 MB with full Linux userland and node dependencies.
- **Dual-Tab Workspace:**
  - ⚡ **CodeOSS Editor Tab:** Embedded WebView connecting to local `code-server` on `127.0.0.1:8080`, with an offline dark HTML editor fallback during initialization.
  - 💻 **Minimal Ubuntu Terminal Tab:** Zero-bloat, raw Linux bash terminal with prompt `root@ubuntu:~# `, real-time stdout/stderr output streaming, quick command chips (`ls -la`, `pwd`, `uname -a`, `python3 --version`, `clear`), and interactive command history (`UP` / `DOWN`).
  - **▶ Run Solution Action:** Automatically maps code (`solution.py`, `Main.java`, `solution.cpp`, etc.) into the Linux workspace, switches directly to the Ubuntu Terminal, and executes it.
  - **Termux-style Mobile Keyboard Bar:** Sticky touchscreen accessory bar providing `ESC`, `TAB`, `CTRL+C`, `{`, `}`, `[`, `]`, `;`, `|`, `~`, `&`, `$`, `UP`, `DOWN` for comfortable terminal coding.
- **Execution Engine (`UbuntuTerminalEngine.kt`):** Bridges PRoot Ubuntu rootfs and Android's Linux environment with custom shell variables (`HOME`, `SHELL=/bin/bash`, `TERM=xterm-256color`, `USER=root`).

---

### 2. Student Info (Profile) Shining Feature
- **Prominent First Position:** Displayed at the very top of the menu items in Student Info (`ProfileScreen.kt`).
- **Animated Infinite Shimmer:** Beautiful animated gradient shimmer brush (`shineBrush`) transitioning across gold, amber, and purple tones.
- **✨ PRO Badge & Live Status:** Displays `✨ PRO` badge alongside live state: `INSTALL` (amber) if uninstalled, and `ACTIVE` (green) when installed.
- **Interactive Extension Dialog:** Offers 1-tap offline asset installation, GitHub download with a live progress indicator, default editor preference toggle, and instant launch button.

---

### 3. Built-In Editor Seamless Fallback & Inter-App Transfer
- **100% Uncompromised Native Fallback:** When the extension pack is not installed, the built-in NeoBrutalistic code editor remains completely active, functional, and unaffected.
- **Terminal Icon Bridge:** The TopAppBar includes a `Terminal` icon. When the companion is installed, tapping it transfers the current code, language, and problem statement to CodeOSS; when not installed, it opens an informative dialog with a direct shortcut to the Student Info page.
- **Offline Asset Installation:** The companion APK is bundled directly inside `assets/companion/vastavik-codeoss-extension.apk` for instantaneous offline installation without requiring internet access.

---

## 🛠 Technical Details & Artifacts
- **Version Code:** 33
- **Version Name:** 1.0.33
- **Target SDK:** 35 (Android 15)
- **Minimum SDK:** 24 (Android 7.0)
- **Platform:** Android
- **Architecture:** Universal APK
- **Release Assets:**
  - `vastavikLearning-v1.0.33.apk` (Main App APK)
  - `vastavik-codeoss-extension.apk` (Companion Extension APK)
  - `vastavikLearning-app-v1.0.33-source.tar.gz` (Source code archive)
  - `vastavikLearning-app-v1.0.33-source.zip` (Source code archive)
