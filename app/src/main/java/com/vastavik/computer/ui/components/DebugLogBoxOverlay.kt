package com.vastavik.computer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.utils.DebugLogBox
import com.vastavik.computer.utils.DebugLogBox.LogEntry
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor

/**
 * Admin-only floating banner box showing Vastavik AI diagnostics.
 *
 * When an AI call fails, presents a high-visibility warning box explaining
 * WHY it is not working (file origin: VastavikAi.kt, model: gemini-3.6-flash,
 * HTTP status, and API error body).
 */
@Composable
fun DebugLogBoxOverlay(modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var tick by remember { mutableStateOf(0) }
    var dismissedError by remember { mutableStateOf(false) }

    // Poll every 500ms to stay in sync with background AI calls
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            tick++
        }
    }

    val entries: List<LogEntry> = remember(tick) { DebugLogBox.getEntries() }
    val lastError: LogEntry? = remember(tick) { DebugLogBox.lastErrorEntry }
    val activeModel: String = remember(tick) { DebugLogBox.activeModel }
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        // High-priority Error Banner Box (shows automatically when an error occurs)
        if (lastError != null && !dismissedError) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                // Brutal shadow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 3.dp, y = 3.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF450A0A))
                        .border(BorderStroke(1.5.dp, Color(0xFFEF4444)), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "ADMIN DIAGNOSTIC: AI CALL FAILED",
                            color = Color(0xFFFCA5A5),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Dismiss Error Banner",
                            tint = Color(0xFFFCA5A5),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { dismissedError = true }
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "File: VastavikAi.kt • Engine: Vastavik AI (${lastError.model})",
                        color = Color(0xFFFCD34D),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Reason: ${lastError.message}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Floating Debug Chip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.BugReport,
                    contentDescription = null,
                    tint = if (lastError != null) Color(0xFFEF4444) else Color(0xFF10B981),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                val last = entries.firstOrNull()
                val label = if (last != null) {
                    val statusMark = when (last.level) {
                        DebugLogBox.Level.ERROR -> "✖"
                        DebugLogBox.Level.WARN -> "⚠"
                        else -> "✓"
                    }
                    "$statusMark [Vastavik AI: $activeModel] ${last.message.take(55)}"
                } else {
                    "✓ [Vastavik AI: $activeModel] Ready • No calls yet"
                }
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Expanded Log Details Viewer
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                    .border(BorderStroke(1.5.dp, bb))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "AI Engine Logs (${entries.size}) — Model: $activeModel",
                        color = Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Clear",
                            color = Color(0xFFFCA5A5),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .clickable {
                                    DebugLogBox.clear()
                                    dismissedError = false
                                }
                                .padding(end = 4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(count = entries.size) { i ->
                        val e = entries[i]
                        val color = when (e.level) {
                            DebugLogBox.Level.ERROR -> Color(0xFFFCA5A5)
                            DebugLogBox.Level.WARN -> Color(0xFFFCD34D)
                            else -> Color(0xFF86EFAC)
                        }
                        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US)
                            .format(java.util.Date(e.timestamp))
                        Text(
                            text = "$time [${e.level}] ${e.tag} (${e.model}): ${e.message}",
                            color = color,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}

