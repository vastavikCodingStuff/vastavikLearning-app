package com.vastavik.computer.ui.screens.onboarding

import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.utils.VastavikAi
import kotlinx.coroutines.launch
import java.net.URLEncoder
import com.vastavik.computer.ui.screens.editor.CodeEditorSharedState

data class PyqItem(
    val id: String,
    val board: String, // ICSE, ISC, CBSE, WB Board
    val grade: String, // Class 10, Class 12
    val year: String,
    val subject: String,
    val title: String,
    val marks: String,
    val questionText: String,
    val markingScheme: String,
    val solution: String,
    val language: String = "Java"
)

private fun getBoardColor(board: String): Color {
    return when (board.uppercase()) {
        "ICSE" -> Color(0xFF1E40AF)     // Cobalt Blue
        "ISC" -> Color(0xFF6D28D9)      // Royal Purple
        "CBSE" -> Color(0xFFB91C1C)     // Crimson Red
        "WB BOARD", "WB" -> Color(0xFF047857) // Emerald Green
        else -> Color(0xFF1F2937)       // Slate
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PYQScreen(onNavigate: (String) -> Unit, onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedGrade by remember { mutableStateOf("All") }
    var selectedBoard by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchingAi by remember { mutableStateOf(false) }
    var selectedPyqForDialog by remember { mutableStateOf<PyqItem?>(null) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                searchQuery = spoken
            }
        }
    }

    val defaultPyqs = remember {
        listOf(
            // ICSE Class 10
            PyqItem(
                id = "icse_2024_1",
                board = "ICSE",
                grade = "Class 10",
                year = "2024",
                subject = "Computer Applications (Java)",
                title = "Section B: Overloaded Methods & Special Number Check",
                marks = "15 Marks",
                questionText = "Design a class to overload a function check() as follows:\n- void check(String str, char ch): to count and print the frequency of character ch in string str.\n- void check(String s1): to check and display whether the string is a Palindrome or not without reversing.",
                markingScheme = "Method headers: 2 marks\nFrequency logic: 5 marks\nPalindrome check: 6 marks\nMain method & instantiation: 2 marks",
                solution = "class OverloadDemo {\n    void check(String str, char ch) {\n        int count = 0;\n        for (int i = 0; i < str.length(); i++) {\n            if (str.charAt(i) == ch) count++;\n        }\n        System.out.println(\"Frequency: \" + count);\n    }\n    void check(String s1) {\n        boolean isPal = true;\n        int n = s1.length();\n        for (int i = 0; i < n / 2; i++) {\n            if (s1.charAt(i) != s1.charAt(n - 1 - i)) { isPal = false; break; }\n        }\n        System.out.println(isPal ? \"Palindrome\" : \"Not Palindrome\");\n    }\n}",
                language = "Java"
            ),
            PyqItem(
                id = "icse_2023_1",
                board = "ICSE",
                grade = "Class 10",
                year = "2023",
                subject = "Computer Applications (Java)",
                title = "Section B: Bubble Sort & Linear Search on Array",
                marks = "15 Marks",
                questionText = "Define a class to accept 15 integers into an array and sort them in ascending order using the Bubble Sort technique. Print the sorted array along with the sum of all odd integers.",
                markingScheme = "Array input: 2 marks\nBubble sort nested loops: 7 marks\nOdd elements sum: 3 marks\nClean output display: 3 marks",
                solution = "import java.util.Scanner;\nclass BubbleSortDemo {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        int[] arr = new int[15];\n        for(int i = 0; i < 15; i++) arr[i] = sc.nextInt();\n        for(int i = 0; i < 14; i++) {\n            for(int j = 0; j < 14 - i; j++) {\n                if(arr[j] > arr[j+1]) {\n                    int t = arr[j]; arr[j] = arr[j+1]; arr[j+1] = t;\n                }\n            }\n        }\n        int oddSum = 0;\n        for(int x : arr) { System.out.print(x + \" \"); if(x % 2 != 0) oddSum += x; }\n        System.out.println(\"\\nOdd Sum: \" + oddSum);\n    }\n}",
                language = "Java"
            ),
            // ISC Class 12
            PyqItem(
                id = "isc_2024_1",
                board = "ISC",
                grade = "Class 12",
                year = "2024",
                subject = "Computer Science (Theory & Java)",
                title = "Section B: Recursive Binary Search & Stack ADT",
                marks = "10 Marks",
                questionText = "A class Stack holds integers. Write the methods void push(int val) and int pop() to implement a LIFO stack using an array of capacity 50. Handle stack overflow and underflow conditions cleanly.",
                markingScheme = "Data members & constructor: 2 marks\nPush method with overflow: 4 marks\nPop method with underflow: 4 marks",
                solution = "class Stack {\n    int[] arr = new int[50];\n    int top = -1;\n    void push(int val) {\n        if (top == arr.length - 1) { System.out.println(\"Stack Overflow\"); return; }\n        arr[++top] = val;\n    }\n    int pop() {\n        if (top == -1) { System.out.println(\"Stack Underflow\"); return -999; }\n        return arr[top--];\n    }\n}",
                language = "Java"
            ),
            PyqItem(
                id = "isc_2023_1",
                board = "ISC",
                grade = "Class 12",
                year = "2023",
                subject = "Computer Science (Theory & Java)",
                title = "Section A: Boolean Function Minimization via K-Map",
                marks = "5 Marks",
                questionText = "Reduce the Boolean function F(A, B, C, D) = Σ(0, 2, 5, 7, 8, 10, 13, 15) using a 4-variable Karnaugh Map. Draw the reduced logic circuit using NAND gates only.",
                markingScheme = "K-Map grid plotting: 2 marks\nQuad/Pair group identification: 2 marks\nMinimal expression: 1 mark",
                solution = "K-Map grouping:\nCorner Quad: m(0, 2, 8, 10) => B' D'\nInner Quad: m(5, 7, 13, 15) => B D\nSimplified Expression: F = B'D' + BD (which is B XNOR D).",
                language = "Java"
            ),
            // CBSE Class 10
            PyqItem(
                id = "cbse_2024_1",
                grade = "Class 10",
                board = "CBSE",
                year = "2024",
                subject = "Information Technology (Code 402)",
                title = "Section B: Python Conditional Loops & Database Integrity",
                marks = "8 Marks",
                questionText = "Write a Python program to read student names and their marks in 3 subjects. Calculate the aggregate percentage and assign grades: >=90 (A+), >=75 (A), >=60 (B), else (C). Also explain the role of a PRIMARY KEY in relational databases.",
                markingScheme = "Python calculation logic: 4 marks\nConditional ladder: 2 marks\nPrimary Key definition: 2 marks",
                solution = "names = input('Enter student name: ')\nm1 = float(input('Marks 1: '))\nm2 = float(input('Marks 2: '))\nm3 = float(input('Marks 3: '))\npct = (m1 + m2 + m3) / 3.0\nif pct >= 90: grade = 'A+'\nelif pct >= 75: grade = 'A'\nelif pct >= 60: grade = 'B'\nelse: grade = 'C'\nprint(f'{names}: {pct:.2f}% -> Grade {grade}')\n\n# Primary Key uniquely identifies each row in a relation with NO null values.",
                language = "Python"
            ),
            // CBSE Class 12
            PyqItem(
                id = "cbse_2024_2",
                grade = "Class 12",
                board = "CBSE",
                year = "2024",
                subject = "Computer Science (Python & SQL)",
                title = "Section C: Binary File Operations (pickle module) & SQL Joins",
                marks = "10 Marks",
                questionText = "Write a Python function update_marks(roll, new_marks) to read a binary file 'STUDENT.DAT' containing objects of dictionary {'Roll': int, 'Name': str, 'Marks': float} and update the marks of the given roll number using pickle.",
                markingScheme = "File opening in binary mode: 1 mark\nEOFError try-except loop: 4 marks\nTemporary file / repositioning write: 3 marks\nFile close and cleanup: 2 marks",
                solution = "import pickle, os\ndef update_marks(roll, new_marks):\n    found = False\n    with open('STUDENT.DAT', 'rb') as f, open('TEMP.DAT', 'wb') as t:\n        while True:\n            try:\n                rec = pickle.load(f)\n                if rec['Roll'] == roll:\n                    rec['Marks'] = new_marks\n                    found = True\n                pickle.dump(rec, t)\n            except EOFError:\n                break\n    os.remove('STUDENT.DAT')\n    os.rename('TEMP.DAT', 'STUDENT.DAT')\n    print('Record updated' if found else 'Record not found')",
                language = "Python"
            ),
            // WB Board Class 10
            PyqItem(
                id = "wb_2024_1",
                grade = "Class 10",
                board = "WB Board",
                year = "2024",
                subject = "Modern Computer Application",
                title = "Section A: Number System Conversion & Boolean Logic",
                marks = "7 Marks",
                questionText = "Convert the hexadecimal number (2F.8)16 to binary and decimal. Prove De Morgan's First Law (A + B)' = A' . B' using truth table truth verification.",
                markingScheme = "Hex to Binary: 2 marks\nHex to Decimal: 2 marks\nTruth Table proof: 3 marks",
                solution = "(2F.8)16 to Binary:\n2 = 0010, F = 1111, 8 = 1000\nBinary = 00101111.10002 = 101111.12\n\nTo Decimal:\n2*16^1 + 15*16^0 + 8*16^-1 = 32 + 15 + 0.5 = 47.5_10.\nDe Morgan Proof verified by checking columns for (A+B)' and A'.B'.",
                language = "Java"
            ),
            // WB Board Class 12
            PyqItem(
                id = "wb_2024_2",
                grade = "Class 12",
                board = "WB Board",
                year = "2024",
                subject = "Computer Science",
                title = "Section B: C Programming Dynamic Memory & DBMS Relations",
                marks = "10 Marks",
                questionText = "Write a C program to allocate memory dynamically for n integers using malloc(). Find the second largest integer without sorting. Explain 1NF, 2NF, and 3NF normalization rules.",
                markingScheme = "Dynamic memory allocation & check: 3 marks\nSecond largest logic: 4 marks\nNormalization explanation: 3 marks",
                solution = "#include <stdio.h>\n#include <stdlib.h>\nint main() {\n    int n, *arr, max1 = -99999, max2 = -99999;\n    scanf(\"%d\", &n);\n    arr = (int*)malloc(n * sizeof(int));\n    for(int i = 0; i < n; i++) {\n        scanf(\"%d\", &arr[i]);\n        if(arr[i] > max1) { max2 = max1; max1 = arr[i]; }\n        else if(arr[i] > max2 && arr[i] != max1) { max2 = arr[i]; }\n    }\n    printf(\"Second largest: %d\\n\", max2);\n    free(arr);\n    return 0;\n}",
                language = "Java"
            )
        )
    }

    var pyqList by remember { mutableStateOf(defaultPyqs) }

    val filteredPyqs = remember(pyqList, selectedGrade, selectedBoard, searchQuery) {
        pyqList.filter { item ->
            (selectedGrade == "All" || item.grade.equals(selectedGrade, ignoreCase = true)) &&
            (selectedBoard == "All" || item.board.equals(selectedBoard, ignoreCase = true)) &&
            (searchQuery.isBlank() ||
             item.title.contains(searchQuery, ignoreCase = true) ||
             item.questionText.contains(searchQuery, ignoreCase = true) ||
             item.year.contains(searchQuery, ignoreCase = true) ||
             item.subject.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Board PYQs & Solutions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Authentic ICSE • ISC • CBSE • WB Board Papers", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Bar: Class 10 & 12 Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Class 10", "Class 12").forEach { grade ->
                    val isSelected = selectedGrade == grade
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedGrade = grade },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.5.dp, brutalBorderColor())
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = grade,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Board Filter Chips with Official Board Colors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "ICSE", "ISC", "CBSE", "WB Board").forEach { board ->
                    val isSelected = selectedBoard == board
                    val boardColor = if (board == "All") MaterialTheme.colorScheme.secondary else getBoardColor(board)
                    Surface(
                        modifier = Modifier.clickable { selectedBoard = board },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) boardColor else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.5.dp, if (isSelected) boardColor else brutalBorderColor().copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = board,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // AI Search & Paper Generator Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(2.dp, brutalBorderColor())
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Search year, topic or question...",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = brutalBorderColor().copy(alpha = 0.3f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        Spacer(Modifier.width(6.dp))

                        // Mic Button
                        IconButton(
                            onClick = {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak board year or topic...")
                                }
                                try {
                                    speechLauncher.launch(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Voice input not supported", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(BorderStroke(1.5.dp, brutalBorderColor()), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                Icons.Filled.Mic,
                                contentDescription = "Voice Search",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(6.dp))

                        // Mistral AI Board Question Fetcher
                        Button(
                            onClick = {
                                if (searchQuery.isBlank()) {
                                    Toast.makeText(context, "Enter a topic, board, or year!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isSearchingAi = true
                                coroutineScope.launch {
                                    try {
                                        val bTarget = if (selectedBoard != "All") selectedBoard else "ICSE/CBSE"
                                        val gTarget = if (selectedGrade != "All") selectedGrade else "Class 10/12"
                                        val prompt = """
                                            You are an expert Indian board exam paper curator for $bTarget ($gTarget).
                                            The student is searching for: "$searchQuery".
                                            Generate an authentic, realistic board exam question based on authentic past papers.
                                            Format your response in exactly this structure:
                                            [BOARD]: $bTarget
                                            [GRADE]: $gTarget
                                            [YEAR]: 2024
                                            [SUBJECT]: Computer Science
                                            [TITLE]: $searchQuery Exam Problem
                                            [MARKS]: 15 Marks
                                            [QUESTION]:
                                            <Detailed question statement>
                                            [MARKING]:
                                            <Clear marking scheme breakdown>
                                            [SOLUTION]:
                                            <Complete executable code and explanation>
                                        """.trimIndent()
                                        val res = VastavikAi.chat(prompt)
                                        if (res.isNotBlank()) {
                                            val newPyq = PyqItem(
                                                id = "ai_${System.currentTimeMillis()}",
                                                board = if (selectedBoard != "All") selectedBoard else "ICSE",
                                                grade = if (selectedGrade != "All") selectedGrade else "Class 10",
                                                year = "2024",
                                                subject = "Computer Science",
                                                title = "AI Board Q: $searchQuery",
                                                marks = "15 Marks",
                                                questionText = res.substringAfter("[QUESTION]:", res).substringBefore("[MARKING]:").trim(),
                                                markingScheme = res.substringAfter("[MARKING]:", "Step-by-step evaluation").substringBefore("[SOLUTION]:").trim(),
                                                solution = res.substringAfter("[SOLUTION]:", "// Solution provided by Mistral AI").trim(),
                                                language = if (bTarget.contains("CBSE")) "Python" else "Java"
                                            )
                                            pyqList = listOf(newPyq) + pyqList
                                            selectedPyqForDialog = newPyq
                                            Toast.makeText(context, "Board question retrieved!", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Search error: ${e.localizedMessage ?: "Unknown"}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isSearchingAi = false
                                    }
                                }
                            },
                            enabled = !isSearchingAi,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            modifier = Modifier.height(40.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            if (isSearchingAi) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(Modifier.width(4.dp))
                                    Text("AI Search", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Results List
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPyqs, key = { it.id }) { pyq ->
                    val bannerColor = getBoardColor(pyq.board)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPyqForDialog = pyq },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(2.dp, brutalBorderColor())
                    ) {
                        Column {
                            // PROMINENT COLORED BOARD BANNER WITH BOLD WHITE TEXT
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bannerColor)
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${pyq.board} • ${pyq.grade}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.White.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = pyq.year,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.White)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = pyq.marks,
                                            color = bannerColor,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            // Card Body
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = pyq.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = pyq.subject,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = pyq.questionText,
                                    fontSize = 13.sp,
                                    maxLines = 3,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tap to view full solution & marking scheme →",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = bannerColor
                                    )
                                    Icon(
                                        Icons.Filled.ChevronRight,
                                        contentDescription = null,
                                        tint = bannerColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog to View PYQ, Marking Scheme, Solution & Solve in Code Editor
    selectedPyqForDialog?.let { pyq ->
        val bannerColor = getBoardColor(pyq.board)
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedPyqForDialog = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(2.dp, brutalBorderColor())
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Colored Board Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bannerColor)
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${pyq.board} ${pyq.grade} • ${pyq.year}",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "${pyq.subject} (${pyq.marks})",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 12.sp
                                    )
                                }
                                IconButton(
                                    onClick = { selectedPyqForDialog = null },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                                }
                            }
                        }

                        // Scrollable Content: Question, Marking Scheme, Solution
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            // Question Section
                            Text("QUESTION", fontSize = 12.sp, fontWeight = FontWeight.Black, color = bannerColor)
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, brutalBorderColor().copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = pyq.questionText,
                                    fontSize = 14.sp,
                                    lineHeight = 21.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }

                            Spacer(Modifier.height(14.dp))

                            // Marking Scheme Section
                            Text("OFFICIAL MARKING SCHEME", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFFD97706))
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7).copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = pyq.markingScheme,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }

                            Spacer(Modifier.height(14.dp))

                            // Solution Section
                            Text("COMPLETE SOLUTION", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF059669))
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E1E2E),
                                border = BorderStroke(1.dp, Color(0xFF313244))
                            ) {
                                Text(
                                    text = pyq.solution,
                                    fontSize = 12.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    lineHeight = 18.sp,
                                    color = Color(0xFFCDD6F4),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Bottom Actions: Solve in Code Editor & Close
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, brutalBorderColor().copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { selectedPyqForDialog = null },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Close")
                                }
                                Button(
                                    onClick = {
                                        CodeEditorSharedState.set(
                                            code = pyq.solution,
                                            language = pyq.language,
                                            question = pyq.questionText
                                        )
                                        val encodedQ = try { URLEncoder.encode(pyq.questionText, "UTF-8") } catch (_: Exception) { "" }
                                        val encodedCode = try { URLEncoder.encode(pyq.solution, "UTF-8") } catch (_: Exception) { "" }
                                        selectedPyqForDialog = null
                                        onNavigate("code_editor?initialCode=$encodedCode&language=${pyq.language}&question=$encodedQ")
                                    },
                                    modifier = Modifier.weight(1.5f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = bannerColor)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Solve in Editor", fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
