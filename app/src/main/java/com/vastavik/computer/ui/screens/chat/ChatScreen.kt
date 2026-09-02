package com.vastavik.computer.ui.screens.chat

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import com.vastavik.computer.ui.theme.neoShape
import com.vastavik.computer.ui.theme.neoCircleShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.vastavik.computer.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import android.net.Uri

data class ChatMessage(val text: String, val isUser: Boolean)

private const val SYSTEM_PROMPT = """You are Vastavik AI, a friendly programming tutor for Indian school students (Class 5-12, CBSE/ICSE boards). You help students learn Java, Python, JavaScript, and SQL.

RULES:
- Only answer questions about programming, computers, and coding (Java, Python, JavaScript, SQL, algorithms, data structures, web development, app development)
- If asked about non-computer topics, politely say: "I can only help with programming and computer science questions!"
- If asked inappropriate or harmful questions, say: "I can only help with programming and computer science questions!"
- Keep answers CRISP and CLEAR — explain properly but don't over-explain
- Use simple language suitable for a school student
- Give code examples when helpful
- For Class 5-8 students: use very simple explanations with real-life analogies
- For Class 9-12 students: can include more technical depth
- Always be encouraging and supportive
- Format code with ```code blocks when showing examples"""

private fun callMistralApi(messages: List<ChatMessage>): String {
    val apiKey = BuildConfig.MISTRAL_API_KEY
    if (apiKey.isBlank()) return "Mistral API key not configured. Please add MISTRAL_API_KEY to local.properties."

    val url = URL("https://api.mistral.ai/v1/chat/completions")
    val conn = url.openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.setRequestProperty("Content-Type", "application/json")
    conn.setRequestProperty("Authorization", "Bearer $apiKey")
    conn.doOutput = true
    conn.connectTimeout = 30000
    conn.readTimeout = 30000

    val apiMessages = JSONArray()
    apiMessages.put(JSONObject().put("role", "system").put("content", SYSTEM_PROMPT))
    for (msg in messages) {
        apiMessages.put(JSONObject().put("role", if (msg.isUser) "user" else "assistant").put("content", msg.text))
    }

    val body = JSONObject().apply {
        put("model", "mistral-small-latest")
        put("messages", apiMessages)
        put("max_tokens", 1024)
        put("temperature", 0.3)
    }

    conn.outputStream.use { it.write(body.toString().toByteArray()) }

    val responseCode = conn.responseCode
    val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
    val response = stream.bufferedReader().use { it.readText() }

    return if (responseCode in 200..299) {
        val json = JSONObject(response)
        json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
    } else {
        "Error ($responseCode): ${JSONObject(response).optJSONObject("message")?.optString("message") ?: "Unknown error"}"
    }
}

