package com.vastavik.codeoss

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var initialCode: String = ""
    private var initialLanguage: String = "Python"
    private var initialQuestion: String = ""
    private var initialAction: String = "EDIT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseIntent(intent)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF181818),
                    surface = Color(0xFF212121),
                    primary = Color(0xFF007ACC)
                )
            ) {
                CodeOssMainScreen(
                    initialCode = initialCode,
                    initialLanguage = initialLanguage,
                    initialQuestion = initialQuestion,
                    initialAction = initialAction
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        parseIntent(intent)
    }

    private fun parseIntent(intent: Intent?) {
        if (intent == null) return
        initialCode = intent.getStringExtra("extra_code") ?: ""
        initialLanguage = intent.getStringExtra("extra_language") ?: "Python"
        initialQuestion = intent.getStringExtra("extra_question") ?: ""
        initialAction = intent.getStringExtra("extra_action") ?: "EDIT"
    }
}

enum class CompanionTab {
    CODE_OSS,
    UBUNTU_TERMINAL
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CodeOssMainScreen(
    initialCode: String,
    initialLanguage: String,
    initialQuestion: String,
    initialAction: String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(if (initialAction == "RUN") CompanionTab.UBUNTU_TERMINAL else CompanionTab.CODE_OSS) }
    var currentCode by remember { mutableStateOf(initialCode) }
    var currentLanguage by remember { mutableStateOf(initialLanguage) }
    var showQuestionDialog by remember { mutableStateOf(false) }

    // Terminal State
    val terminalLogs = remember {
        mutableStateListOf(
            UbuntuTerminalEngine.TerminalOutput("🐧 Ubuntu 22.04 LTS (Minimal PRoot Environment)", isPrompt = true),
            UbuntuTerminalEngine.TerminalOutput("Vastavik CodeOSS & Headless Terminal Suite", isPrompt = true),
            UbuntuTerminalEngine.TerminalOutput("Type commands or tap 'Run Code' to execute in Ubuntu.\n", isPrompt = true)
        )
    }
    var terminalInput by remember { mutableStateOf("") }
    val commandHistory = remember { mutableStateListOf<String>() }
    var historyIndex by remember { mutableIntStateOf(-1) }
    var isExecuting by remember { mutableStateOf(false) }
    val terminalListState = rememberLazyListState()

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    fun runCurrentCode() {
        if (isExecuting) return
        isExecuting = true
        selectedTab = CompanionTab.UBUNTU_TERMINAL
        coroutineScope.launch {
            val file = UbuntuTerminalEngine.saveCodeToFile(context, currentCode, currentLanguage)
            val runCmd = UbuntuTerminalEngine.getRunCommandForLanguage(currentLanguage, file)
            terminalLogs.add(UbuntuTerminalEngine.TerminalOutput("root@ubuntu:~/workspace# $runCmd", isPrompt = true))
            terminalListState.animateScrollToItem(terminalLogs.size - 1)

            val exitCode = UbuntuTerminalEngine.executeCommand(context, runCmd) { line ->
                terminalLogs.add(line)
                coroutineScope.launch {
                    if (terminalLogs.size > 0) {
                        terminalListState.animateScrollToItem(terminalLogs.size - 1)
                    }
                }
            }
            terminalLogs.add(UbuntuTerminalEngine.TerminalOutput("[Process completed with exit code $exitCode]\n", isPrompt = true))
            isExecuting = false
            if (terminalLogs.size > 0) {
                terminalListState.animateScrollToItem(terminalLogs.size - 1)
            }
        }
    }

