package com.vastavik.computer.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.ui.theme.VastavikColors
import com.vastavik.computer.ui.theme.neoShape
import com.vastavik.computer.ui.theme.neoCircleShape

@Composable
fun WelcomeScreen(onNavigate: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFF6366F1))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Celebration,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Welcome to Vastavik!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Let's set up your profile to personalize your learning experience.",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = { onNavigate("user_setup") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = neoShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { onNavigate("home") }) {
                Text(
                    "Skip for now",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
