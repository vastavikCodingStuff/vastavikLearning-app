package com.vastavik.computer.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val BgWhite = Color(0xFFF8FAFC)
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val PrimaryIndigo = Color(0xFF2563EB)
private val BorderBlack = Color.Black

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigate: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var obscurePassword by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.padding(bottom = 20.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BorderBlack)
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PrimaryIndigo)
                        .border(BorderStroke(2.dp, BorderBlack), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.LaptopChromebook,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }
            }

            Text(
                text = "Welcome Back",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Sign in to continue your learning journey",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = BorderBlack,
                    focusedBorderColor = BorderBlack,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { obscurePassword = !obscurePassword }) {
                        Icon(
                            imageVector = if (obscurePassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Toggle password"
                        )
                    }
                },
                visualTransformation = if (obscurePassword) PasswordVisualTransformation() else VisualTransformation.None,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = BorderBlack,
                    focusedBorderColor = BorderBlack,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            TextButton(
                onClick = { onNavigate("forgot_password") },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot Password?", color = PrimaryIndigo, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BorderBlack)
                )
                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            viewModel.signIn(email.trim(), password.trim())
                        } else {
                            onNavigate("home")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(BorderStroke(2.dp, BorderBlack), RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Don't have an account?", color = TextMuted, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Sign Up",
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onNavigate("signup") }
                )
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
}
