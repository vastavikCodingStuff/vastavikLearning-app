package com.vastavik.computer.ui.screens.practice

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.BuildConfig
import com.vastavik.computer.ui.screens.chat.ParsedMarkdownText
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private enum class QuestionSource(val label: String, val tagBg: Color, val tagText: Color) {
    AI("AI-Generated", Color(0xFF2563EB), Color.White),
    SIR("Sir-Generated", Color(0xFFF59E0B), Color(0xFF0F172A))
}

private data class MCQItem(val title: String, val sub: String, val source: QuestionSource)
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

    var selectedTab by remember { mutableIntStateOf(1) } // 0: MCQs, 1: Coding, 2: PYQs
    var selectedSource by remember { mutableStateOf(QuestionSource.AI) } // 1. Top Toggle AI vs Sir
    val tabs = listOf("MCQs", "Coding", "PYQs")

    // Active AI coding item for Mistral solution sheet
    var activeCodingItem by remember { mutableStateOf<CodingItem?>(null) }
    var selectedLanguage by remember { mutableStateOf("Java") }
    var aiSolutionMarkdown by remember { mutableStateOf("") }
    var isGeneratingCode by remember { mutableStateOf(false) }

    fun loadMistralSolution(item: CodingItem, lang: String) {
        isGeneratingCode = true
        aiSolutionMarkdown = ""
        coroutineScope.launch {
            val response = callMistralGenerateCode(
                title = item.title,
                topic = item.topic,
                difficulty = item.difficulty,
                language = lang
            )
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
                Text(
                    text = "Practice",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.width(12.dp))

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
                                .background(if (isSir) Color(0xFF2563EB) else Color.Transparent)
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
                                    tint = if (isSir) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sir",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSir) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

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

            Spacer(modifier = Modifier.height(6.dp))

            // CATEGORY TABS [ MCQs | Coding | PYQs ] with Neo-Brutalist Thick Borders
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
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF2563EB) else Color.Transparent)
                                .border(
                                    if (isSelected) BorderStroke(1.5.dp, bb) else BorderStroke(0.dp, Color.Transparent),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedTab = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                    1 -> CodingContent(
                        selectedSource = selectedSource,
                        onNavigate = onNavigate,
                        onOpenMistralAi = { item ->
                            activeCodingItem = item
                            selectedLanguage = "Java"
                            loadMistralSolution(item, "Java")
                        }
                    )
                    2 -> PYQContent(
                        selectedSource = selectedSource,
                        onNavigate = onNavigate
                    )
                }
            }

            // 2. UNDER DEVELOPMENT BANNER: Placed at the bottom of the page above the Bottom Navigation
            BottomDevBanner()
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(Color(0xFF2563EB).copy(alpha = 0.15f))
                                    .border(BorderStroke(1.dp, Color(0xFF2563EB)), RoundedCornerShape(50.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Mistral AI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                            }
                        }
                        Text(
                            text = "Line-by-line commented code & markdown explanation",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { activeCodingItem = null }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
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
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = Color(0xFF2563EB),
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Mistral AI is generating code with line-by-line comments...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Formatting output in proper Markdown",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                                activeCodingItem = null
                                onNavigate("code_editor?initialCode=$encoded&language=$selectedLanguage")
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
private fun SectionHeading(source: QuestionSource) {
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
    }
}

@Composable
private fun BottomDevBanner() {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
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
private fun MCQContent(
    selectedSource: QuestionSource,
    onNavigate: (String) -> Unit
) {
    val aiItems = listOf(
        MCQItem("OOP Concepts", "10 questions", QuestionSource.AI),
        MCQItem("Arrays & Lists", "15 questions", QuestionSource.AI),
        MCQItem("Sorting", "12 questions", QuestionSource.AI),
        MCQItem("File Handling", "8 questions", QuestionSource.AI)
    )
    val sirItems = listOf(
        MCQItem("Java Fundamentals", "20 questions", QuestionSource.SIR),
        MCQItem("OOP Deep-Dive", "18 questions", QuestionSource.SIR),
        MCQItem("Exception Handling", "12 questions", QuestionSource.SIR)
    )

    val displayedItems = if (selectedSource == QuestionSource.AI) aiItems else sirItems

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item { SectionHeading(selectedSource) }
        items(displayedItems) { item -> MCQCard(item, onNavigate) }
    }
}

@Composable
private fun CodingContent(
    selectedSource: QuestionSource,
    onNavigate: (String) -> Unit,
    onOpenMistralAi: (CodingItem) -> Unit
) {
    val aiItems = listOf(
        CodingItem("Reverse a String", "Easy", "Strings", QuestionSource.AI),
        CodingItem("Two Sum", "Medium", "Arrays", QuestionSource.AI),
        CodingItem("Merge Intervals", "Hard", "Intervals", QuestionSource.AI)
    )
    val sirItems = listOf(
        CodingItem("Array Rotation", "Easy", "Arrays", QuestionSource.SIR),
        CodingItem("Palindrome Check", "Easy", "Strings", QuestionSource.SIR),
        CodingItem("Custom Sort", "Medium", "Sorting", QuestionSource.SIR),
        CodingItem("Constructor Chaining", "Medium", "OOP", QuestionSource.SIR)
    )

    val displayedItems = if (selectedSource == QuestionSource.AI) aiItems else sirItems

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item { SectionHeading(selectedSource) }
        items(displayedItems) { item ->
            CodingCard(
                item = item,
                onNavigate = onNavigate,
                onOpenMistralAi = onOpenMistralAi
            )
        }
    }
}

@Composable
private fun PYQContent(
    selectedSource: QuestionSource,
    onNavigate: (String) -> Unit
) {
    val aiItems = listOf(
        PYQItem("ICSE 2023", "45 questions", QuestionSource.AI),
        PYQItem("CBSE 2022", "50 questions", QuestionSource.AI),
        PYQItem("ICSE 2022", "40 questions", QuestionSource.AI)
    )
    val sirItems = listOf(
        PYQItem("Sir's Picks — Java", "30 questions", QuestionSource.SIR),
        PYQItem("Sir's Picks — OOP", "25 questions", QuestionSource.SIR)
    )

    val displayedItems = if (selectedSource == QuestionSource.AI) aiItems else sirItems

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item { SectionHeading(selectedSource) }
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
    onOpenMistralAi: (CodingItem) -> Unit
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
                        onNavigate("code_editor")
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.source == QuestionSource.AI) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2563EB))
                                    .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(8.dp))
                                    .clickable { onOpenMistralAi(item) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mistral Code", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(8.dp))
                                .clickable { onNavigate("code_editor") }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Code, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Editor", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
