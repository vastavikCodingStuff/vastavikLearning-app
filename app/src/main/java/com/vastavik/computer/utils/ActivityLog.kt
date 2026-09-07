package com.vastavik.computer.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.vastavik.computer.VastavikApplication
import com.vastavik.computer.data.repository.VastavikApiRepository
import com.vastavik.computer.di.RepositoryEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

/**
 * Lightweight in-app activity log.
 *
 * Writes every event to:
 *  1. A local rolling JSON-lines file on disk (`vastavik_activity.log`) so nothing is
 *     lost if the network or Firestore is unreachable.
 *  2. A batched flush to the backend (`POST /api/v1/activity/log`).
 *  3. A direct write to the user's `activity_logs` sub-collection in Firestore
 *     (best-effort, swallowed on failure).
 *
 * The log captures page navigations, button presses, search queries, AI chat usage,
 * video watch progress, premium / payment interactions, code-editor work, settings
 * changes, bug reports, lesson completions and more.
 *
 * All writes are fire-and-forget; nothing on the UI thread blocks.
 */
object ActivityLog {

    private const val TAG = "ActivityLog"
    private const val PREFS_NAME = "vastavik_activity"
    private const val KEY_PENDING_BATCH = "pending_batch"
    private const val KEY_TOTAL_EVENTS = "total_events"
    private const val KEY_LAST_FLUSH = "last_flush"
    private const val MAX_BATCH = 100
    private const val MAX_FILE_BYTES = 2L * 1024 * 1024  // 2 MB rolling
    private const val MAX_BACKLOG_BATCHES = 50

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sequence = AtomicInteger(0)

    private val _totalEvents = MutableStateFlow(0)
    val totalEvents = _totalEvents.asStateFlow()

    private val _lastFlushAt = MutableStateFlow<Long?>(null)
    val lastFlushAt = _lastFlushAt.asStateFlow()

    fun log(context: Context?, event: String, payload: Map<String, Any?> = emptyMap()) {
        val ctx = context ?: try { VastavikApplication.instance } catch (_: Throwable) { null } ?: return
        try {
            val uid = try { FirebaseAuth.getInstance().currentUser?.uid } catch (_: Throwable) { null }
            val email = try { FirebaseAuth.getInstance().currentUser?.email } catch (_: Throwable) { null }
            val isAdmin = AdminSession.isAdmin.value
            val entry = JSONObject().apply {
                put("id", UUID.randomUUID().toString())
                put("seq", sequence.incrementAndGet())
                put("event", event)
                put("uid", uid ?: "anonymous")
                put("email", email ?: "")
                put("role", if (isAdmin) "admin" else (uid?.let { "student" } ?: "anonymous"))
                put("app_version", try { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName } catch (_: Throwable) { "" })
                put("os_version", Build.VERSION.RELEASE ?: "")
                put("device", Build.MODEL ?: "")
                put("manufacturer", Build.MANUFACTURER ?: "")
                put("timestamp_iso", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(Date()))
                put("timestamp_ms", System.currentTimeMillis())
                put("payload", JSONObject(payload))
            }

            // 1) Local file
            appendToLocalLog(ctx, entry)

            // 2) Local pending batch
            queueForBackend(ctx, entry)

            // 3) Best-effort direct Firestore write
            scope.launch {
                if (uid != null) {
                    try {
                        FirebaseFirestore.getInstance()
                            .collection("users").document(uid)
                            .collection("activity_logs")
                            .document(entry.getString("id"))
                            .set(entry, SetOptions.merge())
                    } catch (_: Throwable) { /* offline / not configured */ }
                }
            }

            _totalEvents.value = _totalEvents.value + 1
        } catch (t: Throwable) {
            Log.w(TAG, "log() failed: ${t.message}")
        }
    }

    /** Convenience helper to log a navigation event. */
    fun pageView(context: Context?, route: String) =
        log(context, "page_view", mapOf("route" to route))

    fun search(context: Context?, query: String, results: Int? = null) =
        log(context, "search", buildMap {
            put("query", query)
            if (results != null) put("results", results)
        })

    fun aiChat(context: Context?, model: String, promptLength: Int) =
        log(context, "ai_chat", mapOf("model" to model, "prompt_length" to promptLength))

    fun videoWatch(context: Context?, lessonId: String, currentSec: Int, durationSec: Int, percent: Double) =
        log(context, "video_watch", mapOf(
            "lesson_id" to lessonId,
            "current_sec" to currentSec,
            "duration_sec" to durationSec,
            "percent" to percent
        ))

    fun videoLike(context: Context?, lessonId: String, liked: Boolean) =
        log(context, "video_like", mapOf("lesson_id" to lessonId, "liked" to liked))

