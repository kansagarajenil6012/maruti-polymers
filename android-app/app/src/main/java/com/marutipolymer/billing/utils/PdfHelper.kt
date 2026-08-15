package com.marutipolymer.billing.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.marutipolymer.billing.models.Customer
import java.io.File
import java.io.FileOutputStream

object PdfHelper {

    fun generateAndShareInvoicePdf(
        context: Context,
        invoiceNo: String,
        customerName: String,
        totalAmount: Double
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size in Points
        val page = document.startPage(pageInfo)

        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Draw Header
        paint.color = Color.BLACK
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("MARUTI POLYMERS", 200f, 50f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("TAX INVOICE", 250f, 80f, paint)

        // Draw Details
        paint.textSize = 12f
        canvas.drawText("Invoice No: $invoiceNo", 50f, 120f, paint)
        canvas.drawText("Customer: $customerName", 50f, 140f, paint)
        
        // Draw Table Header
        paint.isFakeBoldText = true
        canvas.drawText("Description", 50f, 180f, paint)
        canvas.drawText("Total Amount", 450f, 180f, paint)
        canvas.drawLine(50f, 190f, 545f, 190f, paint)
        
        // Draw Content (Simplified for MVP)
        paint.isFakeBoldText = false
        canvas.drawText("Goods as per Invoice", 50f, 210f, paint)
        canvas.drawText("Rs. $totalAmount", 450f, 210f, paint)

        // Draw Footer
        canvas.drawLine(50f, 230f, 545f, 230f, paint)
        paint.isFakeBoldText = true
        canvas.drawText("Grand Total: Rs. $totalAmount", 380f, 250f, paint)

        document.finishPage(page)

        // Save PDF to Cache
        val file = File(context.cacheDir, "Invoice_$invoiceNo.pdf")
        try {
            document.writeTo(FileOutputStream(file))
            document.close()
            
            // Share Intent
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Invoice $invoiceNo")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Invoice via"))
            
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
        }
    }
}
