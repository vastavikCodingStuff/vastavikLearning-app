package com.vastavik.computer.ui.components

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

@Composable
fun NinjaCelebrationOverlay(
    planName: String,
    amount: String,
    paymentMethod: String,
    receiptFile: File?,
    onDownloadReceipt: () -> Unit,
    onEnterApp: () -> Unit
) {
    val context = LocalContext.current
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var soundPlayed by remember { mutableStateOf(false) }

    // 1. Audio Boost to 100% & Voice / Sound Effect
    LaunchedEffect(Unit) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.let { am ->
                val curVol = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                if (curVol == 0 || curVol < maxVol) {
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, AudioManager.FLAG_SHOW_UI)
                }
            }
        } catch (_: Exception) {}

        // Play sword slash tone burst
        try {
            val tg = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 280)
        } catch (_: Exception) {}

        // Initialize TTS and speak energetically
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setPitch(1.3f)      // High energetic pitch
                tts?.setSpeechRate(1.15f) // Punchy, dynamic cadence
                tts?.speak("Get Ready Samurai!!!!", TextToSpeech.QUEUE_FLUSH, null, "samurai_shout")
                soundPlayed = true
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                tts?.stop()
                tts?.shutdown()
            } catch (_: Exception) {}
        }
    }

    // Infinite Animation Transitions
    val infiniteTransition = rememberInfiniteTransition(label = "samurai_anim")

    // Pulsing speech bubble scale
    val bubbleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubbleScale"
    )

    // Katana Slash Arc Animation
    val slashProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "slashProgress"
    )

    // Upward Swipe Bounce
    val swipeOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -16f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swipeOffset"
    )

    // Fullscreen Overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070B14),
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF090D16)
                    )
                )
            )
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    // Swipe from down to up detected!
                    if (dragAmount < -25f) {
                        onEnterApp()
                    }
                }
            }
    ) {
        // Ninja Character & Katana Canvas in Center
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 36.dp, bottom = 140.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                    .border(1.5.dp, Color(0xFF10B981), RoundedCornerShape(50.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "PAYMENT SUCCESSFUL • ₹$amount",
                    color = Color(0xFF34D399),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            // Comic Manga Speech Bubble from Ninja's Mouth
            Box(
                modifier = Modifier
                    .scale(bubbleScale)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFFB703), Color(0xFFFB8500), Color(0xFFE63946))
                        )
                    )
                    .border(3.dp, Color.White, RoundedCornerShape(20.dp))
                    .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFFB8500))
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "GET READY SAMURAI!!!!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 1.2.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "⚔️ Your Training in the Dojo Begins! ⚔️",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(alpha = 0.95f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Custom Canvas Drawing: Ninja Mask, Glowing Eyes & Katana Sword
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF312E81), Color(0xFF0F172A), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val cy = h / 2f

                    // 1. Ninja Hood / Cowl
                    drawOval(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(cx - 75f, cy - 85f),
                        size = androidx.compose.ui.geometry.Size(150f, 170f)
                    )

                    // 2. Ninja Face Mask (Deep Black cowl cover)
                    drawRect(
                        color = Color(0xFF0F172A),
                        topLeft = Offset(cx - 65f, cy + 5f),
                        size = androidx.compose.ui.geometry.Size(130f, 80f)
                    )

                    // 3. Shinobi Headband (Forehead band)
                    drawRect(
                        color = Color(0xFFDC2626), // Crimson warrior headband
                        topLeft = Offset(cx - 72f, cy - 65f),
                        size = androidx.compose.ui.geometry.Size(144f, 30f)
                    )

                    // Headband Metal Plate
                    drawRoundRect(
                        color = Color(0xFFE2E8F0),
                        topLeft = Offset(cx - 36f, cy - 62f),
                        size = androidx.compose.ui.geometry.Size(72f, 24f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                    )
                    // Metal Plate Symbol (Japanese slash emblem)
                    drawLine(
                        color = Color(0xFF0F172A),
                        start = Offset(cx - 16f, cy - 50f),
                        end = Offset(cx + 16f, cy - 50f),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = Color(0xFF0F172A),
                        start = Offset(cx, cy - 58f),
                        end = Offset(cx, cy - 42f),
                        strokeWidth = 3f
                    )

                    // 4. Fierce Glowing Samurai Eyes
                    // Eye slit background
                    drawRect(
                        color = Color(0xFF020617),
                        topLeft = Offset(cx - 52f, cy - 25f),
                        size = androidx.compose.ui.geometry.Size(104f, 26f)
                    )

                    // Left Eye (Cyan Glow)
                    drawOval(
                        color = Color(0xFF38BDF8),
                        topLeft = Offset(cx - 40f, cy - 18f),
                        size = androidx.compose.ui.geometry.Size(26f, 12f)
                    )
                    drawOval(
                        color = Color.White,
                        topLeft = Offset(cx - 32f, cy - 16f),
                        size = androidx.compose.ui.geometry.Size(10f, 8f)
                    )

                    // Right Eye (Cyan Glow)
                    drawOval(
                        color = Color(0xFF38BDF8),
                        topLeft = Offset(cx + 14f, cy - 18f),
                        size = androidx.compose.ui.geometry.Size(26f, 12f)
                    )
                    drawOval(
                        color = Color.White,
                        topLeft = Offset(cx + 22f, cy - 16f),
                        size = androidx.compose.ui.geometry.Size(10f, 8f)
                    )

                    // 5. Gleaming Katana Sword across body
                    val bladeStart = Offset(cx - 100f, cy + 85f)
                    val bladeEnd = Offset(cx + 95f, cy - 75f)

                    // Katana Shadow / Outline
                    drawLine(
                        color = Color(0xFF020617),
                        start = bladeStart,
                        end = bladeEnd,
                        strokeWidth = 9f,
                        cap = StrokeCap.Round
                    )

                    // Katana Steel Blade
                    drawLine(
                        color = Color(0xFFF1F5F9),
                        start = bladeStart,
                        end = bladeEnd,
                        strokeWidth = 5.5f,
                        cap = StrokeCap.Round
                    )

                    // Katana Sharp Edge Reflection (Bright cyan/white)
                    drawLine(
                        color = Color(0xFF38BDF8),
                        start = bladeStart,
                        end = bladeEnd,
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )

                    // Katana Tsuba (Handguard)
                    val tsubaCenter = Offset(cx - 65f, cy + 55f)
                    drawCircle(
                        color = Color(0xFFF59E0B), // Golden Guard
                        radius = 14f,
                        center = tsubaCenter
                    )
                    drawCircle(
                        color = Color(0xFF78350F),
                        radius = 14f,
                        center = tsubaCenter,
                        style = Stroke(width = 2.5f)
                    )

                    // Katana Hilt (Tsuka wrapped)
                    val hiltStart = Offset(cx - 65f, cy + 55f)
                    val hiltEnd = Offset(cx - 105f, cy + 92f)
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = hiltStart,
                        end = hiltEnd,
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )
                    // Hilt Wrap cords
                    drawLine(
                        color = Color(0xFFDC2626),
                        start = hiltStart,
                        end = hiltEnd,
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )

                    // 6. Dynamic Katana Slash Animation Arc
                    val arcRadius = 110f
                    val currentSlashX = cx - arcRadius + (2f * arcRadius * slashProgress)
                    val currentSlashY = cy - 40f + (80f * (1f - slashProgress))

                    // Slash spark trail
                    drawCircle(
                        color = Color(0xFFFBBF24),
                        radius = 8f,
                        center = Offset(currentSlashX, currentSlashY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(currentSlashX, currentSlashY)
                    )
                    drawLine(
                        color = Color(0xFFFBBF24).copy(alpha = 0.8f * (1f - slashProgress)),
                        start = Offset(cx - 100f, cy + 50f),
                        end = Offset(currentSlashX, currentSlashY),
                        strokeWidth = 3f
                    )
                }
            }

            // Plan Info & PDF Receipt Action Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.75f))
                    .border(1.5.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Subscribed to $planName via $paymentMethod",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    "All lessons, AI chat, challenges & PYQs are now fully accessible!",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(10.dp))

                // Download Invoice PDF Button
                Button(
                    onClick = { onDownloadReceipt() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(50.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Filled.Download,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Download Payment Receipt (PDF)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Bottom Panel / Drawer: "Swipe Up from Down to Up to Enter the Dojo"
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xFF0F172A))
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFE63946), Color(0xFFFFB703), Color(0xFF2563EB))
                    ),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .clickable { onEnterApp() }
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pulsing Upward Chevron
                Box(
                    modifier = Modifier
                        .offset(y = swipeOffset.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Swipe up",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    "▲ SWIPE UP TO ENTER THE DOJO ▲",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    "Swipe from down to up (or tap here) to enter Vastavik",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
