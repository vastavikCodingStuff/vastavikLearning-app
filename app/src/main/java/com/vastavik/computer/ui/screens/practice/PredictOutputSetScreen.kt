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
import com.vastavik.computer.ui.components.VsCodeSnippetView
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor
import com.vastavik.computer.utils.OutputCheckResult
import com.vastavik.computer.utils.PredictOutputPdf
import com.vastavik.computer.utils.PredictQuestionReview
import com.vastavik.computer.utils.VastavikAi
import kotlinx.coroutines.launch

private fun getCanonicalOutputAndTrace(qId: Int, lang: String): OutputCheckResult {
    return when (qId) {
        1 -> OutputCheckResult(
            isCorrect = true,
            actualOutput = "sum=9",
            explanation = """
• Variables & Types:
  - `sum` is an integer accumulator initialized to 0.
  - Loop variable `i` is an integer iterating from 1 through 5 inclusive.

• Step-by-Step Line Tracing & Control Flow:
  - Line 1: `int sum = 0` sets accumulator to 0.
  - Iteration i = 1: (1 % 2 == 0) is false. The continue statement is skipped. `sum += 1` executes -> sum becomes 0 + 1 = 1.
  - Iteration i = 2: (2 % 2 == 0) is true. The continue statement executes immediately, skipping `sum += i`.
  - Iteration i = 3: (3 % 2 == 0) is false. `sum += 3` executes -> sum becomes 1 + 3 = 4.
  - Iteration i = 4: (4 % 2 == 0) is true. The continue statement executes immediately, skipping addition.
  - Iteration i = 5: (5 % 2 == 0) is false. `sum += 5` executes -> sum becomes 4 + 5 = 9.
  - The loop terminates as i reaches 6, which violates i <= 5.

• Output Statement:
  - Prints string literal "sum=" concatenated with integer sum (9).
  - Exact Console Output: sum=9
            """.trimIndent()
        )
        2 -> OutputCheckResult(
            isCorrect = true,
            actualOutput = "5 6 17",
            explanation = """
• Variables & Types:
  - `a` (integer) = 4, `b` (integer) = 7.
  - `c` (integer) receives evaluation of expression `++a * 2 + b--`.

• Operator Precedence & Evaluation Order:
  - Pre-increment `++a`: Operates before use. `a` is immediately incremented from 4 to 5. Value used in multiplication = 5.
  - Multiplication (*) takes precedence over addition: 5 * 2 = 10.
  - Post-decrement `b--`: The current value of `b` (7) is used in the addition first, and `b` is decremented to 6 afterwards.
  - Addition: 10 + 7 = 17. Value 17 is assigned to `c`.
  - Variable `b` decrements to 6.
  - Final variable states: a = 5, b = 6, c = 17.

• Output Statement:
  - Prints a, b, and c separated by single spaces.
  - Exact Console Output: 5 6 17
            """.trimIndent()
        )
        3 -> OutputCheckResult(
            isCorrect = true,
            actualOutput = "MPUT\n5",
            explanation = """
• Variables & String 0-Based Indexing:
  - String `s` = "COMPUTER" (Length: 8 characters).
  - Index map:
    Index 0: 'C', Index 1: 'O', Index 2: 'M', Index 3: 'P'
    Index 4: 'U', Index 5: 'T', Index 6: 'E', Index 7: 'R'

• Function Calls & Execution Trace:
  - Call 1: `s.substring(2, 6)` (or `s[2:6]` / `s.substr(2, 4)`):
    Extracts characters starting at index 2 (inclusive) up to index 6 (exclusive).
    Characters at indices 2, 3, 4, 5 correspond to 'M', 'P', 'U', 'T'.
    Returns substring "MPUT". Printed on Line 1.
  - Call 2: `s.indexOf('T')` (or `s.find('T')`):
    Searches string `s` from index 0 for character 'T'.
    Character 'T' is located at index 5.
    Returns integer 5. Printed on Line 2.

• Exact Console Output:
  MPUT
  5
            """.trimIndent()
        )
        4 -> OutputCheckResult(
            isCorrect = true,
            actualOutput = "16",
            explanation = """
• Data Structure & Variables:
  - `arr`: 1D integer array initialized with {1, 3, 5, 7}. Length = 4.
  - Loop condition `i < arr.length - 1` (i < 3), so loop runs for i = 0, 1, 2.

• Prefix Accumulation Trace:
  - Iteration i = 0: `arr[1] += arr[0]` -> arr[1] becomes 3 + 1 = 4. Array is now {1, 4, 5, 7}.
  - Iteration i = 1: `arr[2] += arr[1]` -> arr[2] becomes 5 + 4 = 9. Array is now {1, 4, 9, 7}.
  - Iteration i = 2: `arr[3] += arr[2]` -> arr[3] becomes 7 + 9 = 16. Array is now {1, 4, 9, 16}.
  - Loop terminates as i reaches 3.

• Output Statement:
  - Prints element at index 3: `arr[3]`.
  - Exact Console Output: 16
            """.trimIndent()
        )
        5 -> OutputCheckResult(
            isCorrect = true,
            actualOutput = "9",
            explanation = """
• Recursive Function Definition & Types:
  - Function `int f(int n)` accepts integer `n` and returns integer.
  - Base case: if (n <= 1) return 1.
  - Recursive relation: return n + f(n - 2).
  - Invocation: `f(5)`.

• Call Stack Unfolding & Unwinding:
  - Frame 1: `f(5)` evaluates 5 + f(3). Suspends and pushes f(3).
  - Frame 2: `f(3)` evaluates 3 + f(1). Suspends and pushes f(1).
  - Frame 3: `f(1)` encounters base case 1 <= 1. Returns 1.
  - Unwinding Frame 2: f(3) = 3 + 1 = 4. Returns 4.
  - Unwinding Frame 1: f(5) = 5 + 4 = 9. Returns 9.

• Output Statement:
  - System.out.println(f(5)) prints the returned value.
  - Exact Console Output: 9
            """.trimIndent()
        )
        else -> OutputCheckResult(
            isCorrect = true,
            actualOutput = "Execution Completed",
            explanation = "Evaluated code trace and execution flow."
        )
    }
}

