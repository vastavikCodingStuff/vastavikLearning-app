package com.vastavik.computer.data.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val isUser: Boolean = true,
    val timestamp: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "text" to text,
        "isUser" to isUser,
        "timestamp" to if (timestamp.isEmpty()) FieldValue.serverTimestamp() else timestamp
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): ChatMessage {
            val data = doc.data ?: return ChatMessage(id = doc.id)
            return ChatMessage(
                id = doc.id,
                text = data["text"] as? String ?: "",
                isUser = data["isUser"] as? Boolean ?: true,
                timestamp = data["timestamp"]?.toString() ?: ""
            )
        }

        fun fromMap(data: Map<String, Any>): ChatMessage {
            return ChatMessage(
                id = data["id"] as? String ?: "",
                text = data["text"] as? String ?: "",
                isUser = data["isUser"] as? Boolean ?: true,
                timestamp = data["timestamp"]?.toString() ?: ""
            )
        }
    }
}

@Serializable
data class ChatSession(
    val id: String = "",
    val title: String = "",
    val userId: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "title" to title,
        "userId" to userId,
        "messages" to messages.map { it.toMap() },
        "createdAt" to if (createdAt.isEmpty()) FieldValue.serverTimestamp() else createdAt,
        "updatedAt" to if (updatedAt.isEmpty()) FieldValue.serverTimestamp() else updatedAt
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): ChatSession {
            val data = doc.data ?: return ChatSession(id = doc.id)
            val messagesData = (data["messages"] as? List<Map<String, Any>>) ?: emptyList()
            return ChatSession(
                id = doc.id,
                title = data["title"] as? String ?: "",
                userId = data["userId"] as? String ?: "",
                messages = messagesData.map { ChatMessage.fromMap(it) },
                createdAt = data["createdAt"]?.toString() ?: "",
                updatedAt = data["updatedAt"]?.toString() ?: ""
            )
        }
    }
}

@Serializable
data class NoteModel(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val challengeId: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "title" to title,
        "content" to content,
        "imageUrl" to imageUrl,
        "challengeId" to challengeId,
        "createdAt" to if (createdAt.isEmpty()) FieldValue.serverTimestamp() else createdAt,
        "updatedAt" to if (updatedAt.isEmpty()) FieldValue.serverTimestamp() else updatedAt
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): NoteModel {
            val data = doc.data ?: return NoteModel(id = doc.id)
            return NoteModel(
                id = doc.id,
                userId = data["userId"] as? String ?: "",
                title = data["title"] as? String ?: "",
                content = data["content"] as? String ?: "",
                imageUrl = data["imageUrl"] as? String ?: "",
                challengeId = data["challengeId"] as? String ?: "",
                createdAt = data["createdAt"]?.toString() ?: "",
                updatedAt = data["updatedAt"]?.toString() ?: ""
            )
        }
    }
}
