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
import androidx.compose.foundation.shape.CircleShape
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
import com.vastavik.computer.utils.ActivityLog

private val PrimaryIndigo = Color(0xFF2563EB)
private val AdminRed = Color(0xFFDC2626)
private val GithubBlack = Color(0xFF24292F)

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
    var showAdminConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    val coroutineScope = rememberCoroutineScope()

    // Security: block system back from the login screen.
    BackHandler(enabled = true) { /* swallow */ }

    // Google Sign-In
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
                val email = account?.email
                if (!idToken.isNullOrEmpty()) {
                    ActivityLog.log(context, "login_google_success", mapOf("email" to (email ?: "")))
                    viewModel.signInWithGoogle(idToken)
                } else if (email != null) {
                    // OAuth client not configured on Firebase but the user picked an account.
                    // Fall back to Firebase's built-in signInWithCredential on the email
                    // by requesting a token via the FirebaseAuth current user path.
                    ActivityLog.log(context, "login_google_no_idtoken_fallback", mapOf("email" to email))
                    // Try the credential path directly through the AuthViewModel.
                    val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken ?: "", null)
                    // No idToken means we cannot build a credential — surface a clear error.
                    android.widget.Toast.makeText(
                        context,
                        "Google sign-in is missing the OAuth client configuration. " +
                            "Set default_web_client_id in strings.xml to your Firebase Web client ID.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
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
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // ---- Computer / Brand icon (TAP = admin login) ----
            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .clickable {
                        ActivityLog.log(context, "login_admin_icon_tap", mapOf("source" to "login_screen"))
                        showAdminConfirm = true
                    }
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(bs)
                )
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(PrimaryIndigo)
                        .border(BorderStroke(2.dp, bb), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.LaptopChromebook,
                        contentDescription = "Vastavik - tap to login as Admin",
                        modifier = Modifier.size(42.dp),
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sign in to continue your learning journey",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ---- Email ----
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
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
            Spacer(modifier = Modifier.height(12.dp))

            // ---- Password ----
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
                    unfocusedBorderColor = bb,
                    focusedBorderColor = bb,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
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
            Spacer(modifier = Modifier.height(4.dp))

            // ---- Log In ----
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
                        if (email.isNotBlank() && password.isNotBlank()) {
                            ActivityLog.log(context, "login_email_submit", mapOf("email" to email.trim()))
                            viewModel.signIn(email.trim(), password.trim(), context)
                        } else {
                            android.widget.Toast.makeText(context, "Please enter your email and password", android.widget.Toast.LENGTH_SHORT).show()
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
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ---- Google ----
            Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bs)
                )
                Button(
                    onClick = { googleLauncher.launch(googleSignInClient.signInIntent) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(BorderStroke(2.dp, bb), RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1F1F1F)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
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
            Spacer(modifier = Modifier.height(10.dp))

            // ---- GitHub ----
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
                        ActivityLog.log(context, "login_github_clicked", emptyMap())
                        android.widget.Toast.makeText(
                            context,
                            "GitHub sign-in is being configured. Please use email login for now.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(BorderStroke(2.dp, bb), RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = GithubBlack, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Code,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Sign in with GitHub", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showAdminConfirm) {
        AlertDialog(
            onDismissRequest = { showAdminConfirm = false },
            title = { Text("Sign in as Admin?", fontWeight = FontWeight.ExtraBold) },
            text = { Text("You are about to sign in using the administrator account. This is reserved for the app owner.") },
            confirmButton = {
                TextButton(onClick = {
                    showAdminConfirm = false
                    ActivityLog.log(context, "login_admin_confirmed", mapOf("email" to AdminSession.ADMIN_EMAIL))
                    email = AdminSession.ADMIN_EMAIL
                    password = AdminSession.ADMIN_PASSWORD
                    viewModel.loginAsAdmin(context)
                }) {
                    Text("Continue", fontWeight = FontWeight.Bold, color = AdminRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminConfirm = false }) { Text("Cancel") }
            }
        )
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
