package com.vastavik.computer.data.model

import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.serialization.Serializable

@Serializable
data class AppUpdateInfo(
    val latestVersion: String = "",
    val apkUrl: String = "",
    val forceUpdate: Boolean = false,
    val changelog: String = ""
) {
    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): AppUpdateInfo {
            val data = doc.data ?: return AppUpdateInfo()
            return AppUpdateInfo(
                latestVersion = data["latestVersion"] as? String ?: "",
                apkUrl = data["apkUrl"] as? String ?: "",
                forceUpdate = data["forceUpdate"] as? Boolean ?: false,
                changelog = data["changelog"] as? String ?: ""
            )
        }
    }
}
