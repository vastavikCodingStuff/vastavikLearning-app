package com.vastavik.computer.ui.screens.quiz

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.vastavik.computer.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

private val BorderBlack = ComposeColor.Black
private val PrimaryBlue = ComposeColor(0xFF2563EB)

private fun parseMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else { append(text[i]); i++ }
                }
                text.startsWith("*", i) && (i + 1 < text.length && text[i + 1] != '*') -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else { append(text[i]); i++ }
                }
                text.startsWith("`", i) -> {
                    val end = text.indexOf("`", i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = ComposeColor(0xFF1E293B), color = ComposeColor(0xFF93C5FD))) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else { append(text[i]); i++ }
                }
                text.startsWith("- ", i) || text.startsWith("• ", i) -> {
                    val lineEnd = text.indexOf('\n', i).let { if (it == -1) text.length else it }
                    append("  • ")
                    var j = i + 2
                    while (j < lineEnd) { append(text[j]); j++ }
                    i = lineEnd
                    if (i < text.length && text[i] == '\n') i++
                }
                text[i] == '\n' -> { append('\n'); i++ }
                else -> { append(text[i]); i++ }
            }
        }
    }
}

private fun containsCode(text: String): Boolean {
    val codePatterns = listOf("class ", "void ", "System.", "public ", "static ", "String[]", "{", "}", "extends ", "implements ", "new ", "println", "def ", "function ", "return ", "if (", "for (", "while (")
    return codePatterns.any { text.contains(it) }
}

private fun openPdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No PDF viewer found. PDF saved to Downloads.", Toast.LENGTH_LONG).show()
    }
}

private fun saveAndOpenPdf(context: Context, questions: List<QuizQuestionData>, openAfterSave: Boolean = true): File? {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    var page = document.startPage(pageInfo)
    var canvas: Canvas = page.canvas
    val paint = Paint().apply { color = Color.BLACK; textSize = 12f; typeface = android.graphics.Typeface.DEFAULT }
    val boldPaint = Paint(paint).apply { isFakeBoldText = true; textSize = 14f }
    val titlePaint = Paint(paint).apply { isFakeBoldText = true; textSize = 18f }
    var y = 40f

    canvas.drawText("Vastavik Computer - Quiz Questions", 40f, y, titlePaint)
    y += 30f
    paint.color = Color.GRAY
    canvas.drawText("Generated on ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())}", 40f, y, paint)
    y += 30f
    paint.color = Color.BLACK

    questions.forEachIndexed { i, q ->
        if (y > 780f) {
            document.finishPage(page)
            val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, document.pages.size + 1).create()
            page = document.startPage(newPageInfo)
            canvas = page.canvas
            y = 40f
        }
        boldPaint.color = Color.BLACK
        canvas.drawText("Q${i + 1}. ${q.question}", 40f, y, boldPaint)
        y += 22f
        q.options.forEachIndexed { j, opt ->
            val marker = if (j == q.correctIndex) " ✓" else ""
            paint.color = if (j == q.correctIndex) Color.parseColor("#10B981") else Color.DKGRAY
            canvas.drawText("  ${('A' + j)}) $opt$marker", 50f, y, paint)
            y += 18f
        }
        y += 12f
    }

    document.finishPage(page)
    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val file = File(dir, "Vastavik_Quiz_${System.currentTimeMillis()}.pdf")
    FileOutputStream(file).use { document.writeTo(it) }
    document.close()

    if (openAfterSave) {
        openPdf(context, file)
    }
    return file
}

