package com.vastavik.computer.ui.screens.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor
import com.vastavik.computer.utils.AppUpdater

data class AppNotification(
    val id: String,
    val title: String,
    val body: String,
    val time: String,
    val unread: Boolean = true,
    val type: String = "general"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onNavigate: (String) -> Unit, onBack: () -> Unit = {}) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    val updateInfo by AppUpdater.updateState.collectAsState()

    LaunchedEffect(Unit) {
        if (updateInfo == null) {
            AppUpdater.checkGitHubRelease()
        }
    }

    var items by remember {
        mutableStateOf(
            listOf(
                AppNotification("1", "New Lesson: OOP in Java", "VS Code + Whiteboard videos are live", "2h ago", true, "new_lesson"),
                AppNotification("2", "50% OFF ends soon", "Premium at Rs 149/mo. Tap to grab.", "5h ago", true, "promo"),
                AppNotification("3", "Practice Reminder", "You haven't practiced today. 3 MCQs waiting.", "1d ago", false, "reminder"),
                AppNotification("5", "Payment due in 3 days", "Renew to keep Pro access.", "3d ago", true, "expiry")
            )
        )
    }

    val displayItems = remember(items, updateInfo) {
        if (updateInfo?.isUpdateAvailable == true) {
            val ghUpdate = AppNotification(
                id = "gh_update_${updateInfo?.latestVersion}",
                title = "New Update Available: v${updateInfo?.latestVersion}",
                body = "${updateInfo?.releaseTitle ?: "Update"}. Tap to download and install.",
                time = "Just now",
                unread = true,
                type = "update"
            )
            listOf(ghUpdate) + items.filter { it.type != "update" }
        } else {
            items
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { items = items.map { it.copy(unread = false) } }) {
                        Text("Mark all read", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (displayItems.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No notifications", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayItems, key = { it.id }) { n ->
                    val isUpdate = n.type == "update"
                    val isPaymentDue = n.type == "expiry" || n.title.contains("Payment due", ignoreCase = true)

                    val cardColor = when {
                        isUpdate -> Color(0xFF2563EB) // Blue for update
                        isPaymentDue -> Color(0xFFDC2626) // Red for payment due
                        else -> MaterialTheme.colorScheme.surface
                    }

                    val titleColor = when {
                        isUpdate || isPaymentDue -> Color.White
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    val bodyColor = when {
                        isUpdate || isPaymentDue -> Color.White.copy(alpha = 0.92f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    val timeColor = when {
                        isUpdate || isPaymentDue -> Color.White.copy(alpha = 0.75f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }

                    val iconBg = when {
                        isUpdate || isPaymentDue -> Color.White
                        n.unread -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    val iconTint = when {
                        isUpdate -> Color(0xFF2563EB)
                        isPaymentDue -> Color(0xFFDC2626)
                        else -> Color.White
                    }

                    val cardBorder = when {
                        isUpdate || isPaymentDue -> BorderStroke(2.dp, bb)
                        n.unread -> BorderStroke(1.5.dp, bb)
                        else -> BorderStroke(1.5.dp, bb.copy(alpha = 0.3f))
                    }

                    Box(modifier = Modifier.fillMaxWidth().padding(end = 4.dp, bottom = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 3.dp, y = 3.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(bs)
                        )
                        Surface(
                            onClick = {
                                items = items.map { if (it.id == n.id) it.copy(unread = false) else it }
                                when {
                                    isUpdate -> onNavigate("app_update")
                                    isPaymentDue || n.type == "promo" -> onNavigate("payment")
                                    n.type == "new_lesson" -> onNavigate("video_lesson/1/1/1/1")
                                    else -> {}
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = cardColor,
                            border = cardBorder
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(iconBg)
                                        .border(BorderStroke(1.5.dp, bb), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        when {
                                            isUpdate -> Icons.Filled.SystemUpdate
                                            isPaymentDue -> Icons.Filled.Payment
                                            else -> Icons.Filled.Notifications
                                        },
                                        contentDescription = null,
                                        tint = iconTint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            n.title,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            modifier = Modifier.weight(1f),
                                            color = titleColor
                                        )
                                        if (isUpdate) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color.White)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("UPDATE", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
                                            }
                                        } else if (isPaymentDue) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color.White)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("DUE SOON", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2626))
                                            }
                                        } else if (n.unread) {
                                            Box(
                                                Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        n.body,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = bodyColor
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        n.time,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = timeColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
