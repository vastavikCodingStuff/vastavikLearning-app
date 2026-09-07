package com.vastavik.computer.ui.screens.onboarding

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.vastavik.computer.data.api.model.UpdateProfileRequest
import com.vastavik.computer.data.repository.VastavikApiRepository
import com.vastavik.computer.di.RepositoryEntryPoint
import com.vastavik.computer.ui.theme.neoShape
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSetupScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val apiRepository: VastavikApiRepository? = remember {
        try {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                RepositoryEntryPoint::class.java
            ).vastavikApiRepository()
        } catch (_: Exception) {
            null
        }
    }

    var name by remember { mutableStateOf("") }
    var studentClass by remember { mutableStateOf("") }
    var board by remember { mutableStateOf("ICSE") }
    var school by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var hobbies by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("Java") }
    var isLoading by remember { mutableStateOf(false) }

    val boards = listOf("ICSE", "CBSE", "West Bengal Board", "Others")

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        val savedName = prefs.getString("name", null)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (savedName != null && savedName.isNotBlank()) {
            name = savedName
        } else if (firebaseUser?.displayName != null && firebaseUser.displayName!!.isNotBlank()) {
            name = firebaseUser.displayName!!
        }
        val savedClass = prefs.getString("class", "") ?: ""
        if (savedClass.isNotBlank()) studentClass = savedClass

        val savedBoard = prefs.getString("board", "") ?: ""
        if (savedBoard.isNotBlank() && boards.contains(savedBoard)) {
            board = savedBoard
        }

        val savedSchool = prefs.getString("school", "") ?: ""
        if (savedSchool.isNotBlank()) school = savedSchool

        val savedDob = prefs.getString("dob", "") ?: ""
        if (savedDob.isNotBlank()) dob = savedDob

        val savedHobbies = prefs.getString("hobbies", "") ?: ""
        if (savedHobbies.isNotBlank()) hobbies = savedHobbies
    }

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
                "Tell us about yourself to personalize your learning journey.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(28.dp))

            // Full Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name *") },
                placeholder = { Text("e.g. Rahul Sharma") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Class
            OutlinedTextField(
                value = studentClass,
                onValueChange = { studentClass = it },
                label = { Text("Class *") },
                placeholder = { Text("e.g. 10 or Class 10") },
                leadingIcon = { Icon(Icons.Filled.Grade, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Board dropdown
            Text("Board *", fontWeight = FontWeight.W500, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(6.dp))
            var boardExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = boardExpanded,
                onExpandedChange = { boardExpanded = it }
            ) {
                OutlinedTextField(
                    value = board,
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = { Icon(Icons.Filled.AccountBalance, contentDescription = null) },
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

            // School
            OutlinedTextField(
                value = school,
                onValueChange = { school = it },
                label = { Text("School") },
                placeholder = { Text("e.g. St. Xavier's High School") },
                leadingIcon = { Icon(Icons.Filled.School, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Date of Birth
            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text("Date of Birth") },
                placeholder = { Text("DD/MM/YYYY") },
                leadingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Hobbies
            OutlinedTextField(
                value = hobbies,
                onValueChange = { hobbies = it },
                label = { Text("Hobbies") },
                placeholder = { Text("e.g. Coding, Robotics, Chess") },
                leadingIcon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    coroutineScope.launch {
                        // 1. Sync to Backend API
                        try {
                            apiRepository?.updateUserProfile(
                                UpdateProfileRequest(
                                    name = name.trim(),
                                    studentClass = studentClass.trim(),
                                    board = board,
                                    school = school.trim(),
                                    dob = dob.trim(),
                                    hobbies = hobbies.trim(),
                                    preferredLanguage = language
                                )
                            )
                        } catch (_: Exception) {}

                        // 2. Sync to Firebase Firestore
                        try {
                            val user = FirebaseAuth.getInstance().currentUser
                            if (user != null) {
                                val db = FirebaseFirestore.getInstance()
                                db.collection("users").document(user.uid).set(
                                    mapOf(
                                        "name" to name.trim(),
                                        "displayName" to name.trim(),
                                        "studentClass" to studentClass.trim(),
                                        "class" to studentClass.trim(),
                                        "board" to board,
                                        "school" to school.trim(),
                                        "dob" to dob.trim(),
                                        "dateOfBirth" to dob.trim(),
                                        "hobbies" to hobbies.trim(),
                                        "preferredLanguage" to language,
                                        "setupCompleted" to true
                                    ),
                                    SetOptions.merge()
                                )
                            }
                        } catch (_: Exception) {}

                        // 3. Sync to local SharedPreferences
                        val prefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
                        prefs.edit()
                            .putString("name", name.trim())
                            .putString("class", studentClass.trim())
                            .putString("board", board)
                            .putString("school", school.trim())
                            .putString("dob", dob.trim())
                            .putString("hobbies", hobbies.trim())
                            .putString("language", language)
                            .putBoolean("setup_done", true)
                            .apply()

                        isLoading = false
                        onNavigate("home")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = neoShape(16.dp),
                enabled = !isLoading && name.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
