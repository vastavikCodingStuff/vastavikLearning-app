package com.vastavik.computer.ui.screens.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.automirrored.filled.Help
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.vastavik.computer.utils.VastavikAi
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.ui.theme.VastavikColors
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.utils.Judge0Service
import kotlinx.coroutines.launch

private val mono = FontFamily.Monospace

private object SyntaxColors {
    val keyword = Color(0xFFC586C0)   // purple
    val string = Color(0xFFCE9178)    // orange
    val number = Color(0xFFB5CEA8)    // green
    val comment = Color(0xFF6A9955)   // dim green
    val function = Color(0xFFDCDCAA)  // yellow
    val type = Color(0xFF4EC9B0)      // teal
    val operator = Color(0xFFD4D4D4)  // white
    val normal = Color(0xFFD4D4D4)    // default
    val punctuation = Color(0xFF808080) // gray
}

private val javaKeywords = setOf("abstract","assert","boolean","break","byte","case","catch","char","class","const","continue","default","do","double","else","enum","extends","final","finally","float","for","goto","if","implements","import","instanceof","int","interface","long","native","new","package","private","protected","public","return","short","static","strictfp","super","switch","synchronized","this","throw","throws","transient","try","void","volatile","while","true","false","null","var","record","sealed","permits","yield","instanceof")
private val pythonKeywords = setOf("and","as","assert","async","await","break","class","continue","def","del","elif","else","except","finally","for","from","global","if","import","in","is","lambda","nonlocal","not","or","pass","raise","return","try","while","with","yield","True","False","None","print","range","len","int","float","str","list","dict","set","tuple","input","open","type")
private val jsKeywords = setOf("abstract","arguments","async","await","boolean","break","byte","case","catch","char","class","const","continue","debugger","default","delete","do","double","else","enum","export","extends","final","finally","float","for","function","goto","if","implements","import","in","instanceof","int","interface","let","long","native","new","of","package","private","protected","public","return","short","static","super","switch","synchronized","this","throw","throws","transient","try","typeof","undefined","var","void","volatile","while","with","yield","true","false","null","console","document","Math","JSON","Promise","Array","Object","String","Number","Boolean")
private val sqlKeywords = setOf("SELECT","FROM","WHERE","INSERT","UPDATE","DELETE","CREATE","DROP","ALTER","TABLE","INDEX","VIEW","INTO","VALUES","SET","AND","OR","NOT","IN","LIKE","BETWEEN","JOIN","LEFT","RIGHT","INNER","OUTER","ON","AS","ORDER","BY","GROUP","HAVING","LIMIT","OFFSET","DISTINCT","COUNT","SUM","AVG","MIN","MAX","UNION","ALL","ANY","EXISTS","IS","NULL","PRIMARY","KEY","FOREIGN","REFERENCES","CONSTRAINT","CHECK","DEFAULT","AUTO_INCREMENT","VARCHAR","INT","INTEGER","TEXT","DATE","BOOLEAN").map { it.uppercase() }.toSet()

