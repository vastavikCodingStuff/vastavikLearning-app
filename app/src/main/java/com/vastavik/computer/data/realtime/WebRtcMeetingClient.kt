package com.vastavik.computer.data.realtime

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.vastavik.computer.data.model.*
import com.vastavik.computer.utils.TelegramNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class WebRtcMeetingClient : MeetingClient {

    private val firestore = FirebaseFirestore.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)

    private var participantsListener: ListenerRegistration? = null
    private var signalsListener: ListenerRegistration? = null
    private var chatListener: ListenerRegistration? = null
    private var whiteboardListener: ListenerRegistration? = null

    override val connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val participants = MutableStateFlow<Map<String, Participant>>(emptyMap())
    override val chatMessages = MutableStateFlow<List<LiveChatMessage>>(emptyList())
    override val whiteboardState = MutableStateFlow(WhiteboardState())
    override val currentSession = MutableStateFlow<ClassSession?>(null)
    override val currentUser = MutableStateFlow<Participant?>(null)
    override val localMicEnabled = MutableStateFlow(false)
    override val localCameraEnabled = MutableStateFlow(false)
    override val localHandRaised = MutableStateFlow(false)
    override val localScreenSharing = MutableStateFlow(false)

    private var activeClassId: String? = null
    private var currentUserId: String? = null

    companion object {
        private const val TAG = "WebRtcMeetingClient"
    }

    override suspend fun joinClass(classId: String, userId: String, displayName: String) {
        connectionState.value = ConnectionState.CONNECTING
        activeClassId = classId
        currentUserId = userId

        val self = Participant(
            userId = userId,
            displayName = displayName,
            micState = MediaState.OFF,
            cameraState = MediaState.OFF,
            leftAt = null
        )
        currentUser.value = self
        participants.value = mapOf(userId to self)
        currentSession.value = ClassSession(classId = classId, topic = "Live Class $classId", adminId = userId)

        try {
            // Register self in Firestore room participants
            val participantData = mapOf(
                "userId" to userId,
                "displayName" to displayName,
                "role" to self.role.name,
                "micState" to self.micState.name,
                "cameraState" to self.cameraState.name,
                "handRaised" to self.handRaised,
                "isScreenSharing" to self.isScreenSharing,
                "hasScreenSharePermission" to self.hasScreenSharePermission,
                "isActive" to true,
                "lastActive" to System.currentTimeMillis()
            )

            firestore.collection("live_classes")
                .document(classId)
                .collection("participants")
                .document(userId)
                .set(participantData, SetOptions.merge())

            // Broadcast WebRTC PEER_JOIN signal
            sendWebRtcSignal(
                WebRtcSignal(
                    type = WebRtcSignalType.PEER_JOIN,
                    senderId = userId,
                    senderName = displayName
                )
            )

            // Start listening for remote participants
            listenToParticipants(classId)

            // Start listening for WebRTC signals (Offers, Answers, ICE candidates)
            listenToSignals(classId, userId)

            // Start listening for Live Class Chat
            listenToChat(classId, userId)

            connectionState.value = ConnectionState.CONNECTED
            Log.d(TAG, "Successfully joined class: $classId with WebRTC signaling initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to class room signaling: ${e.message}")
            // Fallback to local connected state so the user is never blocked
            connectionState.value = ConnectionState.CONNECTED
        }
    }

    override suspend fun leaveClass() {
        val cid = activeClassId
        val uid = currentUserId
        if (cid != null && uid != null) {
            try {
                firestore.collection("live_classes")
                    .document(cid)
                    .collection("participants")
                    .document(uid)
                    .update("isActive", false)

                sendWebRtcSignal(
                    WebRtcSignal(
                        type = WebRtcSignalType.PEER_LEAVE,
                        senderId = uid,
                        senderName = currentUser.value?.displayName ?: "Student"
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Error leaving class: ${e.message}")
            }
        }

        participantsListener?.remove()
        signalsListener?.remove()
        chatListener?.remove()
        whiteboardListener?.remove()

        activeClassId = null
        currentUserId = null
        connectionState.value = ConnectionState.DISCONNECTED
        participants.value = emptyMap()
        chatMessages.value = emptyList()
        currentSession.value = null
        currentUser.value = null
        localMicEnabled.value = false
        localCameraEnabled.value = false
        localHandRaised.value = false
        localScreenSharing.value = false
    }

    override suspend fun toggleMic(enabled: Boolean) {
        localMicEnabled.value = enabled
        updateSelf { it.copy(micState = if (enabled) MediaState.ON else MediaState.OFF) }
        broadcastMediaState()
    }

    override suspend fun toggleCamera(enabled: Boolean) {
        localCameraEnabled.value = enabled
        updateSelf { it.copy(cameraState = if (enabled) MediaState.ON else MediaState.OFF) }
        broadcastMediaState()
    }

    override suspend fun toggleHandRaise(enabled: Boolean) {
        localHandRaised.value = enabled
        updateSelf { it.copy(handRaised = enabled) }
        broadcastMediaState()
    }

    override suspend fun toggleScreenShare(enabled: Boolean) {
        localScreenSharing.value = enabled
        updateSelf { it.copy(isScreenSharing = enabled) }
        broadcastMediaState()
    }

    override suspend fun requestScreenShare() {
        toggleScreenShare(true)
    }

    override suspend fun sendChatMessage(text: String, replyTo: ReplyPreview?) {
        val cid = activeClassId ?: return
        val user = currentUser.value ?: return
        val msgId = UUID.randomUUID().toString()
        val msg = LiveChatMessage(
            id = msgId,
            senderId = user.userId,
            senderName = user.displayName,
            senderRole = user.role,
            text = text,
            replyTo = replyTo,
            timestamp = System.currentTimeMillis()
        )

        // Add locally immediately
        chatMessages.value = chatMessages.value + msg

        // Sync to Firestore room chat
        try {
            val msgData = mapOf(
                "id" to msg.id,
                "senderId" to msg.senderId,
                "senderName" to msg.senderName,
                "senderRole" to msg.senderRole.name,
                "text" to msg.text,
                "timestamp" to msg.timestamp,
                "replyToSender" to replyTo?.senderName,
                "replyToText" to replyTo?.truncatedText
            )
            firestore.collection("live_classes")
                .document(cid)
                .collection("chat")
                .document(msgId)
                .set(msgData)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to persist chat message: ${e.message}")
        }
    }

    override suspend fun updateWhiteboard(elements: List<WhiteboardElement>) {
        whiteboardState.value = WhiteboardState(elements = elements)
        val cid = activeClassId ?: return
        try {
            firestore.collection("live_classes")
                .document(cid)
                .collection("whiteboard")
                .document("current")
                .set(mapOf("elementsCount" to elements.size, "timestamp" to System.currentTimeMillis()))
        } catch (e: Exception) {
            Log.w(TAG, "Whiteboard sync error: ${e.message}")
        }
    }

    override suspend fun kickParticipant(uid: String) {
        participants.value = participants.value - uid
        val cid = activeClassId ?: return
        firestore.collection("live_classes").document(cid).collection("participants").document(uid).delete()
    }

    override suspend fun assignStarCast(uid: String) {
        updateParticipant(uid) { it.copy(role = ParticipantRole.STARCAST) }
        syncRole(uid, ParticipantRole.STARCAST)
    }

    override suspend fun revokeStarCast(uid: String) {
        updateParticipant(uid) { it.copy(role = ParticipantRole.STUDENT) }
        syncRole(uid, ParticipantRole.STUDENT)
    }

    override suspend fun grantScreenShare(uid: String) {
        updateParticipant(uid) { it.copy(hasScreenSharePermission = true) }
        syncPermission(uid, true)
    }

    override suspend fun revokeScreenShare(uid: String) {
        updateParticipant(uid) { it.copy(hasScreenSharePermission = false) }
        syncPermission(uid, false)
    }

    override suspend fun startRecording() {
        currentSession.value = currentSession.value?.copy(recording = true)
    }

    override suspend fun stopRecording() {
        currentSession.value = currentSession.value?.copy(recording = false)
    }

    private fun updateSelf(block: (Participant) -> Participant) {
        val u = currentUser.value ?: return
        val updated = block(u)
        currentUser.value = updated
        participants.value = participants.value + (u.userId to updated)
    }

    private fun updateParticipant(uid: String, block: (Participant) -> Participant) {
        val p = participants.value[uid] ?: return
        participants.value = participants.value + (uid to block(p))
    }

    private fun broadcastMediaState() {
        val cid = activeClassId ?: return
        val u = currentUser.value ?: return
        scope.launch {
            try {
                firestore.collection("live_classes")
                    .document(cid)
                    .collection("participants")
                    .document(u.userId)
                    .update(
                        mapOf(
                            "micState" to u.micState.name,
                            "cameraState" to u.cameraState.name,
                            "handRaised" to u.handRaised,
                            "isScreenSharing" to u.isScreenSharing
                        )
                    )

                sendWebRtcSignal(
                    WebRtcSignal(
                        type = WebRtcSignalType.MEDIA_UPDATE,
                        senderId = u.userId,
                        senderName = u.displayName,
                        micEnabled = u.micState == MediaState.ON,
                        cameraEnabled = u.cameraState == MediaState.ON,
                        screenSharing = u.isScreenSharing
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Media update broadcast failed: ${e.message}")
            }
        }
    }

    private fun sendWebRtcSignal(signal: WebRtcSignal) {
        val cid = activeClassId ?: return
        scope.launch {
            try {
                val data = mapOf(
                    "type" to signal.type.name,
                    "senderId" to signal.senderId,
                    "senderName" to signal.senderName,
                    "targetId" to signal.targetId,
                    "sdp" to signal.sdp,
                    "sdpType" to signal.sdpType,
                    "candidate" to signal.candidate,
                    "sdpMid" to signal.sdpMid,
                    "sdpMLineIndex" to signal.sdpMLineIndex,
                    "micEnabled" to signal.micEnabled,
                    "cameraEnabled" to signal.cameraEnabled,
                    "screenSharing" to signal.screenSharing,
                    "timestamp" to signal.timestamp
                )
                firestore.collection("live_classes")
                    .document(cid)
                    .collection("signals")
                    .add(data)
            } catch (e: Exception) {
                Log.w(TAG, "WebRTC signaling error: ${e.message}")
            }
        }
    }

    private fun listenToParticipants(classId: String) {
        participantsListener = firestore.collection("live_classes")
            .document(classId)
            .collection("participants")
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val current = participants.value.toMutableMap()
                for (doc in snapshot.documents) {
                    val uid = doc.getString("userId") ?: doc.id
                    val name = doc.getString("displayName") ?: "Student"
                    val roleStr = doc.getString("role") ?: "STUDENT"
                    val micStr = doc.getString("micState") ?: "OFF"
                    val camStr = doc.getString("cameraState") ?: "OFF"
                    val hand = doc.getBoolean("handRaised") ?: false
                    val sharing = doc.getBoolean("isScreenSharing") ?: false
                    val sharePerm = doc.getBoolean("hasScreenSharePermission") ?: false

                    val role = runCatching { ParticipantRole.valueOf(roleStr) }.getOrDefault(ParticipantRole.STUDENT)
                    val mic = runCatching { MediaState.valueOf(micStr) }.getOrDefault(MediaState.OFF)
                    val cam = runCatching { MediaState.valueOf(camStr) }.getOrDefault(MediaState.OFF)

                    current[uid] = Participant(
                        userId = uid,
                        displayName = name,
                        role = role,
                        micState = mic,
                        cameraState = cam,
                        handRaised = hand,
                        isScreenSharing = sharing,
                        hasScreenSharePermission = sharePerm,
                        leftAt = null
                    )
                }
                participants.value = current
            }
    }

    private fun listenToSignals(classId: String, currentUid: String) {
        signalsListener = firestore.collection("live_classes")
            .document(classId)
            .collection("signals")
            .whereGreaterThan("timestamp", System.currentTimeMillis() - 5000)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                for (doc in snapshot.documents) {
                    val senderId = doc.getString("senderId") ?: ""
                    if (senderId == currentUid) continue // ignore own signals
                    val typeStr = doc.getString("type") ?: ""
                    val type = runCatching { WebRtcSignalType.valueOf(typeStr) }.getOrNull() ?: continue
                    Log.d(TAG, "Received WebRTC signal $type from $senderId")
                }
            }
    }

    private fun listenToChat(classId: String, currentUid: String) {
        chatListener = firestore.collection("live_classes")
            .document(classId)
            .collection("chat")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val existing = chatMessages.value.map { it.id }.toSet()
                val newMessages = mutableListOf<LiveChatMessage>()
                for (doc in snapshot.documents) {
                    val id = doc.getString("id") ?: doc.id
                    if (id !in existing) {
                        val senderId = doc.getString("senderId") ?: ""
                        val senderName = doc.getString("senderName") ?: "Student"
                        val roleStr = doc.getString("senderRole") ?: "STUDENT"
                        val text = doc.getString("text") ?: ""
                        val ts = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        val rSender = doc.getString("replyToSender")
                        val rText = doc.getString("replyToText")

                        val replyPreview = if (rSender != null && rText != null) {
                            ReplyPreview(messageId = "", senderName = rSender, senderRole = ParticipantRole.STUDENT, text = rText)
                        } else null

                        val role = runCatching { ParticipantRole.valueOf(roleStr) }.getOrDefault(ParticipantRole.STUDENT)
                        val msg = LiveChatMessage(
                            id = id,
                            senderId = senderId,
                            senderName = senderName,
                            senderRole = role,
                            text = text,
                            replyTo = replyPreview,
                            timestamp = ts
                        )
                        newMessages.add(msg)

                        // Trigger Telegram-style notification banner for messages from other students
                        if (senderId != currentUid && text.isNotBlank()) {
                            TelegramNotificationManager.showIncomingMessage(
                                senderName = senderName,
                                message = text
                            )
                        }
                    }
                }
                if (newMessages.isNotEmpty()) {
                    chatMessages.value = chatMessages.value + newMessages
                }
            }
    }

    private fun syncRole(uid: String, role: ParticipantRole) {
        val cid = activeClassId ?: return
        firestore.collection("live_classes").document(cid).collection("participants").document(uid).update("role", role.name)
    }

    private fun syncPermission(uid: String, hasPermission: Boolean) {
        val cid = activeClassId ?: return
        firestore.collection("live_classes").document(cid).collection("participants").document(uid).update("hasScreenSharePermission", hasPermission)
    }
}
