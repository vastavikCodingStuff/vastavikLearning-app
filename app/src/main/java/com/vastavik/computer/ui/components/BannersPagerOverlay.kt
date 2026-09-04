package com.vastavik.computer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

/**
 * Modal overlay that hosts a paged list of in-app announcements.
 *
 * Behaviour per spec:
 *  - One banner at a time, in a swipeable HorizontalPager.
 *  - Small left/right arrow buttons (the user can also swipe).
 *  - Page indicator dots underneath.
 *  - Tapping the dimmed area OUTSIDE the banner card dismisses the overlay.
 */
@Composable
fun BannersPagerOverlay(
    pages: List<BannerPage>,
    onDismiss: () -> Unit,
    onCta: (BannerPage) -> Unit
) {
    if (pages.isEmpty()) {
        Box(Modifier.fillMaxSize())
        return
    }
    val pagerState = rememberPagerState(initialPage = 0) { pages.size }
    val scope = rememberCoroutineScope()
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    val PrimaryIndigo = Color(0xFF2563EB)

    // Whole screen is the tappable area; banner card lives centered inside.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(enabled = true, onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        // Inner card consumes taps so they don't fall through to the scrim.
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.55f)
                .clickable(enabled = false) { /* swallow */ }
        ) {
            // Drop shadow layer
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(start = 5.dp, top = 5.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(bs)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(BorderStroke(2.dp, bb), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Pager
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        pageSpacing = 0.dp,
                        beyondViewportPageCount = 1
                    ) { pageIndex ->
                        BannerCard(
                            page = pages[pageIndex],
                            onCta = { onCta(pages[pageIndex]) }
                        )
                    }
                    // Controls: arrows + dots
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ArrowButton(
                            icon = Icons.Filled.ArrowBack,
                            contentDescription = "Previous banner",
                            enabled = pagerState.currentPage > 0
                        ) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            pages.indices.forEach { i ->
                                val isSelected = i == pagerState.currentPage
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .size(if (isSelected) 9.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) PrimaryIndigo else bb.copy(alpha = 0.35f))
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        ArrowButton(
                            icon = Icons.Filled.ArrowForward,
                            contentDescription = "Next banner",
                            enabled = pagerState.currentPage < pages.lastIndex
                        ) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArrowButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bb = brutalBorderColor()
    val tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.5.dp, bb), CircleShape)
            .let { if (enabled) it.clickable(onClick = onClick) else it },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun BannerCard(page: BannerPage, onCta: () -> Unit) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    val PrimaryIndigo = Color(0xFF2563EB)
    Column(modifier = Modifier.fillMaxSize()) {
        // Gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF2563EB), Color(0xFF7C3AED), Color(0xFF06B6D4))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (page.accent == BannerAccent.PROMO) Icons.Filled.Celebration else Icons.Filled.Build,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)), RoundedCornerShape(50.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (page.accent == BannerAccent.PROMO) "PROMO" else "BETA",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = page.title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        // Body
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = if (page.accent == BannerAccent.PROMO) Icons.Filled.Celebration else Icons.Filled.RocketLaunch,
                    contentDescription = null,
                    tint = PrimaryIndigo,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = page.body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 5.dp, bottom = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(start = 5.dp, top = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bs)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryIndigo)
                        .border(BorderStroke(2.dp, bb), RoundedCornerShape(12.dp))
                        .clickable(onClick = onCta),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = page.ctaText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

enum class BannerAccent { PROMO, DEV }

data class BannerPage(
    val title: String,
    val body: String,
    val ctaText: String,
    val accent: BannerAccent = BannerAccent.PROMO
)
