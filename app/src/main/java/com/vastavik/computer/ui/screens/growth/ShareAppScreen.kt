package com.vastavik.computer.ui.screens.growth

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.data.api.model.ShareCreateResponse
import com.vastavik.computer.data.api.model.ShareStatusResponse
import com.vastavik.computer.data.repository.VastavikApiRepository
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareAppScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val repo: VastavikApiRepository? = activity?.let {
        EntryPointAccessors.fromActivity(it, GrowthRepoEntryPoint::class.java).repo()
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var status by remember { mutableStateOf<ShareStatusResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isCreating by remember { mutableStateOf(false) }
    var lastCreated by remember { mutableStateOf<ShareCreateResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val r = repo?.getShareStatus()?.getOrNull()
            status = r
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Share App", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator()
                return@Column
            }

            val s = status
            if (s == null) {
                Text(error ?: "Could not load share status.", color = MaterialTheme.colorScheme.error)
                return@Column
            }

            if (!s.eligible) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFFB45309))
                            Spacer(Modifier.width(8.dp))
                            Text("Locked", fontWeight = FontWeight.ExtraBold, color = Color(0xFF92400E))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Share-to-earn unlocks after your first subscription payment.",
                            color = Color(0xFF78350F),
                            fontSize = 13.sp
                        )
                    }
                }
                return@Column
            }

            Text("Earn Rs.10 / friend", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF16A34A))
            Text("Up to ${s.cap} friends", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Progress", fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (s.rewardedCount.toFloat() / s.cap).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50))
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("${s.rewardedCount} / ${s.cap} shares rewarded", fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    isCreating = true
                    scope.launch {
                        try {
                            val created = repo?.generateShareToken()?.getOrNull()
                            lastCreated = created
                            if (created?.shareUrl != null) {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, "I'm learning with Vastavik Pro. Join me: ${created.shareUrl}")
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Share with friends"))
                                status = repo?.getShareStatus()?.getOrNull()
                            } else {
                                snackbarHostState.showSnackbar(created?.message ?: "Already at lifetime cap")
                            }
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Error: ${e.message}")
                        } finally {
                            isCreating = false
                        }
                    }
                },
                enabled = !isCreating,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (isCreating) "..." else "Share with friends", fontWeight = FontWeight.ExtraBold)
            }

            lastCreated?.shareUrl?.let { url ->
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        cm.setPrimaryClip(android.content.ClipData.newPlainText("Vastavik share link", url))
                        scope.launch { snackbarHostState.showSnackbar("Link copied") }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Copy latest link") }
            }

            Spacer(Modifier.height(20.dp))

            Text("Your share links", fontWeight = FontWeight.ExtraBold, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            if (s.shares.isEmpty()) {
                Text("No share links yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            } else {
                s.shares.forEach { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(entry.shareUrl, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val statusColor = when (entry.status) {
                                    "rewarded" -> Color(0xFF16A34A)
                                    "converted" -> Color(0xFF2563EB)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                Text("Status: ${entry.status}", fontSize = 12.sp, color = statusColor, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.weight(1f))
                                Text("Clicks: ${entry.clicks}", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
