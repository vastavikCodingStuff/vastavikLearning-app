package com.vastavik.computer.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import java.io.File

data class SecurityIssue(
    val id: String,
    val title: String,
    val description: String,
    val stepsToFix: String
)

object DeviceSecurityChecker {

    fun checkAll(context: Context): List<SecurityIssue> {
        val issues = mutableListOf<SecurityIssue>()

        if (isUsbDebuggingEnabled(context)) {
            issues.add(
                SecurityIssue(
                    id = "usb_debugging",
                    title = "USB Debugging is ON",
                    description = "USB debugging allows data access via USB connection. This is a security risk.",
                    stepsToFix = "Go to Settings > Developer Options > Turn OFF USB Debugging"
                )
            )
        }

        if (isDeveloperOptionsEnabled(context)) {
            issues.add(
                SecurityIssue(
                    id = "developer_options",
                    title = "Developer Options are ON",
                    description = "Developer options expose advanced settings that can compromise device security.",
                    stepsToFix = "Go to Settings > System > Developer Options > Turn OFF Developer Options"
                )
            )
        }

        if (isDeviceRooted()) {
            issues.add(
                SecurityIssue(
                    id = "rooted",
                    title = "Device is Rooted",
                    description = "Root access bypasses all Android security restrictions. This app cannot run on rooted devices.",
                    stepsToFix = "Unroot your device by removing root access from your root manager app (e.g., Magisk > Uninstall). A factory reset may be required."
                )
            )
        }

        if (isBootloaderUnlocked()) {
            issues.add(
                SecurityIssue(
                    id = "bootloader_unlocked",
                    title = "Bootloader is Unlocked",
                    description = "An unlocked bootloader allows unauthorized OS modifications and compromises device integrity.",
                    stepsToFix = "Re-lock your bootloader via fastboot: reboot to bootloader > run 'fastboot flashing lock'. Warning: this may erase data."
                )
            )
        }

        if (isYouTubeDisabled(context)) {
            issues.add(
                SecurityIssue(
                    id = "youtube_disabled",
                    title = "YouTube is Disabled or Not Installed",
                    description = "YouTube is required for video lessons in this app. Please enable or install it.",
                    stepsToFix = "Go to Settings > Apps > YouTube > Enable. Or install YouTube from the Play Store."
                )
            )
        }

        return issues
    }

    private fun isUsbDebuggingEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (_: Exception) {
            false
        }
    }

    private fun isDeveloperOptionsEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            ) == 1
        } catch (_: Exception) {
            false
        }
    }

    private fun isDeviceRooted(): Boolean {
        val rootPaths = listOf(
            "/system/app/Superuser.apk",
            "/system/xbin/su",
            "/system/bin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
            "/system/app/SuperSU.apk",
            "/system/app/SuperSU",
            "/system/app/eu.chainfire.supersu",
            "/system/app/com.topjohnwu.magisk"
        )

        for (path in rootPaths) {
            if (File(path).exists()) return true
        }

        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) return true

        try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val reader = process.inputStream.bufferedReader()
            val result = reader.readLine()
            reader.close()
            if (result != null && result.isNotEmpty()) return true
        } catch (_: Exception) {}

        return false
    }

    private fun isBootloaderUnlocked(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("getprop", "ro.boot.flash.locked"))
            val reader = process.inputStream.bufferedReader()
            val result = reader.readLine()?.trim()
            reader.close()
            result == "0" || result == "unlocked"
        } catch (_: Exception) {
            false
        }
    }

    private fun isYouTubeDisabled(context: Context): Boolean {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo("com.google.android.youtube", 0)
            !info.enabled
        } catch (_: PackageManager.NameNotFoundException) {
            true
        }
    }
}
