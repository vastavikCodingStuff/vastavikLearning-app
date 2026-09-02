package com.vastavik.computer.ui.screens.video

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.vastavik.computer.BuildConfig
import com.vastavik.computer.data.model.LessonModel
import com.vastavik.computer.ui.components.VastavikYouTubePlayer
import com.vastavik.computer.ui.theme.VastavikColors
import com.vastavik.computer.ui.theme.neoCircleShape
import com.vastavik.computer.ui.theme.neoShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class LessonComment(
    val author: String,
    val text: String,
    val timestamp: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoLessonScreen(
    lessonId: String,
    courseId: String,
    partId: String,
    subpartId: String,
    onNavigate: (String) -> Unit = {},
    viewModel: VideoLessonViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var liked by remember { mutableStateOf(false) }
    var disliked by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var comments by remember { mutableStateOf(mutableListOf<LessonComment>()) }

    val lesson by viewModel.lessonData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(lessonId, courseId, partId, subpartId) {
        viewModel.loadLesson(courseId, partId, subpartId, lessonId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lesson?.title ?: "Video Lesson", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ---- Vastavik branded player (unlisted YouTube, no YouTube logo) ----
            if (isLoading && lesson == null) {
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (error != null && lesson == null) {
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(error ?: "Failed to load", color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
            } else {
                VastavikYouTubePlayer(
                    youtubeUrl = lesson?.youtubeUrl,
                    youtubeVideoId = lesson?.youtubeVideoId?.takeIf { it.isNotBlank() },
                    startSeconds = (lesson?.youtubePositionSec ?: 0).toFloat(),
                    autoplay = true
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    lesson?.title ?: "Loading...",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                // duration + format badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!lesson?.duration.isNullOrBlank()) {
                        Surface(shape = neoShape(6.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                            Text(lesson!!.duration, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    lesson?.videoFormat?.let { fmt ->
                        Surface(shape = neoShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text(fmt.uppercase(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }
                    if (lesson?.isPremium == true) {
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = neoShape(6.dp), color = Color(0xFFFFB800).copy(alpha = 0.18f)) {
                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color(0xFFB77900))
                                Spacer(Modifier.width(3.dp))
                                Text("PREMIUM", fontSize = 10.sp, color = Color(0xFFB77900), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    lesson?.description ?: "Learn with Vastavik — unlisted video playback, no YouTube branding.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }

            // Like / Dislike / Comment row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { liked = !liked; if (liked) disliked = false },
                    shape = neoShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (liked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Icon(Icons.Filled.ThumbUp, contentDescription = "Like", tint = if (liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Like", fontSize = 13.sp, color = if (liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(12.dp))
                FilledTonalButton(
                    onClick = { disliked = !disliked; if (disliked) liked = false },
                    shape = neoShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (disliked) MaterialTheme.colorScheme.error.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Icon(Icons.Filled.ThumbDown, contentDescription = "Dislike", tint = if (disliked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Dislike", fontSize = 13.sp, color = if (disliked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.weight(1f))
                FilledTonalButton(
                    onClick = { showComments = true },
                    shape = neoShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Icon(Icons.Filled.Comment, contentDescription = "Comments", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("${comments.size}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(8.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Code", fontSize = 12.sp) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Whiteboard", fontSize = 12.sp) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Shorts", fontSize = 12.sp) })
            }

            when (selectedTab) {
                0 -> CodeNotesTab(lesson)
                1 -> WhiteboardTab(lesson?.whiteboardImageUrl)
                2 -> ShortsTab(lesson)
            }
        }

        if (showComments) {
            CommentsBottomSheet(
                comments = comments,
                onDismiss = { showComments = false },
                onPost = { newComment -> comments = (comments + newComment).toMutableList() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentsBottomSheet(
    comments: List<LessonComment>,
    onDismiss: () -> Unit,
    onPost: (LessonComment) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }
    var blockedMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = if (MaterialTheme.shapes.medium.toString().contains("0.0")) RoundedCornerShape(0.dp) else RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 300.dp, max = 500.dp)
                .padding(horizontal = 16.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.width(40.dp).height(4.dp),
                    shape = neoShape(2.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                ) {}
            }
            Spacer(Modifier.height(12.dp))
            Text("Comments", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                if (comments.isEmpty()) {
                    Text("No comments yet. Be the first!", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), textAlign = TextAlign.Center)
                } else {
                    comments.forEach { comment ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Box(
                                modifier = Modifier.size(32.dp).clip(neoCircleShape()).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(comment.author.take(1).uppercase(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(comment.author, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(comment.timestamp, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(comment.text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
            if (blockedMessage != null) {
                Surface(shape = neoShape(8.dp), color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Text(blockedMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it; blockedMessage = null },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add a comment...", fontSize = 13.sp) },
                    shape = neoShape(24.dp),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isPosting) {
                            isPosting = true
                            val textToPost = inputText
                            coroutineScope.launch {
                                val result = withContext(Dispatchers.IO) { moderateComment(textToPost) }
                                if (result.first) {
                                    onPost(LessonComment("You", textToPost, "Just now"))
                                    inputText = ""
                                    blockedMessage = null
                                } else {
                                    blockedMessage = result.second
                                }
                                isPosting = false
                            }
                        }
                    },
                    enabled = inputText.isNotBlank() && !isPosting,
                    modifier = Modifier.size(40.dp).clip(neoCircleShape()).background(if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Transparent)
                ) {
                    if (isPosting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                    } else {
                        Icon(Icons.Filled.Send, contentDescription = "Send", tint = if (inputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

private fun moderateComment(text: String): Pair<Boolean, String> {
    val apiKey = BuildConfig.MISTRAL_API_KEY
    if (apiKey.isBlank()) return Pair(true, "")
    return try {
        val url = URL("https://api.mistral.ai/v1/chat/completions")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $apiKey")
        conn.doOutput = true
        conn.connectTimeout = 20000
        conn.readTimeout = 20000
        val prompt = """You are a comment moderator for an educational app for Indian school students (Class 5-12). Review this comment: "$text"
Rules:
- Allow comments that are helpful, encouraging, or ask genuine questions
- BLOCK comments that are: spam, inappropriate, hateful, trolling, off-topic, contain profanity, or are deliberately unhelpful/misleading
- Be strict but fair
Reply ONLY with JSON: {"pass": true/false, "reason": "brief reason if blocked"}"""
        val body = JSONObject().apply {
            put("model", "mistral-small-latest")
            put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
            put("max_tokens", 100)
            put("temperature", 0.1)
        }
        conn.outputStream.use { it.write(body.toString().toByteArray()) }
        val responseCode = conn.responseCode
        val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
        val response = stream.bufferedReader().use { it.readText() }
        if (responseCode in 200..299) {
            val content = JSONObject(response).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim()
            val cleaned = content.replace(Regex("```json\\s*"), "").replace(Regex("```\\s*"), "").trim()
            val json = JSONObject(cleaned)
            val pass = json.optBoolean("pass", true)
            val reason = json.optString("reason", "")
            if (pass) Pair(true, "") else Pair(false, "Comment blocked: $reason")
        } else Pair(true, "")
    } catch (e: Exception) { Pair(true, "") }
}

@Composable
private fun CodeNotesTab(lesson: LessonModel?) {
    Column(modifier = Modifier.padding(16.dp)) {
        if (!lesson?.codeSample.isNullOrBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = neoShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VastavikColors.CodeBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Code Sample", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(lesson!!.codeSample, color = VastavikColors.CodeText, fontSize = 13.sp, lineHeight = 20.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = neoShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VastavikColors.CodeBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Code Sample", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = """class Animal {
    String name;
    void speak() {
        System.out.println(name + " makes a sound");
    }
}
class Dog extends Animal {
    void speak() {
        System.out.println(name + " barks");
    }
}""",
                        color = VastavikColors.CodeText,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = neoShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Notes", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    lesson?.notes?.takeIf { it.isNotBlank() } ?: "Inheritance allows a class to inherit properties and methods from another class. The extends keyword is used to establish inheritance.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun WhiteboardTab(whiteboardImageUrl: String?) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 5f)
        offsetX += panChange.x
        offsetY += panChange.y
    }
    Box(
        modifier = Modifier.fillMaxWidth().height(350.dp).padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = neoShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().transformable(state = transformState).graphicsLayer {
                    scaleX = scale; scaleY = scale; translationX = offsetX; translationY = offsetY
                },
                contentAlignment = Alignment.Center
            ) {
                if (!whiteboardImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = whiteboardImageUrl,
                        contentDescription = "Whiteboard",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Draw, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Whiteboard content will appear here", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 13.sp)
                    }
                }
            }
        }
        Surface(
            shape = neoShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ZoomIn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                Text("Pinch to zoom", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ShortsTab(lesson: LessonModel?) {
    val isShort = lesson?.videoFormat == "short"
    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (isShort && !lesson?.youtubeUrl.isNullOrBlank()) {
            // Reuse same player but in vertical aspect — 9:16 short
            Box(
                modifier = Modifier.width(200.dp).height(360.dp).clip(neoShape(16.dp)).background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                VastavikYouTubePlayer(
                    youtubeUrl = lesson!!.youtubeUrl,
                    youtubeVideoId = lesson.youtubeVideoId.takeIf { it.isNotBlank() },
                    startSeconds = 0f,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Box(
                modifier = Modifier.width(200.dp).height(360.dp).clip(neoShape(16.dp)).background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (isShort) "Short loading..." else "Short 1-2 min vertical", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(shape = neoShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp)) {
                Text("Quick Revision", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    lesson?.description?.takeIf { it.isNotBlank() } ?: "1-2 min fast explanation — perfect for revision before exams.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
