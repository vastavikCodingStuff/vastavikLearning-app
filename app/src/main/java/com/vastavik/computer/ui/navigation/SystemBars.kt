package com.vastavik.computer.ui.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.vastavik.computer.ui.theme.VastavikColors

@Composable
fun SystemBarStyle(darkTheme: Boolean) {
    val backgroundColor = if (darkTheme) VastavikColors.DarkBackground else VastavikColors.LightBackground
    val isDark = darkTheme
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    SideEffect {
        val window = activity.window
        window.statusBarColor = backgroundColor.toArgb()
        window.navigationBarColor = backgroundColor.toArgb()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = isDark
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = isDark
    }
}
