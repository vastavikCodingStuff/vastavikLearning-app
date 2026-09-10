package com.vastavik.computer.ui.screens.chat

import android.content.Context
import android.os.Build
import android.util.Log
import com.vastavik.computer.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import com.vastavik.computer.data.api.ApiConfig

import com.vastavik.computer.di.RepositoryEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.withContext

data class AiConversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Chat",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messages: List<ChatMessageData> = emptyList()
)

data class ChatMessageData(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

object AiConversationCache {
    private const val PREFS_NAME = "vastavik_ai_conversations"
    private const val KEY_ACTIVE_ID = "active_conversation_id"
    private const val CACHE_FILE_NAME = "ai_conversations_cache.json"

    private fun getCacheFileName(userId: String?): String {
        return if (!userId.isNullOrBlank()) "ai_conversations_${userId}.json" else CACHE_FILE_NAME
    }

    private fun getPrefsName(userId: String?): String {
        return if (!userId.isNullOrBlank()) "vastavik_ai_conversations_${userId}" else PREFS_NAME
    }

    fun loadConversations(context: Context, userId: String? = null): List<AiConversation> {
        val fileName = getCacheFileName(userId)
        return try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) {
                val legacyFile = File(context.filesDir, CACHE_FILE_NAME)
                if (fileName != CACHE_FILE_NAME && legacyFile.exists()) {
                    try {
                        legacyFile.copyTo(file, overwrite = false)
                    } catch (_: Exception) {}
                }
            }
            if (!file.exists()) {
                val defaultConv = AiConversation(
                    title = "New Chat",
                    messages = listOf(
                        ChatMessageData(
                            text = "Hello! I am Vastavik AI. Ask me anything about Java, Python, JavaScript, or SQL for Class 5-12!",
                            isUser = false
                        )
                    )
                )
                saveConversations(context, listOf(defaultConv), userId)
                return listOf(defaultConv)
            }
            val jsonStr = file.readText(Charsets.UTF_8)
            val jsonArr = JSONArray(jsonStr)
            val list = mutableListOf<AiConversation>()
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                val msgArr = obj.optJSONArray("messages") ?: JSONArray()
                val msgs = mutableListOf<ChatMessageData>()
                for (j in 0 until msgArr.length()) {
                    val mObj = msgArr.getJSONObject(j)
                    msgs.add(
                        ChatMessageData(
                            text = mObj.optString("text", ""),
                            isUser = mObj.optBoolean("isUser", false),
                            timestamp = mObj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                list.add(
                    AiConversation(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.optString("title", "Chat ${i + 1}"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                        messages = msgs
                    )
                )
            }
            if (list.isEmpty()) {
                val defaultConv = AiConversation(
                    title = "New Chat",
                    messages = listOf(
                        ChatMessageData(
                            text = "Hello! I am Vastavik AI. Ask me anything about Java, Python, JavaScript, or SQL for Class 5-12!",
                            isUser = false
                        )
                    )
                )
                saveConversations(context, listOf(defaultConv), userId)
                listOf(defaultConv)
            } else {
                list.sortedByDescending { it.updatedAt }
            }
        } catch (e: Exception) {
            Log.e("AiConversationCache", "Error loading cached conversations: ${e.message}")
            emptyList()
        }
    }

    fun saveConversations(context: Context, list: List<AiConversation>, userId: String? = null) {
        try {
            val jsonArr = JSONArray()
            for (conv in list) {
                val obj = JSONObject().apply {
                    put("id", conv.id)
                    put("title", conv.title)
                    put("createdAt", conv.createdAt)
                    put("updatedAt", conv.updatedAt)
                    val msgArr = JSONArray()
                    for (m in conv.messages) {
                        msgArr.put(JSONObject().apply {
                            put("text", m.text)
                            put("isUser", m.isUser)
                            put("timestamp", m.timestamp)
                        })
                    }
                    put("messages", msgArr)
                }
                jsonArr.put(obj)
            }
            val file = File(context.filesDir, getCacheFileName(userId))
            file.writeText(jsonArr.toString(), Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("AiConversationCache", "Error saving conversations: ${e.message}")
        }
    }

    fun saveConversation(context: Context, conversation: AiConversation, userId: String? = null) {
        try {
            val existing = loadConversations(context, userId).toMutableList()
            val idx = existing.indexOfFirst { it.id == conversation.id }
            if (idx >= 0) existing[idx] = conversation else existing.add(0, conversation)
            saveConversations(context, existing, userId)
        } catch (e: Exception) {
            Log.e("AiConversationCache", "Error saving single conversation: ${e.message}")
        }
    }

    fun deleteConversation(context: Context, id: String, userId: String? = null) {
        try {
            val existing = loadConversations(context, userId).toMutableList()
            existing.removeAll { it.id == id }
            saveConversations(context, existing, userId)
            if (getActiveConversationId(context, userId) == id) {
                val newActive = existing.firstOrNull()?.id ?: UUID.randomUUID().toString()
                setActiveConversationId(context, newActive, userId)
            }
        } catch (e: Exception) {
            Log.e("AiConversationCache", "Error deleting conversation: ${e.message}")
        }
    }

    fun getActiveConversationId(context: Context, userId: String? = null): String? {
        val prefs = context.getSharedPreferences(getPrefsName(userId), Context.MODE_PRIVATE)
        return prefs.getString(KEY_ACTIVE_ID, null)
    }

    fun setActiveConversationId(context: Context, id: String, userId: String? = null) {
        val prefs = context.getSharedPreferences(getPrefsName(userId), Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ACTIVE_ID, id).apply()
    }
}

object AiConversationSyncManager {
    fun syncConversationToServer(conversation: AiConversation, authToken: String? = null, context: Context? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = authToken ?: if (context != null) {
                    try {
                        EntryPointAccessors.fromApplication(
                            context.applicationContext,
                            RepositoryEntryPoint::class.java
                        ).tokenManager().getAccessToken()
                    } catch (_: Throwable) { null }
                } else null

                val payload = JSONObject().apply {
                    put("conversationId", conversation.id)
                    put("title", conversation.title)
                    put("updatedAt", conversation.updatedAt)
                    put("appVersion", BuildConfig.VERSION_NAME)
                    put("deviceModel", "${Build.MANUFACTURER} ${Build.MODEL}")
                    val msgsArray = JSONArray()
                    for (msg in conversation.messages) {
                        msgsArray.put(JSONObject().apply {
                            put("role", if (msg.isUser) "user" else "assistant")
                            put("content", msg.text)
                            put("timestamp", msg.timestamp)
                        })
                    }
                    put("messages", msgsArray)
                }

                val base = ApiConfig.BASE_URL.trimEnd('/')
                val url = URL("$base/api/v1/ai/conversations/telemetry")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 4000
                    readTimeout = 4000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("User-Agent", "VastavikLearningApp/${BuildConfig.VERSION_NAME}")
                    if (!token.isNullOrBlank()) {
                        setRequestProperty("Authorization", "Bearer $token")
                    }
                }
                conn.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
                val code = conn.responseCode
                Log.d("AiConversationSync", "Server response code: $code")
            } catch (e: Exception) {
                // Graceful fallback: Backend server may be offline or unreachable.
                // Log silently; this MUST NEVER break, interrupt, or delay the AI chat.
                Log.d("AiConversationSync", "Server sync skipped: ${e.message}")
            }
        }
    }

    /**
     * Restore previous AI chat sessions from backend database (GET /api/v1/ai/sessions)
     * so that logging in with any account on any device restores their full AI chat history.
     */
    fun restoreUserConversationsFromServer(
        context: Context,
        userId: String?,
        authToken: String?,
        onComplete: (List<AiConversation>) -> Unit
    ) {
        if (authToken.isNullOrBlank()) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val base = ApiConfig.BASE_URL.trimEnd('/')
                val url = URL("$base/api/v1/ai/sessions")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 7000
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "VastavikLearningApp/${BuildConfig.VERSION_NAME}")
                    setRequestProperty("Authorization", "Bearer $authToken")
                }
                if (conn.responseCode in 200..299) {
                    val resp = conn.inputStream.bufferedReader().readText()
                    val jsonArr = JSONArray(resp)
                    if (jsonArr.length() > 0) {
                        val remoteList = mutableListOf<AiConversation>()
                        for (i in 0 until jsonArr.length()) {
                            val obj = jsonArr.getJSONObject(i)
                            val sId = obj.optString("session_id", obj.optString("id", UUID.randomUUID().toString()))
                            val sTitle = obj.optString("title", "Chat ${i + 1}")
                            val msgArr = obj.optJSONArray("messages") ?: JSONArray()
                            val msgs = mutableListOf<ChatMessageData>()
                            for (j in 0 until msgArr.length()) {
                                val mObj = msgArr.getJSONObject(j)
                                val role = mObj.optString("role", "user")
                                val text = mObj.optString("content", mObj.optString("text", ""))
                                val ts = mObj.optLong("timestamp", System.currentTimeMillis())
                                if (text.isNotBlank()) {
                                    msgs.add(ChatMessageData(text = text, isUser = (role == "user"), timestamp = ts))
                                }
                            }
                            if (msgs.isNotEmpty()) {
                                remoteList.add(
                                    AiConversation(
                                        id = sId,
                                        title = sTitle,
                                        messages = msgs,
                                        updatedAt = System.currentTimeMillis() - (i * 1000)
                                    )
                                )
                            }
                        }
                        if (remoteList.isNotEmpty()) {
                            val existing = AiConversationCache.loadConversations(context, userId)
                            val merged = (remoteList + existing).distinctBy { it.id }
                            AiConversationCache.saveConversations(context, merged, userId)
                            withContext(Dispatchers.Main) {
                                onComplete(merged)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("AiConversationSync", "Restore from server skipped: ${e.message}")
            }
        }
    }
}
