package com.vastavik.computer.utils

import android.util.Log
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Centralized debug logger for Vastavik AI calls.
 *
 * Stores last N entries in memory. Admin-only UI overlay reads from this.
 * Not persisted across process restarts (intentionally lightweight).
 */
object DebugLogBox {

    private const val MAX_ENTRIES = 30
    private val logQueue = ConcurrentLinkedQueue<LogEntry>()
    private const val TAG = "VastavikAi/Debug"

    data class LogEntry(
        val timestamp: Long = System.currentTimeMillis(),
        val level: Level,
        val tag: String,
        val message: String,
        val model: String = "GOD",
        val isError: Boolean = level == Level.ERROR,
        val throwable: Throwable? = null
    )

    enum class Level { INFO, WARN, ERROR }

    @Volatile var lastErrorEntry: LogEntry? = null
        private set

    @Volatile var lastSuccessTimestamp: Long = 0L
        private set

    @Volatile var activeModel: String = "GOD"

    fun info(tag: String, message: String, model: String = activeModel) = add(Level.INFO, tag, message, model)
    fun warn(tag: String, message: String, model: String = activeModel) = add(Level.WARN, tag, message, model)
    fun error(tag: String, message: String, throwable: Throwable? = null, model: String = activeModel) = add(Level.ERROR, tag, message, model, throwable)

    private fun add(level: Level, tag: String, message: String, model: String = activeModel, throwable: Throwable? = null) {
        val entry = LogEntry(level = level, tag = tag, message = message, model = model, throwable = throwable)
        if (level == Level.ERROR) {
            lastErrorEntry = entry
        } else if (level == Level.INFO && message.startsWith("OK")) {
            lastSuccessTimestamp = System.currentTimeMillis()
        }
        logQueue.add(entry)
        while (logQueue.size > MAX_ENTRIES) logQueue.poll()
        // Also write to Android logcat
        when (level) {
            Level.INFO -> Log.i(TAG, "[$tag] $message")
            Level.WARN -> Log.w(TAG, "[$tag] $message", throwable)
            Level.ERROR -> Log.e(TAG, "[$tag] $message", throwable)
        }
    }

    fun getEntries(): List<LogEntry> = logQueue.toList().reversed()

    fun clear() {
        logQueue.clear()
        lastErrorEntry = null
    }
}