package com.vastavik.computer.ui.screens.doubts

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.ui.theme.BrutalBoxCard
import com.vastavik.computer.ui.theme.BrutalCard
import com.vastavik.computer.ui.theme.BrutalDefaults
import com.vastavik.computer.ui.theme.NeoBrutalistColors
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoubtSolvingScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit = {}
) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var doubtQuery by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("Java") }
    var currentStep by remember { mutableIntStateOf(1) }
    var isAnalyzing by remember { mutableStateOf(false) }

    // Step 2 Veo3 Video State
    var isVideoPlaying by remember { mutableStateOf(false) }
    var videoProgress by remember { mutableFloatStateOf(0.42f) }

    // Step 3 Paid Human Expert State
    var isExpertPaid by remember { mutableStateOf(false) }

    fun launchPhonePeForDoubt() {
        val upiUri = Uri.parse("upi://pay?pa=vastavik@ybl&pn=Vastavik+Computers&am=29&cu=INR&tn=1-on-1+Doubt+Expert+Session")
        val phonePeIntent = Intent(Intent.ACTION_VIEW, upiUri).apply {
            setPackage("com.phonepe.app")
        }
        try {
            context.startActivity(phonePeIntent)
            isExpertPaid = true
        } catch (_: Exception) {
            val chooser = Intent.createChooser(Intent(Intent.ACTION_VIEW, upiUri), "Pay ₹29 with PhonePe or UPI")
            try {
                context.startActivity(chooser)
                isExpertPaid = true
            } catch (_: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Payment simulated for doubt resolution!")
                    isExpertPaid = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("3-Step Doubt Engine", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text("AI • Veo 3 Video • Human Expert", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            // 3-Step Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple(1, "1. AI Instant", Color(0xFF2563EB)),
                    Triple(2, "2. Veo3 Video", Color(0xFF7C3AED)),
                    Triple(3, "3. Paisa Do (Expert)", Color(0xFF059669))
                ).forEach { (stepNum, title, activeColor) ->
                    val isActive = currentStep == stepNum
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isActive) activeColor else MaterialTheme.colorScheme.surfaceVariant)
                            .border(BorderStroke(1.5.dp, if (isActive) bb else bb.copy(alpha = 0.4f)), RoundedCornerShape(10.dp))
                            .clickable { currentStep = stepNum }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Doubt Query Input Card
            BrutalCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(BrutalDefaults.Radius),
                backgroundColor = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ask Your Doubt", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = doubtQuery,
                        onValueChange = { doubtQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp),
                        placeholder = { Text("Paste your error, code, or concept question (e.g. NullPointerException in Java)...", fontSize = 13.sp) },
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Java", "Python", "SQL").forEach { lang ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedLanguage == lang) Color.Black else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { selectedLanguage = lang }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        lang,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedLanguage == lang) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        BrutalBoxCard(
                            modifier = Modifier.height(40.dp),
                            shape = RoundedCornerShape(BrutalDefaults.RadiusPill),
                            backgroundColor = Color(0xFF2563EB),
                            onClick = {
                                if (doubtQuery.isBlank()) {
                                    doubtQuery = "How to reverse an array in Java without using extra space?"
                                }
                                isAnalyzing = true
                                scope.launch {
                                    delay(1000)
                                    isAnalyzing = false
                                }
                            }
                        ) {
                            Row(Modifier.fillMaxHeight().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (isAnalyzing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Solving…", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                } else {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Solve", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // STEP 1: AI Instant Solution
            if (currentStep == 1) {
                Text("Step 1: AI Instant Solution (Free)", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))

                BrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(BrutalDefaults.Radius),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2563EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Psychology, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Text("AI Logic Breakdown", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }

                        Spacer(Modifier.height(10.dp))
                        Text(
                            "To reverse an array in place, use the Two-Pointer technique. Swap the elements at the start and end pointers, then move start forward and end backward until they cross.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E293B))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = """
// Two-pointer array reverse
int left = 0, right = arr.length - 1;
while (left < right) {
    int temp = arr[left];
    arr[left] = arr[right];
    arr[right] = temp;
    left++;
    right--;
}
                                """.trimIndent(),
                                color = Color(0xFF38BDF8),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(Modifier.height(14.dp))
                        BrutalBoxCard(
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(BrutalDefaults.RadiusPill),
                            backgroundColor = Color(0xFF7C3AED),
                            onClick = { currentStep = 2 }
                        ) {
                            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Still have doubts? Watch Veo 3 AI Video →", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // STEP 2: AI Video using Google Veo 3
            if (currentStep == 2) {
                Text("Step 2: AI Video Simulation (Veo 3)", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))

                BrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(BrutalDefaults.Radius),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF7C3AED)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Veo 3 Concept Walkthrough", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                Text("AI-generated whiteboard visualizer", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Video Player Simulation Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.Black)
                                .border(BorderStroke(2.dp, bb), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(if (isVideoPlaying) Color(0xFFEF4444) else Color(0xFF7C3AED))
                                        .clickable { isVideoPlaying = !isVideoPlaying },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (isVideoPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    if (isVideoPlaying) "Playing Veo 3 Simulation (1080p)..." else "Tap to Play Veo 3 Video",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Bottom progress bar
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { if (isVideoPlaying) 0.72f else videoProgress },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF7C3AED),
                                    trackColor = Color.White.copy(alpha = 0.3f)
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Veo 3 visually animates the start and end pointers moving toward the center and swapping elements on a digital whiteboard.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(14.dp))
                        BrutalBoxCard(
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(BrutalDefaults.RadiusPill),
                            backgroundColor = Color(0xFF059669),
                            onClick = { currentStep = 3 }
                        ) {
                            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Text("Paisa Do: Talk to Human Expert (₹29) →", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // STEP 3: "Paisa do toh doubts solve hoga" — Paid 1-on-1 Human Expert
            if (currentStep == 3) {
                Text("Step 3: 1-on-1 Human Expert (Paid)", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))

                BrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(BrutalDefaults.Radius),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF059669)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("1-on-1 Teacher Consultation", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                Text("Paisa do toh doubts solve hoga!", fontSize = 11.sp, color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeoBrutalistColors.Yellow)
                                    .border(BorderStroke(1.dp, bb), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("₹29 only", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.Black)
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Get connected directly with a senior Computer Science instructor to solve your doubt via live screen share and voice chat.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(Modifier.height(12.dp))
                        listOf("100% verified CS teachers", "Instant screen share & voice", "Audio call + Live Code Fix", "Money-back guarantee").forEach { item ->
                            Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(item, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        if (!isExpertPaid) {
                            BrutalBoxCard(
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(BrutalDefaults.RadiusPill),
                                backgroundColor = Color(0xFF5F259F), // PhonePe Purple
                                onClick = { launchPhonePeForDoubt() }
                            ) {
                                Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Text("पे", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Pay ₹29 with PhonePe — Unlock Expert", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 13.sp)
                                }
                            }
                        } else {
                            // Unlocked state!
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.1f))
                                    .border(BorderStroke(1.5.dp, Color(0xFF10B981)), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Verified, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Teacher Session Unlocked!", fontWeight = FontWeight.ExtraBold, color = Color(0xFF059669))
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text("Teacher Arvind is waiting in the live meeting lobby.", fontSize = 12.sp)
                                    Spacer(Modifier.height(10.dp))
                                    Button(
                                        onClick = { onNavigate("meeting_lobby/doubt-session-101") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Join Live Expert Room", fontWeight = FontWeight.Bold)
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