    LaunchedEffect(initialAction) {
        if (initialAction == "RUN" && currentCode.isNotBlank()) {
            runCurrentCode()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color(0xFF121212),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Terminal,
                            contentDescription = null,
                            tint = Color(0xFF007ACC),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Vastavik CodeOSS",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // Ubuntu Server Status Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF15803D).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "🐧 Ubuntu CLI",
                                color = Color(0xFF4ADE80),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Quick Run Action Button
                        Button(
                            onClick = { runCurrentCode() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isExecuting) Color(0xFF555555) else Color(0xFF15803D),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isExecuting) "Running..." else "Run", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (initialQuestion.isNotBlank()) {
                            IconButton(
                                onClick = { showQuestionDialog = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.HelpOutline,
                                    contentDescription = "Question",
                                    tint = Color(0xFF007ACC),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Tab Row: CodeOSS vs Ubuntu Terminal
                    TabRow(
                        selectedTabIndex = if (selectedTab == CompanionTab.CODE_OSS) 0 else 1,
                        containerColor = Color(0xFF181818),
                        contentColor = Color(0xFF007ACC)
                    ) {
                        Tab(
                            selected = selectedTab == CompanionTab.CODE_OSS,
                            onClick = { selectedTab = CompanionTab.CODE_OSS },
                            text = {
                                Text(
                                    "⚡ CodeOSS Editor",
                                    color = if (selectedTab == CompanionTab.CODE_OSS) Color(0xFF007ACC) else Color.Gray,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == CompanionTab.UBUNTU_TERMINAL,
                            onClick = { selectedTab = CompanionTab.UBUNTU_TERMINAL },
                            text = {
                                Text(
                                    "💻 Ubuntu Terminal",
                                    color = if (selectedTab == CompanionTab.UBUNTU_TERMINAL) Color(0xFF4ADE80) else Color.Gray,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Mobile Keyboard Row
            MobileKeyboardBar(
                onKey = { key ->
                    when (key) {
                        "ESC" -> webViewInstance?.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE))
                        "TAB" -> {
                            if (selectedTab == CompanionTab.UBUNTU_TERMINAL) {
                                terminalInput += "    "
                            } else {
                                webViewInstance?.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_TAB))
                            }
                        }
                        "UP" -> {
                            if (selectedTab == CompanionTab.UBUNTU_TERMINAL && commandHistory.isNotEmpty()) {
                                if (historyIndex < commandHistory.size - 1) {
                                    historyIndex++
                                    terminalInput = commandHistory[commandHistory.size - 1 - historyIndex]
                                }
                            } else {
                                webViewInstance?.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP))
                            }
                        }
                        "DOWN" -> {
                            if (selectedTab == CompanionTab.UBUNTU_TERMINAL) {
                                if (historyIndex > 0) {
                                    historyIndex--
                                    terminalInput = commandHistory[commandHistory.size - 1 - historyIndex]
                                } else if (historyIndex == 0) {
                                    historyIndex = -1
                                    terminalInput = ""
                                }
                            } else {
                                webViewInstance?.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN))
                            }
                        }
                        "CTRL+C" -> {
                            if (selectedTab == CompanionTab.UBUNTU_TERMINAL) {
                                terminalLogs.add(UbuntuTerminalEngine.TerminalOutput("^C", isPrompt = true))
                                isExecuting = false
                            }
                        }
                        else -> {
                            if (selectedTab == CompanionTab.UBUNTU_TERMINAL) {
                                terminalInput += key
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFF1E1E1E))
        ) {
            when (selectedTab) {
                CompanionTab.CODE_OSS -> {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.databaseEnabled = true
                                settings.useWideViewPort = true
                                settings.loadWithOverviewMode = true
                                settings.allowFileAccess = true

                                webViewClient = object : WebViewClient() {
                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        if (request?.url?.toString()?.contains("127.0.0.1") == true) {
                                            view?.loadDataWithBaseURL(
                                                null,
                                                generateFallbackHtml(currentCode, currentLanguage),
                                                "text/html",
                                                "UTF-8",
                                                null
                                            )
                                        }
                                    }
                                }

                                loadUrl("http://127.0.0.1:8080/?folder=/root/workspace")
                                webViewInstance = this
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                CompanionTab.UBUNTU_TERMINAL -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0C0C0C))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        // Quick Commands Header Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            QuickTerminalChip("▶ Run Solution") { runCurrentCode() }
                            QuickTerminalChip("ls -la") { terminalInput = "ls -la" }
                            QuickTerminalChip("pwd") { terminalInput = "pwd" }
                            QuickTerminalChip("uname -a") { terminalInput = "uname -a" }
                            QuickTerminalChip("python3 --version") { terminalInput = "python3 --version" }
                            QuickTerminalChip("clear") {
                                terminalLogs.clear()
                                terminalLogs.add(UbuntuTerminalEngine.TerminalOutput("🐧 Ubuntu Terminal (cleared)\n", isPrompt = true))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Terminal Output Console
                        LazyColumn(
                            state = terminalListState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color(0xFF0F0F0F), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            items(terminalLogs) { log ->
                                Text(
                                    text = log.text,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = when {
                                        log.isError -> Color(0xFFEF4444)
                                        log.isPrompt -> Color(0xFF4ADE80)
                                        else -> Color(0xFFE2E8F0)
                                    },
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Interactive Command Prompt Input
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1E1E1E),
                            border = BorderStroke(1.dp, Color(0xFF333333)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "root@ubuntu:~# ",
                                    color = Color(0xFF4ADE80),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedTextField(
                                    value = terminalInput,
                                    onValueChange = { terminalInput = it },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = {
                                        val cmd = terminalInput.trim()
                                        if (cmd.isNotBlank()) {
                                            commandHistory.add(cmd)
                                            historyIndex = -1
                                            terminalLogs.add(UbuntuTerminalEngine.TerminalOutput("root@ubuntu:~# $cmd", isPrompt = true))
                                            terminalInput = ""
                                            if (cmd == "clear") {
                                                terminalLogs.clear()
                                            } else {
                                                coroutineScope.launch {
                                                    isExecuting = true
                                                    UbuntuTerminalEngine.executeCommand(context, cmd) { line ->
                                                        terminalLogs.add(line)
                                                    }
                                                    isExecuting = false
                                                    if (terminalLogs.size > 0) {
                                                        terminalListState.animateScrollToItem(terminalLogs.size - 1)
                                                    }
                                                }
                                            }
                                        }
                                    })
                                )
                                IconButton(
                                    onClick = {
                                        val cmd = terminalInput.trim()
                                        if (cmd.isNotBlank()) {
                                            commandHistory.add(cmd)
                                            historyIndex = -1
                                            terminalLogs.add(UbuntuTerminalEngine.TerminalOutput("root@ubuntu:~# $cmd", isPrompt = true))
                                            terminalInput = ""
                                            if (cmd == "clear") {
                                                terminalLogs.clear()
                                            } else {
                                                coroutineScope.launch {
                                                    isExecuting = true
                                                    UbuntuTerminalEngine.executeCommand(context, cmd) { line ->
                                                        terminalLogs.add(line)
                                                    }
                                                    isExecuting = false
                                                    if (terminalLogs.size > 0) {
                                                        terminalListState.animateScrollToItem(terminalLogs.size - 1)
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Send,
                                        contentDescription = "Run Command",
                                        tint = Color(0xFF4ADE80),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showQuestionDialog) {
                AlertDialog(
                    onDismissRequest = { showQuestionDialog = false },
                    title = { Text("Problem Description", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text(initialQuestion, fontSize = 13.sp)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showQuestionDialog = false }) {
                            Text("Dismiss")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun QuickTerminalChip(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF222222),
        border = BorderStroke(0.5.dp, Color(0xFF3B82F6))
    ) {
        Text(
            text = text,
            color = Color(0xFF93C5FD),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun MobileKeyboardBar(onKey: (String) -> Unit) {
    val keys = listOf("ESC", "TAB", "CTRL+C", "{", "}", "[", "]", ";", ":", "=", "+", "-", "*", "/", "|", "~", "&", "$", "UP", "DOWN")

    Surface(
        color = Color(0xFF121212),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            keys.forEach { keyLabel ->
                Surface(
                    onClick = { onKey(keyLabel) },
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF2D2D2D),
                    border = BorderStroke(0.5.dp, Color(0xFF404040)),
                    modifier = Modifier.height(30.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = keyLabel,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

private fun generateFallbackHtml(code: String, language: String): String {
    val escapedCode = code.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { margin: 0; padding: 12px; background: #1e1e1e; color: #d4d4d4; font-family: monospace; }
                .banner { background: #252526; border-left: 4px solid #007acc; padding: 8px 12px; margin-bottom: 12px; border-radius: 4px; font-size: 12px; }
                textarea { width: 100%; height: 75vh; background: #181818; color: #9cdcfe; border: 1px solid #333; border-radius: 6px; padding: 10px; font-family: monospace; font-size: 13px; box-sizing: border-box; resize: none; }
            </style>
        </head>
        <body>
            <div class="banner">
                <strong>🐧 Ubuntu Terminal & CodeOSS Runtime</strong><br>
                Language: $language | Local Ubuntu Daemon starting on 127.0.0.1...
            </div>
            <textarea id="code" spellcheck="false">$escapedCode</textarea>
        </body>
        </html>
    """.trimIndent()
}
