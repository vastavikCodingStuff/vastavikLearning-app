package com.vastavik.computer.ui.screens.practice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

private val BorderBlack = Color.Black

@Composable
fun PracticeScreen(onNavigate: (String) -> Unit) {
    var selectedTab by remember { mutableIntStateOf(1) }
    val tabs = listOf("MCQs", "Coding", "PYQs")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Top bar: "Practice" + profile
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
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                        .border(BorderStroke(1.5.dp, Color.Black), RoundedCornerShape(50))
                        .clickable { onNavigate("profile") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                Box(modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 4.dp, y = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(BorderBlack)
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(BorderStroke(2.dp, BorderBlack), RoundedCornerShape(16.dp))
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .border(
                                        if (isSelected) BorderStroke(1.5.dp, BorderBlack) else BorderStroke(0.dp, Color.Transparent),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedTab = index }
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                when (selectedTab) {
                    0 -> MCQTab(onNavigate = onNavigate)
                    1 -> CodingTab(onNavigate = onNavigate)
                    2 -> PYQTab(onNavigate = onNavigate)
                }
            }
        }
    }
}

@Composable
private fun MCQTab(onNavigate: (String) -> Unit) {
    val quizzes = listOf(
        Triple("OOP Concepts", "10 questions", Icons.Filled.Quiz),
        Triple("Arrays & Lists", "15 questions", Icons.Filled.Quiz),
        Triple("Sorting", "12 questions", Icons.Filled.Quiz),
        Triple("File Handling", "8 questions", Icons.Filled.Quiz)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        items(quizzes) { (title, sub, icon) ->
            Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BorderBlack)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("quiz_setup/$title") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(2.dp, BorderBlack),
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
                                .border(BorderStroke(1.5.dp, BorderBlack.copy(alpha = 0.12f)), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(sub, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    }
}

@Composable
private fun CodingTab(onNavigate: (String) -> Unit) {
    val challenges = listOf(
        Triple("Reverse a String", "Easy", "Strings"),
        Triple("Two Sum", "Medium", "Arrays"),
        Triple("Merge Intervals", "Hard", "Intervals")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        items(challenges) { (title, difficulty, topic) ->
            Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BorderBlack)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("code_editor") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(2.dp, BorderBlack),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(
                                        when (difficulty) {
                                            "Easy" -> Color(0xFF065F46)
                                            "Medium" -> Color(0xFF92400E)
                                            else -> Color(0xFF991B1B)
                                        }
                                    )
                                    .border(BorderStroke(1.5.dp, BorderBlack), RoundedCornerShape(50.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = difficulty,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (difficulty) {
                                        "Easy" -> Color(0xFF6EE7B7)
                                        "Medium" -> Color(0xFFFCD34D)
                                        else -> Color(0xFFFCA5A5)
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(topic, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun PYQTab(onNavigate: (String) -> Unit) {
    val pyqs = listOf(
        Triple("ICSE 2023", "45 questions", Icons.Filled.Article),
        Triple("CBSE 2022", "50 questions", Icons.Filled.Article),
        Triple("ICSE 2022", "40 questions", Icons.Filled.Article)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        items(pyqs) { (title, questions, icon) ->
            Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BorderBlack)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("pyq") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(2.dp, BorderBlack),
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
                                .border(BorderStroke(1.5.dp, BorderBlack.copy(alpha = 0.12f)), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(questions, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    }
}
