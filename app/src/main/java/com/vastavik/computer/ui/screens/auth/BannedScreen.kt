package com.vastavik.computer.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.ui.theme.RobotoSlabFontFamily
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor
import com.vastavik.computer.ui.theme.neoShape
import com.vastavik.computer.utils.BanManager

@Composable
fun BannedScreen(onNavigate: (String) -> Unit) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Tactile hazard badge
            Box(modifier = Modifier.padding(bottom = 24.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(bs)
                )
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                        .border(BorderStroke(2.dp, Color(0xFFEF4444)), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Block,
                        contentDescription = "Banned",
                        modifier = Modifier.size(54.dp),
                        tint = Color(0xFFEF4444)
                    )
                }
            }

            Text(
                text = "Account Banned",
                fontSize = 28.sp,
                fontFamily = RobotoSlabFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your account has been banned and deleted by the administrator.",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFEF4444),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "All your previous records, enrollments, notes, and progress have been permanently wiped from the active system.\n\nTo continue using Vastavik Learning, you must join again with a new account.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Primary: Register fresh account
            Button(
                onClick = {
                    BanManager.clearBanState()
                    onNavigate("signup")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = neoShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Create New Account",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Secondary: Back to Login
            OutlinedButton(
                onClick = {
                    BanManager.clearBanState()
                    onNavigate("login")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = neoShape(12.dp),
                border = BorderStroke(2.dp, bb)
            ) {
                Text(
                    "Back to Login",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
