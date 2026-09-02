package com.vastavik.computer.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LaptopChromebook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.vastavik.computer.BuildConfig
import kotlinx.coroutines.delay

private val BgWhite = Color(0xFFF8FAFC)
private val PrimaryIndigo = Color(0xFF2563EB)
private val TextDark = Color(0xFF0F172A)
private val BorderBlack = Color.Black

@Composable
fun SplashScreen(onNavigate: (String) -> Unit) {
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )
        delay(2000)
        if (BuildConfig.SECURITY_CHECK_ENABLED) {
            onNavigate("security_check")
        } else {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                onNavigate("home")
            } else {
                onNavigate("login")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWhite),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale.value)
        ) {
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
                        .size(96.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PrimaryIndigo)
                        .border(BorderStroke(2.dp, BorderBlack), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.LaptopChromebook,
                        contentDescription = "Vastavik Logo",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
            }
            Row {
                Text(
                    text = "Vastavik",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryIndigo,
                    modifier = Modifier.alpha(alpha.value)
                )
                Text(
                    text = "Computer",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark,
                    modifier = Modifier.alpha(alpha.value)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Learn. Code. Succeed.",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.alpha(alpha.value)
            )
        }
    }
}
