package com.vastavik.computer.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==========================================
// VS Code Dark Modern Theme Color Palette
// ==========================================
private val VsCodeBg = Color(0xFF1E1E1E)
private val VsCodeGutterBg = Color(0xFF181818)
private val VsCodeBorder = Color(0xFF333333)
private val VsCodeGutterText = Color(0xFF858585)
private val VsCodeForeground = Color(0xFFD4D4D4)

private val VsCodeControlFlow = Color(0xFFC586C0)   // if, else, for, while, return, etc.
private val VsCodeKeywordType = Color(0xFF569CD6)   // class, public, static, void, int, etc.
private val VsCodeBuiltinClass = Color(0xFF4EC9B0)  // String, System, Math, Scanner, etc.
private val VsCodeMethodYellow = Color(0xFFDCDCAA)  // out, println, print, substring, main, etc.
private val VsCodeStringRust = Color(0xFFCE9178)    // "strings", 'c'
private val VsCodeNumberSage = Color(0xFFB5CEA8)    // 123, 0.5f, 0xAA
private val VsCodeCommentGreen = Color(0xFF6A9955)  // // comments, # comments
private val VsCodeVariableBlue = Color(0xFF9CDCFE)  // identifiers, parameters
private val VsCodePunctuation = Color(0xFFD4D4D4)   // ; {} () [] = + - * /

private val CONTROL_FLOW_KEYWORDS = setOf(
    "if", "else", "for", "while", "do", "switch", "case", "default",
    "break", "continue", "return", "try", "catch", "finally", "throw", "throws",
    "new", "instanceof", "yield", "match"
)

private val TYPE_DECLARATION_KEYWORDS = setOf(
    "class", "interface", "enum", "record", "public", "private", "protected",
    "static", "final", "abstract", "synchronized", "transient", "volatile",
    "void", "int", "double", "float", "boolean", "char", "byte", "short", "long",
    "extends", "implements", "super", "this", "package", "import",
    "def", "const", "let", "var", "val", "fun", "True", "False", "None",
    "null", "true", "false"
)

private val BUILTIN_CLASSES = setOf(
    "String", "System", "Math", "Scanner", "Integer", "Double", "Boolean",
    "Character", "Float", "Long", "Byte", "Short", "Object", "Exception",
    "RuntimeException", "Throwable", "List", "ArrayList", "Map", "HashMap",
    "Set", "HashSet", "Arrays", "Collections", "StringBuilder", "StringBuffer"
)

private val BUILTIN_METHODS = setOf(
    "out", "println", "print", "printf", "length", "charAt", "substring",
    "indexOf", "lastIndexOf", "toUpperCase", "toLowerCase", "trim", "equals",
    "equalsIgnoreCase", "compareTo", "toString", "valueOf", "parseInt",
    "parseDouble", "main", "append", "size", "get", "add", "remove", "contains",
    "pow", "sqrt", "abs", "max", "min", "round", "random", "len", "range"
)

/**
 * Normalizes tabs to 4 spaces and trims excessive uniform indentation.
 */
fun formatSourceCode(rawCode: String): String {
    val expanded = rawCode.replace("\t", "    ")
    return expanded.trimIndent()
}

/**
 * Highlights a single line of code with VS Code Dark Modern theme syntax rules.
 */
