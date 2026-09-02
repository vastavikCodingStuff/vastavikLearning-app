package com.vastavik.computer.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun neoShape(defaultRadius: Dp): Shape {
    val isNeo = MaterialTheme.shapes.medium.toString().contains("0.0")
    return if (isNeo) RoundedCornerShape(0.dp) else RoundedCornerShape(defaultRadius)
}

@Composable
fun neoCircleShape(): Shape {
    val isNeo = MaterialTheme.shapes.medium.toString().contains("0.0")
    return if (isNeo) RoundedCornerShape(0.dp) else CircleShape
}