    fun videoComment(context: Context?, lessonId: String, text: String) =
        log(context, "video_comment", mapOf("lesson_id" to lessonId, "text_length" to text.length))

    fun practiceAttempt(context: Context?, kind: String, setTitle: String, score: Int? = null, total: Int? = null) =
        log(context, "practice_attempt", buildMap {
            put("kind", kind)
            put("set", setTitle)
            if (score != null) put("score", score)
            if (total != null) put("total", total)
        })

    fun noteCreate(context: Context?, title: String, contentLength: Int) =
        log(context, "note_create", mapOf("title" to title, "content_length" to contentLength))

    fun codeRun(context: Context?, language: String, sourceLength: Int, ok: Boolean) =
        log(context, "code_run", mapOf("language" to language, "source_length" to sourceLength, "ok" to ok))

    fun paymentEvent(context: Context?, kind: String, planId: String? = null, amount: Double? = null) =
        log(context, "payment_event", buildMap {
            put("kind", kind)
            if (planId != null) put("plan_id", planId)
            if (amount != null) put("amount", amount)
        })

    fun premiumUpgrade(context: Context?, planId: String) =
        log(context, "premium_upgrade", mapOf("plan_id" to planId))

    fun settingChange(context: Context?, key: String, value: Any?) =
        log(context, "setting_change", mapOf("key" to key, "value" to value.toString()))

    fun bugReport(context: Context?, category: String, title: String) =
        log(context, "bug_report", mapOf("category" to category, "title" to title))

    fun lessonComplete(context: Context?, lessonId: String, courseId: String) =
        log(context, "lesson_complete", mapOf("lesson_id" to lessonId, "course_id" to courseId))

    fun appOpen(context: Context?) = log(context, "app_open", emptyMap())
    fun appBackground(context: Context?) = log(context, "app_background", emptyMap())

    private fun appendToLocalLog(context: Context, entry: JSONObject) {
        try {
            val file = File(context.filesDir, "vastavik_activity.log")
            if (file.exists() && file.length() > MAX_FILE_BYTES) {
                // Roll: keep the second half.
                val lines = file.readLines()
                val keep = lines.takeLast(lines.size / 2)
                file.writeText(keep.joinToString("\n", postfix = "\n"))
            }
            file.appendText(entry.toString() + "\n")
        } catch (t: Throwable) {
            Log.w(TAG, "appendToLocalLog failed: ${t.message}")
        }
    }

    private fun queueForBackend(context: Context, entry: JSONObject) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY_PENDING_BATCH, "[]") ?: "[]"
            val arr = try { JSONArray(raw) } catch (_: Throwable) { JSONArray() }
            arr.put(entry)
            // Cap pending array to MAX_BATCH * MAX_BACKLOG_BATCHES so prefs don't bloat.
            val cap = MAX_BATCH * MAX_BACKLOG_BATCHES
            while (arr.length() > cap) arr.remove(0)
            prefs.edit().putString(KEY_PENDING_BATCH, arr.toString()).apply()
        } catch (t: Throwable) {
            Log.w(TAG, "queueForBackend failed: ${t.message}")
        }
    }

    /**
     * Try to flush the pending batch to the backend. Safe to call frequently.
     * On success the pending array is cleared.
     */
    fun flush(context: Context?) {
        val ctx = context ?: try { VastavikApplication.instance } catch (_: Throwable) { null } ?: return
        scope.launch {
            try {
                val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val raw = prefs.getString(KEY_PENDING_BATCH, "[]") ?: "[]"
                if (raw == "[]" || raw.isBlank()) return@launch
                val arr = try { JSONArray(raw) } catch (_: Throwable) { return@launch }
                if (arr.length() == 0) return@launch

                val repo = try {
                    EntryPointAccessors.fromApplication(
                        ctx.applicationContext,
                        RepositoryEntryPoint::class.java
                    ).vastavikApiRepository()
                } catch (_: Throwable) { null } ?: return@launch

                val payload = arr.toString()
                val ok = repo.logActivity(payload)
                if (ok) {
                    prefs.edit().putString(KEY_PENDING_BATCH, "[]").apply()
                    prefs.edit().putLong(KEY_LAST_FLUSH, System.currentTimeMillis()).apply()
                    _lastFlushAt.value = System.currentTimeMillis()
                }
            } catch (t: Throwable) {
                Log.w(TAG, "flush failed: ${t.message}")
            }
        }
    }

    /** Returns the on-disk activity log file (caller may upload it as a bug-report attachment). */
    fun localLogFile(context: Context): File =
        File(context.filesDir, "vastavik_activity.log")
}
