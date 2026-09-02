package com.vastavik.computer.ui.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.vastavik.computer.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

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
fun CodeEditorScreen(onNavigate: (String)->Unit, initialCode: String = "", initialLanguage: String = "Python") {
    var language by remember { mutableStateOf(initialLanguage.ifBlank { "Python" }) }
    var code by remember { mutableStateOf(initialCode.ifBlank { defaultCode(language) }) }
    var output by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(language) {
        if (initialCode.isBlank() && (code==defaultCode("Java")||code==defaultCode("Python")||code==defaultCode("JavaScript")||code==defaultCode("SQL")))
            code = defaultCode(language)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Code Editor", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { onNavigate("home") }) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
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
            if (output.isNotEmpty() || isRunning) {
                Surface(shadowElevation = 8.dp, shape = if (MaterialTheme.shapes.medium.toString().contains("0.0")) RoundedCornerShape(0.dp) else RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 260.dp).padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Output", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            IconButton(onClick = { output = "" }, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Clear, contentDescription = "Clear") }
                        }
                        if (isRunning) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top=8.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Running...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            Text(output, fontFamily = mono, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth().padding(top=8.dp))
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    isRunning = true; output = ""
                    coroutineScope.launch {
                        output = withContext(Dispatchers.IO) {
                            try {
                                val apiKey = BuildConfig.MISTRAL_API_KEY
                                if (apiKey.isBlank()) return@withContext "MISTRAL_API_KEY not configured."
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
                                    put("messages", JSONArray().put(JSONObject().put("role", "user").put("content",
                                        "You are a code runner for Class 5-12 students. This is $language code. Explain what the output would be when this code runs. Be concise — just show the expected output, then 1 line explanation.\n\nCode:\n$code")))
                                    put("max_tokens", 512)
                                    put("temperature", 0.1)
                                }
                                conn.outputStream.use { it.write(body.toString().toByteArray()) }
                                val responseCode = conn.responseCode
                                val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
                                val response = stream.bufferedReader().use { it.readText() }
                                if (responseCode in 200..299) {
                                    val json = JSONObject(response)
                                    json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
                                } else { "Error ($responseCode)" }
                            } catch (e: Exception) { "Error: ${e.message}" }
                        }
                        isRunning = false
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
            Row(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF1E1E2E))) {
                val lines = code.split("\n")
                val lineCount = lines.size
                val highlighted = remember(code, language) { highlightCode(code, language) }

                LazyColumn(modifier = Modifier.width(48.dp).fillMaxHeight().background(Color(0xFF252526)).padding(vertical=12.dp), horizontalAlignment = Alignment.End) {
                    items(lineCount) { idx ->
                        Text(
                            text = "${idx + 1}",
                            color = Color(0xFF858585),
                            fontFamily = mono,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(end=8.dp, top=1.dp, bottom=1.dp)
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight().horizontalScroll(rememberScrollState()).padding(12.dp)) {
                    Text(
                        text = highlighted,
                        fontFamily = mono,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
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
