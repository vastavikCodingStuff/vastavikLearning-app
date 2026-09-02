package com.vastavik.computer.ui.theme

import androidx.compose.ui.graphics.Color

object NeoBrutalistColors {
    // 6 accent color options
    val Yellow = Color(0xFFFFE500)   // Electric Yellow (default)
    val Pink = Color(0xFFFF2D78)     // Hot Pink
    val Blue = Color(0xFF2563EB)     // Material Blue 600
    val Lime = Color(0xFF00FF66)     // Lime Green
    val Orange = Color(0xFFFF6600)   // Orange
    val Purple = Color(0xFF9933FF)   // Purple

    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)
    val OffWhite = Color(0xFFF5F5F5)

    // Light neobrutalist backgrounds
    val LightBg = Color(0xFFFFFFFF)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceVariant = Color(0xFFF5F5F5)
    val LightTextPrimary = Color(0xFF000000)
    val LightTextSecondary = Color(0xFF333333)

    // Dark neobrutalist backgrounds
    val DarkBg = Color(0xFF1A1A1A)
    val DarkSurface = Color(0xFF2A2A2A)
    val DarkSurfaceVariant = Color(0xFF333333)
    val DarkTextPrimary = Color(0xFFFFFFFF)
    val DarkTextSecondary = Color(0xFFCCCCCC)

    fun accentByIndex(index: Int): Color = when (index) {
        0 -> Yellow
        1 -> Pink
        2 -> Blue
        3 -> Lime
        4 -> Orange
        5 -> Purple
        else -> Yellow
    }

    val accentNames = listOf("Yellow", "Pink", "Blue", "Lime", "Orange", "Purple")
}
