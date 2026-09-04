package com.vastavik.computer.ui.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor
import com.vastavik.computer.utils.AdminSession
import com.vastavik.computer.utils.DebugLogBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(onNavigate: (String) -> Unit, onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    val isEngineLogsEnabled by AdminSession.isEngineLogsEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            Box(modifier = Modifier.fillMaxWidth().padding(end = 4.dp, bottom = 4.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(bs)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(2.dp, bb)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("Admin Controls", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                            Text("Real-time engine controls & diagnostics", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // AI Engine Logs Overlay Control Card
            Box(modifier = Modifier.fillMaxWidth().padding(end = 4.dp, bottom = 4.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(bs)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(2.dp, bb)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isEngineLogsEnabled) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f))
                                        .border(BorderStroke(1.5.dp, if (isEngineLogsEnabled) Color(0xFF10B981) else Color(0xFFEF4444)), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (isEngineLogsEnabled) Icons.Filled.BugReport else Icons.Filled.Close,
                                        contentDescription = null,
                                        tint = if (isEngineLogsEnabled) Color(0xFF059669) else Color(0xFFDC2626),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("AI Engine Logs", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                    Text(
                                        if (isEngineLogsEnabled) "Status: Enabled (Banner Visible)" else "Status: Disabled (Banner Hidden)",
                                        fontSize = 11.sp,
                                        color = if (isEngineLogsEnabled) Color(0xFF059669) else Color(0xFFDC2626),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Switch(
                                checked = isEngineLogsEnabled,
                                onCheckedChange = { isChecked ->
                                    AdminSession.setEngineLogsEnabled(context, isChecked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF2563EB),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.Gray
                                )
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(thickness = 1.dp, color = bb.copy(alpha = 0.2f))
                        Spacer(Modifier.height(10.dp))

                        Text(
                            "Controls the floating bottom banner box showing real-time Vastavik AI engine model status, network latency, and diagnostics. Turn off to hide the overlay completely.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Engine Diagnostics Info Card
            Box(modifier = Modifier.fillMaxWidth().padding(end = 4.dp, bottom = 4.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(bs)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(2.dp, bb)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Active Engine Diagnostics", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Text("• Primary Engine: Mistral Small (Mistral is GOD)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Fallback 1: Google Gemini 3.7 Flash (Demi-god)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Fallback 2: Google Gemini 3.6 Flash (Human AI)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Thinking Mode: Disabled on all models (Zero reasoning token waste)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Last Logged Model: ${DebugLogBox.activeModel}", fontSize = 12.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
