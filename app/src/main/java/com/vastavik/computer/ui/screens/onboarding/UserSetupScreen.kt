package com.vastavik.computer.ui.screens.onboarding

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.ui.theme.VastavikColors
import com.vastavik.computer.ui.theme.neoShape
import com.vastavik.computer.ui.theme.neoCircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSetupScreen(onNavigate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var studentClass by remember { mutableStateOf("") }
    var board by remember { mutableStateOf("ICSE") }
    var school by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("Java") }

    val boards = listOf("ICSE", "CBSE", "State Board")
    val languages = listOf("Java", "Python", "C++", "JavaScript")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                "Set Up Your Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Tell us about yourself to personalize your experience.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = studentClass,
                onValueChange = { studentClass = it },
                label = { Text("Class") },
                leadingIcon = { Icon(Icons.Filled.Grade, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Board dropdown
            Text("Board", fontWeight = FontWeight.W500, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            var boardExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = boardExpanded,
                onExpandedChange = { boardExpanded = it }
            ) {
                OutlinedTextField(
                    value = board,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = boardExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = boardExpanded,
                    onDismissRequest = { boardExpanded = false }
                ) {
                    boards.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                board = option
                                boardExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = school,
                onValueChange = { school = it },
                label = { Text("School") },
                leadingIcon = { Icon(Icons.Filled.School, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Language dropdown
            Text("Preferred Language", fontWeight = FontWeight.W500, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            var langExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = langExpanded,
                onExpandedChange = { langExpanded = it }
            ) {
                OutlinedTextField(
                    value = language,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = langExpanded,
                    onDismissRequest = { langExpanded = false }
                ) {
                    languages.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                language = option
                                langExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onNavigate("home") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = neoShape(16.dp),
                enabled = name.isNotBlank() && studentClass.isNotBlank() && school.isNotBlank()
            ) {
                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