fun highlightVsCodeLine(line: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < line.length) {
        val c = line[i]

        // 1. Comments: // or #
        if ((c == '/' && i + 1 < line.length && line[i + 1] == '/') || c == '#') {
            withStyle(SpanStyle(color = VsCodeCommentGreen, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) {
                append(line.substring(i))
            }
            break
        }

        // 2. String & Character literals: "..." or '...'
        if (c == '"' || c == '\'') {
            val quote = c
            var j = i + 1
            while (j < line.length && line[j] != quote) {
                if (line[j] == '\\' && j + 1 < line.length) {
                    j += 2
                } else {
                    j++
                }
            }
            val endQuote = if (j < line.length) j + 1 else line.length
            withStyle(SpanStyle(color = VsCodeStringRust)) {
                append(line.substring(i, endQuote))
            }
            i = endQuote
            continue
        }

        // 3. Numbers: 0-9
        if (c.isDigit()) {
            var j = i
            while (j < line.length && (line[j].isLetterOrDigit() || line[j] == '.' || line[j] == 'x' || line[j] == 'X')) {
                j++
            }
            withStyle(SpanStyle(color = VsCodeNumberSage)) {
                append(line.substring(i, j))
            }
            i = j
            continue
        }

        // 4. Identifiers / Keywords
        if (c.isLetter() || c == '_' || c == '$') {
            var j = i
            while (j < line.length && (line[j].isLetterOrDigit() || line[j] == '_' || line[j] == '$')) {
                j++
            }
            val word = line.substring(i, j)

            when {
                word in CONTROL_FLOW_KEYWORDS -> {
                    withStyle(SpanStyle(color = VsCodeControlFlow, fontWeight = FontWeight.Bold)) {
                        append(word)
                    }
                }
                word in TYPE_DECLARATION_KEYWORDS -> {
                    withStyle(SpanStyle(color = VsCodeKeywordType, fontWeight = FontWeight.SemiBold)) {
                        append(word)
                    }
                }
                word in BUILTIN_CLASSES -> {
                    withStyle(SpanStyle(color = VsCodeBuiltinClass, fontWeight = FontWeight.Medium)) {
                        append(word)
                    }
                }
                word in BUILTIN_METHODS -> {
                    withStyle(SpanStyle(color = VsCodeMethodYellow)) {
                        append(word)
                    }
                }
                else -> {
                    // Peek ahead to see if followed by ( -> method call
                    var k = j
                    while (k < line.length && line[k].isWhitespace()) k++
                    if (k < line.length && line[k] == '(') {
                        withStyle(SpanStyle(color = VsCodeMethodYellow)) { append(word) }
                    } else {
                        withStyle(SpanStyle(color = VsCodeVariableBlue)) { append(word) }
                    }
                }
            }
            i = j
            continue
        }

        // 5. Operators, punctuation, or spaces
        withStyle(SpanStyle(color = VsCodePunctuation)) {
            append(c.toString())
        }
        i++
    }
}

/**
 * Builds a complete syntax-highlighted AnnotatedString for a multi-line code block.
 */
fun buildVsCodeAnnotatedString(code: String): AnnotatedString = buildAnnotatedString {
    val cleanCode = formatSourceCode(code)
    val lines = cleanCode.split("\n")
    lines.forEachIndexed { index, line ->
        append(highlightVsCodeLine(line))
        if (index < lines.lastIndex) {
            append("\n")
        }
    }
}

/**
 * Modern, authentic VS Code-style snippet component.
 * Features:
 * - Proper whitespace and indentation preservation
 * - Horizontal scrolling for wide lines
 * - Dark Modern syntax highlighting (keywords, types, methods, strings, comments)
 * - Mac/VS Code window buttons (red, yellow, green)
 * - Line numbers gutter
 * - Quick copy-to-clipboard button
 */
@Composable
fun VsCodeSnippetView(
    code: String,
    modifier: Modifier = Modifier,
    language: String = "java",
    showLineNumbers: Boolean = true,
    allowCopy: Boolean = true,
    maxHeight: androidx.compose.ui.unit.Dp? = null
) {
    val cleanCode = remember(code) { formatSourceCode(code) }
    val lines = remember(cleanCode) { cleanCode.split("\n") }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

    val horizontalScroll = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(VsCodeBg)
            .border(1.dp, VsCodeBorder, RoundedCornerShape(10.dp))
    ) {
        Column {
            // Editor Window Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF252526))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Window Control Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFF27C93F)))

                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = language.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CDCFE)
                    )
                }

                // Copy Button
                if (allowCopy) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF333333))
                            .clickable {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(cleanCode))
                                isCopied = true
                                Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                coroutineScope.launch {
                                    delay(2000)
                                    isCopied = false
                                }
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            if (isCopied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                            contentDescription = "Copy code",
                            tint = if (isCopied) Color(0xFF4EC9B0) else Color(0xFFCCCCCC),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = if (isCopied) "Copied!" else "Copy",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isCopied) Color(0xFF4EC9B0) else Color(0xFFCCCCCC)
                        )
                    }
                }
            }

            // Editor Code Body
            val contentModifier = Modifier
                .fillMaxWidth()
                .then(if (maxHeight != null) Modifier.heightIn(max = maxHeight) else Modifier)

            Row(modifier = contentModifier) {
                // Left Line Number Gutter
                if (showLineNumbers) {
                    Column(
                        modifier = Modifier
                            .background(VsCodeGutterBg)
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        lines.forEachIndexed { idx, _ ->
                            Text(
                                text = "${idx + 1}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 19.sp,
                                color = VsCodeGutterText
                            )
                        }
                    }
                    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(VsCodeBorder))
                }

                // Code Lines with Horizontal Scroll & Text Selection.
                // We also intercept horizontal drag gestures so the parent HorizontalPager
                // does not steal the swipe and switch tabs while the user is scrolling
                // horizontally inside the code window.
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { _, _ -> /* consume */ }
                        }
                ) {
                    SelectionContainer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(horizontalScroll)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Column {
                            lines.forEach { line ->
                                Text(
                                    text = highlightVsCodeLine(line),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.5.sp,
                                    lineHeight = 19.sp,
                                    color = VsCodeForeground,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
