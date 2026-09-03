package com.vastavik.computer.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.ExperimentalFoundationApi
import com.vastavik.computer.ui.components.VastavikTopBar
import com.vastavik.computer.ui.components.PromoPopup
import com.vastavik.computer.ui.components.PromoData
import com.vastavik.computer.ui.components.UnderDevelopmentBanner
import com.vastavik.computer.ui.theme.BrutalBoxCard
import com.vastavik.computer.ui.theme.BrutalCard
import com.vastavik.computer.ui.theme.BrutalDefaults
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor

private var promoShown = false
private var devBannerShown = false

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(initialPage = 1) { 5 }
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var showPromo by remember { mutableStateOf(!promoShown) }
    var showDevBanner by remember { mutableStateOf(!devBannerShown) }

    val sampleCourses = remember {
        listOf(
            Triple("Java Programming", Color(0xFF8B5CF6) to Color(0xFF6366F1), "42 lessons"),
            Triple("Python Basics", Color(0xFF10B981) to Color(0xFF14B8A6), "36 lessons"),
            Triple("Data Structures", Color(0xFFF59E0B) to Color(0xFFF97316), "28 lessons"),
            Triple("Web Development", Color(0xFF06B6D4) to Color(0xFF3B82F6), "51 lessons")
        )
    }

    if (showPromo) {
        promoShown = true
        PromoPopup(
            promo = PromoData(title="50% OFF Premium!", body="Get full access to Java/Python/JS/SQL + AI Chat & papers. UPI AutoPay Rs 149/mo.", ctaText="Grab Now"),
            onDismiss = { showPromo = false },
            onCta = { showPromo = false; onNavigate("payment") }
        )
    }

    if (showDevBanner) {
        devBannerShown = true
        UnderDevelopmentBanner(
            onDismiss = { showDevBanner = false }
        )
    }

    // Intercept back button if not on Home tab (page 1) to return to Home
    BackHandler(enabled = pagerState.currentPage != 1) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(1, animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
        }
    }

    val targetPage = if (pagerState.isScrollInProgress) pagerState.targetPage else pagerState.currentPage
    val currentTab = targetPage - 1

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(
                selectedIndex = currentTab,
                onItemSelected = { tabIdx ->
                    val page = tabIdx + 1
                    if (page != pagerState.currentPage) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                page = page,
                                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        val tabNav: (String) -> Unit = remember(onNavigate) {
            { route ->
                when (route) {
                    "profile" -> coroutineScope.launch { pagerState.animateScrollToPage(0, animationSpec = tween(220, easing = FastOutSlowInEasing)) }
                    "home" -> coroutineScope.launch { pagerState.animateScrollToPage(1, animationSpec = tween(220, easing = FastOutSlowInEasing)) }
                    "learning_path" -> coroutineScope.launch { pagerState.animateScrollToPage(2, animationSpec = tween(220, easing = FastOutSlowInEasing)) }
                    "practice" -> coroutineScope.launch { pagerState.animateScrollToPage(3, animationSpec = tween(220, easing = FastOutSlowInEasing)) }
                    "chat" -> coroutineScope.launch { pagerState.animateScrollToPage(4, animationSpec = tween(220, easing = FastOutSlowInEasing)) }
                    else -> onNavigate(route)
                }
            }
        }

        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
            key = { it }
        ) { page ->
            when (page) {
                0 -> com.vastavik.computer.ui.screens.profile.ProfileScreen(onNavigate = tabNav)
                1 -> HomeTab(
                    modifier = Modifier.fillMaxSize(),
                    onNavigate = tabNav,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    courses = sampleCourses
                )
                2 -> com.vastavik.computer.ui.screens.learning.LearningPathScreen(onNavigate = tabNav)
                3 -> com.vastavik.computer.ui.screens.practice.PracticeScreen(onNavigate = tabNav)
                4 -> com.vastavik.computer.ui.screens.chat.ChatScreen(onNavigate = tabNav)
            }
        }
    }
}

