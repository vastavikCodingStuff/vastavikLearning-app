package com.vastavik.computer.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PaymentReceiptData(
    val invoiceNumber: String,
    val transactionId: String,
    val orderId: String,
    val planName: String,
    val planPeriod: String,
    val amount: String,
    val baseAmount: String,
    val discountAmount: String,
    val gstAmount: String,
    val paymentMethod: String,
    val gateway: String = "Razorpay",
    val customerName: String,
    val customerEmail: String,
    val timestamp: Long = System.currentTimeMillis()
)

object PaymentReceiptPdf {

    fun generateReceipt(context: Context, data: PaymentReceiptData): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 at 72 dpi
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val timesTypeface = try {
            Typeface.create("Times New Roman", Typeface.NORMAL) ?: Typeface.SERIF
        } catch (_: Exception) {
            Typeface.SERIF
        }
        val timesBoldTypeface = Typeface.create(timesTypeface, Typeface.BOLD)
        val sansTypeface = Typeface.SANS_SERIF
        val sansBoldTypeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        // 1-Inch Safe Margins (72 points) on all 4 sides
        val leftMargin = 72f
        val rightMargin = 523f
        val topMargin = 72f
        val bottomMargin = 770f
        val contentWidth = rightMargin - leftMargin // 451 pt

        // 1. Draw Page Boundary Border (subtle elegant dual line)
        val borderPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        canvas.drawRect(leftMargin - 12f, topMargin - 12f, rightMargin + 12f, bottomMargin + 12f, borderPaint)

