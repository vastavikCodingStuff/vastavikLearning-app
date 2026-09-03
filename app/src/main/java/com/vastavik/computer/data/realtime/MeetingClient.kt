package com.vastavik.computer.data.realtime

import com.vastavik.computer.data.model.ClassSession
import com.vastavik.computer.data.model.LiveChatMessage
import com.vastavik.computer.data.model.Participant
import com.vastavik.computer.data.model.ReplyPreview
import com.vastavik.computer.data.model.WhiteboardElement
import com.vastavik.computer.data.model.WhiteboardState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING, FAILED }

interface MeetingClient {
    val connectionState: StateFlow<ConnectionState>
    val participants: StateFlow<Map<String, Participant>>
    val chatMessages: StateFlow<List<LiveChatMessage>>
    val whiteboardState: StateFlow<WhiteboardState>
    val currentSession: StateFlow<ClassSession?>
    val currentUser: StateFlow<Participant?>
    val localMicEnabled: StateFlow<Boolean>
    val localCameraEnabled: StateFlow<Boolean>
    val localHandRaised: StateFlow<Boolean>
    val localScreenSharing: StateFlow<Boolean>

    suspend fun joinClass(classId: String, userId: String, displayName: String)
    suspend fun leaveClass()
    suspend fun toggleMic(enabled: Boolean)
    suspend fun toggleCamera(enabled: Boolean)
    suspend fun toggleHandRaise(enabled: Boolean)
    suspend fun toggleScreenShare(enabled: Boolean)
    suspend fun requestScreenShare()
    suspend fun sendChatMessage(text: String, replyTo: ReplyPreview?)
    suspend fun updateWhiteboard(elements: List<WhiteboardElement>)
    suspend fun kickParticipant(uid: String)
    suspend fun assignStarCast(uid: String)
    suspend fun revokeStarCast(uid: String)
    suspend fun grantScreenShare(uid: String)
    suspend fun revokeScreenShare(uid: String)
    suspend fun startRecording()
    suspend fun stopRecording()
}
