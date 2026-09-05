# Release Notes — Vastavik Computers v1.0.27

**Release Date:** September 6, 2026  
**Build:** v1.0.27 (Production Release)  
**Platform:** Android (minSdk 24, targetSdk 35)

---

## 🚀 Key Highlights in this Release

### 1. Always-On Admin Access & Offline Diagnostics
- **Permanent Profile Menu Action:** Made the "Admin Access" action permanently available in the Student Info / Profile menu without conditional login gating. Administrators and developers can immediately access real-time engine controls and diagnostic logs even if the remote backend or internet connection is offline.
- **Unrestricted Local Route Access:** Decoupled `AppNavHost` navigation from remote session status so tapping Admin Access opens `AdminDashboardScreen` directly without redirecting to home during offline or backend failure states.

---

### 2. FastAPI Backend Networking & Dual REST/gRPC Integration
- **Multi-Cloud Environment Presets:** Integrated full backend support with environment presets for **Render Cloud** (`https://vastavik-backend.onrender.com/`), **Railway Cloud** (`https://vastavik-backend.up.railway.app/`), **Local Emulator** (`http://10.0.2.2:8000/`), and production domains with dynamic WebSocket (`ws://` / `wss://`) routing.
- **Comprehensive 24-Endpoint API Interface:** Expanded `VastavikApiService` with complete production contracts covering Authentication, Catalog, Curriculum, AI, Code Runner, Notes, PYQs, Payments, Bug Reporting, and Device Verification.
- **Complete Kotlin Data Models:** Added 30+ structured models in `com.vastavik.computer.data.api.model.BackendModels` matching the backend schema.

---

### 3. High Authentication, JWT Refresh & Cryptographic HMAC Verification
- **JWT Token Management:** Implemented `TokenManager` for persistent and secure storage of JWT access and refresh tokens.
- **Silent Auto-Refresh Authenticator:** Added `TokenAuthenticator` providing silent auto-refresh on `HTTP 401 Unauthorized` responses via `POST api/v1/auth/refresh`.
- **Hex-Encoded HMAC-SHA256 Signatures:** Updated `AuthInterceptor` with seconds-precision timestamp and hex-encoded HMAC-SHA256 signature verification matching backend formula `HMAC-SHA256(keySecret, timestamp + METHOD + path)`.

---

### 4. Resilient Circuit Breaking & Route Maintenance Protection
- **Fault-Tolerant Circuit Breaker:** Implemented `CircuitBreaker.safeApiCall` catching `HTTP 503 Service Unavailable` responses.
- **Graceful Maintenance Handling:** Gracefully surfaces route maintenance information without crashing or stalling the application, falling back seamlessly to cached/local data when individual backend routes undergo maintenance.

---

### 5. Real-Time Streaming & WebSockets
- **AI Chat Token Streaming (SSE):** Built `VastavikAiStreamer` consuming Server-Sent Events (SSE) from `/api/v1/ai/chat/stream` for real-time word-by-word streaming in AI Chat.
- **Peer Chat WebSocket Client:** Added `PeerChatClient` connecting to `/ws/peer-chat` for instant student community chat.
- **WebRTC Signaling WebSocket Client:** Added `WebRtcSignalingClient` connecting to `/ws/signaling` for low-latency live classroom SDP and ICE relay.

---

## 🛠 Technical Details & Artifacts
- **Version Code:** 27
- **Version Name:** 1.0.27
- **Target SDK:** 35 (Android 15)
- **Minimum SDK:** 24 (Android 7.0)
- **Platform:** Android
- **Architecture:** Universal APK
- **Asset Packages:**
  - `vastavikLearning-v1.0.27.apk`