private fun isAnswerMatching(studentAnswer: String, expectedOutput: String): Boolean {
    val cleanStudent = studentAnswer.trim().replace("\r\n", "\n").replace("\\s+".toRegex(), " ")
    val cleanExpected = expectedOutput.trim().replace("\r\n", "\n").replace("\\s+".toRegex(), " ")
    if (cleanStudent.equals(cleanExpected, ignoreCase = true)) return true

    val normStudent = cleanStudent.replace("\n", " ").trim()
    val normExpected = cleanExpected.replace("\n", " ").trim()
    if (normStudent.equals(normExpected, ignoreCase = true)) return true

    if (cleanExpected.startsWith("sum=") && cleanStudent == "9") return true
    if (cleanStudent.startsWith("sum=") && cleanExpected == "9") return true

    return false
}

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

    val initialLang = com.vastavik.computer.utils.BoardLanguage.getPreferred(context)
    var selectedLanguage by remember(initialLang) { mutableStateOf(initialLang) }
    var langDropdownExpanded by remember { mutableStateOf(false) }

    val languages = com.vastavik.computer.utils.BoardLanguage.supportedLanguages()

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
        val canonical = getCanonicalOutputAndTrace(currentQ.id, selectedLanguage)
        val isLocallyCorrect = isAnswerMatching(inputToVerify, canonical.actualOutput)
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

                val isCorrect = response.contains("VERDICT: CORRECT", ignoreCase = true) || isLocallyCorrect
                val actual = if (response.contains("ACTUAL_OUTPUT:")) {
                    val parsed = response.substringAfter("ACTUAL_OUTPUT:").substringBefore("EXPLANATION:").trim()
                    if (parsed.isNotBlank() && parsed != "Execution output computed") parsed else canonical.actualOutput
                } else {
                    canonical.actualOutput
                }
                val explanation = if (response.contains("EXPLANATION:")) {
                    val parsedExp = response.substringAfter("EXPLANATION:").trim()
                    if (parsedExp.isNotBlank() && parsedExp.length > 20) parsedExp else canonical.explanation
                } else {
                    canonical.explanation
                }
                results[currentIndex] = OutputCheckResult(isCorrect, actual, explanation)
            } catch (e: Exception) {
                results[currentIndex] = OutputCheckResult(
                    isCorrect = isLocallyCorrect,
                    actualOutput = canonical.actualOutput,
                    explanation = canonical.explanation
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
                            val canonical = getCanonicalOutputAndTrace(q.id, selectedLanguage)
                            val r = results[idx] ?: canonical
                            PredictQuestionReview(
                                questionNumber = idx + 1,
                                topic = q.topic,
                                language = selectedLanguage,
                                codeSnippet = getCodeSnippet(q, selectedLanguage),
                                studentAnswer = userAnswers[idx]?.ifBlank { "(no answer)" } ?: "(no answer)",
                                actualOutput = r.actualOutput.ifBlank { canonical.actualOutput },
                                isCorrect = r.isCorrect,
                                explanation = r.explanation.ifBlank { canonical.explanation }
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
                    "Question Breakdown & Step-by-Step Traces",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                questions.forEachIndexed { idx, q ->
                    val canonical = getCanonicalOutputAndTrace(q.id, selectedLanguage)
                    val r = results[idx] ?: canonical
                    val isCorr = r.isCorrect
                    val studentAns = userAnswers[idx]?.trim() ?: ""

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .padding(end = 4.dp, bottom = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 4.dp, y = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(bb)
                        )
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(2.dp, if (isCorr) Color(0xFF059669) else Color(0xFFDC2626))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Header: Question number + Topic + Verdict
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF2563EB))
                                                .border(BorderStroke(1.dp, bb), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Text("Q${idx + 1}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = q.topic,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            maxLines = 1
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(if (isCorr) Color(0xFF059669).copy(alpha = 0.15f) else Color(0xFFDC2626).copy(alpha = 0.15f))
                                            .border(BorderStroke(1.2.dp, if (isCorr) Color(0xFF059669) else Color(0xFFDC2626)), RoundedCornerShape(20.dp))
                                            .padding(horizontal = 9.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                if (isCorr) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                                contentDescription = null,
                                                tint = if (isCorr) Color(0xFF059669) else Color(0xFFDC2626),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = if (isCorr) "CORRECT" else "INCORRECT",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isCorr) Color(0xFF059669) else Color(0xFFDC2626)
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                // Code Snippet Box with authentic VS Code Dark Modern theme
                                VsCodeSnippetView(
                                    code = getCodeSnippet(q, selectedLanguage),
                                    language = selectedLanguage,
                                    showLineNumbers = true
                                )

                                Spacer(Modifier.height(10.dp))

                                // Side by Side Output Comparison: Your Answer vs Correct Output
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Student Answer Box
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Your Answer:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCorr) Color(0xFF059669) else Color(0xFFDC2626)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isCorr) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                                            border = BorderStroke(1.2.dp, if (isCorr) Color(0xFF059669) else Color(0xFFDC2626)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = studentAns.ifBlank { "(no answer)" },
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCorr) Color(0xFF065F46) else Color(0xFF991B1B),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                    // Correct Output Box
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Correct Output:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF059669)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF1E293B),
                                            border = BorderStroke(1.2.dp, Color(0xFF059669)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = r.actualOutput,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF38BDF8),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                // Step-by-step trace & explanation box
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Filled.AutoAwesome,
                                                contentDescription = null,
                                                tint = Color(0xFF2563EB),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(Modifier.width(5.dp))
                                            Text(
                                                "Step-by-Step Execution Trace & Breakdown:",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF1E293B)
                                            )
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = r.explanation,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp,
                                            color = Color(0xFF334155)
                                        )
                                    }
                                }
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
                                        com.vastavik.computer.utils.BoardLanguage.savePreferred(context, lang)
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
                    // Code Card with authentic VS Code Dark Modern syntax highlighting
                    VsCodeSnippetView(
                        code = getCodeSnippet(currentQ, selectedLanguage),
                        language = selectedLanguage,
                        showLineNumbers = true
                    )

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
                            onClick = {
                                questions.forEachIndexed { idx, q ->
                                    val canonical = getCanonicalOutputAndTrace(q.id, selectedLanguage)
                                    val existing = results[idx]
                                    val studentAns = userAnswers[idx]?.trim() ?: ""
                                    val isCorr = if (existing != null) {
                                        existing.isCorrect || isAnswerMatching(studentAns, canonical.actualOutput)
                                    } else {
                                        isAnswerMatching(studentAns, canonical.actualOutput)
                                    }
                                    val actual = if (existing != null && existing.actualOutput.isNotBlank() && existing.actualOutput != "Error during verification" && existing.actualOutput != "Execution output computed" && existing.actualOutput != "N/A") {
                                        existing.actualOutput
                                    } else {
                                        canonical.actualOutput
                                    }
                                    val explanation = if (existing != null && existing.explanation.isNotBlank() && !existing.explanation.startsWith("Could not verify")) {
                                        existing.explanation
                                    } else {
                                        canonical.explanation
                                    }
                                    results[idx] = OutputCheckResult(isCorrect = isCorr, actualOutput = actual, explanation = explanation)
                                }
                                isSubmitted = true
                            },
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
