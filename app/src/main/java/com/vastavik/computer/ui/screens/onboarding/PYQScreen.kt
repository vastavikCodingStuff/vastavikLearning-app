package com.vastavik.computer.ui.screens.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.ui.theme.VastavikColors
import com.vastavik.computer.ui.theme.neoShape
import com.vastavik.computer.ui.theme.neoCircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PYQScreen(onNavigate: (String) -> Unit) {
    var selectedBoard by remember { mutableStateOf("All") }
    var selectedYear by remember { mutableStateOf("All") }

    val boards = listOf("All", "ICSE", "CBSE")
    val years = listOf("All", "2023", "2022", "2021", "2020")

    val pyqs = listOf(
        Triple("ICSE 2023", "Computer Science", "45 questions"),
        Triple("CBSE 2022", "Computer Science", "50 questions"),
        Triple("ICSE 2022", "Computer Applications", "40 questions"),
        Triple("CBSE 2021", "Computer Science", "48 questions")
    )

    val filteredPyqs = pyqs.filter { (title, _, _) ->
        (selectedBoard == "All" || title.contains(selectedBoard)) &&
        (selectedYear == "All" || title.contains(selectedYear))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Past Year Questions") },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("home") }) {
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
        Column(modifier = Modifier.padding(padding)) {
            // Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Board filter
                var boardExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = boardExpanded,
                    onExpandedChange = { boardExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedBoard,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Board") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = boardExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = boardExpanded,
                        onDismissRequest = { boardExpanded = false }
                    ) {
                        boards.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedBoard = option
                                    boardExpanded = false
                                }
                            )
                        }
                    }
                }

                // Year filter
                var yearExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = yearExpanded,
                    onExpandedChange = { yearExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedYear,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Year") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = yearExpanded,
                        onDismissRequest = { yearExpanded = false }
                    ) {
                        years.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedYear = option
                                    yearExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // PYQ list
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPyqs) { (title, subject, questions) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { },
                        shape = neoShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(neoShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Article,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                                Text("$subject \u2022 $questions", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
