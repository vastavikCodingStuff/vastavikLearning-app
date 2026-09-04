package com.vastavik.computer.data.realtime

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.vastavik.computer.data.model.ParticipantRole
import com.vastavik.computer.utils.TelegramNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

object StudentConversationManager {

    private const val TAG = "StudentConversation"
    private val firestore = FirebaseFirestore.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _messages = MutableStateFlow<List<StudentChatMessage>>(emptyList())
    val messages: StateFlow<List<StudentChatMessage>> = _messages.asStateFlow()

    private val _typingStudents = MutableStateFlow<Set<String>>(emptySet())
    val typingStudents: StateFlow<Set<String>> = _typingStudents.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var activeChannelId: String = "general_coding"
    private var chatListener: ListenerRegistration? = null
    private var typingListener: ListenerRegistration? = null
    private var appContext: Context? = null
    private var webSocket: WebSocket? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Joins a student community conversation channel.
     */
    fun joinChannel(channelId: String = "general_coding") {
        if (activeChannelId == channelId && chatListener != null) return

        chatListener?.remove()
        typingListener?.remove()
        activeChannelId = channelId
        _connectionState.value = ConnectionState.CONNECTING

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Connect Socket.IO / WebSocket signaling client
        connectWebSocket(channelId)

        // Realtime Firestore channel listener
        chatListener = firestore.collection("student_channels")
            .document(channelId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limitToLast(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _connectionState.value = ConnectionState.CONNECTED
                    return@addSnapshotListener
                }

                val currentIds = _messages.value.map { it.id }.toSet()
                val list = mutableListOf<StudentChatMessage>()
                var hasNewIncoming = false

                for (doc in snapshot.documents) {
                    val id = doc.getString("id") ?: doc.id
                    val senderId = doc.getString("senderId") ?: ""
                    val senderName = doc.getString("senderName") ?: "Fellow Student"
                    val roleStr = doc.getString("senderRole") ?: "STUDENT"
                    val text = doc.getString("text") ?: ""
                    val ts = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    val rId = doc.getString("replyToId")
                    val rName = doc.getString("replyToName")
                    val rText = doc.getString("replyToText")

                    val role = runCatching { ParticipantRole.valueOf(roleStr) }.getOrDefault(ParticipantRole.STUDENT)
                    val msg = StudentChatMessage(
                        id = id,
                        senderId = senderId,
                        senderName = senderName,
                        senderRole = role,
                        channelId = channelId,
                        text = text,
                        timestamp = ts,
                        replyToId = rId,
                        replyToName = rName,
                        replyToText = rText
                    )
                    list.add(msg)

                    // Trigger Telegram-style notification for messages arriving while in another section
                    if (id !in currentIds && senderId.isNotBlank() && senderId != currentUid && text.isNotBlank()) {
                        hasNewIncoming = true
                        TelegramNotificationManager.showIncomingMessage(
                            senderName = senderName,
                            message = text
                        )
                        appContext?.let { ctx ->
                            TelegramNotificationManager.postTelegramStyleSystemNotification(
                                context = ctx,
                                senderName = senderName,
                                message = text,
                                screen = "chat"
                            )
                        }
                    }
                }

                _messages.value = list
                _connectionState.value = ConnectionState.CONNECTED
            }

        // Listen for typing events
        typingListener = firestore.collection("student_channels")
            .document(channelId)
            .collection("typing")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                val typingSet = mutableSetOf<String>()
                val now = System.currentTimeMillis()
                for (doc in snapshot.documents) {
                    val uid = doc.id
                    val name = doc.getString("senderName") ?: ""
                    val isTyping = doc.getBoolean("isTyping") ?: false
                    val ts = doc.getLong("timestamp") ?: 0L
                    if (uid != currentUid && isTyping && (now - ts < 5000)) {
                        typingSet.add(name)
                    }
                }
                _typingStudents.value = typingSet
            }
    }

    /**
     * Sends a message to fellow students.
     */
    fun sendMessage(text: String, replyTo: StudentChatMessage? = null) {
        if (text.isBlank()) return
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val name = user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Student"

        val msgId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        val msg = StudentChatMessage(
            id = msgId,
            senderId = user.uid,
            senderName = name,
            senderRole = ParticipantRole.STUDENT,
            channelId = activeChannelId,
            text = text.trim(),
            timestamp = timestamp,
            replyToId = replyTo?.id,
            replyToName = replyTo?.senderName,
            replyToText = replyTo?.text
        )

        // Optimistically add to list
        _messages.value = _messages.value + msg

        // Emit via WebSocket / Socket.IO
        emitSocketMessage("chat:message", JSONObject().apply {
            put("id", msgId)
            put("senderId", user.uid)
            put("senderName", name)
            put("text", text.trim())
            put("channelId", activeChannelId)
            put("timestamp", timestamp)
        })

        // Persist to Firestore
        scope.launch {
            try {
                val data = hashMapOf(
                    "id" to msg.id,
                    "senderId" to msg.senderId,
                    "senderName" to msg.senderName,
                    "senderRole" to msg.senderRole.name,
                    "channelId" to msg.channelId,
                    "text" to msg.text,
                    "timestamp" to msg.timestamp,
                    "replyToId" to msg.replyToId,
                    "replyToName" to msg.replyToName,
                    "replyToText" to msg.replyToText
                )
                firestore.collection("student_channels")
                    .document(activeChannelId)
                    .collection("messages")
                    .document(msgId)
                    .set(data)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to persist student message: ${e.message}")
            }
        }
    }

    /**
     * Broadcasts typing status to the channel.
     */
    fun setTyping(isTyping: Boolean) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val name = user.displayName?.takeIf { it.isNotBlank() } ?: "Student"

        scope.launch {
            try {
                firestore.collection("student_channels")
                    .document(activeChannelId)
                    .collection("typing")
                    .document(user.uid)
                    .set(
                        mapOf(
                            "senderName" to name,
                            "isTyping" to isTyping,
                            "timestamp" to System.currentTimeMillis()
                        )
                    )
            } catch (e: Exception) {
                // Ignore transient typing error
            }
        }
    }

    private fun connectWebSocket(channelId: String) {
        try {
            val client = OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build()

            val request = Request.Builder()
                .url("${com.vastavik.computer.data.api.ApiConfig.BASE_URL}socket.io/?EIO=4&transport=websocket&channel=$channelId")
                .build()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d(TAG, "Socket.IO WebSocket connected to channel: $channelId")
                    _connectionState.value = ConnectionState.CONNECTED
                    // Socket.IO handshake / room join
                    webSocket.send("40/student,")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.d(TAG, "Socket.IO message received: $text")
                    // Parse Socket.IO Engine.IO packets (42 is message event)
                    if (text.startsWith("42")) {
                        handleSocketIoPacket(text.substring(2))
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.w(TAG, "Socket.IO WebSocket offline, using Firestore fallback: ${t.message}")
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "Socket.IO client init fallback: ${e.message}")
        }
    }

    private fun emitSocketMessage(event: String, payload: JSONObject) {
        try {
            val packet = "42[\"$event\",${payload}]"
            webSocket?.send(packet)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to send packet over socket: ${e.message}")
        }
    }

    private fun handleSocketIoPacket(packet: String) {
        try {
            val jsonArray = org.json.JSONArray(packet)
            val eventName = jsonArray.getString(0)
            if (eventName == "chat:message" && jsonArray.length() > 1) {
                val data = jsonArray.getJSONObject(1)
                val senderId = data.optString("senderId")
                val senderName = data.optString("senderName", "Student")
                val text = data.optString("text")
                val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                if (senderId != currentUid && text.isNotBlank()) {
                    TelegramNotificationManager.showIncomingMessage(
                        senderName = senderName,
                        message = text
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing socket packet: ${e.message}")
        }
    }
}
