package com.vastavik.computer.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.zIndex
import com.vastavik.computer.ui.screens.onboarding.SettingsViewModel
import com.vastavik.computer.utils.CodeOssManager
import com.vastavik.computer.ui.theme.BrutalCard
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor

@Composable
fun ProfileScreen(
    onNavigate: (String) -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    val context = androidx.compose.ui.platform.LocalContext.current
    val isAdmin by com.vastavik.computer.utils.AdminSession.isAdmin.collectAsState()
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState(initial = false)
    val updateInfo by com.vastavik.computer.utils.AppUpdater.updateState.collectAsState()
    val isUpdateAvailable = updateInfo?.isUpdateAvailable == true

    var profileName by remember { mutableStateOf("Student") }
    var profileEmail by remember { mutableStateOf("student@example.com") }
    var isCompanionInstalled by remember { mutableStateOf(CodeOssManager.isCompanionInstalled(context)) }
    var isCodeOssPreferred by remember { mutableStateOf(CodeOssManager.isCodeOssPreferred(context)) }
    var showCodeOssSheet by remember { mutableStateOf(false) }
    val downloadState by CodeOssManager.downloadState.collectAsState()

    // Shimmer animation for the extension pack option
    val infiniteTransition = rememberInfiniteTransition(label = "codeoss_shine")
    val shineOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shineOffset"
    )
    val shineBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFD600),
            Color(0xFFFF6D00),
            Color(0xFF8B5CF6),
            Color(0xFF00E5FF),
            Color(0xFFFFD600)
        ),
        start = Offset(shineOffset, 0f),
        end = Offset(shineOffset + 350f, 350f)
    )

    LaunchedEffect(Unit) {
        if (updateInfo == null) {
            com.vastavik.computer.utils.AppUpdater.checkGitHubRelease()
        }
        isCompanionInstalled = CodeOssManager.isCompanionInstalled(context)
        isCodeOssPreferred = CodeOssManager.isCodeOssPreferred(context)
        val prefs = context.getSharedPreferences("user_profile", android.content.Context.MODE_PRIVATE)
        val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val rawName = prefs.getString("name", null) ?: firebaseUser?.displayName
        val resolved = com.vastavik.computer.utils.DisplayName.resolveForUser(rawName, isAdmin)
        if (resolved.isNotBlank()) profileName = resolved
        val savedEmail = firebaseUser?.email ?: prefs.getString("email", null)
        if (!savedEmail.isNullOrBlank()) profileEmail = savedEmail
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Top Bar with Back Button, VastavikComputer brand, and Sun/Moon Theme Toggle
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onNavigate("home") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Vastavik",
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Computer",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    // Sun / Moon theme toggle button
                    Box(modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 2.dp, y = 2.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bs)
                        )
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(10.dp))
                                .clickable { settingsViewModel.setDarkMode(!isDarkMode) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                                tint = if (isDarkMode) Color(0xFFFFD600) else Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Deep green rounded notification button
                    Box(modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 2.dp, y = 2.dp)
                                .clip(CircleShape)
                                .background(bs)
                        )
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
                            if (isUpdateAvailable) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-4).dp, y = 4.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFD600))
                                        .border(BorderStroke(1.dp, Color.Black), CircleShape)
                                        .zIndex(2f)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Profile Header Brutal Card
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFF06B6D4))
                                    )
                                )
                        ) {
                            // Small chat chip pinned to the top-right of the gradient header in blue
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 12.dp, end = 12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 3.dp, bottom = 3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .offset(x = 3.dp, y = 3.dp)
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(bs)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(Color(0xFF2563EB))
                                            .border(BorderStroke(1.5.dp, Color.White), RoundedCornerShape(50.dp))
                                            .clickable { onNavigate("peer_chat") },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Forum,
                                            contentDescription = "Open Peer Chat",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .offset(x = 200.dp, y = (-20).dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .offset(x = (-30).dp, y = 80.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                )
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Spacer(modifier = Modifier.height(18.dp))
                                    // Avatar with brutal border
                                    Box(
                                        modifier = Modifier
                                            .size(84.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color.White)
                                            .border(BorderStroke(2.dp, bb), RoundedCornerShape(20.dp))
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Filled.Person,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = profileName,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = profileEmail,
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.surface)
                                                .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(16.dp))
                                                .padding(12.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0xFFFFF7ED))
                                                        .border(BorderStroke(1.dp, bb.copy(alpha = 0.2f)), RoundedCornerShape(10.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("🔥", fontSize = 18.sp)
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text("7 days", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground, lineHeight = 18.sp)
                                                    Text("Day streak", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 14.sp)
                                                }
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.surface)
                                                .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(16.dp))
                                                .padding(12.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0xFFEEF2FF))
                                                        .border(BorderStroke(1.dp, bb.copy(alpha = 0.2f)), RoundedCornerShape(10.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("📚", fontSize = 18.sp)
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text("24", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground, lineHeight = 18.sp)
                                                    Text("Lessons done", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 14.sp)
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

            item { Spacer(modifier = Modifier.height(18.dp)) }

            // Premium card brutal
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate("payment") },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(2.dp, bb),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(Color(0xFFFBBF24), Color(0xFFF97316))
                                            )
                                        )
                                        .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("★", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Upgrade to Premium",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        "Unlock all lessons + AI chat",
                                        color = Color.White.copy(alpha = 0.65f),
                                        fontSize = 12.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(BorderStroke(1.5.dp, bb), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("→", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(18.dp)) }

            // Menu items brutal
            item {
                BrutalCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        // Shining First Option: CodeOSS Extension Pack (Ubuntu Terminal + VS Code)
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showCodeOssSheet = true }
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF7C3AED).copy(alpha = 0.08f),
                                                Color(0xFFFFD600).copy(alpha = 0.08f)
                                            )
                                        )
                                    )
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                                            )
                                        )
                                        .border(BorderStroke(2.dp, shineBrush), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Terminal,
                                        contentDescription = "CodeOSS Extension",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "CodeOSS Extension Pack",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        if (isCompanionInstalled) "Ubuntu Terminal + VS Code • Active" else "Minimal Ubuntu Terminal & VS Code • Tap to Install",
                                        fontSize = 11.sp,
                                        fontWeight = if (!isCompanionInstalled) FontWeight.Bold else FontWeight.Normal,
                                        color = if (!isCompanionInstalled) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isCompanionInstalled) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                                        .border(
                                            BorderStroke(1.dp, if (isCompanionInstalled) Color(0xFF16A34A) else Color(0xFFD97706)),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        if (isCompanionInstalled) "ACTIVE" else "INSTALL",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCompanionInstalled) Color(0xFF15803D) else Color(0xFFB45309)
                                    )
                                }
                            }
                            HorizontalDivider(
                                thickness = 1.5.dp,
                                color = bb
                            )
                        }

                        val menuItems = buildList {
                            add(Quadruple("Edit Profile", "Manage info & avatar", Icons.Filled.Edit, "edit_profile"))
                            add(
                                Quadruple(
                                    "App Update",
                                    if (isUpdateAvailable) "v${updateInfo?.latestVersion} available • Tap to update" else "Version & changelog",
                                    Icons.Filled.SystemUpdate,
                                    "app_update"
                                )
                            )
                            add(Quadruple("Select Course", "Choose your path", Icons.Filled.MenuBook, "course"))
                            add(Quadruple("Online Class", "Join live session", Icons.Filled.VideoCall, "meeting_lobby/default_live_class"))
                            add(Quadruple("Code Editor", "Practice live", Icons.Filled.Code, "code_editor"))
                            add(Quadruple("OCR Exercise", "Scan & solve", Icons.Filled.DocumentScanner, "ocr_exercise"))
                            add(Quadruple("My Notes", "Your saved notes", Icons.Filled.Note, "my_notes"))
                            add(Quadruple("Payment History", "Invoices & plans", Icons.Filled.Receipt, "payment_history"))
                            add(Quadruple("Settings", "Theme & prefs", Icons.Filled.Settings, "settings"))
                            add(Quadruple("Bug Reporting", "Report issues & attach media", Icons.Filled.BugReport, "bug_report"))
                            if (isAdmin) {
                                add(Quadruple("Admin Access", "Engine controls & diagnostics", Icons.Filled.AdminPanelSettings, "admin"))
                            }
                        }

                        val comingSoonTitles = setOf("Select Course", "Online Class", "OCR Exercise")
                        menuItems.forEachIndexed { index, (title, desc, icon, route) ->
                            val isComingSoon = title in comingSoonTitles
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isComingSoon) {
                                                onNavigate("coming_soon/${java.net.URLEncoder.encode(title, "UTF-8")}")
                                            } else {
                                                onNavigate(route)
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (title == "App Update" && isUpdateAvailable) Color(0xFF2563EB).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
                                            .border(BorderStroke(1.5.dp, if (title == "App Update" && isUpdateAvailable) Color(0xFF2563EB) else bb), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            icon,
                                            contentDescription = null,
                                            tint = if (title == "App Update" && isUpdateAvailable) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            desc,
                                            fontSize = 11.sp,
                                            fontWeight = if (title == "App Update" && isUpdateAvailable) FontWeight.Bold else FontWeight.Normal,
                                            color = if (title == "App Update" && isUpdateAvailable) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (title == "App Update" && isUpdateAvailable) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50.dp))
                                                .background(Color(0xFF2563EB))
                                                .border(BorderStroke(1.dp, bb), RoundedCornerShape(50.dp))
                                                .padding(horizontal = 9.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "UPDATE",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    if (isComingSoon) {
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
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Icon(
                                        Icons.Filled.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (index < menuItems.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    com.vastavik.computer.utils.AdminSession.setAdminLoggedIn(context, false)
                                    try {
                                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                    } catch (_: Exception) {}
                                    onNavigate("login")
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Logout,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                "Log Out",
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFFEF4444)
                            )
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showCodeOssSheet) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showCodeOssSheet = false },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .wrapContentHeight()
                        .padding(end = 4.dp, bottom = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 5.dp, y = 5.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(bb)
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(2.dp, bb)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            // Header with Terminal Icon & Close Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF2563EB))
                                        .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Terminal,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Vastavik CodeOSS Studio",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Ubuntu Linux Server & Integrated Terminal",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { showCodeOssSheet = false },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Close")
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Feature Breakdown Cards
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Card 1: Minimal Ubuntu
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .border(BorderStroke(1.dp, bb.copy(alpha = 0.4f)), RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🐧", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            "Minimal Ubuntu Terminal",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            "Stripped to raw bash + coreutils with zero background overhead. Real Linux execution without root.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Card 2: Proper VS Code OSS
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .border(BorderStroke(1.dp, bb.copy(alpha = 0.4f)), RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("💻", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            "CodeOSS (VS Code Web Engine)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            "Official VS Code OSS core with multi-file tabs, file tree, syntax highlights & open-vsx extensions.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Card 3: Modular Companion APK
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .border(BorderStroke(1.dp, bb.copy(alpha = 0.4f)), RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📦", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            "Separate APK Extension Pack",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            "Keeps the main app lightweight. If uninstalled, the default built-in editor stays active seamlessly.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Installation / Action Area
                            if (isCompanionInstalled) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFDCFCE7))
                                            .border(BorderStroke(1.dp, Color(0xFF16A34A)), RoundedCornerShape(10.dp))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF15803D))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Extension Installed & Ready", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF15803D))
                                            Text("Package: com.vastavik.codeoss", fontSize = 11.sp, color = Color(0xFF166534))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val newState = !isCodeOssPreferred
                                                isCodeOssPreferred = newState
                                                CodeOssManager.setCodeOssPreferred(context, newState)
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isCodeOssPreferred,
                                            onCheckedChange = {
                                                isCodeOssPreferred = it
                                                CodeOssManager.setCodeOssPreferred(context, it)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Use CodeOSS as primary code editor", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Box(modifier = Modifier.fillMaxWidth().padding(end = 3.dp, bottom = 3.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .offset(x = 3.dp, y = 3.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(bs)
                                        )
                                        Button(
                                            onClick = {
                                                showCodeOssSheet = false
                                                CodeOssManager.launchCodeOss(context)
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.5.dp, bb)
                                        ) {
                                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Open CodeOSS Studio", fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            } else {
                                when (val state = downloadState) {
                                    is CodeOssManager.DownloadState.Idle -> {
                                        Box(modifier = Modifier.fillMaxWidth().padding(end = 3.dp, bottom = 3.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .offset(x = 3.dp, y = 3.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(bs)
                                            )
                                            Button(
                                                onClick = {
                                                    CodeOssManager.downloadCompanion(context) { file ->
                                                        isCompanionInstalled = CodeOssManager.isCompanionInstalled(context)
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(1.5.dp, bb)
                                            ) {
                                                Icon(Icons.Filled.Download, contentDescription = null, tint = Color.White)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Download & Install Extension", fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                    is CodeOssManager.DownloadState.Downloading -> {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Downloading Extension...", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                Text("${state.progress}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            LinearProgressIndicator(
                                                progress = { state.progress / 100f },
                                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                                color = Color(0xFF7C3AED)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "${state.downloadedBytes / (1024 * 1024)} MB / ${state.totalBytes / (1024 * 1024)} MB",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    is CodeOssManager.DownloadState.ReadyToInstall -> {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text("Download complete! Ready to install.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    val intent = CodeOssManager.buildInstallIntent(context, state.apkFile)
                                                    if (intent != null) context.startActivity(intent)
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(1.5.dp, bb)
                                            ) {
                                                Icon(Icons.Filled.InstallMobile, contentDescription = null, tint = Color.White)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Install Now", fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                    is CodeOssManager.DownloadState.Error -> {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text("Error: ${state.message}", fontSize = 11.sp, color = Color(0xFFDC2626))
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Button(
                                                onClick = {
                                                    CodeOssManager.downloadCompanion(context)
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(1.5.dp, bb)
                                            ) {
                                                Text("Retry Download", fontWeight = FontWeight.Bold, color = Color.White)
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
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
