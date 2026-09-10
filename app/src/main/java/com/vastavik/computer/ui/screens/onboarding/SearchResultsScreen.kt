package com.vastavik.computer.ui.screens.onboarding

import android.content.Context
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.data.api.model.CourseItem
import com.vastavik.computer.data.api.model.LessonResponse
import com.vastavik.computer.data.api.model.TopicItem
import com.vastavik.computer.data.repository.VastavikApiRepository
import com.vastavik.computer.di.RepositoryEntryPoint
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor
import com.vastavik.computer.utils.ActivityLog
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SearchResultDisplayItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String, // "Course", "Topic", "Lesson"
    val targetRoute: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(onNavigate: (String) -> Unit, onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    val coroutineScope = rememberCoroutineScope()

    val repo = remember {
        try {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                RepositoryEntryPoint::class.java
            ).vastavikApiRepository()
        } catch (_: Throwable) { null }
    }

    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<SearchResultDisplayItem>>(emptyList()) }
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    val defaultSuggestions = remember {
        listOf(
            SearchResultDisplayItem("c_java", "Java Basics & OOP", "Complete ICSE Class 9-10 course", "Course", "curriculum/java"),
            SearchResultDisplayItem("c_python", "Python Fundamentals", "CBSE & ICSE programming foundation", "Course", "curriculum/python"),
            SearchResultDisplayItem("t_loops", "Loop Structures (For, While)", "Control flow & nested iterations", "Topic", "practice"),
            SearchResultDisplayItem("t_arrays", "1D & 2D Arrays", "Sorting, binary search, and matrix manipulation", "Topic", "practice")
        )
    }

    fun performSearch(query: String) {
        val q = query.trim()
        if (q.isBlank()) {
            searchResults = emptyList()
            isLoading = false
            return
        }

        isLoading = true
        coroutineScope.launch {
            try {
                val res = repo?.searchCatalog(q)?.getOrNull()
                val items = mutableListOf<SearchResultDisplayItem>()
                if (res != null) {
                    res.courses.forEach { c ->
                        items.add(SearchResultDisplayItem(c.id, c.title, c.description.ifBlank { "Course" }, "Course", "curriculum/${c.id}"))
                    }
                    res.topics.forEach { t ->
                        items.add(SearchResultDisplayItem(t.id, t.name, "Topic • ${t.tag}", "Topic", "practice"))
                    }
                    res.lessons.forEach { l ->
                        items.add(SearchResultDisplayItem(l.id, l.title, l.description.ifBlank { "Lesson" }, "Lesson", "video_lesson/${l.id}"))
                    }
                }
                searchResults = items
                ActivityLog.search(context, q, items.size)
            } catch (e: Exception) {
                searchResults = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            debounceJob?.cancel()
                            debounceJob = coroutineScope.launch {
                                delay(400)
                                performSearch(it)
                            }
                        },
                        placeholder = { Text("Search courses, topics, lessons...", fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    searchResults = emptyList()
                                }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            val displayedList = if (searchQuery.isBlank()) defaultSuggestions else searchResults

            if (searchQuery.isNotBlank() && displayedList.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No matching courses or topics found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Try searching 'Java', 'Python', 'Loops', or 'Functions'",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (searchQuery.isBlank()) {
                        item {
                            Text(
                                "Popular Searches",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(displayedList) { item ->
                        Box(modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 4.dp, y = 4.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(bs)
                            )
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        ActivityLog.log(
                                            context,
                                            "search_result_click",
                                            mapOf("query" to searchQuery, "id" to item.id, "title" to item.title, "category" to item.category)
                                        )
                                        if (!item.targetRoute.isNullOrBlank()) {
                                            onNavigate(item.targetRoute)
                                        }
                                    },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(2.dp, bb),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (item.category) {
                                                    "Course" -> Color(0xFF4F46E5).copy(alpha = 0.15f)
                                                    "Topic" -> Color(0xFF059669).copy(alpha = 0.15f)
                                                    else -> Color(0xFFD97706).copy(alpha = 0.15f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            when (item.category) {
                                                "Course" -> Icons.Filled.School
                                                "Topic" -> Icons.Filled.Lightbulb
                                                else -> Icons.Filled.PlayCircleFilled
                                            },
                                            contentDescription = null,
                                            tint = when (item.category) {
                                                "Course" -> Color(0xFF4F46E5)
                                                "Topic" -> Color(0xFF059669)
                                                else -> Color(0xFFD97706)
                                            },
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            item.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            item.subtitle,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            item.category,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
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
    }
}
