package com.marutipolymer.billing.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.marutipolymer.billing.models.InvoiceItemDetail
import java.io.File
import java.io.FileOutputStream

object PdfHelper {

    fun generateAndShareInvoicePdf(
        context: Context,
        invoiceNo: String,
        customerName: String,
        totalAmount: Double,
        subtotal: Double = totalAmount,
        discount: Double = 0.0,
        paidAmount: Double = 0.0,
        pendingAmount: Double = 0.0,
        items: List<InvoiceItemDetail>? = null
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size in Points
        val page = document.startPage(pageInfo)

        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Header
        paint.color = Color.rgb(46, 125, 50) // Dark Green Header Accent
        canvas.drawRect(0f, 0f, 595f, 70f, paint)

        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("MARUTI POLYMERS", 40f, 45f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("TAX INVOICE", 470f, 45f, paint)

        // Reset Paint for Body
        paint.color = Color.BLACK

        // Details Section
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("Invoice Details:", 40f, 100f, paint)
        paint.isFakeBoldText = false
        canvas.drawText("Invoice No: $invoiceNo", 40f, 120f, paint)
        canvas.drawText("Customer Name: $customerName", 40f, 140f, paint)
        canvas.drawText("Date: ${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())}", 380f, 120f, paint)

        // Table Header
        var yPos = 180f
        paint.color = Color.rgb(240, 240, 240)
        canvas.drawRect(40f, yPos - 15f, 555f, yPos + 15f, paint)

        paint.color = Color.BLACK
        paint.isFakeBoldText = true
        canvas.drawText("Item / Description", 50f, yPos + 5f, paint)
        canvas.drawText("Qty", 320f, yPos + 5f, paint)
        canvas.drawText("Rate", 400f, yPos + 5f, paint)
        canvas.drawText("Amount", 480f, yPos + 5f, paint)

        yPos += 30f
        paint.isFakeBoldText = false

        if (!items.isNullOrEmpty()) {
            for (item in items) {
                canvas.drawText(item.product_name, 50f, yPos, paint)
                canvas.drawText("${item.qty}", 320f, yPos, paint)
                canvas.drawText("Rs. ${item.rate}", 400f, yPos, paint)
                canvas.drawText("Rs. ${item.amount}", 480f, yPos, paint)
                yPos += 22f
            }
        } else {
            canvas.drawText("PVC Edge Band Tapes (Invoice Goods)", 50f, yPos, paint)
            canvas.drawText("1", 320f, yPos, paint)
            canvas.drawText("Rs. $totalAmount", 400f, yPos, paint)
            canvas.drawText("Rs. $totalAmount", 480f, yPos, paint)
            yPos += 22f
        }

        // Line separator
        yPos += 10f
        paint.strokeWidth = 1f
        canvas.drawLine(40f, yPos, 555f, yPos, paint)

        // Summary Calculations
        yPos += 25f
        canvas.drawText("Subtotal:", 380f, yPos, paint)
        canvas.drawText("Rs. $subtotal", 480f, yPos, paint)

        if (discount > 0) {
            yPos += 20f
            canvas.drawText("Discount:", 380f, yPos, paint)
            canvas.drawText("- Rs. $discount", 480f, yPos, paint)
        }

        yPos += 25f
        paint.isFakeBoldText = true
        paint.textSize = 14f
        canvas.drawText("Grand Total:", 380f, yPos, paint)
        canvas.drawText("Rs. $totalAmount", 480f, yPos, paint)

        yPos += 20f
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Amount Paid:", 380f, yPos, paint)
        canvas.drawText("Rs. $paidAmount", 480f, yPos, paint)

        yPos += 20f
        paint.isFakeBoldText = true
        canvas.drawText("Balance Due:", 380f, yPos, paint)
        canvas.drawText("Rs. $pendingAmount", 480f, yPos, paint)

        // Footer Note
        paint.color = Color.GRAY
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("Thank you for your business! - Maruti Polymers", 40f, 800f, paint)

        document.finishPage(page)

        // Replace slashes in filename to prevent FileNotFoundException!
        val safeFileName = "Invoice_${invoiceNo.replace('/', '_')}.pdf"
        val file = File(context.cacheDir, safeFileName)

        try {
            if (file.exists()) file.delete()
            document.writeTo(FileOutputStream(file))
            document.close()

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Tax Invoice $invoiceNo - Maruti Polymers")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Invoice PDF via"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to generate PDF: ${e.message}", Toast.LENGTH_LONG).show()
            document.close()
        }
    }
}
