package com.vastavik.computer.data.realtime

import com.vastavik.computer.data.model.MediaState
import com.vastavik.computer.data.model.ParticipantRole

enum class WebRtcSignalType {
    OFFER,
    ANSWER,
    ICE_CANDIDATE,
    PEER_JOIN,
    PEER_LEAVE,
    MEDIA_UPDATE,
    SCREEN_SHARE_START,
    SCREEN_SHARE_STOP
}

data class WebRtcSignal(
    val type: WebRtcSignalType,
    val senderId: String,
    val senderName: String,
    val targetId: String? = null,
    val sdp: String? = null,
    val sdpType: String? = null, // "offer" or "answer"
    val candidate: String? = null,
    val sdpMid: String? = null,
    val sdpMLineIndex: Int? = null,
    val micEnabled: Boolean = false,
    val cameraEnabled: Boolean = false,
    val screenSharing: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class StudentChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderId: String,
    val senderName: String,
    val senderRole: ParticipantRole = ParticipantRole.STUDENT,
    val channelId: String = "community",
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val replyToId: String? = null,
    val replyToName: String? = null,
    val replyToText: String? = null
)

data class StudentTypingEvent(
    val senderId: String,
    val senderName: String,
    val isTyping: Boolean,
    val channelId: String = "community"
)
