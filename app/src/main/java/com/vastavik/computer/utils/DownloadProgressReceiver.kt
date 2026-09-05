package com.vastavik.computer.utils

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DownloadProgressReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_CANCEL -> {
                val version = intent.getStringExtra(AppUpdater.EXTRA_DOWNLOAD_VERSION)
                AppUpdater.cancelCurrentDownload(context, version)
                try {
                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    nm?.cancel(NOTIFICATION_ID_DOWNLOAD)
                } catch (_: Exception) {
                }
            }
            ACTION_INSTALL -> {
                val version = intent.getStringExtra(AppUpdater.EXTRA_DOWNLOAD_VERSION) ?: ""
                val installIntent = AppUpdater.buildInstallIntent(context, version)
                if (installIntent != null) {
                    try {
                        context.startActivity(installIntent)
                    } catch (e: Exception) {
                        Log.e("DownloadProgressReceiver", "Failed to start install intent: ${e.message}")
                    }
                }
                try {
                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    nm?.cancel(NOTIFICATION_ID_DOWNLOAD)
                } catch (_: Exception) {
                }
            }
        }
    }

    companion object {
        const val ACTION_CANCEL = "com.vastavik.computer.action.CANCEL_DOWNLOAD"
        const val ACTION_INSTALL = "com.vastavik.computer.action.INSTALL_DOWNLOAD"
        const val NOTIFICATION_ID_DOWNLOAD = 2002
    }
}
