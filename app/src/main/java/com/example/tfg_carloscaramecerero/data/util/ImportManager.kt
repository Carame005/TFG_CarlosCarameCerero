package com.example.tfg_carloscaramecerero.data.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import com.example.tfg_carloscaramecerero.data.local.AppDatabase
import com.example.tfg_carloscaramecerero.data.local.entity.BodyMeasurementEntity
import com.example.tfg_carloscaramecerero.data.local.entity.BodyWeightEntity
import com.example.tfg_carloscaramecerero.data.local.entity.ExerciseEntity
import com.example.tfg_carloscaramecerero.data.local.entity.ExerciseType
import com.example.tfg_carloscaramecerero.data.local.entity.FoodCatalogEntity
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.data.local.entity.RoutineEntity
import com.example.tfg_carloscaramecerero.data.local.entity.TrainingSessionEntity
import com.example.tfg_carloscaramecerero.data.local.entity.TrainingSetEntity
import java.io.File
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
            header.contains("sesión id orig") ||
                    (header.contains("ejercicio id") && header.contains("set n"))  -> CsvType.SESSIONS_DETAILED
            header.contains("pecho") || header.contains("cintura") ||
                    header.contains("cadera")                                       -> CsvType.MEASUREMENTS
            header.contains("peso")                                                -> CsvType.WEIGHTS
            header.contains("tipo comida") || (header.contains("descripción") &&
                    header.contains("día"))                                        -> CsvType.NUTRITION
            header.contains("gramos por defecto") && header.contains("nombre")    -> CsvType.FOOD_CATALOG
            header.contains("grupo muscular")                                      -> CsvType.EXERCISES
            header.contains("fecha creación") || (header.contains("nombre")
                    && header.contains("descripción") && !header.contains("grupo")) -> CsvType.ROUTINES
            else                                                                    -> CsvType.UNKNOWN
        }
    }

    enum class CsvType { WEIGHTS, MEASUREMENTS, NUTRITION, ROUTINES, EXERCISES, SESSIONS_DETAILED, FOOD_CATALOG, UNKNOWN }

    /**
     * Datos de una sesión completa parseada del CSV detallado.
     * [session] tiene id=0 listo para insertar; [sets] tienen sessionId=0 (se rellenará tras insertar la sesión).
     */
    data class ParsedSession(
        val originalSessionId: Long,
        val session: TrainingSessionEntity,
        val sets: List<TrainingSetEntity>
    )

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

    // ─── Medidas corporales ───────────────────────────────────────────────────

    /**
     * Parsea un CSV de medidas corporales exportado por [ExportManager.exportMeasurements].
     * Cabecera esperada: "Fecha;Pecho (cm);Cintura (cm);Cadera (cm);Bíceps (cm);Muslos (cm)"
     */
    fun parseMeasurementsCsv(csvContent: String): List<BodyMeasurementEntity> {
        return csvContent.lines()
            .drop(1)
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                try {
                    val parts = parseCsvLine(line)
                    if (parts.isEmpty()) return@mapNotNull null
                    val date = dateFormat.parse(parts[0].trim())?.time ?: return@mapNotNull null
                    BodyMeasurementEntity(
                        date    = date,
                        chest   = parts.getOrNull(1)?.trim()?.toDoubleOrNull(),
                        waist   = parts.getOrNull(2)?.trim()?.toDoubleOrNull(),
                        hips    = parts.getOrNull(3)?.trim()?.toDoubleOrNull(),
                        biceps  = parts.getOrNull(4)?.trim()?.toDoubleOrNull(),
                        thighs  = parts.getOrNull(5)?.trim()?.toDoubleOrNull()
                    )
                } catch (e: Exception) { null }
            }
    }

    // ─── Registro nutricional ─────────────────────────────────────────────────

    /**
     * Parsea un CSV del registro nutricional exportado por [ExportManager.exportNutrition].
     * Formato nuevo (5 col): "Día;Tipo comida;Tipo alimento;Descripción;Gramos"
     * Formato antiguo (8 col): "Día;Tipo comida;Descripción;Gramos;Calorías;…" (retrocompat.)
     *
     * La detección del formato se hace comprobando si la col 2 es "comida" / "bebida".
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

                    // Detectar si col 2 es foodType (nuevo formato) o descripción (formato antiguo)
                    val col2 = parts[2].trim()
                    val isNewFormat = col2.lowercase() == "comida" || col2.lowercase() == "bebida"

                    val foodType: String
                    val description: String
                    val grams: Int?
                    if (isNewFormat) {
                        // Nuevo: Día;Tipo comida;Tipo alimento;Descripción;Gramos
                        foodType = col2.lowercase()
                        description = parts.getOrNull(3)?.trim().orEmpty().ifBlank { return@mapNotNull null }
                        grams = parts.getOrNull(4)?.trim()?.toIntOrNull()
                    } else {
                        // Antiguo: Día;Tipo comida;Descripción;Gramos;Calorías;…
                        foodType = "comida"
                        description = col2.ifBlank { return@mapNotNull null }
                        grams = parts.getOrNull(3)?.trim()?.toIntOrNull()
                    }

                    FoodEntryEntity(
                        description = description,
                        mealType = mealType,
                        dayOfWeek = dayNumber,
                        foodType = foodType,
                        grams = grams
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
                    val createdAt = parts.getOrNull(2)?.trim()
                        ?.let { dateFormat.parse(it)?.time }
                        ?: System.currentTimeMillis()
                    RoutineEntity(name = name, description = description, createdAt = createdAt)
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

    // ─── Catálogo de alimentos ────────────────────────────────────────────────

    /**
     * Parsea un CSV del catálogo de alimentos exportado por [ExportManager.exportFoodCatalog].
     * Formato nuevo (3 col): "Nombre;Tipo;Gramos por defecto"
     * Formato antiguo (7 col): "Nombre;Tipo;Gramos por defecto;Calorías;…" (retrocompat.)
     */
    fun parseFoodCatalogCsv(csvContent: String): List<FoodCatalogEntity> {
        return csvContent.lines()
            .drop(1)
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                try {
                    val parts = parseCsvLine(line)
                    if (parts.isEmpty()) return@mapNotNull null
                    val name     = parts[0].trim().ifBlank { return@mapNotNull null }
                    val foodType = parts.getOrNull(1)?.trim()?.lowercase()
                        ?.let { if (it == "bebida") "bebida" else "comida" } ?: "comida"
                    val defaultGrams = parts.getOrNull(2)?.trim()?.toIntOrNull()
                    // Las columnas de macros (índice 3+) se ignoran en ambos formatos
                    FoodCatalogEntity(
                        name = name, foodType = foodType, defaultGrams = defaultGrams
                    )
                } catch (e: Exception) { null }
            }
    }

    // ─── Sesiones detalladas ──────────────────────────────────────────────────

    /**
     * Parsea el CSV exportado por [ExportManager.exportDetailedSessions].
     * Devuelve una lista de [ParsedSession] lista para insertar (ids = 0).
     */
    fun parseDetailedSessionsCsv(csvContent: String): List<ParsedSession> {
        val sessionMap     = linkedMapOf<Long, MutableList<TrainingSetEntity>>()
        val sessionHeaders = linkedMapOf<Long, TrainingSessionEntity>()

        csvContent.lines().drop(1).filter { it.isNotBlank() }.forEach { line ->
            try {
                val parts = parseCsvLine(line)
                if (parts.size < 6) return@forEach
                val origId    = parts[0].trim().toLongOrNull() ?: return@forEach
                val date      = dateFormat.parse(parts[1].trim())?.time  ?: return@forEach
                val routineId = parts[2].trim().toLongOrNull()
                val dur       = parts[3].trim().toIntOrNull() ?: 0
                val notes     = parts[4].trim().takeIf { it.isNotBlank() }
                val rest      = parts[5].trim().toIntOrNull() ?: 60

                if (!sessionHeaders.containsKey(origId)) {
                    sessionHeaders[origId] = TrainingSessionEntity(
                        id = 0, routineId = routineId, date = date,
                        durationMinutes = dur, notes = notes, restSeconds = rest
                    )
                    sessionMap[origId] = mutableListOf()
                }

                val exerciseId = parts.getOrNull(6)?.trim()?.toLongOrNull()
                if (exerciseId != null) {
                    sessionMap[origId]?.add(
                        TrainingSetEntity(
                            id          = 0,
                            sessionId   = 0, // se reemplaza al insertar
                            exerciseId  = exerciseId,
                            setNumber   = parts.getOrNull(7)?.trim()?.toIntOrNull()    ?: 0,
                            reps        = parts.getOrNull(8)?.trim()?.toIntOrNull()    ?: 0,
                            weight      = parts.getOrNull(9)?.trim()?.toDoubleOrNull() ?: 0.0,
                            durationSeconds = parts.getOrNull(10)?.trim()?.toIntOrNull()    ?: 0,
                            distanceKm  = parts.getOrNull(11)?.trim()?.toDoubleOrNull() ?: 0.0,
                            isCardio    = parts.getOrNull(12)?.trim()
                                              ?.equals("true", ignoreCase = true) ?: false,
                            restSeconds = parts.getOrNull(13)?.trim()?.toIntOrNull(),
                            // Col 14: isCompleted (retrocompat: ausente → false)
                            isCompleted = parts.getOrNull(14)?.trim()
                                              ?.equals("true", ignoreCase = true) ?: false
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        return sessionHeaders.map { (origId, session) ->
            ParsedSession(origId, session, sessionMap[origId] ?: emptyList())
        }
    }

    // ─── Copia de seguridad completa ──────────────────────────────────────────

    /**
     * Cierra la base de datos Room y sobrescribe su archivo con el contenido de [uri].
     * Borra los archivos WAL/SHM para evitar conflictos.
     * La app debe reiniciarse después de llamar a esta función.
     *
     * @return true si la restauración fue exitosa, false en caso de error.
     */
    fun restoreDatabase(context: Context, uri: Uri, appDatabase: AppDatabase): Boolean {
        return try {
            // execSQL está bloqueado por Room; usar query() para el checkpoint WAL
            try {
                appDatabase.openHelper.writableDatabase
                    .query("PRAGMA wal_checkpoint(FULL)", emptyArray<Any?>()).close()
            } catch (_: Exception) {}
            appDatabase.close()

            val dbFile = context.getDatabasePath("fitness_database")
            dbFile.parentFile?.mkdirs()

            context.contentResolver.openInputStream(uri)?.use { input ->
                dbFile.outputStream().use { output -> input.copyTo(output) }
            }

            // Eliminar WAL y SHM para evitar conflictos al reabrir
            File("${dbFile.path}-wal").delete()
            File("${dbFile.path}-shm").delete()
            true
        } catch (_: Exception) { false }
    }

    /**
     * Reinicia la aplicación matando el proceso y relanzando el intent de arranque.
     * Debe llamarse en el hilo principal después de una restauración exitosa de la BD.
     */
    fun restartApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) }
        context.startActivity(intent)
        Process.killProcess(Process.myPid())
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
