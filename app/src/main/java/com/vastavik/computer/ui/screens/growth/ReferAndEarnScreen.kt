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
import com.vastavik.computer.data.api.model.ReferralStatusResponse
import com.vastavik.computer.data.repository.VastavikApiRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ActivityComponent
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(ActivityComponent::class)
interface GrowthRepoEntryPoint {
    fun repo(): VastavikApiRepository
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferAndEarnScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val repo: VastavikApiRepository? = activity?.let {
        EntryPointAccessors.fromActivity(it, GrowthRepoEntryPoint::class.java).repo()
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var status by remember { mutableStateOf<ReferralStatusResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isGenerating by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val r = repo?.getReferralStatus()?.getOrNull()
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
                title = { Text("Refer & Earn", fontWeight = FontWeight.ExtraBold) },
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
                Text(error ?: "Could not load referral status.", color = MaterialTheme.colorScheme.error)
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
                            "Refer & Earn unlocks after your first subscription payment (or after an offline coupon activation).",
                            color = Color(0xFF78350F),
                            fontSize = 13.sp
                        )
                    }
                }
                return@Column
            }

            Text("Earn Rs.${s.rewardsTotal.toInt()}", fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color(0xFF2563EB))
            Text("for every friend who subscribes", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your referral code", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.height(6.dp))
                    val code = s.code ?: "-"
                    Text(code, fontSize = 36.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = {
                                val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                cm.setPrimaryClip(android.content.ClipData.newPlainText("Vastavik Referral", code))
                                scope.launch { snackbarHostState.showSnackbar("Code copied") }
                            }
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Copy")
                        }
                        Spacer(Modifier.width(8.dp))
                        if (s.code == null) {
                            Button(
                                onClick = {
                                    isGenerating = true
                                    scope.launch {
                                        try {
                                            repo?.generateReferralCode()
                                            val fresh = repo?.getReferralStatus()?.getOrNull()
                                            status = fresh
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("Error: ${e.message}")
                                        } finally {
                                            isGenerating = false
                                        }
                                    }
                                },
                                enabled = !isGenerating
                            ) {
                                Text(if (isGenerating) "..." else "Generate Code")
                            }
                        } else {
                            val shareText = "Join me on Vastavik Pro! Use my referral code $code to start. https://vastavikcomputers.firebaseapp.com/r/$code"
                            OutlinedButton(onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Share referral code"))
                            }) {
                                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Share")
                            }
                        }
                    }
                }
            }

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
                    Text("${s.rewardedCount} / ${s.cap} rewards claimed", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Credit balance: Rs.${"%.2f".format(s.creditBalanceInr)}", fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            Text("History", fontWeight = FontWeight.ExtraBold, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            if (s.history.isEmpty()) {
                Text("No referrals yet. Share your code to get started.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            } else {
                s.history.forEach { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (entry.status == "rewarded") Icons.Filled.CheckCircle else Icons.Filled.HourglassEmpty,
                                contentDescription = null,
                                tint = if (entry.status == "rewarded") Color(0xFF16A34A) else Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(entry.refereeEmail ?: entry.refereeUid ?: "Referee", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(
                                    if (entry.status == "rewarded") "+Rs.${entry.rewardAmount.toInt()} rewarded" else "Pending payment verification",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
