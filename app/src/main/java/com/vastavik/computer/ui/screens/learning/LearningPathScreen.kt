package com.vastavik.computer.ui.screens.learning

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningPathScreen(onNavigate: (String) -> Unit) {
    val courses = listOf("Java", "Python", "C++", "Web Dev")
    var selectedCourse by remember { mutableStateOf("Java") }
    var showPartSheet by remember { mutableStateOf(false) }
    var selectedPart by remember { mutableStateOf("") }

    val nodes = listOf(
        "Introduction", "Variables", "Control Flow",
        "Functions", "OOP Basics", "Collections",
        "File I/O", "Project", "Final Project"
    )
    val offsets = listOf(0f, 0.4f, 0.8f, 0.4f, 0f, -0.4f, -0.8f, -0.4f, 0f)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Top bar: "Learn Path" + profile
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Learn Path",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(BorderStroke(1.5.dp, Color.Black), CircleShape)
                            .clickable { onNavigate("profile") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Course selector chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    courses.forEach { course ->
                        val isSelected = selectedCourse == course
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                .border(BorderStroke(2.dp, Color.Black), RoundedCornerShape(50.dp))
                                .clickable { selectedCourse = course }
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = course,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            // Unit Header brutal card
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(2.dp, Color.Black),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("UNIT 1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Java Fundamentals", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Master the basics of Java programming", color = Color(0xFF64748B), fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(MaterialTheme.colorScheme.outline)
                                    .border(BorderStroke(1.dp, Color.Black.copy(alpha = 0.12f)), RoundedCornerShape(50.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.35f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("35% completed", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Zigzag path
            itemsIndexed(nodes) { index, node ->
                val isDone = index < 3
                val isCurrent = index == 3
                val xOffset = offsets[index % offsets.size]

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(92.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (index < nodes.lastIndex) {
                            val nextXOffset = offsets[(index + 1) % offsets.size]
                            val pathColor = if (isDone || isCurrent) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val startX = size.width / 2 + (xOffset * 80.dp.toPx())
                                val endX = size.width / 2 + (nextXOffset * 80.dp.toPx())
                                drawLine(
                                    color = pathColor,
                                    start = Offset(startX, size.height * 0.75f),
                                    end = Offset(endX, size.height * 0.25f + size.height / 2),
                                    strokeWidth = 6.dp.toPx()
                                )
                            }
                        }

                        Box(
                            modifier = Modifier.offset(x = (xOffset * 80).dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .offset(x = 4.dp, y = 4.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black)
                            )
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isDone -> MaterialTheme.colorScheme.primary
                                            isCurrent -> Color.White
                                            else -> MaterialTheme.colorScheme.outline
                                        }
                                    )
                                    .border(BorderStroke(2.5.dp, Color.Black), CircleShape)
                                    .clickable {
                                        if (index != nodes.lastIndex) {
                                            selectedPart = node
                                            showPartSheet = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (index == nodes.lastIndex) {
                                    Icon(
                                        Icons.Filled.EmojiEvents,
                                        contentDescription = "Trophy",
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(30.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = "Star",
                                        tint = when {
                                            isDone -> Color(0xFFFBBF24)
                                            isCurrent -> Color(0xFFF59E0B)
                                            else -> Color(0xFF94A3B8)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .offset(x = (-4).dp, y = (-4).dp)
                                        .clip(CircleShape)
                                        .border(BorderStroke(3.dp, MaterialTheme.colorScheme.primary), CircleShape)
                                )
                            }
                        }
                    }
                    Text(
                        text = node,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }

    if (showPartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPartSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = selectedPart,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))

                val subparts = listOf("Video Lesson", "Practice Quiz", "Coding Exercise", "Notes")
                subparts.forEach { subpart ->
                    Box(modifier = Modifier.padding(vertical = 6.dp).padding(end = 4.dp, bottom = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 4.dp, y = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.Black)
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showPartSheet = false
                                    onNavigate("video_lesson/1/1/1/1")
                                },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(2.dp, Color.Black),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    when (subpart) {
                                        "Video Lesson" -> Icons.Filled.PlayCircle
                                        "Practice Quiz" -> Icons.Filled.Quiz
                                        "Coding Exercise" -> Icons.Filled.Code
                                        else -> Icons.Filled.Note
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(subpart, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
