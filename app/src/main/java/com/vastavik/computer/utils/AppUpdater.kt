package com.vastavik.computer.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.vastavik.computer.BuildConfig
import com.vastavik.computer.MainActivity
import com.vastavik.computer.R
import com.vastavik.computer.data.model.AppUpdateInfo
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject

object AppUpdater {

    private val _updateState = MutableStateFlow<AppUpdateInfo?>(null)
    val updateState: StateFlow<AppUpdateInfo?> = _updateState.asStateFlow()

    fun getApkFile(context: Context, version: String): File =
        File(context.cacheDir, "vastavik_update_$version.apk")

    fun isNewerVersion(remote: String, current: String): Boolean {
        if (remote.isBlank() || current.isBlank()) return false
        val rParts = remote.trim().removePrefix("v").removePrefix("V").split(".").mapNotNull { it.toIntOrNull() }
        val cParts = current.trim().removePrefix("v").removePrefix("V").split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(rParts.size, cParts.size)
        for (i in 0 until maxLen) {
            val r = rParts.getOrElse(i) { 0 }
            val c = cParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    /**
     * Checks GitHub Releases for the repository vastavikCodingStuff/vastavikLearning-app
     * and identifies if an APK asset is available for download.
     */
    suspend fun checkGitHubRelease(currentVersion: String = BuildConfig.VERSION_NAME): AppUpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/vastavikCodingStuff/vastavikLearning-app/releases/latest")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "Vastavik-Android-App")
                connectTimeout = 15000
                readTimeout = 15000
            }
            if (conn.responseCode == 200) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(text)
                val rawTag = json.optString("tag_name", "")
                val latestVer = rawTag.trim().removePrefix("v").removePrefix("V")
                val releaseTitle = json.optString("name", "v$latestVer")
                val changelog = json.optString("body", "")
                val publishedAt = json.optString("published_at", "")

                var apkDownloadUrl = ""
                var apkSizeBytes = 0L
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    var fallbackUrl = ""
                    var fallbackSize = 0L
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val assetName = asset.optString("name", "")
                        if (assetName.endsWith(".apk", ignoreCase = true)) {
                            if (assetName.contains(latestVer, ignoreCase = true)) {
                                apkDownloadUrl = asset.optString("browser_download_url", "")
                                apkSizeBytes = asset.optLong("size", 0L)
                                break
                            } else if (fallbackUrl.isEmpty()) {
                                fallbackUrl = asset.optString("browser_download_url", "")
                                fallbackSize = asset.optLong("size", 0L)
                            }
                        }
                    }
                    if (apkDownloadUrl.isEmpty()) {
                        apkDownloadUrl = fallbackUrl
                        apkSizeBytes = fallbackSize
                    }
                }

                val isNewer = isNewerVersion(latestVer, currentVersion)
                val info = AppUpdateInfo(
                    latestVersion = latestVer,
                    apkUrl = apkDownloadUrl,
                    forceUpdate = false,
                    changelog = changelog,
                    releaseTitle = releaseTitle,
                    apkSize = apkSizeBytes,
                    publishedAt = publishedAt,
                    isUpdateAvailable = isNewer
                )
                _updateState.value = info
                info
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Fetches all GitHub Releases for the repository to display full update and changelog history.
     * Falls back to bundled historical release data if network/rate-limiting fails.
     */
    suspend fun fetchAllGitHubReleases(currentVersion: String = BuildConfig.VERSION_NAME): List<AppUpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/vastavikCodingStuff/vastavikLearning-app/releases?per_page=30")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "Vastavik-Android-App")
                connectTimeout = 15000
                readTimeout = 15000
            }
            if (conn.responseCode == 200) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = org.json.JSONArray(text)
                val list = mutableListOf<AppUpdateInfo>()
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    val rawTag = json.optString("tag_name", "")
                    val ver = rawTag.trim().removePrefix("v").removePrefix("V")
                    val releaseTitle = json.optString("name", "v$ver")
                    val changelog = json.optString("body", "")
                    val publishedAt = json.optString("published_at", "")

                    var apkDownloadUrl = ""
                    var apkSizeBytes = 0L
                    val assets = json.optJSONArray("assets")
                    if (assets != null) {
                        for (j in 0 until assets.length()) {
                            val asset = assets.getJSONObject(j)
                            val assetName = asset.optString("name", "")
                            if (assetName.endsWith(".apk", ignoreCase = true)) {
                                apkDownloadUrl = asset.optString("browser_download_url", "")
                                apkSizeBytes = asset.optLong("size", 0L)
                                break
                            }
                        }
                    }

                    val isNewer = isNewerVersion(ver, currentVersion)
                    list.add(
                        AppUpdateInfo(
                            latestVersion = ver,
                            apkUrl = apkDownloadUrl,
                            forceUpdate = false,
                            changelog = changelog,
                            releaseTitle = releaseTitle,
                            apkSize = apkSizeBytes,
                            publishedAt = publishedAt,
                            isUpdateAvailable = isNewer
                        )
                    )
                }
                if (list.isNotEmpty()) list else getFallbackReleases(currentVersion)
            } else {
                getFallbackReleases(currentVersion)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getFallbackReleases(currentVersion)
        }
    }

    fun getFallbackReleases(currentVersion: String = BuildConfig.VERSION_NAME): List<AppUpdateInfo> {
        return listOf(
            AppUpdateInfo(
                latestVersion = "1.0.17",
                apkUrl = "https://github.com/vastavikCodingStuff/vastavikLearning-app/releases/download/v1.0.17/app-v1.0.17-debug.apk",
                releaseTitle = "v1.0.17 - Notification Icon Visibility in Expanded View",
                changelog = """
### What's New in this Release:
- **Notification Icon Visibility Fix**: Resolved the issue where expanding notifications displayed a hollow/missing app icon in the header.
- **Crisp Monochrome Status Icon**: Updated `ic_notification` with a clean, scaled vector of the Vastavik Computers logo.
- **Full-Color Large Icon**: Added app icon bitmap rendering to notifications so the full-color logo is visible in both collapsed and expanded views.
                """.trimIndent(),
                apkSize = 71397257L,
                publishedAt = "2026-09-04T02:55:00Z",
                isUpdateAvailable = isNewerVersion("1.0.17", currentVersion)
            ),
            AppUpdateInfo(
                latestVersion = "1.0.16",
                apkUrl = "https://github.com/vastavikCodingStuff/vastavikLearning-app/releases/download/v1.0.16/app-v1.0.16-debug.apk",
                releaseTitle = "v1.0.16 - Notification Cards Color Polish (Blue Update & Red Payment Due)",
                changelog = """
### What's New in this Release:
- **Blue Update Notification**: Updated the New Update notification card in the Notifications screen to solid blue with white text and white badge.
- **Red Payment Due Notification**: Updated the "Payment due in 3 days" notification card to vibrant red with white text and "DUE SOON" badge.
- **Direct Navigation**: Tapping the payment due card navigates immediately to the Pro membership renewal screen.
                """.trimIndent(),
                apkSize = 71380769L,
                publishedAt = "2026-09-04T02:45:00Z",
                isUpdateAvailable = isNewerVersion("1.0.16", currentVersion)
            ),
            AppUpdateInfo(
                latestVersion = "1.0.15",
                apkUrl = "https://github.com/vastavikCodingStuff/vastavikLearning-app/releases/download/v1.0.15/app-v1.0.15-debug.apk",
                releaseTitle = "v1.0.15 - App Update History & All Updates Changelog Viewer",
                changelog = """
### What's New in this Release:
- **Update History Screen**: Added an all-new History viewer accessible directly from the top-right of the App Update screen (to the left of the reload button).
- **All Updates Changelog Viewer**: Clean neo-brutalist cards listing every release, published date, size, and detailed markdown changelog.
- **Direct Download & Installation**: Ability to download and install any release APK directly from the history view.
                """.trimIndent(),
                apkSize = 71364385L,
                publishedAt = "2026-09-04T02:35:00Z",
                isUpdateAvailable = isNewerVersion("1.0.15", currentVersion)
            ),
            AppUpdateInfo(
                latestVersion = "1.0.14",
                apkUrl = "https://github.com/vastavikCodingStuff/vastavikLearning-app/releases/download/v1.0.14/app-v1.0.14-debug.apk",
                releaseTitle = "v1.0.14 - Doubts Media Attachment, Centered Solve Button & 3-Tier Expert Pricing",
                changelog = """
### What's New in this Release:
- **Attach Images & Videos to Doubts**: Students can now attach error screenshots or video recordings in the AI Instant Doubt solver.
- **Centered Solve Button**: Fixed the Solve button layout to center text/icon with reduced horizontal padding, neatly aligned on the right.
- **3-Tier Doubt Expert Pricing Div**: Selectable options for ₹29 (Single Doubt), ₹150 (1-Week Unlimited Pass), and ₹200 (1-Month Unlimited Pass).
- **PhonePe Dynamic UPI Intent**: Direct integration with dynamic amount passing for seamless 1-tap checkout.
                """.trimIndent(),
                apkSize = 71364385L,
                publishedAt = "2026-09-04T02:27:00Z",
                isUpdateAvailable = isNewerVersion("1.0.14", currentVersion)
            ),
            AppUpdateInfo(
                latestVersion = "1.0.13",
                apkUrl = "https://github.com/vastavikCodingStuff/vastavikLearning-app/releases/download/v1.0.13/app-v1.0.13-debug.apk",
                releaseTitle = "v1.0.13 - Payment UI Polish, Home Card Spacing & Selective Download Button",
                changelog = """
### What's New in this Release:
- **Payment UI Polish**: Removed yellow Razorpay/PhonePe gateway buttons. Updated monthly/yearly plans and 50% discount icon to crisp blue.
- **Home Card Spacing**: Added top padding above 3-Step Doubt Solver card for proper visual separation from Hello Student header.
- **Selective Download Button**: Displayed on Home and Learn screens only, omitting it from Practice top bar.
                """.trimIndent(),
                apkSize = 71348001L,
                publishedAt = "2026-09-04T02:20:00Z",
                isUpdateAvailable = isNewerVersion("1.0.13", currentVersion)
            ),
            AppUpdateInfo(
                latestVersion = "1.0.12",
                apkUrl = "https://github.com/vastavikCodingStuff/vastavikLearning-app/releases/download/v1.0.12/app-v1.0.12-debug.apk",
                releaseTitle = "v1.0.12 - Conditional Green Download Button & Clean Markdown Changelog",
                changelog = """
### What's New in this Release:
- **Circular Green Download Button**: Added prominent download indicator on top-right of Home & Learn screens when update is available.
- **Clean Markdown Changelog**: Added markdown parsing component to render bullet points, bold text, and code blocks cleanly.
                """.trimIndent(),
                apkSize = 71348001L,
                publishedAt = "2026-09-04T02:12:00Z",
                isUpdateAvailable = isNewerVersion("1.0.12", currentVersion)
            ),
            AppUpdateInfo(
                latestVersion = "1.0.10",
                apkUrl = "https://github.com/vastavikCodingStuff/vastavikLearning-app/releases/download/v1.0.10/app-v1.0.10-debug.apk",
                releaseTitle = "v1.0.10 - Core Enhancements: WebRTC, Socket.IO, Telegram Notifications, PhonePe",
                changelog = """
### What's New in this Release:
- **WebRTC Live Consultation**: Peer-to-peer audio/video connection for 1-on-1 teacher doubt solving.
- **Socket.IO Signaling Server**: Real-time room management and participant events.
- **Telegram Notifications**: Live channel alerts when student asks doubts or updates are posted.
- **PhonePe UPI**: Quick UPI payment launch.
                """.trimIndent(),
                apkSize = 71348005L,
                publishedAt = "2026-09-04T02:05:00Z",
                isUpdateAvailable = isNewerVersion("1.0.10", currentVersion)
            ),
            AppUpdateInfo(
                latestVersion = "1.0.9",
                apkUrl = "https://github.com/vastavikCodingStuff/vastavikLearning-app/releases/download/v1.0.9/app-v1.0.9-debug.apk",
                releaseTitle = "v1.0.9 - 10 Core Enhancements: WebRTC, Socket.IO, Telegram Notifications, PhonePe",
                changelog = """
### What's New in this Release:
- Integrated WebRTC live video consultation.
- Real-time whiteboard synchronization.
- Telegram notification alerts.
- PhonePe UPI checkout intent.
                """.trimIndent(),
                apkSize = 71652616L,
                publishedAt = "2026-09-04T01:46:00Z",
                isUpdateAvailable = isNewerVersion("1.0.9", currentVersion)
            ),
            AppUpdateInfo(
                latestVersion = "1.0.8",
                apkUrl = "https://github.com/vastavikCodingStuff/vastavikLearning-app/releases/download/v1.0.8/app-v1.0.8-debug.apk",
                releaseTitle = "v1.0.8 - Learn Path Oval Curved Connecting Lines & Auto GitHub Update Scanner",
                changelog = """
### What's New in this Release:
- Oval curved bezier connecting lines between learning path nodes.
- Automatic background GitHub Release asset scanner with notifications.
                """.trimIndent(),
                apkSize = 71431554L,
                publishedAt = "2026-09-04T01:30:00Z",
                isUpdateAvailable = isNewerVersion("1.0.8", currentVersion)
            ),
            AppUpdateInfo(
                latestVersion = "1.0.6",
                apkUrl = "",
                releaseTitle = "v1.0.6 - Default Coding Tab & Left Notification Icon",
                changelog = """
### What's New in this Release:
- Set Coding tab as default landing tab on Practice screen.
- Aligned notification bell icon cleanly to top left.
                """.trimIndent(),
                apkSize = 71167781L,
                publishedAt = "2026-09-03T20:30:00Z",
                isUpdateAvailable = isNewerVersion("1.0.6", currentVersion)
            ),
            AppUpdateInfo(
                latestVersion = "1.0.5",
                apkUrl = "",
                releaseTitle = "v1.0.5 - Notification Buttons, Home Pager Swipe & Practice Updates",
                changelog = """
### What's New in this Release:
- Notification buttons and student profile horizontal pager swipe on Home screen.
- Enhanced Practice screen MCQ and quiz modes.
                """.trimIndent(),
                apkSize = 71167781L,
                publishedAt = "2026-09-03T19:40:00Z",
                isUpdateAvailable = isNewerVersion("1.0.5", currentVersion)
            )
        )
    }

    /**
     * Checks GitHub Releases for new update assets and displays a system notification
     * when a newer version is found.
     */
    suspend fun checkGitHubReleaseAndNotify(
        context: Context,
        currentVersion: String = BuildConfig.VERSION_NAME
    ): AppUpdateInfo? = withContext(Dispatchers.IO) {
        val info = checkGitHubRelease(currentVersion)
        if (info != null && info.isUpdateAvailable) {
            postUpdateNotification(context, info)
            TelegramNotificationManager.showUpdateAlert(
                version = info.latestVersion,
                title = info.releaseTitle,
                onUpdateClick = {
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("navigate_to", "app_update")
                    }
                    context.startActivity(intent)
                }
            )
        }
        info
    }

    /**
     * Posts an Android system notification alerting the user about the GitHub asset update.
     */
    fun postUpdateNotification(context: Context, info: AppUpdateInfo) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channelId = "vastavik_app_updates"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "App Updates",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for new APK updates on GitHub Assets"
                    enableVibration(true)
                    enableLights(true)
                }
                nm.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", "app_update")
            }

            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getActivity(context, 2001, intent, pendingIntentFlags)

            val sizeStr = if (info.apkSize > 0) " (${String.format(java.util.Locale.US, "%.1f MB", info.apkSize / (1024.0 * 1024.0))})" else ""
            val appIconBitmap = getAppIconBitmap(context)
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("New App Update Available: v${info.latestVersion}")
                .setContentText("${info.releaseTitle}$sizeStr. Tap to install.")
                .setStyle(NotificationCompat.BigTextStyle().bigText("${info.releaseTitle}$sizeStr\n\nA newer build is available on GitHub Assets. Tap here to download and install."))
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            if (appIconBitmap != null) {
                builder.setLargeIcon(appIconBitmap)
            }

            nm.notify(2001, builder.build())
        } catch (e: Exception) {
            android.util.Log.e("AppUpdater", "Failed to post update notification: ${e.message}")
        }
    }

    /**
     * Safely renders the app launcher icon (including adaptive icon) into a high-resolution Bitmap
     * for notification largeIcon usage across all Android versions.
     */
    fun getAppIconBitmap(context: Context): Bitmap? {
        return try {
            val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher) ?: return null
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 192
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 192
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Downloads the APK with live progress tracking (0.0 to 1.0).
     */
    suspend fun downloadApkWithProgress(
        context: Context,
        info: AppUpdateInfo,
        onProgress: (bytesRead: Long, totalBytes: Long, progressFloat: Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        val urlStr = info.apkUrl
        val version = info.latestVersion
        if (urlStr.isBlank() || version.isBlank()) return@withContext null
        return@withContext try {
            val url = URL(urlStr)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 30000
                readTimeout = 30000
                instanceFollowRedirects = true
            }

            var actualConn: URLConnection = conn
            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == 307 || responseCode == 308) {
                val newUrl = conn.getHeaderField("Location")
                if (newUrl != null) {
                    actualConn = URL(newUrl).openConnection().apply {
                        connectTimeout = 30000
                        readTimeout = 30000
                    }
                }
            }

            val contentLength = actualConn.contentLengthLong.takeIf { it > 0 } ?: info.apkSize
            val input = actualConn.getInputStream()
            val target = getApkFile(context, version)

            target.outputStream().use { out ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalRead = 0L
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    out.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    val progress = if (contentLength > 0) totalRead.toFloat() / contentLength.toFloat() else 0f
                    onProgress(totalRead, contentLength, progress.coerceIn(0f, 1f))
                }
                out.flush()
            }
            input.close()

            if (target.length() > 0) {
                // Purge APKs of other versions so only the current one is kept
                context.cacheDir.listFiles()
                    ?.filter { it.name.startsWith("vastavik_update_") && it != target }
                    ?.forEach { it.delete() }
                target
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun downloadApk(context: Context, info: AppUpdateInfo): File? =
        downloadApkWithProgress(context, info) { _, _, _ -> }

    fun hasUsableApk(context: Context, version: String): Boolean {
        val file = getApkFile(context, version)
        return file.exists() && file.length() > 0L
    }

    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun createManageUnknownAppSourcesIntent(context: Context): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } else null
    }

    fun buildInstallIntent(context: Context, version: String): Intent? {
        val file = getApkFile(context, version)
        if (!file.exists() || file.length() == 0L) return null
        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (_: Exception) {
            null
        }
    }
}
