package com.vastavik.computer.ui.screens.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vastavik.computer.data.model.*
import com.vastavik.computer.data.realtime.ConnectionState
import com.vastavik.computer.data.realtime.LocalMeetingClient
import com.vastavik.computer.data.realtime.MeetingClient
import com.vastavik.computer.ui.components.WhiteboardTool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MeetingViewModel(
    private val meetingClient: MeetingClient = LocalMeetingClient()
) : ViewModel() {
    val connectionState: StateFlow<ConnectionState> = meetingClient.connectionState
    val participants: StateFlow<Map<String, Participant>> = meetingClient.participants
    val chatMessages: StateFlow<List<LiveChatMessage>> = meetingClient.chatMessages
    val whiteboardState: StateFlow<WhiteboardState> = meetingClient.whiteboardState
    val currentSession: StateFlow<ClassSession?> = meetingClient.currentSession

    private val _replyTo = MutableStateFlow<ReplyPreview?>(null)
    val replyTo: StateFlow<ReplyPreview?> = _replyTo

    private val _showParticipants = MutableStateFlow(false)
    val showParticipants: StateFlow<Boolean> = _showParticipants

    private val _showChat = MutableStateFlow(false)
    val showChat: StateFlow<Boolean> = _showChat

    private val _whiteboardTool = MutableStateFlow(WhiteboardTool.PEN)
    val whiteboardTool: StateFlow<WhiteboardTool> = _whiteboardTool

    val currentUser get() = meetingClient.currentUser
    val localMicEnabled = meetingClient.localMicEnabled
    val localCameraEnabled = meetingClient.localCameraEnabled
    val localHandRaised = meetingClient.localHandRaised
    val localScreenSharing = meetingClient.localScreenSharing

    fun joinClass(classId: String, userId: String, displayName: String) {
        viewModelScope.launch { meetingClient.joinClass(classId, userId, displayName) }
    }
    fun leaveClass() { viewModelScope.launch { meetingClient.leaveClass() } }
    fun toggleMic() { viewModelScope.launch { meetingClient.toggleMic(!meetingClient.localMicEnabled.value) } }
    fun toggleCamera() { viewModelScope.launch { meetingClient.toggleCamera(!meetingClient.localCameraEnabled.value) } }
    fun toggleHandRaise() { viewModelScope.launch { meetingClient.toggleHandRaise(!meetingClient.localHandRaised.value) } }
    fun toggleScreenShare() {
        val cur = meetingClient.localScreenSharing.value
        viewModelScope.launch { if (cur) meetingClient.toggleScreenShare(false) else meetingClient.requestScreenShare() }
    }
    fun sendChatMessage(text: String) {
        val preview = _replyTo.value
        viewModelScope.launch { meetingClient.sendChatMessage(text, preview); _replyTo.value = null }
    }
    fun setReplyTo(preview: ReplyPreview?) { _replyTo.value = preview }
    fun onMessageReplyClick(msg: LiveChatMessage) {
        _replyTo.value = ReplyPreview(msg.id, msg.senderName, msg.senderRole, msg.text)
    }
    fun toggleParticipantsPanel() {
        val next = !_showParticipants.value
        _showParticipants.value = next
        if (next) _showChat.value = false
    }
    fun toggleChatPanel() {
        val next = !_showChat.value
        _showChat.value = next
        if (next) _showParticipants.value = false
    }
    fun closePanels() { _showParticipants.value = false; _showChat.value = false }
    fun setWhiteboardTool(tool: WhiteboardTool) { _whiteboardTool.value = tool }
    fun updateWhiteboard(elements: List<WhiteboardElement>) { viewModelScope.launch { meetingClient.updateWhiteboard(elements) } }
    fun kickParticipant(uid: String) { viewModelScope.launch { meetingClient.kickParticipant(uid) } }
    fun assignStarCast(uid: String) { viewModelScope.launch { meetingClient.assignStarCast(uid) } }
    fun revokeStarCast(uid: String) { viewModelScope.launch { meetingClient.revokeStarCast(uid) } }
    fun grantScreenShare(uid: String) { viewModelScope.launch { meetingClient.grantScreenShare(uid) } }
    fun revokeScreenShare(uid: String) { viewModelScope.launch { meetingClient.revokeScreenShare(uid) } }
    fun startRecording() { viewModelScope.launch { meetingClient.startRecording() } }
    fun stopRecording() { viewModelScope.launch { meetingClient.stopRecording() } }
}