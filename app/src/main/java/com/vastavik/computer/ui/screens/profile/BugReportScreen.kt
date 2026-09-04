package com.vastavik.computer.ui.screens.profile

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.vastavik.computer.BuildConfig
import com.vastavik.computer.ui.theme.BrutalBoxCard
import com.vastavik.computer.ui.theme.BrutalCard
import com.vastavik.computer.ui.theme.BrutalDefaults
import com.vastavik.computer.ui.theme.NeoBrutalistColors
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugReportScreen(onBack: () -> Unit) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var issueTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("UI / Display") }
    var selectedSeverity by remember { mutableStateOf("Medium") }
    var issueDescription by remember { mutableStateOf("") }
    var stepsToReproduce by remember { mutableStateOf("") }

    var screenshots by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var screenRecordingUri by remember { mutableStateOf<Uri?>(null) }
    var screenRecordingName by remember { mutableStateOf<String?>(null) }

    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var generatedTicketId by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf(false) }
    var descError by remember { mutableStateOf(false) }

    val categories = listOf(
        "UI / Display",
        "Crash / Freeze",
        "Audio / Video",
        "Course / Content",
        "Payment / Sub",
        "Other"
    )

    val severities = listOf(
        "Low" to Color(0xFF10B981),
        "Medium" to Color(0xFFF59E0B),
        "High" to Color(0xFFF97316),
        "Critical" to Color(0xFFEF4444)
    )

    // Screenshot Picker (Multiple images)
    val screenshotLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            screenshots = (screenshots + uris).distinct()
        }
    }

    // Screen Recording Video Picker
    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            screenRecordingUri = uri
            screenRecordingName = uri.lastPathSegment?.substringAfterLast('/') ?: "screen_recording.mp4"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Report a Bug", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text("Help us improve Vastavik Learning", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(NeoBrutalistColors.Pink)
                            .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(50.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.BugReport, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("BUG LOG", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = 96.dp)
            ) {
                // Header Banner
                BrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(BrutalDefaults.RadiusLarge),
                    backgroundColor = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEF4444))
                                    .border(BorderStroke(1.5.dp, Color.White), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.BugReport, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Found an Issue?", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                Text(
                                    "Attach screenshots and screen recordings below so our engineering team can reproduce and resolve it instantly.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Issue Title
                Text("Issue Summary *", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = issueTitle,
                    onValueChange = {
                        issueTitle = it
                        if (it.isNotBlank()) titleError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Video player freezes when rotating screen") },
                    shape = RoundedCornerShape(12.dp),
                    isError = titleError,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = bb
                    )
                )
                if (titleError) {
                    Text("Summary is required", color = MaterialTheme.colorScheme.error, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                }

                Spacer(Modifier.height(14.dp))

                // Category Selection
                Text("Category", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .border(BorderStroke(1.5.dp, if (isSelected) MaterialTheme.colorScheme.primary else bb), RoundedCornerShape(50.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                cat,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Severity Level Selection
                Text("Severity Level", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    severities.forEach { (sev, color) ->
                        val isSelected = selectedSeverity == sev
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(BorderStroke(if (isSelected) 2.dp else 1.2.dp, if (isSelected) color else bb), RoundedCornerShape(10.dp))
                                .clickable { selectedSeverity = sev }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                sev,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Detailed Description
                Text("Detailed Description *", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = issueDescription,
                    onValueChange = {
                        issueDescription = it
                        if (it.isNotBlank()) descError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    placeholder = { Text("What happened? What did you expect to happen?") },
                    shape = RoundedCornerShape(12.dp),
                    isError = descError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = bb
                    )
                )
                if (descError) {
                    Text("Description is required", color = MaterialTheme.colorScheme.error, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                }

                Spacer(Modifier.height(14.dp))

                // Steps to Reproduce
                Text("Steps to Reproduce (Optional)", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = stepsToReproduce,
                    onValueChange = { stepsToReproduce = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp),
                    placeholder = { Text("1. Open Chapter 2\n2. Click fullscreen\n3. Device locks") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = bb
                    )
                )

                Spacer(Modifier.height(18.dp))

                // Media Attachments Section (Screenshots & Screen Recording)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.PermMedia, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Media Attachments", fontWeight = FontWeight.Black, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFF2563EB).copy(alpha = 0.12f))
                            .border(BorderStroke(1.dp, Color(0xFF2563EB)), RoundedCornerShape(50.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("HIGHLY RECOMMENDED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Screenshots Attachment Card
                BrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Screenshots", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                            if (screenshots.isNotEmpty()) {
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(Color(0xFF10B981))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text("${screenshots.size} added", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = { screenshotLauncher.launch("image/*") }) {
                                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add Images", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (screenshots.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(10.dp))
                                    .clickable { screenshotLauncher.launch("image/*") }
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(28.dp))
                                    Spacer(Modifier.height(6.dp))
                                    Text("Tap to select screenshots from gallery", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Supports PNG, JPG, WEBP", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                items(screenshots) { uri ->
                                    Box(
                                        modifier = Modifier
                                            .size(86.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(10.dp))
                                    ) {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "Screenshot preview",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        // Delete badge button
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.75f))
                                                .clickable { screenshots = screenshots.filter { it != uri } },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                                item {
                                    Box(
                                        modifier = Modifier
                                            .size(86.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(10.dp))
                                            .clickable { screenshotLauncher.launch("image/*") },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("Add More", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Screen Recording Attachment Card
                BrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Videocam, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Screen Recording", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(Modifier.weight(1f))
                            if (screenRecordingUri != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(Color(0xFF7C3AED))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("VIDEO READY", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        if (screenRecordingUri == null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(10.dp))
                                    .clickable { videoLauncher.launch("video/*") }
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.VideoLibrary, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(28.dp))
                                    Spacer(Modifier.height(6.dp))
                                    Text("Attach screen recording video", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Select MP4, MKV, or WebM recording from storage", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF7C3AED).copy(alpha = 0.1f))
                                    .border(BorderStroke(1.5.dp, Color(0xFF7C3AED)), RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF7C3AED)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        screenRecordingName ?: "screen_recording.mp4",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text("Screen video attached", fontSize = 10.sp, color = Color(0xFF7C3AED), fontWeight = FontWeight.SemiBold)
                                }
                                IconButton(onClick = {
                                    screenRecordingUri = null
                                    screenRecordingName = null
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove recording", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Auto-collected System & Device Diagnostics
                BrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Device Diagnostics", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(Modifier.weight(1f))
                            Text("Auto-Included", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "• App: Vastavik Learning v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})\n" +
                            "• Device: ${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}\n" +
                            "• OS: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Sticky Bottom Submit Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                BrutalBoxCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(BrutalDefaults.RadiusPill),
                    backgroundColor = NeoBrutalistColors.Yellow,
                    onClick = {
                        if (issueTitle.isBlank()) {
                            titleError = true
                        }
                        if (issueDescription.isBlank()) {
                            descError = true
                        }
                        if (issueTitle.isNotBlank() && issueDescription.isNotBlank() && !isSubmitting) {
                            isSubmitting = true
                            coroutineScope.launch {
                                delay(1200) // Simulate packaging report and media upload
                                generatedTicketId = "VBUG-${Random.nextInt(10000, 99999)}"
                                isSubmitting = false
                                showSuccessDialog = true
                            }
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Submitting Bug Report...", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color.Black)
                        } else {
                            Icon(Icons.Filled.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Submit Bug Report", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color.Black)
                        }
                    }
                }
            }

            // Success Confirmation Dialog
            if (showSuccessDialog) {
                Dialog(onDismissRequest = { /* require explicit action */ }) {
                    BrutalCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                                    .border(BorderStroke(2.dp, bb), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                            Spacer(Modifier.height(14.dp))
                            Text("Bug Report Submitted!", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Ticket ID: $generatedTicketId",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color(0xFF2563EB)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Thank you for helping us improve Vastavik Learning! Our engineering team has logged your report along with ${screenshots.size} screenshot(s)${if (screenRecordingUri != null) " and 1 screen recording" else ""}.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 17.sp
                            )
                            Spacer(Modifier.height(18.dp))
                            BrutalBoxCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(BrutalDefaults.RadiusPill),
                                backgroundColor = Color.Black,
                                onClick = {
                                    showSuccessDialog = false
                                    onBack()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("Done", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
