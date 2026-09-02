package com.vastavik.computer.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.ui.theme.BrutalCard

@Composable
fun ProfileScreen(onNavigate: (String) -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Profile Header Brutal Card
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp).padding(end = 5.dp, bottom = 5.dp)) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 5.dp, y = 5.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(2.dp, Color.Black),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFF06B6D4))
                                    )
                                )
                        ) {
                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .offset(x = 200.dp, y = (-20).dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .offset(x = (-30).dp, y = 80.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                )
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    // Avatar with brutal border
                                    Box(
                                        modifier = Modifier
                                            .size(84.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color.White)
                                            .border(BorderStroke(2.dp, Color.Black), RoundedCornerShape(20.dp))
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color(0xFFF1F5F9)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Filled.Person,
                                                contentDescription = null,
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Student",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "student@example.com",
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color.White)
                                                .border(BorderStroke(1.5.dp, Color.Black), RoundedCornerShape(16.dp))
                                                .padding(12.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0xFFFFF7ED))
                                                        .border(BorderStroke(1.dp, Color.Black.copy(alpha = 0.08f)), RoundedCornerShape(10.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("🔥", fontSize = 18.sp)
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text("7 days", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground, lineHeight = 18.sp)
                                                    Text("Day streak", fontSize = 11.sp, color = Color(0xFF64748B), lineHeight = 14.sp)
                                                }
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color.White)
                                                .border(BorderStroke(1.5.dp, Color.Black), RoundedCornerShape(16.dp))
                                                .padding(12.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0xFFEEF2FF))
                                                        .border(BorderStroke(1.dp, Color.Black.copy(alpha = 0.08f)), RoundedCornerShape(10.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("📚", fontSize = 18.sp)
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text("24", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground, lineHeight = 18.sp)
                                                    Text("Lessons done", fontSize = 11.sp, color = Color(0xFF64748B), lineHeight = 14.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(18.dp)) }

            // Premium card brutal
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp).padding(end = 5.dp, bottom = 5.dp)) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 5.dp, y = 5.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate("payment") },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(2.dp, Color.Black),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(Color(0xFFFBBF24), Color(0xFFF97316))
                                            )
                                        )
                                        .border(BorderStroke(1.5.dp, Color.Black), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("★", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Upgrade to Premium",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        "Unlock all lessons + AI chat",
                                        color = Color.White.copy(alpha = 0.65f),
                                        fontSize = 12.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(BorderStroke(1.5.dp, Color.Black), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("→", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(18.dp)) }

            // Menu items brutal
            item {
                BrutalCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = Color.White
                ) {
                    Column {
                        val menuItems = listOf(
                            Quadruple("Edit Profile", "Manage info & avatar", Icons.Filled.Edit, "edit_profile"),
                            Quadruple("Select Course", "Choose your path", Icons.Filled.MenuBook, "course"),
                            Quadruple("Online Class", "Join live session", Icons.Filled.VideoCall, "meeting_lobby/default_live_class"),
                            Quadruple("Code Editor", "Practice live", Icons.Filled.Code, "code_editor"),
                            Quadruple("OCR Exercise", "Scan & solve", Icons.Filled.DocumentScanner, "ocr_exercise"),
                            Quadruple("My Notes", "Your saved notes", Icons.Filled.Note, "my_notes"),
                            Quadruple("Notifications", "Alerts & updates", Icons.Filled.Notifications, "notifications"),
                            Quadruple("App Update", "Version & changelog", Icons.Filled.SystemUpdate, "app_update"),
                            Quadruple("Payment History", "Invoices & plans", Icons.Filled.Receipt, "payment_history"),
                            Quadruple("Settings", "Theme & prefs", Icons.Filled.Settings, "settings")
                        )

                        menuItems.forEachIndexed { index, (title, desc, icon, route) ->
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigate(route) }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFF8FAFC))
                                            .border(BorderStroke(1.5.dp, Color.Black), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            icon,
                                            contentDescription = null,
                                            tint = Color(0xFF0F172A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            desc,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Icon(
                                        Icons.Filled.ChevronRight,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (index < menuItems.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = Color(0xFFE2E8F0)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color(0xFFE2E8F0)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigate("login") }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFEF2F2))
                                    .border(BorderStroke(1.5.dp, Color.Black), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Logout,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                "Log Out",
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFFEF4444)
                            )
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
