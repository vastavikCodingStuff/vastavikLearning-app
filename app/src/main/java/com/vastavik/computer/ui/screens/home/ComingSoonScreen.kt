package com.vastavik.computer.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas

private val BgWhite = Color(0xFFF8FAFC)
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val PrimaryBlue = Color(0xFF2563EB)
private val BorderBlack = Color.Black

@Composable
fun ComingSoonScreen(
    courseTitle: String,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigate("home") }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                }
                Text(
                    text = "Vastavik",
                    color = PrimaryBlue,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
                Text(
                    text = "Computer",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.padding(bottom = 24.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(BorderBlack)
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PrimaryBlue)
                        .border(BorderStroke(2.dp, BorderBlack), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚧", fontSize = 40.sp)
                }
            }

            Text(
                text = "Still Rolling In",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We are working on this thing right now",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BorderBlack)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEEF2FF))
                        .border(BorderStroke(2.dp, BorderBlack), RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = courseTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            WaterfallAnimation()

            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BorderBlack)
                )
                Button(
                    onClick = { onNavigate("home") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(BorderStroke(2.dp, BorderBlack), RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        "Back to Home",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun WaterfallAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "waterfall")
    val flowProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flow"
    )
    val splashProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "splash"
    )

    val waterColor = PrimaryBlue
    val waterLight = Color(0xFF93C5FD)
    val foamColor = Color(0xFFDBEAFE)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEFF6FF))
            .border(BorderStroke(2.dp, BorderBlack), RoundedCornerShape(16.dp))
    ) {
        val w = size.width
        val h = size.height
        val riverTop = h * 0.15f
        val riverBottom = h * 0.75f
        val fallHeight = riverBottom - riverTop

        // Waterfall stream - vertical lines with wave
        for (i in 0..12) {
            val xBase = w * 0.3f + (w * 0.4f) * (i / 12f)
            val waveOffset = (flowProgress * 2f * Math.PI.toFloat()) + (i * 0.5f)

            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(xBase + kotlin.math.sin(waveOffset) * 6.dp.toPx(), riverTop)
                quadraticBezierTo(
                    xBase + kotlin.math.sin(waveOffset + 1f) * 10.dp.toPx(),
                    riverTop + fallHeight * 0.3f,
                    xBase + kotlin.math.sin(waveOffset + 2f) * 4.dp.toPx(),
                    riverTop + fallHeight * 0.6f
                )
                quadraticBezierTo(
                    xBase + kotlin.math.sin(waveOffset + 3f) * 8.dp.toPx(),
                    riverTop + fallHeight * 0.8f,
                    xBase + kotlin.math.sin(waveOffset + 4f) * 3.dp.toPx(),
                    riverBottom
                )
            }
            drawPath(
                path = path,
                color = if (i % 3 == 0) waterLight else waterColor,
                alpha = 0.5f + (i % 3) * 0.15f,
                style = Stroke(
                    width = (3 + (i % 3) * 2).dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        // Splash at the bottom
        val splashRadius = 8.dp.toPx() * (1f - splashProgress)
        val splashAlpha = 0.6f * (1f - splashProgress)
        for (j in 0..5) {
            val angle = (j * 60f + splashProgress * 360f) * (Math.PI / 180f).toFloat()
            val cx = w * 0.5f + kotlin.math.cos(angle) * (20.dp.toPx() * splashProgress)
            val cy = riverBottom - 5.dp.toPx() + kotlin.math.sin(angle) * (10.dp.toPx() * splashProgress)
            drawCircle(
                color = foamColor,
                radius = splashRadius,
                center = Offset(cx, cy),
                alpha = splashAlpha
            )
        }

        // Pool at bottom
        drawOval(
            brush = Brush.verticalGradient(
                colors = listOf(waterColor.copy(alpha = 0.7f), waterLight.copy(alpha = 0.4f), foamColor.copy(alpha = 0.3f)),
                startY = riverBottom - 5.dp.toPx(),
                endY = h
            ),
            topLeft = Offset(w * 0.15f, riverBottom - 5.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(w * 0.7f, h - riverBottom + 15.dp.toPx())
        )

        // Drip dots
        for (d in 0..4) {
            val dripY = riverBottom - 15.dp.toPx() + ((splashProgress + d * 0.2f) % 1f) * 20.dp.toPx()
            val dripAlpha = 0.4f * (1f - ((splashProgress + d * 0.2f) % 1f))
            drawCircle(
                color = waterColor,
                radius = 2.dp.toPx(),
                center = Offset(w * 0.5f + (d - 2) * 15.dp.toPx(), dripY),
                alpha = dripAlpha
            )
        }
    }
}
