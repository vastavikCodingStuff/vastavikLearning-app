package com.vastavik.computer.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class ParticipantRole {
    ADMIN,
    STARCAST,
    STUDENT
}

@Serializable
enum class MediaState {
    ON,
    OFF
}

@Serializable
data class Participant(
    val userId: String,
    val displayName: String,
    val role: ParticipantRole = ParticipantRole.STUDENT,
    val micState: MediaState = MediaState.OFF,
    val cameraState: MediaState = MediaState.OFF,
    val handRaised: Boolean = false,
    val isScreenSharing: Boolean = false,
    val hasScreenSharePermission: Boolean = false,
    val joinedAt: Long = System.currentTimeMillis(),
    val leftAt: Long? = null
) {
    val isActive: Boolean get() = leftAt == null
    val starCastTag: String get() = if (role == ParticipantRole.STARCAST) "★ starCast" else ""
}

@Serializable
data class LiveChatMessage(
    val id: String = "",
    val senderId: String,
    val senderName: String,
    val senderRole: ParticipantRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val replyTo: ReplyPreview? = null
) {
    val displayName: String get() = when (senderRole) {
        ParticipantRole.STARCAST -> "$senderName ★"
        ParticipantRole.ADMIN -> "$senderName (Admin)"
        else -> senderName
    }
}

@Serializable
data class ReplyPreview(
    val messageId: String,
    val senderName: String,
    val senderRole: ParticipantRole,
    val text: String
) {
    val truncatedText: String get() = if (text.length > 50) "${text.substring(0, 50)}…" else text
}

@Serializable
data class ClassSession(
    val classId: String,
    val topic: String,
    val adminId: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val isLive: Boolean = true,
    val recording: Boolean = false,
    val participants: Map<String, Participant> = emptyMap(),
    val disabledFeatures: Set<DisabledFeature> = emptySet()
)

@Serializable
enum class DisabledFeature {
    MIC, CAMERA, SCREENSHARE, CAPTIONS, CHAT, RAISE_HAND, EMOJI, RECORDING
}

@Serializable
data class WhiteboardState(
    val elements: List<WhiteboardElement> = emptyList(),
    val viewport: Viewport = Viewport()
)

@Serializable
data class WhiteboardElement(
    val id: String,
    val type: ElementType,
    val points: List<Point> = emptyList(),
    val text: String = "",
    val color: String = "#000000",
    val strokeWidth: Float = 2f,
    val fontSize: Float = 16f,
    val bounds: Rect = Rect()
)

@Serializable
enum class ElementType {
    PEN, ERASER, RECTANGLE, ELLIPSE, LINE, ARROW, TEXT
}

@Serializable
data class Point(val x: Float, val y: Float)

@Serializable
data class Rect(val x: Float = 0f, val y: Float = 0f, val width: Float = 0f, val height: Float = 0f)

@Serializable
data class Viewport(
    val x: Float = 0f,
    val y: Float = 0f,
    val zoom: Float = 1f
)

@Serializable
sealed interface MeetingEvent {
    @Serializable data class Join(
        val classId: String,
        val participant: Participant
    ) : MeetingEvent

    @Serializable data class Leave(
        val classId: String,
        val userId: String
    ) : MeetingEvent

    @Serializable data class ToggleMic(
        val classId: String,
        val userId: String,
        val enabled: Boolean
    ) : MeetingEvent

    @Serializable data class ToggleCamera(
        val classId: String,
        val userId: String,
        val enabled: Boolean
    ) : MeetingEvent

    @Serializable data class ToggleHandRaise(
        val classId: String,
        val userId: String,
        val raised: Boolean
    ) : MeetingEvent

    @Serializable data class ToggleScreenShare(
        val classId: String,
        val userId: String,
        val active: Boolean
    ) : MeetingEvent

    @Serializable data class ScreenShareRequest(
        val classId: String,
        val userId: String
    ) : MeetingEvent

    @Serializable data class ScreenShareGrant(
        val classId: String,
        val targetUserId: String,
        val grantedBy: String
    ) : MeetingEvent

    @Serializable data class ScreenShareRevoke(
        val classId: String,
        val targetUserId: String,
        val revokedBy: String
    ) : MeetingEvent

    @Serializable data class ChatMessageSent(
        val classId: String,
        val message: LiveChatMessage
    ) : MeetingEvent

    @Serializable data class EmojiReaction(
        val classId: String,
        val userId: String,
        val emoji: String
    ) : MeetingEvent

    @Serializable data class KickParticipant(
        val classId: String,
        val targetUserId: String,
        val kickedBy: String
    ) : MeetingEvent

    @Serializable data class AssignStarCast(
        val classId: String,
        val targetUserId: String,
        val assignedBy: String
    ) : MeetingEvent

    @Serializable data class RevokeStarCast(
        val classId: String,
        val targetUserId: String,
        val revokedBy: String
    ) : MeetingEvent

    @Serializable data class FeatureToggle(
        val classId: String,
        val feature: DisabledFeature,
        val enabled: Boolean,
        val toggledBy: String
    ) : MeetingEvent

    @Serializable data class RecordingStart(
        val classId: String,
        val startedBy: String
    ) : MeetingEvent

    @Serializable data class RecordingStop(
        val classId: String,
        val stoppedBy: String
    ) : MeetingEvent

    @Serializable data class ClassStarted(
        val classId: String,
        val topic: String,
        val adminId: String
    ) : MeetingEvent

    @Serializable data class WhiteboardUpdate(
        val classId: String,
        val elements: List<WhiteboardElement>,
        val updatedBy: String
    ) : MeetingEvent

    @Serializable data class ParticipantStatusUpdate(
        val classId: String,
        val userId: String,
        val micState: MediaState? = null,
        val cameraState: MediaState? = null,
        val handRaised: Boolean? = null,
        val isScreenSharing: Boolean? = null
    ) : MeetingEvent
}

@Serializable
data class AuditLogEntry(
    val id: String = "",
    val classId: String,
    val eventType: String,
    val actorId: String,
    val actorRole: ParticipantRole,
    val targetId: String? = null,
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)