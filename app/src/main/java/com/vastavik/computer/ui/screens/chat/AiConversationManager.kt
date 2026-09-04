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

    fun loadConversations(context: Context): List<AiConversation> {
        return try {
            val file = File(context.filesDir, CACHE_FILE_NAME)
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
                saveConversations(context, listOf(defaultConv))
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
                saveConversations(context, listOf(defaultConv))
                listOf(defaultConv)
            } else {
                list.sortedByDescending { it.updatedAt }
            }
        } catch (e: Exception) {
            Log.e("AiConversationCache", "Error loading cached conversations: ${e.message}")
            emptyList()
        }
    }

    fun saveConversations(context: Context, list: List<AiConversation>) {
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
            val file = File(context.filesDir, CACHE_FILE_NAME)
            file.writeText(jsonArr.toString(), Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("AiConversationCache", "Error saving conversations: ${e.message}")
        }
    }

    fun saveConversation(context: Context, conversation: AiConversation) {
        try {
            val existing = loadConversations(context).toMutableList()
            val idx = existing.indexOfFirst { it.id == conversation.id }
            if (idx >= 0) existing[idx] = conversation else existing.add(0, conversation)
            saveConversations(context, existing)
        } catch (e: Exception) {
            Log.e("AiConversationCache", "Error saving single conversation: ${e.message}")
        }
    }

    fun getActiveConversationId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ACTIVE_ID, null)
    }

    fun setActiveConversationId(context: Context, id: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ACTIVE_ID, id).apply()
    }
}

object AiConversationSyncManager {
    // Configured server endpoint (ready for when backend server is deployed)
    private const val DEFAULT_SERVER_ENDPOINT = "https://api.vastavik.computer/api/v1/ai/conversations/telemetry"

    fun syncConversationToServer(conversation: AiConversation) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
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

                val url = URL(DEFAULT_SERVER_ENDPOINT)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 3000
                    readTimeout = 3000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("User-Agent", "VastavikLearningApp/${BuildConfig.VERSION_NAME}")
                }
                conn.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
                val code = conn.responseCode
                Log.d("AiConversationSync", "Server response code: $code")
            } catch (e: Exception) {
                // Graceful fallback: Backend server is not yet deployed.
                // Log silently; this MUST NEVER break, interrupt, or delay the AI chat.
                Log.d("AiConversationSync", "Server sync skipped (server offline or unavailable): ${e.message}")
            }
        }
    }
}