        val innerBorderPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 0.5f
            style = Paint.Style.STROKE
        }
        canvas.drawRect(leftMargin - 10f, topMargin - 10f, rightMargin + 10f, bottomMargin + 10f, innerBorderPaint)

        var y = topMargin + 10f

        // 2. Company Brand & Title Header
        val brandPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 18f
            typeface = sansBoldTypeface
        }
        canvas.drawText("VASTAVIK COMPUTERS EDTECH", leftMargin, y, brandPaint)

        val brandSubPaint = Paint().apply {
            color = Color.parseColor("#2563EB")
            textSize = 9.5f
            typeface = sansBoldTypeface
        }
        canvas.drawText("LEARNING PLATFORM • GSTIN: 19AABCV8821Q1Z4", leftMargin, y + 14f, brandSubPaint)

        // Right side: TAX INVOICE header
        val invoiceTitlePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 15f
            typeface = timesBoldTypeface
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("TAX INVOICE", rightMargin, y, invoiceTitlePaint)

        val invoiceSubPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 9f
            typeface = timesTypeface
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("ORIGINAL FOR RECIPIENT", rightMargin, y + 14f, invoiceSubPaint)

        y += 28f

        // Horizontal Separator
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1.2f
        }
        canvas.drawLine(leftMargin, y, rightMargin, y, dividerPaint)
        y += 18f

        // 3. Invoice & Customer Metadata Box
        val metaBgPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        val metaBorderPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val metaBoxRect = RectF(leftMargin, y, rightMargin, y + 105f)
        canvas.drawRoundRect(metaBoxRect, 6f, 6f, metaBgPaint)
        canvas.drawRoundRect(metaBoxRect, 6f, 6f, metaBorderPaint)

        val labelPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 9f
            typeface = sansTypeface
        }
        val valPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 9.5f
            typeface = sansBoldTypeface
        }

        val dateFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.ENGLISH)
        val formattedDate = dateFormat.format(Date(data.timestamp))

        // Column 1 (Left)
        val c1X = leftMargin + 12f
        var my = y + 18f
        canvas.drawText("Invoice Number:", c1X, my, labelPaint)
        canvas.drawText(data.invoiceNumber, c1X + 80f, my, valPaint)

        my += 16f
        canvas.drawText("Date & Time:", c1X, my, labelPaint)
        canvas.drawText(formattedDate, c1X + 80f, my, valPaint)

        my += 16f
        canvas.drawText("Gateway:", c1X, my, labelPaint)
        canvas.drawText("${data.gateway} Secure Gateway", c1X + 80f, my, valPaint)

        my += 16f
        canvas.drawText("Payment Status:", c1X, my, labelPaint)

        // Paid Pill Badge
        val statusPillPaint = Paint().apply {
            color = Color.parseColor("#10B981")
            style = Paint.Style.FILL
        }
        val statusTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 8.5f
            typeface = sansBoldTypeface
        }
        val pillRect = RectF(c1X + 80f, my - 10f, c1X + 130f, my + 4f)
        canvas.drawRoundRect(pillRect, 4f, 4f, statusPillPaint)
        canvas.drawText("PAID", c1X + 92f, my, statusTextPaint)

        my += 16f
        canvas.drawText("Transaction ID:", c1X, my, labelPaint)
        canvas.drawText(data.transactionId, c1X + 80f, my, valPaint)

        // Column 2 (Right)
        val c2X = leftMargin + 240f
        var my2 = y + 18f
        canvas.drawText("Billed To:", c2X, my2, labelPaint)
        canvas.drawText(data.customerName.ifEmpty { "Student" }, c2X + 65f, my2, valPaint)

        my2 += 16f
        canvas.drawText("Email:", c2X, my2, labelPaint)
        canvas.drawText(data.customerEmail.ifEmpty { "student@vastavik.com" }, c2X + 65f, my2, valPaint)

        my2 += 16f
        canvas.drawText("Payment Mode:", c2X, my2, labelPaint)
        canvas.drawText(data.paymentMethod, c2X + 75f, my2, valPaint)

        my2 += 16f
        canvas.drawText("Account Type:", c2X, my2, labelPaint)
        canvas.drawText("Vastavik Student Pro", c2X + 75f, my2, valPaint)

        my2 += 16f
        canvas.drawText("Order ID:", c2X, my2, labelPaint)
        canvas.drawText(data.orderId, c2X + 75f, my2, valPaint)

        y += 122f

        // 4. Itemized Table
        val tableHeaderBgPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            style = Paint.Style.FILL
        }
        val tableHeaderRect = RectF(leftMargin, y, rightMargin, y + 24f)
        canvas.drawRoundRect(tableHeaderRect, 4f, 4f, tableHeaderBgPaint)

        val thPaint = Paint().apply {
            color = Color.WHITE
            textSize = 9f
            typeface = sansBoldTypeface
        }

        canvas.drawText("S.NO", leftMargin + 10f, y + 15f, thPaint)
        canvas.drawText("DESCRIPTION & PLAN", leftMargin + 45f, y + 15f, thPaint)
        canvas.drawText("DURATION", leftMargin + 200f, y + 15f, thPaint)
        canvas.drawText("BASE PRICE", leftMargin + 270f, y + 15f, thPaint)
        canvas.drawText("DISCOUNT", leftMargin + 345f, y + 15f, thPaint)
        canvas.drawText("TOTAL (INR)", leftMargin + 400f, y + 15f, thPaint)

        y += 24f

        // Table Content Row
        val tableRowBgPaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            style = Paint.Style.FILL
        }
        val tableRowRect = RectF(leftMargin, y, rightMargin, y + 36f)
        canvas.drawRect(tableRowRect, tableRowBgPaint)

        val tdPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 9.5f
            typeface = sansTypeface
        }
        val tdBoldPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 9.5f
            typeface = sansBoldTypeface
        }
        val discountPaint = Paint().apply {
            color = Color.parseColor("#059669")
            textSize = 9.5f
            typeface = sansBoldTypeface
        }

        canvas.drawText("1", leftMargin + 15f, y + 20f, tdPaint)
        canvas.drawText("${data.planName} Subscription", leftMargin + 45f, y + 16f, tdBoldPaint)

        val tdSubPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 8f
            typeface = sansTypeface
        }
        canvas.drawText("Includes all courses, AI Tutor & PYQs", leftMargin + 45f, y + 28f, tdSubPaint)

        canvas.drawText(data.planPeriod, leftMargin + 200f, y + 20f, tdPaint)
        canvas.drawText(data.baseAmount, leftMargin + 270f, y + 20f, tdPaint)
        canvas.drawText("- ${data.discountAmount}", leftMargin + 345f, y + 20f, discountPaint)
        canvas.drawText("₹${data.amount}", leftMargin + 400f, y + 20f, tdBoldPaint)

        y += 36f
        canvas.drawLine(leftMargin, y, rightMargin, y, dividerPaint)
        y += 14f

        // 5. Financial Summary Breakdown Box
        val summaryBoxRect = RectF(leftMargin + 220f, y, rightMargin, y + 96f)
        val summaryBgPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(summaryBoxRect, 6f, 6f, summaryBgPaint)
        canvas.drawRoundRect(summaryBoxRect, 6f, 6f, metaBorderPaint)

        var sy = y + 18f
        val sumLabelPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 9f
            typeface = sansTypeface
        }
        val sumValPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 9.5f
            typeface = sansBoldTypeface
            textAlign = Paint.Align.RIGHT
        }

        canvas.drawText("Subtotal:", leftMargin + 235f, sy, sumLabelPaint)
        canvas.drawText(data.baseAmount, rightMargin - 15f, sy, sumValPaint)

        sy += 16f
        canvas.drawText("Festive Discount (50% OFF):", leftMargin + 235f, sy, sumLabelPaint)
        val sumDiscountPaint = Paint().apply {
            color = Color.parseColor("#059669")
            textSize = 9.5f
            typeface = sansBoldTypeface
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("- ${data.discountAmount}", rightMargin - 15f, sy, sumDiscountPaint)

        sy += 16f
        canvas.drawText("GST (18% Included):", leftMargin + 235f, sy, sumLabelPaint)
        canvas.drawText(data.gstAmount, rightMargin - 15f, sy, sumValPaint)

        sy += 10f
        canvas.drawLine(leftMargin + 235f, sy, rightMargin - 15f, sy, dividerPaint)
        sy += 16f

        // Grand Total Row
        val grandTotalLabelPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 11f
            typeface = sansBoldTypeface
        }
        val grandTotalValPaint = Paint().apply {
            color = Color.parseColor("#2563EB")
            textSize = 13f
            typeface = sansBoldTypeface
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("TOTAL PAID:", leftMargin + 235f, sy, grandTotalLabelPaint)
        canvas.drawText("₹${data.amount} INR", rightMargin - 15f, sy, grandTotalValPaint)

        y += 114f

        // 6. UPI AutoPay Mandate & Security Clause
        val clauseBoxRect = RectF(leftMargin, y, rightMargin, y + 62f)
        val clauseBgPaint = Paint().apply {
            color = Color.parseColor("#EFF6FF")
            style = Paint.Style.FILL
        }
        val clauseBorderPaint = Paint().apply {
            color = Color.parseColor("#BFDBFE")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(clauseBoxRect, 6f, 6f, clauseBgPaint)
        canvas.drawRoundRect(clauseBoxRect, 6f, 6f, clauseBorderPaint)

        val clauseTitlePaint = Paint().apply {
            color = Color.parseColor("#1E40AF")
            textSize = 9f
            typeface = sansBoldTypeface
        }
        canvas.drawText("🔒 PAYMENT SECURITY & AUTOPAY MANDATE POLICY", leftMargin + 12f, y + 16f, clauseTitlePaint)

        val clauseTextPaint = Paint().apply {
            color = Color.parseColor("#1E3A8A")
            textSize = 8.5f
            typeface = sansTypeface
        }
        canvas.drawText("• Processed via Razorpay 256-Bit SSL Encrypted Payment Gateway.", leftMargin + 12f, y + 29f, clauseTextPaint)
        canvas.drawText("• UPI AutoPay Mandate: Automatically renews at the end of each billing cycle with 0% fee.", leftMargin + 12f, y + 41f, clauseTextPaint)
        canvas.drawText("• You can cancel or pause your mandate anytime directly in your UPI App or via Profile > Settings.", leftMargin + 12f, y + 53f, clauseTextPaint)

        y += 82f

        // 7. Digital Stamp and Authorized Signatory
        val stampBoxRect = RectF(rightMargin - 180f, y, rightMargin, y + 64f)
        val stampBorderPaint = Paint().apply {
            color = Color.parseColor("#2563EB")
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }
        canvas.drawRoundRect(stampBoxRect, 6f, 6f, stampBorderPaint)

        val stampTitlePaint = Paint().apply {
            color = Color.parseColor("#2563EB")
            textSize = 8.5f
            typeface = sansBoldTypeface
            textAlign = Paint.Align.CENTER
        }
        val stampCenterX = rightMargin - 90f
        canvas.drawText("VASTAVIK COMPUTERS PVT LTD", stampCenterX, y + 16f, stampTitlePaint)
        canvas.drawText("✔ DIGITALLY VERIFIED", stampCenterX, y + 30f, stampTitlePaint)

        val signSubPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 7.5f
            typeface = sansTypeface
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Authorized Signatory", stampCenterX, y + 45f, signSubPaint)
        canvas.drawText("This is a computer-generated invoice.", stampCenterX, y + 56f, signSubPaint)

        // Left note: Helpdesk
        val helpPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 8.5f
            typeface = sansTypeface
        }
        canvas.drawText("Questions regarding this invoice? Contact us at:", leftMargin, y + 25f, helpPaint)

        val helpBoldPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 9f
            typeface = sansBoldTypeface
        }
        canvas.drawText("billing@vastaviklearning.com  |  +91 98765 43210", leftMargin, y + 40f, helpBoldPaint)

        // Bottom page note
        val footerPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 7.5f
            typeface = sansTypeface
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Thank you for learning with Vastavik! • Page 1 of 1", (leftMargin + rightMargin) / 2f, bottomMargin + 4f, footerPaint)

        document.finishPage(page)

        // Save PDF file
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            ?: context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.cacheDir

        val safeInvoiceId = data.invoiceNumber.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val file = File(dir, "Vastavik_Receipt_${safeInvoiceId}.pdf")

        try {
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
        } catch (e: Exception) {
            // Fallback to internal cache if external storage fails
            val fallbackFile = File(context.cacheDir, "Vastavik_Receipt_${safeInvoiceId}.pdf")
            FileOutputStream(fallbackFile).use { out ->
                document.writeTo(out)
            }
            document.close()
            return fallbackFile
        } finally {
            document.close()
        }

        return file
    }

    fun openPdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Receipt saved to Downloads: ${file.name}", Toast.LENGTH_LONG).show()
        }
    }
}
