package com.vastavik.computer.ui.screens.practice

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor
import com.vastavik.computer.utils.OutputCheckResult
import com.vastavik.computer.utils.PredictOutputPdf
import com.vastavik.computer.utils.PredictQuestionReview
import com.vastavik.computer.utils.VastavikAi
import kotlinx.coroutines.launch

private data class OutputQuestion(
    val id: Int,
    val topic: String,
    val javaCode: String,
    val pythonCode: String,
    val cppCode: String,
    val jsCode: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictOutputSetScreen(
    setTitle: String,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()

    var selectedLanguage by remember { mutableStateOf("Java") }
    var langDropdownExpanded by remember { mutableStateOf(false) }

    val languages = listOf("Java", "Python", "C++", "JavaScript")

    // 5 multi-question problem set
    val questions = remember(setTitle) {
        listOf(
            OutputQuestion(
                id = 1,
                topic = "$setTitle — Iteration & Conditions",
                javaCode = "int sum = 0;\nfor (int i = 1; i <= 5; i++) {\n    if (i % 2 == 0) continue;\n    sum += i;\n}\nSystem.out.println(\"sum=\" + sum);",
                pythonCode = "total = 0\nfor i in range(1, 6):\n    if i % 2 == 0:\n        continue\n    total += i\nprint(f\"sum={total}\")",
                cppCode = "#include <iostream>\nusing namespace std;\nint main() {\n    int sum = 0;\n    for(int i=1; i<=5; i++){\n        if(i%2==0) continue;\n        sum += i;\n    }\n    cout << \"sum=\" << sum << endl;\n}",
                jsCode = "let sum = 0;\nfor (let i = 1; i <= 5; i++) {\n    if (i % 2 === 0) continue;\n    sum += i;\n}\nconsole.log(\"sum=\" + sum);"
            ),
            OutputQuestion(
                id = 2,
                topic = "$setTitle — Prefix & Postfix Increment",
                javaCode = "int a = 4, b = 7;\nint c = ++a * 2 + b--;\nSystem.out.println(a + \" \" + b + \" \" + c);",
                pythonCode = "# Python equivalent of prefix/postfix sequence\na, b = 4, 7\na += 1\nc = a * 2 + b\nb -= 1\nprint(f\"{a} {b} {c}\")",
                cppCode = "#include <iostream>\nusing namespace std;\nint main() {\n    int a = 4, b = 7;\n    int c = ++a * 2 + b--;\n    cout << a << \" \" << b << \" \" << c << endl;\n}",
                jsCode = "let a = 4, b = 7;\nlet c = ++a * 2 + b--;\nconsole.log(a + \" \" + b + \" \" + c);"
            ),
            OutputQuestion(
                id = 3,
                topic = "$setTitle — String Indexing & Substrings",
                javaCode = "String s = \"COMPUTER\";\nSystem.out.println(s.substring(2, 6));\nSystem.out.println(s.indexOf('T'));",
                pythonCode = "s = \"COMPUTER\"\nprint(s[2:6])\nprint(s.find('T'))",
                cppCode = "#include <iostream>\n#include <string>\nusing namespace std;\nint main() {\n    string s = \"COMPUTER\";\n    cout << s.substr(2, 4) << endl;\n    cout << s.find('T') << endl;\n}",
                jsCode = "const s = \"COMPUTER\";\nconsole.log(s.substring(2, 6));\nconsole.log(s.indexOf('T'));"
            ),
            OutputQuestion(
                id = 4,
                topic = "$setTitle — Array Accumulation",
                javaCode = "int[] arr = {1, 3, 5, 7};\nfor (int i = 0; i < arr.length - 1; i++) {\n    arr[i + 1] += arr[i];\n}\nSystem.out.println(arr[3]);",
                pythonCode = "arr = [1, 3, 5, 7]\nfor i in range(len(arr) - 1):\n    arr[i + 1] += arr[i]\nprint(arr[3])",
                cppCode = "#include <iostream>\nusing namespace std;\nint main() {\n    int arr[] = {1, 3, 5, 7};\n    for(int i=0; i<3; i++) arr[i+1] += arr[i];\n    cout << arr[3] << endl;\n}",
                jsCode = "const arr = [1, 3, 5, 7];\nfor (let i = 0; i < arr.length - 1; i++) {\n    arr[i + 1] += arr[i];\n}\nconsole.log(arr[3]);"
            ),
            OutputQuestion(
                id = 5,
                topic = "$setTitle — Recursive Function Call",
                javaCode = "int f(int n) {\n    if (n <= 1) return 1;\n    return n + f(n - 2);\n}\nSystem.out.println(f(5));",
                pythonCode = "def f(n):\n    if n <= 1: return 1\n    return n + f(n - 2)\n\nprint(f(5))",
                cppCode = "#include <iostream>\nusing namespace std;\nint f(int n) {\n    if (n <= 1) return 1;\n    return n + f(n - 2);\n}\nint main() {\n    cout << f(5) << endl;\n}",
                jsCode = "function f(n) {\n    if (n <= 1) return 1;\n    return n + f(n - 2);\n}\nconsole.log(f(5));"
            )
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    val currentQ = questions[currentIndex]

    // Student inputs and AI evaluation results keyed by question index
    val userAnswers = remember { mutableStateMapOf<Int, String>() }
    val results = remember { mutableStateMapOf<Int, OutputCheckResult>() }
    var isChecking by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }

    val currentInput = userAnswers[currentIndex] ?: ""
    val currentResult = results[currentIndex]

    fun getCodeSnippet(q: OutputQuestion, lang: String) = when (lang) {
        "Python" -> q.pythonCode
        "C++" -> q.cppCode
        "JavaScript" -> q.jsCode
        else -> q.javaCode
    }

    fun verifyCurrentOutput() {
        val inputToVerify = userAnswers[currentIndex] ?: ""
        if (inputToVerify.isBlank()) {
            Toast.makeText(context, "Please enter your predicted output first!", Toast.LENGTH_SHORT).show()
            return
        }
        val snippet = getCodeSnippet(currentQ, selectedLanguage)
        isChecking = true

        coroutineScope.launch {
            try {
                val prompt = """
                    You are evaluating a student's answer for a 'Predict the Output' computer science question.
                    Language: $selectedLanguage
                    Topic: ${currentQ.topic}
                    Code:
                    $snippet

                    Student's Predicted Output:
                    $inputToVerify

                    Task:
                    1. Compute the EXACT console output produced by executing this code.
                    2. Check if student's prediction matches the exact output (tolerate trailing spaces/newlines).
                    3. Provide a clear, concise step-by-step trace of how the code executes line-by-line.

                    Format response exactly as:
                    VERDICT: CORRECT (or INCORRECT)
                    ACTUAL_OUTPUT:
                    <exact console output here>
                    EXPLANATION:
                    <concise step-by-step trace here>
                """.trimIndent()

                val response = VastavikAi.chat(
                    systemPrompt = "You are an expert computer science teacher evaluating code outputs accurately.",
                    userPrompt = prompt,
                    temperature = 0.1
                )

                val isCorrect = response.contains("VERDICT: CORRECT", ignoreCase = true)
                val actual = if (response.contains("ACTUAL_OUTPUT:")) {
                    response.substringAfter("ACTUAL_OUTPUT:").substringBefore("EXPLANATION:").trim()
                } else {
                    "Execution output computed"
                }
                val explanation = if (response.contains("EXPLANATION:")) {
                    response.substringAfter("EXPLANATION:").trim()
                } else {
                    response.trim()
                }
                results[currentIndex] = OutputCheckResult(isCorrect, actual, explanation)
            } catch (e: Exception) {
                results[currentIndex] = OutputCheckResult(
                    isCorrect = false,
                    actualOutput = "Error during verification",
                    explanation = "Could not verify: ${e.message ?: "Network error"}"
                )
            } finally {
                isChecking = false
            }
        }
    }

    // Wrap with safe area padding: Status bars, navigation bars, camera punch-hole safe!
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (isSubmitted) {
            // PERFORMANCE OVERVIEW REPORT SCREEN
            val correctCount = results.values.count { it.isCorrect }
            val accuracy = if (questions.isNotEmpty()) (correctCount * 100) / questions.size else 0

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Set Performance Report",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Score Card
                Box(modifier = Modifier.fillMaxWidth().padding(end = 4.dp, bottom = 4.dp)) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 4.dp, y = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(bb)
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(2.dp, bb)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (accuracy >= 80) "🎉 EXCELLENT WORK!" else if (accuracy >= 50) "👍 GOOD EFFORT!" else "📚 PRACTICE NEEDED",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = if (accuracy >= 80) Color(0xFF059669) else if (accuracy >= 50) Color(0xFFD97706) else Color(0xFFDC2626)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "$correctCount / ${questions.size}",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Score: $accuracy% Accuracy",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Download Review PDF Button (1-inch Safe Margins)
                Button(
                    onClick = {
                        val reviews = questions.mapIndexed { idx, q ->
                            val r = results[idx]
                            PredictQuestionReview(
                                questionNumber = idx + 1,
                                topic = q.topic,
                                language = selectedLanguage,
                                codeSnippet = getCodeSnippet(q, selectedLanguage),
                                studentAnswer = userAnswers[idx] ?: "",
                                actualOutput = r?.actualOutput ?: "(unverified)",
                                isCorrect = r?.isCorrect ?: false,
                                explanation = r?.explanation ?: "No explanation available"
                            )
                        }
                        val pdfFile = PredictOutputPdf.generateAndOpenPdf(
                            context = context,
                            setTitle = setTitle,
                            questions = reviews,
                            openAfterSave = true
                        )
                        if (pdfFile != null) {
                            Toast.makeText(context, "Review PDF saved to Downloads!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    border = BorderStroke(2.dp, bb)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.FileDownload, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Download Review PDF", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, bb)
                ) {
                    Text("Return to Practice", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(20.dp))

                // Breakdown list
                Text(
                    "Question Breakdown",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                questions.forEachIndexed { idx, q ->
                    val r = results[idx]
                    val isCorr = r?.isCorrect == true
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.5.dp, if (isCorr) Color(0xFF059669) else Color(0xFFDC2626))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isCorr) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                contentDescription = null,
                                tint = if (isCorr) Color(0xFF059669) else Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Q${idx + 1}: ${q.topic}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "Your: ${userAnswers[idx]?.ifBlank { "(blank)" }} • Actual: ${r?.actualOutput ?: "N/A"}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // SOLVER SCREEN
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                // TOP BAR: Back + Set Title + Language Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text(
                                text = setTitle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1
                            )
                            Text(
                                text = "Question ${currentIndex + 1} of ${questions.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }

                    // Language Selector Dropdown
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.5.dp, bb),
                            modifier = Modifier.clickable { langDropdownExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedLanguage,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("▼", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        DropdownMenu(
                            expanded = langDropdownExpanded,
                            onDismissRequest = { langDropdownExpanded = false }
                        ) {
                            languages.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        selectedLanguage = lang
                                        langDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Scrollable Question & Input Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Code Card with Syntax-like Dark Preview
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF181825),
                        border = BorderStroke(2.dp, bb),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                }
                                Text(
                                    text = "$selectedLanguage • ${currentQ.topic.take(24)}...",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = getCodeSnippet(currentQ, selectedLanguage),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFFD4D4D4)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Output Prediction Input Area
                    Text(
                        "Your Predicted Output:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))

                    // Max 3 lines visible, auto vertical scrolling inside
                    OutlinedTextField(
                        value = currentInput,
                        onValueChange = { userAnswers[currentIndex] = it },
                        placeholder = {
                            Text(
                                "Type exact console output (max 3 lines visible)...",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        },
                        minLines = 1,
                        maxLines = 3,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = bb.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(Modifier.height(10.dp))

                    // Send / Verify Output Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (currentInput.isBlank() || isChecking) Color(0xFF94A3B8) else Color(0xFF2563EB))
                            .border(BorderStroke(1.8.dp, bb), RoundedCornerShape(10.dp))
                            .clickable(enabled = currentInput.isNotBlank() && !isChecking) {
                                verifyCurrentOutput()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChecking) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Mistral Small Evaluating...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Send & Verify with Mistral Small", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    // Evaluation Result Display
                    currentResult?.let { res ->
                        Spacer(Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (res.isCorrect) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                            border = BorderStroke(2.dp, if (res.isCorrect) Color(0xFF059669) else Color(0xFFDC2626)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (res.isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                        contentDescription = null,
                                        tint = if (res.isCorrect) Color(0xFF059669) else Color(0xFFDC2626),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (res.isCorrect) "PERFECT MATCH! ✔" else "OUTPUT MISMATCH ✗",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = if (res.isCorrect) Color(0xFF065F46) else Color(0xFF991B1B)
                                    )
                                }

                                Spacer(Modifier.height(8.dp))
                                Text("Actual Output:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                ) {
                                    Text(
                                        text = res.actualOutput,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF38BDF8),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }

                                Spacer(Modifier.height(6.dp))
                                Text("Step-by-Step Execution Trace:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                Text(
                                    text = res.explanation,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }

                // BOTTOM NAVIGATION: Previous / Next / Submit Set
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (currentIndex > 0) currentIndex-- },
                        enabled = currentIndex > 0,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, bb),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Previous", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.width(10.dp))

                    if (currentIndex < questions.size - 1) {
                        Button(
                            onClick = { currentIndex++ },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            border = BorderStroke(1.5.dp, bb),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Next", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = { isSubmitted = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            border = BorderStroke(1.5.dp, bb),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Submit Set", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
