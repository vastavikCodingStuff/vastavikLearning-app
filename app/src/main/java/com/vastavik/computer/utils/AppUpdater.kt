package com.vastavik.computer.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.vastavik.computer.BuildConfig
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
