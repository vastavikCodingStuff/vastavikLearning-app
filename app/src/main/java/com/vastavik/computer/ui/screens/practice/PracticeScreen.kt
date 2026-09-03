package com.vastavik.computer.ui.screens.practice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor

private enum class QuestionSource(val label: String, val tagBg: Color, val tagText: Color) {
    AI("AI-Generated", Color(0xFF2563EB), Color.White),
    SIR("Sir-Generated", Color(0xFFF59E0B), Color(0xFF0F172A))
}

private data class MCQItem(val title: String, val sub: String, val source: QuestionSource)
private data class CodingItem(val title: String, val difficulty: String, val topic: String, val source: QuestionSource)
private data class PYQItem(val title: String, val questions: String, val source: QuestionSource)

@Composable
fun PracticeScreen(onNavigate: (String) -> Unit) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
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
                        .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(50))
                        .clickable { onNavigate("profile") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Box(modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 4.dp, y = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(bs)
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(BorderStroke(2.dp, bb), RoundedCornerShape(16.dp))
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
                                            if (isSelected) BorderStroke(1.5.dp, bb) else BorderStroke(0.dp, Color.Transparent),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedTab = index }
                                        .padding(horizontal = 20.dp, vertical = 10.dp)
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

                    Spacer(modifier = Modifier.height(20.dp))

                    when (selectedTab) {
                        0 -> MCQContent(onNavigate = onNavigate)
                        1 -> CodingContent(onNavigate = onNavigate)
                        2 -> PYQContent(onNavigate = onNavigate)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                SideDevBanner(modifier = Modifier.fillMaxHeight().width(72.dp))
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
            text = if (source == QuestionSource.SIR) "Hand-picked by Sir" else "Auto-generated",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SideDevBanner(modifier: Modifier = Modifier) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    Box(
        modifier = modifier
            .padding(vertical = 14.dp)
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
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, bb),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
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
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "UNDER",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "DEVELOPMENT",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "One builder. One app. Full version coming eventually.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun MCQContent(onNavigate: (String) -> Unit) {
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item { SectionHeading(QuestionSource.AI) }
        items(aiItems) { item -> MCQCard(item, onNavigate) }
        item { Spacer(modifier = Modifier.height(18.dp)) }
        item { SectionHeading(QuestionSource.SIR) }
        items(sirItems) { item -> MCQCard(item, onNavigate) }
    }
}

@Composable
private fun CodingContent(onNavigate: (String) -> Unit) {
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item { SectionHeading(QuestionSource.AI) }
        items(aiItems) { item -> CodingCard(item, onNavigate) }
        item { Spacer(modifier = Modifier.height(18.dp)) }
        item { SectionHeading(QuestionSource.SIR) }
        items(sirItems) { item -> CodingCard(item, onNavigate) }
    }
}

@Composable
private fun PYQContent(onNavigate: (String) -> Unit) {
    val aiItems = listOf(
        PYQItem("ICSE 2023", "45 questions", QuestionSource.AI),
        PYQItem("CBSE 2022", "50 questions", QuestionSource.AI),
        PYQItem("ICSE 2022", "40 questions", QuestionSource.AI)
    )
    val sirItems = listOf(
        PYQItem("Sir's Picks — Java", "30 questions", QuestionSource.SIR),
        PYQItem("Sir's Picks — OOP", "25 questions", QuestionSource.SIR)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item { SectionHeading(QuestionSource.AI) }
        items(aiItems) { item -> PYQCard(item, onNavigate) }
        item { Spacer(modifier = Modifier.height(18.dp)) }
        item { SectionHeading(QuestionSource.SIR) }
        items(sirItems) { item -> PYQCard(item, onNavigate) }
    }
}

@Composable
private fun MCQCard(item: MCQItem, onNavigate: (String) -> Unit) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
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
                        tint = MaterialTheme.colorScheme.primary,
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
private fun CodingCard(item: CodingItem, onNavigate: (String) -> Unit) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
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
                .clickable { onNavigate("code_editor") },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, bb),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
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
                            .padding(horizontal = 12.dp, vertical = 6.dp)
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
                Spacer(modifier = Modifier.height(8.dp))
                Text(item.topic, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun PYQCard(item: PYQItem, onNavigate: (String) -> Unit) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
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
                        tint = MaterialTheme.colorScheme.primary,
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
