package com.vastavik.computer.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.ui.theme.VastavikColors
import com.vastavik.computer.ui.theme.neoShape
import com.vastavik.computer.ui.theme.neoCircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onNavigate: (String) -> Unit) {
    var name by remember { mutableStateOf("Student") }
    var email by remember { mutableStateOf("student@example.com") }
    var school by remember { mutableStateOf("Delhi Public School") }
    var studentClass by remember { mutableStateOf("10") }
    var board by remember { mutableStateOf("ICSE") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("profile") }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { onNavigate("profile") }) {
                        Text("Save", color = MaterialTheme.colorScheme.primary)
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
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
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = false
            )
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

            OutlinedTextField(
                value = studentClass,
                onValueChange = { studentClass = it },
                label = { Text("Class") },
                leadingIcon = { Icon(Icons.Filled.Grade, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = board,
                onValueChange = { board = it },
                label = { Text("Board") },
                leadingIcon = { Icon(Icons.Filled.AccountBalance, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onNavigate("profile") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = neoShape(16.dp)
            ) {
                Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
