package com.vastavik.computer.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object BrutalDefaults {
    val BorderWidth: Dp = 2.dp
    val ShadowOffset: Dp = 5.dp
    val Radius: Dp = 16.dp
    val RadiusLarge: Dp = 20.dp
    val RadiusPill: Dp = 50.dp
    val Black: Color = Color(0xFF000000)
}

@Composable
fun BrutalCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(BrutalDefaults.Radius),
    borderWidth: Dp = BrutalDefaults.BorderWidth,
    shadowOffset: Dp = BrutalDefaults.ShadowOffset,
    borderColor: Color = BrutalDefaults.Black,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier.padding(end = shadowOffset, bottom = shadowOffset)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .clip(shape)
                .background(BrutalDefaults.Black)
        )
        Card(
            modifier = Modifier
                .fillMaxSize()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            border = BorderStroke(borderWidth, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun BrutalBoxCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(BrutalDefaults.Radius),
    borderWidth: Dp = BrutalDefaults.BorderWidth,
    shadowOffset: Dp = BrutalDefaults.ShadowOffset,
    borderColor: Color = BrutalDefaults.Black,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.padding(end = shadowOffset, bottom = shadowOffset)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .clip(shape)
                .background(BrutalDefaults.Black)
        )
        Card(
            modifier = Modifier
                .fillMaxSize()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            border = BorderStroke(borderWidth, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), content = content)
        }
    }
}

@Composable
fun BrutalGradientCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(BrutalDefaults.RadiusLarge),
    borderWidth: Dp = BrutalDefaults.BorderWidth,
    shadowOffset: Dp = BrutalDefaults.ShadowOffset,
    gradient: Brush,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier.padding(end = shadowOffset, bottom = shadowOffset)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .clip(shape)
                .background(BrutalDefaults.Black)
        )
        Card(
            modifier = Modifier
                .fillMaxSize()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(borderWidth, BrutalDefaults.Black),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradient)
            ) {
                Column(content = content)
            }
        }
    }
}
