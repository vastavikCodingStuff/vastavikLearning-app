package com.vastavik.computer.ui.screens.notifications

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.vastavik.computer.BuildConfig
import com.vastavik.computer.data.model.AppUpdateInfo
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor
import com.vastavik.computer.utils.AppUpdater
import com.vastavik.computer.utils.Constants
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateScreen(onNavigate: (String) -> Unit, onBack: () -> Unit = { onNavigate("home") }) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()

    var updateInfo by remember { mutableStateOf(AppUpdateInfo()) }
    var isChecking by remember { mutableStateOf(true) }

    // History and All Releases State
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Latest Update, 1: All Updates History
    var allReleases by remember { mutableStateOf<List<AppUpdateInfo>>(emptyList()) }
    var isLoadingReleases by remember { mutableStateOf(false) }

    val current = BuildConfig.VERSION_NAME

    // Download & Install state
    var downloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadedBytes by remember { mutableLongStateOf(0L) }
    var totalBytes by remember { mutableLongStateOf(0L) }
    var downloadFailed by remember { mutableStateOf(false) }
    var showInstallDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val isUpdateAvailable = updateInfo.isUpdateAvailable ||
        (updateInfo.latestVersion.isNotBlank() && AppUpdater.isNewerVersion(updateInfo.latestVersion, current))

    // Activity launcher for unknown app install permission settings
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (AppUpdater.canRequestPackageInstalls(context)) {
            val intent = AppUpdater.buildInstallIntent(context, updateInfo.latestVersion)
            if (intent != null) context.startActivity(intent)
        }
    }

    // Activity launcher for package installer
    val installLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    fun loadAllReleases() {
        isLoadingReleases = true
        scope.launch {
            val list = AppUpdater.fetchAllGitHubReleases(current)
            allReleases = list
            isLoadingReleases = false
        }
    }

    fun checkUpdates() {
        isChecking = true
        scope.launch {
            // 1. Check GitHub Releases first
            val ghInfo = AppUpdater.checkGitHubRelease(current)
            if (ghInfo != null && ghInfo.latestVersion.isNotBlank()) {
                updateInfo = ghInfo
            } else {
                // 2. Fallback to Firestore
                try {
                    val doc = FirebaseFirestore.getInstance()
                        .collection(Constants.COLLECTION_ADMIN_SETTINGS)
                        .document(Constants.ADMIN_SETTINGS_UPDATE_DOC)
                        .get()
                        .await()
                    if (doc.exists()) {
                        val fsInfo = AppUpdateInfo.fromSnapshot(doc)
                        updateInfo = fsInfo.copy(isUpdateAvailable = AppUpdater.isNewerVersion(fsInfo.latestVersion, current))
                    }
                } catch (_: Exception) {
                    // Ignore
                }
            }
            isChecking = false
        }
    }

    LaunchedEffect(Unit) {
        checkUpdates()
        loadAllReleases()
    }

    val onUpdateClick: () -> Unit = {
        val version = updateInfo.latestVersion
        if (updateInfo.apkUrl.isBlank()) {
            downloadFailed = true
        } else if (AppUpdater.hasUsableApk(context, version)) {
            // Check install permission first
            if (!AppUpdater.canRequestPackageInstalls(context)) {
                showPermissionDialog = true
            } else {
                showInstallDialog = true
            }
        } else {
            downloading = true
            downloadFailed = false
            downloadProgress = 0f
            scope.launch {
                val file = AppUpdater.downloadApkWithProgress(context, updateInfo) { bytesRead, total, progress ->
                    downloadedBytes = bytesRead
                    totalBytes = total
                    downloadProgress = progress
                }
                downloading = false
                if (file != null) {
                    if (!AppUpdater.canRequestPackageInstalls(context)) {
                        showPermissionDialog = true
                    } else {
                        showInstallDialog = true
                    }
                } else {
                    downloadFailed = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectedTab == 1) "Update History" else "App Update",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedTab == 1) {
                            selectedTab = 0
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Option on the left of the reload button: History of all updates
                    IconButton(onClick = { selectedTab = if (selectedTab == 0) 1 else 0 }) {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.SystemUpdate else Icons.Filled.History,
                            contentDescription = if (selectedTab == 1) "Latest Update" else "All Updates History",
                            tint = if (selectedTab == 1) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = {
                        if (!isChecking && !downloading) {
                            checkUpdates()
                            loadAllReleases()
                        }
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Check for Updates")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // App Icon / Update Header
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB).copy(alpha = 0.12f))
                    .border(BorderStroke(2.dp, Color(0xFF2563EB)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.SystemUpdate,
                    contentDescription = null,
                    modifier = Modifier.size(38.dp),
                    tint = Color(0xFF2563EB)
                )
            }

            Spacer(Modifier.height(14.dp))
            Text("Vastavik Computers", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(BorderStroke(1.dp, bb.copy(alpha = 0.3f)), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Installed: v$current",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (updateInfo.latestVersion.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isUpdateAvailable) Color(0xFF2563EB).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f))
                            .border(
                                BorderStroke(1.dp, if (isUpdateAvailable) Color(0xFF2563EB) else Color(0xFF10B981)),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "GitHub: v${updateInfo.latestVersion}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUpdateAvailable) Color(0xFF2563EB) else Color(0xFF10B981)
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))

            // Two-tab Neo-brutalist Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Latest Update Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (selectedTab == 0) Color(0xFF2563EB) else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.SystemUpdate,
                            contentDescription = null,
                            tint = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Latest Update",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // All Updates History Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (selectedTab == 1) Color(0xFF2563EB) else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.History,
                            contentDescription = null,
                            tint = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (allReleases.isNotEmpty()) "All Updates (${allReleases.size})" else "All Updates",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            if (selectedTab == 1) {
                // ALL UPDATES / CHANGELOG HISTORY VIEW
                if (isLoadingReleases && allReleases.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp, color = Color(0xFF2563EB))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Loading updates history…",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        allReleases.forEachIndexed { index, rel ->
                            val isCurrentVersion = rel.latestVersion == current
                            val isLatest = index == 0

                            Box(modifier = Modifier.fillMaxWidth().padding(end = 4.dp, bottom = 4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .offset(x = 4.dp, y = 4.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(bs)
                                )
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(2.dp, bb)
                                ) {
                                    Column(Modifier.padding(18.dp)) {
                                        // Header Row: Version Badge + Date / Size
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (isCurrentVersion) Color(0xFF10B981)
                                                        else if (isLatest) Color(0xFF2563EB)
                                                        else MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                    .border(
                                                        BorderStroke(
                                                            1.dp,
                                                            if (isCurrentVersion) Color(0xFF10B981)
                                                            else if (isLatest) Color(0xFF2563EB)
                                                            else bb.copy(alpha = 0.4f)
                                                        ),
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = buildString {
                                                        append("v${rel.latestVersion}")
                                                        if (isCurrentVersion) append(" • CURRENT")
                                                        else if (isLatest) append(" • LATEST")
                                                    },
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isCurrentVersion || isLatest) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Spacer(Modifier.weight(1f))

                                            if (rel.apkSize > 0) {
                                                val sizeMb = rel.apkSize.toDouble() / (1024 * 1024)
                                                Text(
                                                    "%.1f MB".format(sizeMb),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            text = rel.releaseTitle.ifBlank { "Version v${rel.latestVersion}" },
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            "What's New in this Release:",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        com.vastavik.computer.ui.components.MarkdownContent(
                                            content = rel.changelog.ifBlank { "- Bug fixes, performance improvements, and UI enhancements." },
                                            baseColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            accentColor = Color(0xFF2563EB)
                                        )

                                        if (rel.apkUrl.isNotBlank()) {
                                            Spacer(Modifier.height(14.dp))
                                            val isInstalled = rel.latestVersion == current
                                            val hasApk = AppUpdater.hasUsableApk(context, rel.latestVersion)

                                            Box(modifier = Modifier.fillMaxWidth().padding(end = 2.dp, bottom = 2.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .matchParentSize()
                                                        .offset(x = 2.dp, y = 2.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(bs)
                                                )
                                                Button(
                                                    onClick = {
                                                        updateInfo = rel
                                                        selectedTab = 0
                                                        onUpdateClick()
                                                    },
                                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isInstalled) Color(0xFF10B981) else Color(0xFF2563EB)
                                                    ),
                                                    border = BorderStroke(1.5.dp, bb)
                                                ) {
                                                    Icon(
                                                        if (isInstalled) Icons.Filled.CheckCircle else Icons.Filled.Download,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        if (isInstalled) "Currently Installed (v$current)"
                                                        else if (hasApk) "Install v${rel.latestVersion} APK"
                                                        else "Download v${rel.latestVersion} APK",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = Color.White
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
            } else {
                if (isChecking) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp, color = Color(0xFF2563EB))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Scanning GitHub Assets for updates…",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (isUpdateAvailable) {
                // Update Card
                Box(modifier = Modifier.fillMaxWidth().padding(end = 4.dp, bottom = 4.dp)) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 4.dp, y = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(bs)
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(2.dp, bb)
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF2563EB))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        "UPDATE AVAILABLE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                if (updateInfo.apkSize > 0) {
                                    val sizeMb = updateInfo.apkSize.toDouble() / (1024 * 1024)
                                    Text(
                                        "%.1f MB".format(sizeMb),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = updateInfo.releaseTitle.ifBlank { "Version v${updateInfo.latestVersion}" },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(Modifier.height(12.dp))
                            Text(
                                "What's New in this Release:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(6.dp))
                            com.vastavik.computer.ui.components.MarkdownContent(
                                content = updateInfo.changelog.ifBlank { "- Improvements, performance optimizations, and bug fixes." },
                                baseColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                accentColor = Color(0xFF2563EB)
                            )

                            if (downloading) {
                                Spacer(Modifier.height(16.dp))
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Downloading APK… ${(downloadProgress * 100).toInt()}%",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2563EB)
                                        )
                                        if (totalBytes > 0) {
                                            val currentMb = downloadedBytes.toDouble() / (1024 * 1024)
                                            val totalMb = totalBytes.toDouble() / (1024 * 1024)
                                            Text(
                                                "%.1f / %.1f MB".format(currentMb, totalMb),
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { downloadProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = Color(0xFF2563EB),
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                if (!downloading) {
                    // Update Now Button
                    Box(modifier = Modifier.fillMaxWidth().padding(end = 4.dp, bottom = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 3.dp, y = 3.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bs)
                        )
                        Button(
                            onClick = onUpdateClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            border = BorderStroke(1.5.dp, bb)
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (AppUpdater.hasUsableApk(context, updateInfo.latestVersion)) "Install Update Now" else "Download & Install Update",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }

                    if (downloadFailed) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Couldn't download the update asset. Please check internet connection and retry.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, bb)
                    ) {
                        Text("Later", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            } else {
                // Up to date state
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.12f))
                        .border(BorderStroke(2.dp, Color(0xFF10B981)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))
                Text(
                    "You're using the latest version!",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "No newer releases found on GitHub. Your app is completely up to date.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(28.dp))

                Box(modifier = Modifier.fillMaxWidth().padding(end = 4.dp, bottom = 4.dp)) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 3.dp, y = 3.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bs)
                    )
                    OutlinedButton(
                        onClick = { checkUpdates() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, bb),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Check for Updates Again", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, bb)
                ) {
                    Text("Back to Home", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    }

    // Permission Dialog for REQUEST_INSTALL_PACKAGES
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Install Permission Required", fontWeight = FontWeight.Bold) },
            text = {
                Text("Android requires permission to install app updates from Vastavik Computers. Please enable 'Allow from this source' in Settings.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        val intent = AppUpdater.createManageUnknownAppSourcesIntent(context)
                        if (intent != null) {
                            permissionLauncher.launch(intent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Open Settings", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Install Now confirmation dialog
    if (showInstallDialog) {
        AlertDialog(
            onDismissRequest = { showInstallDialog = false },
            title = { Text("Install Update", fontWeight = FontWeight.Bold) },
            text = {
                Text("The update for version v${updateInfo.latestVersion} has been downloaded. Install it now?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInstallDialog = false
                        val intent = AppUpdater.buildInstallIntent(context, updateInfo.latestVersion)
                        if (intent != null) {
                            installLauncher.launch(intent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Install Now", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInstallDialog = false }) { Text("Later") }
            }
        )
    }
}
