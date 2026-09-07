package com.vastavik.computer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
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
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor
import com.vastavik.computer.utils.ActivityLog
import com.vastavik.computer.utils.AppUpdater
import androidx.compose.ui.platform.LocalContext

/**
 * Neo-brutalist mandatory-update banner.
 *
 * Visually:
 *  - Yellow / red gradient strip with thick black borders.
 *  - Bottom + right shadow strip for the offset "lifted" effect.
 *  - Rounded corners.
 *  - Bold, ALL-CAPS title + a one-line description.
 *  - "Update Now" pill button on the right.
 *
 * Behaviour:
 *  - Shown whenever `AppUpdater.updateState` reports `isUpdateAvailable == true`.
 *  - Clicking anywhere on the banner navigates to the App Update screen.
 *  - Clicking the explicit Update button also records an "update_prompt_clicked"
 *    activity log entry.
 */
@Composable
fun MandatoryUpdateBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val updateInfo by AppUpdater.updateState.collectAsState()
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    val latest = updateInfo?.latestVersion
    val isAvailable = updateInfo?.isUpdateAvailable == true

    if (!isAvailable) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Bottom + right shadow strip (neo-brutalist offset)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bs)
        )
        // Main banner surface
        Surface(
            onClick = {
                ActivityLog.log(context, "update_prompt_clicked", mapOf("latest_version" to (latest ?: "")))
                onClick()
            },
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent,
            border = BorderStroke(2.5.dp, bb),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFEF3C7), Color(0xFFFCA5A5))
                        )
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFDC2626))
                        .border(BorderStroke(2.dp, bb), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "UPDATE REQUIRED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF7F1D1D),
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "A newer version (v${latest ?: "?"}) is available. " +
                            "Please update to keep using the app.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937),
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF1F2937))
                        .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(50))
                        .clickable {
                            ActivityLog.log(context, "update_prompt_button_clicked", mapOf("latest_version" to (latest ?: "")))
                            onClick()
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.SystemUpdate,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "UPDATE",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
            }
        }
    }
}
