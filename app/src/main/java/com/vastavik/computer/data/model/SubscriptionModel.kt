package com.vastavik.computer.data.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import kotlinx.serialization.Serializable

@Serializable
data class TransactionModel(
    val id: String = "",
    val uid: String = "",
    val amount: Double = 0.0,
    val currency: String = "INR",
    val status: String = "pending",
    val phonePeTransactionId: String = "",
    val merchantTransactionId: String = "",
    val planId: String = "",
    val planName: String = "",
    val timestamp: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "uid" to uid,
        "amount" to amount,
        "currency" to currency,
        "status" to status,
        "phonePeTransactionId" to phonePeTransactionId,
        "merchantTransactionId" to merchantTransactionId,
        "planId" to planId,
        "planName" to planName,
        "timestamp" to if (timestamp.isEmpty()) FieldValue.serverTimestamp() else timestamp
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): TransactionModel {
            val data = doc.data ?: return TransactionModel(id = doc.id)
            return TransactionModel(
                id = doc.id,
                uid = data["uid"] as? String ?: "",
                amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                currency = data["currency"] as? String ?: "INR",
                status = data["status"] as? String ?: "pending",
                phonePeTransactionId = data["phonePeTransactionId"] as? String ?: "",
                merchantTransactionId = data["merchantTransactionId"] as? String ?: "",
                planId = data["planId"] as? String ?: "",
                planName = data["planName"] as? String ?: "",
                timestamp = data["timestamp"]?.toString() ?: ""
            )
        }
    }
}

@Serializable
data class SubscriptionModel(
    val id: String = "",
    val uid: String = "",
    val planId: String = "none",
    val planName: String = "Free",
    val status: String = "free",
    val startDate: String = "",
    val endDate: String = "",
    val autoRenew: Boolean = false,
    val paymentMethod: String = "",
    val createdAt: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "uid" to uid,
        "planId" to planId,
        "planName" to planName,
        "status" to status,
        "startDate" to startDate,
        "endDate" to endDate,
        "autoRenew" to autoRenew,
        "paymentMethod" to paymentMethod,
        "createdAt" to if (createdAt.isEmpty()) FieldValue.serverTimestamp() else createdAt
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): SubscriptionModel {
            val data = doc.data ?: return SubscriptionModel(id = doc.id, uid = "")
            return SubscriptionModel(
                id = doc.id,
                uid = data["uid"] as? String ?: "",
                planId = data["planId"] as? String ?: "none",
                planName = data["planName"] as? String ?: "Free",
                status = data["status"] as? String ?: "free",
                startDate = data["startDate"] as? String ?: "",
                endDate = data["endDate"] as? String ?: "",
                autoRenew = data["autoRenew"] as? Boolean ?: false,
                paymentMethod = data["paymentMethod"] as? String ?: "",
                createdAt = data["createdAt"]?.toString() ?: ""
            )
        }
    }
}

@Serializable
data class SubscriptionPlan(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val currency: String = "INR",
    val durationDays: Int = 30,
    val features: List<String> = emptyList(),
    val isActive: Boolean = true,
    val order: Int = 0
) {
    fun toMap(): Map<String, Any> = mapOf(
        "name" to name,
        "price" to price,
        "currency" to currency,
        "durationDays" to durationDays,
        "features" to features,
        "isActive" to isActive,
        "order" to order
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): SubscriptionPlan {
            val data = doc.data ?: return SubscriptionPlan(id = doc.id)
            return SubscriptionPlan(
                id = doc.id,
                name = data["name"] as? String ?: "",
                price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                currency = data["currency"] as? String ?: "INR",
                durationDays = (data["durationDays"] as? Number)?.toInt() ?: 30,
                features = (data["features"] as? List<String>) ?: emptyList(),
                isActive = data["isActive"] as? Boolean ?: true,
                order = (data["order"] as? Number)?.toInt() ?: 0
            )
        }
    }
}