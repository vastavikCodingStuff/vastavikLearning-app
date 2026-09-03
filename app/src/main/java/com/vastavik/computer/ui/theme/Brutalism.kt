package com.vastavik.computer.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object BrutalDefaults {
    val BorderWidth: Dp = 2.dp
    val ShadowOffset: Dp = 5.dp
    val Radius: Dp = 16.dp
    val RadiusLarge: Dp = 20.dp
    val RadiusPill: Dp = 50.dp
    val Black: Color = Color(0xFF000000)
    // Gray borders for dark mode — visible on dark backgrounds, never white bg.
    val GrayBorder: Color = Color(0xFF64748B)
    val GrayBorderVariant: Color = Color(0xFF475569)
}

/**
 * True when the current MaterialTheme is a dark theme.
 * Detected via onBackground luminance: dark mode uses near-white text.
 */
@Composable
fun isAppInDarkTheme(): Boolean {
    val onBg = MaterialTheme.colorScheme.onBackground
    return (onBg.red + onBg.green + onBg.blue) > 1.5f
}

/**
 * Brutalist border color: black in light mode, gray in dark mode.
 * Gray keeps cards visible on dark backgrounds without using white.
 */
@Composable
fun brutalBorderColor(): Color =
    if (isAppInDarkTheme()) BrutalDefaults.GrayBorder else BrutalDefaults.Black

/** Offset "shadow" block behind brutal cards: black in light, dark-gray in dark. */
@Composable
fun brutalShadowColor(): Color =
    if (isAppInDarkTheme()) BrutalDefaults.GrayBorderVariant else BrutalDefaults.Black

@Composable
fun BrutalCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(BrutalDefaults.Radius),
    borderWidth: Dp = BrutalDefaults.BorderWidth,
    shadowOffset: Dp = BrutalDefaults.ShadowOffset,
    borderColor: Color? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val resolvedBorder = borderColor ?: brutalBorderColor()
    val resolvedShadow = brutalShadowColor()
    Box(modifier = modifier.padding(end = shadowOffset, bottom = shadowOffset)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .clip(shape)
                .background(resolvedShadow)
        )
        Card(
            modifier = Modifier
                .fillMaxSize()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            border = BorderStroke(borderWidth, resolvedBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun BrutalBoxCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(BrutalDefaults.Radius),
    borderWidth: Dp = BrutalDefaults.BorderWidth,
    shadowOffset: Dp = BrutalDefaults.ShadowOffset,
    borderColor: Color? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val resolvedBorder = borderColor ?: brutalBorderColor()
    val resolvedShadow = brutalShadowColor()
    Box(modifier = modifier.padding(end = shadowOffset, bottom = shadowOffset)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .clip(shape)
                .background(resolvedShadow)
        )
        Card(
            modifier = Modifier
                .fillMaxSize()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            border = BorderStroke(borderWidth, resolvedBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), content = content)
        }
    }
}

@Composable
fun BrutalGradientCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(BrutalDefaults.RadiusLarge),
    borderWidth: Dp = BrutalDefaults.BorderWidth,
    shadowOffset: Dp = BrutalDefaults.ShadowOffset,
    gradient: Brush,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val resolvedBorder = borderColor ?: brutalBorderColor()
    val resolvedShadow = brutalShadowColor()
    Box(modifier = modifier.padding(end = shadowOffset, bottom = shadowOffset)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .clip(shape)
                .background(resolvedShadow)
        )
        Card(
            modifier = Modifier
                .fillMaxSize()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(borderWidth, resolvedBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradient)
            ) {
                Column(content = content)
            }
        }
    }
}
