package com.vastavik.computer.ui.screens.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val BorderBlack = Color.Black
private val PrimaryBlue = Color(0xFF2563EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizSetupScreen(
    topic: String,
    onNavigate: (String) -> Unit = {}
) {
    var selectedTopic by remember { mutableStateOf(topic) }
    var questionCount by remember { mutableIntStateOf(10) }
    var difficulty by remember { mutableStateOf("Medium") }
    var isGenerating by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val topicOptions = listOf(
        "OOP Concepts", "Arrays & Lists", "Sorting Algorithms", "File Handling",
        "Java Basics", "Python Basics", "JavaScript Basics", "SQL Basics",
        "Data Structures", "Web Development"
    )
    var showTopicDropdown by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigate("home") }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Text(
                    text = "Generate Quiz",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // Topic card - editable
                Text("Topic", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 5.dp, y = 5.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(BorderBlack)
                    )
                    OutlinedTextField(
                        value = selectedTopic,
                        onValueChange = { selectedTopic = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter topic...", fontSize = 14.sp, color = Color(0xFF94A3B8)) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BorderBlack,
                            unfocusedBorderColor = BorderBlack,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        singleLine = true
                    )
                }

                // Quick topic chips
                Spacer(modifier = Modifier.height(10.dp))
                Text("Quick select:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    topicOptions.chunked(3).forEach { rowTopics ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowTopics.forEach { t ->
                                val isSelected = selectedTopic == t
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.surface)
                                        .border(BorderStroke(1.5.dp, BorderBlack), RoundedCornerShape(50.dp))
                                        .clickable { selectedTopic = t }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        t,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Number of Questions
                Text("Number of Questions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(5, 10, 15, 20).forEach { count ->
                        Box(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 4.dp, y = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BorderBlack)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (questionCount == count) PrimaryBlue else MaterialTheme.colorScheme.surface)
                                    .border(BorderStroke(2.dp, BorderBlack), RoundedCornerShape(12.dp))
                                    .clickable { questionCount = count },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$count",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (questionCount == count) Color.White else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
                // Second row for higher counts
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(25, 30, 40, 50).forEach { count ->
                        Box(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 4.dp, y = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BorderBlack)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (questionCount == count) PrimaryBlue else MaterialTheme.colorScheme.surface)
                                    .border(BorderStroke(2.dp, BorderBlack), RoundedCornerShape(12.dp))
                                    .clickable { questionCount = count },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$count",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (questionCount == count) Color.White else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Difficulty
                Text("Difficulty", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Easy", "Medium", "Hard").forEach { diff ->
                        val isSelected = difficulty == diff
                        val chipColor = when (diff) {
                            "Easy" -> Color(0xFF065F46)
                            "Medium" -> Color(0xFF92400E)
                            else -> Color(0xFF991B1B)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 4.dp, y = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BorderBlack)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) chipColor else MaterialTheme.colorScheme.surface)
                                    .border(BorderStroke(2.dp, BorderBlack), RoundedCornerShape(12.dp))
                                    .clickable { difficulty = diff },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        diff,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }

                if (errorMsg.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }

            // Generate Quiz button pinned at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 5.dp, y = 5.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(BorderBlack)
                    )
                    Button(
                        onClick = {
                            if (selectedTopic.isBlank()) {
                                errorMsg = "Please enter a topic"
                                return@Button
                            }
                            isGenerating = true
                            errorMsg = ""
                            coroutineScope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    QuizManager.generateQuiz(selectedTopic, questionCount, difficulty)
                                }
                                isGenerating = false
                                if (result == "ok") {
                                    onNavigate("quiz_taking/quiz_1")
                                } else {
                                    errorMsg = result
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(BorderStroke(2.dp, BorderBlack), RoundedCornerShape(14.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        enabled = !isGenerating
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Generating $questionCount questions...", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Generate Quiz", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
