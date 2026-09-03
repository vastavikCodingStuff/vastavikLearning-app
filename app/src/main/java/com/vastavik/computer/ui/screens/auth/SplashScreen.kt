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
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

private val PrimaryIndigo = Color(0xFF2563EB)

@Composable
fun SplashScreen(onNavigate: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        com.vastavik.computer.utils.AdminSession.init(context)
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )
        delay(2000)
        // Auto-check for a new APK on the server route (self-hosted updates, no Play Store)
        if (hasUpdateOnRoute()) {
            onNavigate("app_update")
            return@LaunchedEffect
        }
        if (BuildConfig.SECURITY_CHECK_ENABLED) {
            onNavigate("security_check")
        } else {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null || com.vastavik.computer.utils.AdminSession.isAdmin.value) {
                onNavigate("home")
            } else {
                onNavigate("login")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
                        .background(bs)
                )
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PrimaryIndigo)
                        .border(BorderStroke(2.dp, bb), RoundedCornerShape(20.dp)),
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
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.alpha(alpha.value)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Learn. Code. Succeed.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(alpha.value)
            )
        }
    }
}

private suspend fun hasUpdateOnRoute(): Boolean = try {
    val doc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        .collection(com.vastavik.computer.utils.Constants.COLLECTION_ADMIN_SETTINGS)
        .document(com.vastavik.computer.utils.Constants.ADMIN_SETTINGS_UPDATE_DOC)
        .get()
        .await()
    val latest = doc.getString("latestVersion") ?: ""
    latest.isNotEmpty() && latest != BuildConfig.VERSION_NAME
} catch (_: Exception) {
    false
}
