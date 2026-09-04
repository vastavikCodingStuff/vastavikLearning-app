package com.vastavik.computer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Parses inline markdown tokens (**bold**, *italic*, `code`, [text](url))
 * and returns an AnnotatedString with all markdown characters removed.
 */
fun parseInlineMarkdown(
    text: String,
    baseColor: Color,
    accentColor: Color = Color(0xFF2563EB)
): AnnotatedString {
    return buildAnnotatedString {
        // Regex to match:
        // Group 1 & 2: **bold**
        // Group 3 & 4: *italic*
        // Group 5 & 6: `code`
        // Group 7, 8, 9: [label](url)
        val pattern = java.util.regex.Pattern.compile(
            "(\\*\\*(.+?)\\*\\*)|(\\*([^*]+?)\\*)|(`([^`]+?)`)|(\\[([^\\]]+?)\\]\\(([^)]+?)\\))"
        )
        val matcher = pattern.matcher(text)
        var lastEnd = 0

        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()

            // Append preceding plain text
            if (start > lastEnd) {
                append(text.substring(lastEnd, start))
            }

            when {
                // **bold**
                matcher.group(2) != null -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = baseColor)) {
                        append(matcher.group(2) ?: "")
                    }
                }
                // *italic*
                matcher.group(4) != null -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = baseColor)) {
                        append(matcher.group(4) ?: "")
                    }
                }
                // `code`
                matcher.group(6) != null -> {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    ) {
                        append(matcher.group(6) ?: "")
                    }
                }
                // [label](url)
                matcher.group(8) != null -> {
                    withStyle(
                        SpanStyle(
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(matcher.group(8) ?: "")
                    }
                }
            }
            lastEnd = end
        }

        // Append any remaining plain text
        if (lastEnd < text.length) {
            append(text.substring(lastEnd))
        }
    }
}

/**
 * Renders multi-line markdown text cleanly without any raw markdown symbols (##, -, **, `).
 */
@Composable
fun MarkdownContent(
    content: String,
    modifier: Modifier = Modifier,
    baseColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    accentColor: Color = Color(0xFF2563EB)
) {
    val lines = content.lines()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) {
                Spacer(Modifier.height(4.dp))
                continue
            }

            when {
                // H1 Heading
                line.startsWith("# ") -> {
                    val headingText = line.removePrefix("# ").trim()
                    Text(
                        text = headingText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // H2 Heading
                line.startsWith("## ") -> {
                    val headingText = line.removePrefix("## ").trim()
                    Text(
                        text = headingText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // H3 Heading
                line.startsWith("### ") -> {
                    val headingText = line.removePrefix("### ").trim()
                    Text(
                        text = headingText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Bullet item (- or *)
                line.startsWith("- ") || line.startsWith("* ") -> {
                    val bulletText = line.substring(2).trim()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 7.dp, end = 8.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Text(
                            text = parseInlineMarkdown(bulletText, baseColor, accentColor),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = baseColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Numbered list item (e.g. 1. , 2. )
                line.matches(Regex("^\\d+\\.\\s+.*")) -> {
                    val number = line.substringBefore(".").trim()
                    val text = line.substringAfter(".").trim()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "$number.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = accentColor,
                            modifier = Modifier.width(20.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(text, baseColor, accentColor),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = baseColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Regular Paragraph
                else -> {
                    Text(
                        text = parseInlineMarkdown(line, baseColor, accentColor),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = baseColor
                    )
                }
            }
        }
    }
}
