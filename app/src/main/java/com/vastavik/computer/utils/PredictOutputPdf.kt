package com.vastavik.computer.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.vastavik.computer.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PredictQuestionReview(
    val questionNumber: Int,
    val topic: String,
    val language: String,
    val codeSnippet: String,
    val studentAnswer: String,
    val actualOutput: String,
    val isCorrect: Boolean,
    val explanation: String
)

data class OutputCheckResult(
    val isCorrect: Boolean,
    val actualOutput: String,
    val explanation: String
)

object PredictOutputPdf {

    fun generateAndOpenPdf(
        context: Context,
        setTitle: String,
        questions: List<PredictQuestionReview>,
        openAfterSave: Boolean = true
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var pageNumber = 1
        var page = document.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        val timesTypeface = try {
            Typeface.create("Times New Roman", Typeface.NORMAL) ?: Typeface.SERIF
        } catch (_: Exception) {
            Typeface.SERIF
        }
        val timesBoldTypeface = Typeface.create(timesTypeface, Typeface.BOLD)

        val robotoSlabTypeface = try {
            ResourcesCompat.getFont(context, R.font.roboto_slab_regular) ?: Typeface.DEFAULT
        } catch (_: Exception) {
            Typeface.DEFAULT
        }
        val robotoSlabBoldTypeface = try {
            ResourcesCompat.getFont(context, R.font.roboto_slab_bold) ?: Typeface.DEFAULT_BOLD
        } catch (_: Exception) {
            Typeface.DEFAULT_BOLD
        }

        // 1-inch safe margin: left 72, right 523, top 72, bottom 760 (width: 451 pt)
        val leftMargin = 72f
        val rightMargin = 523f
        val contentWidth = rightMargin - leftMargin
        val bottomMargin = 760f
        var y = 84f

        drawPdfPageFrame(canvas, pageNumber, "PREDICT OUTPUT REVIEW")

        val titlePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 15f
            typeface = timesBoldTypeface
        }
        val scorePaint = Paint().apply {
            color = Color.parseColor("#059669")
            textSize = 12f
            typeface = timesBoldTypeface
        }
        val metaPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 9.5f
            typeface = timesTypeface
        }
        val questionPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 11f
            typeface = timesBoldTypeface
        }
        val codeFont = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 9.5f
            typeface = Typeface.MONOSPACE
        }
        val codeBgPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        val codeBorderPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        val correctPaint = Paint().apply {
            color = Color.parseColor("#059669")
            textSize = 10.5f
            typeface = timesBoldTypeface
        }
        val wrongPaint = Paint().apply {
            color = Color.parseColor("#DC2626")
            textSize = 10.5f
            typeface = timesBoldTypeface
        }
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 10.5f
            typeface = timesTypeface
        }
        val aiHeaderPaint = Paint().apply {
            color = Color.parseColor("#1D4ED8")
            textSize = 9.5f
            typeface = robotoSlabBoldTypeface
        }
        val aiTextPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 9.5f
            typeface = robotoSlabTypeface
        }
        val aiBoxBgPaint = Paint().apply {
            color = Color.parseColor("#EFF6FF")
            style = Paint.Style.FILL
        }
        val aiBoxBorderPaint = Paint().apply {
            color = Color.parseColor("#BFDBFE")
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }

        val correctCount = questions.count { it.isCorrect }
        val percentage = if (questions.isNotEmpty()) (correctCount * 100) / questions.size else 0

        canvas.drawText("Vastavik Computers — Predict the Output Review", leftMargin, y, titlePaint)
        y += 18f
        canvas.drawText("Set: $setTitle  •  Score: $correctCount / ${questions.size} ($percentage% Accuracy)", leftMargin, y, scorePaint)
        y += 14f
        val dateStr = "Attempted on: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())}"
        canvas.drawText(dateStr, leftMargin, y, metaPaint)
        y += 12f

        val dividerPaint = Paint().apply { color = Color.parseColor("#CBD5E1"); strokeWidth = 1f }
        canvas.drawLine(leftMargin, y, rightMargin, y, dividerPaint)
        y += 16f

        questions.forEachIndexed { i, q ->
            val statusBadge = if (q.isCorrect) "✓ Correct" else "✗ Incorrect"
            val qHeader = "Q${i + 1}. [${statusBadge}] ${q.topic} (${q.language})"
            val qLines = wrapPdfText(qHeader, questionPaint, contentWidth)

            // Code snippet lines
            val codeLines = q.codeSnippet.split("\n")
            val codeBoxHeight = (codeLines.size * 13f) + 16f

            // Output comparison lines
            val studentAnsLines = wrapPdfText("Student Answer: ${q.studentAnswer.ifBlank { "(no answer)" }}", if (q.isCorrect) correctPaint else wrongPaint, contentWidth)
            val actualAnsLines = wrapPdfText("Actual Output: ${q.actualOutput}", bodyPaint, contentWidth)

            // AI Explanation
            val cleanExp = q.explanation.replace("**", "").replace("*", "").replace("`", "").trim()
            val aiLines = if (cleanExp.isNotBlank()) wrapPdfText(cleanExp, aiTextPaint, contentWidth - 16f) else emptyList()
            val aiBoxHeight = if (aiLines.isNotEmpty()) (aiLines.size * 13f) + 24f else 0f

            val neededHeight = (qLines.size * 15f) + codeBoxHeight + (studentAnsLines.size * 14f) + (actualAnsLines.size * 14f) + aiBoxHeight + 24f

            if (y + neededHeight > bottomMargin) {
                document.finishPage(page)
                pageNumber++
                val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = document.startPage(newPageInfo)
                canvas = page.canvas
                drawPdfPageFrame(canvas, pageNumber, "PREDICT OUTPUT REVIEW")
                y = 84f
            }

            // Draw Question header
            for (qLine in qLines) {
                canvas.drawText(qLine, leftMargin, y, if (q.isCorrect) correctPaint else wrongPaint)
                y += 15f
            }
            y += 4f

            // Draw Code Box
            val codeBoxTop = y
            canvas.drawRect(leftMargin, codeBoxTop, rightMargin, codeBoxTop + codeBoxHeight, codeBgPaint)
            canvas.drawRect(leftMargin, codeBoxTop, rightMargin, codeBoxTop + codeBoxHeight, codeBorderPaint)
            var codeY = codeBoxTop + 14f
            for (cLine in codeLines) {
                canvas.drawText(cLine, leftMargin + 8f, codeY, codeFont)
                codeY += 13f
            }
            y += codeBoxHeight + 8f

            // Draw Answers
            for (sa in studentAnsLines) {
                canvas.drawText(sa, leftMargin, y, if (q.isCorrect) correctPaint else wrongPaint)
                y += 14f
            }
            for (aa in actualAnsLines) {
                canvas.drawText(aa, leftMargin, y, bodyPaint)
                y += 14f
            }
            y += 4f

            // Draw AI Trace Box
            if (aiLines.isNotEmpty()) {
                val aiBoxTop = y
                canvas.drawRect(leftMargin, aiBoxTop, rightMargin, aiBoxTop + aiBoxHeight, aiBoxBgPaint)
                canvas.drawRect(leftMargin, aiBoxTop, rightMargin, aiBoxTop + aiBoxHeight, aiBoxBorderPaint)
                var aiY = aiBoxTop + 13f
                canvas.drawText("Mistral AI Step-by-Step Code Trace:", leftMargin + 8f, aiY, aiHeaderPaint)
                aiY += 14f
                for (line in aiLines) {
                    canvas.drawText(line, leftMargin + 8f, aiY, aiTextPaint)
                    aiY += 13f
                }
                y += aiBoxHeight + 8f
            }

            y += 8f
            canvas.drawLine(leftMargin, y, rightMargin, y, dividerPaint)
            y += 14f
        }

        document.finishPage(page)
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val sanitizedTitle = setTitle.replace(Regex("[^a-zA-Z0-9]"), "_")
        val file = File(dir, "Vastavik_PredictOutput_${sanitizedTitle}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        if (openAfterSave) {
            openPdf(context, file)
        }
        return file
    }

    private fun openPdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Saved to Downloads: ${file.name}", Toast.LENGTH_LONG).show()
        }
    }

    private fun wrapPdfText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val result = mutableListOf<String>()
        val lines = text.split("\n")
        for (line in lines) {
            val words = line.split(" ")
            var current = ""
            for (w in words) {
                if (w.isEmpty()) continue
                val test = if (current.isEmpty()) w else "$current $w"
                if (paint.measureText(test) <= maxWidth) {
                    current = test
                } else {
                    if (current.isNotEmpty()) result.add(current)
                    if (paint.measureText(w) > maxWidth) {
                        var rem = w
                        while (rem.isNotEmpty()) {
                            val cnt = paint.breakText(rem, true, maxWidth, null)
                            result.add(rem.substring(0, cnt))
                            rem = rem.substring(cnt)
                        }
                        current = ""
                    } else {
                        current = w
                    }
                }
            }
            if (current.isNotEmpty()) result.add(current)
        }
        return result
    }

    private fun drawPdfPageFrame(canvas: Canvas, pageNumber: Int, title: String) {
        val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            color = Color.parseColor("#0F172A")
        }
        val innerBorderPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.5f
            color = Color.parseColor("#94A3B8")
        }
        canvas.drawRect(56f, 56f, 539f, 786f, borderPaint)
        canvas.drawRect(60f, 60f, 535f, 782f, innerBorderPaint)

        val headerPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 8.5f
            isFakeBoldText = true
            typeface = Typeface.DEFAULT
        }
        canvas.drawText("VASTAVIK COMPUTERS • $title", 72f, 69f, headerPaint)

        val footerPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 8.5f
            typeface = Typeface.DEFAULT
        }
        canvas.drawText("Confidential • Vastavik AI Learning System", 72f, 775f, footerPaint)
        val pageStr = "Page $pageNumber"
        canvas.drawText(pageStr, 523f - footerPaint.measureText(pageStr), 775f, footerPaint)
    }
}