private fun saveReviewAndOpenPdf(
    context: Context,
    questions: List<QuizQuestionData>,
    userAnswers: Map<Int, Int>,
    explanations: Map<Int, String>
): File? {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    var page = document.startPage(pageInfo)
    var canvas: Canvas = page.canvas
    val paint = Paint().apply { color = Color.BLACK; textSize = 11f; typeface = android.graphics.Typeface.DEFAULT }
    val boldPaint = Paint(paint).apply { isFakeBoldText = true; textSize = 13f }
    val titlePaint = Paint(paint).apply { isFakeBoldText = true; textSize = 16f }
    val smallPaint = Paint(paint).apply { textSize = 10f }
    var y = 40f

    canvas.drawText("Vastavik Computer - Quiz Review", 40f, y, titlePaint)
    y += 25f
    val correct = userAnswers.entries.count { (idx, ans) -> ans == questions[idx].correctIndex }
    canvas.drawText("Score: $correct / ${questions.size}", 40f, y, boldPaint)
    y += 30f

    questions.forEachIndexed { i, q ->
        if (y > 720f) {
            document.finishPage(page)
            val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, document.pages.size + 1).create()
            page = document.startPage(newPageInfo)
            canvas = page.canvas
            y = 40f
        }
        val userAns = userAnswers[i] ?: -1
        val isCorrect = userAns == q.correctIndex

        boldPaint.color = if (isCorrect) Color.parseColor("#10B981") else Color.parseColor("#EF4444")
        val checkMark = if (isCorrect) " ✓" else " ✗"
        canvas.drawText("Q${i + 1}.$checkMark", 40f, y, boldPaint)
        y += 16f
        paint.color = Color.BLACK
        canvas.drawText(q.question, 50f, y, paint)
        y += 16f

        q.options.forEachIndexed { j, opt ->
            val marker = if (j == q.correctIndex) " [Correct]" else if (j == userAns && j != correct) " [Your answer]" else ""
            smallPaint.color = when { j == q.correctIndex -> Color.parseColor("#10B981"); j == userAns && j != q.correctIndex -> Color.parseColor("#EF4444"); else -> Color.DKGRAY }
            canvas.drawText("  ${('A' + j)}) $opt$marker", 55f, y, smallPaint)
            y += 14f
        }

        explanations[i]?.let { exp ->
            y += 4f
            smallPaint.color = Color.parseColor("#2563EB")
            val cleanExp = exp.replace("**", "").replace("*", "").replace("`", "")
            val words = cleanExp.split(" ")
            var line = "AI: "
            for (word in words) {
                if (smallPaint.measureText(line + word) > 490f) {
                    canvas.drawText(line.trim(), 55f, y, smallPaint)
                    y += 12f
                    line = "  $word "
                } else {
                    line += "$word "
                }
            }
            if (line.isNotBlank()) { canvas.drawText(line.trim(), 55f, y, smallPaint); y += 12f }
        }
        y += 10f
    }

    document.finishPage(page)
    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val file = File(dir, "Vastavik_QuizReview_${System.currentTimeMillis()}.pdf")
    FileOutputStream(file).use { document.writeTo(it) }
    document.close()

    openPdf(context, file)
    return file
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTakingScreen(
    quizId: String,
    onNavigate: (String) -> Unit = {}
) {
    var currentQuestion by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var showResult by remember { mutableStateOf(false) }
    var showReview by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Int, Int>() }
    val context = LocalContext.current

    val questions = remember { QuizManager.getQuestions().ifEmpty {
        listOf(
            QuizQuestionData("What is the keyword used to inherit a class in Java?", listOf("implements", "extends", "inherits", "derives"), 1),
            QuizQuestionData("Which is not a primitive data type?", listOf("int", "boolean", "String", "char"), 2),
            QuizQuestionData("Default value of int?", listOf("null", "0", "1", "undefined"), 1)
        )
    }}

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No questions generated. Go back and try again.", color = ComposeColor(0xFF0F172A))
        }
        return
    }

    if (showReview) {
        ReviewAnswersScreen(
            questions = questions,
            userAnswers = userAnswers,
            onBack = { showReview = false },
            onNavigate = onNavigate
        )
        return
    }

    if (showResult) {
        Scaffold(containerColor = ComposeColor(0xFFF8FAFC)) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(modifier = Modifier.padding(bottom = 20.dp)) {
                    Box(modifier = Modifier.matchParentSize().offset(x = 5.dp, y = 5.dp).clip(RoundedCornerShape(20.dp)).background(BorderBlack))
                    Box(
                        modifier = Modifier.size(96.dp).clip(RoundedCornerShape(20.dp)).background(PrimaryBlue).border(BorderStroke(2.dp, BorderBlack), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.EmojiEvents, contentDescription = null, modifier = Modifier.size(48.dp), tint = ComposeColor.White)
                    }
                }
                Text("Quiz Complete!", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = ComposeColor(0xFF0F172A))
                Spacer(modifier = Modifier.height(12.dp))
                Text("You scored $score out of ${questions.size}", fontSize = 18.sp, color = ComposeColor(0xFF64748B))
                Spacer(modifier = Modifier.height(20.dp))

                Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
                    Box(modifier = Modifier.matchParentSize().offset(x = 5.dp, y = 5.dp).clip(RoundedCornerShape(6.dp)).background(BorderBlack))
                    LinearProgressIndicator(
                        progress = { score.toFloat() / questions.size },
                        modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(6.dp)).border(BorderStroke(2.dp, BorderBlack), RoundedCornerShape(6.dp)),
                        color = PrimaryBlue,
                        trackColor = ComposeColor.White
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                ResultButton(text = "Back to Home", icon = Icons.Filled.Home, onClick = { onNavigate("home") })
                Spacer(modifier = Modifier.height(12.dp))
                ResultOutlinedButton(text = "Download Questions", icon = Icons.Filled.Download, onClick = { saveAndOpenPdf(context, questions) })
                Spacer(modifier = Modifier.height(12.dp))
                ResultOutlinedButton(text = "Review Answers", icon = Icons.Filled.Quiz, onClick = { showReview = true })
            }
        }
    } else {
        Scaffold(
            containerColor = ComposeColor(0xFFF8FAFC)
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Top bar: Close + "Vastavik Computer"
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = ComposeColor(0xFF0F172A))
                    }
                    Text("Vastavik", color = PrimaryBlue, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text("Computer", color = ComposeColor(0xFF0F172A), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                }

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Question ${currentQuestion + 1} of ${questions.size}", fontWeight = FontWeight.Bold, color = ComposeColor(0xFF0F172A))
                        Text("${((currentQuestion + 1).toFloat() / questions.size * 100).toInt()}%", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)) {
                        Box(modifier = Modifier.matchParentSize().offset(x = 4.dp, y = 4.dp).clip(RoundedCornerShape(4.dp)).background(BorderBlack))
                        LinearProgressIndicator(
                            progress = { (currentQuestion + 1).toFloat() / questions.size },
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(4.dp)).border(BorderStroke(1.5.dp, BorderBlack), RoundedCornerShape(4.dp)),
                            color = PrimaryBlue,
                            trackColor = ComposeColor.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val questionText = questions[currentQuestion].question
                    val hasCode = containsCode(questionText)

                    Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
                        Box(modifier = Modifier.matchParentSize().offset(x = 5.dp, y = 5.dp).clip(RoundedCornerShape(16.dp)).background(BorderBlack))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ComposeColor.White),
                            border = BorderStroke(2.dp, BorderBlack),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            if (hasCode) {
                                Column(modifier = Modifier.padding(16.dp).heightIn(min = 120.dp, max = 300.dp).verticalScroll(rememberScrollState())) {
                                    Text(questionText, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ComposeColor(0xFF0F172A), fontFamily = FontFamily.Monospace, lineHeight = 20.sp)
                                }
                            } else {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(questionText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ComposeColor(0xFF0F172A))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    questions[currentQuestion].options.forEachIndexed { index, option ->
                        val isSelected = selectedAnswer == index
                        Box(modifier = Modifier.padding(bottom = 6.dp)) {
                            Box(modifier = Modifier.matchParentSize().offset(x = 4.dp, y = 4.dp).clip(RoundedCornerShape(12.dp)).background(BorderBlack))
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { selectedAnswer = index },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) PrimaryBlue else ComposeColor.White
                                ),
                                border = BorderStroke(2.dp, BorderBlack),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) ComposeColor.White else ComposeColor(0xFFE2E8F0))
                                            .border(BorderStroke(1.dp, BorderBlack), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(('A' + index).toString(), color = if (isSelected) PrimaryBlue else ComposeColor(0xFF475569), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(option, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isSelected) ComposeColor.White else ComposeColor(0xFF0F172A))
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth().background(ComposeColor.White).border(BorderStroke(1.dp, BorderBlack.copy(alpha = 0.15f))).padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.padding(end = 4.dp, bottom = 4.dp).weight(1f)) {
                            Box(modifier = Modifier.matchParentSize().offset(x = 4.dp, y = 4.dp).clip(RoundedCornerShape(12.dp)).background(BorderBlack))
                            OutlinedButton(
                                onClick = {
                                    if (currentQuestion > 0) {
                                        val prevScore = userAnswers.entries.take(currentQuestion).count { (idx, ans) -> ans == questions[idx].correctIndex }
                                        score = prevScore
                                        currentQuestion--
                                        selectedAnswer = userAnswers[currentQuestion] ?: -1
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp).border(BorderStroke(2.dp, BorderBlack), RoundedCornerShape(12.dp)),
                                enabled = currentQuestion > 0,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = ComposeColor.White)
                            ) {
                                Text("Previous", fontWeight = FontWeight.Bold, color = ComposeColor(0xFF0F172A))
                            }
                        }
                        Box(modifier = Modifier.padding(end = 4.dp, bottom = 4.dp).weight(1f)) {
                            Box(modifier = Modifier.matchParentSize().offset(x = 4.dp, y = 4.dp).clip(RoundedCornerShape(12.dp)).background(BorderBlack))
                            Button(
                                onClick = {
                                    userAnswers[currentQuestion] = selectedAnswer
                                    if (currentQuestion < questions.lastIndex) {
                                        currentQuestion++
                                        selectedAnswer = userAnswers[currentQuestion] ?: -1
                                    } else {
                                        score = userAnswers.entries.count { (idx, ans) -> ans == questions[idx].correctIndex }
                                        showResult = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp).border(BorderStroke(2.dp, BorderBlack), RoundedCornerShape(12.dp)),
                                enabled = selectedAnswer >= 0,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(12.dp),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Text(if (currentQuestion == questions.lastIndex) "Submit" else "Next", fontWeight = FontWeight.Bold, color = ComposeColor.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
        Box(modifier = Modifier.matchParentSize().offset(x = 5.dp, y = 5.dp).clip(RoundedCornerShape(12.dp)).background(BorderBlack))
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(52.dp).border(BorderStroke(2.dp, BorderBlack), RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = ComposeColor.White)
            Spacer(Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Bold, color = ComposeColor.White)
        }
    }
}

@Composable
private fun ResultOutlinedButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
        Box(modifier = Modifier.matchParentSize().offset(x = 5.dp, y = 5.dp).clip(RoundedCornerShape(12.dp)).background(BorderBlack))
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(52.dp).border(BorderStroke(2.dp, BorderBlack), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = ComposeColor.White)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = ComposeColor(0xFF0F172A))
            Spacer(Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Bold, color = ComposeColor(0xFF0F172A))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewAnswersScreen(
    questions: List<QuizQuestionData>,
    userAnswers: Map<Int, Int>,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var explanations by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var reloadTrigger by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current

    LaunchedEffect(reloadTrigger) {
        isLoading = true
        val results = mutableMapOf<Int, String>()
        withContext(Dispatchers.IO) {
            questions.forEachIndexed { i, q ->
                val userAns = userAnswers[i] ?: -1
                val correct = q.correctIndex
                val userText = if (userAns >= 0) q.options[userAns] else "No answer"
                val correctText = q.options[correct]
                val prompt = """Explain in 2-3 short sentences why "$correctText" is the correct answer for: "${q.question}". The student chose "$userText". Be encouraging."""
                val explanation = callMistralBrief(prompt)
                results[i] = explanation
            }
        }
        explanations = results
        isLoading = false
    }

    Scaffold(
        containerColor = ComposeColor(0xFFF8FAFC)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding() * 0.4f, start = padding.calculateStartPadding(layoutDirection), end = padding.calculateEndPadding(layoutDirection), bottom = padding.calculateBottomPadding())) {
            // Top bar: Back + "Review Answers" + Reload + Download
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = ComposeColor(0xFF0F172A))
                }
                Text("Review Answers", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = ComposeColor(0xFF0F172A))
                Spacer(Modifier.weight(1f))
                if (!isLoading) {
                    IconButton(onClick = { reloadTrigger++ }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reload", tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = {
                        saveReviewAndOpenPdf(context, questions, userAnswers, explanations)
                    }) {
                        Icon(Icons.Filled.Download, contentDescription = "Download Review", tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryBlue)
                        Spacer(Modifier.height(16.dp))
                        Text("Generating explanations...", color = ComposeColor(0xFF64748B))
                    }
                }
            } else {
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
                    questions.forEachIndexed { i, q ->
                        val userAns = userAnswers[i] ?: -1
                        val correct = q.correctIndex
                        val isCorrect = userAns == correct

                        Box(modifier = Modifier.padding(end = 5.dp, bottom = 10.dp)) {
                            Box(modifier = Modifier.matchParentSize().offset(x = 5.dp, y = 5.dp).clip(RoundedCornerShape(14.dp)).background(BorderBlack))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = ComposeColor.White),
                                border = BorderStroke(2.dp, BorderBlack),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                            contentDescription = null,
                                            tint = if (isCorrect) ComposeColor(0xFF10B981) else ComposeColor(0xFFEF4444),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Q${i + 1}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ComposeColor(0xFF0F172A))
                                        Spacer(Modifier.weight(1f))
                                        Text(
                                            if (isCorrect) "Correct" else "Wrong",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isCorrect) ComposeColor(0xFF10B981) else ComposeColor(0xFFEF4444)
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(q.question, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ComposeColor(0xFF0F172A))

                                    Spacer(Modifier.height(10.dp))

                                    q.options.forEachIndexed { j, opt ->
                                        val bg = when {
                                            j == correct -> ComposeColor(0xFF10B981).copy(alpha = 0.15f)
                                            j == userAns && j != correct -> ComposeColor(0xFFEF4444).copy(alpha = 0.15f)
                                            else -> ComposeColor.Transparent
                                        }
                                        val textColor = when {
                                            j == correct -> ComposeColor(0xFF10B981)
                                            j == userAns && j != correct -> ComposeColor(0xFFEF4444)
                                            else -> ComposeColor(0xFF334155)
                                        }
                                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clip(RoundedCornerShape(8.dp)).background(bg)) {
                                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Text(('A' + j).toString(), color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(24.dp))
                                                Text(opt, color = textColor, fontSize = 13.sp)
                                                if (j == correct) { Spacer(Modifier.weight(1f)); Text("✓", color = ComposeColor(0xFF10B981), fontWeight = FontWeight.Bold) }
                                                if (j == userAns && j != correct) { Spacer(Modifier.weight(1f)); Text("✗", color = ComposeColor(0xFFEF4444), fontWeight = FontWeight.Bold) }
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(10.dp))

                                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(PrimaryBlue.copy(alpha = 0.08f)).border(BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f)), RoundedCornerShape(10.dp))) {
                                        Row(modifier = Modifier.padding(12.dp)) {
                                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(parseMarkdown(explanations[i] ?: "Loading..."), fontSize = 13.sp, color = ComposeColor(0xFF475569), lineHeight = 18.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
                        Box(modifier = Modifier.matchParentSize().offset(x = 5.dp, y = 5.dp).clip(RoundedCornerShape(12.dp)).background(BorderBlack))
                        Button(
                            onClick = { onNavigate("home") },
                            modifier = Modifier.fillMaxWidth().height(52.dp).border(BorderStroke(2.dp, BorderBlack), RoundedCornerShape(12.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Icon(Icons.Filled.Home, contentDescription = null, modifier = Modifier.size(20.dp), tint = ComposeColor.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Back to Home", fontWeight = FontWeight.Bold, color = ComposeColor.White)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun callMistralBrief(prompt: String): String {
    val apiKey = BuildConfig.MISTRAL_API_KEY
    if (apiKey.isBlank()) return "API key not configured."
    return try {
        val url = URL("https://api.mistral.ai/v1/chat/completions")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $apiKey")
        conn.doOutput = true
        conn.connectTimeout = 30000
        conn.readTimeout = 30000
        val body = JSONObject().apply {
            put("model", "mistral-small-latest")
            put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
            put("max_tokens", 150)
            put("temperature", 0.3)
        }
        conn.outputStream.use { it.write(body.toString().toByteArray()) }
        val responseCode = conn.responseCode
        val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
        val response = stream.bufferedReader().use { it.readText() }
        if (responseCode in 200..299) {
            JSONObject(response).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim()
        } else "Could not generate explanation."
    } catch (e: Exception) { "Explanation unavailable." }
}
