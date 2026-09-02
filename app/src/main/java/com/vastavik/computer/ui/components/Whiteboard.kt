package com.vastavik.computer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.vastavik.computer.data.model.*

enum class WhiteboardTool { PEN, ERASER, RECTANGLE, ELLIPSE, LINE, ARROW, TEXT, HAND, SELECT }

@Composable
fun NeoBrutalistWhiteboard(
    modifier: Modifier = Modifier,
    elements: List<WhiteboardElement>,
    onElementsChange: (List<WhiteboardElement>) -> Unit,
    viewport: Viewport,
    onViewportChange: (Viewport) -> Unit,
    currentTool: WhiteboardTool = WhiteboardTool.PEN,
    onToolChange: (WhiteboardTool) -> Unit = {},
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 4f,
    readOnly: Boolean = false
) {
    var currentPath by remember { mutableStateOf(listOf<Point>()) }
    var isDrawing by remember { mutableStateOf(false) }
    val latestElements by rememberUpdatedState(elements)
    // Two-finger pinch zoom + plus/minus
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(end = 5.dp, bottom = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(start = 5.dp, top = 5.dp)
                .background(Color.Black, RoundedCornerShape(16.dp))
        )
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            if (currentTool == WhiteboardTool.HAND || zoom != 1f) {
                                scale = (scale * zoom).coerceIn(0.6f, 3.5f)
                                offset += pan
                            }
                        }
                    }
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y)
                        .pointerInput(currentTool) {
                            if (readOnly) return@pointerInput
                            detectDragGestures(
                                onDragStart = {
                                    // Only draw when not in hand/zoom mode with two fingers — hand uses transform above
                                    if (currentTool == WhiteboardTool.HAND) return@detectDragGestures
                                    isDrawing = true
                                    // Account for scale/offset for drawing coords? Keep simple: raw, graphicsLayer handles visual scaling
                                    currentPath = listOf(Point(it.x, it.y))
                                },
                                onDragEnd = {
                                    if (currentPath.size > 1) {
                                        val newElement = WhiteboardElement(
                                            id = "el_${System.currentTimeMillis()}",
                                            type = if (currentTool == WhiteboardTool.ERASER) ElementType.ERASER else ElementType.PEN,
                                            points = currentPath,
                                            color = if (currentTool == WhiteboardTool.ERASER) "#FFFFFF" else "#000000",
                                            strokeWidth = strokeWidth
                                        )
                                        onElementsChange(latestElements + newElement)
                                    }
                                    currentPath = emptyList()
                                    isDrawing = false
                                },
                                onDrag = { change, _ ->
                                    if (currentTool == WhiteboardTool.HAND) return@detectDragGestures
                                    change.consume()
                                    currentPath = currentPath + Point(change.position.x, change.position.y)
                                }
                            )
                        }
                ) {
                    // Grid
                    val gridSize = 50f
                    val gridColor = Color(0xFFE0E0E0)
                    var x = 0f
                    while (x < size.width) {
                        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 0.5f)
                        x += gridSize
                    }
                    var y = 0f
                    while (y < size.height) {
                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 0.5f)
                        y += gridSize
                    }
                    // Existing elements as simple strokes
                    elements.forEach { el ->
                        if (el.points.size > 1) {
                            val path = Path().apply {
                                moveTo(el.points[0].x, el.points[0].y)
                                for (i in 1 until el.points.size) lineTo(el.points[i].x, el.points[i].y)
                            }
                            val col = try { Color(android.graphics.Color.parseColor(el.color)) } catch (_: Exception) { Color.Black }
                            drawPath(path, col, style = Stroke(width = el.strokeWidth))
                        }
                    }
                    // Current path
                    if (currentPath.size > 1) {
                        val path = Path().apply {
                            moveTo(currentPath[0].x, currentPath[0].y)
                            for (i in 1 until currentPath.size) lineTo(currentPath[i].x, currentPath[i].y)
                        }
                        drawPath(path, strokeColor, style = Stroke(width = strokeWidth))
                    }
                }
                if (!readOnly) {
                    // PLUS / MINUS brutal squares at TOP LEFT corner — as requested
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.align(Alignment.TopStart).padding(start = 10.dp, top = 10.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        for ((icon, act) in listOf(
                            Icons.Filled.Add to { scale = (scale + 0.2f).coerceIn(0.6f, 3.5f) },
                            Icons.Filled.Remove to { scale = (scale - 0.2f).coerceIn(0.6f, 3.5f) }
                        )) {
                            Box(modifier = Modifier.size(42.dp).padding(end = 3.dp, bottom = 3.dp)) {
                                Box(modifier = Modifier.matchParentSize().offset(x = 3.dp, y = 3.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black))
                                Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(Color.White).border(androidx.compose.foundation.BorderStroke(2.dp, Color.Black), RoundedCornerShape(12.dp)).clickable { act() }, contentAlignment = Alignment.Center) {
                                    Icon(icon, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                    // Tools vertical at bottom-left + caption
                    WhiteboardToolbar(
                        modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 90.dp),
                        currentTool = currentTool,
                        onToolChange = onToolChange,
                        onClear = { onElementsChange(emptyList()) }
                    )
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 12.dp)
                    ) {
                        Box(modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)) {
                            Box(modifier = Modifier.matchParentSize().offset(x = 4.dp, y = 4.dp).clip(RoundedCornerShape(10.dp)).background(Color.Black))
                            Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color.White).border(androidx.compose.foundation.BorderStroke(2.dp, Color.Black), RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                                androidx.compose.material3.Text("Whiteboard • Draw  ${String.format("%.0f%%", scale * 100)}", fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WhiteboardToolbar(
    modifier: Modifier = Modifier,
    currentTool: WhiteboardTool,
    onToolChange: (WhiteboardTool) -> Unit,
    onClear: () -> Unit
) {
    // Vertical column at bottom-left, SQUARE rounded-squares with brutal bottom/right shadow — per your ask (not circles)
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val tools = listOf(
                WhiteboardTool.PEN to Icons.Filled.Edit,
                WhiteboardTool.ERASER to Icons.Filled.CleaningServices,
                WhiteboardTool.HAND to Icons.Filled.PanTool,
                WhiteboardTool.TEXT to Icons.Filled.TextFields
            )
            tools.forEach { (tool, icon) ->
                Box(modifier = Modifier.size(44.dp).padding(end = 3.dp, bottom = 3.dp)) {
                    Box(modifier = Modifier.matchParentSize().offset(x = 3.dp, y = 3.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black))
                    Box(
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                            .background(if (currentTool == tool) Color.Black else Color.White)
                            .border(androidx.compose.foundation.BorderStroke(2.dp, Color.Black), RoundedCornerShape(12.dp))
                            .clickable { onToolChange(tool) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = tool.name, tint = if (currentTool == tool) Color.White else Color.Black, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Box(modifier = Modifier.size(44.dp).padding(end = 3.dp, bottom = 3.dp)) {
                Box(modifier = Modifier.matchParentSize().offset(x = 3.dp, y = 3.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black))
                Box(
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(Color.White)
                        .border(androidx.compose.foundation.BorderStroke(2.dp, Color.Black), RoundedCornerShape(12.dp))
                        .clickable { onClear() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Delete, contentDescription = "Clear", tint = Color.Black, modifier = Modifier.size(18.dp)) }
            }
        }
    }
}