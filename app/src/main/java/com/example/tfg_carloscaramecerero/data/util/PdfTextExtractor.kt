package com.example.tfg_carloscaramecerero.data.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.Inflater
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Extrae texto de un archivo PDF usando una estrategia en cascada:
 *
 *  1. **Extracción estructural** (rápida, fiel): lee los streams del PDF y descomprime
 *     los que usan FlateDecode. Ideal para analíticas médicas y cualquier PDF de texto.
 *
 *  2. **OCR con ML Kit** (fallback): renderiza cada página a un bitmap con fondo blanco
 *     y aplica reconocimiento de texto on-device. Cubre PDFs escaneados o con texto
 *     embebido como imágenes.
 */
object PdfTextExtractor {

    private const val MAX_CHARS = 8000
    private const val MAX_PAGES_OCR = 5
    private const val RENDER_SCALE = 3        // 3× resolución para OCR preciso
    private const val MIN_STRUCTURAL_LEN = 80 // umbral mínimo para considerar éxito

    // ──────────────────────────────────────────────────────────────────────────
    // API pública
    // ──────────────────────────────────────────────────────────────────────────

    suspend fun extractText(filePath: String): String = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists()) return@withContext "[Error: archivo PDF no encontrado.]"

        // Intento 1 – extracción directa de streams
        val structural = tryStructuralExtraction(file)
        if (structural.length >= MIN_STRUCTURAL_LEN) {
            return@withContext structural.take(MAX_CHARS)
        }

        // Intento 2 – OCR con PdfRenderer + ML Kit
        val ocr = tryOcrExtraction(file)
        if (ocr.isNotBlank()) return@withContext ocr

        // Sin resultado
        val pages = getPageCount(file)
        "[Documento PDF con $pages página(s). No se pudo extraer texto " +
        "(posiblemente imagen sin texto o formato incompat.).]"
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Método 1: extracción estructural (BT/ET + FlateDecode)
    // ──────────────────────────────────────────────────────────────────────────

    private fun tryStructuralExtraction(file: File): String {
        return try {
            val bytes = file.readBytes()
            val textParts = mutableListOf<String>()

            // Localizar objetos de stream con su cabecera
            val streamPattern = Regex(
                "<<([^>]*)>>\\s*stream\\r?\\n([\\s\\S]*?)\\r?\\nendstream",
                RegexOption.DOT_MATCHES_ALL
            )

            for (match in streamPattern.findAll(String(bytes, Charsets.ISO_8859_1))) {
                val header = match.groupValues[1]
                val rawBody = match.groupValues[2]

                // Solo nos interesan streams de contenido (page content o form XObject)
                // Ignorar streams de imagen (/Subtype /Image)
                if (header.contains("/Image", ignoreCase = true)) continue

                val streamText: String = when {
                    header.contains("FlateDecode", ignoreCase = true) -> {
                        val bodyBytes = rawBody.toByteArray(Charsets.ISO_8859_1)
                        val decompressed = inflate(bodyBytes) ?: continue
                        String(decompressed, Charsets.ISO_8859_1)
                    }
                    else -> rawBody
                }

                val extracted = parsePdfContentStream(streamText)
                if (extracted.isNotBlank()) textParts.add(extracted)
            }

            textParts.joinToString("\n")
                .replace(Regex("\\s{3,}"), "  ")
                .trim()
        } catch (_: Exception) { "" }
    }

    /**
     * Parsea operadores de texto PDF: Tj, TJ, ', " dentro de bloques BT…ET.
     */
    private fun parsePdfContentStream(content: String): String {
        val sb = StringBuilder()
        val btEt = Regex("BT([\\s\\S]*?)ET", RegexOption.DOT_MATCHES_ALL)
        // Literal string: (texto)  —  escapa \n \r \t \( \) \\
        val literalStr = Regex("\\(([^)\\\\]*(\\\\.[^)\\\\]*)*)\\)")
        // Array string para TJ: [(texto) n (texto) ...]
        val arrayStr = Regex("\\[([^\\]]+)\\]\\s*TJ")

        for (block in btEt.findAll(content)) {
            val inner = block.groupValues[1]

            // Extraer TJ arrays primero (preservan orden)
            for (arr in arrayStr.findAll(inner)) {
                for (str in literalStr.findAll(arr.groupValues[1])) {
                    val txt = decodePdfLiteralString(str.groupValues[1])
                    if (txt.isNotBlank()) sb.append(txt)
                }
                sb.append(' ')
            }

            // Luego Tj / ' / "
            val tjRegex = Regex("\\(([^)\\\\]*(\\\\.[^)\\\\]*)*)\\)\\s*[Tj'\"]")
            for (tj in tjRegex.findAll(inner)) {
                val txt = decodePdfLiteralString(tj.groupValues[1])
                if (txt.isNotBlank()) sb.append(txt).append(' ')
            }

            sb.append('\n')
        }
        return sb.toString().trim()
    }

    private fun decodePdfLiteralString(raw: String): String =
        raw.replace("\\n", "\n")
           .replace("\\r", "\r")
           .replace("\\t", "\t")
           .replace("\\(", "(")
           .replace("\\)", ")")
           .replace("\\\\", "\\")
           // Filtrar caracteres de control y no imprimibles
           .filter { it == '\n' || it == '\r' || it == '\t' || it >= ' ' }

    /** Descomprime datos zlib/deflate (FlateDecode en PDF). */
    private fun inflate(data: ByteArray): ByteArray? {
        // Los streams PDF FlateDecode usan zlib (con cabecera), no deflate puro.
        // Si falla con cabecera, intentamos sin ella (nowrap).
        for (nowrap in listOf(false, true)) {
            try {
                val inflater = Inflater(nowrap)
                inflater.setInput(data)
                val out = ByteArrayOutputStream(data.size * 3)
                val buf = ByteArray(8192)
                while (!inflater.finished()) {
                    val n = inflater.inflate(buf)
                    if (n == 0 && !inflater.finished()) break
                    out.write(buf, 0, n)
                }
                inflater.end()
                val result = out.toByteArray()
                if (result.isNotEmpty()) return result
            } catch (_: Exception) {}
        }
        return null
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Método 2: OCR con PdfRenderer + ML Kit
    // ──────────────────────────────────────────────────────────────────────────

    private suspend fun tryOcrExtraction(file: File): String =
        withContext(Dispatchers.IO) {
            try {
                val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fd)
                val sb = StringBuilder()
                val pagesToProcess = minOf(renderer.pageCount, MAX_PAGES_OCR)

                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                for (i in 0 until pagesToProcess) {
                    val page = renderer.openPage(i)
                    val w = page.width * RENDER_SCALE
                    val h = page.height * RENDER_SCALE

                    // FONDO BLANCO obligatorio – sin esto ML Kit falla en PDFs con alpha
                    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), Paint().apply { color = Color.WHITE })

                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    val pageText = recognizeTextSuspend(recognizer, bitmap)
                    bitmap.recycle()

                    if (pageText.isNotBlank()) {
                        if (sb.isNotEmpty()) sb.append("\n\n--- Página ${i + 1} ---\n")
                        sb.append(pageText)
                    }
                    if (sb.length >= MAX_CHARS) break
                }

                recognizer.close()
                renderer.close()
                fd.close()

                val result = sb.toString().trim()
                if (result.isBlank()) return@withContext ""
                if (renderer.pageCount > MAX_PAGES_OCR)
                    result.take(MAX_CHARS) + "\n[Nota: se procesaron $MAX_PAGES_OCR de ${renderer.pageCount} páginas.]"
                else
                    result.take(MAX_CHARS)
            } catch (_: Exception) { "" }
        }

    private suspend fun recognizeTextSuspend(
        recognizer: com.google.mlkit.vision.text.TextRecognizer,
        bitmap: Bitmap
    ): String = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { cont.resume(it.text) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Utilidades
    // ──────────────────────────────────────────────────────────────────────────

    private fun getPageCount(file: File): Int = try {
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        PdfRenderer(fd).use { it.pageCount }.also { fd.close() }
    } catch (_: Exception) { 0 }
}
