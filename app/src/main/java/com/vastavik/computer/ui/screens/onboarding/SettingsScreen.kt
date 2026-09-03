package com.vastavik.computer.ui.screens.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vastavik.computer.ui.theme.BrutalCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    val isDarkMode by viewModel.isDarkMode.collectAsState(initial = false)

    val bg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val accent = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onBack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = textPrimary)
                }
                Text(
                    text = "Settings",
                    color = textPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
            }
        },
        containerColor = bg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Appearance — dark mode lives here so every page follows it.
            SectionHeader("Appearance", accent)
            BrutalCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isDarkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                            contentDescription = null,
                            tint = accent
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Dark Mode",
                                fontWeight = FontWeight.W500,
                                color = textPrimary
                            )
                            Text(
                                if (isDarkMode) "On — white text on dark backgrounds"
                                else "Off — dark text on light backgrounds",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { viewModel.setDarkMode(it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = accent)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("Notifications", accent)
            BrutalCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = accent)
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notifications", fontWeight = FontWeight.W500, color = textPrimary)
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = accent)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("General", accent)

            SettingsRow(
                icon = Icons.Filled.NotificationsActive,
                title = "Notification History",
                subtitle = null,
                accent = accent,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                onClick = { onNavigate("notifications") }
            )
            Spacer(modifier = Modifier.height(12.dp))

            SettingsRow(
                icon = Icons.Filled.SystemUpdate,
                title = "App Update",
                subtitle = "Check for updates",
                accent = accent,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                onClick = { onNavigate("app_update") }
            )
            Spacer(modifier = Modifier.height(12.dp))

            SettingsRow(
                icon = Icons.Filled.Info,
                title = "About",
                subtitle = "Version 1.0.0",
                accent = accent,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                onClick = { }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, accent: androidx.compose.ui.graphics.Color) {
    Text(
        title,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = accent,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    accent: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textSecondary: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    BrutalCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = accent)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.W500, color = textPrimary)
                if (subtitle != null) {
                    Text(subtitle, fontSize = 12.sp, color = textSecondary)
                }
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = textSecondary)
        }
    }
}
