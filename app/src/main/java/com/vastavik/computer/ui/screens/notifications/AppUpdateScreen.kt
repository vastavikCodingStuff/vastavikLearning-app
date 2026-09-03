package com.vastavik.computer.ui.screens.notifications

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.vastavik.computer.BuildConfig
import com.vastavik.computer.data.model.AppUpdateInfo
import com.vastavik.computer.ui.theme.neoShape
import com.vastavik.computer.utils.AppUpdater
import com.vastavik.computer.utils.Constants
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateScreen(onNavigate: (String) -> Unit, onBack: () -> Unit = { onNavigate("home") }) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var updateInfo by remember { mutableStateOf(AppUpdateInfo()) }
    var loaded by remember { mutableStateOf(false) }

    val current = BuildConfig.VERSION_NAME

    // Download / install states
    var downloading by remember { mutableStateOf(false) }
    var downloadFailed by remember { mutableStateOf(false) }
    var showInstallDialog by remember { mutableStateOf(false) }
    var installFailed by remember { mutableStateOf(false) }

    val isUpdateAvailable = loaded && updateInfo.latestVersion.isNotEmpty() &&
        updateInfo.latestVersion != current

    // Launch the system package installer
    val installLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // After install attempt, the app may restart; nothing more to do here.
    }

    val onUpdateClick: () -> Unit = {
        if (isUpdateAvailable) {
            val url = updateInfo.apkUrl
            val version = updateInfo.latestVersion
            if (url.isBlank()) {
                downloadFailed = true
            } else if (AppUpdater.hasUsableApk(context, version) && !downloadFailed) {
                // Reuse an already-downloaded APK if it exists in cache
                showInstallDialog = true
            } else {
                downloading = true
                downloadFailed = false
                scope.launch {
                    val result = AppUpdater.downloadApk(context, updateInfo)
                    downloading = false
                    if (result != null) {
                        showInstallDialog = true
                    } else {
                        downloadFailed = true
                    }
                }
            }
        }
    }

    // Load update info from the server route the first time
    LaunchedEffect(Unit) {
        try {
            val doc = FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_ADMIN_SETTINGS)
                .document(Constants.ADMIN_SETTINGS_UPDATE_DOC)
                .get()
                .await()
            updateInfo = if (doc.exists()) AppUpdateInfo.fromSnapshot(doc) else AppUpdateInfo()
        } catch (_: Exception) {
            updateInfo = AppUpdateInfo()
        }
        loaded = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Update", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.SystemUpdate,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text("Vastavik Computers", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(
                "Current: v$current",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(24.dp))

            if (isUpdateAvailable) {
                Card(
                    shape = neoShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Update available: v${updateInfo.latestVersion}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("What's new:", fontWeight = FontWeight.W600, fontSize = 13.sp)
                        Text(
                            updateInfo.changelog.ifBlank { "- New improvements and bug fixes" },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        if (updateInfo.forceUpdate) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "This update is required to continue using the app.",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))

                if (downloading) {
                    Card(
                        shape = neoShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Downloading update…",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                } else {
                    Button(
                        onClick = onUpdateClick,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Update Now", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    if (downloadFailed) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Couldn't download the update. Please check your internet and tap Update Now again.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    if (updateInfo.forceUpdate) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onBack,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Later") }
                    } else {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) { Text("Later") }
                    }
                }
            } else {
                Spacer(Modifier.height(12.dp))
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text("You're up to date!", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
            }
        }
    }

    // Install Now / Cancel dialog after download
    if (showInstallDialog) {
        AlertDialog(
            onDismissRequest = { showInstallDialog = false },
            title = { Text("Install Update") },
            text = {
                if (installFailed) {
                    Text("The update couldn't be installed. Please grant install permission if asked.")
                } else {
                    Text("The new version is ready. Install it now?")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showInstallDialog = false
                        installFailed = false
                        val intent = AppUpdater.buildInstallIntent(context, updateInfo.latestVersion)
                        if (intent != null) {
                            installLauncher.launch(intent)
                        } else {
                            installFailed = true
                            showInstallDialog = true
                        }
                    }
                ) { Text("Install Now") }
            },
            dismissButton = {
                TextButton(onClick = { showInstallDialog = false }) { Text("Cancel") }
            }
        )
    }
}
