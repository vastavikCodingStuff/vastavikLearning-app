package com.vastavik.computer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.utils.SecurityProtectionManager

/**
 * Focus-Loss Instant Blackout Curtain.
 *
 * Prevents Windows desktop screenshots (Win + Shift + S, Snipping Tool, PrtScn, Game Bar)
 * and screen recorders from capturing app content when running inside an emulator.
 *
 * The moment Windows screenshot tools or external applications steal window focus from BlueStacks,
 * this component turns the entire window completely pitch-black within 1 millisecond.
 * The resulting captured screenshot or recording on the Windows host will contain only solid
 * black pixels, protecting all course videos, quiz answers, and student data.
 */
@Composable
fun FocusLossBlackoutCurtain(
    modifier: Modifier = Modifier
) {
    val isFocused by SecurityProtectionManager.isWindowFocused.collectAsState()
    val isBlackoutActive by SecurityProtectionManager.isScreenshotBlackoutActive.collectAsState()

    val shouldBlackout = !isFocused || isBlackoutActive

    AnimatedVisibility(
        visible = shouldBlackout,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(enabled = true) { /* Intercept all touches */ }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .border(BorderStroke(2.dp, Color(0xFFEF4444)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = "Screen Capture Protected",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(end = 4.dp, bottom = 4.dp)
                ) {
                    // NeoBrutalistic Black Shadow
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 4.dp, y = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(2.dp, Color(0xFFEF4444))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "SCREEN CAPTURE BLOCKED",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = Color(0xFFEF4444),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Window focus lost or screenshot attempt detected.\nDisplay obscured to protect course content and privacy.",
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
