package com.vastavik.computer.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.vastavik.computer.R
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor
import com.vastavik.computer.utils.AdminSession
import kotlinx.coroutines.launch

private val PrimaryIndigo = Color(0xFF2563EB)
private val AdminRed = Color(0xFFDC2626)

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
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    val coroutineScope = rememberCoroutineScope()

    // Security: block system back from the login screen so user cannot accidentally
    // back out into the foreground app (e.g. web search results).
    BackHandler(enabled = true) { /* swallow */ }

    val googleSignInClient: GoogleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (!idToken.isNullOrEmpty()) {
                    viewModel.signInWithGoogle(idToken)
                } else {
                    android.widget.Toast.makeText(context, "Google sign-in failed: no token", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                android.widget.Toast.makeText(context, "Google sign-in failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                text = "Welcome Back",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Sign in to continue your learning journey",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))

            // ===========================================================
            // Swipeable Auth Method Carousel
            // Page 0: Google Sign-In (initial)
            // Page 1: Email + Password (default existing form)
            // Page 2: Direct Admin Login (swipe to reveal)
            // ===========================================================
            var currentAuthPage by remember { mutableIntStateOf(0) }
            val authPagerState = androidx.compose.foundation.pager.rememberPagerState(initialPage = 0) { 3 }

            androidx.compose.runtime.LaunchedEffect(authPagerState.currentPage) {
                currentAuthPage = authPagerState.currentPage
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            ) {
                androidx.compose.foundation.pager.HorizontalPager(
                    state = authPagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = true
                ) { page ->
                    when (page) {
                        0 -> GoogleSignInPage(
                            onGoogleClick = { googleLauncher.launch(googleSignInClient.signInIntent) },
                            onEmailLogin = {
                                coroutineScope.launch {
                                    authPagerState.animateScrollToPage(1)
                                }
                            },
                            bb = bb,
                            bs = bs,
                            isLoading = uiState.isLoading
                        )
                        1 -> EmailLoginPage(
                            email = email,
                            password = password,
                            onEmailChange = { email = it },
                            onPasswordChange = { password = it },
                            obscurePassword = obscurePassword,
                            onToggleObscure = { obscurePassword = !obscurePassword },
                            onSubmit = {
                                if (email.isNotBlank() && password.isNotBlank()) {
                                    viewModel.signIn(email.trim(), password.trim(), context)
                                } else {
                                    android.widget.Toast.makeText(context, "Please enter your email and password", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            onForgotPassword = { onNavigate("forgot_password") },
                            bb = bb,
                            bs = bs,
                            isLoading = uiState.isLoading
                        )
                        2 -> AdminLoginPage(
                            onAdminClick = {
                                email = AdminSession.ADMIN_EMAIL
                                password = AdminSession.ADMIN_PASSWORD
                                viewModel.loginAsAdmin(context)
                            },
                            bb = bb,
                            bs = bs,
                            isLoading = uiState.isLoading
                        )
                    }
                }
            }

            // Page indicator dots
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { i ->
                    val active = i == currentAuthPage
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (active) 22.dp else 6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (active) PrimaryIndigo else MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = when (currentAuthPage) {
                    0 -> "Swipe left for email login • Swipe again for admin"
                    1 -> "Swipe left for admin login"
                    else -> "Swipe right to go back"
                },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Don't have an account?", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
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

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.clearSuccess()
            onNavigate("home")
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
}

@Composable
private fun GoogleSignInPage(
    onGoogleClick: () -> Unit,
    onEmailLogin: () -> Unit,
    bb: Color,
    bs: Color,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFF5F5F5))
                .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "G", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4285F4))
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            "Continue with Google",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Fastest way to sign in. We never post anything on your behalf.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 5.dp, y = 5.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bs)
            )
            Button(
                onClick = onGoogleClick,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(BorderStroke(2.dp, bb), RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1F1F1F)),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = PrimaryIndigo)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4285F4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("G", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Sign in with Google", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onEmailLogin) {
            Text("Use email & password instead", color = PrimaryIndigo, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun EmailLoginPage(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    obscurePassword: Boolean,
    onToggleObscure: () -> Unit,
    onSubmit: () -> Unit,
    onForgotPassword: () -> Unit,
    bb: Color,
    bs: Color,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = bb,
                focusedBorderColor = bb,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = onToggleObscure) {
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
                focusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )
        TextButton(
            onClick = onForgotPassword,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Forgot Password?", color = PrimaryIndigo, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 5.dp, y = 5.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bs)
            )
            Button(
                onClick = onSubmit,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(BorderStroke(2.dp, bb), RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun AdminLoginPage(
    onAdminClick: () -> Unit,
    bb: Color,
    bs: Color,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(AdminRed.copy(alpha = 0.1f))
                .border(BorderStroke(1.5.dp, AdminRed), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.AdminPanelSettings,
                contentDescription = null,
                tint = AdminRed,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            "Direct Admin Login",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Restricted to the app owner. Do not use unless you are the administrator.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 5.dp, y = 5.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bs)
            )
            Button(
                onClick = onAdminClick,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(BorderStroke(2.dp, AdminRed), RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = AdminRed),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Login as Admin", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        Spacer(Modifier.weight(1f))
    }
}

private val CircleShape = androidx.compose.foundation.shape.CircleShape
