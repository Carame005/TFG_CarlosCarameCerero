package com.example.tfg_carloscaramecerero.data.util

import android.content.Context
import android.net.Uri
import com.example.tfg_carloscaramecerero.data.local.entity.BodyWeightEntity
import com.example.tfg_carloscaramecerero.data.local.entity.ExerciseEntity
import com.example.tfg_carloscaramecerero.data.local.entity.ExerciseType
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.data.local.entity.RoutineEntity
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Utilidad para importar datos desde ficheros CSV.
 * Compatible con el formato generado por [ExportManager] (separador: `;`, BOM UTF-8).
 *
 * Formatos soportados:
 * - Peso corporal:     "Fecha;Peso (kg)"
 * - Registro nutricional: "Día;Tipo comida;Descripción;..."
 * - Rutinas:           "Nombre;Descripción;Fecha creación"
 * - Ejercicios:        "Nombre;Descripción;Grupo muscular;Tipo"
 */
object ImportManager {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

    // ─── Leer fichero ─────────────────────────────────────────────────────────

    fun readCsvFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                // Elimina BOM UTF-8 si está presente
                stream.bufferedReader(Charsets.UTF_8).readText().removePrefix("\uFEFF")
            }
        } catch (e: Exception) {
            null
        }
    }

    // ─── Detección automática de tipo ─────────────────────────────────────────

    fun detectCsvType(csvContent: String): CsvType {
        val header = csvContent.lines().firstOrNull()?.lowercase() ?: return CsvType.UNKNOWN
        return when {
            header.contains("peso")                                          -> CsvType.WEIGHTS
            header.contains("tipo comida") || header.contains("descripción") &&
                    header.contains("día")                                   -> CsvType.NUTRITION
            header.contains("grupo muscular")                               -> CsvType.EXERCISES
            header.contains("fecha creación") || (header.contains("nombre")
                    && header.contains("descripción") && !header.contains("grupo")) -> CsvType.ROUTINES
            else                                                             -> CsvType.UNKNOWN
        }
    }

    enum class CsvType { WEIGHTS, NUTRITION, ROUTINES, EXERCISES, UNKNOWN }

    // ─── Peso corporal ────────────────────────────────────────────────────────

    fun parseWeightsCsv(csvContent: String): List<BodyWeightEntity> {
        return csvContent.lines()
            .drop(1)
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                try {
                    val parts = parseCsvLine(line)
                    if (parts.size < 2) return@mapNotNull null
                    val date = dateFormat.parse(parts[0].trim())?.time ?: return@mapNotNull null
                    val weight = parts[1].trim().toDoubleOrNull() ?: return@mapNotNull null
                    BodyWeightEntity(weight = weight, date = date)
                } catch (e: Exception) { null }
            }
    }

    // ─── Registro nutricional ─────────────────────────────────────────────────

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
                    val grams    = parts.getOrNull(3)?.trim()?.toIntOrNull()
                    val calories = parts.getOrNull(4)?.trim()?.toDoubleOrNull()
                    val protein  = parts.getOrNull(5)?.trim()?.toDoubleOrNull()
                    val carbs    = parts.getOrNull(6)?.trim()?.toDoubleOrNull()
                    val fat      = parts.getOrNull(7)?.trim()?.toDoubleOrNull()
                    FoodEntryEntity(
                        description = description, mealType = mealType,
                        dayOfWeek = dayNumber, grams = grams, calories = calories,
                        protein = protein, carbs = carbs, fat = fat,
                        aiAnalyzed = calories != null
                    )
                } catch (e: Exception) { null }
            }
    }

    // ─── Rutinas ──────────────────────────────────────────────────────────────

    /**
     * Parsea un CSV de rutinas en el formato exportado por [ExportManager.exportRoutines].
     * Cabecera esperada: "Nombre;Descripción;Fecha creación"
     */
    fun parseRoutinesCsv(csvContent: String): List<RoutineEntity> {
        return csvContent.lines()
            .drop(1)
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                try {
                    val parts = parseCsvLine(line)
                    if (parts.isEmpty()) return@mapNotNull null
                    val name = parts[0].trim().ifBlank { return@mapNotNull null }
                    val description = parts.getOrNull(1)?.trim() ?: ""
                    RoutineEntity(name = name, description = description)
                } catch (e: Exception) { null }
            }
    }

    // ─── Ejercicios ───────────────────────────────────────────────────────────

    /**
     * Parsea un CSV de ejercicios en el formato exportado por [ExportManager.exportExercises].
     * Cabecera esperada: "Nombre;Descripción;Grupo muscular;Tipo"
     */
    fun parseExercisesCsv(csvContent: String): List<ExerciseEntity> {
        return csvContent.lines()
            .drop(1)
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                try {
                    val parts = parseCsvLine(line)
                    if (parts.size < 3) return@mapNotNull null
                    val name        = parts[0].trim().ifBlank { return@mapNotNull null }
                    val description = parts.getOrNull(1)?.trim() ?: ""
                    val muscleGroup = parts[2].trim().ifBlank { return@mapNotNull null }
                    val exerciseType = parts.getOrNull(3)?.trim()?.uppercase()
                        ?.let { if (it == ExerciseType.CARDIO.name) it else ExerciseType.STRENGTH.name }
                        ?: ExerciseType.STRENGTH.name
                    ExerciseEntity(name = name, description = description,
                        muscleGroup = muscleGroup, exerciseType = exerciseType)
                } catch (e: Exception) { null }
            }
    }

    // ─── Utilidades internas ──────────────────────────────────────────────────

    /**
     * Divide una línea CSV respetando campos entre comillas dobles.
     * Usa `;` como separador (compatible con ExportManager).
     */
    private fun parseCsvLine(line: String, separator: Char = ';'): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        for (char in line) {
            when {
                char == '"'                   -> inQuotes = !inQuotes
                char == separator && !inQuotes -> { result.add(current.toString()); current.clear() }
                else                          -> current.append(char)
            }
        }
        result.add(current.toString())
        return result
    }
}
