package pl.edu.pk.student.feature_share.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.util.Base64
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.edu.pk.student.core.domain.ShareableItem
import pl.edu.pk.student.core.domain.formatTimestamp
import pl.edu.pk.student.feature_medical_records.data.repository.MedicalRecordsRepository
import pl.edu.pk.student.feature_medical_records.domain.models.MedicalRecord
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

sealed class ShareFormat {
    object PlainText : ShareFormat()
    object Html : ShareFormat()
    object Pdf : ShareFormat()
}


sealed class ShareResult {
    data class Success(val intent: Intent) : ShareResult()
    data class Error(val message: String) : ShareResult()
}


@Singleton
class ShareRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val medicalRecordsRepository: MedicalRecordsRepository
) {
    suspend fun shareXRayWithDoctor(
        record: MedicalRecord,
        expiresInHours: Int = 48
    ): Result<String> {
        val storagePath = record.supabaseStoragePath

        return if (storagePath != null) {
            medicalRecordsRepository.supabaseStorageService.generateSignedUrl(
                path = storagePath,
                expiresInHours = expiresInHours
            )
        } else {
            Result.failure(Exception("No Supabase file found"))
        }
    }
    fun shareItems(
        items: List<ShareableItem>,
        format: ShareFormat,
        includeImages: Boolean = true
    ): ShareResult {
        return try {
            when (format) {
                ShareFormat.PlainText -> createTextShare(items)
                ShareFormat.Html -> createHtmlShare(items, includeImages)
                ShareFormat.Pdf -> createPdfShare(items, includeImages)
            }
        } catch (e: Exception) {
            ShareResult.Error("Failed to create share: ${e.message}")
        }
    }


    private fun createTextShare(items: List<ShareableItem>): ShareResult {
        val text = if (items.size == 1) {
            items.first().toPlainText()
        } else {
            buildString {
                appendLine("=" .repeat(50))
                appendLine("MEDICAL RECORDS EXPORT")
                appendLine("Total Records: ${items.size}")
                appendLine("=" .repeat(50))
                appendLine()

                items.forEachIndexed { index, item ->
                    appendLine("Record ${index + 1}/${items.size}")
                    appendLine("-" .repeat(50))
                    appendLine(item.toPlainText())
                    appendLine()
                }
            }
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Medical Records - MediMeow")
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return ShareResult.Success(Intent.createChooser(intent, "Share via"))
    }

    private fun createHtmlShare(items: List<ShareableItem>, includeImages: Boolean): ShareResult {
        val html = if (items.size == 1) {
            items.first().toHtml()
        } else {
            createMultipleRecordsHtml(items, includeImages)
        }

        val fileName = "medical_records_${System.currentTimeMillis()}.html"
        val file = File(context.cacheDir, fileName)

        file.writeText(html)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_SUBJECT, "Medical Records - MediMeow")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return ShareResult.Success(Intent.createChooser(intent, "Share via"))
    }

    private fun createPdfShare(items: List<ShareableItem>, includeImages: Boolean): ShareResult {
        val pdfDocument = PdfDocument()
        var pageNumber = 1

        items.forEach { item ->
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            drawRecordOnCanvas(canvas, item, includeImages)

            pdfDocument.finishPage(page)
            pageNumber++
        }

        val fileName = "medical_records_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)

        FileOutputStream(file).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, "Medical Records - MediMeow")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return ShareResult.Success(Intent.createChooser(intent, "Share via"))
    }

    private fun drawRecordOnCanvas(canvas: Canvas, item: ShareableItem, includeImages: Boolean) {
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        var yPosition = 50f
        val margin = 40f
        val pageWidth = canvas.width - 2 * margin

        canvas.drawRect(margin, yPosition, canvas.width - margin, yPosition + 60f, Paint().apply {
            color = Color.parseColor("#2d758d")
        })

        paint.color = Color.WHITE
        paint.textSize = 20f
        canvas.drawText("MEDICAL RECORD", margin + 20f, yPosition + 40f, paint)

        yPosition += 80f
        paint.color = Color.BLACK
        paint.textSize = 12f

        canvas.drawText("Title: ${item.title}", margin, yPosition, titlePaint)
        yPosition += 30f

        canvas.drawText("Date: ${formatTimestamp(item.timestamp)}", margin, yPosition, paint)
        yPosition += 30f

        if (item.content != null) {
            canvas.drawText("Content:", margin, yPosition, titlePaint)
            yPosition += 25f

            val lines = item.content!!.split("\n")
            lines.forEach { line ->
                if (yPosition < canvas.height - 100) {
                    canvas.drawText(line, margin, yPosition, paint)
                    yPosition += 20f
                }
            }
        }


        if (includeImages && item.imageUri != null) {
            yPosition += 20f
            canvas.drawText("Image attached", margin, yPosition, paint)

            try {
                val imageBytes = Base64.decode(item.imageUri, Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                if (bitmap != null) {
                    val maxWidth = pageWidth.toInt()
                    val maxHeight = 300

                    val scale = Math.min(
                        maxWidth.toFloat() / bitmap.width,
                        maxHeight.toFloat() / bitmap.height
                    )

                    val scaledWidth = (bitmap.width * scale).toInt()
                    val scaledHeight = (bitmap.height * scale).toInt()

                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)

                    yPosition += 20f
                    canvas.drawBitmap(scaledBitmap, margin, yPosition, null)

                    bitmap.recycle()
                    scaledBitmap.recycle()
                }
            } catch (e: Exception) {
                canvas.drawText("Image could not be loaded", margin, yPosition + 20f, paint)
            }
        }
    }

    private fun createMultipleRecordsHtml(items: List<ShareableItem>, includeImages: Boolean): String {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Medical Records Export</title>
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: linear-gradient(135deg, #74b7bf 0%, #2d758d 100%);
                        padding: 20px;
                        color: #333;
                    }
                    
                    .container {
                        max-width: 900px;
                        margin: 0 auto;
                    }
                    
                    .header {
                        background: white;
                        padding: 30px;
                        border-radius: 12px 12px 0 0;
                        text-align: center;
                        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
                    }
                    
                    .header h1 {
                        color: #2d758d;
                        font-size: 32px;
                        margin-bottom: 10px;
                    }
                    
                    .header .subtitle {
                        color: #666;
                        font-size: 16px;
                    }
                    
                    .record-card {
                        background: white;
                        margin-top: 20px;
                        border-radius: 12px;
                        overflow: hidden;
                        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
                    }
                    
                    .record-header {
                        background: linear-gradient(135deg, #2d758d 0%, #74b7bf 100%);
                        color: white;
                        padding: 20px;
                    }
                    
                    .record-header h2 {
                        font-size: 24px;
                        margin-bottom: 5px;
                    }
                    
                    .record-header .type {
                        font-size: 14px;
                        opacity: 0.9;
                    }
                    
                    .record-body {
                        padding: 20px;
                    }
                    
                    .metadata {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                        gap: 15px;
                        padding: 15px;
                        background: #f8f9fa;
                        border-radius: 8px;
                        margin-bottom: 20px;
                    }
                    
                    .meta-item {
                        display: flex;
                        flex-direction: column;
                    }
                    
                    .meta-label {
                        font-size: 12px;
                        color: #666;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                        margin-bottom: 5px;
                    }
                    
                    .meta-value {
                        font-size: 14px;
                        color: #333;
                        font-weight: 500;
                    }
                    
                    .content-section {
                        margin-top: 20px;
                    }
                    
                    .content-section h3 {
                        color: #2d758d;
                        font-size: 18px;
                        margin-bottom: 10px;
                        border-bottom: 2px solid #74b7bf;
                        padding-bottom: 5px;
                    }
                    
                    .content-text {
                        line-height: 1.8;
                        color: #444;
                        white-space: pre-wrap;
                    }
                    
                    .image-section {
                        margin-top: 20px;
                    }
                    
                    .image-section img {
                        max-width: 100%;
                        border-radius: 8px;
                        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
                    }
                    
                    .footer {
                        background: white;
                        padding: 20px;
                        text-align: center;
                        margin-top: 20px;
                        border-radius: 0 0 12px 12px;
                    }
                    
                    .footer-warning {
                        background: #fff3cd;
                        border: 1px solid #ffc107;
                        border-radius: 6px;
                        padding: 15px;
                        margin-bottom: 15px;
                        font-size: 14px;
                        color: #856404;
                    }
                    
                    @media print {
                        body {
                            background: white;
                            padding: 0;
                        }
                        
                        .record-card {
                            page-break-inside: avoid;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>MEDICAL RECORDS EXPORT</h1>
                        <div class="subtitle">Total Records: ${items.size}</div>
                    </div>
                    
                    ${items.mapIndexed { index, item -> """
                    <div class="record-card">
                        <div class="record-header">
                            <h2>Record ${index + 1}/${items.size}</h2>
                            <div class="type">${item.getShareableType().displayName}</div>
                        </div>
                        
                        <div class="record-body">
                            <div class="metadata">
                                <div class="meta-item">
                                    <span class="meta-label">Title</span>
                                    <span class="meta-value">${item.title}</span>
                                </div>
                                <div class="meta-item">
                                    <span class="meta-label">Date</span>
                                    <span class="meta-value">${formatTimestamp(item.timestamp)}</span>
                                </div>
                                <div class="meta-item">
                                    <span class="meta-label">Record ID</span>
                                    <span class="meta-value">${item.id.take(8)}...</span>
                                </div>
                            </div>
                            
                            ${if (item.content != null) """
                            <div class="content-section">
                                <h3>Content</h3>
                                <div class="content-text">${item.content}</div>
                            </div>
                            """ else ""}
                            
                            ${if (includeImages && item.imageUri != null) """
                            <div class="image-section">
                                <h3>Attached Image</h3>
                                <img src="data:image/jpeg;base64,${item.imageUri}" alt="Medical record image">
                            </div>
                            """ else ""}
                        </div>
                    </div>
                    """ }.joinToString("\n")}
                    
                    <div class="footer">
                        <div class="footer-warning">
                            ⚠️ <strong>Confidential Medical Records</strong><br>
                            These documents contain sensitive medical information. Handle with care and in accordance with privacy regulations.
                        </div>
                        <div style="font-size: 12px; color: #666;">
                            Generated by <strong style="color: #2d758d;">MediMeow</strong> Medical Records App<br>
                            ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}
                        </div>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
