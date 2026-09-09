package com.vastavik.computer.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.Window
import android.view.WindowManager
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Security Protection Manager for Vastavik Learning.
 * 
 * Provides:
 * 1. Immediate and unconditional enforcement of WindowManager.LayoutParams.FLAG_SECURE
 *    to prevent screenshots, screen recordings, Recent Apps caching, and screen mirroring.
 * 2. Real-time heuristic detection of emulators (BlueStacks, Nox, Genymotion, QEMU, LDPlayer).
 * 3. Root detection heuristics to alert or protect against memory hooking and screen scraping.
 */
object SecurityProtectionManager {

    /**
     * Applies WindowManager.LayoutParams.FLAG_SECURE immediately to the target window.
     * Must be called in Activity.onCreate() before any layout inflation.
     */
    fun enforceWindowSecurity(window: Window) {
        try {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } catch (_: Exception) { }
    }

    /**
     * Checks if the application is executing inside an Android emulator (BlueStacks, Nox, QEMU, etc.).
     */
    fun isRunningOnEmulator(): Boolean {
        // Check Build Properties
        val isGenericFingerprint = Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.FINGERPRINT.contains("vbox") ||
                Build.FINGERPRINT.contains("test-keys") && Build.FINGERPRINT.contains("sdk")

        val isGenericModel = Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MODEL.contains("BlueStacks") ||
                Build.MODEL.contains("Droid4X") ||
                Build.MODEL.contains("TIANYU") ||
                Build.MODEL.contains("NOX")

        val isGenericManufacturer = Build.MANUFACTURER.contains("Genymotion") ||
                Build.MANUFACTURER.contains("BlueStacks") ||
                Build.MANUFACTURER.contains("Nox")

        val isGenericBrand = (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                Build.BRAND.equals("generic", ignoreCase = true)

        val isGenericDevice = Build.DEVICE.startsWith("generic") ||
                Build.DEVICE.contains("vbox86") ||
                Build.DEVICE.contains("emulator")

        val isGenericProduct = Build.PRODUCT.contains("sdk") ||
                Build.PRODUCT.contains("google_sdk") ||
                Build.PRODUCT.contains("sdk_x86") ||
                Build.PRODUCT.contains("vbox86p") ||
                Build.PRODUCT.contains("emulator") ||
                Build.PRODUCT.contains("bluestacks")

        val isGenericHardware = Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("vbox86") ||
                Build.HARDWARE.contains("nox") ||
                Build.HARDWARE.contains("ttVM_x86")

        val isGenericBoard = Build.BOARD.contains("goldfish") ||
                Build.BOARD.contains("unknown") ||
                Build.BOARD.contains("vbox86")

        if (isGenericFingerprint || isGenericModel || isGenericManufacturer ||
            isGenericBrand || isGenericDevice || isGenericProduct || isGenericHardware || isGenericBoard) {
            return true
        }

        // Check specific BlueStacks and emulator filesystem indicators
        val emulatorPaths = arrayOf(
            "/sdcard/windows/BstSharedFolder",
            "/mnt/windows/BstSharedFolder",
            "/system/bin/bstk/",
            "/dev/qemu_pipe",
            "/dev/socket/qemud",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/nox-prop",
            "/dev/socket/genyd",
            "/dev/socket/baseband_genyd"
        )

        for (path in emulatorPaths) {
            try {
                val file = File(path)
                if (file.exists()) {
                    return true
                }
            } catch (_: Exception) { }
        }

        return false
    }

    /**
     * Checks if the device appears to have root access enabled (common in emulators and modded OS).
     */
    fun isDeviceRooted(): Boolean {
        // Check test-keys
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        // Check for su binaries
        val suPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )

        for (path in suPaths) {
            try {
                if (File(path).exists()) return true
            } catch (_: Exception) { }
        }

        return false
    }

    private val _isWindowFocused = kotlinx.coroutines.flow.MutableStateFlow(true)
    val isWindowFocused: kotlinx.coroutines.flow.StateFlow<Boolean> = _isWindowFocused

    private val _isScreenshotBlackoutActive = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isScreenshotBlackoutActive: kotlinx.coroutines.flow.StateFlow<Boolean> = _isScreenshotBlackoutActive

    private var blackoutJob: kotlinx.coroutines.Job? = null
    private val securityScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main + kotlinx.coroutines.SupervisorJob())

    /**
     * Called whenever Activity window focus changes.
     * When focus is lost (e.g. Windows Snipping Tool, Win+Shift+S, Alt+Tab, Taskbar click),
     * the screen is immediately blacked out before the host OS can sample any pixels.
     */
    fun setWindowFocused(focused: Boolean) {
        _isWindowFocused.value = focused
        if (!focused) {
            _isScreenshotBlackoutActive.value = true
        } else {
            securityScope.launch {
                kotlinx.coroutines.delay(350)
                if (_isWindowFocused.value) {
                    _isScreenshotBlackoutActive.value = false
                }
            }
        }
    }

    /**
     * Triggered when a screenshot key (PrintScreen, SysRq, Ctrl+Shift+S) is intercepted.
     * Keeps the screen blanked for the specified duration to defeat rapid capture.
     */
    fun triggerScreenshotDefense(durationMillis: Long = 4000L) {
        _isScreenshotBlackoutActive.value = true
        blackoutJob?.cancel()
        blackoutJob = securityScope.launch {
            kotlinx.coroutines.delay(durationMillis)
            if (_isWindowFocused.value) {
                _isScreenshotBlackoutActive.value = false
            }
        }
    }
}