private fun highlightCode(code: String, language: String) = buildAnnotatedString {
    val keywords = when (language) {
        "Java" -> javaKeywords
        "Python" -> pythonKeywords
        "JavaScript" -> jsKeywords
        "SQL" -> sqlKeywords
        else -> javaKeywords
    }
    val lang = language.uppercase()

    var i = 0
    while (i < code.length) {
        // Single-line comment
        if (code[i] == '/' && i + 1 < code.length && code[i + 1] == '/') {
            val end = code.indexOf('\n', i).let { if (it == -1) code.length else it }
            withStyle(SpanStyle(color = SyntaxColors.comment, fontStyle = FontStyle.Italic)) { append(code.substring(i, end)) }
            i = end
        }
        // Multi-line comment
        else if (code[i] == '/' && i + 1 < code.length && code[i + 1] == '*') {
            val end = code.indexOf("*/", i + 2).let { if (it == -1) code.length else it + 2 }
            withStyle(SpanStyle(color = SyntaxColors.comment, fontStyle = FontStyle.Italic)) { append(code.substring(i, end)) }
            i = end
        }
        // Hash comment (Python)
        else if (code[i] == '#' && lang == "PYTHON") {
            val end = code.indexOf('\n', i).let { if (it == -1) code.length else it }
            withStyle(SpanStyle(color = SyntaxColors.comment, fontStyle = FontStyle.Italic)) { append(code.substring(i, end)) }
            i = end
        }
        // Strings
        else if (code[i] == '"' || code[i] == '\'') {
            val quote = code[i]
            var j = i + 1
            while (j < code.length && code[j] != quote) {
                if (code[j] == '\\') j++
                j++
            }
            j = minOf(j + 1, code.length)
            withStyle(SpanStyle(color = SyntaxColors.string)) { append(code.substring(i, j)) }
            i = j
        }
        // Numbers
        else if (code[i].isDigit() && (i == 0 || !code[i - 1].isLetter())) {
            var j = i
            while (j < code.length && (code[j].isDigit() || code[j] == '.')) j++
            withStyle(SpanStyle(color = SyntaxColors.number)) { append(code.substring(i, j)) }
            i = j
        }
        // Words (keywords, types, functions)
        else if (code[i].isLetter() || code[i] == '_') {
            var j = i
            while (j < code.length && (code[j].isLetterOrDigit() || code[j] == '_')) j++
            val word = code.substring(i, j)
            val wordForLookup = if (language == "SQL") word.uppercase() else word.lowercase()
            when {
                wordForLookup in keywords -> withStyle(SpanStyle(color = SyntaxColors.keyword, fontWeight = FontWeight.Bold)) { append(word) }
                word[0].isUpperCase() && j < code.length && code[j] == '(' -> withStyle(SpanStyle(color = SyntaxColors.function)) { append(word) }
                word[0].isUpperCase() -> withStyle(SpanStyle(color = SyntaxColors.type)) { append(word) }
                j < code.length && code[j] == '(' -> withStyle(SpanStyle(color = SyntaxColors.function)) { append(word) }
                else -> withStyle(SpanStyle(color = SyntaxColors.normal)) { append(word) }
            }
            i = j
        }
        // Operators
        else if (code[i] in "+-*/%=!<>&|^~?:.") {
            withStyle(SpanStyle(color = SyntaxColors.operator)) { append(code[i]) }
            i++
        }
        // Punctuation
        else if (code[i] in "(){}[];,") {
            withStyle(SpanStyle(color = SyntaxColors.punctuation)) { append(code[i]) }
            i++
        }
        // Whitespace / other
        else {
            append(code[i])
            i++
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeEditorScreen(@Suppress("UNUSED_PARAMETER") onNavigate: (String)->Unit, onBack: () -> Unit = {}, initialCode: String = "", initialLanguage: String = "Python", @Suppress("UNUSED_PARAMETER") initialQuestion: String = "") {
    var language by remember { mutableStateOf(initialLanguage.ifBlank { "Python" }) }
    var code by remember { mutableStateOf(initialCode.ifBlank { defaultCode(language) }) }
    var output by remember { mutableStateOf("") }
    var stdin by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(true) }
    var executionMeta by remember { mutableStateOf("") }
    var question by remember { mutableStateOf(initialQuestion) }
    var showQuestion by remember { mutableStateOf(initialQuestion.isNotBlank()) }
    var isWordWrap by remember { mutableStateOf(false) }
    var aiPromptText by remember { mutableStateOf("") }
    var isGeneratingAiQuestion by remember { mutableStateOf(false) }
    var isEnhancingQuestion by remember { mutableStateOf(false) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                aiPromptText = spoken
            }
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = androidx.compose.ui.platform.LocalContext.current
    var showRunInputDialog by remember { mutableStateOf(false) }

    fun executeCode() {
        if (isRunning) return
        val languageId = Judge0Service.languageIdFor(language)
        if (languageId == null) {
            output = "Judge0 execution is not supported for $language. Please select Java, Python, or JavaScript."
            isSuccess = false
            executionMeta = "Unsupported language"
            return
        }
        if (code.isBlank()) {
            output = "No source code to run."
            isSuccess = false
            executionMeta = "Failed"
            return
        }
        isRunning = true; output = ""; executionMeta = ""; isSuccess = true
        coroutineScope.launch {
            val result = Judge0Service.runCode(languageId, code, stdin)
            output = result.output.ifBlank { if (result.success) "(program ran with no output)" else result.statusDescription }
            isSuccess = result.success
            val parts = mutableListOf<String>()
            if (result.statusDescription.isNotBlank()) parts.add(result.statusDescription)
            result.executionTime?.let { if (it.isNotBlank() && it != "null") parts.add("${it}s") }
            result.memoryKb?.let { parts.add("${it} KB") }
            executionMeta = parts.joinToString(" • ")
            isRunning = false
        }
    }

    fun expandQuestionWithAi(targetQuestion: String) {
        if (targetQuestion.isBlank()) return
        isEnhancingQuestion = true
        coroutineScope.launch {
            try {
                val prompt = """
                    You are a senior computer science teacher for Indian school students (Class 9-12 ICSE/CBSE).
                    Expand this problem title: "$targetQuestion" into a comprehensive structured coding challenge for language $language.
                    You MUST strictly structure your output with the following 4 distinct numbered sections:

                    1. Question
                    [Provide a clear, detailed problem statement, definitions, requirements and constraints for $targetQuestion]

                    2. Explanation
                    [Explain the core logic, concepts, mathematical foundations, and approach needed to solve this]

                    3. Input / Output
                    [Provide 2 to 3 concrete sample test cases with sample input, expected output, and step-by-step walkthrough]

                    4. Algorithm
                    [Provide clear, step-by-step numbered algorithmic steps to solve this problem]
                """.trimIndent()
                val expanded = VastavikAi.chat(prompt)
                if (expanded.isNotBlank()) {
                    question = expanded
                }
            } catch (_: Exception) {}
            isEnhancingQuestion = false
        }
    }

    LaunchedEffect(initialQuestion) {
        if (question.isNotBlank()) {
            val sections = parseProblemSections(question)
            if (sections.size < 2) {
                expandQuestionWithAi(question)
            }
        }
    }

    val vScrollState = rememberScrollState()
    val hScrollState = rememberScrollState()

    val lines = remember(code) { code.split("\n") }
    val lineCount = lines.size
    val lineHeightSp = 22.sp
    val fontSizeSp = 13.sp
    val lineNumbersString = remember(lineCount) {
        (1..lineCount).joinToString("\n")
    }

    LaunchedEffect(language) {
        if (initialCode.isBlank() && (code==defaultCode("Java")||code==defaultCode("Python")||code==defaultCode("JavaScript")||code==defaultCode("SQL")))
            code = defaultCode(language)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Code Editor", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { onBack() }) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    // Question Icon - Gray when empty with Toast feedback, active when question present
                    val hasQuestion = question.isNotBlank()
                    IconButton(onClick = {
                        if (!hasQuestion) {
                            Toast.makeText(context, "Question isn't available over here!", Toast.LENGTH_SHORT).show()
                        } else {
                            showQuestion = !showQuestion
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Help,
                            contentDescription = "View question",
                            tint = if (!hasQuestion) Color.Gray else if (showQuestion) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        TextButton(onClick = { expanded = true }) { Text(language, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            listOf("Java","Python","JavaScript","SQL").forEach { lang ->
                                DropdownMenuItem(text = { Text(lang) }, onClick = { language = lang; expanded = false })
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (output.isNotEmpty() || isRunning || executionMeta.isNotEmpty()) {
                Surface(shadowElevation = 8.dp, shape = if (MaterialTheme.shapes.medium.toString().contains("0.0")) RoundedCornerShape(0.dp) else RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 280.dp).padding(16.dp).verticalScroll(rememberScrollState())) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Output", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            if (executionMeta.isNotEmpty() && !isRunning) {
                                Surface(shape = RoundedCornerShape(8.dp), color = if (isSuccess) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)) {
                                    Text(executionMeta, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isSuccess) Color(0xFF15803D) else Color(0xFFB91C1C), fontFamily = mono, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                            }
                            IconButton(onClick = { output = ""; executionMeta = "" }, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Close, contentDescription = "Clear") }
                        }
                        if (isRunning) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top=8.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Running on Judge0...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            Text(output.ifEmpty { if (executionMeta.isNotEmpty()) "(no output)" else "" }, fontFamily = mono, fontSize = 13.sp, color = if (isSuccess) MaterialTheme.colorScheme.onSurface else Color(0xFFB91C1C), modifier = Modifier.fillMaxWidth().padding(top=8.dp))
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (isRunning) return@ExtendedFloatingActionButton
                    val requiresInput = code.contains("Scanner") ||
                            code.contains("BufferedReader") ||
                            code.contains("System.in") ||
                            code.contains("input(") ||
                            code.contains("cin") ||
                            code.contains("readline") ||
                            code.contains("prompt(")
                    if (requiresInput && stdin.isBlank()) {
                        showRunInputDialog = true
                    } else {
                        executeCode()
                    }
                },
                icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
                text = { Text("Run") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Program Input (stdin) Dialog - prompted on Run or from toolbar
            if (showRunInputDialog) {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { showRunInputDialog = false },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .wrapContentHeight()
                            .padding(end = 4.dp, bottom = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 4.dp, y = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(brutalBorderColor())
                        )
                        Surface(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(2.dp, brutalBorderColor())
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Program Input (stdin)", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                                    IconButton(onClick = { showRunInputDialog = false }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Enter input values for execution (e.g. for Scanner, input(), or cin):",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = stdin,
                                    onValueChange = { stdin = it },
                                    placeholder = { Text("e.g. 12 28 for Scanner input", fontFamily = mono, fontSize = 12.sp, color = Color.Gray) },
                                    minLines = 2,
                                    maxLines = 4,
                                    textStyle = TextStyle(fontFamily = mono, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = brutalBorderColor().copy(alpha = 0.4f),
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                                Spacer(Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            stdin = ""
                                            showRunInputDialog = false
                                            executeCode()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.5.dp, brutalBorderColor())
                                    ) {
                                        Text("Run Without Input", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            showRunInputDialog = false
                                            executeCode()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                        border = BorderStroke(1.5.dp, brutalBorderColor())
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Run Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Question Overview Dialog: Expanded Height (0.84f) with Pinned AI Generator Bar at the Bottom
            if (showQuestion) {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { showQuestion = false },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.96f)
                            .fillMaxHeight(0.84f)
                            .padding(end = 4.dp, bottom = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 5.dp, y = 5.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(brutalBorderColor())
                        )
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(2.5.dp, brutalBorderColor())
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp)
                            ) {
                                // 1. Top Header Row: Badges and Close
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF2563EB))
                                                .border(BorderStroke(1.5.dp, brutalBorderColor()), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Text("QUESTION OVERVIEW", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .border(BorderStroke(1.2.dp, brutalBorderColor().copy(alpha = 0.4f)), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(language, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (question.isNotBlank()) {
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(question))
                                                    Toast.makeText(context, "Question copied to clipboard!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Spacer(Modifier.width(4.dp))
                                        }
                                        IconButton(
                                            onClick = { showQuestion = false },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))
                                HorizontalDivider(color = brutalBorderColor().copy(alpha = 0.2f), thickness = 1.5.dp)
                                Spacer(Modifier.height(10.dp))

                                // 2. Scrollable Middle: 4 Distinct Problem Sections
                                val parsedSections = remember(question, language) { getStructuredSections(question, language) }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    if (parsedSections.isEmpty()) {
                                        Text(
                                            text = "No question specified yet. Type a topic below to generate a complete challenge with Mistral AI!",
                                            fontSize = 14.sp,
                                            lineHeight = 21.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    } else {
                                        parsedSections.forEach { sec ->
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 5.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                                border = BorderStroke(1.5.dp, brutalBorderColor().copy(alpha = 0.3f))
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(sec.badgeColor)
                                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                                    ) {
                                                        Text(
                                                            text = sec.title,
                                                            color = Color.White,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Spacer(Modifier.height(6.dp))
                                                    Text(
                                                        text = sec.content,
                                                        fontSize = 13.sp,
                                                        lineHeight = 19.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        fontWeight = FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (isEnhancingQuestion) {
                                        Spacer(Modifier.height(6.dp))
                                        Surface(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF2563EB).copy(alpha = 0.08f),
                                            border = BorderStroke(1.5.dp, Color(0xFF2563EB).copy(alpha = 0.4f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp,
                                                    color = Color(0xFF2563EB)
                                                )
                                                Spacer(Modifier.width(10.dp))
                                                Text(
                                                    "Enriching 4-part breakdown (Explanation, I/O, Algorithm) with Mistral AI...",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF1D4ED8)
                                                )
                                            }
                                        }
                                    }

                                    if (question.isNotBlank() && !isEnhancingQuestion) {
                                        Spacer(Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(
                                                onClick = { expandQuestionWithAi(question) },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFF2563EB))
                                                Spacer(Modifier.width(4.dp))
                                                Text("AI Re-Enhance Details", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))
                                HorizontalDivider(color = brutalBorderColor().copy(alpha = 0.2f), thickness = 1.5.dp)
                                Spacer(Modifier.height(10.dp))

                                // 3. Bottom Pinned AI Problem Generator Bar with Inside Mic & Generate Button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = aiPromptText,
                                        onValueChange = { aiPromptText = it },
                                        placeholder = {
                                            Text(
                                                "Topic (e.g. Palindrome, Factorial)...",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(fontSize = 12.sp),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = brutalBorderColor().copy(alpha = 0.4f),
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        trailingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .padding(end = 4.dp)
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF2563EB))
                                                    .border(BorderStroke(1.2.dp, brutalBorderColor()), CircleShape)
                                                    .clickable {
                                                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak coding problem topic...")
                                                        }
                                                        try {
                                                            speechRecognizerLauncher.launch(intent)
                                                        } catch (_: Exception) {
                                                            Toast.makeText(context, "Voice input not supported", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Filled.Mic,
                                                    contentDescription = "Voice input",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    // NeoBrutalistic Generate / Run Button
                                    Box(
                                        modifier = Modifier
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isGeneratingAiQuestion) Color(0xFF94A3B8) else Color(0xFF2563EB))
                                            .border(BorderStroke(1.8.dp, brutalBorderColor()), RoundedCornerShape(10.dp))
                                            .clickable(enabled = !isGeneratingAiQuestion) {
                                                if (aiPromptText.isBlank()) {
                                                    Toast.makeText(context, "Please enter or speak a topic!", Toast.LENGTH_SHORT).show()
                                                    return@clickable
                                                }
                                                isGeneratingAiQuestion = true
                                                coroutineScope.launch {
                                                    try {
                                                        val prompt = """
                                                            You are an expert computer science teacher and coding interviewer.
                                                            Generate a comprehensive coding challenge for language $language on the topic or prompt: "$aiPromptText".
                                                            You MUST strictly structure your output with the following 4 distinct numbered sections:

                                                            1. Question
                                                            [Provide a clear, detailed problem statement, constraints, and requirements]

                                                            2. Explanation
                                                            [Explain the core logic, concepts, mathematical foundations, and approach needed]

                                                            3. Input / Output
                                                            [Provide 2 to 3 concrete sample test cases with sample input, expected output, and step-by-step walkthrough]

                                                            4. Algorithm
                                                            [Provide clear, step-by-step algorithmic steps to solve this problem]
                                                        """.trimIndent()
                                                        val generated = VastavikAi.chat(prompt)
                                                        if (generated.isNotBlank()) {
                                                            question = generated
                                                            aiPromptText = ""
                                                            Toast.makeText(context, "Problem generated successfully!", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, "Could not generate problem. Try again.", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Error: ${e.localizedMessage ?: "Unknown"}", Toast.LENGTH_SHORT).show()
                                                    } finally {
                                                        isGeneratingAiQuestion = false
                                                    }
                                                }
                                            }
                                            .padding(horizontal = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isGeneratingAiQuestion) {
                                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                        } else {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                                Spacer(Modifier.width(4.dp))
                                                Text("Generate", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // MONOCODE EDITOR TOOLBAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF181825))
                    .border(BorderStroke(1.dp, Color(0xFF313244)))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = when (language.lowercase()) {
                            "python" -> "main.py"
                            "javascript" -> "index.js"
                            "sql" -> "query.sql"
                            else -> "Main.java"
                        },
                        fontFamily = mono,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCDD6F4)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "$lineCount lines",
                        fontFamily = mono,
                        fontSize = 11.sp,
                        color = Color(0xFF6C7086)
                    )
                    Spacer(Modifier.width(8.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (stdin.isNotBlank()) Color(0xFF2563EB).copy(alpha = 0.25f) else Color(0xFF313244).copy(alpha = 0.4f))
                            .border(BorderStroke(1.dp, if (stdin.isNotBlank()) Color(0xFF2563EB) else Color(0xFF45475A)), RoundedCornerShape(6.dp))
                            .clickable { showRunInputDialog = true }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Terminal, contentDescription = null, tint = if (stdin.isNotBlank()) Color(0xFF38BDF8) else Color(0xFFA6ADC8), modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = if (stdin.isNotBlank()) "stdin: ${stdin.take(8)}..." else "stdin",
                            fontFamily = mono,
                            fontSize = 10.sp,
                            color = if (stdin.isNotBlank()) Color(0xFF38BDF8) else Color(0xFFA6ADC8)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Wrap Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isWordWrap) Color(0xFF2563EB).copy(alpha = 0.25f) else Color.Transparent)
                            .border(BorderStroke(1.dp, if (isWordWrap) Color(0xFF2563EB) else Color(0xFF45475A)), RoundedCornerShape(6.dp))
                            .clickable { isWordWrap = !isWordWrap }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isWordWrap) "Wrap: ON" else "Wrap: OFF",
                            fontFamily = mono,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWordWrap) Color(0xFF93C5FD) else Color(0xFFA6ADC8)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    // Copy button
                    IconButton(
                        onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(code))
                            android.widget.Toast.makeText(context, "Code copied!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy code", tint = Color(0xFFA6ADC8), modifier = Modifier.size(15.dp))
                    }
                }
            }

            // MONOCODE EDITOR CANVAS (Unified Vertical Scroll, 100% Aligned Gutter)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E2E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(vScrollState)
                ) {
                    // 1. Gutter / Line Numbers column
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .background(Color(0xFF181825))
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = lineNumbersString,
                            fontFamily = mono,
                            fontSize = fontSizeSp,
                            lineHeight = lineHeightSp,
                            color = Color(0xFF6C7086),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 10.dp)
                        )
                    }

                    // Divider line between line numbers and code
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF313244))
                    )

                    // 2. Code Area
                    val codeBoxModifier = if (isWordWrap) {
                        Modifier
                            .weight(1f)
                            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 24.dp)
                    } else {
                        Modifier
                            .weight(1f)
                            .horizontalScroll(hScrollState)
                            .padding(start = 12.dp, end = 24.dp, top = 12.dp, bottom = 24.dp)
                    }

                    Box(modifier = codeBoxModifier) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = code,
                            onValueChange = { code = it },
                            textStyle = TextStyle(
                                fontFamily = mono,
                                fontSize = fontSizeSp,
                                lineHeight = lineHeightSp,
                                color = SyntaxColors.normal
                            ),
                            visualTransformation = { text ->
                                androidx.compose.ui.text.input.TransformedText(
                                    highlightCode(text.text, language),
                                    androidx.compose.ui.text.input.OffsetMapping.Identity
                                )
                            },
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2563EB)),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

private fun defaultCode(lang: String) = when(lang) {
    "Java" -> "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello Vastavik\");\n    }\n}"
    "Python" -> "def solve():\n    print(\"Hello Vastavik\")\n\nsolve()"
    "JavaScript" -> "function greet(){\n  console.log(\"Hello Vastavik\");\n}\ngreet();"
    "SQL" -> "SELECT * FROM students\nWHERE class BETWEEN 5 AND 12;"
    else -> ""
}

private data class ProblemSection(val title: String, val content: String, val badgeColor: Color)

private fun parseProblemSections(text: String): List<ProblemSection> {
    if (text.isBlank()) return emptyList()
    val pattern = Regex("(?im)^[#*\\s]*(?:(1\\.\\s*Question|##\\s*Question|Question|Problem Statement|Problem Details|Problem)|(2\\.\\s*Explanation|##\\s*Explanation|Explanation|Approach|Logic)|(3\\.\\s*Input\\s*(?:\\/|and)?\\s*Output|##\\s*Input\\s*(?:\\/|and)?\\s*Output|Input\\s*\\/\\s*Output|Sample Input|Test Cases)|(4\\.\\s*Algorithm|##\\s*Algorithm|Algorithm|Steps))[:*\\s]*")
    val matches = pattern.findAll(text).toList()
    if (matches.size < 2) {
        return listOf(ProblemSection("Question Details", text.trim(), Color(0xFF2563EB)))
    }
    val sections = mutableListOf<ProblemSection>()
    for (i in matches.indices) {
        val currentMatch = matches[i]
        val header = currentMatch.value.trim()
        val startIndex = currentMatch.range.last + 1
        val endIndex = if (i + 1 < matches.size) matches[i + 1].range.first else text.length
        val sectionContent = text.substring(startIndex, endIndex).trim()
        val (title, color) = when {
            header.contains("Question", ignoreCase = true) || header.contains("Problem", ignoreCase = true) -> "1. Question" to Color(0xFF2563EB)
            header.contains("Explanation", ignoreCase = true) || header.contains("Approach", ignoreCase = true) || header.contains("Logic", ignoreCase = true) -> "2. Explanation" to Color(0xFF059669)
            header.contains("Input", ignoreCase = true) || header.contains("Output", ignoreCase = true) || header.contains("Test", ignoreCase = true) -> "3. Input / Output" to Color(0xFFD97706)
            header.contains("Algorithm", ignoreCase = true) || header.contains("Steps", ignoreCase = true) -> "4. Algorithm" to Color(0xFF7C3AED)
            else -> header to Color(0xFF2563EB)
        }
        if (sectionContent.isNotBlank()) {
            sections.add(ProblemSection(title, sectionContent, color))
        }
    }
    return if (sections.isEmpty()) listOf(ProblemSection("Question Details", text.trim(), Color(0xFF2563EB))) else sections
}

private fun getStructuredSections(rawQuestion: String, lang: String): List<ProblemSection> {
    if (rawQuestion.isBlank()) return emptyList()
    val parsed = parseProblemSections(rawQuestion)
    if (parsed.size >= 2 && !parsed.all { it.title == "Question Details" }) {
        return parsed
    }

    val cleanTitle = rawQuestion.trim().removePrefix("#").trim()
    return listOf(
        ProblemSection(
            title = "1. Question",
            content = "Write a complete and optimized solution in $lang for the following problem:\n\n$cleanTitle\n\nEnsure that all edge cases, input constraints, and required computational steps are properly handled.",
            badgeColor = Color(0xFF2563EB)
        ),
        ProblemSection(
            title = "2. Explanation",
            content = "Understand the fundamental logic and mathematical/algorithmic conditions behind '$cleanTitle'. Analyze how inputs are transformed into the desired outputs using standard data structures, loops, and conditions in $lang.",
            badgeColor = Color(0xFF059669)
        ),
        ProblemSection(
            title = "3. Input / Output",
            content = "Sample Input:\nRepresentative test case inputs for $cleanTitle\n\nSample Output:\nExpected console output matching the problem constraints.",
            badgeColor = Color(0xFFD97706)
        ),
        ProblemSection(
            title = "4. Algorithm",
            content = "1. Read and parse inputs from standard input.\n2. Initialize required accumulators or data structures.\n3. Execute the core conditional checks and iterative loops.\n4. Output the final computed result to the console.",
            badgeColor = Color(0xFF7C3AED)
        )
    )
}
