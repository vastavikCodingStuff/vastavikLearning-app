package com.vastavik.computer.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vastavik.computer.MainActivity
import com.vastavik.computer.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token: $token")
        sendTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        // Handle live class started event (drives notification banner + lobby flow)
        val event = remoteMessage.data["event"] ?: remoteMessage.data["type"]
        if (event == "class-started" || event == "class_started") {
            val classId = remoteMessage.data["classId"] ?: remoteMessage.data["class_id"] ?: ""
            val topic = remoteMessage.data["topic"] ?: remoteMessage.data["title"] ?: "Live Class"
            if (classId.isNotEmpty()) {
                MeetingNotificationManager(this).showClassLiveNotification(classId, topic)
                return
            }
        }
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Vastavik Computers"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: ""
        val screen = remoteMessage.data["screen"]
        val screenId = remoteMessage.data["screenId"]

        showNotification(title, body, screen, screenId)
    }

    private fun showNotification(
        title: String,
        body: String,
        screen: String? = null,
        screenId: String? = null
    ) {
        val channelId = "vastavik_notifications"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Vastavik Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications from Vastavik Computers"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (screen != null) putExtra("navigate_to", screen)
            if (screenId != null) putExtra("screen_id", screenId)
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_ONE_SHOT
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        notificationManager.notify(notificationId, notification)
    }

    private fun sendTokenToServer(token: String) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d(TAG, "FCM token updated in Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update FCM token", e)
            }
    }

    companion object {
        private const val TAG = "FCMService"
    }
}
