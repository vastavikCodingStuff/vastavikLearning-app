package com.vastavik.computer

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.vastavik.computer.data.repository.VastavikApiRepository
import com.vastavik.computer.ui.navigation.AppNavHost
import com.vastavik.computer.ui.theme.VastavikTheme
import com.vastavik.computer.utils.AdminSession
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.vastavik.computer.utils.ThemePreferences
import javax.inject.Inject
import androidx.compose.foundation.isSystemInDarkTheme
import android.view.Display
import android.os.Build
import android.util.Log
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.lifecycleScope
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity(), PaymentResultListener {

    @Inject
    lateinit var themePreferences: ThemePreferences

    @Inject
    lateinit var apiRepository: VastavikApiRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enforce FLAG_SECURE immediately on window creation to block all screenshots, recordings, and background caching
        com.vastavik.computer.utils.SecurityProtectionManager.enforceWindowSecurity(window)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        captureGrowthAttribution(intent)

        // Track app-open / app-background so the activity log captures session boundaries.
        com.vastavik.computer.utils.ActivityLog.appOpen(this)
        com.vastavik.computer.utils.ActivityLog.flush(this)

        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Initialize Student Conversation & WebRTC signaling manager
        com.vastavik.computer.data.realtime.StudentConversationManager.initialize(this@MainActivity)

        // Asynchronously check for new app update assets on GitHub and notify the user
        lifecycleScope.launch {
            com.vastavik.computer.utils.AppUpdater.checkGitHubReleaseAndNotify(this@MainActivity)
        }

        // Schedule periodic background GenZ teacher notifications via WorkManager
        com.vastavik.computer.utils.VastavikEngagementWorker.enqueue(this@MainActivity)

        // Request the highest supported refresh rate (60/90/120 FPS) so the
        // Compose UI renders at the device's full display capability.
        setHighRefreshRate()

        setContent {
            val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)
            val isNeo by themePreferences.isNeoBrutalish.collectAsState(initial = true)
            val neoAccentIndex by themePreferences.neoBrutalAccentIndex.collectAsState(initial = 0)

            AdminSession.init(this@MainActivity)
            AdminSession.update(FirebaseAuth.getInstance().currentUser)
            val isAdmin by AdminSession.isAdmin.collectAsState()
            val isEngineLogsEnabled by AdminSession.isEngineLogsEnabled.collectAsState()
            val isBanned by com.vastavik.computer.utils.BanManager.isBanned.collectAsState()
            VastavikTheme(darkTheme = isDarkMode, neoBrutalish = isNeo, neoBrutalAccentIndex = neoAccentIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .statusBarsPadding()
                        .padding(top = 2.dp)
                ) {
                    val navController = rememberNavController()

                    LaunchedEffect(isBanned) {
                        if (isBanned) {
                            navController.navigate("banned") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }

                    AppNavHost(
                        navController = navController,
                        startRoute = getStartRoute(intent)
                    )

                    // Forensic anti-leak watermark overlay across all screens
                    com.vastavik.computer.ui.components.PrivacyWatermarkOverlay()

                    // Telegram-style floating heads-up in-app notification banner
                    com.vastavik.computer.ui.components.TelegramNotificationHost(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter),
                        onNavigate = { route -> navController.navigate(route) }
                    )

                    // NeoBrutalistic No-Internet notification banner
                    com.vastavik.computer.ui.components.NoInternetBannerHost(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
                    )

                    // Emulator security detection banner (when running inside BlueStacks / VM)
                    com.vastavik.computer.ui.components.EmulatorWarningBannerHost(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
                    )

                    // Admin-only floating debug banner showing Vastavik AI call logs (can be toggled in Admin Dashboard)
                    if (isAdmin && isEngineLogsEnabled) {
                        com.vastavik.computer.ui.components.DebugLogBoxOverlay(
                            modifier = Modifier
                                .align(androidx.compose.ui.Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(bottom = 68.dp)
                        )
                    }
                }
            }
        }
    }

    /**
     * Requests the display's maximum supported refresh rate (e.g. 120 Hz)
     * by selecting the best display mode at the current resolution.
     */
    private fun setHighRefreshRate() {
        try {
            val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                this@MainActivity.display
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            }
            val modes = display?.supportedModes
            if (modes != null && modes.isNotEmpty()) {
                val current = display.mode
                // Pick the mode with the highest refresh rate at the current resolution (or highest overall)
                val best = modes
                    .filter { it.physicalWidth == current.physicalWidth && it.physicalHeight == current.physicalHeight }
                    .maxByOrNull { it.refreshRate } ?: modes.maxByOrNull { it.refreshRate }
                if (best != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val params = window.attributes
                    params.preferredDisplayModeId = best.modeId
                    window.attributes = params
                }
            }
        } catch (e: Exception) {
            Log.w("MainActivity", "High refresh rate request failed: ${e.message}")
        }
    }

    override fun onStop() {
        super.onStop()
        com.vastavik.computer.utils.ActivityLog.appBackground(this)
        com.vastavik.computer.utils.ActivityLog.flush(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun getStartRoute(intent: Intent?): String {
        val navigateTo = intent?.getStringExtra("navigate_to")
        val screenId = intent?.getStringExtra("screen_id")

        return when (navigateTo) {
            "app_update" -> "app_update"
            "notifications" -> "notifications"
            "course" -> "home"
            "quiz" -> if (!screenId.isNullOrEmpty()) "quiz_taking/$screenId" else "home"
            "lesson" -> if (!screenId.isNullOrEmpty()) {
                val parts = screenId.split("/")
                if (parts.size == 4) {
                    "video_lesson/${parts[0]}/${parts[1]}/${parts[2]}/${parts[3]}"
                } else "home"
            } else "home"
            "chat" -> "chat"
            "profile" -> "profile"
            "code_editor" -> "code_editor"
            "pyq" -> "pyq"
            "practice" -> "practice"
            "meeting_lobby" -> if (!screenId.isNullOrEmpty()) "meeting_lobby/$screenId" else if (!intent?.getStringExtra("class_id").isNullOrEmpty()) "meeting_lobby/${intent.getStringExtra("class_id")}" else "home"
            "meeting_inclass" -> if (!screenId.isNullOrEmpty()) "meeting_inclass/$screenId" else if (!intent?.getStringExtra("class_id").isNullOrEmpty()) "meeting_inclass/${intent.getStringExtra("class_id")}" else "home"
            "class_started" -> if (!intent?.getStringExtra("class_id").isNullOrEmpty()) "meeting_lobby/${intent.getStringExtra("class_id")}" else "home"
            else -> "splash"
        }
    }

    private fun captureGrowthAttribution(intent: Intent?) {
        val data = intent?.data ?: return
        val prefs = getSharedPreferences("growth_attribution", MODE_PRIVATE)
        val path = data.pathSegments
        when {
            path.size >= 2 && path[0] == "r" -> {
                prefs.edit().putString("pending_referral_code", path[1]).apply()
            }
            path.size >= 2 && path[0] == "s" -> {
                val token = path[1]
                prefs.edit().putString("pending_share_token", token).apply()
                lifecycleScope.launch {
                    try { apiRepository.trackShareClick(token) } catch (_: Exception) {}
                }
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        com.vastavik.computer.utils.RazorpayBridge.onSuccess?.invoke(razorpayPaymentId ?: "")
    }

    override fun onPaymentError(code: Int, description: String?) {
        com.vastavik.computer.utils.RazorpayBridge.onError?.invoke(code, description)
    }
}
