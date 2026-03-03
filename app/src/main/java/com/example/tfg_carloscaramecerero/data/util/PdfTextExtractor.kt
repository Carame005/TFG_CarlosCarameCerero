package com.example.tfg_carloscaramecerero.data.util

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Extrae texto de un archivo PDF utilizando Android PdfRenderer.
 * Como PdfRenderer no extrae texto directamente (solo renderiza),
 * usamos una aproximación: leer el contenido raw del PDF para buscar strings de texto.
 */
object PdfTextExtractor {

    private const val MAX_CHARS = 4000

    /**
     * Intenta extraer texto de un PDF.
     * Usa lectura raw del archivo para encontrar texto plano embebido.
     */
    suspend fun extractText(filePath: String): String = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) return@withContext ""

            val rawText = extractTextFromRawPdf(file)
            if (rawText.isNotBlank()) {
                return@withContext rawText.take(MAX_CHARS)
            }

            // Si no se pudo extraer texto, indicar que es un PDF escaneado
            val pageCount = getPageCount(file)
            return@withContext "[Documento PDF con $pageCount página(s). No se pudo extraer texto (posiblemente escaneado/imagen).]"
        } catch (e: Exception) {
            return@withContext "[Error al leer el PDF: ${e.message}]"
        }
    }

    /**
     * Extrae texto directamente del contenido raw del PDF.
     * Busca secuencias de texto entre paréntesis (texto PDF estándar)
     * y secuencias BT/ET (bloques de texto).
     */
    private fun extractTextFromRawPdf(file: File): String {
        val bytes = file.readBytes()
        val content = String(bytes, Charsets.ISO_8859_1)

        val textParts = mutableListOf<String>()

        // Método 1: Extraer texto entre paréntesis dentro de bloques BT..ET
        val btEtRegex = Regex("BT(.*?)ET", RegexOption.DOT_MATCHES_ALL)
        val textInParens = Regex("\\(([^)]+)\\)")

        for (block in btEtRegex.findAll(content)) {
            for (match in textInParens.findAll(block.groupValues[1])) {
                val text = decodePdfString(match.groupValues[1])
                if (text.isNotBlank() && text.length > 1) {
                    textParts.add(text)
                }
            }
        }

        // Método 2: Si no encontramos texto en bloques BT/ET, buscar streams de texto
        if (textParts.isEmpty()) {
            val streamRegex = Regex("stream\r?\n(.*?)\r?\nendstream", RegexOption.DOT_MATCHES_ALL)
            for (stream in streamRegex.findAll(content)) {
                for (match in textInParens.findAll(stream.groupValues[1])) {
                    val text = decodePdfString(match.groupValues[1])
                    if (text.isNotBlank() && text.length > 2 && text.any { it.isLetter() }) {
                        textParts.add(text)
                    }
                }
            }
        }

        return textParts
            .joinToString(" ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Decodifica escapes básicos de strings PDF.
     */
    private fun decodePdfString(input: String): String {
        return input
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\(", "(")
            .replace("\\)", ")")
            .replace("\\\\", "\\")
    }

    private fun getPageCount(file: File): Int {
        return try {
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fd)
            val count = renderer.pageCount
            renderer.close()
            fd.close()
            count
        } catch (_: Exception) {
            0
        }
    }
}

