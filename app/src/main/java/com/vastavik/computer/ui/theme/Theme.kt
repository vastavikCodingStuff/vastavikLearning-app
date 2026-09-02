package com.vastavik.computer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = VastavikColors.LightPrimary,
    primaryContainer = VastavikColors.LightPrimary.copy(alpha = 0.1f),
    secondary = VastavikColors.LightAccent,
    secondaryContainer = VastavikColors.LightAccent.copy(alpha = 0.1f),
    tertiary = VastavikColors.LightSuccess,
    tertiaryContainer = VastavikColors.LightSuccessContainer,
    error = VastavikColors.LightError,
    errorContainer = VastavikColors.LightErrorContainer,
    background = VastavikColors.LightBackground,
    surface = VastavikColors.LightSurface,
    surfaceVariant = VastavikColors.LightSurfaceVariant,
    onPrimary = Color.White,
    onPrimaryContainer = VastavikColors.LightPrimary,
    onSecondary = Color.White,
    onSecondaryContainer = VastavikColors.LightAccent,
    onTertiary = Color.White,
    onTertiaryContainer = VastavikColors.LightSuccess,
    onError = Color.White,
    onErrorContainer = VastavikColors.LightError,
    onBackground = VastavikColors.LightTextPrimary,
    onSurface = VastavikColors.LightTextPrimary,
    onSurfaceVariant = VastavikColors.LightTextSecondary,
    outline = VastavikColors.LightOutline,
    outlineVariant = VastavikColors.LightOutline.copy(alpha = 0.5f),
    inverseSurface = VastavikColors.DarkSurface,
    inverseOnSurface = VastavikColors.DarkTextPrimary,
    inversePrimary = VastavikColors.DarkPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = VastavikColors.DarkPrimary,
    primaryContainer = VastavikColors.DarkPrimary.copy(alpha = 0.2f),
    secondary = VastavikColors.DarkAccent,
    secondaryContainer = VastavikColors.DarkAccent.copy(alpha = 0.2f),
    tertiary = VastavikColors.DarkSuccess,
    tertiaryContainer = VastavikColors.DarkSuccessContainer,
    error = VastavikColors.DarkError,
    errorContainer = VastavikColors.DarkErrorContainer,
    background = VastavikColors.DarkBackground,
    surface = VastavikColors.DarkSurface,
    surfaceVariant = VastavikColors.DarkSurfaceVariant,
    onPrimary = Color.White,
    onPrimaryContainer = VastavikColors.DarkPrimary,
    onSecondary = Color.White,
    onSecondaryContainer = VastavikColors.DarkAccent,
    onTertiary = Color.White,
    onTertiaryContainer = VastavikColors.DarkSuccess,
    onError = Color.White,
    onErrorContainer = VastavikColors.DarkError,
    onBackground = VastavikColors.DarkTextPrimary,
    onSurface = VastavikColors.DarkTextPrimary,
    onSurfaceVariant = VastavikColors.DarkTextSecondary,
    outline = VastavikColors.DarkOutline,
    outlineVariant = VastavikColors.DarkOutline.copy(alpha = 0.5f),
    inverseSurface = VastavikColors.LightSurface,
    inverseOnSurface = VastavikColors.LightTextPrimary,
    inversePrimary = VastavikColors.LightPrimary
)

@Composable
fun VastavikTheme(
    darkTheme: Boolean = false,
    neoBrutalish: Boolean = false,
    neoBrutalAccentIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val accentColor = NeoBrutalistColors.accentByIndex(neoBrutalAccentIndex)

    val colorScheme = if (neoBrutalish) {
        if (darkTheme) {
            darkColorScheme(
                primary = accentColor,
                primaryContainer = accentColor.copy(alpha = 0.2f),
                secondary = accentColor,
                secondaryContainer = accentColor.copy(alpha = 0.15f),
                tertiary = NeoBrutalistColors.Lime,
                error = Color(0xFFFF4444),
                background = NeoBrutalistColors.DarkBg,
                surface = NeoBrutalistColors.DarkSurface,
                surfaceVariant = NeoBrutalistColors.DarkSurfaceVariant,
                onPrimary = Color.Black,
                onPrimaryContainer = Color.Black,
                onSecondary = Color.Black,
                onSecondaryContainer = Color.Black,
                onTertiary = Color.Black,
                onError = Color.White,
                onErrorContainer = Color.White,
                onBackground = NeoBrutalistColors.DarkTextPrimary,
                onSurface = NeoBrutalistColors.DarkTextPrimary,
                onSurfaceVariant = NeoBrutalistColors.DarkTextSecondary,
                outline = NeoBrutalistColors.Black,
                outlineVariant = NeoBrutalistColors.Black.copy(alpha = 0.3f),
                inverseSurface = NeoBrutalistColors.LightSurface,
                inverseOnSurface = NeoBrutalistColors.LightTextPrimary,
                inversePrimary = accentColor
            )
        } else {
            lightColorScheme(
                primary = accentColor,
                primaryContainer = accentColor.copy(alpha = 0.15f),
                secondary = accentColor,
                secondaryContainer = accentColor.copy(alpha = 0.1f),
                tertiary = Color(0xFF00CC44),
                error = Color(0xFFFF2222),
                background = NeoBrutalistColors.LightBg,
                surface = NeoBrutalistColors.LightSurface,
                surfaceVariant = NeoBrutalistColors.LightSurfaceVariant,
                onPrimary = Color.Black,
                onPrimaryContainer = Color.Black,
                onSecondary = Color.Black,
                onSecondaryContainer = Color.Black,
                onTertiary = Color.White,
                onError = Color.White,
                onErrorContainer = Color.White,
                onBackground = NeoBrutalistColors.LightTextPrimary,
                onSurface = NeoBrutalistColors.LightTextPrimary,
                onSurfaceVariant = NeoBrutalistColors.LightTextSecondary,
                outline = NeoBrutalistColors.Black,
                outlineVariant = NeoBrutalistColors.Black.copy(alpha = 0.3f),
                inverseSurface = NeoBrutalistColors.DarkSurface,
                inverseOnSurface = NeoBrutalistColors.DarkTextPrimary,
                inversePrimary = accentColor
            )
        }
    } else {
        if (darkTheme) DarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VastavikTypography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(12.dp),
            small = RoundedCornerShape(16.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(20.dp),
            extraLarge = RoundedCornerShape(24.dp)
        ),
        content = content
    )
}

val androidx.compose.material3.ColorScheme.appPrimary: Color
    get() = primary
val androidx.compose.material3.ColorScheme.appAccent: Color
    get() = secondary
val androidx.compose.material3.ColorScheme.appBackground: Color
    get() = background
val androidx.compose.material3.ColorScheme.appSurface: Color
    get() = surface
val androidx.compose.material3.ColorScheme.appTextPrimary: Color
    get() = onBackground
val androidx.compose.material3.ColorScheme.appTextSecondary: Color
    get() = onSurfaceVariant
