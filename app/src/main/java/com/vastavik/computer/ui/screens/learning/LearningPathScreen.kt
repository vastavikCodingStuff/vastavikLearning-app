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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningPathScreen(onNavigate: (String) -> Unit) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    val courses = listOf("Java", "Python", "C++", "Web Dev")
    var selectedCourse by remember { mutableStateOf("Java") }
    var showPartSheet by remember { mutableStateOf(false) }
    var selectedPart by remember { mutableStateOf("") }

    val nodes = listOf(
        "Array", "String", "Functions", "Constructor", "Wrapper Functions"
    )
    val offsets = listOf(0f, 0.4f, 0f, -0.4f, 0f)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
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
                    val updateInfo by com.vastavik.computer.utils.AppUpdater.updateState.collectAsState()
                    val hasUpdate = updateInfo?.isUpdateAvailable == true
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB))
                            .border(BorderStroke(1.5.dp, bb), CircleShape)
                            .clickable { onNavigate("notifications") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        if (hasUpdate) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFD600))
                                    .border(BorderStroke(1.dp, Color.Black), CircleShape)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(BorderStroke(1.5.dp, bb), CircleShape)
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
                                .border(BorderStroke(2.dp, bb), RoundedCornerShape(50.dp))
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
                            .background(bs)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(2.dp, bb),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .border(BorderStroke(1.dp, bb), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("UNIT 1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Java Fundamentals", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Array • String • Functions • Constructor • Wrapper Functions", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(MaterialTheme.colorScheme.outline)
                                    .border(BorderStroke(1.dp, bb.copy(alpha = 0.12f)), RoundedCornerShape(50.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.0f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("0% completed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Continuous winding path with oval curved connecting lines
            item {
                Spacer(modifier = Modifier.height(12.dp))
                LearningPathWindingView(
                    nodes = nodes,
                    offsets = offsets,
                    onNodeClick = { node, index ->
                        if (index != nodes.lastIndex) {
                            selectedPart = node
                            showPartSheet = true
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
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
                                .background(bs)
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showPartSheet = false
                                    onNavigate("video_lesson/1/1/1/1")
                                },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(2.dp, bb),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
private fun LearningPathWindingView(
    nodes: List<String>,
    offsets: List<Float>,
    onNodeClick: (String, Int) -> Unit
) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    val density = LocalDensity.current

    val nodeCount = nodes.size
    val stepHeightDp = 126.dp
    val circleSizeDp = 68.dp
    val circleRadiusDp = circleSizeDp / 2
    val topPaddingDp = 12.dp
    val totalHeightDp = topPaddingDp + (stepHeightDp * (nodeCount - 1)) + circleSizeDp + 40.dp

    val stepHeightPx = with(density) { stepHeightDp.toPx() }
    val circleRadiusPx = with(density) { circleRadiusDp.toPx() }
    val topPaddingPx = with(density) { topPaddingDp.toPx() }
    val xSpreadPx = with(density) { 80.dp.toPx() }
    val bowWidthPx = with(density) { 46.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeightDp)
    ) {
        // 1. Curved Connecting Lines Canvas behind the nodes
        Canvas(modifier = Modifier.fillMaxSize()) {
            val midX = size.width / 2

            // Center coordinates for each circle
            val centers = (0 until nodeCount).map { i ->
                val xOff = offsets[i % offsets.size]
                val cx = midX + (xOff * xSpreadPx)
                val cy = topPaddingPx + (i * stepHeightPx) + circleRadiusPx
                Offset(cx, cy)
            }

            // Draw connecting curved segments between consecutive nodes
            for (i in 0 until nodeCount - 1) {
                val p1 = centers[i]
                val p2 = centers[i + 1]
                val dy = p2.y - p1.y

                // Alternating oval-like bends: right on even index, left on odd index
                val isRightBend = (i % 2 == 0)

                val cp1: Offset
                val cp2: Offset
                if (isRightBend) {
                    val maxX = maxOf(p1.x, p2.x)
                    cp1 = Offset(maxX + bowWidthPx, p1.y + dy * 0.18f)
                    cp2 = Offset(maxX + bowWidthPx, p2.y - dy * 0.18f)
                } else {
                    val minX = minOf(p1.x, p2.x)
                    cp1 = Offset(minX - bowWidthPx, p1.y + dy * 0.18f)
                    cp2 = Offset(minX - bowWidthPx, p2.y - dy * 0.18f)
                }

                val path = Path().apply {
                    moveTo(p1.x, p1.y)
                    cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, p2.x, p2.y)
                }

                // Neo-brutalist border / outline under the curve
                drawPath(
                    path = path,
                    color = bb,
                    style = Stroke(
                        width = with(density) { 9.dp.toPx() },
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Main vibrant blue connecting curve
                drawPath(
                    path = path,
                    color = Color(0xFF2563EB),
                    style = Stroke(
                        width = with(density) { 5.dp.toPx() },
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        // 2. Nodes placed on top of the canvas
        nodes.forEachIndexed { index, node ->
            val isDone = false
            val isCurrent = index == 0
            val xOff = offsets[index % offsets.size]
            val nodeTopDp = topPaddingDp + (stepHeightDp * index)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = nodeTopDp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.offset(x = (xOff * 80).dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Neo-brutalist drop shadow
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .offset(x = 4.dp, y = 4.dp)
                            .clip(CircleShape)
                            .background(bs)
                    )
                    // Circle node
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isDone -> MaterialTheme.colorScheme.primary
                                    isCurrent -> MaterialTheme.colorScheme.surface
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            )
                            .border(BorderStroke(2.5.dp, bb), CircleShape)
                            .clickable { onNodeClick(node, index) },
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
                                .clip(CircleShape)
                                .border(BorderStroke(3.dp, MaterialTheme.colorScheme.primary), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = node,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

