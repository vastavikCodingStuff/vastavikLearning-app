package com.vastavik.computer.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.vastavik.computer.MainActivity
import com.vastavik.computer.R

class MeetingForegroundService : Service() {
    companion object {
        const val CHANNEL_ID = "meeting_foreground_service"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_CLASS_ID = "extra_class_id"
        const val EXTRA_CLASS_TOPIC = "extra_class_topic"
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val topic = intent?.getStringExtra(EXTRA_CLASS_TOPIC) ?: "Live Class"
        val classId = intent?.getStringExtra(EXTRA_CLASS_ID) ?: ""
        val notification = buildNotification(topic, classId)
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Active Meeting", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Ongoing meeting call"
                setSound(null, null)
                enableVibration(false)
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(topic: String, classId: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "meeting_inclass")
            putExtra("class_id", classId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Active Meeting: $topic")
            .setContentText("Tap to return to class")
            .setOngoing(true)
            .setContentIntent(pending)
            .build()
    }
}

class MeetingNotificationManager(private val context: Context) {
    private val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "class_notifications"
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "Class Notifications", NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(ch)
        }
    }
    fun showClassLiveNotification(classId: String, topic: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "meeting_lobby")
            putExtra("class_id", classId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$topic is Live!")
            .setContentText("Tap to join the class")
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        nm.notify(2001, notif)
    }
    fun startForegroundService(classId: String, topic: String) {
        val intent = Intent(context, MeetingForegroundService::class.java).apply {
            putExtra(MeetingForegroundService.EXTRA_CLASS_ID, classId)
            putExtra(MeetingForegroundService.EXTRA_CLASS_TOPIC, topic)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ContextCompat.startForegroundService(context, intent) else context.startService(intent)
    }
    fun stopForegroundService() { context.stopService(Intent(context, MeetingForegroundService::class.java)) }
}