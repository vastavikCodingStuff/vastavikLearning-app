package com.vastavik.computer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.ui.theme.VastavikColors
import kotlin.math.sin

enum class PathNodeState {
    Completed, Current, Locked
}

data class PathNodeData(
    val index: Int,
    val label: String,
    val state: PathNodeState,
    val offset: Float = 0f
)

@Composable
fun PathNode(
    number: Int,
    state: PathNodeState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val backgroundColor = when (state) {
        PathNodeState.Completed -> VastavikColors.LightSuccess
        PathNodeState.Current -> MaterialTheme.colorScheme.primary
        PathNodeState.Locked -> MaterialTheme.colorScheme.outline
    }
    val contentColor = Color.White
    val scale by animateFloatAsState(
        targetValue = if (state == PathNodeState.Current) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "nodeScale"
    )

    Box(
        modifier = modifier
            .size(size * scale)
            .shadow(
                elevation = if (state == PathNodeState.Current) 8.dp else 4.dp,
                shape = CircleShape,
                ambientColor = backgroundColor.copy(alpha = 0.3f)
            )
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.8f)
                    )
                )
            )
            .clickable(enabled = state != PathNodeState.Locked) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            PathNodeState.Completed -> {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Completed",
                    tint = contentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            PathNodeState.Current -> {
                Text(
                    text = "$number",
                    color = contentColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            PathNodeState.Locked -> {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Locked",
                    tint = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun PathConnector(
    startOffset: Offset,
    endOffset: Offset,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isCompleted) VastavikColors.LightSuccess else MaterialTheme.colorScheme.outline
    val animProgress by rememberInfiniteTransition(label = "connectorAnim").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "connectorProgress"
    )
    val dashPhase by rememberInfiniteTransition(label = "dashAnim").animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing)
        ),
        label = "dashPhase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val midX = (startOffset.x + endOffset.x) / 2
        val midY = (startOffset.y + endOffset.y) / 2
        val controlOffset = 40f * animProgress

        val path = Path().apply {
            moveTo(startOffset.x, startOffset.y)
            cubicTo(
                startOffset.x, startOffset.y + controlOffset,
                endOffset.x, endOffset.y - controlOffset,
                endOffset.x, endOffset.y
            )
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = if (!isCompleted) {
                    androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(10f, 10f),
                        dashPhase
                    )
                } else null
            )
        )
    }
}

@Composable
fun TrophyNode(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isUnlocked: Boolean = false
) {
    val backgroundColor = if (isUnlocked) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFBBF24),
                Color(0xFFF59E0B)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.outline,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
            )
        )
    }

    val pulseAnim by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .size(80.dp * if (isUnlocked) pulseAnim else 1f)
            .shadow(
                elevation = if (isUnlocked) 12.dp else 4.dp,
                shape = CircleShape,
                ambientColor = if (isUnlocked) Color(0xFFFBBF24).copy(alpha = 0.4f) else Color.Transparent
            )
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(enabled = isUnlocked) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Trophy",
                tint = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = if (isUnlocked) "PRO" else "LOCKED",
                color = Color.White.copy(alpha = if (isUnlocked) 1f else 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DuolingoPath(
    nodes: List<PathNodeData>,
    modifier: Modifier = Modifier,
    onNodeClick: (Int) -> Unit,
    onTrophyClick: () -> Unit = {},
    showTrophy: Boolean = true
) {
    val nodeSpacing = 120.dp
    val horizontalPadding = 40.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        nodes.forEachIndexed { index, node ->
            val xOffset = if (index % 2 == 0) 0f else 60f

            Box(
                modifier = Modifier
                    .offset(x = xOffset.dp)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PathNode(
                        number = node.index + 1,
                        state = node.state,
                        onClick = { onNodeClick(node.index) },
                        size = if (node.state == PathNodeState.Current) 72.dp else 60.dp
                    )

                    Text(
                        text = node.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (node.state) {
                            PathNodeState.Locked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            if (index < nodes.lastIndex) {
                Box(
                    modifier = Modifier
                        .offset(x = xOffset.dp)
                        .height(40.dp)
                        .width(4.dp)
                ) {
                    val connectorColor = if (node.state == PathNodeState.Completed) {
                        VastavikColors.LightSuccess
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawLine(
                            color = connectorColor,
                            start = Offset(size.width / 2, 0f),
                            end = Offset(size.width / 2, size.height),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        if (showTrophy) {
            Spacer(modifier = Modifier.height(16.dp))
            TrophyNode(
                onClick = onTrophyClick,
                isUnlocked = nodes.all { it.state == PathNodeState.Completed }
            )
        }
    }
}
