package com.vastavik.computer.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.vastavik.computer.MainActivity
import com.vastavik.computer.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Background WorkManager worker that ensures the student receives high-priority
 * engagement alerts with a GenZ teacher tone even when the app is completely closed or killed.
 * Rotates between Quiz, Predict Output, Coding, Board PYQs, and Video content alerts,
 * and automatically verifies GitHub Release updates.
 */
class VastavikEngagementWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    data class EngagementNotification(
        val title: String,
        val message: String,
        val targetScreen: String,
        val category: String
    )

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. Check for GitHub Release updates in background
            val updateInfo = try {
                AppUpdater.checkGitHubRelease()
            } catch (_: Exception) {
                null
            }

            if (updateInfo != null && updateInfo.isUpdateAvailable) {
                AppUpdater.postUpdateNotification(applicationContext, updateInfo)
                // If update notification was posted, do not flood with a second engagement notification right away
                return@withContext Result.success()
            }

            // 2. Rotate through GenZ Teacher engagement alerts
            val prefs = applicationContext.getSharedPreferences("vastavik_engagement_worker", Context.MODE_PRIVATE)
            val lastIndex = prefs.getInt("last_prompt_index", 0)

            val prompts = listOf(
                EngagementNotification(
                    title = "Bro, your daily quiz is getting lonely 💀",
                    message = "5 quick MCQs to keep that academic comeback real. Don't ghost your syllabus fr fr!",
                    targetScreen = "practice",
                    category = "Quiz"
                ),
                EngagementNotification(
                    title = "Code won't debug itself no cap 🧢",
                    message = "New Predict the Output set dropped! Test your brain before compiler catches you slipping.",
                    targetScreen = "practice",
                    category = "Predict Output"
                ),
                EngagementNotification(
                    title = "Time to lock in! ⚡ Daily coding problem ready",
                    message = "One algorithm a day keeps backlog away. Tap to cook some code in the editor rn.",
                    targetScreen = "code_editor",
                    category = "Code Challenge"
                ),
                EngagementNotification(
                    title = "Board exam PYQs just landed 🎯",
                    message = "ICSE & CBSE authentic questions with marking schemes. Level up your prep bestie!",
                    targetScreen = "pyq",
                    category = "Board PYQs"
                ),
                EngagementNotification(
                    title = "New masterclass drop alert 🚀",
                    message = "Skip the doomscrolling, watch 10 mins of high-yield concepts. Your future self says thanks.",
                    targetScreen = "practice",
                    category = "Video Lecture"
                )
            )

            val currentIndex = (lastIndex + 1) % prompts.size
            prefs.edit().putInt("last_prompt_index", currentIndex).apply()

            val selectedPrompt = prompts[currentIndex]
            postEngagementNotification(selectedPrompt)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun postEngagementNotification(prompt: EngagementNotification) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val channelId = "vastavik_genz_engagement"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily GenZ Teacher Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "GenZ teacher reminders for Quizzes, Predict Output, and Coding streaks"
                enableVibration(true)
                enableLights(true)
            }
            nm.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", prompt.targetScreen)
        }

        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, 4001, intent, pendingFlags)

        val appIcon = AppUpdater.getAppIconBitmap(applicationContext)

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(prompt.title)
            .setContentText(prompt.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(prompt.message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(0xFF2563EB.toInt())
            .setContentIntent(pendingIntent)

        if (appIcon != null) {
            builder.setLargeIcon(appIcon)
        }

        nm.notify(9001, builder.build())
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "VastavikGenZEngagementWork"

        /**
         * Enqueues periodic background work every 4 hours with network constraints.
         * Runs even when the app is closed or device restarts.
         */
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<VastavikEngagementWorker>(
                4, TimeUnit.HOURS,
                30, TimeUnit.MINUTES // 30-minute flex window
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        /**
         * Optional one-shot check triggered on boot or user request.
         */
        fun runOnce(context: Context) {
            val oneTimeRequest = OneTimeWorkRequestBuilder<VastavikEngagementWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueue(oneTimeRequest)
        }
    }
}
