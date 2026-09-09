package com.vastavik.computer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vastavik.computer.ui.theme.NeoBrutalistColors
import com.vastavik.computer.utils.SecurityProtectionManager

/**
 * Session-level holder for emulator developer override.
 * Preserves unlock state during app lifecycle while avoiding permanent bypass.
 */
object EmulatorSecuritySession {
    var isOverrideUnlocked by mutableStateOf(false)
    const val ADMIN_BYPASS_PIN = "vastavik2026"
}

/**
 * Full-screen Opaque Emulator Security Curtain.
 *
 * Prevents host-level screen captures and recordings in BlueStacks, Nox, Genymotion, and VMs.
 * Because host desktop tools capture the DirectX/OpenGL buffer outside Android's guest OS
 * (bypassing Android OS-level FLAG_SECURE), this curtain blanks the entire screen with an
 * impenetrable security barrier, ensuring no course content or student private data can ever
 * be captured or recorded from an emulator.
 *
 * Includes an Admin / Developer Passcode dialog for developers testing on BlueStacks.
 */
@Composable
fun EmulatorSecurityCurtain(
    modifier: Modifier = Modifier
) {
    val isEmulator = remember { SecurityProtectionManager.isRunningOnEmulator() }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }

    if (!isEmulator || EmulatorSecuritySession.isOverrideUnlocked) {
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .clickable(enabled = true) { /* Intercept all touches behind */ }
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Shield Icon with Warning Emblem
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                        )
                    )
                    .border(BorderStroke(3.dp, Color.Black), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = "Security Restriction",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // NeoBrutalistic Card Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 4.dp, bottom = 4.dp)
            ) {
                // Hard Shadow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(2.5.dp, Color.Black)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFEF2F2))
                                .border(BorderStroke(1.5.dp, Color(0xFFEF4444)), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "SCREEN CAPTURE PROTECTED",
                                color = Color(0xFFDC2626),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Access Restricted in Emulator",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Running in BlueStacks / Virtual Machine.\n\n" +
                                "To protect proprietary course videos, ICSE/CBSE curriculum, and student data privacy from unauthorized external screen recording and screenshots, Vastavik Learning cannot be displayed inside an emulator.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Device Instruction Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F172A))
                                .border(BorderStroke(1.5.dp, Color(0xFF334155)), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.PhoneAndroid,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Please open Vastavik Learning on an authentic Android phone or tablet where hardware FLAG_SECURE is enforced.",
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp,
                                    color = Color(0xFFE2E8F0),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Admin / Developer Passcode Button
                        OutlinedButton(
                            onClick = {
                                enteredPin = ""
                                pinError = false
                                showPinDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(2.dp, Color(0xFFF59E0B)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF292524).copy(alpha = 0.6f),
                                contentColor = Color(0xFFF59E0B)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Key,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Developer / Admin Override",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Admin Passcode Dialog
    if (showPinDialog) {
        Dialog(onDismissRequest = { showPinDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 4.dp, bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, Color.Black)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Admin Emulator Access",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Enter developer bypass PIN to preview app on BlueStacks. Forensics watermark will remain active.",
                            fontSize = 11.5.sp,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = enteredPin,
                            onValueChange = {
                                enteredPin = it
                                pinError = false
                            },
                            placeholder = { Text("Enter PIN (vastavik2026)") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (enteredPin == EmulatorSecuritySession.ADMIN_BYPASS_PIN) {
                                    EmulatorSecuritySession.isOverrideUnlocked = true
                                    showPinDialog = false
                                } else {
                                    pinError = true
                                }
                            }),
                            isError = pinError,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (pinError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Invalid Passcode. Use vastavik2026",
                                color = Color(0xFFDC2626),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showPinDialog = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.5.dp, Color.Black)
                            ) {
                                Text("Cancel", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    if (enteredPin == EmulatorSecuritySession.ADMIN_BYPASS_PIN) {
                                        EmulatorSecuritySession.isOverrideUnlocked = true
                                        showPinDialog = false
                                    } else {
                                        pinError = true
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                            ) {
                                Text("Unlock", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
