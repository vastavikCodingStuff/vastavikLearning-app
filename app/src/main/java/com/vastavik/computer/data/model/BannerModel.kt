package com.vastavik.computer.data.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import kotlinx.serialization.Serializable

@Serializable
data class BannerModel(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val actionLink: String = "",
    val color: Long = 0xFF4F46E5L,
    val order: Int = 0,
    val imageUrl: String = "",
    val createdAt: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "title" to title,
        "subtitle" to subtitle,
        "actionLink" to actionLink,
        "color" to color,
        "order" to order,
        "imageUrl" to imageUrl,
        "createdAt" to if (createdAt.isEmpty()) FieldValue.serverTimestamp() else createdAt
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): BannerModel {
            val data = doc.data ?: return BannerModel(id = doc.id)
            return BannerModel(
                id = doc.id,
                title = data["title"] as? String ?: "",
                subtitle = data["subtitle"] as? String ?: "",
                actionLink = data["actionLink"] as? String ?: "",
                color = (data["color"] as? Number)?.toLong() ?: 0xFF4F46E5L,
                order = (data["order"] as? Number)?.toInt() ?: 0,
                imageUrl = data["imageUrl"] as? String ?: "",
                createdAt = data["createdAt"]?.toString() ?: ""
            )
        }
    }
}

@Serializable
data class PopularTopicModel(
    val id: String = "",
    val title: String = "",
    val duration: String = "",
    val subject: String = "",
    val courseId: String = "",
    val order: Int = 0,
    val createdAt: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "title" to title,
        "duration" to duration,
        "subject" to subject,
        "courseId" to courseId,
        "order" to order,
        "createdAt" to if (createdAt.isEmpty()) FieldValue.serverTimestamp() else createdAt
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): PopularTopicModel {
            val data = doc.data ?: return PopularTopicModel(id = doc.id)
            return PopularTopicModel(
                id = doc.id,
                title = data["title"] as? String ?: "",
                duration = data["duration"] as? String ?: "",
                subject = data["subject"] as? String ?: "",
                courseId = data["courseId"] as? String ?: "",
                order = (data["order"] as? Number)?.toInt() ?: 0,
                createdAt = data["createdAt"]?.toString() ?: ""
            )
        }
    }
}

@Serializable
data class StudentSelection(
    val courseId: String = "",
    val courseName: String = "",
    val visitedParts: List<String> = emptyList(),
    val selectedAt: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "courseId" to courseId,
        "courseName" to courseName,
        "visitedParts" to visitedParts,
        "selectedAt" to if (selectedAt.isEmpty()) FieldValue.serverTimestamp() else selectedAt
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): StudentSelection {
            val data = doc.data ?: return StudentSelection()
            return StudentSelection(
                courseId = data["courseId"] as? String ?: "",
                courseName = data["courseName"] as? String ?: "",
                visitedParts = (data["visitedParts"] as? List<String>) ?: emptyList(),
                selectedAt = data["selectedAt"]?.toString() ?: ""
            )
        }
    }
}
