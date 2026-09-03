package com.vastavik.computer.data.realtime

import com.vastavik.computer.data.model.ClassSession
import com.vastavik.computer.data.model.LiveChatMessage
import com.vastavik.computer.data.model.MediaState
import com.vastavik.computer.data.model.Participant
import com.vastavik.computer.data.model.ParticipantRole
import com.vastavik.computer.data.model.ReplyPreview
import com.vastavik.computer.data.model.WhiteboardElement
import com.vastavik.computer.data.model.WhiteboardState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class LocalMeetingClient : MeetingClient {
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

    override suspend fun joinClass(classId: String, userId: String, displayName: String) {
        connectionState.value = ConnectionState.CONNECTING
        val p = Participant(userId = userId, displayName = displayName)
        currentUser.value = p
        participants.value = mapOf(userId to p)
        currentSession.value = ClassSession(classId = classId, topic = "Class $classId", adminId = userId)
        connectionState.value = ConnectionState.CONNECTED
    }

    override suspend fun leaveClass() {
        connectionState.value = ConnectionState.DISCONNECTED
        participants.value = emptyMap()
        chatMessages.value = emptyList()
        currentSession.value = null
        currentUser.value = null
    }

    override suspend fun toggleMic(enabled: Boolean) {
        localMicEnabled.value = enabled
        updateSelf { it.copy(micState = if (enabled) MediaState.ON else MediaState.OFF) }
    }

    override suspend fun toggleCamera(enabled: Boolean) {
        localCameraEnabled.value = enabled
        updateSelf { it.copy(cameraState = if (enabled) MediaState.ON else MediaState.OFF) }
    }

    override suspend fun toggleHandRaise(enabled: Boolean) {
        localHandRaised.value = enabled
        updateSelf { it.copy(handRaised = enabled) }
    }

    override suspend fun toggleScreenShare(enabled: Boolean) { localScreenSharing.value = enabled }
    override suspend fun requestScreenShare() { localScreenSharing.value = true }

    override suspend fun sendChatMessage(text: String, replyTo: ReplyPreview?) {
        val user = currentUser.value ?: return
        val msg = LiveChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = user.userId,
            senderName = user.displayName,
            senderRole = user.role,
            text = text,
            replyTo = replyTo
        )
        chatMessages.value = chatMessages.value + msg
    }

    override suspend fun updateWhiteboard(elements: List<WhiteboardElement>) {
        whiteboardState.value = WhiteboardState(elements = elements)
    }

    override suspend fun kickParticipant(uid: String) { participants.value = participants.value - uid }
    override suspend fun assignStarCast(uid: String) { updateParticipant(uid) { it.copy(role = ParticipantRole.STARCAST) } }
    override suspend fun revokeStarCast(uid: String) { updateParticipant(uid) { it.copy(role = ParticipantRole.STUDENT) } }
    override suspend fun grantScreenShare(uid: String) { updateParticipant(uid) { it.copy(hasScreenSharePermission = true) } }
    override suspend fun revokeScreenShare(uid: String) { updateParticipant(uid) { it.copy(hasScreenSharePermission = false) } }
    override suspend fun startRecording() { currentSession.value = currentSession.value?.copy(recording = true) }
    override suspend fun stopRecording() { currentSession.value = currentSession.value?.copy(recording = false) }

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
}