@Composable
private fun HomeTab(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    courses: List<Triple<String, Pair<Color, Color>, String>>
) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            VastavikTopBar(
                onProfileClick = { onNavigate("profile") },
                onNotificationClick = { onNavigate("notifications") }
            )
        }

        // Hero Brutal Card with integrated stats footer
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp).padding(end = 5.dp, bottom = 5.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(bs)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(2.dp, bb),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        // Gradient top
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF2563EB), Color(0xFF7C3AED), Color(0xFF06B6D4))
                                    )
                                )
                        ) {
                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(140.dp)
                                        .offset(x = 100.dp, y = (-40).dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .offset(x = (-20).dp, y = 80.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                )
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = "WELCOME BACK",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.85f),
                                        letterSpacing = 1.4.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Hello, Student \uD83D\uDC4B",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Ready to write some code? Pick up where you left off.",
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(18.dp))
                                    // Search pill
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(BorderStroke(2.dp, bb), RoundedCornerShape(50.dp))
                                            .clickable { onNavigate("search") }
                                            .padding(horizontal = 16.dp, vertical = 14.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Filled.Search,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = if (searchQuery.isEmpty()) "Search courses, topics, lessons..." else searchQuery,
                                                fontSize = 13.sp,
                                                color = if (searchQuery.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        // Stats footer strip (theme-aware)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "7 day streak",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "  |  ",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = "65% avg progress",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(22.dp)) }

        // Continue Learning
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Continue Learning",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "View all →",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.clickable { onNavigate("learning_path") }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            ContinueLearningCard(onNavigate = onNavigate)
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }

        // Course Catalog
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Course Catalog",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Browse all →",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.clickable { onNavigate("learning_path") }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            CourseCatalogGrid(courses = courses, onNavigate = onNavigate)
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }

        // Stats section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                StatsBrutalCard(
                    modifier = Modifier.width(100.dp),
                    icon = "◐",
                    value = "12.4k",
                    label = "Active learners",
                    iconBg = Color(0xFFEEF2FF),
                    iconTint = Color(0xFF6366F1)
                )
                StatsBrutalCard(
                    modifier = Modifier.width(100.dp),
                    icon = "✓",
                    value = "500+",
                    label = "Hands-on lessons",
                    iconBg = Color(0xFFECFDF5),
                    iconTint = Color(0xFF10B981)
                )
                StatsBrutalCard(
                    modifier = Modifier.width(100.dp),
                    icon = "✦",
                    value = "4.8/5",
                    label = "Avg rating",
                    iconBg = Color(0xFFFFFBE6),
                    iconTint = Color(0xFFF59E0B)
                )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        // Popular Topics
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Popular Topics",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            val topics = listOf("OOP Concepts", "Arrays & Lists", "Sorting Algorithms", "File Handling")
            topics.forEach { topic ->
                PopularTopicItem(title = topic, subject = "CS", duration = "15 min")
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun ContinueLearningCard(onNavigate: (String) -> Unit) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    Box(modifier = Modifier.padding(horizontal = 16.dp).padding(end = 5.dp, bottom = 5.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 5.dp, y = 5.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(bs)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate("video_lesson/1/1/1/1") },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(2.dp, bb),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("</>", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Java Programming",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.White.copy(alpha = 0.14f))
                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(50.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "In progress",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Object-Oriented Programming — OOP Concepts",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Lesson 6 of 12 • 18 min",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(50.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.65f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.White)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progress 65%",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.White)
                                .border(BorderStroke(2.dp, bb), RoundedCornerShape(50.dp))
                                .clickable { onNavigate("video_lesson/1/1/1/1") }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Continue →",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseCatalogGrid(
    courses: List<Triple<String, Pair<Color, Color>, String>>,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            courses.take(2).forEach { (title, colors, lessons) ->
                CourseCatalogCard(
                    modifier = Modifier.weight(1f),
                    title = title,
                    colors = colors,
                    lessons = lessons,
                    onClick = {
                        if (title == "Java Programming") {
                            onNavigate("learning_path")
                        } else {
                            onNavigate("coming_soon/${java.net.URLEncoder.encode(title, "UTF-8")}")
                        }
                    }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            courses.drop(2).forEach { (title, colors, lessons) ->
                CourseCatalogCard(
                    modifier = Modifier.weight(1f),
                    title = title,
                    colors = colors,
                    lessons = lessons,
                    onClick = {
                        if (title == "Java Programming") {
                            onNavigate("learning_path")
                        } else {
                            onNavigate("coming_soon/${java.net.URLEncoder.encode(title, "UTF-8")}")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CourseCatalogCard(
    modifier: Modifier = Modifier,
    title: String,
    colors: Pair<Color, Color>,
    lessons: String,
    onClick: () -> Unit
) {
    val bb = brutalBorderColor()
    BrutalCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(colors = listOf(colors.first, colors.second)))
                    .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    when (title) {
                        "Java Programming" -> "☕"
                        "Python Basics" -> "🐍"
                        "Data Structures" -> "◈"
                        else -> "</>"
                    },
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = lessons,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Explore →",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2563EB)
            )
        }
    }
}

@Composable
private fun StatsBrutalCard(
    modifier: Modifier = Modifier,
    icon: String,
    value: String,
    label: String,
    iconBg: Color,
    iconTint: Color
) {
    val bb = brutalBorderColor()
    BrutalCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg)
                    .border(BorderStroke(1.5.dp, bb.copy(alpha = 0.12f)), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 18.sp, color = iconTint, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PopularTopicItem(title: String, subject: String, duration: String) {
    BrutalCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$subject • $duration",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(50.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Coming soon",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BottomNavBar(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    val items = listOf(
        Triple(Icons.Filled.Home, "Home", 0),
        Triple(Icons.Filled.Map, "Learn", 1),
        Triple(Icons.Filled.Assignment, "Practice", 2),
        Triple(Icons.Filled.SmartToy, "AI Chat", 3)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp, bottom = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 5.dp, y = 5.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(bs)
            )
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(2.dp, bb),
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { (icon, label, index) ->
                        val isSelected = selectedIndex == index
                        val pillBg = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        val pillPadding = if (isSelected) 18.dp else 14.dp
                        val iconScale = if (isSelected) 1.12f else 1f
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(pillBg)
                                .border(
                                    if (isSelected) BorderStroke(1.5.dp, bb) else BorderStroke(0.dp, Color.Transparent),
                                    RoundedCornerShape(50.dp)
                                )
                                .clickable { onItemSelected(index) }
                                .padding(horizontal = pillPadding, vertical = 10.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    icon,
                                    contentDescription = label,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .scale(iconScale)
                                )
                                AnimatedVisibility(
                                    visible = isSelected,
                                    enter = expandHorizontally(
                                        animationSpec = tween(140, easing = FastOutSlowInEasing)
                                    ) + fadeIn(tween(120)),
                                    exit = shrinkHorizontally(tween(100)) + fadeOut(tween(80))
                                ) {
                                    Row {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            label,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