@Composable
fun ChatScreen(onNavigate: (String) -> Unit) {
    val viewModel = remember { ChatViewModel.getInstance() }
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Voice input state
    var isVoiceMode by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var partialTranscript by remember { mutableStateOf("") }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    suspend fun askMistral(prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                callMistralApi(messages + ChatMessage(prompt, isUser = true))
            } catch (e: Exception) {
                "Error: ${'$'}{e.message}. Check your internet connection."
            }
        }
    }

    fun sendToAI(text: String) {
        if (text.isNotBlank() && !isLoading) {
            val userText = text.trim()
            viewModel.addMessage(ChatMessage(userText, isUser = true))
            inputText = ""
            isLoading = true
            coroutineScope.launch {
                try {
                    listState.animateScrollToItem(messages.lastIndex + 1)
                    val resp = askMistral(userText)
                    viewModel.addMessage(ChatMessage(resp, isUser = false))
                    listState.animateScrollToItem(messages.lastIndex)
                } finally { isLoading = false }
            }
        }
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            isVoiceMode = false
            return
        }
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListening = false
                }
                override fun onError(error: Int) {
                    isListening = false
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotEmpty()) {
                        sendToAI(text)
                    }
                    isVoiceMode = false
                    isListening = false
                    partialTranscript = ""
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotEmpty()) partialTranscript = text
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
        isListening = true
        partialTranscript = ""
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
    }

    // Permission launcher
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isVoiceMode = true
            startListening()
        }
    }

    // Launch voice mode
    LaunchedEffect(isVoiceMode) {
        if (isVoiceMode) {
            permLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        } else {
            speechRecognizer?.cancel()
            isListening = false
            partialTranscript = ""
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            // Top bar with "Vastavik AI Mistral Small" + New button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("Vastavik", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text("AI", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Text(
                        "Mistral Small",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .border(BorderStroke(1.5.dp, Color.Black), RoundedCornerShape(12.dp))
                        .clickable { viewModel.clearMessages() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("New", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Suggestion chips
            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf(
                    "Explain Code" to "Explain this Java code for Class 8: public class Hello { public static void main(String[] args){ System.out.println(\"hi\"); } }",
                    "Generate Quiz" to "Generate 3 MCQs about Python loops for Class 7 with 4 options each.",
                    "Find Bug" to "Help me find bug in this Python: for i in range(5) print(i)"
                )) { (label, prompt) ->
                    Surface(
                        onClick = {
                            if (!isLoading) {
                                viewModel.addMessage(ChatMessage(prompt, isUser = true))
                                isLoading = true
                                coroutineScope.launch {
                                    listState.animateScrollToItem(messages.lastIndex + 1)
                                    val resp = askMistral(prompt)
                                    viewModel.addMessage(ChatMessage(resp, isUser = false))
                                    isLoading = false
                                    listState.animateScrollToItem(messages.lastIndex)
                                }
                            }
                        },
                        shape = RoundedCornerShape(50.dp),
                        color = Color.White,
                        border = BorderStroke(2.dp, Color.Black),
                        shadowElevation = 0.dp
                    ) {
                        Text(
                            label,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }

            // Messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { message -> ChatBubbleRow(message, onNavigate) }
                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(modifier = Modifier.padding(14.dp)) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Thinking\u2026", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Voice Input Overlay
            if (isVoiceMode) {
                VoiceInputOverlay(
                    isListening = isListening,
                    partialTranscript = partialTranscript,
                    amplitude = if (isListening) 0.5f + 0.5f * kotlin.math.sin(System.currentTimeMillis() / 100.0).toFloat() else 0f,
                    onConfirm = {
                        if (partialTranscript.isNotEmpty()) {
                            sendToAI(partialTranscript)
                        }
                        isVoiceMode = false
                    },
                    onCancel = {
                        isVoiceMode = false
                    }
                )
            }

            // Input area - hidden when voice mode is active
            if (!isVoiceMode) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 0.dp,
                    border = BorderStroke(2.dp, Color.Black)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Ask anything...", fontSize = 14.sp, color = Color(0xFF94A3B8)) },
                            trailingIcon = {
                                if (inputText.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                            .border(BorderStroke(1.5.dp, Color.Black), CircleShape)
                                            .clickable { isVoiceMode = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Mic,
                                            contentDescription = "Voice input",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 42.dp, max = 130.dp),
                            shape = RoundedCornerShape(50.dp),
                            singleLine = false,
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        FilledIconButton(
                            onClick = {
                                if (inputText.isNotBlank() && !isLoading) {
                                    val userText = inputText.trim()
                                    viewModel.addMessage(ChatMessage(userText, isUser = true))
                                    inputText = ""
                                    isLoading = true
                                    coroutineScope.launch {
                                        try {
                                            listState.animateScrollToItem(messages.lastIndex + 1)
                                            val resp = askMistral(userText)
                                            viewModel.addMessage(ChatMessage(resp, isUser = false))
                                            listState.animateScrollToItem(messages.lastIndex)
                                        } finally { isLoading = false }
                                    }
                                }
                            },
                            enabled = inputText.isNotBlank() && !isLoading,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleRow(message: ChatMessage, onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(BorderStroke(1.5.dp, Color.Black), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(8.dp))
        }
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            color = if (message.isUser) Color(0xFF2563EB) else Color.White,
            border = BorderStroke(1.8.dp, Color.Black),
            shadowElevation = 0.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            if (message.isUser) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(12.dp),
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            } else {
                ParsedMarkdownText(text = message.text, modifier = Modifier, onNavigate = onNavigate)
            }
        }
        if (message.isUser) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A))
                    .border(BorderStroke(1.5.dp, Color.Black), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun VoiceInputOverlay(
    isListening: Boolean,
    partialTranscript: String,
    amplitude: Float,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val primaryBlue = Color(0xFF2563EB)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 0.dp,
        border = BorderStroke(2.dp, Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isListening) "Listening..." else "Tap the mic to speak",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color(0xFF0F172A)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Say everything you need to get done.",
                fontSize = 14.sp,
                color = Color(0xFF64748B)
            )
            Spacer(Modifier.height(24.dp))

            // Waveform visualizer
            WaveformVisualizer(
                isListening = isListening,
                amplitude = amplitude,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            Spacer(Modifier.height(20.dp))

            // Partial transcript
            if (partialTranscript.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Text(
                        text = partialTranscript,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        fontSize = 14.sp,
                        color = Color(0xFF334155),
                        maxLines = 3
                    )
                }
                Spacer(Modifier.height(20.dp))
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause / Cancel button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .border(BorderStroke(2.dp, Color.Black), CircleShape)
                        .clickable { onCancel() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Cancel",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Confirm button
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(primaryBlue)
                        .border(BorderStroke(2.5.dp, Color.Black), CircleShape)
                        .clickable {
                            if (partialTranscript.isNotEmpty()) onConfirm()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Confirm",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WaveformVisualizer(
    isListening: Boolean,
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    val primaryBlue = Color(0xFF2563EB)
    val barCount = 32

    val infinite = rememberInfiniteTransition(label = "audio")

    // A single phase that cycles 0..1, used to drive all bars
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // A global "impulse" that bounces 0.15..1 while listening
    val impulse by infinite.animateFloat(
        initialValue = 0.15f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening) 520 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "impulse"
    )

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(BorderStroke(2.dp, Color.Black), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        val totalW = size.width
        val barWidth = totalW / (barCount * 1.7f)
        val gapW = (totalW - barWidth * barCount) / (barCount - 1).coerceAtLeast(1)
        val maxH = size.height * 0.95f
        val cY = size.height / 2f

        for (i in 0 until barCount) {
            // Deterministic pseudo-random per-bar envelope using sin
            val envelope = (0.35f + 0.65f * kotlin.math.sin(i * 0.65f).toFloat().coerceIn(-1f, 1f)).coerceIn(0.2f, 1f)
            // Traveling wave: bars shift amplitude as phase advances
            val wave = (0.5f + 0.5f * kotlin.math.sin(i * 0.45f + phase * 2f * kotlin.math.PI.toFloat())).toFloat()
            // Combine impulse (mic level) with extra phase variation
            val base = if (isListening) impulse * (0.4f + 0.6f * wave) * envelope else 0.15f
            val barH = maxH * base.coerceIn(0.06f, 1f)
            val x = i * (barWidth + gapW)
            val topY = cY - barH / 2f

            drawRoundRect(
                color = primaryBlue,
                topLeft = Offset(x, topY),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2f)
            )
        }
    }
}

@Composable
fun ParsedMarkdownText(text: String, modifier: Modifier = Modifier, onNavigate: ((String) -> Unit)? = null) {
    val parts = text.split("`")
    val codeBlocks = mutableListOf<Pair<String, String>>()
    parts.forEachIndexed { index, part ->
        if (index % 2 == 1) {
            val lines = part.trim().lines()
            val lang = lines.firstOrNull()?.trim() ?: ""
            val codeLines = if (lines.size > 1) lines.drop(1) else listOf()
            val codeContent = codeLines.joinToString("\n")
            if (codeContent.isNotBlank()) codeBlocks.add(lang to codeContent)
        }
    }
    Column(modifier = modifier) {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                val lines = part.trim().lines()
                val language = lines.firstOrNull()?.trim() ?: ""
                val codeLines = if (lines.size > 1) lines.drop(1) else listOf()
                val codeContent = codeLines.joinToString("\n")
                val isFirstCode = codeBlocks.isNotEmpty() && codeContent == codeBlocks.first().second
                if (onNavigate != null && codeContent.isNotBlank() && isFirstCode) {
                    val allCode = codeBlocks.joinToString("\n\n") { it.second }
                    val ext = when (language.lowercase()) {
                        "python", "py" -> "py"
                        "javascript", "js" -> "js"
                        "sql" -> "sql"
                        else -> "java"
                    }
                    val filename = "code.$ext"
                    Surface(
                        onClick = {
                            val encoded = Uri.encode(allCode, "UTF-8")
                            onNavigate("code_editor?initialCode=$encoded&language=$language")
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary,
                        border = BorderStroke(1.5.dp, Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.OpenInFull, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("View in Editor", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(filename, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            }
                        }
                    }
                } else if (codeContent.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E1E2E),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (language.isNotEmpty()) {
                                Text(language, fontSize = 10.sp, color = Color.Gray)
                                Spacer(Modifier.height(8.dp))
                            }
                            codeLines.forEachIndexed { i, line ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "${'$'}{i + 1}",
                                        color = Color(0xFF858585),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                        modifier = Modifier
                                            .width(24.dp)
                                            .padding(end = 8.dp)
                                    )
                                    Text(
                                        text = highlightCode(if (line.isEmpty()) " " else line, language),
                                        color = Color(0xFFD4D4D4),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                if (part.trim().isNotEmpty()) {
                    Text(
                        text = parseBasicMarkdown(part.trim()),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun parseBasicMarkdown(text: String) = buildAnnotatedString {
    val lines = text.split("\n")
    for (i in lines.indices) {
        val line = lines[i]
        val isHeader3 = line.startsWith("### ")
        val isHeader2 = line.startsWith("## ")
        val isHeader1 = line.startsWith("# ")
        val style = when {
            isHeader1 -> SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
            isHeader2 -> SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)
            isHeader3 -> SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
            else -> null
        }
        val textToProcess = when {
            isHeader1 -> line.removePrefix("# ")
            isHeader2 -> line.removePrefix("## ")
            isHeader3 -> line.removePrefix("### ")
            else -> line
        }
        if (style != null) {
            withStyle(style) { parseInlineMarkdown(textToProcess, this@buildAnnotatedString) }
        } else {
            parseInlineMarkdown(textToProcess, this@buildAnnotatedString)
        }
        if (i < lines.size - 1) append("\n")
    }
}

private fun parseInlineMarkdown(text: String, builder: androidx.compose.ui.text.AnnotatedString.Builder) {
    val regex = Regex("\\*\\*(.*?)\\*\\*|`(.*?)`|\\*(.*?)\\*")
    var currentIndex = 0
    val matches = regex.findAll(text)
    for (match in matches) {
        builder.append(text.substring(currentIndex, match.range.first))
        when {
            match.groups[1] != null -> {
                builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(match.groups[1]!!.value) }
            }
            match.groups[2] != null -> {
                builder.withStyle(SpanStyle(background = Color(0x22888888), fontFamily = FontFamily.Monospace)) { append(match.groups[2]!!.value) }
            }
            match.groups[3] != null -> {
                builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(match.groups[3]!!.value) }
            }
        }
        currentIndex = match.range.last + 1
    }
    builder.append(text.substring(currentIndex))
}

private object ChatSyntaxColors {
    val keyword = Color(0xFFC586C0)
    val string = Color(0xFFCE9178)
    val number = Color(0xFFB5CEA8)
    val comment = Color(0xFF6A9955)
    val function = Color(0xFFDCDCAA)
    val type = Color(0xFF4EC9B0)
    val operator = Color(0xFFD4D4D4)
    val normal = Color(0xFFD4D4D4)
    val punctuation = Color(0xFF808080)
}

private val javaKeywords = setOf("abstract","assert","boolean","break","byte","case","catch","char","class","const","continue","default","do","double","else","enum","extends","final","finally","float","for","goto","if","implements","import","instanceof","int","interface","long","native","new","package","private","protected","public","return","short","static","strictfp","super","switch","synchronized","this","throw","throws","transient","try","void","volatile","while","true","false","null","var","record","sealed","permits","yield","instanceof")
private val pythonKeywords = setOf("and","as","assert","async","await","break","class","continue","def","del","elif","else","except","finally","for","from","global","if","import","in","is","lambda","nonlocal","not","or","pass","raise","return","try","while","with","yield","True","False","None","print","range","len","int","float","str","list","dict","set","tuple","input","open","type")
private val jsKeywords = setOf("abstract","arguments","async","await","boolean","break","byte","case","catch","char","class","const","continue","debugger","default","delete","do","double","else","enum","export","extends","final","finally","float","for","function","goto","if","implements","import","in","instanceof","int","interface","let","long","native","new","of","package","private","protected","public","return","short","static","super","switch","synchronized","this","throw","throws","transient","try","typeof","undefined","var","void","volatile","while","with","yield","true","false","null","console","document","Math","JSON","Promise","Array","Object","String","Number","Boolean")
private val sqlKeywords = setOf("SELECT","FROM","WHERE","INSERT","UPDATE","DELETE","CREATE","DROP","ALTER","TABLE","INDEX","VIEW","INTO","VALUES","SET","AND","OR","NOT","IN","LIKE","BETWEEN","JOIN","LEFT","RIGHT","INNER","OUTER","ON","AS","ORDER","BY","GROUP","HAVING","LIMIT","OFFSET","DISTINCT","COUNT","SUM","AVG","MIN","MAX","UNION","ALL","ANY","EXISTS","IS","NULL","PRIMARY","KEY","FOREIGN","REFERENCES","CONSTRAINT","CHECK","DEFAULT","AUTO_INCREMENT","VARCHAR","INT","INTEGER","TEXT","DATE","BOOLEAN").map { it.uppercase() }.toSet()

private fun highlightCode(code: String, language: String) = buildAnnotatedString {
    val keywords = when (language.lowercase()) {
        "python" -> pythonKeywords
        "javascript", "js" -> jsKeywords
        "sql" -> sqlKeywords
        else -> javaKeywords
    }
    val lang = language.uppercase()

    var i = 0
    while (i < code.length) {
        if (code[i] == '/' && i + 1 < code.length && code[i + 1] == '/') {
            val end = code.indexOf('\n', i).let { if (it == -1) code.length else it }
            withStyle(SpanStyle(color = ChatSyntaxColors.comment, fontStyle = FontStyle.Italic)) { append(code.substring(i, end)) }
            i = end
        } else if (code[i] == '/' && i + 1 < code.length && code[i + 1] == '*') {
            val end = code.indexOf("*/", i + 2).let { if (it == -1) code.length else it + 2 }
            withStyle(SpanStyle(color = ChatSyntaxColors.comment, fontStyle = FontStyle.Italic)) { append(code.substring(i, end)) }
            i = end
        } else if (code[i] == '#' && lang == "PYTHON") {
            val end = code.indexOf('\n', i).let { if (it == -1) code.length else it }
            withStyle(SpanStyle(color = ChatSyntaxColors.comment, fontStyle = FontStyle.Italic)) { append(code.substring(i, end)) }
            i = end
        } else if (code[i] == '"' || code[i] == '\'') {
            val quote = code[i]
            var j = i + 1
            while (j < code.length && code[j] != quote) {
                if (code[j] == '\\') j++
                j++
            }
            j = minOf(j + 1, code.length)
            withStyle(SpanStyle(color = ChatSyntaxColors.string)) { append(code.substring(i, j)) }
            i = j
        } else if (code[i].isDigit() && (i == 0 || !code[i - 1].isLetter())) {
            var j = i
            while (j < code.length && (code[j].isDigit() || code[j] == '.')) j++
            withStyle(SpanStyle(color = ChatSyntaxColors.number)) { append(code.substring(i, j)) }
            i = j
        } else if (code[i].isLetter() || code[i] == '_') {
            var j = i
            while (j < code.length && (code[j].isLetterOrDigit() || code[j] == '_')) j++
            val word = code.substring(i, j)
            val wordForLookup = if (language.lowercase() == "sql") word.uppercase() else word.lowercase()
            when {
                wordForLookup in keywords -> withStyle(SpanStyle(color = ChatSyntaxColors.keyword, fontWeight = FontWeight.Bold)) { append(word) }
                word[0].isUpperCase() && j < code.length && code[j] == '(' -> withStyle(SpanStyle(color = ChatSyntaxColors.function)) { append(word) }
                word[0].isUpperCase() -> withStyle(SpanStyle(color = ChatSyntaxColors.type)) { append(word) }
                j < code.length && code[j] == '(' -> withStyle(SpanStyle(color = ChatSyntaxColors.function)) { append(word) }
                else -> withStyle(SpanStyle(color = ChatSyntaxColors.normal)) { append(word) }
            }
            i = j
        } else if (code[i] in "+-*/%=!<>&|^~?:.") {
            withStyle(SpanStyle(color = ChatSyntaxColors.operator)) { append(code[i]) }
            i++
        } else if (code[i] in "(){}[];,") {
            withStyle(SpanStyle(color = ChatSyntaxColors.punctuation)) { append(code[i]) }
            i++
        } else {
            append(code[i])
            i++
        }
    }
}
