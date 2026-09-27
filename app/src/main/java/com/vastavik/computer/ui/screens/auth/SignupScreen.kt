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
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor

private val PrimaryIndigo = Color(0xFF2563EB)

@Composable
fun SignupScreen(
    onNavigate: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val clerkOtp by viewModel.clerkOtpPrompt.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var obscurePassword by remember { mutableStateOf(true) }
    var obscureConfirmPassword by remember { mutableStateOf(true) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }
    var otpCode by remember { mutableStateOf("") }
    val context = LocalContext.current
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()

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
                        .background(bs)
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PrimaryIndigo)
                        .border(BorderStroke(2.dp, bb), RoundedCornerShape(16.dp)),
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
                text = "Create Account",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Start your learning journey today",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                    formError = null
                },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = bb,
                    focusedBorderColor = bb,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    errorBorderColor = Color(0xFFEF4444),
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = emailError != null,
                supportingText = emailError?.let { msg ->
                    { Text(msg, color = Color(0xFFEF4444)) }
                }
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                    formError = null
                },
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
                    unfocusedBorderColor = bb,
                    focusedBorderColor = bb,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    errorBorderColor = Color(0xFFEF4444),
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = passwordError != null,
                supportingText = passwordError?.let { msg ->
                    { Text(msg, color = Color(0xFFEF4444)) }
                }
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmError = null
                    formError = null
                },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { obscureConfirmPassword = !obscureConfirmPassword }) {
                        Icon(
                            imageVector = if (obscureConfirmPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Toggle confirm password"
                        )
                    }
                },
                visualTransformation = if (obscureConfirmPassword) PasswordVisualTransformation() else VisualTransformation.None,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = bb,
                    focusedBorderColor = bb,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    errorBorderColor = Color(0xFFEF4444),
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                supportingText = {
                    val mismatch = confirmPassword.isNotEmpty() && password != confirmPassword
                    if (mismatch || confirmError != null) {
                        Text(
                            text = confirmError ?: "Passwords do not match",
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (clerkOtp != null) {
                // ---- Clerk email verification (OTP) step ----
                Text(
                    text = "Verify your email",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Enter the 6-digit code sent to ${clerkOtp?.email}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = {
                        otpCode = it.filter { c -> c.isDigit() }.take(8)
                        formError = null
                    },
                    label = { Text("Email code") },
                    singleLine = true,
                    isError = formError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 5.dp, y = 5.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bs)
                    )
                    Button(
                        onClick = {
                            formError = null
                            viewModel.verifyClerkOtp(otpCode.trim())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(BorderStroke(2.dp, bb), RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        enabled = !uiState.isLoading && otpCode.trim().length >= 6
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Verify Email", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                formError?.let { msg ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = msg,
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Didn't get a code?",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Resend",
                        color = PrimaryIndigo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { viewModel.resendClerkOtp() }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Use a different email",
                        color = PrimaryIndigo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable {
                            otpCode = ""
                            viewModel.cancelClerkOtp()
                        }
                    )
                }
            } else {
            Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bs)
                )
                Button(
                    onClick = {
                        emailError = null
                        passwordError = null
                        confirmError = null
                        formError = null
                        val emailValue = email.trim()
                        val pwd = password.trim()
                        val confirmPwd = confirmPassword.trim()
                        val minPwd = if (viewModel.clerkEnabled) 8 else 6
                        when {
                            emailValue.isBlank() -> emailError = "Please enter your email"
                            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches() ->
                                emailError = "Please enter a valid email address"
                            pwd.isBlank() -> passwordError = "Please enter a password"
                            pwd.length < minPwd -> passwordError = "Password must be at least $minPwd characters"
                            pwd != confirmPwd -> confirmError = "Passwords do not match"
                            else -> {
                                if (viewModel.clerkEnabled) {
                                    viewModel.signUpWithClerk(emailValue, pwd)
                                } else {
                                    viewModel.signUp(emailValue, pwd, context = context)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(BorderStroke(2.dp, bb), RoundedCornerShape(12.dp)),
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
                        Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Inline server-side / validation error — never a floating notification.
            formError?.let { msg ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = msg,
                    color = Color(0xFFEF4444),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            } // end clerk OTP step / sign-up form
            Spacer(modifier = Modifier.height(28.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Already have an account?", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Log In",
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onNavigate("login") }
                )
            }
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.clearSuccess()
            onNavigate("user_setup")
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            formError = error
            viewModel.clearError()
        }
    }
}
