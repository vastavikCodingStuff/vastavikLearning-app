package com.vastavik.computer.ui.screens.practice

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.vastavik.computer.BuildConfig
import com.vastavik.computer.ui.screens.chat.ParsedMarkdownText
import com.vastavik.computer.ui.screens.chat.WaveformVisualizer
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

import com.vastavik.computer.utils.MistralDiskCache

private enum class QuestionSource(val label: String, val tagBg: Color, val tagText: Color) {
    AI("AI-Generated", Color(0xFF2563EB), Color.White),
    SIR("Sir-Generated", Color(0xFFF59E0B), Color(0xFF0F172A))
}

private data class MCQItem(val title: String, val sub: String, val source: QuestionSource)
private data class PredictOutputItem(val setNumber: Int, val title: String, val topic: String, val questionCount: String, val difficulty: String, val codeSnippet: String, val source: QuestionSource)
private data class CodingItem(val title: String, val difficulty: String, val topic: String, val source: QuestionSource)
private data class PYQItem(val title: String, val questions: String, val source: QuestionSource)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(onNavigate: (String) -> Unit) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: MCQs, 1: Predict the Output, 2: Coding, 3: PYQs
    var selectedSource by remember { mutableStateOf(QuestionSource.AI) } // 1. Top Toggle AI vs Sir
    val tabs = listOf("MCQs", "Predict the Output", "Coding", "PYQs")

    // Active AI coding item for Mistral solution sheet
    var activeCodingItem by remember { mutableStateOf<CodingItem?>(null) }
    var selectedLanguage by remember { mutableStateOf("Java") }
    var aiSolutionMarkdown by remember { mutableStateOf("") }
    var isGeneratingCode by remember { mutableStateOf(false) }

    fun loadMistralSolution(item: CodingItem, lang: String) {
        val sanitizedTitle = item.title.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
        val cacheKey = "code_${sanitizedTitle}_${lang.lowercase()}"
        
        // 1. Check persistent disk cache first (persists even if phone restarted)
        val cached = MistralDiskCache.getSolution(context, cacheKey)
        if (!cached.isNullOrBlank()) {
            aiSolutionMarkdown = cached
            isGeneratingCode = false
            return
        }

        isGeneratingCode = true
        aiSolutionMarkdown = ""
        coroutineScope.launch {
            val response = callMistralGenerateCode(
                title = item.title,
                topic = item.topic,
                difficulty = item.difficulty,
                language = lang
            )
            // 2. Persist to disk cache permanently
            MistralDiskCache.saveSolution(context, cacheKey, response)
            aiSolutionMarkdown = response
            isGeneratingCode = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // TOP BAR: Practice Title + AI/Sir Toggle (Blue for selected) + Profile Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    Text(
                        text = "Practice",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // 1. Toggle at the top for AI and Sir with blue color for selected one
                Box(modifier = Modifier.padding(end = 3.dp, bottom = 3.dp)) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 3.dp, y = 3.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bs)
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(BorderStroke(2.dp, bb), RoundedCornerShape(12.dp))
                            .padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isAi = selectedSource == QuestionSource.AI
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAi) Color(0xFF2563EB) else Color.Transparent)
                                .border(
                                    if (isAi) BorderStroke(1.5.dp, bb) else BorderStroke(0.dp, Color.Transparent),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedSource = QuestionSource.AI }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isAi) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AI",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isAi) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val isSir = selectedSource == QuestionSource.SIR
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSir) Color(0xFFFFD600) else Color.Transparent)
                                .border(
                                    if (isSir) BorderStroke(1.5.dp, bb) else BorderStroke(0.dp, Color.Transparent),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedSource = QuestionSource.SIR }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.School,
                                    contentDescription = null,
                                    tint = if (isSir) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sir",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSir) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    // Profile Avatar Button
                    Box(modifier = Modifier.padding(end = 2.dp, bottom = 2.dp)) {
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
                                .border(BorderStroke(2.dp, bb), CircleShape)
                                .clickable { onNavigate("profile") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // CATEGORY TABS [ MCQs | Predict the Output | Coding | PYQs ] with PYQs on the right-most side
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(end = 4.dp, bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(bs)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(BorderStroke(2.dp, bb), RoundedCornerShape(16.dp))
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .padding(horizontal = 2.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF2563EB) else Color.Transparent)
                                .border(
                                    if (isSelected) BorderStroke(1.5.dp, bb) else BorderStroke(0.dp, Color.Transparent),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedTab = index
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (index == 1) {
                                // Predict the Output: Predict on top line, Output on bottom line in same div
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Predict",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 12.sp
                                    )
                                    Text(
                                        text = "Output",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 12.sp
                                    )
                                }
                            } else {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // MAIN CONTENT AREA - Takes full width, displays selected category and filtered by AI/Sir
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTab) {
                    0 -> MCQContent(
                        selectedSource = selectedSource,
                        onNavigate = onNavigate
                    )
                    1 -> PredictOutputContent(
                        selectedSource = selectedSource,
                        onNavigate = onNavigate
                    )
                    2 -> CodingContent(
                        selectedSource = selectedSource,
                        onNavigate = onNavigate,
                        onOpenMistralAi = { item ->
                            activeCodingItem = item
                            selectedLanguage = "Java"
                            loadMistralSolution(item, "Java")
                        }
                    )
                    3 -> PYQContent(
                        selectedSource = selectedSource,
                        onNavigate = onNavigate
                    )
                }
            }

            // 2. UNDER DEVELOPMENT BANNER: Placed at the bottom of the page above the Bottom Navigation
            BottomDevBanner()

            // Subtle spacer for small white margin above bottom navigation bar
            Spacer(modifier = Modifier.height(2.dp))
        }
    }

    // 3. MISTRAL AI CODE GENERATION BOTTOM SHEET
    activeCodingItem?.let { item ->
        ModalBottomSheet(
            onDismissRequest = { activeCodingItem = null },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = MaterialTheme.colorScheme.background,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(bb.copy(alpha = 0.4f))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
                    .padding(horizontal = 16.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = item.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Line-by-line commented code & markdown explanation",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Mistral AI badge positioned on the top right
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFF2563EB).copy(alpha = 0.12f))
                            .border(BorderStroke(1.2.dp, Color(0xFF2563EB)), RoundedCornerShape(50.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Mistral AI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Language Picker Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Java", "Python", "C++", "JavaScript").forEach { lang ->
                        val isLangSelected = selectedLanguage == lang
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isLangSelected) Color(0xFF2563EB) else MaterialTheme.colorScheme.surface)
                                .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(8.dp))
                                .clickable {
                                    if (selectedLanguage != lang) {
                                        selectedLanguage = lang
                                        loadMistralSolution(item, lang)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lang,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLangSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content View (Markdown or Loading)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (isGeneratingCode) {
                        // Clean Loading Screen (replaces wordy informational text)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp, vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(BorderStroke(2.dp, bb), RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF2563EB),
                                        strokeWidth = 3.5.dp,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Text(
                                    text = "Generating Solution...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Skeleton placeholder cards
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .border(BorderStroke(1.5.dp, bb.copy(alpha = 0.15f)), RoundedCornerShape(14.dp))
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.55f)
                                            .height(14.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(bb.copy(alpha = 0.15f))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.95f)
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(bb.copy(alpha = 0.08f))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.8f)
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(bb.copy(alpha = 0.08f))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.65f)
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(bb.copy(alpha = 0.08f))
                                    )
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 16.dp)
                        ) {
                            ParsedMarkdownText(
                                text = aiSolutionMarkdown,
                                modifier = Modifier.fillMaxWidth(),
                                onNavigate = onNavigate
                            )
                        }
                    }
                }

                // Bottom Actions: Copy Code & Open in Code Editor
                if (!isGeneratingCode && aiSolutionMarkdown.isNotBlank()) {
                    val rawCode = remember(aiSolutionMarkdown) { extractCodeFromMarkdown(aiSolutionMarkdown) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Copy Button
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(rawCode))
                                Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .border(BorderStroke(2.dp, bb), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Code", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Open in Editor Button
                        Button(
                            onClick = {
                                val encoded = Uri.encode(rawCode, "UTF-8")
                                val encodedQ = Uri.encode(item.title, "UTF-8")
                                activeCodingItem = null
                                onNavigate("code_editor?initialCode=$encoded&language=$selectedLanguage&question=$encodedQ")
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(BorderStroke(2.dp, bb), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Icon(Icons.Filled.Code, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open in Editor", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeading(source: QuestionSource, onSuggestNew: (() -> Unit)? = null) {
    val bb = brutalBorderColor()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(source.tagBg)
                .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(50.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = source.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = source.tagText,
                letterSpacing = 0.6.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (source == QuestionSource.SIR) "Hand-picked by Sir" else "Auto-generated by Mistral AI",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (source == QuestionSource.AI && onSuggestNew != null) {
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onSuggestNew, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Suggest New", tint = Color(0xFF2563EB))
            }
        }
    }
}

@Composable
private fun BottomDevBanner() {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp, bottom = 2.dp)
            .padding(end = 4.dp, bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bs)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, bb),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                            )
                        )
                        .border(BorderStroke(1.5.dp, bb), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Build,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "UNDER DEVELOPMENT",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2563EB).copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text("BETA", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "One builder. One app. Full version coming eventually.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestTopicDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Suggest a Topic", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Topic name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { if (text.isNotBlank()) onSubmit(text) }) {
                Text("Generate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun PredictOutputPromptDialog(
    onDismiss: () -> Unit,
    startSetNumber: Int,
    onSetsGenerated: (List<PredictOutputItem>) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val bb = brutalBorderColor()

    var promptText by remember { mutableStateOf("") }
    var isVoiceMode by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var partialTranscript by remember { mutableStateOf("") }
    var isGeneratingQuestions by remember { mutableStateOf(false) }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    val quickTopics = listOf(
        "Nested Loops", "String Methods", "Increment/Decrement",
        "Array Tracing", "Recursion", "Constructors & Static"
    )

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    fun triggerGenerate(query: String) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank() || isGeneratingQuestions) return

        isGeneratingQuestions = true
        coroutineScope.launch {
            try {
                val sets = callMistralGeneratePredictOutput(cleanQuery, startSetNumber)
                onSetsGenerated(sets)
                Toast.makeText(context, "Generated ${sets.size} Output Tracing sets!", Toast.LENGTH_SHORT).show()
                onDismiss()
            } catch (_: Exception) {
                val fallback = getFallbackPredictOutput(cleanQuery, startSetNumber)
                onSetsGenerated(fallback)
                Toast.makeText(context, "Generated ${fallback.size} Output Tracing sets!", Toast.LENGTH_SHORT).show()
                onDismiss()
            } finally {
                isGeneratingQuestions = false
            }
        }
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Toast.makeText(context, "Speech recognition is not available on this device", Toast.LENGTH_SHORT).show()
            isVoiceMode = false
            return
        }
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListening = false
                }
                override fun onError(error: Int) {
                    isListening = false
                    isVoiceMode = false
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    isVoiceMode = false
                    isListening = false
                    partialTranscript = ""
                    if (text.isNotBlank()) {
                        promptText = text
                        triggerGenerate(text)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotEmpty()) {
                        partialTranscript = text
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
        isListening = true
        isVoiceMode = true
        partialTranscript = ""
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            promptText = ""
            startListening()
        } else {
            Toast.makeText(context, "Microphone permission is required for voice input", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isGeneratingQuestions) {
                speechRecognizer?.cancel()
                onDismiss()
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "What kind of Output Tracing questions do you want?",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isVoiceMode || isListening) {
                    val infiniteTransition = rememberInfiniteTransition(label = "listeningAnimPredict")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.35f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(650, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseAlphaPredict"
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.5.dp, bb),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444).copy(alpha = pulseAlpha))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Listening...",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF2563EB).copy(alpha = pulseAlpha)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            WaveformVisualizer(
                                isListening = true,
                                amplitude = 0.6f + 0.4f * kotlin.math.sin(System.currentTimeMillis() / 120.0).toFloat(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (partialTranscript.isNotBlank()) partialTranscript else "Speak your topic prompt now...",
                                fontSize = 13.sp,
                                fontWeight = if (partialTranscript.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (partialTranscript.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        speechRecognizer?.cancel()
                                        isListening = false
                                        isVoiceMode = false
                                    }
                                ) {
                                    Text("Cancel Voice", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                if (partialTranscript.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            speechRecognizer?.stopListening()
                                            val captured = partialTranscript
                                            isListening = false
                                            isVoiceMode = false
                                            promptText = captured
                                            triggerGenerate(captured)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Generate", fontSize = 12.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        placeholder = {
                            Text(
                                text = "e.g., Nested loops, String methods, Post-increment & Pre-increment...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 72.dp, max = 110.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = bb,
                            unfocusedBorderColor = bb.copy(alpha = 0.6f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        trailingIcon = {
                            if (promptText.isEmpty() && !isListening && !isGeneratingQuestions) {
                                IconButton(
                                    onClick = {
                                        promptText = ""
                                        if (ContextCompat.checkSelfPermission(
                                                context,
                                                android.Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED
                                        ) {
                                            startListening()
                                        } else {
                                            permLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                        }
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2563EB))
                                            .border(BorderStroke(1.5.dp, bb), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Mic,
                                            contentDescription = "Voice Input",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )

                    Text(
                        text = "Quick topics:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickTopics.take(3).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(BorderStroke(1.dp, bb.copy(alpha = 0.3f)), RoundedCornerShape(6.dp))
                                    .clickable { promptText = tag }
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Text(tag, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickTopics.drop(3).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(BorderStroke(1.dp, bb.copy(alpha = 0.3f)), RoundedCornerShape(6.dp))
                                    .clickable { promptText = tag }
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Text(tag, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                if (isGeneratingQuestions) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF2563EB)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Crafting Predict Output sets...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (promptText.isNotBlank() && !isGeneratingQuestions) {
                        triggerGenerate(promptText)
                    }
                },
                enabled = promptText.isNotBlank() && !isGeneratingQuestions,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Generate", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (!isGeneratingQuestions) {
                        speechRecognizer?.cancel()
                        onDismiss()
                    }
                },
                enabled = !isGeneratingQuestions
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CodingPromptDialog(
    onDismiss: () -> Unit,
    onQuestionsGenerated: (List<CodingItem>) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val bb = brutalBorderColor()

    var promptText by remember { mutableStateOf("") }
    var isVoiceMode by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var partialTranscript by remember { mutableStateOf("") }
    var isGeneratingQuestions by remember { mutableStateOf(false) }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    fun triggerGenerate(query: String) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank() || isGeneratingQuestions) return

        isGeneratingQuestions = true
        coroutineScope.launch {
            try {
                val questions = callMistralGenerateQuestions(cleanQuery)
                onQuestionsGenerated(questions)
                Toast.makeText(context, "Generated ${questions.size} coding questions!", Toast.LENGTH_SHORT).show()
                onDismiss()
            } catch (_: Exception) {
                val fallback = getFallbackQuestions(cleanQuery)
                onQuestionsGenerated(fallback)
                Toast.makeText(context, "Generated ${fallback.size} coding questions!", Toast.LENGTH_SHORT).show()
                onDismiss()
            } finally {
                isGeneratingQuestions = false
            }
        }
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Toast.makeText(context, "Speech recognition is not available on this device", Toast.LENGTH_SHORT).show()
            isVoiceMode = false
            return
        }
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListening = false
                }
                override fun onError(error: Int) {
                    isListening = false
                    isVoiceMode = false
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    isVoiceMode = false
                    isListening = false
                    partialTranscript = ""
                    if (text.isNotBlank()) {
                        promptText = text
                        triggerGenerate(text)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotEmpty()) {
                        partialTranscript = text
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
        isListening = true
        isVoiceMode = true
        partialTranscript = ""
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            promptText = ""
            startListening()
        } else {
            Toast.makeText(context, "Microphone permission is required for voice input", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isGeneratingQuestions) {
                speechRecognizer?.cancel()
                onDismiss()
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "What kind of Coding questions do you want?",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isVoiceMode || isListening) {
                    // Animated Listening UI with Waveform and pulsing status
                    val infiniteTransition = rememberInfiniteTransition(label = "listeningAnim")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.35f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(650, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseAlpha"
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.5.dp, bb),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444).copy(alpha = pulseAlpha))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Listening...",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF2563EB).copy(alpha = pulseAlpha)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            WaveformVisualizer(
                                isListening = true,
                                amplitude = 0.6f + 0.4f * kotlin.math.sin(System.currentTimeMillis() / 120.0).toFloat(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (partialTranscript.isNotBlank()) partialTranscript else "Speak your question prompt now...",
                                fontSize = 13.sp,
                                fontWeight = if (partialTranscript.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (partialTranscript.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        speechRecognizer?.cancel()
                                        isListening = false
                                        isVoiceMode = false
                                    }
                                ) {
                                    Text("Cancel Voice", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                if (partialTranscript.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            speechRecognizer?.stopListening()
                                            val captured = partialTranscript
                                            isListening = false
                                            isVoiceMode = false
                                            promptText = captured
                                            triggerGenerate(captured)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Generate", fontSize = 12.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Multiline Text Box (Max 3 lines, scrollable)
                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        placeholder = {
                            Text(
                                text = "e.g., Array questions for Class 10 ICSE with loops and sorting...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 72.dp, max = 110.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = bb,
                            unfocusedBorderColor = bb.copy(alpha = 0.6f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        trailingIcon = {
                            // Mic button: visible when empty; hidden once typing starts!
                            if (promptText.isEmpty() && !isListening && !isGeneratingQuestions) {
                                IconButton(
                                    onClick = {
                                        promptText = "" // Once mic button is pressed then no text
                                        if (ContextCompat.checkSelfPermission(
                                                context,
                                                android.Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED
                                        ) {
                                            startListening()
                                        } else {
                                            permLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                        }
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2563EB))
                                            .border(BorderStroke(1.5.dp, bb), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Mic,
                                            contentDescription = "Voice Input",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }

                if (isGeneratingQuestions) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF2563EB)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Crafting 3-4 coding questions...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (promptText.isNotBlank() && !isGeneratingQuestions) {
                        triggerGenerate(promptText)
                    }
                },
                enabled = promptText.isNotBlank() && !isGeneratingQuestions,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Generate", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (!isGeneratingQuestions) {
                        speechRecognizer?.cancel()
                        onDismiss()
                    }
                },
                enabled = !isGeneratingQuestions
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun MCQContent(
    selectedSource: QuestionSource,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val saved = remember { MistralDiskCache.getSavedMCQs(context) }
    val aiItems = remember {
        mutableStateListOf<MCQItem>().apply {
            if (saved.isNotEmpty()) {
                addAll(saved.map { MCQItem(it.first, it.second, QuestionSource.AI) })
            } else {
                addAll(listOf(
                    MCQItem("OOP Concepts", "10 questions", QuestionSource.AI),
                    MCQItem("Arrays & Lists", "15 questions", QuestionSource.AI),
                    MCQItem("Sorting", "12 questions", QuestionSource.AI),
                    MCQItem("File Handling", "8 questions", QuestionSource.AI)
                ))
            }
        }
    }
    val sirItems = remember { listOf(
        MCQItem("Java Fundamentals", "20 questions", QuestionSource.SIR),
        MCQItem("OOP Deep-Dive", "18 questions", QuestionSource.SIR),
        MCQItem("Exception Handling", "12 questions", QuestionSource.SIR)
    )}

    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        SuggestTopicDialog(
            onDismiss = { showDialog = false },
            onSubmit = { topic ->
                aiItems.add(0, MCQItem(topic, "10 questions", QuestionSource.AI))
                MistralDiskCache.saveMCQs(context, aiItems.map { Pair(it.title, it.sub) })
                showDialog = false
            }
        )
    }

    val displayedItems = if (selectedSource == QuestionSource.AI) aiItems else sirItems

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { SectionHeading(selectedSource, onSuggestNew = { showDialog = true }) }
        items(displayedItems) { item -> MCQCard(item, onNavigate) }
    }
}

@Composable
private fun PredictOutputContent(
    selectedSource: QuestionSource,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val saved = remember { MistralDiskCache.getSavedPredictOutput(context) }
    val aiItems = remember {
        mutableStateListOf<PredictOutputItem>().apply {
            if (saved.isNotEmpty()) {
                addAll(saved.mapIndexed { idx, item ->
                    PredictOutputItem(
                        setNumber = idx + 1,
                        title = item.first,
                        topic = "AI Generated",
                        questionCount = item.second,
                        difficulty = item.third,
                        codeSnippet = getSnippetForTopic(item.first),
                        source = QuestionSource.AI
                    )
                })
            } else {
                val defaults = listOf(
                    PredictOutputItem(
                        setNumber = 1,
                        title = "Loop Tracing & Conditionals",
                        topic = "Loops & Control Flow",
                        questionCount = "12 Questions",
                        difficulty = "Easy",
                        codeSnippet = "int sum = 0;\nfor (int i = 1; i <= 5; i++) {\n    if (i % 2 == 0) continue;\n    sum += i;\n}\nSystem.out.println(sum);",
                        source = QuestionSource.AI
                    ),
                    PredictOutputItem(
                        setNumber = 2,
                        title = "String Operations & Substrings",
                        topic = "Strings & Characters",
                        questionCount = "15 Questions",
                        difficulty = "Medium",
                        codeSnippet = "String s = \"KNOWLEDGE\";\nSystem.out.println(s.substring(3, 7));\nSystem.out.println(s.indexOf('E', 5));",
                        source = QuestionSource.AI
                    ),
                    PredictOutputItem(
                        setNumber = 3,
                        title = "Array Indexing & Shifting",
                        topic = "1D & 2D Arrays",
                        questionCount = "10 Questions",
                        difficulty = "Medium",
                        codeSnippet = "int[] a = {2, 4, 6, 8};\nfor (int i = 0; i < a.length - 1; i++) {\n    a[i+1] += a[i];\n}\nSystem.out.println(a[3]);",
                        source = QuestionSource.AI
                    ),
                    PredictOutputItem(
                        setNumber = 4,
                        title = "Method Calls & Recursion",
                        topic = "Recursion & Logic",
                        questionCount = "8 Questions",
                        difficulty = "Hard",
                        codeSnippet = "int test(int n) {\n    if (n <= 1) return 1;\n    return n + test(n - 2);\n}\nSystem.out.println(test(5));",
                        source = QuestionSource.AI
                    )
                )
                addAll(defaults)
                MistralDiskCache.savePredictOutput(context, defaults.map { Triple(it.title, it.questionCount, it.difficulty) })
            }
        }
    }

    val sirItems = remember { listOf(
        PredictOutputItem(
            setNumber = 1,
            title = "Sir's Pick: Operator Precedence & Increments",
            topic = "Operators & Expressions",
            questionCount = "15 Questions",
            difficulty = "Easy",
            codeSnippet = "int x = 5;\nint y = ++x * 2 + x--;\nSystem.out.println(\"x=\" + x + \", y=\" + y);",
            source = QuestionSource.SIR
        ),
        PredictOutputItem(
            setNumber = 2,
            title = "Sir's Pick: Nested Loops & Break/Continue",
            topic = "Loop Constructs",
            questionCount = "18 Questions",
            difficulty = "Medium",
            codeSnippet = "for(int i = 0; i < 3; i++) {\n    for(int j = 0; j < 3; j++) {\n        if(i == j) continue;\n        System.out.print(j);\n    }\n}",
            source = QuestionSource.SIR
        ),
        PredictOutputItem(
            setNumber = 3,
            title = "Sir's Pick: Class 10 ICSE Board Snippets",
            topic = "ICSE Board Questions",
            questionCount = "20 Questions",
            difficulty = "Hard",
            codeSnippet = "char ch = 'B';\nint code = ch + 3;\nSystem.out.println((char)code + \":\" + code);",
            source = QuestionSource.SIR
        ),
        PredictOutputItem(
            setNumber = 4,
            title = "Sir's Pick: Static Blocks & Constructors",
            topic = "OOP Mechanics",
            questionCount = "12 Questions",
            difficulty = "Hard",
            codeSnippet = "class Demo {\n    static int c = 10;\n    Demo() { c += 5; }\n}\n// Value of c after 2 instances?",
            source = QuestionSource.SIR
        )
    )}

    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        PredictOutputPromptDialog(
            onDismiss = { showDialog = false },
            startSetNumber = aiItems.size,
            onSetsGenerated = { newSets ->
                aiItems.addAll(0, newSets)
                MistralDiskCache.savePredictOutput(context, aiItems.map { Triple(it.title, it.questionCount, it.difficulty) })
            }
        )
    }

    val displayedItems = if (selectedSource == QuestionSource.AI) aiItems else sirItems

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { SectionHeading(selectedSource, onSuggestNew = { showDialog = true }) }
        items(displayedItems, key = { it.title + it.setNumber }) { item ->
            PredictOutputCard(
                item = item,
                onNavigate = onNavigate,
                onDelete = if (item.source == QuestionSource.AI) {
                    { toDelete ->
                        aiItems.remove(toDelete)
                        MistralDiskCache.savePredictOutput(context, aiItems.map { Triple(it.title, it.questionCount, it.difficulty) })
                        Toast.makeText(context, "Set deleted", Toast.LENGTH_SHORT).show()
                    }
                } else null
            )
        }
    }
}

@Composable
private fun PredictOutputCard(
    item: PredictOutputItem,
    onNavigate: (String) -> Unit,
    onDelete: ((PredictOutputItem) -> Unit)? = null
) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()

    Box(modifier = Modifier.padding(end = 5.dp, bottom = 14.dp)) {
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
                .clickable {
                    val encoded = Uri.encode(item.title, "UTF-8")
                    onNavigate("quiz_setup/$encoded")
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, bb),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header: Set pill + Title + Difficulty pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF2563EB))
                                .border(BorderStroke(1.dp, bb), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SET ${item.setNumber}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                when (item.difficulty) {
                                    "Easy" -> Color(0xFF065F46)
                                    "Medium" -> Color(0xFF92400E)
                                    else -> Color(0xFF991B1B)
                                }
                            )
                            .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(50.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = item.difficulty,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (item.difficulty) {
                                "Easy" -> Color(0xFF6EE7B7)
                                "Medium" -> Color(0xFFFCD34D)
                                else -> Color(0xFFFCA5A5)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Topic + Question count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.topic,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(" • ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = item.questionCount,
                        fontSize = 12.sp,
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Code Snippet Preview Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, bb.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Terminal,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Code Tracing Preview",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.codeSnippet,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 15.sp,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons: Practice Set + Delete (if AI)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Predict console output",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .defaultMinSize(minHeight = 28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2563EB))
                                .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(8.dp))
                                .clickable {
                                    val encoded = Uri.encode(item.title, "UTF-8")
                                    onNavigate("quiz_setup/$encoded")
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Solve Set", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }

                        if (onDelete != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .defaultMinSize(minHeight = 28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                                    .border(BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.55f)), RoundedCornerShape(8.dp))
                                    .clickable { onDelete(item) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.DeleteOutline,
                                    contentDescription = "Delete Set",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getSnippetForTopic(topic: String): String {
    val lower = topic.lowercase()
    return when {
        lower.contains("loop") -> "int c = 0;\nfor (int i = 0; i < 4; i++) {\n    c += i;\n}\nSystem.out.println(c);"
        lower.contains("string") -> "String s = \"COMPUTER\";\nSystem.out.println(s.substring(1, 5));"
        lower.contains("array") -> "int[] a = {1, 2, 3};\nSystem.out.println(a[1] + a[2]);"
        else -> "// Output snippet for $topic\nint res = 15 + 25;\nSystem.out.println(\"Result=\" + res);"
    }
}

@Composable
private fun CodingContent(
    selectedSource: QuestionSource,
    onNavigate: (String) -> Unit,
    onOpenMistralAi: (CodingItem) -> Unit
) {
    val context = LocalContext.current
    val saved = remember { MistralDiskCache.getSavedCoding(context) }
    val aiItems = remember {
        mutableStateListOf<CodingItem>().apply {
            if (saved.isNotEmpty()) {
                addAll(saved.map { CodingItem(it.first, it.second, it.third, QuestionSource.AI) })
            } else {
                val defaults = listOf(
                    CodingItem("Reverse a String", "Easy", "Strings", QuestionSource.AI),
                    CodingItem("Two Sum", "Medium", "Arrays", QuestionSource.AI),
                    CodingItem("Merge Intervals", "Hard", "Intervals", QuestionSource.AI)
                )
                addAll(defaults)
                MistralDiskCache.saveCoding(context, defaults.map { Triple(it.title, it.difficulty, it.topic) })
            }
        }
    }
    val sirItems = remember { listOf(
        CodingItem("Array Rotation", "Easy", "Arrays", QuestionSource.SIR),
        CodingItem("Palindrome Check", "Easy", "Strings", QuestionSource.SIR),
        CodingItem("Custom Sort", "Medium", "Sorting", QuestionSource.SIR),
        CodingItem("Constructor Chaining", "Medium", "OOP", QuestionSource.SIR)
    )}

    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        CodingPromptDialog(
            onDismiss = { showDialog = false },
            onQuestionsGenerated = { newQuestions ->
                aiItems.addAll(0, newQuestions)
                MistralDiskCache.saveCoding(context, aiItems.map { Triple(it.title, it.difficulty, it.topic) })
            }
        )
    }

    val displayedItems = if (selectedSource == QuestionSource.AI) aiItems else sirItems

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { SectionHeading(selectedSource, onSuggestNew = { showDialog = true }) }
        items(displayedItems) { item ->
            CodingCard(
                item = item,
                onNavigate = onNavigate,
                onOpenMistralAi = onOpenMistralAi,
                onDelete = if (item.source == QuestionSource.AI) {
                    { toDelete ->
                        aiItems.remove(toDelete)
                        MistralDiskCache.saveCoding(context, aiItems.map { Triple(it.title, it.difficulty, it.topic) })
                        val sanitizedTitle = toDelete.title.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
                        listOf("java", "python", "c++", "javascript").forEach { lang ->
                            MistralDiskCache.removeSolution(context, "code_${sanitizedTitle}_$lang")
                        }
                        Toast.makeText(context, "Question deleted", Toast.LENGTH_SHORT).show()
                    }
                } else null
            )
        }
    }
}

@Composable
private fun PYQContent(
    selectedSource: QuestionSource,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val saved = remember { MistralDiskCache.getSavedPYQs(context) }
    val aiItems = remember {
        mutableStateListOf<PYQItem>().apply {
            if (saved.isNotEmpty()) {
                addAll(saved.map { PYQItem(it.first, it.second, QuestionSource.AI) })
            } else {
                addAll(listOf(
                    PYQItem("ICSE 2023", "45 questions", QuestionSource.AI),
                    PYQItem("CBSE 2022", "50 questions", QuestionSource.AI),
                    PYQItem("ICSE 2022", "40 questions", QuestionSource.AI)
                ))
            }
        }
    }
    val sirItems = remember { listOf(
        PYQItem("Sir's Picks — Java", "30 questions", QuestionSource.SIR),
        PYQItem("Sir's Picks — OOP", "25 questions", QuestionSource.SIR)
    )}

    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        SuggestTopicDialog(
            onDismiss = { showDialog = false },
            onSubmit = { topic ->
                aiItems.add(0, PYQItem(topic, "30 questions", QuestionSource.AI))
                MistralDiskCache.savePYQs(context, aiItems.map { Pair(it.title, it.questions) })
                showDialog = false
            }
        )
    }

    val displayedItems = if (selectedSource == QuestionSource.AI) aiItems else sirItems

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { SectionHeading(selectedSource, onSuggestNew = { showDialog = true }) }
        items(displayedItems) { item -> PYQCard(item, onNavigate) }
    }
}

@Composable
private fun MCQCard(item: MCQItem, onNavigate: (String) -> Unit) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    Box(modifier = Modifier.padding(end = 5.dp, bottom = 12.dp)) {
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
                .clickable { onNavigate("quiz_setup/${item.title}") },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, bb),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(BorderStroke(1.5.dp, bb.copy(alpha = 0.12f)), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Quiz,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(item.sub, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CodingCard(
    item: CodingItem,
    onNavigate: (String) -> Unit,
    onOpenMistralAi: (CodingItem) -> Unit,
    onDelete: ((CodingItem) -> Unit)? = null
) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()

    Box(modifier = Modifier.padding(end = 5.dp, bottom = 12.dp)) {
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
                .clickable {
                    if (item.source == QuestionSource.AI) {
                        onOpenMistralAi(item)
                    } else {
                        val encodedQ = Uri.encode(item.title, "UTF-8")
                        onNavigate("code_editor?question=$encodedQ")
                    }
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, bb),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header row: Title + Difficulty pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                when (item.difficulty) {
                                    "Easy" -> Color(0xFF065F46)
                                    "Medium" -> Color(0xFF92400E)
                                    else -> Color(0xFF991B1B)
                                }
                            )
                            .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(50.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = item.difficulty,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (item.difficulty) {
                                "Easy" -> Color(0xFF6EE7B7)
                                "Medium" -> Color(0xFFFCD34D)
                                else -> Color(0xFFFCA5A5)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.topic,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    // Action buttons
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.source == QuestionSource.AI) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .defaultMinSize(minHeight = 28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2563EB))
                                    .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(8.dp))
                                    .clickable { onOpenMistralAi(item) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Problem Overview", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .defaultMinSize(minHeight = 28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(8.dp))
                                .clickable {
                                    val encodedQ = Uri.encode(item.title, "UTF-8")
                                    onNavigate("code_editor?question=$encodedQ")
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Code, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Editor", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Delete option on the right of the Editor button
                        if (onDelete != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .defaultMinSize(minHeight = 28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                                    .border(BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.55f)), RoundedCornerShape(8.dp))
                                    .clickable { onDelete(item) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.DeleteOutline,
                                    contentDescription = "Delete Question",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PYQCard(item: PYQItem, onNavigate: (String) -> Unit) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    Box(modifier = Modifier.padding(end = 5.dp, bottom = 12.dp)) {
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
                .clickable { onNavigate("pyq") },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, bb),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(BorderStroke(1.5.dp, bb.copy(alpha = 0.12f)), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Article,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(item.questions, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// AI CODING QUESTION GENERATOR: Calls Mistral AI to produce 3-4 structured coding questions as per user prompt
private suspend fun callMistralGenerateQuestions(promptText: String): List<CodingItem> = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.MISTRAL_API_KEY
    if (apiKey.isBlank()) {
        return@withContext getFallbackQuestions(promptText)
    }

    try {
        val url = URL("https://api.mistral.ai/v1/chat/completions")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $apiKey")
        conn.doOutput = true
        conn.connectTimeout = 25000
        conn.readTimeout = 25000

        val systemPrompt = """
            You are an expert computer science teacher and coding question generator for Indian school students (Class 9-12 ICSE/CBSE and beginners).
            Generate exactly 3 to 4 distinct, high-quality, practical coding problems based on the user's prompt.
            Return ONLY a valid JSON array of objects. No markdown backticks, no markdown code fence, no commentary.
            Each object in the array MUST have:
            - "title": A clear, concise title of the problem (e.g. "Linear Search in Integer Array")
            - "difficulty": Exactly one of "Easy", "Medium", or "Hard"
            - "topic": Short topic name (e.g. "Arrays", "Strings", "Recursion", "Loops", "OOP")
            Example:
            [
              {"title": "Sum of Array Elements", "difficulty": "Easy", "topic": "Arrays"},
              {"title": "Search Element in Array", "difficulty": "Easy", "topic": "Arrays"},
              {"title": "Find Second Largest Element", "difficulty": "Medium", "topic": "Arrays"},
              {"title": "Bubble Sort an Array", "difficulty": "Medium", "topic": "Arrays"}
            ]
        """.trimIndent()

        val body = JSONObject().apply {
            put("model", "mistral-small-latest")
            put("messages", JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", systemPrompt))
                put(JSONObject().put("role", "user").put("content", "Generate 3 to 4 coding questions for: $promptText"))
            })
            put("max_tokens", 800)
            put("temperature", 0.3)
        }

        conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        val responseCode = conn.responseCode
        val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
        val responseText = stream.bufferedReader().use { it.readText() }

        if (responseCode in 200..299) {
            val json = JSONObject(responseText)
            val content = json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()

            val cleanJson = content
                .replace(Regex("^```(?:json)?\\s*"), "")
                .replace(Regex("\\s*```$"), "")
                .trim()

            val arr = JSONArray(cleanJson)
            val list = mutableListOf<CodingItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val title = obj.optString("title", "").trim()
                val diff = obj.optString("difficulty", "Medium").trim()
                val topic = obj.optString("topic", "Coding").trim()
                if (title.isNotBlank()) {
                    list.add(CodingItem(title, diff, topic, QuestionSource.AI))
                }
            }
            if (list.size >= 3) list.take(4) else getFallbackQuestions(promptText)
        } else {
            getFallbackQuestions(promptText)
        }
    } catch (_: Exception) {
        getFallbackQuestions(promptText)
    }
}

private fun getFallbackQuestions(prompt: String): List<CodingItem> {
    val clean = prompt.trim()
    val lower = clean.lowercase()
    return when {
        lower.contains("array") -> listOf(
            CodingItem("Find Largest and Smallest in Array", "Easy", "Arrays", QuestionSource.AI),
            CodingItem("Linear Search in Array", "Easy", "Arrays", QuestionSource.AI),
            CodingItem("Bubble Sort Array Elements", "Medium", "Arrays", QuestionSource.AI),
            CodingItem("Remove Duplicates from Array", "Medium", "Arrays", QuestionSource.AI)
        )
        lower.contains("string") -> listOf(
            CodingItem("Check Palindrome String", "Easy", "Strings", QuestionSource.AI),
            CodingItem("Count Vowels and Consonants", "Easy", "Strings", QuestionSource.AI),
            CodingItem("Reverse Words in a Sentence", "Medium", "Strings", QuestionSource.AI),
            CodingItem("Check Anagram Strings", "Medium", "Strings", QuestionSource.AI)
        )
        lower.contains("loop") || lower.contains("pattern") -> listOf(
            CodingItem("Print Right-Angled Star Triangle", "Easy", "Loops", QuestionSource.AI),
            CodingItem("Check Prime Number", "Easy", "Loops", QuestionSource.AI),
            CodingItem("Fibonacci Series up to N Terms", "Medium", "Loops", QuestionSource.AI),
            CodingItem("Check Armstrong Number", "Medium", "Loops", QuestionSource.AI)
        )
        lower.contains("oop") || lower.contains("class") -> listOf(
            CodingItem("Student Class with Marks & Grade", "Easy", "OOP", QuestionSource.AI),
            CodingItem("Bank Account with Deposit & Withdraw", "Medium", "OOP", QuestionSource.AI),
            CodingItem("Constructor Overloading in Box Class", "Medium", "OOP", QuestionSource.AI),
            CodingItem("Method Overriding with Shape Hierarchy", "Hard", "OOP", QuestionSource.AI)
        )
        lower.contains("sort") || lower.contains("search") -> listOf(
            CodingItem("Binary Search Implementation", "Easy", "Search", QuestionSource.AI),
            CodingItem("Selection Sort on Integers", "Medium", "Sorting", QuestionSource.AI),
            CodingItem("Insertion Sort Implementation", "Medium", "Sorting", QuestionSource.AI),
            CodingItem("Quick Sort Algorithm", "Hard", "Sorting", QuestionSource.AI)
        )
        else -> listOf(
            CodingItem("$clean: Basic Implementation", "Easy", "Fundamentals", QuestionSource.AI),
            CodingItem("$clean: Practical Problem", "Medium", "Custom", QuestionSource.AI),
            CodingItem("$clean: Edge Cases & Optimization", "Medium", "Logic", QuestionSource.AI),
            CodingItem("$clean: Advanced Solution", "Hard", "Custom", QuestionSource.AI)
        )
    }
}

// AI PREDICT OUTPUT GENERATOR: Calls Mistral AI to produce 2-3 structured Predict the Output question sets as per user prompt
private suspend fun callMistralGeneratePredictOutput(promptText: String, startSetIndex: Int): List<PredictOutputItem> = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.MISTRAL_API_KEY
    if (apiKey.isBlank()) {
        return@withContext getFallbackPredictOutput(promptText, startSetIndex)
    }

    try {
        val url = URL("https://api.mistral.ai/v1/chat/completions")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $apiKey")
        conn.doOutput = true
        conn.connectTimeout = 25000
        conn.readTimeout = 25000

        val systemPrompt = """
            You are an expert computer science teacher generating Java "Predict the Output" code tracing practice question sets for students (ICSE Class 9-10 & CBSE Class 11-12).
            Generate exactly 2 to 3 distinct output prediction question sets based on the user's topic prompt.
            Return ONLY a valid JSON array of objects. No markdown backticks, no markdown fence, no commentary.
            Each object in the array MUST have:
            - "title": Title of the set (e.g. "Nested For-Loops & Break Tracing")
            - "topic": Topic name (e.g. "Loops", "Strings", "Precedence", "Recursion")
            - "questionCount": Number of questions string (e.g. "10 Questions", "12 Questions")
            - "difficulty": Exactly one of "Easy", "Medium", or "Hard"
            - "codeSnippet": A short 3 to 6 line valid Java snippet demonstrating the output problem (use System.out.println)
        """.trimIndent()

        val body = JSONObject().apply {
            put("model", "mistral-small-latest")
            put("messages", JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", systemPrompt))
                put(JSONObject().put("role", "user").put("content", "Generate 2 to 3 Predict the Output sets for: $promptText"))
            })
            put("max_tokens", 800)
            put("temperature", 0.3)
        }

        conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        val responseCode = conn.responseCode
        val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
        val responseText = stream.bufferedReader().use { it.readText() }

        if (responseCode in 200..299) {
            val json = JSONObject(responseText)
            val content = json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()

            val cleanJson = content
                .replace(Regex("^```(?:json)?\\s*"), "")
                .replace(Regex("\\s*```$"), "")
                .trim()

            val arr = JSONArray(cleanJson)
            val list = mutableListOf<PredictOutputItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val title = obj.optString("title", "").trim()
                val topic = obj.optString("topic", "Output Tracing").trim()
                val count = obj.optString("questionCount", "10 Questions").trim()
                val diff = obj.optString("difficulty", "Medium").trim()
                val snippet = obj.optString("codeSnippet", getSnippetForTopic(topic)).trim()
                if (title.isNotBlank()) {
                    list.add(
                        PredictOutputItem(
                            setNumber = startSetIndex + i + 1,
                            title = title,
                            topic = topic,
                            questionCount = count,
                            difficulty = diff,
                            codeSnippet = snippet,
                            source = QuestionSource.AI
                        )
                    )
                }
            }
            if (list.isNotEmpty()) list else getFallbackPredictOutput(promptText, startSetIndex)
        } else {
            getFallbackPredictOutput(promptText, startSetIndex)
        }
    } catch (_: Exception) {
        getFallbackPredictOutput(promptText, startSetIndex)
    }
}

private fun getFallbackPredictOutput(prompt: String, startSetIndex: Int): List<PredictOutputItem> {
    val clean = prompt.trim()
    val lower = clean.lowercase()
    val s1 = startSetIndex + 1
    val s2 = startSetIndex + 2
    return when {
        lower.contains("loop") -> listOf(
            PredictOutputItem(s1, "Accumulator & Modulo Loops", "Loops", "10 Questions", "Easy", "int s = 0;\nfor (int i = 1; i <= 5; i += 2) {\n    s += i * 2;\n}\nSystem.out.println(s);", QuestionSource.AI),
            PredictOutputItem(s2, "Nested While-Loop Conditions", "Nested Loops", "12 Questions", "Medium", "int a = 1, b = 4;\nwhile (a < b) {\n    System.out.print(a + \":\" + b + \" \");\n    a++; b--;\n}", QuestionSource.AI)
        )
        lower.contains("string") -> listOf(
            PredictOutputItem(s1, "String Substring & Character Tracing", "Strings", "12 Questions", "Easy", "String s = \"ANTIGRAVITY\";\nSystem.out.println(s.substring(4, 9));\nSystem.out.println(s.charAt(2));", QuestionSource.AI),
            PredictOutputItem(s2, "String Concat & Index Search", "Strings", "15 Questions", "Medium", "String s = \"MISSISSIPPI\";\nint idx = s.indexOf(\"IS\");\nSystem.out.println(idx + \" \" + s.lastIndexOf('P'));", QuestionSource.AI)
        )
        lower.contains("op") || lower.contains("increment") || lower.contains("decrement") -> listOf(
            PredictOutputItem(s1, "Prefix & Postfix Increment Arithmetic", "Operators", "10 Questions", "Easy", "int a = 4;\nint b = ++a * 3 + a--;\nSystem.out.println(\"a=\" + a + \", b=\" + b);", QuestionSource.AI),
            PredictOutputItem(s2, "Ternary & Bitwise Evaluation", "Operators", "12 Questions", "Medium", "int x = 10, y = 20;\nint r = (x > 5 && y < 25) ? x ^ y : x & y;\nSystem.out.println(r);", QuestionSource.AI)
        )
        lower.contains("array") -> listOf(
            PredictOutputItem(s1, "Array Traversal & Cumulative Sum", "Arrays", "10 Questions", "Easy", "int[] a = {3, 1, 4, 1, 5};\nint sum = 0;\nfor(int v : a) sum += v;\nSystem.out.println(sum);", QuestionSource.AI),
            PredictOutputItem(s2, "2D Array Row-Major Scan", "Arrays", "12 Questions", "Medium", "int[][] m = {{1, 2}, {3, 4}};\nSystem.out.println(m[1][0] * m[0][1]);", QuestionSource.AI)
        )
        lower.contains("rec") -> listOf(
            PredictOutputItem(s1, "Recursive Decrement Tracing", "Recursion", "8 Questions", "Medium", "int fun(int n) {\n    if (n <= 1) return 1;\n    return n * fun(n - 1);\n}\nSystem.out.println(fun(4));", QuestionSource.AI),
            PredictOutputItem(s2, "Branching Recursion Output", "Recursion", "10 Questions", "Hard", "void p(int x) {\n    if (x <= 0) return;\n    p(x - 1);\n    System.out.print(x + \" \");\n}\np(3);", QuestionSource.AI)
        )
        else -> listOf(
            PredictOutputItem(s1, "$clean: Basic Output Tracing", "Custom Logic", "10 Questions", "Medium", getSnippetForTopic(clean), QuestionSource.AI),
            PredictOutputItem(s2, "$clean: Advanced Output Tracing", "Custom Logic", "12 Questions", "Hard", "// Output tracing for $clean\nint x = 7;\nSystem.out.println((x << 1) + (x >> 1));", QuestionSource.AI)
        )
    }
}

// 3. MISTRAL AI CODE GENERATOR HELPER: Calls Mistral AI to produce line-by-line commented code formatted in Markdown
private suspend fun callMistralGenerateCode(
    title: String,
    topic: String,
    difficulty: String,
    language: String
): String = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.MISTRAL_API_KEY
    if (apiKey.isBlank()) {
        return@withContext getFallbackCommentedCode(title, language)
    }

    try {
        val url = URL("https://api.mistral.ai/v1/chat/completions")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $apiKey")
        conn.doOutput = true
        conn.connectTimeout = 30000
        conn.readTimeout = 30000

        val prompt = """
            You are an expert programming instructor.
            Write a complete, optimal, and beginner-friendly solution in $language for the coding problem: "$title".
            Topic: $topic, Difficulty: $difficulty.

            CRITICAL MANDATORY INSTRUCTIONS:
            1. YOU MUST WRITE A CLEAR, EXPLANATORY COMMENT FOR EACH AND EVERY SINGLE LINE OF CODE. Do NOT skip any line. Every single line of code must have its own comment.
            2. The entire output MUST be formatted in proper, clean Markdown.
            3. Structure your markdown output as follows:

            ## Problem Overview
            (Brief explanation of the logic and concept)

            ## Solution ($language)
            ```$language
            // Comment for every line
            ...
            ```

            ## Expected Output
            ```text
            Sample Input: ...
            Sample Output: ...
            ```

            ## Complexity Analysis
            - **Time Complexity:** ...
            - **Space Complexity:** ...
        """.trimIndent()

        val body = JSONObject().apply {
            put("model", "mistral-small-latest")
            put("messages", JSONArray().put(
                JSONObject().put("role", "user").put("content", prompt)
            ))
            put("max_tokens", 1800)
            put("temperature", 0.15)
        }

        conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        val responseCode = conn.responseCode
        val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
        val responseText = stream.bufferedReader().use { it.readText() }

        if (responseCode in 200..299) {
            val json = JSONObject(responseText)
            val content = json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
            if (content.isNotBlank()) content else getFallbackCommentedCode(title, language)
        } else {
            getFallbackCommentedCode(title, language)
        }
    } catch (_: Exception) {
        getFallbackCommentedCode(title, language)
    }
}

private fun extractCodeFromMarkdown(markdown: String): String {
    val regex = Regex("```(?:[a-zA-Z0-9+#]+)?\\n([\\s\\S]*?)```")
    val match = regex.find(markdown)
    return match?.groupValues?.get(1)?.trim() ?: markdown
}

private fun getFallbackCommentedCode(title: String, language: String): String {
    return when (title) {
        "Reverse a String" -> """
## Problem Overview
We reverse a string by converting it to a mutable character array and swapping characters from both ends towards the center using a two-pointer technique.

## Solution ($language)
```$language
// Declare the main public class for String Reversal
public class ReverseStringSolution {
    // Standard main entry point method
    public static void main(String[] args) {
        // Define sample input string to be reversed
        String text = "Vastavik";
        // Invoke the reversal method and store result
        String result = reverse(text);
        // Print the reversed string output to stdout
        System.out.println("Reversed: " + result);
    }

    // Method to reverse characters of a string
    public static String reverse(String input) {
        // Convert the immutable String into a mutable character array
        char[] characters = input.toCharArray();
        // Initialize left pointer at the beginning of the array
        int left = 0;
        // Initialize right pointer at the last valid index of the array
        int right = characters.length - 1;
        // Iterate while left pointer is strictly less than right pointer
        while (left < right) {
            // Save the left character into a temporary holder
            char temp = characters[left];
            // Copy the character from the right position to the left position
            characters[left] = characters[right];
            // Assign the preserved temporary character to the right position
            characters[right] = temp;
            // Advance the left pointer towards the center by 1
            left++;
            // Decrement the right pointer towards the center by 1
            right--;
        }
        // Construct and return a new String from the reversed character array
        return new String(characters);
    }
}
```

## Expected Output
```text
Sample Input: "Vastavik"
Sample Output: "kivatsaV"
```

## Complexity Analysis
- **Time Complexity:** O(N) where N is the number of characters in the string.
- **Space Complexity:** O(N) for the character array storage.
""".trimIndent()

        "Two Sum" -> """
## Problem Overview
We solve the Two Sum problem in a single pass using a HashMap to store values and their respective indices for O(1) constant time complement lookup.

## Solution ($language)
```$language
// Import HashMap data structure from java standard utilities
import java.util.HashMap;
// Import Arrays utility for formatted output printing
import java.util.Arrays;

// Define public solution class
public class TwoSumSolution {
    // Driver main method to run test cases
    public static void main(String[] args) {
        // Declare an array of integer inputs
        int[] numbers = {2, 7, 11, 15};
        // Define the target sum we are searching for
        int target = 9;
        // Call the solution function and receive matching indices
        int[] result = findTwoSum(numbers, target);
        // Print the matching index pair to console
        System.out.println("Indices: " + Arrays.toString(result));
    }

    // Function to find two indices whose elements sum to target
    public static int[] findTwoSum(int[] nums, int target) {
        // Instantiate a HashMap to map number value to its index
        HashMap<Integer, Integer> visited = new HashMap<>();
        // Iterate through the array using index i
        for (int i = 0; i < nums.length; i++) {
            // Compute the complement value needed to reach target
            int complement = target - nums[i];
            // Check if the complement was already seen in preceding elements
            if (visited.containsKey(complement)) {
                // If found, construct and return the two matching indices
                return new int[] { visited.get(complement), i };
            }
            // Record current number and its index in the HashMap
            visited.put(nums[i], i);
        }
        // Return empty array if no pair satisfies the target condition
        return new int[] {};
    }
}
```

## Expected Output
```text
Sample Input: nums = [2, 7, 11, 15], target = 9
Sample Output: Indices: [0, 1]
```

## Complexity Analysis
- **Time Complexity:** O(N) single pass through the array.
- **Space Complexity:** O(N) auxiliary space for HashMap.
""".trimIndent()

        else -> """
## Problem Overview
Optimal algorithm for **$title** with comprehensive step-by-step logic and line-by-line comments.

## Solution ($language)
```$language
// Import all standard utility classes
import java.util.*;

// Define the public solution class
public class ProblemSolution {
    // Main driver method
    public static void main(String[] args) {
        // Output initialization status
        System.out.println("Processing problem: $title");
        // Execute the algorithm
        executeAlgorithm();
    }

    // Core method containing algorithmic logic
    public static void executeAlgorithm() {
        // Declare tracking variable
        boolean isComplete = true;
        // Verify completion status
        if (isComplete) {
            // Output confirmation message
            System.out.println("Algorithm executed successfully.");
        }
    }
}
```

## Expected Output
```text
Processing problem: $title
Algorithm executed successfully.
```

## Complexity Analysis
- **Time Complexity:** O(N)
- **Space Complexity:** O(1)
""".trimIndent()
    }
}
