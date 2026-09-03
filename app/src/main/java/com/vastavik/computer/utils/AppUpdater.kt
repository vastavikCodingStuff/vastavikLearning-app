package com.vastavik.computer.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.vastavik.computer.data.model.AppUpdateInfo
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppUpdater {

    fun getApkFile(context: Context, version: String): File =
        File(context.cacheDir, "vastavik_update_$version.apk")

    /**
     * Downloads the latest APK into the app's private cache directory.
     * The cache dir is only accessible by this app (and root), so the APK
     * cannot be removed or intercepted by the user through file managers.
     * The file is version-scoped so a stale cached APK is never installed.
     */
    suspend fun downloadApk(context: Context, info: AppUpdateInfo): File? = withContext(Dispatchers.IO) {
        val url = info.apkUrl
        val version = info.latestVersion
        if (url.isBlank() || version.isBlank()) return@withContext null
        return@withContext try {
            val conn = java.net.URL(url).openConnection()
            conn.connectTimeout = 30000
            conn.readTimeout = 30000
            val input = conn.getInputStream()
            val target = getApkFile(context, version)
            target.outputStream().use { out ->
                input.copyTo(out)
            }
            input.close()
            if (target.length() > 0) {
                // Purge APKs of other versions so only the current one is kept
                context.cacheDir.listFiles()
                    ?.filter { it.name.startsWith("vastavik_update_") && it != target }
                    ?.forEach { it.delete() }
                target
            } else null
        } catch (_: Exception) {
            null
        }
    }

    fun hasUsableApk(context: Context, version: String): Boolean {
        val file = getApkFile(context, version)
        return file.exists() && file.length() > 0L
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
