package com.vastavik.computer

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.vastavik.computer.ui.navigation.AppNavHost
import com.vastavik.computer.ui.theme.VastavikTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.vastavik.computer.utils.ThemePreferences
import javax.inject.Inject
import androidx.compose.foundation.isSystemInDarkTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)
            val isNeo by themePreferences.isNeoBrutalish.collectAsState(initial = true)
            val neoAccentIndex by themePreferences.neoBrutalAccentIndex.collectAsState(initial = 0)
            VastavikTheme(darkTheme = isDarkMode, neoBrutalish = isNeo, neoBrutalAccentIndex = neoAccentIndex) {
                val navController = rememberNavController()
                AppNavHost(
                    navController = navController,
                    startRoute = getStartRoute(intent)
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun getStartRoute(intent: Intent?): String {
        val navigateTo = intent?.getStringExtra("navigate_to")
        val screenId = intent?.getStringExtra("screen_id")

        return when (navigateTo) {
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
            "pyq" -> "pyq"
            "practice" -> "practice"
            "meeting_lobby" -> if (!screenId.isNullOrEmpty()) "meeting_lobby/$screenId" else if (!intent?.getStringExtra("class_id").isNullOrEmpty()) "meeting_lobby/${intent.getStringExtra("class_id")}" else "home"
            "meeting_inclass" -> if (!screenId.isNullOrEmpty()) "meeting_inclass/$screenId" else if (!intent?.getStringExtra("class_id").isNullOrEmpty()) "meeting_inclass/${intent.getStringExtra("class_id")}" else "home"
            "class_started" -> if (!intent?.getStringExtra("class_id").isNullOrEmpty()) "meeting_lobby/${intent.getStringExtra("class_id")}" else "home"
            else -> "splash"
        }
    }
}
