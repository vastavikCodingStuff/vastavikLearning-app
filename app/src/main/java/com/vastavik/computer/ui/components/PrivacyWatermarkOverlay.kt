package com.vastavik.computer.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

/**
 * Forensic Anti-Leak Watermark Overlay.
 * 
 * Renders a subtle, non-intrusive repeating diagonal watermark containing the student's email/UID.
 * Does not block touch interactions or hinder reading, but ensures that any external screenshot,
 * screen recording (via BlueStacks host or external camera), or photo has forensic traceability.
 */
@Composable
fun PrivacyWatermarkOverlay(
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    if (!enabled) return

    val currentUser = FirebaseAuth.getInstance().currentUser
    val watermarkText = remember(currentUser?.email, currentUser?.uid) {
        val userIdentifier = currentUser?.email?.takeIf { it.isNotBlank() }
            ?: currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: "Protected User"
        val uidPart = currentUser?.uid?.take(8) ?: "V-SECURE"
        "$userIdentifier • $uidPart • Vastavik Learning"
    }

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val textColor = if (isDark) Color.White.copy(alpha = 0.055f) else Color.Black.copy(alpha = 0.045f)
    val density = LocalDensity.current

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val paint = Paint().apply {
            color = textColor.toArgb()
            textSize = with(density) { 12.sp.toPx() }
            isAntiAlias = true
            letterSpacing = 0.04f
        }

        val stepX = with(density) { 260.dp.toPx() }
        val stepY = with(density) { 140.dp.toPx() }

        val canvasWidth = size.width
        val canvasHeight = size.height

        drawContext.canvas.nativeCanvas.apply {
            save()
            var y = -80f
            var row = 0
            while (y < canvasHeight + 160f) {
                val offsetX = if (row % 2 == 1) stepX / 2 else 0f
                var x = -80f + offsetX
                while (x < canvasWidth + 160f) {
                    save()
                    translate(x, y)
                    rotate(-25f)
                    drawText(watermarkText, 0f, 0f, paint)
                    restore()
                    x += stepX
                }
                y += stepY
                row++
            }
            restore()
        }
    }
}
