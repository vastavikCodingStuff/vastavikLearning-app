package com.vastavik.computer.data.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import kotlinx.serialization.Serializable

@Serializable
data class UserModel(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val dateOfBirth: String = "",
    val school: String = "",
    val studentClass: String = "",
    val board: String = "ICSE",
    val preferredLanguage: String = "Java",
    val role: String = "student",
    val isPremium: Boolean = false,
    val photoUrl: String = "",
    val createdAt: String = "",
    val subscriptionStatus: String = "free",
    val subscriptionExpiresAt: String = "",
    val streakCount: Int = 0,
    val lastActiveDate: String = "",
    val totalLessonsCompleted: Int = 0,
    val theme: String = "system"
) {
    fun toMap(): Map<String, Any> = mapOf(
        "uid" to uid,
        "name" to name,
        "email" to email,
        "dateOfBirth" to dateOfBirth,
        "school" to school,
        "studentClass" to studentClass,
        "board" to board,
        "preferredLanguage" to preferredLanguage,
        "role" to role,
        "isPremium" to isPremium,
        "photoUrl" to photoUrl,
        "createdAt" to if (createdAt.isEmpty()) FieldValue.serverTimestamp() else createdAt,
        "subscriptionStatus" to subscriptionStatus,
        "subscriptionExpiresAt" to subscriptionExpiresAt,
        "streakCount" to streakCount,
        "lastActiveDate" to lastActiveDate,
        "totalLessonsCompleted" to totalLessonsCompleted,
        "theme" to theme
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): UserModel {
            val data = doc.data ?: return UserModel(uid = doc.id)
            return UserModel(
                uid = doc.id,
                name = data["name"] as? String ?: "",
                email = data["email"] as? String ?: "",
                dateOfBirth = data["dateOfBirth"] as? String ?: "",
                school = data["school"] as? String ?: "",
                studentClass = data["studentClass"] as? String ?: "",
                board = data["board"] as? String ?: "ICSE",
                preferredLanguage = data["preferredLanguage"] as? String ?: "Java",
                role = data["role"] as? String ?: "student",
                isPremium = data["isPremium"] as? Boolean ?: false,
                photoUrl = data["photoUrl"] as? String ?: "",
                createdAt = data["createdAt"]?.toString() ?: "",
                subscriptionStatus = data["subscriptionStatus"] as? String ?: "free",
                subscriptionExpiresAt = data["subscriptionExpiresAt"]?.toString() ?: "",
                streakCount = data["streakCount"] as? Int ?: 0,
                lastActiveDate = data["lastActiveDate"]?.toString() ?: "",
                totalLessonsCompleted = data["totalLessonsCompleted"] as? Int ?: 0,
                theme = data["theme"] as? String ?: "system"
            )
        }
    }
}