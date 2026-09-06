package com.vastavik.computer.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Manages the optional Vastavik CodeOSS (VS Code + Ubuntu Linux) Extension Pack.
 * Handles:
 * 1. Detecting if the companion APK (com.vastavik.codeoss) is installed.
 * 2. User preference (switch between default editor and CodeOSS).
 * 3. Downloading and triggering installation of the companion APK.
 * 4. Launching the CodeOSS environment with code, language, and question payload.
 */
object CodeOssManager {

    const val COMPANION_PACKAGE_NAME = "com.vastavik.codeoss"
    const val ACTION_OPEN_EDITOR = "com.vastavik.codeoss.OPEN_EDITOR"
    const val EXTRA_CODE = "extra_code"
    const val EXTRA_LANGUAGE = "extra_language"
    const val EXTRA_QUESTION = "extra_question"
    const val EXTRA_ACTION = "extra_action"

    private const val PREFS_NAME = "codeoss_preferences"
    private const val KEY_USE_CODEOSS = "pref_use_codeoss"

    // Default GitHub Release asset download URL for the companion extension
    const val DEFAULT_DOWNLOAD_URL =
        "https://github.com/vastavikCodingStuff/vastavikLearning-app/releases/download/v1.0.35/vastavik-codeoss-extension.apk"

    sealed class DownloadState {
        object Idle : DownloadState()
        data class Downloading(val progress: Int, val downloadedBytes: Long, val totalBytes: Long) : DownloadState()
        data class ReadyToInstall(val apkFile: File) : DownloadState()
        data class Error(val message: String) : DownloadState()
    }

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private var currentDownloadJob: Job? = null

    /**
     * Checks if the CodeOSS companion APK is installed on the user's Android device.
     */
    fun isCompanionInstalled(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    COMPANION_PACKAGE_NAME,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(COMPANION_PACKAGE_NAME, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Retrieves whether the user prefers CodeOSS when installed.
     */
    fun isCodeOssPreferred(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_USE_CODEOSS, true) && isCompanionInstalled(context)
    }

    /**
     * Updates user preference for CodeOSS.
     */
    fun setCodeOssPreferred(context: Context, preferred: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_USE_CODEOSS, preferred).apply()
    }

    /**
     * Returns the cached APK file location for the companion extension.
     */
    fun getCompanionApkFile(context: Context): File {
        val dir = File(context.cacheDir, "extensions").apply { if (!exists()) mkdirs() }
        return File(dir, "vastavik-codeoss-extension.apk")
    }

    /**
     * Downloads the companion APK in the background and reports progress.
     */
    fun downloadCompanion(
        context: Context,
        urlStr: String = DEFAULT_DOWNLOAD_URL,
        onComplete: ((File) -> Unit)? = null
    ) {
        currentDownloadJob?.cancel()
        currentDownloadJob = CoroutineScope(Dispatchers.IO).launch {
            val targetFile = getCompanionApkFile(context)
            if (targetFile.exists()) {
                targetFile.delete()
            }

            try {
                _downloadState.value = DownloadState.Downloading(0, 0, 0)
                var currentUrl = urlStr
                var conn: HttpURLConnection? = null
                var redirectCount = 0

                while (redirectCount < 5) {
                    val url = URL(currentUrl)
                    conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        setRequestProperty("User-Agent", "Vastavik-Android-App")
                        connectTimeout = 15000
                        readTimeout = 30000
                        instanceFollowRedirects = true
                    }
                    val code = conn.responseCode
                    if (code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP || code == 307 || code == 308) {
                        val newLoc = conn.getHeaderField("Location") ?: break
                        conn.disconnect()
                        currentUrl = newLoc
                        redirectCount++
                    } else {
                        break
                    }
                }

                if (conn == null || conn.responseCode != HttpURLConnection.HTTP_OK) {
                    _downloadState.value = DownloadState.Error("Server returned code ${conn?.responseCode ?: -1}")
                    return@launch
                }

                val totalBytes = conn.contentLength.toLong().coerceAtLeast(1L)
                var downloadedBytes = 0L

                conn.inputStream.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        val buffer = ByteArray(8192)
                        var read: Int
                        var lastReportedProgress = -1

                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            downloadedBytes += read
                            val progress = ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)

                            if (progress != lastReportedProgress) {
                                lastReportedProgress = progress
                                _downloadState.value = DownloadState.Downloading(progress, downloadedBytes, totalBytes)
                            }
                        }
                        output.flush()
                    }
                }

                _downloadState.value = DownloadState.ReadyToInstall(targetFile)
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(targetFile)
                    val installIntent = buildInstallIntent(context, targetFile)
                    if (installIntent != null) {
                        context.startActivity(installIntent)
                    }
                }
            } catch (e: Exception) {
                _downloadState.value = DownloadState.Error(e.message ?: "Failed to download extension")
            }
        }
    }

    /**
     * Creates an Intent to install the downloaded companion APK.
     */
    fun buildInstallIntent(context: Context, file: File): Intent? {
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

    /**
     * Resets the download state back to Idle.
     */
    fun resetDownloadState() {
        _downloadState.value = DownloadState.Idle
    }

    /**
     * Attempts to install the companion APK directly from packaged app assets if bundled offline.
     */
    fun installFromAssetsIfAvailable(context: Context): Boolean {
        return try {
            val assetList = context.assets.list("companion") ?: emptyArray()
            val match = assetList.firstOrNull { it.endsWith(".apk") } ?: return false
            val targetFile = getCompanionApkFile(context)
            context.assets.open("companion/$match").use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            val intent = buildInstallIntent(context, targetFile)
            if (intent != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Launches the CodeOSS companion app via intent, passing active code, language, problem question, and action.
     */
    fun launchCodeOss(
        context: Context,
        code: String = "",
        language: String = "Python",
        question: String = "",
        action: String = "EDIT"
    ): Boolean {
        return try {
            val intent = Intent(ACTION_OPEN_EDITOR).apply {
                setPackage(COMPANION_PACKAGE_NAME)
                putExtra(EXTRA_CODE, code)
                putExtra(EXTRA_LANGUAGE, language)
                putExtra(EXTRA_QUESTION, question)
                putExtra(EXTRA_ACTION, action)
                putExtra("extra_mistral_api_key", com.vastavik.computer.BuildConfig.MISTRAL_API_KEY)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(COMPANION_PACKAGE_NAME)
                if (launchIntent != null) {
                    launchIntent.putExtra(EXTRA_CODE, code)
                    launchIntent.putExtra(EXTRA_LANGUAGE, language)
                    launchIntent.putExtra(EXTRA_QUESTION, question)
                    launchIntent.putExtra(EXTRA_ACTION, action)
                    launchIntent.putExtra("extra_mistral_api_key", com.vastavik.computer.BuildConfig.MISTRAL_API_KEY)
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    true
                } else {
                    false
                }
            }
        } catch (_: Exception) {
            false
        }
    }
}
