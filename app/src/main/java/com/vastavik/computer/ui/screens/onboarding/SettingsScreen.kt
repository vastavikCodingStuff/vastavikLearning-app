package com.vastavik.computer.ui.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val BgWhite = Color(0xFFF8FAFC)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val PrimaryBlue = Color(0xFF2563EB)
private val BorderBlack = Color.Black
private val DividerColor = Color(0xFFE2E8F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigate: (String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var notificationsEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigate("profile") }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                }
                Text(
                    text = "Settings",
                    color = TextDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
            }
        },
        containerColor = BgWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Notifications",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = PrimaryBlue,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BorderBlack)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(2.dp, BorderBlack),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = PrimaryBlue)
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notifications", fontWeight = FontWeight.W500, color = TextDark)
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = PrimaryBlue)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "General",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = PrimaryBlue,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BorderBlack)
                )
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate("notifications") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(2.dp, BorderBlack),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = PrimaryBlue)
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notification History", fontWeight = FontWeight.W500, color = TextDark)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMuted)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BorderBlack)
                )
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate("app_update") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(2.dp, BorderBlack),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.SystemUpdate, contentDescription = null, tint = PrimaryBlue)
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("App Update", fontWeight = FontWeight.W500, color = TextDark)
                            Text("Check for updates", fontSize = 12.sp, color = TextMuted)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMuted)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BorderBlack)
                )
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(2.dp, BorderBlack),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = PrimaryBlue)
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("About", fontWeight = FontWeight.W500, color = TextDark)
                            Text("Version 1.0.0", fontSize = 12.sp, color = TextMuted)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMuted)
                    }
                }
            }
        }
    }
}
