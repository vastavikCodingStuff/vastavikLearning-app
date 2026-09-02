package com.vastavik.computer.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.vastavik.computer.utils.DeviceSecurityChecker
import com.vastavik.computer.utils.SecurityIssue
import kotlinx.coroutines.delay

@Composable
fun SecurityCheckScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    var issues by remember { mutableStateOf<List<SecurityIssue>>(emptyList()) }
    var isChecking by remember { mutableStateOf(true) }
    var hasChecked by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        delay(800)
        issues = DeviceSecurityChecker.checkAll(context)
        isChecking = false
        hasChecked = true
    }

    LaunchedEffect(issues, hasChecked) {
        if (hasChecked && !isChecking && issues.isEmpty()) {
            delay(1500)
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) onNavigate("home") else onNavigate("login")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = "Security Check",
                    modifier = Modifier.size(44.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Device Security Check",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "We need to verify your device is secure before you can use VastavikCode.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isChecking) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Scanning device...",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            } else if (issues.isEmpty() && hasChecked) {
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn() + fadeIn()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "All Clear",
                                modifier = Modifier.size(48.dp),
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "All Clear!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your device is secure. Redirecting...",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            } else if (issues.isNotEmpty()) {
                Text(
                    text = "${issues.size} issue${if (issues.size > 1) "s" else ""} found",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFBBF24),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF78350F).copy(alpha = 0.6f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                issues.forEach { issue ->
                    SecurityIssueCard(issue = issue)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        isChecking = true
                        issues = emptyList()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Re-check Device",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Fix all issues above, then tap Re-check to continue.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SecurityIssueCard(issue: SecurityIssue) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF7F1D1D).copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = issue.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = issue.description,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "Toggle details",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.2f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "How to fix:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFBBF24)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = issue.stepsToFix,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
