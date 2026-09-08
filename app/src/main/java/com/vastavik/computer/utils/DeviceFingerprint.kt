package com.vastavik.computer.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import java.util.UUID

object DeviceFingerprint {

    private const val PREF_NAME = "device_fingerprint"
    private const val KEY_UUID = "app_uuid"

    @SuppressLint("HardwareIds")
    fun get(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val androidId = try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
        } catch (_: Exception) {
            ""
        }
        val uuid = prefs.getString(KEY_UUID, null) ?: run {
            val generated = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_UUID, generated).apply()
            generated
        }
        val raw = "${androidId}_${Build.PRODUCT}_${uuid}"
        return UUID.nameUUIDFromBytes(raw.toByteArray()).toString()
    }

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER?.replaceFirstChar { it.titlecase() } ?: "Android"
        val model = Build.MODEL ?: "Device"
        return "$manufacturer $model".trim()
    }

    fun getPlatform(): String = "android-${Build.VERSION.RELEASE}"
}
