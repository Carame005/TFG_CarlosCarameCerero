package com.example.tfg_carloscaramecerero.data.util

import android.content.Context
import android.net.Uri
import com.example.tfg_carloscaramecerero.data.local.entity.BodyWeightEntity
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Utilidad para importar datos desde ficheros CSV.
 * Compatible con el formato generado por [ExportManager].
 *
 * Formatos soportados:
 * - Peso corporal: "Fecha,Peso (kg)" → "01/01/2025,75.5"
 * - Registro nutricional: "Día,Tipo comida,Descripción,Gramos,Calorías,Proteínas,Carbos,Grasas"
 */
object ImportManager {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

    // ─── Leer fichero ─────────────────────────────────────────────────────────

    /**
     * Lee el contenido de un fichero CSV a partir de su URI (selector del sistema).
     * @return Contenido del fichero como texto, o null si no se pudo leer.
     */
    fun readCsvFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            }
        } catch (e: Exception) {
            null
        }
    }

    // ─── Peso corporal ────────────────────────────────────────────────────────

    /**
     * Parsea un CSV de historial de peso en el formato exportado por [ExportManager.exportWeights].
     *
     * Ejemplo de fichero:
     * ```
     * Fecha,Peso (kg)
     * 15/05/2025,78.5
     * 16/05/2025,78.2
     * ```
     * @return Lista de entidades listas para insertar en Room.
     */
    fun parseWeightsCsv(csvContent: String): List<BodyWeightEntity> {
        return csvContent.lines()
            .drop(1) // saltar cabecera
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                try {
                    val parts = line.trim().split(",")
                    if (parts.size < 2) return@mapNotNull null
                    val date = dateFormat.parse(parts[0].trim())?.time
                        ?: return@mapNotNull null
                    val weight = parts[1].trim().toDoubleOrNull()
                        ?: return@mapNotNull null
                    BodyWeightEntity(weight = weight, date = date)
                } catch (e: Exception) {
                    null // fila malformada → ignorar
                }
            }
    }

    // ─── Registro nutricional ─────────────────────────────────────────────────

    /**
     * Parsea un CSV nutricional en el formato exportado por [ExportManager.exportNutrition].
     *
     * Ejemplo de fichero:
     * ```
     * Día,Tipo comida,Descripción,Gramos,Calorías,Proteínas,Carbos,Grasas
     * Lunes,desayuno,"Tostada con aguacate",80,,,,
     * Martes,almuerzo,"Pollo con arroz",300,450,35,50,10
     * ```
     * @return Lista de entidades listas para insertar en Room.
     */
    fun parseNutritionCsv(csvContent: String): List<FoodEntryEntity> {
        val dayNameToNumber = mapOf(
            "lunes" to 1, "martes" to 2, "miércoles" to 3,
            "jueves" to 4, "viernes" to 5, "sábado" to 6, "domingo" to 7
        )
        return csvContent.lines()
            .drop(1)
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                try {
                    val parts = parseCsvLine(line)
                    if (parts.size < 3) return@mapNotNull null

                    val dayNumber = dayNameToNumber[parts[0].trim().lowercase()]
                        ?: return@mapNotNull null
                    val mealType = parts[1].trim().ifBlank { return@mapNotNull null }
                    val description = parts[2].trim().ifBlank { return@mapNotNull null }
                    val grams = parts.getOrNull(3)?.trim()?.toIntOrNull()
                    val calories = parts.getOrNull(4)?.trim()?.toDoubleOrNull()
                    val protein = parts.getOrNull(5)?.trim()?.toDoubleOrNull()
                    val carbs = parts.getOrNull(6)?.trim()?.toDoubleOrNull()
                    val fat = parts.getOrNull(7)?.trim()?.toDoubleOrNull()

                    FoodEntryEntity(
                        description = description,
                        mealType = mealType,
                        dayOfWeek = dayNumber,
                        grams = grams,
                        calories = calories,
                        protein = protein,
                        carbs = carbs,
                        fat = fat,
                        aiAnalyzed = calories != null
                    )
                } catch (e: Exception) {
                    null
                }
            }
    }

    // ─── Validación ───────────────────────────────────────────────────────────

    /**
     * Detecta el tipo de CSV analizando la cabecera del fichero.
     */
    fun detectCsvType(csvContent: String): CsvType {
        val header = csvContent.lines().firstOrNull()?.lowercase() ?: return CsvType.UNKNOWN
        return when {
            header.contains("peso") -> CsvType.WEIGHTS
            header.contains("tipo comida") || header.contains("descripción") -> CsvType.NUTRITION
            else -> CsvType.UNKNOWN
        }
    }

    enum class CsvType { WEIGHTS, NUTRITION, UNKNOWN }

    // ─── Utilidades internas ──────────────────────────────────────────────────

    /**
     * Divide una línea CSV respetando los campos entre comillas dobles.
     * Ejemplo: `Lunes,almuerzo,"Pollo con arroz, tomate",300` → 4 partes.
     */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString())
        return result
    }
}

