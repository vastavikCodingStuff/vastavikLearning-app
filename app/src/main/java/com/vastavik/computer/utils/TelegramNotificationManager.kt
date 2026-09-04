package com.vastavik.computer.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.vastavik.computer.MainActivity
import com.vastavik.computer.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TelegramBannerData(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderName: String,
    val message: String,
    val avatarLetter: String = senderName.firstOrNull()?.uppercase() ?: "S",
    val time: String = "Just now",
    val type: TelegramBannerType = TelegramBannerType.MESSAGE,
    val onAction: (() -> Unit)? = null,
    val actionLabel: String = "View"
)

enum class TelegramBannerType {
    MESSAGE,
    APP_UPDATE,
    LIVE_CLASS
}

object TelegramNotificationManager {

    private val _currentBanner = MutableStateFlow<TelegramBannerData?>(null)
    val currentBanner: StateFlow<TelegramBannerData?> = _currentBanner.asStateFlow()

    fun showNotification(banner: TelegramBannerData) {
        _currentBanner.value = banner
    }

    fun dismiss() {
        _currentBanner.value = null
    }

    /**
     * Pops up an in-app Telegram-style floating notification for a student chat message.
     */
    fun showIncomingMessage(
        senderName: String,
        message: String,
        onOpenChat: (() -> Unit)? = null
    ) {
        val banner = TelegramBannerData(
            senderName = senderName,
            message = message,
            avatarLetter = senderName.firstOrNull()?.uppercase() ?: "S",
            time = "Just now",
            type = TelegramBannerType.MESSAGE,
            actionLabel = "Reply",
            onAction = {
                dismiss()
                onOpenChat?.invoke()
            }
        )
        _currentBanner.value = banner
    }

    /**
     * Pops up an in-app Telegram-style floating notification for an app update.
     */
    fun showUpdateAlert(
        version: String,
        title: String,
        onUpdateClick: () -> Unit
    ) {
        val banner = TelegramBannerData(
            senderName = "Vastavik Updater",
            message = "New Version v$version available: $title. Tap to download APK.",
            avatarLetter = "⬆",
            time = "Update",
            type = TelegramBannerType.APP_UPDATE,
            actionLabel = "Install",
            onAction = {
                dismiss()
                onUpdateClick()
            }
        )
        _currentBanner.value = banner
    }

    /**
     * Posts a Telegram-styled Android system notification using NotificationCompat.MessagingStyle.
     */
    fun postTelegramStyleSystemNotification(
        context: Context,
        senderName: String,
        message: String,
        screen: String = "chat"
    ) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channelId = "vastavik_telegram_messages"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Student Messages",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Telegram-style student-to-student messages"
                    enableVibration(true)
                    enableLights(true)
                }
                nm.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", screen)
            }

            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getActivity(context, 3001, intent, pendingIntentFlags)

            val sender = Person.Builder()
                .setName(senderName)
                .setKey(senderName.lowercase())
                .build()

            val messagingStyle = NotificationCompat.MessagingStyle(sender)
                .setConversationTitle("Student Chat")
                .addMessage(message, System.currentTimeMillis(), sender)

            val appIconBitmap = AppUpdater.getAppIconBitmap(context)
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setStyle(messagingStyle)
                .setColor(0xFF2AABEE.toInt()) // Telegram Cyan-Blue
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            if (appIconBitmap != null) {
                builder.setLargeIcon(appIconBitmap)
            }

            nm.notify(3001, builder.build())
        } catch (e: Exception) {
            android.util.Log.e("TelegramNotification", "Failed to post system notification: ${e.message}")
        }
    }
}
