package com.vastavik.computer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor
import com.vastavik.computer.utils.TelegramBannerData
import com.vastavik.computer.utils.TelegramBannerType
import com.vastavik.computer.utils.TelegramNotificationManager
import kotlinx.coroutines.delay

private val TelegramBlue = Color(0xFF2AABEE)

@Composable
fun TelegramNotificationHost(
    modifier: Modifier = Modifier,
    onNavigate: ((String) -> Unit)? = null
) {
    val banner by TelegramNotificationManager.currentBanner.collectAsState()

    LaunchedEffect(banner?.id) {
        if (banner != null) {
            delay(4500)
            TelegramNotificationManager.dismiss()
        }
    }

    AnimatedVisibility(
        visible = banner != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f)
        ) + fadeOut(),
        modifier = modifier
    ) {
        banner?.let { b ->
            TelegramNotificationCard(
                banner = b,
                onDismiss = { TelegramNotificationManager.dismiss() },
                onNavigate = onNavigate
            )
        }
    }
}

@Composable
fun TelegramNotificationCard(
    banner: TelegramBannerData,
    onDismiss: () -> Unit,
    onNavigate: ((String) -> Unit)? = null
) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .padding(end = 4.dp, bottom = 4.dp)
    ) {
        // Neo-brutalist shadow offset
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bs)
        )

        // Main card body
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    banner.onAction?.invoke()
                    onDismiss()
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), // Telegram Dark/Slate
            border = BorderStroke(2.dp, bb),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Telegram-style Avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            when (banner.type) {
                                TelegramBannerType.APP_UPDATE -> Color(0xFF10B981)
                                TelegramBannerType.LIVE_CLASS -> Color(0xFFEF4444)
                                else -> TelegramBlue
                            }
                        )
                        .border(BorderStroke(1.5.dp, Color.White), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (banner.type == TelegramBannerType.APP_UPDATE) {
                        Icon(
                            Icons.Filled.SystemUpdate,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = banner.avatarLetter,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                // Sender and Message text
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = banner.senderName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = TelegramBlue,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = banner.time,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = banner.message,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Action Pill Button (e.g. Reply / View / Install)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TelegramBlue)
                        .clickable {
                            banner.onAction?.invoke()
                            onDismiss()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = banner.actionLabel,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(Modifier.width(4.dp))

                // Dismiss X button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
