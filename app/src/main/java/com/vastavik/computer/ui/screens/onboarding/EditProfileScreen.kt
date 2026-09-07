package com.vastavik.computer.ui.screens.onboarding

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun EditProfileScreen(onNavigate: (String) -> Unit, onBack: () -> Unit = {}) {
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

    val boards = listOf("ICSE", "CBSE", "West Bengal Board", "Others")

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var studentClass by remember { mutableStateOf("") }
    var board by remember { mutableStateOf("ICSE") }
    var dob by remember { mutableStateOf("") }
    var hobbies by remember { mutableStateOf("") }
    var boardExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val isAdmin by com.vastavik.computer.utils.AdminSession.isAdmin.collectAsState()

    LaunchedEffect(Unit) {
        // 1. Load from local prefs and Firebase
        val prefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val adminNow = com.vastavik.computer.utils.AdminSession.isAdmin.value

        val rawName = prefs.getString("name", null) ?: firebaseUser?.displayName ?: ""
        name = com.vastavik.computer.utils.DisplayName.resolveForUser(rawName, adminNow)
        email = firebaseUser?.email ?: prefs.getString("email", "") ?: ""
        // Admin profile shows "Admin" with no student details.
        if (adminNow) {
            school = ""
            studentClass = ""
            board = "ICSE"
            dob = ""
            hobbies = ""
        } else {
            school = prefs.getString("school", "") ?: ""
            studentClass = prefs.getString("class", "") ?: ""
            val savedBoard = prefs.getString("board", "") ?: "ICSE"
            if (boards.contains(savedBoard)) {
                board = savedBoard
            }
            dob = prefs.getString("dob", "") ?: ""
            hobbies = prefs.getString("hobbies", "") ?: ""
        }

        // 2. Fetch latest from Backend API
        try {
            apiRepository?.getUserProfile()?.onSuccess { profile ->
                if (adminNow) {
                    name = com.vastavik.computer.utils.DisplayName.ADMIN
                    email = profile.email.ifBlank { email }
                } else {
                    if (profile.name.isNotBlank()) name = com.vastavik.computer.utils.DisplayName.resolve(profile.name)
                    if (profile.email.isNotBlank()) email = profile.email
                    if (!profile.school.isNullOrBlank()) school = profile.school
                    if (!profile.studentClass.isNullOrBlank()) studentClass = profile.studentClass
                    if (!profile.board.isNullOrBlank() && boards.contains(profile.board)) board = profile.board
                    if (!profile.dob.isNullOrBlank()) dob = profile.dob
                    if (!profile.hobbies.isNullOrBlank()) hobbies = profile.hobbies
                }
            }
        } catch (_: Exception) {}
    }

    val saveProfile: () -> Unit = {
        if (name.isBlank()) {
            Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
        } else {
            isSaving = true
            coroutineScope.launch {
                // 1. Update Backend API
                try {
                    apiRepository?.updateUserProfile(
                        UpdateProfileRequest(
                            name = name.trim(),
                            studentClass = studentClass.trim(),
                            board = board,
                            school = school.trim(),
                            dob = dob.trim(),
                            hobbies = hobbies.trim()
                        )
                    )
                } catch (_: Exception) {}

                // 2. Update Firebase Firestore
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
                                "hobbies" to hobbies.trim()
                            ),
                            SetOptions.merge()
                        )
                    }
                } catch (_: Exception) {}

                // 3. Update SharedPreferences
                val prefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("name", name.trim())
                    .putString("class", studentClass.trim())
                    .putString("board", board)
                    .putString("school", school.trim())
                    .putString("dob", dob.trim())
                    .putString("hobbies", hobbies.trim())
                    .apply()

                isSaving = false
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                onBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = saveProfile, enabled = !isSaving) {
                        Text("Save", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
            // Full Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name *") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Email (read-only)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (Linked Account)") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = false
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (!isAdmin) {

            // Class
            OutlinedTextField(
                value = studentClass,
                onValueChange = { studentClass = it },
                label = { Text("Class") },
                placeholder = { Text("e.g. 10 or Class 10") },
                leadingIcon = { Icon(Icons.Filled.Grade, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Board dropdown
            Text("Board", fontWeight = FontWeight.W500, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(6.dp))
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
                placeholder = { Text("e.g. Modern Public School") },
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
            Spacer(modifier = Modifier.height(32.dp))

            } else {
                // Admin: short info card explaining why no student fields show.
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Administrator profile",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "You are signed in as the app administrator. Class, board, school, " +
                                "date of birth and hobbies are student-only fields and are not shown here.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            Button(
                onClick = saveProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = neoShape(16.dp),
                enabled = !isSaving && name.isNotBlank()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

